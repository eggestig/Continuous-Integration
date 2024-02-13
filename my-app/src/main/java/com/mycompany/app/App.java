package com.mycompany.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.kohsuke.github.GHCommitState;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.cdimascio.dotenv.Dotenv;


/** 
 Skeleton of a ContinuousIntegrationServer which acts as webhook
 See the Jetty documentation for API documentation of those classes.
*/
public class App extends AbstractHandler
{   
    private JsonNode jsonNode;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String CloneDirectoryPath = System.getProperty("user.dir") + "/../tempRepo"; // '/my-app/../tempRepo'
    private static final String BUILD_SUCCESS = "BUILD SUCCESS";
    
    private static final String CONTEXT_BUILD = "BUILD";
    private static final String CONTEXT_TEST = "TEST";
    private static final String CONTEXT_ASSEMBLY = "ASSEMBLY";
    
    private static String buildLog = "";
    
    /**
     * Handles the incoming HTTP request.
     * 
     * 
     * @param target The target of the request.
     * @param baseRequest The base request object.
     * @param request The HTTP servlet request.
     * @param response The HTTP servlet response.
     * @throws IOException
     * @throws ServletException
     */
    public void handle(String target,
            Request baseRequest,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException, ServletException {

        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);

        System.out.println(target);

        // Check if the incoming request is a GitHub push event
        String eventType = request.getHeader("X-GitHub-Event");
        if ("push".equals(eventType)) {

            String payload = request.getReader().lines().collect(Collectors.joining());

            jsonNode = objectMapper.readTree(payload);

            // Here we can extract the needed information from the payload
            String repositoryName = jsonNode.path("repository").path("name").asText();
            String pushedBranch = jsonNode.path("ref").asText();
            String repoURI = jsonNode.path("repository").path("clone_url").asText();
            String timestamp = jsonNode.path("head_commit").path("timestamp").asText();
            String commitID = jsonNode.path("after").asText();

            System.out.println("Received GitHub push event for repository: " + repositoryName +
                    ", pushed to branch: " + pushedBranch);

            System.out.println(repoURI + "/" + pushedBranch);

            try {
                
                // Set initial pending statuses
                setCommitStatus(jsonNode, GHCommitState.PENDING, "BUILDING...", CONTEXT_BUILD);
                setCommitStatus(jsonNode, GHCommitState.PENDING, "ASSEMBLING...", CONTEXT_ASSEMBLY);
                setCommitStatus(jsonNode, GHCommitState.PENDING, "TESTING...", CONTEXT_TEST);

                //Clone repo to local directory
                cloneRepo(repoURI, pushedBranch);

                // BUILD
                GHCommitState packageStatus = projectBuilder(CloneDirectoryPath); // Returns 'BUILD SUCCESS' or 'BUILD FAILURE'
                setCommitStatus(jsonNode, packageStatus, "BUILD: " + packageStatus, CONTEXT_BUILD);

                // TEST
                GHCommitState testStatus = projectTester(CloneDirectoryPath);
                setCommitStatus(jsonNode, testStatus, "TEST: " + testStatus, CONTEXT_TEST);

                // ASSEMBLE
                GHCommitState assembleStatus = projectAssembler(CloneDirectoryPath);

                // Set commit status
                setCommitStatus(jsonNode, assembleStatus, "ASSEMBLE: " + testStatus, CONTEXT_ASSEMBLY);

                //Write the JSON content to a file
                ReserveHistory.writeJsonToHtml(commitID,timestamp,buildLog);
                
            } catch (GitAPIException | IOException e) {
                System.out.println("Exception occurred while cloning repo");
                e.printStackTrace();
            }
        }

        PrintWriter out = response.getWriter();

        ReserveHistory.generateHtmlContent(out);
        if ("/".equals(target)) {
        // Check the requested path
            ReserveHistory.showAllCommit();
        }else if (target.startsWith("/commit")) {
            // Extract file name from the path
            String fileName = target.substring("/commit".length());
            ReserveHistory.serveCommitContent(fileName);
        }else ReserveHistory.generateErrorPage();

        response.getWriter().println("CI job done");
        
    }
    /**
     * Clones the repository specified by URI and branch to a local directory.
     * @param URI
     * @param branch
     * @throws GitAPIException
     * @throws IOException
     */
    public static void cloneRepo(String URI, String branch) throws GitAPIException, IOException {
        System.out.println("Deleting directory " + URI + "...");
        FileUtils.deleteDirectory(new File(CloneDirectoryPath));
        System.out.println("Directory deleted!");
        System.out.println("Cloning " + URI + " into " + CloneDirectoryPath + "...");

        Git.cloneRepository()
                .setURI(URI)
                .setBranch(branch)
                .setDirectory(Paths.get(CloneDirectoryPath).toFile())
                .call();
        System.out.println("Cloning completed!");
        System.out.println("Creating skeleton .env file");

        // Create file
        try {
            File myObj = new File(CloneDirectoryPath + "/my-app/.env");
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred when cloning the repo");
            e.printStackTrace();
        }
        // Write to created file
        String filePath = System.getProperty("user.dir");
        Dotenv dotenv = Dotenv.configure()
            .directory(filePath)
            .load();

        try {
            FileWriter myWriter = new FileWriter(CloneDirectoryPath + "/my-app/.env");
            myWriter.write("AUTH_TOKEN_ENV=\"" + dotenv.get("AUTH_TOKEN_ENV") + "\"\nPORT=0");
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred when cloning the repo.");
            e.printStackTrace();
        }
    }
    /**
     * Builds the Maven project located at the specified path.
     * @param path The path to the Maven project.
     * @return The build status of the project.
     */
    public static GHCommitState projectBuilder(String path) {

        GHCommitState buildResult = GHCommitState.FAILURE;

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(new String[] { "mvn", "clean", "package" });
            processBuilder.directory(new java.io.File(path + "/my-app"));
            Process commandRunner = processBuilder.start();

            String output = captureOutput(commandRunner.getInputStream());
            buildLog = output;
            int exitCode = commandRunner.waitFor();

            System.out.println("Captured Output:\n" + output);
            System.out.println("Exit Code: " + exitCode);

            if (output.contains(BUILD_SUCCESS)) {
                buildResult = GHCommitState.SUCCESS;
            }

        } catch (IOException | InterruptedException e) {
            System.out.println("Something went wrong when building the project");
            e.printStackTrace();
        }

        System.out.println(buildResult);
        return buildResult;
    }

    /**
     * Captures the output from an input stream and returns it as a string.
     * 
     * @param inputStream The input stream to capture output from.
     * @return The captured output as a string.
     * @throws IOException If an I/O error occurs.
     */
    public static String captureOutput(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder output = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }
            return output.toString();
        }
    }

    /**
     * Sets the commit status on GitHub.
     * 
     * @param payload     The JSON payload received from the webhook.
     * @param state       The state of the commit (e.g., pending, success, failure).
     * @param description The description of the commit status.
     * @param context     The context of the commit status (e.g., build, test, assembly).
     * @return True if the commit status was successfully updated, false otherwise.
     */
    public static boolean setCommitStatus(JsonNode payload, GHCommitState state, String description, String context) {
        try {
            String filePath = System.getProperty("user.dir"); // Sometimes need to add 'my-app' to the path
            // String filePath = System.getProperty("user.dir") + File.separator + "my-app";
            Dotenv dotenv = Dotenv.configure()
            .directory(filePath)
            .load();

            GitHub github = getGithub(dotenv.get("AUTH_TOKEN_ENV"));

            String owner = payload.path("repository")
                    .path("owner")
                    .path("name")
                    .asText();
            String repoName = payload.path("repository")
                    .path("name")
                    .asText();

            String sha = payload.path("after").asText();

            String targetUrl = payload.path("head_commit").path("url").asText();

            GHRepository repository = github.getRepository(owner + "/" + repoName);
            repository.createCommitStatus(sha,
                    state,
                    targetUrl,
                    description,
                    context);

            System.out.println("Commit status updated successfully: '" + description + "'");
            System.out.println(repository.getLastCommitStatus(sha));
            System.out.flush();
            return true;
        } catch (Exception e) {
            System.out.println("Commit status update failed.");
            e.printStackTrace();
            System.out.flush();
            return false;
        }
    }

    /**
     * Retrieves a GitHub instance using the provided authentication token.
     * 
     * @param token The GitHub authentication token.
     * @return A GitHub instance.
     */
    private static GitHub getGithub(final String token) {
        try {
            return GitHub.connectUsingOAuth("https://api.github.com", token);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Tests the Maven project by running the tests.
     * 
     * @param path The path to the Maven project.
     * @return The test status of the project.
     */
    public static GHCommitState projectTester(String path) {

        GHCommitState buildResult = GHCommitState.FAILURE;

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(new String[] { "mvn", "clean", "test" });
            processBuilder.directory(new java.io.File(path + "/my-app"));
            Process commandRunner = processBuilder.start();

            String output = captureOutput(commandRunner.getInputStream());
            int exitCode = commandRunner.waitFor();

            System.out.println("Captured Output:\n" + output);
            System.out.println("Exit Code: " + exitCode);

            // Build fails if atleast one test fails
            if (output.contains(BUILD_SUCCESS)) {
                buildResult = GHCommitState.SUCCESS;

            }

        } catch (IOException | InterruptedException e) {
            System.out.println("Something went wrong when testing the project");
            e.printStackTrace();
        }

        System.out.println(buildResult);
        return buildResult;
    }

    /**
     * Assembles the Maven project.
     * 
     * @param path The path to the Maven project.
     * @return The assembly status of the project.
     */
    public static GHCommitState projectAssembler(String path) {

        GHCommitState buildResult = GHCommitState.FAILURE;

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(new String[] { "mvn", "assembly:assembly", "--batch-mode", "--update-snapshots", "verify" });
            processBuilder.directory(new java.io.File(path + "/my-app"));
            Process commandRunner = processBuilder.start();

            String output = captureOutput(commandRunner.getInputStream());
            int exitCode = commandRunner.waitFor();

            System.out.println("Captured Output:\n" + output);
            System.out.println("Exit Code: " + exitCode);

            // Build fails if atleast one test fails
            if (output.contains(BUILD_SUCCESS)) {
                buildResult = GHCommitState.SUCCESS;
            }

        } catch (IOException | InterruptedException e) {
            System.out.println("Something went wrong when assembling the project");
            e.printStackTrace();
        }

        System.out.println("ASSEMBLE RESULT: " + buildResult);
        return buildResult;
    }

    /**
     * Starts the Continuous Integration (CI) server.
     * 
     * @param args The command-line arguments.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        System.out.println("Waiting for a push event to trigger the Github webHook...");
        int port = 0;
        Dotenv dotenv = Dotenv.load();
        System.out.println(dotenv.get("PORT"));
        port = dotenv.get("PORT").compareTo("8080") == 0 ? 8080 : 0;

        System.out.println("Try to run on port: " + port + " from directory: " + System.getProperty("user.dir"));
        
        Server server = new Server(port);
        server.setHandler(new App()); 
        server.start();
        server.join();
    }
}
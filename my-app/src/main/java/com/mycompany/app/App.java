package com.mycompany.app;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig.Host;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.Session;

/**
 * Skeleton of a ContinuousIntegrationServer which acts as webhook
 * See the Jetty documentation for API documentation of those classes.
 */
public class App extends AbstractHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String CloneDirectoryPath = "../" + System.getProperty("user.dir") + "/../tempRepo"; // '/my-app/../tempRepo'

    private JsonNode jsonNode;

    public static void cloneRepo(String URI, String branch) throws GitAPIException, IOException {
        System.out.println("Deleting directory " + URI + "...");
        FileUtils.deleteDirectory(new File(CloneDirectoryPath));

        System.out.println("Cloning " + URI + " into " + URI);
        Git.cloneRepository()
            .setURI(URI)
            .setBranch(branch)
            .setDirectory(Paths.get(CloneDirectoryPath).toFile())
            .call();        
        System.out.println("Completed Cloning");
    }

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
            System.out.println("Received GitHub push event for repository: " + repositoryName +
                    ", pushed to branch: " + pushedBranch);

            // System.out.println("Received GitHub push event: " + payload);
            setCommitStatus(jsonNode);
        }

        // Respond with a 200 OK status
        // here you do all the continuous integration tasks
        // for example
        // 1st clone your repository
        try {
            if ("push".equals(eventType)) {
                String repoURI = jsonNode.path("repository").path("clone_url").asText();
                System.out.println(repoURI);
                System.out.flush();
                cloneRepo(repoURI, "assessment");
            }
        } catch(GitAPIException e) {
            System.out.println("Exception occurred while cloning repo");
            e.printStackTrace();
        }
        // 2nd compile the code with mvn
        response.setStatus(HttpServletResponse.SC_OK);

        projectBuilder(System.getProperty("user.dir"));

        response.getWriter().println("CI job done");

    }

    private static GitHub getGithub(final String token) {
        try {
            return GitHub.connectUsingOAuth("https://api.github.com", token);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setCommitStatus(JsonNode payload) {
        try {
            // Make sure to run before: "export AUTH_TOKEN="<insert_tok>""
            String token = System.getenv("AUTH_TOKEN");
            GitHub github = getGithub(token);

            String owner = payload.path("repository")
                            .path("owner")
                            .path("name")
                            .asText();
            String repoName = payload.path("repository")
                            .path("name")
                            .asText();

            String sha1 = payload.path("after").asText();

            String description = "The build succeeded!";

            String targetUrl = payload.path("head_commit").path("url").asText();

            GHRepository repository = github.getRepository(owner + "/" + repoName);
            repository.createCommitStatus(sha1, GHCommitState.SUCCESS, targetUrl, description);

            System.out.println("Commit status updated successfully.");
        } catch (Exception e) {
            System.out.println("Commit status update failed.");
            e.printStackTrace();
        }
    }

    public static String projectBuilder(String path){
       
        String buildResult = "";

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(new String[]{"mvn", "package"}); 
            processBuilder.directory(new java.io.File(path));
            Process commandRunner = processBuilder.start();

            String output = captureOutput(commandRunner.getInputStream());
            int exitCode = commandRunner.waitFor();

            System.out.println("Captured Output:\n" + output);
            System.out.println("Exit Code: " + exitCode);

            if (output.contains("BUILD SUCCESS")) {
                buildResult = "SUCCESS";

            } else if (output.contains("BUILD FAILURE")){
                buildResult = "FAILURE";
            }

        } catch (IOException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println(buildResult);
        return buildResult;

    }

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
 
    // used to start the CI server in command line
    public static void main(String[] args) throws Exception {
        System.out.println("Hello World!");
        Server server = new Server(8080);
        server.setHandler(new App());
        server.start();
        server.join();
    }
}
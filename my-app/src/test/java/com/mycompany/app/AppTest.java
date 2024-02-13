package com.mycompany.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Server;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.Test;
import org.kohsuke.github.GHCommitState;

import java.nio.file.Files;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.cdimascio.dotenv.Dotenv;


/**
 * Unit test for simple App.
 */
public class AppTest {
    private final String URI = "https://github.com/eggestig/Continuous-Integration.git";
    private final String CloneDirectoryPath = System.getProperty("user.dir") + "/../tempRepo"; // '/my-app/../tempRepo'
    private final String Branch = "assessment";

    private final String BuildSuccessDirectoryPath = System.getProperty("user.dir") + "/../testBuildSuccess";
    private final String BuildFailDirectoryPath = System.getProperty("user.dir") + "/../testBuildFailure";

    /**
     * Test setting commit status for a valid payload.
     * 
     * @throws IOException
     */
    @Test
    public void test_setCommitStatus_returns_true_for_valid_payload() throws IOException {
        String filePath = System.getProperty("user.dir") + File.separator
                + "src/test/java/com/mycompany/app/testJson.txt";
        String payload = new String(Files.readAllBytes(Paths.get(filePath)));
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(payload);

        assertTrue(App.setCommitStatus(jsonNode, GHCommitState.SUCCESS, "TEST", "TEST"));
    }

    /**
     * Test authentication token retrieval for a valid token.
     * 
     * @throws Exception
     */
    @Test
    public void test_auth_token_returns_true_for_valid_token() throws Exception {
        String filePath = System.getProperty("user.dir");
        Dotenv dotenv = Dotenv.configure()
                .directory(filePath)
                .load();

        String token = dotenv.get("AUTH_TOKEN_ENV");
        assertTrue(token != null);
    }

    /**
     * Test cloning a repository.
     * 
     * @throws GitAPIException
     * @throws IOException
     */
    @Test
    public void testClonedRepo() throws GitAPIException, IOException {
        App.cloneRepo(URI, Branch);

        FileRepositoryBuilder repo = new FileRepositoryBuilder()
                .findGitDir(new File(CloneDirectoryPath + "/.git"));

        assertTrue("Cloned Git Repo exists", repo.getGitDir() != null);
    }

    /**
     * Test connection to server.
     * 
     * @throws Exception
     */
    @Test
    public void testConnection() throws Exception {
        Random random = new Random();
        int port = 49152 + random.nextInt(1000); // 49152 - 50151
        // Start server
        Server server = new Server(port);
        server.setHandler(new App());
        server.start();

        // Test connection
        HttpURLConnection http = (HttpURLConnection) new URL("http://localhost:" + port + "/").openConnection();
        http.connect();
        assertTrue("Response Code", http.getResponseCode() == HttpStatus.OK_200);

        // Stop server
        server.stop();
        server.join();
    }

    /**
     * Test the captureOutput method to ensure correct console output capture.
     * 
     * @throws IOException
     * @throws GitAPIException
     */
    @Test
    public void testCaptureOutput() throws IOException, GitAPIException {
        App.cloneRepo(URI, Branch);
        String input = "[INFO] Scanning for projects...\n[INFO] BUILD FAILURE\n[INFO] Total time:  0.033 s";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));

        String output = App.captureOutput(inputStream);

        String expectedOutput = "[INFO] Scanning for projects..." + System.lineSeparator() +
                "[INFO] BUILD FAILURE" + System.lineSeparator() +
                "[INFO] Total time:  0.033 s" + System.lineSeparator();

        assertEquals(expectedOutput, output);
    }

    /**
     * Test checking out a specific branch of a cloned repository.
     * 
     * @throws GitAPIException
     * @throws IOException
     */
    @Test
    public void testClonedRepoBranch() throws GitAPIException, IOException {
        App.cloneRepo(URI, Branch);

        Repository repo = new FileRepositoryBuilder()
                .setGitDir(new File(CloneDirectoryPath + "/.git"))
                .readEnvironment()
                .findGitDir()
                .build();

        try (Git git = new Git(repo)) {
            assertTrue("Cloned repo branch exists, and is valid",
                    Branch.equals(git.getRepository().getBranch()));
        }
    }

    /**
     * Tests the projectBuilder method on a buildable maven skeleton project
     * found under Continuos_Integration/testBuildSuccess
     */
    @Test
    public void testProjectBuilderSuccess() {
        GHCommitState buildResult = App.projectBuilder(BuildSuccessDirectoryPath);
        assertEquals("Test successful build", buildResult, GHCommitState.SUCCESS);
    }

    /**
     * Tests the projectBuilder method on a none buildable maven skeleton project
     * found under Continuos_Integration/testBuildFailure
     */
    @Test
    public void testProjectBuilderFailure() {
        GHCommitState buildResult = App.projectBuilder(BuildFailDirectoryPath);
        assertEquals("Test unsuccessful build", buildResult, GHCommitState.FAILURE);
    }

    /**
     * Tests the projectTester method on a buildable maven skeleton project
     * found under Continuos_Integration/testBuildSuccess
     */
    @Test
    public void testProjectTesterSuccess() {
        GHCommitState testResult = App.projectTester(BuildSuccessDirectoryPath);
        assertEquals("Test successful tester", testResult, GHCommitState.SUCCESS);
    }

    /**
     * Tests the projectTester method on a none buildable maven skeleton project
     * found under Continuos_Integration/testBuildFailure
     */
    @Test
    public void testProjectTesterFailure() {
        GHCommitState testResult = App.projectTester(BuildFailDirectoryPath);
        assertEquals("Test unsuccessful tester", testResult, GHCommitState.FAILURE);
    }

    /**
     * Tests the projectAssembler method on a buildable maven skeleton project
     * found under Continuos_Integration/testBuildSuccess
     */
    @Test
    public void testProjectAssemblerSuccess() {
        GHCommitState assembleResult = App.projectAssembler(BuildSuccessDirectoryPath);
        System.out.println("assembleResult(SuccessMethod): " + assembleResult);
        assertEquals("Test successful assembler", assembleResult, GHCommitState.SUCCESS);
    }

    /**
     * Tests the projectAssembler method on a none buildable maven skeleton project
     * found under Continuos_Integration/testBuildFailure
     */
    @Test
    public void testProjectAssemblerFailure() {
        GHCommitState assembleResult = App.projectAssembler(BuildFailDirectoryPath);
        System.out.println("assembleResult(failureMethod): " + assembleResult);
        assertEquals("Test unsuccessful assembler", assembleResult, GHCommitState.FAILURE);
    }
}

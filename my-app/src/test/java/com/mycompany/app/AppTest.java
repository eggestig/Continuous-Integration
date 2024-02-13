package com.mycompany.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.io.File;
import java.nio.file.Paths;
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
    private static final String BUILD_SUCCESS = "BUILD SUCCESS";
    private static final String BUILD_FAILURE = "BUILD FAILURE";

    /**
     * Test commit status for pre-defined JSON Node
     */
    @Test
    public void test_setCommitStatus_returns_true_for_valid_payload() throws IOException {
        String filePath = System.getProperty("user.dir") + File.separator + "src/test/java/com/mycompany/app/testJson.txt";
        String payload = new String(Files.readAllBytes(Paths.get(filePath)));
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(payload);

        assertTrue(App.setCommitStatus(jsonNode, "SUCCESS"));
    }

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
     * Test correctly cloned repo
     */
    @Test
    public void testClonedRepo() throws GitAPIException, IOException {

        App.cloneRepo(URI, Branch);

        File repoDir = Paths.get(CloneDirectoryPath).toFile();
        try (Git git = Git.open(repoDir)) {
            Repository repository = git.getRepository();
            assertTrue("Cloned Git Repo exists", repository.getObjectDatabase().exists());
        }
        
}

    /**
     * Test connection to server
     */
    @Test
    public void testConnection() throws Exception
    {
        Random random = new Random();
        int port = 49152 + random.nextInt(1000); //49152 - 50151
        // Start server
        Server server = new Server(port);
        server.setHandler(new App()); 
        server.start();

        // Test connection
        HttpURLConnection http = (HttpURLConnection)new URL("http://localhost:" + port + "/").openConnection();
        http.connect();
        assertTrue("Response Code", http.getResponseCode() == HttpStatus.OK_200);

        // Stop server
        server.stop();
        server.join();
    }

    /**
     * Test that the helper method captureOutput
     * captures the correct console output for a given input.
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
     * Test correctly cloned repo branch
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
        String buildResult = App.projectBuilder(BuildSuccessDirectoryPath);
        assertEquals("Test successful build", buildResult, BUILD_SUCCESS);
    }

     /**
     * Tests the projectBuilder method on a none buildable maven skeleton project 
     * found under Continuos_Integration/testBuildFailure
     */
    @Test
    public void testProjectBuilderFailure() {
        String buildResult = App.projectBuilder(BuildFailDirectoryPath);
        assertEquals("Test unsuccessful build", buildResult, BUILD_FAILURE);
    }
}

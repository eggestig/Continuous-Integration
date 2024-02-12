package com.mycompany.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Server;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.URIish;
import org.junit.Test;


/**
 * Unit test for simple App.
 */
public class AppTest 
{
    private final String URI = "https://github.com/eggestig/Continuous-Integration.git";
    private final String CloneDirectoryPath = System.getProperty("user.dir") + "/../tempRepo"; // '/my-app/../tempRepo'
    private final String Branch = "assessment";
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue()
    {
        assertTrue( true );
    }
    
    /**
     * Test correctly cloned repo
     */
    @Test
    public void testClonedRepo() throws GitAPIException, IOException {
        App.cloneRepo(URI, Branch);

        FileRepositoryBuilder repo = new FileRepositoryBuilder()
            .findGitDir(new File(CloneDirectoryPath + "/.git"));

        assertTrue("Cloned Git Repo exists", repo.getGitDir() != null);
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

        Git git = new Git(repo);

        assertTrue("Cloned repo branch exists, and is valid", Branch.equals(git.getRepository().getBranch()));
    }
}

package com.mycompany.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Server;
import org.junit.Test; 


/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue()
    {
        assertTrue( true );
    }
    /**
     * Test connection to server
     */
    @Test
    public void testConnection() throws Exception
    {
        // Start server
        Server server = new Server(8080);
        server.setHandler(new App()); 
        server.start();

        // Test connection
        HttpURLConnection http = (HttpURLConnection)new URL("http://localhost:8080/").openConnection();
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
    public void testCaptureOutput() throws IOException {

        String input = "[INFO] Scanning for projects...\n[INFO] BUILD FAILURE\n[INFO] Total time:  0.033 s";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));

        String output = App.captureOutput(inputStream);

        String expectedOutput = "[INFO] Scanning for projects..." + System.lineSeparator() +
                                "[INFO] BUILD FAILURE" + System.lineSeparator() +
                                "[INFO] Total time:  0.033 s" + System.lineSeparator();

        assertEquals(expectedOutput, output);
    }
}

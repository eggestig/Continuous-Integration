package com.mycompany.app;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.net.HttpURLConnection;
import java.net.URL;

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
}

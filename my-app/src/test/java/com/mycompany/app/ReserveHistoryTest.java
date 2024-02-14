package com.mycompany.app;

import static org.junit.Assert.*;
import org.junit.Test;
import java.io.*;

public class ReserveHistoryTest {

    /*
     * Test GenerateHtmlFromFile() if the path do not exist
     */
    @Test
    public void testGenerateHtmlFromFile() {

        // Specify the path to a non-existing HTML file for testing
        String nonExistingFilePath = "not_exist.html";

        // Create a StringWriter to capture the output
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        // Mock the PrintWriter for testing purposes
        ReserveHistory.generateHtmlContent(printWriter);

        // Test with a non-existing file
        assertFalse(ReserveHistory.generateHtmlFromFile(nonExistingFilePath));
        assertTrue(stringWriter.toString().contains("Error"));
    }

    @Test
    public void testShowAllCommit() throws IOException {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        ReserveHistory.generateHtmlContent(printWriter);

        File tempDir = new File(System.getProperty("java.io.tmpdir"), "testCommits");
        tempDir.mkdir();

        try {
            createTestCommitFile(tempDir, "commit1.html");
            ReserveHistory.path = tempDir.getAbsolutePath() + "/";

            ReserveHistory.showAllCommit();

            String generatedHtml = stringWriter.toString();
            assertTrue(generatedHtml.contains("<h1>All Commits</h1>"));
            } finally {
            deleteDirectory(tempDir);
        }
    }

    private void createTestCommitFile(File directory, String fileName) throws IOException {
        File commitFile = new File(directory, fileName);
        try (PrintWriter writer = new PrintWriter(new FileWriter(commitFile))) {
            writer.println("test");
        }
    }

    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }

    @Test
    public void testWriteJsonToHtml() throws IOException {
        // Mock the 'out' field in ReserveHistory for testing
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        ReserveHistory.generateHtmlContent(printWriter);

        File tempDir = new File(System.getProperty("java.io.tmpdir"), "testCommits");
        tempDir.mkdir();

        try {
            // Set the path for testing
            ReserveHistory.path = tempDir.getAbsolutePath() + "/";

            // Call the method to test
            ReserveHistory.writeJsonToHtml("12345", "20220214120000", "Build log content");

            // Check if the HTML file is generated
            assertTrue(fileExists(tempDir.getAbsolutePath() + "/commits/commit1.html"));

            // Check the content of the generated HTML file
            String generatedHtmlContent = readHtmlFile(tempDir.getAbsolutePath() + "/commits/commit1.html");
            assertTrue(generatedHtmlContent.contains("<title>Commit Details</title>"));
            assertTrue(generatedHtmlContent.contains("<p>Commit ID: 12345</p>"));
            assertTrue(generatedHtmlContent.contains("<p>Timestamp: 20220214120000</p>"));
            assertTrue(generatedHtmlContent.contains("Build log content"));

        } finally {
            // Clean up: Delete temporary directory and its contents
            deleteDirectory(tempDir);
        }
    }

    private boolean fileExists(String filePath) {
        return new File(filePath).exists();
    }

    private String readHtmlFile(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

}

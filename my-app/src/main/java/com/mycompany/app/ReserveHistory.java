package com.mycompany.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * Class for managing commit history and generating HTML content with commit
 * information.
 */
public class ReserveHistory {
    private static String path = "../my-app/src/main/java/com/mycompany/app/";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static PrintWriter out;

    /**
     * Set the PrintWriter for generating HTML content.
     * 
     * @param o PrintWriter object.
     */
    public static void generateHtmlContent(PrintWriter o) {
        out = o;
    }

    /**
     * Generate HTML content from a file.
     * 
     * @param s Path to the HTML file.
     */
    public static void generateHtmlFromFile(String s) {
        try {
            // Read HTML content from file
            BufferedReader reader = new BufferedReader(new FileReader(s));
            String line;
            while ((line = reader.readLine()) != null) {
                out.println(line);
            }
            reader.close();
        } catch (IOException e) {
            generateErrorPage();
        }
    }

    /**
     * Serve commit content by generating HTML from a commit file.
     * 
     * @param commitName Name of the commit.
     */
    public static void serveCommitContent(String commitName) {
        String commitFilePath = path + "commits/commit" + commitName + ".html";
        generateHtmlFromFile(commitFilePath);
    }

    /**
     * Generate an error page by reading HTML content from an error file.
     */
    public static void generateErrorPage() {
        generateHtmlFromFile(path + "error.html");
    }

    /**
     * Show all commits by generating HTML content with links to each commit.
     */
    public static void showAllCommit() {
        File commitsDir = new File(path + "commits");
        File[] commitFiles = commitsDir.listFiles();

        out.println("<html>");
        out.println("<head><title>All Commits</title></head>");
        out.println("<body>");
        out.println("<h1>All Commits</h1>");

        if (commitFiles != null) {
            Arrays.sort(commitFiles);

            for (File commitFile : commitFiles) {
                if (commitFile.isFile() && commitFile.getName().endsWith(".html")) {
                    // Extract commit number from the file name
                    String fileName = commitFile.getName();
                    String commitNumberStr = fileName.replaceAll("[^0-9]", "");

                    if (!commitNumberStr.isEmpty()) {
                        int commitNumber = Integer.parseInt(commitNumberStr);

                        // Provide a link to each commit
                        out.println("<p><a href=\"/commit" + commitNumber + "\">Commit " + commitNumber + "</a></p>");
                    }
                }
            }
        } else {
            out.println("<p>No commits found.</p>");
        }

        out.println("</body>");
        out.println("</html>");
    }

    /**
     * Write JSON node content to a file.
     * 
     * @param jsonNode JSON node to write.
     */
    public static void writeJsonToFile(JsonNode jsonNode) {
        try {
            // Define the file path
            String filePath = path + "jsonFiles/payload.json";

            // Create FileWriter with append mode (change to false if you want to overwrite
            // the file)
            FileWriter fileWriter = new FileWriter(filePath, true);

            // Write JSON content to the file
            objectMapper.writeValue(fileWriter, jsonNode);

            // Close the FileWriter
            fileWriter.close();

            System.out.println("JSON content written to file: " + filePath);

            System.out.println("Generating HTML file...");

            // writeJsonToHtml(jsonNode);

        } catch (IOException e) {
            System.out.println("Exception occurred while writing JSON to file");
            e.printStackTrace();
        }
    }

    /**
     * Check if a file exists.
     * 
     * @param filePath Path to the file.
     * @return True if the file exists, false otherwise.
     */
    private static boolean fileExists(String filePath) {
        return new File(filePath).exists();
    }

    /**
     * Write JSON node content to an HTML file.
     * 
     * @param ID       Commit ID.
     * @param time     Timestamp.
     * @param buildLog Build log content.
     */
    public static void writeJsonToHtml(String ID, String time, String buildLog) {
        try {

            int fileNum = 1;

            while (fileExists(path + "commits/commit" + fileNum + ".html")) {
                fileNum++;
            }

            // Define the file path
            String fileName = "commit" + fileNum + ".html";
            String filePath = path + "commits/" + fileName;

            // Create PrintWriter to write HTML content to the file
            PrintWriter writer = new PrintWriter(new FileWriter(filePath));

            // Write HTML content to the file
            writer.println("<html>");
            writer.println("<head><title>Commit Details</title></head>");
            writer.println("<body>");
            writer.println("<p>Commit ID: " + ID + "</p>");
            writer.println("<p>Timestamp: " + time + "</p>");
            writer.println("<p>Build Log:</p>");
            writer.println("<pre>");
            writer.println(buildLog);
            writer.println("</pre>");
            writer.println("</body>");
            writer.println("</html>");

            // Close the PrintWriter
            writer.close();

            System.out.println("HTML file generated: " + filePath);
        } catch (IOException e) {
            System.out.println("Exception occurred while generating HTML file");
            e.printStackTrace();
        }
    }

}
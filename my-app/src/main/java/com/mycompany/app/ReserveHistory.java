package com.mycompany.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

public class ReserveHistory {
    public static String path = "../my-app/src/main/java/com/mycompany/app/";
    private static PrintWriter out;
    public static void generateHtmlContent(PrintWriter o) {
        out = o;
    }
    
    /*
     * from a html file generate content of web
     * return false if file path do not exist
     */
    public static boolean generateHtmlFromFile(String s) {
        try {
            // Read HTML content from file
            BufferedReader reader = new BufferedReader(new FileReader(s));
            String line;
            while ((line = reader.readLine()) != null) {
                out.println(line);
            }
            reader.close();
            return true;
        } catch (IOException e) {
            generateErrorPage();
            return false;
        }
    }

    public static void serveCommitContent(String commitName) {
        String commitFilePath = path + "commits/commit" + commitName + ".html";
        generateHtmlFromFile(commitFilePath);
    }
    
    public static void generateErrorPage(){
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("    <title>Error Page</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("    <h1>Error</h1>");
        out.println("    <p>The requested commit file does not exist or there was an error.</p>");
        out.println("</body>");
        out.println("</html>");
    }

    public static void showAllCommit() {
        File commitsDir = new File(path + "commits");
        File[] commitFiles = commitsDir.listFiles();
    
        out.println("<html>");
        out.println("<head><title>All Commits</title></head>");
        out.println("<body>");
        out.println("<h1>All Commits</h1>");
    
        if (commitFiles != null && commitFiles.length > 0) {
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

    private static boolean fileExists(String filePath) {
        return new File(filePath).exists();
    }

    public static void writeJsonToHtml(String ID, String time, String buildLog) {
        try {

            File commitsDir = new File(path + "commits");
            if (!commitsDir.exists()) {
                commitsDir.mkdirs();
            }

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
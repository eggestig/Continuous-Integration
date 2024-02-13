package com.mycompany.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ReserveHistory {
    private static String path = "../my-app/src/main/java/com/mycompany/app/";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static PrintWriter out;
    public static void generateHtmlContent(PrintWriter o) {
        out = o;
    }
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
    public static void serveCommitContent(String commitName) {
        String commitFilePath = path + "commits/commit" + commitName + ".txt";
        generateHtmlFromFile(commitFilePath);
    }
    
    public static void generateErrorPage(){
        generateHtmlFromFile(path + "error.txt");
    }
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
                if (commitFile.isFile() && commitFile.getName().endsWith(".txt")) {
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

    public static void writeJsonToFile(JsonNode jsonNode) {
        try {
            // Define the file path
            String filePath = path + "jsonFiles/payload.json";

            // Create FileWriter with append mode (change to false if you want to overwrite the file)
            FileWriter fileWriter = new FileWriter(filePath, true);

            
            // Write JSON content to the file
            objectMapper.writeValue(fileWriter, jsonNode);
            
            // Close the FileWriter
            fileWriter.close();

            System.out.println("JSON content written to file: " + filePath);

            System.out.println("Generating HTML file...");

            //writeJsonToHtml(jsonNode);
            
        } catch (IOException e) {
            System.out.println("Exception occurred while writing JSON to file");
            e.printStackTrace();
        }
    }

    // public static void writeJsonToHtml(JsonNode jsonNode) {
    //     try {
    //         // Create a unique identifier (using timestamp in this example)
    //         String timestamp = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(Calendar.getInstance().getTime());
    //         String uniqueFileName = "commit_" + timestamp + ".html";

    //         // Define the file path
    //         String filePath = path + "commits/" + uniqueFileName;

    //         // Create PrintWriter to write HTML content to the file
    //         PrintWriter writer = new PrintWriter(new FileWriter(filePath));

    //         // Write HTML content to the file
    //         writer.println("<html>");
    //         writer.println("<head><title>JSON Content</title></head>");
    //         writer.println("<body>");
    //         writer.println("<pre>");
    //         writer.println(jsonNode.toPrettyString());
    //         writer.println("</pre>");
    //         writer.println("</body>");
    //         writer.println("</html>");

    //         // Close the PrintWriter
    //         writer.close();

    //         System.out.println("HTML file generated from JSON content: " + filePath);
    //     } catch (IOException e) {
    //         System.out.println("Exception occurred while generating HTML file from JSON");
    //         e.printStackTrace();
    //     }
   // }
}
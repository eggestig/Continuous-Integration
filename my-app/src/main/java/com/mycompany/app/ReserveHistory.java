package com.mycompany.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class ReserveHistory {
    //need to change base on your computer
    private static String path = "../my-app/src/main/java/com/mycompany/app/";
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
    public static void serveCommitContent(int commitNumber) {
        String commitFilePath = path + "commits/commit" + commitNumber + ".txt";
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
}
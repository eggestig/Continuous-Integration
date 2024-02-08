package com.mycompany.app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
/** 
 Skeleton of a ContinuousIntegrationServer which acts as webhook
 See the Jetty documentation for API documentation of those classes.
*/
public class App extends AbstractHandler
{
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response) 
        throws IOException, ServletException
    {
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);

        System.out.println(target);

        // Check if the incoming request is a GitHub push event
        String eventType = request.getHeader("X-GitHub-Event");
        if ("push".equals(eventType)) {

            String payload = request.getReader().lines().collect(Collectors.joining());

            JsonNode jsonNode = objectMapper.readTree(payload);

            // Here we can extract the needed information from the payload
            String repositoryName = jsonNode.path("repository").path("name").asText();
            String pushedBranch = jsonNode.path("ref").asText();
            System.out.println("Received GitHub push event for repository: " + repositoryName +
                    ", pushed to branch: " + pushedBranch);

            //System.out.println("Received GitHub push event: " + payload);
        }

        // Respond with a 200 OK status
        // here you do all the continuous integration tasks
        // for example
        // 1st clone your repository
        // 2nd compile the code with mvn
        response.setStatus(HttpServletResponse.SC_OK);

        projectBuilder(System.getProperty("user.dir"));

        response.getWriter().println("CI job done");
    }

    public static String projectBuilder(String path){
       
        String buildResult = "";

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(new String[]{"mvn", "package"}); 
            processBuilder.directory(new java.io.File(path));
            Process commandRunner = processBuilder.start();

            String output = captureOutput(commandRunner.getInputStream());
            int exitCode = commandRunner.waitFor();

            System.out.println("Captured Output:\n" + output);
            System.out.println("Exit Code: " + exitCode);

            if (output.contains("BUILD SUCCESS")) {
                buildResult = "SUCCESS";

            } else if (output.contains("BUILD FAILURE")){
                buildResult = "FAILURE";
            }

        } catch (IOException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println(buildResult);
        return buildResult;

    }

    public static String captureOutput(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder output = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }

            return output.toString();
        }
    }
 
    // used to start the CI server in command line
    public static void main(String[] args) throws Exception
    {
        System.out.println("Hello World!");
        Server server = new Server(8080);
        server.setHandler(new App()); 
        server.start();
        server.join();
    }
}
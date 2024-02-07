package com.mycompany.app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
 
import java.io.IOException;
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
        response.setStatus(HttpServletResponse.SC_OK);

        // here you do all the continuous integration tasks
        // for example
        // 1st clone your repository
        // 2nd compile the code with mvn
        response.getWriter().println("CI job done");
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
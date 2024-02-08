package com.mycompany.app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.srujankujmar.commons.config.SetupConfig;

import io.github.cdimascio.dotenv.Dotenv;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.IOException;
import java.util.stream.Collectors;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.kohsuke.github.GHCommitState;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

/**
 * Skeleton of a ContinuousIntegrationServer which acts as webhook
 * See the Jetty documentation for API documentation of those classes.
 */
public class App extends AbstractHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void handle(String target,
            Request baseRequest,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException, ServletException {
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

            // System.out.println("Received GitHub push event: " + payload);
            setCommitStatus(jsonNode);
        }

        // Respond with a 200 OK status
        response.setStatus(HttpServletResponse.SC_OK);

        // here you do all the continuous integration tasks
        // for example
        // 1st clone your repository
        // 2nd compile the code with mvn


        response.getWriter().println("CI job done");

    }

    private static GitHub getGithub(final String token) {
        try {
            return GitHub.connectUsingOAuth("https://api.github.com", token);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setCommitStatus(JsonNode payload) {
        try {
            // Make sure to run before: "export AUTH_TOKEN="<insert_tok>""
            String token = System.getenv("AUTH_TOKEN");
            GitHub github = getGithub(token);

            String owner = payload.path("repository")
                            .path("owner")
                            .path("name")
                            .asText();
            String repoName = payload.path("repository")
                            .path("name")
                            .asText();

            String sha1 = payload.path("after").asText();

            String description = "The build succeeded!";

            String targetUrl = payload.path("head_commit").path("url").asText();

            GHRepository repository = github.getRepository(owner + "/" + repoName);
            repository.createCommitStatus(sha1, GHCommitState.SUCCESS, targetUrl, description);

            System.out.println("Commit status updated successfully.");
        } catch (Exception e) {
            System.out.println("Commit status update failed.");
            e.printStackTrace();
        }
    }

    // used to start the CI server in command line
    public static void main(String[] args) throws Exception {
        System.out.println("Hello World!");
        Server server = new Server(8080);
        server.setHandler(new App());
        server.start();
        server.join();
    }
}
package evolution;


import evolution.analysis.jv.ParseClassService;
import evolution.analysis.jv.ParsePackageService;
import io.helidon.config.Config;
import io.helidon.webserver.*;
import io.helidon.webserver.json.JsonSupport;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.LogManager;

/**
 * Simple  rest application.
 */
public final class Main {

    /**
     * Cannot be instantiated.
     */
    private Main() { }

    /**
     * Creates new {@link Routing}.
     *
     * @return the new instance
     */
    private static Routing createRouting() {
        return Routing.builder()
                .register(JsonSupport.get())
                .register("/greet", new GreetService())
                .register("/analysis/class", new ParseClassService())
                .register("/analysis/package", new ParsePackageService())
                .build();
    }

    /**
     * Application main entry point.
     * @param args command line arguments.
     * @throws IOException if there are problems reading logging properties
     */
    public static void main(final String[] args) throws IOException {
        System.out.println("start server");
        startServer();
    }

    /**
     * Start the server.
     * @return the created {@link WebServer} instance
     * @throws IOException if there are problems reading logging properties
     */
    protected static WebServer startServer() throws IOException {

        // load logging configuration
        LogManager.getLogManager().readConfiguration(
                Main.class.getResourceAsStream("/logging.properties"));

        // By default this will pick up application.yaml from the classpath
        Config config = Config.create();

        // Get webserver config from the "server" section of application.yaml
        ServerConfiguration serverConfig =
                ServerConfiguration.fromConfig(config.get("server"));

        WebServer server = WebServer.create(serverConfig, createRouting());

        // Start the server and print some info.
        server.start().thenAccept(ws -> {
            System.out.println(
                    "WEB server is up! http://localhost:" + ws.port());
        });

        // Server threads are not demon. NO need to block. Just react.
        server.whenShutdown().thenRun(()
                -> System.out.println("WEB server is DOWN. Good bye!"));

        return server;
    }

    static Path resourcePath(String resourceName) throws URISyntaxException {
        URL resourceUrl = Thread.currentThread().getContextClassLoader().getResource(resourceName);
        if (resourceUrl != null) {
            return Paths.get(resourceUrl.toURI());
        } else {
            return null;
        }
    }

    public static class GreetService implements Service {


        private String greeting = "Hello";
        /**
         * A service registers itself by updating the routine rules.
         * @param rules the routing rules.
         */
        @Override
        public final void update(final Routing.Rules rules) {
            rules
                    .get("/{name}", this::getMessage)
                    .put("/greeting/{greeting}", this::updateGreeting);
        }

        /**
         * Return a greeting message using the name that was provided.
         * @param request the server request
         * @param response the server response
         */
        private void getMessage(final ServerRequest request,
                                final ServerResponse response) {
            String name = request.path().param("name");
            String msg = String.format("%s %s!", greeting, name);

            JsonObject returnObject = Json.createObjectBuilder()
                    .add("message", msg)
                    .build();
            response.send(returnObject);
        }

        /**
         * Set the greeting to use in future messages.
         * @param request the server request
         * @param response the server response
         */
        private void updateGreeting(final ServerRequest request,
                                    final ServerResponse response) {
            greeting = request.path().param("greeting");

            JsonObject returnObject = Json.createObjectBuilder()
                    .add("greeting", greeting)
                    .build();
            response.send(returnObject);
        }
    }
}

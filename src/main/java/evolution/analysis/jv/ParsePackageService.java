package evolution.analysis.jv;

import evolution.analysis.jv.calls.JavaCallApp;
import evolution.analysis.jv.identifier.JavaClassQuery;
import evolution.analysis.jv.identifier.JavaIdentifierApp;
import evolution.store.Neo4JDriverFactory;
import io.helidon.config.Config;
import io.helidon.webserver.Routing;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import io.helidon.webserver.Service;
import org.neo4j.driver.v1.Driver;

import javax.json.Json;
import javax.json.JsonObject;
import java.util.List;
import java.util.logging.Logger;

public class ParsePackageService implements Service {

    private static final Logger LOGGER = Logger.getLogger(ParsePackageService.class.getName());
    /**
     * This gets config from application.yaml on classpath
     * and uses "app" section.
     */
    private final Config CONFIG = Config.create().get("app");
    private final String rootDir = CONFIG.get("root").asString("~/workspace");

    @Override
    public void update(Routing.Rules rules) {
        rules
                .get("/{package}", this::parse);
    }

    /**
     * Return a wordly greeting message.
     * @param request the server request
     * @param response the server response
     */
    private void parse(final ServerRequest request,
                       final ServerResponse response) {
        String pkg = request.path().param("package");

        try {
            parse(pkg);
            JsonObject returnObject = Json.createObjectBuilder()
                    .add("message", pkg)
                    .build();
            response.send(returnObject);
        } catch (Exception e) {
            e.printStackTrace();
            JsonObject returnObject = Json.createObjectBuilder()
                    .add("message", e.getMessage())
                    .build();
            response.send(returnObject);
        }

    }

    private void parse(String pkg) throws Exception {
        Driver driver = Neo4JDriverFactory.create();
        String dir = String.format("%s/cbs/src/main/java/%s",rootDir,pkg.replaceAll("\\.","/"));
        new JavaIdentifierApp(driver).analysisDir(dir);
        JavaClassQuery classQuery = new JavaClassQuery(driver);
        List<String> clzs = classQuery.load();
        new JavaCallApp(driver).analysisDir(dir,clzs);
        LOGGER.info("finished and close driver.");
        driver.close();
    }

}

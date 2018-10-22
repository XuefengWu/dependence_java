package evolution.analysis.jv;

import evolution.analysis.jv.calls.plugins.MyBatisParser;
import evolution.factory.daoparser.JavaDaoParserFactory;
import io.helidon.config.Config;
import io.helidon.webserver.Routing;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import io.helidon.webserver.Service;

import javax.json.Json;
import javax.json.JsonObject;

public class ParseClassService implements Service {

    /**
     * This gets config from application.yaml on classpath
     * and uses "app" section.
     */
    private final Config CONFIG = Config.create().get("app");
    private final String rootDir = CONFIG.get("root").asString("~/workspace");
    private final ParseClassApp clzParser = new ParseClassApp();
    private final JavaDaoParserFactory javaDaoParserFactory;

    public ParseClassService(JavaDaoParserFactory javaDaoParserFactory) {

        this.javaDaoParserFactory = javaDaoParserFactory;
    }

    @Override
    public void update(Routing.Rules rules) {
        rules
                .post("/{class}", this::parse);
    }

    /**
     * Return a wordly greeting message.
     * @param request the server request
     * @param response the server response
     */
    private void parse(final ServerRequest request,
                                   final ServerResponse response) {
        String clz = request.path().param("class");

        try {
            clzParser.parse(rootDir,clz, javaDaoParserFactory.createDaoParser());
            JsonObject returnObject = Json.createObjectBuilder()
                    .add("message", clz)
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


}

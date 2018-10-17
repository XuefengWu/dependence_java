package evolution.store;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;

public class Neo4JDriverFactory {

    public static Driver create(String neo4jServer){
        String uri = String.format("bolt://%s:7687",neo4jServer);
        String user = "neo4j";
        String password = "admin";

        return GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }
}

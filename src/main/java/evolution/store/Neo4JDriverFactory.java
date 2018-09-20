package evolution.store;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;

public class Neo4JDriverFactory {

    public static Driver create(){
        String uri = "bolt://localhost:7687";
        String user = "xuefeng";
        String password = "xuefeng";

        return GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }
}

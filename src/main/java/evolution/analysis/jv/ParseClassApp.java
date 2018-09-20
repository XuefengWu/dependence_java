package evolution.analysis.jv;

import evolution.analysis.jv.calls.JavaCallApp;
import evolution.analysis.jv.identifier.JavaClassQuery;
import evolution.analysis.jv.identifier.JavaIdentifierApp;
import evolution.store.Neo4JDriverFactory;
import evolution.store.StoreManager;
import org.neo4j.driver.v1.Driver;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;

public class ParseClassApp {

    private static final Logger LOGGER = Logger.getLogger(ParseClassApp.class.getName());

    public void parse(String rootDir, String clz) throws IOException {
        Driver driver = Neo4JDriverFactory.create();
        JavaClassQuery classQuery = new JavaClassQuery(driver);
        List<String> clzs = classQuery.load();
        StoreManager store = new StoreManager(driver); 
        String deleteState=String.format("MATCH (c1:Class{fullname:'%s'})-[r:Has]->(m2) DETACH DELETE c1,r,m2",clz);
        System.out.println("Delete: " + clz);
        store.update(deleteState);
        String file = String.format("%s/cbs/src/main/java/%s.java",rootDir,clz.replaceAll("\\.","/"));

        Path path = Paths.get(file);
        new JavaIdentifierApp(driver).parse(path);
        new JavaCallApp(driver).parse(path,clzs);
        driver.close();
        LOGGER.info("finished and close driver.");
    }

}

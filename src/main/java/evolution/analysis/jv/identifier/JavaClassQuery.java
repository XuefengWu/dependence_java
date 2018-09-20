package evolution.analysis.jv.identifier;

import org.neo4j.driver.v1.*;

import java.util.ArrayList;
import java.util.List;

public class JavaClassQuery {

    private Driver driver;

    public JavaClassQuery(Driver driver) {
        this.driver = driver;
    }

    public List<String> load() {
        try(Session session = driver.session()) {
           return session.readTransaction(new TransactionWork<List<String>>() {
                @Override
                public List<String> execute(Transaction tx) {
                    StatementResult res = tx.run("MATCH (c:Class) RETURN c");
                    List<String> clzs = new ArrayList<>();
                    while(res.hasNext()) {
                        Value c1 = res.next().get("c");
                        String c = c1.get("fullname").asString();
                        clzs.add(c);
                    }
                    return clzs;
                }
            });
        }
    }
}

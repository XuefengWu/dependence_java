package evolution.analysis.jv.identifier;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import static org.neo4j.driver.v1.Values.parameters;

public class JavaIdentifierStore {
    private static final Logger LOGGER = Logger.getLogger(JavaIdentifierStore.class.getName());
    private Driver driver;

    public JavaIdentifierStore(Driver driver) {
        this.driver = driver;
    }

    public void save(JIdentifier ident) {
        System.out.println(ident);
        try(Session session = driver.session()) {
            session.writeTransaction(new TransactionWork<Integer>() {
                @Override
                public Integer execute(Transaction tx) {
                    String pkg = ident.getPkg();
                    String clz = ident.getName();
                    tx.run("MERGE (p:Package {name: $pkg})  " +
                                    "MERGE (c:__typ__ {name: $clz, fullname: $clzFullname})  ".replace("__typ__",ident.getType()) +
                            "MERGE  (p)-[:Has]->(c)",
                            parameters("pkg", ident.getPkg(),
                                    "clz", clz, "clzFullname", pkg + "." + clz));
                    LOGGER.info(clz);
                    for (JMethod m : ident.getMethods()) {
                        String mFullName = pkg + "." + clz + "." + m.getName();
                        LOGGER.info("\t"+m.getName());
                        tx.run("MATCH (c {fullname: $clzFullname})  \n" +
                                "MERGE (m:JMethod {name: $name, fullname: $fullname,date:$date," +
                                        "startline: $startline, startlinePos: $startlinePos," +
                                        "stopline: $stopline, stoplinePos: $stoplinePos})  " +
                                        "MERGE  (c)-[:Has]->(m)",
                                parameters("clzFullname", pkg + "." + clz,
                                        "name", m.getName(), "fullname", mFullName,"date",getToday(),
                                        "startline",m.getStartLine(),"startlinePos",m.getStartLinePosition(),
                                        "stopline",m.getStopLine(),"stoplinePos",m.getStopLinePosition()));
                    }
                    return ident.getMethods().size();
                }
            });
        }
    }
    private String getToday() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }
}

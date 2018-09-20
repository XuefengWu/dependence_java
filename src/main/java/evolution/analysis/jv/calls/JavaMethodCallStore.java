package evolution.analysis.jv.calls;

import evolution.analysis.jv.calls.model.JMethodCall;
import io.helidon.config.Config;
import org.neo4j.driver.v1.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.neo4j.driver.v1.Values.parameters;

public class JavaMethodCallStore {

    private static final Logger LOGGER = Logger.getLogger(JavaMethodCallStore.class.getName());
    private final Config config = Config.create().get("app");
    private final List<String> includeKeyWorks = config.get("keywords").asList(String.class);
    private Driver driver;

    public JavaMethodCallStore(Driver driver) {
        this.driver = driver;
    }

    private boolean isTargetClass(String clz) {
        boolean res = false;
        for(String keyword: includeKeyWorks){
            res = res || clz.contains(keyword);
        }
        return res;
    }
    public void save(List<JMethodCall> methodCalls) {
        try (Session session = driver.session()) {
            session.writeTransaction(new TransactionWork<Integer>() {
                @Override
                public Integer execute(Transaction tx) {
                    for (JMethodCall ident : methodCalls) {
                        String pkg = ident.getPkg();
                        String clz = ident.getClz();
                        String callerFullName = pkg + "." + clz + "." + ident.getMethodName();
                        LOGGER.info("save: "+callerFullName);
                        StatementResult sr = tx.run("MATCH (m:JMethod{fullname:$callerfullname})-[r]->() RETURN COUNT(r)",
                                parameters("callerfullname", callerFullName));
                        if (!sr.hasNext() || sr.single().values().get(0).asInt() == 0) {
                            for (String calleeClz : ident.getMethodCalls().keySet()) {
                                if (isTargetClass(calleeClz)) {
                                    //System.out.println("try to save: " + calleeClz);
                                    for (String callee : ident.getMethodCalls().get(calleeClz)) {
                                        //System.out.println("\t" + callee);
                                        tx.run("MATCH (caller:JMethod {fullname: $callerfullname})  " +
                                                        "MATCH (callee:JMethod {fullname: $calleefullname})  " +
                                                        "MERGE  (caller)-[:Call {date:$date}]->(callee)",
                                                parameters("callerfullname", callerFullName, "date", getToday(),
                                                        "calleefullname", calleeClz + "." + callee));
                                    }
                                }
                            }
                            for (Map.Entry s : ident.getTableOps().entrySet()) {
                                String table = s.getKey().toString();
                                String op = s.getValue().toString();
                                tx.run("MATCH (caller:JMethod {fullname: $callerfullname})  " +
                                                "MATCH (t:Table {name: $table})  " +
                                                "MERGE  (caller)-[:__op__ {date:$date}]->(t)".replace("__op__", op),
                                        parameters("callerfullname", callerFullName, "date", getToday(),
                                                "table", table));
                            }
                            for(String pl: ident.getProcedures()) {
                                tx.run("MATCH (caller:JMethod {fullname: $callerfullname})  " +
                                                "MATCH (callee:PLProcedure {fullname: $calleefullname})  " +
                                                "MERGE  (caller)-[:Call {date:$date}]->(callee)",
                                        parameters("callerfullname", callerFullName, "date", getToday(),
                                                "calleefullname", pl));
                            }
                        }
                    }
                    return methodCalls.size();
                }
            });
        }
    }

    private String getToday() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }
}

package evolution.store;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;

public class StoreManager {
    private final Driver driver;

    public StoreManager(Driver driver) {
        this.driver = driver;
    }

    public void update(String state) {
        try (Session session = driver.session()) {
             session.writeTransaction(new TransactionWork<Integer>() {
                @Override
                public Integer execute(Transaction tx) {
                    tx.run(state);
                    return 0;
                }
            });
        }
    }

}

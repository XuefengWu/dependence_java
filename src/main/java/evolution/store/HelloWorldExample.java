package evolution.store;

import org.neo4j.driver.v1.*;

import static org.neo4j.driver.v1.Values.parameters;

public class HelloWorldExample implements AutoCloseable {
    private final Driver driver;

    public HelloWorldExample(String uri, String user, String password) {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    @Override
    public void close() throws Exception {
        driver.close();
    }

    public void printGreeting(final String message) {
        try (Session session = driver.session()) {
            System.out.println(isActorExist(session,"Tom Hanks"));

            String greeting = session.writeTransaction(new TransactionWork<String>() {
                @Override
                public String execute(Transaction tx) {
                    StatementResult result = tx.run("CREATE (a:Greeting) " +
                                    "SET a.message = $message " +
                                    "RETURN a.message + ', from node ' + id(a)",
                            parameters("message", message));

                    return result.single().get(0).asString();
                }
            });
            System.out.println(greeting);
        }
    }

    public boolean isActorExist(Session session, String name) {
        return session.readTransaction(new TransactionWork<Boolean>() {
            @Override
            public Boolean execute(Transaction tx) {
                StatementResult result = tx.run("MATCH (a {name: $name}) RETURN a", parameters("name",name));
                //Node node = result.single().get(0).asNode();
                //System.out.println(node.asMap());
                return result.hasNext();
            }
        });
    }

    public static void main(String... args) throws Exception {
        try (HelloWorldExample greeter = new HelloWorldExample("bolt://localhost:7687", "xuefeng", "xuefeng")) {
            greeter.printGreeting("hello, world");
        }
    }
}

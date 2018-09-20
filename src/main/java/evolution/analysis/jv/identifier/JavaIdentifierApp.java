package evolution.analysis.jv.identifier;


import evolution.analysis.jv.JavaParserBaseVisitor;
import evolution.analysis.jv.ProcessFiles;
import evolution.store.Neo4JDriverFactory;
import org.antlr.v4.runtime.tree.ParseTree;
import org.neo4j.driver.v1.Driver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class JavaIdentifierApp {

    private final JavaIdentifierStore identStore;
    public JavaIdentifierApp(Driver driver) {
        this.identStore =  new JavaIdentifierStore(driver);
    } 
  
    public void analysisDir(String dir) throws IOException, InterruptedException, ExecutionException {
        Path startingDir = Paths.get(dir);
        int poolSize = 8;
        ExecutorService pool = Executors.newFixedThreadPool(poolSize);
        List<Future> futures = new ArrayList<>();
        ProcessFiles pf = new ProcessFiles(this::parse, pool,futures);
        Files.walkFileTree(startingDir, pf);
        for(Future f: futures) {
            f.get();
        }
        pool.shutdown();
    }

    public void parse(Path path) {
        try {

            if (!path.toString().endsWith("Tests.java")
                    && !path.toString().endsWith("Test.java") && path.toString().endsWith(".java")) {
                ParseTree tree = JavaFileParser.parse(path);
                JIdentifier interfaceIdent = new JIdentifier();
                JavaParserBaseVisitor interfaceVisitor = new JavaIdentifierVisitor(interfaceIdent);
                interfaceVisitor.visit(tree);
                if (interfaceIdent.getName() != null && interfaceIdent.getName() != "") {
                    identStore.save(interfaceIdent);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

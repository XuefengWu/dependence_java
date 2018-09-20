package evolution.analysis.jv.calls;

import evolution.analysis.jv.JavaParserBaseVisitor;
import evolution.analysis.jv.ProcessFiles;
import evolution.analysis.jv.calls.model.JMethodCall;
import evolution.analysis.jv.identifier.JavaClassQuery;
import evolution.analysis.jv.identifier.JavaFileParser;
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
import java.util.function.Consumer;

public class JavaCallApp {

    private final JavaMethodCallStore store;
    public JavaCallApp(Driver driver) {
        this.store = new JavaMethodCallStore(driver);
    }

    public void analysisDir(String dir, List<String> clzs) throws IOException, InterruptedException, ExecutionException {
                Path startingDir = Paths.get(dir);
        Consumer<Path> fileAnalysis = parse(clzs);
        int poolSize = 8;
        ExecutorService pool = Executors.newFixedThreadPool(poolSize);
        List<Future> futures = new ArrayList<>();
        ProcessFiles pf = new ProcessFiles(fileAnalysis,pool,futures);
        Files.walkFileTree(startingDir, pf);
        for(Future f: futures) {
            f.get();
        }
        pool.shutdown();
    }

    private Consumer<Path> parse(List<String> clzs) {
        return (Path path) -> {
                try {
                    if (!path.toString().endsWith("Tests.java")
                            && !path.toString().endsWith("Test.java") && path.toString().endsWith(".java")) {
                        parse(path, clzs);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };
    }

    public void parse(Path path, List<String> clzs) throws IOException {
        ParseTree tree = JavaFileParser.parse(path);
        List<JMethodCall> calls = new ArrayList<>();
        JavaParserBaseVisitor visitor = new JavaCallVisitor(calls,clzs);
        visitor.visit(tree);
        long start = System.currentTimeMillis();
        store.save(calls);
        long stop = System.currentTimeMillis();
        System.out.println("Save "+path.getFileName().toString()+", Spend Time: " + (stop - start) / 1000 + " s @" + Thread.currentThread().getName());
    }

}

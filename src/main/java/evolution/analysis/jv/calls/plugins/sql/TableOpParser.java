package evolution.analysis.jv.calls.plugins.sql;

import evolution.analysis.sql.SqlLexer;
import evolution.analysis.sql.SqlParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.IOException;


public class TableOpParser {

    public ParseTree parse(String sql) throws IOException {
        CharStream stream = CharStreams.fromString(sql);
        SqlLexer lexer = new SqlLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream( lexer );
        SqlParser parser = new SqlParser(tokens);
        return parser.parse();
    }

}

package evolution.analysis.jv.calls.plugins.sql;


import evolution.analysis.sql.SqlBaseVisitor;
import evolution.analysis.sql.SqlParser;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.Map;

public class TableVisitor extends SqlBaseVisitor<Void> {

    private Map<String, String> tableOps;
    public TableVisitor(Map<String, String> tableOps) {
        this.tableOps = tableOps;
    }


    @Override
    public Void visitSelect_stmt(SqlParser.Select_stmtContext ctx) {
        System.out.println("SELECT: " + ctx.getText());
        return super.visitChildren(ctx);
    }

    @Override
    public Void visitSimple_select_stmt(SqlParser.Simple_select_stmtContext ctx) {
        System.out.printf("visitSimple_select SELECT: %s%n", ctx.getText());
        return super.visitChildren(ctx);
    }

    @Override
    public Void visitSelect_or_values(SqlParser.Select_or_valuesContext ctx) {
        System.out.printf("visitSelect_or_values SELECT: %s%n", ctx.getText());
        return super.visitChildren(ctx);
    }

    @Override
    public Void visitTable_or_subquery(SqlParser.Table_or_subqueryContext ctx) {
        ParseTree child = ctx.getChild(0);
        if(child instanceof evolution.analysis.sql.SqlParser.Table_nameContext) {
            tableOps.put(child.getText(),"SELECT");
        } else {
            //System.out.printf("visitTable_or_subquery SELECT: %s @%s \n", ctx.getText(), child.getClass().getCanonicalName());
        }
        return super.visitChildren(ctx);
    }

    @Override
    public Void visitSelect_core(SqlParser.Select_coreContext ctx) {
        //System.out.printf("visitSelect_core SELECT: %s%n", ctx.getText());
        return super.visitChildren(ctx);
    }

    @Override
    public Void visitCompound_select_stmt(SqlParser.Compound_select_stmtContext ctx) {
        System.out.printf("visitCompound_select_stmt SELECT: %s%n", ctx.getText());
        return super.visitChildren(ctx);
    }

    @Override
    public Void visitFactored_select_stmt(SqlParser.Factored_select_stmtContext ctx) {
        //System.out.printf("visitFactored_select_stmt SELECT: %s%n", ctx.getText());
        return super.visitChildren(ctx);
    }

    @Override
    public Void visitUpdate_stmt(SqlParser.Update_stmtContext ctx) {
        //System.out.println("UPDATE: " + ctx.getText());
        tableOps.put(ctx.getChild(1).getText(),"UPDATE");
        return super.visitChildren(ctx);
    }

    @Override
    public Void visitDelete_stmt(SqlParser.Delete_stmtContext ctx) {
        System.out.println("DELETE: " + ctx.getText());
        tableOps.put(ctx.getChild(2).getText(),"DELETE");
        return super.visitChildren(ctx);
    }

    @Override
    public Void visitInsert_stmt(SqlParser.Insert_stmtContext ctx) {
        //System.out.println("INSERT: " + ctx.getText());
        tableOps.put(ctx.getChild(2).getText(),"INSERT");
        return super.visitChildren(ctx);
    }
}

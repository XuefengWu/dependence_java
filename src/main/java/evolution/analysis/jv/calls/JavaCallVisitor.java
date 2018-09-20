package evolution.analysis.jv.calls;

import evolution.analysis.jv.JavaParser;
import evolution.analysis.jv.JavaParserBaseVisitor;
import evolution.analysis.jv.calls.model.JMethodCall;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaCallVisitor extends JavaParserBaseVisitor {

    private Map<String, String> fields = new HashMap<>();
    private Map<String, String> formalParameters = new HashMap<>();
    private Map<String, String> localVars = new HashMap<>();
    private String currentClz = null;
    private String currentPkg = null;
    private List<String> imports = new ArrayList<>();

    private final List<String> clzs;
    private JMethodCall currentMethodCall;

    private List<JMethodCall> methodCalls;

    public JavaCallVisitor(List<JMethodCall> methodCalls,List<String> clzs) {
        this.clzs = clzs;
        this.methodCalls = methodCalls;
    }

    @Override
    public Object visitPackageDeclaration(evolution.analysis.jv.JavaParser.PackageDeclarationContext ctx) {
        currentPkg = ctx.qualifiedName().getText();
        return super.visitPackageDeclaration(ctx);
    }

    @Override
    public Object visitImportDeclaration(evolution.analysis.jv.JavaParser.ImportDeclarationContext ctx) {
        imports.add(ctx.qualifiedName().getText());
        return super.visitImportDeclaration(ctx);
    }

    @Override
    public Object visitClassDeclaration(evolution.analysis.jv.JavaParser.ClassDeclarationContext ctx) {

//        System.out.println(ctx.getText());
        currentClz = ctx.IDENTIFIER().getText();

//        System.out.println(ctx.getChildCount());
//        for (int i = 0; i < ctx.getChildCount(); i++) {
//            System.out.println(i+":"+ctx.getChild(i).getText());
//        }
//        if(ctx.typeType()!=null){System.out.println(ctx.typeType().getText());}
//        if(ctx.EXTENDS()!=null){System.out.println(ctx.EXTENDS().getText());}
//        TerminalNode anImplements = ctx.IMPLEMENTS();
//        if(anImplements !=null){
//            System.out.println(anImplements);
//        }
//        if(ctx.typeParameters()!=null){System.out.println(ctx.typeParameters().getText());}
        return super.visitClassDeclaration(ctx);
    }

    @Override
    public Object visitTypeDeclaration(evolution.analysis.jv.JavaParser.TypeDeclarationContext ctx) {
        //System.out.println(ctx.getText());
        return super.visitTypeDeclaration(ctx);
    }

    @Override
    public Object visitMethodDeclaration(evolution.analysis.jv.JavaParser.MethodDeclarationContext ctx) {
        //System.out.println("\nMethod: " + ctx.IDENTIFIER().getText());
        currentMethodCall = new JMethodCall();
        methodCalls.add(currentMethodCall);
        currentMethodCall.setPkg(currentPkg);
        currentMethodCall.setClz(currentClz);
        currentMethodCall.setMethodName(ctx.IDENTIFIER().getText());

        String body = ctx.getText();
        buildTableOps(body);
        buildProcedureCalls(body);
        return super.visitMethodDeclaration(ctx);
    }

    private void buildProcedureCalls(String body) {
        currentMethodCall.addProcedures(parseProcedureCalls(body));
    }

    protected List<String> parseProcedureCalls(String body) {
        ArrayList<String> res = new ArrayList<>();
        String s = "(?:\\{.?(call|select|SELECT|CALL)\\s+)([a-zA-Z_\\d]+\\.[a-zA-Z_\\d]+)\\(";
        final Pattern p = Pattern.compile(s);
        final Matcher m = p.matcher(body);
        while (m.find()) {
            String pl = m.group(2);
            res.add(pl);
        }
        return res;
    }

    private void buildTableOps(String body) {
        List<String> tableOps = parseTables(body);
        if (tableOps == null || tableOps.size() == 0) {
            return;
        }

        String op = tableOps.get(0);
        for (int i = 1; i < tableOps.size(); i++) {
            String s = tableOps.get(i);
            if (isTableOperator(s)) {
                op = s;
            } else {
                currentMethodCall.addTableOp(s, op);
            }
        }
    }

    private boolean isTableOperator(String s) {
        return "INSERT".equalsIgnoreCase(s)
                || "SELECT".equalsIgnoreCase(s)
                || "UPDATE".equalsIgnoreCase(s)
                || "DELETE".equalsIgnoreCase(s);
    }

    private List<String> parseTables(String body) {
        List<String> result = new ArrayList<>();
        String[] ts = body.split("[\\s|\"|,]");
        for (int i = 0; i < ts.length - 1; i++) {
            String s = ts[i];
            if (s.startsWith("t_") || s.startsWith("T_")) {
                //select from, insert into,delete from,update
                String op = parseTableOperate(ts, i);
                if (op != null) {
                    result.add(op);
                }
                result.add(s);
            }
        }
        return result;
    }

    private String parseTableOperate(String[] ts, int index) {
        String pre = ts[index - 1];
        String op = null;
        if ("update".equalsIgnoreCase(pre)) {
            op = "UPDATE";
        } else if ("into".equalsIgnoreCase(pre)) {
            op = "INSERT";
        } else if ("from".equalsIgnoreCase(pre)) {
            String prePre = ts[index - 2];
            if ("delete".equalsIgnoreCase(prePre)) {
                op = "DELETE";
            } else {
                op = "SELECT";
            }
        } else if ("delete".equalsIgnoreCase(pre)) {
            op = "DELETE";
        }
        return op;
    }

    @Override
    public Object visitMethodCall(evolution.analysis.jv.JavaParser.MethodCallContext ctx) {

        if (currentMethodCall != null) {
            String targetType = parseTargetType(ctx);
            String callee = ctx.getChild(0).getText();

            String warpTargetFullType = warpTargetFullType(targetType);
            if(warpTargetFullType != null) {
                currentMethodCall.addMethodCall(warpTargetFullType, callee);
            } else {
                //System.out.println("Can not wrap:\t" + targetType);
            }

        } else {
            //class static block
            //System.out.println("-----------------visitMethodCall: currentMethodCall is null ------------");
            //System.out.println(ctx.parent.getText());
        }

        return super.visitMethodCall(ctx);
    }

    private String warpTargetFullType(String targetType) {
        if (currentClz.equalsIgnoreCase(targetType)) {
            return currentPkg + "." + targetType;
        }
        for (String imp : imports) {
            if (imp.endsWith(targetType)) {
                return imp;
            }
        }
        //maybe the same package
        for(String c: clzs) {
            if(c.endsWith(targetType)) {
                return c;
            }
        }
        //1. current package, 2. import by *
        return null;
    }

    private String parseTargetType(JavaParser.MethodCallContext ctx) {
        ParseTree targetCtx = ctx.getParent().getChild(0);
        String targetVar = targetCtx.getText();
        String targetType = targetVar;
        String parentCtxClz = targetCtx.getClass().getCanonicalName();
        if ("me.analysis.jv.JavaParser.MethodCallContext".equals(parentCtxClz)) {
            targetType = currentClz;
        } else if ("this".equalsIgnoreCase(targetVar)) {
            targetType = currentClz;
        } else if (targetVar.matches(".*new.*\\)\\..*") && isNotSpecialNewWord(targetVar)) {
            try {
                targetType = parseNewType(targetCtx);
                //System.out.println("Matched: " + targetVar + " , " +targetType);
            } catch (NullPointerException e) {
                //not create object new method. but name include new word.
                System.out.println(ctx.getParent().getText());
                //System.out.println(currentClz + " . " + currentMethodCall.getMethodName());
                //System.out.println(targetCtx.getText());
                e.printStackTrace();
            }
        } else {
            String fieldType = fields.get(targetVar);
            String formalType = formalParameters.get(targetVar);
            String localVarType = localVars.get(targetVar);
            if (fieldType != null) {
                targetType = fieldType;
            } else if (formalType != null) {
                targetType = formalType;
            } else if (localVarType != null) {
                targetType = localVarType;
            }
        }
        return targetType;
    }

    private boolean isNotSpecialNewWord(String targetVar) {
        return !targetVar.contains("inspectionnew") && !targetVar.contains("renew") && !targetVar.contains("Renew")
                && !targetVar.contains("newcoverages") && !targetVar.contains("newCoverages");
    }

    private String parseNewType(ParseTree ctx) {
        ParseTree creatorCxt = getJavaParserCreatorCxt(ctx);
        return creatorCxt.getChild(0).getText();
    }

    private ParseTree getJavaParserCreatorCxt(ParseTree ctx) {
        if ("me.analysis.jv.JavaParser.CreatorContext".equals(ctx.getClass().getCanonicalName())) {
            return ctx;
        } else {
            ParseTree res = null;
            for (int i = 0; i < ctx.getChildCount(); i++) {
                ParseTree c = getJavaParserCreatorCxt(ctx.getChild(i));
                if (c != null) {
                    res = c;
                    break;
                }
            }
            return res;
        }
    }

    @Override
    public Object visitFormalParameters(evolution.analysis.jv.JavaParser.FormalParametersContext ctx) {
        //System.out.println(ctx.getText());
        return super.visitFormalParameters(ctx);
    }

    @Override
    public Object visitFormalParameter(evolution.analysis.jv.JavaParser.FormalParameterContext ctx) {
        //System.out.println(ctx.typeType().getText() + ":" + ctx.variableDeclaratorId().getText() + "@me.analysis.jv.JavaParser.FormalParameterContext");
        formalParameters.put(ctx.variableDeclaratorId().getText(), ctx.typeType().getText());
        return super.visitFormalParameter(ctx);
    }

    @Override
    public Object visitFieldDeclaration(evolution.analysis.jv.JavaParser.FieldDeclarationContext ctx) {
        JavaParser.VariableDeclaratorsContext variableDeclaratorsContext = ctx.variableDeclarators();
        String variableName = variableDeclaratorsContext.getChild(0).getChild(0).getText();
        //System.out.println(ctx.typeType().getText() + ":" + variableName);
        fields.put(variableName, ctx.typeType().getText());
        return super.visitFieldDeclaration(ctx);
    }

    @Override
    public Object visitLocalVariableDeclaration(JavaParser.LocalVariableDeclarationContext ctx) {
        String typ = ctx.getChild(0).getText();
        String variableName = ctx.getChild(1).getChild(0).getChild(0).getText();
        localVars.put(variableName, typ);
        return super.visitChildren(ctx);
    }

    @Override
    public Object visitVariableDeclarators(evolution.analysis.jv.JavaParser.VariableDeclaratorsContext ctx) {
        return super.visitVariableDeclarators(ctx);
    }

    @Override
    public Object visitArguments(evolution.analysis.jv.JavaParser.ArgumentsContext ctx) {
        //调其他方法的参数
//        System.out.println("====");
//          System.out.println(ctx.getText()+"@me.analysis.jv.JavaParser.ArgumentsContext");
//        System.out.println("====");
        return super.visitArguments(ctx);
    }

}

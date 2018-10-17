package evolution.analysis.jv.calls;

import evolution.analysis.jv.JavaParser;
import evolution.analysis.jv.JavaParserBaseVisitor;
import evolution.analysis.jv.calls.model.JMethodCall;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class JavaCallVisitor extends JavaParserBaseVisitor {

    private static final Logger LOGGER = Logger.getLogger(JavaCallVisitor.class.getName());
    private Map<String, String> fields = new HashMap<>();
    private Map<String, String> formalParameters = new HashMap<>();
    private Map<String, String> localVars = new HashMap<>();
    private String currentClz = null;
    private String currentPkg = null;
    private List<String> imports = new ArrayList<>();
    private JavaDaoParser daoParser;

    private final List<String> clzs;
    private JMethodCall currentMethodCall;

    private List<JMethodCall> methodCalls;

    public JavaCallVisitor(List<JMethodCall> methodCalls,List<String> clzs,JavaDaoParser daoParser) {
        this.clzs = clzs;
        this.methodCalls = methodCalls;
        this.daoParser = daoParser;
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
        currentClz = ctx.IDENTIFIER().getText();
        return super.visitClassDeclaration(ctx);
    }

    @Override
    public Object visitTypeDeclaration(evolution.analysis.jv.JavaParser.TypeDeclarationContext ctx) {
        //System.out.println(ctx.getText());
        return super.visitTypeDeclaration(ctx);
    }

    @Override public Object visitInterfaceDeclaration(JavaParser.InterfaceDeclarationContext ctx) {
        currentClz = ctx.IDENTIFIER().getText();
        return super.visitChildren(ctx);
    }

    @Override public Object visitInterfaceMethodDeclaration(JavaParser.InterfaceMethodDeclarationContext ctx) {
        currentMethodCall = new JMethodCall();
        methodCalls.add(currentMethodCall);
        currentMethodCall.setPkg(currentPkg);
        currentMethodCall.setClz(currentClz);
        currentMethodCall.setMethodName(ctx.IDENTIFIER().getText());
        daoParser.parse(currentMethodCall,ctx.IDENTIFIER().getText());
        return super.visitChildren(ctx);
    }

    @Override
    public Object visitMethodDeclaration(evolution.analysis.jv.JavaParser.MethodDeclarationContext ctx) {
        //System.out.println("\nMethod: " + ctx.IDENTIFIER().getText());
        currentMethodCall = new JMethodCall();
        methodCalls.add(currentMethodCall);
        currentMethodCall.setPkg(currentPkg);
        currentMethodCall.setClz(currentClz);
        currentMethodCall.setMethodName(ctx.IDENTIFIER().getText());

        String body = ctx.getText().toUpperCase();
        daoParser.parse(currentMethodCall,body);

        return super.visitMethodDeclaration(ctx);
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
                LOGGER.info(ctx.getParent().getText());
                //System.out.println(currentClz + " . " + currentMethodCall.getMethodName());
                //System.out.println(targetCtx.getText());
                LOGGER.info(e.getMessage());
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

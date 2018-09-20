package evolution.analysis.jv.calls.model;

import java.util.*;

public class JMethodCall {
    private String pkg;
    private String clz;
    private String methodName;
    private Map<String,Set<String>> methodCalls = new HashMap<>();
    private Map<String,String> tableOps = new HashMap<>();
    private Set<String> procedures = new HashSet<>();

    public String getPkg() {
        return pkg;
    }

    public void setPkg(String pkg) {
        this.pkg = pkg;
    }

    public String getClz() {
        return clz;
    }

    public void setClz(String clz) {
        this.clz = clz;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Map<String, Set<String>> getMethodCalls() {
        return methodCalls;
    }

    public void addMethodCall(String targetType, String method) {
        //System.out.println(targetType+"->"+method);
        Set<String> methods = methodCalls.get(targetType);
        if(methods == null) {
            methods = new HashSet<>();
            methodCalls.put(targetType,methods);
        }
        methods.add(method);
    }

    public Set<String> getProcedures() {
        return procedures;
    }

    public void addProcedures(List<String> procedures) {
        for(String p:procedures) {
            this.procedures.add(p.toUpperCase());
        }
    }

    public Map<String, String> getTableOps() {
        return tableOps;
    }

    public void addTableOp(String table,String op) {
        this.tableOps.put(table.toUpperCase(),op.toUpperCase());
    }

    @Override
    public String toString() {
        return "JMethodCall{" +
                "pkg='" + pkg + '\'' +
                ", clz='" + clz + '\'' +
                ", methodName='" + methodName + '\'' +
                ", methodCalls=" + methodCalls +
                ", tableOps=" + tableOps +
                '}';
    }
}

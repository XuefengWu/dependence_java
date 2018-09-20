package evolution.analysis.jv.identifier;

import java.util.ArrayList;
import java.util.List;

public class JIdentifier {

    private String pkg;
    private String name;
    private String typ;

    private List<JMethod> methods = new ArrayList<>();

    public String getPkg() {
        return pkg;
    }

    public String getName() {
        return name;
    }


    public List<JMethod> getMethods() {
        return methods;
    }

    public void setPkg(String pkg) {
        this.pkg = pkg;
    }

    public void setName(String interfaceName) {
        this.name = interfaceName;
    }

    public void addMethod(JMethod method) {
        this.methods.add(method);
    }

    public String getType() {
        return typ;
    }

    public void setType(String typ) {
        this.typ = typ;
    }

    @Override
    public String toString() {
        return "JIdentifier{" +
                "pkg='" + pkg + '\'' +
                ", name='" + name + '\'' +
                ", methods=" + methods +
                '}';
    }

}

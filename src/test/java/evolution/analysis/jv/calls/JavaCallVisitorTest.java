package evolution.analysis.jv.calls;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class JavaCallVisitorTest  {

    @Test
    public void testParseProcedureCalls() {
        JavaCallVisitor visitor = new JavaCallVisitor(null,null);
        String body = "Stringsql=\"{call PKG_LS_CLM_CALC.P_GET_ANXIN_PRO_AMOUNT(?,?,?)} \";";
        List<String> res = visitor.parseProcedureCalls(body);
        Assertions.assertEquals(1,res.size());
        Assertions.assertEquals("PKG_LS_CLM_CALC.P_GET_ANXIN_PRO_AMOUNT",res.get(0));

        String body2 = "Stringsql=\" \";  " +
                "Stringsql=\"{call PKG_LS_CLM_CALC.P_GET_ANXIN_PRO_AMOUNT2(?,?,?)} \";";
        List<String> res2 = visitor.parseProcedureCalls(body2);
        Assertions.assertEquals(1,res2.size());
        Assertions.assertEquals("PKG_LS_CLM_CALC.P_GET_ANXIN_PRO_AMOUNT2",res2.get(0));

        String body3 = "Stringsql=\"{call PKG_LS_CLM_CALC.P_GET_ANXIN_PRO_AMOUNT(?,?,?)} \";" +
                "Stringsql=\"{call PKG_LS_CLM_CALC.P_GET_ANXIN_PRO_AMOUNT2(?,?,?)} \";";
        List<String> res3 = visitor.parseProcedureCalls(body3);
        Assertions.assertEquals(2,res3.size());
        Assertions.assertEquals("PKG_LS_CLM_CALC.P_GET_ANXIN_PRO_AMOUNT",res3.get(0));
        Assertions.assertEquals("PKG_LS_CLM_CALC.P_GET_ANXIN_PRO_AMOUNT2",res3.get(1));
    }
}

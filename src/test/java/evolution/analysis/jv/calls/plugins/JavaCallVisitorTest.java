package evolution.analysis.jv.calls.plugins;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

public class JavaCallVisitorTest  {

    private JavaDaoStringParser visitor = new JavaDaoStringParser();
    @Test
    public void testParseProcedureCalls() {
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

    @Test
    public void testParseTables() {
        String body = "newStringBuffer(\"select distinct tclp.item_id,\").append(\"               tpl.liab_id,\").append(\" tprl.product_abbr,\").append(\" tprl.product_name,\").append(\"             tliab.liab_name\").append(\"    from  t_Liab_Pay_Relative tpl,\").append(\"    t_claim_product     tclp,\").append(\"   t_product_life      tprl,\").append(\"   t_liability         tliab,\").append(\"   t_contract_master   tcm\").append(\" where tclp.policy_id = ? and tclp.product_id = tpl.product_id and\").append(\" tliab.liab_id = tpl.liab_id  and\").append(\" tclp.product_id = tprl.product_id and tclp.case_id = ? and\").append(\"        tcm.policy_id = tclp.policy_id \")";
        List<String> res = visitor.parseTables(body);
        Stream.of(res).forEach(System.out::println);
        Assertions.assertEquals(10,res.size());
        Assertions.assertEquals("SELECT",res.get(0));
    }


    @Test
    public void cleanTableName() {
        Assertions.assertEquals("T_HEALTH_PRODUCT_SPECIAL", visitor.cleanTableName("T_HEALTH_PRODUCT_SPECIAL)\\N"));
        Assertions.assertEquals("T_HEALTH_PRODUCT_SPECIAL", visitor.cleanTableName("T_HEALTH_PRODUCT_SPECIAL)"));
        Assertions.assertEquals("T_HEALTH_PRODUCT_SPECIAL", visitor.cleanTableName("T_HEALTH_PRODUCT_SPECIAL"));
        Assertions.assertEquals("T_CONTRACT_MASTER_LIAB_HI", visitor.cleanTableName("T_CONTRACT_MASTER_LIAB_HI"));
    }
}

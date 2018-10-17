package evolution.analysis.jv.calls.plugins;

import evolution.analysis.jv.calls.JavaDaoParser;
import evolution.analysis.jv.calls.model.JMethodCall;

import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaDaoStringParser implements JavaDaoParser {
    private static final Logger LOGGER = Logger.getLogger(JavaDaoStringParser.class.getName());

    @Override
    public void parse(JMethodCall currentMethod, String body) {
        Map<String,String> tableOpsMap = buildTableOps(body);
        currentMethod.addTableOps(tableOpsMap);
        LOGGER.info(currentMethod.getMethodName() + " use table size: " + currentMethod.getTableOps().size());
        List<String> procedures = parseProcedureCalls(body);
        currentMethod.addProcedures(procedures);
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


    private Map<String,String>  buildTableOps(String body) {
        Map<String,String> tableOpsMap = new HashMap<>();
        List<String> tableOps = parseTables(body);
        LOGGER.info("buildTableOps: " + tableOps.toString());
        if (tableOps == null || tableOps.size() == 0) {
            return tableOpsMap;
        }

        String op = tableOps.get(0);
        if(!isTableOperator(op)) {
            LOGGER.warning(body);
        }
        LOGGER.info("tableOps.size: " + tableOps.size());
        for (int i = 1; i < tableOps.size(); i++) {
            String s = tableOps.get(i);
            if (isTableOperator(s)) {
                op = s;
            } else {
                tableOpsMap.put(cleanTableName(s.toUpperCase()), op);
            }
        }

        return tableOpsMap;
    }


    public String cleanTableName(String table){
        String pattern = "([A-Z_]+).*";
        Pattern r = Pattern.compile(pattern);
        Matcher matcher =  r.matcher(table);
        matcher.find();
        return matcher.group(1);
    }


    private boolean isTableOperator(String s) {
        return "INSERT".equalsIgnoreCase(s)
                || "SELECT".equalsIgnoreCase(s)
                || "UPDATE".equalsIgnoreCase(s)
                || "DELETE".equalsIgnoreCase(s);
    }

    protected List<String> parseTables(String body) {
        List<String> result = new ArrayList<>();
        String[] ts = body.split("[\\s{2,}|\"|,]");
        Object[] ss = Arrays.stream(ts).filter(s -> !s.trim().isEmpty()).toArray();

        for (int i = 0; i < ss.length - 1; i++) {
            String s = ss[i].toString();
            if (s.startsWith("t_") || s.startsWith("T_") || s.startsWith("vdm_") || s.startsWith("VDM_") ) {
                //select from, insert into,delete from,update
                String op = parseTableOperate(ss, i);
                if (op != null) {
                    result.add(op);
                }
                result.add(s);
            }
        }
        return result;
    }

    private String parseTableOperate(Object[] ts, int index) {
        String pre = ts[index - 1].toString();
        while(!("from".equalsIgnoreCase(pre.trim()) || isTableOperator(pre))&&index>0) {
            index = index - 1;
            pre = ts[index - 1].toString();
        }
        String op = null;
        if ("update".equalsIgnoreCase(pre)) {
            op = "UPDATE";
        } else if ("into".equalsIgnoreCase(pre)) {
            op = "INSERT";
        } else if ("from".equalsIgnoreCase(pre)) {
            String prePre = ts[index - 2].toString();
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

}

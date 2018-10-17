package evolution.analysis.jv.calls.plugins;

import evolution.analysis.jv.calls.JavaDaoParser;
import evolution.analysis.jv.calls.model.JMethodCall;

import evolution.analysis.jv.calls.plugins.sql.TableOpParser;
import evolution.analysis.jv.calls.plugins.sql.TableVisitor;
import org.antlr.v4.runtime.tree.ParseTree;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import java.io.*;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class MyBatisParser implements JavaDaoParser {

    private static final Logger LOGGER = Logger.getLogger(MyBatisParser.class.getName());

    private String rootDir;
    public MyBatisParser(String rootDir) {
        this.rootDir = rootDir;
    }
    @Override
    public void parse(JMethodCall currentMethod, String body) {
        String pkg = currentMethod.getPkg();
        String clz = currentMethod.getClz();
        String clzFullName = pkg + "." + clz;
        String file = String.format("%s/src/main/resources/%s.xml",rootDir,clzFullName.replaceAll("\\.","/"));
        String sql = "";
        try {
            sql = findMethodSql(file,currentMethod.getMethodName());

            System.out.println(currentMethod.getMethodName());
            //System.out.println(sql.trim());
            Map<String,String> tableOpsMap = parse(replaceVariable(sql.trim()));
            System.out.println(tableOpsMap);
            currentMethod.addTableOps(tableOpsMap);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    private Map<String,String> parse(String sql) throws IOException {
        Map<String,String> tableOpsMap = new HashMap<>();
        TableOpParser parser = new TableOpParser();
        ParseTree tree = parser.parse(sql);
        tree.accept(new TableVisitor(tableOpsMap));
        return tableOpsMap;
    }

    private String replaceVariable(String sql) {
        return sql.replaceAll("#\\{","").replaceAll("\\}","");
    }

    private String findMethodSql(String path, String method) throws ParserConfigurationException, IOException, SAXException {
        File inputFile = new File(path);
        //parse mapper xml
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(inputFile);
        doc.getDocumentElement().normalize();
        //find method

        NodeList nList = doc.getDocumentElement().getChildNodes();
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            if(nNode.getAttributes() != null && nNode.getAttributes().getNamedItem("id") != null) {
                String id = nNode.getAttributes().getNamedItem("id").getNodeValue();
                if (method.equalsIgnoreCase(id)) {
                    //extract sql string
                    String sql = nNode.getLastChild().getNodeValue();
                    return sql;
                }
            }
        }
        return "";
    }
}

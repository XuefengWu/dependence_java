package evolution.factory.daoparser;

import evolution.analysis.jv.calls.JavaDaoParser;
import evolution.analysis.jv.calls.plugins.JavaDaoStringParser;
import evolution.analysis.jv.calls.plugins.MyBatisParser;

public class JavaDaoParserFactory {

    private final DaoParserProvider provider;

    public JavaDaoParserFactory(DaoParserProvider provider) {
        this.provider = provider;
    }

    public JavaDaoParser createDaoParser()  {
        DaoParserTypeEnum daoParserType = this.provider.getDaoParserType();
        if(daoParserType == DaoParserTypeEnum.StringParser) {
            return new JavaDaoStringParser();
        }
        if(daoParserType == DaoParserTypeEnum.MyBatisParser) {
            return new MyBatisParser(this.provider.getDaoParseRootPath());
        }
        throw new OutOfJavaDaoParserEnumError(daoParserType);
    }

}

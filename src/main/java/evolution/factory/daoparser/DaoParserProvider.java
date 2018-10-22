package evolution.factory.daoparser;

import evolution.factory.daoparser.DaoParserTypeEnum;

public interface DaoParserProvider {
    DaoParserTypeEnum getDaoParserType();

    String getDaoParseRootPath();
}

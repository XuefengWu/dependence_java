package evolution.configuration;

import evolution.factory.daoparser.DaoParserTypeEnum;
import evolution.factory.daoparser.DaoParserProvider;
import io.helidon.config.Config;

public class ConfigReader implements DaoParserProvider {
    private final Config CONFIG = Config.create().get("app");
    @Override
    public DaoParserTypeEnum getDaoParserType() {
        String daoParseType = CONFIG.get("dao-parse-type").asString();
        return DaoParserTypeEnum.valueOf(daoParseType);
    }


    @Override
    public String getDaoParseRootPath() {
        return CONFIG.get("root").asString("~/workspace");
    }
}

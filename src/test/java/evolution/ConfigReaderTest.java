package evolution;

import evolution.configuration.ConfigReader;
import evolution.factory.daoparser.DaoParserTypeEnum;
import evolution.factory.daoparser.DaoParserProvider;
import org.junit.jupiter.api.Test;

class ConfigReaderTest {

    @Test
    void should_get_correct_dao_parse_type_as_config_setting() {
        DaoParserProvider reader = new ConfigReader();
        DaoParserTypeEnum daoParserType = reader.getDaoParserType();
        assert(daoParserType).equals(DaoParserTypeEnum.MyBatisParser);
    }
}
package evolution.factory.daoparser;

import evolution.analysis.jv.calls.JavaDaoParser;
import evolution.analysis.jv.calls.plugins.JavaDaoStringParser;
import evolution.analysis.jv.calls.plugins.MyBatisParser;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

class JavaDaoParserFactoryTest {

    @Test
    void should_create_myBatis_parser_when_type_is_mybatis() {
        DaoParserProvider provider = new FakeConfigReader(DaoParserTypeEnum.MyBatisParser, "");
        JavaDaoParserFactory factory = new JavaDaoParserFactory(provider);
        JavaDaoParser daoParser = factory.createDaoParser();

        assertThat(daoParser, instanceOf(MyBatisParser.class));
    }

    @Test
    void should_create_java_string_dao_parser_when_type_is_stringParser() {
        DaoParserProvider provider = new FakeConfigReader(DaoParserTypeEnum.StringParser, "");
        JavaDaoParserFactory factory = new JavaDaoParserFactory(provider);
        JavaDaoParser daoParser = factory.createDaoParser();

        assertThat(daoParser, instanceOf(JavaDaoStringParser.class));
    }

}
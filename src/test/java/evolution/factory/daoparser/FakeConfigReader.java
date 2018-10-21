package evolution.factory.daoparser;

public class FakeConfigReader implements DaoParserProvider {
    private final DaoParserTypeEnum daoParserTypeEnum;
    private final String parsRootPath;

    public FakeConfigReader(DaoParserTypeEnum daoParserTypeEnum, String parsRootPath) {
        this.daoParserTypeEnum = daoParserTypeEnum;
        this.parsRootPath = parsRootPath;
    }

    @Override
    public DaoParserTypeEnum getDaoParserType() {
        return this.daoParserTypeEnum;
    }

    @Override
    public String getDaoParseRootPath() {
        return this.parsRootPath;
    }
}

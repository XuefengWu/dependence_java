package evolution.factory;

import evolution.analysis.jv.ParseClassService;
import evolution.analysis.jv.ParsePackageService;
import evolution.factory.daoparser.DaoParserProvider;
import evolution.factory.daoparser.JavaDaoParserFactory;

public class ServiceFactory {
    private JavaDaoParserFactory javaDaoParserFactory;

    public ServiceFactory(DaoParserProvider provider) {
        javaDaoParserFactory = new JavaDaoParserFactory(provider);
    }

    public ParsePackageService createParsePackageService() {
        return new ParsePackageService(javaDaoParserFactory);
    }

    public ParseClassService createParseClassService() {
        return new ParseClassService(javaDaoParserFactory);
    }
}

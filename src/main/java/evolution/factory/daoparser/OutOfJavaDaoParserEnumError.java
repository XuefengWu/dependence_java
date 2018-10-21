package evolution.factory.daoparser;

class OutOfJavaDaoParserEnumError extends Error {
    OutOfJavaDaoParserEnumError(DaoParserTypeEnum daoParserType) {
        super(String.format("the type %s is out of range", daoParserType.toString()));
    }
}

package evolution.analysis.jv.calls;

import evolution.analysis.jv.calls.model.JMethodCall;

public interface JavaDaoParser {
    void parse(JMethodCall currentMethodCall, String body);
}

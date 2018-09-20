package evolution.analysis.jv.identifier;

public class JMethod {

    private String name;
    private int startLine;
    private int startLinePosition;
    private int stopLine;
    private int stopLinePosition;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStartLine() {
        return startLine;
    }

    public void setStartLine(int startLine) {
        this.startLine = startLine;
    }

    public int getStartLinePosition() {
        return startLinePosition;
    }

    public void setStartLinePosition(int startLinePosition) {
        this.startLinePosition = startLinePosition;
    }

    public int getStopLine() {
        return stopLine;
    }

    public void setStopLine(int stopLine) {
        this.stopLine = stopLine;
    }

    public int getStopLinePosition() {
        return stopLinePosition;
    }

    public void setStopLinePosition(int stopLinePosition) {
        this.stopLinePosition = stopLinePosition;
    }
}

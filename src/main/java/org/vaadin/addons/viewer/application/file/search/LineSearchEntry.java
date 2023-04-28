package org.vaadin.addons.viewer.application.file.search;

/**
 * Container object for grid
 */
public class LineSearchEntry {

    private final String line;
    private final int lineNumber;

    private LineSearchEntry(String line, int lineNumber) {
        this.line = line;
        this.lineNumber = lineNumber;
    }

    public static LineSearchEntry of(String line, int index) {
        return new LineSearchEntry(line, index);
    }

    public String getLine() {
        return line;
    }

    public int getLineNumber() {
        return lineNumber;
    }
}

package org.vaadin.addons.viewer.application.file.index;

/**
 * Container object for grid
 */
public class LineEntry {

    private final String log;

    private LineEntry(String log) {
        this.log = log;
    }

    public static LineEntry of(String line) {
        return new LineEntry(line);
    }

    public String getLog() {
        return log;
    }
}

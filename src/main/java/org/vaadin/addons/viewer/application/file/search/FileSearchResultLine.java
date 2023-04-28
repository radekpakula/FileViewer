package org.vaadin.addons.viewer.application.file.search;

/**
 * The container object for the found line in the file. Contains position information as well as line number.
 */
public class FileSearchResultLine {

    private final int line;
    private final long position;

    private FileSearchResultLine(int line, long position) {
        this.line = line;
        this.position = position;
    }

    public static FileSearchResultLine of(int line, long position) {
        return new FileSearchResultLine(line, position);
    }

    public int getLine() {
        return line;
    }

    public long getPosition() {
        return position;
    }
}

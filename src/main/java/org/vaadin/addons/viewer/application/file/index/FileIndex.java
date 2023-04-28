
package org.vaadin.addons.viewer.application.file.index;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * This class store index for given file
 */
public class FileIndex {

    /**
     * This configured result per page that will be returned to UI.
     * This value must be equal to index configuration (index per items count)
     */
    public static final int INDEX_PER_ROWINDEX_PER_ROW = 50;

    private final Path path;
    private final List<Long> lineCursor = new ArrayList<>();

    private long indexedSize;
    private int linesCount;

    public FileIndex(Path path) {
        this.path = path;
        //First index. Beginning of the file
        lineCursor.add(0L);
    }

    public Path getPath() {
        return path;
    }

    /**
     *
     */
    public int getMaxIndex() {
        return linesCount;
    }

    /**
     * Return position in the file for the given index.
     */
    public long getPage(int i) {
        if (i == lineCursor.size()) {
            return indexedSize;
        }
        return lineCursor.get(i);
    }

    long getIndexedSize() {
        return indexedSize;
    }

    void setIndexedSize(long indexedSize) {
        this.indexedSize = indexedSize;
    }

    List<Long> getLineCursor() {
        return lineCursor;
    }

    void newLineAtPosition(long position) {
        if (++linesCount % FileIndex.INDEX_PER_ROWINDEX_PER_ROW == 0) {
            lineCursor.add(position);
        }
    }
}

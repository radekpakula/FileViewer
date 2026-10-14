package org.vaadin.addons.viewer.application.file.index;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.vaadin.addons.viewer.application.file.EncodedLineReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FileIndex {

    public static final int INDEX_PER_ROWINDEX_PER_ROW = 50;

    private final Path path;
    private final Charset charset;
    private final List<Long> lineCursor = new ArrayList<>();
    private long indexedSize;
    private int linesCount;

    public FileIndex(Path path) {
        this(path, StandardCharsets.UTF_8);
    }

    public FileIndex(Path path, Charset charset) {
        this.path = Objects.requireNonNull(path);
        this.charset = Objects.requireNonNull(charset);
        EncodedLineReader.validateCharset(charset);
        lineCursor.add(0L);
    }

    public Path getPath() {
        return path;
    }

    public Charset getCharset() {
        return charset;
    }

    public int getMaxIndex() {
        return linesCount;
    }

    public long getPosition(int i) {
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
        if (++linesCount % INDEX_PER_ROWINDEX_PER_ROW == 0) {
            lineCursor.add(position);
        }
    }
}

package org.vaadin.addons.viewer.application.file.search;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FileSearchIndex {

    public static final int RESULT_PER_PAGE = 20;

    private final Path path;
    private final Charset charset;
    private final List<FileSearchResultLine> lineCursor = new ArrayList<>();
    private boolean searchDone;
    private long processingTime;
    private long processingPosition;
    private long fileSize;
    private boolean interrupt;
    private int lineCounter;

    public FileSearchIndex(Path path) {
        this(path, StandardCharsets.UTF_8);
    }

    public FileSearchIndex(Path path, Charset charset) {
        this.path = Objects.requireNonNull(path);
        this.charset = Objects.requireNonNull(charset);
    }

    public Charset getCharset() {
        return charset;
    }

    public int getMatchCount() {
        return lineCursor.size();
    }

    public Path getPath() {
        return path;
    }

    public FileSearchResultLine getCursor(int i) {
        return lineCursor.get(i);
    }

    public long getProcessingTime() {
        return processingTime;
    }

    void setProcessingTime(long processingTime) {
        this.processingTime = processingTime;
    }

    public double processingPercentage() {
        if (fileSize == 0) {
            return searchDone ? 1 : 0;
        }
        return Math.min(1, (double) processingPosition / fileSize);
    }

    public void interrupt() {
        this.interrupt = true;
    }

    public boolean isFinishSearching() {
        return searchDone;
    }

    void increaseLineCounter() {
        ++lineCounter;
    }

    int getLineCounter() {
        return lineCounter;
    }

    FileSearchResultLine getLastEntry() {
        return lineCursor.get(lineCursor.size() - 1);
    }

    void removeLastEntryIfEqualToCurrent(long beginningOfTheLineMarker) {
        if (getLineCounter() > 0 && getLastEntry().getPosition() == beginningOfTheLineMarker) {
            lineCursor.remove(lineCursor.size() - 1);
            --lineCounter;
        }
    }

    void updateFileSize() {
        this.fileSize = getPath().toFile().length();
    }

    void addPointer(FileSearchResultLine result) {
        lineCursor.add(result);
    }

    long getProcessingPosition() {
        return processingPosition;
    }

    void increasePosition() {
        processingPosition++;
    }

    long getFileSize() {
        return fileSize;
    }

    boolean isInterrupt() {
        return interrupt;
    }

    void setSearchDone() {
        this.searchDone = true;
    }
}

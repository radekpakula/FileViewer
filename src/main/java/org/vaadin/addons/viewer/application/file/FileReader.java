package org.vaadin.addons.viewer.application.file;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addons.viewer.application.exception.ViewerException;
import org.vaadin.addons.viewer.application.file.index.FileIndex;
import org.vaadin.addons.viewer.application.file.index.LineEntry;
import org.vaadin.addons.viewer.application.file.search.FileSearchIndex;
import org.vaadin.addons.viewer.application.file.search.FileSearchResultLine;
import org.vaadin.addons.viewer.application.file.search.LineSearchEntry;

public final class FileReader {
    private static final Logger LOG = LoggerFactory.getLogger(FileReader.class);
    private FileReader() {}

    public static List<LineEntry> readLines(FileIndex fileIndex, int offset, int limit) {
        Path path = fileIndex.getPath();
        LOG.debug("Request {} limit {}", offset, limit);
        int maxLine = fileIndex.getMaxIndex() + 1;
        if (offset >= maxLine) {
            return List.of();
        }
        limit = Math.min(limit, maxLine - offset);
        int index = offset / FileIndex.INDEX_PER_ROWINDEX_PER_ROW;
        long rowFrom = fileIndex.getPosition(index);
        if (index > 0) {
            rowFrom++;
        }
        List<LineEntry> lines = new ArrayList<>();
        try (RandomAccessFile raf = new RandomAccessFile(path.toFile(), "r")) {
            raf.seek(rowFrom);
            EncodedLineReader reader = new EncodedLineReader(raf, fileIndex.getCharset());
            for (int i = 0; i < offset % FileIndex.INDEX_PER_ROWINDEX_PER_ROW; i++) {
                if (reader.readLine() == null) {
                    return lines;
                }
            }
            for (int i = 0; i < limit; i++) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                lines.add(LineEntry.of(line));
            }
        } catch (IOException e) {
            throw new ViewerException(e);
        }
        return lines;
    }

    public static List<LineSearchEntry> readLines(FileSearchIndex fileIndex, int from, int to) {
        List<LineSearchEntry> lines = new ArrayList<>();
        to = Math.min(to, fileIndex.getMatchCount());
        try (RandomAccessFile raf = new RandomAccessFile(fileIndex.getPath().toFile(), "r")) {
            for (int i = from; i < to; i++) {
                FileSearchResultLine index = fileIndex.getCursor(i);
                raf.seek(index.getPosition());
                String line = new EncodedLineReader(raf, fileIndex.getCharset()).readLine();
                lines.add(LineSearchEntry.of(line, index.getLine()));
            }
        } catch (IOException e) {
            throw new ViewerException(e);
        }
        return lines;
    }

    public static LineSearchEntry readLine(Path path, long rowFrom, int lineNumber) {
        try (RandomAccessFile raf = new RandomAccessFile(path.toFile(), "r")) {
            raf.seek(rowFrom);
            return LineSearchEntry.of(new EncodedLineReader(raf, StandardCharsets.UTF_8).readLine(), lineNumber);
        } catch (IOException e) {
            throw new ViewerException(e);
        }
    }
}

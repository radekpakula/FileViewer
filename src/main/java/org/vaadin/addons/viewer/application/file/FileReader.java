
package org.vaadin.addons.viewer.application.file;

import java.io.IOException;
import java.io.RandomAccessFile;
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

/**
 * Reads a file from, to the specified location.
 */
public final class FileReader {

    private static final Logger LOG = LoggerFactory.getLogger(FileReader.class);

    private FileReader() {
    }

    public static List<LineEntry> readLines(FileIndex fileIndex, int offset, int limit) {
        Path path = fileIndex.getPath();
        LOG.debug("Request {} limit {}", offset, limit);
        if (offset + limit > fileIndex.getMaxIndex()) {
            limit = limit - ((limit + offset) - fileIndex.getMaxIndex()) + 1;
        }
        int index = offset == 0 ? 0 : offset / FileIndex.INDEX_PER_ROWINDEX_PER_ROW;

        long rowFrom = fileIndex.getPage(index);
        if (rowFrom > 0) {
            ++rowFrom;
        }
        long rowTo = fileIndex.getPage(index + 1);

        validateParameters(path, rowFrom, rowTo);

        List<LineEntry> lines = new ArrayList<>();
        try (RandomAccessFile raf = new RandomAccessFile(path.toFile(), "r")) {
            raf.seek(rowFrom);
            for (int i = 0; i < limit; i++) {
                final String line = raf.readLine();
                lines.add(LineEntry.of(line));
            }
        } catch (IOException e) {
            throw new ViewerException(e);
        }
        return lines;
    }

    public static List<LineSearchEntry> readLines(FileSearchIndex fileIndex, int from, int to) {
        Path path = fileIndex.getPath();
        List<LineSearchEntry> lines = new ArrayList<>();
        if (to > fileIndex.getMatchCount()) {
            to = fileIndex.getMatchCount();
        }
        try (RandomAccessFile raf = new RandomAccessFile(path.toFile(), "r")) {
            for (int i = from; i < to; i++) {
                FileSearchResultLine index = fileIndex.getCursor(i);
                raf.seek(index.getPosition());
                lines.add(LineSearchEntry.of(raf.readLine(), index.getLine()));
            }
        } catch (IOException e) {
            throw new ViewerException(e);
        }

        return lines;
    }

    private static void validateParameters(Path path, long rowFrom, long rowTo) {
        if (!path.toFile().exists()) {
            throw new ViewerException(String.format("File no longer exists! %s", path));
        }
        if (path.toFile().length() < rowFrom || path.toFile().length() < rowTo) {
            throw new ViewerException(String.format(
                    "The given position is bigger then file size. size: %s, from: %s, to: %s",
                    path.toFile().length(), rowFrom, rowTo));
        }
    }
}

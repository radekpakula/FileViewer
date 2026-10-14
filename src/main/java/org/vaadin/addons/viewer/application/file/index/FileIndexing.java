package org.vaadin.addons.viewer.application.file.index;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addons.viewer.application.exception.ViewerException;

import java.io.IOException;
import java.io.RandomAccessFile;

public final class FileIndexing {
    private static final Logger LOG = LoggerFactory.getLogger(FileIndexing.class);

    private FileIndexing() {}

    public static FileIndex indexFile(FileIndex fileIndex) {
        long start = System.currentTimeMillis();
        long position = fileIndex.getIndexedSize();
        long to = fileIndex.getPath().toFile().length();

        try (RandomAccessFile raf = new RandomAccessFile(fileIndex.getPath().toFile(), "r")) {
            if (position > to) {
                throw new ViewerException("File was truncated; create a new index before indexing it again");
            }
            raf.seek(position);
            while (position < to) {
                int value = raf.read();
                if (value == -1) {
                    break;
                }
                if (value == '\r') {
                    long carriageReturnPosition = position;
                    int next = raf.read();
                    if (next == '\n') {
                        position++;
                        fileIndex.newLineAtPosition(position);
                    } else if (next == -1) {
                        position = carriageReturnPosition;
                        break;
                    } else {
                        fileIndex.newLineAtPosition(carriageReturnPosition);
                        raf.seek(position + 1);
                    }
                } else if (value == '\n') {
                    fileIndex.newLineAtPosition(position);
                }
                position++;
            }
        } catch (IOException e) {
            throw new ViewerException(e);
        }
        fileIndex.setIndexedSize(to);
        LOG.debug("Indexing of the file {} took {} ms. File length {} found lines {}",
                fileIndex.getPath(), System.currentTimeMillis() - start, fileIndex.getIndexedSize(),
                fileIndex.getMaxIndex() + 1);
        return fileIndex;
    }
}

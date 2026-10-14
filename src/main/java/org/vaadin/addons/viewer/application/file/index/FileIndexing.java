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
        byte[] newline = "\n".getBytes(fileIndex.getCharset());

        try (RandomAccessFile raf = new RandomAccessFile(fileIndex.getPath().toFile(), "r")) {
            if (position > to) {
                throw new ViewerException("File was truncated; create a new index before indexing it again");
            }
            raf.seek(position);
            int matched = 0;
            int value;
            while (position < to && (value = raf.read()) != -1) {
                byte current = (byte) value;
                if (current == newline[matched]) {
                    matched++;
                    if (matched == newline.length) {
                        fileIndex.newLineAtPosition(position - newline.length + 1);
                        matched = 0;
                    }
                } else {
                    matched = current == newline[0] ? 1 : 0;
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

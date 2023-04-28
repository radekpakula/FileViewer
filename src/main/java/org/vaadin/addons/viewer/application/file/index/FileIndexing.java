
package org.vaadin.addons.viewer.application.file.index;

import java.io.IOException;
import java.io.RandomAccessFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addons.viewer.application.exception.ViewerException;

/**
 * The function of this class is to count the occurrence of a newline in the entire file.
 * Every item on the nth line configured by  FileIndex.INDEX_PER_ROWINDEX_PER_ROW is added to the index.
 */
public final class FileIndexing {

    private static final Logger LOG = LoggerFactory.getLogger(FileIndexing.class);

    private static final int defaultBufferSize = 8192;

    public static FileIndex updateIndex(FileIndex fileIndex) {
        long start = System.currentTimeMillis();
        long position = fileIndex.getIndexedSize();
        long to = fileIndex.getPath().toFile().length();

        if (fileIndex.getIndexedSize() != 0) {
            ++position;
        }

        try (RandomAccessFile raf = new RandomAccessFile(fileIndex.getPath().toFile(), "r")) {
            raf.seek(position);
            byte[] buf = new byte[defaultBufferSize];
            int charsRead = raf.read(buf, 0, buf.length);

            while (charsRead != -1) {
                for (int i = 0; i < charsRead; i++) {
                    char c = (char) buf[i];

                    if (c == '\n') {
                        fileIndex.newLineAtPosition(position);
                    }
                    ++position;
                }
                charsRead = raf.read(buf, 0, buf.length);
            }

        } catch (IOException e) {
            throw new ViewerException(e);
        }

        fileIndex.setIndexedSize(to);

        LOG.info("Indexing of the file {} took {} ms. File length {} found lines {}",
                fileIndex.getPath(), (System.currentTimeMillis() - start),
                fileIndex.getIndexedSize(), fileIndex.getMaxIndex() + 1);
        return fileIndex;
    }
}

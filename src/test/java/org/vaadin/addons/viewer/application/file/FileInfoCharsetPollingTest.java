package org.vaadin.addons.viewer.application.file;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileInfoCharsetPollingTest {

    @TempDir
    Path tempDir;

    @Test
    void whenFileIsAppendedThenPollingDetectsChangeForEachSelectedCharset() throws Exception {
        Path file = tempDir.resolve("polled.log");
        Files.write(file, "zażółć\n".getBytes(StandardCharsets.UTF_8));

        FileInfo utf8Info = FileInfo.of(file, StandardCharsets.UTF_8);
        FileInfo windowsInfo = FileInfo.of(file, Charset.forName("windows-1250"));
        assertNotEquals(utf8Info, windowsInfo);
        assertFalse(utf8Info.isFileChanged());
        assertFalse(windowsInfo.isFileChanged());

        Files.write(file, "nowa linia\n".getBytes(StandardCharsets.UTF_8), java.nio.file.StandardOpenOption.APPEND);

        assertTrue(utf8Info.isFileChanged());
        assertTrue(windowsInfo.isFileChanged());

        utf8Info.updateFileLength();
        windowsInfo.updateFileLength();

        assertFalse(utf8Info.isFileChanged());
        assertFalse(windowsInfo.isFileChanged());
    }

    @Test
    void whenFileMetadataReadFailsThenPollingCheckThrowsForCallerToHandle() throws Exception {
        Path path = tempDir.resolve("metadata-error.log");
        Files.writeString(path, "line\\n", StandardCharsets.UTF_8);
        FailingLengthFile file = new FailingLengthFile(path.toFile());
        FileInfo fileInfo = FileInfo.of(file);

        file.failOnLength = true;

        assertThrows(SecurityException.class, fileInfo::isFileChanged);
    }

    @Test
    void whenFileMetadataUpdateFailsThenRememberedLengthIsNotUpdated() throws Exception {
        Path path = tempDir.resolve("metadata-update-error.log");
        Files.writeString(path, "line\\n", StandardCharsets.UTF_8);
        FailingLengthFile file = new FailingLengthFile(path.toFile());
        FileInfo fileInfo = FileInfo.of(file);
        long previousLength = fileInfo.length;
        Files.writeString(path, "next line\\n", StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.APPEND);

        file.failOnLength = true;

        assertThrows(SecurityException.class, fileInfo::updateFileLength);
        file.failOnLength = false;

        assertTrue(fileInfo.isFileChanged());
        assertTrue(previousLength < file.length());
    }

    private static final class FailingLengthFile extends File {
        private boolean failOnLength;

        private FailingLengthFile(File file) {
            super(file.getPath());
        }

        @Override
        public long length() {
            if (failOnLength) {
                throw new SecurityException("Simulated file metadata read failure");
            }
            return super.length();
        }
    }
}

package org.vaadin.addons.viewer.application.file;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileInfoCharsetPollingTest {

    @TempDir
    Path tempDir;

    @Test
    void whenFileContentChangesWithoutLengthChangeThenDetectsModification() throws Exception {
        Path file = tempDir.resolve("same-size-change.log");
        Files.writeString(file, "first\n", StandardCharsets.UTF_8);
        FileInfo info = FileInfo.of(file, StandardCharsets.UTF_8);
        long originalLength = Files.size(file);
        long modifiedTime = Files.getLastModifiedTime(file).toMillis();

        Files.writeString(file, "other\n", StandardCharsets.UTF_8);
        Files.setLastModifiedTime(file, java.nio.file.attribute.FileTime.fromMillis(modifiedTime + 5000));

        assertTrue(Files.size(file) == originalLength);
        assertTrue(info.isFileChanged());

        info.updateFileLength();
        assertFalse(info.isFileChanged());
    }

    @Test
    void whenFileIsTruncatedThenPollingDetectsChange() throws Exception {
        Path file = tempDir.resolve("truncated.log");
        Files.writeString(file, "first line\\nsecond line\\n", StandardCharsets.UTF_8);
        FileInfo info = FileInfo.of(file, StandardCharsets.UTF_8);

        Files.writeString(file, "short\\n", StandardCharsets.UTF_8);

        assertTrue(info.isFileChanged());
        assertTrue(info.requiresIndexRebuild());

        info.updateFileLength();
        assertFalse(info.isFileChanged());
    }

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
}

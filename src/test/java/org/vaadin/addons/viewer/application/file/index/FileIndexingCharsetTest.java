package org.vaadin.addons.viewer.application.file.index;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.vaadin.addons.viewer.application.file.FileReader;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.vaadin.addons.viewer.application.exception.ViewerException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileIndexingCharsetTest {

    @TempDir
    Path tempDir;

    @Test
    void whenIndexingUtf8FileThenReadsDecodedLines() throws Exception {
        Path file = tempDir.resolve("utf8.log");
        Files.writeString(file, "Zażółć gęślą jaźń\n第二行\nlast line", StandardCharsets.UTF_8);

        FileIndex index = FileIndexing.indexFile(new FileIndex(file, StandardCharsets.UTF_8));

        assertEquals(2, index.getMaxIndex());
        assertEquals(List.of("Zażółć gęślą jaźń", "第二行", "last line"),
                FileReader.readLines(index, 0, 10).stream().map(e -> e.getLog()).toList());
    }

    @Test
    void whenIndexingWindows1250FileThenReadsDecodedLines() throws Exception {
        Charset charset = Charset.forName("windows-1250");
        Path file = tempDir.resolve("windows-1250.log");
        Files.write(file, "Zażółć gęślą jaźń\nPchnąć w tę łódź jeża lub ośm skrzyń fig\n".getBytes(charset));

        FileIndex index = FileIndexing.indexFile(new FileIndex(file, charset));

        assertEquals(2, index.getMaxIndex());
        assertEquals(List.of("Zażółć gęślą jaźń", "Pchnąć w tę łódź jeża lub ośm skrzyń fig"),
                FileReader.readLines(index, 0, 10).stream().map(e -> e.getLog()).toList());
    }

    @Test
    void whenReadingPastFiftiethIndexedLineThenStartsAtCorrectLine() throws Exception {
        Path file = tempDir.resolve("many-lines.log");
        List<String> expected = java.util.stream.IntStream.rangeClosed(1, 120)
                .mapToObj(i -> "linia-" + i + " żółć")
                .toList();
        Files.writeString(file, String.join("\n", expected), StandardCharsets.UTF_8);

        FileIndex index = FileIndexing.indexFile(new FileIndex(file, StandardCharsets.UTF_8));

        assertEquals(expected.subList(49, 60),
                FileReader.readLines(index, 49, 11).stream().map(e -> e.getLog()).toList());
        assertEquals(expected.subList(99, 110),
                FileReader.readLines(index, 99, 11).stream().map(e -> e.getLog()).toList());
    }

    @Test
    void whenIndexingCrLfAndCrSeparatedFileThenReadsAllLines() throws Exception {
        Path file = tempDir.resolve("mixed-newlines.log");
        Files.writeString(file, "first\r\nsecond\rthird\nlast", StandardCharsets.UTF_8);

        FileIndex index = FileIndexing.indexFile(new FileIndex(file, StandardCharsets.UTF_8));

        assertEquals(List.of("first", "second", "third", "last"),
                FileReader.readLines(index, 0, 10).stream().map(e -> e.getLog()).toList());
    }

    @Test
    void whenIndexingEmptyFileThenReturnsNoLines() throws Exception {
        Path file = tempDir.resolve("empty.log");
        Files.createFile(file);

        FileIndex index = FileIndexing.indexFile(new FileIndex(file, StandardCharsets.UTF_8));

        assertEquals(List.of(), FileReader.readLines(index, 0, 10));
    }

    @Test
    void whenFileIsTruncatedThenRejectsExistingIndex() throws Exception {
        Path file = tempDir.resolve("truncated.log");
        Files.writeString(file, "first\nsecond\n", StandardCharsets.UTF_8);
        FileIndex index = FileIndexing.indexFile(new FileIndex(file, StandardCharsets.UTF_8));

        Files.writeString(file, "x", StandardCharsets.UTF_8);

        assertThrows(ViewerException.class, () -> FileIndexing.indexFile(index));
    }

    @Test
    void whenAppendingCrLfAcrossIndexingRunsThenCountsSingleLineBreak() throws Exception {
        Path file = tempDir.resolve("split-crlf.log");
        Files.writeString(file, "first\r", StandardCharsets.UTF_8);
        FileIndex index = FileIndexing.indexFile(new FileIndex(file, StandardCharsets.UTF_8));

        Files.writeString(file, "\nsecond", StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.APPEND);
        FileIndexing.indexFile(index);

        assertEquals(List.of("first", "second"),
                FileReader.readLines(index, 0, 10).stream().map(e -> e.getLog()).toList());
    }

    @Test
    void whenIndexingAfterFileWasExtendedThenAddsOnlyNewLines() throws Exception {
        Path file = tempDir.resolve("append.log");
        Files.writeString(file, "pierwsza\n", StandardCharsets.UTF_8);
        FileIndex index = FileIndexing.indexFile(new FileIndex(file, StandardCharsets.UTF_8));

        Files.writeString(file, "druga żółć\ntrzecia", StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.APPEND);
        FileIndexing.indexFile(index);

        assertEquals(List.of("pierwsza", "druga żółć", "trzecia"),
                FileReader.readLines(index, 0, 10).stream().map(e -> e.getLog()).toList());
    }
}

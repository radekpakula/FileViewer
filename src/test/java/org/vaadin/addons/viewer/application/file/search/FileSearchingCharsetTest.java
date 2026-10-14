package org.vaadin.addons.viewer.application.file.search;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.vaadin.addons.viewer.application.file.FileReader;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileSearchingCharsetTest {

    @TempDir
    Path tempDir;

    @Test
    void whenSearchingUtf8FileThenMatchesDecodedTextAndCorrectLineNumbers() throws Exception {
        Path file = tempDir.resolve("utf8-search.log");
        Files.writeString(file, "pierwsza linia\nżółć i gęślą\ntrzecia linia\n", StandardCharsets.UTF_8);
        SearchForm form = new SearchForm();
        form.setText("żółć");
        FileSearchIndex index = FileSearching.search(form, new FileSearchIndex(file, StandardCharsets.UTF_8));

        assertTrue(index.isFinishSearching());
        assertEquals(1, index.getMatchCount());
        assertEquals(2, index.getCursor(0).getLine());
        assertEquals("żółć i gęślą", FileReader.readLines(index, 0, 1).get(0).getLine());
        assertEquals("pierwsza linia\n".getBytes(StandardCharsets.UTF_8).length,
                index.getCursor(0).getPosition());
    }

    @Test
    void whenSearchingWindows1250FileThenMatchesDecodedTextAndCorrectLineNumbers() throws Exception {
        Charset charset = Charset.forName("windows-1250");
        Path file = tempDir.resolve("windows-search.log");
        Files.write(file, "pierwsza linia\nżółć i gęślą\ntrzecia linia\n".getBytes(charset));
        SearchForm form = new SearchForm();
        form.setText("żółć");
        FileSearchIndex index = FileSearching.search(form, new FileSearchIndex(file, charset));

        assertEquals(1, index.getMatchCount());
        assertEquals(2, index.getCursor(0).getLine());
        assertEquals("żółć i gęślą", FileReader.readLines(index, 0, 1).get(0).getLine());
        assertEquals("pierwsza linia\n".getBytes(charset).length,
                index.getCursor(0).getPosition());
    }

    @Test
    void whenUsingUtf16CharsetThenRejectsUnsupportedByteBasedIndexing() throws Exception {
        Path file = tempDir.resolve("utf16.log");
        Files.writeString(file, "first\nsecond", StandardCharsets.UTF_16LE);
        assertThrows(IllegalArgumentException.class,
                () -> new FileSearchIndex(file, StandardCharsets.UTF_16LE));
    }
}


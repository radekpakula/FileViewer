package org.vaadin.addons.viewer.application.file;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.vaadin.addons.viewer.application.file.search.FileSearchIndex;
import org.vaadin.addons.viewer.application.file.search.FileSearching;
import org.vaadin.addons.viewer.application.file.search.LineSearchEntry;
import org.vaadin.addons.viewer.application.file.search.SearchForm;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileReaderCharsetTest {

    @TempDir
    Path tempDir;

    @Test
    void whenReadingSingleLineWithWindows1250ThenDecodesWithRequestedCharset() throws Exception {
        Path file = tempDir.resolve("single-line.log");
        Charset charset = Charset.forName("windows-1250");
        Files.write(file, "zażółć".getBytes(charset));

        LineSearchEntry line = FileReader.readLine(file, 0, 1, charset);

        assertEquals("zażółć", line.getLine());
        assertEquals(1, line.getLineNumber());
    }

    @Test
    void whenReadingSearchResultsFromNegativeOffsetThenRejectsRequest() throws Exception {
        Path file = tempDir.resolve("search.log");
        Files.writeString(file, "match", StandardCharsets.UTF_8);
        SearchForm form = new SearchForm();
        form.setText("match");
        FileSearchIndex index = FileSearching.search(form, new FileSearchIndex(file, StandardCharsets.UTF_8));

        assertThrows(IllegalArgumentException.class, () -> FileReader.readLines(index, -1, 1));
    }

    @Test
    void whenReadingSearchResultsBeyondMatchCountThenReturnsNoLines() throws Exception {
        Path file = tempDir.resolve("search.log");
        Files.writeString(file, "match", StandardCharsets.UTF_8);
        SearchForm form = new SearchForm();
        form.setText("match");
        FileSearchIndex index = FileSearching.search(form, new FileSearchIndex(file, StandardCharsets.UTF_8));

        assertEquals(0, FileReader.readLines(index, 1, 2).size());
    }
}

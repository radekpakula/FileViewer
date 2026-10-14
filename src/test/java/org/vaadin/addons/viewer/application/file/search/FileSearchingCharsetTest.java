package org.vaadin.addons.viewer.application.file.search;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.vaadin.addons.viewer.application.file.FileReader;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

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
    void whenSearchingFileWithoutTrailingNewlineThenFindsLastLine() throws Exception {
        Path file = tempDir.resolve("no-trailing-newline.log");
        Files.writeString(file, "first\nlast match", StandardCharsets.UTF_8);
        SearchForm form = new SearchForm();
        form.setText("match");

        FileSearchIndex index = FileSearching.search(form, new FileSearchIndex(file, StandardCharsets.UTF_8));

        assertEquals(1, index.getMatchCount());
        assertEquals(2, index.getCursor(0).getLine());
        assertEquals("last match", FileReader.readLines(index, 0, 1).get(0).getLine());
    }

    @Test
    void whenSearchingEmptyFileThenCompletesWithoutMatches() throws Exception {
        Path file = tempDir.resolve("empty-search.log");
        Files.createFile(file);
        SearchForm form = new SearchForm();
        form.setText("anything");

        FileSearchIndex index = FileSearching.search(form, new FileSearchIndex(file, StandardCharsets.UTF_8));

        assertTrue(index.isFinishSearching());
        assertEquals(0, index.getMatchCount());
    }

    @Test
    void whenPollingWithUnchangedFiltersThenSearchesOnlyAppendedContent() throws Exception {
        Path file = tempDir.resolve("incremental-search.log");
        Files.writeString(file, "match one\nlast mat", StandardCharsets.UTF_8);
        SearchForm form = new SearchForm();
        form.setText("mat");
        FileSearchIndex index = new FileSearchIndex(file, StandardCharsets.UTF_8);

        FileSearching.search(form, index);
        assertEquals(2, index.getMatchCount());
        assertEquals(2, index.getCursor(1).getLine());

        Files.writeString(file, "ch completed\nmatch three\n", StandardCharsets.UTF_8,
                java.nio.file.StandardOpenOption.APPEND);
        FileSearching.search(form, index);

        assertEquals(3, index.getMatchCount());
        assertEquals(2, index.getCursor(1).getLine());
        assertEquals(3, index.getCursor(2).getLine());
        assertEquals("last match completed", FileReader.readLines(index, 1, 2).get(0).getLine());
    }

    @Test
    void whenSearchFiltersChangeThenRebuildsSearchResults() throws Exception {
        Path file = tempDir.resolve("changed-filters.log");
        Files.writeString(file, "alpha\nbeta\n", StandardCharsets.UTF_8);
        FileSearchIndex index = new FileSearchIndex(file, StandardCharsets.UTF_8);
        SearchForm form = new SearchForm();
        form.setText("alpha");

        FileSearching.search(form, index);
        assertEquals(1, index.getMatchCount());

        form.setText("beta");
        FileSearching.search(form, index);

        assertEquals(1, index.getMatchCount());
        assertEquals(2, index.getCursor(0).getLine());
    }

    @Test
    void whenIgnoreCaseChangesThenRebuildsSearchResults() throws Exception {
        Path file = tempDir.resolve("ignore-case.log");
        Files.writeString(file, "Alpha\nalpha\n", StandardCharsets.UTF_8);
        FileSearchIndex index = new FileSearchIndex(file, StandardCharsets.UTF_8);
        SearchForm form = new SearchForm();
        form.setText("alpha");

        FileSearching.search(form, index);
        assertEquals(1, index.getMatchCount());

        form.setIgnoreCase(true);
        FileSearching.search(form, index);

        assertEquals(2, index.getMatchCount());
    }

    @Test
    void whenPollingWithoutFileChangesThenKeepsExistingResults() throws Exception {
        Path file = tempDir.resolve("unchanged.log");
        Files.writeString(file, "match\nother\n", StandardCharsets.UTF_8);
        SearchForm form = new SearchForm();
        form.setText("match");
        FileSearchIndex index = new FileSearchIndex(file, StandardCharsets.UTF_8);

        FileSearching.search(form, index);
        FileSearching.search(form, index);

        assertEquals(1, index.getMatchCount());
        assertEquals(1, index.getCursor(0).getLine());
    }

    @Test
    void whenCrLfIsCompletedDuringPollingThenDoesNotCreateDuplicateLine() throws Exception {
        Path file = tempDir.resolve("incremental-crlf.log");
        Files.writeString(file, "match\r", StandardCharsets.UTF_8);
        SearchForm form = new SearchForm();
        form.setText("match");
        FileSearchIndex index = new FileSearchIndex(file, StandardCharsets.UTF_8);

        FileSearching.search(form, index);
        assertEquals(1, index.getMatchCount());

        Files.writeString(file, "\nmatch two\n", StandardCharsets.UTF_8,
                java.nio.file.StandardOpenOption.APPEND);
        FileSearching.search(form, index);

        assertEquals(2, index.getMatchCount());
        assertEquals(2, index.getCursor(1).getLine());
    }

    @Test
    void whenIgnoringCaseThenMatchingDoesNotDependOnDefaultLocale() throws Exception {
        Path file = tempDir.resolve("locale-search.log");
        Files.writeString(file, "ISTANBUL\\n", StandardCharsets.UTF_8);
        SearchForm form = new SearchForm();
        form.setText("istanbul");
        form.setIgnoreCase(true);
        FileSearchIndex index = new FileSearchIndex(file, StandardCharsets.UTF_8);
        java.util.Locale previousLocale = java.util.Locale.getDefault();
        try {
            java.util.Locale.setDefault(java.util.Locale.forLanguageTag("tr-TR"));
            FileSearching.search(form, index);
        } finally {
            java.util.Locale.setDefault(previousLocale);
        }

        assertEquals(1, index.getMatchCount());
    }

    @Test
    void whenRegexIsInvalidThenDoesNotMutateExistingSearchResults() throws Exception {
        Path file = tempDir.resolve("invalid-regex.log");
        Files.writeString(file, "match\\nother\\n", StandardCharsets.UTF_8);
        SearchForm form = new SearchForm();
        form.setText("match");
        FileSearchIndex index = new FileSearchIndex(file, StandardCharsets.UTF_8);
        FileSearching.search(form, index);
        assertEquals(1, index.getMatchCount());

        form.setText("[");
        form.setRegex(true);
        assertThrows(java.util.regex.PatternSyntaxException.class, () -> FileSearching.search(form, index));

        assertEquals(1, index.getMatchCount());
        assertEquals(1, index.getCursor(0).getLine());
    }

    @Test
    void whenUsingUtf16CharsetThenRejectsUnsupportedByteBasedIndexing() throws Exception {
        Path file = tempDir.resolve("utf16.log");
        Files.writeString(file, "first\nsecond", StandardCharsets.UTF_16LE);
        assertThrows(IllegalArgumentException.class,
                () -> new FileSearchIndex(file, StandardCharsets.UTF_16LE));
    }
}


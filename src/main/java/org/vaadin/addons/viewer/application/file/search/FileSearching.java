package org.vaadin.addons.viewer.application.file.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addons.viewer.application.exception.ViewerException;
import org.vaadin.addons.viewer.application.file.EncodedLineReader;
import org.vaadin.addons.viewer.application.file.search.matcher.DefaultMatcher;
import org.vaadin.addons.viewer.application.file.search.matcher.IgnoreCaseMatcher;
import org.vaadin.addons.viewer.application.file.search.matcher.RegexMatcher;
import org.vaadin.addons.viewer.application.file.search.matcher.SearchMatcher;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Locale;
import java.util.regex.Pattern;

public final class FileSearching {
    private static final Logger LOG = LoggerFactory.getLogger(FileSearching.class);
    private FileSearching() {}

    public static FileSearchIndex search(SearchForm form, FileSearchIndex searchIndex) {
        long start = System.currentTimeMillis();
        prepareForm(form);
        searchIndex.reset();
        SearchMatcher matchers = prepareMatchersBasedOnForm(form);

        try (RandomAccessFile raf = new RandomAccessFile(searchIndex.getPath().toFile(), "r")) {
            EncodedLineReader reader = new EncodedLineReader(raf, searchIndex.getCharset());
            String line;
            while (true) {
                long linePosition = raf.getFilePointer();
                line = reader.readLine();
                if (line == null) {
                    break;
                }
                if (matchers.match(form.getRegexPattern(), form.getText(), line)) {
                    searchIndex.addPointer(FileSearchResultLine.of(searchIndex.getLineCounter() + 1, linePosition));
                }
                searchIndex.increaseLineCounter();
                searchIndex.setProcessingPosition(raf.getFilePointer());
                if (searchIndex.isInterrupt()) {
                    searchIndex.setProcessingTime(System.currentTimeMillis() - start);
                    return searchIndex;
                }
            }
        } catch (IOException e) {
            throw new ViewerException(e);
        }

        searchIndex.setProcessingTime(System.currentTimeMillis() - start);
        searchIndex.setSearchDone();
        LOG.debug("Searching of the file {} took {} ms. File length {} found lines {}",
                searchIndex.getPath(), searchIndex.getProcessingTime(),
                searchIndex.getPath().toFile().length(), searchIndex.getMatchCount());
        return searchIndex;
    }

    private static SearchMatcher prepareMatchersBasedOnForm(SearchForm form) {
        SearchMatcher matcher = null;
        if (form.isIgnoreCase()) {
            matcher = new IgnoreCaseMatcher();
        }
        if (form.isRegex()) {
            if (matcher != null) {
                matcher.setNextMatcher(new RegexMatcher());
            } else {
                matcher = new RegexMatcher();
            }
        }
        if (matcher != null) {
            matcher.setNextMatcher(new DefaultMatcher());
        } else {
            matcher = new DefaultMatcher();
        }
        return matcher;
    }

    private static void prepareForm(SearchForm form) {
        if (form.isIgnoreCase()) {
            form.setText(form.getText().toLowerCase(Locale.getDefault()));
        }
        if (form.isRegex()) {
            form.setRegexPattern(Pattern.compile(form.getText()));
        }
    }
}

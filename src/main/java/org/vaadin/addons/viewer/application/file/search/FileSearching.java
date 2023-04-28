
package org.vaadin.addons.viewer.application.file.search;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.Locale;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addons.viewer.application.exception.ViewerException;
import org.vaadin.addons.viewer.application.file.search.matcher.DefaultMatcher;
import org.vaadin.addons.viewer.application.file.search.matcher.IgnoreCaseMatcher;
import org.vaadin.addons.viewer.application.file.search.matcher.RegexMatcher;
import org.vaadin.addons.viewer.application.file.search.matcher.SearchMatcher;

/**
 * Searches the file from the indicated position. Performs a check operation for each found line.
 * If the line is extended, the algorithm will go back to the beginning of the line in the next search process.
 * The position of each found line is stored in the search index.
 */
public final class FileSearching {

    private static final Logger LOG = LoggerFactory.getLogger(FileSearching.class);

    private static final int defaultBufferSize = 8192;

    private FileSearching() {
    }

    public static FileSearchIndex search(SearchForm form, FileSearchIndex searchIndex) {
        long start = System.currentTimeMillis();
        prepareForm(form);

        long from = findBeginningOfTheLine(searchIndex.getPath(), searchIndex.getFileSize());
        long beginningOfTheLineMarker = from;
        searchIndex.updateFileSize();
        searchIndex.removeLastEntryIfEqualToCurrent(beginningOfTheLineMarker);
        SearchMatcher matchers = prepareMatchersBasedOnForm(form);

        try (RandomAccessFile raf = new RandomAccessFile(searchIndex.getPath().toFile(), "r")) {
            StringBuilder sb = new StringBuilder();
            raf.seek(from);
            byte[] buf = new byte[defaultBufferSize];
            int charsRead = raf.read(buf, 0, buf.length);

            while (charsRead != -1) {
                for (int i = 0; i < charsRead; i++) {
                    char c = (char) buf[i];

                    if (c == '\r') {
                        searchIndex.increasePosition();
                        continue;
                    } else if (c == '\n') {
                        beginningOfTheLineMarker =
                                newLineDetected(form, searchIndex, beginningOfTheLineMarker, matchers, sb);
                        sb = new StringBuilder();
                    } else {
                        sb.append(c);
                    }
                    searchIndex.increasePosition();
                }
                charsRead = raf.read(buf, 0, buf.length);
                if (searchIndex.isInterrupt()) {
                    searchIndex.setProcessingTime((System.currentTimeMillis() - start));
                    LOG.info("Searching interrupted by the user");
                    return searchIndex;
                }
            }
            if (sb.length() > 0) {
                checkLastLine(form, searchIndex, beginningOfTheLineMarker, matchers, sb);
            }
        } catch (IOException e) {
            throw new ViewerException(e);
        }
        LOG.info("Searching of the file {} took {} ms. File length {} found lines {}",
                searchIndex.getPath(), (System.currentTimeMillis() - start),
                searchIndex.getPath().toFile().length(), searchIndex.getMatchCount());
        searchIndex.setProcessingTime((System.currentTimeMillis() - start));
        searchIndex.setSearchDone();
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

    private static void checkLastLine(SearchForm form, FileSearchIndex searchIndex, long beginningOfTheLineMarker,
            SearchMatcher matchers, StringBuilder sb) {
        if (matchers.match(form.getRegexPattern(), form.getText(), sb.toString())) {
            searchIndex.increaseLineCounter();
            searchIndex.addPointer(FileSearchResultLine.of(searchIndex.getLineCounter(), beginningOfTheLineMarker));
        }
    }

    private static long newLineDetected(SearchForm form, FileSearchIndex searchIndex,
            long beginningOfTheLineMarker, SearchMatcher matchers, StringBuilder sb) {
        searchIndex.increaseLineCounter();
        if (matchers.match(form.getRegexPattern(), form.getText(), sb.toString())) {
            searchIndex.addPointer(FileSearchResultLine.of(searchIndex.getLineCounter(),
                    beginningOfTheLineMarker));
        }
        beginningOfTheLineMarker = searchIndex.getProcessingPosition() + 1;
        return beginningOfTheLineMarker;
    }

    private static void prepareForm(SearchForm form) {
        if (form.isIgnoreCase()) {
            form.setText(form.getText().toLowerCase(Locale.getDefault()));
        }
        if (form.isRegex()) {
            form.setRegexPattern(Pattern.compile(form.getText()));
        }
    }

    private static long findBeginningOfTheLine(Path path, long from) {
        if (from == 0) {
            return 0;
        }
        try (RandomAccessFile raf = new RandomAccessFile(path.toFile(), "r")) {
            while (true) {
                raf.seek(from);
                int c = raf.read();
                if (c == '\n') {
                    return from + 1;
                }
                --from;
                if (from < 0) {
                    return 0;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

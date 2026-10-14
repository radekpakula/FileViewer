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
        long currentSize = searchIndex.getPath().toFile().length();
        SearchMatcher matchers = prepareMatchersBasedOnForm(form);
        String searchText = form.isIgnoreCase() ? form.getText().toLowerCase(Locale.ROOT) : form.getText();
        Pattern pattern = form.isRegex() ? Pattern.compile(searchText) : null;
        boolean incremental = searchIndex.canContinue(form, currentSize);
        long startPosition = 0;
        boolean reprocessLastLine = false;

        if (incremental) {
            long previousSize = searchIndex.getFileSize();
            startPosition = previousSize;
            if (currentSize > previousSize && previousSize > 0) {
                try (RandomAccessFile raf = new RandomAccessFile(searchIndex.getPath().toFile(), "r")) {
                    raf.seek(previousSize - 1);
                    int lastByte = raf.read();
                    raf.seek(previousSize);
                    int firstAppendedByte = raf.read();
                    boolean completesCrLf = lastByte == '\r' && firstAppendedByte == '\n';
                    if (completesCrLf) {
                        startPosition = previousSize + 1;
                    } else if (lastByte != '\n' && lastByte != '\r') {
                        reprocessLastLine = true;
                        long position = previousSize - 1;
                        while (position >= 0) {
                            raf.seek(position);
                            int value = raf.read();
                            if (value == '\n' || value == '\r') {
                                startPosition = position + 1;
                                break;
                            }
                            startPosition = 0;
                            position--;
                        }
                    }
                } catch (IOException e) {
                    throw new ViewerException(e);
                }
            }
            searchIndex.prepareIncrementalSearch(startPosition, reprocessLastLine);
        } else {
            searchIndex.reset();
        }

        searchIndex.rememberSearchForm(form);
        if (incremental && currentSize == searchIndex.getProcessingPosition()) {
            searchIndex.setProcessingTime(System.currentTimeMillis() - start);
            searchIndex.setSearchDone();
            searchIndex.updateFileSize();
            return searchIndex;
        }

        try (RandomAccessFile raf = new RandomAccessFile(searchIndex.getPath().toFile(), "r")) {
            raf.seek(startPosition);
            EncodedLineReader reader = new EncodedLineReader(raf, searchIndex.getCharset());
            String line;
            while (true) {
                long linePosition = raf.getFilePointer();
                line = reader.readLine();
                if (line == null) {
                    break;
                }
                if (matchers.match(pattern, searchText, line)) {
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

        searchIndex.setProcessingPosition(currentSize);
        searchIndex.updateFileSize();
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
}

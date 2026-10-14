package org.vaadin.addons.viewer.event;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addons.viewer.application.file.search.FileSearchIndex;
import org.vaadin.addons.viewer.application.file.search.FileSearching;
import org.vaadin.addons.viewer.application.file.search.SearchForm;
import org.vaadin.addons.viewer.view.content.FileSearchLayout;

public class FileSearchEvent {

    private static final Logger LOG = LoggerFactory.getLogger(FileSearchEvent.class);

    private final FileSearchLayout searchView;
    private ScheduledExecutorService executorService;
    private FileSearchIndex fileSearchIndex;

    public FileSearchEvent(FileSearchLayout searchView) {
        this.searchView = searchView;
    }

    private boolean isSearching() {
        return executorService != null && !executorService.isTerminated();
    }

    public synchronized void search(SearchForm form, FileSearchIndex index) {
        terminate();
        this.fileSearchIndex = index;
        ScheduledExecutorService taskExecutor = Executors.newScheduledThreadPool(2, runnable -> {
            Thread thread = new Thread(runnable, "file-viewer-search");
            thread.setDaemon(true);
            return thread;
        });
        this.executorService = taskExecutor;

        SearchForm criteria = new SearchForm();
        criteria.setText(form.getText());
        criteria.setIgnoreCase(form.isIgnoreCase());
        criteria.setRegex(form.isRegex());

        taskExecutor.scheduleAtFixedRate(() -> {
            if (fileSearchIndex == index && !taskExecutor.isShutdown()) {
                searchView.refreshSearchResultEvent(index);
            }
        }, 500, 1000, TimeUnit.MILLISECONDS);

        CompletableFuture.supplyAsync(() -> FileSearching.search(criteria, index), taskExecutor)
                .whenComplete((result, error) -> {
                    if (error != null) {
                        LOG.warn("Search failed", error);
                    } else {
                        LOG.info("Searching completed in {}ms", index.getProcessingTime());
                    }
                    synchronized (FileSearchEvent.this) {
                        if (fileSearchIndex != index) {
                            taskExecutor.shutdownNow();
                            return;
                        }
                        if (executorService == taskExecutor) {
                            executorService = null;
                        }
                    }
                    taskExecutor.shutdownNow();
                    searchView.searchCompleteEvent(result, error);
                });
    }

    public synchronized void terminate() {
        FileSearchIndex currentIndex = fileSearchIndex;
        fileSearchIndex = null;
        if (currentIndex != null) {
            currentIndex.interrupt();
        }
        if (executorService != null) {
            executorService.shutdownNow();
            executorService = null;
        }
    }
}

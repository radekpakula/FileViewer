package org.vaadin.addons.viewer.event;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addons.viewer.application.file.search.FileSearchIndex;
import org.vaadin.addons.viewer.application.file.search.FileSearching;
import org.vaadin.addons.viewer.application.file.search.SearchForm;
import org.vaadin.addons.viewer.view.content.FileSearchLayout;

public class FileSearchEvent {

    private static final Logger LOG = LoggerFactory.getLogger(FileSearchEvent.class);

    private final FileSearchLayout searchView;
    private volatile ScheduledExecutorService executorService;
    private volatile FileSearchIndex fileSearchIndex;

    public FileSearchEvent(FileSearchLayout searchView) {
        this.searchView = searchView;
    }

    public synchronized void search(SearchForm form, FileSearchIndex index) {
        terminate();
        this.fileSearchIndex = index;
        final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2, runnable -> {
            Thread thread = new Thread(runnable, "file-viewer-search");
            thread.setDaemon(true);
            return thread;
        });
        this.executorService = executor;

        executor.scheduleAtFixedRate(() -> {
            if (fileSearchIndex == index && !executor.isShutdown()) {
                searchView.refreshSearchResultEvent(index);
            }
        }, 500, 1000, TimeUnit.MILLISECONDS);

        CompletableFuture.supplyAsync(() -> FileSearching.search(form, index))
                .whenComplete((result, error) -> {
                    executor.shutdownNow();
                    if (fileSearchIndex != index) {
                        return;
                    }
                    LOG.debug("Searching completed in {}ms", index.getProcessingTime());
                    searchView.searchCompleteEvent(result, error);
                });
    }

    public synchronized void terminate() {
        FileSearchIndex currentIndex = fileSearchIndex;
        fileSearchIndex = null;
        if (currentIndex != null) {
            currentIndex.interrupt();
        }
        ScheduledExecutorService currentExecutor = executorService;
        executorService = null;
        if (currentExecutor != null) {
            currentExecutor.shutdownNow();
        }
    }
}

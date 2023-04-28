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
    private ScheduledExecutorService executorService;
    private FileSearchIndex fileSearchIndex;

    public FileSearchEvent(FileSearchLayout searchView) {
        this.searchView = searchView;
    }

    private boolean isSearching() {
        return executorService != null && (!executorService.isShutdown() || !executorService.isTerminated());
    }

    public void search(SearchForm form, FileSearchIndex fileSearchIndex) {
        if (isSearching()) {
            Notification notification = Notification.show("Search has been terminated");
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.open();
            return;
        }
        this.fileSearchIndex = fileSearchIndex;
        executorService = Executors.newSingleThreadScheduledExecutor();

        new CompletableFuture<FileSearchIndex>().completeAsync(() -> FileSearching.search(
                        form, fileSearchIndex))
                .whenCompleteAsync((fileIndex, error) -> {
                    LOG.info("Searching completed in {}ms", fileSearchIndex.getProcessingTime());
                    executorService.shutdown();
                    searchView.searchCompleteEvent(fileIndex, error);
                });

        executorService.scheduleAtFixedRate(() -> {
            searchView.refreshSearchResultEvent(fileSearchIndex);
        }, 500, 1000, TimeUnit.MILLISECONDS);
    }

    public void terminate() {
        LOG.info("Stopping search task");
        fileSearchIndex.interrupt();
        if (isSearching()) {
            executorService.shutdown();
        }
    }
}

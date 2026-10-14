package org.vaadin.addons.viewer.view.content;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addons.viewer.application.file.FileInfo;
import org.vaadin.addons.viewer.application.file.search.FileSearchIndex;
import org.vaadin.addons.viewer.application.file.search.FileSearching;
import org.vaadin.addons.viewer.system.FileViewerSystem;
import org.vaadin.addons.viewer.event.FileSearchEvent;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class FileSearchLayout extends VerticalLayout {
    private static final Logger LOG = LoggerFactory.getLogger(FileSearchLayout.class);
    private final SearchResultGrid resultGrid;
    private final FileInfo fileInfo;
    private final FileViewerSystem system;
    private final ProgressBar progressBar;
    private final SearchBarFields searchBar;
    private final FileSearchEvent searchEvent;
    private FileSearchIndex searchIndex;
    private final AtomicBoolean incrementalSearchRunning = new AtomicBoolean();

    FileSearchLayout(FileViewerSystem system, FileInfo fileInfo, FileContentGrid fileViewGrid) {
        this.setSizeFull();
        this.setPadding(false);
        this.setSpacing(false);
        this.setMargin(false);
        this.system = system;
        this.resultGrid = new SearchResultGrid(fileViewGrid);
        this.fileInfo = fileInfo;
        this.searchBar = new SearchBarFields(system, this);
        this.searchEvent = new FileSearchEvent(this);
        this.progressBar = new ProgressBar();
        progressBar.setWidth(100, Unit.PERCENTAGE);
        progressBar.setVisible(true);
        progressBar.setValue(0);
        add(progressBar);
        add(searchBar);
        add(resultGrid);
    }

    void startSearchEvent() {
        if (!searchBar.isValid()) {
            return;
        }
        LOG.debug("Start searching in file {}", fileInfo.getFile());
        searchEvent.terminate();
        searchBar.setSearchMode(true);
        this.searchIndex = new FileSearchIndex(fileInfo.getFile().toPath(), fileInfo.getCharset());
        resultGrid.setResult(searchIndex);
        searchEvent.search(searchBar.getForm(), searchIndex);
    }

    void stopSearchEvent() {
        searchEvent.terminate();
        searchBar.setSearchMode(false);
    }

    public void searchCompleteEvent(FileSearchIndex fileIndex, Throwable error) {
        system.getUi().access(() -> {
            searchBar.setSearchMode(false);
            progressBar.setValue(0);
            if (error == null && fileIndex != null) {
                searchBar.refreshState(fileIndex);
                resultGrid.getDataProvider().refreshAll();
            } else if (error != null) {
                Throwable cause = error.getCause() == null ? error : error.getCause();
                String message = cause.getMessage() == null ? "Search failed" : cause.getMessage();
                Notification notification = Notification.show(message);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                notification.open();
            }
        });
        system.getUi().push();
    }

    public void refreshSearchResultEvent(FileSearchIndex fileSearchIndex) {
        system.getUi().access(() -> {
            searchBar.refreshState(fileSearchIndex);
            resultGrid.getDataProvider().refreshAll();
            progressBar.setValue(fileSearchIndex.processingPercentage());
        });
    }

    public void rebuildForFileRewrite() {
        searchEvent.terminate();
        searchBar.setSearchMode(false);
        searchIndex = new FileSearchIndex(fileInfo.getFile().toPath(), fileInfo.getCharset());
        resultGrid.setResult(searchIndex);
        resultGrid.getDataProvider().refreshAll();
        if (searchBar.isValid()) {
            startSearchEvent();
        }
    }

    public void extendSearchEvent() {
        if (system.getConfig().pollingEnabled() && searchBar.isAutoRefresh()
                && searchIndex != null && searchIndex.isFinishSearching()) {
            if (!incrementalSearchRunning.compareAndSet(false, true)) {
                return;
            }
            FileSearchIndex indexToSearch = searchIndex;
            org.vaadin.addons.viewer.application.file.search.SearchForm criteria = new org.vaadin.addons.viewer.application.file.search.SearchForm();
            criteria.setText(searchBar.getForm().getText());
            criteria.setIgnoreCase(searchBar.getForm().isIgnoreCase());
            criteria.setRegex(searchBar.getForm().isRegex());
            int previousMatchCount = indexToSearch.getMatchCount();
            CompletableFuture.supplyAsync(() -> FileSearching.search(criteria, indexToSearch))
                    .whenComplete((updatedIndex, error) -> {
                        incrementalSearchRunning.set(false);
                        if (error != null) {
                            LOG.warn("Unable to extend search results", error);
                            system.getUi().access(() -> {
                                Notification notification = Notification.show(
                                        error.getCause() != null && error.getCause().getMessage() != null
                                                ? error.getCause().getMessage() : "Search refresh failed");
                                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                                notification.open();
                            });
                            return;
                        }
                        if (searchIndex == indexToSearch && previousMatchCount != updatedIndex.getMatchCount()) {
                            system.getUi().access(() -> {
                                searchBar.refreshState(updatedIndex);
                                resultGrid.getDataProvider().refreshAll();
                            });
                        }
                    });
        }
    }

    public SearchResultGrid getResultGrid() {
        return resultGrid;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public SearchBarFields getSearchBar() {
        return searchBar;
    }
}

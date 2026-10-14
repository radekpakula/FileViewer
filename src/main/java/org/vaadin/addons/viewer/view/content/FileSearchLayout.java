package org.vaadin.addons.viewer.view.content;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import com.vaadin.flow.component.UI;
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
import org.vaadin.addons.viewer.application.file.search.SearchForm;
import org.vaadin.addons.viewer.event.FileSearchEvent;
import org.vaadin.addons.viewer.system.FileViewerSystem;

public class FileSearchLayout extends VerticalLayout {

    private static final Logger LOG = LoggerFactory.getLogger(FileSearchLayout.class);

    private final SearchResultGrid resultGrid;
    private final FileInfo fileInfo;
    private final FileViewerSystem system;
    private final ProgressBar progressBar;
    private final SearchBarFields searchBar;
    private final FileSearchEvent searchEvent;
    private final AtomicBoolean incrementalSearchRunning = new AtomicBoolean();

    private FileSearchIndex searchIndex;

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
        searchIndex = new FileSearchIndex(fileInfo.getFile().toPath(), fileInfo.getCharset());
        resultGrid.setResult(searchIndex);
        searchEvent.search(searchBar.getForm(), searchIndex);
    }

    void stopSearchEvent() {
        searchEvent.terminate();
        searchBar.setSearchMode(false);
    }

    public void searchCompleteEvent(FileSearchIndex fileIndex, Throwable error) {
        UI ui = system.getUi();
        if (ui == null || !ui.isAttached()) {
            return;
        }
        ui.access(() -> {
            if (!ui.isAttached()) {
                return;
            }
            searchBar.setSearchMode(false);
            progressBar.setValue(0);
            if (error == null && fileIndex != null) {
                searchBar.refreshState(fileIndex);
                resultGrid.getDataProvider().refreshAll();
            } else if (error != null) {
                showError(error);
            }
            ui.push();
        });
    }

    public void refreshSearchResultEvent(FileSearchIndex fileSearchIndex) {
        UI ui = system.getUi();
        if (ui == null || !ui.isAttached()) {
            return;
        }
        ui.access(() -> {
            if (!ui.isAttached()) {
                return;
            }
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
        if (!system.getConfig().pollingEnabled() || !searchBar.isAutoRefresh()
                || searchIndex == null || !searchIndex.isFinishSearching()
                || !incrementalSearchRunning.compareAndSet(false, true)) {
            return;
        }

        FileSearchIndex indexToSearch = searchIndex;
        SearchForm criteria = new SearchForm();
        criteria.setText(searchBar.getForm().getText());
        criteria.setIgnoreCase(searchBar.getForm().isIgnoreCase());
        criteria.setRegex(searchBar.getForm().isRegex());
        int previousMatchCount = indexToSearch.getMatchCount();

        CompletableFuture.supplyAsync(() -> FileSearching.search(criteria, indexToSearch))
                .whenComplete((updatedIndex, error) -> {
                    incrementalSearchRunning.set(false);
                    if (error != null) {
                        LOG.warn("Unable to extend search results", error);
                        showError(error);
                        return;
                    }
                    if (searchIndex == indexToSearch && previousMatchCount != updatedIndex.getMatchCount()) {
                        UI ui = system.getUi();
                        if (ui != null && ui.isAttached()) {
                            ui.access(() -> {
                                if (ui.isAttached() && searchIndex == updatedIndex) {
                                    searchBar.refreshState(updatedIndex);
                                    resultGrid.getDataProvider().refreshAll();
                                }
                            });
                        }
                    }
                });
    }

    private void showError(Throwable error) {
        UI ui = system.getUi();
        if (ui == null || !ui.isAttached()) {
            return;
        }
        Throwable cause = error.getCause() == null ? error : error.getCause();
        String message = cause.getMessage() == null ? "Search failed" : cause.getMessage();
        ui.access(() -> {
            if (!ui.isAttached()) {
                return;
            }
            Notification notification = Notification.show(message);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.open();
        });
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

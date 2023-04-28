
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

public class FileSearchLayout extends VerticalLayout {

    private static final Logger LOG = LoggerFactory.getLogger(FileSearchLayout.class);

    private final SearchResultGrid resultGrid;
    private final FileInfo fileInfo;
    private final FileViewerSystem system;
    private final ProgressBar progressBar;

    private final SearchBarFields searchBar;
    private final FileSearchEvent searchEvent;
    private FileSearchIndex searchIndex;

    FileSearchLayout(FileViewerSystem system, FileInfo fileInfo, FileContentGrid fileViewGrid) {
        this.setSizeFull();
        this.setPadding(false);
        this.setSpacing(false);
        this.setMargin(false);

        this.system = system;
        this.resultGrid = new SearchResultGrid(fileViewGrid);
        this.fileInfo = fileInfo;
        this.searchBar = new SearchBarFields(system,this);
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
        if (searchBar.isValid()) {
            LOG.debug("Start searching in file {}", fileInfo.getFile());

            searchBar.setSearchMode(true);

            this.searchIndex = new FileSearchIndex(fileInfo.getFile().toPath());

            resultGrid.setResult(searchIndex);

            searchEvent.search(searchBar.getForm(), searchIndex);
        }
    }

    void stopSearchEvent() {
        searchEvent.terminate();
    }

    public void searchCompleteEvent(FileSearchIndex fileIndex, Throwable error) {
        system.getUi().access(() -> {
            searchBar.setSearchMode(false);
            searchBar.refreshState(fileIndex);
            progressBar.setValue(0);
            if (error == null) {
                resultGrid.getDataProvider().refreshAll();
            } else {
                Notification notification = Notification.show(error.getMessage());
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                notification.open();
            }
        });
        system.getUi().push();
    }

    public void refreshSearchResultEvent(FileSearchIndex fileSearchIndex) {
        try{
            system.getUi().access(() -> {
                searchBar.refreshState(fileSearchIndex);
                resultGrid.getDataProvider().refreshAll();
                progressBar.setValue(fileSearchIndex.processingPercentage());
            });
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void extendSearchEvent() {
        if (searchBar.isAutoRefresh()) {
            if (searchIndex != null && searchIndex.isFinishSearching()) {
                int match = searchIndex.getMatchCount();
                FileSearching.search(searchBar.getForm(), searchIndex);
                if (match != searchIndex.getMatchCount()) {
                    system.getUi().access(() -> {
                        searchBar.refreshState(searchIndex);
                        resultGrid.getDataProvider().refreshAll();
                    });
                }
            }
        }
    }

    // UI access
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

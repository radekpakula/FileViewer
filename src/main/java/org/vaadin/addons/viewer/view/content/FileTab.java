package org.vaadin.addons.viewer.view.content;

import java.util.concurrent.CompletableFuture;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import org.vaadin.addons.viewer.application.file.FileInfo;
import org.vaadin.addons.viewer.application.file.index.FileIndex;
import org.vaadin.addons.viewer.application.file.index.FileIndexing;
import org.vaadin.addons.viewer.system.FileViewerSystem;

public class FileTab extends Div {
    private final FileViewerSystem system;
    private final FileInfo fileInfo;
    private final Div loader;
    private boolean initialized = false;
    private FileContentGrid fileViewGrid;
    private FileSearchLayout searchView;
    private boolean contentRewritten;

    public FileTab(FileViewerSystem system, FileInfo fileInfo) {
        this.system = system;
        this.fileInfo = fileInfo;
        this.setSizeFull();
        ProgressBar progressBar = new ProgressBar();
        progressBar.setIndeterminate(true);
        this.loader = new Div();
        loader.setClassName("center-loader");
        loader.add(progressBar, new Text("Indexing file. Please wait a while.."));
        add(loader);
    }

    public void showGrid() {
        if (initialized) {
            return;
        }
        initialized = true;
        new CompletableFuture<FileIndex>().completeAsync(() -> FileIndexing.indexFile(
                        new FileIndex(fileInfo.getFile().toPath(), fileInfo.getCharset())))
                .whenCompleteAsync((fileMap, error) -> {
                    if (error != null) {
                        system.getUi().access(() -> loader.add(new Text(error.getMessage())));
                        return;
                    }
                    FileContentGrid grid = new FileContentGrid(system, fileMap);
                    system.getUi().access(() -> initFileView(grid));
                });
    }

    private void initFileView(FileContentGrid fileViewGrid) {
        loader.getElement().removeFromParent();
        this.fileViewGrid = fileViewGrid;
        if (system.getConfig().searchViewVisible()) {
            final FileSearchLayout searchView = new FileSearchLayout(system, fileInfo, fileViewGrid);
            this.searchView = searchView;
            SplitLayout splitLayout = new SplitLayout(fileViewGrid, searchView);
            splitLayout.setOrientation(SplitLayout.Orientation.VERTICAL);
            splitLayout.setSplitterPosition(50);
            splitLayout.setSizeFull();
            add(splitLayout);
        } else {
            add(fileViewGrid);
        }
    }

    public void onCloseEvent() {
        getElement().removeFromParent();
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }

    public void setContentRewritten(boolean contentRewritten) {
        this.contentRewritten |= contentRewritten;
    }

    public void fileWasChangedEvent() {
        if (fileViewGrid == null) {
            return;
        }
        if (contentRewritten) {
            contentRewritten = false;
            fileViewGrid.rebuildIndexEvent();
            if (searchView != null) {
                searchView.rebuildForFileRewrite();
            }
            return;
        }
        fileViewGrid.extendIndexEvent();
        if (searchView != null) {
            searchView.extendSearchEvent();
        }
    }

    public void scrollToFirstLine() {
        fileViewGrid.scrollToFirstLine();
    }

    public void scrollToLastLine() {
        fileViewGrid.scrollToLastLine();
    }

    public Div getLoader() {
        return loader;
    }

    public FileContentGrid getFileViewGrid() {
        return fileViewGrid;
    }

    public FileSearchLayout getSearchView() {
        return searchView;
    }
}

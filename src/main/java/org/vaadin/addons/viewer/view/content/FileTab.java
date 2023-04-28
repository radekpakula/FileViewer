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

        new CompletableFuture<FileIndex>().completeAsync(() -> FileIndexing.updateIndex(
                        new FileIndex(fileInfo.getFile().toPath())))
                .whenCompleteAsync((fileMap, error) -> {
                    FileContentGrid grid = new FileContentGrid(system, fileMap);
                    system.getUi().access(() -> initFileView(grid));
                    system.getUi();
                });
    }

    private void initFileView(FileContentGrid fileViewGrid) {
        loader.getElement().removeFromParent();

        this.fileViewGrid = fileViewGrid;

        if(system.getConfig().searchViewVisible()) {
            final FileSearchLayout searchView = new FileSearchLayout(system, fileInfo, fileViewGrid);

            this.searchView = searchView;

            SplitLayout splitLayout = new SplitLayout(fileViewGrid, searchView);
            splitLayout.setOrientation(SplitLayout.Orientation.VERTICAL);
            splitLayout.setSplitterPosition(50);
            splitLayout.setSizeFull();

            add(splitLayout);
        }else{
            add(fileViewGrid);
        }
    }

    public void onCloseEvent() {
        getElement().removeFromParent();
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }

    public void fileWasChangedEvent() {
        if (fileViewGrid != null) {
            fileViewGrid.extendIndexEvent();
            searchView.extendSearchEvent();
        }
    }

    //UI access
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

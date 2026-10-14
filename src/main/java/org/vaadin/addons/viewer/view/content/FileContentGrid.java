package org.vaadin.addons.viewer.view.content;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addons.viewer.application.exception.ViewerException;
import org.vaadin.addons.viewer.application.file.FileReader;
import org.vaadin.addons.viewer.application.file.index.FileIndex;
import org.vaadin.addons.viewer.application.file.index.FileIndexing;
import org.vaadin.addons.viewer.application.file.index.LineEntry;
import org.vaadin.addons.viewer.system.FileViewerSystem;

public class FileContentGrid extends Grid<LineEntry> {

    private static final Logger LOG = LoggerFactory.getLogger(FileContentGrid.class);
    private final FileIndex fileIndex;
    private final FileViewerSystem system;

    FileContentGrid(FileViewerSystem system, FileIndex fileIndex) {
        super(LineEntry.class, false);
        this.system = system;
        this.fileIndex = fileIndex;
        addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_COMPACT);
        addThemeName("log");
        addClassName("file-viewer-grid");
        setSizeFull();
        setPageSize(FileIndex.LINES_PER_INDEX_PAGE);

        Column<LineEntry> indexColumn = addColumn(item -> "")
                .setWidth("80px")
                .setFlexGrow(0)
                .setKey("rowIndex")
                .setPartNameGenerator(e -> "ident")
                .setFrozen(true);
        addColumn(LineEntry::getLog);
        setItems((CallbackDataProvider.FetchCallback<LineEntry, Void>) query -> {
            List<LineEntry> lines;
            try {
                lines = FileReader.readLines(fileIndex, query.getOffset(), query.getLimit());
            } catch (ViewerException e) {
                return generateEmptyResult(query, e);
            }
            LOG.debug("Returned {} lines", lines.size());
            refreshIndexColumn(indexColumn);
            return lines.stream();
        }, query -> fileIndex.getMaxIndex() + 1);

        refreshIndexColumn(indexColumn);
    }

    private static Stream<LineEntry> generateEmptyResult(Query<LineEntry, Void> query, ViewerException error) {
        LOG.error("Unable to read file page", error);
        List<LineEntry> lines = new ArrayList<>();
        for (int i = 0; i < query.getLimit(); i++) {
            lines.add(LineEntry.of(""));
        }
        return lines.stream();
    }

    private static void refreshIndexColumn(Column<LineEntry> indexColumn) {
        indexColumn.getElement().executeJs(
                "this.renderer = function(root, column, rowData) {root.textContent = rowData.index+1}"
        );
    }

    public void scrollToFirstLine() {
        scrollToIndex(0);
    }

    public void scrollToLastLine() {
        scrollToIndex(fileIndex.getMaxIndex());
    }

    public void selectLineByIndex(int index) {
        LineEntry entry = getLazyDataView().getItem(index);
        getSelectionModel().select(entry);
        scrollToIndex(index);
    }

    public void extendIndexEvent() {
        FileIndexing.indexFile(fileIndex);
        system.getUi().access(() -> {
            getDataProvider().refreshAll();
            if (system.isFollowChanges()) {
                scrollToIndex(fileIndex.getMaxIndex());
            }
        });
    }
}

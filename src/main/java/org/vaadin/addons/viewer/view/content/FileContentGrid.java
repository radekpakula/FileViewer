
package org.vaadin.addons.viewer.view.content;

import java.util.List;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addons.viewer.application.file.FileReader;
import org.vaadin.addons.viewer.application.file.index.FileIndex;
import org.vaadin.addons.viewer.application.file.index.FileIndexing;
import org.vaadin.addons.viewer.application.file.index.LineEntry;
import org.vaadin.addons.viewer.system.FileViewerSystem;

public class FileContentGrid extends Grid<LineEntry> {

    private static final Logger LOG = LoggerFactory.getLogger(FileContentGrid.class);
    private final FileIndex fileIndex;
    private final FileViewerSystem system;

    FileContentGrid(FileViewerSystem system, FileIndex fileMap) {
        super(LineEntry.class, false);
        this.system = system;
        this.fileIndex = fileMap;
        addThemeVariants(GridVariant.LUMO_NO_BORDER);
        addThemeVariants(GridVariant.LUMO_NO_ROW_BORDERS);
        addThemeVariants(GridVariant.LUMO_COMPACT);
        addThemeName("log");
        setSizeFull();
        setPageSize(FileIndex.INDEX_PER_ROWINDEX_PER_ROW);

        Column<?> indexCol = addColumn(item -> "").setWidth("80px").setFlexGrow(0).setKey("rowIndex")
                .setClassNameGenerator(e -> "ident").setFrozen(true);
        addColumn(LineEntry::getLog);
        setItems((CallbackDataProvider.FetchCallback<LineEntry, Void>) query -> {

            List<LineEntry> list = FileReader.readLines(fileMap, query.getOffset(), query.getLimit());

            LOG.debug("returned {} elements", list.size());

            return list.stream();
        }, query -> fileMap.getMaxIndex() + 1);

        indexCol.getElement().executeJs(
                "this.renderer = function(root, column, rowData) {root.textContent = rowData.index+1}"
        );
    }

    public void extendIndexEvent() {
        FileIndexing.updateIndex(fileIndex);
        system.getUi().access(() -> {
            getDataProvider().refreshAll();
            if (system.isFollowChanges()) {
                scrollToIndex(fileIndex.getMaxIndex());
            }
        });

    }
}


package org.vaadin.addons.viewer.view.content;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.server.VaadinServletService;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import com.vaadin.flow.component.AttachEvent;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

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

        Column<LineEntry> indexCol = addColumn(item -> "").setWidth("80px").setFlexGrow(0).setKey("rowIndex")
                .setClassNameGenerator(e -> "ident").setFrozen(true);
        addColumn(LineEntry::getLog);
        setItems((CallbackDataProvider.FetchCallback<LineEntry, Void>) query -> {
            List<LineEntry> list;
            try {
                list = FileReader.readLines(fileMap, query.getOffset(), query.getLimit());
            } catch (ViewerException e) {
                return generateEmptyResult(query, e);
            }
            LOG.debug("returned {} elements", list.size());
            refreshIndexColumn(indexCol);
            return list.stream();
        }, query -> fileMap.getMaxIndex() + 1);

        refreshIndexColumn(indexCol);
    }

    private static Stream<LineEntry> generateEmptyResult(Query<LineEntry, Void> query,
                                                         ViewerException e) {
        List<LineEntry> list = new ArrayList<>();
        LOG.error(e.getMessage());
        for (int i = 0; i < query.getLimit(); i++) {
            list.add(LineEntry.of(""));
        }
        return list.stream();
    }

    private static void refreshIndexColumn(Column<LineEntry> indexCol) {
        indexCol.getElement().executeJs(
                "this.renderer = function(root, column, rowData) {root.textContent = rowData.index+1}"
        );
    }

    public void scrollToFirstLine() {
        scrollToIndex(0);
    }

    public void scrollToLastLine() {
        scrollToIndex(fileIndex.getMaxIndex());
    }

    public void selectLineByIndex(int i) {
        LineEntry entry = getLazyDataView().getItem(i);
        getSelectionModel().select(entry);
        scrollToIndex(i);
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

    @Override
    protected void onAttach(AttachEvent attachEvent){
        super.onAttach(attachEvent);

        this.getElement().executeJs("var style = document.createElement('style'); style.textContent = '.cell { color: red; }'; this.shadowRoot.appendChild(style);");


    }
}

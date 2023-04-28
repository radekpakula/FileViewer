
package org.vaadin.addons.viewer.view.content;

import java.util.List;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addons.viewer.application.file.FileReader;
import org.vaadin.addons.viewer.application.file.search.FileSearchIndex;
import org.vaadin.addons.viewer.application.file.search.LineSearchEntry;

public class SearchResultGrid extends Grid<LineSearchEntry> {

    private static final Logger LOG = LoggerFactory.getLogger(SearchResultGrid.class);

    SearchResultGrid(FileContentGrid grid) {
        super(LineSearchEntry.class, false);
        addThemeVariants(GridVariant.LUMO_NO_BORDER);
        addThemeVariants(GridVariant.LUMO_NO_ROW_BORDERS);
        addThemeVariants(GridVariant.LUMO_COMPACT);
        addThemeName("log");
        setSizeFull();
        setPageSize(FileSearchIndex.RESULT_PER_PAGE);

        addColumn(LineSearchEntry::getLineNumber).setWidth("80px").setFlexGrow(0).setKey("rowIndex")
                .setClassNameGenerator(e -> "ident").setFrozen(true);
        addColumn(LineSearchEntry::getLine);
        addItemClickListener(e -> goToLine(e.getItem(), grid));
    }

    private void goToLine(LineSearchEntry item, FileContentGrid grid) {
        grid.scrollToIndex(item.getLineNumber() - 1);
    }

    void setResult(FileSearchIndex searchIndex) {
        setItems((CallbackDataProvider.FetchCallback<LineSearchEntry, Void>) query -> {
            LOG.debug("Request {} limit {}", query.getOffset(), query.getLimit());

            int from = query.getOffset();
            int limit = query.getLimit();

            LOG.debug("Call from {}  to {}", from, (from + limit));

            List<LineSearchEntry> list = FileReader.readLines(searchIndex, from, from + limit);

            LOG.debug("returned {} elements", list.size());

            return list.stream();
        }, query -> searchIndex.getMatchCount());
    }

}

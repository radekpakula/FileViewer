
package org.vaadin.addons.viewer.event;

import com.vaadin.flow.component.tabs.Tab;
import org.vaadin.addons.viewer.view.content.FileTabSheet;

public class FileCloseAllEvent implements EventAction<Tab> {

    private final FileTabSheet tabSheet;

    public FileCloseAllEvent(FileTabSheet tabSheet) {
        this.tabSheet = tabSheet;
    }

    @Override
    public void fireEvent(Tab t) {
        tabSheet.getTabs().forEach(
                tabSheet::closeTab);
    }
}

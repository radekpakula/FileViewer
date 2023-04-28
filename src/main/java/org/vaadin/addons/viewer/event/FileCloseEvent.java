
package org.vaadin.addons.viewer.event;

import com.vaadin.flow.component.tabs.Tab;
import org.vaadin.addons.viewer.view.content.FileTabSheet;

public class FileCloseEvent implements EventAction<Tab> {

    private final FileTabSheet tabSheet;

    public FileCloseEvent(FileTabSheet tabSheet) {
        this.tabSheet = tabSheet;
    }

    @Override
    public void fireEvent(Tab tab) {
        tabSheet.closeTab(tab);
    }
}

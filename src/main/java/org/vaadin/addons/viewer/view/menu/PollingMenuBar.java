
package org.vaadin.addons.viewer.view.menu;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.textfield.IntegerField;
import org.vaadin.addons.viewer.system.FileViewerSystem;

public class PollingMenuBar extends MenuBar {

    public PollingMenuBar(FileViewerSystem system) {

        this.addItem(new Checkbox("Follow changes", true, e -> system.setFollowChanges(e.getValue())));
        IntegerField pollingIntervalField = new IntegerField();
        pollingIntervalField.setMin(1);
        pollingIntervalField.setMax(30);
        pollingIntervalField.setValue(system.getConfig().pollingInterval());
        pollingIntervalField.setWidth(60, Unit.PIXELS);

        final Checkbox pollingEnabled =
                new Checkbox("Polling [s]", true, e -> system.enablePolling(e.getValue(), pollingIntervalField.getValue()));
        this.addItem(pollingEnabled);
        this.addItem(pollingIntervalField);

        pollingIntervalField.addValueChangeListener(e -> {
            system.interruptFilePolling();
            system.enablePolling(pollingEnabled.getValue(), e.getValue());
        });

    }
}


package org.vaadin.addons.viewer.view.menu;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.textfield.IntegerField;
import org.vaadin.addons.viewer.FileViewerConfig;
import org.vaadin.addons.viewer.system.FileViewerSystem;
import org.vaadin.addons.viewer.view.content.FileTabSheet;

public class ViewerMenuBar extends MenuBar {

    private final FileTabSheet tabSheet;

    public ViewerMenuBar(FileViewerSystem system, FileTabSheet fileTabSheet, FileViewerConfig fileViewerConfig) {
        this.tabSheet = fileTabSheet;

        addPoolingMenu(system, fileViewerConfig);

        this.addItem(VaadinIcon.ARROW_UP.create(), e -> scrollToTheBeginning());
        this.addItem(VaadinIcon.ARROW_DOWN.create(), e -> scrollToTheEnd());

        addFontMenu(system, fileViewerConfig);


    }

    private void addFontMenu(FileViewerSystem system, FileViewerConfig fileViewerConfig) {
        if (!fileViewerConfig.fontResizeAllowed()) {
            return;
        }
        MenuItem font = this.addItem(VaadinIcon.FONT.create());
        SubMenu fontSubMenu = font.getSubMenu();
        IntegerField fontSize = new IntegerField("Font size");
        fontSize.setMin(8);
        fontSize.setMax(30);
        fontSize.setWidth(100, Unit.PIXELS);
        fontSize.setStepButtonsVisible(true);
        fontSubMenu.addItem(fontSize);
        fontSize.addValueChangeListener(e -> {
            tabSheet.getElement().getStyle().set("--grid-font-size", e.getValue() + "px");
            system.getViewerSettings().setFontSize(e.getValue());
        });

        IntegerField lineHeight = new IntegerField("Line height");
        lineHeight.setMin(8);
        lineHeight.setMax(30);

        lineHeight.setWidth(100, Unit.PIXELS);
        lineHeight.setStepButtonsVisible(true);
        fontSubMenu.addItem(lineHeight);
        lineHeight.addValueChangeListener(e -> {
            tabSheet.getElement().getStyle().set("--grid-line-height", e.getValue() + "px");
            system.getViewerSettings().setLineHeight(e.getValue());
        });

        IntegerField rowHeight = new IntegerField("Row height");
        rowHeight.setMin(14);
        rowHeight.setMax(30);

        rowHeight.setWidth(100, Unit.PIXELS);
        rowHeight.setStepButtonsVisible(true);
        fontSubMenu.addItem(rowHeight);
        rowHeight.addValueChangeListener(e -> {
            tabSheet.getElement().getStyle().set("--grid-row-height", e.getValue() + "px");
            system.getViewerSettings().setRowHeight(e.getValue());
        });
        RetainOpenedMenuItemDecorator.keepOpenOnClick(fontSubMenu);
        fontSize.setValue(system.getViewerSettings().getFontSize());
        lineHeight.setValue(system.getViewerSettings().getLineHeight());
        rowHeight.setValue(system.getViewerSettings().getRowHeight());
    }

    private void addPoolingMenu(FileViewerSystem system, FileViewerConfig fileViewerConfig) {
        if (!fileViewerConfig.pollingEnabled()) {
            return;
        }
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

    private void scrollToTheBeginning() {
        Tab tab = tabSheet.getSelectedTab();
        if (tab != null) {
            tabSheet.getByTab(tab).scrollToFirstLine();
        }
    }

    private void scrollToTheEnd() {
        Tab tab = tabSheet.getSelectedTab();
        if (tab != null) {
            tabSheet.getByTab(tab).scrollToLastLine();
        }
    }
}

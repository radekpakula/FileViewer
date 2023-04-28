
package org.vaadin.addons.viewer.view.content;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.tabs.TabSheetVariant;
import org.vaadin.addons.viewer.application.file.FileInfo;
import org.vaadin.addons.viewer.system.FileViewerSystem;
import org.vaadin.addons.viewer.event.FileCloseAllEvent;
import org.vaadin.addons.viewer.event.FileCloseEvent;

public class FileTabSheet extends TabSheet {

    private final FileViewerSystem system;
    private static final String ACTIVE_CLASS = "notify";
    private final Map<Tab, FileTab> tabMap = new HashMap<>();

    public FileTabSheet(FileViewerSystem system) {
        this.setSizeFull();
        this.addThemeVariants(TabSheetVariant.LUMO_TABS_SMALL);
        this.addThemeName("no-padding");
        this.addThemeName("no-margin");

        this.system = system;

        Button closeAllButton = new Button("Close all", e ->
                new FileCloseAllEvent(this).fireEvent(null));
        closeAllButton.addThemeVariants(ButtonVariant.LUMO_ICON);
        closeAllButton.getElement().setAttribute("aria-label", "Add tab");
        setSuffixComponent(closeAllButton);

        addSelectedChangeListener(e -> removeInformationIconForCurrentlyDisplayed(e.getSelectedTab()));
    }

    public void addTab(FileInfo fileInfo) {
        if (tabMap.values().stream().anyMatch(c -> c.getFileInfo().equals(fileInfo))) {
            tabMap.entrySet()
                    .stream()
                    .filter(entry -> entry.getValue().getFileInfo().equals(fileInfo))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .ifPresent(this::setSelectedTab);
            return;
        }

        final FileTab content = new FileTab(system, fileInfo);
        Tab tab = new Tab(fileInfo.getName());
        tabMap.put(tab, content);
        add(tab, content);
        setSelectedTab(tab);

        final Button close = new Button(VaadinIcon.CLOSE.create(), e ->
                new FileCloseEvent(this).fireEvent(getSelectedTab())
        );
        close.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
        tab.add(close);
    }

    //If panel will be closed without removing elem, vaadin will throw exception
    public void detachAll() {
        tabMap.values().forEach(FileTab::onCloseEvent);
    }

    public void closeTab(Tab tab) {
        remove(tab);
        tabMap.remove(tab);
    }

    public Set<Tab> getTabs() {
        return tabMap.keySet();
    }

    public FileTab getByTab(Tab tab) {
        return tabMap.get(tab);
    }

    public void initializeVisibleTab() {
        this.addSelectedChangeListener(e -> {
                    if (e.getSelectedTab() != null) {
                        tabMap.get(e.getSelectedTab()).showGrid();
                    }
                }
        );
        if (getSelectedTab() != null) {
            tabMap.get(getSelectedTab()).showGrid();
        }
    }

    public void markTabWithAnInformationIcon(Tab tab) {
        if (!tab.getClassNames().contains(ACTIVE_CLASS)) {
            system.getUi().access(() -> {
                tab.addClassName(ACTIVE_CLASS);
                tab.addComponentAtIndex(0, VaadinIcon.INFO_CIRCLE.create());
            });
        }
    }

    public void removeInformationIconForCurrentlyDisplayed(Tab tab) {
        if (tab != null && tab.getClassNames().contains(ACTIVE_CLASS)) {
            system.getUi().access(() -> {
                tab.removeClassName(ACTIVE_CLASS);
                final Optional<Component> image =
                        tab.getChildren().filter(e -> e instanceof Icon).findAny();
                image.ifPresent(tab::remove);
                FileTab content = getByTab(tab);
                content.fileWasChangedEvent();
            });
        }
    }
}

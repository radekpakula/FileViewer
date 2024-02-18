package org.vaadin.addons.viewer.view.menu;

import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;

public class RetainOpenedMenuItemDecorator {
    public static final String CHECKED_ATTR = "menu-item-checked";
    private final MenuItem menuItem;

    public RetainOpenedMenuItemDecorator(MenuItem menuItem) {
        this.menuItem = menuItem;
        menuItem.getElement().addEventListener("click", e -> {
            if (!menuItem.isCheckable()) {
                return;
            }
            // needed because UI change is also prevented
            if (menuItem.isChecked()) {
                menuItem.getElement().executeJs("this.setAttribute('" + CHECKED_ATTR + "', '')");
            } else {
                menuItem.getElement().executeJs("this.removeAttribute('" + CHECKED_ATTR + "')");
            }
        }).addEventData("event.preventDefault()");
    }

    public static void keepOpenOnClick(MenuItem menuItem) {
        new RetainOpenedMenuItemDecorator(menuItem);
    }

    public static void keepOpenOnClick(SubMenu subMenu) {
        subMenu.getItems().forEach(RetainOpenedMenuItemDecorator::keepOpenOnClick);
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }
}

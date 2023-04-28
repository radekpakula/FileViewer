package org.vaadin.addons.viewer.view.content;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.vaadin.addons.viewer.application.file.search.FileSearchIndex;
import org.vaadin.addons.viewer.application.file.search.SearchForm;
import org.vaadin.addons.viewer.system.FileViewerSystem;

public class SearchBarFields extends MenuBar {

    private final Binder<SearchForm> binder;

    private final TextField searchField;
    private final Label percent;
    private final Label matches;
    private final Button searchBtn;
    private final Button stopBtn;
    private final Checkbox autoRefresh;

    public SearchBarFields(FileViewerSystem system, FileSearchLayout fileSearchView) {
        this.addClassName("menu-block");
        this.addThemeVariants(MenuBarVariant.LUMO_TERTIARY);
        this.addThemeVariants(MenuBarVariant.LUMO_SMALL);

        this.searchField = configureSearchField();
        searchField.setWidth(450, Unit.PIXELS);
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addKeyDownListener(event -> {
                    if (event.getKey().matches(Key.ENTER.getKeys().get(0)) || event.getKey()
                            .matches(Key.NUMPAD_ENTER.getKeys().get(0))) {
                        fileSearchView.startSearchEvent();
                    }
                }
        );

        Checkbox ignoreCase = new Checkbox("Ignore case");
        Checkbox regex = new Checkbox("Regex");
        this.autoRefresh = new Checkbox("Auto refresh");
        autoRefresh.setValue(true);

        this.searchBtn = new Button("Search", e -> fileSearchView.startSearchEvent());
        searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

        this.stopBtn = new Button("Stop", e -> fileSearchView.stopSearchEvent());
        stopBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        stopBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        stopBtn.setVisible(false);

        this.percent = new Label();
        this.percent.setVisible(false);

        this.binder = new Binder<>();
        this.binder.setBean(new SearchForm());

        this.matches = new Label("Found: 0");
        this.matches.getStyle().set("text-align", "right");
        this.matches.getStyle().set("margin-right", "5px");

        binder.forField(searchField)
                .withValidator(new StringLengthValidator("Can't be empty", 1, 1000))
                .bind(SearchForm::getText, SearchForm::setText);
        binder.forField(ignoreCase).bind(SearchForm::isIgnoreCase, SearchForm::setIgnoreCase);
        binder.forField(regex).bind(SearchForm::isRegex, SearchForm::setRegex);

        addItem(searchField);
        addItem(ignoreCase);
        addItem(regex);
        if(system.getConfig().pollingEnabled()) {
            addItem(autoRefresh);
        }
        addItem(percent);
        addItem(matches);
        addItem(searchBtn);
        addItem(stopBtn);
    }

    void setSearchMode(boolean searching) {
        percent.setVisible(searching);
        percent.setText("0%");
        searchBtn.setVisible(!searching);
        searchField.setEnabled(!searching);
        stopBtn.setVisible(searching);
    }

    public boolean isValid() {
        return binder.isValid();
    }

    public SearchForm getForm() {
        return binder.getBean();
    }

    public void refreshState(FileSearchIndex fileSearchIndex) {
        matches.setText("Found: " + fileSearchIndex.getMatchCount());
        percent.setText(((int) (fileSearchIndex.processingPercentage() * 100)) + "%");
    }

    public boolean isAutoRefresh() {
        return autoRefresh.getValue();
    }

    private static TextField configureSearchField() {
        TextField searchField = new TextField();
        searchField.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        searchField.addThemeName("no-border");
        searchField.setClassName("search-filter");
        searchField.setClearButtonVisible(true);
        searchField.setPlaceholder("search");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        return searchField;
    }


    // UI ACCESS

    public TextField getSearchField() {
        return searchField;
    }

    public Label getPercent() {
        return percent;
    }

    public Label getMatches() {
        return matches;
    }

    public Button getSearchBtn() {
        return searchBtn;
    }

    public Button getStopBtn() {
        return stopBtn;
    }

    public Checkbox getAutoRefresh() {
        return autoRefresh;
    }
}

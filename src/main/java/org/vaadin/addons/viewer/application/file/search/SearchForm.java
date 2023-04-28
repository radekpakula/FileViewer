package org.vaadin.addons.viewer.application.file.search;

import java.util.Objects;
import java.util.regex.Pattern;

public class SearchForm {

    private String text;
    private boolean ignoreCase;
    private boolean regex;
    private Pattern regexPattern;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    public boolean isRegex() {
        return regex;
    }

    public void setRegex(boolean regex) {
        this.regex = regex;
    }

    public Pattern getRegexPattern() {
        return regexPattern;
    }

    public void setRegexPattern(Pattern regexPattern) {
        this.regexPattern = regexPattern;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SearchForm that = (SearchForm) o;
        return ignoreCase == that.ignoreCase && regex == that.regex && Objects.equals(text, that.text)
                && Objects.equals(regexPattern, that.regexPattern);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, ignoreCase, regex, regexPattern);
    }
}

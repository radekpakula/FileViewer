package org.vaadin.addons.viewer.application.file.search.matcher;

import java.util.regex.Pattern;

public abstract class SearchMatcher {

    protected SearchMatcher next;

    public abstract boolean match(Pattern pattern, String text, String line);

    public void setNextMatcher(SearchMatcher matcher) {
        if (next != null) {
            next.setNextMatcher(matcher);
        } else {
            this.next = matcher;
        }
    }
}

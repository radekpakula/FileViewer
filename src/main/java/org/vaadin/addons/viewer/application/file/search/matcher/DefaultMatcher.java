package org.vaadin.addons.viewer.application.file.search.matcher;

import java.util.regex.Pattern;

public class DefaultMatcher extends SearchMatcher {

    @Override
    public boolean match(Pattern pattern, String text, String line) {
        return line.contains(text);
    }
}

package org.vaadin.addons.viewer.application.file.search.matcher;

import java.util.regex.Pattern;

public class RegexMatcher extends SearchMatcher {

    @Override
    public boolean match(Pattern pattern, String text, String line) {
        return pattern.matcher(line).find();
    }
}

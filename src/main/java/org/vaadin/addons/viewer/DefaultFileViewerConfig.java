package org.vaadin.addons.viewer;

public class DefaultFileViewerConfig implements FileViewerConfig{

    @Override
    public boolean pollingEnabled() {
        return true;
    }

    @Override
    public int pollingInterval() {
        return 3;
    }

    @Override
    public boolean searchViewVisible() {
        return true;
    }
}

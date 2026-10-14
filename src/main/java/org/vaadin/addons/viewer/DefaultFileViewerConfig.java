package org.vaadin.addons.viewer;

/**
 * Default {@link FileViewerConfig}: polling, search UI, and font resizing are enabled,
 * with a polling interval of 3 seconds.
 */
public class DefaultFileViewerConfig implements FileViewerConfig {

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

    @Override
    public boolean fontResizeAllowed() {
        return true;
    }
}

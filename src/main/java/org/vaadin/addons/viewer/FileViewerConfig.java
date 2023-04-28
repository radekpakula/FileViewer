package org.vaadin.addons.viewer;

public interface FileViewerConfig {

    /**
     * By default, set to true
     * If false, the top bar with polling and monitor configuration will be removed and background task for
     * polling will be disabled
     */
    boolean pollingEnabled();

    /**
     * Polling interval. By default, set to 3 seconds.
     */
    int pollingInterval();

    /**
     * By default, set to true
     * If false, the bottom bar for searching and also search result grid will be removed.
     */
    boolean searchViewVisible();
}

package org.vaadin.addons.viewer;

/**
 * Controls optional features and polling defaults of a {@link FileViewer}.
 * Use {@link DefaultFileViewerConfig} as a base when overriding only some options.
 */
public interface FileViewerConfig {

    /**
     * Whether background polling is enabled and its controls are displayed.
     *
     * @return {@code true} to enable polling; the default is {@code true}
     */
    boolean pollingEnabled();

    /**
     * Returns the initial polling interval, in seconds.
     *
     * @return polling interval in seconds; the default is 3 seconds
     */
    int pollingInterval();

    /**
     * Whether the search bar and search-results grid are displayed.
     *
     * @return {@code true} to display search UI; the default is {@code true}
     */
    boolean searchViewVisible();

    /**
     * Whether users can change font size, line height, and grid row height
     * from the viewer's font menu.
     *
     * @return {@code true} to allow interactive resizing; the default is {@code true}
     */
    boolean fontResizeAllowed();

}

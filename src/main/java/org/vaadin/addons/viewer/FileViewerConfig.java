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


    /**
     * By default, set to false
     * If true, you have to extends component style (frontend/your_theme_name/components/vaadin-grind.css
     * about:
     *
     *   vaadin-grid{
     *     --grid-row-height: 20px;
     *     --grid-line-height: 20px;
     *     --grid-font-size: 18px;
     *   }
     *
     *   vaadin-grid:host([theme~='log']) [part~='cell'] {
     *     min-height: var(--grid-row-height);
     *     font-size: var(--grid-font-size);
     *     line-height: var(--grid-line-height);
     *   }
     */
    boolean fontResizeAllowed();
}

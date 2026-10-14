package org.vaadin.addons.viewer.system;

import org.vaadin.addons.viewer.FileViewer;

/**
 * Initial display settings for a {@link FileViewer}.
 * All values are expressed in pixels.
 */
public class ViewerSettings {

    private int fontSize;
    private int lineHeight;
    private int rowHeight;

    /**
     * Creates display settings.
     *
     * @param fontSize text font size in pixels
     * @param lineHeight text line height in pixels
     * @param rowHeight grid row height in pixels
     */
    public ViewerSettings(int fontSize, int lineHeight, int rowHeight) {
        this.fontSize = fontSize;
        this.lineHeight = lineHeight;
        this.rowHeight = rowHeight;
    }

    /**
     * Returns the default display settings: font size 20px, line height 20px,
     * and grid row height 18px.
     *
     * @return default display settings
     */
    public static ViewerSettings base() {
        return new ViewerSettings(20, 20, 18);
    }

    /** @return text font size in pixels */
    public int getFontSize() {
        return fontSize;
    }

    /** @param fontSize text font size in pixels */
    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    /** @return text line height in pixels */
    public int getLineHeight() {
        return lineHeight;
    }

    /** @param lineHeight text line height in pixels */
    public void setLineHeight(int lineHeight) {
        this.lineHeight = lineHeight;
    }

    /** @return grid row height in pixels */
    public int getRowHeight() {
        return rowHeight;
    }

    /** @param rowHeight grid row height in pixels */
    public void setRowHeight(int rowHeight) {
        this.rowHeight = rowHeight;
    }

    /**
     * Creates an independent copy of these display settings.
     *
     * @return a copy containing the current font size, line height, and row height
     */
    public ViewerSettings getCopy() {
        return new ViewerSettings(fontSize, lineHeight, rowHeight);
    }
}

package org.vaadin.addons.viewer.system;

import org.vaadin.addons.viewer.FileViewer;

/**
 * Initial display settings for a {@link FileViewer}.
 * All values are expressed in pixels.
 */
public class ViewerSettings {

    public static final int MIN_FONT_SIZE = 8;
    public static final int MAX_FONT_SIZE = 30;
    public static final int MIN_LINE_HEIGHT = 8;
    public static final int MAX_LINE_HEIGHT = 30;
    public static final int MIN_ROW_HEIGHT = 14;
    public static final int MAX_ROW_HEIGHT = 30;

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
        validateRange("fontSize", fontSize, MIN_FONT_SIZE, MAX_FONT_SIZE);
        validateRange("lineHeight", lineHeight, MIN_LINE_HEIGHT, MAX_LINE_HEIGHT);
        validateRange("rowHeight", rowHeight, MIN_ROW_HEIGHT, MAX_ROW_HEIGHT);
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

    private static void validateRange(String name, int value, int min, int max) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(name + " must be between " + min + " and " + max);
        }
    }

    /** @return text font size in pixels */
    public int getFontSize() {
        return fontSize;
    }

    /** @param fontSize text font size in pixels */
    public void setFontSize(int fontSize) {
        validateRange("fontSize", fontSize, MIN_FONT_SIZE, MAX_FONT_SIZE);
        this.fontSize = fontSize;
    }

    /** @return text line height in pixels */
    public int getLineHeight() {
        return lineHeight;
    }

    /** @param lineHeight text line height in pixels */
    public void setLineHeight(int lineHeight) {
        validateRange("lineHeight", lineHeight, MIN_LINE_HEIGHT, MAX_LINE_HEIGHT);
        this.lineHeight = lineHeight;
    }

    /** @return grid row height in pixels */
    public int getRowHeight() {
        return rowHeight;
    }

    /** @param rowHeight grid row height in pixels */
    public void setRowHeight(int rowHeight) {
        validateRange("rowHeight", rowHeight, MIN_ROW_HEIGHT, MAX_ROW_HEIGHT);
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

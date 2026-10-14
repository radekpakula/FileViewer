package org.vaadin.addons.viewer.system;

import org.vaadin.addons.viewer.FileViewer;

/**
 * Display settings for a {@link FileViewer}. All values are expressed in pixels.
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
     * Creates display settings and validates them against the supported UI ranges.
     *
     * @param fontSize text font size in pixels, from 8 to 30
     * @param lineHeight text line height in pixels, from 8 to 30
     * @param rowHeight grid row height in pixels, from 14 to 30
     */
    public ViewerSettings(int fontSize, int lineHeight, int rowHeight) {
        setFontSize(fontSize);
        setLineHeight(lineHeight);
        setRowHeight(rowHeight);
    }

    public static ViewerSettings base() {
        return new ViewerSettings(20, 20, 18);
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = requireRange("fontSize", fontSize, MIN_FONT_SIZE, MAX_FONT_SIZE);
    }

    public int getLineHeight() {
        return lineHeight;
    }

    public void setLineHeight(int lineHeight) {
        this.lineHeight = requireRange("lineHeight", lineHeight, MIN_LINE_HEIGHT, MAX_LINE_HEIGHT);
    }

    public int getRowHeight() {
        return rowHeight;
    }

    public void setRowHeight(int rowHeight) {
        this.rowHeight = requireRange("rowHeight", rowHeight, MIN_ROW_HEIGHT, MAX_ROW_HEIGHT);
    }

    public ViewerSettings getCopy() {
        return new ViewerSettings(fontSize, lineHeight, rowHeight);
    }

    private static int requireRange(String name, int value, int min, int max) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(name + " must be between " + min + " and " + max);
        }
        return value;
    }
}

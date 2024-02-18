package org.vaadin.addons.viewer.system;

public class ViewerSettings {

    private int fontSize;
    private int lineHeight;
    private int rowHeight;

    public ViewerSettings(int fontSize, int lineHeight, int rowHeight) {
        this.fontSize = fontSize;
        this.lineHeight = lineHeight;
        this.rowHeight = rowHeight;
    }

    public static ViewerSettings base(){
        return new ViewerSettings(20,20,18);
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public int getLineHeight() {
        return lineHeight;
    }

    public void setLineHeight(int lineHeight) {
        this.lineHeight = lineHeight;
    }

    public int getRowHeight() {
        return rowHeight;
    }

    public void setRowHeight(int rowHeight) {
        this.rowHeight = rowHeight;
    }
}

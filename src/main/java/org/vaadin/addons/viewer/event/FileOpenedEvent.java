
package org.vaadin.addons.viewer.event;

import org.vaadin.addons.viewer.application.file.FileInfo;
import org.vaadin.addons.viewer.view.content.FileTabSheet;

public class FileOpenedEvent implements EventAction<FileInfo> {

    private final FileTabSheet fileTabSheet;

    public FileOpenedEvent(FileTabSheet fileTabSheet) {
        this.fileTabSheet = fileTabSheet;
    }

    @Override
    public void fireEvent(FileInfo fileInfo) {
        fileTabSheet.addTab(fileInfo);
    }
}

package org.vaadin.addons.viewer.system;

import com.vaadin.flow.component.UI;
import org.vaadin.addons.viewer.FileViewerConfig;
import org.vaadin.addons.viewer.view.content.FilePolling;

public class FileViewerSystem {

    private UI ui;
    private final FileViewerConfig config;
    private FilePolling filePolling;
    private boolean followChanges = true;

    public FileViewerSystem(FileViewerConfig config) {
        this.ui = UI.getCurrent();
        this.config = config;
    }

    public UI getUi() {
        if(UI.getCurrent()!=null){
            this.ui = UI.getCurrent();
        }
        return ui;
    }

    public boolean isFollowChanges() {
        return followChanges;
    }

    public void setFollowChanges(boolean followChanges) {
        this.followChanges = followChanges;
    }

    public void setFilePolling(FilePolling filePolling) {
        this.filePolling = filePolling;
    }

    public void interruptFilePolling() {
        filePolling.interrupt();
    }

    public void enablePolling(Boolean enable, Integer pollingTime) {
        if (enable) {
            filePolling.schedule(pollingTime);
        } else {
            filePolling.interrupt();
        }
    }

    public FileViewerConfig getConfig() {
        return config;
    }
}

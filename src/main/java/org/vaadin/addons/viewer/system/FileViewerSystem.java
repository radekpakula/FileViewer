package org.vaadin.addons.viewer.system;

import com.vaadin.flow.component.UI;
import org.vaadin.addons.viewer.FileViewerConfig;
import org.vaadin.addons.viewer.view.content.FilePolling;

public class FileViewerSystem {

    private UI ui;
    private final FileViewerConfig config;
    private FilePolling filePolling;
    private boolean followChanges = true;

    private final ViewerSettings viewerSettings;
    private Boolean pollingEnabled;
    private Integer pollingInterval;

    public FileViewerSystem(FileViewerConfig config) {
        this(config,ViewerSettings.base());
    }

    public FileViewerSystem(FileViewerConfig config, ViewerSettings viewerSettings) {
        this.config = config;
        this.viewerSettings = viewerSettings;
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

    public void enablePolling(Boolean enable, Integer pollingInterval) {
        this.pollingEnabled = enable;
        this.pollingInterval = pollingInterval;
        if (enable) {
            filePolling.schedule(pollingInterval);
        } else {
            filePolling.interrupt();
        }
    }

    public FileViewerConfig getConfig() {
        return new FileViewerConfig() {
            @Override
            public boolean pollingEnabled() {
                return pollingEnabled;
            }

            @Override
            public int pollingInterval() {
                return pollingInterval;
            }

            @Override
            public boolean searchViewVisible() {
                return config.searchViewVisible();
            }

            @Override
            public boolean fontResizeAllowed() {
                return config.fontResizeAllowed();
            }
        };
    }

    public ViewerSettings getViewerSettings() {
        return viewerSettings;
    }

}

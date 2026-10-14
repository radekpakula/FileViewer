package org.vaadin.addons.viewer.system;

import java.util.Objects;

import com.vaadin.flow.component.UI;
import org.vaadin.addons.viewer.FileViewerConfig;
import org.vaadin.addons.viewer.view.content.FilePolling;

public class FileViewerSystem {

    private UI ui;
    private final FileViewerConfig config;
    private FilePolling filePolling;
    private boolean followChanges = true;
    private final ViewerSettings viewerSettings;
    private boolean pollingEnabled;
    private int pollingInterval;

    public FileViewerSystem(FileViewerConfig config) {
        this(config, ViewerSettings.base());
    }

    public FileViewerSystem(FileViewerConfig config, ViewerSettings viewerSettings) {
        this.config = Objects.requireNonNull(config, "config");
        this.viewerSettings = Objects.requireNonNull(viewerSettings, "viewerSettings");
        this.pollingEnabled = config.pollingEnabled();
        this.pollingInterval = config.pollingInterval();
    }

    public UI getUi() {
        UI current = UI.getCurrent();
        if (current != null) {
            ui = current;
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
        this.filePolling = Objects.requireNonNull(filePolling, "filePolling");
    }

    public void interruptFilePolling() {
        if (filePolling != null) {
            filePolling.interrupt();
        }
    }

    public void enablePolling(Boolean enable, Integer pollingInterval) {
        Objects.requireNonNull(enable, "enable");
        if (pollingInterval == null || pollingInterval < 1 || pollingInterval > 30) {
            throw new IllegalArgumentException("Polling interval must be between 1 and 30 seconds");
        }
        this.pollingEnabled = enable;
        this.pollingInterval = pollingInterval;
        if (enable) {
            if (filePolling == null) {
                throw new IllegalStateException("File polling has not been initialized");
            }
            filePolling.schedule(pollingInterval);
        } else {
            interruptFilePolling();
        }
    }

    /**
     * Returns a snapshot of the feature configuration and current polling state.
     */
    public FileViewerConfig getConfig() {
        boolean currentPollingEnabled = pollingEnabled;
        int currentPollingInterval = pollingInterval;
        return new FileViewerConfig() {
            @Override
            public boolean pollingEnabled() {
                return currentPollingEnabled;
            }

            @Override
            public int pollingInterval() {
                return currentPollingInterval;
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

package org.vaadin.addons.viewer.view.content;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.tabs.Tab;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addons.viewer.system.FileViewerSystem;

/**
 * Periodically checks open files for changes and refreshes the selected tab or
 * marks inactive tabs as changed. Component state is accessed only through
 * the Vaadin UI thread.
 */
public class FilePolling {

    private static final Logger LOG = LoggerFactory.getLogger(FilePolling.class);

    private final FileViewerSystem system;
    private final FileTabSheet fileTabSheet;
    private ScheduledExecutorService executorService;

    public FilePolling(FileViewerSystem system, FileTabSheet fileTabSheet) {
        this.system = system;
        this.fileTabSheet = fileTabSheet;
    }

    private void scheduleCheck() {
        UI ui = system.getUi();
        if (ui == null || !ui.isAttached()) {
            LOG.debug("Skipping file polling because the UI is unavailable or detached");
            interrupt();
            return;
        }

        ui.access(() -> {
            if (!ui.isAttached()) {
                interrupt();
                return;
            }
            checkOpenedFilesOnUiThread();
        });
    }

    private void checkOpenedFilesOnUiThread() {
        for (Tab tab : fileTabSheet.getTabs()) {
            FileTab tabContent = fileTabSheet.getByTab(tab);
            if (tabContent == null) {
                continue;
            }
            try {
                if (!tabContent.getFileInfo().isFileChanged()) {
                    continue;
                }
                boolean contentRewritten = tabContent.getFileInfo().isContentRewritten();
                tabContent.getFileInfo().updateFileLength();
                tabContent.setContentRewritten(contentRewritten);
                Tab selectedTab = fileTabSheet.getSelectedTab();
                if (selectedTab == null || !selectedTab.equals(tab)) {
                    fileTabSheet.markTabWithAnInformationIcon(tab);
                } else {
                    tabContent.fileWasChangedEvent();
                }
            } catch (Exception e) {
                LOG.error("Error while polling file {}", tabContent.getFileInfo().getFile(), e);
            }
        }
    }

    public synchronized void interrupt() {
        if (executorService != null) {
            executorService.shutdownNow();
            executorService = null;
        }
    }

    public synchronized void schedule(Integer pollingTime) {
        interrupt();
        if (pollingTime == null || pollingTime < 1) {
            throw new IllegalArgumentException("Polling interval must be at least one second");
        }
        executorService = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "file-viewer-polling");
            thread.setDaemon(true);
            return thread;
        });
        executorService.scheduleAtFixedRate(this::scheduleCheck, 2, pollingTime, TimeUnit.SECONDS);
    }
}

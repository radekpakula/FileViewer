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
 * Polling files task. Checks all open files for size changes.
 * If the file has been changed, the tab id extended about information icon, and the open tab receives information
 * that the content needs to be updated.
 */
public class FilePolling {

    private static final Logger LOG = LoggerFactory.getLogger(FilePolling.class);

    private final FileTabSheet fileTabSheet;
    private ScheduledExecutorService executorService;
    private final UI ui;

    public FilePolling(FileViewerSystem system, FileTabSheet fileTabSheet) {
        this.fileTabSheet = fileTabSheet;
        this.ui = system.getUi();
    }

    private void checkOpenedFile() {
        if (ui == null || !ui.isAttached()) {
            interrupt();
            LOG.debug("Interrupt file polling because of detached UI");
            return;
        }
        ui.access(() -> {
            if (!ui.isAttached()) {
                return;
            }
            for (Tab tab : fileTabSheet.getTabs()) {
                try {
                    FileTab tabContent = fileTabSheet.getByTab(tab);
                    if (tabContent == null || !tabContent.getFileInfo().isFileChanged()) {
                        continue;
                    }
                    tabContent.getFileInfo().updateFileLength();
                    if (fileTabSheet.getSelectedTab() == null || !fileTabSheet.getSelectedTab().equals(tab)) {
                        fileTabSheet.markTabWithAnInformationIcon(tab);
                    } else {
                        tabContent.fileWasChangedEvent();
                    }
                } catch (Exception e) {
                    LOG.error("Error during polling file in tab {}", tab, e);
                }
            }
        });
    }

    public void interrupt() {
        if (executorService != null) {
            executorService.shutdown();
            executorService = null;
        }
    }

    public void schedule(Integer pollingTime) {
        if (pollingTime == null || pollingTime < 1) {
            throw new IllegalArgumentException("Polling interval must be at least one second");
        }
        executorService = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "file-viewer-polling");
            thread.setDaemon(true);
            return thread;
        });
        executorService.scheduleAtFixedRate(this::checkOpenedFile, 2, pollingTime, TimeUnit.SECONDS);
    }
}

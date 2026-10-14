package org.vaadin.addons.viewer;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addons.viewer.application.file.FileInfo;
import org.vaadin.addons.viewer.event.FileOpenedEvent;
import org.vaadin.addons.viewer.system.FileViewerSystem;
import org.vaadin.addons.viewer.system.ViewerSettings;
import org.vaadin.addons.viewer.view.content.FilePolling;
import org.vaadin.addons.viewer.view.content.FileTabSheet;
import org.vaadin.addons.viewer.view.menu.ViewerMenuBar;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.ElementDetachEvent;
import com.vaadin.flow.dom.ElementDetachListener;

/**
 * Vaadin component for viewing, searching, and monitoring text files.
 * Files are opened in separate tabs. UTF-8 is used unless a charset is
 * explicitly supplied to {@link #openFile(Path, Charset)}.
 */
@StyleSheet("fileviewer.css")
public class FileViewer extends VerticalLayout implements ElementDetachListener {
    private static final Logger LOG = LoggerFactory.getLogger(FileViewer.class);
    private final FileViewerSystem system;
    private final ViewerMenuBar menuBar;
    private final FileTabSheet fileTabSheet;

    /**
     * Creates a viewer with the default configuration and default display settings.
     */
    public FileViewer() {
        this(new DefaultFileViewerConfig());
    }

    /**
     * Creates a viewer with the supplied feature configuration and default display settings.
     *
     * @param fileViewerConfig polling, search visibility, and font-resizing configuration
     */
    public FileViewer(FileViewerConfig fileViewerConfig) {
        this(fileViewerConfig, ViewerSettings.base());
    }

    /**
     * Creates a viewer with explicit feature configuration and initial display settings.
     *
     * @param fileViewerConfig polling, search visibility, and font-resizing configuration
     * @param viewerSettings initial font size, line height, and grid row height, in pixels
     */
    public FileViewer(FileViewerConfig fileViewerConfig, ViewerSettings viewerSettings) {
        this.setSizeFull();
        this.setPadding(false);
        this.setSpacing(false);
        this.setMargin(false);
        this.system = new FileViewerSystem(fileViewerConfig, viewerSettings);
        this.fileTabSheet = new FileTabSheet(system);
        this.system.setFilePolling(new FilePolling(system, fileTabSheet));
        this.system.enablePolling(fileViewerConfig.pollingEnabled(), fileViewerConfig.pollingInterval());
        this.menuBar = new ViewerMenuBar(system, fileTabSheet, fileViewerConfig);
        add(menuBar);
        add(fileTabSheet);
        this.fileTabSheet.initializeVisibleTab();
    }

    /**
     * Stops polling and detaches the opened file tabs.
     */
    public void onCloseEvent() {
        system.interruptFilePolling();
        fileTabSheet.detachAll();
    }

    /**
     * Opens a file using UTF-8.
     *
     * @param path path to the text file
     */
    public void openFile(Path path) {
        openFile(path, StandardCharsets.UTF_8);
    }

    /**
     * Opens a file using the requested character encoding.
     * The charset must encode CR and LF as single ASCII bytes; UTF-16 encodings
     * are not supported by the byte-based line indexer.
     *
     * @param path path to the text file
     * @param charset encoding used to decode the file
     */
    public void openFile(Path path, Charset charset) {
        LOG.debug("Open file {} using charset {}", path, charset);
        new FileOpenedEvent(fileTabSheet).fireEvent(FileInfo.of(path, charset));
    }

    @Override
    public void onDetach(ElementDetachEvent elementDetachEvent) {
        onCloseEvent();
    }

    /**
     * Returns the viewer's top menu bar.
     *
     * @return the menu bar
     */
    public ViewerMenuBar getMenuBar() {
        return menuBar;
    }

    /**
     * Returns the tab sheet containing opened files.
     *
     * @return the file tab sheet
     */
    public FileTabSheet getFileTabSheet() {
        return fileTabSheet;
    }

    /**
     * Returns a copy of the current display settings.
     * Changes made to the returned object do not modify the viewer's settings.
     *
     * @return a copy of the current font size, line height, and row height settings
     */
    public ViewerSettings getViewerSettings() {
        return system.getViewerSettings().getCopy();
    }

    /**
     * Returns the configuration used to initialize this viewer.
     *
     * @return the viewer's feature configuration
     */
    public FileViewerConfig getViewerConfig() {
        return system.getConfig();
    }

}

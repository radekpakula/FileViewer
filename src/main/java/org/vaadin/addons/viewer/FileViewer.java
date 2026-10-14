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

@StyleSheet("fileviewer.css")
public class FileViewer extends VerticalLayout implements ElementDetachListener {
    private static final Logger LOG = LoggerFactory.getLogger(FileViewer.class);
    private final FileViewerSystem system;
    private final ViewerMenuBar menuBar;
    private final FileTabSheet fileTabSheet;

    public FileViewer() {
        this(new DefaultFileViewerConfig());
    }

    public FileViewer(FileViewerConfig fileViewerConfig) {
        this(fileViewerConfig, ViewerSettings.base());
    }

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

    public void onCloseEvent() {
        system.interruptFilePolling();
        fileTabSheet.detachAll();
    }

    public void openFile(Path path) {
        openFile(path, StandardCharsets.UTF_8);
    }

    public void openFile(Path path, Charset charset) {
        LOG.debug("Open file {} using charset {}", path, charset);
        new FileOpenedEvent(fileTabSheet).fireEvent(FileInfo.of(path, charset));
    }

    @Override
    public void onDetach(ElementDetachEvent elementDetachEvent) {
        onCloseEvent();
    }

    public ViewerMenuBar getMenuBar() {
        return menuBar;
    }

    public FileTabSheet getFileTabSheet() {
        return fileTabSheet;
    }

}

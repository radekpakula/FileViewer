package org.vaadin.addons.mygroup;

import java.io.File;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;
import org.vaadin.addons.viewer.FileViewer;
import org.vaadin.addons.viewer.FileViewerConfig;

@Route("")
@Push
@PreserveOnRefresh
public class AddonView extends Div implements AppShellConfigurator {

    public AddonView() {
        setSizeFull();
        FileViewer viewer = new FileViewer(new FileViewerConfig(){

            @Override
            public boolean pollingEnabled() {
                return true;
            }

            @Override
            public int pollingInterval() {
                return 1;
            }

            @Override
            public boolean searchViewVisible() {
                return true;
            }

            @Override
            public boolean fontResizeAllowed() {
                return true;
            }
        });
        viewer.setId("theAddon");

        add(viewer);

        viewer.openFile(new File("C:\\temp\\coupler\\logs\\coupler.log").toPath());
        viewer.openFile(new File("C:\\temp\\coupler\\logs\\system.log").toPath());
    }

}

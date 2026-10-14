package org.vaadin.addons.viewer;

import java.io.File;
import java.nio.charset.Charset;

import org.vaadin.addons.viewer.system.ViewerSettings;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("")
public class AddonView extends Div {

    public AddonView() {
        setSizeFull();
        FileViewer viewer = new FileViewer(new FileViewerConfig() {

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
        }, new ViewerSettings(12, 12, 15));
        viewer.setId("theAddon");
        add(viewer);

        viewer.openFile(new File("src/test/resources/test.txt").toPath(), Charset.defaultCharset());
        //return current configuration for futher processing
        viewer.getViewerSettings();
        viewer.getViewerConfig();
    }

}

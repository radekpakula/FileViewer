package org.vaadin.addons.viewer;

import java.io.File;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("")
//@Push
//@PreserveOnRefresh
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
        });
        viewer.setId("theAddon");

        add(viewer);

        viewer.openFile(new File("src/test/resources/test.txt").toPath());
    }

}

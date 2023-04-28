# Vaadin addon - File viewer (glogg)
![image](https://user-images.githubusercontent.com/1457603/235087580-accbe673-caa3-4b96-b00c-ba262b74d554.png)

This add-on is based on the popular log viewer software called glogg.

Enables:
- Viewing huge files, especially logs, without performance or ram usage loss
- Monitoring changes in displayed files and dynamically extending the content
- File search
- Dynamic extension of search results with changes in files (new entries)


Requires @Push enabled for stable and comfortable work

### Usage
```
FileViewer viewer = new FileViewer();
viewer.openFile(new File("...).toPath());
viewer.openFile(new File("...").toPath());
viewer.openFile(new File("...").toPath());
```
### Styling

Since the extension uses default vaadin components, all used components have been made available for modification 
via getters.

If you want to display the entire log line without text-wrapping, you need to extend your style file vaadin-grid.css about this entry:
```
:host([theme~='log']) [part~='cell']:not([part~='details-cell']) {
    white-space: break-spaces;
}
```
### Additional configuration

- You can disable/hide polling configuration and also search bar. To do that you have to pass configuration to the constructor of the FileViewer
```
    FileViewer viewer = new FileViewer(new FileViewerConfig(){

            @Override
            public boolean pollingEnabled() {
                return false;
            }

            @Override
            public int pollingInterval() {
                return 1;
            }

            @Override
            public boolean searchViewVisible() {
                return false;
            }
        });
```
### Layout structure

```
|---------------------------------------------------------------|
|                FileViewer - VerticalLayout                    |
|  |---------------------------------------------------------|  |
|  |              PollingMenuBar  - MenuBar                  |  |
|  |---------------------------------------------------------|  |
|  |               FileTabSheet  - TabSheet                  |  |
|  |  |---------------------------------------------------|  |  |
|  |  |               FileTab  -  Div                     |  |  |
|  |  |  |---------------------------------------------|  |  |  |
|  |  |  |          FileContentGrid - Grid             |  |  |  |
|  |  |  |---------------------------------------------|  |  |  |
|  |  |  |      FileSearchLayout - VerticalLayout      |  |  |  |
|  |  |  |  |---------------------------------------|  |  |  |  |
|  |  |  |  |       SearchBarFields - MenuBar       |  |  |  |  |
|  |  |  |  |---------------------------------------|  |  |  |  |
|  |  |  |  |        SearchResultGrid - Grid        |  |  |  |  |
|  |  |  |  |---------------------------------------|  |  |  |  |
|  |  |  |                                             |  |  |  |
|  |  |  | --------------------------------------------|  |  |  |
|  |  |---------------------------------------------------|  |  |
|  |---------------------------------------------------------|  |
|---------------------------------------------------------------|
```
Original template https://github.com/vaadin/addon-template

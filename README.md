# Vaadin FileViewer

FileViewer is a Vaadin Flow component for viewing and monitoring large text files, especially application logs. It is inspired by [glogg](https://github.com/nickbnf/glogg).

## Features

- Opens multiple files in tabs.
- Indexes files and displays their contents in a grid, including large files.
- Searches file contents and displays matching lines in a separate results grid.
- Supports case-sensitive/case-insensitive and regular-expression searches.
- Can poll open files for appended content and update the content and search-results grids.
- Supports selecting a character encoding per file.
- Lets users adjust font size, line height, and row height, or initialize those values in code.
- Allows polling, search UI, and interactive font resizing to be configured.

For a responsive UI when polling is enabled, configure Vaadin Push in your application.

## Basic usage

Add the component to a Vaadin view and open a file using a `Path`:

```java
import java.nio.file.Path;
import org.vaadin.addons.viewer.FileViewer;

FileViewer fileViewer = new FileViewer();
add(fileViewer);

fileViewer.openFile(Path.of("/var/log/application.log"));
```

Calling `openFile(Path)` uses UTF-8. To open a file with a different encoding, pass a `Charset`:

```java
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

fileViewer.openFile(Path.of("/var/log/application.log"), StandardCharsets.UTF_8);
fileViewer.openFile(Path.of("/var/log/legacy.log"), Charset.forName("windows-1250"));
```

The encoding must represent CR and LF as their single ASCII bytes. UTF-8 and Windows-1250 are examples of supported encodings; UTF-16 encodings are rejected because their line delimiters are not represented this way.

## Configuration

Pass a `FileViewerConfig` to control polling, search visibility, and whether users can resize the font. The defaults are provided by `DefaultFileViewerConfig`: polling, search, and font resizing are enabled, and the polling interval is 3 seconds.

```java
import org.vaadin.addons.viewer.DefaultFileViewerConfig;
import org.vaadin.addons.viewer.FileViewer;
import org.vaadin.addons.viewer.FileViewerConfig;

FileViewerConfig config = new DefaultFileViewerConfig() {
    @Override
    public boolean pollingEnabled() {
        return true;
    }

    @Override
    public int pollingInterval() {
        return 3; // seconds
    }

    @Override
    public boolean searchViewVisible() {
        return true;
    }

    @Override
    public boolean fontResizeAllowed() {
        return true;
    }
};

FileViewer fileViewer = new FileViewer(config);
add(fileViewer);
```

Configuration options:

| Option | Description |
| --- | --- |
| `pollingEnabled()` | Enables background polling and displays the polling controls. |
| `pollingInterval()` | Initial polling interval in seconds. The UI allows values from 1 to 30 seconds. |
| `searchViewVisible()` | Shows or hides the search bar and search-results grid. |
| `fontResizeAllowed()` | Shows or hides the menu for changing font size, line height, and row height. |

You can also use `new FileViewer()` for the default configuration.

## Initial font and row settings

Use the `FileViewer(FileViewerConfig, ViewerSettings)` constructor to set the initial font size, line height, and row height. Values are in pixels.

```java
import org.vaadin.addons.viewer.DefaultFileViewerConfig;
import org.vaadin.addons.viewer.FileViewer;
import org.vaadin.addons.viewer.system.ViewerSettings;

ViewerSettings settings = new ViewerSettings(
    16, // font size in px
    20, // line height in px
    18  // grid row height in px
);

FileViewer fileViewer = new FileViewer(new DefaultFileViewerConfig(), settings);
add(fileViewer);
```

The default settings are `fontSize = 20`, `lineHeight = 20`, and `rowHeight = 18`. If font resizing is enabled, users can subsequently adjust these values from the font menu. The interactive controls accept font-size and line-height values from 8 to 30, and row-height values from 14 to 30.

## Layout

The component contains a top menu, a tab sheet with the opened files, and—when enabled—a search area for the selected file.

```text
FileViewer
├── ViewerMenuBar (polling controls, navigation, font settings)
└── FileTabSheet
    └── FileTab (one per opened file)
        ├── FileContentGrid
        └── FileSearchLayout (optional)
            ├── SearchBarFields
            └── SearchResultGrid
```

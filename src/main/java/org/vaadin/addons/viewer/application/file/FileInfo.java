package org.vaadin.addons.viewer.application.file;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

import org.vaadin.addons.viewer.application.exception.ViewerException;

public class FileInfo {

    protected final File file;
    protected long length;

    private FileInfo(File file) {
        if (file == null) {
            throw new NullPointerException();
        }
        if (!file.exists()) {
            throw new ViewerException("File not exists");
        }
        this.file = file;
        this.length = file.length();
    }

    public static FileInfo of(Path path) {
        return new FileInfo(path.toFile());
    }

    public static FileInfo of(File file) {
        return new FileInfo(file);
    }

    public boolean isFile() {
        return file.isFile();
    }

    public String getName() {
        if (file.getName().isEmpty()) {
            return file.getPath();
        }
        return file.getName();
    }

    public File getFile() {
        return file;
    }

    public boolean isFileChanged() {
        return length != file.length();
    }

    public void updateFileLength() {
        this.length = file.length();
    }

    @Override
    public String toString() {
        return "FileInfo={" +
                "file='" + file + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FileInfo fileInfo = (FileInfo) o;
        return Objects.equals(file, fileInfo.file);
    }

    @Override
    public int hashCode() {
        return Objects.hash(file);
    }

}

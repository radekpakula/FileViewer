package org.vaadin.addons.viewer.application.file;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Objects;

import org.vaadin.addons.viewer.application.exception.ViewerException;

public class FileInfo {

    protected final File file;
    protected long length;
    private long lastModified;
    private final Charset charset;

    private FileInfo(File file, Charset charset) {
        Objects.requireNonNull(file, "file");
        if (!file.exists()) {
            throw new ViewerException("File does not exist");
        }
        this.file = file;
        this.charset = Objects.requireNonNull(charset, "charset");
        EncodedLineReader.validateCharset(charset);
        updateFileMetadata();
    }

    public static FileInfo of(Path path) {
        return of(path, StandardCharsets.UTF_8);
    }

    public static FileInfo of(Path path, Charset charset) {
        return new FileInfo(Objects.requireNonNull(path, "path").toFile(), charset);
    }

    public static FileInfo of(File file) {
        return of(file, StandardCharsets.UTF_8);
    }

    public static FileInfo of(File file, Charset charset) {
        return new FileInfo(file, charset);
    }

    public boolean isFile() {
        return file.isFile();
    }

    public String getName() {
        return file.getName().isEmpty() ? file.getPath() : file.getName();
    }

    public File getFile() {
        return file;
    }

    public Charset getCharset() {
        return charset;
    }

    public boolean isFileChanged() {
        return length != file.length() || lastModified != file.lastModified();
    }

    public boolean requiresIndexRebuild() {
        long currentLength = file.length();
        return currentLength < length || currentLength == length && lastModified != file.lastModified();
    }

    public void updateFileLength() {
        updateFileMetadata();
    }

    private void updateFileMetadata() {
        this.length = file.length();
        this.lastModified = file.lastModified();
    }

    @Override
    public String toString() {
        return "FileInfo={file='" + file + "'}";
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
        return Objects.equals(file, fileInfo.file) && Objects.equals(charset, fileInfo.charset);
    }

    @Override
    public int hashCode() {
        return Objects.hash(file, charset);
    }
}

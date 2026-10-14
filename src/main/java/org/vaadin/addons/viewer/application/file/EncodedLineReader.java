package org.vaadin.addons.viewer.application.file;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Objects;

public final class EncodedLineReader {
    private final RandomAccessFile file;
    private final Charset charset;

    public EncodedLineReader(RandomAccessFile file, Charset charset) {
        this.file = Objects.requireNonNull(file);
        this.charset = Objects.requireNonNull(charset);
        validateCharset(charset);
    }

    private static void validateCharset(Charset charset) {
        if (!Arrays.equals("\n".getBytes(charset), new byte[]{'\n'})
                || !Arrays.equals("\r".getBytes(charset), new byte[]{'\r'})) {
            throw new IllegalArgumentException("Charset must encode CR and LF as single ASCII bytes: " + charset);
        }
    }

    public String readLine() throws IOException {
        ByteArrayOutputStream line = new ByteArrayOutputStream();
        boolean readAny = false;
        int value;
        while ((value = file.read()) != -1) {
            readAny = true;
            if (value == '\n') {
                byte[] bytes = line.toByteArray();
                if (bytes.length > 0 && bytes[bytes.length - 1] == '\r') {
                    bytes = Arrays.copyOf(bytes, bytes.length - 1);
                }
                return new String(bytes, charset);
            }
            line.write(value);
        }
        if (!readAny && line.size() == 0) {
            return null;
        }
        return new String(line.toByteArray(), charset);
    }
}

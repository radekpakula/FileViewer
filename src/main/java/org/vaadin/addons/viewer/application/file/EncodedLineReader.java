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

    public static void validateCharset(Charset charset) {
        if (!Arrays.equals("\n".getBytes(charset), new byte[]{'\n'})
                || !Arrays.equals("\r".getBytes(charset), new byte[]{'\r'})) {
            throw new IllegalArgumentException("Charset must encode CR and LF as single ASCII bytes: " + charset);
        }
    }

    public String readLine() throws IOException {
        ByteArrayOutputStream line = new ByteArrayOutputStream();
        int value;
        while ((value = file.read()) != -1) {
            if (value == '\n') {
                return decodeLine(line);
            }
            if (value == '\r') {
                long afterCarriageReturn = file.getFilePointer();
                int next = file.read();
                if (next != '\n' && next != -1) {
                    file.seek(afterCarriageReturn);
                }
                return decodeLine(line);
            }
            line.write(value);
        }
        if (line.size() == 0) {
            return null;
        }
        return new String(line.toByteArray(), charset);
    }

    private String decodeLine(ByteArrayOutputStream line) {
        return new String(line.toByteArray(), charset);
    }
}

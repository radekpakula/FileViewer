package org.vaadin.addons.viewer.application.file;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.Arrays;

public final class EncodedLineReader {
    private final RandomAccessFile file;
    private final Charset charset;
    private final byte[] newline;
    private final byte[] carriageReturn;

    public EncodedLineReader(RandomAccessFile file, Charset charset) {
        this.file = file;
        this.charset = charset;
        this.newline = "\n".getBytes(charset);
        this.carriageReturn = "\r".getBytes(charset);
    }

    public String readLine() throws IOException {
        ByteArrayOutputStream line = new ByteArrayOutputStream();
        int matched = 0;
        boolean readAny = false;
        int value;
        while ((value = file.read()) != -1) {
            readAny = true;
            byte b = (byte) value;
            if (b == newline[matched]) {
                matched++;
                if (matched == newline.length) {
                    byte[] bytes = line.toByteArray();
                    if (endsWith(bytes, carriageReturn)) {
                        bytes = Arrays.copyOf(bytes, bytes.length - carriageReturn.length);
                    }
                    return new String(bytes, charset);
                }
            } else {
                if (matched > 0) {
                    line.write(newline, 0, matched);
                    matched = 0;
                    if (b == newline[0]) {
                        matched = 1;
                        continue;
                    }
                }
                line.write(b);
            }
        }
        if (!readAny && line.size() == 0 && matched == 0) {
            return null;
        }
        if (matched > 0) {
            line.write(newline, 0, matched);
        }
        return new String(line.toByteArray(), charset);
    }

    private static boolean endsWith(byte[] value, byte[] suffix) {
        if (value.length < suffix.length) {
            return false;
        }
        for (int i = 0; i < suffix.length; i++) {
            if (value[value.length - suffix.length + i] != suffix[i]) {
                return false;
            }
        }
        return true;
    }
}

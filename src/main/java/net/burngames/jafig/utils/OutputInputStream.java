package net.burngames.jafig.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author PaulBGD
 */
public class OutputInputStream {

    private final OutputStream outputStream;
    private final InputStream inputStream;

    public OutputInputStream(OutputStream outputStream, InputStream inputStream) {
        this.outputStream = outputStream;
        this.inputStream = inputStream;
    }

    public byte[] read() throws IOException {
        if (inputStream == null) {
            return new byte[0];
        }
        byte[] bytes = new byte[inputStream.available()];
        inputStream.read(bytes);
        return bytes;
    }

    public void write(byte[] bytes) throws IOException {
        if (outputStream != null) {
            outputStream.write(bytes);
        }
    }

}

package com.wiilink24.emu;

import java.io.*;


public class FileReader {

    private final long fileSize;

    private final BufferedInputStream stream;

    public FileReader(String filePath) throws FileNotFoundException {
        File file = new File(filePath);
        stream = new BufferedInputStream(new FileInputStream(file));

        // If we get here the file exists, so we are safe to call file methods.
        fileSize = file.length();
    }

    public byte[] readBytes(int N) throws IOException {
        return stream.readNBytes(N);
    }

    public int getFileSize() {
        return (int)fileSize;
    }
}

package me.wy.gooloader.gooloader.util;

import org.apache.commons.io.input.BOMInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

public class ZippedFileInputStream extends BOMInputStream {

    private ZipInputStream is;

    public ZippedFileInputStream(ZipInputStream is){
        super(is);
        this.is = is;
    }

    @Override
    public int read() throws IOException {
        return is.read();
    }

    @Override
    public void close() throws IOException {
        is.closeEntry();
    }
}

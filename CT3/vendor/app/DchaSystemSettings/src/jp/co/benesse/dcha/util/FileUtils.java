package jp.co.benesse.dcha.util;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
/* loaded from: s.zip:jp/co/benesse/dcha/util/FileUtils.class */
public class FileUtils {
    private FileUtils() {
    }

    public static final boolean canReadFile(File file) {
        return file != null && file.exists() && file.isFile() && file.canRead();
    }

    public static final void close(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException e) {
        }
    }
}

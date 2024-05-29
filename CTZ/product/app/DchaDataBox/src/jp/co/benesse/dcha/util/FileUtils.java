package jp.co.benesse.dcha.util;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
/* loaded from: classes.dex */
public class FileUtils {
    private FileUtils() {
    }

    public static final void close(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException unused) {
        }
    }

    public static boolean fileDelete(File file) {
        File[] listFiles;
        if (file.exists()) {
            if (file.isFile()) {
                return file.delete();
            }
            if (!file.isDirectory() || (listFiles = file.listFiles()) == null) {
                return false;
            }
            for (File file2 : listFiles) {
                if (!fileDelete(file2)) {
                    return false;
                }
            }
            return file.delete();
        }
        return false;
    }
}

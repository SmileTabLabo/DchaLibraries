package android.support.v4.graphics;

import android.content.Context;
import android.content.res.Resources;
import android.os.Process;
import android.util.Log;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
/* loaded from: classes.dex */
public class TypefaceCompatUtil {
    public static File getTempFile(Context context) {
        String prefix = ".font" + Process.myPid() + "-" + Process.myTid() + "-";
        for (int i = 0; i < 100; i++) {
            File file = new File(context.getCacheDir(), prefix + i);
            if (file.createNewFile()) {
                return file;
            }
        }
        return null;
    }

    private static ByteBuffer mmap(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            FileChannel channel = fis.getChannel();
            long size = channel.size();
            MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_ONLY, 0L, size);
            fis.close();
            return map;
        } catch (IOException e) {
            return null;
        }
    }

    /*  JADX ERROR: JadxRuntimeException in pass: BlockProcessor
        jadx.core.utils.exceptions.JadxRuntimeException: Found unreachable blocks
        	at jadx.core.dex.visitors.blocks.DominatorTree.sortBlocks(DominatorTree.java:35)
        	at jadx.core.dex.visitors.blocks.DominatorTree.compute(DominatorTree.java:25)
        	at jadx.core.dex.visitors.blocks.BlockProcessor.computeDominators(BlockProcessor.java:202)
        	at jadx.core.dex.visitors.blocks.BlockProcessor.processBlocksTree(BlockProcessor.java:45)
        	at jadx.core.dex.visitors.blocks.BlockProcessor.visit(BlockProcessor.java:39)
        */
    public static java.nio.ByteBuffer mmap(android.content.Context r12, android.os.CancellationSignal r13, android.net.Uri r14) {
        /*
            android.content.ContentResolver r0 = r12.getContentResolver()
            r1 = 0
            java.lang.String r2 = "r"
            android.os.ParcelFileDescriptor r2 = r0.openFileDescriptor(r14, r2, r13)     // Catch: java.io.IOException -> L69
            if (r2 != 0) goto L14
        Le:
            if (r2 == 0) goto L13
            r2.close()     // Catch: java.io.IOException -> L69
        L13:
            return r1
        L14:
            java.io.FileInputStream r3 = new java.io.FileInputStream     // Catch: java.lang.Throwable -> L52
            java.io.FileDescriptor r4 = r2.getFileDescriptor()     // Catch: java.lang.Throwable -> L52
            r3.<init>(r4)     // Catch: java.lang.Throwable -> L52
            java.nio.channels.FileChannel r4 = r3.getChannel()     // Catch: java.lang.Throwable -> L3a
            long r9 = r4.size()     // Catch: java.lang.Throwable -> L3a
            java.nio.channels.FileChannel$MapMode r6 = java.nio.channels.FileChannel.MapMode.READ_ONLY     // Catch: java.lang.Throwable -> L3a
            r7 = 0
            r5 = r4
            java.nio.MappedByteBuffer r5 = r5.map(r6, r7, r9)     // Catch: java.lang.Throwable -> L3a
            r3.close()     // Catch: java.lang.Throwable -> L52
            if (r2 == 0) goto L36
            r2.close()     // Catch: java.io.IOException -> L69
        L36:
            return r5
        L37:
            r4 = move-exception
            r5 = r1
            goto L40
        L3a:
            r4 = move-exception
            throw r4     // Catch: java.lang.Throwable -> L3c
        L3c:
            r5 = move-exception
            r11 = r5
            r5 = r4
            r4 = r11
        L40:
            if (r5 == 0) goto L4b
            r3.close()     // Catch: java.lang.Throwable -> L46
            goto L4e
        L46:
            r6 = move-exception
            r5.addSuppressed(r6)     // Catch: java.lang.Throwable -> L52
            goto L4e
        L4b:
            r3.close()     // Catch: java.lang.Throwable -> L52
        L4e:
            throw r4     // Catch: java.lang.Throwable -> L52
        L4f:
            r3 = move-exception
            r4 = r1
            goto L58
        L52:
            r3 = move-exception
            throw r3     // Catch: java.lang.Throwable -> L54
        L54:
            r4 = move-exception
            r11 = r4
            r4 = r3
            r3 = r11
        L58:
            if (r2 == 0) goto L68
            if (r4 == 0) goto L65
            r2.close()     // Catch: java.lang.Throwable -> L60
            goto L68
        L60:
            r5 = move-exception
            r4.addSuppressed(r5)     // Catch: java.io.IOException -> L69
            goto L68
        L65:
            r2.close()     // Catch: java.io.IOException -> L69
        L68:
            throw r3     // Catch: java.io.IOException -> L69
        L69:
            r2 = move-exception
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v4.graphics.TypefaceCompatUtil.mmap(android.content.Context, android.os.CancellationSignal, android.net.Uri):java.nio.ByteBuffer");
    }

    public static ByteBuffer copyToDirectBuffer(Context context, Resources res, int id) {
        File tmpFile = getTempFile(context);
        ByteBuffer byteBuffer = null;
        if (tmpFile == null) {
            return null;
        }
        try {
            if (copyToFile(tmpFile, res, id)) {
                byteBuffer = mmap(tmpFile);
            }
            return byteBuffer;
        } finally {
            tmpFile.delete();
        }
    }

    public static boolean copyToFile(File file, InputStream is) {
        FileOutputStream os = null;
        boolean z = false;
        try {
            try {
                os = new FileOutputStream(file, false);
                byte[] buffer = new byte[1024];
                while (true) {
                    int readLen = is.read(buffer);
                    if (readLen == -1) {
                        break;
                    }
                    os.write(buffer, 0, readLen);
                }
                z = true;
            } catch (IOException e) {
                Log.e("TypefaceCompatUtil", "Error copying resource contents to temp file: " + e.getMessage());
            }
            return z;
        } finally {
            closeQuietly(os);
        }
    }

    public static boolean copyToFile(File file, Resources res, int id) {
        InputStream is = null;
        try {
            is = res.openRawResource(id);
            return copyToFile(file, is);
        } finally {
            closeQuietly(is);
        }
    }

    public static void closeQuietly(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
            }
        }
    }
}

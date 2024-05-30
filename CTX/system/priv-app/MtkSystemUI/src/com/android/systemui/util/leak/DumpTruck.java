package com.android.systemui.util.leak;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Debug;
import android.os.Process;
import android.support.v4.content.FileProvider;
import android.util.Log;
import com.android.systemui.Dependency;
import com.android.systemui.util.leak.GarbageMonitor;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
/* loaded from: classes.dex */
public class DumpTruck {
    final StringBuilder body = new StringBuilder();
    private final Context context;
    private Uri hprofUri;

    public DumpTruck(Context context) {
        this.context = context;
    }

    public DumpTruck captureHeaps(int[] iArr) {
        int[] copyOf;
        GarbageMonitor.ProcessMemInfo memInfo;
        GarbageMonitor garbageMonitor = (GarbageMonitor) Dependency.get(GarbageMonitor.class);
        File file = new File(this.context.getCacheDir(), "leak");
        file.mkdirs();
        this.hprofUri = null;
        this.body.setLength(0);
        StringBuilder sb = this.body;
        sb.append("Build: ");
        sb.append(Build.DISPLAY);
        sb.append("\n\nProcesses:\n");
        ArrayList arrayList = new ArrayList();
        int myPid = Process.myPid();
        for (int i : Arrays.copyOf(iArr, iArr.length)) {
            StringBuilder sb2 = this.body;
            sb2.append("  pid ");
            sb2.append(i);
            if (garbageMonitor != null && (memInfo = garbageMonitor.getMemInfo(i)) != null) {
                StringBuilder sb3 = this.body;
                sb3.append(":");
                sb3.append(" up=");
                sb3.append(memInfo.getUptime());
                sb3.append(" pss=");
                sb3.append(memInfo.currentPss);
                sb3.append(" uss=");
                sb3.append(memInfo.currentUss);
            }
            if (i == myPid) {
                String path = new File(file, String.format("heap-%d.ahprof", Integer.valueOf(i))).getPath();
                Log.v("DumpTruck", "Dumping memory info for process " + i + " to " + path);
                try {
                    Debug.dumpHprofData(path);
                    arrayList.add(path);
                    this.body.append(" (hprof attached)");
                } catch (IOException e) {
                    Log.e("DumpTruck", "error dumping memory:", e);
                    StringBuilder sb4 = this.body;
                    sb4.append("\n** Could not dump heap: \n");
                    sb4.append(e.toString());
                    sb4.append("\n");
                }
            }
            this.body.append("\n");
        }
        try {
            String canonicalPath = new File(file, String.format("hprof-%d.zip", Long.valueOf(System.currentTimeMillis()))).getCanonicalPath();
            if (zipUp(canonicalPath, arrayList)) {
                this.hprofUri = FileProvider.getUriForFile(this.context, "com.android.systemui.fileprovider", new File(canonicalPath));
            }
        } catch (IOException e2) {
            Log.e("DumpTruck", "unable to zip up heapdumps", e2);
            StringBuilder sb5 = this.body;
            sb5.append("\n** Could not zip up files: \n");
            sb5.append(e2.toString());
            sb5.append("\n");
        }
        return this;
    }

    public Intent createShareIntent() {
        Intent intent = new Intent("android.intent.action.SEND");
        intent.addFlags(268435456);
        intent.addFlags(1);
        intent.putExtra("android.intent.extra.SUBJECT", "SystemUI memory dump");
        intent.putExtra("android.intent.extra.TEXT", this.body.toString());
        if (this.hprofUri != null) {
            intent.setType("application/zip");
            intent.putExtra("android.intent.extra.STREAM", this.hprofUri);
        }
        return intent;
    }

    private static boolean zipUp(String str, ArrayList<String> arrayList) {
        try {
            ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(str));
            byte[] bArr = new byte[524288];
            Iterator<String> it = arrayList.iterator();
            while (it.hasNext()) {
                String next = it.next();
                BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(next));
                zipOutputStream.putNextEntry(new ZipEntry(next));
                while (true) {
                    int read = bufferedInputStream.read(bArr, 0, 524288);
                    if (read <= 0) {
                        break;
                    }
                    zipOutputStream.write(bArr, 0, read);
                }
                zipOutputStream.closeEntry();
                $closeResource(null, bufferedInputStream);
            }
            $closeResource(null, zipOutputStream);
            return true;
        } catch (IOException e) {
            Log.e("DumpTruck", "error zipping up profile data", e);
            return false;
        }
    }

    private static /* synthetic */ void $closeResource(Throwable th, AutoCloseable autoCloseable) {
        if (th == null) {
            autoCloseable.close();
            return;
        }
        try {
            autoCloseable.close();
        } catch (Throwable th2) {
            th.addSuppressed(th2);
        }
    }
}

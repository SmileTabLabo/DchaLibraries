package com.android.browser;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
/* loaded from: classes.dex */
public class CrashRecoveryHandler {
    private static CrashRecoveryHandler sInstance;
    private Context mContext;
    private Controller mController;
    private boolean mIsPreloading = false;
    private boolean mDidPreload = false;
    private Bundle mRecoveryState = null;
    private Runnable mCreateState = new Runnable() { // from class: com.android.browser.CrashRecoveryHandler.2
        @Override // java.lang.Runnable
        public void run() {
            try {
                Message.obtain(CrashRecoveryHandler.this.mBackgroundHandler, 1, CrashRecoveryHandler.this.mController.createSaveState()).sendToTarget();
                CrashRecoveryHandler.this.mForegroundHandler.removeCallbacks(CrashRecoveryHandler.this.mCreateState);
            } catch (Throwable th) {
                Log.w("BrowserCrashRecovery", "Failed to save state", th);
            }
        }
    };
    private Handler mForegroundHandler = new Handler();
    private Handler mBackgroundHandler = new Handler(BackgroundHandler.getLooper()) { // from class: com.android.browser.CrashRecoveryHandler.1
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    CrashRecoveryHandler.this.writeState((Bundle) message.obj);
                    return;
                case 2:
                    File file = new File(CrashRecoveryHandler.this.mContext.getCacheDir(), "browser_state.parcel");
                    if (file.exists()) {
                        file.delete();
                        return;
                    }
                    return;
                case 3:
                    CrashRecoveryHandler.this.mRecoveryState = CrashRecoveryHandler.this.loadCrashState();
                    synchronized (CrashRecoveryHandler.this) {
                        CrashRecoveryHandler.this.mIsPreloading = false;
                        CrashRecoveryHandler.this.mDidPreload = true;
                        CrashRecoveryHandler.this.notifyAll();
                    }
                    return;
                default:
                    return;
            }
        }
    };

    public static CrashRecoveryHandler initialize(Controller controller) {
        if (sInstance == null) {
            sInstance = new CrashRecoveryHandler(controller);
        } else {
            sInstance.mController = controller;
        }
        return sInstance;
    }

    private CrashRecoveryHandler(Controller controller) {
        this.mController = controller;
        this.mContext = this.mController.getActivity().getApplicationContext();
    }

    public void backupState() {
        this.mForegroundHandler.postDelayed(this.mCreateState, 500L);
    }

    public void clearState() {
        this.mBackgroundHandler.sendEmptyMessage(2);
        updateLastRecovered(0L);
    }

    private boolean shouldRestore() {
        BrowserSettings browserSettings = BrowserSettings.getInstance();
        return System.currentTimeMillis() - browserSettings.getLastRecovered() > 300000 || browserSettings.wasLastRunPaused();
    }

    private void updateLastRecovered(long j) {
        BrowserSettings.getInstance().setLastRecovered(j);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Code restructure failed: missing block: B:50:0x008e, code lost:
        if (r4 == null) goto L34;
     */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v1, types: [boolean] */
    /* JADX WARN: Type inference failed for: r0v5, types: [android.os.Parcel] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public synchronized Bundle loadCrashState() {
        FileInputStream fileInputStream;
        Parcel shouldRestore = shouldRestore();
        FileInputStream fileInputStream2 = null;
        if (shouldRestore == 0) {
            return null;
        }
        try {
            BrowserSettings.getInstance().setLastRunPaused(false);
            shouldRestore = Parcel.obtain();
        } catch (Throwable th) {
            th = th;
        }
        try {
            fileInputStream = new FileInputStream(new File(this.mContext.getCacheDir(), "browser_state.parcel"));
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] bArr = new byte[4096];
                while (true) {
                    int read = fileInputStream.read(bArr);
                    if (read <= 0) {
                        break;
                    }
                    byteArrayOutputStream.write(bArr, 0, read);
                }
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                shouldRestore.unmarshall(byteArray, 0, byteArray.length);
                shouldRestore.setDataPosition(0);
                Bundle readBundle = shouldRestore.readBundle();
                if (readBundle != null) {
                    if (!readBundle.isEmpty()) {
                        shouldRestore.recycle();
                        try {
                            fileInputStream.close();
                        } catch (IOException e) {
                        }
                        return readBundle;
                    }
                }
                shouldRestore.recycle();
            } catch (FileNotFoundException e2) {
                shouldRestore.recycle();
            } catch (Throwable th2) {
                th = th2;
                Log.w("BrowserCrashRecovery", "Failed to recover state!", th);
                shouldRestore.recycle();
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                return null;
            }
        } catch (FileNotFoundException e3) {
            fileInputStream = null;
        } catch (Throwable th3) {
            th = th3;
            fileInputStream = null;
        }
        try {
            fileInputStream.close();
        } catch (IOException e4) {
        }
        return null;
    }

    public void startRecovery(Intent intent) {
        synchronized (this) {
            while (this.mIsPreloading) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
        }
        if (!this.mDidPreload) {
            this.mRecoveryState = loadCrashState();
        }
        updateLastRecovered(this.mRecoveryState != null ? System.currentTimeMillis() : 0L);
        this.mController.doStart(this.mRecoveryState, intent);
        this.mRecoveryState = null;
    }

    public void preloadCrashState() {
        synchronized (this) {
            if (this.mIsPreloading) {
                return;
            }
            this.mIsPreloading = true;
            this.mBackgroundHandler.sendEmptyMessage(3);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public synchronized void writeState(Bundle bundle) {
        Parcel obtain = Parcel.obtain();
        bundle.writeToParcel(obtain, 0);
        File file = new File(this.mContext.getCacheDir(), "browser_state.parcel.journal");
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(obtain.marshall());
        fileOutputStream.close();
        File file2 = new File(this.mContext.getCacheDir(), "browser_state.parcel");
        if (!file.renameTo(file2)) {
            file2.delete();
            file.renameTo(file2);
        }
        obtain.recycle();
    }
}

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
/* loaded from: b.zip:com/android/browser/CrashRecoveryHandler.class */
public class CrashRecoveryHandler {
    private static CrashRecoveryHandler sInstance;
    private Context mContext;
    private Controller mController;
    private boolean mIsPreloading = false;
    private boolean mDidPreload = false;
    private Bundle mRecoveryState = null;
    private Runnable mCreateState = new Runnable(this) { // from class: com.android.browser.CrashRecoveryHandler.1
        final CrashRecoveryHandler this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            try {
                Message.obtain(this.this$0.mBackgroundHandler, 1, this.this$0.mController.createSaveState()).sendToTarget();
                this.this$0.mForegroundHandler.removeCallbacks(this.this$0.mCreateState);
            } catch (Throwable th) {
                Log.w("BrowserCrashRecovery", "Failed to save state", th);
            }
        }
    };
    private Handler mForegroundHandler = new Handler();
    private Handler mBackgroundHandler = new Handler(this, BackgroundHandler.getLooper()) { // from class: com.android.browser.CrashRecoveryHandler.2
        final CrashRecoveryHandler this$0;

        {
            this.this$0 = this;
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    this.this$0.writeState((Bundle) message.obj);
                    return;
                case 2:
                    File file = new File(this.this$0.mContext.getCacheDir(), "browser_state.parcel");
                    if (file.exists()) {
                        file.delete();
                        return;
                    }
                    return;
                case 3:
                    this.this$0.mRecoveryState = this.this$0.loadCrashState();
                    synchronized (this.this$0) {
                        this.this$0.mIsPreloading = false;
                        this.this$0.mDidPreload = true;
                        this.this$0.notifyAll();
                    }
                    return;
                default:
                    return;
            }
        }
    };

    private CrashRecoveryHandler(Controller controller) {
        this.mController = controller;
        this.mContext = this.mController.getActivity().getApplicationContext();
    }

    public static CrashRecoveryHandler initialize(Controller controller) {
        if (sInstance == null) {
            sInstance = new CrashRecoveryHandler(controller);
        } else {
            sInstance.mController = controller;
        }
        return sInstance;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Bundle loadCrashState() {
        FileInputStream fileInputStream;
        FileInputStream fileInputStream2;
        synchronized (this) {
            if (shouldRestore()) {
                BrowserSettings.getInstance().setLastRunPaused(false);
                Parcel obtain = Parcel.obtain();
                FileInputStream fileInputStream3 = null;
                try {
                    try {
                        fileInputStream = new FileInputStream(new File(this.mContext.getCacheDir(), "browser_state.parcel"));
                    } catch (Throwable th) {
                        th = th;
                        fileInputStream2 = fileInputStream3;
                    }
                } catch (FileNotFoundException e) {
                    fileInputStream = null;
                } catch (Throwable th2) {
                    th = th2;
                    fileInputStream = null;
                }
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
                    obtain.unmarshall(byteArray, 0, byteArray.length);
                    obtain.setDataPosition(0);
                    fileInputStream3 = obtain.readBundle();
                    if (fileInputStream3 != null) {
                        if (!fileInputStream3.isEmpty()) {
                            obtain.recycle();
                            if (fileInputStream != null) {
                                try {
                                    fileInputStream.close();
                                } catch (IOException e2) {
                                }
                            }
                            return fileInputStream3;
                        }
                    }
                    obtain.recycle();
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e3) {
                        }
                    }
                } catch (FileNotFoundException e4) {
                    obtain.recycle();
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e5) {
                        }
                    }
                    return null;
                } catch (Throwable th3) {
                    fileInputStream2 = fileInputStream;
                    th = th3;
                    obtain.recycle();
                    if (fileInputStream2 != null) {
                        try {
                            fileInputStream2.close();
                        } catch (IOException e6) {
                        }
                    }
                    throw th;
                }
                return null;
            }
            return null;
        }
    }

    private boolean shouldRestore() {
        BrowserSettings browserSettings = BrowserSettings.getInstance();
        return System.currentTimeMillis() - browserSettings.getLastRecovered() <= 300000 ? browserSettings.wasLastRunPaused() : true;
    }

    private void updateLastRecovered(long j) {
        BrowserSettings.getInstance().setLastRecovered(j);
    }

    public void backupState() {
        this.mForegroundHandler.postDelayed(this.mCreateState, 500L);
    }

    public void clearState() {
        this.mBackgroundHandler.sendEmptyMessage(2);
        updateLastRecovered(0L);
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

    /* JADX INFO: Access modifiers changed from: package-private */
    public void writeState(Bundle bundle) {
        synchronized (this) {
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
}

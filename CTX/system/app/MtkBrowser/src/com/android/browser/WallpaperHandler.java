package com.android.browser;

import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MenuItem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Thread;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
/* loaded from: classes.dex */
public class WallpaperHandler extends Thread implements DialogInterface.OnCancelListener, MenuItem.OnMenuItemClickListener {
    private boolean mCanceled = false;
    private Context mContext;
    private String mUrl;
    private ProgressDialog mWallpaperProgress;

    public WallpaperHandler(Context context, String str) {
        this.mContext = context;
        this.mUrl = str;
    }

    @Override // android.content.DialogInterface.OnCancelListener
    public void onCancel(DialogInterface dialogInterface) {
        this.mCanceled = true;
    }

    @Override // android.view.MenuItem.OnMenuItemClickListener
    public boolean onMenuItemClick(MenuItem menuItem) {
        if (this.mUrl != null && getState() == Thread.State.NEW) {
            this.mWallpaperProgress = new ProgressDialog(this.mContext);
            this.mWallpaperProgress.setIndeterminate(true);
            this.mWallpaperProgress.setMessage(this.mContext.getResources().getText(R.string.progress_dialog_setting_wallpaper));
            this.mWallpaperProgress.setCancelable(true);
            this.mWallpaperProgress.setOnCancelListener(this);
            this.mWallpaperProgress.show();
            start();
        }
        return true;
    }

    /* JADX WARN: Removed duplicated region for block: B:36:0x0096  */
    @Override // java.lang.Thread, java.lang.Runnable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public void run() {
        InputStream inputStream;
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this.mContext);
        Drawable drawable = wallpaperManager.getDrawable();
        InputStream inputStream2 = null;
        try {
        } catch (Throwable th) {
            th = th;
        }
        try {
            try {
                inputStream = openStream();
                if (inputStream != null) {
                    try {
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        byte[] bArr = new byte[8192];
                        while (true) {
                            int read = inputStream.read(bArr);
                            if (read == -1) {
                                break;
                            }
                            byteArrayOutputStream.write(bArr, 0, read);
                        }
                        byte[] byteArray = byteArrayOutputStream.toByteArray();
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, options);
                        int desiredMinimumWidth = (int) (wallpaperManager.getDesiredMinimumWidth() * 1.25d);
                        int desiredMinimumHeight = (int) (wallpaperManager.getDesiredMinimumHeight() * 1.25d);
                        int i = options.outWidth;
                        int i2 = options.outHeight;
                        int i3 = 1;
                        while (true) {
                            if (i <= desiredMinimumWidth && i2 <= desiredMinimumHeight) {
                                break;
                            }
                            i3 <<= 1;
                            i >>= 1;
                            i2 >>= 1;
                        }
                        options.inJustDecodeBounds = false;
                        options.inSampleSize = i3;
                        Bitmap decodeByteArray = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, options);
                        if (decodeByteArray != null) {
                            wallpaperManager.setBitmap(decodeByteArray);
                        } else {
                            Log.e("WallpaperHandler", "Unable to set new wallpaper, decodeStream returned null.");
                        }
                    } catch (IOException e) {
                        e = e;
                        e.printStackTrace();
                        Log.e("WallpaperHandler", "Unable to set new wallpaper");
                        this.mCanceled = true;
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        if (this.mCanceled) {
                        }
                        destroyDialog();
                    }
                }
            } catch (IOException e2) {
            }
        } catch (IOException e3) {
            e = e3;
            inputStream = null;
        } catch (Throwable th2) {
            th = th2;
            if (0 != 0) {
                try {
                    inputStream2.close();
                } catch (IOException e4) {
                }
            }
            throw th;
        }
        if (inputStream != null) {
            inputStream.close();
        }
        if (this.mCanceled) {
            int intrinsicWidth = drawable.getIntrinsicWidth();
            int intrinsicHeight = drawable.getIntrinsicHeight();
            Bitmap createBitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(createBitmap);
            drawable.setBounds(0, 0, intrinsicWidth, intrinsicHeight);
            drawable.draw(canvas);
            canvas.setBitmap(null);
            try {
                wallpaperManager.setBitmap(createBitmap);
            } catch (IOException e5) {
                Log.e("WallpaperHandler", "Unable to restore old wallpaper.");
            }
            this.mCanceled = false;
        }
        destroyDialog();
    }

    private InputStream openStream() throws IOException, MalformedURLException {
        if (DataUri.isDataUri(this.mUrl)) {
            return new ByteArrayInputStream(new DataUri(this.mUrl).getData());
        }
        URLConnection openConnection = new URL(this.mUrl).openConnection();
        openConnection.setRequestProperty("Connection", "close");
        return openConnection.getInputStream();
    }

    public void destroyDialog() {
        if (this.mWallpaperProgress != null && this.mWallpaperProgress.isShowing()) {
            this.mWallpaperProgress.dismiss();
        }
    }
}

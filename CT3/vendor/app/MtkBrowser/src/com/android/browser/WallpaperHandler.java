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
/* loaded from: b.zip:com/android/browser/WallpaperHandler.class */
public class WallpaperHandler extends Thread implements MenuItem.OnMenuItemClickListener, DialogInterface.OnCancelListener {
    private boolean mCanceled = false;
    private Context mContext;
    private String mUrl;
    private ProgressDialog mWallpaperProgress;

    public WallpaperHandler(Context context, String str) {
        this.mContext = context;
        this.mUrl = str;
    }

    private InputStream openStream() throws IOException, MalformedURLException {
        ByteArrayInputStream inputStream;
        if (DataUri.isDataUri(this.mUrl)) {
            inputStream = new ByteArrayInputStream(new DataUri(this.mUrl).getData());
        } else {
            URLConnection openConnection = new URL(this.mUrl).openConnection();
            openConnection.setRequestProperty("Connection", "close");
            inputStream = openConnection.getInputStream();
        }
        return inputStream;
    }

    public void destroyDialog() {
        if (this.mWallpaperProgress == null || !this.mWallpaperProgress.isShowing()) {
            return;
        }
        this.mWallpaperProgress.dismiss();
    }

    @Override // android.content.DialogInterface.OnCancelListener
    public void onCancel(DialogInterface dialogInterface) {
        this.mCanceled = true;
    }

    @Override // android.view.MenuItem.OnMenuItemClickListener
    public boolean onMenuItemClick(MenuItem menuItem) {
        if (this.mUrl == null || getState() != Thread.State.NEW) {
            return true;
        }
        this.mWallpaperProgress = new ProgressDialog(this.mContext);
        this.mWallpaperProgress.setIndeterminate(true);
        this.mWallpaperProgress.setMessage(this.mContext.getResources().getText(2131493258));
        this.mWallpaperProgress.setCancelable(true);
        this.mWallpaperProgress.setOnCancelListener(this);
        this.mWallpaperProgress.show();
        start();
        return true;
    }

    @Override // java.lang.Thread, java.lang.Runnable
    public void run() {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this.mContext);
        Drawable drawable = wallpaperManager.getDrawable();
        InputStream inputStream = null;
        InputStream inputStream2 = null;
        try {
            try {
                InputStream openStream = openStream();
                if (openStream != null) {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    byte[] bArr = new byte[8192];
                    while (true) {
                        int read = openStream.read(bArr);
                        if (read == -1) {
                            break;
                        }
                        byteArrayOutputStream.write(bArr, 0, read);
                    }
                    byte[] byteArray = byteArrayOutputStream.toByteArray();
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, options);
                    int desiredMinimumWidth = wallpaperManager.getDesiredMinimumWidth();
                    int desiredMinimumHeight = wallpaperManager.getDesiredMinimumHeight();
                    int i = (int) (desiredMinimumWidth * 1.25d);
                    int i2 = (int) (desiredMinimumHeight * 1.25d);
                    int i3 = options.outWidth;
                    int i4 = options.outHeight;
                    int i5 = 1;
                    while (true) {
                        if (i3 <= i && i4 <= i2) {
                            break;
                        }
                        i5 <<= 1;
                        i3 >>= 1;
                        i4 >>= 1;
                    }
                    options.inJustDecodeBounds = false;
                    options.inSampleSize = i5;
                    Bitmap decodeByteArray = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, options);
                    if (decodeByteArray != null) {
                        wallpaperManager.setBitmap(decodeByteArray);
                    } else {
                        Log.e("WallpaperHandler", "Unable to set new wallpaper, decodeStream returned null.");
                    }
                }
                if (openStream != null) {
                    try {
                        openStream.close();
                    } catch (IOException e) {
                    }
                }
            } catch (IOException e2) {
                e2.printStackTrace();
                Log.e("WallpaperHandler", "Unable to set new wallpaper");
                this.mCanceled = true;
                if (0 != 0) {
                    try {
                        inputStream2.close();
                    } catch (IOException e3) {
                    }
                }
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
                } catch (IOException e4) {
                    Log.e("WallpaperHandler", "Unable to restore old wallpaper.");
                }
                this.mCanceled = false;
            }
            destroyDialog();
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    inputStream.close();
                } catch (IOException e5) {
                }
            }
            throw th;
        }
    }
}

package com.android.launcher3;

import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.AsyncTask;
import java.io.IOException;
import java.io.InputStream;
/* loaded from: a.zip:com/android/launcher3/NycWallpaperUtils.class */
public class NycWallpaperUtils {
    public static void clear(Context context, int i) throws IOException {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
        if (Utilities.ATLEAST_N) {
            wallpaperManager.clear(i);
        } else {
            wallpaperManager.clear();
        }
    }

    public static void executeCropTaskAfterPrompt(Context context, AsyncTask<Integer, ?, ?> asyncTask, DialogInterface.OnCancelListener onCancelListener) {
        if (Utilities.ATLEAST_N) {
            new AlertDialog.Builder(context).setTitle(2131558545).setItems(2131623937, new DialogInterface.OnClickListener(asyncTask) { // from class: com.android.launcher3.NycWallpaperUtils.1
                final AsyncTask val$cropTask;

                {
                    this.val$cropTask = asyncTask;
                }

                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i) {
                    this.val$cropTask.execute(Integer.valueOf(i == 0 ? 1 : i == 1 ? 2 : 3));
                }
            }).setOnCancelListener(onCancelListener).show();
        } else {
            asyncTask.execute(1);
        }
    }

    public static void setStream(Context context, InputStream inputStream, Rect rect, boolean z, int i) throws IOException {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
        if (Utilities.ATLEAST_N) {
            wallpaperManager.setStream(inputStream, rect, z, i);
        } else {
            wallpaperManager.setStream(inputStream);
        }
    }
}

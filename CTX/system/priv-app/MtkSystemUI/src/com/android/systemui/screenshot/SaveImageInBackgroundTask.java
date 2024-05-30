package com.android.systemui.screenshot;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Picture;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BenesseExtension;
import android.os.Environment;
import android.os.Process;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.provider.MediaStore;
import android.util.Slog;
import com.android.systemui.R;
import com.android.systemui.SystemUI;
import com.android.systemui.screenshot.GlobalScreenshot;
import com.android.systemui.util.NotificationChannels;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
/* compiled from: GlobalScreenshot.java */
/* loaded from: classes.dex */
class SaveImageInBackgroundTask extends AsyncTask<Void, Void, Void> {
    private final String mImageFileName;
    private final String mImageFilePath;
    private final int mImageHeight;
    private final long mImageTime;
    private final int mImageWidth;
    private final Notification.Builder mNotificationBuilder;
    private final NotificationManager mNotificationManager;
    private final Notification.BigPictureStyle mNotificationStyle;
    private final SaveImageInBackgroundData mParams;
    private final Notification.Builder mPublicNotificationBuilder;
    private final File mScreenshotDir;

    /* JADX INFO: Access modifiers changed from: package-private */
    public SaveImageInBackgroundTask(Context context, SaveImageInBackgroundData saveImageInBackgroundData, NotificationManager notificationManager) {
        Resources resources = context.getResources();
        this.mParams = saveImageInBackgroundData;
        this.mImageTime = System.currentTimeMillis();
        this.mImageFileName = String.format("Screenshot_%s.png", new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date(this.mImageTime)));
        if (BenesseExtension.getDchaState() != 0) {
            String str = null;
            for (VolumeInfo volumeInfo : ((StorageManager) context.getSystemService("storage")).getVolumes()) {
                if (volumeInfo.getDisk() != null && volumeInfo.getDisk().sysPath.startsWith("/sys//devices/platform/soc/11240000.mmc")) {
                    str = volumeInfo.internalPath;
                }
            }
            if (str != null) {
                this.mScreenshotDir = new File(new File(str, Environment.DIRECTORY_PICTURES), "Screenshots");
            } else {
                this.mScreenshotDir = null;
            }
        } else {
            this.mScreenshotDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Screenshots");
        }
        this.mImageFilePath = new File(this.mScreenshotDir, this.mImageFileName).getAbsolutePath();
        this.mImageWidth = saveImageInBackgroundData.image.getWidth();
        this.mImageHeight = saveImageInBackgroundData.image.getHeight();
        int i = saveImageInBackgroundData.iconSize;
        int i2 = saveImageInBackgroundData.previewWidth;
        int i3 = saveImageInBackgroundData.previewheight;
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0.25f);
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        Matrix matrix = new Matrix();
        matrix.setTranslate((i2 - this.mImageWidth) / 2, (i3 - this.mImageHeight) / 2);
        Bitmap generateAdjustedHwBitmap = generateAdjustedHwBitmap(saveImageInBackgroundData.image, i2, i3, matrix, paint, 1090519039);
        float f = i;
        float min = f / Math.min(this.mImageWidth, this.mImageHeight);
        matrix.setScale(min, min);
        matrix.postTranslate((f - (this.mImageWidth * min)) / 2.0f, (f - (min * this.mImageHeight)) / 2.0f);
        Bitmap generateAdjustedHwBitmap2 = generateAdjustedHwBitmap(saveImageInBackgroundData.image, i, i, matrix, paint, 1090519039);
        this.mNotificationManager = notificationManager;
        long currentTimeMillis = System.currentTimeMillis();
        this.mNotificationStyle = new Notification.BigPictureStyle().bigPicture(generateAdjustedHwBitmap.createAshmemBitmap());
        this.mPublicNotificationBuilder = new Notification.Builder(context, NotificationChannels.SCREENSHOTS_HEADSUP).setContentTitle(resources.getString(R.string.screenshot_saving_title)).setSmallIcon(R.drawable.stat_notify_image).setCategory("progress").setWhen(currentTimeMillis).setShowWhen(true).setColor(resources.getColor(17170774));
        SystemUI.overrideNotificationAppName(context, this.mPublicNotificationBuilder, true);
        this.mNotificationBuilder = new Notification.Builder(context, NotificationChannels.SCREENSHOTS_HEADSUP).setContentTitle(resources.getString(R.string.screenshot_saving_title)).setSmallIcon(R.drawable.stat_notify_image).setWhen(currentTimeMillis).setShowWhen(true).setColor(resources.getColor(17170774)).setStyle(this.mNotificationStyle).setPublicVersion(this.mPublicNotificationBuilder.build());
        this.mNotificationBuilder.setFlag(32, true);
        SystemUI.overrideNotificationAppName(context, this.mNotificationBuilder, true);
        this.mNotificationManager.notify(1, this.mNotificationBuilder.build());
        this.mNotificationBuilder.setLargeIcon(generateAdjustedHwBitmap2.createAshmemBitmap());
        this.mNotificationStyle.bigLargeIcon((Bitmap) null);
    }

    private Bitmap generateAdjustedHwBitmap(Bitmap bitmap, int i, int i2, Matrix matrix, Paint paint, int i3) {
        Picture picture = new Picture();
        Canvas beginRecording = picture.beginRecording(i, i2);
        beginRecording.drawColor(i3);
        beginRecording.drawBitmap(bitmap, matrix, paint);
        picture.endRecording();
        return Bitmap.createBitmap(picture);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    public Void doInBackground(Void... voidArr) {
        if (isCancelled()) {
            return null;
        }
        Process.setThreadPriority(-2);
        Context context = this.mParams.context;
        Bitmap bitmap = this.mParams.image;
        Resources resources = context.getResources();
        try {
            this.mScreenshotDir.mkdirs();
            long j = this.mImageTime / 1000;
            FileOutputStream fileOutputStream = new FileOutputStream(this.mImageFilePath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            ContentValues contentValues = new ContentValues();
            ContentResolver contentResolver = context.getContentResolver();
            contentValues.put("_data", this.mImageFilePath.startsWith("/mnt/media_rw/") ? this.mImageFilePath.replaceFirst("/mnt/media_rw/", "/storage/") : this.mImageFilePath);
            contentValues.put("title", this.mImageFileName);
            contentValues.put("_display_name", this.mImageFileName);
            contentValues.put("datetaken", Long.valueOf(this.mImageTime));
            contentValues.put("date_added", Long.valueOf(j));
            contentValues.put("date_modified", Long.valueOf(j));
            contentValues.put("mime_type", "image/png");
            contentValues.put("width", Integer.valueOf(this.mImageWidth));
            contentValues.put("height", Integer.valueOf(this.mImageHeight));
            contentValues.put("_size", Long.valueOf(new File(this.mImageFilePath).length()));
            Uri insert = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            String format = String.format("Screenshot (%s)", DateFormat.getDateTimeInstance().format(new Date(this.mImageTime)));
            Intent intent = new Intent("android.intent.action.SEND");
            intent.setType("image/png");
            intent.putExtra("android.intent.extra.STREAM", insert);
            intent.putExtra("android.intent.extra.SUBJECT", format);
            intent.addFlags(1);
            this.mNotificationBuilder.addAction(new Notification.Action.Builder((int) R.drawable.ic_screenshot_share, resources.getString(17040851), PendingIntent.getBroadcast(context, 0, new Intent(context, GlobalScreenshot.ScreenshotActionReceiver.class).putExtra("android:screenshot_sharing_intent", intent), 268435456)).build());
            Intent intent2 = new Intent("android.intent.action.EDIT");
            intent2.setType("image/png");
            intent2.setData(insert);
            intent2.addFlags(1);
            intent2.addFlags(2);
            this.mNotificationBuilder.addAction(new Notification.Action.Builder((int) R.drawable.ic_screenshot_edit, resources.getString(17040813), PendingIntent.getBroadcast(context, 1, new Intent(context, GlobalScreenshot.ScreenshotActionReceiver.class).putExtra("android:screenshot_sharing_intent", intent2), 268435456)).build());
            this.mNotificationBuilder.addAction(new Notification.Action.Builder((int) R.drawable.ic_screenshot_delete, resources.getString(17039785), PendingIntent.getBroadcast(context, 0, new Intent(context, GlobalScreenshot.DeleteScreenshotReceiver.class).putExtra("android:screenshot_uri_id", insert.toString()), 1342177280)).build());
            this.mParams.imageUri = insert;
            this.mParams.image = null;
            this.mParams.errorMsgResId = 0;
        } catch (Exception e) {
            Slog.e("SaveImageInBackgroundTask", "unable to save screenshot", e);
            this.mParams.clearImage();
            this.mParams.errorMsgResId = R.string.screenshot_failed_to_save_text;
        }
        if (bitmap != null) {
            bitmap.recycle();
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    public void onPostExecute(Void r13) {
        if (this.mParams.errorMsgResId != 0) {
            GlobalScreenshot.notifyScreenshotError(this.mParams.context, this.mNotificationManager, this.mParams.errorMsgResId);
        } else {
            Context context = this.mParams.context;
            Resources resources = context.getResources();
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.setDataAndType(this.mParams.imageUri, "image/png");
            intent.setFlags(268435457);
            long currentTimeMillis = System.currentTimeMillis();
            this.mPublicNotificationBuilder.setContentTitle(resources.getString(R.string.screenshot_saved_title)).setContentText(resources.getString(R.string.screenshot_saved_text)).setContentIntent(PendingIntent.getActivity(this.mParams.context, 0, intent, 67108864)).setWhen(currentTimeMillis).setAutoCancel(true).setColor(context.getColor(17170774));
            this.mNotificationBuilder.setContentTitle(resources.getString(R.string.screenshot_saved_title)).setContentText(resources.getString(R.string.screenshot_saved_text)).setContentIntent(PendingIntent.getActivity(this.mParams.context, 0, intent, 67108864)).setWhen(currentTimeMillis).setAutoCancel(true).setColor(context.getColor(17170774)).setPublicVersion(this.mPublicNotificationBuilder.build()).setFlag(32, false);
            this.mNotificationManager.notify(1, this.mNotificationBuilder.build());
        }
        this.mParams.finisher.run();
        this.mParams.clearContext();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    public void onCancelled(Void r2) {
        this.mParams.finisher.run();
        this.mParams.clearImage();
        this.mParams.clearContext();
        this.mNotificationManager.cancel(1);
    }
}

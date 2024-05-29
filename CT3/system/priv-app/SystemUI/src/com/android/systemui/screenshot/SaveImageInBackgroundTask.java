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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Process;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import com.android.systemui.SystemUI;
import com.android.systemui.screenshot.GlobalScreenshot;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
/* loaded from: a.zip:com/android/systemui/screenshot/SaveImageInBackgroundTask.class */
class SaveImageInBackgroundTask extends AsyncTask<Void, Void, Void> {
    private static boolean mTickerAddSpace;
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
        if (isDchaStateOn(context)) {
            this.mScreenshotDir = new File("/storage/sdcard1/Pictures", "Screenshots");
        } else {
            this.mScreenshotDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Screenshots");
        }
        this.mImageFilePath = new File(this.mScreenshotDir, this.mImageFileName).getAbsolutePath();
        this.mImageWidth = saveImageInBackgroundData.image.getWidth();
        this.mImageHeight = saveImageInBackgroundData.image.getHeight();
        int i = saveImageInBackgroundData.iconSize;
        int i2 = saveImageInBackgroundData.previewWidth;
        int i3 = saveImageInBackgroundData.previewheight;
        Canvas canvas = new Canvas();
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0.25f);
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        Matrix matrix = new Matrix();
        Bitmap createBitmap = Bitmap.createBitmap(i2, i3, saveImageInBackgroundData.image.getConfig());
        matrix.setTranslate((i2 - this.mImageWidth) / 2, (i3 - this.mImageHeight) / 2);
        canvas.setBitmap(createBitmap);
        canvas.drawBitmap(saveImageInBackgroundData.image, matrix, paint);
        canvas.drawColor(1090519039);
        canvas.setBitmap(null);
        float min = i / Math.min(this.mImageWidth, this.mImageHeight);
        Bitmap createBitmap2 = Bitmap.createBitmap(i, i, saveImageInBackgroundData.image.getConfig());
        matrix.setScale(min, min);
        matrix.postTranslate((i - (this.mImageWidth * min)) / 2.0f, (i - (this.mImageHeight * min)) / 2.0f);
        canvas.setBitmap(createBitmap2);
        canvas.drawBitmap(saveImageInBackgroundData.image, matrix, paint);
        canvas.drawColor(1090519039);
        canvas.setBitmap(null);
        mTickerAddSpace = !mTickerAddSpace;
        this.mNotificationManager = notificationManager;
        long currentTimeMillis = System.currentTimeMillis();
        this.mNotificationStyle = new Notification.BigPictureStyle().bigPicture(createBitmap.createAshmemBitmap());
        this.mPublicNotificationBuilder = new Notification.Builder(context).setContentTitle(resources.getString(2131493335)).setContentText(resources.getString(2131493336)).setSmallIcon(2130838249).setCategory("progress").setWhen(currentTimeMillis).setShowWhen(true).setColor(resources.getColor(17170521));
        SystemUI.overrideNotificationAppName(context, this.mPublicNotificationBuilder);
        this.mNotificationBuilder = new Notification.Builder(context).setTicker(resources.getString(2131493334) + (mTickerAddSpace ? " " : "")).setContentTitle(resources.getString(2131493335)).setContentText(resources.getString(2131493336)).setSmallIcon(2130838249).setWhen(currentTimeMillis).setShowWhen(true).setColor(resources.getColor(17170521)).setStyle(this.mNotificationStyle).setPublicVersion(this.mPublicNotificationBuilder.build());
        this.mNotificationBuilder.setFlag(32, true);
        SystemUI.overrideNotificationAppName(context, this.mNotificationBuilder);
        this.mNotificationManager.notify(2131886133, this.mNotificationBuilder.build());
        this.mNotificationBuilder.setLargeIcon(createBitmap2.createAshmemBitmap());
        this.mNotificationStyle.bigLargeIcon((Bitmap) null);
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
            contentValues.put("_data", this.mImageFilePath);
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
            this.mNotificationBuilder.addAction(new Notification.Action.Builder(2130837770, resources.getString(17040517), PendingIntent.getActivity(context, 0, Intent.createChooser(intent, null, PendingIntent.getBroadcast(context, 0, new Intent(context, GlobalScreenshot.TargetChosenReceiver.class), 1342177280).getIntentSender()).addFlags(268468224), 268435456)).build());
            this.mNotificationBuilder.addAction(new Notification.Action.Builder(2130837769, resources.getString(17040224), PendingIntent.getBroadcast(context, 0, new Intent(context, GlobalScreenshot.DeleteScreenshotReceiver.class).putExtra("android:screenshot_uri_id", insert.toString()), 1342177280)).build());
            this.mParams.imageUri = insert;
            this.mParams.image = null;
            this.mParams.errorMsgResId = 0;
        } catch (Exception e) {
            this.mParams.clearImage();
            this.mParams.errorMsgResId = 2131493341;
        }
        if (bitmap != null) {
            bitmap.recycle();
            return null;
        }
        return null;
    }

    boolean isDchaStateOn(Context context) {
        int i = Settings.System.getInt(context.getContentResolver(), "dcha_state", 0);
        Log.d("isDchaStateOn", "------ isDchaStateOn -- dcha_state : " + i);
        return i != 0;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    public void onCancelled(Void r4) {
        this.mParams.finisher.run();
        this.mParams.clearImage();
        this.mParams.clearContext();
        this.mNotificationManager.cancel(2131886133);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    public void onPostExecute(Void r7) {
        if (this.mParams.errorMsgResId != 0) {
            GlobalScreenshot.notifyScreenshotError(this.mParams.context, this.mNotificationManager, this.mParams.errorMsgResId);
        } else {
            Context context = this.mParams.context;
            Resources resources = context.getResources();
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.setDataAndType(this.mParams.imageUri, "image/png");
            intent.setFlags(268435456);
            long currentTimeMillis = System.currentTimeMillis();
            this.mPublicNotificationBuilder.setContentTitle(resources.getString(2131493337)).setContentText(resources.getString(2131493338)).setContentIntent(PendingIntent.getActivity(this.mParams.context, 0, intent, 0)).setWhen(currentTimeMillis).setAutoCancel(true).setColor(context.getColor(17170521));
            this.mNotificationBuilder.setContentTitle(resources.getString(2131493337)).setContentText(resources.getString(2131493338)).setContentIntent(PendingIntent.getActivity(this.mParams.context, 0, intent, 0)).setWhen(currentTimeMillis).setAutoCancel(true).setColor(context.getColor(17170521)).setPublicVersion(this.mPublicNotificationBuilder.build()).setFlag(32, false);
            this.mNotificationManager.notify(2131886133, this.mNotificationBuilder.build());
        }
        this.mParams.finisher.run();
        this.mParams.clearContext();
    }
}

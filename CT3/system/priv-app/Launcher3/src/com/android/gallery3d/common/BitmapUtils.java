package com.android.gallery3d.common;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.util.Log;
import com.android.gallery3d.exif.ExifInterface;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
/* loaded from: a.zip:com/android/gallery3d/common/BitmapUtils.class */
public class BitmapUtils {
    public static int computeSampleSizeLarger(float f) {
        int floor = (int) Math.floor(1.0f / f);
        if (floor <= 1) {
            return 1;
        }
        return floor <= 8 ? Utils.prevPowerOf2(floor) : (floor / 8) * 8;
    }

    private static Bitmap.Config getConfig(Bitmap bitmap) {
        Bitmap.Config config = bitmap.getConfig();
        Bitmap.Config config2 = config;
        if (config == null) {
            config2 = Bitmap.Config.ARGB_8888;
        }
        return config2;
    }

    public static int getRotationFromExif(Context context, Uri uri) {
        return getRotationFromExifHelper(null, 0, context, uri);
    }

    public static int getRotationFromExif(Resources resources, int i) {
        return getRotationFromExifHelper(resources, i, null, null);
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Not initialized variable reg: 17, insn: 0x0155: MOVE  (r0 I:??[int, float, boolean, short, byte, char, OBJECT, ARRAY]) = (r17 I:??[int, float, boolean, short, byte, char, OBJECT, ARRAY]), block:B:39:0x0155 */
    /* JADX WARN: Type inference failed for: r7v0, types: [android.net.Uri] */
    /* JADX WARN: Type inference failed for: r7v1 */
    /* JADX WARN: Type inference failed for: r7v2 */
    /* JADX WARN: Type inference failed for: r7v5 */
    /* JADX WARN: Type inference failed for: r7v7 */
    private static int getRotationFromExifHelper(Resources resources, int i, Context context, Uri uri) {
        BufferedInputStream bufferedInputStream;
        BufferedInputStream bufferedInputStream2;
        BufferedInputStream bufferedInputStream3;
        BufferedInputStream bufferedInputStream4;
        InputStream openRawResource;
        BufferedInputStream bufferedInputStream5;
        InputStream inputStream;
        ExifInterface exifInterface = new ExifInterface();
        BufferedInputStream bufferedInputStream6 = null;
        InputStream inputStream2 = null;
        BufferedInputStream bufferedInputStream7 = null;
        try {
            try {
                try {
                    if (uri != 0) {
                        openRawResource = context.getContentResolver().openInputStream(uri);
                        bufferedInputStream5 = new BufferedInputStream(openRawResource);
                        bufferedInputStream7 = bufferedInputStream5;
                        inputStream = openRawResource;
                        bufferedInputStream6 = bufferedInputStream5;
                        inputStream2 = openRawResource;
                        bufferedInputStream3 = bufferedInputStream5;
                        uri = openRawResource;
                        exifInterface.readExif(bufferedInputStream5);
                    } else {
                        openRawResource = resources.openRawResource(i);
                        bufferedInputStream5 = new BufferedInputStream(openRawResource);
                        bufferedInputStream7 = bufferedInputStream5;
                        inputStream = openRawResource;
                        bufferedInputStream6 = bufferedInputStream5;
                        inputStream2 = openRawResource;
                        bufferedInputStream3 = bufferedInputStream5;
                        uri = openRawResource;
                        exifInterface.readExif(bufferedInputStream5);
                    }
                    BufferedInputStream bufferedInputStream8 = bufferedInputStream5;
                    Integer tagIntValue = exifInterface.getTagIntValue(ExifInterface.TAG_ORIENTATION);
                    if (tagIntValue == null) {
                        Utils.closeSilently(bufferedInputStream5);
                        Utils.closeSilently(openRawResource);
                        return 0;
                    }
                    bufferedInputStream3 = bufferedInputStream5;
                    bufferedInputStream7 = openRawResource;
                    bufferedInputStream2 = bufferedInputStream5;
                    bufferedInputStream6 = openRawResource;
                    int rotationForOrientationValue = ExifInterface.getRotationForOrientationValue(tagIntValue.shortValue());
                    Utils.closeSilently(bufferedInputStream5);
                    Utils.closeSilently(openRawResource);
                    return rotationForOrientationValue;
                } catch (Throwable th) {
                    th = th;
                    bufferedInputStream = bufferedInputStream4;
                    Utils.closeSilently(bufferedInputStream);
                    Utils.closeSilently(inputStream2);
                    throw th;
                }
            } catch (IOException e) {
                e = e;
                Log.w("BitmapUtils", "Getting exif data failed", e);
                Utils.closeSilently(bufferedInputStream2);
                Utils.closeSilently(bufferedInputStream6);
                return 0;
            } catch (NullPointerException e2) {
                e = e2;
                uri = bufferedInputStream7;
                Log.w("BitmapUtils", "Getting exif data failed", e);
                Utils.closeSilently(bufferedInputStream3);
                Utils.closeSilently(uri);
                return 0;
            }
        } catch (IOException e3) {
            e = e3;
            bufferedInputStream2 = bufferedInputStream6;
            bufferedInputStream6 = inputStream2;
            Log.w("BitmapUtils", "Getting exif data failed", e);
            Utils.closeSilently(bufferedInputStream2);
            Utils.closeSilently(bufferedInputStream6);
            return 0;
        } catch (NullPointerException e4) {
            e = e4;
            Log.w("BitmapUtils", "Getting exif data failed", e);
            Utils.closeSilently(bufferedInputStream3);
            Utils.closeSilently(uri);
            return 0;
        } catch (Throwable th2) {
            th = th2;
            bufferedInputStream = bufferedInputStream7;
            inputStream2 = inputStream;
            Utils.closeSilently(bufferedInputStream);
            Utils.closeSilently(inputStream2);
            throw th;
        }
    }

    public static Bitmap resizeBitmapByScale(Bitmap bitmap, float f, boolean z) {
        int round = Math.round(bitmap.getWidth() * f);
        int round2 = Math.round(bitmap.getHeight() * f);
        if (round == bitmap.getWidth() && round2 == bitmap.getHeight()) {
            return bitmap;
        }
        Bitmap createBitmap = Bitmap.createBitmap(round, round2, getConfig(bitmap));
        Canvas canvas = new Canvas(createBitmap);
        canvas.scale(f, f);
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, new Paint(6));
        if (z) {
            bitmap.recycle();
        }
        return createBitmap;
    }
}

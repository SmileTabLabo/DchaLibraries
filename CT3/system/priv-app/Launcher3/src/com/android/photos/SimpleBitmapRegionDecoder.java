package com.android.photos;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
/* loaded from: a.zip:com/android/photos/SimpleBitmapRegionDecoder.class */
interface SimpleBitmapRegionDecoder {
    Bitmap decodeRegion(Rect rect, BitmapFactory.Options options);

    int getHeight();

    int getWidth();
}

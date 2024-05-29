package com.android.ex.editstyledtext;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.style.ImageSpan;
import android.util.Log;
import java.io.InputStream;
/* loaded from: a.zip:com/android/ex/editstyledtext/EditStyledText$EditStyledTextSpans$RescalableImageSpan.class */
public class EditStyledText$EditStyledTextSpans$RescalableImageSpan extends ImageSpan {
    private final int MAXWIDTH;
    Uri mContentUri;
    private Context mContext;
    private Drawable mDrawable;
    public int mIntrinsicHeight;
    public int mIntrinsicWidth;

    private void rescaleBigImage(Drawable drawable) {
        Log.d("EditStyledTextSpan", "--- rescaleBigImage:");
        if (this.MAXWIDTH < 0) {
            return;
        }
        int intrinsicWidth = drawable.getIntrinsicWidth();
        int intrinsicHeight = drawable.getIntrinsicHeight();
        Log.d("EditStyledTextSpan", "--- rescaleBigImage:" + intrinsicWidth + "," + intrinsicHeight + "," + this.MAXWIDTH);
        int i = intrinsicHeight;
        int i2 = intrinsicWidth;
        if (intrinsicWidth > this.MAXWIDTH) {
            i2 = this.MAXWIDTH;
            i = (this.MAXWIDTH * intrinsicHeight) / i2;
        }
        drawable.setBounds(0, 0, i2, i);
    }

    @Override // android.text.style.ImageSpan, android.text.style.DynamicDrawableSpan
    public Drawable getDrawable() {
        Bitmap decodeStream;
        if (this.mDrawable != null) {
            return this.mDrawable;
        }
        if (this.mContentUri != null) {
            System.gc();
            try {
                InputStream openInputStream = this.mContext.getContentResolver().openInputStream(this.mContentUri);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(openInputStream, null, options);
                openInputStream.close();
                InputStream openInputStream2 = this.mContext.getContentResolver().openInputStream(this.mContentUri);
                int i = options.outWidth;
                int i2 = options.outHeight;
                this.mIntrinsicWidth = i;
                this.mIntrinsicHeight = i2;
                if (options.outWidth > this.MAXWIDTH) {
                    i = this.MAXWIDTH;
                    i2 = (this.MAXWIDTH * i2) / options.outWidth;
                    decodeStream = BitmapFactory.decodeStream(openInputStream2, new Rect(0, 0, i, i2), null);
                } else {
                    decodeStream = BitmapFactory.decodeStream(openInputStream2);
                }
                this.mDrawable = new BitmapDrawable(this.mContext.getResources(), decodeStream);
                this.mDrawable.setBounds(0, 0, i, i2);
                openInputStream2.close();
            } catch (Exception e) {
                Log.e("EditStyledTextSpan", "Failed to loaded content " + this.mContentUri, e);
                return null;
            } catch (OutOfMemoryError e2) {
                Log.e("EditStyledTextSpan", "OutOfMemoryError");
                return null;
            }
        } else {
            this.mDrawable = super.getDrawable();
            rescaleBigImage(this.mDrawable);
            this.mIntrinsicWidth = this.mDrawable.getIntrinsicWidth();
            this.mIntrinsicHeight = this.mDrawable.getIntrinsicHeight();
        }
        return this.mDrawable;
    }
}

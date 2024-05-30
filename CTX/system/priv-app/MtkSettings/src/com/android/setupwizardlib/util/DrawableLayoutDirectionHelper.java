package com.android.setupwizardlib.util;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Build;
import android.view.View;
/* loaded from: classes.dex */
public class DrawableLayoutDirectionHelper {
    @SuppressLint({"InlinedApi"})
    public static InsetDrawable createRelativeInsetDrawable(Drawable drawable, int i, int i2, int i3, int i4, View view) {
        boolean z = true;
        return createRelativeInsetDrawable(drawable, i, i2, i3, i4, (Build.VERSION.SDK_INT < 17 || view.getLayoutDirection() != 1) ? false : false);
    }

    private static InsetDrawable createRelativeInsetDrawable(Drawable drawable, int i, int i2, int i3, int i4, boolean z) {
        if (z) {
            return new InsetDrawable(drawable, i3, i2, i, i4);
        }
        return new InsetDrawable(drawable, i, i2, i3, i4);
    }
}

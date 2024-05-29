package com.android.ex.editstyledtext;

import android.graphics.drawable.Drawable;
import android.text.style.DynamicDrawableSpan;
/* loaded from: a.zip:com/android/ex/editstyledtext/EditStyledText$EditStyledTextSpans$HorizontalLineSpan.class */
public class EditStyledText$EditStyledTextSpans$HorizontalLineSpan extends DynamicDrawableSpan {
    EditStyledText$EditStyledTextSpans$HorizontalLineDrawable mDrawable;

    @Override // android.text.style.DynamicDrawableSpan
    public Drawable getDrawable() {
        return this.mDrawable;
    }

    public void resetWidth(int i) {
        this.mDrawable.renewBounds(i);
    }
}

package com.android.settings;

import android.content.Context;
import android.support.v7.preference.PreferenceCategory;
import android.util.AttributeSet;
/* loaded from: classes.dex */
public abstract class ProgressCategoryBase extends PreferenceCategory {
    public ProgressCategoryBase(Context context) {
        this(context, null);
    }

    public ProgressCategoryBase(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressCategoryBase(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, 0);
    }

    public ProgressCategoryBase(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}

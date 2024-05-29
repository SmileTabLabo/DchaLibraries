package com.android.launcher3;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
/* loaded from: a.zip:com/android/launcher3/PageIndicatorMarker.class */
public class PageIndicatorMarker extends FrameLayout {
    private ImageView mActiveMarker;
    private ImageView mInactiveMarker;
    private boolean mIsActive;

    public PageIndicatorMarker(Context context) {
        this(context, null);
    }

    public PageIndicatorMarker(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public PageIndicatorMarker(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mIsActive = false;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void activate(boolean z) {
        if (z) {
            this.mActiveMarker.animate().cancel();
            this.mActiveMarker.setAlpha(1.0f);
            this.mActiveMarker.setScaleX(1.0f);
            this.mActiveMarker.setScaleY(1.0f);
            this.mInactiveMarker.animate().cancel();
            this.mInactiveMarker.setAlpha(0.0f);
        } else {
            this.mActiveMarker.animate().alpha(1.0f).scaleX(1.0f).scaleY(1.0f).setDuration(175L).start();
            this.mInactiveMarker.animate().alpha(0.0f).setDuration(175L).start();
        }
        this.mIsActive = true;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void inactivate(boolean z) {
        if (z) {
            this.mInactiveMarker.animate().cancel();
            this.mInactiveMarker.setAlpha(1.0f);
            this.mActiveMarker.animate().cancel();
            this.mActiveMarker.setAlpha(0.0f);
            this.mActiveMarker.setScaleX(0.5f);
            this.mActiveMarker.setScaleY(0.5f);
        } else {
            this.mInactiveMarker.animate().alpha(1.0f).setDuration(175L).start();
            this.mActiveMarker.animate().alpha(0.0f).scaleX(0.5f).scaleY(0.5f).setDuration(175L).start();
        }
        this.mIsActive = false;
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mActiveMarker = (ImageView) findViewById(2131296306);
        this.mInactiveMarker = (ImageView) findViewById(2131296305);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setMarkerDrawables(int i, int i2) {
        Resources resources = getResources();
        this.mActiveMarker.setImageDrawable(resources.getDrawable(i));
        this.mInactiveMarker.setImageDrawable(resources.getDrawable(i2));
    }
}

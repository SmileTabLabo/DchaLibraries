package com.android.launcher3.allapps;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.android.launcher3.PagedView;
/* loaded from: classes.dex */
public class AllAppsPagedView extends PagedView<PersonalWorkSlidingTabStrip> {
    static final float MAX_SWIPE_ANGLE = 1.0471976f;
    static final float START_DAMPING_TOUCH_SLOP_ANGLE = 0.5235988f;
    static final float TOUCH_SLOP_DAMPING_FACTOR = 4.0f;

    public AllAppsPagedView(Context context) {
        this(context, null);
    }

    public AllAppsPagedView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public AllAppsPagedView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    @Override // com.android.launcher3.PagedView
    protected String getCurrentPageDescription() {
        return "";
    }

    @Override // android.view.View
    protected void onScrollChanged(int i, int i2, int i3, int i4) {
        super.onScrollChanged(i, i2, i3, i4);
        ((PersonalWorkSlidingTabStrip) this.mPageIndicator).setScroll(i, this.mMaxScrollX);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.launcher3.PagedView
    public void determineScrollingStart(MotionEvent motionEvent) {
        float abs = Math.abs(motionEvent.getX() - getDownMotionX());
        float abs2 = Math.abs(motionEvent.getY() - getDownMotionY());
        if (Float.compare(abs, 0.0f) == 0) {
            return;
        }
        float atan = (float) Math.atan(abs2 / abs);
        if (abs > this.mTouchSlop || abs2 > this.mTouchSlop) {
            cancelCurrentPageLongPress();
        }
        if (atan > MAX_SWIPE_ANGLE) {
            return;
        }
        if (atan > START_DAMPING_TOUCH_SLOP_ANGLE) {
            super.determineScrollingStart(motionEvent, 1.0f + (TOUCH_SLOP_DAMPING_FACTOR * ((float) Math.sqrt((atan - START_DAMPING_TOUCH_SLOP_ANGLE) / START_DAMPING_TOUCH_SLOP_ANGLE))));
        } else {
            super.determineScrollingStart(motionEvent);
        }
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }
}

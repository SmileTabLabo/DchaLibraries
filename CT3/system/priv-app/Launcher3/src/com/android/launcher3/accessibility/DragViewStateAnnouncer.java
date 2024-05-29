package com.android.launcher3.accessibility;

import android.view.View;
import android.view.accessibility.AccessibilityManager;
/* loaded from: a.zip:com/android/launcher3/accessibility/DragViewStateAnnouncer.class */
public class DragViewStateAnnouncer implements Runnable {
    private final View mTargetView;

    private DragViewStateAnnouncer(View view) {
        this.mTargetView = view;
    }

    public static DragViewStateAnnouncer createFor(View view) {
        if (((AccessibilityManager) view.getContext().getSystemService("accessibility")).isEnabled()) {
            return new DragViewStateAnnouncer(view);
        }
        return null;
    }

    public void announce(CharSequence charSequence) {
        this.mTargetView.setContentDescription(charSequence);
        this.mTargetView.removeCallbacks(this);
        this.mTargetView.postDelayed(this, 200L);
    }

    public void cancel() {
        this.mTargetView.removeCallbacks(this);
    }

    @Override // java.lang.Runnable
    public void run() {
        this.mTargetView.sendAccessibilityEvent(4);
    }
}

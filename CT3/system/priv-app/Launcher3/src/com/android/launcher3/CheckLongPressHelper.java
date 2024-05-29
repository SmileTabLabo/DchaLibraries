package com.android.launcher3;

import android.view.View;
/* loaded from: a.zip:com/android/launcher3/CheckLongPressHelper.class */
public class CheckLongPressHelper {
    boolean mHasPerformedLongPress;
    View.OnLongClickListener mListener;
    private int mLongPressTimeout = 300;
    private CheckForLongPress mPendingCheckForLongPress;
    View mView;

    /* loaded from: a.zip:com/android/launcher3/CheckLongPressHelper$CheckForLongPress.class */
    class CheckForLongPress implements Runnable {
        final CheckLongPressHelper this$0;

        CheckForLongPress(CheckLongPressHelper checkLongPressHelper) {
            this.this$0 = checkLongPressHelper;
        }

        @Override // java.lang.Runnable
        public void run() {
            if (this.this$0.mView.getParent() == null || !this.this$0.mView.hasWindowFocus() || this.this$0.mHasPerformedLongPress) {
                return;
            }
            if (this.this$0.mListener != null ? this.this$0.mListener.onLongClick(this.this$0.mView) : this.this$0.mView.performLongClick()) {
                this.this$0.mView.setPressed(false);
                this.this$0.mHasPerformedLongPress = true;
            }
        }
    }

    public CheckLongPressHelper(View view) {
        this.mView = view;
    }

    public void cancelLongPress() {
        this.mHasPerformedLongPress = false;
        if (this.mPendingCheckForLongPress != null) {
            this.mView.removeCallbacks(this.mPendingCheckForLongPress);
            this.mPendingCheckForLongPress = null;
        }
    }

    public boolean hasPerformedLongPress() {
        return this.mHasPerformedLongPress;
    }

    public void postCheckForLongPress() {
        this.mHasPerformedLongPress = false;
        if (this.mPendingCheckForLongPress == null) {
            this.mPendingCheckForLongPress = new CheckForLongPress(this);
        }
        this.mView.postDelayed(this.mPendingCheckForLongPress, this.mLongPressTimeout);
    }

    public void setLongPressTimeout(int i) {
        this.mLongPressTimeout = i;
    }
}

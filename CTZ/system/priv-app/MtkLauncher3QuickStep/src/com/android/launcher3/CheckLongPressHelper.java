package com.android.launcher3;

import android.view.View;
/* loaded from: classes.dex */
public class CheckLongPressHelper {
    public static final int DEFAULT_LONG_PRESS_TIMEOUT = 300;
    boolean mHasPerformedLongPress;
    View.OnLongClickListener mListener;
    private int mLongPressTimeout = 300;
    private CheckForLongPress mPendingCheckForLongPress;
    View mView;

    /* loaded from: classes.dex */
    class CheckForLongPress implements Runnable {
        CheckForLongPress() {
        }

        @Override // java.lang.Runnable
        public void run() {
            boolean performLongClick;
            if (CheckLongPressHelper.this.mView.getParent() != null && CheckLongPressHelper.this.mView.hasWindowFocus() && !CheckLongPressHelper.this.mHasPerformedLongPress) {
                if (CheckLongPressHelper.this.mListener != null) {
                    performLongClick = CheckLongPressHelper.this.mListener.onLongClick(CheckLongPressHelper.this.mView);
                } else {
                    performLongClick = CheckLongPressHelper.this.mView.performLongClick();
                }
                if (performLongClick) {
                    CheckLongPressHelper.this.mView.setPressed(false);
                    CheckLongPressHelper.this.mHasPerformedLongPress = true;
                }
            }
        }
    }

    public CheckLongPressHelper(View view) {
        this.mView = view;
    }

    public CheckLongPressHelper(View view, View.OnLongClickListener onLongClickListener) {
        this.mView = view;
        this.mListener = onLongClickListener;
    }

    public void setLongPressTimeout(int i) {
        this.mLongPressTimeout = i;
    }

    public void postCheckForLongPress() {
        this.mHasPerformedLongPress = false;
        if (this.mPendingCheckForLongPress == null) {
            this.mPendingCheckForLongPress = new CheckForLongPress();
        }
        this.mView.postDelayed(this.mPendingCheckForLongPress, this.mLongPressTimeout);
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
}

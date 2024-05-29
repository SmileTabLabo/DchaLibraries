package com.android.launcher3;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import com.android.launcher3.compat.PackageInstallerCompat;
/* loaded from: a.zip:com/android/launcher3/StylusEventHelper.class */
public class StylusEventHelper {
    private boolean mIsButtonPressed;
    private View mView;

    public StylusEventHelper(View view) {
        this.mView = view;
    }

    private static boolean isStylusButtonPressed(MotionEvent motionEvent) {
        boolean z = false;
        if (motionEvent.getToolType(0) == 2) {
            z = false;
            if ((motionEvent.getButtonState() & 2) == 2) {
                z = true;
            }
        }
        return z;
    }

    public boolean checkAndPerformStylusEvent(MotionEvent motionEvent) {
        float scaledTouchSlop = ViewConfiguration.get(this.mView.getContext()).getScaledTouchSlop();
        if (this.mView.isLongClickable()) {
            boolean isStylusButtonPressed = isStylusButtonPressed(motionEvent);
            switch (motionEvent.getAction()) {
                case PackageInstallerCompat.STATUS_INSTALLED /* 0 */:
                    this.mIsButtonPressed = false;
                    if (isStylusButtonPressed && this.mView.performLongClick()) {
                        this.mIsButtonPressed = true;
                        return true;
                    }
                    return false;
                case 1:
                case 3:
                    this.mIsButtonPressed = false;
                    return false;
                case 2:
                    if (Utilities.pointInView(this.mView, motionEvent.getX(), motionEvent.getY(), scaledTouchSlop)) {
                        if (!this.mIsButtonPressed && isStylusButtonPressed && this.mView.performLongClick()) {
                            this.mIsButtonPressed = true;
                            return true;
                        } else if (!this.mIsButtonPressed || isStylusButtonPressed) {
                            return false;
                        } else {
                            this.mIsButtonPressed = false;
                            return false;
                        }
                    }
                    return false;
                default:
                    return false;
            }
        }
        return false;
    }

    public boolean inStylusButtonPressed() {
        return this.mIsButtonPressed;
    }
}

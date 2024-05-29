package com.android.systemui.statusbar.phone;

import android.graphics.Rect;
import com.android.systemui.statusbar.policy.BatteryController;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/LightStatusBarController.class */
public class LightStatusBarController {
    private final BatteryController mBatteryController;
    private boolean mDockedLight;
    private int mDockedStackVisibility;
    private FingerprintUnlockController mFingerprintUnlockController;
    private boolean mFullscreenLight;
    private int mFullscreenStackVisibility;
    private final StatusBarIconController mIconController;
    private final Rect mLastFullscreenBounds = new Rect();
    private final Rect mLastDockedBounds = new Rect();

    public LightStatusBarController(StatusBarIconController statusBarIconController, BatteryController batteryController) {
        this.mIconController = statusBarIconController;
        this.mBatteryController = batteryController;
    }

    private boolean animateChange() {
        boolean z = true;
        if (this.mFingerprintUnlockController == null) {
            return false;
        }
        int mode = this.mFingerprintUnlockController.getMode();
        if (mode == 2) {
            z = false;
        } else if (mode == 1) {
            z = false;
        }
        return z;
    }

    private boolean isLight(int i, int i2) {
        boolean z = true;
        if (i2 != 4) {
            z = i2 == 6;
        }
        boolean z2 = z && !this.mBatteryController.isPowerSave();
        boolean z3 = (i & 8192) != 0;
        if (!z2) {
            z3 = false;
        }
        return z3;
    }

    private void update(Rect rect, Rect rect2) {
        boolean z = !rect2.isEmpty();
        if ((this.mFullscreenLight && this.mDockedLight) || (this.mFullscreenLight && !z)) {
            this.mIconController.setIconsDarkArea(null);
            this.mIconController.setIconsDark(true, animateChange());
        } else if ((!this.mFullscreenLight && !this.mDockedLight) || (!this.mFullscreenLight && !z)) {
            this.mIconController.setIconsDark(false, animateChange());
        } else {
            if (!this.mFullscreenLight) {
                rect = rect2;
            }
            if (rect.isEmpty()) {
                this.mIconController.setIconsDarkArea(null);
            } else {
                this.mIconController.setIconsDarkArea(rect);
            }
            this.mIconController.setIconsDark(true, animateChange());
        }
    }

    public void onSystemUiVisibilityChanged(int i, int i2, int i3, Rect rect, Rect rect2, boolean z, int i4) {
        int i5 = this.mFullscreenStackVisibility;
        int i6 = ((i3 ^ (-1)) & i5) | (i & i3);
        int i7 = this.mDockedStackVisibility;
        int i8 = ((i3 ^ (-1)) & i7) | (i2 & i3);
        if (((i6 ^ i5) & 8192) != 0 || ((i8 ^ i7) & 8192) != 0 || z || !this.mLastFullscreenBounds.equals(rect) || !this.mLastDockedBounds.equals(rect2)) {
            this.mFullscreenLight = isLight(i6, i4);
            this.mDockedLight = isLight(i8, i4);
            update(rect, rect2);
        }
        this.mFullscreenStackVisibility = i6;
        this.mDockedStackVisibility = i8;
        this.mLastFullscreenBounds.set(rect);
        this.mLastDockedBounds.set(rect2);
    }

    public void setFingerprintUnlockController(FingerprintUnlockController fingerprintUnlockController) {
        this.mFingerprintUnlockController = fingerprintUnlockController;
    }
}

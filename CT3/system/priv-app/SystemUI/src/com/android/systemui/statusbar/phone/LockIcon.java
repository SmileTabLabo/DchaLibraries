package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.statusbar.KeyguardAffordanceView;
import com.android.systemui.statusbar.policy.AccessibilityController;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/LockIcon.class */
public class LockIcon extends KeyguardAffordanceView {
    private AccessibilityController mAccessibilityController;
    private int mDensity;
    private boolean mDeviceInteractive;
    private boolean mHasFingerPrintIcon;
    private boolean mLastDeviceInteractive;
    private boolean mLastScreenOn;
    private int mLastState;
    private boolean mScreenOn;
    private boolean mTransientFpError;
    private TrustDrawable mTrustDrawable;
    private final UnlockMethodCache mUnlockMethodCache;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/statusbar/phone/LockIcon$IntrinsicSizeDrawable.class */
    public static class IntrinsicSizeDrawable extends InsetDrawable {
        private final int mIntrinsicHeight;
        private final int mIntrinsicWidth;

        public IntrinsicSizeDrawable(Drawable drawable, int i, int i2) {
            super(drawable, 0);
            this.mIntrinsicWidth = i;
            this.mIntrinsicHeight = i2;
        }

        @Override // android.graphics.drawable.InsetDrawable, android.graphics.drawable.DrawableWrapper, android.graphics.drawable.Drawable
        public int getIntrinsicHeight() {
            return this.mIntrinsicHeight;
        }

        @Override // android.graphics.drawable.InsetDrawable, android.graphics.drawable.DrawableWrapper, android.graphics.drawable.Drawable
        public int getIntrinsicWidth() {
            return this.mIntrinsicWidth;
        }
    }

    public LockIcon(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mLastState = 0;
        this.mTrustDrawable = new TrustDrawable(context);
        setBackground(this.mTrustDrawable);
        this.mUnlockMethodCache = UnlockMethodCache.getInstance(context);
    }

    private int getAnimationResForTransition(int i, int i2, boolean z, boolean z2, boolean z3, boolean z4) {
        if (i == 3 && i2 == 4) {
            return 2130837894;
        }
        if (i == 1 && i2 == 4) {
            return 2130838340;
        }
        if (i == 4 && i2 == 1) {
            return 2130837613;
        }
        if (i == 4 && i2 == 3) {
            return 2130837892;
        }
        if (i == 3 && i2 == 1 && !this.mUnlockMethodCache.isTrusted()) {
            return 2130837888;
        }
        if (i2 == 3) {
            if (!z3 && z4 && z2) {
                return 2130837890;
            }
            return (z4 && !z && z2) ? 2130837890 : -1;
        }
        return -1;
    }

    private int getIconForState(int i, boolean z, boolean z2) {
        switch (i) {
            case 0:
                return 2130837678;
            case 1:
                return 2130837679;
            case 2:
                return 17302245;
            case 3:
                return (z && z2) ? 2130837650 : 2130837890;
            case 4:
                return 2130837651;
            default:
                throw new IllegalArgumentException();
        }
    }

    private int getState() {
        KeyguardUpdateMonitor keyguardUpdateMonitor = KeyguardUpdateMonitor.getInstance(this.mContext);
        boolean isFingerprintDetectionRunning = keyguardUpdateMonitor.isFingerprintDetectionRunning();
        boolean isUnlockingWithFingerprintAllowed = keyguardUpdateMonitor.isUnlockingWithFingerprintAllowed();
        if (this.mTransientFpError) {
            return 4;
        }
        if (this.mUnlockMethodCache.canSkipBouncer()) {
            return 1;
        }
        if (this.mUnlockMethodCache.isFaceUnlockRunning()) {
            return 2;
        }
        return (isFingerprintDetectionRunning && isUnlockingWithFingerprintAllowed) ? 3 : 0;
    }

    private void updateClickability() {
        if (this.mAccessibilityController == null) {
            return;
        }
        boolean isTouchExplorationEnabled = this.mAccessibilityController.isTouchExplorationEnabled();
        boolean z = this.mUnlockMethodCache.isTrustManaged() ? !this.mAccessibilityController.isAccessibilityEnabled() : false;
        boolean z2 = this.mUnlockMethodCache.isTrustManaged() ? !z : false;
        if (z) {
            isTouchExplorationEnabled = true;
        }
        setClickable(isTouchExplorationEnabled);
        setLongClickable(z2);
        setFocusable(this.mAccessibilityController.isAccessibilityEnabled());
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        int i = configuration.densityDpi;
        if (i != this.mDensity) {
            this.mDensity = i;
            this.mTrustDrawable.stop();
            this.mTrustDrawable = new TrustDrawable(getContext());
            setBackground(this.mTrustDrawable);
            update();
        }
    }

    @Override // android.widget.ImageView, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mTrustDrawable.stop();
    }

    @Override // android.view.View
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        if (this.mHasFingerPrintIcon) {
            accessibilityNodeInfo.setClassName(LockIcon.class.getName());
            accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(16, getContext().getString(2131493357)));
        }
    }

    @Override // android.view.View
    protected void onVisibilityChanged(View view, int i) {
        super.onVisibilityChanged(view, i);
        if (isShown()) {
            this.mTrustDrawable.start();
        } else {
            this.mTrustDrawable.stop();
        }
    }

    public void setAccessibilityController(AccessibilityController accessibilityController) {
        this.mAccessibilityController = accessibilityController;
    }

    public void setDeviceInteractive(boolean z) {
        this.mDeviceInteractive = z;
        update();
    }

    public void setScreenOn(boolean z) {
        this.mScreenOn = z;
        update();
    }

    public void setTransientFpError(boolean z) {
        this.mTransientFpError = z;
        update();
    }

    public void update() {
        update(false);
    }

    /* JADX WARN: Code restructure failed: missing block: B:31:0x00dc, code lost:
        if (r0.getIntrinsicWidth() != r0) goto L44;
     */
    /* JADX WARN: Code restructure failed: missing block: B:61:0x01a1, code lost:
        if (r9 != false) goto L29;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public void update(boolean z) {
        boolean z2;
        boolean z3;
        boolean z4;
        if (isShown() ? KeyguardUpdateMonitor.getInstance(this.mContext).isDeviceInteractive() : false) {
            this.mTrustDrawable.start();
        } else {
            this.mTrustDrawable.stop();
        }
        int state = getState();
        boolean z5 = state == 3 || state == 4;
        boolean z6 = z5;
        boolean z7 = z5;
        if (state == this.mLastState && this.mDeviceInteractive == this.mLastDeviceInteractive && this.mScreenOn == this.mLastScreenOn) {
            z4 = z7;
        }
        boolean z8 = true;
        int animationResForTransition = getAnimationResForTransition(this.mLastState, state, this.mLastDeviceInteractive, this.mDeviceInteractive, this.mLastScreenOn, this.mScreenOn);
        if (animationResForTransition == 2130837888) {
            z5 = true;
            z3 = true;
            z2 = true;
        } else if (animationResForTransition == 2130838340) {
            z5 = true;
            z3 = false;
            z2 = true;
        } else {
            z2 = z7;
            z3 = z6;
            if (animationResForTransition == 2130837613) {
                z5 = true;
                z3 = false;
                z2 = false;
            }
        }
        int i = animationResForTransition;
        if (animationResForTransition == -1) {
            i = getIconForState(state, this.mScreenOn, this.mDeviceInteractive);
            z8 = false;
        }
        Drawable drawable = this.mContext.getDrawable(i);
        AnimatedVectorDrawable animatedVectorDrawable = drawable instanceof AnimatedVectorDrawable ? (AnimatedVectorDrawable) drawable : null;
        int dimensionPixelSize = getResources().getDimensionPixelSize(2131689935);
        int dimensionPixelSize2 = getResources().getDimensionPixelSize(2131689936);
        IntrinsicSizeDrawable intrinsicSizeDrawable = drawable;
        if (!z5) {
            if (drawable.getIntrinsicHeight() == dimensionPixelSize) {
                intrinsicSizeDrawable = drawable;
            }
            intrinsicSizeDrawable = new IntrinsicSizeDrawable(drawable, dimensionPixelSize2, dimensionPixelSize);
        }
        setPaddingRelative(0, 0, 0, z3 ? getResources().getDimensionPixelSize(2131689966) : 0);
        setRestingAlpha(z5 ? 1.0f : 0.5f);
        setImageDrawable(intrinsicSizeDrawable);
        setContentDescription(getResources().getString(z5 ? 2131493356 : 2131493355));
        this.mHasFingerPrintIcon = z5;
        if (animatedVectorDrawable != null && z8) {
            animatedVectorDrawable.start();
        }
        this.mLastState = state;
        this.mLastDeviceInteractive = this.mDeviceInteractive;
        this.mLastScreenOn = this.mScreenOn;
        z4 = z2;
        this.mTrustDrawable.setTrustManaged(this.mUnlockMethodCache.isTrustManaged() && !z4);
        updateClickability();
    }
}

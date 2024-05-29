package com.android.systemui.statusbar.phone;

import android.graphics.Color;
import android.os.Trace;
import android.util.MathUtils;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.statusbar.ScrimView;
/* loaded from: classes.dex */
public enum ScrimState {
    UNINITIALIZED(-1),
    KEYGUARD(0) { // from class: com.android.systemui.statusbar.phone.ScrimState.1
        @Override // com.android.systemui.statusbar.phone.ScrimState
        public void prepare(ScrimState scrimState) {
            this.mBlankScreen = false;
            if (scrimState == ScrimState.AOD) {
                this.mAnimationDuration = 500L;
                if (this.mDisplayRequiresBlanking) {
                    this.mBlankScreen = true;
                }
            } else {
                this.mAnimationDuration = 220L;
            }
            this.mCurrentBehindAlpha = this.mScrimBehindAlphaKeyguard;
            this.mCurrentInFrontAlpha = 0.0f;
        }

        @Override // com.android.systemui.statusbar.phone.ScrimState
        public float getBehindAlpha(float f) {
            return MathUtils.map(0.0f, 1.0f, this.mScrimBehindAlphaKeyguard, 0.7f, f);
        }
    },
    BOUNCER(1) { // from class: com.android.systemui.statusbar.phone.ScrimState.2
        @Override // com.android.systemui.statusbar.phone.ScrimState
        public void prepare(ScrimState scrimState) {
            this.mCurrentBehindAlpha = 0.7f;
            this.mCurrentInFrontAlpha = 0.0f;
        }
    },
    BOUNCER_SCRIMMED(2) { // from class: com.android.systemui.statusbar.phone.ScrimState.3
        @Override // com.android.systemui.statusbar.phone.ScrimState
        public void prepare(ScrimState scrimState) {
            this.mCurrentBehindAlpha = 0.0f;
            this.mCurrentInFrontAlpha = 0.7f;
        }
    },
    BRIGHTNESS_MIRROR(3) { // from class: com.android.systemui.statusbar.phone.ScrimState.4
        @Override // com.android.systemui.statusbar.phone.ScrimState
        public void prepare(ScrimState scrimState) {
            this.mCurrentBehindAlpha = 0.0f;
            this.mCurrentInFrontAlpha = 0.0f;
        }
    },
    AOD(4) { // from class: com.android.systemui.statusbar.phone.ScrimState.5
        @Override // com.android.systemui.statusbar.phone.ScrimState
        public void prepare(ScrimState scrimState) {
            boolean alwaysOn = this.mDozeParameters.getAlwaysOn();
            this.mBlankScreen = this.mDisplayRequiresBlanking;
            this.mCurrentBehindAlpha = (!this.mWallpaperSupportsAmbientMode || this.mKeyguardUpdateMonitor.hasLockscreenWallpaper()) ? 1.0f : 0.0f;
            this.mCurrentInFrontAlpha = alwaysOn ? this.mAodFrontScrimAlpha : 1.0f;
            this.mCurrentInFrontTint = -16777216;
            this.mCurrentBehindTint = -16777216;
            this.mAnimationDuration = 1000L;
            this.mAnimateChange = this.mDozeParameters.shouldControlScreenOff();
        }

        @Override // com.android.systemui.statusbar.phone.ScrimState
        public boolean isLowPowerState() {
            return true;
        }
    },
    PULSING(5) { // from class: com.android.systemui.statusbar.phone.ScrimState.6
        @Override // com.android.systemui.statusbar.phone.ScrimState
        public void prepare(ScrimState scrimState) {
            float f = 0.0f;
            this.mCurrentInFrontAlpha = 0.0f;
            this.mCurrentInFrontTint = -16777216;
            this.mCurrentBehindAlpha = (!this.mWallpaperSupportsAmbientMode || this.mKeyguardUpdateMonitor.hasLockscreenWallpaper()) ? 1.0f : 1.0f;
            this.mCurrentBehindTint = -16777216;
            this.mBlankScreen = this.mDisplayRequiresBlanking;
        }
    },
    UNLOCKED(6) { // from class: com.android.systemui.statusbar.phone.ScrimState.7
        @Override // com.android.systemui.statusbar.phone.ScrimState
        public void prepare(ScrimState scrimState) {
            this.mCurrentBehindAlpha = 0.0f;
            this.mCurrentInFrontAlpha = 0.0f;
            this.mAnimationDuration = 300L;
            if (scrimState == ScrimState.AOD || scrimState == ScrimState.PULSING) {
                updateScrimColor(this.mScrimInFront, 1.0f, -16777216);
                updateScrimColor(this.mScrimBehind, 1.0f, -16777216);
                this.mCurrentInFrontTint = -16777216;
                this.mCurrentBehindTint = -16777216;
                this.mBlankScreen = true;
                return;
            }
            this.mCurrentInFrontTint = 0;
            this.mCurrentBehindTint = 0;
            this.mBlankScreen = false;
        }
    };
    
    boolean mAnimateChange;
    long mAnimationDuration;
    float mAodFrontScrimAlpha;
    boolean mBlankScreen;
    float mCurrentBehindAlpha;
    int mCurrentBehindTint;
    float mCurrentInFrontAlpha;
    int mCurrentInFrontTint;
    boolean mDisplayRequiresBlanking;
    DozeParameters mDozeParameters;
    int mIndex;
    KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    ScrimView mScrimBehind;
    float mScrimBehindAlphaKeyguard;
    ScrimView mScrimInFront;
    boolean mWallpaperSupportsAmbientMode;

    ScrimState(int i) {
        this.mBlankScreen = false;
        this.mAnimationDuration = 220L;
        this.mCurrentInFrontTint = 0;
        this.mCurrentBehindTint = 0;
        this.mAnimateChange = true;
        this.mIndex = i;
    }

    public void init(ScrimView scrimView, ScrimView scrimView2, DozeParameters dozeParameters) {
        this.mScrimInFront = scrimView;
        this.mScrimBehind = scrimView2;
        this.mDozeParameters = dozeParameters;
        this.mDisplayRequiresBlanking = dozeParameters.getDisplayNeedsBlanking();
        this.mKeyguardUpdateMonitor = KeyguardUpdateMonitor.getInstance(scrimView.getContext());
    }

    public void prepare(ScrimState scrimState) {
    }

    public int getIndex() {
        return this.mIndex;
    }

    public float getFrontAlpha() {
        return this.mCurrentInFrontAlpha;
    }

    public float getBehindAlpha(float f) {
        return this.mCurrentBehindAlpha;
    }

    public int getFrontTint() {
        return this.mCurrentInFrontTint;
    }

    public int getBehindTint() {
        return this.mCurrentBehindTint;
    }

    public long getAnimationDuration() {
        return this.mAnimationDuration;
    }

    public boolean getBlanksScreen() {
        return this.mBlankScreen;
    }

    public void updateScrimColor(ScrimView scrimView, float f, int i) {
        Trace.traceCounter(4096L, scrimView == this.mScrimInFront ? "front_scrim_alpha" : "back_scrim_alpha", (int) (255.0f * f));
        Trace.traceCounter(4096L, scrimView == this.mScrimInFront ? "front_scrim_tint" : "back_scrim_tint", Color.alpha(i));
        scrimView.setTint(i);
        scrimView.setViewAlpha(f);
    }

    public boolean getAnimateChange() {
        return this.mAnimateChange;
    }

    public void setAodFrontScrimAlpha(float f) {
        this.mAodFrontScrimAlpha = f;
    }

    public void setScrimBehindAlphaKeyguard(float f) {
        this.mScrimBehindAlphaKeyguard = f;
    }

    public void setWallpaperSupportsAmbientMode(boolean z) {
        this.mWallpaperSupportsAmbientMode = z;
    }

    public boolean isLowPowerState() {
        return false;
    }
}

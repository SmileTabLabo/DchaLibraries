package com.android.keyguard;

import android.content.Context;
import android.media.AudioSystem;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import com.android.settingslib.animation.AppearAnimationUtils;
import com.android.settingslib.animation.DisappearAnimationUtils;
/* loaded from: a.zip:com/android/keyguard/KeyguardPINView.class */
public class KeyguardPINView extends KeyguardPinBasedInputView {
    private final AppearAnimationUtils mAppearAnimationUtils;
    private ViewGroup mContainer;
    private final DisappearAnimationUtils mDisappearAnimationUtils;
    private int mDisappearYTranslation;
    private View mDivider;
    private ViewGroup mRow0;
    private ViewGroup mRow1;
    private ViewGroup mRow2;
    private ViewGroup mRow3;
    private View[][] mViews;

    public KeyguardPINView(Context context) {
        this(context, null);
    }

    public KeyguardPINView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mAppearAnimationUtils = new AppearAnimationUtils(context);
        this.mDisappearAnimationUtils = new DisappearAnimationUtils(context, 125L, 0.6f, 0.45f, AnimationUtils.loadInterpolator(this.mContext, 17563663));
        this.mDisappearYTranslation = getResources().getDimensionPixelSize(R$dimen.disappear_y_translation);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void enableClipping(boolean z) {
        this.mContainer.setClipToPadding(z);
        this.mContainer.setClipChildren(z);
        this.mRow1.setClipToPadding(z);
        this.mRow2.setClipToPadding(z);
        this.mRow3.setClipToPadding(z);
        setClipChildren(z);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public int getPasswordTextViewId() {
        return R$id.pinEntry;
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public int getWrongPasswordStringId() {
        return R$string.kg_wrong_pin;
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Type inference failed for: r1v35, types: [android.view.View[], android.view.View[][]] */
    @Override // com.android.keyguard.KeyguardPinBasedInputView, com.android.keyguard.KeyguardAbsKeyInputView, android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mContainer = (ViewGroup) findViewById(R$id.container);
        this.mRow0 = (ViewGroup) findViewById(R$id.row0);
        this.mRow1 = (ViewGroup) findViewById(R$id.row1);
        this.mRow2 = (ViewGroup) findViewById(R$id.row2);
        this.mRow3 = (ViewGroup) findViewById(R$id.row3);
        this.mDivider = findViewById(R$id.divider);
        View[] viewArr = {this.mRow0, null, null};
        View findViewById = findViewById(R$id.key1);
        View findViewById2 = findViewById(R$id.key2);
        View findViewById3 = findViewById(R$id.key3);
        this.mViews = new View[]{viewArr, new View[]{findViewById, findViewById2, findViewById3}, new View[]{findViewById(R$id.key4), findViewById(R$id.key5), findViewById(R$id.key6)}, new View[]{findViewById(R$id.key7), findViewById(R$id.key8), findViewById(R$id.key9)}, new View[]{null, findViewById(R$id.key0), findViewById(R$id.key_enter)}, new View[]{null, this.mEcaView, null}};
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView, com.android.keyguard.KeyguardSecurityView
    public void onResume(int i) {
        super.onResume(i);
        boolean isStreamActive = AudioSystem.isStreamActive(3, 0);
        if (this.mLockPatternUtils.usingVoiceWeak() && isStreamActive) {
            this.mSecurityMessageDisplay.setMessage(R$string.voice_unlock_media_playing, true);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardPinBasedInputView, com.android.keyguard.KeyguardAbsKeyInputView
    public void resetState() {
        super.resetState();
        if (!KeyguardUpdateMonitor.getInstance(this.mContext).getMaxBiometricUnlockAttemptsReached()) {
            this.mSecurityMessageDisplay.setMessage(R$string.kg_pin_instructions, true);
        } else if (this.mLockPatternUtils.usingVoiceWeak()) {
            this.mSecurityMessageDisplay.setMessage(R$string.voiceunlock_multiple_failures, true);
        }
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void startAppearAnimation() {
        enableClipping(false);
        setAlpha(1.0f);
        setTranslationY(this.mAppearAnimationUtils.getStartTranslation());
        AppearAnimationUtils.startTranslationYAnimation(this, 0L, 500L, 0.0f, this.mAppearAnimationUtils.getInterpolator());
        this.mAppearAnimationUtils.startAnimation2d(this.mViews, new Runnable(this) { // from class: com.android.keyguard.KeyguardPINView.1
            final KeyguardPINView this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.enableClipping(true);
            }
        });
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView, com.android.keyguard.KeyguardSecurityView
    public boolean startDisappearAnimation(Runnable runnable) {
        enableClipping(false);
        setTranslationY(0.0f);
        AppearAnimationUtils.startTranslationYAnimation(this, 0L, 280L, this.mDisappearYTranslation, this.mDisappearAnimationUtils.getInterpolator());
        this.mDisappearAnimationUtils.startAnimation2d(this.mViews, new Runnable(this, runnable) { // from class: com.android.keyguard.KeyguardPINView.2
            final KeyguardPINView this$0;
            final Runnable val$finishRunnable;

            {
                this.this$0 = this;
                this.val$finishRunnable = runnable;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.enableClipping(true);
                if (this.val$finishRunnable != null) {
                    this.val$finishRunnable.run();
                }
            }
        });
        return true;
    }
}

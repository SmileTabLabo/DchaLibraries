package com.android.systemui.tv.pip;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import com.android.systemui.tv.pip.PipControlsView;
/* loaded from: a.zip:com/android/systemui/tv/pip/PipRecentsControlsView.class */
public class PipRecentsControlsView extends FrameLayout {
    private Animator mFocusGainAnimator;
    private AnimatorSet mFocusLossAnimatorSet;
    private PipControlsView mPipControlsView;
    private final PipManager mPipManager;
    private View mScrim;

    /* loaded from: a.zip:com/android/systemui/tv/pip/PipRecentsControlsView$Listener.class */
    public interface Listener extends PipControlsView.Listener {
        void onBackPressed();
    }

    public PipRecentsControlsView(Context context) {
        this(context, null, 0, 0);
    }

    public PipRecentsControlsView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0, 0);
    }

    public PipRecentsControlsView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public PipRecentsControlsView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mPipManager = PipManager.getInstance();
    }

    private static void cancelAnimator(Animator animator) {
        if (animator.isStarted()) {
            animator.cancel();
        }
    }

    private Animator loadAnimator(View view, int i) {
        Animator loadAnimator = AnimatorInflater.loadAnimator(getContext(), i);
        loadAnimator.setTarget(view);
        return loadAnimator;
    }

    private static void startAnimator(Animator animator, Animator animator2) {
        cancelAnimator(animator2);
        if (animator.isStarted()) {
            return;
        }
        animator.start();
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        if (!keyEvent.isCanceled()) {
            if (keyEvent.getKeyCode() == 4 && keyEvent.getAction() == 1) {
                if (this.mPipControlsView.mListener != null) {
                    ((Listener) this.mPipControlsView.mListener).onBackPressed();
                    return true;
                }
                return true;
            } else if (keyEvent.getKeyCode() == 20) {
                if (keyEvent.getAction() == 0) {
                    this.mPipManager.getPipRecentsOverlayManager().clearFocus();
                    return true;
                }
                return true;
            }
        }
        return super.dispatchKeyEvent(keyEvent);
    }

    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mPipControlsView = (PipControlsView) findViewById(2131886738);
        this.mScrim = findViewById(2131886128);
        this.mFocusGainAnimator = loadAnimator(this.mPipControlsView, 2131034316);
        this.mFocusLossAnimatorSet = new AnimatorSet();
        this.mFocusLossAnimatorSet.playSequentially(loadAnimator(this.mPipControlsView, 2131034317), loadAnimator(this.mScrim, 2131034318));
        setPadding(0, this.mPipManager.getRecentsFocusedPipBounds().bottom, 0, 0);
    }

    public void reset() {
        cancelAnimator(this.mFocusGainAnimator);
        cancelAnimator(this.mFocusLossAnimatorSet);
        this.mScrim.setAlpha(0.0f);
        this.mPipControlsView.setTranslationY(0.0f);
        this.mPipControlsView.setScaleX(1.0f);
        this.mPipControlsView.setScaleY(1.0f);
        this.mPipControlsView.reset();
    }

    public void setListener(Listener listener) {
        this.mPipControlsView.setListener(listener);
    }

    public void startFocusGainAnimation() {
        this.mScrim.setAlpha(0.0f);
        PipControlButtonView focusedButton = this.mPipControlsView.getFocusedButton();
        if (focusedButton != null) {
            focusedButton.startFocusGainAnimation();
        }
        startAnimator(this.mFocusGainAnimator, this.mFocusLossAnimatorSet);
    }

    public void startFocusLossAnimation() {
        PipControlButtonView focusedButton = this.mPipControlsView.getFocusedButton();
        if (focusedButton != null) {
            focusedButton.startFocusLossAnimation();
        }
        startAnimator(this.mFocusLossAnimatorSet, this.mFocusGainAnimator);
    }
}

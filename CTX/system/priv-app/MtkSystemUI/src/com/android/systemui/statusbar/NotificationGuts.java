package com.android.systemui.statusbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.FrameLayout;
import com.android.internal.R;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
/* loaded from: classes.dex */
public class NotificationGuts extends FrameLayout {
    private int mActualHeight;
    private Drawable mBackground;
    private int mClipBottomAmount;
    private int mClipTopAmount;
    private OnGutsClosedListener mClosedListener;
    private boolean mExposed;
    private Runnable mFalsingCheck;
    private GutsContent mGutsContent;
    private Handler mHandler;
    private OnHeightChangedListener mHeightListener;
    private boolean mNeedsFalsingProtection;

    /* loaded from: classes.dex */
    public interface OnGutsClosedListener {
        void onGutsClosed(NotificationGuts notificationGuts);
    }

    /* loaded from: classes.dex */
    public interface OnHeightChangedListener {
        void onHeightChanged(NotificationGuts notificationGuts);
    }

    /* loaded from: classes.dex */
    public interface GutsContent {
        int getActualHeight();

        View getContentView();

        boolean handleCloseControls(boolean z, boolean z2);

        void setGutsParent(NotificationGuts notificationGuts);

        boolean shouldBeSaved();

        boolean willBeRemoved();

        default boolean isLeavebehind() {
            return false;
        }
    }

    public NotificationGuts(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setWillNotDraw(false);
        this.mHandler = new Handler();
        this.mFalsingCheck = new Runnable() { // from class: com.android.systemui.statusbar.NotificationGuts.1
            @Override // java.lang.Runnable
            public void run() {
                if (NotificationGuts.this.mNeedsFalsingProtection && NotificationGuts.this.mExposed) {
                    NotificationGuts.this.closeControls(-1, -1, false, false);
                }
            }
        };
        context.obtainStyledAttributes(attributeSet, R.styleable.Theme, 0, 0).recycle();
    }

    public NotificationGuts(Context context) {
        this(context, null);
    }

    public void setGutsContent(GutsContent gutsContent) {
        this.mGutsContent = gutsContent;
        removeAllViews();
        addView(this.mGutsContent.getContentView());
    }

    public GutsContent getGutsContent() {
        return this.mGutsContent;
    }

    public void resetFalsingCheck() {
        this.mHandler.removeCallbacks(this.mFalsingCheck);
        if (this.mNeedsFalsingProtection && this.mExposed) {
            this.mHandler.postDelayed(this.mFalsingCheck, 8000L);
        }
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        draw(canvas, this.mBackground);
    }

    private void draw(Canvas canvas, Drawable drawable) {
        int i = this.mClipTopAmount;
        int i2 = this.mActualHeight - this.mClipBottomAmount;
        if (drawable != null && i < i2) {
            drawable.setBounds(0, i, getWidth(), i2);
            drawable.draw(canvas);
        }
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mBackground = this.mContext.getDrawable(com.android.systemui.R.drawable.notification_guts_bg);
        if (this.mBackground != null) {
            this.mBackground.setCallback(this);
        }
    }

    @Override // android.view.View
    protected boolean verifyDrawable(Drawable drawable) {
        return super.verifyDrawable(drawable) || drawable == this.mBackground;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void drawableStateChanged() {
        drawableStateChanged(this.mBackground);
    }

    private void drawableStateChanged(Drawable drawable) {
        if (drawable != null && drawable.isStateful()) {
            drawable.setState(getDrawableState());
        }
    }

    @Override // android.view.View
    public void drawableHotspotChanged(float f, float f2) {
        if (this.mBackground != null) {
            this.mBackground.setHotspot(f, f2);
        }
    }

    public void openControls(boolean z, int i, int i2, boolean z2, Runnable runnable) {
        animateOpen(z, i, i2, runnable);
        setExposed(true, z2);
    }

    public void closeControls(boolean z, boolean z2, int i, int i2, boolean z3) {
        if (this.mGutsContent != null) {
            if ((this.mGutsContent.isLeavebehind() && z) || (!this.mGutsContent.isLeavebehind() && z2)) {
                closeControls(i, i2, this.mGutsContent.shouldBeSaved(), z3);
            }
        }
    }

    public void closeControls(int i, int i2, boolean z, boolean z2) {
        boolean dismissCurrentBlockingHelper = ((NotificationBlockingHelperManager) Dependency.get(NotificationBlockingHelperManager.class)).dismissCurrentBlockingHelper();
        if (getWindowToken() == null) {
            if (this.mClosedListener != null) {
                this.mClosedListener.onGutsClosed(this);
            }
        } else if (this.mGutsContent == null || !this.mGutsContent.handleCloseControls(z, z2) || dismissCurrentBlockingHelper) {
            animateClose(i, i2, !dismissCurrentBlockingHelper);
            setExposed(false, this.mNeedsFalsingProtection);
            if (this.mClosedListener != null) {
                this.mClosedListener.onGutsClosed(this);
            }
        }
    }

    private void animateOpen(boolean z, int i, int i2, Runnable runnable) {
        if (!isAttachedToWindow()) {
            Log.w("NotificationGuts", "Failed to animate guts open");
        } else if (z) {
            Animator createCircularReveal = ViewAnimationUtils.createCircularReveal(this, i, i2, 0.0f, (float) Math.hypot(Math.max(getWidth() - i, i), Math.max(getHeight() - i2, i2)));
            createCircularReveal.setDuration(360L);
            createCircularReveal.setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
            createCircularReveal.addListener(new AnimateOpenListener(runnable));
            createCircularReveal.start();
        } else {
            setAlpha(0.0f);
            animate().alpha(1.0f).setDuration(240L).setInterpolator(Interpolators.ALPHA_IN).setListener(new AnimateOpenListener(runnable)).start();
        }
    }

    @VisibleForTesting
    void animateClose(int i, int i2, boolean z) {
        if (!isAttachedToWindow()) {
            Log.w("NotificationGuts", "Failed to animate guts close");
        } else if (z) {
            if (i == -1 || i2 == -1) {
                i = (getLeft() + getRight()) / 2;
                i2 = getTop() + (getHeight() / 2);
            }
            Animator createCircularReveal = ViewAnimationUtils.createCircularReveal(this, i, i2, (float) Math.hypot(Math.max(getWidth() - i, i), Math.max(getHeight() - i2, i2)), 0.0f);
            createCircularReveal.setDuration(360L);
            createCircularReveal.setInterpolator(Interpolators.FAST_OUT_LINEAR_IN);
            createCircularReveal.addListener(new AnimateCloseListener(this));
            createCircularReveal.start();
        } else {
            animate().alpha(0.0f).setDuration(240L).setInterpolator(Interpolators.ALPHA_OUT).setListener(new AnimateCloseListener(this)).start();
        }
    }

    public void setActualHeight(int i) {
        this.mActualHeight = i;
        invalidate();
    }

    public int getIntrinsicHeight() {
        return (this.mGutsContent == null || !this.mExposed) ? getHeight() : this.mGutsContent.getActualHeight();
    }

    public void setClipTopAmount(int i) {
        this.mClipTopAmount = i;
        invalidate();
    }

    public void setClipBottomAmount(int i) {
        this.mClipBottomAmount = i;
        invalidate();
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    public void setClosedListener(OnGutsClosedListener onGutsClosedListener) {
        this.mClosedListener = onGutsClosedListener;
    }

    public void setHeightChangedListener(OnHeightChangedListener onHeightChangedListener) {
        this.mHeightListener = onHeightChangedListener;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onHeightChanged() {
        if (this.mHeightListener != null) {
            this.mHeightListener.onHeightChanged(this);
        }
    }

    @VisibleForTesting
    void setExposed(boolean z, boolean z2) {
        boolean z3 = this.mExposed;
        this.mExposed = z;
        this.mNeedsFalsingProtection = z2;
        if (this.mExposed && this.mNeedsFalsingProtection) {
            resetFalsingCheck();
        } else {
            this.mHandler.removeCallbacks(this.mFalsingCheck);
        }
        if (z3 != this.mExposed && this.mGutsContent != null) {
            View contentView = this.mGutsContent.getContentView();
            contentView.sendAccessibilityEvent(32);
            if (this.mExposed) {
                contentView.requestAccessibilityFocus();
            }
        }
    }

    public boolean willBeRemoved() {
        if (this.mGutsContent != null) {
            return this.mGutsContent.willBeRemoved();
        }
        return false;
    }

    public boolean isExposed() {
        return this.mExposed;
    }

    public boolean isLeavebehind() {
        return this.mGutsContent != null && this.mGutsContent.isLeavebehind();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class AnimateOpenListener extends AnimatorListenerAdapter {
        final Runnable mOnAnimationEnd;

        private AnimateOpenListener(Runnable runnable) {
            this.mOnAnimationEnd = runnable;
        }

        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animator) {
            super.onAnimationEnd(animator);
            if (this.mOnAnimationEnd != null) {
                this.mOnAnimationEnd.run();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class AnimateCloseListener extends AnimatorListenerAdapter {
        final View mView;

        private AnimateCloseListener(View view) {
            this.mView = view;
        }

        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animator) {
            super.onAnimationEnd(animator);
            this.mView.setVisibility(8);
        }
    }
}

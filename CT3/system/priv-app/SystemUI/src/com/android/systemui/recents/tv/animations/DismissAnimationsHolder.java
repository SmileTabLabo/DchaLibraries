package com.android.systemui.recents.tv.animations;

import android.animation.Animator;
import android.content.res.Resources;
import android.graphics.drawable.TransitionDrawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.systemui.Interpolators;
import com.android.systemui.recents.tv.views.TaskCardView;
/* loaded from: a.zip:com/android/systemui/recents/tv/animations/DismissAnimationsHolder.class */
public class DismissAnimationsHolder {
    private ImageView mCardDismissIcon;
    private TransitionDrawable mDismissDrawable;
    private int mDismissEnterYDelta;
    private float mDismissIconNotInDismissStateAlpha;
    private int mDismissStartYDelta;
    private TextView mDismissText;
    private LinearLayout mInfoField;
    private long mLongDuration;
    private long mShortDuration;
    private View mThumbnailView;

    public DismissAnimationsHolder(TaskCardView taskCardView) {
        this.mInfoField = (LinearLayout) taskCardView.findViewById(2131886626);
        this.mThumbnailView = taskCardView.findViewById(2131886629);
        this.mCardDismissIcon = (ImageView) taskCardView.findViewById(2131886631);
        this.mDismissDrawable = (TransitionDrawable) this.mCardDismissIcon.getDrawable();
        this.mDismissDrawable.setCrossFadeEnabled(true);
        this.mDismissText = (TextView) taskCardView.findViewById(2131886632);
        Resources resources = taskCardView.getResources();
        this.mDismissEnterYDelta = resources.getDimensionPixelOffset(2131690063);
        this.mDismissStartYDelta = this.mDismissEnterYDelta * 2;
        this.mShortDuration = resources.getInteger(2131755095);
        this.mLongDuration = resources.getInteger(2131755096);
        this.mDismissIconNotInDismissStateAlpha = resources.getFloat(2131755104);
    }

    public void reset() {
        this.mInfoField.setAlpha(1.0f);
        this.mInfoField.setTranslationY(0.0f);
        this.mInfoField.animate().setListener(null);
        this.mThumbnailView.setAlpha(1.0f);
        this.mThumbnailView.setTranslationY(0.0f);
        this.mCardDismissIcon.setAlpha(0.0f);
        this.mDismissText.setAlpha(0.0f);
    }

    public void startDismissAnimation(Animator.AnimatorListener animatorListener) {
        this.mCardDismissIcon.animate().setDuration(this.mShortDuration).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).alpha(0.0f).withEndAction(new Runnable(this) { // from class: com.android.systemui.recents.tv.animations.DismissAnimationsHolder.3
            final DismissAnimationsHolder this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.mDismissDrawable.reverseTransition(0);
            }
        });
        this.mDismissText.animate().setDuration(this.mShortDuration).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).alpha(0.0f);
        this.mInfoField.animate().setDuration(this.mLongDuration).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).translationY(this.mDismissStartYDelta).alpha(0.0f).setListener(animatorListener);
        this.mThumbnailView.animate().setDuration(this.mLongDuration).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).translationY(this.mDismissStartYDelta).alpha(0.0f);
    }

    public void startEnterAnimation() {
        this.mCardDismissIcon.animate().setDuration(this.mShortDuration).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).alpha(1.0f).withStartAction(new Runnable(this) { // from class: com.android.systemui.recents.tv.animations.DismissAnimationsHolder.1
            final DismissAnimationsHolder this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.mDismissDrawable.startTransition(0);
            }
        });
        this.mDismissText.animate().setDuration(this.mShortDuration).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).alpha(1.0f);
        this.mInfoField.animate().setDuration(this.mShortDuration).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).translationY(this.mDismissEnterYDelta).alpha(0.5f);
        this.mThumbnailView.animate().setDuration(this.mShortDuration).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).translationY(this.mDismissEnterYDelta).alpha(0.5f);
    }

    public void startExitAnimation() {
        this.mCardDismissIcon.animate().setDuration(this.mShortDuration).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).alpha(this.mDismissIconNotInDismissStateAlpha).withEndAction(new Runnable(this) { // from class: com.android.systemui.recents.tv.animations.DismissAnimationsHolder.2
            final DismissAnimationsHolder this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.mDismissDrawable.reverseTransition(0);
            }
        });
        this.mDismissText.animate().setDuration(this.mShortDuration).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).alpha(0.0f);
        this.mInfoField.animate().setDuration(this.mShortDuration).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).translationY(0.0f).alpha(1.0f);
        this.mThumbnailView.animate().setDuration(this.mShortDuration).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).translationY(0.0f).alpha(1.0f);
    }
}

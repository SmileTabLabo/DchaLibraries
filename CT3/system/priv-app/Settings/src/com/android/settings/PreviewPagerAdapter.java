package com.android.settings;

import android.animation.Animator;
import android.content.Context;
import android.content.res.Configuration;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
/* loaded from: classes.dex */
public class PreviewPagerAdapter extends PagerAdapter {
    private static final Interpolator FADE_IN_INTERPOLATOR = new DecelerateInterpolator();
    private static final Interpolator FADE_OUT_INTERPOLATOR = new AccelerateInterpolator();
    private int mAnimationCounter;
    private Runnable mAnimationEndAction;
    private boolean mIsLayoutRtl;
    private FrameLayout[] mPreviewFrames;

    public PreviewPagerAdapter(Context context, boolean isLayoutRtl, int[] previewSampleResIds, Configuration[] configurations) {
        this.mIsLayoutRtl = isLayoutRtl;
        this.mPreviewFrames = new FrameLayout[previewSampleResIds.length];
        for (int i = 0; i < previewSampleResIds.length; i++) {
            int p = this.mIsLayoutRtl ? (previewSampleResIds.length - 1) - i : i;
            this.mPreviewFrames[p] = new FrameLayout(context);
            this.mPreviewFrames[p].setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
            for (Configuration configuration : configurations) {
                Context configContext = context.createConfigurationContext(configuration);
                configContext.setTheme(context.getThemeResId());
                LayoutInflater configInflater = LayoutInflater.from(configContext);
                View sampleView = configInflater.inflate(previewSampleResIds[i], (ViewGroup) this.mPreviewFrames[p], false);
                sampleView.setAlpha(0.0f);
                sampleView.setVisibility(4);
                this.mPreviewFrames[p].addView(sampleView);
            }
        }
    }

    @Override // android.support.v4.view.PagerAdapter
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override // android.support.v4.view.PagerAdapter
    public int getCount() {
        return this.mPreviewFrames.length;
    }

    @Override // android.support.v4.view.PagerAdapter
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(this.mPreviewFrames[position]);
        return this.mPreviewFrames[position];
    }

    @Override // android.support.v4.view.PagerAdapter
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean isAnimating() {
        return this.mAnimationCounter > 0;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setAnimationEndAction(Runnable action) {
        this.mAnimationEndAction = action;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setPreviewLayer(int newIndex, int currentIndex, int currentItem, boolean animate) {
        FrameLayout[] frameLayoutArr;
        for (FrameLayout previewFrame : this.mPreviewFrames) {
            if (currentIndex >= 0) {
                final View lastLayer = previewFrame.getChildAt(currentIndex);
                if (animate && previewFrame == this.mPreviewFrames[currentItem]) {
                    lastLayer.animate().alpha(0.0f).setInterpolator(FADE_OUT_INTERPOLATOR).setDuration(400L).setListener(new PreviewFrameAnimatorListener(this, null)).withEndAction(new Runnable() { // from class: com.android.settings.PreviewPagerAdapter.1
                        @Override // java.lang.Runnable
                        public void run() {
                            lastLayer.setVisibility(4);
                        }
                    });
                } else {
                    lastLayer.setAlpha(0.0f);
                    lastLayer.setVisibility(4);
                }
            }
            final View nextLayer = previewFrame.getChildAt(newIndex);
            if (animate && previewFrame == this.mPreviewFrames[currentItem]) {
                nextLayer.animate().alpha(1.0f).setInterpolator(FADE_IN_INTERPOLATOR).setDuration(400L).setListener(new PreviewFrameAnimatorListener(this, null)).withStartAction(new Runnable() { // from class: com.android.settings.PreviewPagerAdapter.2
                    @Override // java.lang.Runnable
                    public void run() {
                        nextLayer.setVisibility(0);
                    }
                });
            } else {
                nextLayer.setVisibility(0);
                nextLayer.setAlpha(1.0f);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void runAnimationEndAction() {
        if (this.mAnimationEndAction == null || isAnimating()) {
            return;
        }
        this.mAnimationEndAction.run();
        this.mAnimationEndAction = null;
    }

    /* loaded from: classes.dex */
    private class PreviewFrameAnimatorListener implements Animator.AnimatorListener {
        /* synthetic */ PreviewFrameAnimatorListener(PreviewPagerAdapter this$0, PreviewFrameAnimatorListener previewFrameAnimatorListener) {
            this();
        }

        private PreviewFrameAnimatorListener() {
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationStart(Animator animation) {
            PreviewPagerAdapter.this.mAnimationCounter++;
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animation) {
            PreviewPagerAdapter previewPagerAdapter = PreviewPagerAdapter.this;
            previewPagerAdapter.mAnimationCounter--;
            PreviewPagerAdapter.this.runAnimationEndAction();
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationCancel(Animator animation) {
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationRepeat(Animator animation) {
        }
    }
}

package com.android.settings.widget;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
/* loaded from: classes.dex */
public class LoadingViewController {
    public final View mContentView;
    public final View mLoadingView;
    private Runnable mShowLoadingContainerRunnable = new Runnable() { // from class: com.android.settings.widget.LoadingViewController.1
        @Override // java.lang.Runnable
        public void run() {
            LoadingViewController.this.handleLoadingContainer(false, false);
        }
    };
    public final Handler mFgHandler = new Handler(Looper.getMainLooper());

    public LoadingViewController(View view, View view2) {
        this.mLoadingView = view;
        this.mContentView = view2;
    }

    public void showContent(boolean z) {
        this.mFgHandler.removeCallbacks(this.mShowLoadingContainerRunnable);
        handleLoadingContainer(true, z);
    }

    public void showLoadingViewDelayed() {
        this.mFgHandler.postDelayed(this.mShowLoadingContainerRunnable, 100L);
    }

    public void handleLoadingContainer(boolean z, boolean z2) {
        handleLoadingContainer(this.mLoadingView, this.mContentView, z, z2);
    }

    public static void handleLoadingContainer(View view, View view2, boolean z, boolean z2) {
        setViewShown(view, !z, z2);
        setViewShown(view2, z, z2);
    }

    private static void setViewShown(final View view, boolean z, boolean z2) {
        if (z2) {
            Animation loadAnimation = AnimationUtils.loadAnimation(view.getContext(), z ? 17432576 : 17432577);
            if (z) {
                view.setVisibility(0);
            } else {
                loadAnimation.setAnimationListener(new Animation.AnimationListener() { // from class: com.android.settings.widget.LoadingViewController.2
                    @Override // android.view.animation.Animation.AnimationListener
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override // android.view.animation.Animation.AnimationListener
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override // android.view.animation.Animation.AnimationListener
                    public void onAnimationEnd(Animation animation) {
                        view.setVisibility(4);
                    }
                });
            }
            view.startAnimation(loadAnimation);
            return;
        }
        view.clearAnimation();
        view.setVisibility(z ? 0 : 4);
    }
}

package com.android.systemui.recents.tv.animations;

import android.content.Context;
import com.android.systemui.Interpolators;
import com.android.systemui.recents.events.activity.DismissRecentsToHomeAnimationStarted;
import com.android.systemui.recents.tv.views.TaskCardView;
import com.android.systemui.recents.tv.views.TaskStackHorizontalGridView;
/* loaded from: a.zip:com/android/systemui/recents/tv/animations/HomeRecentsEnterExitAnimationHolder.class */
public class HomeRecentsEnterExitAnimationHolder {
    private Context mContext;
    private long mDelay;
    private float mDimAlpha;
    private int mDuration;
    private TaskStackHorizontalGridView mGridView;
    private int mTranslationX;

    public HomeRecentsEnterExitAnimationHolder(Context context, TaskStackHorizontalGridView taskStackHorizontalGridView) {
        this.mContext = context;
        this.mGridView = taskStackHorizontalGridView;
        this.mDimAlpha = this.mContext.getResources().getFloat(2131690027);
        this.mTranslationX = this.mContext.getResources().getDimensionPixelSize(2131690070);
        this.mDelay = this.mContext.getResources().getInteger(2131755099);
        this.mDuration = this.mContext.getResources().getInteger(2131755098);
    }

    public void setEnterFromAppStartingAnimationValues(boolean z) {
        for (int i = 0; i < this.mGridView.getChildCount(); i++) {
            TaskCardView taskCardView = (TaskCardView) this.mGridView.getChildAt(i);
            taskCardView.setTranslationX(0.0f);
            taskCardView.setAlpha(z ? this.mDimAlpha : 1.0f);
            taskCardView.getInfoFieldView().setAlpha(z ? 0.0f : 1.0f);
            if (z && taskCardView.hasFocus()) {
                taskCardView.getViewFocusAnimator().changeSize(false);
            }
        }
    }

    public void setEnterFromHomeStartingAnimationValues(boolean z) {
        for (int i = 0; i < this.mGridView.getChildCount(); i++) {
            TaskCardView taskCardView = (TaskCardView) this.mGridView.getChildAt(i);
            taskCardView.setTranslationX(0.0f);
            taskCardView.setAlpha(0.0f);
            taskCardView.getInfoFieldView().setAlpha(z ? 0.0f : 1.0f);
            if (z && taskCardView.hasFocus()) {
                taskCardView.getViewFocusAnimator().changeSize(false);
            }
        }
    }

    public void startEnterAnimation(boolean z) {
        for (int i = 0; i < this.mGridView.getChildCount(); i++) {
            TaskCardView taskCardView = (TaskCardView) this.mGridView.getChildAt(i);
            taskCardView.setTranslationX(-this.mTranslationX);
            taskCardView.animate().alpha(z ? this.mDimAlpha : 1.0f).translationX(0.0f).setDuration(this.mDuration).setStartDelay(this.mDelay * i).setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
        }
    }

    public void startExitAnimation(DismissRecentsToHomeAnimationStarted dismissRecentsToHomeAnimationStarted) {
        for (int childCount = this.mGridView.getChildCount() - 1; childCount >= 0; childCount--) {
            TaskCardView taskCardView = (TaskCardView) this.mGridView.getChildAt(childCount);
            taskCardView.animate().alpha(0.0f).translationXBy(-this.mTranslationX).setDuration(this.mDuration).setStartDelay(this.mDelay * ((this.mGridView.getChildCount() - 1) - childCount)).setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
            if (childCount == 0) {
                taskCardView.animate().setListener(dismissRecentsToHomeAnimationStarted.getAnimationTrigger().decrementOnAnimationEnd());
                dismissRecentsToHomeAnimationStarted.getAnimationTrigger().increment();
            }
        }
    }
}

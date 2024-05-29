package com.android.systemui.statusbar.notification;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.service.notification.StatusBarNotification;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.android.systemui.statusbar.CrossFadeHelper;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.TransformableView;
import com.android.systemui.statusbar.ViewTransformationHelper;
/* loaded from: a.zip:com/android/systemui/statusbar/notification/NotificationTemplateViewWrapper.class */
public class NotificationTemplateViewWrapper extends NotificationHeaderViewWrapper {
    private View mActionsContainer;
    private int mContentHeight;
    private int mMinHeightHint;
    protected ImageView mPicture;
    private ProgressBar mProgressBar;
    private TextView mText;
    private TextView mTitle;

    /* JADX INFO: Access modifiers changed from: protected */
    public NotificationTemplateViewWrapper(Context context, View view, ExpandableNotificationRow expandableNotificationRow) {
        super(context, view, expandableNotificationRow);
        this.mTransformationHelper.setCustomTransformation(new ViewTransformationHelper.CustomTransformation(this) { // from class: com.android.systemui.statusbar.notification.NotificationTemplateViewWrapper.1
            final NotificationTemplateViewWrapper this$0;

            {
                this.this$0 = this;
            }

            private float getTransformationY(TransformState transformState, TransformState transformState2) {
                return ((transformState2.getLaidOutLocationOnScreen()[1] + transformState2.getTransformedView().getHeight()) - transformState.getLaidOutLocationOnScreen()[1]) * 0.33f;
            }

            @Override // com.android.systemui.statusbar.ViewTransformationHelper.CustomTransformation
            public boolean customTransformTarget(TransformState transformState, TransformState transformState2) {
                transformState.setTransformationEndY(getTransformationY(transformState, transformState2));
                return true;
            }

            @Override // com.android.systemui.statusbar.ViewTransformationHelper.CustomTransformation
            public boolean initTransformation(TransformState transformState, TransformState transformState2) {
                transformState.setTransformationStartY(getTransformationY(transformState, transformState2));
                return true;
            }

            @Override // com.android.systemui.statusbar.ViewTransformationHelper.CustomTransformation
            public boolean transformFrom(TransformState transformState, TransformableView transformableView, float f) {
                if (transformableView instanceof HybridNotificationView) {
                    TransformState currentState = transformableView.getCurrentState(1);
                    CrossFadeHelper.fadeIn(transformState.getTransformedView(), f);
                    if (currentState != null) {
                        transformState.transformViewVerticalFrom(currentState, this, f);
                        currentState.recycle();
                        return true;
                    }
                    return true;
                }
                return false;
            }

            @Override // com.android.systemui.statusbar.ViewTransformationHelper.CustomTransformation
            public boolean transformTo(TransformState transformState, TransformableView transformableView, float f) {
                if (transformableView instanceof HybridNotificationView) {
                    TransformState currentState = transformableView.getCurrentState(1);
                    CrossFadeHelper.fadeOut(transformState.getTransformedView(), f);
                    if (currentState != null) {
                        transformState.transformViewVerticalTo(currentState, this, f);
                        currentState.recycle();
                        return true;
                    }
                    return true;
                }
                return false;
            }
        }, 2);
    }

    private void fadeProgressDark(ProgressBar progressBar, boolean z, long j) {
        startIntensityAnimation(new ValueAnimator.AnimatorUpdateListener(this, progressBar) { // from class: com.android.systemui.statusbar.notification.NotificationTemplateViewWrapper.2
            final NotificationTemplateViewWrapper this$0;
            final ProgressBar val$target;

            {
                this.this$0 = this;
                this.val$target = progressBar;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.this$0.updateProgressDark(this.val$target, ((Float) valueAnimator.getAnimatedValue()).floatValue());
            }
        }, z, j, null);
    }

    private static int interpolateColor(int i, int i2, float f) {
        int alpha = Color.alpha(i);
        int red = Color.red(i);
        int green = Color.green(i);
        int blue = Color.blue(i);
        return Color.argb((int) ((alpha * (1.0f - f)) + (Color.alpha(i2) * f)), (int) ((red * (1.0f - f)) + (Color.red(i2) * f)), (int) ((green * (1.0f - f)) + (Color.green(i2) * f)), (int) ((blue * (1.0f - f)) + (Color.blue(i2) * f)));
    }

    private void resolveTemplateViews(StatusBarNotification statusBarNotification) {
        this.mPicture = (ImageView) this.mView.findViewById(16908356);
        this.mPicture.setTag(2131886146, statusBarNotification.getNotification().getLargeIcon());
        this.mTitle = (TextView) this.mView.findViewById(16908310);
        this.mText = (TextView) this.mView.findViewById(16908413);
        View findViewById = this.mView.findViewById(16908301);
        if (findViewById instanceof ProgressBar) {
            this.mProgressBar = (ProgressBar) findViewById;
        } else {
            this.mProgressBar = null;
        }
        this.mActionsContainer = this.mView.findViewById(16909218);
    }

    private void setProgressBarDark(boolean z, boolean z2, long j) {
        if (this.mProgressBar != null) {
            if (z2) {
                fadeProgressDark(this.mProgressBar, z, j);
            } else {
                updateProgressDark(this.mProgressBar, z);
            }
        }
    }

    private void updateActionOffset() {
        if (this.mActionsContainer != null) {
            this.mActionsContainer.setTranslationY(Math.max(this.mContentHeight, this.mMinHeightHint) - this.mView.getHeight());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateProgressDark(ProgressBar progressBar, float f) {
        int interpolateColor = interpolateColor(this.mColor, -1, f);
        progressBar.getIndeterminateDrawable().mutate().setTint(interpolateColor);
        progressBar.getProgressDrawable().mutate().setTint(interpolateColor);
    }

    private void updateProgressDark(ProgressBar progressBar, boolean z) {
        updateProgressDark(progressBar, z ? 1.0f : 0.0f);
    }

    @Override // com.android.systemui.statusbar.notification.NotificationHeaderViewWrapper, com.android.systemui.statusbar.notification.NotificationViewWrapper
    public void notifyContentUpdated(StatusBarNotification statusBarNotification) {
        resolveTemplateViews(statusBarNotification);
        super.notifyContentUpdated(statusBarNotification);
    }

    @Override // com.android.systemui.statusbar.notification.NotificationViewWrapper
    public void setContentHeight(int i, int i2) {
        super.setContentHeight(i, i2);
        this.mContentHeight = i;
        this.mMinHeightHint = i2;
        updateActionOffset();
    }

    @Override // com.android.systemui.statusbar.notification.NotificationHeaderViewWrapper, com.android.systemui.statusbar.notification.NotificationViewWrapper
    public void setDark(boolean z, boolean z2, long j) {
        if (z == this.mDark && this.mDarkInitialized) {
            return;
        }
        super.setDark(z, z2, j);
        setPictureGrayscale(z, z2, j);
        setProgressBarDark(z, z2, j);
    }

    protected void setPictureGrayscale(boolean z, boolean z2, long j) {
        if (this.mPicture != null) {
            if (z2) {
                fadeGrayscale(this.mPicture, z, j);
            } else {
                updateGrayscale(this.mPicture, z);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.NotificationHeaderViewWrapper
    public void updateInvertHelper() {
        super.updateInvertHelper();
        View findViewById = this.mView.findViewById(16909233);
        if (findViewById != null) {
            this.mInvertHelper.addTarget(findViewById);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.NotificationHeaderViewWrapper
    public void updateTransformedTypes() {
        super.updateTransformedTypes();
        if (this.mTitle != null) {
            this.mTransformationHelper.addTransformedView(1, this.mTitle);
        }
        if (this.mText != null) {
            this.mTransformationHelper.addTransformedView(2, this.mText);
        }
        if (this.mPicture != null) {
            this.mTransformationHelper.addTransformedView(3, this.mPicture);
        }
        if (this.mProgressBar != null) {
            this.mTransformationHelper.addTransformedView(4, this.mProgressBar);
        }
    }
}

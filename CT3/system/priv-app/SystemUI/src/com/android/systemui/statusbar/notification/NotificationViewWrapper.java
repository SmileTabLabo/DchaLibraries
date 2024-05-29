package com.android.systemui.statusbar.notification;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.ColorMatrix;
import android.service.notification.StatusBarNotification;
import android.view.NotificationHeaderView;
import android.view.View;
import com.android.systemui.Interpolators;
import com.android.systemui.statusbar.CrossFadeHelper;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.TransformableView;
/* loaded from: a.zip:com/android/systemui/statusbar/notification/NotificationViewWrapper.class */
public abstract class NotificationViewWrapper implements TransformableView {
    protected boolean mDark;
    protected final ExpandableNotificationRow mRow;
    protected final View mView;
    protected final ColorMatrix mGrayscaleColorMatrix = new ColorMatrix();
    protected boolean mDarkInitialized = false;

    /* JADX INFO: Access modifiers changed from: protected */
    public NotificationViewWrapper(View view, ExpandableNotificationRow expandableNotificationRow) {
        this.mView = view;
        this.mRow = expandableNotificationRow;
    }

    public static NotificationViewWrapper wrap(Context context, View view, ExpandableNotificationRow expandableNotificationRow) {
        return view.getId() == 16909232 ? "bigPicture".equals(view.getTag()) ? new NotificationBigPictureTemplateViewWrapper(context, view, expandableNotificationRow) : "bigText".equals(view.getTag()) ? new NotificationBigTextTemplateViewWrapper(context, view, expandableNotificationRow) : ("media".equals(view.getTag()) || "bigMediaNarrow".equals(view.getTag())) ? new NotificationMediaTemplateViewWrapper(context, view, expandableNotificationRow) : "messaging".equals(view.getTag()) ? new NotificationMessagingTemplateViewWrapper(context, view, expandableNotificationRow) : new NotificationTemplateViewWrapper(context, view, expandableNotificationRow) : view instanceof NotificationHeaderView ? new NotificationHeaderViewWrapper(context, view, expandableNotificationRow) : new NotificationCustomViewWrapper(view, expandableNotificationRow);
    }

    @Override // com.android.systemui.statusbar.TransformableView
    public TransformState getCurrentState(int i) {
        return null;
    }

    public int getCustomBackgroundColor() {
        return 0;
    }

    public NotificationHeaderView getNotificationHeader() {
        return null;
    }

    public void notifyContentUpdated(StatusBarNotification statusBarNotification) {
        this.mDarkInitialized = false;
    }

    public void setContentHeight(int i, int i2) {
    }

    public void setDark(boolean z, boolean z2, long j) {
        this.mDark = z;
        this.mDarkInitialized = true;
    }

    public void setShowingLegacyBackground(boolean z) {
    }

    @Override // com.android.systemui.statusbar.TransformableView
    public void setVisible(boolean z) {
        this.mView.animate().cancel();
        this.mView.setVisibility(z ? 0 : 4);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void startIntensityAnimation(ValueAnimator.AnimatorUpdateListener animatorUpdateListener, boolean z, long j, Animator.AnimatorListener animatorListener) {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(z ? 0.0f : 1.0f, z ? 1.0f : 0.0f);
        ofFloat.addUpdateListener(animatorUpdateListener);
        ofFloat.setDuration(700L);
        ofFloat.setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
        ofFloat.setStartDelay(j);
        if (animatorListener != null) {
            ofFloat.addListener(animatorListener);
        }
        ofFloat.start();
    }

    @Override // com.android.systemui.statusbar.TransformableView
    public void transformFrom(TransformableView transformableView) {
        CrossFadeHelper.fadeIn(this.mView);
    }

    @Override // com.android.systemui.statusbar.TransformableView
    public void transformFrom(TransformableView transformableView, float f) {
        CrossFadeHelper.fadeIn(this.mView, f);
    }

    @Override // com.android.systemui.statusbar.TransformableView
    public void transformTo(TransformableView transformableView, float f) {
        CrossFadeHelper.fadeOut(this.mView, f);
    }

    @Override // com.android.systemui.statusbar.TransformableView
    public void transformTo(TransformableView transformableView, Runnable runnable) {
        CrossFadeHelper.fadeOut(this.mView, runnable);
    }

    public void updateExpandability(boolean z, View.OnClickListener onClickListener) {
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void updateGrayscaleMatrix(float f) {
        this.mGrayscaleColorMatrix.setSaturation(1.0f - f);
    }
}

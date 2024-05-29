package com.android.systemui.statusbar.notification;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.service.notification.StatusBarNotification;
import android.support.v4.graphics.ColorUtils;
import android.view.View;
import com.android.systemui.ViewInvertHelper;
import com.android.systemui.statusbar.ExpandableNotificationRow;
/* loaded from: a.zip:com/android/systemui/statusbar/notification/NotificationCustomViewWrapper.class */
public class NotificationCustomViewWrapper extends NotificationViewWrapper {
    private int mBackgroundColor;
    private final Paint mGreyPaint;
    private final ViewInvertHelper mInvertHelper;
    private boolean mShouldInvertDark;
    private boolean mShowingLegacyBackground;

    /* JADX INFO: Access modifiers changed from: protected */
    public NotificationCustomViewWrapper(View view, ExpandableNotificationRow expandableNotificationRow) {
        super(view, expandableNotificationRow);
        this.mGreyPaint = new Paint();
        this.mBackgroundColor = 0;
        this.mInvertHelper = new ViewInvertHelper(view, 700L);
    }

    private boolean isColorLight(int i) {
        boolean z = true;
        if (Color.alpha(i) != 0) {
            z = ColorUtils.calculateLuminance(i) > 0.5d;
        }
        return z;
    }

    protected void fadeGrayscale(boolean z, long j) {
        startIntensityAnimation(new ValueAnimator.AnimatorUpdateListener(this) { // from class: com.android.systemui.statusbar.notification.NotificationCustomViewWrapper.1
            final NotificationCustomViewWrapper this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.this$0.updateGrayscaleMatrix(((Float) valueAnimator.getAnimatedValue()).floatValue());
                this.this$0.mGreyPaint.setColorFilter(new ColorMatrixColorFilter(this.this$0.mGrayscaleColorMatrix));
                this.this$0.mView.setLayerPaint(this.this$0.mGreyPaint);
            }
        }, z, j, new AnimatorListenerAdapter(this, z) { // from class: com.android.systemui.statusbar.notification.NotificationCustomViewWrapper.2
            final NotificationCustomViewWrapper this$0;
            final boolean val$dark;

            {
                this.this$0 = this;
                this.val$dark = z;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (this.val$dark) {
                    return;
                }
                this.this$0.mView.setLayerType(0, null);
            }
        });
    }

    @Override // com.android.systemui.statusbar.notification.NotificationViewWrapper
    public int getCustomBackgroundColor() {
        return this.mRow.isSummaryWithChildren() ? 0 : this.mBackgroundColor;
    }

    @Override // com.android.systemui.statusbar.notification.NotificationViewWrapper
    public void notifyContentUpdated(StatusBarNotification statusBarNotification) {
        super.notifyContentUpdated(statusBarNotification);
        Drawable background = this.mView.getBackground();
        this.mBackgroundColor = 0;
        if (background instanceof ColorDrawable) {
            this.mBackgroundColor = ((ColorDrawable) background).getColor();
            this.mView.setBackground(null);
            this.mView.setTag(2131886140, Integer.valueOf(this.mBackgroundColor));
        } else if (this.mView.getTag(2131886140) != null) {
            this.mBackgroundColor = ((Integer) this.mView.getTag(2131886140)).intValue();
        }
        this.mShouldInvertDark = this.mBackgroundColor != 0 ? isColorLight(this.mBackgroundColor) : true;
    }

    @Override // com.android.systemui.statusbar.notification.NotificationViewWrapper
    public void setDark(boolean z, boolean z2, long j) {
        if (z == this.mDark && this.mDarkInitialized) {
            return;
        }
        super.setDark(z, z2, j);
        if (!this.mShowingLegacyBackground && this.mShouldInvertDark) {
            if (z2) {
                this.mInvertHelper.fade(z, j);
                return;
            } else {
                this.mInvertHelper.update(z);
                return;
            }
        }
        this.mView.setLayerType(z ? 2 : 0, null);
        if (z2) {
            fadeGrayscale(z, j);
        } else {
            updateGrayscale(z);
        }
    }

    @Override // com.android.systemui.statusbar.notification.NotificationViewWrapper
    public void setShowingLegacyBackground(boolean z) {
        super.setShowingLegacyBackground(z);
        this.mShowingLegacyBackground = z;
    }

    @Override // com.android.systemui.statusbar.notification.NotificationViewWrapper, com.android.systemui.statusbar.TransformableView
    public void setVisible(boolean z) {
        super.setVisible(z);
        this.mView.setAlpha(z ? 1.0f : 0.0f);
    }

    protected void updateGrayscale(boolean z) {
        if (z) {
            updateGrayscaleMatrix(1.0f);
            this.mGreyPaint.setColorFilter(new ColorMatrixColorFilter(this.mGrayscaleColorMatrix));
            this.mView.setLayerPaint(this.mGreyPaint);
        }
    }
}

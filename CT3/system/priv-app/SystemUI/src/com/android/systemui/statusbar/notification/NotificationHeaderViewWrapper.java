package com.android.systemui.statusbar.notification;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.service.notification.StatusBarNotification;
import android.util.ArraySet;
import android.view.NotificationHeaderView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.android.systemui.ViewInvertHelper;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.TransformableView;
import com.android.systemui.statusbar.ViewTransformationHelper;
import java.util.Stack;
/* loaded from: a.zip:com/android/systemui/statusbar/notification/NotificationHeaderViewWrapper.class */
public class NotificationHeaderViewWrapper extends NotificationViewWrapper {
    protected int mColor;
    private ImageView mExpandButton;
    private ImageView mIcon;
    private final PorterDuffColorFilter mIconColorFilter;
    private final int mIconDarkAlpha;
    private final int mIconDarkColor;
    protected final ViewInvertHelper mInvertHelper;
    private NotificationHeaderView mNotificationHeader;
    protected final ViewTransformationHelper mTransformationHelper;

    /* JADX INFO: Access modifiers changed from: protected */
    public NotificationHeaderViewWrapper(Context context, View view, ExpandableNotificationRow expandableNotificationRow) {
        super(view, expandableNotificationRow);
        this.mIconColorFilter = new PorterDuffColorFilter(0, PorterDuff.Mode.SRC_ATOP);
        this.mIconDarkColor = -1;
        this.mIconDarkAlpha = context.getResources().getInteger(2131755083);
        this.mInvertHelper = new ViewInvertHelper(context, 700L);
        this.mTransformationHelper = new ViewTransformationHelper();
        resolveHeaderViews();
        updateInvertHelper();
    }

    private void addRemainingTransformTypes() {
        this.mTransformationHelper.addRemainingTransformTypes(this.mView);
    }

    private void fadeIconAlpha(ImageView imageView, boolean z, long j) {
        startIntensityAnimation(new ValueAnimator.AnimatorUpdateListener(this, imageView) { // from class: com.android.systemui.statusbar.notification.NotificationHeaderViewWrapper.2
            final NotificationHeaderViewWrapper this$0;
            final ImageView val$target;

            {
                this.this$0 = this;
                this.val$target = imageView;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                this.val$target.setImageAlpha((int) (((1.0f - floatValue) * 255.0f) + (this.this$0.mIconDarkAlpha * floatValue)));
            }
        }, z, j, null);
    }

    private void fadeIconColorFilter(ImageView imageView, boolean z, long j) {
        startIntensityAnimation(new ValueAnimator.AnimatorUpdateListener(this, imageView) { // from class: com.android.systemui.statusbar.notification.NotificationHeaderViewWrapper.1
            final NotificationHeaderViewWrapper this$0;
            final ImageView val$target;

            {
                this.this$0 = this;
                this.val$target = imageView;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.this$0.updateIconColorFilter(this.val$target, ((Float) valueAnimator.getAnimatedValue()).floatValue());
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

    private int resolveColor(ImageView imageView) {
        if (imageView == null || imageView.getDrawable() == null) {
            return 0;
        }
        ColorFilter colorFilter = imageView.getDrawable().getColorFilter();
        if (colorFilter instanceof PorterDuffColorFilter) {
            return ((PorterDuffColorFilter) colorFilter).getColor();
        }
        return 0;
    }

    private void updateCropToPaddingForImageViews() {
        Stack stack = new Stack();
        stack.push(this.mView);
        while (!stack.isEmpty()) {
            View view = (View) stack.pop();
            if (view instanceof ImageView) {
                ((ImageView) view).setCropToPadding(true);
            } else if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                for (int i = 0; i < viewGroup.getChildCount(); i++) {
                    stack.push(viewGroup.getChildAt(i));
                }
            }
        }
    }

    private void updateIconAlpha(ImageView imageView, boolean z) {
        imageView.setImageAlpha(z ? this.mIconDarkAlpha : 255);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateIconColorFilter(ImageView imageView, float f) {
        this.mIconColorFilter.setColor(interpolateColor(this.mColor, -1, f));
        Drawable drawable = imageView.getDrawable();
        if (drawable != null) {
            drawable.mutate().setColorFilter(this.mIconColorFilter);
        }
    }

    private void updateIconColorFilter(ImageView imageView, boolean z) {
        updateIconColorFilter(imageView, z ? 1.0f : 0.0f);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void fadeGrayscale(ImageView imageView, boolean z, long j) {
        startIntensityAnimation(new ValueAnimator.AnimatorUpdateListener(this, imageView) { // from class: com.android.systemui.statusbar.notification.NotificationHeaderViewWrapper.3
            final NotificationHeaderViewWrapper this$0;
            final ImageView val$target;

            {
                this.this$0 = this;
                this.val$target = imageView;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.this$0.updateGrayscaleMatrix(((Float) valueAnimator.getAnimatedValue()).floatValue());
                this.val$target.setColorFilter(new ColorMatrixColorFilter(this.this$0.mGrayscaleColorMatrix));
            }
        }, z, j, new AnimatorListenerAdapter(this, z, imageView) { // from class: com.android.systemui.statusbar.notification.NotificationHeaderViewWrapper.4
            final NotificationHeaderViewWrapper this$0;
            final boolean val$dark;
            final ImageView val$target;

            {
                this.this$0 = this;
                this.val$dark = z;
                this.val$target = imageView;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (this.val$dark) {
                    return;
                }
                this.val$target.setColorFilter((ColorFilter) null);
            }
        });
    }

    @Override // com.android.systemui.statusbar.notification.NotificationViewWrapper, com.android.systemui.statusbar.TransformableView
    public TransformState getCurrentState(int i) {
        return this.mTransformationHelper.getCurrentState(i);
    }

    @Override // com.android.systemui.statusbar.notification.NotificationViewWrapper
    public NotificationHeaderView getNotificationHeader() {
        return this.mNotificationHeader;
    }

    @Override // com.android.systemui.statusbar.notification.NotificationViewWrapper
    public void notifyContentUpdated(StatusBarNotification statusBarNotification) {
        super.notifyContentUpdated(statusBarNotification);
        ArraySet<View> allTransformingViews = this.mTransformationHelper.getAllTransformingViews();
        resolveHeaderViews();
        updateInvertHelper();
        updateTransformedTypes();
        addRemainingTransformTypes();
        updateCropToPaddingForImageViews();
        ArraySet<View> allTransformingViews2 = this.mTransformationHelper.getAllTransformingViews();
        for (int i = 0; i < allTransformingViews.size(); i++) {
            View valueAt = allTransformingViews.valueAt(i);
            if (!allTransformingViews2.contains(valueAt)) {
                this.mTransformationHelper.resetTransformedView(valueAt);
            }
        }
    }

    protected void resolveHeaderViews() {
        this.mIcon = (ImageView) this.mView.findViewById(16908294);
        this.mExpandButton = (ImageView) this.mView.findViewById(16909230);
        this.mColor = resolveColor(this.mExpandButton);
        this.mNotificationHeader = this.mView.findViewById(16909224);
    }

    @Override // com.android.systemui.statusbar.notification.NotificationViewWrapper
    public void setDark(boolean z, boolean z2, long j) {
        if (z == this.mDark && this.mDarkInitialized) {
            return;
        }
        super.setDark(z, z2, j);
        if (z2) {
            this.mInvertHelper.fade(z, j);
        } else {
            this.mInvertHelper.update(z);
        }
        if (this.mIcon == null || this.mRow.isChildInGroup()) {
            return;
        }
        boolean z3 = this.mNotificationHeader.getOriginalIconColor() != -1;
        if (z2) {
            if (!z3) {
                fadeGrayscale(this.mIcon, z, j);
                return;
            }
            fadeIconColorFilter(this.mIcon, z, j);
            fadeIconAlpha(this.mIcon, z, j);
        } else if (!z3) {
            updateGrayscale(this.mIcon, z);
        } else {
            updateIconColorFilter(this.mIcon, z);
            updateIconAlpha(this.mIcon, z);
        }
    }

    @Override // com.android.systemui.statusbar.notification.NotificationViewWrapper, com.android.systemui.statusbar.TransformableView
    public void setVisible(boolean z) {
        super.setVisible(z);
        this.mTransformationHelper.setVisible(z);
    }

    @Override // com.android.systemui.statusbar.notification.NotificationViewWrapper, com.android.systemui.statusbar.TransformableView
    public void transformFrom(TransformableView transformableView) {
        this.mTransformationHelper.transformFrom(transformableView);
    }

    @Override // com.android.systemui.statusbar.notification.NotificationViewWrapper, com.android.systemui.statusbar.TransformableView
    public void transformFrom(TransformableView transformableView, float f) {
        this.mTransformationHelper.transformFrom(transformableView, f);
    }

    @Override // com.android.systemui.statusbar.notification.NotificationViewWrapper, com.android.systemui.statusbar.TransformableView
    public void transformTo(TransformableView transformableView, float f) {
        this.mTransformationHelper.transformTo(transformableView, f);
    }

    @Override // com.android.systemui.statusbar.notification.NotificationViewWrapper, com.android.systemui.statusbar.TransformableView
    public void transformTo(TransformableView transformableView, Runnable runnable) {
        this.mTransformationHelper.transformTo(transformableView, runnable);
    }

    @Override // com.android.systemui.statusbar.notification.NotificationViewWrapper
    public void updateExpandability(boolean z, View.OnClickListener onClickListener) {
        this.mExpandButton.setVisibility(z ? 0 : 8);
        NotificationHeaderView notificationHeaderView = this.mNotificationHeader;
        if (!z) {
            onClickListener = null;
        }
        notificationHeaderView.setOnClickListener(onClickListener);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void updateGrayscale(ImageView imageView, boolean z) {
        if (!z) {
            imageView.setColorFilter((ColorFilter) null);
            return;
        }
        updateGrayscaleMatrix(1.0f);
        imageView.setColorFilter(new ColorMatrixColorFilter(this.mGrayscaleColorMatrix));
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void updateInvertHelper() {
        this.mInvertHelper.clearTargets();
        for (int i = 0; i < this.mNotificationHeader.getChildCount(); i++) {
            View childAt = this.mNotificationHeader.getChildAt(i);
            if (childAt != this.mIcon) {
                this.mInvertHelper.addTarget(childAt);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void updateTransformedTypes() {
        this.mTransformationHelper.reset();
        this.mTransformationHelper.addTransformedView(0, this.mNotificationHeader);
    }
}

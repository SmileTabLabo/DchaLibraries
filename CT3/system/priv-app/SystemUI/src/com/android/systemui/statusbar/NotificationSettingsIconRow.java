package com.android.systemui.statusbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import com.android.systemui.Interpolators;
/* loaded from: a.zip:com/android/systemui/statusbar/NotificationSettingsIconRow.class */
public class NotificationSettingsIconRow extends FrameLayout implements View.OnClickListener {
    private boolean mAnimating;
    private boolean mDismissing;
    private ValueAnimator mFadeAnimator;
    private AlphaOptimizedImageView mGearIcon;
    private int[] mGearLocation;
    private float mHorizSpaceForGear;
    private boolean mIconPlaced;
    private SettingsIconRowListener mListener;
    private boolean mOnLeft;
    private ExpandableNotificationRow mParent;
    private int[] mParentLocation;
    private boolean mSettingsFadedIn;
    private boolean mSnapping;
    private int mVertSpaceForGear;

    /* loaded from: a.zip:com/android/systemui/statusbar/NotificationSettingsIconRow$SettingsIconRowListener.class */
    public interface SettingsIconRowListener {
        void onGearTouched(ExpandableNotificationRow expandableNotificationRow, int i, int i2);

        void onSettingsIconRowReset(ExpandableNotificationRow expandableNotificationRow);
    }

    public NotificationSettingsIconRow(Context context) {
        this(context, null);
    }

    public NotificationSettingsIconRow(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public NotificationSettingsIconRow(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public NotificationSettingsIconRow(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet);
        this.mSettingsFadedIn = false;
        this.mAnimating = false;
        this.mOnLeft = true;
        this.mDismissing = false;
        this.mSnapping = false;
        this.mIconPlaced = false;
        this.mGearLocation = new int[2];
        this.mParentLocation = new int[2];
    }

    public void cancelFadeAnimator() {
        if (this.mFadeAnimator != null) {
            this.mFadeAnimator.cancel();
        }
    }

    public void fadeInSettings(boolean z, float f, float f2) {
        if (this.mDismissing || this.mAnimating) {
            return;
        }
        if (isIconLocationChange(f)) {
            setGearAlpha(0.0f);
        }
        setIconLocation(f > 0.0f);
        this.mFadeAnimator = ValueAnimator.ofFloat(this.mGearIcon.getAlpha(), 1.0f);
        this.mFadeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this, f, z, f2) { // from class: com.android.systemui.statusbar.NotificationSettingsIconRow.1
            final NotificationSettingsIconRow this$0;
            final boolean val$fromLeft;
            final float val$notiThreshold;
            final float val$transX;

            {
                this.this$0 = this;
                this.val$transX = f;
                this.val$fromLeft = z;
                this.val$notiThreshold = f2;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                boolean z2 = true;
                float abs = Math.abs(this.val$transX);
                if ((!this.val$fromLeft || this.val$transX > this.val$notiThreshold) && (this.val$fromLeft || abs > this.val$notiThreshold)) {
                    z2 = false;
                }
                if (!z2 || this.this$0.mSettingsFadedIn) {
                    return;
                }
                this.this$0.setGearAlpha(((Float) valueAnimator.getAnimatedValue()).floatValue());
            }
        });
        this.mFadeAnimator.addListener(new AnimatorListenerAdapter(this) { // from class: com.android.systemui.statusbar.NotificationSettingsIconRow.2
            final NotificationSettingsIconRow this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
                this.this$0.mGearIcon.setAlpha(0.0f);
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                boolean z2 = false;
                this.this$0.mAnimating = false;
                NotificationSettingsIconRow notificationSettingsIconRow = this.this$0;
                if (this.this$0.mGearIcon.getAlpha() == 1.0f) {
                    z2 = true;
                }
                notificationSettingsIconRow.mSettingsFadedIn = z2;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                this.this$0.mAnimating = true;
            }
        });
        this.mFadeAnimator.setInterpolator(Interpolators.ALPHA_IN);
        this.mFadeAnimator.setDuration(200L);
        this.mFadeAnimator.start();
    }

    public float getSpaceForGear() {
        return this.mHorizSpaceForGear;
    }

    public boolean isIconLocationChange(float f) {
        boolean z = f > ((float) this.mGearIcon.getPaddingStart());
        boolean z2 = f < ((float) (-this.mGearIcon.getPaddingStart()));
        if (this.mOnLeft && z2) {
            return true;
        }
        return !this.mOnLeft && z;
    }

    public boolean isIconOnLeft() {
        return this.mOnLeft;
    }

    public boolean isVisible() {
        return this.mGearIcon.getAlpha() > 0.0f;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (view.getId() != 2131886562 || this.mListener == null) {
            return;
        }
        this.mGearIcon.getLocationOnScreen(this.mGearLocation);
        this.mParent.getLocationOnScreen(this.mParentLocation);
        int i = (int) (this.mHorizSpaceForGear / 2.0f);
        int translationY = ((int) ((this.mGearIcon.getTranslationY() * 2.0f) + this.mGearIcon.getHeight())) / 2;
        this.mListener.onGearTouched(this.mParent, (this.mGearLocation[0] - this.mParentLocation[0]) + i, (this.mGearLocation[1] - this.mParentLocation[1]) + translationY);
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mGearIcon = (AlphaOptimizedImageView) findViewById(2131886562);
        this.mGearIcon.setOnClickListener(this);
        setOnClickListener(this);
        this.mHorizSpaceForGear = getResources().getDimensionPixelOffset(2131689792);
        this.mVertSpaceForGear = getResources().getDimensionPixelOffset(2131689785);
        resetState();
    }

    @Override // android.view.View
    public void onRtlPropertiesChanged(int i) {
        setIconLocation(this.mOnLeft);
    }

    public void resetState() {
        setGearAlpha(0.0f);
        this.mIconPlaced = false;
        this.mSettingsFadedIn = false;
        this.mAnimating = false;
        this.mSnapping = false;
        this.mDismissing = false;
        setIconLocation(true);
        if (this.mListener != null) {
            this.mListener.onSettingsIconRowReset(this.mParent);
        }
    }

    public void setAppName(String str) {
        this.mGearIcon.setContentDescription(String.format(getResources().getString(2131493778), str));
    }

    public void setGearAlpha(float f) {
        if (f == 0.0f) {
            this.mSettingsFadedIn = false;
            setVisibility(4);
        } else {
            setVisibility(0);
        }
        this.mGearIcon.setAlpha(f);
    }

    public void setGearListener(SettingsIconRowListener settingsIconRowListener) {
        this.mListener = settingsIconRowListener;
    }

    public void setIconLocation(boolean z) {
        float f = 0.0f;
        if ((this.mIconPlaced && z == this.mOnLeft) || this.mSnapping || this.mParent == null || this.mGearIcon.getWidth() == 0) {
            return;
        }
        boolean isLayoutRtl = this.mParent.isLayoutRtl();
        float f2 = isLayoutRtl ? -(this.mParent.getWidth() - this.mHorizSpaceForGear) : 0.0f;
        if (!isLayoutRtl) {
            f = this.mParent.getWidth() - this.mHorizSpaceForGear;
        }
        float width = (this.mHorizSpaceForGear - this.mGearIcon.getWidth()) / 2.0f;
        setTranslationX(z ? f2 + width : f + width);
        this.mOnLeft = z;
        this.mIconPlaced = true;
    }

    public void setNotificationRowParent(ExpandableNotificationRow expandableNotificationRow) {
        this.mParent = expandableNotificationRow;
        setIconLocation(this.mOnLeft);
    }

    public void setSnapping(boolean z) {
        this.mSnapping = z;
    }

    public void updateSettingsIcons(float f, float f2) {
        if (this.mAnimating || !this.mSettingsFadedIn) {
            return;
        }
        float f3 = f2 * 0.3f;
        float abs = Math.abs(f);
        setGearAlpha(abs == 0.0f ? 0.0f : abs <= f3 ? 1.0f : 1.0f - ((abs - f3) / (f2 - f3)));
    }

    public void updateVerticalLocation() {
        if (this.mParent == null) {
            return;
        }
        int collapsedHeight = this.mParent.getCollapsedHeight();
        if (collapsedHeight < this.mVertSpaceForGear) {
            this.mGearIcon.setTranslationY((collapsedHeight / 2) - (this.mGearIcon.getHeight() / 2));
        } else {
            this.mGearIcon.setTranslationY((this.mVertSpaceForGear - this.mGearIcon.getHeight()) / 2);
        }
    }
}

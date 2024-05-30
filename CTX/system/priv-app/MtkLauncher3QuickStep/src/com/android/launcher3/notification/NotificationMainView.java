package com.android.launcher3.notification;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.RippleDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.FloatProperty;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.anim.AnimationSuccessListener;
import com.android.launcher3.anim.Interpolators;
import com.android.launcher3.touch.OverScroll;
import com.android.launcher3.touch.SwipeDetector;
import com.android.launcher3.util.Themes;
@TargetApi(24)
/* loaded from: classes.dex */
public class NotificationMainView extends FrameLayout implements SwipeDetector.Listener {
    private static FloatProperty<NotificationMainView> CONTENT_TRANSLATION = new FloatProperty<NotificationMainView>("contentTranslation") { // from class: com.android.launcher3.notification.NotificationMainView.1
        @Override // android.util.FloatProperty
        public void setValue(NotificationMainView notificationMainView, float f) {
            notificationMainView.setContentTranslation(f);
        }

        @Override // android.util.Property
        public Float get(NotificationMainView notificationMainView) {
            return Float.valueOf(notificationMainView.mTextAndBackground.getTranslationX());
        }
    };
    public static final ItemInfo NOTIFICATION_ITEM_INFO = new ItemInfo();
    private int mBackgroundColor;
    private final ObjectAnimator mContentTranslateAnimator;
    private View mIconView;
    private NotificationInfo mNotificationInfo;
    private SwipeDetector mSwipeDetector;
    private ViewGroup mTextAndBackground;
    private TextView mTextView;
    private TextView mTitleView;

    public NotificationMainView(Context context) {
        this(context, null, 0);
    }

    public NotificationMainView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public NotificationMainView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mContentTranslateAnimator = ObjectAnimator.ofFloat(this, CONTENT_TRANSLATION, 0.0f);
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mTextAndBackground = (ViewGroup) findViewById(R.id.text_and_background);
        ColorDrawable colorDrawable = (ColorDrawable) this.mTextAndBackground.getBackground();
        this.mBackgroundColor = colorDrawable.getColor();
        this.mTextAndBackground.setBackground(new RippleDrawable(ColorStateList.valueOf(Themes.getAttrColor(getContext(), 16843820)), colorDrawable, null));
        this.mTitleView = (TextView) this.mTextAndBackground.findViewById(R.id.title);
        this.mTextView = (TextView) this.mTextAndBackground.findViewById(R.id.text);
        this.mIconView = findViewById(R.id.popup_item_icon);
    }

    public void setSwipeDetector(SwipeDetector swipeDetector) {
        this.mSwipeDetector = swipeDetector;
    }

    public void applyNotificationInfo(NotificationInfo notificationInfo, boolean z) {
        this.mNotificationInfo = notificationInfo;
        CharSequence charSequence = this.mNotificationInfo.title;
        CharSequence charSequence2 = this.mNotificationInfo.text;
        if (!TextUtils.isEmpty(charSequence) && !TextUtils.isEmpty(charSequence2)) {
            this.mTitleView.setText(charSequence.toString());
            this.mTextView.setText(charSequence2.toString());
        } else {
            this.mTitleView.setMaxLines(2);
            this.mTitleView.setText(TextUtils.isEmpty(charSequence) ? charSequence2.toString() : charSequence.toString());
            this.mTextView.setVisibility(8);
        }
        this.mIconView.setBackground(this.mNotificationInfo.getIconForBackground(getContext(), this.mBackgroundColor));
        if (this.mNotificationInfo.intent != null) {
            setOnClickListener(this.mNotificationInfo);
        }
        setContentTranslation(0.0f);
        setTag(NOTIFICATION_ITEM_INFO);
        if (z) {
            ObjectAnimator.ofFloat(this.mTextAndBackground, ALPHA, 0.0f, 1.0f).setDuration(150L).start();
        }
    }

    public void setContentTranslation(float f) {
        this.mTextAndBackground.setTranslationX(f);
        this.mIconView.setTranslationX(f);
    }

    public void setContentVisibility(int i) {
        this.mTextAndBackground.setVisibility(i);
        this.mIconView.setVisibility(i);
    }

    public NotificationInfo getNotificationInfo() {
        return this.mNotificationInfo;
    }

    public boolean canChildBeDismissed() {
        return this.mNotificationInfo != null && this.mNotificationInfo.dismissable;
    }

    public void onChildDismissed() {
        Launcher launcher = Launcher.getLauncher(getContext());
        launcher.getPopupDataProvider().cancelNotification(this.mNotificationInfo.notificationKey);
        launcher.getUserEventDispatcher().logActionOnItem(3, 4, 8);
    }

    @Override // com.android.launcher3.touch.SwipeDetector.Listener
    public void onDragStart(boolean z) {
    }

    @Override // com.android.launcher3.touch.SwipeDetector.Listener
    public boolean onDrag(float f, float f2) {
        if (!canChildBeDismissed()) {
            f = OverScroll.dampedScroll(f, getWidth());
        }
        setContentTranslation(f);
        this.mContentTranslateAnimator.cancel();
        return true;
    }

    @Override // com.android.launcher3.touch.SwipeDetector.Listener
    public void onDragEnd(float f, boolean z) {
        final boolean z2;
        float translationX = this.mTextAndBackground.getTranslationX();
        float f2 = 0.0f;
        if (canChildBeDismissed()) {
            if (z) {
                f2 = f < 0.0f ? -getWidth() : getWidth();
            } else if (Math.abs(translationX) > getWidth() / 2) {
                f2 = translationX < 0.0f ? -getWidth() : getWidth();
            }
            z2 = true;
            long calculateDuration = SwipeDetector.calculateDuration(f, (f2 - translationX) / getWidth());
            this.mContentTranslateAnimator.removeAllListeners();
            this.mContentTranslateAnimator.setDuration(calculateDuration).setInterpolator(Interpolators.scrollInterpolatorForVelocity(f));
            this.mContentTranslateAnimator.setFloatValues(translationX, f2);
            this.mContentTranslateAnimator.addListener(new AnimationSuccessListener() { // from class: com.android.launcher3.notification.NotificationMainView.2
                @Override // com.android.launcher3.anim.AnimationSuccessListener
                public void onAnimationSuccess(Animator animator) {
                    NotificationMainView.this.mSwipeDetector.finishedScrolling();
                    if (z2) {
                        NotificationMainView.this.onChildDismissed();
                    }
                }
            });
            this.mContentTranslateAnimator.start();
        }
        z2 = false;
        long calculateDuration2 = SwipeDetector.calculateDuration(f, (f2 - translationX) / getWidth());
        this.mContentTranslateAnimator.removeAllListeners();
        this.mContentTranslateAnimator.setDuration(calculateDuration2).setInterpolator(Interpolators.scrollInterpolatorForVelocity(f));
        this.mContentTranslateAnimator.setFloatValues(translationX, f2);
        this.mContentTranslateAnimator.addListener(new AnimationSuccessListener() { // from class: com.android.launcher3.notification.NotificationMainView.2
            @Override // com.android.launcher3.anim.AnimationSuccessListener
            public void onAnimationSuccess(Animator animator) {
                NotificationMainView.this.mSwipeDetector.finishedScrolling();
                if (z2) {
                    NotificationMainView.this.onChildDismissed();
                }
            }
        });
        this.mContentTranslateAnimator.start();
    }
}

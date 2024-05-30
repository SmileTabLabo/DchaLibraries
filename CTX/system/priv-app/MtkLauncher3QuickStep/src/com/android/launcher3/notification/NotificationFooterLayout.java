package com.android.launcher3.notification;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.android.launcher3.LauncherAnimUtils;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.anim.PropertyListBuilder;
import com.android.launcher3.anim.PropertyResetListener;
import com.android.launcher3.util.Themes;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
/* loaded from: classes.dex */
public class NotificationFooterLayout extends FrameLayout {
    private static final int MAX_FOOTER_NOTIFICATIONS = 5;
    private static final Rect sTempRect = new Rect();
    private final int mBackgroundColor;
    private NotificationItemView mContainer;
    FrameLayout.LayoutParams mIconLayoutParams;
    private LinearLayout mIconRow;
    private final List<NotificationInfo> mNotifications;
    private View mOverflowEllipsis;
    private final List<NotificationInfo> mOverflowNotifications;
    private final boolean mRtl;

    /* loaded from: classes.dex */
    public interface IconAnimationEndListener {
        void onIconAnimationEnd(NotificationInfo notificationInfo);
    }

    public NotificationFooterLayout(Context context) {
        this(context, null, 0);
    }

    public NotificationFooterLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public NotificationFooterLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mNotifications = new ArrayList();
        this.mOverflowNotifications = new ArrayList();
        Resources resources = getResources();
        this.mRtl = Utilities.isRtl(resources);
        int dimensionPixelSize = resources.getDimensionPixelSize(R.dimen.notification_footer_icon_size);
        this.mIconLayoutParams = new FrameLayout.LayoutParams(dimensionPixelSize, dimensionPixelSize);
        this.mIconLayoutParams.gravity = 16;
        this.mIconLayoutParams.setMarginStart((((resources.getDimensionPixelSize(R.dimen.bg_popup_item_width) - resources.getDimensionPixelSize(R.dimen.notification_footer_icon_row_padding)) - (resources.getDimensionPixelSize(R.dimen.horizontal_ellipsis_offset) + resources.getDimensionPixelSize(R.dimen.horizontal_ellipsis_size))) - (dimensionPixelSize * 5)) / 5);
        this.mBackgroundColor = Themes.getAttrColor(context, R.attr.popupColorPrimary);
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mOverflowEllipsis = findViewById(R.id.overflow);
        this.mIconRow = (LinearLayout) findViewById(R.id.icon_row);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setContainer(NotificationItemView notificationItemView) {
        this.mContainer = notificationItemView;
    }

    public void addNotificationInfo(NotificationInfo notificationInfo) {
        if (this.mNotifications.size() < 5) {
            this.mNotifications.add(notificationInfo);
        } else {
            this.mOverflowNotifications.add(notificationInfo);
        }
    }

    public void commitNotificationInfos() {
        this.mIconRow.removeAllViews();
        for (int i = 0; i < this.mNotifications.size(); i++) {
            addNotificationIconForInfo(this.mNotifications.get(i));
        }
        updateOverflowEllipsisVisibility();
    }

    private void updateOverflowEllipsisVisibility() {
        this.mOverflowEllipsis.setVisibility(this.mOverflowNotifications.isEmpty() ? 8 : 0);
    }

    private View addNotificationIconForInfo(NotificationInfo notificationInfo) {
        View view = new View(getContext());
        view.setBackground(notificationInfo.getIconForBackground(getContext(), this.mBackgroundColor));
        view.setOnClickListener(notificationInfo);
        view.setTag(notificationInfo);
        view.setImportantForAccessibility(2);
        this.mIconRow.addView(view, 0, this.mIconLayoutParams);
        return view;
    }

    public void animateFirstNotificationTo(Rect rect, final IconAnimationEndListener iconAnimationEndListener) {
        Rect rect2;
        AnimatorSet createAnimatorSet = LauncherAnimUtils.createAnimatorSet();
        final View childAt = this.mIconRow.getChildAt(this.mIconRow.getChildCount() - 1);
        childAt.getGlobalVisibleRect(sTempRect);
        float height = rect.height() / rect2.height();
        ObjectAnimator ofPropertyValuesHolder = LauncherAnimUtils.ofPropertyValuesHolder(childAt, new PropertyListBuilder().scale(height).translationY((rect.top - rect2.top) + (((rect2.height() * height) - rect2.height()) / 2.0f)).build());
        ofPropertyValuesHolder.addListener(new AnimatorListenerAdapter() { // from class: com.android.launcher3.notification.NotificationFooterLayout.1
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                iconAnimationEndListener.onIconAnimationEnd((NotificationInfo) childAt.getTag());
                NotificationFooterLayout.this.removeViewFromIconRow(childAt);
            }
        });
        createAnimatorSet.play(ofPropertyValuesHolder);
        int marginStart = this.mIconLayoutParams.width + this.mIconLayoutParams.getMarginStart();
        if (this.mRtl) {
            marginStart = -marginStart;
        }
        if (!this.mOverflowNotifications.isEmpty()) {
            NotificationInfo remove = this.mOverflowNotifications.remove(0);
            this.mNotifications.add(remove);
            createAnimatorSet.play(ObjectAnimator.ofFloat(addNotificationIconForInfo(remove), ALPHA, 0.0f, 1.0f));
        }
        int childCount = this.mIconRow.getChildCount() - 1;
        PropertyResetListener propertyResetListener = new PropertyResetListener(TRANSLATION_X, Float.valueOf(0.0f));
        for (int i = 0; i < childCount; i++) {
            ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this.mIconRow.getChildAt(i), TRANSLATION_X, marginStart);
            ofFloat.addListener(propertyResetListener);
            createAnimatorSet.play(ofFloat);
        }
        createAnimatorSet.start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void removeViewFromIconRow(View view) {
        this.mIconRow.removeView(view);
        this.mNotifications.remove(view.getTag());
        updateOverflowEllipsisVisibility();
        if (this.mIconRow.getChildCount() == 0 && this.mContainer != null) {
            this.mContainer.removeFooter();
        }
    }

    public void trimNotifications(List<String> list) {
        if (!isAttachedToWindow() || this.mIconRow.getChildCount() == 0) {
            return;
        }
        Iterator<NotificationInfo> it = this.mOverflowNotifications.iterator();
        while (it.hasNext()) {
            if (!list.contains(it.next().notificationKey)) {
                it.remove();
            }
        }
        for (int childCount = this.mIconRow.getChildCount() - 1; childCount >= 0; childCount--) {
            View childAt = this.mIconRow.getChildAt(childCount);
            if (!list.contains(((NotificationInfo) childAt.getTag()).notificationKey)) {
                removeViewFromIconRow(childAt);
            }
        }
    }
}

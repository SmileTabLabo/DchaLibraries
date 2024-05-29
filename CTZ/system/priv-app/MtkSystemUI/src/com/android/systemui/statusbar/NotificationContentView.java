package com.android.systemui.statusbar;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.Context;
import android.graphics.Rect;
import android.service.notification.StatusBarNotification;
import android.util.ArraySet;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.NotificationHeaderView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.NotificationColorUtil;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.notification.HybridGroupManager;
import com.android.systemui.statusbar.notification.HybridNotificationView;
import com.android.systemui.statusbar.notification.NotificationCustomViewWrapper;
import com.android.systemui.statusbar.notification.NotificationUtils;
import com.android.systemui.statusbar.notification.NotificationViewWrapper;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.policy.RemoteInputView;
import com.android.systemui.statusbar.policy.SmartReplyConstants;
import com.android.systemui.statusbar.policy.SmartReplyView;
import java.util.function.Consumer;
/* loaded from: classes.dex */
public class NotificationContentView extends FrameLayout {
    private View mAmbientChild;
    private HybridNotificationView mAmbientSingleLineChild;
    private NotificationViewWrapper mAmbientWrapper;
    private boolean mAnimate;
    private int mAnimationStartVisibleType;
    private boolean mBeforeN;
    private RemoteInputView mCachedExpandedRemoteInput;
    private RemoteInputView mCachedHeadsUpRemoteInput;
    private int mClipBottomAmount;
    private final Rect mClipBounds;
    private boolean mClipToActualHeight;
    private int mClipTopAmount;
    private ExpandableNotificationRow mContainingNotification;
    private int mContentHeight;
    private int mContentHeightAtAnimationStart;
    private View mContractedChild;
    private NotificationViewWrapper mContractedWrapper;
    private boolean mDark;
    private final ViewTreeObserver.OnPreDrawListener mEnableAnimationPredrawListener;
    private View.OnClickListener mExpandClickListener;
    private boolean mExpandable;
    private View mExpandedChild;
    private RemoteInputView mExpandedRemoteInput;
    private SmartReplyView mExpandedSmartReplyView;
    private Runnable mExpandedVisibleListener;
    private NotificationViewWrapper mExpandedWrapper;
    private boolean mFocusOnVisibilityChange;
    private boolean mForceSelectNextLayout;
    private NotificationGroupManager mGroupManager;
    private boolean mHeadsUpAnimatingAway;
    private View mHeadsUpChild;
    private int mHeadsUpHeight;
    private RemoteInputView mHeadsUpRemoteInput;
    private NotificationViewWrapper mHeadsUpWrapper;
    private HybridGroupManager mHybridGroupManager;
    private boolean mIconsVisible;
    private boolean mIsChildInGroup;
    private boolean mIsContentExpandable;
    private boolean mIsHeadsUp;
    private boolean mIsLowPriority;
    private boolean mLegacy;
    private int mMinContractedHeight;
    private int mNotificationAmbientHeight;
    private int mNotificationContentMarginEnd;
    private int mNotificationMaxHeight;
    private PendingIntent mPreviousExpandedRemoteInputIntent;
    private PendingIntent mPreviousHeadsUpRemoteInputIntent;
    private RemoteInputController mRemoteInputController;
    private boolean mRemoteInputVisible;
    private HybridNotificationView mSingleLineView;
    private int mSingleLineWidthIndention;
    private int mSmallHeight;
    private SmartReplyConstants mSmartReplyConstants;
    private SmartReplyController mSmartReplyController;
    private StatusBarNotification mStatusBarNotification;
    private int mTransformationStartVisibleType;
    private int mUnrestrictedContentHeight;
    private boolean mUserExpanding;
    private int mVisibleType;

    public NotificationContentView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mClipBounds = new Rect();
        this.mVisibleType = 0;
        this.mEnableAnimationPredrawListener = new ViewTreeObserver.OnPreDrawListener() { // from class: com.android.systemui.statusbar.NotificationContentView.1
            @Override // android.view.ViewTreeObserver.OnPreDrawListener
            public boolean onPreDraw() {
                NotificationContentView.this.post(new Runnable() { // from class: com.android.systemui.statusbar.NotificationContentView.1.1
                    @Override // java.lang.Runnable
                    public void run() {
                        NotificationContentView.this.mAnimate = true;
                    }
                });
                NotificationContentView.this.getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
        };
        this.mClipToActualHeight = true;
        this.mAnimationStartVisibleType = -1;
        this.mForceSelectNextLayout = true;
        this.mContentHeightAtAnimationStart = -1;
        this.mHybridGroupManager = new HybridGroupManager(getContext(), this);
        this.mSmartReplyConstants = (SmartReplyConstants) Dependency.get(SmartReplyConstants.class);
        this.mSmartReplyController = (SmartReplyController) Dependency.get(SmartReplyController.class);
        initView();
    }

    public void initView() {
        this.mMinContractedHeight = getResources().getDimensionPixelSize(R.dimen.min_notification_layout_height);
        this.mNotificationContentMarginEnd = getResources().getDimensionPixelSize(17105199);
    }

    public void setHeights(int i, int i2, int i3, int i4) {
        this.mSmallHeight = i;
        this.mHeadsUpHeight = i2;
        this.mNotificationMaxHeight = i3;
        this.mNotificationAmbientHeight = i4;
    }

    @Override // android.widget.FrameLayout, android.view.View
    protected void onMeasure(int i, int i2) {
        int i3;
        int i4;
        boolean z;
        int i5;
        boolean z2;
        int i6;
        boolean z3;
        int makeMeasureSpec;
        int i7;
        boolean z4;
        int mode = View.MeasureSpec.getMode(i2);
        boolean z5 = true;
        boolean z6 = mode == 1073741824;
        boolean z7 = mode == Integer.MIN_VALUE;
        int i8 = 1073741823;
        int size = View.MeasureSpec.getSize(i);
        if (z6 || z7) {
            i8 = View.MeasureSpec.getSize(i2);
        }
        int i9 = i8;
        if (this.mExpandedChild != null) {
            int i10 = this.mNotificationMaxHeight;
            if (this.mExpandedSmartReplyView != null) {
                i10 += this.mExpandedSmartReplyView.getHeightUpperLimit();
            }
            int extraMeasureHeight = i10 + this.mExpandedWrapper.getExtraMeasureHeight();
            ViewGroup.LayoutParams layoutParams = this.mExpandedChild.getLayoutParams();
            if (layoutParams.height >= 0) {
                extraMeasureHeight = Math.min(extraMeasureHeight, layoutParams.height);
                z4 = true;
            } else {
                z4 = false;
            }
            measureChildWithMargins(this.mExpandedChild, i, 0, View.MeasureSpec.makeMeasureSpec(extraMeasureHeight, z4 ? 1073741824 : Integer.MIN_VALUE), 0);
            i3 = Math.max(0, this.mExpandedChild.getMeasuredHeight());
        } else {
            i3 = 0;
        }
        if (this.mContractedChild != null) {
            int i11 = this.mSmallHeight;
            ViewGroup.LayoutParams layoutParams2 = this.mContractedChild.getLayoutParams();
            if (layoutParams2.height >= 0) {
                i6 = Math.min(i11, layoutParams2.height);
                z3 = true;
            } else {
                i6 = i11;
                z3 = false;
            }
            if (shouldContractedBeFixedSize() || z3) {
                makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(i6, 1073741824);
            } else {
                makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(i6, Integer.MIN_VALUE);
            }
            int i12 = makeMeasureSpec;
            measureChildWithMargins(this.mContractedChild, i, 0, i12, 0);
            int measuredHeight = this.mContractedChild.getMeasuredHeight();
            if (measuredHeight < this.mMinContractedHeight) {
                i7 = View.MeasureSpec.makeMeasureSpec(this.mMinContractedHeight, 1073741824);
                measureChildWithMargins(this.mContractedChild, i, 0, i7, 0);
            } else {
                i7 = i12;
            }
            i3 = Math.max(i3, measuredHeight);
            if (updateContractedHeaderWidth()) {
                measureChildWithMargins(this.mContractedChild, i, 0, i7, 0);
            }
            if (this.mExpandedChild != null && this.mContractedChild.getMeasuredHeight() > this.mExpandedChild.getMeasuredHeight()) {
                measureChildWithMargins(this.mExpandedChild, i, 0, View.MeasureSpec.makeMeasureSpec(this.mContractedChild.getMeasuredHeight(), 1073741824), 0);
            }
        }
        if (this.mHeadsUpChild != null) {
            int extraMeasureHeight2 = this.mHeadsUpHeight + this.mHeadsUpWrapper.getExtraMeasureHeight();
            ViewGroup.LayoutParams layoutParams3 = this.mHeadsUpChild.getLayoutParams();
            if (layoutParams3.height >= 0) {
                extraMeasureHeight2 = Math.min(extraMeasureHeight2, layoutParams3.height);
                z2 = true;
            } else {
                z2 = false;
            }
            measureChildWithMargins(this.mHeadsUpChild, i, 0, View.MeasureSpec.makeMeasureSpec(extraMeasureHeight2, z2 ? 1073741824 : Integer.MIN_VALUE), 0);
            i3 = Math.max(i3, this.mHeadsUpChild.getMeasuredHeight());
        }
        if (this.mSingleLineView != null) {
            if (this.mSingleLineWidthIndention != 0 && View.MeasureSpec.getMode(i) != 0) {
                i5 = View.MeasureSpec.makeMeasureSpec((size - this.mSingleLineWidthIndention) + this.mSingleLineView.getPaddingEnd(), 1073741824);
            } else {
                i5 = i;
            }
            this.mSingleLineView.measure(i5, View.MeasureSpec.makeMeasureSpec(this.mNotificationMaxHeight, Integer.MIN_VALUE));
            i3 = Math.max(i3, this.mSingleLineView.getMeasuredHeight());
        }
        if (this.mAmbientChild != null) {
            int i13 = this.mNotificationAmbientHeight;
            ViewGroup.LayoutParams layoutParams4 = this.mAmbientChild.getLayoutParams();
            if (layoutParams4.height >= 0) {
                i13 = Math.min(i13, layoutParams4.height);
                z = true;
            } else {
                z = false;
            }
            View view = this.mAmbientChild;
            int makeMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(i13, z ? 1073741824 : Integer.MIN_VALUE);
            i4 = i;
            view.measure(i4, makeMeasureSpec2);
            i3 = Math.max(i3, this.mAmbientChild.getMeasuredHeight());
        } else {
            i4 = i;
        }
        if (this.mAmbientSingleLineChild != null) {
            int i14 = this.mNotificationAmbientHeight;
            ViewGroup.LayoutParams layoutParams5 = this.mAmbientSingleLineChild.getLayoutParams();
            if (layoutParams5.height >= 0) {
                i14 = Math.min(i14, layoutParams5.height);
            } else {
                z5 = false;
            }
            if (this.mSingleLineWidthIndention != 0 && View.MeasureSpec.getMode(i) != 0) {
                i4 = View.MeasureSpec.makeMeasureSpec((size - this.mSingleLineWidthIndention) + this.mAmbientSingleLineChild.getPaddingEnd(), 1073741824);
            }
            this.mAmbientSingleLineChild.measure(i4, View.MeasureSpec.makeMeasureSpec(i14, z5 ? 1073741824 : Integer.MIN_VALUE));
            i3 = Math.max(i3, this.mAmbientSingleLineChild.getMeasuredHeight());
        }
        setMeasuredDimension(size, Math.min(i3, i9));
    }

    private int getExtraRemoteInputHeight(RemoteInputView remoteInputView) {
        if (remoteInputView != null) {
            if (remoteInputView.isActive() || remoteInputView.isSending()) {
                return getResources().getDimensionPixelSize(17105198);
            }
            return 0;
        }
        return 0;
    }

    private boolean updateContractedHeaderWidth() {
        int paddingLeft;
        int paddingLeft2;
        NotificationHeaderView notificationHeader = this.mContractedWrapper.getNotificationHeader();
        if (notificationHeader != null) {
            if (this.mExpandedChild != null && this.mExpandedWrapper.getNotificationHeader() != null) {
                NotificationHeaderView notificationHeader2 = this.mExpandedWrapper.getNotificationHeader();
                int measuredWidth = notificationHeader2.getMeasuredWidth() - notificationHeader2.getPaddingEnd();
                if (measuredWidth != notificationHeader.getMeasuredWidth() - notificationHeader2.getPaddingEnd()) {
                    int measuredWidth2 = notificationHeader.getMeasuredWidth() - measuredWidth;
                    if (!notificationHeader.isLayoutRtl()) {
                        paddingLeft2 = notificationHeader.getPaddingLeft();
                    } else {
                        paddingLeft2 = measuredWidth2;
                    }
                    int paddingTop = notificationHeader.getPaddingTop();
                    if (notificationHeader.isLayoutRtl()) {
                        measuredWidth2 = notificationHeader.getPaddingLeft();
                    }
                    notificationHeader.setPadding(paddingLeft2, paddingTop, measuredWidth2, notificationHeader.getPaddingBottom());
                    notificationHeader.setShowWorkBadgeAtEnd(true);
                    return true;
                }
            } else {
                int i = this.mNotificationContentMarginEnd;
                if (notificationHeader.getPaddingEnd() != i) {
                    if (!notificationHeader.isLayoutRtl()) {
                        paddingLeft = notificationHeader.getPaddingLeft();
                    } else {
                        paddingLeft = i;
                    }
                    int paddingTop2 = notificationHeader.getPaddingTop();
                    if (notificationHeader.isLayoutRtl()) {
                        i = notificationHeader.getPaddingLeft();
                    }
                    notificationHeader.setPadding(paddingLeft, paddingTop2, i, notificationHeader.getPaddingBottom());
                    notificationHeader.setShowWorkBadgeAtEnd(false);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean shouldContractedBeFixedSize() {
        return this.mBeforeN && (this.mContractedWrapper instanceof NotificationCustomViewWrapper);
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int i5;
        if (this.mExpandedChild != null) {
            i5 = this.mExpandedChild.getHeight();
        } else {
            i5 = 0;
        }
        super.onLayout(z, i, i2, i3, i4);
        if (i5 != 0 && this.mExpandedChild.getHeight() != i5) {
            this.mContentHeightAtAnimationStart = i5;
        }
        updateClipping();
        invalidateOutline();
        selectLayout(false, this.mForceSelectNextLayout);
        this.mForceSelectNextLayout = false;
        updateExpandButtons(this.mExpandable);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateVisibility();
    }

    public View getContractedChild() {
        return this.mContractedChild;
    }

    public View getExpandedChild() {
        return this.mExpandedChild;
    }

    public View getHeadsUpChild() {
        return this.mHeadsUpChild;
    }

    public View getAmbientChild() {
        return this.mAmbientChild;
    }

    public HybridNotificationView getAmbientSingleLineChild() {
        return this.mAmbientSingleLineChild;
    }

    public void setContractedChild(View view) {
        if (this.mContractedChild != null) {
            this.mContractedChild.animate().cancel();
            removeView(this.mContractedChild);
        }
        addView(view);
        this.mContractedChild = view;
        this.mContractedWrapper = NotificationViewWrapper.wrap(getContext(), view, this.mContainingNotification);
    }

    private NotificationViewWrapper getWrapperForView(View view) {
        if (view == this.mContractedChild) {
            return this.mContractedWrapper;
        }
        if (view == this.mExpandedChild) {
            return this.mExpandedWrapper;
        }
        if (view == this.mHeadsUpChild) {
            return this.mHeadsUpWrapper;
        }
        if (view == this.mAmbientChild) {
            return this.mAmbientWrapper;
        }
        return null;
    }

    public void setExpandedChild(View view) {
        if (this.mExpandedChild != null) {
            this.mPreviousExpandedRemoteInputIntent = null;
            if (this.mExpandedRemoteInput != null) {
                this.mExpandedRemoteInput.onNotificationUpdateOrReset();
                if (this.mExpandedRemoteInput.isActive()) {
                    this.mPreviousExpandedRemoteInputIntent = this.mExpandedRemoteInput.getPendingIntent();
                    this.mCachedExpandedRemoteInput = this.mExpandedRemoteInput;
                    this.mExpandedRemoteInput.dispatchStartTemporaryDetach();
                    ((ViewGroup) this.mExpandedRemoteInput.getParent()).removeView(this.mExpandedRemoteInput);
                }
            }
            this.mExpandedChild.animate().cancel();
            removeView(this.mExpandedChild);
            this.mExpandedRemoteInput = null;
        }
        if (view == null) {
            this.mExpandedChild = null;
            this.mExpandedWrapper = null;
            if (this.mVisibleType == 1) {
                this.mVisibleType = 0;
            }
            if (this.mTransformationStartVisibleType == 1) {
                this.mTransformationStartVisibleType = -1;
                return;
            }
            return;
        }
        addView(view);
        this.mExpandedChild = view;
        this.mExpandedWrapper = NotificationViewWrapper.wrap(getContext(), view, this.mContainingNotification);
    }

    public void setHeadsUpChild(View view) {
        if (this.mHeadsUpChild != null) {
            this.mPreviousHeadsUpRemoteInputIntent = null;
            if (this.mHeadsUpRemoteInput != null) {
                this.mHeadsUpRemoteInput.onNotificationUpdateOrReset();
                if (this.mHeadsUpRemoteInput.isActive()) {
                    this.mPreviousHeadsUpRemoteInputIntent = this.mHeadsUpRemoteInput.getPendingIntent();
                    this.mCachedHeadsUpRemoteInput = this.mHeadsUpRemoteInput;
                    this.mHeadsUpRemoteInput.dispatchStartTemporaryDetach();
                    ((ViewGroup) this.mHeadsUpRemoteInput.getParent()).removeView(this.mHeadsUpRemoteInput);
                }
            }
            this.mHeadsUpChild.animate().cancel();
            removeView(this.mHeadsUpChild);
            this.mHeadsUpRemoteInput = null;
        }
        if (view == null) {
            this.mHeadsUpChild = null;
            this.mHeadsUpWrapper = null;
            if (this.mVisibleType == 2) {
                this.mVisibleType = 0;
            }
            if (this.mTransformationStartVisibleType == 2) {
                this.mTransformationStartVisibleType = -1;
                return;
            }
            return;
        }
        addView(view);
        this.mHeadsUpChild = view;
        this.mHeadsUpWrapper = NotificationViewWrapper.wrap(getContext(), view, this.mContainingNotification);
    }

    public void setAmbientChild(View view) {
        if (this.mAmbientChild != null) {
            this.mAmbientChild.animate().cancel();
            removeView(this.mAmbientChild);
        }
        if (view == null) {
            return;
        }
        addView(view);
        this.mAmbientChild = view;
        this.mAmbientWrapper = NotificationViewWrapper.wrap(getContext(), view, this.mContainingNotification);
    }

    @Override // android.view.View
    protected void onVisibilityChanged(View view, int i) {
        super.onVisibilityChanged(view, i);
        updateVisibility();
    }

    private void updateVisibility() {
        setVisible(isShown());
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeOnPreDrawListener(this.mEnableAnimationPredrawListener);
    }

    private void setVisible(boolean z) {
        if (z) {
            getViewTreeObserver().removeOnPreDrawListener(this.mEnableAnimationPredrawListener);
            getViewTreeObserver().addOnPreDrawListener(this.mEnableAnimationPredrawListener);
            return;
        }
        getViewTreeObserver().removeOnPreDrawListener(this.mEnableAnimationPredrawListener);
        this.mAnimate = false;
    }

    private void focusExpandButtonIfNecessary() {
        ImageView expandButton;
        if (this.mFocusOnVisibilityChange) {
            NotificationHeaderView visibleNotificationHeader = getVisibleNotificationHeader();
            if (visibleNotificationHeader != null && (expandButton = visibleNotificationHeader.getExpandButton()) != null) {
                expandButton.requestAccessibilityFocus();
            }
            this.mFocusOnVisibilityChange = false;
        }
    }

    public void setContentHeight(int i) {
        this.mUnrestrictedContentHeight = Math.max(i, getMinHeight());
        this.mContentHeight = Math.min(this.mUnrestrictedContentHeight, (this.mContainingNotification.getIntrinsicHeight() - getExtraRemoteInputHeight(this.mExpandedRemoteInput)) - getExtraRemoteInputHeight(this.mHeadsUpRemoteInput));
        selectLayout(this.mAnimate, false);
        int minContentHeightHint = getMinContentHeightHint();
        NotificationViewWrapper visibleWrapper = getVisibleWrapper(this.mVisibleType);
        if (visibleWrapper != null) {
            visibleWrapper.setContentHeight(this.mUnrestrictedContentHeight, minContentHeightHint);
        }
        NotificationViewWrapper visibleWrapper2 = getVisibleWrapper(this.mTransformationStartVisibleType);
        if (visibleWrapper2 != null) {
            visibleWrapper2.setContentHeight(this.mUnrestrictedContentHeight, minContentHeightHint);
        }
        updateClipping();
        invalidateOutline();
    }

    private int getMinContentHeightHint() {
        int viewHeight;
        if (this.mIsChildInGroup && isVisibleOrTransitioning(3)) {
            return this.mContext.getResources().getDimensionPixelSize(17105189);
        }
        if (this.mHeadsUpChild != null && this.mExpandedChild != null) {
            boolean z = isTransitioningFromTo(2, 1) || isTransitioningFromTo(1, 2);
            boolean z2 = !isVisibleOrTransitioning(0) && (this.mIsHeadsUp || this.mHeadsUpAnimatingAway) && !this.mContainingNotification.isOnKeyguard();
            if (z || z2) {
                return Math.min(getViewHeight(2), getViewHeight(1));
            }
        }
        if (this.mVisibleType == 1 && this.mContentHeightAtAnimationStart >= 0 && this.mExpandedChild != null) {
            return Math.min(this.mContentHeightAtAnimationStart, getViewHeight(1));
        }
        if (this.mAmbientChild != null && isVisibleOrTransitioning(4)) {
            viewHeight = this.mAmbientChild.getHeight();
        } else if (this.mAmbientSingleLineChild != null && isVisibleOrTransitioning(5)) {
            viewHeight = this.mAmbientSingleLineChild.getHeight();
        } else if (this.mHeadsUpChild != null && isVisibleOrTransitioning(2)) {
            viewHeight = getViewHeight(2);
        } else if (this.mExpandedChild != null) {
            viewHeight = getViewHeight(1);
        } else {
            viewHeight = getViewHeight(0) + this.mContext.getResources().getDimensionPixelSize(17105189);
        }
        if (this.mExpandedChild != null && isVisibleOrTransitioning(1)) {
            return Math.min(viewHeight, getViewHeight(1));
        }
        return viewHeight;
    }

    private boolean isTransitioningFromTo(int i, int i2) {
        return (this.mTransformationStartVisibleType == i || this.mAnimationStartVisibleType == i) && this.mVisibleType == i2;
    }

    private boolean isVisibleOrTransitioning(int i) {
        return this.mVisibleType == i || this.mTransformationStartVisibleType == i || this.mAnimationStartVisibleType == i;
    }

    private void updateContentTransformation() {
        int calculateVisibleType = calculateVisibleType();
        if (calculateVisibleType != this.mVisibleType) {
            this.mTransformationStartVisibleType = this.mVisibleType;
            TransformableView transformableViewForVisibleType = getTransformableViewForVisibleType(calculateVisibleType);
            TransformableView transformableViewForVisibleType2 = getTransformableViewForVisibleType(this.mTransformationStartVisibleType);
            transformableViewForVisibleType.transformFrom(transformableViewForVisibleType2, 0.0f);
            getViewForVisibleType(calculateVisibleType).setVisibility(0);
            transformableViewForVisibleType2.transformTo(transformableViewForVisibleType, 0.0f);
            this.mVisibleType = calculateVisibleType;
            updateBackgroundColor(true);
        }
        if (this.mForceSelectNextLayout) {
            forceUpdateVisibilities();
        }
        if (this.mTransformationStartVisibleType != -1 && this.mVisibleType != this.mTransformationStartVisibleType && getViewForVisibleType(this.mTransformationStartVisibleType) != null) {
            TransformableView transformableViewForVisibleType3 = getTransformableViewForVisibleType(this.mVisibleType);
            TransformableView transformableViewForVisibleType4 = getTransformableViewForVisibleType(this.mTransformationStartVisibleType);
            float calculateTransformationAmount = calculateTransformationAmount();
            transformableViewForVisibleType3.transformFrom(transformableViewForVisibleType4, calculateTransformationAmount);
            transformableViewForVisibleType4.transformTo(transformableViewForVisibleType3, calculateTransformationAmount);
            updateBackgroundTransformation(calculateTransformationAmount);
            return;
        }
        updateViewVisibilities(calculateVisibleType);
        updateBackgroundColor(false);
    }

    private void updateBackgroundTransformation(float f) {
        int backgroundColor = getBackgroundColor(this.mVisibleType);
        int backgroundColor2 = getBackgroundColor(this.mTransformationStartVisibleType);
        if (backgroundColor != backgroundColor2) {
            if (backgroundColor2 == 0) {
                backgroundColor2 = this.mContainingNotification.getBackgroundColorWithoutTint();
            }
            if (backgroundColor == 0) {
                backgroundColor = this.mContainingNotification.getBackgroundColorWithoutTint();
            }
            backgroundColor = NotificationUtils.interpolateColors(backgroundColor2, backgroundColor, f);
        }
        this.mContainingNotification.updateBackgroundAlpha(f);
        this.mContainingNotification.setContentBackground(backgroundColor, false, this);
    }

    private float calculateTransformationAmount() {
        int viewHeight = getViewHeight(this.mTransformationStartVisibleType);
        int viewHeight2 = getViewHeight(this.mVisibleType);
        int abs = Math.abs(this.mContentHeight - viewHeight);
        int abs2 = Math.abs(viewHeight2 - viewHeight);
        if (abs2 == 0) {
            Log.wtf("NotificationContentView", "the total transformation distance is 0\n StartType: " + this.mTransformationStartVisibleType + " height: " + viewHeight + "\n VisibleType: " + this.mVisibleType + " height: " + viewHeight2 + "\n mContentHeight: " + this.mContentHeight);
            return 1.0f;
        }
        return Math.min(1.0f, abs / abs2);
    }

    public int getMaxHeight() {
        if (this.mContainingNotification.isShowingAmbient()) {
            return getShowingAmbientView().getHeight();
        }
        if (this.mExpandedChild != null) {
            return getViewHeight(1) + getExtraRemoteInputHeight(this.mExpandedRemoteInput);
        }
        if (this.mIsHeadsUp && this.mHeadsUpChild != null && !this.mContainingNotification.isOnKeyguard()) {
            return getViewHeight(2) + getExtraRemoteInputHeight(this.mHeadsUpRemoteInput);
        }
        return getViewHeight(0);
    }

    private int getViewHeight(int i) {
        View viewForVisibleType = getViewForVisibleType(i);
        int height = viewForVisibleType.getHeight();
        NotificationViewWrapper wrapperForView = getWrapperForView(viewForVisibleType);
        if (wrapperForView != null) {
            return height + wrapperForView.getHeaderTranslation();
        }
        return height;
    }

    public int getMinHeight() {
        return getMinHeight(false);
    }

    public int getMinHeight(boolean z) {
        if (this.mContainingNotification.isShowingAmbient()) {
            return getShowingAmbientView().getHeight();
        }
        if (z || !this.mIsChildInGroup || isGroupExpanded()) {
            return getViewHeight(0);
        }
        return this.mSingleLineView.getHeight();
    }

    public View getShowingAmbientView() {
        View view = this.mIsChildInGroup ? this.mAmbientSingleLineChild : this.mAmbientChild;
        if (view != null) {
            return view;
        }
        return this.mContractedChild;
    }

    private boolean isGroupExpanded() {
        return this.mGroupManager.isGroupExpanded(this.mStatusBarNotification);
    }

    public void setClipTopAmount(int i) {
        this.mClipTopAmount = i;
        updateClipping();
    }

    public void setClipBottomAmount(int i) {
        this.mClipBottomAmount = i;
        updateClipping();
    }

    @Override // android.view.View
    public void setTranslationY(float f) {
        super.setTranslationY(f);
        updateClipping();
    }

    private void updateClipping() {
        if (this.mClipToActualHeight) {
            int translationY = (int) (this.mClipTopAmount - getTranslationY());
            this.mClipBounds.set(0, translationY, getWidth(), Math.max(translationY, (int) ((this.mUnrestrictedContentHeight - this.mClipBottomAmount) - getTranslationY())));
            setClipBounds(this.mClipBounds);
            return;
        }
        setClipBounds(null);
    }

    public void setClipToActualHeight(boolean z) {
        this.mClipToActualHeight = z;
        updateClipping();
    }

    private void selectLayout(boolean z, boolean z2) {
        if (this.mContractedChild == null) {
            return;
        }
        if (this.mUserExpanding) {
            updateContentTransformation();
            return;
        }
        int calculateVisibleType = calculateVisibleType();
        boolean z3 = calculateVisibleType != this.mVisibleType;
        if (z3 || z2) {
            View viewForVisibleType = getViewForVisibleType(calculateVisibleType);
            if (viewForVisibleType != null) {
                viewForVisibleType.setVisibility(0);
                transferRemoteInputFocus(calculateVisibleType);
            }
            if (z && ((calculateVisibleType == 1 && this.mExpandedChild != null) || ((calculateVisibleType == 2 && this.mHeadsUpChild != null) || ((calculateVisibleType == 3 && this.mSingleLineView != null) || calculateVisibleType == 0)))) {
                animateToVisibleType(calculateVisibleType);
            } else {
                updateViewVisibilities(calculateVisibleType);
            }
            this.mVisibleType = calculateVisibleType;
            if (z3) {
                focusExpandButtonIfNecessary();
            }
            NotificationViewWrapper visibleWrapper = getVisibleWrapper(calculateVisibleType);
            if (visibleWrapper != null) {
                visibleWrapper.setContentHeight(this.mUnrestrictedContentHeight, getMinContentHeightHint());
            }
            updateBackgroundColor(z);
        }
    }

    private void forceUpdateVisibilities() {
        forceUpdateVisibility(0, this.mContractedChild, this.mContractedWrapper);
        forceUpdateVisibility(1, this.mExpandedChild, this.mExpandedWrapper);
        forceUpdateVisibility(2, this.mHeadsUpChild, this.mHeadsUpWrapper);
        forceUpdateVisibility(3, this.mSingleLineView, this.mSingleLineView);
        forceUpdateVisibility(4, this.mAmbientChild, this.mAmbientWrapper);
        forceUpdateVisibility(5, this.mAmbientSingleLineChild, this.mAmbientSingleLineChild);
        fireExpandedVisibleListenerIfVisible();
        this.mAnimationStartVisibleType = -1;
    }

    private void fireExpandedVisibleListenerIfVisible() {
        if (this.mExpandedVisibleListener != null && this.mExpandedChild != null && isShown() && this.mExpandedChild.getVisibility() == 0) {
            Runnable runnable = this.mExpandedVisibleListener;
            this.mExpandedVisibleListener = null;
            runnable.run();
        }
    }

    private void forceUpdateVisibility(int i, View view, TransformableView transformableView) {
        if (view == null) {
            return;
        }
        if (!(this.mVisibleType == i || this.mTransformationStartVisibleType == i)) {
            view.setVisibility(4);
        } else {
            transformableView.setVisible(true);
        }
    }

    public void updateBackgroundColor(boolean z) {
        int backgroundColor = getBackgroundColor(this.mVisibleType);
        this.mContainingNotification.resetBackgroundAlpha();
        this.mContainingNotification.setContentBackground(backgroundColor, z, this);
    }

    public void setBackgroundTintColor(int i) {
        if (this.mExpandedSmartReplyView != null) {
            this.mExpandedSmartReplyView.setBackgroundTintColor(i);
        }
    }

    public int getVisibleType() {
        return this.mVisibleType;
    }

    public int getBackgroundColorForExpansionState() {
        int calculateVisibleType;
        if (this.mContainingNotification.isGroupExpanded() || this.mContainingNotification.isUserLocked()) {
            calculateVisibleType = calculateVisibleType();
        } else {
            calculateVisibleType = getVisibleType();
        }
        return getBackgroundColor(calculateVisibleType);
    }

    public int getBackgroundColor(int i) {
        NotificationViewWrapper visibleWrapper = getVisibleWrapper(i);
        if (visibleWrapper != null) {
            return visibleWrapper.getCustomBackgroundColor();
        }
        return 0;
    }

    private void updateViewVisibilities(int i) {
        updateViewVisibility(i, 0, this.mContractedChild, this.mContractedWrapper);
        updateViewVisibility(i, 1, this.mExpandedChild, this.mExpandedWrapper);
        updateViewVisibility(i, 2, this.mHeadsUpChild, this.mHeadsUpWrapper);
        updateViewVisibility(i, 3, this.mSingleLineView, this.mSingleLineView);
        updateViewVisibility(i, 4, this.mAmbientChild, this.mAmbientWrapper);
        updateViewVisibility(i, 5, this.mAmbientSingleLineChild, this.mAmbientSingleLineChild);
        fireExpandedVisibleListenerIfVisible();
        this.mAnimationStartVisibleType = -1;
    }

    private void updateViewVisibility(int i, int i2, View view, TransformableView transformableView) {
        if (view != null) {
            transformableView.setVisible(i == i2);
        }
    }

    private void animateToVisibleType(int i) {
        TransformableView transformableViewForVisibleType = getTransformableViewForVisibleType(i);
        final TransformableView transformableViewForVisibleType2 = getTransformableViewForVisibleType(this.mVisibleType);
        if (transformableViewForVisibleType == transformableViewForVisibleType2 || transformableViewForVisibleType2 == null) {
            transformableViewForVisibleType.setVisible(true);
            return;
        }
        this.mAnimationStartVisibleType = this.mVisibleType;
        transformableViewForVisibleType.transformFrom(transformableViewForVisibleType2);
        getViewForVisibleType(i).setVisibility(0);
        transformableViewForVisibleType2.transformTo(transformableViewForVisibleType, new Runnable() { // from class: com.android.systemui.statusbar.NotificationContentView.2
            @Override // java.lang.Runnable
            public void run() {
                if (transformableViewForVisibleType2 != NotificationContentView.this.getTransformableViewForVisibleType(NotificationContentView.this.mVisibleType)) {
                    transformableViewForVisibleType2.setVisible(false);
                }
                NotificationContentView.this.mAnimationStartVisibleType = -1;
            }
        });
        fireExpandedVisibleListenerIfVisible();
    }

    private void transferRemoteInputFocus(int i) {
        if (i == 2 && this.mHeadsUpRemoteInput != null && this.mExpandedRemoteInput != null && this.mExpandedRemoteInput.isActive()) {
            this.mHeadsUpRemoteInput.stealFocusFrom(this.mExpandedRemoteInput);
        }
        if (i == 1 && this.mExpandedRemoteInput != null && this.mHeadsUpRemoteInput != null && this.mHeadsUpRemoteInput.isActive()) {
            this.mExpandedRemoteInput.stealFocusFrom(this.mHeadsUpRemoteInput);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public TransformableView getTransformableViewForVisibleType(int i) {
        switch (i) {
            case 1:
                return this.mExpandedWrapper;
            case 2:
                return this.mHeadsUpWrapper;
            case 3:
                return this.mSingleLineView;
            case 4:
                return this.mAmbientWrapper;
            case 5:
                return this.mAmbientSingleLineChild;
            default:
                return this.mContractedWrapper;
        }
    }

    private View getViewForVisibleType(int i) {
        switch (i) {
            case 1:
                return this.mExpandedChild;
            case 2:
                return this.mHeadsUpChild;
            case 3:
                return this.mSingleLineView;
            case 4:
                return this.mAmbientChild;
            case 5:
                return this.mAmbientSingleLineChild;
            default:
                return this.mContractedChild;
        }
    }

    public NotificationViewWrapper getVisibleWrapper(int i) {
        if (i != 4) {
            switch (i) {
                case 0:
                    return this.mContractedWrapper;
                case 1:
                    return this.mExpandedWrapper;
                case 2:
                    return this.mHeadsUpWrapper;
                default:
                    return null;
            }
        }
        return this.mAmbientWrapper;
    }

    public int calculateVisibleType() {
        int maxContentHeight;
        int visualTypeForHeight;
        if (this.mContainingNotification.isShowingAmbient()) {
            if (this.mIsChildInGroup && this.mAmbientSingleLineChild != null) {
                return 5;
            }
            if (this.mAmbientChild != null) {
                return 4;
            }
            return 0;
        } else if (this.mUserExpanding) {
            if (!this.mIsChildInGroup || isGroupExpanded() || this.mContainingNotification.isExpanded(true)) {
                maxContentHeight = this.mContainingNotification.getMaxContentHeight();
            } else {
                maxContentHeight = this.mContainingNotification.getShowingLayout().getMinHeight();
            }
            if (maxContentHeight == 0) {
                maxContentHeight = this.mContentHeight;
            }
            int visualTypeForHeight2 = getVisualTypeForHeight(maxContentHeight);
            if (this.mIsChildInGroup && !isGroupExpanded()) {
                visualTypeForHeight = 3;
            } else {
                visualTypeForHeight = getVisualTypeForHeight(this.mContainingNotification.getCollapsedHeight());
            }
            return this.mTransformationStartVisibleType == visualTypeForHeight ? visualTypeForHeight2 : visualTypeForHeight;
        } else {
            int intrinsicHeight = this.mContainingNotification.getIntrinsicHeight();
            int i = this.mContentHeight;
            if (intrinsicHeight != 0) {
                i = Math.min(this.mContentHeight, intrinsicHeight);
            }
            return getVisualTypeForHeight(i);
        }
    }

    private int getVisualTypeForHeight(float f) {
        boolean z = this.mExpandedChild == null;
        if (z || f != getViewHeight(1)) {
            if (this.mUserExpanding || !this.mIsChildInGroup || isGroupExpanded()) {
                return ((!this.mIsHeadsUp && !this.mHeadsUpAnimatingAway) || this.mHeadsUpChild == null || this.mContainingNotification.isOnKeyguard()) ? (z || (f <= ((float) getViewHeight(0)) && !(this.mIsChildInGroup && !isGroupExpanded() && this.mContainingNotification.isExpanded(true)))) ? 0 : 1 : (f <= ((float) getViewHeight(2)) || z) ? 2 : 1;
            }
            return 3;
        }
        return 1;
    }

    public boolean isContentExpandable() {
        return this.mIsContentExpandable;
    }

    public void setDark(boolean z, boolean z2, long j) {
        if (this.mContractedChild == null) {
            return;
        }
        this.mDark = z;
        selectLayout(!z && z2, false);
    }

    public void setHeadsUp(boolean z) {
        this.mIsHeadsUp = z;
        selectLayout(false, true);
        updateExpandButtons(this.mExpandable);
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    public void setLegacy(boolean z) {
        this.mLegacy = z;
        updateLegacy();
    }

    private void updateLegacy() {
        if (this.mContractedChild != null) {
            this.mContractedWrapper.setLegacy(this.mLegacy);
        }
        if (this.mExpandedChild != null) {
            this.mExpandedWrapper.setLegacy(this.mLegacy);
        }
        if (this.mHeadsUpChild != null) {
            this.mHeadsUpWrapper.setLegacy(this.mLegacy);
        }
    }

    public void setIsChildInGroup(boolean z) {
        this.mIsChildInGroup = z;
        if (this.mContractedChild != null) {
            this.mContractedWrapper.setIsChildInGroup(this.mIsChildInGroup);
        }
        if (this.mExpandedChild != null) {
            this.mExpandedWrapper.setIsChildInGroup(this.mIsChildInGroup);
        }
        if (this.mHeadsUpChild != null) {
            this.mHeadsUpWrapper.setIsChildInGroup(this.mIsChildInGroup);
        }
        if (this.mAmbientChild != null) {
            this.mAmbientWrapper.setIsChildInGroup(this.mIsChildInGroup);
        }
        updateAllSingleLineViews();
    }

    public void onNotificationUpdated(NotificationData.Entry entry) {
        this.mStatusBarNotification = entry.notification;
        this.mBeforeN = entry.targetSdk < 24;
        updateAllSingleLineViews();
        if (this.mContractedChild != null) {
            this.mContractedWrapper.onContentUpdated(entry.row);
        }
        if (this.mExpandedChild != null) {
            this.mExpandedWrapper.onContentUpdated(entry.row);
        }
        if (this.mHeadsUpChild != null) {
            this.mHeadsUpWrapper.onContentUpdated(entry.row);
        }
        if (this.mAmbientChild != null) {
            this.mAmbientWrapper.onContentUpdated(entry.row);
        }
        applyRemoteInputAndSmartReply(entry);
        updateLegacy();
        this.mForceSelectNextLayout = true;
        setDark(this.mDark, false, 0L);
        this.mPreviousExpandedRemoteInputIntent = null;
        this.mPreviousHeadsUpRemoteInputIntent = null;
    }

    private void updateAllSingleLineViews() {
        updateSingleLineView();
        updateAmbientSingleLineView();
    }

    private void updateSingleLineView() {
        if (!this.mIsChildInGroup) {
            if (this.mSingleLineView != null) {
                removeView(this.mSingleLineView);
                this.mSingleLineView = null;
                return;
            }
            return;
        }
        boolean z = this.mSingleLineView == null;
        this.mSingleLineView = this.mHybridGroupManager.bindFromNotification(this.mSingleLineView, this.mStatusBarNotification.getNotification());
        if (z) {
            updateViewVisibility(this.mVisibleType, 3, this.mSingleLineView, this.mSingleLineView);
        }
    }

    private void updateAmbientSingleLineView() {
        if (!this.mIsChildInGroup) {
            if (this.mAmbientSingleLineChild != null) {
                removeView(this.mAmbientSingleLineChild);
                this.mAmbientSingleLineChild = null;
                return;
            }
            return;
        }
        boolean z = this.mAmbientSingleLineChild == null;
        this.mAmbientSingleLineChild = this.mHybridGroupManager.bindAmbientFromNotification(this.mAmbientSingleLineChild, this.mStatusBarNotification.getNotification());
        if (z) {
            updateViewVisibility(this.mVisibleType, 5, this.mAmbientSingleLineChild, this.mAmbientSingleLineChild);
        }
    }

    private void applyRemoteInputAndSmartReply(NotificationData.Entry entry) {
        PendingIntent pendingIntent;
        boolean z;
        RemoteInput[] remoteInputs;
        if (this.mRemoteInputController == null) {
            return;
        }
        boolean z2 = this.mSmartReplyConstants.isEnabled() && (!this.mSmartReplyConstants.requiresTargetingP() || entry.targetSdk >= 28);
        Notification.Action[] actionArr = entry.notification.getNotification().actions;
        RemoteInput remoteInput = null;
        if (actionArr == null) {
            pendingIntent = null;
            z = false;
        } else {
            RemoteInput remoteInput2 = null;
            pendingIntent = null;
            z = false;
            for (Notification.Action action : actionArr) {
                if (action.getRemoteInputs() != null) {
                    PendingIntent pendingIntent2 = pendingIntent;
                    RemoteInput remoteInput3 = remoteInput2;
                    boolean z3 = z;
                    for (RemoteInput remoteInput4 : action.getRemoteInputs()) {
                        boolean allowFreeFormInput = remoteInput4.getAllowFreeFormInput();
                        boolean z4 = z2 && remoteInput4.getChoices() != null && remoteInput4.getChoices().length > 0;
                        if (allowFreeFormInput) {
                            z3 = true;
                        }
                        if (z4) {
                            pendingIntent2 = action.actionIntent;
                            remoteInput3 = remoteInput4;
                        }
                        if (allowFreeFormInput || z4) {
                            break;
                        }
                    }
                    z = z3;
                    remoteInput2 = remoteInput3;
                    pendingIntent = pendingIntent2;
                }
            }
            remoteInput = remoteInput2;
        }
        applyRemoteInput(entry, z);
        applySmartReplyView(remoteInput, pendingIntent, entry);
    }

    private void applyRemoteInput(NotificationData.Entry entry, boolean z) {
        View view = this.mExpandedChild;
        if (view != null) {
            this.mExpandedRemoteInput = applyRemoteInput(view, entry, z, this.mPreviousExpandedRemoteInputIntent, this.mCachedExpandedRemoteInput, this.mExpandedWrapper);
        } else {
            this.mExpandedRemoteInput = null;
        }
        if (this.mCachedExpandedRemoteInput != null && this.mCachedExpandedRemoteInput != this.mExpandedRemoteInput) {
            this.mCachedExpandedRemoteInput.dispatchFinishTemporaryDetach();
        }
        this.mCachedExpandedRemoteInput = null;
        View view2 = this.mHeadsUpChild;
        if (view2 != null) {
            this.mHeadsUpRemoteInput = applyRemoteInput(view2, entry, z, this.mPreviousHeadsUpRemoteInputIntent, this.mCachedHeadsUpRemoteInput, this.mHeadsUpWrapper);
        } else {
            this.mHeadsUpRemoteInput = null;
        }
        if (this.mCachedHeadsUpRemoteInput != null && this.mCachedHeadsUpRemoteInput != this.mHeadsUpRemoteInput) {
            this.mCachedHeadsUpRemoteInput.dispatchFinishTemporaryDetach();
        }
        this.mCachedHeadsUpRemoteInput = null;
    }

    private RemoteInputView applyRemoteInput(View view, NotificationData.Entry entry, boolean z, PendingIntent pendingIntent, RemoteInputView remoteInputView, NotificationViewWrapper notificationViewWrapper) {
        View findViewById = view.findViewById(16908685);
        if (findViewById instanceof FrameLayout) {
            RemoteInputView remoteInputView2 = (RemoteInputView) view.findViewWithTag(RemoteInputView.VIEW_TAG);
            if (remoteInputView2 != null) {
                remoteInputView2.onNotificationUpdateOrReset();
            }
            if (remoteInputView2 == null && z) {
                FrameLayout frameLayout = (FrameLayout) findViewById;
                if (remoteInputView == null) {
                    remoteInputView = RemoteInputView.inflate(this.mContext, frameLayout, entry, this.mRemoteInputController);
                    remoteInputView.setVisibility(4);
                    frameLayout.addView(remoteInputView, new FrameLayout.LayoutParams(-1, -1));
                } else {
                    frameLayout.addView(remoteInputView);
                    remoteInputView.dispatchFinishTemporaryDetach();
                    remoteInputView.requestFocus();
                }
            } else {
                remoteInputView = remoteInputView2;
            }
            if (z) {
                int i = entry.notification.getNotification().color;
                if (i == 0) {
                    i = this.mContext.getColor(R.color.default_remote_input_background);
                }
                remoteInputView.setBackgroundColor(NotificationColorUtil.ensureTextBackgroundColor(i, this.mContext.getColor(R.color.remote_input_text_enabled), this.mContext.getColor(R.color.remote_input_hint)));
                remoteInputView.setWrapper(notificationViewWrapper);
                remoteInputView.setOnVisibilityChangedListener(new Consumer() { // from class: com.android.systemui.statusbar.-$$Lambda$ajnJ2IkdZIqGVFWc6Wtl4AFbcm4
                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        NotificationContentView.this.setRemoteInputVisible(((Boolean) obj).booleanValue());
                    }
                });
                if (pendingIntent != null || remoteInputView.isActive()) {
                    Notification.Action[] actionArr = entry.notification.getNotification().actions;
                    if (pendingIntent != null) {
                        remoteInputView.setPendingIntent(pendingIntent);
                    }
                    if (remoteInputView.updatePendingIntentFromActions(actionArr)) {
                        if (!remoteInputView.isActive()) {
                            remoteInputView.focus();
                        }
                    } else if (remoteInputView.isActive()) {
                        remoteInputView.close();
                    }
                }
            }
            return remoteInputView;
        }
        return null;
    }

    private void applySmartReplyView(RemoteInput remoteInput, PendingIntent pendingIntent, NotificationData.Entry entry) {
        if (this.mExpandedChild != null) {
            this.mExpandedSmartReplyView = applySmartReplyView(this.mExpandedChild, remoteInput, pendingIntent, entry);
            if (this.mExpandedSmartReplyView != null && remoteInput != null && remoteInput.getChoices() != null && remoteInput.getChoices().length > 0) {
                this.mSmartReplyController.smartRepliesAdded(entry, remoteInput.getChoices().length);
            }
        }
    }

    private SmartReplyView applySmartReplyView(View view, RemoteInput remoteInput, PendingIntent pendingIntent, NotificationData.Entry entry) {
        View findViewById = view.findViewById(16909318);
        SmartReplyView smartReplyView = null;
        if (findViewById instanceof LinearLayout) {
            LinearLayout linearLayout = (LinearLayout) findViewById;
            if (remoteInput == null || pendingIntent == null) {
                linearLayout.setVisibility(8);
                return null;
            } else if (entry.notification.getNotification().extras.getBoolean("android.remoteInputSpinner", false)) {
                linearLayout.setVisibility(8);
                return null;
            } else if (entry.notification.getNotification().extras.getBoolean("android.hideSmartReplies", false)) {
                linearLayout.setVisibility(8);
                return null;
            } else {
                if (linearLayout.getChildCount() == 0) {
                    smartReplyView = SmartReplyView.inflate(this.mContext, linearLayout);
                    linearLayout.addView(smartReplyView);
                } else if (linearLayout.getChildCount() == 1) {
                    View childAt = linearLayout.getChildAt(0);
                    if (childAt instanceof SmartReplyView) {
                        smartReplyView = (SmartReplyView) childAt;
                    }
                }
                if (smartReplyView != null) {
                    smartReplyView.setRepliesFromRemoteInput(remoteInput, pendingIntent, this.mSmartReplyController, entry, linearLayout);
                    linearLayout.setVisibility(0);
                }
                return smartReplyView;
            }
        }
        return null;
    }

    public void closeRemoteInput() {
        if (this.mHeadsUpRemoteInput != null) {
            this.mHeadsUpRemoteInput.close();
        }
        if (this.mExpandedRemoteInput != null) {
            this.mExpandedRemoteInput.close();
        }
    }

    public void setGroupManager(NotificationGroupManager notificationGroupManager) {
        this.mGroupManager = notificationGroupManager;
    }

    public void setRemoteInputController(RemoteInputController remoteInputController) {
        this.mRemoteInputController = remoteInputController;
    }

    public void setExpandClickListener(View.OnClickListener onClickListener) {
        this.mExpandClickListener = onClickListener;
    }

    public void updateExpandButtons(boolean z) {
        this.mExpandable = z;
        boolean z2 = false;
        if (this.mExpandedChild == null || this.mExpandedChild.getHeight() == 0 || ((this.mIsHeadsUp || this.mHeadsUpAnimatingAway) && this.mHeadsUpChild != null && !this.mContainingNotification.isOnKeyguard() ? this.mExpandedChild.getHeight() > this.mHeadsUpChild.getHeight() : this.mExpandedChild.getHeight() > this.mContractedChild.getHeight())) {
            z2 = z;
        }
        if (this.mExpandedChild != null) {
            this.mExpandedWrapper.updateExpandability(z2, this.mExpandClickListener);
        }
        if (this.mContractedChild != null) {
            this.mContractedWrapper.updateExpandability(z2, this.mExpandClickListener);
        }
        if (this.mHeadsUpChild != null) {
            this.mHeadsUpWrapper.updateExpandability(z2, this.mExpandClickListener);
        }
        this.mIsContentExpandable = z2;
    }

    public NotificationHeaderView getNotificationHeader() {
        NotificationHeaderView notificationHeaderView;
        if (this.mContractedChild != null) {
            notificationHeaderView = this.mContractedWrapper.getNotificationHeader();
        } else {
            notificationHeaderView = null;
        }
        if (notificationHeaderView == null && this.mExpandedChild != null) {
            notificationHeaderView = this.mExpandedWrapper.getNotificationHeader();
        }
        if (notificationHeaderView == null && this.mHeadsUpChild != null) {
            notificationHeaderView = this.mHeadsUpWrapper.getNotificationHeader();
        }
        if (notificationHeaderView == null && this.mAmbientChild != null) {
            return this.mAmbientWrapper.getNotificationHeader();
        }
        return notificationHeaderView;
    }

    public void showAppOpsIcons(ArraySet<Integer> arraySet) {
        if (this.mContractedChild != null && this.mContractedWrapper.getNotificationHeader() != null) {
            this.mContractedWrapper.getNotificationHeader().showAppOpsIcons(arraySet);
        }
        if (this.mExpandedChild != null && this.mExpandedWrapper.getNotificationHeader() != null) {
            this.mExpandedWrapper.getNotificationHeader().showAppOpsIcons(arraySet);
        }
        if (this.mHeadsUpChild != null && this.mHeadsUpWrapper.getNotificationHeader() != null) {
            this.mHeadsUpWrapper.getNotificationHeader().showAppOpsIcons(arraySet);
        }
    }

    public NotificationHeaderView getContractedNotificationHeader() {
        if (this.mContractedChild != null) {
            return this.mContractedWrapper.getNotificationHeader();
        }
        return null;
    }

    public NotificationHeaderView getVisibleNotificationHeader() {
        NotificationViewWrapper visibleWrapper = getVisibleWrapper(this.mVisibleType);
        if (visibleWrapper == null) {
            return null;
        }
        return visibleWrapper.getNotificationHeader();
    }

    public void setContainingNotification(ExpandableNotificationRow expandableNotificationRow) {
        this.mContainingNotification = expandableNotificationRow;
    }

    public void requestSelectLayout(boolean z) {
        selectLayout(z, false);
    }

    public void reInflateViews() {
        if (this.mIsChildInGroup && this.mSingleLineView != null) {
            removeView(this.mSingleLineView);
            this.mSingleLineView = null;
            updateAllSingleLineViews();
        }
    }

    public void setUserExpanding(boolean z) {
        this.mUserExpanding = z;
        if (z) {
            this.mTransformationStartVisibleType = this.mVisibleType;
            return;
        }
        this.mTransformationStartVisibleType = -1;
        this.mVisibleType = calculateVisibleType();
        updateViewVisibilities(this.mVisibleType);
        updateBackgroundColor(false);
    }

    public void setSingleLineWidthIndention(int i) {
        if (i != this.mSingleLineWidthIndention) {
            this.mSingleLineWidthIndention = i;
            this.mContainingNotification.forceLayout();
            forceLayout();
        }
    }

    public HybridNotificationView getSingleLineView() {
        return this.mSingleLineView;
    }

    public void setRemoved() {
        if (this.mExpandedRemoteInput != null) {
            this.mExpandedRemoteInput.setRemoved();
        }
        if (this.mHeadsUpRemoteInput != null) {
            this.mHeadsUpRemoteInput.setRemoved();
        }
    }

    public void setContentHeightAnimating(boolean z) {
        if (!z) {
            this.mContentHeightAtAnimationStart = -1;
        }
    }

    @VisibleForTesting
    boolean isAnimatingVisibleType() {
        return this.mAnimationStartVisibleType != -1;
    }

    public void setHeadsUpAnimatingAway(boolean z) {
        this.mHeadsUpAnimatingAway = z;
        selectLayout(false, true);
    }

    public void setFocusOnVisibilityChange() {
        this.mFocusOnVisibilityChange = true;
    }

    public void setIconsVisible(boolean z) {
        this.mIconsVisible = z;
        updateIconVisibilities();
    }

    private void updateIconVisibilities() {
        NotificationHeaderView notificationHeader;
        NotificationHeaderView notificationHeader2;
        NotificationHeaderView notificationHeader3;
        if (this.mContractedWrapper != null && (notificationHeader3 = this.mContractedWrapper.getNotificationHeader()) != null) {
            notificationHeader3.getIcon().setForceHidden(!this.mIconsVisible);
        }
        if (this.mHeadsUpWrapper != null && (notificationHeader2 = this.mHeadsUpWrapper.getNotificationHeader()) != null) {
            notificationHeader2.getIcon().setForceHidden(!this.mIconsVisible);
        }
        if (this.mExpandedWrapper != null && (notificationHeader = this.mExpandedWrapper.getNotificationHeader()) != null) {
            notificationHeader.getIcon().setForceHidden(!this.mIconsVisible);
        }
    }

    @Override // android.view.View
    public void onVisibilityAggregated(boolean z) {
        super.onVisibilityAggregated(z);
        if (z) {
            fireExpandedVisibleListenerIfVisible();
        }
    }

    public void setOnExpandedVisibleListener(Runnable runnable) {
        this.mExpandedVisibleListener = runnable;
        fireExpandedVisibleListenerIfVisible();
    }

    public void setIsLowPriority(boolean z) {
        this.mIsLowPriority = z;
    }

    public boolean isDimmable() {
        if (!this.mContractedWrapper.isDimmable()) {
            return false;
        }
        return true;
    }

    public boolean disallowSingleClick(float f, float f2) {
        NotificationViewWrapper visibleWrapper = getVisibleWrapper(getVisibleType());
        if (visibleWrapper != null) {
            return visibleWrapper.disallowSingleClick(f, f2);
        }
        return false;
    }

    public boolean shouldClipToRounding(boolean z, boolean z2) {
        boolean shouldClipToRounding = shouldClipToRounding(getVisibleType(), z, z2);
        if (this.mUserExpanding) {
            return shouldClipToRounding | shouldClipToRounding(this.mTransformationStartVisibleType, z, z2);
        }
        return shouldClipToRounding;
    }

    private boolean shouldClipToRounding(int i, boolean z, boolean z2) {
        NotificationViewWrapper visibleWrapper = getVisibleWrapper(i);
        if (visibleWrapper == null) {
            return false;
        }
        return visibleWrapper.shouldClipToRounding(z, z2);
    }

    public CharSequence getActiveRemoteInputText() {
        if (this.mExpandedRemoteInput != null && this.mExpandedRemoteInput.isActive()) {
            return this.mExpandedRemoteInput.getText();
        }
        if (this.mHeadsUpRemoteInput != null && this.mHeadsUpRemoteInput.isActive()) {
            return this.mHeadsUpRemoteInput.getText();
        }
        return null;
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        float y = motionEvent.getY();
        RemoteInputView remoteInputForView = getRemoteInputForView(getViewForVisibleType(this.mVisibleType));
        if (remoteInputForView != null && remoteInputForView.getVisibility() == 0) {
            int height = this.mUnrestrictedContentHeight - remoteInputForView.getHeight();
            if (y <= this.mUnrestrictedContentHeight && y >= height) {
                motionEvent.offsetLocation(0.0f, -height);
                return remoteInputForView.dispatchTouchEvent(motionEvent);
            }
        }
        return super.dispatchTouchEvent(motionEvent);
    }

    public boolean pointInView(float f, float f2, float f3) {
        return f >= (-f3) && f2 >= ((float) this.mClipTopAmount) - f3 && f < ((float) (this.mRight - this.mLeft)) + f3 && f2 < ((float) this.mUnrestrictedContentHeight) + f3;
    }

    private RemoteInputView getRemoteInputForView(View view) {
        if (view == this.mExpandedChild) {
            return this.mExpandedRemoteInput;
        }
        if (view == this.mHeadsUpChild) {
            return this.mHeadsUpRemoteInput;
        }
        return null;
    }

    public int getExpandHeight() {
        int i;
        if (this.mExpandedChild == null) {
            i = 0;
        } else {
            i = 1;
        }
        return getViewHeight(i) + getExtraRemoteInputHeight(this.mExpandedRemoteInput);
    }

    public int getHeadsUpHeight() {
        int i;
        if (this.mHeadsUpChild == null) {
            i = 0;
        } else {
            i = 2;
        }
        return getViewHeight(i) + getExtraRemoteInputHeight(this.mHeadsUpRemoteInput) + getExtraRemoteInputHeight(this.mExpandedRemoteInput);
    }

    public void setRemoteInputVisible(boolean z) {
        this.mRemoteInputVisible = z;
        setClipChildren(!z);
    }

    @Override // android.view.ViewGroup
    public void setClipChildren(boolean z) {
        super.setClipChildren(z && !this.mRemoteInputVisible);
    }

    public void setHeaderVisibleAmount(float f) {
        if (this.mContractedWrapper != null) {
            this.mContractedWrapper.setHeaderVisibleAmount(f);
        }
        if (this.mHeadsUpWrapper != null) {
            this.mHeadsUpWrapper.setHeaderVisibleAmount(f);
        }
        if (this.mExpandedWrapper != null) {
            this.mExpandedWrapper.setHeaderVisibleAmount(f);
        }
    }
}

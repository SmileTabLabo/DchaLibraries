package com.android.systemui.statusbar;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.Context;
import android.graphics.Rect;
import android.service.notification.StatusBarNotification;
import android.util.AttributeSet;
import android.view.NotificationHeaderView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.android.internal.util.NotificationColorUtil;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.notification.HybridGroupManager;
import com.android.systemui.statusbar.notification.HybridNotificationView;
import com.android.systemui.statusbar.notification.NotificationCustomViewWrapper;
import com.android.systemui.statusbar.notification.NotificationUtils;
import com.android.systemui.statusbar.notification.NotificationViewWrapper;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.policy.RemoteInputView;
/* loaded from: a.zip:com/android/systemui/statusbar/NotificationContentView.class */
public class NotificationContentView extends FrameLayout {
    private boolean mAnimate;
    private int mAnimationStartVisibleType;
    private boolean mBeforeN;
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
    private NotificationViewWrapper mExpandedWrapper;
    private boolean mFocusOnVisibilityChange;
    private boolean mForceSelectNextLayout;
    private NotificationGroupManager mGroupManager;
    private View mHeadsUpChild;
    private int mHeadsUpHeight;
    private RemoteInputView mHeadsUpRemoteInput;
    private NotificationViewWrapper mHeadsUpWrapper;
    private boolean mHeadsupDisappearRunning;
    private HybridGroupManager mHybridGroupManager;
    private boolean mIsChildInGroup;
    private boolean mIsHeadsUp;
    private final int mMinContractedHeight;
    private final int mNotificationContentMarginEnd;
    private int mNotificationMaxHeight;
    private PendingIntent mPreviousExpandedRemoteInputIntent;
    private PendingIntent mPreviousHeadsUpRemoteInputIntent;
    private RemoteInputController mRemoteInputController;
    private boolean mShowingLegacyBackground;
    private HybridNotificationView mSingleLineView;
    private int mSingleLineWidthIndention;
    private int mSmallHeight;
    private StatusBarNotification mStatusBarNotification;
    private int mTransformationStartVisibleType;
    private boolean mUserExpanding;
    private int mVisibleType;

    /* renamed from: com.android.systemui.statusbar.NotificationContentView$1  reason: invalid class name */
    /* loaded from: a.zip:com/android/systemui/statusbar/NotificationContentView$1.class */
    class AnonymousClass1 implements ViewTreeObserver.OnPreDrawListener {
        final NotificationContentView this$0;

        AnonymousClass1(NotificationContentView notificationContentView) {
            this.this$0 = notificationContentView;
        }

        @Override // android.view.ViewTreeObserver.OnPreDrawListener
        public boolean onPreDraw() {
            this.this$0.post(new Runnable(this) { // from class: com.android.systemui.statusbar.NotificationContentView.1.1
                final AnonymousClass1 this$1;

                {
                    this.this$1 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$1.this$0.mAnimate = true;
                }
            });
            this.this$0.getViewTreeObserver().removeOnPreDrawListener(this);
            return true;
        }
    }

    public NotificationContentView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mClipBounds = new Rect();
        this.mVisibleType = 0;
        this.mEnableAnimationPredrawListener = new AnonymousClass1(this);
        this.mClipToActualHeight = true;
        this.mAnimationStartVisibleType = -1;
        this.mForceSelectNextLayout = true;
        this.mContentHeightAtAnimationStart = -1;
        this.mHybridGroupManager = new HybridGroupManager(getContext(), this);
        this.mMinContractedHeight = getResources().getDimensionPixelSize(2131689791);
        this.mNotificationContentMarginEnd = getResources().getDimensionPixelSize(17104960);
        reset();
    }

    private void animateToVisibleType(int i) {
        TransformableView transformableViewForVisibleType = getTransformableViewForVisibleType(i);
        TransformableView transformableViewForVisibleType2 = getTransformableViewForVisibleType(this.mVisibleType);
        if (transformableViewForVisibleType == transformableViewForVisibleType2 || transformableViewForVisibleType2 == null) {
            transformableViewForVisibleType.setVisible(true);
            return;
        }
        this.mAnimationStartVisibleType = this.mVisibleType;
        transformableViewForVisibleType.transformFrom(transformableViewForVisibleType2);
        getViewForVisibleType(i).setVisibility(0);
        transformableViewForVisibleType2.transformTo(transformableViewForVisibleType, new Runnable(this, transformableViewForVisibleType2) { // from class: com.android.systemui.statusbar.NotificationContentView.2
            final NotificationContentView this$0;
            final TransformableView val$hiddenView;

            {
                this.this$0 = this;
                this.val$hiddenView = transformableViewForVisibleType2;
            }

            @Override // java.lang.Runnable
            public void run() {
                if (this.val$hiddenView != this.this$0.getTransformableViewForVisibleType(this.this$0.mVisibleType)) {
                    this.val$hiddenView.setVisible(false);
                }
                this.this$0.mAnimationStartVisibleType = -1;
            }
        });
    }

    private RemoteInputView applyRemoteInput(View view, NotificationData.Entry entry, boolean z, PendingIntent pendingIntent) {
        View findViewById = view.findViewById(16909218);
        if (findViewById instanceof FrameLayout) {
            RemoteInputView remoteInputView = (RemoteInputView) view.findViewWithTag(RemoteInputView.VIEW_TAG);
            if (remoteInputView != null) {
                remoteInputView.onNotificationUpdateOrReset();
            }
            RemoteInputView remoteInputView2 = remoteInputView;
            if (remoteInputView == null) {
                remoteInputView2 = remoteInputView;
                if (z) {
                    FrameLayout frameLayout = (FrameLayout) findViewById;
                    remoteInputView2 = RemoteInputView.inflate(this.mContext, frameLayout, entry, this.mRemoteInputController);
                    remoteInputView2.setVisibility(4);
                    frameLayout.addView(remoteInputView2, new FrameLayout.LayoutParams(-1, -1));
                }
            }
            if (z) {
                int i = entry.notification.getNotification().color;
                int i2 = i;
                if (i == 0) {
                    i2 = this.mContext.getColor(2131558591);
                }
                remoteInputView2.setBackgroundColor(NotificationColorUtil.ensureTextBackgroundColor(i2, this.mContext.getColor(2131558592), this.mContext.getColor(2131558593)));
                if (pendingIntent != null || remoteInputView2.isActive()) {
                    Notification.Action[] actionArr = entry.notification.getNotification().actions;
                    if (pendingIntent != null) {
                        remoteInputView2.setPendingIntent(pendingIntent);
                    }
                    if (remoteInputView2.updatePendingIntentFromActions(actionArr)) {
                        if (!remoteInputView2.isActive()) {
                            remoteInputView2.focus();
                        }
                    } else if (remoteInputView2.isActive()) {
                        remoteInputView2.close();
                    }
                }
            }
            return remoteInputView2;
        }
        return null;
    }

    private void applyRemoteInput(NotificationData.Entry entry) {
        if (this.mRemoteInputController == null) {
            return;
        }
        boolean z = false;
        boolean z2 = false;
        Notification.Action[] actionArr = entry.notification.getNotification().actions;
        if (actionArr != null) {
            int length = actionArr.length;
            int i = 0;
            while (true) {
                z = z2;
                if (i >= length) {
                    break;
                }
                Notification.Action action = actionArr[i];
                boolean z3 = z2;
                if (action.getRemoteInputs() != null) {
                    RemoteInput[] remoteInputs = action.getRemoteInputs();
                    int length2 = remoteInputs.length;
                    int i2 = 0;
                    while (true) {
                        z3 = z2;
                        if (i2 >= length2) {
                            break;
                        } else if (remoteInputs[i2].getAllowFreeFormInput()) {
                            z3 = true;
                            break;
                        } else {
                            i2++;
                        }
                    }
                }
                i++;
                z2 = z3;
            }
        }
        View view = this.mExpandedChild;
        if (view != null) {
            this.mExpandedRemoteInput = applyRemoteInput(view, entry, z, this.mPreviousExpandedRemoteInputIntent);
        } else {
            this.mExpandedRemoteInput = null;
        }
        View view2 = this.mHeadsUpChild;
        if (view2 != null) {
            this.mHeadsUpRemoteInput = applyRemoteInput(view2, entry, z, this.mPreviousHeadsUpRemoteInputIntent);
        } else {
            this.mHeadsUpRemoteInput = null;
        }
    }

    private float calculateTransformationAmount() {
        int height = getViewForVisibleType(this.mTransformationStartVisibleType).getHeight();
        int height2 = getViewForVisibleType(this.mVisibleType).getHeight();
        return Math.min(1.0f, Math.abs(this.mContentHeight - height) / Math.abs(height2 - height));
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

    private void forceUpdateVisibilities() {
        boolean z = this.mVisibleType != 0 ? this.mTransformationStartVisibleType == 0 : true;
        boolean z2 = this.mVisibleType != 1 ? this.mTransformationStartVisibleType == 1 : true;
        boolean z3 = this.mVisibleType != 2 ? this.mTransformationStartVisibleType == 2 : true;
        boolean z4 = this.mVisibleType != 3 ? this.mTransformationStartVisibleType == 3 : true;
        if (z) {
            this.mContractedWrapper.setVisible(true);
        } else {
            this.mContractedChild.setVisibility(4);
        }
        if (this.mExpandedChild != null) {
            if (z2) {
                this.mExpandedWrapper.setVisible(true);
            } else {
                this.mExpandedChild.setVisibility(4);
            }
        }
        if (this.mHeadsUpChild != null) {
            if (z3) {
                this.mHeadsUpWrapper.setVisible(true);
            } else {
                this.mHeadsUpChild.setVisibility(4);
            }
        }
        if (this.mSingleLineView != null) {
            if (z4) {
                this.mSingleLineView.setVisible(true);
            } else {
                this.mSingleLineView.setVisibility(4);
            }
        }
    }

    private int getMinContentHeightHint() {
        if (this.mIsChildInGroup && isVisibleOrTransitioning(3)) {
            return this.mContext.getResources().getDimensionPixelSize(17104963);
        }
        if (this.mHeadsUpChild != null && this.mExpandedChild != null) {
            boolean isTransitioningFromTo = !isTransitioningFromTo(2, 1) ? isTransitioningFromTo(1, 2) : true;
            boolean z = !isVisibleOrTransitioning(0) ? !this.mIsHeadsUp ? this.mHeadsupDisappearRunning : true : false;
            if (isTransitioningFromTo || z) {
                return Math.min(this.mHeadsUpChild.getHeight(), this.mExpandedChild.getHeight());
            }
        }
        if (this.mVisibleType != 1 || this.mContentHeightAtAnimationStart < 0 || this.mExpandedChild == null) {
            int height = (this.mHeadsUpChild == null || !isVisibleOrTransitioning(2)) ? this.mExpandedChild != null ? this.mExpandedChild.getHeight() : this.mContractedChild.getHeight() + this.mContext.getResources().getDimensionPixelSize(17104963) : this.mHeadsUpChild.getHeight();
            int i = height;
            if (this.mExpandedChild != null) {
                i = height;
                if (isVisibleOrTransitioning(1)) {
                    i = Math.min(height, this.mExpandedChild.getHeight());
                }
            }
            return i;
        }
        return Math.min(this.mContentHeightAtAnimationStart, this.mExpandedChild.getHeight());
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
            default:
                return this.mContractedChild;
        }
    }

    private NotificationViewWrapper getVisibleWrapper(int i) {
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

    private int getVisualTypeForHeight(float f) {
        boolean z = this.mExpandedChild == null;
        if (z || f != this.mExpandedChild.getHeight()) {
            if (this.mUserExpanding || !this.mIsChildInGroup || isGroupExpanded()) {
                if ((this.mIsHeadsUp || this.mHeadsupDisappearRunning) && this.mHeadsUpChild != null) {
                    return (f <= ((float) this.mHeadsUpChild.getHeight()) || z) ? 2 : 1;
                } else if (z) {
                    return 0;
                } else {
                    if (f <= this.mContractedChild.getHeight()) {
                        return (this.mIsChildInGroup && !isGroupExpanded() && this.mContainingNotification.isExpanded(true)) ? 1 : 0;
                    }
                    return 1;
                }
            }
            return 3;
        }
        return 1;
    }

    private boolean isGroupExpanded() {
        return this.mGroupManager.isGroupExpanded(this.mStatusBarNotification);
    }

    /* JADX WARN: Code restructure failed: missing block: B:5:0x0012, code lost:
        if (r3.mAnimationStartVisibleType == r4) goto L8;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private boolean isTransitioningFromTo(int i, int i2) {
        boolean z;
        if (this.mTransformationStartVisibleType != i) {
            z = false;
        }
        z = false;
        if (this.mVisibleType == i2) {
            z = true;
        }
        return z;
    }

    private boolean isVisibleOrTransitioning(int i) {
        boolean z = true;
        if (this.mVisibleType != i) {
            if (this.mTransformationStartVisibleType == i) {
                z = true;
            } else {
                z = true;
                if (this.mAnimationStartVisibleType != i) {
                    z = false;
                }
            }
        }
        return z;
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
            NotificationViewWrapper visibleWrapper = getVisibleWrapper(calculateVisibleType);
            if (visibleWrapper != null) {
                visibleWrapper.setContentHeight(this.mContentHeight, getMinContentHeightHint());
            }
            if (!z || ((calculateVisibleType != 1 || this.mExpandedChild == null) && ((calculateVisibleType != 2 || this.mHeadsUpChild == null) && ((calculateVisibleType != 3 || this.mSingleLineView == null) && calculateVisibleType != 0)))) {
                updateViewVisibilities(calculateVisibleType);
            } else {
                animateToVisibleType(calculateVisibleType);
            }
            this.mVisibleType = calculateVisibleType;
            if (z3) {
                focusExpandButtonIfNecessary();
            }
            updateBackgroundColor(z);
        }
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

    private boolean shouldContractedBeFixedSize() {
        return this.mBeforeN ? this.mContractedWrapper instanceof NotificationCustomViewWrapper : false;
    }

    private void transferRemoteInputFocus(int i) {
        if (i == 2 && this.mHeadsUpRemoteInput != null && this.mExpandedRemoteInput != null && this.mExpandedRemoteInput.isActive()) {
            this.mHeadsUpRemoteInput.stealFocusFrom(this.mExpandedRemoteInput);
        }
        if (i != 1 || this.mExpandedRemoteInput == null || this.mHeadsUpRemoteInput == null || !this.mHeadsUpRemoteInput.isActive()) {
            return;
        }
        this.mExpandedRemoteInput.stealFocusFrom(this.mHeadsUpRemoteInput);
    }

    private void updateBackgroundTransformation(float f) {
        int backgroundColor = getBackgroundColor(this.mVisibleType);
        int backgroundColor2 = getBackgroundColor(this.mTransformationStartVisibleType);
        int i = backgroundColor;
        if (backgroundColor != backgroundColor2) {
            int i2 = backgroundColor2;
            if (backgroundColor2 == 0) {
                i2 = this.mContainingNotification.getBackgroundColorWithoutTint();
            }
            int i3 = backgroundColor;
            if (backgroundColor == 0) {
                i3 = this.mContainingNotification.getBackgroundColorWithoutTint();
            }
            i = NotificationUtils.interpolateColors(i2, i3, f);
        }
        this.mContainingNotification.updateBackgroundAlpha(f);
        this.mContainingNotification.setContentBackground(i, false, this);
    }

    private void updateClipping() {
        if (!this.mClipToActualHeight) {
            setClipBounds(null);
            return;
        }
        this.mClipBounds.set(0, this.mClipTopAmount, getWidth(), this.mContentHeight);
        setClipBounds(this.mClipBounds);
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
        if (this.mTransformationStartVisibleType == -1 || this.mVisibleType == this.mTransformationStartVisibleType || getViewForVisibleType(this.mTransformationStartVisibleType) == null) {
            updateViewVisibilities(calculateVisibleType);
            updateBackgroundColor(false);
            return;
        }
        TransformableView transformableViewForVisibleType3 = getTransformableViewForVisibleType(this.mVisibleType);
        TransformableView transformableViewForVisibleType4 = getTransformableViewForVisibleType(this.mTransformationStartVisibleType);
        float calculateTransformationAmount = calculateTransformationAmount();
        transformableViewForVisibleType3.transformFrom(transformableViewForVisibleType4, calculateTransformationAmount);
        transformableViewForVisibleType4.transformTo(transformableViewForVisibleType3, calculateTransformationAmount);
        updateBackgroundTransformation(calculateTransformationAmount);
    }

    private boolean updateContractedHeaderWidth() {
        NotificationHeaderView notificationHeader = this.mContractedWrapper.getNotificationHeader();
        if (notificationHeader != null) {
            if (this.mExpandedChild == null || this.mExpandedWrapper.getNotificationHeader() == null) {
                int i = this.mNotificationContentMarginEnd;
                if (notificationHeader.getPaddingEnd() != i) {
                    int paddingLeft = notificationHeader.isLayoutRtl() ? i : notificationHeader.getPaddingLeft();
                    int paddingTop = notificationHeader.getPaddingTop();
                    if (notificationHeader.isLayoutRtl()) {
                        i = notificationHeader.getPaddingLeft();
                    }
                    notificationHeader.setPadding(paddingLeft, paddingTop, i, notificationHeader.getPaddingBottom());
                    notificationHeader.setShowWorkBadgeAtEnd(false);
                    return true;
                }
                return false;
            }
            NotificationHeaderView notificationHeader2 = this.mExpandedWrapper.getNotificationHeader();
            int measuredWidth = notificationHeader2.getMeasuredWidth() - notificationHeader2.getPaddingEnd();
            if (measuredWidth != notificationHeader.getMeasuredWidth() - notificationHeader2.getPaddingEnd()) {
                int measuredWidth2 = notificationHeader.getMeasuredWidth() - measuredWidth;
                int paddingLeft2 = notificationHeader.isLayoutRtl() ? measuredWidth2 : notificationHeader.getPaddingLeft();
                int paddingTop2 = notificationHeader.getPaddingTop();
                if (notificationHeader.isLayoutRtl()) {
                    measuredWidth2 = notificationHeader.getPaddingLeft();
                }
                notificationHeader.setPadding(paddingLeft2, paddingTop2, measuredWidth2, notificationHeader.getPaddingBottom());
                notificationHeader.setShowWorkBadgeAtEnd(true);
                return true;
            }
            return false;
        }
        return false;
    }

    private void updateShowingLegacyBackground() {
        if (this.mContractedChild != null) {
            this.mContractedWrapper.setShowingLegacyBackground(this.mShowingLegacyBackground);
        }
        if (this.mExpandedChild != null) {
            this.mExpandedWrapper.setShowingLegacyBackground(this.mShowingLegacyBackground);
        }
        if (this.mHeadsUpChild != null) {
            this.mHeadsUpWrapper.setShowingLegacyBackground(this.mShowingLegacyBackground);
        }
    }

    private void updateSingleLineView() {
        if (this.mIsChildInGroup) {
            this.mSingleLineView = this.mHybridGroupManager.bindFromNotification(this.mSingleLineView, this.mStatusBarNotification.getNotification());
        } else if (this.mSingleLineView != null) {
            removeView(this.mSingleLineView);
            this.mSingleLineView = null;
        }
    }

    private void updateViewVisibilities(int i) {
        this.mContractedWrapper.setVisible(i == 0);
        if (this.mExpandedChild != null) {
            this.mExpandedWrapper.setVisible(i == 1);
        }
        if (this.mHeadsUpChild != null) {
            this.mHeadsUpWrapper.setVisible(i == 2);
        }
        if (this.mSingleLineView != null) {
            this.mSingleLineView.setVisible(i == 3);
        }
    }

    private void updateVisibility() {
        setVisible(isShown());
    }

    public int calculateVisibleType() {
        if (!this.mUserExpanding) {
            int intrinsicHeight = this.mContainingNotification.getIntrinsicHeight();
            int i = this.mContentHeight;
            if (intrinsicHeight != 0) {
                i = Math.min(this.mContentHeight, intrinsicHeight);
            }
            return getVisualTypeForHeight(i);
        }
        int maxContentHeight = (!this.mIsChildInGroup || isGroupExpanded() || this.mContainingNotification.isExpanded(true)) ? this.mContainingNotification.getMaxContentHeight() : this.mContainingNotification.getShowingLayout().getMinHeight();
        int i2 = maxContentHeight;
        if (maxContentHeight == 0) {
            i2 = this.mContentHeight;
        }
        int visualTypeForHeight = getVisualTypeForHeight(i2);
        int visualTypeForHeight2 = (!this.mIsChildInGroup || isGroupExpanded()) ? getVisualTypeForHeight(this.mContainingNotification.getCollapsedHeight()) : 3;
        if (this.mTransformationStartVisibleType == visualTypeForHeight2) {
            visualTypeForHeight2 = visualTypeForHeight;
        }
        return visualTypeForHeight2;
    }

    public void closeRemoteInput() {
        if (this.mHeadsUpRemoteInput != null) {
            this.mHeadsUpRemoteInput.close();
        }
        if (this.mExpandedRemoteInput != null) {
            this.mExpandedRemoteInput.close();
        }
    }

    public int getBackgroundColor(int i) {
        NotificationViewWrapper visibleWrapper = getVisibleWrapper(i);
        int i2 = 0;
        if (visibleWrapper != null) {
            i2 = visibleWrapper.getCustomBackgroundColor();
        }
        return i2;
    }

    public int getBackgroundColorForExpansionState() {
        return getBackgroundColor((this.mContainingNotification.isGroupExpanded() || this.mContainingNotification.isUserLocked()) ? calculateVisibleType() : getVisibleType());
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

    public int getMaxHeight() {
        return this.mExpandedChild != null ? this.mExpandedChild.getHeight() : (!this.mIsHeadsUp || this.mHeadsUpChild == null) ? this.mContractedChild.getHeight() : this.mHeadsUpChild.getHeight();
    }

    public int getMinHeight() {
        return getMinHeight(false);
    }

    public int getMinHeight(boolean z) {
        return (z || !this.mIsChildInGroup || isGroupExpanded()) ? this.mContractedChild.getHeight() : this.mSingleLineView.getHeight();
    }

    public NotificationHeaderView getNotificationHeader() {
        NotificationHeaderView notificationHeaderView = null;
        if (this.mContractedChild != null) {
            notificationHeaderView = this.mContractedWrapper.getNotificationHeader();
        }
        NotificationHeaderView notificationHeaderView2 = notificationHeaderView;
        if (notificationHeaderView == null) {
            notificationHeaderView2 = notificationHeaderView;
            if (this.mExpandedChild != null) {
                notificationHeaderView2 = this.mExpandedWrapper.getNotificationHeader();
            }
        }
        NotificationHeaderView notificationHeaderView3 = notificationHeaderView2;
        if (notificationHeaderView2 == null) {
            notificationHeaderView3 = notificationHeaderView2;
            if (this.mHeadsUpChild != null) {
                notificationHeaderView3 = this.mHeadsUpWrapper.getNotificationHeader();
            }
        }
        return notificationHeaderView3;
    }

    public HybridNotificationView getSingleLineView() {
        return this.mSingleLineView;
    }

    public NotificationHeaderView getVisibleNotificationHeader() {
        NotificationHeaderView notificationHeaderView = null;
        NotificationViewWrapper visibleWrapper = getVisibleWrapper(this.mVisibleType);
        if (visibleWrapper != null) {
            notificationHeaderView = visibleWrapper.getNotificationHeader();
        }
        return notificationHeaderView;
    }

    public int getVisibleType() {
        return this.mVisibleType;
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    public boolean isContentExpandable() {
        return this.mExpandedChild != null;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateVisibility();
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeOnPreDrawListener(this.mEnableAnimationPredrawListener);
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int i5 = 0;
        if (this.mExpandedChild != null) {
            i5 = this.mExpandedChild.getHeight();
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

    @Override // android.widget.FrameLayout, android.view.View
    protected void onMeasure(int i, int i2) {
        int mode = View.MeasureSpec.getMode(i2);
        boolean z = mode == 1073741824;
        boolean z2 = mode == Integer.MIN_VALUE;
        int i3 = Integer.MAX_VALUE;
        int size = View.MeasureSpec.getSize(i);
        if (z || z2) {
            i3 = View.MeasureSpec.getSize(i2);
        }
        int i4 = 0;
        if (this.mExpandedChild != null) {
            int min = Math.min(i3, this.mNotificationMaxHeight);
            ViewGroup.LayoutParams layoutParams = this.mExpandedChild.getLayoutParams();
            if (layoutParams.height >= 0) {
                min = Math.min(i3, layoutParams.height);
            }
            this.mExpandedChild.measure(i, min == Integer.MAX_VALUE ? View.MeasureSpec.makeMeasureSpec(0, 0) : View.MeasureSpec.makeMeasureSpec(min, Integer.MIN_VALUE));
            i4 = Math.max(0, this.mExpandedChild.getMeasuredHeight());
        }
        int i5 = i4;
        if (this.mContractedChild != null) {
            int min2 = Math.min(i3, this.mSmallHeight);
            int makeMeasureSpec = shouldContractedBeFixedSize() ? View.MeasureSpec.makeMeasureSpec(min2, 1073741824) : View.MeasureSpec.makeMeasureSpec(min2, Integer.MIN_VALUE);
            this.mContractedChild.measure(i, makeMeasureSpec);
            int measuredHeight = this.mContractedChild.getMeasuredHeight();
            if (measuredHeight < this.mMinContractedHeight) {
                makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(this.mMinContractedHeight, 1073741824);
                this.mContractedChild.measure(i, makeMeasureSpec);
            }
            int max = Math.max(i4, measuredHeight);
            if (updateContractedHeaderWidth()) {
                this.mContractedChild.measure(i, makeMeasureSpec);
            }
            i5 = max;
            if (this.mExpandedChild != null) {
                i5 = max;
                if (this.mContractedChild.getMeasuredHeight() > this.mExpandedChild.getMeasuredHeight()) {
                    this.mExpandedChild.measure(i, View.MeasureSpec.makeMeasureSpec(this.mContractedChild.getMeasuredHeight(), 1073741824));
                    i5 = max;
                }
            }
        }
        int i6 = i5;
        if (this.mHeadsUpChild != null) {
            int min3 = Math.min(i3, this.mHeadsUpHeight);
            ViewGroup.LayoutParams layoutParams2 = this.mHeadsUpChild.getLayoutParams();
            int i7 = min3;
            if (layoutParams2.height >= 0) {
                i7 = Math.min(min3, layoutParams2.height);
            }
            this.mHeadsUpChild.measure(i, View.MeasureSpec.makeMeasureSpec(i7, Integer.MIN_VALUE));
            i6 = Math.max(i5, this.mHeadsUpChild.getMeasuredHeight());
        }
        int i8 = i6;
        if (this.mSingleLineView != null) {
            int i9 = i;
            if (this.mSingleLineWidthIndention != 0) {
                i9 = i;
                if (View.MeasureSpec.getMode(i) != 0) {
                    i9 = View.MeasureSpec.makeMeasureSpec((size - this.mSingleLineWidthIndention) + this.mSingleLineView.getPaddingEnd(), Integer.MIN_VALUE);
                }
            }
            this.mSingleLineView.measure(i9, View.MeasureSpec.makeMeasureSpec(i3, Integer.MIN_VALUE));
            i8 = Math.max(i6, this.mSingleLineView.getMeasuredHeight());
        }
        setMeasuredDimension(size, Math.min(i8, i3));
    }

    public void onNotificationUpdated(NotificationData.Entry entry) {
        this.mStatusBarNotification = entry.notification;
        this.mBeforeN = entry.targetSdk < 24;
        updateSingleLineView();
        applyRemoteInput(entry);
        if (this.mContractedChild != null) {
            this.mContractedWrapper.notifyContentUpdated(entry.notification);
        }
        if (this.mExpandedChild != null) {
            this.mExpandedWrapper.notifyContentUpdated(entry.notification);
        }
        if (this.mHeadsUpChild != null) {
            this.mHeadsUpWrapper.notifyContentUpdated(entry.notification);
        }
        updateShowingLegacyBackground();
        this.mForceSelectNextLayout = true;
        setDark(this.mDark, false, 0L);
        this.mPreviousExpandedRemoteInputIntent = null;
        this.mPreviousHeadsUpRemoteInputIntent = null;
    }

    @Override // android.view.View
    protected void onVisibilityChanged(View view, int i) {
        super.onVisibilityChanged(view, i);
        updateVisibility();
    }

    public void reInflateViews() {
        if (!this.mIsChildInGroup || this.mSingleLineView == null) {
            return;
        }
        removeView(this.mSingleLineView);
        this.mSingleLineView = null;
        updateSingleLineView();
    }

    public void requestSelectLayout(boolean z) {
        selectLayout(z, false);
    }

    public void reset() {
        if (this.mContractedChild != null) {
            this.mContractedChild.animate().cancel();
            removeView(this.mContractedChild);
        }
        this.mPreviousExpandedRemoteInputIntent = null;
        if (this.mExpandedRemoteInput != null) {
            this.mExpandedRemoteInput.onNotificationUpdateOrReset();
            if (this.mExpandedRemoteInput.isActive()) {
                this.mPreviousExpandedRemoteInputIntent = this.mExpandedRemoteInput.getPendingIntent();
            }
        }
        if (this.mExpandedChild != null) {
            this.mExpandedChild.animate().cancel();
            removeView(this.mExpandedChild);
            this.mExpandedRemoteInput = null;
        }
        this.mPreviousHeadsUpRemoteInputIntent = null;
        if (this.mHeadsUpRemoteInput != null) {
            this.mHeadsUpRemoteInput.onNotificationUpdateOrReset();
            if (this.mHeadsUpRemoteInput.isActive()) {
                this.mPreviousHeadsUpRemoteInputIntent = this.mHeadsUpRemoteInput.getPendingIntent();
            }
        }
        if (this.mHeadsUpChild != null) {
            this.mHeadsUpChild.animate().cancel();
            removeView(this.mHeadsUpChild);
            this.mHeadsUpRemoteInput = null;
        }
        this.mContractedChild = null;
        this.mExpandedChild = null;
        this.mHeadsUpChild = null;
    }

    public void setClipToActualHeight(boolean z) {
        this.mClipToActualHeight = z;
        updateClipping();
    }

    public void setClipTopAmount(int i) {
        this.mClipTopAmount = i;
        updateClipping();
    }

    public void setContainingNotification(ExpandableNotificationRow expandableNotificationRow) {
        this.mContainingNotification = expandableNotificationRow;
    }

    public void setContentHeight(int i) {
        this.mContentHeight = Math.max(Math.min(i, getHeight()), getMinHeight());
        selectLayout(this.mAnimate, false);
        int minContentHeightHint = getMinContentHeightHint();
        NotificationViewWrapper visibleWrapper = getVisibleWrapper(this.mVisibleType);
        if (visibleWrapper != null) {
            visibleWrapper.setContentHeight(this.mContentHeight, minContentHeightHint);
        }
        NotificationViewWrapper visibleWrapper2 = getVisibleWrapper(this.mTransformationStartVisibleType);
        if (visibleWrapper2 != null) {
            visibleWrapper2.setContentHeight(this.mContentHeight, minContentHeightHint);
        }
        updateClipping();
        invalidateOutline();
    }

    public void setContentHeightAnimating(boolean z) {
        if (z) {
            return;
        }
        this.mContentHeightAtAnimationStart = -1;
    }

    public void setContractedChild(View view) {
        if (this.mContractedChild != null) {
            this.mContractedChild.animate().cancel();
            removeView(this.mContractedChild);
        }
        addView(view);
        this.mContractedChild = view;
        this.mContractedWrapper = NotificationViewWrapper.wrap(getContext(), view, this.mContainingNotification);
        this.mContractedWrapper.setDark(this.mDark, false, 0L);
    }

    public void setDark(boolean z, boolean z2, long j) {
        if (this.mContractedChild == null) {
            return;
        }
        this.mDark = z;
        if (this.mVisibleType == 0 || !z) {
            this.mContractedWrapper.setDark(z, z2, j);
        }
        if (this.mVisibleType == 1 || (this.mExpandedChild != null && !z)) {
            this.mExpandedWrapper.setDark(z, z2, j);
        }
        if (this.mVisibleType == 2 || (this.mHeadsUpChild != null && !z)) {
            this.mHeadsUpWrapper.setDark(z, z2, j);
        }
        if (this.mSingleLineView != null) {
            if (this.mVisibleType == 3 || !z) {
                this.mSingleLineView.setDark(z, z2, j);
            }
        }
    }

    public void setExpandClickListener(View.OnClickListener onClickListener) {
        this.mExpandClickListener = onClickListener;
    }

    public void setExpandedChild(View view) {
        if (this.mExpandedChild != null) {
            this.mExpandedChild.animate().cancel();
            removeView(this.mExpandedChild);
        }
        addView(view);
        this.mExpandedChild = view;
        this.mExpandedWrapper = NotificationViewWrapper.wrap(getContext(), view, this.mContainingNotification);
    }

    public void setFocusOnVisibilityChange() {
        this.mFocusOnVisibilityChange = true;
    }

    public void setGroupManager(NotificationGroupManager notificationGroupManager) {
        this.mGroupManager = notificationGroupManager;
    }

    public void setHeadsUp(boolean z) {
        this.mIsHeadsUp = z;
        selectLayout(false, true);
        updateExpandButtons(this.mExpandable);
    }

    public void setHeadsUpChild(View view) {
        if (this.mHeadsUpChild != null) {
            this.mHeadsUpChild.animate().cancel();
            removeView(this.mHeadsUpChild);
        }
        addView(view);
        this.mHeadsUpChild = view;
        this.mHeadsUpWrapper = NotificationViewWrapper.wrap(getContext(), view, this.mContainingNotification);
    }

    public void setHeadsupDisappearRunning(boolean z) {
        this.mHeadsupDisappearRunning = z;
        selectLayout(false, true);
    }

    public void setHeights(int i, int i2, int i3) {
        this.mSmallHeight = i;
        this.mHeadsUpHeight = i2;
        this.mNotificationMaxHeight = i3;
    }

    public void setIsChildInGroup(boolean z) {
        this.mIsChildInGroup = z;
        updateSingleLineView();
    }

    public void setRemoteInputController(RemoteInputController remoteInputController) {
        this.mRemoteInputController = remoteInputController;
    }

    public void setRemoved() {
        if (this.mExpandedRemoteInput != null) {
            this.mExpandedRemoteInput.setRemoved();
        }
        if (this.mHeadsUpRemoteInput != null) {
            this.mHeadsUpRemoteInput.setRemoved();
        }
    }

    public void setShowingLegacyBackground(boolean z) {
        this.mShowingLegacyBackground = z;
        updateShowingLegacyBackground();
    }

    public void setSingleLineWidthIndention(int i) {
        if (i != this.mSingleLineWidthIndention) {
            this.mSingleLineWidthIndention = i;
            this.mContainingNotification.forceLayout();
            forceLayout();
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

    public void updateBackgroundColor(boolean z) {
        int backgroundColor = getBackgroundColor(this.mVisibleType);
        this.mContainingNotification.resetBackgroundAlpha();
        this.mContainingNotification.setContentBackground(backgroundColor, z, this);
    }

    public void updateExpandButtons(boolean z) {
        this.mExpandable = z;
        boolean z2 = z;
        if (this.mExpandedChild != null) {
            z2 = z;
            if (this.mExpandedChild.getHeight() != 0) {
                if (!this.mIsHeadsUp || this.mHeadsUpChild == null) {
                    z2 = z;
                    if (this.mExpandedChild.getHeight() == this.mContractedChild.getHeight()) {
                        z2 = false;
                    }
                } else {
                    z2 = z;
                    if (this.mExpandedChild.getHeight() == this.mHeadsUpChild.getHeight()) {
                        z2 = false;
                    }
                }
            }
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
    }
}

package com.android.systemui.statusbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.util.AttributeSet;
import android.util.FloatProperty;
import android.util.Property;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.NotificationHeaderView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Chronometer;
import android.widget.ImageView;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.util.NotificationColorUtil;
import com.android.systemui.classifier.FalsingManager;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.notification.HybridNotificationView;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.stack.NotificationChildrenContainer;
import com.android.systemui.statusbar.stack.NotificationStackScrollLayout;
import com.android.systemui.statusbar.stack.StackScrollState;
import com.android.systemui.statusbar.stack.StackStateAnimator;
import java.util.ArrayList;
import java.util.List;
/* loaded from: a.zip:com/android/systemui/statusbar/ExpandableNotificationRow.class */
public class ExpandableNotificationRow extends ActivatableNotificationView {
    private static final Property<ExpandableNotificationRow, Float> TRANSLATE_CONTENT = new FloatProperty<ExpandableNotificationRow>("translate") { // from class: com.android.systemui.statusbar.ExpandableNotificationRow.2
        @Override // android.util.Property
        public Float get(ExpandableNotificationRow expandableNotificationRow) {
            return Float.valueOf(expandableNotificationRow.getTranslation());
        }

        @Override // android.util.FloatProperty
        public void setValue(ExpandableNotificationRow expandableNotificationRow, float f) {
            expandableNotificationRow.setTranslation(f);
        }
    };
    private String mAppName;
    private View mChildAfterViewWhenDismissed;
    private NotificationChildrenContainer mChildrenContainer;
    private ViewStub mChildrenContainerStub;
    private boolean mChildrenExpanded;
    private boolean mDismissed;
    private NotificationData.Entry mEntry;
    private View.OnClickListener mExpandClickListener;
    private boolean mExpandable;
    private boolean mExpandedWhenPinned;
    private FalsingManager mFalsingManager;
    private boolean mForceUnlocked;
    private boolean mGroupExpansionChanging;
    private NotificationGroupManager mGroupManager;
    private View mGroupParentWhenDismissed;
    private NotificationGuts mGuts;
    private ViewStub mGutsStub;
    private boolean mHasUserChangedExpansion;
    private int mHeadsUpHeight;
    private HeadsUpManager mHeadsUpManager;
    private boolean mHeadsupDisappearRunning;
    private boolean mHideSensitiveForIntrinsicHeight;
    private boolean mIconAnimationRunning;
    private int mIncreasedPaddingBetweenElements;
    private boolean mIsHeadsUp;
    private boolean mIsPinned;
    private boolean mIsSummaryWithChildren;
    private boolean mIsSystemChildExpanded;
    private boolean mIsSystemExpanded;
    private boolean mJustClicked;
    private boolean mKeepInParent;
    private boolean mLastChronometerRunning;
    private ExpansionLogger mLogger;
    private String mLoggingKey;
    private int mMaxExpandHeight;
    private int mMaxHeadsUpHeight;
    private int mMaxHeadsUpHeightLegacy;
    private int mNotificationColor;
    private int mNotificationMaxHeight;
    private int mNotificationMinHeight;
    private int mNotificationMinHeightLegacy;
    private ExpandableNotificationRow mNotificationParent;
    private View.OnClickListener mOnClickListener;
    private OnExpandClickListener mOnExpandClickListener;
    private boolean mOnKeyguard;
    private NotificationContentView mPrivateLayout;
    private NotificationContentView mPublicLayout;
    private boolean mRefocusOnDismiss;
    private boolean mRemoved;
    private boolean mSensitive;
    private boolean mSensitiveHiddenInGeneral;
    private NotificationSettingsIconRow mSettingsIconRow;
    private ViewStub mSettingsIconRowStub;
    private boolean mShowNoBackground;
    private boolean mShowingPublic;
    private boolean mShowingPublicInitialized;
    private StatusBarNotification mStatusBarNotification;
    private Animator mTranslateAnim;
    private ArrayList<View> mTranslateableViews;
    private boolean mUserExpanded;
    private boolean mUserLocked;
    private View mVetoButton;

    /* loaded from: a.zip:com/android/systemui/statusbar/ExpandableNotificationRow$ExpansionLogger.class */
    public interface ExpansionLogger {
        void logNotificationExpansion(String str, boolean z, boolean z2);
    }

    /* loaded from: a.zip:com/android/systemui/statusbar/ExpandableNotificationRow$OnExpandClickListener.class */
    public interface OnExpandClickListener {
        void onExpandClicked(NotificationData.Entry entry, boolean z);
    }

    public ExpandableNotificationRow(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mLastChronometerRunning = true;
        this.mExpandClickListener = new View.OnClickListener(this) { // from class: com.android.systemui.statusbar.ExpandableNotificationRow.1
            final ExpandableNotificationRow this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                boolean z;
                if (!this.this$0.mShowingPublic && this.this$0.mGroupManager.isSummaryOfGroup(this.this$0.mStatusBarNotification)) {
                    this.this$0.mGroupExpansionChanging = true;
                    boolean isGroupExpanded = this.this$0.mGroupManager.isGroupExpanded(this.this$0.mStatusBarNotification);
                    boolean z2 = this.this$0.mGroupManager.toggleGroupExpansion(this.this$0.mStatusBarNotification);
                    this.this$0.mOnExpandClickListener.onExpandClicked(this.this$0.mEntry, z2);
                    MetricsLogger.action(this.this$0.mContext, 408, z2);
                    this.this$0.logExpansionEvent(true, isGroupExpanded);
                    return;
                }
                if (view.isAccessibilityFocused()) {
                    this.this$0.mPrivateLayout.setFocusOnVisibilityChange();
                }
                if (this.this$0.isPinned()) {
                    z = !this.this$0.mExpandedWhenPinned;
                    this.this$0.mExpandedWhenPinned = z;
                } else {
                    z = !this.this$0.isExpanded();
                    this.this$0.setUserExpanded(z);
                }
                this.this$0.notifyHeightChanged(true);
                this.this$0.mOnExpandClickListener.onExpandClicked(this.this$0.mEntry, z);
                MetricsLogger.action(this.this$0.mContext, 407, z);
            }
        };
        this.mFalsingManager = FalsingManager.getInstance(context);
        initDimens();
    }

    private void animateShowingPublic(long j, long j2) {
        View[] viewArr = this.mIsSummaryWithChildren ? new View[]{this.mChildrenContainer} : new View[]{this.mPrivateLayout};
        View[] viewArr2 = {this.mPublicLayout};
        View[] viewArr3 = this.mShowingPublic ? viewArr : viewArr2;
        if (this.mShowingPublic) {
            viewArr = viewArr2;
        }
        for (View view : viewArr3) {
            view.setVisibility(0);
            view.animate().cancel();
            view.animate().alpha(0.0f).setStartDelay(j).setDuration(j2).withEndAction(new Runnable(this, view) { // from class: com.android.systemui.statusbar.ExpandableNotificationRow.7
                final ExpandableNotificationRow this$0;
                final View val$hiddenView;

                {
                    this.this$0 = this;
                    this.val$hiddenView = view;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.val$hiddenView.setVisibility(4);
                }
            });
        }
        for (View view2 : viewArr) {
            view2.setVisibility(0);
            view2.setAlpha(0.0f);
            view2.animate().cancel();
            view2.animate().alpha(1.0f).setStartDelay(j).setDuration(j2);
        }
    }

    private int getFontScaledHeight(int i) {
        return (int) (getResources().getDimensionPixelSize(i) * Math.max(1.0f, getResources().getDisplayMetrics().scaledDensity / getResources().getDisplayMetrics().density));
    }

    private NotificationHeaderView getVisibleNotificationHeader() {
        return this.mIsSummaryWithChildren ? this.mChildrenContainer.getHeaderView() : getShowingLayout().getVisibleNotificationHeader();
    }

    private void initDimens() {
        this.mNotificationMinHeightLegacy = getFontScaledHeight(2131689786);
        this.mNotificationMinHeight = getFontScaledHeight(2131689785);
        this.mNotificationMaxHeight = getFontScaledHeight(2131689787);
        this.mMaxHeadsUpHeightLegacy = getFontScaledHeight(2131689788);
        this.mMaxHeadsUpHeight = getFontScaledHeight(2131689789);
        this.mIncreasedPaddingBetweenElements = getResources().getDimensionPixelSize(2131689878);
    }

    private boolean isSystemChildExpanded() {
        return this.mIsSystemChildExpanded;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void logExpansionEvent(boolean z, boolean z2) {
        boolean isExpanded = isExpanded();
        if (this.mIsSummaryWithChildren) {
            isExpanded = this.mGroupManager.isGroupExpanded(this.mStatusBarNotification);
        }
        if (z2 == isExpanded || this.mLogger == null) {
            return;
        }
        this.mLogger.logNotificationExpansion(this.mLoggingKey, z, isExpanded);
    }

    private void onChildrenCountChanged() {
        this.mIsSummaryWithChildren = (!BaseStatusBar.ENABLE_CHILD_NOTIFICATIONS || this.mChildrenContainer == null) ? false : this.mChildrenContainer.getNotificationChildCount() > 0;
        if (this.mIsSummaryWithChildren && this.mChildrenContainer.getHeaderView() == null) {
            this.mChildrenContainer.recreateNotificationHeader(this.mExpandClickListener, this.mEntry.notification);
        }
        getShowingLayout().updateBackgroundColor(false);
        this.mPrivateLayout.updateExpandButtons(isExpandable());
        updateChildrenHeaderAppearance();
        updateChildrenVisibility();
    }

    private void setChronometerRunning(boolean z, NotificationContentView notificationContentView) {
        if (notificationContentView != null) {
            boolean isPinned = !z ? isPinned() : true;
            View contractedChild = notificationContentView.getContractedChild();
            View expandedChild = notificationContentView.getExpandedChild();
            View headsUpChild = notificationContentView.getHeadsUpChild();
            setChronometerRunningForChild(isPinned, contractedChild);
            setChronometerRunningForChild(isPinned, expandedChild);
            setChronometerRunningForChild(isPinned, headsUpChild);
        }
    }

    private void setChronometerRunningForChild(boolean z, View view) {
        if (view != null) {
            View findViewById = view.findViewById(16909229);
            if (findViewById instanceof Chronometer) {
                ((Chronometer) findViewById).setStarted(z);
            }
        }
    }

    private void setIconAnimationRunning(boolean z, NotificationContentView notificationContentView) {
        if (notificationContentView != null) {
            View contractedChild = notificationContentView.getContractedChild();
            View expandedChild = notificationContentView.getExpandedChild();
            View headsUpChild = notificationContentView.getHeadsUpChild();
            setIconAnimationRunningForChild(z, contractedChild);
            setIconAnimationRunningForChild(z, expandedChild);
            setIconAnimationRunningForChild(z, headsUpChild);
        }
    }

    private void setIconAnimationRunningForChild(boolean z, View view) {
        if (view != null) {
            setIconRunning((ImageView) view.findViewById(16908294), z);
            setIconRunning((ImageView) view.findViewById(16908356), z);
        }
    }

    private void setIconRunning(ImageView imageView, boolean z) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AnimationDrawable) {
                AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
                if (z) {
                    animationDrawable.start();
                } else {
                    animationDrawable.stop();
                }
            } else if (drawable instanceof AnimatedVectorDrawable) {
                AnimatedVectorDrawable animatedVectorDrawable = (AnimatedVectorDrawable) drawable;
                if (z) {
                    animatedVectorDrawable.start();
                } else {
                    animatedVectorDrawable.stop();
                }
            }
        }
    }

    private void updateChildrenVisibility() {
        this.mPrivateLayout.setVisibility((this.mShowingPublic || this.mIsSummaryWithChildren) ? 4 : 0);
        if (this.mChildrenContainer != null) {
            this.mChildrenContainer.setVisibility((this.mShowingPublic || !this.mIsSummaryWithChildren) ? 4 : 0);
            this.mChildrenContainer.updateHeaderVisibility((this.mShowingPublic || !this.mIsSummaryWithChildren) ? 4 : 0);
        }
        updateLimits();
    }

    private void updateClickAndFocus() {
        boolean isGroupExpanded = isChildInGroup() ? isGroupExpanded() : true;
        boolean z = this.mOnClickListener != null ? isGroupExpanded : false;
        if (isFocusable() != isGroupExpanded) {
            setFocusable(isGroupExpanded);
        }
        if (isClickable() != z) {
            setClickable(z);
        }
    }

    private void updateLimits() {
        updateLimitsForView(this.mPrivateLayout);
        updateLimitsForView(this.mPublicLayout);
    }

    private void updateLimitsForView(NotificationContentView notificationContentView) {
        boolean z = notificationContentView.getContractedChild().getId() != 16909232;
        boolean z2 = this.mEntry.targetSdk < 24;
        int i = (z && z2 && !this.mIsSummaryWithChildren) ? this.mNotificationMinHeightLegacy : this.mNotificationMinHeight;
        boolean z3 = false;
        if (notificationContentView.getHeadsUpChild() != null) {
            z3 = false;
            if (notificationContentView.getHeadsUpChild().getId() != 16909232) {
                z3 = true;
            }
        }
        notificationContentView.setHeights(i, (z3 && z2) ? this.mMaxHeadsUpHeightLegacy : this.mMaxHeadsUpHeight, this.mNotificationMaxHeight);
    }

    private void updateMaxHeights() {
        int intrinsicHeight = getIntrinsicHeight();
        View expandedChild = this.mPrivateLayout.getExpandedChild();
        View view = expandedChild;
        if (expandedChild == null) {
            view = this.mPrivateLayout.getContractedChild();
        }
        this.mMaxExpandHeight = view.getHeight();
        View headsUpChild = this.mPrivateLayout.getHeadsUpChild();
        View view2 = headsUpChild;
        if (headsUpChild == null) {
            view2 = this.mPrivateLayout.getContractedChild();
        }
        this.mHeadsUpHeight = view2.getHeight();
        if (intrinsicHeight != getIntrinsicHeight()) {
            notifyHeightChanged(false);
        }
    }

    private void updateNotificationColor() {
        this.mNotificationColor = NotificationColorUtil.resolveContrastColor(this.mContext, getStatusBarNotification().getNotification().color);
    }

    public void addChildNotification(ExpandableNotificationRow expandableNotificationRow, int i) {
        if (this.mChildrenContainer == null) {
            this.mChildrenContainerStub.inflate();
        }
        this.mChildrenContainer.addNotification(expandableNotificationRow, i);
        onChildrenCountChanged();
        expandableNotificationRow.setIsChildInGroup(true, this);
    }

    public void animateTranslateNotification(float f) {
        if (this.mTranslateAnim != null) {
            this.mTranslateAnim.cancel();
        }
        this.mTranslateAnim = getTranslateViewAnimator(f, null);
        if (this.mTranslateAnim != null) {
            this.mTranslateAnim.start();
        }
    }

    public boolean applyChildOrder(List<ExpandableNotificationRow> list) {
        return this.mChildrenContainer != null ? this.mChildrenContainer.applyChildOrder(list) : false;
    }

    public void applyChildrenState(StackScrollState stackScrollState) {
        if (this.mIsSummaryWithChildren) {
            this.mChildrenContainer.applyState(stackScrollState);
        }
    }

    @Override // com.android.systemui.statusbar.ExpandableView
    public boolean areChildrenExpanded() {
        return this.mChildrenExpanded;
    }

    public boolean areGutsExposed() {
        return this.mGuts != null ? this.mGuts.areGutsExposed() : false;
    }

    /* JADX WARN: Code restructure failed: missing block: B:7:0x0018, code lost:
        if (r2.mSensitiveHiddenInGeneral != false) goto L10;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public boolean canViewBeDismissed() {
        boolean z;
        if (isClearable()) {
            z = true;
            if (this.mShowingPublic) {
                z = true;
            }
            return z;
        }
        z = false;
        return z;
    }

    public void closeRemoteInput() {
        this.mPrivateLayout.closeRemoteInput();
        this.mPublicLayout.closeRemoteInput();
    }

    @Override // com.android.systemui.statusbar.ActivatableNotificationView
    protected boolean disallowSingleClick(MotionEvent motionEvent) {
        float x = motionEvent.getX();
        float y = motionEvent.getY();
        NotificationHeaderView visibleNotificationHeader = getVisibleNotificationHeader();
        return visibleNotificationHeader != null ? visibleNotificationHeader.isInTouchRect(x - getTranslation(), y) : super.disallowSingleClick(motionEvent);
    }

    public View getChildAfterViewWhenDismissed() {
        return this.mChildAfterViewWhenDismissed;
    }

    public NotificationChildrenContainer getChildrenContainer() {
        return this.mChildrenContainer;
    }

    public void getChildrenStates(StackScrollState stackScrollState) {
        if (this.mIsSummaryWithChildren) {
            this.mChildrenContainer.getState(stackScrollState, stackScrollState.getViewStateForView(this));
        }
    }

    @Override // com.android.systemui.statusbar.ExpandableView
    public int getCollapsedHeight() {
        return (!this.mIsSummaryWithChildren || this.mShowingPublic) ? getMinHeight() : this.mChildrenContainer.getCollapsedHeight();
    }

    @Override // com.android.systemui.statusbar.ActivatableNotificationView
    protected View getContentView() {
        return this.mIsSummaryWithChildren ? this.mChildrenContainer : getShowingLayout();
    }

    @Override // com.android.systemui.statusbar.ExpandableView
    public int getExtraBottomPadding() {
        if (this.mIsSummaryWithChildren && isGroupExpanded()) {
            return this.mIncreasedPaddingBetweenElements;
        }
        return 0;
    }

    public View getGroupParentWhenDismissed() {
        return this.mGroupParentWhenDismissed;
    }

    public NotificationGuts getGuts() {
        return this.mGuts;
    }

    @Override // com.android.systemui.statusbar.ExpandableView
    public float getIncreasedPaddingAmount() {
        if (this.mIsSummaryWithChildren) {
            if (isGroupExpanded()) {
                return 1.0f;
            }
            if (isUserLocked()) {
                return this.mChildrenContainer.getGroupExpandFraction();
            }
            return 0.0f;
        }
        return 0.0f;
    }

    @Override // com.android.systemui.statusbar.ExpandableView
    public int getIntrinsicHeight() {
        return isUserLocked() ? getActualHeight() : (this.mGuts == null || !this.mGuts.areGutsExposed()) ? (!isChildInGroup() || isGroupExpanded()) ? (this.mSensitive && this.mHideSensitiveForIntrinsicHeight) ? getMinHeight() : (!this.mIsSummaryWithChildren || this.mOnKeyguard) ? (this.mIsHeadsUp || this.mHeadsupDisappearRunning) ? (isPinned() || this.mHeadsupDisappearRunning) ? getPinnedHeadsUpHeight(true) : isExpanded() ? Math.max(getMaxExpandHeight(), this.mHeadsUpHeight) : Math.max(getCollapsedHeight(), this.mHeadsUpHeight) : isExpanded() ? getMaxExpandHeight() : getCollapsedHeight() : this.mChildrenContainer.getIntrinsicHeight() : this.mPrivateLayout.getMinHeight() : this.mGuts.getHeight();
    }

    @Override // com.android.systemui.statusbar.ExpandableView
    public int getMaxContentHeight() {
        return (!this.mIsSummaryWithChildren || this.mShowingPublic) ? getShowingLayout().getMaxHeight() : this.mChildrenContainer.getMaxContentHeight();
    }

    public int getMaxExpandHeight() {
        return this.mMaxExpandHeight;
    }

    @Override // com.android.systemui.statusbar.ExpandableView
    public int getMinHeight() {
        return (this.mIsHeadsUp && this.mHeadsUpManager.isTrackingHeadsUp()) ? getPinnedHeadsUpHeight(false) : (!this.mIsSummaryWithChildren || isGroupExpanded() || this.mShowingPublic) ? this.mIsHeadsUp ? this.mHeadsUpHeight : getShowingLayout().getMinHeight() : this.mChildrenContainer.getMinHeight();
    }

    public List<ExpandableNotificationRow> getNotificationChildren() {
        List<ExpandableNotificationRow> list = null;
        if (this.mChildrenContainer != null) {
            list = this.mChildrenContainer.getNotificationChildren();
        }
        return list;
    }

    public int getNotificationColor() {
        return this.mNotificationColor;
    }

    public NotificationHeaderView getNotificationHeader() {
        return this.mIsSummaryWithChildren ? this.mChildrenContainer.getHeaderView() : this.mPrivateLayout.getNotificationHeader();
    }

    public ExpandableNotificationRow getNotificationParent() {
        return this.mNotificationParent;
    }

    public int getPinnedHeadsUpHeight(boolean z) {
        return this.mIsSummaryWithChildren ? this.mChildrenContainer.getIntrinsicHeight() : this.mExpandedWhenPinned ? Math.max(getMaxExpandHeight(), this.mHeadsUpHeight) : z ? Math.max(getCollapsedHeight(), this.mHeadsUpHeight) : this.mHeadsUpHeight;
    }

    public int getPositionOfChild(ExpandableNotificationRow expandableNotificationRow) {
        if (this.mIsSummaryWithChildren) {
            return this.mChildrenContainer.getPositionInLinearLayout(expandableNotificationRow);
        }
        return 0;
    }

    public NotificationContentView getPrivateLayout() {
        return this.mPrivateLayout;
    }

    public NotificationContentView getPublicLayout() {
        return this.mPublicLayout;
    }

    public NotificationSettingsIconRow getSettingsRow() {
        if (this.mSettingsIconRow == null) {
            this.mSettingsIconRowStub.inflate();
        }
        return this.mSettingsIconRow;
    }

    public NotificationContentView getShowingLayout() {
        return this.mShowingPublic ? this.mPublicLayout : this.mPrivateLayout;
    }

    public HybridNotificationView getSingleLineView() {
        return this.mPrivateLayout.getSingleLineView();
    }

    public float getSpaceForGear() {
        if (this.mSettingsIconRow != null) {
            return this.mSettingsIconRow.getSpaceForGear();
        }
        return 0.0f;
    }

    public StatusBarNotification getStatusBarNotification() {
        return this.mStatusBarNotification;
    }

    public Animator getTranslateViewAnimator(float f, ValueAnimator.AnimatorUpdateListener animatorUpdateListener) {
        if (this.mTranslateAnim != null) {
            this.mTranslateAnim.cancel();
        }
        if (areGutsExposed()) {
            return null;
        }
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this, TRANSLATE_CONTENT, f);
        if (animatorUpdateListener != null) {
            ofFloat.addUpdateListener(animatorUpdateListener);
        }
        ofFloat.addListener(new AnimatorListenerAdapter(this, f) { // from class: com.android.systemui.statusbar.ExpandableNotificationRow.6
            boolean cancelled = false;
            final ExpandableNotificationRow this$0;
            final float val$leftTarget;

            {
                this.this$0 = this;
                this.val$leftTarget = f;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
                this.cancelled = true;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (this.cancelled || this.this$0.mSettingsIconRow == null || this.val$leftTarget != 0.0f) {
                    return;
                }
                this.this$0.mSettingsIconRow.resetState();
                this.this$0.mTranslateAnim = null;
            }
        });
        this.mTranslateAnim = ofFloat;
        return ofFloat;
    }

    @Override // com.android.systemui.statusbar.ExpandableView
    public float getTranslation() {
        if (this.mTranslateableViews == null || this.mTranslateableViews.size() <= 0) {
            return 0.0f;
        }
        return this.mTranslateableViews.get(0).getTranslationX();
    }

    public ExpandableNotificationRow getViewAtPosition(float f) {
        if (this.mIsSummaryWithChildren && this.mChildrenExpanded) {
            ExpandableNotificationRow viewAtPosition = this.mChildrenContainer.getViewAtPosition(f);
            if (viewAtPosition == null) {
                viewAtPosition = this;
            }
            return viewAtPosition;
        }
        return this;
    }

    @Override // com.android.systemui.statusbar.ActivatableNotificationView
    protected boolean handleSlideBack() {
        if (this.mSettingsIconRow == null || !this.mSettingsIconRow.isVisible()) {
            return false;
        }
        animateTranslateNotification(0.0f);
        return true;
    }

    public boolean hasUserChangedExpansion() {
        return this.mHasUserChangedExpansion;
    }

    public void inflateGuts() {
        if (this.mGuts == null) {
            this.mGutsStub.inflate();
        }
    }

    @Override // com.android.systemui.statusbar.ExpandableView
    public boolean isChildInGroup() {
        return this.mNotificationParent != null;
    }

    public boolean isClearable() {
        if (this.mStatusBarNotification == null || !this.mStatusBarNotification.isClearable()) {
            return false;
        }
        if (this.mIsSummaryWithChildren) {
            List<ExpandableNotificationRow> notificationChildren = this.mChildrenContainer.getNotificationChildren();
            for (int i = 0; i < notificationChildren.size(); i++) {
                if (!notificationChildren.get(i).isClearable()) {
                    return false;
                }
            }
            return true;
        }
        return true;
    }

    @Override // com.android.systemui.statusbar.ExpandableView
    public boolean isContentExpandable() {
        return getShowingLayout().isContentExpandable();
    }

    public boolean isDismissed() {
        return this.mDismissed;
    }

    public boolean isExpandable() {
        if (!this.mIsSummaryWithChildren || this.mShowingPublic) {
            return this.mExpandable;
        }
        return !this.mChildrenExpanded;
    }

    public boolean isExpanded() {
        return isExpanded(false);
    }

    public boolean isExpanded(boolean z) {
        return (!this.mOnKeyguard || z) ? (hasUserChangedExpansion() || !(isSystemExpanded() || isSystemChildExpanded())) ? isUserExpanded() : true : false;
    }

    @Override // com.android.systemui.statusbar.ExpandableView
    public boolean isGroupExpanded() {
        return this.mGroupManager.isGroupExpanded(this.mStatusBarNotification);
    }

    @Override // com.android.systemui.statusbar.ExpandableView
    public boolean isGroupExpansionChanging() {
        return isChildInGroup() ? this.mNotificationParent.isGroupExpansionChanging() : this.mGroupExpansionChanging;
    }

    public boolean isHeadsUp() {
        return this.mIsHeadsUp;
    }

    public boolean isOnKeyguard() {
        return this.mOnKeyguard;
    }

    public boolean isPinned() {
        return this.mIsPinned;
    }

    public boolean isRemoved() {
        return this.mRemoved;
    }

    @Override // com.android.systemui.statusbar.ExpandableView
    public boolean isSummaryWithChildren() {
        return this.mIsSummaryWithChildren;
    }

    public boolean isSystemExpanded() {
        return this.mIsSystemExpanded;
    }

    public boolean isUserExpanded() {
        return this.mUserExpanded;
    }

    public boolean isUserLocked() {
        boolean z = false;
        if (this.mUserLocked) {
            z = !this.mForceUnlocked;
        }
        return z;
    }

    public boolean keepInParent() {
        return this.mKeepInParent;
    }

    public void makeActionsVisibile() {
        setUserExpanded(true, true);
        if (isChildInGroup()) {
            this.mGroupManager.setGroupExpanded(this.mStatusBarNotification, true);
        }
        notifyHeightChanged(false);
    }

    @Override // com.android.systemui.statusbar.ExpandableView
    public boolean mustStayOnScreen() {
        return this.mIsHeadsUp;
    }

    @Override // com.android.systemui.statusbar.ExpandableView
    public void notifyHeightChanged(boolean z) {
        super.notifyHeightChanged(z);
        getShowingLayout().requestSelectLayout(!z ? isUserLocked() : true);
    }

    public void onExpandedByGesture(boolean z) {
        int i = 409;
        if (this.mGroupManager.isSummaryOfGroup(getStatusBarNotification())) {
            i = 410;
        }
        MetricsLogger.action(this.mContext, i, z);
    }

    @Override // com.android.systemui.statusbar.ActivatableNotificationView, android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mPublicLayout = (NotificationContentView) findViewById(2131886702);
        this.mPublicLayout.setContainingNotification(this);
        this.mPrivateLayout = (NotificationContentView) findViewById(2131886701);
        this.mPrivateLayout.setExpandClickListener(this.mExpandClickListener);
        this.mPrivateLayout.setContainingNotification(this);
        this.mPublicLayout.setExpandClickListener(this.mExpandClickListener);
        this.mSettingsIconRowStub = (ViewStub) findViewById(2131886699);
        this.mSettingsIconRowStub.setOnInflateListener(new ViewStub.OnInflateListener(this) { // from class: com.android.systemui.statusbar.ExpandableNotificationRow.3
            final ExpandableNotificationRow this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.ViewStub.OnInflateListener
            public void onInflate(ViewStub viewStub, View view) {
                this.this$0.mSettingsIconRow = (NotificationSettingsIconRow) view;
                this.this$0.mSettingsIconRow.setNotificationRowParent(this.this$0);
                this.this$0.mSettingsIconRow.setAppName(this.this$0.mAppName);
            }
        });
        this.mGutsStub = (ViewStub) findViewById(2131886706);
        this.mGutsStub.setOnInflateListener(new ViewStub.OnInflateListener(this) { // from class: com.android.systemui.statusbar.ExpandableNotificationRow.4
            final ExpandableNotificationRow this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.ViewStub.OnInflateListener
            public void onInflate(ViewStub viewStub, View view) {
                this.this$0.mGuts = (NotificationGuts) view;
                this.this$0.mGuts.setClipTopAmount(this.this$0.getClipTopAmount());
                this.this$0.mGuts.setActualHeight(this.this$0.getActualHeight());
                this.this$0.mGutsStub = null;
            }
        });
        this.mChildrenContainerStub = (ViewStub) findViewById(2131886704);
        this.mChildrenContainerStub.setOnInflateListener(new ViewStub.OnInflateListener(this) { // from class: com.android.systemui.statusbar.ExpandableNotificationRow.5
            final ExpandableNotificationRow this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.ViewStub.OnInflateListener
            public void onInflate(ViewStub viewStub, View view) {
                this.this$0.mChildrenContainer = (NotificationChildrenContainer) view;
                this.this$0.mChildrenContainer.setNotificationParent(this.this$0);
                this.this$0.mChildrenContainer.onNotificationUpdated();
                this.this$0.mTranslateableViews.add(this.this$0.mChildrenContainer);
            }
        });
        this.mVetoButton = findViewById(2131886703);
        this.mVetoButton.setImportantForAccessibility(2);
        this.mVetoButton.setContentDescription(this.mContext.getString(2131493428));
        this.mTranslateableViews = new ArrayList<>();
        for (int i = 0; i < getChildCount(); i++) {
            this.mTranslateableViews.add(getChildAt(i));
        }
        this.mTranslateableViews.remove(this.mVetoButton);
        this.mTranslateableViews.remove(this.mSettingsIconRowStub);
        this.mTranslateableViews.remove(this.mChildrenContainerStub);
        this.mTranslateableViews.remove(this.mGutsStub);
    }

    public void onFinishedExpansionChange() {
        this.mGroupExpansionChanging = false;
        updateBackgroundForGroupState();
    }

    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfoInternal(accessibilityNodeInfo);
        if (canViewBeDismissed()) {
            accessibilityNodeInfo.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_DISMISS);
        }
    }

    @Override // com.android.systemui.statusbar.ActivatableNotificationView, com.android.systemui.statusbar.ExpandableView, android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        updateMaxHeights();
        if (this.mSettingsIconRow != null) {
            this.mSettingsIconRow.updateVerticalLocation();
        }
    }

    public void onNotificationUpdated(NotificationData.Entry entry) {
        this.mEntry = entry;
        this.mStatusBarNotification = entry.notification;
        this.mPrivateLayout.onNotificationUpdated(entry);
        this.mPublicLayout.onNotificationUpdated(entry);
        this.mShowingPublicInitialized = false;
        updateNotificationColor();
        if (this.mIsSummaryWithChildren) {
            this.mChildrenContainer.recreateNotificationHeader(this.mExpandClickListener, this.mEntry.notification);
            this.mChildrenContainer.onNotificationUpdated();
        }
        if (this.mIconAnimationRunning) {
            setIconAnimationRunning(true);
        }
        if (this.mNotificationParent != null) {
            this.mNotificationParent.updateChildrenHeaderAppearance();
        }
        onChildrenCountChanged();
        this.mPublicLayout.updateExpandButtons(true);
        updateLimits();
    }

    public boolean onRequestSendAccessibilityEventInternal(View view, AccessibilityEvent accessibilityEvent) {
        if (super.onRequestSendAccessibilityEventInternal(view, accessibilityEvent)) {
            AccessibilityEvent obtain = AccessibilityEvent.obtain();
            onInitializeAccessibilityEvent(obtain);
            dispatchPopulateAccessibilityEvent(obtain);
            accessibilityEvent.appendRecord(obtain);
            return true;
        }
        return false;
    }

    @Override // com.android.systemui.statusbar.ActivatableNotificationView, android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getActionMasked() == 0 && isChildInGroup() && !isGroupExpanded()) {
            return false;
        }
        return super.onTouchEvent(motionEvent);
    }

    public boolean performAccessibilityActionInternal(int i, Bundle bundle) {
        if (super.performAccessibilityActionInternal(i, bundle)) {
            return true;
        }
        switch (i) {
            case 1048576:
                NotificationStackScrollLayout.performDismiss(this, this.mGroupManager, true);
                return true;
            default:
                return false;
        }
    }

    public void performDismiss() {
        this.mVetoButton.performClick();
    }

    public void prepareExpansionChanged(StackScrollState stackScrollState) {
        if (this.mIsSummaryWithChildren) {
            this.mChildrenContainer.prepareExpansionChanged(stackScrollState);
        }
    }

    public void reInflateViews() {
        initDimens();
        if (this.mIsSummaryWithChildren && this.mChildrenContainer != null) {
            this.mChildrenContainer.reInflateViews(this.mExpandClickListener, this.mEntry.notification);
        }
        if (this.mGuts != null) {
            NotificationGuts notificationGuts = this.mGuts;
            int indexOfChild = indexOfChild(notificationGuts);
            removeView(notificationGuts);
            this.mGuts = (NotificationGuts) LayoutInflater.from(this.mContext).inflate(2130968725, (ViewGroup) this, false);
            this.mGuts.setVisibility(notificationGuts.getVisibility());
            addView(this.mGuts, indexOfChild);
        }
        if (this.mSettingsIconRow != null) {
            NotificationSettingsIconRow notificationSettingsIconRow = this.mSettingsIconRow;
            int indexOfChild2 = indexOfChild(notificationSettingsIconRow);
            removeView(notificationSettingsIconRow);
            this.mSettingsIconRow = (NotificationSettingsIconRow) LayoutInflater.from(this.mContext).inflate(2130968729, (ViewGroup) this, false);
            this.mSettingsIconRow.setNotificationRowParent(this);
            this.mSettingsIconRow.setAppName(this.mAppName);
            this.mSettingsIconRow.setVisibility(notificationSettingsIconRow.getVisibility());
            addView(this.mSettingsIconRow, indexOfChild2);
        }
        this.mPrivateLayout.reInflateViews();
        this.mPublicLayout.reInflateViews();
    }

    public void removeAllChildren() {
        ArrayList arrayList = new ArrayList(this.mChildrenContainer.getNotificationChildren());
        for (int i = 0; i < arrayList.size(); i++) {
            ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) arrayList.get(i);
            if (!expandableNotificationRow.keepInParent()) {
                this.mChildrenContainer.removeNotification(expandableNotificationRow);
                expandableNotificationRow.setIsChildInGroup(false, null);
            }
        }
        onChildrenCountChanged();
    }

    public void removeChildNotification(ExpandableNotificationRow expandableNotificationRow) {
        if (this.mChildrenContainer != null) {
            this.mChildrenContainer.removeNotification(expandableNotificationRow);
        }
        onChildrenCountChanged();
        expandableNotificationRow.setIsChildInGroup(false, null);
    }

    @Override // com.android.systemui.statusbar.ActivatableNotificationView
    public void reset() {
        super.reset();
        boolean isExpanded = isExpanded();
        this.mExpandable = false;
        this.mHasUserChangedExpansion = false;
        this.mUserLocked = false;
        this.mShowingPublic = false;
        this.mSensitive = false;
        this.mShowingPublicInitialized = false;
        this.mIsSystemExpanded = false;
        this.mOnKeyguard = false;
        this.mPublicLayout.reset();
        this.mPrivateLayout.reset();
        resetHeight();
        resetTranslation();
        logExpansionEvent(false, isExpanded);
    }

    public void resetHeight() {
        this.mMaxExpandHeight = 0;
        this.mHeadsUpHeight = 0;
        onHeightReset();
        requestLayout();
    }

    public void resetTranslation() {
        if (this.mTranslateableViews != null) {
            for (int i = 0; i < this.mTranslateableViews.size(); i++) {
                this.mTranslateableViews.get(i).setTranslationX(0.0f);
            }
        }
        invalidateOutline();
        if (this.mSettingsIconRow != null) {
            this.mSettingsIconRow.resetState();
        }
    }

    public void resetUserExpansion() {
        this.mHasUserChangedExpansion = false;
        this.mUserExpanded = false;
    }

    @Override // com.android.systemui.statusbar.ActivatableNotificationView, com.android.systemui.statusbar.ExpandableOutlineView, com.android.systemui.statusbar.ExpandableView
    public void setActualHeight(int i, boolean z) {
        super.setActualHeight(i, z);
        if (this.mGuts != null && this.mGuts.areGutsExposed()) {
            this.mGuts.setActualHeight(i);
            return;
        }
        int max = Math.max(getMinHeight(), i);
        this.mPrivateLayout.setContentHeight(max);
        this.mPublicLayout.setContentHeight(max);
        if (this.mIsSummaryWithChildren) {
            this.mChildrenContainer.setActualHeight(i);
        }
        if (this.mGuts != null) {
            this.mGuts.setActualHeight(i);
        }
    }

    @Override // com.android.systemui.statusbar.ExpandableView
    public void setActualHeightAnimating(boolean z) {
        if (this.mPrivateLayout != null) {
            this.mPrivateLayout.setContentHeightAnimating(z);
        }
    }

    public void setAppName(String str) {
        this.mAppName = str;
        if (this.mSettingsIconRow != null) {
            this.mSettingsIconRow.setAppName(this.mAppName);
        }
    }

    public void setChildrenExpanded(boolean z, boolean z2) {
        this.mChildrenExpanded = z;
        if (this.mChildrenContainer != null) {
            this.mChildrenContainer.setChildrenExpanded(z);
        }
        updateBackgroundForGroupState();
        updateClickAndFocus();
    }

    public void setChronometerRunning(boolean z) {
        this.mLastChronometerRunning = z;
        setChronometerRunning(z, this.mPrivateLayout);
        setChronometerRunning(z, this.mPublicLayout);
        if (this.mChildrenContainer != null) {
            List<ExpandableNotificationRow> notificationChildren = this.mChildrenContainer.getNotificationChildren();
            for (int i = 0; i < notificationChildren.size(); i++) {
                notificationChildren.get(i).setChronometerRunning(z);
            }
        }
    }

    @Override // com.android.systemui.statusbar.ExpandableView
    public void setClipToActualHeight(boolean z) {
        super.setClipToActualHeight(!z ? isUserLocked() : true);
        NotificationContentView showingLayout = getShowingLayout();
        boolean z2 = true;
        if (!z) {
            z2 = isUserLocked();
        }
        showingLayout.setClipToActualHeight(z2);
    }

    @Override // com.android.systemui.statusbar.ActivatableNotificationView, com.android.systemui.statusbar.ExpandableOutlineView, com.android.systemui.statusbar.ExpandableView
    public void setClipTopAmount(int i) {
        super.setClipTopAmount(i);
        this.mPrivateLayout.setClipTopAmount(i);
        this.mPublicLayout.setClipTopAmount(i);
        if (this.mGuts != null) {
            this.mGuts.setClipTopAmount(i);
        }
    }

    public void setContentBackground(int i, boolean z, NotificationContentView notificationContentView) {
        if (getShowingLayout() == notificationContentView) {
            setTintColor(i, z);
        }
    }

    @Override // com.android.systemui.statusbar.ActivatableNotificationView, com.android.systemui.statusbar.ExpandableView
    public void setDark(boolean z, boolean z2, long j) {
        super.setDark(z, z2, j);
        NotificationContentView showingLayout = getShowingLayout();
        if (showingLayout != null) {
            showingLayout.setDark(z, z2, j);
        }
        if (this.mIsSummaryWithChildren) {
            this.mChildrenContainer.setDark(z, z2, j);
        }
    }

    public void setDismissed(boolean z, boolean z2) {
        List<ExpandableNotificationRow> notificationChildren;
        int indexOf;
        this.mDismissed = z;
        this.mGroupParentWhenDismissed = this.mNotificationParent;
        this.mRefocusOnDismiss = z2;
        this.mChildAfterViewWhenDismissed = null;
        if (!isChildInGroup() || (indexOf = (notificationChildren = this.mNotificationParent.getNotificationChildren()).indexOf(this)) == -1 || indexOf >= notificationChildren.size() - 1) {
            return;
        }
        this.mChildAfterViewWhenDismissed = notificationChildren.get(indexOf + 1);
    }

    public void setExpandable(boolean z) {
        this.mExpandable = z;
        this.mPrivateLayout.updateExpandButtons(isExpandable());
    }

    public void setExpansionLogger(ExpansionLogger expansionLogger, String str) {
        this.mLogger = expansionLogger;
        this.mLoggingKey = str;
    }

    public void setForceUnlocked(boolean z) {
        this.mForceUnlocked = z;
        if (this.mIsSummaryWithChildren) {
            for (ExpandableNotificationRow expandableNotificationRow : getNotificationChildren()) {
                expandableNotificationRow.setForceUnlocked(z);
            }
        }
    }

    public void setGroupExpansionChanging(boolean z) {
        this.mGroupExpansionChanging = z;
    }

    public void setGroupManager(NotificationGroupManager notificationGroupManager) {
        this.mGroupManager = notificationGroupManager;
        this.mPrivateLayout.setGroupManager(notificationGroupManager);
    }

    public void setHeadsUp(boolean z) {
        int intrinsicHeight = getIntrinsicHeight();
        this.mIsHeadsUp = z;
        this.mPrivateLayout.setHeadsUp(z);
        if (this.mIsSummaryWithChildren) {
            this.mChildrenContainer.updateGroupOverflow();
        }
        if (intrinsicHeight != getIntrinsicHeight()) {
            notifyHeightChanged(false);
        }
    }

    public void setHeadsUpManager(HeadsUpManager headsUpManager) {
        this.mHeadsUpManager = headsUpManager;
    }

    public void setHeadsupDisappearRunning(boolean z) {
        this.mHeadsupDisappearRunning = z;
        this.mPrivateLayout.setHeadsupDisappearRunning(z);
    }

    @Override // com.android.systemui.statusbar.ExpandableView
    public void setHideSensitive(boolean z, boolean z2, long j, long j2) {
        int i = 0;
        boolean z3 = this.mShowingPublic;
        if (!this.mSensitive) {
            z = false;
        }
        this.mShowingPublic = z;
        if ((this.mShowingPublicInitialized && this.mShowingPublic == z3) || this.mPublicLayout.getChildCount() == 0) {
            return;
        }
        if (z2) {
            animateShowingPublic(j, j2);
        } else {
            this.mPublicLayout.animate().cancel();
            this.mPrivateLayout.animate().cancel();
            if (this.mChildrenContainer != null) {
                this.mChildrenContainer.animate().cancel();
                this.mChildrenContainer.setAlpha(1.0f);
            }
            this.mPublicLayout.setAlpha(1.0f);
            this.mPrivateLayout.setAlpha(1.0f);
            NotificationContentView notificationContentView = this.mPublicLayout;
            if (!this.mShowingPublic) {
                i = 4;
            }
            notificationContentView.setVisibility(i);
            updateChildrenVisibility();
        }
        getShowingLayout().updateBackgroundColor(z2);
        this.mPrivateLayout.updateExpandButtons(isExpandable());
        this.mShowingPublicInitialized = true;
    }

    @Override // com.android.systemui.statusbar.ExpandableView
    public void setHideSensitiveForIntrinsicHeight(boolean z) {
        this.mHideSensitiveForIntrinsicHeight = z;
        if (this.mIsSummaryWithChildren) {
            List<ExpandableNotificationRow> notificationChildren = this.mChildrenContainer.getNotificationChildren();
            for (int i = 0; i < notificationChildren.size(); i++) {
                notificationChildren.get(i).setHideSensitiveForIntrinsicHeight(z);
            }
        }
    }

    public void setIconAnimationRunning(boolean z) {
        setIconAnimationRunning(z, this.mPublicLayout);
        setIconAnimationRunning(z, this.mPrivateLayout);
        if (this.mIsSummaryWithChildren) {
            setIconAnimationRunningForChild(z, this.mChildrenContainer.getHeaderView());
            List<ExpandableNotificationRow> notificationChildren = this.mChildrenContainer.getNotificationChildren();
            for (int i = 0; i < notificationChildren.size(); i++) {
                notificationChildren.get(i).setIconAnimationRunning(z);
            }
        }
        this.mIconAnimationRunning = z;
    }

    public void setIsChildInGroup(boolean z, ExpandableNotificationRow expandableNotificationRow) {
        if (!BaseStatusBar.ENABLE_CHILD_NOTIFICATIONS) {
            z = false;
        }
        if (!z) {
            expandableNotificationRow = null;
        }
        this.mNotificationParent = expandableNotificationRow;
        this.mPrivateLayout.setIsChildInGroup(z);
        resetBackgroundAlpha();
        updateBackgroundForGroupState();
        updateClickAndFocus();
        if (this.mNotificationParent != null) {
            this.mNotificationParent.updateBackgroundForGroupState();
        }
    }

    public void setJustClicked(boolean z) {
        this.mJustClicked = z;
    }

    public void setKeepInParent(boolean z) {
        this.mKeepInParent = z;
    }

    @Override // android.view.View
    public void setOnClickListener(View.OnClickListener onClickListener) {
        super.setOnClickListener(onClickListener);
        this.mOnClickListener = onClickListener;
        updateClickAndFocus();
    }

    public void setOnDismissListener(View.OnClickListener onClickListener) {
        this.mVetoButton.setOnClickListener(onClickListener);
    }

    public void setOnExpandClickListener(OnExpandClickListener onExpandClickListener) {
        this.mOnExpandClickListener = onExpandClickListener;
    }

    public void setOnKeyguard(boolean z) {
        if (z != this.mOnKeyguard) {
            boolean isExpanded = isExpanded();
            this.mOnKeyguard = z;
            logExpansionEvent(false, isExpanded);
            if (isExpanded != isExpanded()) {
                if (this.mIsSummaryWithChildren) {
                    this.mChildrenContainer.updateGroupOverflow();
                }
                notifyHeightChanged(false);
            }
        }
    }

    public void setPinned(boolean z) {
        int intrinsicHeight = getIntrinsicHeight();
        this.mIsPinned = z;
        if (intrinsicHeight != getIntrinsicHeight()) {
            notifyHeightChanged(false);
        }
        if (z) {
            setIconAnimationRunning(true);
            this.mExpandedWhenPinned = false;
        } else if (this.mExpandedWhenPinned) {
            setUserExpanded(true);
        }
        setChronometerRunning(this.mLastChronometerRunning);
    }

    public void setRemoteInputController(RemoteInputController remoteInputController) {
        this.mPrivateLayout.setRemoteInputController(remoteInputController);
    }

    public void setRemoved() {
        this.mRemoved = true;
        this.mPrivateLayout.setRemoved();
    }

    public void setSensitive(boolean z, boolean z2) {
        this.mSensitive = z;
        this.mSensitiveHiddenInGeneral = z2;
    }

    @Override // com.android.systemui.statusbar.ActivatableNotificationView
    public void setShowingLegacyBackground(boolean z) {
        super.setShowingLegacyBackground(z);
        this.mPrivateLayout.setShowingLegacyBackground(z);
        this.mPublicLayout.setShowingLegacyBackground(z);
    }

    public void setSingleLineWidthIndention(int i) {
        this.mPrivateLayout.setSingleLineWidthIndention(i);
    }

    public void setSystemChildExpanded(boolean z) {
        this.mIsSystemChildExpanded = z;
    }

    public void setSystemExpanded(boolean z) {
        if (z != this.mIsSystemExpanded) {
            boolean isExpanded = isExpanded();
            this.mIsSystemExpanded = z;
            notifyHeightChanged(false);
            logExpansionEvent(false, isExpanded);
            if (this.mIsSummaryWithChildren) {
                this.mChildrenContainer.updateGroupOverflow();
            }
        }
    }

    @Override // com.android.systemui.statusbar.ExpandableView
    public void setTranslation(float f) {
        if (areGutsExposed()) {
            return;
        }
        for (int i = 0; i < this.mTranslateableViews.size(); i++) {
            if (this.mTranslateableViews.get(i) != null) {
                this.mTranslateableViews.get(i).setTranslationX(f);
            }
        }
        invalidateOutline();
        if (this.mSettingsIconRow != null) {
            this.mSettingsIconRow.updateSettingsIcons(f, getMeasuredWidth());
        }
    }

    public void setUserExpanded(boolean z) {
        setUserExpanded(z, false);
    }

    public void setUserExpanded(boolean z, boolean z2) {
        this.mFalsingManager.setNotificationExpanded();
        if (this.mIsSummaryWithChildren && !this.mShowingPublic && z2) {
            boolean isGroupExpanded = this.mGroupManager.isGroupExpanded(this.mStatusBarNotification);
            this.mGroupManager.setGroupExpanded(this.mStatusBarNotification, z);
            logExpansionEvent(true, isGroupExpanded);
        } else if (!z || this.mExpandable) {
            boolean isExpanded = isExpanded();
            this.mHasUserChangedExpansion = true;
            this.mUserExpanded = z;
            logExpansionEvent(true, isExpanded);
        }
    }

    public void setUserLocked(boolean z) {
        this.mUserLocked = z;
        this.mPrivateLayout.setUserExpanding(z);
        if (this.mIsSummaryWithChildren) {
            this.mChildrenContainer.setUserLocked(z);
            if (z || !(z || isGroupExpanded())) {
                updateBackgroundForGroupState();
            }
        }
    }

    @Override // com.android.systemui.statusbar.ActivatableNotificationView
    protected boolean shouldHideBackground() {
        return !super.shouldHideBackground() ? this.mShowNoBackground : true;
    }

    public boolean shouldRefocusOnDismiss() {
        return !this.mRefocusOnDismiss ? isAccessibilityFocused() : true;
    }

    public void startChildAnimation(StackScrollState stackScrollState, StackStateAnimator stackStateAnimator, long j, long j2) {
        if (this.mIsSummaryWithChildren) {
            this.mChildrenContainer.startAnimationToState(stackScrollState, stackStateAnimator, j, j2);
        }
    }

    public void updateBackgroundForGroupState() {
        if (this.mIsSummaryWithChildren) {
            boolean z = false;
            if (isGroupExpanded()) {
                if (isGroupExpansionChanging()) {
                    z = false;
                } else {
                    z = false;
                    if (!isUserLocked()) {
                        z = true;
                    }
                }
            }
            this.mShowNoBackground = z;
            this.mChildrenContainer.updateHeaderForExpansion(this.mShowNoBackground);
            List<ExpandableNotificationRow> notificationChildren = this.mChildrenContainer.getNotificationChildren();
            for (int i = 0; i < notificationChildren.size(); i++) {
                notificationChildren.get(i).updateBackgroundForGroupState();
            }
        } else if (isChildInGroup()) {
            this.mShowNoBackground = !isGroupExpanded() ? (this.mNotificationParent.isGroupExpansionChanging() || this.mNotificationParent.isUserLocked()) ? getShowingLayout().getBackgroundColorForExpansionState() != 0 : false : true ? false : true;
        } else {
            this.mShowNoBackground = false;
        }
        updateOutline();
        updateBackground();
    }

    @Override // com.android.systemui.statusbar.ActivatableNotificationView
    protected void updateBackgroundTint() {
        super.updateBackgroundTint();
        updateBackgroundForGroupState();
        if (this.mIsSummaryWithChildren) {
            List<ExpandableNotificationRow> notificationChildren = this.mChildrenContainer.getNotificationChildren();
            for (int i = 0; i < notificationChildren.size(); i++) {
                notificationChildren.get(i).updateBackgroundForGroupState();
            }
        }
    }

    public void updateChildrenHeaderAppearance() {
        if (this.mIsSummaryWithChildren) {
            this.mChildrenContainer.updateChildrenHeaderAppearance();
        }
    }

    public boolean wasJustClicked() {
        return this.mJustClicked;
    }
}

package com.android.systemui.statusbar.stack;

import android.app.Notification;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.service.notification.StatusBarNotification;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.NotificationHeaderView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import android.widget.TextView;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.R;
import com.android.systemui.statusbar.CrossFadeHelper;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.NotificationHeaderUtil;
import com.android.systemui.statusbar.TransformableView;
import com.android.systemui.statusbar.notification.HybridGroupManager;
import com.android.systemui.statusbar.notification.HybridNotificationView;
import com.android.systemui.statusbar.notification.NotificationUtils;
import com.android.systemui.statusbar.notification.NotificationViewWrapper;
import com.android.systemui.statusbar.notification.VisualStabilityManager;
import java.util.ArrayList;
import java.util.List;
/* loaded from: classes.dex */
public class NotificationChildrenContainer extends ViewGroup {
    private static final AnimationProperties ALPHA_FADE_IN = new AnimationProperties() { // from class: com.android.systemui.statusbar.stack.NotificationChildrenContainer.1
        private AnimationFilter mAnimationFilter = new AnimationFilter().animateAlpha();

        @Override // com.android.systemui.statusbar.stack.AnimationProperties
        public AnimationFilter getAnimationFilter() {
            return this.mAnimationFilter;
        }
    }.setDuration(200);
    private int mActualHeight;
    private int mChildPadding;
    private final List<ExpandableNotificationRow> mChildren;
    private boolean mChildrenExpanded;
    private int mClipBottomAmount;
    private float mCollapsedBottompadding;
    private ExpandableNotificationRow mContainingNotification;
    private ViewGroup mCurrentHeader;
    private int mCurrentHeaderTranslation;
    private float mDividerAlpha;
    private int mDividerHeight;
    private final List<View> mDividers;
    private boolean mEnableShadowOnChildNotifications;
    private ViewState mGroupOverFlowState;
    private View.OnClickListener mHeaderClickListener;
    private int mHeaderHeight;
    private NotificationHeaderUtil mHeaderUtil;
    private ViewState mHeaderViewState;
    private float mHeaderVisibleAmount;
    private boolean mHideDividersDuringExpand;
    private final HybridGroupManager mHybridGroupManager;
    private boolean mIsLowPriority;
    private boolean mNeverAppliedGroupState;
    private NotificationHeaderView mNotificationHeader;
    private ViewGroup mNotificationHeaderAmbient;
    private NotificationHeaderView mNotificationHeaderLowPriority;
    private int mNotificationHeaderMargin;
    private NotificationViewWrapper mNotificationHeaderWrapper;
    private NotificationViewWrapper mNotificationHeaderWrapperAmbient;
    private NotificationViewWrapper mNotificationHeaderWrapperLowPriority;
    private int mNotificatonTopPadding;
    private TextView mOverflowNumber;
    private int mRealHeight;
    private boolean mShowDividersWhenExpanded;
    private int mTranslationForHeader;
    private boolean mUserLocked;

    public NotificationChildrenContainer(Context context) {
        this(context, null);
    }

    public NotificationChildrenContainer(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public NotificationChildrenContainer(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public NotificationChildrenContainer(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mDividers = new ArrayList();
        this.mChildren = new ArrayList();
        this.mCurrentHeaderTranslation = 0;
        this.mHeaderVisibleAmount = 1.0f;
        this.mHybridGroupManager = new HybridGroupManager(getContext(), this);
        initDimens();
        setClipChildren(false);
    }

    private void initDimens() {
        Resources resources = getResources();
        this.mChildPadding = resources.getDimensionPixelSize(R.dimen.notification_children_padding);
        this.mDividerHeight = resources.getDimensionPixelSize(R.dimen.notification_children_container_divider_height);
        this.mDividerAlpha = resources.getFloat(R.dimen.notification_divider_alpha);
        this.mNotificationHeaderMargin = resources.getDimensionPixelSize(R.dimen.notification_children_container_margin_top);
        this.mNotificatonTopPadding = resources.getDimensionPixelSize(R.dimen.notification_children_container_top_padding);
        this.mHeaderHeight = this.mNotificationHeaderMargin + this.mNotificatonTopPadding;
        this.mCollapsedBottompadding = resources.getDimensionPixelSize(17105198);
        this.mEnableShadowOnChildNotifications = resources.getBoolean(R.bool.config_enableShadowOnChildNotifications);
        this.mShowDividersWhenExpanded = resources.getBoolean(R.bool.config_showDividersWhenGroupNotificationExpanded);
        this.mHideDividersDuringExpand = resources.getBoolean(R.bool.config_hideDividersDuringExpand);
        this.mTranslationForHeader = resources.getDimensionPixelSize(17105198) - this.mNotificationHeaderMargin;
        this.mHybridGroupManager.initDimens();
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int width;
        int min = Math.min(this.mChildren.size(), 8);
        for (int i5 = 0; i5 < min; i5++) {
            ExpandableNotificationRow expandableNotificationRow = this.mChildren.get(i5);
            expandableNotificationRow.layout(0, 0, expandableNotificationRow.getMeasuredWidth(), expandableNotificationRow.getMeasuredHeight());
            this.mDividers.get(i5).layout(0, 0, getWidth(), this.mDividerHeight);
        }
        if (this.mOverflowNumber != null) {
            if (!(getLayoutDirection() == 1)) {
                width = getWidth() - this.mOverflowNumber.getMeasuredWidth();
            } else {
                width = 0;
            }
            this.mOverflowNumber.layout(width, 0, this.mOverflowNumber.getMeasuredWidth() + width, this.mOverflowNumber.getMeasuredHeight());
        }
        if (this.mNotificationHeader != null) {
            this.mNotificationHeader.layout(0, 0, this.mNotificationHeader.getMeasuredWidth(), this.mNotificationHeader.getMeasuredHeight());
        }
        if (this.mNotificationHeaderLowPriority != null) {
            this.mNotificationHeaderLowPriority.layout(0, 0, this.mNotificationHeaderLowPriority.getMeasuredWidth(), this.mNotificationHeaderLowPriority.getMeasuredHeight());
        }
        if (this.mNotificationHeaderAmbient != null) {
            this.mNotificationHeaderAmbient.layout(0, 0, this.mNotificationHeaderAmbient.getMeasuredWidth(), this.mNotificationHeaderAmbient.getMeasuredHeight());
        }
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        int makeMeasureSpec;
        int i3;
        int mode = View.MeasureSpec.getMode(i2);
        boolean z = mode == 1073741824;
        boolean z2 = mode == Integer.MIN_VALUE;
        int size = View.MeasureSpec.getSize(i2);
        if (z || z2) {
            makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(size, Integer.MIN_VALUE);
        } else {
            makeMeasureSpec = i2;
        }
        int size2 = View.MeasureSpec.getSize(i);
        if (this.mOverflowNumber != null) {
            this.mOverflowNumber.measure(View.MeasureSpec.makeMeasureSpec(size2, Integer.MIN_VALUE), makeMeasureSpec);
        }
        int makeMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(this.mDividerHeight, 1073741824);
        int i4 = this.mNotificationHeaderMargin + this.mNotificatonTopPadding;
        int min = Math.min(this.mChildren.size(), 8);
        int maxAllowedVisibleChildren = getMaxAllowedVisibleChildren(true);
        int i5 = min > maxAllowedVisibleChildren ? maxAllowedVisibleChildren - 1 : -1;
        int i6 = i4;
        int i7 = 0;
        while (i7 < min) {
            ExpandableNotificationRow expandableNotificationRow = this.mChildren.get(i7);
            if (!(i7 == i5) || this.mOverflowNumber == null || this.mContainingNotification.isShowingAmbient()) {
                i3 = 0;
            } else {
                i3 = this.mOverflowNumber.getMeasuredWidth();
            }
            expandableNotificationRow.setSingleLineWidthIndention(i3);
            expandableNotificationRow.measure(i, makeMeasureSpec);
            this.mDividers.get(i7).measure(i, makeMeasureSpec2);
            if (expandableNotificationRow.getVisibility() != 8) {
                i6 += expandableNotificationRow.getMeasuredHeight() + this.mDividerHeight;
            }
            i7++;
        }
        this.mRealHeight = i6;
        if (mode != 0) {
            i6 = Math.min(i6, size);
        }
        int makeMeasureSpec3 = View.MeasureSpec.makeMeasureSpec(this.mHeaderHeight, 1073741824);
        if (this.mNotificationHeader != null) {
            this.mNotificationHeader.measure(i, makeMeasureSpec3);
        }
        if (this.mNotificationHeaderLowPriority != null) {
            this.mNotificationHeaderLowPriority.measure(i, View.MeasureSpec.makeMeasureSpec(this.mHeaderHeight, 1073741824));
        }
        if (this.mNotificationHeaderAmbient != null) {
            this.mNotificationHeaderAmbient.measure(i, View.MeasureSpec.makeMeasureSpec(this.mHeaderHeight, 1073741824));
        }
        setMeasuredDimension(size2, i6);
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    public boolean pointInView(float f, float f2, float f3) {
        float f4 = -f3;
        return f >= f4 && f2 >= f4 && f < ((float) (this.mRight - this.mLeft)) + f3 && f2 < ((float) this.mRealHeight) + f3;
    }

    public void addNotification(ExpandableNotificationRow expandableNotificationRow, int i) {
        if (i < 0) {
            i = this.mChildren.size();
        }
        this.mChildren.add(i, expandableNotificationRow);
        addView(expandableNotificationRow);
        expandableNotificationRow.setUserLocked(this.mUserLocked);
        View inflateDivider = inflateDivider();
        addView(inflateDivider);
        this.mDividers.add(i, inflateDivider);
        updateGroupOverflow();
        expandableNotificationRow.setContentTransformationAmount(0.0f, false);
        ExpandableNotificationRow.NotificationViewState viewState = expandableNotificationRow.getViewState();
        if (viewState != null) {
            viewState.cancelAnimations(expandableNotificationRow);
            expandableNotificationRow.cancelAppearDrawing();
        }
    }

    public void removeNotification(ExpandableNotificationRow expandableNotificationRow) {
        int indexOf = this.mChildren.indexOf(expandableNotificationRow);
        this.mChildren.remove(expandableNotificationRow);
        removeView(expandableNotificationRow);
        final View remove = this.mDividers.remove(indexOf);
        removeView(remove);
        getOverlay().add(remove);
        CrossFadeHelper.fadeOut(remove, new Runnable() { // from class: com.android.systemui.statusbar.stack.NotificationChildrenContainer.2
            @Override // java.lang.Runnable
            public void run() {
                NotificationChildrenContainer.this.getOverlay().remove(remove);
            }
        });
        expandableNotificationRow.setSystemChildExpanded(false);
        expandableNotificationRow.setUserLocked(false);
        updateGroupOverflow();
        if (!expandableNotificationRow.isRemoved()) {
            this.mHeaderUtil.restoreNotificationHeader(expandableNotificationRow);
        }
    }

    public int getNotificationChildCount() {
        return this.mChildren.size();
    }

    public void recreateNotificationHeader(View.OnClickListener onClickListener) {
        this.mHeaderClickListener = onClickListener;
        Notification.Builder recoverBuilder = Notification.Builder.recoverBuilder(getContext(), this.mContainingNotification.getStatusBarNotification().getNotification());
        RemoteViews makeNotificationHeader = recoverBuilder.makeNotificationHeader(false);
        if (this.mNotificationHeader == null) {
            this.mNotificationHeader = makeNotificationHeader.apply(getContext(), this);
            this.mNotificationHeader.findViewById(16908862).setVisibility(0);
            this.mNotificationHeader.setOnClickListener(this.mHeaderClickListener);
            this.mNotificationHeaderWrapper = NotificationViewWrapper.wrap(getContext(), this.mNotificationHeader, this.mContainingNotification);
            addView((View) this.mNotificationHeader, 0);
            invalidate();
        } else {
            makeNotificationHeader.reapply(getContext(), this.mNotificationHeader);
        }
        this.mNotificationHeaderWrapper.onContentUpdated(this.mContainingNotification);
        recreateLowPriorityHeader(recoverBuilder);
        recreateAmbientHeader(recoverBuilder);
        updateHeaderVisibility(false);
        updateChildrenHeaderAppearance();
    }

    private void recreateAmbientHeader(Notification.Builder builder) {
        StatusBarNotification statusBarNotification = this.mContainingNotification.getStatusBarNotification();
        if (builder == null) {
            builder = Notification.Builder.recoverBuilder(getContext(), statusBarNotification.getNotification());
        }
        RemoteViews makeNotificationHeader = builder.makeNotificationHeader(true);
        if (this.mNotificationHeaderAmbient == null) {
            this.mNotificationHeaderAmbient = (ViewGroup) makeNotificationHeader.apply(getContext(), this);
            this.mNotificationHeaderWrapperAmbient = NotificationViewWrapper.wrap(getContext(), this.mNotificationHeaderAmbient, this.mContainingNotification);
            this.mNotificationHeaderWrapperAmbient.onContentUpdated(this.mContainingNotification);
            addView(this.mNotificationHeaderAmbient, 0);
            invalidate();
        } else {
            makeNotificationHeader.reapply(getContext(), this.mNotificationHeaderAmbient);
        }
        resetHeaderVisibilityIfNeeded(this.mNotificationHeaderAmbient, calculateDesiredHeader());
        this.mNotificationHeaderWrapperAmbient.onContentUpdated(this.mContainingNotification);
    }

    private void recreateLowPriorityHeader(Notification.Builder builder) {
        StatusBarNotification statusBarNotification = this.mContainingNotification.getStatusBarNotification();
        if (this.mIsLowPriority) {
            if (builder == null) {
                builder = Notification.Builder.recoverBuilder(getContext(), statusBarNotification.getNotification());
            }
            RemoteViews makeLowPriorityContentView = builder.makeLowPriorityContentView(true);
            if (this.mNotificationHeaderLowPriority == null) {
                this.mNotificationHeaderLowPriority = makeLowPriorityContentView.apply(getContext(), this);
                this.mNotificationHeaderLowPriority.findViewById(16908862).setVisibility(0);
                this.mNotificationHeaderLowPriority.setOnClickListener(this.mHeaderClickListener);
                this.mNotificationHeaderWrapperLowPriority = NotificationViewWrapper.wrap(getContext(), this.mNotificationHeaderLowPriority, this.mContainingNotification);
                addView((View) this.mNotificationHeaderLowPriority, 0);
                invalidate();
            } else {
                makeLowPriorityContentView.reapply(getContext(), this.mNotificationHeaderLowPriority);
            }
            this.mNotificationHeaderWrapperLowPriority.onContentUpdated(this.mContainingNotification);
            resetHeaderVisibilityIfNeeded(this.mNotificationHeaderLowPriority, calculateDesiredHeader());
            return;
        }
        removeView(this.mNotificationHeaderLowPriority);
        this.mNotificationHeaderLowPriority = null;
        this.mNotificationHeaderWrapperLowPriority = null;
    }

    public void updateChildrenHeaderAppearance() {
        this.mHeaderUtil.updateChildrenHeaderAppearance();
    }

    public void updateGroupOverflow() {
        int size = this.mChildren.size();
        int maxAllowedVisibleChildren = getMaxAllowedVisibleChildren(true);
        HybridNotificationView hybridNotificationView = null;
        if (size <= maxAllowedVisibleChildren) {
            if (this.mOverflowNumber != null) {
                removeView(this.mOverflowNumber);
                if (isShown() && isAttachedToWindow()) {
                    final TextView textView = this.mOverflowNumber;
                    addTransientView(textView, getTransientViewCount());
                    CrossFadeHelper.fadeOut(textView, new Runnable() { // from class: com.android.systemui.statusbar.stack.NotificationChildrenContainer.3
                        @Override // java.lang.Runnable
                        public void run() {
                            NotificationChildrenContainer.this.removeTransientView(textView);
                        }
                    });
                }
                this.mOverflowNumber = null;
                this.mGroupOverFlowState = null;
                return;
            }
            return;
        }
        int i = size - maxAllowedVisibleChildren;
        this.mOverflowNumber = this.mHybridGroupManager.bindOverflowNumber(this.mOverflowNumber, i);
        if (this.mContainingNotification.isShowingAmbient()) {
            ExpandableNotificationRow expandableNotificationRow = this.mChildren.get(0);
            if (expandableNotificationRow != null) {
                hybridNotificationView = expandableNotificationRow.getAmbientSingleLineView();
            }
            if (hybridNotificationView != null) {
                this.mHybridGroupManager.bindOverflowNumberAmbient(hybridNotificationView.getTitleView(), this.mContainingNotification.getStatusBarNotification().getNotification(), i);
            }
        }
        if (this.mGroupOverFlowState == null) {
            this.mGroupOverFlowState = new ViewState();
            this.mNeverAppliedGroupState = true;
        }
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        updateGroupOverflow();
    }

    private View inflateDivider() {
        return LayoutInflater.from(this.mContext).inflate(R.layout.notification_children_divider, (ViewGroup) this, false);
    }

    public List<ExpandableNotificationRow> getNotificationChildren() {
        return this.mChildren;
    }

    public boolean applyChildOrder(List<ExpandableNotificationRow> list, VisualStabilityManager visualStabilityManager, VisualStabilityManager.Callback callback) {
        if (list == null) {
            return false;
        }
        boolean z = false;
        for (int i = 0; i < this.mChildren.size() && i < list.size(); i++) {
            ExpandableNotificationRow expandableNotificationRow = this.mChildren.get(i);
            ExpandableNotificationRow expandableNotificationRow2 = list.get(i);
            if (expandableNotificationRow != expandableNotificationRow2) {
                if (visualStabilityManager.canReorderNotification(expandableNotificationRow2)) {
                    this.mChildren.remove(expandableNotificationRow2);
                    this.mChildren.add(i, expandableNotificationRow2);
                    z = true;
                } else {
                    visualStabilityManager.addReorderingAllowedCallback(callback);
                }
            }
        }
        updateExpansionStates();
        return z;
    }

    private void updateExpansionStates() {
        if (this.mChildrenExpanded || this.mUserLocked) {
            return;
        }
        int size = this.mChildren.size();
        for (int i = 0; i < size; i++) {
            ExpandableNotificationRow expandableNotificationRow = this.mChildren.get(i);
            boolean z = true;
            if (i != 0 || size != 1) {
                z = false;
            }
            expandableNotificationRow.setSystemChildExpanded(z);
        }
    }

    public int getIntrinsicHeight() {
        return getIntrinsicHeight(getMaxAllowedVisibleChildren());
    }

    private int getIntrinsicHeight(float f) {
        float f2;
        int i;
        int i2;
        if (showingAsLowPriority()) {
            return this.mNotificationHeaderLowPriority.getHeight();
        }
        int i3 = this.mNotificationHeaderMargin + this.mCurrentHeaderTranslation;
        int size = this.mChildren.size();
        if (this.mUserLocked) {
            f2 = getGroupExpandFraction();
        } else {
            f2 = 0.0f;
        }
        boolean z = this.mChildrenExpanded || this.mContainingNotification.isShowingAmbient();
        int i4 = i3;
        boolean z2 = true;
        int i5 = 0;
        for (int i6 = 0; i6 < size && i5 < f; i6++) {
            if (!z2) {
                if (this.mUserLocked) {
                    i2 = (int) (i4 + NotificationUtils.interpolate(this.mChildPadding, this.mDividerHeight, f2));
                } else {
                    i2 = i4 + (z ? this.mDividerHeight : this.mChildPadding);
                }
            } else {
                if (this.mUserLocked) {
                    i2 = (int) (i4 + NotificationUtils.interpolate(0.0f, this.mNotificatonTopPadding + this.mDividerHeight, f2));
                } else {
                    if (z) {
                        i = this.mNotificatonTopPadding + this.mDividerHeight;
                    } else {
                        i = 0;
                    }
                    i2 = i4 + i;
                }
                z2 = false;
            }
            i4 = i2 + this.mChildren.get(i6).getIntrinsicHeight();
            i5++;
        }
        if (this.mUserLocked) {
            return (int) (i4 + NotificationUtils.interpolate(this.mCollapsedBottompadding, 0.0f, f2));
        }
        if (!z) {
            return (int) (i4 + this.mCollapsedBottompadding);
        }
        return i4;
    }

    /* JADX WARN: Removed duplicated region for block: B:56:0x010d  */
    /* JADX WARN: Removed duplicated region for block: B:80:0x0195  */
    /* JADX WARN: Removed duplicated region for block: B:91:0x0113 A[SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:92:? A[RETURN, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public void getState(StackScrollState stackScrollState, ExpandableViewState expandableViewState, AmbientState ambientState) {
        int i;
        float f;
        float f2;
        float f3;
        int i2;
        float f4;
        int i3;
        int size = this.mChildren.size();
        int i4 = this.mNotificationHeaderMargin + this.mCurrentHeaderTranslation;
        int maxAllowedVisibleChildren = getMaxAllowedVisibleChildren() - 1;
        int i5 = maxAllowedVisibleChildren + 1;
        boolean z = this.mUserLocked && !showingAsLowPriority();
        if (this.mUserLocked) {
            f = getGroupExpandFraction();
            i = getMaxAllowedVisibleChildren(true);
        } else {
            i = i5;
            f = 0.0f;
        }
        boolean z2 = this.mChildrenExpanded && !this.mContainingNotification.isGroupExpansionChanging();
        int i6 = i4;
        boolean z3 = true;
        int i7 = 0;
        int i8 = 0;
        while (i7 < size) {
            ExpandableNotificationRow expandableNotificationRow = this.mChildren.get(i7);
            if (!z3) {
                if (z) {
                    i2 = (int) (i6 + NotificationUtils.interpolate(this.mChildPadding, this.mDividerHeight, f));
                } else {
                    i2 = (this.mChildrenExpanded ? this.mDividerHeight : this.mChildPadding) + i6;
                }
            } else {
                if (z) {
                    i2 = (int) (i6 + NotificationUtils.interpolate(0.0f, this.mNotificatonTopPadding + this.mDividerHeight, f));
                } else {
                    i2 = i6 + (this.mChildrenExpanded ? this.mNotificatonTopPadding + this.mDividerHeight : 0);
                }
                z3 = false;
            }
            ExpandableViewState viewStateForView = stackScrollState.getViewStateForView(expandableNotificationRow);
            int intrinsicHeight = expandableNotificationRow.getIntrinsicHeight();
            viewStateForView.height = intrinsicHeight;
            boolean z4 = z;
            viewStateForView.yTranslation = i2 + i8;
            viewStateForView.hidden = false;
            if (z2 && this.mEnableShadowOnChildNotifications) {
                f4 = expandableViewState.zTranslation;
            } else {
                f4 = 0.0f;
            }
            viewStateForView.zTranslation = f4;
            viewStateForView.dimmed = expandableViewState.dimmed;
            viewStateForView.dark = expandableViewState.dark;
            viewStateForView.hideSensitive = expandableViewState.hideSensitive;
            viewStateForView.belowSpeedBump = expandableViewState.belowSpeedBump;
            viewStateForView.clipTopAmount = 0;
            viewStateForView.alpha = 0.0f;
            if (i7 < i) {
                viewStateForView.alpha = showingAsLowPriority() ? f : 1.0f;
            } else if (f == 1.0f && i7 <= maxAllowedVisibleChildren) {
                i3 = maxAllowedVisibleChildren;
                viewStateForView.alpha = (this.mActualHeight - viewStateForView.yTranslation) / viewStateForView.height;
                viewStateForView.alpha = Math.max(0.0f, Math.min(1.0f, viewStateForView.alpha));
                viewStateForView.location = expandableViewState.location;
                viewStateForView.inShelf = expandableViewState.inShelf;
                i6 = intrinsicHeight + i2;
                if (!expandableNotificationRow.isExpandAnimationRunning()) {
                    i8 = -ambientState.getExpandAnimationTopChange();
                }
                i7++;
                z = z4;
                maxAllowedVisibleChildren = i3;
            }
            i3 = maxAllowedVisibleChildren;
            viewStateForView.location = expandableViewState.location;
            viewStateForView.inShelf = expandableViewState.inShelf;
            i6 = intrinsicHeight + i2;
            if (!expandableNotificationRow.isExpandAnimationRunning()) {
            }
            i7++;
            z = z4;
            maxAllowedVisibleChildren = i3;
        }
        if (this.mOverflowNumber != null) {
            ExpandableNotificationRow expandableNotificationRow2 = this.mChildren.get(Math.min(getMaxAllowedVisibleChildren(true), size) - 1);
            this.mGroupOverFlowState.copyFrom(stackScrollState.getViewStateForView(expandableNotificationRow2));
            if (this.mContainingNotification.isShowingAmbient()) {
                this.mGroupOverFlowState.alpha = 0.0f;
                f2 = 0.0f;
            } else if (!this.mChildrenExpanded) {
                HybridNotificationView singleLineView = expandableNotificationRow2.getSingleLineView();
                if (singleLineView != null) {
                    TextView textView = singleLineView.getTextView();
                    if (textView.getVisibility() == 8) {
                        textView = singleLineView.getTitleView();
                    }
                    if (textView.getVisibility() != 8) {
                        singleLineView = textView;
                    }
                    this.mGroupOverFlowState.alpha = singleLineView.getAlpha();
                    this.mGroupOverFlowState.yTranslation += NotificationUtils.getRelativeYOffset(singleLineView, expandableNotificationRow2);
                }
            } else {
                this.mGroupOverFlowState.yTranslation += this.mNotificationHeaderMargin;
                f2 = 0.0f;
                this.mGroupOverFlowState.alpha = 0.0f;
            }
            if (this.mNotificationHeader == null) {
                if (this.mHeaderViewState == null) {
                    this.mHeaderViewState = new ViewState();
                }
                this.mHeaderViewState.initFrom(this.mNotificationHeader);
                ViewState viewState = this.mHeaderViewState;
                if (z2) {
                    f3 = expandableViewState.zTranslation;
                } else {
                    f3 = f2;
                }
                viewState.zTranslation = f3;
                this.mHeaderViewState.yTranslation = this.mCurrentHeaderTranslation;
                this.mHeaderViewState.alpha = this.mHeaderVisibleAmount;
                this.mHeaderViewState.hidden = false;
                return;
            }
            return;
        }
        f2 = 0.0f;
        if (this.mNotificationHeader == null) {
        }
    }

    private int getMaxAllowedVisibleChildren() {
        return getMaxAllowedVisibleChildren(false);
    }

    private int getMaxAllowedVisibleChildren(boolean z) {
        if (this.mContainingNotification.isShowingAmbient()) {
            return 1;
        }
        if (!z && (this.mChildrenExpanded || this.mContainingNotification.isUserLocked())) {
            return 8;
        }
        if (this.mIsLowPriority) {
            return 5;
        }
        if (!this.mContainingNotification.isOnKeyguard()) {
            if (this.mContainingNotification.isExpanded() || this.mContainingNotification.isHeadsUp()) {
                return 5;
            }
            return 2;
        }
        return 2;
    }

    public void applyState(StackScrollState stackScrollState) {
        float f;
        int size = this.mChildren.size();
        ViewState viewState = new ViewState();
        if (this.mUserLocked) {
            f = getGroupExpandFraction();
        } else {
            f = 0.0f;
        }
        boolean z = (this.mUserLocked && !showingAsLowPriority()) || (this.mChildrenExpanded && this.mShowDividersWhenExpanded) || (this.mContainingNotification.isGroupExpansionChanging() && !this.mHideDividersDuringExpand);
        for (int i = 0; i < size; i++) {
            ExpandableNotificationRow expandableNotificationRow = this.mChildren.get(i);
            ExpandableViewState viewStateForView = stackScrollState.getViewStateForView(expandableNotificationRow);
            viewStateForView.applyToView(expandableNotificationRow);
            View view = this.mDividers.get(i);
            viewState.initFrom(view);
            viewState.yTranslation = viewStateForView.yTranslation - this.mDividerHeight;
            float f2 = (!this.mChildrenExpanded || viewStateForView.alpha == 0.0f) ? 0.0f : this.mDividerAlpha;
            if (this.mUserLocked && !showingAsLowPriority() && viewStateForView.alpha != 0.0f) {
                f2 = NotificationUtils.interpolate(0.0f, 0.5f, Math.min(viewStateForView.alpha, f));
            }
            viewState.hidden = !z;
            viewState.alpha = f2;
            viewState.applyToView(view);
            expandableNotificationRow.setFakeShadowIntensity(0.0f, 0.0f, 0, 0);
        }
        if (this.mGroupOverFlowState != null) {
            this.mGroupOverFlowState.applyToView(this.mOverflowNumber);
            this.mNeverAppliedGroupState = false;
        }
        if (this.mHeaderViewState != null) {
            this.mHeaderViewState.applyToView(this.mNotificationHeader);
        }
        updateChildrenClipping();
    }

    private void updateChildrenClipping() {
        int i;
        boolean z;
        if (this.mContainingNotification.hasExpandingChild()) {
            return;
        }
        int size = this.mChildren.size();
        int actualHeight = this.mContainingNotification.getActualHeight() - this.mClipBottomAmount;
        for (int i2 = 0; i2 < size; i2++) {
            ExpandableNotificationRow expandableNotificationRow = this.mChildren.get(i2);
            if (expandableNotificationRow.getVisibility() != 8) {
                float translationY = expandableNotificationRow.getTranslationY();
                float actualHeight2 = expandableNotificationRow.getActualHeight() + translationY;
                float f = actualHeight;
                if (translationY <= f) {
                    if (actualHeight2 <= f) {
                        i = 0;
                    } else {
                        i = (int) (actualHeight2 - f);
                    }
                    z = true;
                } else {
                    z = false;
                    i = 0;
                }
                if (z != (expandableNotificationRow.getVisibility() == 0)) {
                    expandableNotificationRow.setVisibility(z ? 0 : 4);
                }
                expandableNotificationRow.setClipBottomAmount(i);
            }
        }
    }

    public void prepareExpansionChanged(StackScrollState stackScrollState) {
    }

    public void startAnimationToState(StackScrollState stackScrollState, AnimationProperties animationProperties) {
        int size = this.mChildren.size();
        ViewState viewState = new ViewState();
        float groupExpandFraction = getGroupExpandFraction();
        boolean z = (this.mUserLocked && !showingAsLowPriority()) || (this.mChildrenExpanded && this.mShowDividersWhenExpanded) || (this.mContainingNotification.isGroupExpansionChanging() && !this.mHideDividersDuringExpand);
        for (int i = size - 1; i >= 0; i--) {
            ExpandableNotificationRow expandableNotificationRow = this.mChildren.get(i);
            ExpandableViewState viewStateForView = stackScrollState.getViewStateForView(expandableNotificationRow);
            viewStateForView.animateTo(expandableNotificationRow, animationProperties);
            View view = this.mDividers.get(i);
            viewState.initFrom(view);
            viewState.yTranslation = viewStateForView.yTranslation - this.mDividerHeight;
            float f = (!this.mChildrenExpanded || viewStateForView.alpha == 0.0f) ? 0.0f : 0.5f;
            if (this.mUserLocked && !showingAsLowPriority() && viewStateForView.alpha != 0.0f) {
                f = NotificationUtils.interpolate(0.0f, 0.5f, Math.min(viewStateForView.alpha, groupExpandFraction));
            }
            viewState.hidden = !z;
            viewState.alpha = f;
            viewState.animateTo(view, animationProperties);
            expandableNotificationRow.setFakeShadowIntensity(0.0f, 0.0f, 0, 0);
        }
        if (this.mOverflowNumber != null) {
            if (this.mNeverAppliedGroupState) {
                float f2 = this.mGroupOverFlowState.alpha;
                this.mGroupOverFlowState.alpha = 0.0f;
                this.mGroupOverFlowState.applyToView(this.mOverflowNumber);
                this.mGroupOverFlowState.alpha = f2;
                this.mNeverAppliedGroupState = false;
            }
            this.mGroupOverFlowState.animateTo(this.mOverflowNumber, animationProperties);
        }
        if (this.mNotificationHeader != null) {
            this.mHeaderViewState.applyToView(this.mNotificationHeader);
        }
        updateChildrenClipping();
    }

    public ExpandableNotificationRow getViewAtPosition(float f) {
        int size = this.mChildren.size();
        for (int i = 0; i < size; i++) {
            ExpandableNotificationRow expandableNotificationRow = this.mChildren.get(i);
            float translationY = expandableNotificationRow.getTranslationY();
            float clipTopAmount = expandableNotificationRow.getClipTopAmount() + translationY;
            float actualHeight = translationY + expandableNotificationRow.getActualHeight();
            if (f >= clipTopAmount && f <= actualHeight) {
                return expandableNotificationRow;
            }
        }
        return null;
    }

    public void setChildrenExpanded(boolean z) {
        this.mChildrenExpanded = z;
        updateExpansionStates();
        if (this.mNotificationHeader != null) {
            this.mNotificationHeader.setExpanded(z);
        }
        int size = this.mChildren.size();
        for (int i = 0; i < size; i++) {
            this.mChildren.get(i).setChildrenExpanded(z, false);
        }
    }

    public void setContainingNotification(ExpandableNotificationRow expandableNotificationRow) {
        this.mContainingNotification = expandableNotificationRow;
        this.mHeaderUtil = new NotificationHeaderUtil(this.mContainingNotification);
    }

    public NotificationHeaderView getHeaderView() {
        return this.mNotificationHeader;
    }

    public NotificationHeaderView getLowPriorityHeaderView() {
        return this.mNotificationHeaderLowPriority;
    }

    @VisibleForTesting
    public ViewGroup getCurrentHeaderView() {
        return this.mCurrentHeader;
    }

    public void notifyShowAmbientChanged() {
        updateHeaderVisibility(false);
        updateGroupOverflow();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateHeaderVisibility(boolean z) {
        ViewGroup viewGroup = this.mCurrentHeader;
        ViewGroup calculateDesiredHeader = calculateDesiredHeader();
        if (viewGroup == calculateDesiredHeader) {
            return;
        }
        if (calculateDesiredHeader == this.mNotificationHeaderAmbient || viewGroup == this.mNotificationHeaderAmbient) {
            z = false;
        }
        if (z) {
            if (calculateDesiredHeader != null && viewGroup != null) {
                viewGroup.setVisibility(0);
                calculateDesiredHeader.setVisibility(0);
                TransformableView wrapperForView = getWrapperForView(calculateDesiredHeader);
                TransformableView wrapperForView2 = getWrapperForView(viewGroup);
                wrapperForView.transformFrom(wrapperForView2);
                wrapperForView2.transformTo(wrapperForView, new Runnable() { // from class: com.android.systemui.statusbar.stack.-$$Lambda$NotificationChildrenContainer$l4ZjZHbrQfPYWXU5LoOnRTSkuy8
                    @Override // java.lang.Runnable
                    public final void run() {
                        NotificationChildrenContainer.this.updateHeaderVisibility(false);
                    }
                });
                startChildAlphaAnimations(calculateDesiredHeader == this.mNotificationHeader);
            } else {
                z = false;
            }
        }
        if (!z) {
            if (calculateDesiredHeader != null) {
                getWrapperForView(calculateDesiredHeader).setVisible(true);
                calculateDesiredHeader.setVisibility(0);
            }
            if (viewGroup != null) {
                NotificationViewWrapper wrapperForView3 = getWrapperForView(viewGroup);
                if (wrapperForView3 != null) {
                    wrapperForView3.setVisible(false);
                }
                viewGroup.setVisibility(4);
            }
        }
        resetHeaderVisibilityIfNeeded(this.mNotificationHeader, calculateDesiredHeader);
        resetHeaderVisibilityIfNeeded(this.mNotificationHeaderAmbient, calculateDesiredHeader);
        resetHeaderVisibilityIfNeeded(this.mNotificationHeaderLowPriority, calculateDesiredHeader);
        this.mCurrentHeader = calculateDesiredHeader;
    }

    private void resetHeaderVisibilityIfNeeded(View view, View view2) {
        if (view == null) {
            return;
        }
        if (view != this.mCurrentHeader && view != view2) {
            getWrapperForView(view).setVisible(false);
            view.setVisibility(4);
        }
        if (view == view2 && view.getVisibility() != 0) {
            getWrapperForView(view).setVisible(true);
            view.setVisibility(0);
        }
    }

    private ViewGroup calculateDesiredHeader() {
        if (this.mContainingNotification.isShowingAmbient()) {
            return this.mNotificationHeaderAmbient;
        }
        if (showingAsLowPriority()) {
            return this.mNotificationHeaderLowPriority;
        }
        return this.mNotificationHeader;
    }

    private void startChildAlphaAnimations(boolean z) {
        float f;
        if (!z) {
            f = 0.0f;
        } else {
            f = 1.0f;
        }
        float f2 = 1.0f - f;
        int size = this.mChildren.size();
        for (int i = 0; i < size && i < 5; i++) {
            ExpandableNotificationRow expandableNotificationRow = this.mChildren.get(i);
            expandableNotificationRow.setAlpha(f2);
            ViewState viewState = new ViewState();
            viewState.initFrom(expandableNotificationRow);
            viewState.alpha = f;
            ALPHA_FADE_IN.setDelay(i * 50);
            viewState.animateTo(expandableNotificationRow, ALPHA_FADE_IN);
        }
    }

    private void updateHeaderTransformation() {
        if (this.mUserLocked && showingAsLowPriority()) {
            float groupExpandFraction = getGroupExpandFraction();
            this.mNotificationHeaderWrapper.transformFrom(this.mNotificationHeaderWrapperLowPriority, groupExpandFraction);
            this.mNotificationHeader.setVisibility(0);
            this.mNotificationHeaderWrapperLowPriority.transformTo(this.mNotificationHeaderWrapper, groupExpandFraction);
        }
    }

    private NotificationViewWrapper getWrapperForView(View view) {
        if (view == this.mNotificationHeader) {
            return this.mNotificationHeaderWrapper;
        }
        if (view == this.mNotificationHeaderAmbient) {
            return this.mNotificationHeaderWrapperAmbient;
        }
        return this.mNotificationHeaderWrapperLowPriority;
    }

    public void updateHeaderForExpansion(boolean z) {
        if (this.mNotificationHeader != null) {
            if (z) {
                ColorDrawable colorDrawable = new ColorDrawable();
                colorDrawable.setColor(this.mContainingNotification.calculateBgColor());
                this.mNotificationHeader.setHeaderBackgroundDrawable(colorDrawable);
                return;
            }
            this.mNotificationHeader.setHeaderBackgroundDrawable((Drawable) null);
        }
    }

    public int getMaxContentHeight() {
        int minHeight;
        if (showingAsLowPriority()) {
            return getMinHeight(5, true);
        }
        int i = this.mNotificationHeaderMargin + this.mCurrentHeaderTranslation + this.mNotificatonTopPadding;
        int size = this.mChildren.size();
        int i2 = i;
        int i3 = 0;
        for (int i4 = 0; i4 < size && i3 < 8; i4++) {
            ExpandableNotificationRow expandableNotificationRow = this.mChildren.get(i4);
            if (expandableNotificationRow.isExpanded(true)) {
                minHeight = expandableNotificationRow.getMaxExpandHeight();
            } else {
                minHeight = expandableNotificationRow.getShowingLayout().getMinHeight(true);
            }
            i2 = (int) (i2 + minHeight);
            i3++;
        }
        if (i3 > 0) {
            return i2 + (i3 * this.mDividerHeight);
        }
        return i2;
    }

    public void setActualHeight(int i) {
        float minHeight;
        if (!this.mUserLocked) {
            return;
        }
        this.mActualHeight = i;
        float groupExpandFraction = getGroupExpandFraction();
        boolean showingAsLowPriority = showingAsLowPriority();
        updateHeaderTransformation();
        int maxAllowedVisibleChildren = getMaxAllowedVisibleChildren(true);
        int size = this.mChildren.size();
        for (int i2 = 0; i2 < size; i2++) {
            ExpandableNotificationRow expandableNotificationRow = this.mChildren.get(i2);
            if (showingAsLowPriority) {
                minHeight = expandableNotificationRow.getShowingLayout().getMinHeight(false);
            } else if (!expandableNotificationRow.isExpanded(true)) {
                minHeight = expandableNotificationRow.getShowingLayout().getMinHeight(true);
            } else {
                minHeight = expandableNotificationRow.getMaxExpandHeight();
            }
            if (i2 < maxAllowedVisibleChildren) {
                expandableNotificationRow.setActualHeight((int) NotificationUtils.interpolate(expandableNotificationRow.getShowingLayout().getMinHeight(false), minHeight, groupExpandFraction), false);
            } else {
                expandableNotificationRow.setActualHeight((int) minHeight, false);
            }
        }
    }

    public float getGroupExpandFraction() {
        int maxContentHeight = showingAsLowPriority() ? getMaxContentHeight() : getVisibleChildrenExpandHeight();
        int collapsedHeight = getCollapsedHeight();
        return Math.max(0.0f, Math.min(1.0f, (this.mActualHeight - collapsedHeight) / (maxContentHeight - collapsedHeight)));
    }

    private int getVisibleChildrenExpandHeight() {
        int minHeight;
        int i = this.mNotificationHeaderMargin + this.mCurrentHeaderTranslation + this.mNotificatonTopPadding + this.mDividerHeight;
        int size = this.mChildren.size();
        int maxAllowedVisibleChildren = getMaxAllowedVisibleChildren(true);
        int i2 = i;
        int i3 = 0;
        for (int i4 = 0; i4 < size && i3 < maxAllowedVisibleChildren; i4++) {
            ExpandableNotificationRow expandableNotificationRow = this.mChildren.get(i4);
            if (!expandableNotificationRow.isExpanded(true)) {
                minHeight = expandableNotificationRow.getShowingLayout().getMinHeight(true);
            } else {
                minHeight = expandableNotificationRow.getMaxExpandHeight();
            }
            i2 = (int) (i2 + minHeight);
            i3++;
        }
        return i2;
    }

    public int getMinHeight() {
        int i;
        if (this.mContainingNotification.isShowingAmbient()) {
            i = 1;
        } else {
            i = 2;
        }
        return getMinHeight(i, false);
    }

    public int getCollapsedHeight() {
        return getMinHeight(getMaxAllowedVisibleChildren(true), false);
    }

    private int getMinHeight(int i, boolean z) {
        if (!z && showingAsLowPriority()) {
            return this.mNotificationHeaderLowPriority.getHeight();
        }
        int i2 = this.mNotificationHeaderMargin + this.mCurrentHeaderTranslation;
        int size = this.mChildren.size();
        int i3 = i2;
        boolean z2 = true;
        int i4 = 0;
        for (int i5 = 0; i5 < size && i4 < i; i5++) {
            if (!z2) {
                i3 += this.mChildPadding;
            } else {
                z2 = false;
            }
            i3 += this.mChildren.get(i5).getSingleLineView().getHeight();
            i4++;
        }
        return (int) (i3 + this.mCollapsedBottompadding);
    }

    public boolean showingAsLowPriority() {
        return this.mIsLowPriority && !this.mContainingNotification.isExpanded();
    }

    public void setDark(boolean z, boolean z2, long j) {
        if (this.mOverflowNumber != null) {
            this.mHybridGroupManager.setOverflowNumberDark(this.mOverflowNumber, z, z2, j);
        }
    }

    public void reInflateViews(View.OnClickListener onClickListener, StatusBarNotification statusBarNotification) {
        if (this.mNotificationHeader != null) {
            removeView(this.mNotificationHeader);
            this.mNotificationHeader = null;
        }
        if (this.mNotificationHeaderLowPriority != null) {
            removeView(this.mNotificationHeaderLowPriority);
            this.mNotificationHeaderLowPriority = null;
        }
        if (this.mNotificationHeaderAmbient != null) {
            removeView(this.mNotificationHeaderAmbient);
            this.mNotificationHeaderAmbient = null;
        }
        recreateNotificationHeader(onClickListener);
        initDimens();
        for (int i = 0; i < this.mDividers.size(); i++) {
            View view = this.mDividers.get(i);
            int indexOfChild = indexOfChild(view);
            removeView(view);
            View inflateDivider = inflateDivider();
            addView(inflateDivider, indexOfChild);
            this.mDividers.set(i, inflateDivider);
        }
        removeView(this.mOverflowNumber);
        this.mOverflowNumber = null;
        this.mGroupOverFlowState = null;
        updateGroupOverflow();
    }

    public void setUserLocked(boolean z) {
        this.mUserLocked = z;
        if (!this.mUserLocked) {
            updateHeaderVisibility(false);
        }
        int size = this.mChildren.size();
        for (int i = 0; i < size; i++) {
            this.mChildren.get(i).setUserLocked(z && !showingAsLowPriority());
        }
    }

    public void onNotificationUpdated() {
        this.mHybridGroupManager.setOverflowNumberColor(this.mOverflowNumber, this.mContainingNotification.getNotificationColor(), this.mContainingNotification.getNotificationColorAmbient());
    }

    public int getPositionInLinearLayout(View view) {
        int i = this.mNotificationHeaderMargin + this.mCurrentHeaderTranslation + this.mNotificatonTopPadding;
        for (int i2 = 0; i2 < this.mChildren.size(); i2++) {
            ExpandableNotificationRow expandableNotificationRow = this.mChildren.get(i2);
            boolean z = expandableNotificationRow.getVisibility() != 8;
            if (z) {
                i += this.mDividerHeight;
            }
            if (expandableNotificationRow == view) {
                return i;
            }
            if (z) {
                i += expandableNotificationRow.getIntrinsicHeight();
            }
        }
        return 0;
    }

    public void setIconsVisible(boolean z) {
        NotificationHeaderView notificationHeader;
        NotificationHeaderView notificationHeader2;
        if (this.mNotificationHeaderWrapper != null && (notificationHeader2 = this.mNotificationHeaderWrapper.getNotificationHeader()) != null) {
            notificationHeader2.getIcon().setForceHidden(!z);
        }
        if (this.mNotificationHeaderWrapperLowPriority != null && (notificationHeader = this.mNotificationHeaderWrapperLowPriority.getNotificationHeader()) != null) {
            notificationHeader.getIcon().setForceHidden(!z);
        }
    }

    public void setClipBottomAmount(int i) {
        this.mClipBottomAmount = i;
        updateChildrenClipping();
    }

    public void setIsLowPriority(boolean z) {
        this.mIsLowPriority = z;
        if (this.mContainingNotification != null) {
            recreateLowPriorityHeader(null);
            updateHeaderVisibility(false);
        }
        if (this.mUserLocked) {
            setUserLocked(this.mUserLocked);
        }
    }

    public NotificationHeaderView getVisibleHeader() {
        NotificationHeaderView notificationHeaderView = this.mNotificationHeader;
        if (showingAsLowPriority()) {
            return this.mNotificationHeaderLowPriority;
        }
        return notificationHeaderView;
    }

    public void onExpansionChanged() {
        if (this.mIsLowPriority) {
            if (this.mUserLocked) {
                setUserLocked(this.mUserLocked);
            }
            updateHeaderVisibility(true);
        }
    }

    public float getIncreasedPaddingAmount() {
        if (showingAsLowPriority()) {
            return 0.0f;
        }
        return getGroupExpandFraction();
    }

    @VisibleForTesting
    public boolean isUserLocked() {
        return this.mUserLocked;
    }

    public void setCurrentBottomRoundness(float f) {
        float f2;
        boolean z = true;
        for (int size = this.mChildren.size() - 1; size >= 0; size--) {
            ExpandableNotificationRow expandableNotificationRow = this.mChildren.get(size);
            if (expandableNotificationRow.getVisibility() != 8) {
                if (!z) {
                    f2 = 0.0f;
                } else {
                    f2 = f;
                }
                expandableNotificationRow.setBottomRoundness(f2, isShown());
                z = false;
            }
        }
    }

    public void setHeaderVisibleAmount(float f) {
        this.mHeaderVisibleAmount = f;
        this.mCurrentHeaderTranslation = (int) ((1.0f - f) * this.mTranslationForHeader);
    }
}

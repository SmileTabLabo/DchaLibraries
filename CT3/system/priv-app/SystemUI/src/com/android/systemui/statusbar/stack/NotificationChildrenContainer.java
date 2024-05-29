package com.android.systemui.statusbar.stack;

import android.app.Notification;
import android.content.Context;
import android.content.res.Configuration;
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
import com.android.systemui.ViewInvertHelper;
import com.android.systemui.statusbar.CrossFadeHelper;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.ExpandableView;
import com.android.systemui.statusbar.NotificationHeaderUtil;
import com.android.systemui.statusbar.notification.HybridGroupManager;
import com.android.systemui.statusbar.notification.HybridNotificationView;
import com.android.systemui.statusbar.notification.NotificationUtils;
import com.android.systemui.statusbar.notification.NotificationViewWrapper;
import java.util.ArrayList;
import java.util.List;
/* loaded from: a.zip:com/android/systemui/statusbar/stack/NotificationChildrenContainer.class */
public class NotificationChildrenContainer extends ViewGroup {
    private int mActualHeight;
    private int mChildPadding;
    private final List<ExpandableNotificationRow> mChildren;
    private boolean mChildrenExpanded;
    private float mCollapsedBottompadding;
    private int mDividerHeight;
    private final List<View> mDividers;
    private ViewState mGroupOverFlowState;
    private int mHeaderHeight;
    private NotificationHeaderUtil mHeaderUtil;
    private ViewState mHeaderViewState;
    private final HybridGroupManager mHybridGroupManager;
    private int mMaxNotificationHeight;
    private boolean mNeverAppliedGroupState;
    private NotificationHeaderView mNotificationHeader;
    private int mNotificationHeaderMargin;
    private NotificationViewWrapper mNotificationHeaderWrapper;
    private ExpandableNotificationRow mNotificationParent;
    private int mNotificatonTopPadding;
    private ViewInvertHelper mOverflowInvertHelper;
    private TextView mOverflowNumber;
    private int mRealHeight;
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
        initDimens();
        this.mHybridGroupManager = new HybridGroupManager(getContext(), this);
    }

    private int getIntrinsicHeight(float f) {
        int i;
        int interpolate;
        int i2 = this.mNotificationHeaderMargin;
        int i3 = 0;
        int size = this.mChildren.size();
        boolean z = true;
        float groupExpandFraction = this.mUserLocked ? getGroupExpandFraction() : 0.0f;
        for (int i4 = 0; i4 < size && i3 < f; i4++) {
            if (z) {
                interpolate = this.mUserLocked ? (int) (i2 + NotificationUtils.interpolate(0.0f, this.mNotificatonTopPadding + this.mDividerHeight, groupExpandFraction)) : i2 + (this.mChildrenExpanded ? this.mNotificatonTopPadding + this.mDividerHeight : 0);
                z = false;
            } else if (this.mUserLocked) {
                interpolate = (int) (i2 + NotificationUtils.interpolate(this.mChildPadding, this.mDividerHeight, groupExpandFraction));
            } else {
                interpolate = i2 + (this.mChildrenExpanded ? this.mDividerHeight : this.mChildPadding);
            }
            i2 = interpolate + this.mChildren.get(i4).getIntrinsicHeight();
            i3++;
        }
        if (this.mUserLocked) {
            i = (int) (i2 + NotificationUtils.interpolate(this.mCollapsedBottompadding, 0.0f, groupExpandFraction));
        } else {
            i = i2;
            if (!this.mChildrenExpanded) {
                i = (int) (i2 + this.mCollapsedBottompadding);
            }
        }
        return i;
    }

    private int getMaxAllowedVisibleChildren() {
        return getMaxAllowedVisibleChildren(false);
    }

    private int getMaxAllowedVisibleChildren(boolean z) {
        if (z || !(this.mChildrenExpanded || this.mNotificationParent.isUserLocked())) {
            if (this.mNotificationParent.isOnKeyguard()) {
                return 2;
            }
            return (this.mNotificationParent.isExpanded() || this.mNotificationParent.isHeadsUp()) ? 5 : 2;
        }
        return 8;
    }

    private int getMinHeight(int i) {
        int i2 = this.mNotificationHeaderMargin;
        int i3 = 0;
        boolean z = true;
        int size = this.mChildren.size();
        for (int i4 = 0; i4 < size && i3 < i; i4++) {
            if (z) {
                z = false;
            } else {
                i2 += this.mChildPadding;
            }
            i2 += this.mChildren.get(i4).getSingleLineView().getHeight();
            i3++;
        }
        return (int) (i2 + this.mCollapsedBottompadding);
    }

    private int getVisibleChildrenExpandHeight() {
        ExpandableNotificationRow expandableNotificationRow;
        int i = this.mNotificationHeaderMargin + this.mNotificatonTopPadding + this.mDividerHeight;
        int i2 = 0;
        int size = this.mChildren.size();
        int maxAllowedVisibleChildren = getMaxAllowedVisibleChildren(true);
        for (int i3 = 0; i3 < size && i2 < maxAllowedVisibleChildren; i3++) {
            i = (int) (i + (this.mChildren.get(i3).isExpanded(true) ? expandableNotificationRow.getMaxExpandHeight() : expandableNotificationRow.getShowingLayout().getMinHeight(true)));
            i2++;
        }
        return i;
    }

    private View inflateDivider() {
        return LayoutInflater.from(this.mContext).inflate(2130968724, (ViewGroup) this, false);
    }

    private void initDimens() {
        this.mChildPadding = getResources().getDimensionPixelSize(2131689897);
        this.mDividerHeight = Math.max(1, getResources().getDimensionPixelSize(2131689876));
        this.mHeaderHeight = getResources().getDimensionPixelSize(2131689877);
        this.mMaxNotificationHeight = getResources().getDimensionPixelSize(2131689787);
        this.mNotificationHeaderMargin = getResources().getDimensionPixelSize(17104964);
        this.mNotificatonTopPadding = getResources().getDimensionPixelSize(2131689898);
        this.mCollapsedBottompadding = getResources().getDimensionPixelSize(17104965);
    }

    private boolean updateChildStateForExpandedGroup(ExpandableNotificationRow expandableNotificationRow, int i, StackViewState stackViewState, int i2) {
        int clipTopAmount = i2 + expandableNotificationRow.getClipTopAmount();
        int intrinsicHeight = expandableNotificationRow.getIntrinsicHeight();
        int i3 = intrinsicHeight;
        if (clipTopAmount + intrinsicHeight >= i) {
            i3 = Math.max(i - clipTopAmount, 0);
        }
        stackViewState.hidden = i3 == 0;
        stackViewState.height = i3;
        boolean z = false;
        if (stackViewState.height != intrinsicHeight) {
            z = !stackViewState.hidden;
        }
        return z;
    }

    private void updateExpansionStates() {
        if (this.mChildrenExpanded || this.mUserLocked) {
            return;
        }
        int size = this.mChildren.size();
        int i = 0;
        while (i < size) {
            this.mChildren.get(i).setSystemChildExpanded(i == 0 && size == 1);
            i++;
        }
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
    }

    public boolean applyChildOrder(List<ExpandableNotificationRow> list) {
        if (list == null) {
            return false;
        }
        boolean z = false;
        for (int i = 0; i < this.mChildren.size() && i < list.size(); i++) {
            ExpandableNotificationRow expandableNotificationRow = this.mChildren.get(i);
            ExpandableNotificationRow expandableNotificationRow2 = list.get(i);
            if (expandableNotificationRow != expandableNotificationRow2) {
                this.mChildren.remove(expandableNotificationRow2);
                this.mChildren.add(i, expandableNotificationRow2);
                z = true;
            }
        }
        updateExpansionStates();
        return z;
    }

    public void applyState(StackScrollState stackScrollState) {
        int size = this.mChildren.size();
        ViewState viewState = new ViewState();
        float groupExpandFraction = this.mUserLocked ? getGroupExpandFraction() : 0.0f;
        boolean isGroupExpansionChanging = !this.mUserLocked ? this.mNotificationParent.isGroupExpansionChanging() : true;
        for (int i = 0; i < size; i++) {
            ExpandableView expandableView = (ExpandableNotificationRow) this.mChildren.get(i);
            StackViewState viewStateForView = stackScrollState.getViewStateForView(expandableView);
            stackScrollState.applyState(expandableView, viewStateForView);
            View view = this.mDividers.get(i);
            viewState.initFrom(view);
            viewState.yTranslation = viewStateForView.yTranslation - this.mDividerHeight;
            float f = (!this.mChildrenExpanded || viewStateForView.alpha == 0.0f) ? 0.0f : 0.5f;
            float f2 = f;
            if (this.mUserLocked) {
                f2 = f;
                if (viewStateForView.alpha != 0.0f) {
                    f2 = NotificationUtils.interpolate(0.0f, 0.5f, Math.min(viewStateForView.alpha, groupExpandFraction));
                }
            }
            viewState.hidden = !isGroupExpansionChanging;
            viewState.alpha = f2;
            stackScrollState.applyViewState(view, viewState);
            expandableView.setFakeShadowIntensity(0.0f, 0.0f, 0, 0);
        }
        if (this.mOverflowNumber != null) {
            stackScrollState.applyViewState(this.mOverflowNumber, this.mGroupOverFlowState);
            this.mNeverAppliedGroupState = false;
        }
        if (this.mNotificationHeader != null) {
            stackScrollState.applyViewState(this.mNotificationHeader, this.mHeaderViewState);
        }
    }

    public int getCollapsedHeight() {
        return getMinHeight(getMaxAllowedVisibleChildren(true));
    }

    public float getGroupExpandFraction() {
        int visibleChildrenExpandHeight = getVisibleChildrenExpandHeight();
        int collapsedHeight = getCollapsedHeight();
        return Math.max(0.0f, Math.min(1.0f, (this.mActualHeight - collapsedHeight) / (visibleChildrenExpandHeight - collapsedHeight)));
    }

    public NotificationHeaderView getHeaderView() {
        return this.mNotificationHeader;
    }

    public int getIntrinsicHeight() {
        return getIntrinsicHeight(getMaxAllowedVisibleChildren());
    }

    public int getMaxContentHeight() {
        ExpandableNotificationRow expandableNotificationRow;
        int i = this.mNotificationHeaderMargin + this.mNotificatonTopPadding;
        int i2 = 0;
        int size = this.mChildren.size();
        for (int i3 = 0; i3 < size && i2 < 8; i3++) {
            i = (int) (i + (this.mChildren.get(i3).isExpanded(true) ? expandableNotificationRow.getMaxExpandHeight() : expandableNotificationRow.getShowingLayout().getMinHeight(true)));
            i2++;
        }
        int i4 = i;
        if (i2 > 0) {
            i4 = i + (this.mDividerHeight * i2);
        }
        return i4;
    }

    public int getMinHeight() {
        return getMinHeight(2);
    }

    public int getNotificationChildCount() {
        return this.mChildren.size();
    }

    public List<ExpandableNotificationRow> getNotificationChildren() {
        return this.mChildren;
    }

    public int getPositionInLinearLayout(View view) {
        int i = this.mNotificationHeaderMargin + this.mNotificatonTopPadding;
        for (int i2 = 0; i2 < this.mChildren.size(); i2++) {
            ExpandableNotificationRow expandableNotificationRow = this.mChildren.get(i2);
            boolean z = expandableNotificationRow.getVisibility() != 8;
            int i3 = i;
            if (z) {
                i3 = i + this.mDividerHeight;
            }
            if (expandableNotificationRow == view) {
                return i3;
            }
            i = i3;
            if (z) {
                i = i3 + expandableNotificationRow.getIntrinsicHeight();
            }
        }
        return 0;
    }

    public void getState(StackScrollState stackScrollState, StackViewState stackViewState) {
        int interpolate;
        int size = this.mChildren.size();
        int i = this.mNotificationHeaderMargin;
        boolean z = true;
        int maxAllowedVisibleChildren = getMaxAllowedVisibleChildren() - 1;
        int i2 = maxAllowedVisibleChildren + 1;
        float f = 0.0f;
        if (this.mUserLocked) {
            f = getGroupExpandFraction();
            i2 = getMaxAllowedVisibleChildren(true);
        }
        boolean z2 = !this.mNotificationParent.isGroupExpansionChanging() ? this.mChildrenExpanded : false;
        int i3 = stackViewState.height;
        for (int i4 = 0; i4 < size; i4++) {
            ExpandableNotificationRow expandableNotificationRow = this.mChildren.get(i4);
            if (z) {
                interpolate = this.mUserLocked ? (int) (i + NotificationUtils.interpolate(0.0f, this.mNotificatonTopPadding + this.mDividerHeight, f)) : i + (this.mChildrenExpanded ? this.mNotificatonTopPadding + this.mDividerHeight : 0);
                z = false;
            } else if (this.mUserLocked) {
                interpolate = (int) (i + NotificationUtils.interpolate(this.mChildPadding, this.mDividerHeight, f));
            } else {
                interpolate = i + (this.mChildrenExpanded ? this.mDividerHeight : this.mChildPadding);
            }
            StackViewState viewStateForView = stackScrollState.getViewStateForView(expandableNotificationRow);
            int intrinsicHeight = expandableNotificationRow.getIntrinsicHeight();
            if (!z2) {
                viewStateForView.hidden = false;
                viewStateForView.height = intrinsicHeight;
                viewStateForView.isBottomClipped = false;
            } else if (updateChildStateForExpandedGroup(expandableNotificationRow, i3, viewStateForView, interpolate)) {
                viewStateForView.isBottomClipped = true;
            }
            viewStateForView.yTranslation = interpolate;
            viewStateForView.zTranslation = z2 ? this.mNotificationParent.getTranslationZ() : 0.0f;
            viewStateForView.dimmed = stackViewState.dimmed;
            viewStateForView.dark = stackViewState.dark;
            viewStateForView.hideSensitive = stackViewState.hideSensitive;
            viewStateForView.belowSpeedBump = stackViewState.belowSpeedBump;
            viewStateForView.clipTopAmount = 0;
            viewStateForView.alpha = 0.0f;
            if (i4 < i2) {
                viewStateForView.alpha = 1.0f;
            } else if (f == 1.0f && i4 <= maxAllowedVisibleChildren) {
                viewStateForView.alpha = (this.mActualHeight - viewStateForView.yTranslation) / viewStateForView.height;
                viewStateForView.alpha = Math.max(0.0f, Math.min(1.0f, viewStateForView.alpha));
            }
            viewStateForView.location = stackViewState.location;
            i = interpolate + intrinsicHeight;
        }
        if (this.mOverflowNumber != null) {
            ExpandableNotificationRow expandableNotificationRow2 = this.mChildren.get(Math.min(getMaxAllowedVisibleChildren(true), size) - 1);
            this.mGroupOverFlowState.copyFrom(stackScrollState.getViewStateForView(expandableNotificationRow2));
            if (this.mChildrenExpanded) {
                this.mGroupOverFlowState.yTranslation += this.mNotificationHeaderMargin;
                this.mGroupOverFlowState.alpha = 0.0f;
            } else if (this.mUserLocked) {
                HybridNotificationView singleLineView = expandableNotificationRow2.getSingleLineView();
                TextView textView = singleLineView.getTextView();
                TextView textView2 = textView;
                if (textView.getVisibility() == 8) {
                    textView2 = singleLineView.getTitleView();
                }
                TextView textView3 = textView2;
                if (textView2.getVisibility() == 8) {
                    textView3 = singleLineView;
                }
                this.mGroupOverFlowState.yTranslation += NotificationUtils.getRelativeYOffset(textView3, expandableNotificationRow2);
                this.mGroupOverFlowState.alpha = textView3.getAlpha();
            }
        }
        if (this.mNotificationHeader != null) {
            if (this.mHeaderViewState == null) {
                this.mHeaderViewState = new ViewState();
            }
            this.mHeaderViewState.initFrom(this.mNotificationHeader);
            this.mHeaderViewState.zTranslation = z2 ? this.mNotificationParent.getTranslationZ() : 0.0f;
        }
    }

    public ExpandableNotificationRow getViewAtPosition(float f) {
        int size = this.mChildren.size();
        for (int i = 0; i < size; i++) {
            ExpandableNotificationRow expandableNotificationRow = this.mChildren.get(i);
            float translationY = expandableNotificationRow.getTranslationY();
            float clipTopAmount = expandableNotificationRow.getClipTopAmount();
            float actualHeight = expandableNotificationRow.getActualHeight();
            if (f >= translationY + clipTopAmount && f <= translationY + actualHeight) {
                return expandableNotificationRow;
            }
        }
        return null;
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        updateGroupOverflow();
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int min = Math.min(this.mChildren.size(), 8);
        for (int i5 = 0; i5 < min; i5++) {
            ExpandableNotificationRow expandableNotificationRow = this.mChildren.get(i5);
            expandableNotificationRow.layout(0, 0, expandableNotificationRow.getMeasuredWidth(), expandableNotificationRow.getMeasuredHeight());
            this.mDividers.get(i5).layout(0, 0, getWidth(), this.mDividerHeight);
        }
        if (this.mOverflowNumber != null) {
            this.mOverflowNumber.layout(getWidth() - this.mOverflowNumber.getMeasuredWidth(), 0, getWidth(), this.mOverflowNumber.getMeasuredHeight());
        }
        if (this.mNotificationHeader != null) {
            this.mNotificationHeader.layout(0, 0, this.mNotificationHeader.getMeasuredWidth(), this.mNotificationHeader.getMeasuredHeight());
        }
    }

    /* JADX WARN: Code restructure failed: missing block: B:11:0x0030, code lost:
        if (r10 != false) goto L47;
     */
    @Override // android.view.View
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    protected void onMeasure(int i, int i2) {
        int min;
        int i3 = this.mMaxNotificationHeight;
        int mode = View.MeasureSpec.getMode(i2);
        boolean z = mode == 1073741824;
        boolean z2 = mode == Integer.MIN_VALUE;
        int size = View.MeasureSpec.getSize(i2);
        if (!z) {
            min = i3;
        }
        min = Math.min(i3, size);
        int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(min, Integer.MIN_VALUE);
        int size2 = View.MeasureSpec.getSize(i);
        if (this.mOverflowNumber != null) {
            this.mOverflowNumber.measure(View.MeasureSpec.makeMeasureSpec(size2, Integer.MIN_VALUE), makeMeasureSpec);
        }
        int makeMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(this.mDividerHeight, 1073741824);
        int i4 = this.mNotificationHeaderMargin + this.mNotificatonTopPadding;
        int min2 = Math.min(this.mChildren.size(), 8);
        int maxAllowedVisibleChildren = getMaxAllowedVisibleChildren(true);
        int i5 = min2 > maxAllowedVisibleChildren ? maxAllowedVisibleChildren - 1 : -1;
        int i6 = 0;
        while (i6 < min2) {
            ExpandableNotificationRow expandableNotificationRow = this.mChildren.get(i6);
            expandableNotificationRow.setSingleLineWidthIndention((!(i6 == i5) || this.mOverflowNumber == null) ? 0 : this.mOverflowNumber.getMeasuredWidth());
            expandableNotificationRow.measure(i, makeMeasureSpec);
            this.mDividers.get(i6).measure(i, makeMeasureSpec2);
            int i7 = i4;
            if (expandableNotificationRow.getVisibility() != 8) {
                i7 = i4 + expandableNotificationRow.getMeasuredHeight() + this.mDividerHeight;
            }
            i6++;
            i4 = i7;
        }
        this.mRealHeight = i4;
        int i8 = i4;
        if (mode != 0) {
            i8 = Math.min(i4, size);
        }
        if (this.mNotificationHeader != null) {
            this.mNotificationHeader.measure(i, View.MeasureSpec.makeMeasureSpec(this.mHeaderHeight, 1073741824));
        }
        setMeasuredDimension(size2, i8);
    }

    public void onNotificationUpdated() {
        this.mHybridGroupManager.setOverflowNumberColor(this.mOverflowNumber, this.mNotificationParent.getNotificationColor());
    }

    public boolean pointInView(float f, float f2, float f3) {
        boolean z = false;
        if (f >= (-f3)) {
            z = false;
            if (f2 >= (-f3)) {
                z = false;
                if (f < (this.mRight - this.mLeft) + f3) {
                    z = false;
                    if (f2 < this.mRealHeight + f3) {
                        z = true;
                    }
                }
            }
        }
        return z;
    }

    public void prepareExpansionChanged(StackScrollState stackScrollState) {
    }

    public void reInflateViews(View.OnClickListener onClickListener, StatusBarNotification statusBarNotification) {
        removeView(this.mNotificationHeader);
        this.mNotificationHeader = null;
        recreateNotificationHeader(onClickListener, statusBarNotification);
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
        this.mOverflowInvertHelper = null;
        this.mGroupOverFlowState = null;
        updateGroupOverflow();
    }

    public void recreateNotificationHeader(View.OnClickListener onClickListener, StatusBarNotification statusBarNotification) {
        RemoteViews makeNotificationHeader = Notification.Builder.recoverBuilder(getContext(), this.mNotificationParent.getStatusBarNotification().getNotification()).makeNotificationHeader();
        if (this.mNotificationHeader == null) {
            this.mNotificationHeader = makeNotificationHeader.apply(getContext(), this);
            this.mNotificationHeader.findViewById(16909230).setVisibility(0);
            this.mNotificationHeader.setOnClickListener(onClickListener);
            this.mNotificationHeaderWrapper = NotificationViewWrapper.wrap(getContext(), this.mNotificationHeader, this.mNotificationParent);
            addView((View) this.mNotificationHeader, 0);
            invalidate();
        } else {
            makeNotificationHeader.reapply(getContext(), this.mNotificationHeader);
            this.mNotificationHeaderWrapper.notifyContentUpdated(statusBarNotification);
        }
        updateChildrenHeaderAppearance();
    }

    public void removeNotification(ExpandableNotificationRow expandableNotificationRow) {
        int indexOf = this.mChildren.indexOf(expandableNotificationRow);
        this.mChildren.remove(expandableNotificationRow);
        removeView(expandableNotificationRow);
        View remove = this.mDividers.remove(indexOf);
        removeView(remove);
        getOverlay().add(remove);
        CrossFadeHelper.fadeOut(remove, new Runnable(this, remove) { // from class: com.android.systemui.statusbar.stack.NotificationChildrenContainer.1
            final NotificationChildrenContainer this$0;
            final View val$divider;

            {
                this.this$0 = this;
                this.val$divider = remove;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.getOverlay().remove(this.val$divider);
            }
        });
        expandableNotificationRow.setSystemChildExpanded(false);
        expandableNotificationRow.setUserLocked(false);
        updateGroupOverflow();
        if (expandableNotificationRow.isRemoved()) {
            return;
        }
        this.mHeaderUtil.restoreNotificationHeader(expandableNotificationRow);
    }

    public void setActualHeight(int i) {
        if (this.mUserLocked) {
            this.mActualHeight = i;
            float groupExpandFraction = getGroupExpandFraction();
            int maxAllowedVisibleChildren = getMaxAllowedVisibleChildren(true);
            int size = this.mChildren.size();
            for (int i2 = 0; i2 < size; i2++) {
                ExpandableNotificationRow expandableNotificationRow = this.mChildren.get(i2);
                float maxExpandHeight = expandableNotificationRow.isExpanded(true) ? expandableNotificationRow.getMaxExpandHeight() : expandableNotificationRow.getShowingLayout().getMinHeight(true);
                if (i2 < maxAllowedVisibleChildren) {
                    expandableNotificationRow.setActualHeight((int) NotificationUtils.interpolate(expandableNotificationRow.getShowingLayout().getMinHeight(false), maxExpandHeight, groupExpandFraction), false);
                } else {
                    expandableNotificationRow.setActualHeight((int) maxExpandHeight, false);
                }
            }
        }
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

    public void setDark(boolean z, boolean z2, long j) {
        if (this.mOverflowNumber != null) {
            this.mOverflowInvertHelper.setInverted(z, z2, j);
        }
        this.mNotificationHeaderWrapper.setDark(z, z2, j);
    }

    public void setNotificationParent(ExpandableNotificationRow expandableNotificationRow) {
        this.mNotificationParent = expandableNotificationRow;
        this.mHeaderUtil = new NotificationHeaderUtil(this.mNotificationParent);
    }

    public void setUserLocked(boolean z) {
        this.mUserLocked = z;
        int size = this.mChildren.size();
        for (int i = 0; i < size; i++) {
            this.mChildren.get(i).setUserLocked(z);
        }
    }

    public void startAnimationToState(StackScrollState stackScrollState, StackStateAnimator stackStateAnimator, long j, long j2) {
        int size = this.mChildren.size();
        ViewState viewState = new ViewState();
        float groupExpandFraction = getGroupExpandFraction();
        boolean isGroupExpansionChanging = !this.mUserLocked ? this.mNotificationParent.isGroupExpansionChanging() : true;
        while (true) {
            size--;
            if (size < 0) {
                break;
            }
            ExpandableNotificationRow expandableNotificationRow = this.mChildren.get(size);
            StackViewState viewStateForView = stackScrollState.getViewStateForView(expandableNotificationRow);
            stackStateAnimator.startStackAnimations(expandableNotificationRow, viewStateForView, stackScrollState, -1, j);
            View view = this.mDividers.get(size);
            viewState.initFrom(view);
            viewState.yTranslation = viewStateForView.yTranslation - this.mDividerHeight;
            float f = (!this.mChildrenExpanded || viewStateForView.alpha == 0.0f) ? 0.0f : 0.5f;
            float f2 = f;
            if (this.mUserLocked) {
                f2 = f;
                if (viewStateForView.alpha != 0.0f) {
                    f2 = NotificationUtils.interpolate(0.0f, 0.5f, Math.min(viewStateForView.alpha, groupExpandFraction));
                }
            }
            viewState.hidden = !isGroupExpansionChanging;
            viewState.alpha = f2;
            stackStateAnimator.startViewAnimations(view, viewState, j, j2);
            expandableNotificationRow.setFakeShadowIntensity(0.0f, 0.0f, 0, 0);
        }
        if (this.mOverflowNumber != null) {
            if (this.mNeverAppliedGroupState) {
                float f3 = this.mGroupOverFlowState.alpha;
                this.mGroupOverFlowState.alpha = 0.0f;
                stackScrollState.applyViewState(this.mOverflowNumber, this.mGroupOverFlowState);
                this.mGroupOverFlowState.alpha = f3;
                this.mNeverAppliedGroupState = false;
            }
            stackStateAnimator.startViewAnimations(this.mOverflowNumber, this.mGroupOverFlowState, j, j2);
        }
        if (this.mNotificationHeader != null) {
            stackScrollState.applyViewState(this.mNotificationHeader, this.mHeaderViewState);
        }
    }

    public void updateChildrenHeaderAppearance() {
        this.mHeaderUtil.updateChildrenHeaderAppearance();
    }

    public void updateGroupOverflow() {
        int size = this.mChildren.size();
        int maxAllowedVisibleChildren = getMaxAllowedVisibleChildren(true);
        if (size > maxAllowedVisibleChildren) {
            this.mOverflowNumber = this.mHybridGroupManager.bindOverflowNumber(this.mOverflowNumber, size - maxAllowedVisibleChildren);
            if (this.mOverflowInvertHelper == null) {
                this.mOverflowInvertHelper = new ViewInvertHelper(this.mOverflowNumber, 700L);
            }
            if (this.mGroupOverFlowState == null) {
                this.mGroupOverFlowState = new ViewState();
                this.mNeverAppliedGroupState = true;
            }
        } else if (this.mOverflowNumber != null) {
            removeView(this.mOverflowNumber);
            if (isShown()) {
                TextView textView = this.mOverflowNumber;
                addTransientView(textView, getTransientViewCount());
                CrossFadeHelper.fadeOut(textView, new Runnable(this, textView) { // from class: com.android.systemui.statusbar.stack.NotificationChildrenContainer.2
                    final NotificationChildrenContainer this$0;
                    final View val$removedOverflowNumber;

                    {
                        this.this$0 = this;
                        this.val$removedOverflowNumber = textView;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        this.this$0.removeTransientView(this.val$removedOverflowNumber);
                    }
                });
            }
            this.mOverflowNumber = null;
            this.mOverflowInvertHelper = null;
            this.mGroupOverFlowState = null;
        }
    }

    public void updateHeaderForExpansion(boolean z) {
        if (this.mNotificationHeader != null) {
            if (!z) {
                this.mNotificationHeader.setHeaderBackgroundDrawable((Drawable) null);
                return;
            }
            ColorDrawable colorDrawable = new ColorDrawable();
            colorDrawable.setColor(this.mNotificationParent.calculateBgColor());
            this.mNotificationHeader.setHeaderBackgroundDrawable(colorDrawable);
        }
    }

    public void updateHeaderVisibility(int i) {
        if (this.mNotificationHeader != null) {
            this.mNotificationHeader.setVisibility(i);
        }
    }
}

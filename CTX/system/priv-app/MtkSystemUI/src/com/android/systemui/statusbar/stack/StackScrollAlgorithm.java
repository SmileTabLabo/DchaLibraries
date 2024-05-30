package com.android.systemui.statusbar.stack;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.R;
import com.android.systemui.statusbar.ActivatableNotificationView;
import com.android.systemui.statusbar.EmptyShadeView;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.ExpandableView;
import com.android.systemui.statusbar.FooterView;
import com.android.systemui.statusbar.NotificationShelf;
import com.android.systemui.statusbar.notification.NotificationUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
/* loaded from: classes.dex */
public class StackScrollAlgorithm {
    private boolean mClipNotificationScrollToTop;
    private int mCollapsedSize;
    private float mHeadsUpInset;
    private int mIncreasedPaddingBetweenElements;
    private boolean mIsExpanded;
    private int mPaddingBetweenElements;
    private int mPinnedZTranslationExtra;
    private int mStatusBarHeight;
    private StackScrollAlgorithmState mTempAlgorithmState = new StackScrollAlgorithmState();

    public StackScrollAlgorithm(Context context) {
        initView(context);
    }

    public void initView(Context context) {
        initConstants(context);
    }

    private void initConstants(Context context) {
        Resources resources = context.getResources();
        this.mPaddingBetweenElements = resources.getDimensionPixelSize(R.dimen.notification_divider_height);
        this.mIncreasedPaddingBetweenElements = resources.getDimensionPixelSize(R.dimen.notification_divider_height_increased);
        this.mCollapsedSize = resources.getDimensionPixelSize(R.dimen.notification_min_height);
        this.mStatusBarHeight = resources.getDimensionPixelSize(R.dimen.status_bar_height);
        this.mClipNotificationScrollToTop = resources.getBoolean(R.bool.config_clipNotificationScrollToTop);
        this.mHeadsUpInset = this.mStatusBarHeight + resources.getDimensionPixelSize(R.dimen.heads_up_status_bar_padding);
        this.mPinnedZTranslationExtra = resources.getDimensionPixelSize(R.dimen.heads_up_pinned_elevation);
    }

    public void getStackScrollState(AmbientState ambientState, StackScrollState stackScrollState) {
        StackScrollAlgorithmState stackScrollAlgorithmState = this.mTempAlgorithmState;
        stackScrollState.resetViewStates();
        initAlgorithmState(stackScrollState, stackScrollAlgorithmState, ambientState);
        updatePositionsForState(stackScrollState, stackScrollAlgorithmState, ambientState);
        updateZValuesForState(stackScrollState, stackScrollAlgorithmState, ambientState);
        updateHeadsUpStates(stackScrollState, stackScrollAlgorithmState, ambientState);
        handleDraggedViews(ambientState, stackScrollState, stackScrollAlgorithmState);
        updateDimmedActivatedHideSensitive(ambientState, stackScrollState, stackScrollAlgorithmState);
        updateClipping(stackScrollState, stackScrollAlgorithmState, ambientState);
        updateSpeedBumpState(stackScrollState, stackScrollAlgorithmState, ambientState);
        updateShelfState(stackScrollState, ambientState);
        getNotificationChildrenStates(stackScrollState, stackScrollAlgorithmState, ambientState);
    }

    private void getNotificationChildrenStates(StackScrollState stackScrollState, StackScrollAlgorithmState stackScrollAlgorithmState, AmbientState ambientState) {
        int size = stackScrollAlgorithmState.visibleChildren.size();
        for (int i = 0; i < size; i++) {
            ExpandableView expandableView = stackScrollAlgorithmState.visibleChildren.get(i);
            if (expandableView instanceof ExpandableNotificationRow) {
                ((ExpandableNotificationRow) expandableView).getChildrenStates(stackScrollState, ambientState);
            }
        }
    }

    private void updateSpeedBumpState(StackScrollState stackScrollState, StackScrollAlgorithmState stackScrollAlgorithmState, AmbientState ambientState) {
        int size = stackScrollAlgorithmState.visibleChildren.size();
        int speedBumpIndex = ambientState.getSpeedBumpIndex();
        int i = 0;
        while (i < size) {
            stackScrollState.getViewStateForView(stackScrollAlgorithmState.visibleChildren.get(i)).belowSpeedBump = i >= speedBumpIndex;
            i++;
        }
    }

    private void updateShelfState(StackScrollState stackScrollState, AmbientState ambientState) {
        NotificationShelf shelf = ambientState.getShelf();
        if (shelf != null) {
            shelf.updateState(stackScrollState, ambientState);
        }
    }

    private void updateClipping(StackScrollState stackScrollState, StackScrollAlgorithmState stackScrollAlgorithmState, AmbientState ambientState) {
        float f;
        if (!ambientState.isOnKeyguard()) {
            f = ambientState.getTopPadding() + ambientState.getStackTranslation() + ambientState.getExpandAnimationTopChange();
        } else {
            f = 0.0f;
        }
        int size = stackScrollAlgorithmState.visibleChildren.size();
        float f2 = 0.0f;
        float f3 = 0.0f;
        for (int i = 0; i < size; i++) {
            ExpandableView expandableView = stackScrollAlgorithmState.visibleChildren.get(i);
            ExpandableViewState viewStateForView = stackScrollState.getViewStateForView(expandableView);
            if (!expandableView.mustStayOnScreen() || viewStateForView.headsUpIsVisible) {
                f2 = Math.max(f, f2);
                f3 = Math.max(f, f3);
            }
            float f4 = viewStateForView.yTranslation;
            float f5 = viewStateForView.height + f4;
            boolean z = (expandableView instanceof ExpandableNotificationRow) && ((ExpandableNotificationRow) expandableView).isPinned();
            if (this.mClipNotificationScrollToTop && !viewStateForView.inShelf && f4 < f2 && (!z || ambientState.isShadeExpanded())) {
                viewStateForView.clipTopAmount = (int) (f2 - f4);
            } else {
                viewStateForView.clipTopAmount = 0;
            }
            if (!expandableView.isTransparent()) {
                f3 = f4;
                f2 = f5;
            }
        }
    }

    public static boolean canChildBeDismissed(View view) {
        if (view instanceof ExpandableNotificationRow) {
            ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) view;
            if (expandableNotificationRow.areGutsExposed()) {
                return false;
            }
            return expandableNotificationRow.canViewBeDismissed();
        }
        return false;
    }

    private void updateDimmedActivatedHideSensitive(AmbientState ambientState, StackScrollState stackScrollState, StackScrollAlgorithmState stackScrollAlgorithmState) {
        boolean isDimmed = ambientState.isDimmed();
        boolean isFullyDark = ambientState.isFullyDark();
        boolean isHideSensitive = ambientState.isHideSensitive();
        ActivatableNotificationView activatedChild = ambientState.getActivatedChild();
        int size = stackScrollAlgorithmState.visibleChildren.size();
        for (int i = 0; i < size; i++) {
            ExpandableView expandableView = stackScrollAlgorithmState.visibleChildren.get(i);
            ExpandableViewState viewStateForView = stackScrollState.getViewStateForView(expandableView);
            viewStateForView.dimmed = isDimmed;
            viewStateForView.dark = isFullyDark;
            viewStateForView.hideSensitive = isHideSensitive;
            boolean z = activatedChild == expandableView;
            if (isDimmed && z) {
                viewStateForView.zTranslation += 2.0f * ambientState.getZDistanceBetweenElements();
            }
        }
    }

    private void handleDraggedViews(AmbientState ambientState, StackScrollState stackScrollState, StackScrollAlgorithmState stackScrollAlgorithmState) {
        ArrayList<View> draggedViews = ambientState.getDraggedViews();
        Iterator<View> it = draggedViews.iterator();
        while (it.hasNext()) {
            View next = it.next();
            int indexOf = stackScrollAlgorithmState.visibleChildren.indexOf(next);
            if (indexOf >= 0 && indexOf < stackScrollAlgorithmState.visibleChildren.size() - 1) {
                ExpandableView expandableView = stackScrollAlgorithmState.visibleChildren.get(indexOf + 1);
                if (!draggedViews.contains(expandableView)) {
                    ExpandableViewState viewStateForView = stackScrollState.getViewStateForView(expandableView);
                    if (ambientState.isShadeExpanded()) {
                        viewStateForView.shadowAlpha = 1.0f;
                        viewStateForView.hidden = false;
                    }
                }
                stackScrollState.getViewStateForView(next).alpha = next.getAlpha();
            }
        }
    }

    private void initAlgorithmState(StackScrollState stackScrollState, StackScrollAlgorithmState stackScrollAlgorithmState, AmbientState ambientState) {
        stackScrollAlgorithmState.scrollY = (int) (Math.max(0, ambientState.getScrollY()) + ambientState.getOverScrollAmount(false));
        ViewGroup hostView = stackScrollState.getHostView();
        int childCount = hostView.getChildCount();
        stackScrollAlgorithmState.visibleChildren.clear();
        stackScrollAlgorithmState.visibleChildren.ensureCapacity(childCount);
        stackScrollAlgorithmState.paddingMap.clear();
        int i = ambientState.isDark() ? ambientState.hasPulsingNotifications() ? 1 : 0 : childCount;
        int i2 = 0;
        ExpandableView expandableView = null;
        for (int i3 = 0; i3 < childCount; i3++) {
            ExpandableView expandableView2 = (ExpandableView) hostView.getChildAt(i3);
            if (expandableView2.getVisibility() != 8 && expandableView2 != ambientState.getShelf()) {
                if (i3 >= i) {
                    expandableView = null;
                }
                i2 = updateNotGoneIndex(stackScrollState, stackScrollAlgorithmState, i2, expandableView2);
                float increasedPaddingAmount = expandableView2.getIncreasedPaddingAmount();
                int i4 = (increasedPaddingAmount > 0.0f ? 1 : (increasedPaddingAmount == 0.0f ? 0 : -1));
                if (i4 != 0) {
                    stackScrollAlgorithmState.paddingMap.put(expandableView2, Float.valueOf(increasedPaddingAmount));
                    if (expandableView != null) {
                        Float f = stackScrollAlgorithmState.paddingMap.get(expandableView);
                        float paddingForValue = getPaddingForValue(Float.valueOf(increasedPaddingAmount));
                        if (f != null) {
                            float paddingForValue2 = getPaddingForValue(f);
                            if (i4 > 0) {
                                paddingForValue = NotificationUtils.interpolate(paddingForValue2, paddingForValue, increasedPaddingAmount);
                            } else if (f.floatValue() > 0.0f) {
                                paddingForValue = NotificationUtils.interpolate(paddingForValue, paddingForValue2, f.floatValue());
                            }
                        }
                        stackScrollAlgorithmState.paddingMap.put(expandableView, Float.valueOf(paddingForValue));
                    }
                } else if (expandableView != null) {
                    stackScrollAlgorithmState.paddingMap.put(expandableView, Float.valueOf(getPaddingForValue(stackScrollAlgorithmState.paddingMap.get(expandableView))));
                }
                if (expandableView2 instanceof ExpandableNotificationRow) {
                    ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) expandableView2;
                    List<ExpandableNotificationRow> notificationChildren = expandableNotificationRow.getNotificationChildren();
                    if (expandableNotificationRow.isSummaryWithChildren() && notificationChildren != null) {
                        for (ExpandableNotificationRow expandableNotificationRow2 : notificationChildren) {
                            if (expandableNotificationRow2.getVisibility() != 8) {
                                stackScrollState.getViewStateForView(expandableNotificationRow2).notGoneIndex = i2;
                                i2++;
                            }
                        }
                    }
                }
                expandableView = expandableView2;
            }
        }
        ExpandableNotificationRow expandingNotification = ambientState.getExpandingNotification();
        stackScrollAlgorithmState.indexOfExpandingNotification = expandingNotification != null ? expandingNotification.isChildInGroup() ? stackScrollAlgorithmState.visibleChildren.indexOf(expandingNotification.getNotificationParent()) : stackScrollAlgorithmState.visibleChildren.indexOf(expandingNotification) : -1;
    }

    private float getPaddingForValue(Float f) {
        if (f == null) {
            return this.mPaddingBetweenElements;
        }
        if (f.floatValue() >= 0.0f) {
            return NotificationUtils.interpolate(this.mPaddingBetweenElements, this.mIncreasedPaddingBetweenElements, f.floatValue());
        }
        return NotificationUtils.interpolate(0.0f, this.mPaddingBetweenElements, 1.0f + f.floatValue());
    }

    private int updateNotGoneIndex(StackScrollState stackScrollState, StackScrollAlgorithmState stackScrollAlgorithmState, int i, ExpandableView expandableView) {
        stackScrollState.getViewStateForView(expandableView).notGoneIndex = i;
        stackScrollAlgorithmState.visibleChildren.add(expandableView);
        return i + 1;
    }

    private void updatePositionsForState(StackScrollState stackScrollState, StackScrollAlgorithmState stackScrollAlgorithmState, AmbientState ambientState) {
        int size = stackScrollAlgorithmState.visibleChildren.size();
        float f = -stackScrollAlgorithmState.scrollY;
        for (int i = 0; i < size; i++) {
            f = updateChild(i, stackScrollState, stackScrollAlgorithmState, ambientState, f);
        }
    }

    protected float updateChild(int i, StackScrollState stackScrollState, StackScrollAlgorithmState stackScrollAlgorithmState, AmbientState ambientState, float f) {
        ExpandableView expandableView = stackScrollAlgorithmState.visibleChildren.get(i);
        ExpandableViewState viewStateForView = stackScrollState.getViewStateForView(expandableView);
        viewStateForView.location = 0;
        int paddingAfterChild = getPaddingAfterChild(stackScrollAlgorithmState, expandableView);
        int maxAllowedChildHeight = getMaxAllowedChildHeight(expandableView);
        viewStateForView.yTranslation = f;
        boolean z = expandableView instanceof FooterView;
        boolean z2 = expandableView instanceof EmptyShadeView;
        viewStateForView.location = 4;
        float topPadding = ambientState.getTopPadding() + ambientState.getStackTranslation();
        if (i <= stackScrollAlgorithmState.getIndexOfExpandingNotification()) {
            topPadding += ambientState.getExpandAnimationTopChange();
        }
        if (expandableView.mustStayOnScreen() && viewStateForView.yTranslation >= 0.0f) {
            viewStateForView.headsUpIsVisible = (viewStateForView.yTranslation + ((float) viewStateForView.height)) + topPadding < ambientState.getMaxHeadsUpTranslation();
        }
        if (z) {
            viewStateForView.yTranslation = Math.min(viewStateForView.yTranslation, ambientState.getInnerHeight() - maxAllowedChildHeight);
        } else if (z2) {
            viewStateForView.yTranslation = (ambientState.getInnerHeight() - maxAllowedChildHeight) + (ambientState.getStackTranslation() * 0.25f);
        } else {
            clampPositionToShelf(expandableView, viewStateForView, ambientState);
        }
        float f2 = viewStateForView.yTranslation + maxAllowedChildHeight + paddingAfterChild;
        if (f2 <= 0.0f) {
            viewStateForView.location = 2;
        }
        if (viewStateForView.location == 0) {
            Log.wtf("StackScrollAlgorithm", "Failed to assign location for child " + i);
        }
        viewStateForView.yTranslation += topPadding;
        return f2;
    }

    protected int getPaddingAfterChild(StackScrollAlgorithmState stackScrollAlgorithmState, ExpandableView expandableView) {
        return stackScrollAlgorithmState.getPaddingAfterChild(expandableView);
    }

    private void updateHeadsUpStates(StackScrollState stackScrollState, StackScrollAlgorithmState stackScrollAlgorithmState, AmbientState ambientState) {
        int size = stackScrollAlgorithmState.visibleChildren.size();
        View view = null;
        for (int i = 0; i < size; i++) {
            ExpandableView expandableView = stackScrollAlgorithmState.visibleChildren.get(i);
            if (expandableView instanceof ExpandableNotificationRow) {
                ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) expandableView;
                if (expandableNotificationRow.isHeadsUp()) {
                    ExpandableViewState viewStateForView = stackScrollState.getViewStateForView(expandableNotificationRow);
                    boolean z = true;
                    if (view == null && expandableNotificationRow.mustStayOnScreen() && !viewStateForView.headsUpIsVisible) {
                        viewStateForView.location = 1;
                        view = expandableNotificationRow;
                    }
                    if (view != expandableNotificationRow) {
                        z = false;
                    }
                    float f = viewStateForView.yTranslation + viewStateForView.height;
                    if (this.mIsExpanded && expandableNotificationRow.mustStayOnScreen() && !viewStateForView.headsUpIsVisible) {
                        clampHunToTop(ambientState, expandableNotificationRow, viewStateForView);
                        if (i == 0 && ambientState.isAboveShelf(expandableNotificationRow)) {
                            clampHunToMaxTranslation(ambientState, expandableNotificationRow, viewStateForView);
                            viewStateForView.hidden = false;
                        }
                    }
                    if (expandableNotificationRow.isPinned()) {
                        viewStateForView.yTranslation = Math.max(viewStateForView.yTranslation, this.mHeadsUpInset);
                        viewStateForView.height = Math.max(expandableNotificationRow.getIntrinsicHeight(), viewStateForView.height);
                        viewStateForView.hidden = false;
                        ExpandableViewState viewStateForView2 = stackScrollState.getViewStateForView(view);
                        if (viewStateForView2 != null && !z && (!this.mIsExpanded || f < viewStateForView2.yTranslation + viewStateForView2.height)) {
                            viewStateForView.height = expandableNotificationRow.getIntrinsicHeight();
                            viewStateForView.yTranslation = (viewStateForView2.yTranslation + viewStateForView2.height) - viewStateForView.height;
                        }
                    }
                    if (expandableNotificationRow.isHeadsUpAnimatingAway()) {
                        viewStateForView.hidden = false;
                    }
                } else {
                    return;
                }
            } else {
                return;
            }
        }
    }

    private void clampHunToTop(AmbientState ambientState, ExpandableNotificationRow expandableNotificationRow, ExpandableViewState expandableViewState) {
        float max = Math.max(ambientState.getTopPadding() + ambientState.getStackTranslation(), expandableViewState.yTranslation);
        expandableViewState.height = (int) Math.max(expandableViewState.height - (max - expandableViewState.yTranslation), expandableNotificationRow.getCollapsedHeight());
        expandableViewState.yTranslation = max;
    }

    private void clampHunToMaxTranslation(AmbientState ambientState, ExpandableNotificationRow expandableNotificationRow, ExpandableViewState expandableViewState) {
        float min = Math.min(ambientState.getMaxHeadsUpTranslation(), ambientState.getInnerHeight() + ambientState.getTopPadding() + ambientState.getStackTranslation());
        float min2 = Math.min(expandableViewState.yTranslation, min - expandableNotificationRow.getCollapsedHeight());
        expandableViewState.height = (int) Math.min(expandableViewState.height, min - min2);
        expandableViewState.yTranslation = min2;
    }

    private void clampPositionToShelf(ExpandableView expandableView, ExpandableViewState expandableViewState, AmbientState ambientState) {
        if (ambientState.getShelf() == null) {
            return;
        }
        int innerHeight = ambientState.getInnerHeight() - ambientState.getShelf().getIntrinsicHeight();
        if (ambientState.isAppearing() && !expandableView.isAboveShelf()) {
            expandableViewState.yTranslation = Math.max(expandableViewState.yTranslation, innerHeight);
        }
        float f = innerHeight;
        expandableViewState.yTranslation = Math.min(expandableViewState.yTranslation, f);
        if (expandableViewState.yTranslation >= f) {
            expandableViewState.hidden = (expandableView.isExpandAnimationRunning() || expandableView.hasExpandingChild()) ? false : true;
            expandableViewState.inShelf = true;
            expandableViewState.headsUpIsVisible = false;
        }
    }

    protected int getMaxAllowedChildHeight(View view) {
        if (view instanceof ExpandableView) {
            return ((ExpandableView) view).getIntrinsicHeight();
        }
        return view == null ? this.mCollapsedSize : view.getHeight();
    }

    private void updateZValuesForState(StackScrollState stackScrollState, StackScrollAlgorithmState stackScrollAlgorithmState, AmbientState ambientState) {
        float f = 0.0f;
        for (int size = stackScrollAlgorithmState.visibleChildren.size() - 1; size >= 0; size--) {
            f = updateChildZValue(size, f, stackScrollState, stackScrollAlgorithmState, ambientState);
        }
    }

    protected float updateChildZValue(int i, float f, StackScrollState stackScrollState, StackScrollAlgorithmState stackScrollAlgorithmState, AmbientState ambientState) {
        ExpandableView expandableView = stackScrollAlgorithmState.visibleChildren.get(i);
        ExpandableViewState viewStateForView = stackScrollState.getViewStateForView(expandableView);
        int zDistanceBetweenElements = ambientState.getZDistanceBetweenElements();
        float baseZHeight = ambientState.getBaseZHeight();
        if (expandableView.mustStayOnScreen() && !viewStateForView.headsUpIsVisible && !ambientState.isDozingAndNotPulsing(expandableView) && viewStateForView.yTranslation < ambientState.getTopPadding() + ambientState.getStackTranslation()) {
            if (f != 0.0f) {
                f += 1.0f;
            } else {
                f += Math.min(1.0f, ((ambientState.getTopPadding() + ambientState.getStackTranslation()) - viewStateForView.yTranslation) / viewStateForView.height);
            }
            viewStateForView.zTranslation = baseZHeight + (zDistanceBetweenElements * f);
        } else if (i == 0 && ambientState.isAboveShelf(expandableView)) {
            int intrinsicHeight = ambientState.getShelf() == null ? 0 : ambientState.getShelf().getIntrinsicHeight();
            float innerHeight = (ambientState.getInnerHeight() - intrinsicHeight) + ambientState.getTopPadding() + ambientState.getStackTranslation();
            float pinnedHeadsUpHeight = viewStateForView.yTranslation + expandableView.getPinnedHeadsUpHeight() + this.mPaddingBetweenElements;
            if (innerHeight > pinnedHeadsUpHeight) {
                viewStateForView.zTranslation = baseZHeight;
            } else {
                viewStateForView.zTranslation = baseZHeight + (Math.min((pinnedHeadsUpHeight - innerHeight) / intrinsicHeight, 1.0f) * zDistanceBetweenElements);
            }
        } else {
            viewStateForView.zTranslation = baseZHeight;
        }
        viewStateForView.zTranslation += (1.0f - expandableView.getHeaderVisibleAmount()) * this.mPinnedZTranslationExtra;
        return f;
    }

    public void setIsExpanded(boolean z) {
        this.mIsExpanded = z;
    }

    /* loaded from: classes.dex */
    public class StackScrollAlgorithmState {
        private int indexOfExpandingNotification;
        public int scrollY;
        public final ArrayList<ExpandableView> visibleChildren = new ArrayList<>();
        public final HashMap<ExpandableView, Float> paddingMap = new HashMap<>();

        public StackScrollAlgorithmState() {
        }

        public int getPaddingAfterChild(ExpandableView expandableView) {
            Float f = this.paddingMap.get(expandableView);
            if (f == null) {
                return StackScrollAlgorithm.this.mPaddingBetweenElements;
            }
            return (int) f.floatValue();
        }

        public int getIndexOfExpandingNotification() {
            return this.indexOfExpandingNotification;
        }
    }
}

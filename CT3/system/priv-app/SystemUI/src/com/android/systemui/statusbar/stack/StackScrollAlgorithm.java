package com.android.systemui.statusbar.stack;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.statusbar.ActivatableNotificationView;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.ExpandableView;
import com.android.systemui.statusbar.notification.NotificationUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
/* loaded from: a.zip:com/android/systemui/statusbar/stack/StackScrollAlgorithm.class */
public class StackScrollAlgorithm {
    private StackIndentationFunctor mBottomStackIndentationFunctor;
    private int mBottomStackPeekSize;
    private int mBottomStackSlowDownLength;
    private int mCollapsedSize;
    private int mIncreasedPaddingBetweenElements;
    private boolean mIsExpanded;
    private int mPaddingBetweenElements;
    private StackScrollAlgorithmState mTempAlgorithmState = new StackScrollAlgorithmState(this);
    private int mZBasicHeight;
    private int mZDistanceBetweenElements;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:com/android/systemui/statusbar/stack/StackScrollAlgorithm$StackScrollAlgorithmState.class */
    public class StackScrollAlgorithmState {
        public float itemsInBottomStack;
        public float partialInBottom;
        public int scrollY;
        final StackScrollAlgorithm this$0;
        public final ArrayList<ExpandableView> visibleChildren = new ArrayList<>();
        public final HashMap<ExpandableView, Float> increasedPaddingMap = new HashMap<>();

        StackScrollAlgorithmState(StackScrollAlgorithm stackScrollAlgorithm) {
            this.this$0 = stackScrollAlgorithm;
        }
    }

    public StackScrollAlgorithm(Context context) {
        initView(context);
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

    private void clampHunToMaxTranslation(AmbientState ambientState, ExpandableNotificationRow expandableNotificationRow, StackViewState stackViewState) {
        float min = Math.min(stackViewState.yTranslation, ambientState.getMaxHeadsUpTranslation() - expandableNotificationRow.getCollapsedHeight());
        stackViewState.height = (int) Math.max(stackViewState.height - (stackViewState.yTranslation - min), expandableNotificationRow.getCollapsedHeight());
        stackViewState.yTranslation = min;
    }

    private void clampHunToTop(AmbientState ambientState, ExpandableNotificationRow expandableNotificationRow, StackViewState stackViewState) {
        float max = Math.max(ambientState.getTopPadding() + ambientState.getStackTranslation(), stackViewState.yTranslation);
        stackViewState.height = (int) Math.max(stackViewState.height - (max - stackViewState.yTranslation), expandableNotificationRow.getCollapsedHeight());
        stackViewState.yTranslation = max;
    }

    private void clampPositionToBottomStackStart(StackViewState stackViewState, int i, int i2, AmbientState ambientState) {
        int innerHeight = (ambientState.getInnerHeight() - this.mBottomStackPeekSize) - this.mBottomStackSlowDownLength;
        if (innerHeight - i < stackViewState.yTranslation) {
            float f = innerHeight - stackViewState.yTranslation;
            float f2 = f;
            if (f < i2) {
                f2 = i2;
                stackViewState.yTranslation = innerHeight - i2;
            }
            stackViewState.height = (int) f2;
        }
    }

    private int getMaxAllowedChildHeight(View view) {
        if (view instanceof ExpandableView) {
            return ((ExpandableView) view).getIntrinsicHeight();
        }
        return view == null ? this.mCollapsedSize : view.getHeight();
    }

    private void getNotificationChildrenStates(StackScrollState stackScrollState, StackScrollAlgorithmState stackScrollAlgorithmState) {
        int size = stackScrollAlgorithmState.visibleChildren.size();
        for (int i = 0; i < size; i++) {
            ExpandableView expandableView = stackScrollAlgorithmState.visibleChildren.get(i);
            if (expandableView instanceof ExpandableNotificationRow) {
                ((ExpandableNotificationRow) expandableView).getChildrenStates(stackScrollState);
            }
        }
    }

    private int getPaddingAfterChild(StackScrollAlgorithmState stackScrollAlgorithmState, ExpandableView expandableView) {
        Float f = stackScrollAlgorithmState.increasedPaddingMap.get(expandableView);
        return f == null ? this.mPaddingBetweenElements : (int) NotificationUtils.interpolate(this.mPaddingBetweenElements, this.mIncreasedPaddingBetweenElements, f.floatValue());
    }

    private void handleDraggedViews(AmbientState ambientState, StackScrollState stackScrollState, StackScrollAlgorithmState stackScrollAlgorithmState) {
        ArrayList<View> draggedViews = ambientState.getDraggedViews();
        for (View view : draggedViews) {
            int indexOf = stackScrollAlgorithmState.visibleChildren.indexOf(view);
            if (indexOf >= 0 && indexOf < stackScrollAlgorithmState.visibleChildren.size() - 1) {
                ExpandableView expandableView = stackScrollAlgorithmState.visibleChildren.get(indexOf + 1);
                if (!draggedViews.contains(expandableView)) {
                    StackViewState viewStateForView = stackScrollState.getViewStateForView(expandableView);
                    if (ambientState.isShadeExpanded()) {
                        viewStateForView.shadowAlpha = 1.0f;
                        viewStateForView.hidden = false;
                    }
                }
                stackScrollState.getViewStateForView(view).alpha = view.getAlpha();
            }
        }
    }

    private void initAlgorithmState(StackScrollState stackScrollState, StackScrollAlgorithmState stackScrollAlgorithmState, AmbientState ambientState) {
        stackScrollAlgorithmState.itemsInBottomStack = 0.0f;
        stackScrollAlgorithmState.partialInBottom = 0.0f;
        stackScrollAlgorithmState.scrollY = (int) (Math.max(0, ambientState.getScrollY()) + ambientState.getOverScrollAmount(false));
        ViewGroup hostView = stackScrollState.getHostView();
        int childCount = hostView.getChildCount();
        stackScrollAlgorithmState.visibleChildren.clear();
        stackScrollAlgorithmState.visibleChildren.ensureCapacity(childCount);
        stackScrollAlgorithmState.increasedPaddingMap.clear();
        int i = 0;
        ExpandableView expandableView = null;
        int i2 = 0;
        while (i2 < childCount) {
            ExpandableView expandableView2 = (ExpandableView) hostView.getChildAt(i2);
            ExpandableView expandableView3 = expandableView;
            int i3 = i;
            if (expandableView2.getVisibility() != 8) {
                int updateNotGoneIndex = updateNotGoneIndex(stackScrollState, stackScrollAlgorithmState, i, expandableView2);
                float increasedPaddingAmount = expandableView2.getIncreasedPaddingAmount();
                if (increasedPaddingAmount != 0.0f) {
                    stackScrollAlgorithmState.increasedPaddingMap.put(expandableView2, Float.valueOf(increasedPaddingAmount));
                    if (expandableView != null) {
                        Float f = stackScrollAlgorithmState.increasedPaddingMap.get(expandableView);
                        if (f != null) {
                            increasedPaddingAmount = Math.max(f.floatValue(), increasedPaddingAmount);
                        }
                        stackScrollAlgorithmState.increasedPaddingMap.put(expandableView, Float.valueOf(increasedPaddingAmount));
                    }
                }
                int i4 = updateNotGoneIndex;
                if (expandableView2 instanceof ExpandableNotificationRow) {
                    ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) expandableView2;
                    List<ExpandableNotificationRow> notificationChildren = expandableNotificationRow.getNotificationChildren();
                    i4 = updateNotGoneIndex;
                    if (expandableNotificationRow.isSummaryWithChildren()) {
                        i4 = updateNotGoneIndex;
                        if (notificationChildren != null) {
                            Iterator<T> it = notificationChildren.iterator();
                            while (true) {
                                i4 = updateNotGoneIndex;
                                if (!it.hasNext()) {
                                    break;
                                }
                                ExpandableNotificationRow expandableNotificationRow2 = (ExpandableNotificationRow) it.next();
                                if (expandableNotificationRow2.getVisibility() != 8) {
                                    stackScrollState.getViewStateForView(expandableNotificationRow2).notGoneIndex = updateNotGoneIndex;
                                    updateNotGoneIndex++;
                                }
                            }
                        }
                    }
                }
                expandableView3 = expandableView2;
                i3 = i4;
            }
            i2++;
            expandableView = expandableView3;
            i = i3;
        }
    }

    private void initConstants(Context context) {
        this.mPaddingBetweenElements = Math.max(1, context.getResources().getDimensionPixelSize(2131689876));
        this.mIncreasedPaddingBetweenElements = context.getResources().getDimensionPixelSize(2131689878);
        this.mCollapsedSize = context.getResources().getDimensionPixelSize(2131689785);
        this.mBottomStackPeekSize = context.getResources().getDimensionPixelSize(2131689872);
        this.mZDistanceBetweenElements = Math.max(1, context.getResources().getDimensionPixelSize(2131689875));
        this.mZBasicHeight = this.mZDistanceBetweenElements * 4;
        this.mBottomStackSlowDownLength = context.getResources().getDimensionPixelSize(2131689874);
        this.mBottomStackIndentationFunctor = new PiecewiseLinearIndentationFunctor(3, this.mBottomStackPeekSize, getBottomStackSlowDownLength(), 0.5f);
    }

    private void updateClipping(StackScrollState stackScrollState, StackScrollAlgorithmState stackScrollAlgorithmState, AmbientState ambientState) {
        float topPadding = ambientState.getTopPadding() + ambientState.getStackTranslation();
        float f = 0.0f;
        float f2 = 0.0f;
        int size = stackScrollAlgorithmState.visibleChildren.size();
        int i = 0;
        while (i < size) {
            ExpandableView expandableView = stackScrollAlgorithmState.visibleChildren.get(i);
            StackViewState viewStateForView = stackScrollState.getViewStateForView(expandableView);
            float f3 = f;
            float f4 = f2;
            if (!expandableView.mustStayOnScreen()) {
                f3 = Math.max(topPadding, f);
                f4 = Math.max(topPadding, f2);
            }
            float f5 = viewStateForView.yTranslation;
            float f6 = viewStateForView.height;
            boolean isPinned = expandableView instanceof ExpandableNotificationRow ? ((ExpandableNotificationRow) expandableView).isPinned() : false;
            if (f5 >= f3 || (isPinned && !ambientState.isShadeExpanded())) {
                viewStateForView.clipTopAmount = 0;
            } else {
                viewStateForView.clipTopAmount = (int) (f3 - f5);
            }
            if (!expandableView.isTransparent()) {
                f3 = f5 + f6;
                f4 = f5;
            }
            i++;
            f = f3;
            f2 = f4;
        }
    }

    private void updateDimmedActivatedHideSensitive(AmbientState ambientState, StackScrollState stackScrollState, StackScrollAlgorithmState stackScrollAlgorithmState) {
        boolean isDimmed = ambientState.isDimmed();
        boolean isDark = ambientState.isDark();
        boolean isHideSensitive = ambientState.isHideSensitive();
        ActivatableNotificationView activatedChild = ambientState.getActivatedChild();
        int size = stackScrollAlgorithmState.visibleChildren.size();
        for (int i = 0; i < size; i++) {
            ExpandableView expandableView = stackScrollAlgorithmState.visibleChildren.get(i);
            StackViewState viewStateForView = stackScrollState.getViewStateForView(expandableView);
            viewStateForView.dimmed = isDimmed;
            viewStateForView.dark = isDark;
            viewStateForView.hideSensitive = isHideSensitive;
            boolean z = activatedChild == expandableView;
            if (isDimmed && z) {
                viewStateForView.zTranslation += this.mZDistanceBetweenElements * 2.0f;
            }
        }
    }

    private void updateFirstChildHeight(ExpandableView expandableView, StackViewState stackViewState, int i, AmbientState ambientState) {
        stackViewState.height = (int) Math.max(Math.min(((ambientState.getInnerHeight() - this.mBottomStackPeekSize) - this.mBottomStackSlowDownLength) + ambientState.getScrollY(), i), expandableView.getCollapsedHeight());
    }

    private void updateHeadsUpStates(StackScrollState stackScrollState, StackScrollAlgorithmState stackScrollAlgorithmState, AmbientState ambientState) {
        int size = stackScrollAlgorithmState.visibleChildren.size();
        View view = null;
        int i = 0;
        while (i < size) {
            ExpandableView expandableView = stackScrollAlgorithmState.visibleChildren.get(i);
            if (!(expandableView instanceof ExpandableNotificationRow)) {
                return;
            }
            ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) expandableView;
            if (!expandableNotificationRow.isHeadsUp()) {
                return;
            }
            StackViewState viewStateForView = stackScrollState.getViewStateForView(expandableNotificationRow);
            View view2 = view;
            if (view == null) {
                view2 = expandableNotificationRow;
                viewStateForView.location = 1;
            }
            boolean z = view2 == expandableNotificationRow;
            float f = viewStateForView.yTranslation;
            float f2 = viewStateForView.height;
            if (this.mIsExpanded) {
                clampHunToTop(ambientState, expandableNotificationRow, viewStateForView);
                clampHunToMaxTranslation(ambientState, expandableNotificationRow, viewStateForView);
            }
            if (expandableNotificationRow.isPinned()) {
                viewStateForView.yTranslation = Math.max(viewStateForView.yTranslation, 0.0f);
                viewStateForView.height = Math.max(expandableNotificationRow.getIntrinsicHeight(), viewStateForView.height);
                StackViewState viewStateForView2 = stackScrollState.getViewStateForView(view2);
                if (!z && (!this.mIsExpanded || f + f2 < viewStateForView2.yTranslation + viewStateForView2.height)) {
                    viewStateForView.height = expandableNotificationRow.getIntrinsicHeight();
                    viewStateForView.yTranslation = (viewStateForView2.yTranslation + viewStateForView2.height) - viewStateForView.height;
                }
            }
            i++;
            view = view2;
        }
    }

    private int updateNotGoneIndex(StackScrollState stackScrollState, StackScrollAlgorithmState stackScrollAlgorithmState, int i, ExpandableView expandableView) {
        stackScrollState.getViewStateForView(expandableView).notGoneIndex = i;
        stackScrollAlgorithmState.visibleChildren.add(expandableView);
        return i + 1;
    }

    private void updatePositionsForState(StackScrollState stackScrollState, StackScrollAlgorithmState stackScrollAlgorithmState, AmbientState ambientState) {
        float innerHeight = (ambientState.getInnerHeight() - this.mBottomStackPeekSize) - this.mBottomStackSlowDownLength;
        float f = -stackScrollAlgorithmState.scrollY;
        int size = stackScrollAlgorithmState.visibleChildren.size();
        for (int i = 0; i < size; i++) {
            ExpandableView expandableView = stackScrollAlgorithmState.visibleChildren.get(i);
            StackViewState viewStateForView = stackScrollState.getViewStateForView(expandableView);
            viewStateForView.location = 0;
            int paddingAfterChild = getPaddingAfterChild(stackScrollAlgorithmState, expandableView);
            int maxAllowedChildHeight = getMaxAllowedChildHeight(expandableView);
            int collapsedHeight = expandableView.getCollapsedHeight();
            viewStateForView.yTranslation = f;
            if (i == 0) {
                updateFirstChildHeight(expandableView, viewStateForView, maxAllowedChildHeight, ambientState);
            }
            if (maxAllowedChildHeight + f + paddingAfterChild < innerHeight) {
                viewStateForView.location = 4;
                clampPositionToBottomStackStart(viewStateForView, viewStateForView.height, maxAllowedChildHeight, ambientState);
            } else if (f >= innerHeight) {
                updateStateForChildFullyInBottomStack(stackScrollAlgorithmState, innerHeight, viewStateForView, collapsedHeight, ambientState, expandableView);
            } else {
                updateStateForChildTransitioningInBottom(stackScrollAlgorithmState, innerHeight, expandableView, f, viewStateForView, maxAllowedChildHeight);
            }
            if (i == 0 && ambientState.getScrollY() <= 0) {
                viewStateForView.yTranslation = Math.max(0.0f, viewStateForView.yTranslation);
            }
            f = viewStateForView.yTranslation + maxAllowedChildHeight + paddingAfterChild;
            if (f <= 0.0f) {
                viewStateForView.location = 2;
            }
            if (viewStateForView.location == 0) {
                Log.wtf("StackScrollAlgorithm", "Failed to assign location for child " + i);
            }
            viewStateForView.yTranslation += ambientState.getTopPadding() + ambientState.getStackTranslation();
        }
    }

    private void updateSpeedBumpState(StackScrollState stackScrollState, StackScrollAlgorithmState stackScrollAlgorithmState, int i) {
        int size = stackScrollAlgorithmState.visibleChildren.size();
        int i2 = 0;
        while (i2 < size) {
            stackScrollState.getViewStateForView(stackScrollAlgorithmState.visibleChildren.get(i2)).belowSpeedBump = i != -1 && i2 >= i;
            i2++;
        }
    }

    private void updateStateForChildFullyInBottomStack(StackScrollAlgorithmState stackScrollAlgorithmState, float f, StackViewState stackViewState, int i, AmbientState ambientState, ExpandableView expandableView) {
        float innerHeight;
        stackScrollAlgorithmState.itemsInBottomStack += 1.0f;
        if (stackScrollAlgorithmState.itemsInBottomStack < 3.0f) {
            innerHeight = (this.mBottomStackIndentationFunctor.getValue(stackScrollAlgorithmState.itemsInBottomStack) + f) - getPaddingAfterChild(stackScrollAlgorithmState, expandableView);
            stackViewState.location = 8;
        } else {
            if (stackScrollAlgorithmState.itemsInBottomStack > 5.0f) {
                stackViewState.hidden = true;
                stackViewState.shadowAlpha = 0.0f;
            } else if (stackScrollAlgorithmState.itemsInBottomStack > 4.0f) {
                stackViewState.shadowAlpha = 1.0f - stackScrollAlgorithmState.partialInBottom;
            }
            stackViewState.location = 16;
            innerHeight = ambientState.getInnerHeight();
        }
        stackViewState.height = i;
        stackViewState.yTranslation = innerHeight - i;
    }

    private void updateStateForChildTransitioningInBottom(StackScrollAlgorithmState stackScrollAlgorithmState, float f, ExpandableView expandableView, float f2, StackViewState stackViewState, int i) {
        stackScrollAlgorithmState.partialInBottom = 1.0f - ((f - f2) / (getPaddingAfterChild(stackScrollAlgorithmState, expandableView) + i));
        float value = this.mBottomStackIndentationFunctor.getValue(stackScrollAlgorithmState.partialInBottom);
        stackScrollAlgorithmState.itemsInBottomStack += stackScrollAlgorithmState.partialInBottom;
        int i2 = i;
        if (i > expandableView.getCollapsedHeight()) {
            i2 = (int) Math.max(Math.min(((f + value) - getPaddingAfterChild(stackScrollAlgorithmState, expandableView)) - f2, i), expandableView.getCollapsedHeight());
            stackViewState.height = i2;
        }
        stackViewState.yTranslation = ((f + value) - i2) - getPaddingAfterChild(stackScrollAlgorithmState, expandableView);
        stackViewState.location = 4;
    }

    private void updateZValuesForState(StackScrollState stackScrollState, StackScrollAlgorithmState stackScrollAlgorithmState, AmbientState ambientState) {
        int size;
        float f;
        float f2 = 0.0f;
        int size2 = stackScrollAlgorithmState.visibleChildren.size() - 1;
        while (size2 >= 0) {
            ExpandableView expandableView = stackScrollAlgorithmState.visibleChildren.get(size2);
            StackViewState viewStateForView = stackScrollState.getViewStateForView(expandableView);
            if (size2 > (size - 1) - stackScrollAlgorithmState.itemsInBottomStack) {
                float f3 = size2 - ((size - 1) - stackScrollAlgorithmState.itemsInBottomStack);
                viewStateForView.zTranslation = this.mZBasicHeight - (f3 <= 1.0f ? f3 <= 0.2f ? (0.1f * f3) * 5.0f : 0.1f + (((f3 - 0.2f) * (1.0f / 0.8f)) * (this.mZDistanceBetweenElements - 0.1f)) : f3 * this.mZDistanceBetweenElements);
                f = f2;
            } else if (!expandableView.mustStayOnScreen() || viewStateForView.yTranslation >= ambientState.getTopPadding() + ambientState.getStackTranslation()) {
                viewStateForView.zTranslation = this.mZBasicHeight;
                f = f2;
            } else {
                f = f2 != 0.0f ? f2 + 1.0f : f2 + Math.min(1.0f, ((ambientState.getTopPadding() + ambientState.getStackTranslation()) - viewStateForView.yTranslation) / viewStateForView.height);
                viewStateForView.zTranslation = this.mZBasicHeight + (this.mZDistanceBetweenElements * f);
            }
            size2--;
            f2 = f;
        }
    }

    public int getBottomStackSlowDownLength() {
        return this.mBottomStackSlowDownLength + this.mPaddingBetweenElements;
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
        updateSpeedBumpState(stackScrollState, stackScrollAlgorithmState, ambientState.getSpeedBumpIndex());
        getNotificationChildrenStates(stackScrollState, stackScrollAlgorithmState);
    }

    public void initView(Context context) {
        initConstants(context);
    }

    public void setIsExpanded(boolean z) {
        this.mIsExpanded = z;
    }
}

package com.android.systemui.statusbar.stack;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.statusbar.DismissView;
import com.android.systemui.statusbar.EmptyShadeView;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.ExpandableView;
import java.util.List;
import java.util.WeakHashMap;
/* loaded from: a.zip:com/android/systemui/statusbar/stack/StackScrollState.class */
public class StackScrollState {
    private final int mClearAllTopPadding;
    private final ViewGroup mHostView;
    private WeakHashMap<ExpandableView, StackViewState> mStateMap = new WeakHashMap<>();

    public StackScrollState(ViewGroup viewGroup) {
        this.mHostView = viewGroup;
        this.mClearAllTopPadding = viewGroup.getContext().getResources().getDimensionPixelSize(2131689919);
    }

    private void resetViewState(ExpandableView expandableView) {
        StackViewState stackViewState = this.mStateMap.get(expandableView);
        StackViewState stackViewState2 = stackViewState;
        if (stackViewState == null) {
            stackViewState2 = new StackViewState();
            this.mStateMap.put(expandableView, stackViewState2);
        }
        stackViewState2.height = expandableView.getIntrinsicHeight();
        stackViewState2.gone = expandableView.getVisibility() == 8;
        stackViewState2.alpha = 1.0f;
        stackViewState2.shadowAlpha = 1.0f;
        stackViewState2.notGoneIndex = -1;
        stackViewState2.hidden = false;
    }

    public void apply() {
        int childCount = this.mHostView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ExpandableView expandableView = (ExpandableView) this.mHostView.getChildAt(i);
            StackViewState stackViewState = this.mStateMap.get(expandableView);
            if (applyState(expandableView, stackViewState)) {
                if (expandableView instanceof DismissView) {
                    DismissView dismissView = (DismissView) expandableView;
                    dismissView.performVisibilityAnimation((stackViewState.clipTopAmount < this.mClearAllTopPadding) && !dismissView.willBeGone());
                } else if (expandableView instanceof EmptyShadeView) {
                    EmptyShadeView emptyShadeView = (EmptyShadeView) expandableView;
                    emptyShadeView.performVisibilityAnimation((stackViewState.clipTopAmount <= 0) && !emptyShadeView.willBeGone());
                }
            }
        }
    }

    public boolean applyState(ExpandableView expandableView, StackViewState stackViewState) {
        if (stackViewState == null) {
            Log.wtf("StackScrollStateNoSuchChild", "No child state was found when applying this state to the hostView");
            return false;
        } else if (stackViewState.gone) {
            return false;
        } else {
            applyViewState(expandableView, stackViewState);
            int actualHeight = expandableView.getActualHeight();
            int i = stackViewState.height;
            if (actualHeight != i) {
                expandableView.setActualHeight(i, false);
            }
            float shadowAlpha = expandableView.getShadowAlpha();
            float f = stackViewState.shadowAlpha;
            if (shadowAlpha != f) {
                expandableView.setShadowAlpha(f);
            }
            expandableView.setDimmed(stackViewState.dimmed, false);
            expandableView.setHideSensitive(stackViewState.hideSensitive, false, 0L, 0L);
            expandableView.setBelowSpeedBump(stackViewState.belowSpeedBump);
            expandableView.setDark(stackViewState.dark, false, 0L);
            if (expandableView.getClipTopAmount() != stackViewState.clipTopAmount) {
                expandableView.setClipTopAmount(stackViewState.clipTopAmount);
            }
            if (expandableView instanceof ExpandableNotificationRow) {
                ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) expandableView;
                if (stackViewState.isBottomClipped) {
                    expandableNotificationRow.setClipToActualHeight(true);
                }
                expandableNotificationRow.applyChildrenState(this);
                return true;
            }
            return true;
        }
    }

    public void applyViewState(View view, ViewState viewState) {
        float alpha = view.getAlpha();
        float translationY = view.getTranslationY();
        float translationX = view.getTranslationX();
        float translationZ = view.getTranslationZ();
        float f = viewState.alpha;
        float f2 = viewState.yTranslation;
        float f3 = viewState.zTranslation;
        boolean z = f != 0.0f ? viewState.hidden : true;
        if (alpha != f && translationX == 0.0f) {
            boolean hasOverlappingRendering = (z || ((f > 1.0f ? 1 : (f == 1.0f ? 0 : -1)) == 0)) ? false : view.hasOverlappingRendering();
            int layerType = view.getLayerType();
            int i = hasOverlappingRendering ? 2 : 0;
            if (layerType != i) {
                view.setLayerType(i, null);
            }
            view.setAlpha(f);
        }
        int visibility = view.getVisibility();
        int i2 = z ? 4 : 0;
        if (i2 != visibility && (!(view instanceof ExpandableView) || !((ExpandableView) view).willBeGone())) {
            view.setVisibility(i2);
        }
        if (translationY != f2) {
            view.setTranslationY(f2);
        }
        if (translationZ != f3) {
            view.setTranslationZ(f3);
        }
    }

    public ViewGroup getHostView() {
        return this.mHostView;
    }

    public StackViewState getViewStateForView(View view) {
        return this.mStateMap.get(view);
    }

    public void removeViewStateForView(View view) {
        this.mStateMap.remove(view);
    }

    public void resetViewStates() {
        int childCount = this.mHostView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ExpandableView expandableView = (ExpandableView) this.mHostView.getChildAt(i);
            resetViewState(expandableView);
            if (expandableView instanceof ExpandableNotificationRow) {
                ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) expandableView;
                List<ExpandableNotificationRow> notificationChildren = expandableNotificationRow.getNotificationChildren();
                if (expandableNotificationRow.isSummaryWithChildren() && notificationChildren != null) {
                    for (ExpandableNotificationRow expandableNotificationRow2 : notificationChildren) {
                        resetViewState(expandableNotificationRow2);
                    }
                }
            }
        }
    }
}

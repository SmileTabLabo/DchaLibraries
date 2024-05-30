package com.android.systemui.statusbar.stack;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.ExpandableView;
import java.util.List;
import java.util.WeakHashMap;
/* loaded from: classes.dex */
public class StackScrollState {
    private final ViewGroup mHostView;
    private WeakHashMap<ExpandableView, ExpandableViewState> mStateMap = new WeakHashMap<>();

    public StackScrollState(ViewGroup viewGroup) {
        this.mHostView = viewGroup;
    }

    public ViewGroup getHostView() {
        return this.mHostView;
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

    private void resetViewState(ExpandableView expandableView) {
        ExpandableViewState expandableViewState = this.mStateMap.get(expandableView);
        if (expandableViewState == null) {
            expandableViewState = expandableView.createNewViewState(this);
            this.mStateMap.put(expandableView, expandableViewState);
        }
        expandableViewState.height = expandableView.getIntrinsicHeight();
        expandableViewState.gone = expandableView.getVisibility() == 8;
        expandableViewState.alpha = 1.0f;
        expandableViewState.shadowAlpha = 1.0f;
        expandableViewState.notGoneIndex = -1;
        expandableViewState.xTranslation = expandableView.getTranslationX();
        expandableViewState.hidden = false;
        expandableViewState.scaleX = expandableView.getScaleX();
        expandableViewState.scaleY = expandableView.getScaleY();
        expandableViewState.inShelf = false;
        expandableViewState.headsUpIsVisible = false;
    }

    public ExpandableViewState getViewStateForView(View view) {
        return this.mStateMap.get(view);
    }

    public void removeViewStateForView(View view) {
        this.mStateMap.remove(view);
    }

    public void apply() {
        int childCount = this.mHostView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ExpandableView expandableView = (ExpandableView) this.mHostView.getChildAt(i);
            ExpandableViewState expandableViewState = this.mStateMap.get(expandableView);
            if (expandableViewState == null) {
                Log.wtf("StackScrollStateNoSuchChild", "No child state was found when applying this state to the hostView");
            } else if (!expandableViewState.gone) {
                expandableViewState.applyToView(expandableView);
            }
        }
    }
}

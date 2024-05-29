package com.android.systemui.statusbar.stack;

import android.view.View;
import com.android.systemui.statusbar.ActivatableNotificationView;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import java.util.ArrayList;
/* loaded from: a.zip:com/android/systemui/statusbar/stack/AmbientState.class */
public class AmbientState {
    private ActivatableNotificationView mActivatedChild;
    private boolean mDark;
    private boolean mDimmed;
    private boolean mDismissAllInProgress;
    private HeadsUpManager mHeadsUpManager;
    private boolean mHideSensitive;
    private int mLayoutHeight;
    private float mMaxHeadsUpTranslation;
    private float mOverScrollBottomAmount;
    private float mOverScrollTopAmount;
    private int mScrollY;
    private boolean mShadeExpanded;
    private float mStackTranslation;
    private int mTopPadding;
    private ArrayList<View> mDraggedViews = new ArrayList<>();
    private int mSpeedBumpIndex = -1;

    public ActivatableNotificationView getActivatedChild() {
        return this.mActivatedChild;
    }

    public ArrayList<View> getDraggedViews() {
        return this.mDraggedViews;
    }

    public int getInnerHeight() {
        return this.mLayoutHeight - this.mTopPadding;
    }

    public float getMaxHeadsUpTranslation() {
        return this.mMaxHeadsUpTranslation;
    }

    public float getOverScrollAmount(boolean z) {
        return z ? this.mOverScrollTopAmount : this.mOverScrollBottomAmount;
    }

    public int getScrollY() {
        return this.mScrollY;
    }

    public int getSpeedBumpIndex() {
        return this.mSpeedBumpIndex;
    }

    public float getStackTranslation() {
        return this.mStackTranslation;
    }

    public float getTopPadding() {
        return this.mTopPadding;
    }

    public boolean isDark() {
        return this.mDark;
    }

    public boolean isDimmed() {
        return this.mDimmed;
    }

    public boolean isHideSensitive() {
        return this.mHideSensitive;
    }

    public boolean isShadeExpanded() {
        return this.mShadeExpanded;
    }

    public void onBeginDrag(View view) {
        this.mDraggedViews.add(view);
    }

    public void onDragFinished(View view) {
        this.mDraggedViews.remove(view);
    }

    public void setActivatedChild(ActivatableNotificationView activatableNotificationView) {
        this.mActivatedChild = activatableNotificationView;
    }

    public void setDark(boolean z) {
        this.mDark = z;
    }

    public void setDimmed(boolean z) {
        this.mDimmed = z;
    }

    public void setDismissAllInProgress(boolean z) {
        this.mDismissAllInProgress = z;
    }

    public void setHeadsUpManager(HeadsUpManager headsUpManager) {
        this.mHeadsUpManager = headsUpManager;
    }

    public void setHideSensitive(boolean z) {
        this.mHideSensitive = z;
    }

    public void setLayoutHeight(int i) {
        this.mLayoutHeight = i;
    }

    public void setMaxHeadsUpTranslation(float f) {
        this.mMaxHeadsUpTranslation = f;
    }

    public void setOverScrollAmount(float f, boolean z) {
        if (z) {
            this.mOverScrollTopAmount = f;
        } else {
            this.mOverScrollBottomAmount = f;
        }
    }

    public void setScrollY(int i) {
        this.mScrollY = i;
    }

    public void setShadeExpanded(boolean z) {
        this.mShadeExpanded = z;
    }

    public void setSpeedBumpIndex(int i) {
        this.mSpeedBumpIndex = i;
    }

    public void setStackTranslation(float f) {
        this.mStackTranslation = f;
    }

    public void setTopPadding(int i) {
        this.mTopPadding = i;
    }
}

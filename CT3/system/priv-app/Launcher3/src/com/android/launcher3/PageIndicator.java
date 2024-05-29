package com.android.launcher3;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import java.util.ArrayList;
/* loaded from: a.zip:com/android/launcher3/PageIndicator.class */
public class PageIndicator extends LinearLayout {
    private int mActiveMarkerIndex;
    private LayoutInflater mLayoutInflater;
    private ArrayList<PageIndicatorMarker> mMarkers;
    private int mMaxWindowSize;
    private int[] mWindowRange;

    /* loaded from: a.zip:com/android/launcher3/PageIndicator$PageMarkerResources.class */
    public static class PageMarkerResources {
        int activeId;
        int inactiveId;

        public PageMarkerResources() {
            this.activeId = 2130837526;
            this.inactiveId = 2130837528;
        }

        public PageMarkerResources(int i, int i2) {
            this.activeId = i;
            this.inactiveId = i2;
        }
    }

    public PageIndicator(Context context) {
        this(context, null);
    }

    public PageIndicator(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public PageIndicator(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mWindowRange = new int[2];
        this.mMarkers = new ArrayList<>();
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.PageIndicator, i, 0);
        this.mMaxWindowSize = obtainStyledAttributes.getInteger(0, 15);
        this.mWindowRange[0] = 0;
        this.mWindowRange[1] = 0;
        this.mLayoutInflater = LayoutInflater.from(context);
        obtainStyledAttributes.recycle();
        getLayoutTransition().setDuration(175L);
    }

    private void disableLayoutTransitions() {
        LayoutTransition layoutTransition = getLayoutTransition();
        layoutTransition.disableTransitionType(2);
        layoutTransition.disableTransitionType(3);
        layoutTransition.disableTransitionType(0);
        layoutTransition.disableTransitionType(1);
    }

    private void enableLayoutTransitions() {
        LayoutTransition layoutTransition = getLayoutTransition();
        layoutTransition.enableTransitionType(2);
        layoutTransition.enableTransitionType(3);
        layoutTransition.enableTransitionType(0);
        layoutTransition.enableTransitionType(1);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void addMarker(int i, PageMarkerResources pageMarkerResources, boolean z) {
        int max = Math.max(0, Math.min(i, this.mMarkers.size()));
        PageIndicatorMarker pageIndicatorMarker = (PageIndicatorMarker) this.mLayoutInflater.inflate(2130968599, (ViewGroup) this, false);
        pageIndicatorMarker.setMarkerDrawables(pageMarkerResources.activeId, pageMarkerResources.inactiveId);
        this.mMarkers.add(max, pageIndicatorMarker);
        offsetWindowCenterTo(this.mActiveMarkerIndex, z);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void addMarkers(ArrayList<PageMarkerResources> arrayList, boolean z) {
        for (int i = 0; i < arrayList.size(); i++) {
            addMarker(Integer.MAX_VALUE, arrayList.get(i), z);
        }
    }

    void offsetWindowCenterTo(int i, boolean z) {
        if (i < 0) {
            new Throwable().printStackTrace();
        }
        int min = Math.min(this.mMarkers.size(), this.mMaxWindowSize);
        int i2 = min / 2;
        float f = min / 2.0f;
        int min2 = Math.min(this.mMarkers.size(), this.mMaxWindowSize + Math.max(0, i - i2));
        int min3 = min2 - Math.min(this.mMarkers.size(), min);
        int i3 = (min2 - min3) / 2;
        if (min3 == 0) {
        }
        if (min2 == this.mMarkers.size()) {
        }
        boolean z2 = this.mWindowRange[0] == min3 ? this.mWindowRange[1] != min2 : true;
        if (!z) {
            disableLayoutTransitions();
        }
        for (int childCount = getChildCount() - 1; childCount >= 0; childCount--) {
            View view = (PageIndicatorMarker) getChildAt(childCount);
            int indexOf = this.mMarkers.indexOf(view);
            if (indexOf < min3 || indexOf >= min2) {
                removeView(view);
            }
        }
        for (int i4 = 0; i4 < this.mMarkers.size(); i4++) {
            PageIndicatorMarker pageIndicatorMarker = this.mMarkers.get(i4);
            if (min3 > i4 || i4 >= min2) {
                pageIndicatorMarker.inactivate(true);
            } else {
                if (indexOfChild(pageIndicatorMarker) < 0) {
                    addView(pageIndicatorMarker, i4 - min3);
                }
                if (i4 == i) {
                    pageIndicatorMarker.activate(z2);
                } else {
                    pageIndicatorMarker.inactivate(z2);
                }
            }
        }
        if (!z) {
            enableLayoutTransitions();
        }
        this.mWindowRange[0] = min3;
        this.mWindowRange[1] = min2;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void removeAllMarkers(boolean z) {
        while (this.mMarkers.size() > 0) {
            removeMarker(Integer.MAX_VALUE, z);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void removeMarker(int i, boolean z) {
        if (this.mMarkers.size() > 0) {
            this.mMarkers.remove(Math.max(0, Math.min(this.mMarkers.size() - 1, i)));
            offsetWindowCenterTo(this.mActiveMarkerIndex, z);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setActiveMarker(int i) {
        this.mActiveMarkerIndex = i;
        offsetWindowCenterTo(i, false);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void updateMarker(int i, PageMarkerResources pageMarkerResources) {
        this.mMarkers.get(i).setMarkerDrawables(pageMarkerResources.activeId, pageMarkerResources.inactiveId);
    }
}

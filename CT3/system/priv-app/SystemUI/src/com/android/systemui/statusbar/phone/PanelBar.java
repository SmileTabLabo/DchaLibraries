package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/PanelBar.class */
public abstract class PanelBar extends FrameLayout {
    public static final String TAG = PanelBar.class.getSimpleName();
    PanelView mPanel;
    private int mState;
    private boolean mTracking;

    public PanelBar(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mState = 0;
    }

    public void collapsePanel(boolean z, boolean z2, float f) {
        boolean z3 = false;
        PanelView panelView = this.mPanel;
        if (!z || panelView.isFullyCollapsed()) {
            panelView.resetViews();
            panelView.setExpandedFraction(0.0f);
            panelView.cancelPeek();
        } else {
            panelView.collapse(z2, f);
            z3 = true;
        }
        if (z3 || this.mState == 0) {
            return;
        }
        go(0);
        onPanelCollapsed();
    }

    public void go(int i) {
        this.mState = i;
    }

    public void onClosingFinished() {
    }

    public void onExpandingFinished() {
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    public void onPanelCollapsed() {
    }

    public void onPanelFullyOpened() {
    }

    public void onPanelPeeked() {
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        boolean z = true;
        if (!panelEnabled()) {
            if (motionEvent.getAction() == 0) {
                Log.v(TAG, String.format("onTouch: all panels disabled, ignoring touch at (%d,%d)", Integer.valueOf((int) motionEvent.getX()), Integer.valueOf((int) motionEvent.getY())));
                return false;
            }
            return false;
        }
        if (motionEvent.getAction() == 0) {
            PanelView panelView = this.mPanel;
            if (panelView == null) {
                Log.v(TAG, String.format("onTouch: no panel for touch at (%d,%d)", Integer.valueOf((int) motionEvent.getX()), Integer.valueOf((int) motionEvent.getY())));
                return true;
            } else if (!panelView.isEnabled()) {
                Log.v(TAG, String.format("onTouch: panel (%s) is disabled, ignoring touch at (%d,%d)", panelView, Integer.valueOf((int) motionEvent.getX()), Integer.valueOf((int) motionEvent.getY())));
                return true;
            }
        }
        if (this.mPanel != null) {
            z = this.mPanel.onTouchEvent(motionEvent);
        }
        return z;
    }

    public void onTrackingStarted() {
        this.mTracking = true;
    }

    public void onTrackingStopped(boolean z) {
        this.mTracking = false;
    }

    public boolean panelEnabled() {
        return true;
    }

    public void panelExpansionChanged(float f, boolean z) {
        boolean z2 = true;
        PanelView panelView = this.mPanel;
        panelView.setVisibility(z ? 0 : 4);
        boolean z3 = false;
        if (z) {
            if (this.mState == 0) {
                go(1);
                onPanelPeeked();
            }
            z2 = false;
            z3 = panelView.getExpandedFraction() >= 1.0f;
        }
        if (z3 && !this.mTracking) {
            go(2);
            onPanelFullyOpened();
        } else if (!z2 || this.mTracking || this.mState == 0) {
        } else {
            go(0);
            onPanelCollapsed();
        }
    }

    public abstract void panelScrimMinFractionChanged(float f);

    public void setBouncerShowing(boolean z) {
        int i = z ? 4 : 0;
        setImportantForAccessibility(i);
        if (this.mPanel != null) {
            this.mPanel.setImportantForAccessibility(i);
        }
    }

    public void setPanel(PanelView panelView) {
        this.mPanel = panelView;
        panelView.setBar(this);
    }
}

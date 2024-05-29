package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import com.android.systemui.DejankUtils;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/PhoneStatusBarView.class */
public class PhoneStatusBarView extends PanelBar {
    PhoneStatusBar mBar;
    private final PhoneStatusBarTransitions mBarTransitions;
    private Runnable mHideExpandedRunnable;
    boolean mIsFullyOpenedPanel;
    private float mMinFraction;
    private float mPanelFraction;
    private ScrimController mScrimController;

    public PhoneStatusBarView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mIsFullyOpenedPanel = false;
        this.mHideExpandedRunnable = new Runnable(this) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBarView.1
            final PhoneStatusBarView this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                if (this.this$0.mPanelFraction == 0.0f) {
                    this.this$0.mBar.makeExpandedInvisible();
                }
            }
        };
        this.mBarTransitions = new PhoneStatusBarTransitions(this);
    }

    private void updateScrimFraction() {
        this.mScrimController.setPanelExpansion(Math.max(this.mPanelFraction, this.mMinFraction));
    }

    public BarTransitions getBarTransitions() {
        return this.mBarTransitions;
    }

    @Override // com.android.systemui.statusbar.phone.PanelBar
    public void onClosingFinished() {
        super.onClosingFinished();
        this.mBar.onClosingFinished();
    }

    public void onDensityOrFontScaleChanged() {
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.height = getResources().getDimensionPixelSize(2131690071);
        setLayoutParams(layoutParams);
    }

    @Override // com.android.systemui.statusbar.phone.PanelBar
    public void onExpandingFinished() {
        super.onExpandingFinished();
        this.mScrimController.onExpandingFinished();
    }

    @Override // com.android.systemui.statusbar.phone.PanelBar, android.view.View
    public void onFinishInflate() {
        this.mBarTransitions.init();
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        return !this.mBar.interceptTouchEvent(motionEvent) ? super.onInterceptTouchEvent(motionEvent) : true;
    }

    @Override // com.android.systemui.statusbar.phone.PanelBar
    public void onPanelCollapsed() {
        super.onPanelCollapsed();
        DejankUtils.postAfterTraversal(this.mHideExpandedRunnable);
        this.mIsFullyOpenedPanel = false;
    }

    @Override // com.android.systemui.statusbar.phone.PanelBar
    public void onPanelFullyOpened() {
        super.onPanelFullyOpened();
        if (!this.mIsFullyOpenedPanel) {
            this.mPanel.sendAccessibilityEvent(32);
        }
        this.mIsFullyOpenedPanel = true;
    }

    @Override // com.android.systemui.statusbar.phone.PanelBar
    public void onPanelPeeked() {
        super.onPanelPeeked();
        this.mBar.makeExpandedVisible(false);
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

    @Override // com.android.systemui.statusbar.phone.PanelBar, android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        return !this.mBar.interceptTouchEvent(motionEvent) ? super.onTouchEvent(motionEvent) : true;
    }

    @Override // com.android.systemui.statusbar.phone.PanelBar
    public void onTrackingStarted() {
        super.onTrackingStarted();
        this.mBar.onTrackingStarted();
        this.mScrimController.onTrackingStarted();
        removePendingHideExpandedRunnables();
    }

    @Override // com.android.systemui.statusbar.phone.PanelBar
    public void onTrackingStopped(boolean z) {
        super.onTrackingStopped(z);
        this.mBar.onTrackingStopped(z);
    }

    @Override // com.android.systemui.statusbar.phone.PanelBar
    public boolean panelEnabled() {
        return this.mBar.panelsEnabled();
    }

    @Override // com.android.systemui.statusbar.phone.PanelBar
    public void panelExpansionChanged(float f, boolean z) {
        super.panelExpansionChanged(f, z);
        this.mPanelFraction = f;
        updateScrimFraction();
    }

    @Override // com.android.systemui.statusbar.phone.PanelBar
    public void panelScrimMinFractionChanged(float f) {
        if (this.mMinFraction != f) {
            this.mMinFraction = f;
            if (f != 0.0f) {
                this.mScrimController.animateNextChange();
            }
            updateScrimFraction();
        }
    }

    public void removePendingHideExpandedRunnables() {
        DejankUtils.removeCallbacks(this.mHideExpandedRunnable);
    }

    public void setBar(PhoneStatusBar phoneStatusBar) {
        this.mBar = phoneStatusBar;
    }

    public void setScrimController(ScrimController scrimController) {
        this.mScrimController = scrimController;
    }
}

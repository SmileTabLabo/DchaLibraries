package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewStub;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import com.android.systemui.AutoReinflateContainer;
import com.android.systemui.qs.QSContainer;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/NotificationsQuickSettingsContainer.class */
public class NotificationsQuickSettingsContainer extends FrameLayout implements ViewStub.OnInflateListener, AutoReinflateContainer.InflateListener {
    private int mBottomPadding;
    private boolean mCustomizerAnimating;
    private boolean mInflated;
    private View mKeyguardStatusBar;
    private AutoReinflateContainer mQsContainer;
    private boolean mQsExpanded;
    private View mStackScroller;
    private int mStackScrollerMargin;
    private View mUserSwitcher;

    public NotificationsQuickSettingsContainer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    private void reloadWidth(View view) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
        layoutParams.width = getContext().getResources().getDimensionPixelSize(2131689816);
        view.setLayoutParams(layoutParams);
    }

    private void setBottomMargin(View view, int i) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
        layoutParams.bottomMargin = i;
        view.setLayoutParams(layoutParams);
    }

    @Override // android.view.ViewGroup
    protected boolean drawChild(Canvas canvas, View view, long j) {
        boolean z = this.mInflated && this.mUserSwitcher.getVisibility() == 0;
        boolean z2 = this.mKeyguardStatusBar.getVisibility() == 0;
        boolean z3 = false;
        if (this.mQsExpanded) {
            z3 = !this.mCustomizerAnimating;
        }
        AutoReinflateContainer autoReinflateContainer = z3 ? this.mStackScroller : this.mQsContainer;
        AutoReinflateContainer autoReinflateContainer2 = !z3 ? this.mStackScroller : this.mQsContainer;
        if (view == this.mQsContainer) {
            if (z && z2) {
                autoReinflateContainer2 = this.mUserSwitcher;
            } else if (z2) {
                autoReinflateContainer2 = this.mKeyguardStatusBar;
            } else if (z) {
                autoReinflateContainer2 = this.mUserSwitcher;
            }
            return super.drawChild(canvas, autoReinflateContainer2, j);
        } else if (view != this.mStackScroller) {
            if (view != this.mUserSwitcher) {
                return view == this.mKeyguardStatusBar ? super.drawChild(canvas, autoReinflateContainer, j) : super.drawChild(canvas, view, j);
            }
            if (!z || !z2) {
                autoReinflateContainer2 = autoReinflateContainer;
            }
            return super.drawChild(canvas, autoReinflateContainer2, j);
        } else {
            if (z && z2) {
                autoReinflateContainer = this.mKeyguardStatusBar;
            } else if (z2 || z) {
                autoReinflateContainer = autoReinflateContainer2;
            }
            return super.drawChild(canvas, autoReinflateContainer, j);
        }
    }

    @Override // android.view.View
    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        this.mBottomPadding = windowInsets.getStableInsetBottom();
        setPadding(0, 0, 0, this.mBottomPadding);
        return windowInsets;
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        reloadWidth(this.mQsContainer);
        reloadWidth(this.mStackScroller);
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mQsContainer = (AutoReinflateContainer) findViewById(2131886683);
        this.mQsContainer.addInflateListener(this);
        this.mStackScroller = findViewById(2131886684);
        this.mStackScrollerMargin = ((FrameLayout.LayoutParams) this.mStackScroller.getLayoutParams()).bottomMargin;
        this.mKeyguardStatusBar = findViewById(2131886348);
        ViewStub viewStub = (ViewStub) findViewById(2131886685);
        viewStub.setOnInflateListener(this);
        this.mUserSwitcher = viewStub;
    }

    @Override // android.view.ViewStub.OnInflateListener
    public void onInflate(ViewStub viewStub, View view) {
        if (viewStub == this.mUserSwitcher) {
            this.mUserSwitcher = view;
            this.mInflated = true;
        }
    }

    @Override // com.android.systemui.AutoReinflateContainer.InflateListener
    public void onInflated(View view) {
        ((QSContainer) view).getCustomizer().setContainer(this);
    }

    public void setCustomizerAnimating(boolean z) {
        if (this.mCustomizerAnimating != z) {
            this.mCustomizerAnimating = z;
            invalidate();
        }
    }

    public void setCustomizerShowing(boolean z) {
        if (z) {
            setPadding(0, 0, 0, 0);
            setBottomMargin(this.mStackScroller, 0);
            return;
        }
        setPadding(0, 0, 0, this.mBottomPadding);
        setBottomMargin(this.mStackScroller, this.mStackScrollerMargin);
    }

    public void setQsExpanded(boolean z) {
        if (this.mQsExpanded != z) {
            this.mQsExpanded = z;
            invalidate();
        }
    }
}

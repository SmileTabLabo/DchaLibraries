package com.android.systemui.statusbar.policy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.FrameLayout;
import com.android.systemui.Interpolators;
import com.android.systemui.statusbar.ScrimView;
import com.android.systemui.statusbar.phone.StatusBarWindowView;
import com.android.systemui.statusbar.stack.NotificationStackScrollLayout;
/* loaded from: a.zip:com/android/systemui/statusbar/policy/BrightnessMirrorController.class */
public class BrightnessMirrorController {
    private View mBrightnessMirror;
    private final View mNotificationPanel;
    private final ScrimView mScrimBehind;
    private final NotificationStackScrollLayout mStackScroller;
    private final StatusBarWindowView mStatusBarWindow;
    public long TRANSITION_DURATION_OUT = 150;
    public long TRANSITION_DURATION_IN = 200;
    private final int[] mInt2Cache = new int[2];

    public BrightnessMirrorController(StatusBarWindowView statusBarWindowView) {
        this.mStatusBarWindow = statusBarWindowView;
        this.mScrimBehind = (ScrimView) statusBarWindowView.findViewById(2131886712);
        this.mBrightnessMirror = statusBarWindowView.findViewById(2131886259);
        this.mNotificationPanel = statusBarWindowView.findViewById(2131886681);
        this.mStackScroller = (NotificationStackScrollLayout) statusBarWindowView.findViewById(2131886684);
    }

    private ViewPropertyAnimator inAnimation(ViewPropertyAnimator viewPropertyAnimator) {
        return viewPropertyAnimator.alpha(1.0f).setDuration(this.TRANSITION_DURATION_IN).setInterpolator(Interpolators.ALPHA_IN);
    }

    private ViewPropertyAnimator outAnimation(ViewPropertyAnimator viewPropertyAnimator) {
        return viewPropertyAnimator.alpha(0.0f).setDuration(this.TRANSITION_DURATION_OUT).setInterpolator(Interpolators.ALPHA_OUT).withEndAction(null);
    }

    public View getMirror() {
        return this.mBrightnessMirror;
    }

    public void hideMirror() {
        this.mScrimBehind.animateViewAlpha(1.0f, this.TRANSITION_DURATION_IN, Interpolators.ALPHA_IN);
        inAnimation(this.mNotificationPanel.animate()).withLayer().withEndAction(new Runnable(this) { // from class: com.android.systemui.statusbar.policy.BrightnessMirrorController.1
            final BrightnessMirrorController this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.mBrightnessMirror.setVisibility(4);
                this.this$0.mStackScroller.setFadingOut(false);
            }
        });
    }

    public void onDensityOrFontScaleChanged() {
        int indexOfChild = this.mStatusBarWindow.indexOfChild(this.mBrightnessMirror);
        this.mStatusBarWindow.removeView(this.mBrightnessMirror);
        this.mBrightnessMirror = LayoutInflater.from(this.mBrightnessMirror.getContext()).inflate(2130968605, (ViewGroup) this.mStatusBarWindow, false);
        this.mStatusBarWindow.addView(this.mBrightnessMirror, indexOfChild);
    }

    public void setLocation(View view) {
        view.getLocationInWindow(this.mInt2Cache);
        int i = this.mInt2Cache[0];
        int width = view.getWidth() / 2;
        int i2 = this.mInt2Cache[1];
        int height = view.getHeight() / 2;
        this.mBrightnessMirror.setTranslationX(0.0f);
        this.mBrightnessMirror.setTranslationY(0.0f);
        this.mBrightnessMirror.getLocationInWindow(this.mInt2Cache);
        int i3 = this.mInt2Cache[0];
        int width2 = this.mBrightnessMirror.getWidth() / 2;
        int i4 = this.mInt2Cache[1];
        int height2 = this.mBrightnessMirror.getHeight() / 2;
        this.mBrightnessMirror.setTranslationX((i + width) - (i3 + width2));
        this.mBrightnessMirror.setTranslationY((i2 + height) - (i4 + height2));
    }

    public void showMirror() {
        this.mBrightnessMirror.setVisibility(0);
        this.mStackScroller.setFadingOut(true);
        this.mScrimBehind.animateViewAlpha(0.0f, this.TRANSITION_DURATION_OUT, Interpolators.ALPHA_OUT);
        outAnimation(this.mNotificationPanel.animate()).withLayer();
    }

    public void updateResources() {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.mBrightnessMirror.getLayoutParams();
        layoutParams.width = this.mBrightnessMirror.getResources().getDimensionPixelSize(2131689816);
        layoutParams.gravity = this.mBrightnessMirror.getResources().getInteger(2131755093);
        this.mBrightnessMirror.setLayoutParams(layoutParams);
    }
}

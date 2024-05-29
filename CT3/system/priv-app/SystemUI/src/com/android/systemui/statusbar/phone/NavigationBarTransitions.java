package com.android.systemui.statusbar.phone;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.view.MotionEvent;
import android.view.View;
import com.android.internal.statusbar.IStatusBarService;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/NavigationBarTransitions.class */
public final class NavigationBarTransitions extends BarTransitions {
    private final IStatusBarService mBarService;
    private boolean mLightsOut;
    private final View.OnTouchListener mLightsOutListener;
    private final NavigationBarView mView;

    public NavigationBarTransitions(NavigationBarView navigationBarView) {
        super(navigationBarView, 2130837949);
        this.mLightsOutListener = new View.OnTouchListener(this) { // from class: com.android.systemui.statusbar.phone.NavigationBarTransitions.1
            final NavigationBarTransitions this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == 0) {
                    this.this$0.applyLightsOut(false, false, false);
                    try {
                        this.this$0.mBarService.setSystemUiVisibility(0, 1, "LightsOutListener");
                        return false;
                    } catch (RemoteException e) {
                        return false;
                    }
                }
                return false;
            }
        };
        this.mView = navigationBarView;
        this.mBarService = IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar"));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void applyLightsOut(boolean z, boolean z2, boolean z3) {
        if (z3 || z != this.mLightsOut) {
            this.mLightsOut = z;
            View findViewById = this.mView.getCurrentView().findViewById(2131886266);
            findViewById.animate().cancel();
            float f = z ? 0.5f : 1.0f;
            if (z2) {
                findViewById.animate().alpha(f).setDuration(z ? 750 : 250).start();
            } else {
                findViewById.setAlpha(f);
            }
        }
    }

    private void applyMode(int i, boolean z, boolean z2) {
        applyLightsOut(isLightsOut(i), z, z2);
    }

    public void init() {
        applyModeBackground(-1, getMode(), false);
        applyMode(getMode(), false, true);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.BarTransitions
    public void onTransition(int i, int i2, boolean z) {
        super.onTransition(i, i2, z);
        applyMode(i2, z, false);
    }
}

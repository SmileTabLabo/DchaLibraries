package com.android.systemui.statusbar.phone;

import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.SparseArray;
import android.view.IWallpaperVisibilityListener;
import android.view.IWindowManager;
import android.view.MotionEvent;
import android.view.View;
import com.android.internal.statusbar.IStatusBarService;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.statusbar.phone.LightBarTransitionsController;
/* loaded from: classes.dex */
public final class NavigationBarTransitions extends BarTransitions {
    private final boolean mAllowAutoDimWallpaperNotVisible;
    private boolean mAutoDim;
    private final IStatusBarService mBarService;
    private final LightBarTransitionsController mLightTransitionsController;
    private boolean mLightsOut;
    private final View.OnTouchListener mLightsOutListener;
    private View mNavButtons;
    private final NavigationBarView mView;
    private boolean mWallpaperVisible;

    public NavigationBarTransitions(NavigationBarView navigationBarView) {
        super(navigationBarView, R.drawable.nav_background);
        this.mLightsOutListener = new View.OnTouchListener() { // from class: com.android.systemui.statusbar.phone.NavigationBarTransitions.2
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == 0) {
                    NavigationBarTransitions.this.applyLightsOut(false, false, false);
                    try {
                        NavigationBarTransitions.this.mBarService.setSystemUiVisibility(0, 1, "LightsOutListener");
                    } catch (RemoteException e) {
                    }
                }
                return false;
            }
        };
        this.mView = navigationBarView;
        this.mBarService = IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar"));
        this.mLightTransitionsController = new LightBarTransitionsController(navigationBarView.getContext(), new LightBarTransitionsController.DarkIntensityApplier() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$wMNbjUhGbyej8_fCHrQk9JvpMpc
            @Override // com.android.systemui.statusbar.phone.LightBarTransitionsController.DarkIntensityApplier
            public final void applyDarkIntensity(float f) {
                NavigationBarTransitions.this.applyDarkIntensity(f);
            }
        });
        this.mAllowAutoDimWallpaperNotVisible = navigationBarView.getContext().getResources().getBoolean(R.bool.config_navigation_bar_enable_auto_dim_no_visible_wallpaper);
        try {
            this.mWallpaperVisible = ((IWindowManager) Dependency.get(IWindowManager.class)).registerWallpaperVisibilityListener(new AnonymousClass1(Handler.getMain()), 0);
        } catch (RemoteException e) {
        }
        this.mView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$NavigationBarTransitions$XJcD0ZRW4UO2juvu7uZcSTj_ILk
            @Override // android.view.View.OnLayoutChangeListener
            public final void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                NavigationBarTransitions.lambda$new$0(NavigationBarTransitions.this, view, i, i2, i3, i4, i5, i6, i7, i8);
            }
        });
        View currentView = this.mView.getCurrentView();
        if (currentView != null) {
            this.mNavButtons = currentView.findViewById(R.id.nav_buttons);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.android.systemui.statusbar.phone.NavigationBarTransitions$1  reason: invalid class name */
    /* loaded from: classes.dex */
    public class AnonymousClass1 extends IWallpaperVisibilityListener.Stub {
        final /* synthetic */ Handler val$handler;

        AnonymousClass1(Handler handler) {
            this.val$handler = handler;
        }

        public void onWallpaperVisibilityChanged(boolean z, int i) throws RemoteException {
            NavigationBarTransitions.this.mWallpaperVisible = z;
            this.val$handler.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$NavigationBarTransitions$1$5foY_Yygo1gW25-mVBRpPSQRb_g
                @Override // java.lang.Runnable
                public final void run() {
                    NavigationBarTransitions.this.applyLightsOut(true, false);
                }
            });
        }
    }

    public static /* synthetic */ void lambda$new$0(NavigationBarTransitions navigationBarTransitions, View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        View currentView = navigationBarTransitions.mView.getCurrentView();
        if (currentView != null) {
            navigationBarTransitions.mNavButtons = currentView.findViewById(R.id.nav_buttons);
            navigationBarTransitions.applyLightsOut(false, true);
        }
    }

    public void init() {
        applyModeBackground(-1, getMode(), false);
        applyLightsOut(false, true);
    }

    @Override // com.android.systemui.statusbar.phone.BarTransitions
    public void setAutoDim(boolean z) {
        if (this.mAutoDim == z) {
            return;
        }
        this.mAutoDim = z;
        applyLightsOut(true, false);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.BarTransitions
    public boolean isLightsOut(int i) {
        return super.isLightsOut(i) || (this.mAllowAutoDimWallpaperNotVisible && this.mAutoDim && !this.mWallpaperVisible && i != 5);
    }

    public LightBarTransitionsController getLightTransitionsController() {
        return this.mLightTransitionsController;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.BarTransitions
    public void onTransition(int i, int i2, boolean z) {
        super.onTransition(i, i2, z);
        applyLightsOut(z, false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void applyLightsOut(boolean z, boolean z2) {
        applyLightsOut(isLightsOut(getMode()), z, z2);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void applyLightsOut(boolean z, boolean z2, boolean z3) {
        if (z3 || z != this.mLightsOut) {
            this.mLightsOut = z;
            if (this.mNavButtons == null) {
                return;
            }
            this.mNavButtons.animate().cancel();
            float currentDarkIntensity = z ? 0.6f + (this.mLightTransitionsController.getCurrentDarkIntensity() / 10.0f) : 1.0f;
            if (!z2) {
                this.mNavButtons.setAlpha(currentDarkIntensity);
            } else {
                this.mNavButtons.animate().alpha(currentDarkIntensity).setDuration(z ? 1500 : 250).start();
            }
        }
    }

    public void reapplyDarkIntensity() {
        applyDarkIntensity(this.mLightTransitionsController.getCurrentDarkIntensity());
    }

    public void applyDarkIntensity(float f) {
        SparseArray<ButtonDispatcher> buttonDispatchers = this.mView.getButtonDispatchers();
        for (int size = buttonDispatchers.size() - 1; size >= 0; size--) {
            buttonDispatchers.valueAt(size).setDarkIntensity(f);
        }
        if (this.mAutoDim) {
            applyLightsOut(false, true);
        }
        this.mView.onDarkIntensityChange(f);
    }
}

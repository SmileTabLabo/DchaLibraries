package com.android.systemui.statusbar.car;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import com.android.keyguard.AlphaOptimizedImageButton;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.statusbar.phone.StatusBarIconController;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class CarNavigationBarView extends LinearLayout {
    private CarStatusBar mCarStatusBar;
    private Context mContext;
    private View mLockScreenButtons;
    private View mNavButtons;
    private AlphaOptimizedImageButton mNotificationsButton;

    public CarNavigationBarView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mContext = context;
    }

    @Override // android.view.View
    public void onFinishInflate() {
        this.mNavButtons = findViewById(R.id.nav_buttons);
        this.mLockScreenButtons = findViewById(R.id.lock_screen_nav_buttons);
        this.mNotificationsButton = (AlphaOptimizedImageButton) findViewById(R.id.notifications);
        if (this.mNotificationsButton != null) {
            this.mNotificationsButton.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.statusbar.car.-$$Lambda$Y4nI6w7N50JXOiy6kyuMQKNxBt8
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    CarNavigationBarView.this.onNotificationsClick(view);
                }
            });
        }
        View findViewById = findViewById(R.id.statusIcons);
        if (findViewById != null) {
            StatusBarIconController.DarkIconManager darkIconManager = new StatusBarIconController.DarkIconManager((LinearLayout) findViewById.findViewById(R.id.statusIcons));
            darkIconManager.setShouldLog(true);
            ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).addIconGroup(darkIconManager);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setStatusBar(CarStatusBar carStatusBar) {
        this.mCarStatusBar = carStatusBar;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onNotificationsClick(View view) {
        this.mCarStatusBar.togglePanel();
    }

    public void showKeyguardButtons() {
        if (this.mLockScreenButtons == null) {
            return;
        }
        this.mLockScreenButtons.setVisibility(0);
        this.mNavButtons.setVisibility(8);
    }

    public void hideKeyguardButtons() {
        if (this.mLockScreenButtons == null) {
            return;
        }
        this.mNavButtons.setVisibility(0);
        this.mLockScreenButtons.setVisibility(8);
    }
}

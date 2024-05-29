package com.android.systemui.statusbar.car;

import android.view.View;
import android.view.ViewStub;
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import com.android.systemui.statusbar.policy.UserSwitcherController;
/* loaded from: a.zip:com/android/systemui/statusbar/car/FullscreenUserSwitcher.class */
public class FullscreenUserSwitcher {
    private View mContainer;
    private UserGridView mUserGridView;
    private UserSwitcherController mUserSwitcherController;

    public FullscreenUserSwitcher(PhoneStatusBar phoneStatusBar, UserSwitcherController userSwitcherController, ViewStub viewStub) {
        this.mUserSwitcherController = userSwitcherController;
        this.mContainer = viewStub.inflate();
        this.mUserGridView = (UserGridView) this.mContainer.findViewById(2131886265);
        this.mUserGridView.init(phoneStatusBar, this.mUserSwitcherController);
    }

    public void hide() {
        this.mContainer.setVisibility(8);
    }

    public void onUserSwitched(int i) {
        this.mUserGridView.onUserSwitched(i);
    }

    public void show() {
        this.mContainer.setVisibility(0);
    }
}

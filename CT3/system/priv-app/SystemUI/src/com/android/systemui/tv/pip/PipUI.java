package com.android.systemui.tv.pip;

import android.content.pm.PackageManager;
import android.content.res.Configuration;
import com.android.systemui.SystemUI;
/* loaded from: a.zip:com/android/systemui/tv/pip/PipUI.class */
public class PipUI extends SystemUI {
    private boolean mSupportPip;

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.SystemUI
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        if (this.mSupportPip) {
            PipManager.getInstance().onConfigurationChanged();
        }
    }

    @Override // com.android.systemui.SystemUI
    public void start() {
        PackageManager packageManager = this.mContext.getPackageManager();
        this.mSupportPip = packageManager.hasSystemFeature("android.software.picture_in_picture") ? packageManager.hasSystemFeature("android.software.leanback") : false;
        if (this.mSupportPip) {
            PipManager.getInstance().initialize(this.mContext);
        }
    }
}

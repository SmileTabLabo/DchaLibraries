package com.android.settings.applications;

import android.content.Intent;
import com.android.settings.SettingsActivity;
/* loaded from: classes.dex */
public class InstalledAppOpenByDefaultPage extends SettingsActivity {
    @Override // com.android.settings.SettingsActivity, android.app.Activity
    public Intent getIntent() {
        Intent intent = new Intent(super.getIntent());
        intent.putExtra(":settings:show_fragment", AppLaunchSettings.class.getName());
        return intent;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.SettingsActivity
    public boolean isValidFragment(String str) {
        return AppLaunchSettings.class.getName().equals(str);
    }
}

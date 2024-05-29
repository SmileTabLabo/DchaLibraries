package com.android.settings.applications;

import android.content.Intent;
import com.android.settings.SettingsActivity;
/* loaded from: classes.dex */
public class InstalledAppDetailsTop extends SettingsActivity {
    @Override // com.android.settings.SettingsActivity, android.app.Activity
    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(":settings:show_fragment", InstalledAppDetails.class.getName());
        return modIntent;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.SettingsActivity
    public boolean isValidFragment(String fragmentName) {
        return InstalledAppDetails.class.getName().equals(fragmentName);
    }
}

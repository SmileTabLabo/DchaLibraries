package com.android.settings;

import android.util.Log;
/* loaded from: classes.dex */
public class SubSettings extends SettingsActivity {
    @Override // android.app.Activity
    public boolean onNavigateUp() {
        finish();
        return true;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.SettingsActivity
    public boolean isValidFragment(String fragmentName) {
        Log.d("SubSettings", "Launching fragment " + fragmentName);
        return true;
    }
}

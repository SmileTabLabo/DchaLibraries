package com.android.settings.inputmethod;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import com.android.settings.SettingsActivity;
/* loaded from: classes.dex */
public class InputMethodAndSubtypeEnablerActivity extends SettingsActivity {
    private static final String FRAGMENT_NAME = InputMethodAndSubtypeEnabler.class.getName();

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.SettingsActivity, com.android.settingslib.drawer.SettingsDrawerActivity, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    @Override // com.android.settingslib.drawer.SettingsDrawerActivity, android.app.Activity
    public boolean onNavigateUp() {
        finish();
        return true;
    }

    @Override // com.android.settings.SettingsActivity, android.app.Activity
    public Intent getIntent() {
        Intent intent = new Intent(super.getIntent());
        if (!intent.hasExtra(":settings:show_fragment")) {
            intent.putExtra(":settings:show_fragment", FRAGMENT_NAME);
        }
        return intent;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.SettingsActivity
    public boolean isValidFragment(String str) {
        return FRAGMENT_NAME.equals(str);
    }
}

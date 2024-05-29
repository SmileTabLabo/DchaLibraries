package com.android.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import com.android.settings.EncryptionInterstitial;
/* loaded from: classes.dex */
public class SetupEncryptionInterstitial extends EncryptionInterstitial {

    /* loaded from: classes.dex */
    public static class SetupEncryptionInterstitialFragment extends EncryptionInterstitial.EncryptionInterstitialFragment {
    }

    public static Intent createStartIntent(Context context, int i, boolean z, Intent intent) {
        Intent createStartIntent = EncryptionInterstitial.createStartIntent(context, i, z, intent);
        createStartIntent.setClass(context, SetupEncryptionInterstitial.class);
        createStartIntent.putExtra("extra_prefs_show_button_bar", false).putExtra(":settings:show_fragment_title_resid", -1);
        return createStartIntent;
    }

    @Override // com.android.settings.EncryptionInterstitial, com.android.settings.SettingsActivity, android.app.Activity
    public Intent getIntent() {
        Intent intent = new Intent(super.getIntent());
        intent.putExtra(":settings:show_fragment", SetupEncryptionInterstitialFragment.class.getName());
        return intent;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.EncryptionInterstitial, com.android.settings.SettingsActivity
    public boolean isValidFragment(String str) {
        return SetupEncryptionInterstitialFragment.class.getName().equals(str);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.EncryptionInterstitial, com.android.settings.SettingsActivity, com.android.settingslib.drawer.SettingsDrawerActivity, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        ((LinearLayout) findViewById(R.id.content_parent)).setFitsSystemWindows(false);
    }
}

package com.android.browser;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;
import com.android.browser.preferences.AccessibilityPreferencesFragment;
import com.android.browser.preferences.AdvancedPreferencesFragment;
import com.android.browser.preferences.BandwidthPreferencesFragment;
import com.android.browser.preferences.DebugPreferencesFragment;
import com.android.browser.preferences.GeneralPreferencesFragment;
import com.android.browser.preferences.LabPreferencesFragment;
import com.android.browser.preferences.PrivacySecurityPreferencesFragment;
import com.android.browser.preferences.SearchEngineSettings;
import com.android.browser.preferences.WebsiteSettingsFragment;
import java.util.List;
/* loaded from: b.zip:com/android/browser/BrowserPreferencesPage.class */
public class BrowserPreferencesPage extends PreferenceActivity {
    private List<PreferenceActivity.Header> mHeaders;

    @Override // android.preference.PreferenceActivity
    protected boolean isValidFragment(String str) {
        return (AccessibilityPreferencesFragment.class.getName().equals(str) || AdvancedPreferencesFragment.class.getName().equals(str) || BandwidthPreferencesFragment.class.getName().equals(str) || DebugPreferencesFragment.class.getName().equals(str) || GeneralPreferencesFragment.class.getName().equals(str) || LabPreferencesFragment.class.getName().equals(str) || PrivacySecurityPreferencesFragment.class.getName().equals(str) || WebsiteSettingsFragment.class.getName().equals(str) || SearchEngineSettings.class.getName().equals(str)) ? true : "com.android.browser.search.SearchEnginePreference".equals(str);
    }

    @Override // android.preference.PreferenceActivity
    public void onBuildHeaders(List<PreferenceActivity.Header> list) {
        loadHeadersFromResource(2131099658, list);
        if (BrowserSettings.getInstance().isDebugEnabled()) {
            PreferenceActivity.Header header = new PreferenceActivity.Header();
            header.title = getText(2131493157);
            header.fragment = DebugPreferencesFragment.class.getName();
            list.add(header);
        }
        this.mHeaders = list;
    }

    @Override // android.preference.PreferenceActivity
    public Intent onBuildStartFragmentIntent(String str, Bundle bundle, int i, int i2) {
        Intent onBuildStartFragmentIntent = super.onBuildStartFragmentIntent(str, bundle, i, i2);
        onBuildStartFragmentIntent.putExtra("currentPage", getIntent().getStringExtra("currentPage"));
        return onBuildStartFragmentIntent;
    }

    @Override // android.preference.PreferenceActivity, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(4, 4);
        }
    }

    @Override // android.preference.PreferenceActivity
    public PreferenceActivity.Header onGetInitialHeader() {
        if ("android.intent.action.MANAGE_NETWORK_USAGE".equals(getIntent().getAction())) {
            String name = BandwidthPreferencesFragment.class.getName();
            for (PreferenceActivity.Header header : this.mHeaders) {
                if (name.equals(header.fragment)) {
                    return header;
                }
            }
        }
        return super.onGetInitialHeader();
    }

    @Override // android.preference.PreferenceActivity, android.app.Activity
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case 16908332:
                if (getFragmentManager().getBackStackEntryCount() > 0) {
                    getFragmentManager().popBackStack();
                    return true;
                }
                finish();
                return true;
            default:
                return false;
        }
    }

    @Override // android.app.Activity, android.view.Window.Callback
    public boolean onSearchRequested() {
        return false;
    }
}

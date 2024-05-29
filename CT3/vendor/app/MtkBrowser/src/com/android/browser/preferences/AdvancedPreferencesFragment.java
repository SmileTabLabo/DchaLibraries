package com.android.browser.preferences;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.webkit.GeolocationPermissions;
import android.webkit.ValueCallback;
import android.webkit.WebStorage;
import com.android.browser.BrowserActivity;
import com.android.browser.BrowserSettings;
import com.android.browser.Extensions;
import com.mediatek.browser.ext.IBrowserFeatureIndexExt;
import com.mediatek.browser.ext.IBrowserMiscExt;
import com.mediatek.browser.ext.IBrowserSettingExt;
import java.util.Map;
import java.util.Set;
/* loaded from: b.zip:com/android/browser/preferences/AdvancedPreferencesFragment.class */
public class AdvancedPreferencesFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    private IBrowserSettingExt mBrowserSettingExt = null;
    private IBrowserMiscExt mBrowserMiscExt = null;

    @Override // android.preference.PreferenceFragment, android.app.Fragment
    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        this.mBrowserMiscExt = Extensions.getMiscPlugin(getActivity());
        this.mBrowserMiscExt.onActivityResult(i, i2, intent, this);
    }

    @Override // android.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        addPreferencesFromResource(2131099649);
        ((PreferenceScreen) findPreference("search_engine")).setFragment(SearchEngineSettings.class.getName());
        ((PreferenceScreen) findPreference("website_settings")).setFragment(WebsiteSettingsFragment.class.getName());
        Preference findPreference = findPreference("default_text_encoding");
        this.mBrowserSettingExt = Extensions.getSettingPlugin(getActivity());
        this.mBrowserSettingExt.setTextEncodingChoices((ListPreference) findPreference);
        String string = getPreferenceScreen().getSharedPreferences().getString("default_text_encoding", "");
        String str = string;
        if (string != null) {
            str = string;
            if (string.length() != 0) {
                str = string;
                if (string.equals("auto-detector")) {
                    str = getString(2131492864);
                }
            }
        }
        findPreference.setSummary(str);
        findPreference.setOnPreferenceChangeListener(this);
        findPreference("reset_default_preferences").setOnPreferenceChangeListener(this);
        findPreference("search_engine").setOnPreferenceChangeListener(this);
        Preference findPreference2 = findPreference("plugin_state");
        findPreference2.setOnPreferenceChangeListener(this);
        updateListPreferenceSummary((ListPreference) findPreference2);
        getPreferenceScreen().removePreference(findPreference2);
        this.mBrowserSettingExt.customizePreference(IBrowserFeatureIndexExt.CUSTOM_PREFERENCE_ADVANCED, getPreferenceScreen(), this, BrowserSettings.getInstance().getPreferences(), this);
        ((CheckBoxPreference) findPreference("load_page")).setChecked(PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("load_page", true));
    }

    @Override // android.preference.Preference.OnPreferenceChangeListener
    public boolean onPreferenceChange(Preference preference, Object obj) {
        if (getActivity() == null) {
            Log.w("PageContentPreferencesFragment", "onPreferenceChange called from detached fragment!");
            return false;
        } else if (preference.getKey().equals("default_text_encoding")) {
            String obj2 = obj.toString();
            String str = obj2;
            if (obj2 != null) {
                str = obj2;
                if (obj2.length() != 0) {
                    str = obj2;
                    if (obj2.equals("auto-detector")) {
                        str = getString(2131492864);
                    }
                }
            }
            preference.setSummary(str);
            return true;
        } else {
            if (preference.getKey().equals("reset_default_preferences")) {
                if (((Boolean) obj).booleanValue()) {
                    startActivity(new Intent("--restart--", null, getActivity(), BrowserActivity.class));
                    return true;
                }
            } else if (preference.getKey().equals("plugin_state") || preference.getKey().equals("search_engine")) {
                ListPreference listPreference = (ListPreference) preference;
                listPreference.setValue((String) obj);
                updateListPreferenceSummary(listPreference);
                return false;
            }
            this.mBrowserSettingExt = Extensions.getSettingPlugin(getActivity());
            return this.mBrowserSettingExt.updatePreferenceItem(preference, obj);
        }
    }

    @Override // android.app.Fragment
    public void onResume() {
        super.onResume();
        PreferenceScreen preferenceScreen = (PreferenceScreen) findPreference("website_settings");
        preferenceScreen.setEnabled(false);
        WebStorage.getInstance().getOrigins(new ValueCallback<Map>(this, preferenceScreen) { // from class: com.android.browser.preferences.AdvancedPreferencesFragment.1
            final AdvancedPreferencesFragment this$0;
            final PreferenceScreen val$websiteSettings;

            {
                this.this$0 = this;
                this.val$websiteSettings = preferenceScreen;
            }

            @Override // android.webkit.ValueCallback
            public void onReceiveValue(Map map) {
                if (map == null || map.isEmpty()) {
                    return;
                }
                this.val$websiteSettings.setEnabled(true);
            }
        });
        GeolocationPermissions.getInstance().getOrigins(new ValueCallback<Set<String>>(this, preferenceScreen) { // from class: com.android.browser.preferences.AdvancedPreferencesFragment.2
            final AdvancedPreferencesFragment this$0;
            final PreferenceScreen val$websiteSettings;

            {
                this.this$0 = this;
                this.val$websiteSettings = preferenceScreen;
            }

            @Override // android.webkit.ValueCallback
            public void onReceiveValue(Set<String> set) {
                if (set == null || set.isEmpty()) {
                    return;
                }
                this.val$websiteSettings.setEnabled(true);
            }
        });
    }

    void updateListPreferenceSummary(ListPreference listPreference) {
        listPreference.setSummary(listPreference.getEntry());
    }
}

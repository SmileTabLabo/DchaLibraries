package com.android.browser.preferences;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import com.android.browser.BrowserSettings;
import com.mediatek.common.search.SearchEngine;
import com.mediatek.search.SearchEngineManager;
import java.util.ArrayList;
import java.util.List;
/* loaded from: b.zip:com/android/browser/preferences/SearchEngineSettings.class */
public class SearchEngineSettings extends PreferenceFragment implements Preference.OnPreferenceClickListener {
    private PreferenceActivity mActivity;
    private CheckBoxPreference mCheckBoxPref;
    private String[] mEntries;
    private String[] mEntryFavicon;
    private String[] mEntryValues;
    private SharedPreferences mPrefs;
    private List<RadioPreference> mRadioPrefs;

    private void broadcastSearchEngineChangedExternal(Context context) {
        Intent intent = new Intent("com.android.quicksearchbox.SEARCH_ENGINE_CHANGED");
        intent.setPackage("com.android.quicksearchbox");
        intent.putExtra("search_engine", BrowserSettings.getInstance().getSearchEngineName());
        Log.i("@M_browser/SearchEngineSettings", "Broadcasting: " + intent);
        context.sendBroadcast(intent);
    }

    private PreferenceScreen createPreferenceHierarchy() {
        PreferenceScreen createPreferenceScreen = getPreferenceManager().createPreferenceScreen(this.mActivity);
        PreferenceCategory preferenceCategory = new PreferenceCategory(this.mActivity);
        preferenceCategory.setTitle(2131492889);
        createPreferenceScreen.addPreference(preferenceCategory);
        this.mCheckBoxPref = new CheckBoxPreference(this.mActivity);
        this.mCheckBoxPref.setKey("toggle_consistency");
        this.mCheckBoxPref.setTitle(2131492890);
        this.mCheckBoxPref.setSummaryOn(2131492891);
        this.mCheckBoxPref.setSummaryOff(2131492891);
        preferenceCategory.addPreference(this.mCheckBoxPref);
        this.mCheckBoxPref.setChecked(this.mPrefs.getBoolean("syc_search_engine", true));
        PreferenceCategory preferenceCategory2 = new PreferenceCategory(this.mActivity);
        preferenceCategory2.setTitle(2131493064);
        createPreferenceScreen.addPreference(preferenceCategory2);
        for (int i = 0; i < this.mEntries.length; i++) {
            RadioPreference radioPreference = new RadioPreference(this.mActivity);
            radioPreference.setWidgetLayoutResource(2130968618);
            radioPreference.setTitle(this.mEntries[i]);
            radioPreference.setOrder(i);
            radioPreference.setOnPreferenceClickListener(this);
            preferenceCategory2.addPreference(radioPreference);
            this.mRadioPrefs.add(radioPreference);
        }
        return createPreferenceScreen;
    }

    @Override // android.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mRadioPrefs = new ArrayList();
        this.mActivity = (PreferenceActivity) getActivity();
        this.mPrefs = PreferenceManager.getDefaultSharedPreferences(this.mActivity);
        int i = -1;
        String searchEngineName = BrowserSettings.getInstance().getSearchEngineName();
        if (searchEngineName != null) {
            List availables = ((SearchEngineManager) this.mActivity.getSystemService("search_engine")).getAvailables();
            int size = availables.size();
            this.mEntryValues = new String[size];
            this.mEntries = new String[size];
            this.mEntryFavicon = new String[size];
            for (int i2 = 0; i2 < size; i2++) {
                this.mEntryValues[i2] = ((SearchEngine) availables.get(i2)).getName();
                this.mEntries[i2] = ((SearchEngine) availables.get(i2)).getLabel();
                this.mEntryFavicon[i2] = ((SearchEngine) availables.get(i2)).getFaviconUri();
                if (this.mEntryValues[i2].equals(searchEngineName)) {
                    i = i2;
                }
            }
            setPreferenceScreen(createPreferenceHierarchy());
            this.mRadioPrefs.get(i).setChecked(true);
        }
        if (this.mCheckBoxPref.isChecked()) {
            broadcastSearchEngineChangedExternal(this.mActivity);
        }
    }

    @Override // android.app.Fragment
    public void onPause() {
        super.onPause();
        SharedPreferences.Editor edit = this.mPrefs.edit();
        edit.putBoolean("syc_search_engine", this.mCheckBoxPref.isChecked());
        edit.commit();
        if (this.mCheckBoxPref.isChecked()) {
            broadcastSearchEngineChangedExternal(this.mActivity);
        }
    }

    @Override // android.preference.Preference.OnPreferenceClickListener
    public boolean onPreferenceClick(Preference preference) {
        for (RadioPreference radioPreference : this.mRadioPrefs) {
            radioPreference.setChecked(false);
        }
        ((RadioPreference) preference).setChecked(true);
        SharedPreferences.Editor edit = this.mPrefs.edit();
        edit.putString("search_engine", this.mEntryValues[preference.getOrder()]);
        edit.putString("search_engine_favicon", this.mEntryFavicon[preference.getOrder()]);
        edit.commit();
        return true;
    }
}

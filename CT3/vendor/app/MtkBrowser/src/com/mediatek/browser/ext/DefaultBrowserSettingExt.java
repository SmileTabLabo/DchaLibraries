package com.mediatek.browser.ext;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.webkit.WebSettings;
import com.mediatek.common.search.SearchEngine;
import com.mediatek.search.SearchEngineManager;
import com.mediatek.storage.StorageManagerEx;
/* loaded from: b.zip:com/mediatek/browser/ext/DefaultBrowserSettingExt.class */
public class DefaultBrowserSettingExt implements IBrowserSettingExt {
    private static final String DEFAULT_DOWNLOAD_DIRECTORY = "/storage/sdcard0/MyFavorite";
    private static final String DEFAULT_MY_FAVORITE_FOLDER = "/MyFavorite";
    private static final String DEFAULT_SEARCH_ENGIN = "google";
    private static final String PREF_SEARCH_ENGINE = "search_engine";
    private static final String TAG = "DefaultBrowserSettingsExt";

    @Override // com.mediatek.browser.ext.IBrowserSettingExt
    public void customizePreference(int i, PreferenceScreen preferenceScreen, Preference.OnPreferenceChangeListener onPreferenceChangeListener, SharedPreferences sharedPreferences, PreferenceFragment preferenceFragment) {
        Log.i("@M_DefaultBrowserSettingsExt", "Enter: customizePreference --default implement");
    }

    @Override // com.mediatek.browser.ext.IBrowserSettingExt
    public String getCustomerHomepage() {
        Log.i("@M_DefaultBrowserSettingsExt", "Enter: getCustomerHomepage --default implement");
        return null;
    }

    @Override // com.mediatek.browser.ext.IBrowserSettingExt
    public String getDefaultDownloadFolder() {
        Log.i("@M_DefaultBrowserSettingsExt", "Enter: getDefaultDownloadFolder() --default implement");
        String str = DEFAULT_DOWNLOAD_DIRECTORY;
        String defaultPath = StorageManagerEx.getDefaultPath();
        if (defaultPath != null) {
            str = defaultPath + DEFAULT_MY_FAVORITE_FOLDER;
        }
        Log.v("@M_DefaultBrowserSettingsExt", "device default storage is: " + defaultPath + " defaultPath is: " + str);
        return str;
    }

    @Override // com.mediatek.browser.ext.IBrowserSettingExt
    public String getOperatorUA(String str) {
        Log.i("@M_DefaultBrowserSettingsExt", "Enter: getOperatorUA --default implement");
        return null;
    }

    @Override // com.mediatek.browser.ext.IBrowserSettingExt
    public String getSearchEngine(SharedPreferences sharedPreferences, Context context) {
        Log.i("@M_DefaultBrowserSettingsExt", "Enter: getSearchEngine --default implement");
        SearchEngine searchEngine = ((SearchEngineManager) context.getSystemService(PREF_SEARCH_ENGINE)).getDefault();
        String str = DEFAULT_SEARCH_ENGIN;
        if (searchEngine != null) {
            str = searchEngine.getName();
        }
        return sharedPreferences.getString(PREF_SEARCH_ENGINE, str);
    }

    @Override // com.mediatek.browser.ext.IBrowserSettingExt
    public void setOnlyLandscape(SharedPreferences sharedPreferences, Activity activity) {
        Log.i("@M_DefaultBrowserSettingsExt", "Enter: setOnlyLandscape --default implement");
        if (activity != null) {
            activity.setRequestedOrientation(-1);
        }
    }

    @Override // com.mediatek.browser.ext.IBrowserSettingExt
    public void setStandardFontFamily(WebSettings webSettings, SharedPreferences sharedPreferences) {
        Log.i("@M_DefaultBrowserSettingsExt", "Enter: setStandardFontFamily --default implement");
    }

    @Override // com.mediatek.browser.ext.IBrowserSettingExt
    public void setTextEncodingChoices(ListPreference listPreference) {
        Log.i("@M_DefaultBrowserSettingsExt", "Enter: setTextEncodingChoices --default implement");
    }

    @Override // com.mediatek.browser.ext.IBrowserSettingExt
    public boolean updatePreferenceItem(Preference preference, Object obj) {
        Log.i("@M_DefaultBrowserSettingsExt", "Enter: updatePreferenceItem --default implement");
        return false;
    }
}

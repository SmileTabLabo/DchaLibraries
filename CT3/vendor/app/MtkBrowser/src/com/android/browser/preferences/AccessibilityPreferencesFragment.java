package com.android.browser.preferences;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.webkit.WebView;
import com.android.browser.BrowserSettings;
import com.android.browser.Extensions;
import com.mediatek.browser.ext.IBrowserFeatureIndexExt;
import com.mediatek.browser.ext.IBrowserSettingExt;
import java.text.NumberFormat;
/* loaded from: b.zip:com/android/browser/preferences/AccessibilityPreferencesFragment.class */
public class AccessibilityPreferencesFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    private IBrowserSettingExt mBrowserSettingExt = null;
    WebView mControlWebView;
    NumberFormat mFormat;

    @Override // android.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mControlWebView = new WebView(getActivity());
        addPreferencesFromResource(2131099648);
        BrowserSettings browserSettings = BrowserSettings.getInstance();
        this.mFormat = NumberFormat.getPercentInstance();
        Preference findPreference = findPreference("min_font_size");
        findPreference.setOnPreferenceChangeListener(this);
        updateMinFontSummary(findPreference, browserSettings.getMinimumFontSize());
        Preference findPreference2 = findPreference("text_zoom");
        findPreference2.setOnPreferenceChangeListener(this);
        updateTextZoomSummary(findPreference2, browserSettings.getTextZoom());
        Preference findPreference3 = findPreference("double_tap_zoom");
        findPreference3.setOnPreferenceChangeListener(this);
        updateDoubleTapZoomSummary(findPreference3, browserSettings.getDoubleTapZoom());
        this.mBrowserSettingExt = Extensions.getSettingPlugin(getActivity());
        this.mBrowserSettingExt.customizePreference(IBrowserFeatureIndexExt.CUSTOM_PREFERENCE_ACCESSIBILITY, getPreferenceScreen(), this, browserSettings.getPreferences(), this);
    }

    @Override // android.preference.PreferenceFragment, android.app.Fragment
    public void onDestroy() {
        super.onDestroy();
        this.mControlWebView.destroy();
        this.mControlWebView = null;
    }

    @Override // android.app.Fragment
    public void onPause() {
        super.onPause();
        this.mControlWebView.pauseTimers();
    }

    @Override // android.preference.Preference.OnPreferenceChangeListener
    public boolean onPreferenceChange(Preference preference, Object obj) {
        if (getActivity() == null) {
            return false;
        }
        if ("min_font_size".equals(preference.getKey())) {
            updateMinFontSummary(preference, BrowserSettings.getAdjustedMinimumFontSize(((Integer) obj).intValue()));
        }
        if ("text_zoom".equals(preference.getKey())) {
            updateTextZoomSummary(preference, BrowserSettings.getInstance().getAdjustedTextZoom(((Integer) obj).intValue()));
        }
        if ("double_tap_zoom".equals(preference.getKey())) {
            updateDoubleTapZoomSummary(preference, BrowserSettings.getInstance().getAdjustedDoubleTapZoom(((Integer) obj).intValue()));
        }
        this.mBrowserSettingExt = Extensions.getSettingPlugin(getActivity());
        this.mBrowserSettingExt.updatePreferenceItem(preference, obj);
        return true;
    }

    @Override // android.app.Fragment
    public void onResume() {
        super.onResume();
        this.mControlWebView.resumeTimers();
    }

    void updateDoubleTapZoomSummary(Preference preference, int i) {
        preference.setSummary(this.mFormat.format(i / 100.0d));
    }

    void updateMinFontSummary(Preference preference, int i) {
        preference.setSummary(getActivity().getString(2131493137, Integer.valueOf(i)));
    }

    void updateTextZoomSummary(Preference preference, int i) {
        preference.setSummary(this.mFormat.format(i / 100.0d));
    }
}

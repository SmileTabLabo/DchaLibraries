package com.android.settings;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;
import java.util.ArrayList;
import java.util.List;
/* loaded from: classes.dex */
public class WallpaperTypeSettings extends SettingsPreferenceFragment implements Indexable {
    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() { // from class: com.android.settings.WallpaperTypeSettings.1
        @Override // com.android.settings.search.BaseSearchIndexProvider, com.android.settings.search.Indexable.SearchIndexProvider
        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            List<SearchIndexableRaw> result = new ArrayList<>();
            Intent intent = new Intent("android.intent.action.SET_WALLPAPER");
            PackageManager pm = context.getPackageManager();
            List<ResolveInfo> rList = pm.queryIntentActivities(intent, 65536);
            for (ResolveInfo info : rList) {
                CharSequence label = info.loadLabel(pm);
                if (label == null) {
                    label = info.activityInfo.packageName;
                }
                SearchIndexableRaw data = new SearchIndexableRaw(context);
                data.title = label.toString();
                data.screenTitle = context.getResources().getString(R.string.wallpaper_settings_fragment_title);
                data.intentAction = "android.intent.action.SET_WALLPAPER";
                data.intentTargetPackage = info.activityInfo.packageName;
                data.intentTargetClass = info.activityInfo.name;
                result.add(data);
            }
            return result;
        }
    };

    @Override // com.android.settings.InstrumentedPreferenceFragment
    protected int getMetricsCategory() {
        return 101;
    }

    @Override // com.android.settings.SettingsPreferenceFragment
    protected int getHelpResource() {
        return R.string.help_uri_wallpaper;
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.wallpaper_settings);
        populateWallpaperTypes();
    }

    private void populateWallpaperTypes() {
        Intent intent = new Intent("android.intent.action.SET_WALLPAPER");
        PackageManager pm = getPackageManager();
        List<ResolveInfo> rList = pm.queryIntentActivities(intent, 65536);
        PreferenceScreen parent = getPreferenceScreen();
        parent.setOrderingAsAdded(false);
        for (ResolveInfo info : rList) {
            Preference pref = new Preference(getPrefContext());
            pref.setLayoutResource(R.layout.preference_wallpaper_type);
            Intent prefIntent = new Intent(intent);
            prefIntent.setComponent(new ComponentName(info.activityInfo.packageName, info.activityInfo.name));
            pref.setIntent(prefIntent);
            CharSequence label = info.loadLabel(pm);
            if (label == null) {
                label = info.activityInfo.packageName;
            }
            pref.setTitle(label);
            pref.setIcon(info.loadIcon(pm));
            parent.addPreference(pref);
        }
    }
}

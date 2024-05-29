package com.android.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.support.v7.preference.PreferenceGroup;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/* loaded from: classes.dex */
public class LegalSettings extends SettingsPreferenceFragment implements Indexable {
    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() { // from class: com.android.settings.LegalSettings.1
        @Override // com.android.settings.search.BaseSearchIndexProvider, com.android.settings.search.Indexable.SearchIndexProvider
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = R.xml.about_legal;
            return Arrays.asList(sir);
        }

        @Override // com.android.settings.search.BaseSearchIndexProvider, com.android.settings.search.Indexable.SearchIndexProvider
        public List<String> getNonIndexableKeys(Context context) {
            List<String> keys = new ArrayList<>();
            if (!checkIntentAction(context, "android.settings.TERMS")) {
                keys.add("terms");
            }
            if (!checkIntentAction(context, "android.settings.LICENSE")) {
                keys.add("license");
            }
            if (!checkIntentAction(context, "android.settings.COPYRIGHT")) {
                keys.add("copyright");
            }
            if (!checkIntentAction(context, "android.settings.WEBVIEW_LICENSE")) {
                keys.add("webview_license");
            }
            return keys;
        }

        private boolean checkIntentAction(Context context, String action) {
            Intent intent = new Intent(action);
            PackageManager pm = context.getPackageManager();
            List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);
            int listSize = list.size();
            for (int i = 0; i < listSize; i++) {
                ResolveInfo resolveInfo = list.get(i);
                if ((resolveInfo.activityInfo.applicationInfo.flags & 1) != 0) {
                    return true;
                }
            }
            return false;
        }
    };

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.about_legal);
        Activity act = getActivity();
        PreferenceGroup parentPreference = getPreferenceScreen();
        Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, "terms", 1);
        Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, "license", 1);
        Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, "copyright", 1);
        Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, "webview_license", 1);
    }

    @Override // com.android.settings.InstrumentedPreferenceFragment
    protected int getMetricsCategory() {
        return 225;
    }
}

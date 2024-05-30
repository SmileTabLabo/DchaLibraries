package com.android.settings.dream;

import android.content.Context;
import android.provider.SearchIndexableResource;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.dream.DreamBackend;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/* loaded from: classes.dex */
public class DreamSettings extends DashboardFragment {
    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() { // from class: com.android.settings.dream.DreamSettings.1
        @Override // com.android.settings.search.BaseSearchIndexProvider, com.android.settings.search.Indexable.SearchIndexProvider
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean z) {
            SearchIndexableResource searchIndexableResource = new SearchIndexableResource(context);
            searchIndexableResource.xmlResId = R.xml.dream_fragment_overview;
            return Arrays.asList(searchIndexableResource);
        }

        @Override // com.android.settings.search.BaseSearchIndexProvider
        public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
            return DreamSettings.buildPreferenceControllers(context);
        }
    };

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int getSettingFromPrefKey(String str) {
        char c;
        int hashCode = str.hashCode();
        if (hashCode == -1592701525) {
            if (str.equals("while_docked_only")) {
                c = 1;
            }
            c = 65535;
        } else if (hashCode == -294641318) {
            if (str.equals("either_charging_or_docked")) {
                c = 2;
            }
            c = 65535;
        } else if (hashCode != 104712844) {
            if (hashCode == 1019349036 && str.equals("while_charging_only")) {
                c = 0;
            }
            c = 65535;
        } else {
            if (str.equals("never")) {
                c = 3;
            }
            c = 65535;
        }
        switch (c) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 2:
                return 2;
            default:
                return 3;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static String getKeyFromSetting(int i) {
        switch (i) {
            case 0:
                return "while_charging_only";
            case 1:
                return "while_docked_only";
            case 2:
                return "either_charging_or_docked";
            default:
                return "never";
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int getDreamSettingDescriptionResId(int i) {
        switch (i) {
            case 0:
                return R.string.screensaver_settings_summary_sleep;
            case 1:
                return R.string.screensaver_settings_summary_dock;
            case 2:
                return R.string.screensaver_settings_summary_either_long;
            default:
                return R.string.screensaver_settings_summary_never;
        }
    }

    @Override // com.android.settingslib.core.instrumentation.Instrumentable
    public int getMetricsCategory() {
        return 47;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.dashboard.DashboardFragment, com.android.settings.core.InstrumentedPreferenceFragment
    public int getPreferenceScreenResId() {
        return R.xml.dream_fragment_overview;
    }

    @Override // com.android.settings.support.actionbar.HelpResourceProvider
    public int getHelpResource() {
        return R.string.help_url_screen_saver;
    }

    @Override // com.android.settings.dashboard.DashboardFragment
    protected String getLogTag() {
        return "DreamSettings";
    }

    @Override // com.android.settings.dashboard.DashboardFragment
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context);
    }

    public static CharSequence getSummaryTextWithDreamName(Context context) {
        return getSummaryTextFromBackend(DreamBackend.getInstance(context), context);
    }

    static CharSequence getSummaryTextFromBackend(DreamBackend dreamBackend, Context context) {
        if (!dreamBackend.isEnabled()) {
            return context.getString(R.string.screensaver_settings_summary_off);
        }
        return dreamBackend.getActiveDreamName();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static List<AbstractPreferenceController> buildPreferenceControllers(Context context) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(new CurrentDreamPreferenceController(context));
        arrayList.add(new WhenToDreamPreferenceController(context));
        arrayList.add(new StartNowPreferenceController(context));
        return arrayList;
    }
}

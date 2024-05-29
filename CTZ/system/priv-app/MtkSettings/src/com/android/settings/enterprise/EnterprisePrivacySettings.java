package com.android.settings.enterprise;

import android.content.Context;
import android.provider.SearchIndexableResource;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.widget.PreferenceCategoryController;
import com.android.settingslib.core.AbstractPreferenceController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/* loaded from: classes.dex */
public class EnterprisePrivacySettings extends DashboardFragment {
    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() { // from class: com.android.settings.enterprise.EnterprisePrivacySettings.1
        @Override // com.android.settings.search.BaseSearchIndexProvider
        protected boolean isPageSearchEnabled(Context context) {
            return EnterprisePrivacySettings.isPageEnabled(context);
        }

        @Override // com.android.settings.search.BaseSearchIndexProvider, com.android.settings.search.Indexable.SearchIndexProvider
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean z) {
            SearchIndexableResource searchIndexableResource = new SearchIndexableResource(context);
            searchIndexableResource.xmlResId = R.xml.enterprise_privacy_settings;
            return Arrays.asList(searchIndexableResource);
        }

        @Override // com.android.settings.search.BaseSearchIndexProvider
        public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
            return EnterprisePrivacySettings.buildPreferenceControllers(context, false);
        }
    };

    @Override // com.android.settingslib.core.instrumentation.Instrumentable
    public int getMetricsCategory() {
        return 628;
    }

    @Override // com.android.settings.dashboard.DashboardFragment
    protected String getLogTag() {
        return "EnterprisePrivacySettings";
    }

    @Override // com.android.settings.dashboard.DashboardFragment, com.android.settings.core.InstrumentedPreferenceFragment
    protected int getPreferenceScreenResId() {
        return R.xml.enterprise_privacy_settings;
    }

    @Override // com.android.settings.dashboard.DashboardFragment
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static List<AbstractPreferenceController> buildPreferenceControllers(Context context, boolean z) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(new NetworkLogsPreferenceController(context));
        arrayList.add(new BugReportsPreferenceController(context));
        arrayList.add(new SecurityLogsPreferenceController(context));
        ArrayList arrayList2 = new ArrayList();
        arrayList2.add(new EnterpriseInstalledPackagesPreferenceController(context, z));
        arrayList2.add(new AdminGrantedLocationPermissionsPreferenceController(context, z));
        arrayList2.add(new AdminGrantedMicrophonePermissionPreferenceController(context, z));
        arrayList2.add(new AdminGrantedCameraPermissionPreferenceController(context, z));
        arrayList2.add(new EnterpriseSetDefaultAppsPreferenceController(context));
        arrayList2.add(new AlwaysOnVpnCurrentUserPreferenceController(context));
        arrayList2.add(new AlwaysOnVpnManagedProfilePreferenceController(context));
        arrayList2.add(new ImePreferenceController(context));
        arrayList2.add(new GlobalHttpProxyPreferenceController(context));
        arrayList2.add(new CaCertsCurrentUserPreferenceController(context));
        arrayList2.add(new CaCertsManagedProfilePreferenceController(context));
        arrayList2.add(new BackupsEnabledPreferenceController(context));
        arrayList.addAll(arrayList2);
        arrayList.add(new PreferenceCategoryController(context, "exposure_changes_category").setChildren(arrayList2));
        arrayList.add(new FailedPasswordWipeCurrentUserPreferenceController(context));
        arrayList.add(new FailedPasswordWipeManagedProfilePreferenceController(context));
        return arrayList;
    }

    public static boolean isPageEnabled(Context context) {
        return FeatureFactory.getFactory(context).getEnterprisePrivacyFeatureProvider(context).hasDeviceOwner();
    }
}

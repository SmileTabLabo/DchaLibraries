package com.android.settings.security;

import android.content.Context;
import android.os.UserManager;
import android.provider.SearchIndexableResource;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.widget.PreferenceCategoryController;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/* loaded from: classes.dex */
public class EncryptionAndCredential extends DashboardFragment {
    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new SecuritySearchIndexProvider();

    @Override // com.android.settingslib.core.instrumentation.Instrumentable
    public int getMetricsCategory() {
        return 846;
    }

    @Override // com.android.settings.dashboard.DashboardFragment
    protected String getLogTag() {
        return "EncryptionAndCredential";
    }

    @Override // com.android.settings.dashboard.DashboardFragment
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getLifecycle());
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.dashboard.DashboardFragment, com.android.settings.core.InstrumentedPreferenceFragment
    public int getPreferenceScreenResId() {
        return R.xml.encryption_and_credential;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static List<AbstractPreferenceController> buildPreferenceControllers(Context context, Lifecycle lifecycle) {
        ArrayList arrayList = new ArrayList();
        EncryptionStatusPreferenceController encryptionStatusPreferenceController = new EncryptionStatusPreferenceController(context, "encryption_and_credentials_encryption_status");
        arrayList.add(encryptionStatusPreferenceController);
        arrayList.add(new PreferenceCategoryController(context, "encryption_and_credentials_status_category").setChildren(Arrays.asList(encryptionStatusPreferenceController)));
        arrayList.add(new CredentialStoragePreferenceController(context));
        arrayList.add(new UserCredentialsPreferenceController(context));
        arrayList.add(new ResetCredentialsPreferenceController(context, lifecycle));
        arrayList.add(new InstallCredentialsPreferenceController(context));
        return arrayList;
    }

    @Override // com.android.settings.support.actionbar.HelpResourceProvider
    public int getHelpResource() {
        return R.string.help_url_encryption;
    }

    /* loaded from: classes.dex */
    private static class SecuritySearchIndexProvider extends BaseSearchIndexProvider {
        private SecuritySearchIndexProvider() {
        }

        @Override // com.android.settings.search.BaseSearchIndexProvider, com.android.settings.search.Indexable.SearchIndexProvider
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean z) {
            SearchIndexableResource searchIndexableResource = new SearchIndexableResource(context);
            searchIndexableResource.xmlResId = R.xml.encryption_and_credential;
            return Arrays.asList(searchIndexableResource);
        }

        @Override // com.android.settings.search.BaseSearchIndexProvider
        public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
            return EncryptionAndCredential.buildPreferenceControllers(context, null);
        }

        @Override // com.android.settings.search.BaseSearchIndexProvider
        protected boolean isPageSearchEnabled(Context context) {
            return ((UserManager) context.getSystemService("user")).isAdminUser();
        }
    }
}

package com.android.settings.search;

import android.content.ComponentName;
import android.content.Context;
import android.text.TextUtils;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.search.indexing.IndexData;
import java.util.Locale;
/* loaded from: classes.dex */
public class SearchFeatureProviderImpl implements SearchFeatureProvider {
    private SearchIndexableResources mSearchIndexableResources;

    @Override // com.android.settings.search.SearchFeatureProvider
    public void verifyLaunchSearchResultPageCaller(Context context, ComponentName componentName) {
        if (componentName == null) {
            throw new IllegalArgumentException("ExternalSettingsTrampoline intents must be called with startActivityForResult");
        }
        String packageName = componentName.getPackageName();
        boolean z = TextUtils.equals(packageName, context.getPackageName()) || TextUtils.equals(getSettingsIntelligencePkgName(), packageName);
        boolean isSignatureWhitelisted = isSignatureWhitelisted(context, componentName.getPackageName());
        if (z || isSignatureWhitelisted) {
            return;
        }
        throw new SecurityException("Search result intents must be called with from a whitelisted package.");
    }

    @Override // com.android.settings.search.SearchFeatureProvider
    public SearchIndexableResources getSearchIndexableResources() {
        if (this.mSearchIndexableResources == null) {
            this.mSearchIndexableResources = new SearchIndexableResourcesImpl();
        }
        return this.mSearchIndexableResources;
    }

    protected boolean isSignatureWhitelisted(Context context, String str) {
        return false;
    }

    @VisibleForTesting
    String cleanQuery(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        if (Locale.getDefault().equals(Locale.JAPAN)) {
            str = IndexData.normalizeJapaneseString(str);
        }
        return str.trim();
    }
}

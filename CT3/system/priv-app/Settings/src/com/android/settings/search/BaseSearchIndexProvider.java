package com.android.settings.search;

import android.content.Context;
import android.provider.SearchIndexableResource;
import com.android.settings.search.Indexable;
import java.util.Collections;
import java.util.List;
/* loaded from: classes.dex */
public class BaseSearchIndexProvider implements Indexable.SearchIndexProvider {
    private static final List<String> EMPTY_LIST = Collections.emptyList();

    @Override // com.android.settings.search.Indexable.SearchIndexProvider
    public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
        return null;
    }

    @Override // com.android.settings.search.Indexable.SearchIndexProvider
    public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
        return null;
    }

    @Override // com.android.settings.search.Indexable.SearchIndexProvider
    public List<String> getNonIndexableKeys(Context context) {
        return EMPTY_LIST;
    }
}

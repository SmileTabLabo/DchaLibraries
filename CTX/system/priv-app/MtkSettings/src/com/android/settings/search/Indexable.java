package com.android.settings.search;

import android.content.Context;
import android.provider.SearchIndexableResource;
import java.util.List;
/* loaded from: classes.dex */
public interface Indexable {

    /* loaded from: classes.dex */
    public interface SearchIndexProvider {
        List<String> getNonIndexableKeys(Context context);

        List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean z);

        List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean z);
    }
}

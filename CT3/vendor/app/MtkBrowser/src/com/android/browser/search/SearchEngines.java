package com.android.browser.search;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.mediatek.search.SearchEngineManager;
/* loaded from: b.zip:com/android/browser/search/SearchEngines.class */
public class SearchEngines {
    public static SearchEngine get(Context context, String str) {
        SearchEngine defaultSearchEngine = getDefaultSearchEngine(context);
        if (TextUtils.isEmpty(str) || (defaultSearchEngine != null && str.equals(defaultSearchEngine.getName()))) {
            return defaultSearchEngine;
        }
        com.mediatek.common.search.SearchEngine searchEngineInfo = getSearchEngineInfo(context, str);
        return searchEngineInfo == null ? defaultSearchEngine : new OpenSearchSearchEngine(context, searchEngineInfo);
    }

    public static SearchEngine getDefaultSearchEngine(Context context) {
        return DefaultSearchEngine.create(context);
    }

    public static com.mediatek.common.search.SearchEngine getSearchEngineInfo(Context context, String str) {
        try {
            return ((SearchEngineManager) context.getSystemService("search_engine")).getByName(str);
        } catch (IllegalArgumentException e) {
            Log.e("SearchEngines", "Cannot load search engine " + str, e);
            return null;
        }
    }
}

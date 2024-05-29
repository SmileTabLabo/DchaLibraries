package com.android.browser;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import com.mediatek.common.search.SearchEngine;
import com.mediatek.search.SearchEngineManager;
/* loaded from: b.zip:com/android/browser/ChangeSearchEngineReceiver.class */
public class ChangeSearchEngineReceiver extends BroadcastReceiver {
    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
        SearchEngineManager searchEngineManager = (SearchEngineManager) context.getSystemService("search_engine");
        String action = intent.getAction();
        if (!"com.android.browser.SEARCH_ENGINE_CHANGED".equals(action)) {
            if ("com.mediatek.search.SEARCH_ENGINE_CHANGED".equals(action)) {
                Log.d("@M_browser/ChangeSearchEngineReceiver", "ChangeSearchEngineReceiver (search): search_engine---" + BrowserSettings.getInstance().getSearchEngineName());
            }
        } else if (intent.getExtras() == null) {
        } else {
            String string = intent.getExtras().getString("search_engine");
            SearchEngine byName = searchEngineManager.getByName(string);
            String faviconUri = byName != null ? byName.getFaviconUri() : "";
            edit.putString("search_engine", string);
            edit.putString("search_engine_favicon", faviconUri);
            edit.commit();
            Log.d("@M_browser/ChangeSearchEngineReceiver", "ChangeSearchEngineReceiver (browser): search_engine---" + intent.getExtras().getString("search_engine"));
        }
    }
}

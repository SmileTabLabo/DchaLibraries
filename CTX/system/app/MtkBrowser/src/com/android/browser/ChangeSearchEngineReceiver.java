package com.android.browser;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import com.mediatek.common.search.SearchEngine;
import com.mediatek.search.SearchEngineManager;
/* loaded from: classes.dex */
public class ChangeSearchEngineReceiver extends BroadcastReceiver {
    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
        SearchEngineManager searchEngineManager = (SearchEngineManager) context.getSystemService("search_engine_service");
        String str = "";
        if (!"com.android.browser.SEARCH_ENGINE_CHANGED".equals(intent.getAction()) || intent.getExtras() == null) {
            return;
        }
        String string = intent.getExtras().getString("search_engine");
        SearchEngine byName = searchEngineManager.getByName(string);
        if (byName != null) {
            str = byName.getFaviconUri();
        }
        edit.putString("search_engine", string);
        edit.putString("search_engine_favicon", str);
        edit.commit();
        Log.d("@M_browser/ChangeSearchEngineReceiver", "ChangeSearchEngineReceiver (browser): search_engine---" + intent.getExtras().getString("search_engine"));
    }
}

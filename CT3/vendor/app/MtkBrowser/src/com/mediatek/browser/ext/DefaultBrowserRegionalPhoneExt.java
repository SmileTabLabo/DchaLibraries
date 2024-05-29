package com.mediatek.browser.ext;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
/* loaded from: b.zip:com/mediatek/browser/ext/DefaultBrowserRegionalPhoneExt.class */
public class DefaultBrowserRegionalPhoneExt implements IBrowserRegionalPhoneExt {
    private static final String TAG = "DefaultBrowserRegionalPhoneExt";

    @Override // com.mediatek.browser.ext.IBrowserRegionalPhoneExt
    public String getSearchEngine(SharedPreferences sharedPreferences, Context context) {
        Log.i("@M_DefaultBrowserRegionalPhoneExt", "Enter: updateSearchEngine --default implement");
        return null;
    }

    @Override // com.mediatek.browser.ext.IBrowserRegionalPhoneExt
    public void updateBookmarks(Context context) {
        Log.i("@M_DefaultBrowserRegionalPhoneExt", "Enter: updateBookmarks --default implement");
    }
}

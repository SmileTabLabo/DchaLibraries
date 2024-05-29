package com.mediatek.browser.ext;

import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
/* loaded from: b.zip:com/mediatek/browser/ext/DefaultBrowserHistoryExt.class */
public class DefaultBrowserHistoryExt implements IBrowserHistoryExt {
    private static final String TAG = "DefaultBrowserHistoryExt";

    @Override // com.mediatek.browser.ext.IBrowserHistoryExt
    public void createHistoryPageOptionsMenu(Menu menu, MenuInflater menuInflater) {
        Log.i("@M_DefaultBrowserHistoryExt", "Enter: createHistoryPageOptionsMenu --default implement");
    }

    @Override // com.mediatek.browser.ext.IBrowserHistoryExt
    public boolean historyPageOptionsMenuItemSelected(MenuItem menuItem, Activity activity) {
        Log.i("@M_DefaultBrowserHistoryExt", "Enter: historyPageOptionsMenuItemSelected --default implement");
        return false;
    }

    @Override // com.mediatek.browser.ext.IBrowserHistoryExt
    public void prepareHistoryPageOptionsMenuItem(Menu menu, boolean z, boolean z2) {
        Log.i("@M_DefaultBrowserHistoryExt", "Enter: prepareHistoryPageOptionsMenuItem --default implement");
    }
}

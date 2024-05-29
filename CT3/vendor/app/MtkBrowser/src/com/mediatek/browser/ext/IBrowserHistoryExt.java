package com.mediatek.browser.ext;

import android.app.Activity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
/* loaded from: b.zip:com/mediatek/browser/ext/IBrowserHistoryExt.class */
public interface IBrowserHistoryExt {
    void createHistoryPageOptionsMenu(Menu menu, MenuInflater menuInflater);

    boolean historyPageOptionsMenuItemSelected(MenuItem menuItem, Activity activity);

    void prepareHistoryPageOptionsMenuItem(Menu menu, boolean z, boolean z2);
}

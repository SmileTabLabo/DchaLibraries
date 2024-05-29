package com.mediatek.browser.ext;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
/* loaded from: b.zip:com/mediatek/browser/ext/DefaultBrowserBookmarkExt.class */
public class DefaultBrowserBookmarkExt implements IBrowserBookmarkExt {
    private static final String TAG = "DefaultBrowserBookmarkExt";

    @Override // com.mediatek.browser.ext.IBrowserBookmarkExt
    public int addDefaultBookmarksForCustomer(SQLiteDatabase sQLiteDatabase) {
        Log.i("@M_DefaultBrowserBookmarkExt", "Enter: addDefaultBookmarksForCustomer --default implement");
        return 0;
    }

    @Override // com.mediatek.browser.ext.IBrowserBookmarkExt
    public boolean bookmarksPageOptionsMenuItemSelected(MenuItem menuItem, Activity activity, long j) {
        Log.i("@M_DefaultBrowserBookmarkExt", "Enter: bookmarksPageOptionsMenuItemSelected --default implement");
        return false;
    }

    @Override // com.mediatek.browser.ext.IBrowserBookmarkExt
    public void createBookmarksPageOptionsMenu(Menu menu, MenuInflater menuInflater) {
        Log.i("@M_DefaultBrowserBookmarkExt", "Enter: createBookmarksPageOptionsMenu --default implement");
    }

    @Override // com.mediatek.browser.ext.IBrowserBookmarkExt
    public boolean customizeEditExistingFolderState(Bundle bundle, boolean z) {
        Log.i(TAG, "Enter: customizeEditExistingFolderState --default implement");
        return z;
    }

    @Override // com.mediatek.browser.ext.IBrowserBookmarkExt
    public String getCustomizedEditFolderFakeTitleString(Bundle bundle, String str) {
        Log.i(TAG, "Enter: getCustomizedEditFolderFakeTitleString --default implement");
        return str;
    }

    @Override // com.mediatek.browser.ext.IBrowserBookmarkExt
    public Boolean saveCustomizedEditFolder(Context context, String str, long j, Bundle bundle, String str2) {
        Log.i(TAG, "Enter: saveCustomizedEditFolder --default implement");
        return null;
    }

    @Override // com.mediatek.browser.ext.IBrowserBookmarkExt
    public boolean shouldSetCustomizedEditFolderSelection(Bundle bundle, boolean z) {
        Log.i(TAG, "Enter: shouldSetCustomizedEditFolderSelection --default implement");
        return z;
    }

    @Override // com.mediatek.browser.ext.IBrowserBookmarkExt
    public void showCustomizedEditFolderNewFolderView(View view, View view2, Bundle bundle) {
        Log.i(TAG, "Enter: showCustomizedEditFolderNewFolderView --default implement");
        if (view != null) {
            view.setVisibility(0);
        }
        if (view2 != null) {
            view2.setVisibility(0);
        }
    }
}

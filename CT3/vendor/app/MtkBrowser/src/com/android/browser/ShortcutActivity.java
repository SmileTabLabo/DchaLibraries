package com.android.browser;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
/* loaded from: b.zip:com/android/browser/ShortcutActivity.class */
public class ShortcutActivity extends Activity implements BookmarksPageCallbacks, View.OnClickListener {
    private BrowserBookmarksPage mBookmarks;

    @Override // com.android.browser.BookmarksPageCallbacks
    public boolean onBookmarkSelected(Cursor cursor, boolean z) {
        if (z) {
            return false;
        }
        setResult(-1, BrowserBookmarksPage.createShortcutIntent(this, cursor));
        finish();
        return true;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        switch (view.getId()) {
            case 2131558462:
                finish();
                return;
            default:
                return;
        }
    }

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setTitle(2131493027);
        setContentView(2130968613);
        this.mBookmarks = (BrowserBookmarksPage) getFragmentManager().findFragmentById(2131558445);
        this.mBookmarks.setEnableContextMenu(false);
        this.mBookmarks.setCallbackListener(this);
        View findViewById = findViewById(2131558462);
        if (findViewById != null) {
            findViewById.setOnClickListener(this);
        }
    }

    @Override // com.android.browser.BookmarksPageCallbacks
    public boolean onOpenInNewWindow(String... strArr) {
        return false;
    }
}

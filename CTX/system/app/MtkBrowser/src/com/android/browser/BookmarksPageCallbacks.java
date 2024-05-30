package com.android.browser;

import android.database.Cursor;
/* compiled from: BrowserBookmarksPage.java */
/* loaded from: classes.dex */
interface BookmarksPageCallbacks {
    boolean onBookmarkSelected(Cursor cursor, boolean z);

    boolean onOpenInNewWindow(String... strArr);
}

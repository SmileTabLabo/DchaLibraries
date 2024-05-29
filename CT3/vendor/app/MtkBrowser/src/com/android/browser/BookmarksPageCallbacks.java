package com.android.browser;

import android.database.Cursor;
/* loaded from: b.zip:com/android/browser/BookmarksPageCallbacks.class */
interface BookmarksPageCallbacks {
    boolean onBookmarkSelected(Cursor cursor, boolean z);

    boolean onOpenInNewWindow(String... strArr);
}

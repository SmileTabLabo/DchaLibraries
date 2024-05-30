package com.android.browser.provider;

import android.content.ContentUris;
import android.net.Uri;
/* loaded from: classes.dex */
public class BrowserContract {
    public static final Uri AUTHORITY_URI = Uri.parse("content://com.android.browser.provider");

    /* loaded from: classes.dex */
    public static final class Accounts {
        public static final Uri CONTENT_URI = BrowserContract.AUTHORITY_URI.buildUpon().appendPath("accounts").build();
    }

    /* loaded from: classes.dex */
    public static final class Combined {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BrowserContract.AUTHORITY_URI, "combined");
    }

    /* loaded from: classes.dex */
    public static final class History {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BrowserContract.AUTHORITY_URI, "history");
    }

    /* loaded from: classes.dex */
    public static final class Images {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BrowserContract.AUTHORITY_URI, "images");
    }

    /* loaded from: classes.dex */
    public static final class Searches {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BrowserContract.AUTHORITY_URI, "searches");
    }

    /* loaded from: classes.dex */
    public static final class Bookmarks {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BrowserContract.AUTHORITY_URI, "bookmarks");
        public static final Uri CONTENT_URI_DEFAULT_FOLDER = Uri.withAppendedPath(CONTENT_URI, "folder");

        public static final Uri buildFolderUri(long j) {
            return ContentUris.withAppendedId(CONTENT_URI_DEFAULT_FOLDER, j);
        }
    }
}

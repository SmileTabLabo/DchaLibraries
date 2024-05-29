package com.android.browser.provider;

import android.content.ContentUris;
import android.net.Uri;
/* loaded from: b.zip:com/android/browser/provider/BrowserContract.class */
public class BrowserContract {
    public static final Uri AUTHORITY_URI = Uri.parse("content://com.android.browser.provider");

    /* loaded from: b.zip:com/android/browser/provider/BrowserContract$Accounts.class */
    public static final class Accounts {
        public static final Uri CONTENT_URI = BrowserContract.AUTHORITY_URI.buildUpon().appendPath("accounts").build();
    }

    /* loaded from: b.zip:com/android/browser/provider/BrowserContract$Bookmarks.class */
    public static final class Bookmarks {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BrowserContract.AUTHORITY_URI, "bookmarks");
        public static final Uri CONTENT_URI_DEFAULT_FOLDER = Uri.withAppendedPath(CONTENT_URI, "folder");

        private Bookmarks() {
        }

        public static final Uri buildFolderUri(long j) {
            return ContentUris.withAppendedId(CONTENT_URI_DEFAULT_FOLDER, j);
        }
    }

    /* loaded from: b.zip:com/android/browser/provider/BrowserContract$Combined.class */
    public static final class Combined {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BrowserContract.AUTHORITY_URI, "combined");

        private Combined() {
        }
    }

    /* loaded from: b.zip:com/android/browser/provider/BrowserContract$History.class */
    public static final class History {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BrowserContract.AUTHORITY_URI, "history");

        private History() {
        }
    }

    /* loaded from: b.zip:com/android/browser/provider/BrowserContract$Images.class */
    public static final class Images {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BrowserContract.AUTHORITY_URI, "images");

        private Images() {
        }
    }

    /* loaded from: b.zip:com/android/browser/provider/BrowserContract$Searches.class */
    public static final class Searches {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BrowserContract.AUTHORITY_URI, "searches");

        private Searches() {
        }
    }
}

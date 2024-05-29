package com.android.quicksearchbox.google;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import com.android.quicksearchbox.CursorBackedSourceResult;
import com.android.quicksearchbox.QsbApplication;
import com.android.quicksearchbox.SourceResult;
import com.android.quicksearchbox.SuggestionCursorBackedCursor;
/* loaded from: a.zip:com/android/quicksearchbox/google/GoogleSuggestionProvider.class */
public class GoogleSuggestionProvider extends ContentProvider {
    private GoogleSource mSource;
    private UriMatcher mUriMatcher;

    private UriMatcher buildUriMatcher(Context context) {
        String authority = getAuthority(context);
        UriMatcher uriMatcher = new UriMatcher(-1);
        uriMatcher.addURI(authority, "search_suggest_query", 0);
        uriMatcher.addURI(authority, "search_suggest_query/*", 0);
        uriMatcher.addURI(authority, "search_suggest_shortcut", 1);
        uriMatcher.addURI(authority, "search_suggest_shortcut/*", 1);
        return uriMatcher;
    }

    private SourceResult emptyIfNull(SourceResult sourceResult, GoogleSource googleSource, String str) {
        CursorBackedSourceResult cursorBackedSourceResult = sourceResult;
        if (sourceResult == null) {
            cursorBackedSourceResult = new CursorBackedSourceResult(googleSource, str);
        }
        return cursorBackedSourceResult;
    }

    private String getQuery(Uri uri) {
        return uri.getPathSegments().size() > 1 ? uri.getLastPathSegment() : "";
    }

    @Override // android.content.ContentProvider
    public int delete(Uri uri, String str, String[] strArr) {
        throw new UnsupportedOperationException();
    }

    protected String getAuthority(Context context) {
        return context.getPackageName() + ".google";
    }

    @Override // android.content.ContentProvider
    public String getType(Uri uri) {
        return "vnd.android.cursor.dir/vnd.android.search.suggest";
    }

    @Override // android.content.ContentProvider
    public Uri insert(Uri uri, ContentValues contentValues) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.ContentProvider
    public boolean onCreate() {
        this.mSource = QsbApplication.get(getContext()).getGoogleSource();
        this.mUriMatcher = buildUriMatcher(getContext());
        return true;
    }

    @Override // android.content.ContentProvider
    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        int match = this.mUriMatcher.match(uri);
        if (match == 0) {
            String query = getQuery(uri);
            return new SuggestionCursorBackedCursor(emptyIfNull(this.mSource.queryExternal(query), this.mSource, query));
        } else if (match == 1) {
            return new SuggestionCursorBackedCursor(this.mSource.refreshShortcut(getQuery(uri), uri.getQueryParameter("suggest_intent_extra_data")));
        } else {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override // android.content.ContentProvider
    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        throw new UnsupportedOperationException();
    }
}

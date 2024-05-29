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
/* loaded from: classes.dex */
public class GoogleSuggestionProvider extends ContentProvider {
    private GoogleSource mSource;
    private UriMatcher mUriMatcher;

    @Override // android.content.ContentProvider
    public boolean onCreate() {
        this.mSource = QsbApplication.get(getContext()).getGoogleSource();
        this.mUriMatcher = buildUriMatcher(getContext());
        return true;
    }

    @Override // android.content.ContentProvider
    public String getType(Uri uri) {
        return "vnd.android.cursor.dir/vnd.android.search.suggest";
    }

    private SourceResult emptyIfNull(SourceResult sourceResult, GoogleSource googleSource, String str) {
        return sourceResult == null ? new CursorBackedSourceResult(googleSource, str) : sourceResult;
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

    private String getQuery(Uri uri) {
        if (uri.getPathSegments().size() > 1) {
            return uri.getLastPathSegment();
        }
        return "";
    }

    @Override // android.content.ContentProvider
    public Uri insert(Uri uri, ContentValues contentValues) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.ContentProvider
    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.ContentProvider
    public int delete(Uri uri, String str, String[] strArr) {
        throw new UnsupportedOperationException();
    }

    private UriMatcher buildUriMatcher(Context context) {
        String authority = getAuthority(context);
        UriMatcher uriMatcher = new UriMatcher(-1);
        uriMatcher.addURI(authority, "search_suggest_query", 0);
        uriMatcher.addURI(authority, "search_suggest_query/*", 0);
        uriMatcher.addURI(authority, "search_suggest_shortcut", 1);
        uriMatcher.addURI(authority, "search_suggest_shortcut/*", 1);
        return uriMatcher;
    }

    protected String getAuthority(Context context) {
        return context.getPackageName() + ".google";
    }
}

package com.android.browser.search;

import android.content.Context;
import android.content.Intent;
import android.database.AbstractCursor;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import libcore.io.Streams;
import libcore.net.http.ResponseUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
/* loaded from: b.zip:com/android/browser/search/OpenSearchSearchEngine.class */
public class OpenSearchSearchEngine implements SearchEngine {
    private static final String[] COLUMNS = {"_id", "suggest_intent_query", "suggest_icon_1", "suggest_text_1", "suggest_text_2"};
    private static final String[] COLUMNS_WITHOUT_DESCRIPTION = {"_id", "suggest_intent_query", "suggest_icon_1", "suggest_text_1"};
    private final com.mediatek.common.search.SearchEngine mSearchEngine;

    /* loaded from: b.zip:com/android/browser/search/OpenSearchSearchEngine$SuggestionsCursor.class */
    private static class SuggestionsCursor extends AbstractCursor {
        private final JSONArray mDescriptions;
        private final JSONArray mSuggestions;

        public SuggestionsCursor(JSONArray jSONArray, JSONArray jSONArray2) {
            this.mSuggestions = jSONArray;
            this.mDescriptions = jSONArray2;
        }

        @Override // android.database.AbstractCursor, android.database.Cursor
        public String[] getColumnNames() {
            return this.mDescriptions != null ? OpenSearchSearchEngine.COLUMNS : OpenSearchSearchEngine.COLUMNS_WITHOUT_DESCRIPTION;
        }

        @Override // android.database.AbstractCursor, android.database.Cursor
        public int getCount() {
            return this.mSuggestions.length();
        }

        @Override // android.database.AbstractCursor, android.database.Cursor
        public double getDouble(int i) {
            throw new UnsupportedOperationException();
        }

        @Override // android.database.AbstractCursor, android.database.Cursor
        public float getFloat(int i) {
            throw new UnsupportedOperationException();
        }

        @Override // android.database.AbstractCursor, android.database.Cursor
        public int getInt(int i) {
            throw new UnsupportedOperationException();
        }

        @Override // android.database.AbstractCursor, android.database.Cursor
        public long getLong(int i) {
            if (i == 0) {
                return this.mPos;
            }
            throw new UnsupportedOperationException();
        }

        @Override // android.database.AbstractCursor, android.database.Cursor
        public short getShort(int i) {
            throw new UnsupportedOperationException();
        }

        @Override // android.database.AbstractCursor, android.database.Cursor
        public String getString(int i) {
            if (this.mPos != -1) {
                if (i == 1 || i == 3) {
                    try {
                        return this.mSuggestions.getString(this.mPos);
                    } catch (JSONException e) {
                        Log.w("OpenSearchSearchEngine", "Error", e);
                        return null;
                    }
                } else if (i != 4) {
                    if (i == 2) {
                        return String.valueOf(2130837597);
                    }
                    return null;
                } else {
                    try {
                        return this.mDescriptions.getString(this.mPos);
                    } catch (JSONException e2) {
                        Log.w("OpenSearchSearchEngine", "Error", e2);
                        return null;
                    }
                }
            }
            return null;
        }

        @Override // android.database.AbstractCursor, android.database.Cursor
        public boolean isNull(int i) {
            throw new UnsupportedOperationException();
        }
    }

    public OpenSearchSearchEngine(Context context, com.mediatek.common.search.SearchEngine searchEngine) {
        this.mSearchEngine = searchEngine;
    }

    private NetworkInfo getActiveNetworkInfo(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        if (connectivityManager == null) {
            return null;
        }
        return connectivityManager.getActiveNetworkInfo();
    }

    private boolean isNetworkConnected(Context context) {
        NetworkInfo activeNetworkInfo = getActiveNetworkInfo(context);
        return activeNetworkInfo != null ? activeNetworkInfo.isConnected() : false;
    }

    @Override // com.android.browser.search.SearchEngine
    public String getName() {
        return this.mSearchEngine.getName();
    }

    @Override // com.android.browser.search.SearchEngine
    public Cursor getSuggestions(Context context, String str) {
        JSONArray jSONArray;
        JSONArray jSONArray2;
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        if (!isNetworkConnected(context)) {
            Log.i("OpenSearchSearchEngine", "Not connected to network.");
            return null;
        }
        String suggestUriForQuery = this.mSearchEngine.getSuggestUriForQuery(str);
        if (TextUtils.isEmpty(suggestUriForQuery)) {
            return null;
        }
        try {
            String readUrl = readUrl(suggestUriForQuery);
            if (readUrl == null) {
                return null;
            }
            if (!this.mSearchEngine.getName().equals("baidu")) {
                JSONArray jSONArray3 = new JSONArray(readUrl);
                JSONArray jSONArray4 = jSONArray3.getJSONArray(1);
                jSONArray = null;
                jSONArray2 = jSONArray4;
                if (jSONArray3.length() > 2) {
                    jSONArray = jSONArray3.getJSONArray(2);
                    jSONArray2 = jSONArray4;
                    if (jSONArray.length() == 0) {
                        jSONArray = null;
                        jSONArray2 = jSONArray4;
                    }
                }
            } else if (readUrl.length() < 19) {
                return null;
            } else {
                jSONArray2 = new JSONObject(readUrl.substring(17, readUrl.length() - 2)).getJSONArray("s");
                jSONArray = null;
            }
            return new SuggestionsCursor(jSONArray2, jSONArray);
        } catch (JSONException e) {
            Log.w("OpenSearchSearchEngine", "Error", e);
            return null;
        }
    }

    public String readUrl(String str) {
        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(str).openConnection();
            httpURLConnection.setRequestProperty("User-Agent", "Android/1.0");
            httpURLConnection.setConnectTimeout(1000);
            if (httpURLConnection.getResponseCode() != 200) {
                Log.i("OpenSearchSearchEngine", "Suggestion request failed");
                return null;
            }
            try {
                return new String(Streams.readFully(httpURLConnection.getInputStream()), ResponseUtils.responseCharset(httpURLConnection.getContentType()));
            } catch (IllegalCharsetNameException e) {
                Log.i("OpenSearchSearchEngine", "Illegal response charset", e);
                return null;
            } catch (UnsupportedCharsetException e2) {
                Log.i("OpenSearchSearchEngine", "Unsupported response charset", e2);
                return null;
            }
        } catch (IOException e3) {
            Log.w("OpenSearchSearchEngine", "Error", e3);
            return null;
        }
    }

    @Override // com.android.browser.search.SearchEngine
    public void startSearch(Context context, String str, Bundle bundle, String str2) {
        String searchUriForQuery = this.mSearchEngine.getSearchUriForQuery(str);
        if (searchUriForQuery == null) {
            Log.e("OpenSearchSearchEngine", "Unable to get search URI for " + this.mSearchEngine);
            return;
        }
        Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(searchUriForQuery));
        intent.setPackage(context.getPackageName());
        intent.addCategory("android.intent.category.DEFAULT");
        intent.putExtra("query", str);
        if (bundle != null) {
            intent.putExtra("app_data", bundle);
        }
        if (str2 != null) {
            intent.putExtra("intent_extra_data_key", str2);
        }
        intent.putExtra("com.android.browser.application_id", context.getPackageName());
        context.startActivity(intent);
    }

    @Override // com.android.browser.search.SearchEngine
    public boolean supportsSuggestions() {
        return this.mSearchEngine.supportsSuggestions();
    }

    public String toString() {
        return "OpenSearchSearchEngine{" + this.mSearchEngine + "}";
    }

    @Override // com.android.browser.search.SearchEngine
    public boolean wantsEmptyQuery() {
        return false;
    }
}

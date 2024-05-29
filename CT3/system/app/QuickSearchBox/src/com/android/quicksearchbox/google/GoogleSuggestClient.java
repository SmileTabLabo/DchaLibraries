package com.android.quicksearchbox.google;

import android.content.ComponentName;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.http.AndroidHttpClient;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import com.android.quicksearchbox.Config;
import com.android.quicksearchbox.Source;
import com.android.quicksearchbox.SourceResult;
import com.android.quicksearchbox.SuggestionCursor;
import com.android.quicksearchbox.util.NamedTaskExecutor;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
/* loaded from: a.zip:com/android/quicksearchbox/google/GoogleSuggestClient.class */
public class GoogleSuggestClient extends AbstractGoogleSource {
    private static final String USER_AGENT = "Android/" + Build.VERSION.RELEASE;
    private final HttpClient mHttpClient;
    private String mSuggestUri;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/quicksearchbox/google/GoogleSuggestClient$GoogleSuggestCursor.class */
    public static class GoogleSuggestCursor extends AbstractGoogleSourceResult {
        private final JSONArray mPopularity;
        private final JSONArray mSuggestions;

        public GoogleSuggestCursor(Source source, String str, JSONArray jSONArray, JSONArray jSONArray2) {
            super(source, str);
            this.mSuggestions = jSONArray;
            this.mPopularity = jSONArray2;
        }

        @Override // com.android.quicksearchbox.google.AbstractGoogleSourceResult, com.android.quicksearchbox.SuggestionCursor
        public int getCount() {
            return this.mSuggestions.length();
        }

        @Override // com.android.quicksearchbox.google.AbstractGoogleSourceResult, com.android.quicksearchbox.Suggestion
        public String getSuggestionQuery() {
            try {
                return this.mSuggestions.getString(getPosition());
            } catch (JSONException e) {
                Log.w("GoogleSearch", "Error parsing response: " + e);
                return null;
            }
        }

        @Override // com.android.quicksearchbox.google.AbstractGoogleSourceResult, com.android.quicksearchbox.Suggestion
        public String getSuggestionText2() {
            try {
                return this.mPopularity.getString(getPosition());
            } catch (JSONException e) {
                Log.w("GoogleSearch", "Error parsing response: " + e);
                return null;
            }
        }
    }

    public GoogleSuggestClient(Context context, Handler handler, NamedTaskExecutor namedTaskExecutor, Config config) {
        super(context, handler, namedTaskExecutor);
        this.mHttpClient = AndroidHttpClient.newInstance(USER_AGENT, context);
        this.mHttpClient.getParams().setLongParameter("http.conn-manager.timeout", config.getHttpConnectTimeout());
        this.mSuggestUri = null;
    }

    private NetworkInfo getActiveNetworkInfo() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService("connectivity");
        if (connectivityManager == null) {
            return null;
        }
        return connectivityManager.getActiveNetworkInfo();
    }

    private boolean isNetworkConnected() {
        NetworkInfo activeNetworkInfo = getActiveNetworkInfo();
        return activeNetworkInfo != null ? activeNetworkInfo.isConnected() : false;
    }

    private SourceResult query(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        if (!isNetworkConnected()) {
            Log.i("GoogleSearch", "Not connected to network.");
            return null;
        }
        try {
            String encode = URLEncoder.encode(str, "UTF-8");
            if (this.mSuggestUri == null) {
                this.mSuggestUri = getContext().getResources().getString(2131296262, GoogleSearch.getLanguage(Locale.getDefault()));
            }
            HttpResponse execute = this.mHttpClient.execute(new HttpGet(this.mSuggestUri + encode));
            if (execute.getStatusLine().getStatusCode() == 200) {
                JSONArray jSONArray = new JSONArray(EntityUtils.toString(execute.getEntity()));
                return new GoogleSuggestCursor(this, str, jSONArray.getJSONArray(1), jSONArray.getJSONArray(2));
            }
            return null;
        } catch (UnsupportedEncodingException e) {
            Log.w("GoogleSearch", "Error", e);
            return null;
        } catch (IOException e2) {
            Log.w("GoogleSearch", "Error", e2);
            return null;
        } catch (JSONException e3) {
            Log.w("GoogleSearch", "Error", e3);
            return null;
        }
    }

    @Override // com.android.quicksearchbox.google.AbstractGoogleSource, com.android.quicksearchbox.Source
    public ComponentName getIntentComponent() {
        return new ComponentName(getContext(), GoogleSearch.class);
    }

    @Override // com.android.quicksearchbox.google.AbstractGoogleSource, com.android.quicksearchbox.google.GoogleSource
    public SourceResult queryExternal(String str) {
        return query(str);
    }

    @Override // com.android.quicksearchbox.google.AbstractGoogleSource
    public SourceResult queryInternal(String str) {
        return query(str);
    }

    @Override // com.android.quicksearchbox.google.AbstractGoogleSource, com.android.quicksearchbox.google.GoogleSource
    public SuggestionCursor refreshShortcut(String str, String str2) {
        return null;
    }
}

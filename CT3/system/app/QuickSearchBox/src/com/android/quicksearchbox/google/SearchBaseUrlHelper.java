package com.android.quicksearchbox.google;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import com.android.quicksearchbox.SearchSettings;
import com.android.quicksearchbox.util.HttpHelper;
import java.util.Locale;
/* loaded from: a.zip:com/android/quicksearchbox/google/SearchBaseUrlHelper.class */
public class SearchBaseUrlHelper implements SharedPreferences.OnSharedPreferenceChangeListener {
    private final Context mContext;
    private final HttpHelper mHttpHelper;
    private final SearchSettings mSearchSettings;

    public SearchBaseUrlHelper(Context context, HttpHelper httpHelper, SearchSettings searchSettings, SharedPreferences sharedPreferences) {
        this.mHttpHelper = httpHelper;
        this.mContext = context;
        this.mSearchSettings = searchSettings;
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        maybeUpdateBaseUrlSetting(false);
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.quicksearchbox.google.SearchBaseUrlHelper$1] */
    private void checkSearchDomain() {
        new AsyncTask<Void, Void, Void>(this, new HttpHelper.GetRequest("https://www.google.com/searchdomaincheck?format=domain")) { // from class: com.android.quicksearchbox.google.SearchBaseUrlHelper.1
            final SearchBaseUrlHelper this$0;
            final HttpHelper.GetRequest val$request;

            {
                this.this$0 = this;
                this.val$request = r5;
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public Void doInBackground(Void... voidArr) {
                try {
                    this.this$0.setSearchBaseDomain(this.this$0.mHttpHelper.get(this.val$request));
                    return null;
                } catch (Exception e) {
                    this.this$0.getDefaultBaseDomain();
                    return null;
                }
            }
        }.execute(new Void[0]);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String getDefaultBaseDomain() {
        return this.mContext.getResources().getString(2131296261);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setSearchBaseDomain(String str) {
        this.mSearchSettings.setSearchBaseDomain(str);
    }

    public String getSearchBaseUrl() {
        return this.mContext.getResources().getString(2131296260, getSearchDomain(), GoogleSearch.getLanguage(Locale.getDefault()));
    }

    public String getSearchDomain() {
        String searchBaseDomain = this.mSearchSettings.getSearchBaseDomain();
        String str = searchBaseDomain;
        if (searchBaseDomain == null) {
            str = getDefaultBaseDomain();
        }
        String str2 = str;
        if (str.startsWith(".")) {
            str2 = "www" + str;
        }
        return str2;
    }

    public void maybeUpdateBaseUrlSetting(boolean z) {
        long searchBaseDomainApplyTime = this.mSearchSettings.getSearchBaseDomainApplyTime();
        long currentTimeMillis = System.currentTimeMillis();
        if (z || searchBaseDomainApplyTime == -1 || currentTimeMillis - searchBaseDomainApplyTime >= 86400000) {
            if (this.mSearchSettings.shouldUseGoogleCom()) {
                setSearchBaseDomain(getDefaultBaseDomain());
            } else {
                checkSearchDomain();
            }
        }
    }

    @Override // android.content.SharedPreferences.OnSharedPreferenceChangeListener
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String str) {
        if ("use_google_com".equals(str)) {
            maybeUpdateBaseUrlSetting(true);
        }
    }
}

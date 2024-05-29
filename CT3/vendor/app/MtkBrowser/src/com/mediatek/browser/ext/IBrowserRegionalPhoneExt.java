package com.mediatek.browser.ext;

import android.content.Context;
import android.content.SharedPreferences;
/* loaded from: b.zip:com/mediatek/browser/ext/IBrowserRegionalPhoneExt.class */
public interface IBrowserRegionalPhoneExt {
    String getSearchEngine(SharedPreferences sharedPreferences, Context context);

    void updateBookmarks(Context context);
}

package com.android.quicksearchbox;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
/* loaded from: a.zip:com/android/quicksearchbox/Help.class */
public class Help {
    private final Config mConfig;
    private final Context mContext;

    public Help(Context context, Config config) {
        this.mContext = context;
        this.mConfig = config;
    }

    private Intent getHelpIntent(String str) {
        Uri helpUrl = this.mConfig.getHelpUrl(str);
        if (helpUrl == null) {
            return null;
        }
        return new Intent("android.intent.action.VIEW", helpUrl);
    }

    public void addHelpMenuItem(Menu menu, String str) {
        addHelpMenuItem(menu, str, false);
    }

    public void addHelpMenuItem(Menu menu, String str, boolean z) {
        Intent intent = null;
        if (Settings.System.getInt(this.mContext.getContentResolver(), "dcha_state", 0) == 0) {
            intent = getHelpIntent(str);
        }
        if (intent != null) {
            new MenuInflater(this.mContext).inflate(2131623936, menu);
            MenuItem findItem = menu.findItem(2131689503);
            findItem.setIntent(intent);
            if (z) {
                findItem.setShowAsAction(2);
            }
        }
    }
}

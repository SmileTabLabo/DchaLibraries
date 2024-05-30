package com.android.quicksearchbox;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
/* loaded from: classes.dex */
public class Help {
    private final Config mConfig;
    private final Context mContext;

    public Help(Context context, Config config) {
        this.mContext = context;
        this.mConfig = config;
    }

    public void addHelpMenuItem(Menu menu, String str) {
        addHelpMenuItem(menu, str, false);
    }

    public void addHelpMenuItem(Menu menu, String str, boolean z) {
        Intent intent;
        if (Settings.System.getInt(this.mContext.getContentResolver(), "dcha_state", 0) == 0) {
            intent = getHelpIntent(str);
        } else {
            intent = null;
        }
        if (intent != null) {
            new MenuInflater(this.mContext).inflate(R.menu.help, menu);
            MenuItem findItem = menu.findItem(R.id.menu_help);
            findItem.setIntent(intent);
            if (z) {
                findItem.setShowAsAction(2);
            }
        }
    }

    private Intent getHelpIntent(String str) {
        Uri helpUrl = this.mConfig.getHelpUrl(str);
        if (helpUrl == null) {
            return null;
        }
        return new Intent("android.intent.action.VIEW", helpUrl);
    }
}

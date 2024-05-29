package com.android.launcher3.allapps;

import android.os.Handler;
import com.android.launcher3.AppInfo;
import com.android.launcher3.allapps.AllAppsSearchBarController;
import com.android.launcher3.util.ComponentKey;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
/* loaded from: a.zip:com/android/launcher3/allapps/DefaultAppSearchAlgorithm.class */
public class DefaultAppSearchAlgorithm {
    private static final Pattern SPLIT_PATTERN = Pattern.compile("[\\s|\\p{javaSpaceChar}]+");
    private final List<AppInfo> mApps;
    protected final Handler mResultHandler = new Handler();

    public DefaultAppSearchAlgorithm(List<AppInfo> list) {
        this.mApps = list;
    }

    public void cancel(boolean z) {
        if (z) {
            this.mResultHandler.removeCallbacksAndMessages(null);
        }
    }

    public void doSearch(String str, AllAppsSearchBarController.Callbacks callbacks) {
        this.mResultHandler.post(new Runnable(this, callbacks, str, getTitleMatchResult(str)) { // from class: com.android.launcher3.allapps.DefaultAppSearchAlgorithm.1
            final DefaultAppSearchAlgorithm this$0;
            final AllAppsSearchBarController.Callbacks val$callback;
            final String val$query;
            final ArrayList val$result;

            {
                this.this$0 = this;
                this.val$callback = callbacks;
                this.val$query = str;
                this.val$result = r7;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.val$callback.onSearchResult(this.val$query, this.val$result);
            }
        });
    }

    protected ArrayList<ComponentKey> getTitleMatchResult(String str) {
        String[] split = SPLIT_PATTERN.split(str.toLowerCase());
        ArrayList<ComponentKey> arrayList = new ArrayList<>();
        for (AppInfo appInfo : this.mApps) {
            if (matches(appInfo, split)) {
                arrayList.add(appInfo.toComponentKey());
            }
        }
        return arrayList;
    }

    protected boolean matches(AppInfo appInfo, String[] strArr) {
        boolean z;
        String[] split = SPLIT_PATTERN.split(appInfo.title.toString().toLowerCase());
        for (String str : strArr) {
            int i = 0;
            while (true) {
                z = false;
                if (i >= split.length) {
                    break;
                } else if (split[i].startsWith(str)) {
                    z = true;
                    break;
                } else {
                    i++;
                }
            }
            if (!z) {
                return false;
            }
        }
        return true;
    }
}

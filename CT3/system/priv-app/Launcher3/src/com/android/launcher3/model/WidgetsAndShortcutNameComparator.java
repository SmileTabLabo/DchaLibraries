package com.android.launcher3.model;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;
import com.android.launcher3.LauncherAppWidgetProviderInfo;
import com.android.launcher3.Utilities;
import com.android.launcher3.compat.AppWidgetManagerCompat;
import com.android.launcher3.compat.UserHandleCompat;
import com.android.launcher3.util.ComponentKey;
import java.text.Collator;
import java.util.Comparator;
import java.util.HashMap;
/* loaded from: a.zip:com/android/launcher3/model/WidgetsAndShortcutNameComparator.class */
public class WidgetsAndShortcutNameComparator implements Comparator<Object> {
    private final AppWidgetManagerCompat mManager;
    private final PackageManager mPackageManager;
    private final HashMap<ComponentKey, String> mLabelCache = new HashMap<>();
    private final Collator mCollator = Collator.getInstance();
    private final UserHandleCompat mMainHandle = UserHandleCompat.myUserHandle();

    public WidgetsAndShortcutNameComparator(Context context) {
        this.mManager = AppWidgetManagerCompat.getInstance(context);
        this.mPackageManager = context.getPackageManager();
    }

    private ComponentKey getComponentKey(Object obj) {
        if (obj instanceof LauncherAppWidgetProviderInfo) {
            LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo = (LauncherAppWidgetProviderInfo) obj;
            return new ComponentKey(launcherAppWidgetProviderInfo.provider, this.mManager.getUser(launcherAppWidgetProviderInfo));
        }
        ResolveInfo resolveInfo = (ResolveInfo) obj;
        return new ComponentKey(new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name), UserHandleCompat.myUserHandle());
    }

    private String getLabel(Object obj) {
        if (obj instanceof LauncherAppWidgetProviderInfo) {
            return Utilities.trim(this.mManager.loadLabel((LauncherAppWidgetProviderInfo) obj));
        }
        try {
            return Utilities.trim(((ResolveInfo) obj).loadLabel(this.mPackageManager));
        } catch (Exception e) {
            Log.e("ShortcutNameComparator", "Failed to extract app display name from resolve info", e);
            return "";
        }
    }

    @Override // java.util.Comparator
    public final int compare(Object obj, Object obj2) {
        ComponentKey componentKey = getComponentKey(obj);
        ComponentKey componentKey2 = getComponentKey(obj2);
        boolean z = !this.mMainHandle.equals(componentKey.user);
        boolean z2 = !this.mMainHandle.equals(componentKey2.user);
        if (!z || z2) {
            if (z || !z2) {
                String str = this.mLabelCache.get(componentKey);
                String str2 = this.mLabelCache.get(componentKey2);
                String str3 = str;
                if (str == null) {
                    str3 = getLabel(obj);
                    this.mLabelCache.put(componentKey, str3);
                }
                String str4 = str2;
                if (str2 == null) {
                    str4 = getLabel(obj2);
                    this.mLabelCache.put(componentKey2, str4);
                }
                return this.mCollator.compare(str3, str4);
            }
            return -1;
        }
        return 1;
    }

    public void reset() {
        this.mLabelCache.clear();
    }
}

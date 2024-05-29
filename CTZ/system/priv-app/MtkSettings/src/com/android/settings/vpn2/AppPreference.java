package com.android.settings.vpn2;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;
import android.support.v7.preference.Preference;
import com.android.internal.net.VpnConfig;
/* loaded from: classes.dex */
public class AppPreference extends ManageablePreference {
    public static final int STATE_DISCONNECTED = STATE_NONE;
    private final String mName;
    private final String mPackageName;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public AppPreference(Context context, int i, String str) {
        super(context, null);
        Drawable drawable;
        String str2;
        Drawable drawable2 = null;
        super.setUserId(i);
        this.mPackageName = str;
        try {
            Context userContext = getUserContext();
            PackageManager packageManager = userContext.getPackageManager();
            try {
                PackageInfo packageInfo = packageManager.getPackageInfo(this.mPackageName, 0);
                if (packageInfo != null) {
                    Drawable loadIcon = packageInfo.applicationInfo.loadIcon(packageManager);
                    try {
                        str2 = VpnConfig.getVpnLabel(userContext, this.mPackageName).toString();
                        drawable2 = loadIcon;
                    } catch (PackageManager.NameNotFoundException e) {
                        drawable2 = loadIcon;
                    }
                } else {
                    str2 = str;
                }
                str = str2;
            } catch (PackageManager.NameNotFoundException e2) {
            }
            if (drawable2 == null) {
                drawable = packageManager.getDefaultActivityIcon();
            } else {
                drawable = drawable2;
            }
        } catch (PackageManager.NameNotFoundException e3) {
            drawable = drawable2;
        }
        this.mName = str;
        setTitle(this.mName);
        setIcon(drawable);
    }

    public PackageInfo getPackageInfo() {
        try {
            return getUserContext().getPackageManager().getPackageInfo(this.mPackageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public String getLabel() {
        return this.mName;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    private Context getUserContext() throws PackageManager.NameNotFoundException {
        return getContext().createPackageContextAsUser(getContext().getPackageName(), 0, UserHandle.of(this.mUserId));
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // android.support.v7.preference.Preference, java.lang.Comparable
    public int compareTo(Preference preference) {
        if (preference instanceof AppPreference) {
            AppPreference appPreference = (AppPreference) preference;
            int i = appPreference.mState - this.mState;
            if (i == 0) {
                int compareToIgnoreCase = this.mName.compareToIgnoreCase(appPreference.mName);
                if (compareToIgnoreCase == 0) {
                    int compareTo = this.mPackageName.compareTo(appPreference.mPackageName);
                    if (compareTo == 0) {
                        return this.mUserId - appPreference.mUserId;
                    }
                    return compareTo;
                }
                return compareToIgnoreCase;
            }
            return i;
        } else if (preference instanceof LegacyVpnPreference) {
            return -((LegacyVpnPreference) preference).compareTo((Preference) this);
        } else {
            return super.compareTo(preference);
        }
    }
}

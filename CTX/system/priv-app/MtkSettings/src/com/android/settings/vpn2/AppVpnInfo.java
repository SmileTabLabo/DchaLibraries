package com.android.settings.vpn2;

import com.android.internal.util.Preconditions;
import java.util.Objects;
/* loaded from: classes.dex */
class AppVpnInfo implements Comparable {
    public final String packageName;
    public final int userId;

    public AppVpnInfo(int i, String str) {
        this.userId = i;
        this.packageName = (String) Preconditions.checkNotNull(str);
    }

    @Override // java.lang.Comparable
    public int compareTo(Object obj) {
        AppVpnInfo appVpnInfo = (AppVpnInfo) obj;
        int compareTo = this.packageName.compareTo(appVpnInfo.packageName);
        if (compareTo == 0) {
            return appVpnInfo.userId - this.userId;
        }
        return compareTo;
    }

    public boolean equals(Object obj) {
        if (obj instanceof AppVpnInfo) {
            AppVpnInfo appVpnInfo = (AppVpnInfo) obj;
            return this.userId == appVpnInfo.userId && Objects.equals(this.packageName, appVpnInfo.packageName);
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(this.packageName, Integer.valueOf(this.userId));
    }
}

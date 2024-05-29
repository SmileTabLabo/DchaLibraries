package com.android.settings.vpn2;

import com.android.internal.util.Preconditions;
import java.util.Objects;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class AppVpnInfo implements Comparable {
    public final String packageName;
    public final int userId;

    public AppVpnInfo(int userId, String packageName) {
        this.userId = userId;
        this.packageName = (String) Preconditions.checkNotNull(packageName);
    }

    @Override // java.lang.Comparable
    public int compareTo(Object other) {
        AppVpnInfo that = (AppVpnInfo) other;
        int result = this.packageName.compareTo(that.packageName);
        if (result == 0) {
            return that.userId - this.userId;
        }
        return result;
    }

    public boolean equals(Object other) {
        if (other instanceof AppVpnInfo) {
            AppVpnInfo that = (AppVpnInfo) other;
            if (this.userId == that.userId) {
                return Objects.equals(this.packageName, that.packageName);
            }
            return false;
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(this.packageName, Integer.valueOf(this.userId));
    }
}

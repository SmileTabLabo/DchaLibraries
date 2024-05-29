package jp.co.benesse.dcha.util;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.os.UserHandle;
import android.os.UserManager;
/* loaded from: classes.dex */
public class RestrictedLockUtils {
    public static void sendShowAdminSupportDetailsIntent(Context context, EnforcedAdmin enforcedAdmin) {
        Intent showAdminSupportDetailsIntent = getShowAdminSupportDetailsIntent(context, enforcedAdmin);
        int myUserId = UserHandle.myUserId();
        if (enforcedAdmin != null && enforcedAdmin.userId != -10000 && isCurrentUserOrProfile(context, enforcedAdmin.userId)) {
            myUserId = enforcedAdmin.userId;
        }
        context.startActivityAsUser(showAdminSupportDetailsIntent, new UserHandle(myUserId));
    }

    private static Intent getShowAdminSupportDetailsIntent(Context context, EnforcedAdmin enforcedAdmin) {
        Intent intent = new Intent("android.settings.SHOW_ADMIN_SUPPORT_DETAILS");
        if (enforcedAdmin != null) {
            if (enforcedAdmin.component != null) {
                intent.putExtra("android.app.extra.DEVICE_ADMIN", enforcedAdmin.component);
            }
            int myUserId = UserHandle.myUserId();
            if (enforcedAdmin.userId != -10000) {
                myUserId = enforcedAdmin.userId;
            }
            intent.putExtra("android.intent.extra.USER_ID", myUserId);
        }
        return intent;
    }

    public static boolean isCurrentUserOrProfile(Context context, int i) {
        for (UserInfo userInfo : UserManager.get(context).getProfiles(UserHandle.myUserId())) {
            if (userInfo.id == i) {
                return true;
            }
        }
        return false;
    }

    public static EnforcedAdmin getDeviceOwner(Context context) {
        ComponentName deviceOwnerComponentOnAnyUser;
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService("device_policy");
        if (devicePolicyManager == null || (deviceOwnerComponentOnAnyUser = devicePolicyManager.getDeviceOwnerComponentOnAnyUser()) == null) {
            return null;
        }
        return new EnforcedAdmin(deviceOwnerComponentOnAnyUser, devicePolicyManager.getDeviceOwnerUserId());
    }

    /* loaded from: classes.dex */
    public static class EnforcedAdmin {
        public static final EnforcedAdmin MULTIPLE_ENFORCED_ADMIN = new EnforcedAdmin();
        public ComponentName component;
        public int userId;

        public EnforcedAdmin(ComponentName componentName, int i) {
            this.component = null;
            this.userId = -10000;
            this.component = componentName;
            this.userId = i;
        }

        public EnforcedAdmin() {
            this.component = null;
            this.userId = -10000;
        }

        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof EnforcedAdmin) {
                EnforcedAdmin enforcedAdmin = (EnforcedAdmin) obj;
                if (this.userId != enforcedAdmin.userId) {
                    return false;
                }
                return (this.component == null && enforcedAdmin.component == null) || (this.component != null && this.component.equals(enforcedAdmin.component));
            }
            return false;
        }

        public String toString() {
            return "EnforcedAdmin{component=" + this.component + ",userId=" + this.userId + "}";
        }
    }
}

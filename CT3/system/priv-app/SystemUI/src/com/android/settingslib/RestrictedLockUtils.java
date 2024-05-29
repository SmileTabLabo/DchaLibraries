package com.android.settingslib;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.os.BenesseExtension;
import android.os.UserHandle;
import android.os.UserManager;
/* loaded from: a.zip:com/android/settingslib/RestrictedLockUtils.class */
public class RestrictedLockUtils {

    /* loaded from: a.zip:com/android/settingslib/RestrictedLockUtils$EnforcedAdmin.class */
    public static class EnforcedAdmin {
        public static final EnforcedAdmin MULTIPLE_ENFORCED_ADMIN = new EnforcedAdmin();
        public ComponentName component;
        public int userId;

        public EnforcedAdmin() {
            this.component = null;
            this.userId = -10000;
        }

        public EnforcedAdmin(ComponentName componentName, int i) {
            this.component = null;
            this.userId = -10000;
            this.component = componentName;
            this.userId = i;
        }

        public EnforcedAdmin(EnforcedAdmin enforcedAdmin) {
            this.component = null;
            this.userId = -10000;
            if (enforcedAdmin == null) {
                throw new IllegalArgumentException();
            }
            this.component = enforcedAdmin.component;
            this.userId = enforcedAdmin.userId;
        }

        public void copyTo(EnforcedAdmin enforcedAdmin) {
            if (enforcedAdmin == null) {
                throw new IllegalArgumentException();
            }
            enforcedAdmin.component = this.component;
            enforcedAdmin.userId = this.userId;
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
                if (this.component == null && enforcedAdmin.component == null) {
                    return true;
                }
                return this.component != null && this.component.equals(enforcedAdmin.component);
            }
            return false;
        }

        public String toString() {
            return "EnforcedAdmin{component=" + this.component + ",userId=" + this.userId + "}";
        }
    }

    public static EnforcedAdmin checkIfRestrictionEnforced(Context context, String str, int i) {
        int userRestrictionSource;
        if (((DevicePolicyManager) context.getSystemService("device_policy")) == null || (userRestrictionSource = UserManager.get(context).getUserRestrictionSource(str, UserHandle.of(i))) == 0 || userRestrictionSource == 1) {
            return null;
        }
        boolean z = (userRestrictionSource & 4) != 0;
        boolean z2 = (userRestrictionSource & 2) != 0;
        if (z) {
            return getProfileOwner(context, i);
        }
        if (z2) {
            EnforcedAdmin deviceOwner = getDeviceOwner(context);
            if (deviceOwner.userId != i) {
                deviceOwner = EnforcedAdmin.MULTIPLE_ENFORCED_ADMIN;
            }
            return deviceOwner;
        }
        return null;
    }

    public static EnforcedAdmin getDeviceOwner(Context context) {
        ComponentName deviceOwnerComponentOnAnyUser;
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService("device_policy");
        if (devicePolicyManager == null || (deviceOwnerComponentOnAnyUser = devicePolicyManager.getDeviceOwnerComponentOnAnyUser()) == null) {
            return null;
        }
        return new EnforcedAdmin(deviceOwnerComponentOnAnyUser, devicePolicyManager.getDeviceOwnerUserId());
    }

    private static EnforcedAdmin getProfileOwner(Context context, int i) {
        DevicePolicyManager devicePolicyManager;
        ComponentName profileOwnerAsUser;
        if (i == -10000 || (devicePolicyManager = (DevicePolicyManager) context.getSystemService("device_policy")) == null || (profileOwnerAsUser = devicePolicyManager.getProfileOwnerAsUser(i)) == null) {
            return null;
        }
        return new EnforcedAdmin(profileOwnerAsUser, i);
    }

    public static Intent getShowAdminSupportDetailsIntent(Context context, EnforcedAdmin enforcedAdmin) {
        if (BenesseExtension.getDchaState() != 0) {
            return null;
        }
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

    public static boolean hasBaseUserRestriction(Context context, String str, int i) {
        return ((UserManager) context.getSystemService("user")).hasBaseUserRestriction(str, UserHandle.of(i));
    }

    public static boolean isCurrentUserOrProfile(Context context, int i) {
        for (UserInfo userInfo : UserManager.get(context).getProfiles(UserHandle.myUserId())) {
            if (userInfo.id == i) {
                return true;
            }
        }
        return false;
    }

    public static void sendShowAdminSupportDetailsIntent(Context context, EnforcedAdmin enforcedAdmin) {
        Intent showAdminSupportDetailsIntent = getShowAdminSupportDetailsIntent(context, enforcedAdmin);
        if (showAdminSupportDetailsIntent == null) {
            return;
        }
        int myUserId = UserHandle.myUserId();
        int i = myUserId;
        if (enforcedAdmin != null) {
            i = myUserId;
            if (enforcedAdmin.userId != -10000) {
                i = myUserId;
                if (isCurrentUserOrProfile(context, enforcedAdmin.userId)) {
                    i = enforcedAdmin.userId;
                }
            }
        }
        context.startActivityAsUser(showAdminSupportDetailsIntent, new UserHandle(i));
    }
}

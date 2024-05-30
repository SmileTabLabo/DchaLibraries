package com.android.settings.applications;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import java.util.Set;
/* loaded from: classes.dex */
public class AppPermissionsPreferenceController extends BasePreferenceController {
    private static final String KEY_APP_PERMISSION_GROUPS = "manage_perms";
    private static final int NUM_PERMISSION_TO_USE = 3;
    private static final String[] PERMISSION_GROUPS = {"android.permission-group.LOCATION", "android.permission-group.MICROPHONE", "android.permission-group.CAMERA", "android.permission-group.SMS", "android.permission-group.CONTACTS", "android.permission-group.PHONE"};
    private static final String TAG = "AppPermissionPrefCtrl";
    private final PackageManager mPackageManager;

    public AppPermissionsPreferenceController(Context context) {
        super(context, KEY_APP_PERMISSION_GROUPS);
        this.mPackageManager = context.getPackageManager();
    }

    @Override // com.android.settings.core.BasePreferenceController
    public int getAvailabilityStatus() {
        return 0;
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public CharSequence getSummary() {
        String[] strArr;
        Set<String> grantedPermissionGroups = getGrantedPermissionGroups(getAllPermissionsInGroups());
        CharSequence charSequence = null;
        int i = 0;
        for (String str : PERMISSION_GROUPS) {
            if (grantedPermissionGroups.contains(str)) {
                charSequence = concatSummaryText(charSequence, str);
                i++;
                if (i >= 3) {
                    break;
                }
            }
        }
        if (i > 0) {
            return this.mContext.getString(R.string.app_permissions_summary, charSequence);
        }
        return null;
    }

    private Set<String> getGrantedPermissionGroups(Set<String> set) {
        PermissionInfo[] permissionInfoArr;
        ArraySet arraySet = new ArraySet();
        for (PackageInfo packageInfo : this.mPackageManager.getInstalledPackages(4096)) {
            if (packageInfo.permissions != null) {
                for (PermissionInfo permissionInfo : packageInfo.permissions) {
                    if (set.contains(permissionInfo.name) && !arraySet.contains(permissionInfo.group)) {
                        arraySet.add(permissionInfo.group);
                    }
                }
            }
        }
        return arraySet;
    }

    private CharSequence concatSummaryText(CharSequence charSequence, String str) {
        String lowerCase = getPermissionGroupLabel(str).toString().toLowerCase();
        return TextUtils.isEmpty(charSequence) ? lowerCase : this.mContext.getString(R.string.join_many_items_middle, charSequence, lowerCase);
    }

    private CharSequence getPermissionGroupLabel(String str) {
        try {
            return this.mPackageManager.getPermissionGroupInfo(str, 0).loadLabel(this.mPackageManager);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Error getting permissions label.", e);
            return str;
        }
    }

    private Set<String> getAllPermissionsInGroups() {
        String[] strArr;
        ArraySet arraySet = new ArraySet();
        for (String str : PERMISSION_GROUPS) {
            try {
                for (PermissionInfo permissionInfo : this.mPackageManager.queryPermissionsByGroup(str, 0)) {
                    arraySet.add(permissionInfo.name);
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "Error getting permissions in group " + str, e);
            }
        }
        return arraySet;
    }
}

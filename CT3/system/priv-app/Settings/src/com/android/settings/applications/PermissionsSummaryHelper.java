package com.android.settings.applications;

import android.content.Context;
import android.content.pm.permission.RuntimePermissionPresentationInfo;
import android.content.pm.permission.RuntimePermissionPresenter;
import android.os.Handler;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/* loaded from: classes.dex */
public class PermissionsSummaryHelper {
    public static void getPermissionSummary(Context context, String pkg, final PermissionsResultCallback callback) {
        RuntimePermissionPresenter presenter = RuntimePermissionPresenter.getInstance(context);
        presenter.getAppPermissions(pkg, new RuntimePermissionPresenter.OnResultCallback() { // from class: com.android.settings.applications.PermissionsSummaryHelper.1
            public void onGetAppPermissions(List<RuntimePermissionPresentationInfo> permissions) {
                int permissionCount = permissions.size();
                int grantedStandardCount = 0;
                int grantedAdditionalCount = 0;
                int requestedCount = 0;
                List<CharSequence> grantedStandardLabels = new ArrayList<>();
                for (int i = 0; i < permissionCount; i++) {
                    RuntimePermissionPresentationInfo permission = permissions.get(i);
                    requestedCount++;
                    if (permission.isGranted()) {
                        if (permission.isStandard()) {
                            grantedStandardLabels.add(permission.getLabel());
                            grantedStandardCount++;
                        } else {
                            grantedAdditionalCount++;
                        }
                    }
                }
                Collator collator = Collator.getInstance();
                collator.setStrength(0);
                Collections.sort(grantedStandardLabels, collator);
                PermissionsResultCallback.this.onPermissionSummaryResult(grantedStandardCount, requestedCount, grantedAdditionalCount, grantedStandardLabels);
            }
        }, (Handler) null);
    }

    /* loaded from: classes.dex */
    public static abstract class PermissionsResultCallback {
        public void onPermissionSummaryResult(int standardGrantedPermissionCount, int requestedPermissionCount, int additionalGrantedPermissionCount, List<CharSequence> grantedGroupLabels) {
        }
    }
}

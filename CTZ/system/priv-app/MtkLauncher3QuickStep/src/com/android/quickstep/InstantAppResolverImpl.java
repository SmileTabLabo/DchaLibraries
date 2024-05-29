package com.android.quickstep;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.InstantAppInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import com.android.launcher3.AppInfo;
import com.android.launcher3.util.InstantAppResolver;
import java.util.ArrayList;
import java.util.List;
/* loaded from: classes.dex */
public class InstantAppResolverImpl extends InstantAppResolver {
    public static final String COMPONENT_CLASS_MARKER = "@instantapp";
    private static final String TAG = "InstantAppResolverImpl";
    private final PackageManager mPM;

    public InstantAppResolverImpl(Context context) throws NoSuchMethodException, ClassNotFoundException {
        this.mPM = context.getPackageManager();
    }

    @Override // com.android.launcher3.util.InstantAppResolver
    public boolean isInstantApp(ApplicationInfo applicationInfo) {
        return applicationInfo.isInstantApp();
    }

    @Override // com.android.launcher3.util.InstantAppResolver
    public boolean isInstantApp(AppInfo appInfo) {
        ComponentName targetComponent = appInfo.getTargetComponent();
        return targetComponent != null && targetComponent.getClassName().equals(COMPONENT_CLASS_MARKER);
    }

    @Override // com.android.launcher3.util.InstantAppResolver
    public List<ApplicationInfo> getInstantApps() {
        try {
            ArrayList arrayList = new ArrayList();
            for (InstantAppInfo instantAppInfo : this.mPM.getInstantApps()) {
                ApplicationInfo applicationInfo = instantAppInfo.getApplicationInfo();
                if (applicationInfo != null) {
                    arrayList.add(applicationInfo);
                }
            }
            return arrayList;
        } catch (SecurityException e) {
            Log.w(TAG, "getInstantApps failed. Launcher may not be the default home app.", e);
            return super.getInstantApps();
        } catch (Exception e2) {
            Log.e(TAG, "Error calling API: getInstantApps", e2);
            return super.getInstantApps();
        }
    }
}

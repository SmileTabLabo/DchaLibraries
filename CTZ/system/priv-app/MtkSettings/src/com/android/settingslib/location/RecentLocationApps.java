package com.android.settingslib.location;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.IconDrawableFactory;
import android.util.Log;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
/* loaded from: classes.dex */
public class RecentLocationApps {
    static final String ANDROID_SYSTEM_PACKAGE_NAME = "android";
    private final Context mContext;
    private final IconDrawableFactory mDrawableFactory;
    private final PackageManager mPackageManager;
    private static final String TAG = RecentLocationApps.class.getSimpleName();
    static final int[] LOCATION_OPS = {41, 42};

    public RecentLocationApps(Context context) {
        this.mContext = context;
        this.mPackageManager = context.getPackageManager();
        this.mDrawableFactory = IconDrawableFactory.newInstance(context);
    }

    public List<Request> getAppList() {
        Request requestFromOps;
        List packagesForOps = ((AppOpsManager) this.mContext.getSystemService("appops")).getPackagesForOps(LOCATION_OPS);
        int size = packagesForOps != null ? packagesForOps.size() : 0;
        ArrayList arrayList = new ArrayList(size);
        long currentTimeMillis = System.currentTimeMillis();
        List<UserHandle> userProfiles = ((UserManager) this.mContext.getSystemService("user")).getUserProfiles();
        for (int i = 0; i < size; i++) {
            AppOpsManager.PackageOps packageOps = (AppOpsManager.PackageOps) packagesForOps.get(i);
            String packageName = packageOps.getPackageName();
            int uid = packageOps.getUid();
            int userId = UserHandle.getUserId(uid);
            if (!(uid == 1000 && ANDROID_SYSTEM_PACKAGE_NAME.equals(packageName)) && userProfiles.contains(new UserHandle(userId)) && (requestFromOps = getRequestFromOps(currentTimeMillis, packageOps)) != null) {
                arrayList.add(requestFromOps);
            }
        }
        return arrayList;
    }

    public List<Request> getAppListSorted() {
        List<Request> appList = getAppList();
        Collections.sort(appList, Collections.reverseOrder(new Comparator<Request>() { // from class: com.android.settingslib.location.RecentLocationApps.1
            @Override // java.util.Comparator
            public int compare(Request request, Request request2) {
                return Long.compare(request.requestFinishTime, request2.requestFinishTime);
            }
        }));
        return appList;
    }

    private Request getRequestFromOps(long j, AppOpsManager.PackageOps packageOps) {
        String packageName = packageOps.getPackageName();
        long j2 = j - 86400000;
        boolean z = false;
        long j3 = 0;
        boolean z2 = false;
        for (AppOpsManager.OpEntry opEntry : packageOps.getOps()) {
            if (opEntry.isRunning() || opEntry.getTime() >= j2) {
                j3 = opEntry.getTime() + opEntry.getDuration();
                switch (opEntry.getOp()) {
                    case 41:
                        z = true;
                        continue;
                    case 42:
                        z2 = true;
                        continue;
                }
            }
        }
        if (!z2 && !z) {
            if (Log.isLoggable(TAG, 2)) {
                Log.v(TAG, packageName + " hadn't used location within the time interval.");
            }
            return null;
        }
        int userId = UserHandle.getUserId(packageOps.getUid());
        try {
            ApplicationInfo applicationInfoAsUser = this.mPackageManager.getApplicationInfoAsUser(packageName, 128, userId);
            if (applicationInfoAsUser == null) {
                Log.w(TAG, "Null application info retrieved for package " + packageName + ", userId " + userId);
                return null;
            }
            UserHandle userHandle = new UserHandle(userId);
            Drawable badgedIcon = this.mDrawableFactory.getBadgedIcon(applicationInfoAsUser, userId);
            CharSequence applicationLabel = this.mPackageManager.getApplicationLabel(applicationInfoAsUser);
            CharSequence userBadgedLabel = this.mPackageManager.getUserBadgedLabel(applicationLabel, userHandle);
            return new Request(packageName, userHandle, badgedIcon, applicationLabel, z2, applicationLabel.toString().contentEquals(userBadgedLabel) ? null : userBadgedLabel, j3);
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "package name not found for " + packageName + ", userId " + userId);
            return null;
        }
    }

    /* loaded from: classes.dex */
    public static class Request {
        public final CharSequence contentDescription;
        public final Drawable icon;
        public final boolean isHighBattery;
        public final CharSequence label;
        public final String packageName;
        public final long requestFinishTime;
        public final UserHandle userHandle;

        private Request(String str, UserHandle userHandle, Drawable drawable, CharSequence charSequence, boolean z, CharSequence charSequence2, long j) {
            this.packageName = str;
            this.userHandle = userHandle;
            this.icon = drawable;
            this.label = charSequence;
            this.isHighBattery = z;
            this.contentDescription = charSequence2;
            this.requestFinishTime = j;
        }
    }
}

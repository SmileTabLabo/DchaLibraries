package com.android.settings.applications.appops;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.SparseArray;
import com.android.settings.R;
import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
/* loaded from: classes.dex */
public class AppOpsState {
    final AppOpsManager mAppOps;
    final Context mContext;
    final CharSequence[] mOpLabels;
    final CharSequence[] mOpSummaries;
    final PackageManager mPm;
    public static final OpsTemplate LOCATION_TEMPLATE = new OpsTemplate(new int[]{0, 1, 2, 10, 12, 41, 42}, new boolean[]{true, true, false, false, false, false, false});
    public static final OpsTemplate PERSONAL_TEMPLATE = new OpsTemplate(new int[]{4, 5, 6, 7, 8, 9, 29, 30}, new boolean[]{true, true, true, true, true, true, false, false});
    public static final OpsTemplate MESSAGING_TEMPLATE = new OpsTemplate(new int[]{14, 16, 17, 18, 19, 15, 20, 21, 22}, new boolean[]{true, true, true, true, true, true, true, true, true});
    public static final OpsTemplate MEDIA_TEMPLATE = new OpsTemplate(new int[]{3, 26, 27, 28, 31, 32, 33, 34, 35, 36, 37, 38, 39, 64, 44}, new boolean[]{false, true, true, false, false, false, false, false, false, false, false, false, false, false});
    public static final OpsTemplate DEVICE_TEMPLATE = new OpsTemplate(new int[]{11, 25, 13, 23, 24, 40, 46, 47, 49, 50}, new boolean[]{false, true, true, true, true, true, false, false, false, false});
    public static final OpsTemplate RUN_IN_BACKGROUND_TEMPLATE = new OpsTemplate(new int[]{63}, new boolean[]{false});
    public static final OpsTemplate[] ALL_TEMPLATES = {LOCATION_TEMPLATE, PERSONAL_TEMPLATE, MESSAGING_TEMPLATE, MEDIA_TEMPLATE, DEVICE_TEMPLATE, RUN_IN_BACKGROUND_TEMPLATE};
    public static final Comparator<AppOpEntry> RECENCY_COMPARATOR = new Comparator<AppOpEntry>() { // from class: com.android.settings.applications.appops.AppOpsState.1
        private final Collator sCollator = Collator.getInstance();

        @Override // java.util.Comparator
        public int compare(AppOpEntry appOpEntry, AppOpEntry appOpEntry2) {
            if (appOpEntry.getSwitchOrder() != appOpEntry2.getSwitchOrder()) {
                return appOpEntry.getSwitchOrder() < appOpEntry2.getSwitchOrder() ? -1 : 1;
            } else if (appOpEntry.isRunning() != appOpEntry2.isRunning()) {
                return appOpEntry.isRunning() ? -1 : 1;
            } else if (appOpEntry.getTime() != appOpEntry2.getTime()) {
                return appOpEntry.getTime() > appOpEntry2.getTime() ? -1 : 1;
            } else {
                return this.sCollator.compare(appOpEntry.getAppEntry().getLabel(), appOpEntry2.getAppEntry().getLabel());
            }
        }
    };
    public static final Comparator<AppOpEntry> LABEL_COMPARATOR = new Comparator<AppOpEntry>() { // from class: com.android.settings.applications.appops.AppOpsState.2
        private final Collator sCollator = Collator.getInstance();

        @Override // java.util.Comparator
        public int compare(AppOpEntry appOpEntry, AppOpEntry appOpEntry2) {
            return this.sCollator.compare(appOpEntry.getAppEntry().getLabel(), appOpEntry2.getAppEntry().getLabel());
        }
    };

    public AppOpsState(Context context) {
        this.mContext = context;
        this.mAppOps = (AppOpsManager) context.getSystemService("appops");
        this.mPm = context.getPackageManager();
        this.mOpSummaries = context.getResources().getTextArray(R.array.app_ops_summaries);
        this.mOpLabels = context.getResources().getTextArray(R.array.app_ops_labels);
    }

    /* loaded from: classes.dex */
    public static class OpsTemplate implements Parcelable {
        public static final Parcelable.Creator<OpsTemplate> CREATOR = new Parcelable.Creator<OpsTemplate>() { // from class: com.android.settings.applications.appops.AppOpsState.OpsTemplate.1
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.Creator
            public OpsTemplate createFromParcel(Parcel parcel) {
                return new OpsTemplate(parcel);
            }

            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.Creator
            public OpsTemplate[] newArray(int i) {
                return new OpsTemplate[i];
            }
        };
        public final int[] ops;
        public final boolean[] showPerms;

        public OpsTemplate(int[] iArr, boolean[] zArr) {
            this.ops = iArr;
            this.showPerms = zArr;
        }

        OpsTemplate(Parcel parcel) {
            this.ops = parcel.createIntArray();
            this.showPerms = parcel.createBooleanArray();
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeIntArray(this.ops);
            parcel.writeBooleanArray(this.showPerms);
        }
    }

    /* loaded from: classes.dex */
    public static class AppEntry {
        private final File mApkFile;
        private Drawable mIcon;
        private final ApplicationInfo mInfo;
        private String mLabel;
        private boolean mMounted;
        private final AppOpsState mState;
        private final SparseArray<AppOpsManager.OpEntry> mOps = new SparseArray<>();
        private final SparseArray<AppOpEntry> mOpSwitches = new SparseArray<>();

        public AppEntry(AppOpsState appOpsState, ApplicationInfo applicationInfo) {
            this.mState = appOpsState;
            this.mInfo = applicationInfo;
            this.mApkFile = new File(applicationInfo.sourceDir);
        }

        public void addOp(AppOpEntry appOpEntry, AppOpsManager.OpEntry opEntry) {
            this.mOps.put(opEntry.getOp(), opEntry);
            this.mOpSwitches.put(AppOpsManager.opToSwitch(opEntry.getOp()), appOpEntry);
        }

        public boolean hasOp(int i) {
            return this.mOps.indexOfKey(i) >= 0;
        }

        public AppOpEntry getOpSwitch(int i) {
            return this.mOpSwitches.get(AppOpsManager.opToSwitch(i));
        }

        public ApplicationInfo getApplicationInfo() {
            return this.mInfo;
        }

        public String getLabel() {
            return this.mLabel;
        }

        public Drawable getIcon() {
            if (this.mIcon == null) {
                if (this.mApkFile.exists()) {
                    this.mIcon = this.mInfo.loadIcon(this.mState.mPm);
                    return this.mIcon;
                }
                this.mMounted = false;
            } else if (!this.mMounted) {
                if (this.mApkFile.exists()) {
                    this.mMounted = true;
                    this.mIcon = this.mInfo.loadIcon(this.mState.mPm);
                    return this.mIcon;
                }
            } else {
                return this.mIcon;
            }
            return this.mState.mContext.getDrawable(17301651);
        }

        public String toString() {
            return this.mLabel;
        }

        void loadLabel(Context context) {
            if (this.mLabel == null || !this.mMounted) {
                if (!this.mApkFile.exists()) {
                    this.mMounted = false;
                    this.mLabel = this.mInfo.packageName;
                    return;
                }
                this.mMounted = true;
                CharSequence loadLabel = this.mInfo.loadLabel(context.getPackageManager());
                this.mLabel = loadLabel != null ? loadLabel.toString() : this.mInfo.packageName;
            }
        }
    }

    /* loaded from: classes.dex */
    public static class AppOpEntry {
        private final AppEntry mApp;
        private final AppOpsManager.PackageOps mPkgOps;
        private final int mSwitchOrder;
        private final ArrayList<AppOpsManager.OpEntry> mOps = new ArrayList<>();
        private final ArrayList<AppOpsManager.OpEntry> mSwitchOps = new ArrayList<>();
        private int mOverriddenPrimaryMode = -1;

        public AppOpEntry(AppOpsManager.PackageOps packageOps, AppOpsManager.OpEntry opEntry, AppEntry appEntry, int i) {
            this.mPkgOps = packageOps;
            this.mApp = appEntry;
            this.mSwitchOrder = i;
            this.mApp.addOp(this, opEntry);
            this.mOps.add(opEntry);
            this.mSwitchOps.add(opEntry);
        }

        private static void addOp(ArrayList<AppOpsManager.OpEntry> arrayList, AppOpsManager.OpEntry opEntry) {
            for (int i = 0; i < arrayList.size(); i++) {
                AppOpsManager.OpEntry opEntry2 = arrayList.get(i);
                if (opEntry2.isRunning() != opEntry.isRunning()) {
                    if (opEntry.isRunning()) {
                        arrayList.add(i, opEntry);
                        return;
                    }
                } else if (opEntry2.getTime() < opEntry.getTime()) {
                    arrayList.add(i, opEntry);
                    return;
                }
            }
            arrayList.add(opEntry);
        }

        public void addOp(AppOpsManager.OpEntry opEntry) {
            this.mApp.addOp(this, opEntry);
            addOp(this.mOps, opEntry);
            if (this.mApp.getOpSwitch(AppOpsManager.opToSwitch(opEntry.getOp())) == null) {
                addOp(this.mSwitchOps, opEntry);
            }
        }

        public AppEntry getAppEntry() {
            return this.mApp;
        }

        public int getSwitchOrder() {
            return this.mSwitchOrder;
        }

        public AppOpsManager.OpEntry getOpEntry(int i) {
            return this.mOps.get(i);
        }

        public int getPrimaryOpMode() {
            return this.mOverriddenPrimaryMode >= 0 ? this.mOverriddenPrimaryMode : this.mOps.get(0).getMode();
        }

        public void overridePrimaryOpMode(int i) {
            this.mOverriddenPrimaryMode = i;
        }

        public CharSequence getTimeText(Resources resources, boolean z) {
            if (isRunning()) {
                return resources.getText(R.string.app_ops_running);
            }
            if (getTime() > 0) {
                return DateUtils.getRelativeTimeSpanString(getTime(), System.currentTimeMillis(), 60000L, 262144);
            }
            return z ? resources.getText(R.string.app_ops_never_used) : "";
        }

        public boolean isRunning() {
            return this.mOps.get(0).isRunning();
        }

        public long getTime() {
            return this.mOps.get(0).getTime();
        }

        public String toString() {
            return this.mApp.getLabel();
        }
    }

    private void addOp(List<AppOpEntry> list, AppOpsManager.PackageOps packageOps, AppEntry appEntry, AppOpsManager.OpEntry opEntry, boolean z, int i) {
        if (z && list.size() > 0) {
            AppOpEntry appOpEntry = list.get(list.size() - 1);
            if (appOpEntry.getAppEntry() == appEntry) {
                if ((appOpEntry.getTime() != 0) == (opEntry.getTime() != 0)) {
                    appOpEntry.addOp(opEntry);
                    return;
                }
            }
        }
        AppOpEntry opSwitch = appEntry.getOpSwitch(opEntry.getOp());
        if (opSwitch != null) {
            opSwitch.addOp(opEntry);
        } else {
            list.add(new AppOpEntry(packageOps, opEntry, appEntry, i));
        }
    }

    public AppOpsManager getAppOpsManager() {
        return this.mAppOps;
    }

    private AppEntry getAppEntry(Context context, HashMap<String, AppEntry> hashMap, String str, ApplicationInfo applicationInfo) {
        AppEntry appEntry = hashMap.get(str);
        if (appEntry == null) {
            if (applicationInfo == null) {
                try {
                    applicationInfo = this.mPm.getApplicationInfo(str, 4194816);
                } catch (PackageManager.NameNotFoundException e) {
                    Log.w("AppOpsState", "Unable to find info for package " + str);
                    return null;
                }
            }
            AppEntry appEntry2 = new AppEntry(this, applicationInfo);
            appEntry2.loadLabel(context);
            hashMap.put(str, appEntry2);
            return appEntry2;
        }
        return appEntry;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v18 */
    /* JADX WARN: Type inference failed for: r0v19, types: [java.util.List] */
    public List<AppOpEntry> buildState(OpsTemplate opsTemplate, int i, String str, Comparator<AppOpEntry> comparator) {
        List packagesForOps;
        List<PackageInfo> packagesHoldingPermissions;
        int i2;
        int i3;
        AppEntry appEntry;
        PackageInfo packageInfo;
        int i4;
        ArrayList arrayList;
        ApplicationInfo applicationInfo;
        int i5;
        char c;
        String opToPermission;
        Context context = this.mContext;
        HashMap<String, AppEntry> hashMap = new HashMap<>();
        ArrayList arrayList2 = new ArrayList();
        ArrayList arrayList3 = new ArrayList();
        ArrayList arrayList4 = new ArrayList();
        int[] iArr = new int[78];
        char c2 = 0;
        for (int i6 = 0; i6 < opsTemplate.ops.length; i6++) {
            if (opsTemplate.showPerms[i6] && (opToPermission = AppOpsManager.opToPermission(opsTemplate.ops[i6])) != null && !arrayList3.contains(opToPermission)) {
                arrayList3.add(opToPermission);
                arrayList4.add(Integer.valueOf(opsTemplate.ops[i6]));
                iArr[opsTemplate.ops[i6]] = i6;
            }
        }
        if (str != null) {
            packagesForOps = this.mAppOps.getOpsForPackage(i, str, opsTemplate.ops);
        } else {
            packagesForOps = this.mAppOps.getPackagesForOps(opsTemplate.ops);
        }
        ApplicationInfo applicationInfo2 = null;
        if (packagesForOps != null) {
            int i7 = 0;
            while (i7 < packagesForOps.size()) {
                AppOpsManager.PackageOps packageOps = (AppOpsManager.PackageOps) packagesForOps.get(i7);
                AppEntry appEntry2 = getAppEntry(context, hashMap, packageOps.getPackageName(), applicationInfo2);
                if (appEntry2 != null) {
                    int i8 = c2;
                    while (i8 < packageOps.getOps().size()) {
                        AppOpsManager.OpEntry opEntry = (AppOpsManager.OpEntry) packageOps.getOps().get(i8);
                        char c3 = str == null ? true : c2;
                        if (str == null) {
                            c = c2;
                        } else {
                            c = iArr[opEntry.getOp()];
                        }
                        addOp(arrayList2, packageOps, appEntry2, opEntry, c3, c);
                        i8++;
                        c2 = c2;
                        applicationInfo2 = applicationInfo2;
                        packageOps = packageOps;
                        i7 = i7;
                        arrayList2 = arrayList2;
                    }
                }
                i7++;
                c2 = c2;
                applicationInfo2 = applicationInfo2;
                arrayList2 = arrayList2;
            }
        }
        ApplicationInfo applicationInfo3 = applicationInfo2;
        ArrayList arrayList5 = arrayList2;
        char c4 = c2;
        if (str != null) {
            packagesHoldingPermissions = new ArrayList<>();
            try {
                packagesHoldingPermissions.add(this.mPm.getPackageInfo(str, 4096));
            } catch (PackageManager.NameNotFoundException e) {
            }
        } else {
            String[] strArr = new String[arrayList3.size()];
            arrayList3.toArray(strArr);
            packagesHoldingPermissions = this.mPm.getPackagesHoldingPermissions(strArr, c4);
        }
        List<PackageInfo> list = packagesHoldingPermissions;
        int i9 = c4;
        while (i9 < list.size()) {
            PackageInfo packageInfo2 = list.get(i9);
            AppEntry appEntry3 = getAppEntry(context, hashMap, packageInfo2.packageName, packageInfo2.applicationInfo);
            if (appEntry3 != null && packageInfo2.requestedPermissions != null) {
                int i10 = c4;
                ApplicationInfo applicationInfo4 = applicationInfo3;
                ApplicationInfo applicationInfo5 = applicationInfo4;
                while (i10 < packageInfo2.requestedPermissions.length) {
                    if (packageInfo2.requestedPermissionsFlags == null || (packageInfo2.requestedPermissionsFlags[i10] & 2) != 0) {
                        int i11 = c4;
                        while (i11 < arrayList3.size()) {
                            List<PackageInfo> list2 = list;
                            if (!((String) arrayList3.get(i11)).equals(packageInfo2.requestedPermissions[i10]) || appEntry3.hasOp(((Integer) arrayList4.get(i11)).intValue())) {
                                i2 = i11;
                                i3 = i10;
                                appEntry = appEntry3;
                                packageInfo = packageInfo2;
                                i4 = i9;
                            } else {
                                if (applicationInfo4 == null) {
                                    ArrayList arrayList6 = new ArrayList();
                                    applicationInfo = new AppOpsManager.PackageOps(packageInfo2.packageName, packageInfo2.applicationInfo.uid, arrayList6);
                                    arrayList = arrayList6;
                                } else {
                                    arrayList = applicationInfo4;
                                    applicationInfo = applicationInfo5;
                                }
                                AppOpsManager.OpEntry opEntry2 = new AppOpsManager.OpEntry(((Integer) arrayList4.get(i11)).intValue(), 0, 0L, 0L, 0, -1, (String) null);
                                arrayList.add(opEntry2);
                                boolean z = str == null;
                                if (str == null) {
                                    i5 = 0;
                                } else {
                                    i5 = iArr[opEntry2.getOp()];
                                }
                                i2 = i11;
                                i3 = i10;
                                appEntry = appEntry3;
                                packageInfo = packageInfo2;
                                boolean z2 = z;
                                i4 = i9;
                                addOp(arrayList5, applicationInfo, appEntry3, opEntry2, z2, i5);
                                applicationInfo4 = arrayList;
                                applicationInfo5 = applicationInfo;
                            }
                            i11 = i2 + 1;
                            i9 = i4;
                            packageInfo2 = packageInfo;
                            i10 = i3;
                            appEntry3 = appEntry;
                            list = list2;
                        }
                    }
                    i10++;
                    i9 = i9;
                    packageInfo2 = packageInfo2;
                    appEntry3 = appEntry3;
                    list = list;
                    c4 = 0;
                }
            }
            i9++;
            list = list;
            c4 = 0;
        }
        Collections.sort(arrayList5, comparator);
        return arrayList5;
    }
}

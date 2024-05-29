package com.android.launcher3.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import com.android.launcher3.FolderInfo;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.LauncherModel;
import com.android.launcher3.MainThreadExecutor;
import com.android.launcher3.ShortcutInfo;
import com.android.launcher3.Utilities;
import com.android.launcher3.compat.LauncherActivityInfoCompat;
import com.android.launcher3.compat.LauncherAppsCompat;
import com.android.launcher3.compat.UserHandleCompat;
import com.android.launcher3.compat.UserManagerCompat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
@TargetApi(21)
/* loaded from: a.zip:com/android/launcher3/util/ManagedProfileHeuristic.class */
public class ManagedProfileHeuristic {
    private final Context mContext;
    private ArrayList<ShortcutInfo> mHomescreenApps;
    private final LauncherModel mModel = LauncherAppState.getInstance().getModel();
    private final String mPackageSetKey;
    private final SharedPreferences mPrefs;
    private HashMap<ShortcutInfo, Long> mShortcutToInstallTimeMap;
    private final UserHandleCompat mUser;
    private final long mUserCreationTime;
    private final long mUserSerial;
    private ArrayList<ShortcutInfo> mWorkFolderApps;

    private ManagedProfileHeuristic(Context context, UserHandleCompat userHandleCompat) {
        this.mContext = context;
        this.mUser = userHandleCompat;
        UserManagerCompat userManagerCompat = UserManagerCompat.getInstance(context);
        this.mUserSerial = userManagerCompat.getSerialNumberForUser(userHandleCompat);
        this.mUserCreationTime = userManagerCompat.getUserCreationTime(userHandleCompat);
        this.mPackageSetKey = "installed_packages_for_user_" + this.mUserSerial;
        this.mPrefs = this.mContext.getSharedPreferences("com.android.launcher3.managedusers.prefs", 0);
    }

    private static void addAllUserKeys(long j, HashSet<String> hashSet) {
        hashSet.add("installed_packages_for_user_" + j);
        hashSet.add("user_folder_" + j);
    }

    private void finalizeAdditions(boolean z) {
        finalizeWorkFolder();
        if (!z || this.mHomescreenApps.isEmpty()) {
            return;
        }
        sortList(this.mHomescreenApps);
        this.mModel.addAndBindAddedWorkspaceItems(this.mContext, this.mHomescreenApps);
    }

    private void finalizeWorkFolder() {
        if (this.mWorkFolderApps.isEmpty()) {
            return;
        }
        sortList(this.mWorkFolderApps);
        String str = "user_folder_" + this.mUserSerial;
        if (this.mPrefs.contains(str)) {
            long j = this.mPrefs.getLong(str, 0L);
            FolderInfo findFolderById = this.mModel.findFolderById(Long.valueOf(j));
            if (findFolderById == null || !findFolderById.hasOption(2)) {
                this.mHomescreenApps.addAll(this.mWorkFolderApps);
                return;
            }
            saveWorkFolderShortcuts(j, findFolderById.contents.size());
            new MainThreadExecutor().execute(new Runnable(this, this.mWorkFolderApps, findFolderById) { // from class: com.android.launcher3.util.ManagedProfileHeuristic.2
                final ManagedProfileHeuristic this$0;
                final ArrayList val$shortcuts;
                final FolderInfo val$workFolder;

                {
                    this.this$0 = this;
                    this.val$shortcuts = r5;
                    this.val$workFolder = findFolderById;
                }

                @Override // java.lang.Runnable
                public void run() {
                    for (ShortcutInfo shortcutInfo : this.val$shortcuts) {
                        this.val$workFolder.add(shortcutInfo);
                    }
                }
            });
            return;
        }
        FolderInfo folderInfo = new FolderInfo();
        folderInfo.title = this.mContext.getText(2131558406);
        folderInfo.setOption(2, true, null);
        for (ShortcutInfo shortcutInfo : this.mWorkFolderApps) {
            folderInfo.add(shortcutInfo);
        }
        ArrayList<? extends ItemInfo> arrayList = new ArrayList<>(1);
        arrayList.add(folderInfo);
        this.mModel.addAndBindAddedWorkspaceItems(this.mContext, arrayList);
        this.mPrefs.edit().putLong("user_folder_" + this.mUserSerial, folderInfo.id).apply();
        saveWorkFolderShortcuts(folderInfo.id, 0);
    }

    public static ManagedProfileHeuristic get(Context context, UserHandleCompat userHandleCompat) {
        if (!Utilities.ATLEAST_LOLLIPOP || UserHandleCompat.myUserHandle().equals(userHandleCompat)) {
            return null;
        }
        return new ManagedProfileHeuristic(context, userHandleCompat);
    }

    private boolean getUserApps(HashSet<String> hashSet) {
        Set<String> stringSet = this.mPrefs.getStringSet(this.mPackageSetKey, null);
        if (stringSet == null) {
            return false;
        }
        hashSet.addAll(stringSet);
        return true;
    }

    private void initVars() {
        this.mHomescreenApps = new ArrayList<>();
        this.mWorkFolderApps = new ArrayList<>();
        this.mShortcutToInstallTimeMap = new HashMap<>();
    }

    public static void markExistingUsersForNoFolderCreation(Context context) {
        UserManagerCompat userManagerCompat = UserManagerCompat.getInstance(context);
        UserHandleCompat myUserHandle = UserHandleCompat.myUserHandle();
        SharedPreferences sharedPreferences = null;
        for (UserHandleCompat userHandleCompat : userManagerCompat.getUserProfiles()) {
            if (!myUserHandle.equals(userHandleCompat)) {
                SharedPreferences sharedPreferences2 = sharedPreferences;
                if (sharedPreferences == null) {
                    sharedPreferences2 = context.getSharedPreferences("com.android.launcher3.managedusers.prefs", 0);
                }
                String str = "user_folder_" + userManagerCompat.getSerialNumberForUser(userHandleCompat);
                sharedPreferences = sharedPreferences2;
                if (!sharedPreferences2.contains(str)) {
                    sharedPreferences2.edit().putLong(str, -1L).apply();
                    sharedPreferences = sharedPreferences2;
                }
            }
        }
    }

    private void markForAddition(LauncherActivityInfoCompat launcherActivityInfoCompat, long j) {
        ArrayList<ShortcutInfo> arrayList = j <= this.mUserCreationTime + 28800000 ? this.mWorkFolderApps : this.mHomescreenApps;
        ShortcutInfo fromActivityInfo = ShortcutInfo.fromActivityInfo(launcherActivityInfoCompat, this.mContext);
        this.mShortcutToInstallTimeMap.put(fromActivityInfo, Long.valueOf(j));
        arrayList.add(fromActivityInfo);
    }

    public static void processAllUsers(List<UserHandleCompat> list, Context context) {
        if (Utilities.ATLEAST_LOLLIPOP) {
            UserManagerCompat userManagerCompat = UserManagerCompat.getInstance(context);
            HashSet hashSet = new HashSet();
            for (UserHandleCompat userHandleCompat : list) {
                addAllUserKeys(userManagerCompat.getSerialNumberForUser(userHandleCompat), hashSet);
            }
            SharedPreferences sharedPreferences = context.getSharedPreferences("com.android.launcher3.managedusers.prefs", 0);
            SharedPreferences.Editor edit = sharedPreferences.edit();
            for (String str : sharedPreferences.getAll().keySet()) {
                if (!hashSet.contains(str)) {
                    edit.remove(str);
                }
            }
            edit.apply();
        }
    }

    private void saveWorkFolderShortcuts(long j, int i) {
        for (ShortcutInfo shortcutInfo : this.mWorkFolderApps) {
            shortcutInfo.rank = i;
            LauncherModel.addItemToDatabase(this.mContext, shortcutInfo, j, 0L, 0, 0);
            i++;
        }
    }

    private void sortList(ArrayList<ShortcutInfo> arrayList) {
        Collections.sort(arrayList, new Comparator<ShortcutInfo>(this) { // from class: com.android.launcher3.util.ManagedProfileHeuristic.1
            final ManagedProfileHeuristic this$0;

            {
                this.this$0 = this;
            }

            @Override // java.util.Comparator
            public int compare(ShortcutInfo shortcutInfo, ShortcutInfo shortcutInfo2) {
                long j = 0;
                Long l = (Long) this.this$0.mShortcutToInstallTimeMap.get(shortcutInfo);
                Long l2 = (Long) this.this$0.mShortcutToInstallTimeMap.get(shortcutInfo2);
                long longValue = l == null ? 0L : l.longValue();
                if (l2 != null) {
                    j = l2.longValue();
                }
                return Utilities.longCompare(longValue, j);
            }
        });
    }

    public void processPackageAdd(String[] strArr) {
        initVars();
        HashSet<String> hashSet = new HashSet<>();
        boolean userApps = getUserApps(hashSet);
        boolean z = false;
        long currentTimeMillis = System.currentTimeMillis();
        LauncherAppsCompat launcherAppsCompat = LauncherAppsCompat.getInstance(this.mContext);
        for (String str : strArr) {
            if (!hashSet.contains(str)) {
                hashSet.add(str);
                List<LauncherActivityInfoCompat> activityList = launcherAppsCompat.getActivityList(str, this.mUser);
                z = true;
                if (!activityList.isEmpty()) {
                    markForAddition(activityList.get(0), currentTimeMillis);
                    z = true;
                }
            }
        }
        if (z) {
            this.mPrefs.edit().putStringSet(this.mPackageSetKey, hashSet).apply();
            finalizeAdditions(userApps);
        }
    }

    public void processPackageRemoved(String[] strArr) {
        HashSet<String> hashSet = new HashSet<>();
        getUserApps(hashSet);
        boolean z = false;
        for (String str : strArr) {
            if (hashSet.remove(str)) {
                z = true;
            }
        }
        if (z) {
            this.mPrefs.edit().putStringSet(this.mPackageSetKey, hashSet).apply();
        }
    }

    public void processUserApps(List<LauncherActivityInfoCompat> list) {
        initVars();
        HashSet<String> hashSet = new HashSet<>();
        boolean userApps = getUserApps(hashSet);
        boolean z = false;
        for (LauncherActivityInfoCompat launcherActivityInfoCompat : list) {
            String packageName = launcherActivityInfoCompat.getComponentName().getPackageName();
            if (!hashSet.contains(packageName)) {
                hashSet.add(packageName);
                z = true;
                markForAddition(launcherActivityInfoCompat, launcherActivityInfoCompat.getFirstInstallTime());
            }
        }
        if (z) {
            this.mPrefs.edit().putStringSet(this.mPackageSetKey, hashSet).apply();
            finalizeAdditions(userApps);
        }
    }
}

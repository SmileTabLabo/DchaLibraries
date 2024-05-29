package com.android.launcher3.model;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.util.LongSparseArray;
import android.util.Pair;
import com.android.launcher3.AllAppsList;
import com.android.launcher3.AppInfo;
import com.android.launcher3.FolderInfo;
import com.android.launcher3.InvariantDeviceProfile;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.LauncherAppWidgetInfo;
import com.android.launcher3.LauncherModel;
import com.android.launcher3.LauncherSettings;
import com.android.launcher3.ShortcutInfo;
import com.android.launcher3.Utilities;
import com.android.launcher3.util.GridOccupancy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
/* loaded from: classes.dex */
public class AddWorkspaceItemsTask extends BaseModelUpdateTask {
    private final List<Pair<ItemInfo, Object>> mItemList;

    public AddWorkspaceItemsTask(List<Pair<ItemInfo, Object>> list) {
        this.mItemList = list;
    }

    @Override // com.android.launcher3.model.BaseModelUpdateTask
    public void execute(LauncherAppState launcherAppState, BgDataModel bgDataModel, AllAppsList allAppsList) {
        if (this.mItemList.isEmpty()) {
            return;
        }
        Context context = launcherAppState.getContext();
        final ArrayList arrayList = new ArrayList();
        final ArrayList<Long> arrayList2 = new ArrayList<>();
        ArrayList<Long> loadWorkspaceScreensDb = LauncherModel.loadWorkspaceScreensDb(context);
        synchronized (bgDataModel) {
            ArrayList<ItemInfo> arrayList3 = new ArrayList();
            for (Pair<ItemInfo, Object> pair : this.mItemList) {
                ItemInfo itemInfo = (ItemInfo) pair.first;
                if ((itemInfo.itemType != 0 && itemInfo.itemType != 1) || !shortcutExists(bgDataModel, itemInfo.getIntent(), itemInfo.user)) {
                    if (itemInfo.itemType == 0 && (itemInfo instanceof AppInfo)) {
                        itemInfo = ((AppInfo) itemInfo).makeShortcut();
                    }
                    if (itemInfo != null) {
                        arrayList3.add(itemInfo);
                    }
                }
            }
            for (ItemInfo itemInfo2 : arrayList3) {
                Pair<Long, int[]> findSpaceForItem = findSpaceForItem(launcherAppState, bgDataModel, loadWorkspaceScreensDb, arrayList2, itemInfo2.spanX, itemInfo2.spanY);
                long longValue = ((Long) findSpaceForItem.first).longValue();
                int[] iArr = (int[]) findSpaceForItem.second;
                if (!(itemInfo2 instanceof ShortcutInfo) && !(itemInfo2 instanceof FolderInfo) && !(itemInfo2 instanceof LauncherAppWidgetInfo)) {
                    if (itemInfo2 instanceof AppInfo) {
                        itemInfo2 = ((AppInfo) itemInfo2).makeShortcut();
                    } else {
                        throw new RuntimeException("Unexpected info type");
                    }
                }
                getModelWriter().addItemToDatabase(itemInfo2, -100L, longValue, iArr[0], iArr[1]);
                arrayList.add(itemInfo2);
            }
        }
        updateScreens(context, loadWorkspaceScreensDb);
        if (!arrayList.isEmpty()) {
            scheduleCallbackTask(new LauncherModel.CallbackTask() { // from class: com.android.launcher3.model.AddWorkspaceItemsTask.1
                @Override // com.android.launcher3.LauncherModel.CallbackTask
                public void execute(LauncherModel.Callbacks callbacks) {
                    ArrayList<ItemInfo> arrayList4 = new ArrayList<>();
                    ArrayList<ItemInfo> arrayList5 = new ArrayList<>();
                    if (!arrayList.isEmpty()) {
                        long j = ((ItemInfo) arrayList.get(arrayList.size() - 1)).screenId;
                        Iterator it = arrayList.iterator();
                        while (it.hasNext()) {
                            ItemInfo itemInfo3 = (ItemInfo) it.next();
                            if (itemInfo3.screenId == j) {
                                arrayList4.add(itemInfo3);
                            } else {
                                arrayList5.add(itemInfo3);
                            }
                        }
                    }
                    callbacks.bindAppsAdded(arrayList2, arrayList5, arrayList4);
                }
            });
        }
    }

    protected void updateScreens(Context context, ArrayList<Long> arrayList) {
        LauncherModel.updateWorkspaceScreenOrder(context, arrayList);
    }

    protected boolean shortcutExists(BgDataModel bgDataModel, Intent intent, UserHandle userHandle) {
        String uri;
        String uri2;
        String str;
        if (intent == null) {
            return true;
        }
        if (intent.getComponent() != null) {
            str = intent.getComponent().getPackageName();
            if (intent.getPackage() != null) {
                uri = intent.toUri(0);
                uri2 = new Intent(intent).setPackage(null).toUri(0);
            } else {
                uri = new Intent(intent).setPackage(str).toUri(0);
                uri2 = intent.toUri(0);
            }
        } else {
            uri = intent.toUri(0);
            uri2 = intent.toUri(0);
            str = null;
        }
        boolean isLauncherAppTarget = Utilities.isLauncherAppTarget(intent);
        synchronized (bgDataModel) {
            Iterator<ItemInfo> it = bgDataModel.itemsIdMap.iterator();
            while (it.hasNext()) {
                ItemInfo next = it.next();
                if (next instanceof ShortcutInfo) {
                    ShortcutInfo shortcutInfo = (ShortcutInfo) next;
                    if (next.getIntent() != null && shortcutInfo.user.equals(userHandle)) {
                        Intent intent2 = new Intent(next.getIntent());
                        intent2.setSourceBounds(intent.getSourceBounds());
                        String uri3 = intent2.toUri(0);
                        if (!uri.equals(uri3) && !uri2.equals(uri3)) {
                            if (isLauncherAppTarget && shortcutInfo.isPromise() && shortcutInfo.hasStatusFlag(2) && shortcutInfo.getTargetComponent() != null && str != null && str.equals(shortcutInfo.getTargetComponent().getPackageName())) {
                                return true;
                            }
                        }
                        return true;
                    }
                }
            }
            return false;
        }
    }

    protected Pair<Long, int[]> findSpaceForItem(LauncherAppState launcherAppState, BgDataModel bgDataModel, ArrayList<Long> arrayList, ArrayList<Long> arrayList2, int i, int i2) {
        long j;
        LongSparseArray longSparseArray = new LongSparseArray();
        synchronized (bgDataModel) {
            Iterator<ItemInfo> it = bgDataModel.itemsIdMap.iterator();
            while (it.hasNext()) {
                ItemInfo next = it.next();
                if (next.container == -100) {
                    ArrayList arrayList3 = (ArrayList) longSparseArray.get(next.screenId);
                    if (arrayList3 == null) {
                        arrayList3 = new ArrayList();
                        longSparseArray.put(next.screenId, arrayList3);
                    }
                    arrayList3.add(next);
                }
            }
        }
        long j2 = 0;
        int[] iArr = new int[2];
        boolean z = false;
        int size = arrayList.size();
        int i3 = !arrayList.isEmpty() ? 1 : 0;
        if (i3 < size) {
            j2 = arrayList.get(i3).longValue();
            z = findNextAvailableIconSpaceInScreen(launcherAppState, (ArrayList) longSparseArray.get(j2), iArr, i, i2);
        }
        long j3 = j2;
        boolean z2 = z;
        if (!z2) {
            long j4 = j3;
            int i4 = 1;
            while (true) {
                if (i4 < size) {
                    j = arrayList.get(i4).longValue();
                    if (!findNextAvailableIconSpaceInScreen(launcherAppState, (ArrayList) longSparseArray.get(j), iArr, i, i2)) {
                        i4++;
                        j4 = j;
                    } else {
                        z2 = true;
                        break;
                    }
                } else {
                    j = j4;
                    break;
                }
            }
        } else {
            j = j3;
        }
        if (!z2) {
            j = LauncherSettings.Settings.call(launcherAppState.getContext().getContentResolver(), LauncherSettings.Settings.METHOD_NEW_SCREEN_ID).getLong(LauncherSettings.Settings.EXTRA_VALUE);
            arrayList.add(Long.valueOf(j));
            arrayList2.add(Long.valueOf(j));
            if (!findNextAvailableIconSpaceInScreen(launcherAppState, (ArrayList) longSparseArray.get(j), iArr, i, i2)) {
                throw new RuntimeException("Can't find space to add the item");
            }
        }
        return Pair.create(Long.valueOf(j), iArr);
    }

    private boolean findNextAvailableIconSpaceInScreen(LauncherAppState launcherAppState, ArrayList<ItemInfo> arrayList, int[] iArr, int i, int i2) {
        InvariantDeviceProfile invariantDeviceProfile = launcherAppState.getInvariantDeviceProfile();
        GridOccupancy gridOccupancy = new GridOccupancy(invariantDeviceProfile.numColumns, invariantDeviceProfile.numRows);
        if (arrayList != null) {
            Iterator<ItemInfo> it = arrayList.iterator();
            while (it.hasNext()) {
                gridOccupancy.markCells(it.next(), true);
            }
        }
        return gridOccupancy.findVacantCell(iArr, i, i2);
    }
}

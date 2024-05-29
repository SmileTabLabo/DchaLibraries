package com.android.systemui.usb;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.BenesseExtension;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.os.storage.DiskInfo;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.os.storage.VolumeRecord;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.SparseArray;
import com.android.systemui.SystemUI;
import com.android.systemui.util.NotificationChannels;
/* loaded from: classes.dex */
public class StorageNotification extends SystemUI {
    private NotificationManager mNotificationManager;
    private StorageManager mStorageManager;
    private final SparseArray<MoveInfo> mMoves = new SparseArray<>();
    private final StorageEventListener mListener = new StorageEventListener() { // from class: com.android.systemui.usb.StorageNotification.1
        public void onVolumeStateChanged(VolumeInfo volumeInfo, int i, int i2) {
            StorageNotification.this.onVolumeStateChangedInternal(volumeInfo);
        }

        public void onVolumeRecordChanged(VolumeRecord volumeRecord) {
            VolumeInfo findVolumeByUuid = StorageNotification.this.mStorageManager.findVolumeByUuid(volumeRecord.getFsUuid());
            if (findVolumeByUuid != null && findVolumeByUuid.isMountedReadable()) {
                StorageNotification.this.onVolumeStateChangedInternal(findVolumeByUuid);
            }
        }

        public void onVolumeForgotten(String str) {
            StorageNotification.this.mNotificationManager.cancelAsUser(str, 1397772886, UserHandle.ALL);
        }

        public void onDiskScanned(DiskInfo diskInfo, int i) {
            StorageNotification.this.onDiskScannedInternal(diskInfo, i);
        }

        public void onDiskDestroyed(DiskInfo diskInfo) {
            StorageNotification.this.onDiskDestroyedInternal(diskInfo);
        }
    };
    private final BroadcastReceiver mSnoozeReceiver = new BroadcastReceiver() { // from class: com.android.systemui.usb.StorageNotification.2
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            StorageNotification.this.mStorageManager.setVolumeSnoozed(intent.getStringExtra("android.os.storage.extra.FS_UUID"), true);
        }
    };
    private final BroadcastReceiver mFinishReceiver = new BroadcastReceiver() { // from class: com.android.systemui.usb.StorageNotification.3
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            StorageNotification.this.mNotificationManager.cancelAsUser(null, 1397575510, UserHandle.ALL);
        }
    };
    private final PackageManager.MoveCallback mMoveCallback = new PackageManager.MoveCallback() { // from class: com.android.systemui.usb.StorageNotification.4
        public void onCreated(int i, Bundle bundle) {
            MoveInfo moveInfo = new MoveInfo();
            moveInfo.moveId = i;
            moveInfo.extras = bundle;
            if (bundle != null) {
                moveInfo.packageName = bundle.getString("android.intent.extra.PACKAGE_NAME");
                moveInfo.label = bundle.getString("android.intent.extra.TITLE");
                moveInfo.volumeUuid = bundle.getString("android.os.storage.extra.FS_UUID");
            }
            StorageNotification.this.mMoves.put(i, moveInfo);
        }

        public void onStatusChanged(int i, int i2, long j) {
            MoveInfo moveInfo = (MoveInfo) StorageNotification.this.mMoves.get(i);
            if (moveInfo == null) {
                Log.w("StorageNotification", "Ignoring unknown move " + i);
            } else if (PackageManager.isMoveStatusFinished(i2)) {
                StorageNotification.this.onMoveFinished(moveInfo, i2);
            } else {
                StorageNotification.this.onMoveProgress(moveInfo, i2, j);
            }
        }
    };

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class MoveInfo {
        public Bundle extras;
        public String label;
        public int moveId;
        public String packageName;
        public String volumeUuid;

        private MoveInfo() {
        }
    }

    @Override // com.android.systemui.SystemUI
    public void start() {
        this.mNotificationManager = (NotificationManager) this.mContext.getSystemService(NotificationManager.class);
        this.mStorageManager = (StorageManager) this.mContext.getSystemService(StorageManager.class);
        this.mStorageManager.registerListener(this.mListener);
        this.mContext.registerReceiver(this.mSnoozeReceiver, new IntentFilter("com.android.systemui.action.SNOOZE_VOLUME"), "android.permission.MOUNT_UNMOUNT_FILESYSTEMS", null);
        this.mContext.registerReceiver(this.mFinishReceiver, new IntentFilter("com.android.systemui.action.FINISH_WIZARD"), "android.permission.MOUNT_UNMOUNT_FILESYSTEMS", null);
        for (DiskInfo diskInfo : this.mStorageManager.getDisks()) {
            onDiskScannedInternal(diskInfo, diskInfo.volumeCount);
        }
        for (VolumeInfo volumeInfo : this.mStorageManager.getVolumes()) {
            onVolumeStateChangedInternal(volumeInfo);
        }
        this.mContext.getPackageManager().registerMoveCallback(this.mMoveCallback, new Handler());
        updateMissingPrivateVolumes();
    }

    private void updateMissingPrivateVolumes() {
        if (isTv()) {
            return;
        }
        for (VolumeRecord volumeRecord : this.mStorageManager.getVolumeRecords()) {
            if (volumeRecord.getType() == 1) {
                String fsUuid = volumeRecord.getFsUuid();
                VolumeInfo findVolumeByUuid = this.mStorageManager.findVolumeByUuid(fsUuid);
                if ((findVolumeByUuid != null && findVolumeByUuid.isMountedWritable()) || volumeRecord.isSnoozed()) {
                    this.mNotificationManager.cancelAsUser(fsUuid, 1397772886, UserHandle.ALL);
                } else {
                    String string = this.mContext.getString(17039850, volumeRecord.getNickname());
                    String string2 = this.mContext.getString(17039849);
                    Notification.Builder extend = new Notification.Builder(this.mContext, NotificationChannels.STORAGE).setSmallIcon(17302732).setColor(this.mContext.getColor(17170774)).setContentTitle(string).setContentText(string2).setContentIntent(buildForgetPendingIntent(volumeRecord)).setStyle(new Notification.BigTextStyle().bigText(string2)).setVisibility(1).setLocalOnly(true).setCategory("sys").setDeleteIntent(buildSnoozeIntent(fsUuid)).extend(new Notification.TvExtender());
                    SystemUI.overrideNotificationAppName(this.mContext, extend, false);
                    this.mNotificationManager.notifyAsUser(fsUuid, 1397772886, extend.build(), UserHandle.ALL);
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onDiskScannedInternal(DiskInfo diskInfo, int i) {
        if (i != 0 || diskInfo.size <= 0) {
            this.mNotificationManager.cancelAsUser(diskInfo.getId(), 1396986699, UserHandle.ALL);
            return;
        }
        String string = this.mContext.getString(17039879, diskInfo.getDescription());
        String string2 = this.mContext.getString(17039878, diskInfo.getDescription());
        Notification.Builder extend = new Notification.Builder(this.mContext, NotificationChannels.STORAGE).setSmallIcon(getSmallIcon(diskInfo, 6)).setColor(this.mContext.getColor(17170774)).setContentTitle(string).setContentText(string2).setContentIntent(buildInitPendingIntent(diskInfo)).setStyle(new Notification.BigTextStyle().bigText(string2)).setVisibility(1).setLocalOnly(true).setCategory("err").extend(new Notification.TvExtender());
        SystemUI.overrideNotificationAppName(this.mContext, extend, false);
        this.mNotificationManager.notifyAsUser(diskInfo.getId(), 1396986699, extend.build(), UserHandle.ALL);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onDiskDestroyedInternal(DiskInfo diskInfo) {
        this.mNotificationManager.cancelAsUser(diskInfo.getId(), 1396986699, UserHandle.ALL);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onVolumeStateChangedInternal(VolumeInfo volumeInfo) {
        switch (volumeInfo.getType()) {
            case 0:
                onPublicVolumeStateChangedInternal(volumeInfo);
                return;
            case 1:
                onPrivateVolumeStateChangedInternal(volumeInfo);
                return;
            default:
                return;
        }
    }

    private void onPrivateVolumeStateChangedInternal(VolumeInfo volumeInfo) {
        Log.d("StorageNotification", "Notifying about private volume: " + volumeInfo.toString());
        updateMissingPrivateVolumes();
    }

    private void onPublicVolumeStateChangedInternal(VolumeInfo volumeInfo) {
        Notification onVolumeUnmounted;
        Log.d("StorageNotification", "Notifying about public volume: " + volumeInfo.toString());
        switch (volumeInfo.getState()) {
            case 0:
                onVolumeUnmounted = onVolumeUnmounted(volumeInfo);
                break;
            case 1:
                onVolumeUnmounted = onVolumeChecking(volumeInfo);
                break;
            case 2:
            case 3:
                onVolumeUnmounted = onVolumeMounted(volumeInfo);
                break;
            case 4:
                onVolumeUnmounted = onVolumeFormatting(volumeInfo);
                break;
            case 5:
                onVolumeUnmounted = onVolumeEjecting(volumeInfo);
                break;
            case 6:
                onVolumeUnmounted = onVolumeUnmountable(volumeInfo);
                break;
            case 7:
                onVolumeUnmounted = onVolumeRemoved(volumeInfo);
                break;
            case 8:
                onVolumeUnmounted = onVolumeBadRemoval(volumeInfo);
                break;
            default:
                onVolumeUnmounted = null;
                break;
        }
        if (onVolumeUnmounted != null) {
            this.mNotificationManager.notifyAsUser(volumeInfo.getId(), 1397773634, onVolumeUnmounted, UserHandle.of(volumeInfo.getMountUserId()));
        } else {
            this.mNotificationManager.cancelAsUser(volumeInfo.getId(), 1397773634, UserHandle.of(volumeInfo.getMountUserId()));
        }
    }

    private Notification onVolumeUnmounted(VolumeInfo volumeInfo) {
        return null;
    }

    private Notification onVolumeChecking(VolumeInfo volumeInfo) {
        DiskInfo disk = volumeInfo.getDisk();
        return buildNotificationBuilder(volumeInfo, this.mContext.getString(17039847, disk.getDescription()), this.mContext.getString(17039846, disk.getDescription())).setCategory("progress").setOngoing(true).build();
    }

    private Notification onVolumeMounted(VolumeInfo volumeInfo) {
        VolumeRecord findRecordByUuid = this.mStorageManager.findRecordByUuid(volumeInfo.getFsUuid());
        DiskInfo disk = volumeInfo.getDisk();
        if (findRecordByUuid.isSnoozed() && disk.isAdoptable()) {
            return null;
        }
        if (disk.isAdoptable() && !findRecordByUuid.isInited()) {
            String description = disk.getDescription();
            String string = this.mContext.getString(17039857, disk.getDescription());
            buildInitPendingIntent(volumeInfo);
            return buildNotificationBuilder(volumeInfo, description, string).addAction(new Notification.Action(17302365, this.mContext.getString(17039873), buildUnmountPendingIntent(volumeInfo))).setDeleteIntent(buildSnoozeIntent(volumeInfo.getFsUuid())).build();
        }
        String description2 = disk.getDescription();
        String string2 = this.mContext.getString(17039861, disk.getDescription());
        buildBrowsePendingIntent(volumeInfo);
        Notification.Builder category = buildNotificationBuilder(volumeInfo, description2, string2).addAction(new Notification.Action(17302365, this.mContext.getString(17039873), buildUnmountPendingIntent(volumeInfo))).setCategory("sys");
        if (disk.isAdoptable()) {
            category.setDeleteIntent(buildSnoozeIntent(volumeInfo.getFsUuid()));
        }
        return category.build();
    }

    private Notification onVolumeFormatting(VolumeInfo volumeInfo) {
        return null;
    }

    private Notification onVolumeEjecting(VolumeInfo volumeInfo) {
        DiskInfo disk = volumeInfo.getDisk();
        return buildNotificationBuilder(volumeInfo, this.mContext.getString(17039877, disk.getDescription()), this.mContext.getString(17039876, disk.getDescription())).setCategory("progress").setOngoing(true).build();
    }

    private Notification onVolumeUnmountable(VolumeInfo volumeInfo) {
        DiskInfo disk = volumeInfo.getDisk();
        return buildNotificationBuilder(volumeInfo, this.mContext.getString(17039875, disk.getDescription()), this.mContext.getString(17039874, disk.getDescription())).setContentIntent(buildInitPendingIntent(volumeInfo)).setCategory("err").build();
    }

    private Notification onVolumeRemoved(VolumeInfo volumeInfo) {
        if (!volumeInfo.isPrimary()) {
            return null;
        }
        DiskInfo disk = volumeInfo.getDisk();
        return buildNotificationBuilder(volumeInfo, this.mContext.getString(17039860, disk.getDescription()), this.mContext.getString(17039859, disk.getDescription())).setCategory("err").build();
    }

    private Notification onVolumeBadRemoval(VolumeInfo volumeInfo) {
        if (!volumeInfo.isPrimary()) {
            return null;
        }
        DiskInfo disk = volumeInfo.getDisk();
        return buildNotificationBuilder(volumeInfo, this.mContext.getString(17039844, disk.getDescription()), this.mContext.getString(17039843, disk.getDescription())).setCategory("err").build();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onMoveProgress(MoveInfo moveInfo, int i, long j) {
        String string;
        CharSequence formatDuration;
        PendingIntent buildWizardMigratePendingIntent;
        if (!TextUtils.isEmpty(moveInfo.label)) {
            string = this.mContext.getString(17039853, moveInfo.label);
        } else {
            string = this.mContext.getString(17039856);
        }
        if (j < 0) {
            formatDuration = null;
        } else {
            formatDuration = DateUtils.formatDuration(j);
        }
        if (moveInfo.packageName != null) {
            buildWizardMigratePendingIntent = buildWizardMovePendingIntent(moveInfo);
        } else {
            buildWizardMigratePendingIntent = buildWizardMigratePendingIntent(moveInfo);
        }
        Notification.Builder ongoing = new Notification.Builder(this.mContext, NotificationChannels.STORAGE).setSmallIcon(17302732).setColor(this.mContext.getColor(17170774)).setContentTitle(string).setContentText(formatDuration).setContentIntent(buildWizardMigratePendingIntent).setStyle(new Notification.BigTextStyle().bigText(formatDuration)).setVisibility(1).setLocalOnly(true).setCategory("progress").setProgress(100, i, false).setOngoing(true);
        SystemUI.overrideNotificationAppName(this.mContext, ongoing, false);
        this.mNotificationManager.notifyAsUser(moveInfo.packageName, 1397575510, ongoing.build(), UserHandle.ALL);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onMoveFinished(MoveInfo moveInfo, int i) {
        String string;
        String string2;
        PendingIntent pendingIntent;
        if (moveInfo.packageName != null) {
            this.mNotificationManager.cancelAsUser(moveInfo.packageName, 1397575510, UserHandle.ALL);
            return;
        }
        VolumeInfo primaryStorageCurrentVolume = this.mContext.getPackageManager().getPrimaryStorageCurrentVolume();
        String bestVolumeDescription = this.mStorageManager.getBestVolumeDescription(primaryStorageCurrentVolume);
        if (i == -100) {
            string = this.mContext.getString(17039855);
            string2 = this.mContext.getString(17039854, bestVolumeDescription);
        } else {
            string = this.mContext.getString(17039852);
            string2 = this.mContext.getString(17039851);
        }
        if (primaryStorageCurrentVolume != null && primaryStorageCurrentVolume.getDisk() != null) {
            pendingIntent = buildWizardReadyPendingIntent(primaryStorageCurrentVolume.getDisk());
        } else if (primaryStorageCurrentVolume != null) {
            pendingIntent = buildVolumeSettingsPendingIntent(primaryStorageCurrentVolume);
        } else {
            pendingIntent = null;
        }
        Notification.Builder autoCancel = new Notification.Builder(this.mContext, NotificationChannels.STORAGE).setSmallIcon(17302732).setColor(this.mContext.getColor(17170774)).setContentTitle(string).setContentText(string2).setContentIntent(pendingIntent).setStyle(new Notification.BigTextStyle().bigText(string2)).setVisibility(1).setLocalOnly(true).setCategory("sys").setAutoCancel(true);
        SystemUI.overrideNotificationAppName(this.mContext, autoCancel, false);
        this.mNotificationManager.notifyAsUser(moveInfo.packageName, 1397575510, autoCancel.build(), UserHandle.ALL);
    }

    private int getSmallIcon(DiskInfo diskInfo, int i) {
        return diskInfo.isSd() ? (i == 1 || i == 5) ? 17302732 : 17302732 : diskInfo.isUsb() ? 17302765 : 17302732;
    }

    private Notification.Builder buildNotificationBuilder(VolumeInfo volumeInfo, CharSequence charSequence, CharSequence charSequence2) {
        Notification.Builder extend = new Notification.Builder(this.mContext, NotificationChannels.STORAGE).setSmallIcon(getSmallIcon(volumeInfo.getDisk(), volumeInfo.getState())).setColor(this.mContext.getColor(17170774)).setContentTitle(charSequence).setContentText(charSequence2).setStyle(new Notification.BigTextStyle().bigText(charSequence2)).setVisibility(1).setLocalOnly(true).extend(new Notification.TvExtender());
        overrideNotificationAppName(this.mContext, extend, false);
        return extend;
    }

    private PendingIntent buildInitPendingIntent(DiskInfo diskInfo) {
        if (BenesseExtension.getDchaState() != 0) {
            return null;
        }
        Intent intent = new Intent();
        if (isTv()) {
            intent.setPackage("com.android.tv.settings");
            intent.setAction("com.android.tv.settings.action.NEW_STORAGE");
        } else {
            intent.setClassName("com.android.settings", "com.android.settings.deviceinfo.StorageWizardInit");
        }
        intent.putExtra("android.os.storage.extra.DISK_ID", diskInfo.getId());
        return PendingIntent.getActivityAsUser(this.mContext, diskInfo.getId().hashCode(), intent, 268435456, null, UserHandle.CURRENT);
    }

    private PendingIntent buildInitPendingIntent(VolumeInfo volumeInfo) {
        if (BenesseExtension.getDchaState() != 0) {
            return null;
        }
        Intent intent = new Intent();
        if (isTv()) {
            intent.setPackage("com.android.tv.settings");
            intent.setAction("com.android.tv.settings.action.NEW_STORAGE");
        } else {
            intent.setClassName("com.android.settings", "com.android.settings.deviceinfo.StorageWizardInit");
        }
        intent.putExtra("android.os.storage.extra.VOLUME_ID", volumeInfo.getId());
        return PendingIntent.getActivityAsUser(this.mContext, volumeInfo.getId().hashCode(), intent, 268435456, null, UserHandle.CURRENT);
    }

    private PendingIntent buildUnmountPendingIntent(VolumeInfo volumeInfo) {
        if (BenesseExtension.getDchaState() != 0) {
            return null;
        }
        Intent intent = new Intent();
        if (isTv()) {
            intent.setPackage("com.android.tv.settings");
            intent.setAction("com.android.tv.settings.action.UNMOUNT_STORAGE");
            intent.putExtra("android.os.storage.extra.VOLUME_ID", volumeInfo.getId());
            return PendingIntent.getActivityAsUser(this.mContext, volumeInfo.getId().hashCode(), intent, 268435456, null, UserHandle.CURRENT);
        }
        intent.setClassName("com.android.settings", "com.android.settings.deviceinfo.StorageUnmountReceiver");
        intent.putExtra("android.os.storage.extra.VOLUME_ID", volumeInfo.getId());
        return PendingIntent.getBroadcastAsUser(this.mContext, volumeInfo.getId().hashCode(), intent, 268435456, UserHandle.CURRENT);
    }

    private PendingIntent buildBrowsePendingIntent(VolumeInfo volumeInfo) {
        Intent buildBrowseIntentForUser = volumeInfo.buildBrowseIntentForUser(volumeInfo.getMountUserId());
        return PendingIntent.getActivityAsUser(this.mContext, volumeInfo.getId().hashCode(), buildBrowseIntentForUser, 268435456, null, UserHandle.CURRENT);
    }

    private PendingIntent buildVolumeSettingsPendingIntent(VolumeInfo volumeInfo) {
        if (BenesseExtension.getDchaState() != 0) {
            return null;
        }
        Intent intent = new Intent();
        if (isTv()) {
            intent.setPackage("com.android.tv.settings");
            intent.setAction("android.settings.INTERNAL_STORAGE_SETTINGS");
        } else {
            switch (volumeInfo.getType()) {
                case 0:
                    intent.setClassName("com.android.settings", "com.android.settings.Settings$PublicVolumeSettingsActivity");
                    break;
                case 1:
                    intent.setClassName("com.android.settings", "com.android.settings.Settings$PrivateVolumeSettingsActivity");
                    break;
                default:
                    return null;
            }
        }
        intent.putExtra("android.os.storage.extra.VOLUME_ID", volumeInfo.getId());
        return PendingIntent.getActivityAsUser(this.mContext, volumeInfo.getId().hashCode(), intent, 268435456, null, UserHandle.CURRENT);
    }

    private PendingIntent buildSnoozeIntent(String str) {
        Intent intent = new Intent("com.android.systemui.action.SNOOZE_VOLUME");
        intent.putExtra("android.os.storage.extra.FS_UUID", str);
        return PendingIntent.getBroadcastAsUser(this.mContext, str.hashCode(), intent, 268435456, UserHandle.CURRENT);
    }

    private PendingIntent buildForgetPendingIntent(VolumeRecord volumeRecord) {
        if (BenesseExtension.getDchaState() != 0) {
            return null;
        }
        Intent intent = new Intent();
        intent.setClassName("com.android.settings", "com.android.settings.Settings$PrivateVolumeForgetActivity");
        intent.putExtra("android.os.storage.extra.FS_UUID", volumeRecord.getFsUuid());
        return PendingIntent.getActivityAsUser(this.mContext, volumeRecord.getFsUuid().hashCode(), intent, 268435456, null, UserHandle.CURRENT);
    }

    private PendingIntent buildWizardMigratePendingIntent(MoveInfo moveInfo) {
        if (BenesseExtension.getDchaState() != 0) {
            return null;
        }
        Intent intent = new Intent();
        if (isTv()) {
            intent.setPackage("com.android.tv.settings");
            intent.setAction("com.android.tv.settings.action.MIGRATE_STORAGE");
        } else {
            intent.setClassName("com.android.settings", "com.android.settings.deviceinfo.StorageWizardMigrateProgress");
        }
        intent.putExtra("android.content.pm.extra.MOVE_ID", moveInfo.moveId);
        VolumeInfo findVolumeByQualifiedUuid = this.mStorageManager.findVolumeByQualifiedUuid(moveInfo.volumeUuid);
        if (findVolumeByQualifiedUuid != null) {
            intent.putExtra("android.os.storage.extra.VOLUME_ID", findVolumeByQualifiedUuid.getId());
        }
        return PendingIntent.getActivityAsUser(this.mContext, moveInfo.moveId, intent, 268435456, null, UserHandle.CURRENT);
    }

    private PendingIntent buildWizardMovePendingIntent(MoveInfo moveInfo) {
        if (BenesseExtension.getDchaState() != 0) {
            return null;
        }
        Intent intent = new Intent();
        if (isTv()) {
            intent.setPackage("com.android.tv.settings");
            intent.setAction("com.android.tv.settings.action.MOVE_APP");
        } else {
            intent.setClassName("com.android.settings", "com.android.settings.deviceinfo.StorageWizardMoveProgress");
        }
        intent.putExtra("android.content.pm.extra.MOVE_ID", moveInfo.moveId);
        return PendingIntent.getActivityAsUser(this.mContext, moveInfo.moveId, intent, 268435456, null, UserHandle.CURRENT);
    }

    private PendingIntent buildWizardReadyPendingIntent(DiskInfo diskInfo) {
        if (BenesseExtension.getDchaState() != 0) {
            return null;
        }
        Intent intent = new Intent();
        if (isTv()) {
            intent.setPackage("com.android.tv.settings");
            intent.setAction("android.settings.INTERNAL_STORAGE_SETTINGS");
        } else {
            intent.setClassName("com.android.settings", "com.android.settings.deviceinfo.StorageWizardReady");
        }
        intent.putExtra("android.os.storage.extra.DISK_ID", diskInfo.getId());
        return PendingIntent.getActivityAsUser(this.mContext, diskInfo.getId().hashCode(), intent, 268435456, null, UserHandle.CURRENT);
    }

    private boolean isTv() {
        return this.mContext.getPackageManager().hasSystemFeature("android.software.leanback");
    }
}

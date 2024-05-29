package com.android.systemui.usb;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
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
/* loaded from: a.zip:com/android/systemui/usb/StorageNotification.class */
public class StorageNotification extends SystemUI {
    private NotificationManager mNotificationManager;
    private StorageManager mStorageManager;
    private Notification mUsbStorageNotification;
    private boolean mIsUmsConnect = false;
    private int mNotifcationState = 0;
    private boolean mIsLastVisible = false;
    private final SparseArray<MoveInfo> mMoves = new SparseArray<>();
    private final StorageEventListener mListener = new StorageEventListener(this) { // from class: com.android.systemui.usb.StorageNotification.1
        final StorageNotification this$0;

        {
            this.this$0 = this;
        }

        public void onDiskDestroyed(DiskInfo diskInfo) {
            this.this$0.onDiskDestroyedInternal(diskInfo);
        }

        public void onDiskScanned(DiskInfo diskInfo, int i) {
            this.this$0.onDiskScannedInternal(diskInfo, i);
        }

        public void onVolumeForgotten(String str) {
            this.this$0.mNotificationManager.cancelAsUser(str, 1397772886, UserHandle.ALL);
        }

        public void onVolumeRecordChanged(VolumeRecord volumeRecord) {
            VolumeInfo findVolumeByUuid = this.this$0.mStorageManager.findVolumeByUuid(volumeRecord.getFsUuid());
            if (findVolumeByUuid == null || !findVolumeByUuid.isMountedReadable()) {
                return;
            }
            this.this$0.onVolumeStateChangedInternal(findVolumeByUuid);
        }

        public void onVolumeStateChanged(VolumeInfo volumeInfo, int i, int i2) {
            this.this$0.onVolumeStateChangedInternal(volumeInfo);
        }
    };
    private final BroadcastReceiver mSnoozeReceiver = new BroadcastReceiver(this) { // from class: com.android.systemui.usb.StorageNotification.2
        final StorageNotification this$0;

        {
            this.this$0 = this;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            this.this$0.mStorageManager.setVolumeSnoozed(intent.getStringExtra("android.os.storage.extra.FS_UUID"), true);
        }
    };
    private final BroadcastReceiver mFinishReceiver = new BroadcastReceiver(this) { // from class: com.android.systemui.usb.StorageNotification.3
        final StorageNotification this$0;

        {
            this.this$0 = this;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            this.this$0.mNotificationManager.cancelAsUser(null, 1397575510, UserHandle.ALL);
        }
    };
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver(this) { // from class: com.android.systemui.usb.StorageNotification.4
        final StorageNotification this$0;

        {
            this.this$0 = this;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            boolean booleanExtra = intent.getBooleanExtra("configured", false) ? intent.getBooleanExtra("mass_storage", false) : false;
            Log.i("StorageNotification", "onReceive=" + intent.getAction() + ",available=" + booleanExtra);
            this.this$0.onUsbMassStorageConnectionChangedAsync(booleanExtra);
        }
    };
    private final BroadcastReceiver mUserReceiver = new BroadcastReceiver(this) { // from class: com.android.systemui.usb.StorageNotification.5
        final StorageNotification this$0;

        {
            this.this$0 = this;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.USER_SWITCHED".equals(intent.getAction())) {
                this.this$0.updateUsbMassStorageNotification();
            }
        }
    };
    private final PackageManager.MoveCallback mMoveCallback = new PackageManager.MoveCallback(this) { // from class: com.android.systemui.usb.StorageNotification.6
        final StorageNotification this$0;

        {
            this.this$0 = this;
        }

        public void onCreated(int i, Bundle bundle) {
            MoveInfo moveInfo = new MoveInfo(null);
            moveInfo.moveId = i;
            moveInfo.extras = bundle;
            if (bundle != null) {
                moveInfo.packageName = bundle.getString("android.intent.extra.PACKAGE_NAME");
                moveInfo.label = bundle.getString("android.intent.extra.TITLE");
                moveInfo.volumeUuid = bundle.getString("android.os.storage.extra.FS_UUID");
            }
            this.this$0.mMoves.put(i, moveInfo);
        }

        public void onStatusChanged(int i, int i2, long j) {
            MoveInfo moveInfo = (MoveInfo) this.this$0.mMoves.get(i);
            if (moveInfo == null) {
                Log.w("StorageNotification", "Ignoring unknown move " + i);
            } else if (PackageManager.isMoveStatusFinished(i2)) {
                this.this$0.onMoveFinished(moveInfo, i2);
            } else {
                this.this$0.onMoveProgress(moveInfo, i2, j);
            }
        }
    };

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/usb/StorageNotification$MoveInfo.class */
    public static class MoveInfo {
        public Bundle extras;
        public String label;
        public int moveId;
        public String packageName;
        public String volumeUuid;

        private MoveInfo() {
        }

        /* synthetic */ MoveInfo(MoveInfo moveInfo) {
            this();
        }
    }

    private PendingIntent buildBrowsePendingIntent(VolumeInfo volumeInfo) {
        Intent buildBrowseIntent = volumeInfo.buildBrowseIntent();
        return PendingIntent.getActivityAsUser(this.mContext, volumeInfo.getId().hashCode(), buildBrowseIntent, 268435456, null, UserHandle.CURRENT);
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

    private PendingIntent buildInitPendingIntent(DiskInfo diskInfo) {
        if (BenesseExtension.getDchaState() != 0) {
            return null;
        }
        Intent intent = new Intent();
        intent.setClassName("com.android.settings", "com.android.settings.deviceinfo.StorageWizardInit");
        intent.putExtra("android.os.storage.extra.DISK_ID", diskInfo.getId());
        return PendingIntent.getActivityAsUser(this.mContext, diskInfo.getId().hashCode(), intent, 268435456, null, UserHandle.CURRENT);
    }

    private PendingIntent buildInitPendingIntent(VolumeInfo volumeInfo) {
        if (BenesseExtension.getDchaState() != 0) {
            return null;
        }
        Intent intent = new Intent();
        intent.setClassName("com.android.settings", "com.android.settings.deviceinfo.StorageWizardInit");
        intent.putExtra("android.os.storage.extra.VOLUME_ID", volumeInfo.getId());
        return PendingIntent.getActivityAsUser(this.mContext, volumeInfo.getId().hashCode(), intent, 268435456, null, UserHandle.CURRENT);
    }

    private Notification.Builder buildNotificationBuilder(VolumeInfo volumeInfo, CharSequence charSequence, CharSequence charSequence2) {
        Notification.Builder localOnly = new Notification.Builder(this.mContext).setSmallIcon(getSmallIcon(volumeInfo.getDisk(), volumeInfo.getState())).setColor(this.mContext.getColor(17170521)).setContentTitle(charSequence).setContentText(charSequence2).setStyle(new Notification.BigTextStyle().bigText(charSequence2)).setVisibility(1).setLocalOnly(true);
        overrideNotificationAppName(this.mContext, localOnly);
        return localOnly;
    }

    private PendingIntent buildSnoozeIntent(String str) {
        Intent intent = new Intent("com.android.systemui.action.SNOOZE_VOLUME");
        intent.putExtra("android.os.storage.extra.FS_UUID", str);
        return PendingIntent.getBroadcastAsUser(this.mContext, str.hashCode(), intent, 268435456, UserHandle.CURRENT);
    }

    private PendingIntent buildUnmountPendingIntent(VolumeInfo volumeInfo) {
        if (BenesseExtension.getDchaState() != 0) {
            return null;
        }
        Intent intent = new Intent();
        intent.setClassName("com.android.settings", "com.android.settings.deviceinfo.StorageUnmountReceiver");
        intent.putExtra("android.os.storage.extra.VOLUME_ID", volumeInfo.getId());
        return PendingIntent.getBroadcastAsUser(this.mContext, volumeInfo.getId().hashCode(), intent, 268435456, UserHandle.CURRENT);
    }

    private PendingIntent buildVolumeSettingsPendingIntent(VolumeInfo volumeInfo) {
        if (BenesseExtension.getDchaState() != 0) {
            return null;
        }
        Intent intent = new Intent();
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
        intent.putExtra("android.os.storage.extra.VOLUME_ID", volumeInfo.getId());
        return PendingIntent.getActivityAsUser(this.mContext, volumeInfo.getId().hashCode(), intent, 268435456, null, UserHandle.CURRENT);
    }

    private PendingIntent buildWizardMigratePendingIntent(MoveInfo moveInfo) {
        if (BenesseExtension.getDchaState() != 0) {
            return null;
        }
        Intent intent = new Intent();
        intent.setClassName("com.android.settings", "com.android.settings.deviceinfo.StorageWizardMigrateProgress");
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
        intent.setClassName("com.android.settings", "com.android.settings.deviceinfo.StorageWizardMoveProgress");
        intent.putExtra("android.content.pm.extra.MOVE_ID", moveInfo.moveId);
        return PendingIntent.getActivityAsUser(this.mContext, moveInfo.moveId, intent, 268435456, null, UserHandle.CURRENT);
    }

    private PendingIntent buildWizardReadyPendingIntent(DiskInfo diskInfo) {
        if (BenesseExtension.getDchaState() != 0) {
            return null;
        }
        Intent intent = new Intent();
        intent.setClassName("com.android.settings", "com.android.settings.deviceinfo.StorageWizardReady");
        intent.putExtra("android.os.storage.extra.DISK_ID", diskInfo.getId());
        return PendingIntent.getActivityAsUser(this.mContext, diskInfo.getId().hashCode(), intent, 268435456, null, UserHandle.CURRENT);
    }

    private int getSmallIcon(DiskInfo diskInfo, int i) {
        if (!diskInfo.isSd()) {
            return diskInfo.isUsb() ? 17302577 : 17302555;
        }
        switch (i) {
            case 1:
            case 5:
                return 17302555;
            default:
                return 17302555;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onDiskDestroyedInternal(DiskInfo diskInfo) {
        this.mNotificationManager.cancelAsUser(diskInfo.getId(), 1396986699, UserHandle.ALL);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onDiskScannedInternal(DiskInfo diskInfo, int i) {
        if (i != 0 || diskInfo.size <= 0) {
            this.mNotificationManager.cancelAsUser(diskInfo.getId(), 1396986699, UserHandle.ALL);
            return;
        }
        String string = this.mContext.getString(17040427, diskInfo.getDescription());
        String string2 = this.mContext.getString(17040428, diskInfo.getDescription());
        Notification.Builder category = new Notification.Builder(this.mContext).setSmallIcon(getSmallIcon(diskInfo, 6)).setColor(this.mContext.getColor(17170521)).setContentTitle(string).setContentText(string2).setContentIntent(buildInitPendingIntent(diskInfo)).setStyle(new Notification.BigTextStyle().bigText(string2)).setVisibility(1).setLocalOnly(true).setCategory("err");
        SystemUI.overrideNotificationAppName(this.mContext, category);
        this.mNotificationManager.notifyAsUser(diskInfo.getId(), 1396986699, category.build(), UserHandle.ALL);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onMoveFinished(MoveInfo moveInfo, int i) {
        String string;
        String string2;
        if (moveInfo.packageName != null) {
            this.mNotificationManager.cancelAsUser(moveInfo.packageName, 1397575510, UserHandle.ALL);
            return;
        }
        VolumeInfo primaryStorageCurrentVolume = this.mContext.getPackageManager().getPrimaryStorageCurrentVolume();
        String bestVolumeDescription = this.mStorageManager.getBestVolumeDescription(primaryStorageCurrentVolume);
        if (i == -100) {
            string = this.mContext.getString(17040442);
            string2 = this.mContext.getString(17040443, bestVolumeDescription);
        } else {
            string = this.mContext.getString(17040444);
            string2 = this.mContext.getString(17040445);
        }
        Notification.Builder autoCancel = new Notification.Builder(this.mContext).setSmallIcon(17302555).setColor(this.mContext.getColor(17170521)).setContentTitle(string).setContentText(string2).setContentIntent((primaryStorageCurrentVolume == null || primaryStorageCurrentVolume.getDisk() == null) ? primaryStorageCurrentVolume != null ? buildVolumeSettingsPendingIntent(primaryStorageCurrentVolume) : null : buildWizardReadyPendingIntent(primaryStorageCurrentVolume.getDisk())).setStyle(new Notification.BigTextStyle().bigText(string2)).setVisibility(1).setLocalOnly(true).setCategory("sys").setPriority(-1).setAutoCancel(true);
        SystemUI.overrideNotificationAppName(this.mContext, autoCancel);
        this.mNotificationManager.notifyAsUser(moveInfo.packageName, 1397575510, autoCancel.build(), UserHandle.ALL);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onMoveProgress(MoveInfo moveInfo, int i, long j) {
        String string = !TextUtils.isEmpty(moveInfo.label) ? this.mContext.getString(17040440, moveInfo.label) : this.mContext.getString(17040441);
        CharSequence formatDuration = j < 0 ? null : DateUtils.formatDuration(j);
        PendingIntent buildWizardMovePendingIntent = moveInfo.packageName != null ? buildWizardMovePendingIntent(moveInfo) : buildWizardMigratePendingIntent(moveInfo);
        if (buildWizardMovePendingIntent == null) {
            return;
        }
        Notification.Builder ongoing = new Notification.Builder(this.mContext).setSmallIcon(17302555).setColor(this.mContext.getColor(17170521)).setContentTitle(string).setContentText(formatDuration).setContentIntent(buildWizardMovePendingIntent).setStyle(new Notification.BigTextStyle().bigText(formatDuration)).setVisibility(1).setLocalOnly(true).setCategory("progress").setPriority(-1).setProgress(100, i, false).setOngoing(true);
        SystemUI.overrideNotificationAppName(this.mContext, ongoing);
        this.mNotificationManager.notifyAsUser(moveInfo.packageName, 1397575510, ongoing.build(), UserHandle.ALL);
    }

    private void onPrivateVolumeStateChangedInternal(VolumeInfo volumeInfo) {
        Log.d("StorageNotification", "Notifying about private volume: " + volumeInfo.toString());
        updateMissingPrivateVolumes();
    }

    private void onPublicVolumeStateChangedInternal(VolumeInfo volumeInfo) {
        Notification notification;
        Log.d("StorageNotification", "Notifying about public volume: " + volumeInfo.toString());
        switch (volumeInfo.getState()) {
            case 0:
                notification = onVolumeUnmounted(volumeInfo);
                break;
            case 1:
                notification = onVolumeChecking(volumeInfo);
                break;
            case 2:
            case 3:
                notification = onVolumeMounted(volumeInfo);
                break;
            case 4:
                notification = onVolumeFormatting(volumeInfo);
                break;
            case 5:
                notification = onVolumeEjecting(volumeInfo);
                break;
            case 6:
                notification = onVolumeUnmountable(volumeInfo);
                break;
            case 7:
                notification = onVolumeRemoved(volumeInfo);
                break;
            case 8:
                notification = onVolumeBadRemoval(volumeInfo);
                break;
            case 9:
                notification = null;
                break;
            default:
                notification = null;
                break;
        }
        updateUsbMassStorageNotification();
        if (notification != null) {
            this.mNotificationManager.notifyAsUser(volumeInfo.getId(), 1397773634, notification, UserHandle.ALL);
        } else {
            this.mNotificationManager.cancelAsUser(volumeInfo.getId(), 1397773634, UserHandle.ALL);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onUsbMassStorageConnectionChangedAsync(boolean z) {
        this.mIsUmsConnect = z;
        updateUsbMassStorageNotification();
    }

    private Notification onVolumeBadRemoval(VolumeInfo volumeInfo) {
        if (volumeInfo.isPrimary()) {
            DiskInfo disk = volumeInfo.getDisk();
            return buildNotificationBuilder(volumeInfo, this.mContext.getString(17040429, disk.getDescription()), this.mContext.getString(17040430, disk.getDescription())).setCategory("err").build();
        }
        return null;
    }

    private Notification onVolumeChecking(VolumeInfo volumeInfo) {
        DiskInfo disk = volumeInfo.getDisk();
        return buildNotificationBuilder(volumeInfo, this.mContext.getString(17040421, disk.getDescription()), this.mContext.getString(17040422, disk.getDescription())).setCategory("progress").setPriority(-1).setOngoing(true).build();
    }

    private Notification onVolumeEjecting(VolumeInfo volumeInfo) {
        DiskInfo disk = volumeInfo.getDisk();
        return buildNotificationBuilder(volumeInfo, this.mContext.getString(17040433, disk.getDescription()), this.mContext.getString(17040434, disk.getDescription())).setCategory("progress").setPriority(-1).setOngoing(true).build();
    }

    private Notification onVolumeFormatting(VolumeInfo volumeInfo) {
        return null;
    }

    private Notification onVolumeMounted(VolumeInfo volumeInfo) {
        VolumeRecord findRecordByUuid = this.mStorageManager.findRecordByUuid(volumeInfo.getFsUuid());
        DiskInfo disk = volumeInfo.getDisk();
        if (findRecordByUuid == null) {
            return null;
        }
        if (findRecordByUuid.isSnoozed() && disk.isAdoptable()) {
            return null;
        }
        if (disk.isAdoptable() && !findRecordByUuid.isInited()) {
            String description = disk.getDescription();
            String string = this.mContext.getString(17040423, disk.getDescription());
            buildInitPendingIntent(volumeInfo);
            return buildNotificationBuilder(volumeInfo, description, string).addAction(new Notification.Action(17302324, this.mContext.getString(17040436), buildUnmountPendingIntent(volumeInfo))).setDeleteIntent(buildSnoozeIntent(volumeInfo.getFsUuid())).setCategory("sys").build();
        }
        String description2 = disk.getDescription();
        String string2 = this.mContext.getString(17040424, disk.getDescription());
        buildBrowsePendingIntent(volumeInfo);
        Notification.Builder priority = buildNotificationBuilder(volumeInfo, description2, string2).addAction(new Notification.Action(17302324, this.mContext.getString(17040436), buildUnmountPendingIntent(volumeInfo))).setCategory("sys").setPriority(-1);
        if (disk.isAdoptable()) {
            priority.setDeleteIntent(buildSnoozeIntent(volumeInfo.getFsUuid()));
        }
        return priority.build();
    }

    private Notification onVolumeRemoved(VolumeInfo volumeInfo) {
        if (volumeInfo.isPrimary()) {
            DiskInfo disk = volumeInfo.getDisk();
            return buildNotificationBuilder(volumeInfo, this.mContext.getString(17040431, disk.getDescription()), this.mContext.getString(17040432, disk.getDescription())).setCategory("err").build();
        }
        return null;
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

    private Notification onVolumeUnmountable(VolumeInfo volumeInfo) {
        DiskInfo disk = volumeInfo.getDisk();
        return buildNotificationBuilder(volumeInfo, this.mContext.getString(17040425, disk.getDescription()), this.mContext.getString(17040426, disk.getDescription())).setContentIntent(buildInitPendingIntent(volumeInfo)).setCategory("err").build();
    }

    private Notification onVolumeUnmounted(VolumeInfo volumeInfo) {
        return null;
    }

    private void setUsbStorageNotification(int i, int i2, int i3, boolean z, boolean z2, PendingIntent pendingIntent) {
        synchronized (this) {
            Log.d("StorageNotification", "setUsbStorageNotification visible=" + z2 + ",mIsLastVisible=" + this.mIsLastVisible);
            if (z2 || this.mUsbStorageNotification != null) {
                NotificationManager notificationManager = (NotificationManager) this.mContext.getSystemService("notification");
                if (notificationManager == null) {
                    return;
                }
                if (z2) {
                    Resources system = Resources.getSystem();
                    CharSequence text = system.getText(i);
                    CharSequence text2 = system.getText(i2);
                    if (this.mUsbStorageNotification == null) {
                        this.mUsbStorageNotification = new Notification();
                        this.mUsbStorageNotification.icon = i3;
                        this.mUsbStorageNotification.when = 0L;
                        this.mUsbStorageNotification.priority = -2;
                    }
                    if (z) {
                        this.mUsbStorageNotification.defaults |= 1;
                    } else {
                        this.mUsbStorageNotification.defaults &= -2;
                    }
                    this.mUsbStorageNotification.flags = 2;
                    this.mUsbStorageNotification.tickerText = text;
                    PendingIntent pendingIntent2 = pendingIntent;
                    if (pendingIntent == null) {
                        pendingIntent2 = PendingIntent.getBroadcastAsUser(this.mContext, 0, new Intent(), 0, UserHandle.CURRENT);
                    }
                    this.mUsbStorageNotification.color = this.mContext.getResources().getColor(17170521);
                    this.mUsbStorageNotification.setLatestEventInfo(this.mContext, text, text2, pendingIntent2);
                    this.mUsbStorageNotification.visibility = 1;
                    this.mUsbStorageNotification.category = "sys";
                }
                int i4 = this.mUsbStorageNotification.icon;
                if (z2) {
                    notificationManager.notifyAsUser(null, i4, this.mUsbStorageNotification, UserHandle.ALL);
                    this.mIsLastVisible = true;
                } else {
                    notificationManager.cancelAsUser(null, i4, UserHandle.ALL);
                    this.mIsLastVisible = false;
                }
            }
        }
    }

    private int sharableStorageNum() {
        int i = 0;
        for (VolumeInfo volumeInfo : this.mStorageManager.getVolumes()) {
            if (volumeInfo != null && volumeInfo.isAllowUsbMassStorage(ActivityManager.getCurrentUser()) && volumeInfo.getType() == 0 && (volumeInfo.getState() != 6 || volumeInfo.getState() != 7 || volumeInfo.getState() != 8 || volumeInfo.getState() != 4)) {
                i++;
            }
        }
        return i;
    }

    private int sharedStorageNum() {
        int i = 0;
        for (VolumeInfo volumeInfo : this.mStorageManager.getVolumes()) {
            if (volumeInfo != null && volumeInfo.getState() == 9 && volumeInfo.getType() == 0) {
                i++;
            }
        }
        return i;
    }

    private void updateMissingPrivateVolumes() {
        for (VolumeRecord volumeRecord : this.mStorageManager.getVolumeRecords()) {
            if (volumeRecord.getType() == 1) {
                String fsUuid = volumeRecord.getFsUuid();
                VolumeInfo findVolumeByUuid = this.mStorageManager.findVolumeByUuid(fsUuid);
                if ((findVolumeByUuid == null || !findVolumeByUuid.isMountedWritable()) && !volumeRecord.isSnoozed()) {
                    String string = this.mContext.getString(17040438, volumeRecord.getNickname());
                    String string2 = this.mContext.getString(17040439);
                    Notification.Builder deleteIntent = new Notification.Builder(this.mContext).setSmallIcon(17302555).setColor(this.mContext.getColor(17170521)).setContentTitle(string).setContentText(string2).setContentIntent(buildForgetPendingIntent(volumeRecord)).setStyle(new Notification.BigTextStyle().bigText(string2)).setVisibility(1).setLocalOnly(true).setCategory("sys").setDeleteIntent(buildSnoozeIntent(fsUuid));
                    SystemUI.overrideNotificationAppName(this.mContext, deleteIntent);
                    this.mNotificationManager.notifyAsUser(fsUuid, 1397772886, deleteIntent.build(), UserHandle.ALL);
                } else {
                    this.mNotificationManager.cancelAsUser(fsUuid, 1397772886, UserHandle.ALL);
                }
            }
        }
    }

    @Override // com.android.systemui.SystemUI
    public void start() {
        this.mNotificationManager = (NotificationManager) this.mContext.getSystemService(NotificationManager.class);
        this.mStorageManager = (StorageManager) this.mContext.getSystemService(StorageManager.class);
        this.mStorageManager.registerListener(this.mListener);
        this.mContext.registerReceiver(this.mSnoozeReceiver, new IntentFilter("com.android.systemui.action.SNOOZE_VOLUME"), "android.permission.MOUNT_UNMOUNT_FILESYSTEMS", null);
        this.mContext.registerReceiver(this.mFinishReceiver, new IntentFilter("com.android.systemui.action.FINISH_WIZARD"), "android.permission.MOUNT_UNMOUNT_FILESYSTEMS", null);
        this.mContext.registerReceiver(this.mUsbReceiver, new IntentFilter("android.hardware.usb.action.USB_STATE"));
        this.mContext.registerReceiver(this.mUserReceiver, new IntentFilter("android.intent.action.USER_SWITCHED"));
        for (DiskInfo diskInfo : this.mStorageManager.getDisks()) {
            onDiskScannedInternal(diskInfo, diskInfo.volumeCount);
        }
        for (VolumeInfo volumeInfo : this.mStorageManager.getVolumes()) {
            onVolumeStateChangedInternal(volumeInfo);
        }
        this.mContext.getPackageManager().registerMoveCallback(this.mMoveCallback, new Handler());
        updateMissingPrivateVolumes();
    }

    void updateUsbMassStorageNotification() {
        int sharableStorageNum = sharableStorageNum();
        int sharedStorageNum = sharedStorageNum();
        Log.d("StorageNotification", "updateUsbMassStorageNotification - canSharedNum=" + sharableStorageNum + ",sharedNum=" + sharedStorageNum + ",mIsUmsConnect=" + this.mIsUmsConnect + ",mNotifcationState=" + this.mNotifcationState);
        if (this.mIsUmsConnect && sharableStorageNum > 0 && this.mNotifcationState != 1) {
            Log.d("StorageNotification", "updateUsbMassStorageNotification - Turn on noti.");
            this.mNotifcationState = 1;
        } else if (this.mIsUmsConnect && sharedStorageNum > 0 && this.mNotifcationState != 2) {
            Log.d("StorageNotification", "updateUsbMassStorageNotification - Turn off noti.");
            this.mNotifcationState = 2;
        } else if ((this.mIsUmsConnect && sharableStorageNum != 0) || this.mNotifcationState == 0) {
            Log.d("StorageNotification", "updateUsbMassStorageNotification - What?");
        } else {
            Log.d("StorageNotification", "updateUsbMassStorageNotification - Cancel noti.");
            setUsbStorageNotification(0, 0, 0, false, false, null);
            this.mNotifcationState = 0;
        }
    }
}

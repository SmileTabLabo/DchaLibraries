package com.android.browser;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.StatFs;
import android.webkit.WebStorage;
import com.android.browser.preferences.WebsiteSettingsFragment;
import java.io.File;
/* loaded from: classes.dex */
public class WebStorageSizeManager {
    private static long mLastOutOfSpaceNotificationTime = -1;
    private long mAppCacheMaxSize;
    private final Context mContext;
    private DiskInfo mDiskInfo;
    private final long mGlobalLimit = getGlobalLimit();

    /* loaded from: classes.dex */
    public interface AppCacheInfo {
        long getAppCacheSizeBytes();
    }

    /* loaded from: classes.dex */
    public interface DiskInfo {
        long getFreeSpaceSizeBytes();

        long getTotalSizeBytes();
    }

    /* loaded from: classes.dex */
    public static class StatFsDiskInfo implements DiskInfo {
        private StatFs mFs;

        public StatFsDiskInfo(String str) {
            this.mFs = new StatFs(str);
        }

        @Override // com.android.browser.WebStorageSizeManager.DiskInfo
        public long getFreeSpaceSizeBytes() {
            return this.mFs.getAvailableBlocks() * this.mFs.getBlockSize();
        }

        @Override // com.android.browser.WebStorageSizeManager.DiskInfo
        public long getTotalSizeBytes() {
            return this.mFs.getBlockCount() * this.mFs.getBlockSize();
        }
    }

    /* loaded from: classes.dex */
    public static class WebKitAppCacheInfo implements AppCacheInfo {
        private String mAppCachePath;

        public WebKitAppCacheInfo(String str) {
            this.mAppCachePath = str;
        }

        @Override // com.android.browser.WebStorageSizeManager.AppCacheInfo
        public long getAppCacheSizeBytes() {
            return new File(this.mAppCachePath + File.separator + "ApplicationCache.db").length();
        }
    }

    public WebStorageSizeManager(Context context, DiskInfo diskInfo, AppCacheInfo appCacheInfo) {
        this.mContext = context.getApplicationContext();
        this.mDiskInfo = diskInfo;
        this.mAppCacheMaxSize = Math.max(this.mGlobalLimit / 4, appCacheInfo.getAppCacheSizeBytes());
    }

    public long getAppCacheMaxSize() {
        return this.mAppCacheMaxSize;
    }

    public void onExceededDatabaseQuota(String str, String str2, long j, long j2, long j3, WebStorage.QuotaUpdater quotaUpdater) {
        long j4 = (this.mGlobalLimit - j3) - this.mAppCacheMaxSize;
        if (j4 <= 0) {
            if (j3 > 0) {
                scheduleOutOfSpaceNotification();
            }
            quotaUpdater.updateQuota(j);
            return;
        }
        if (j != 0) {
            if (j2 == 0) {
                j2 = Math.min(1048576L, j4);
            }
            long j5 = j + j2;
            if (j2 <= j4) {
                j = j5;
            }
        } else {
            j = j4 >= j2 ? j2 : 0L;
        }
        quotaUpdater.updateQuota(j);
    }

    public void onReachedMaxAppCacheSize(long j, long j2, WebStorage.QuotaUpdater quotaUpdater) {
        long j3 = j + 524288;
        if ((this.mGlobalLimit - j2) - this.mAppCacheMaxSize < j3) {
            if (j2 > 0) {
                scheduleOutOfSpaceNotification();
            }
            quotaUpdater.updateQuota(0L);
            return;
        }
        this.mAppCacheMaxSize += j3;
        quotaUpdater.updateQuota(this.mAppCacheMaxSize);
    }

    public static void resetLastOutOfSpaceNotificationTime() {
        mLastOutOfSpaceNotificationTime = (System.currentTimeMillis() - 300000) + 3000;
    }

    private long getGlobalLimit() {
        return calculateGlobalLimit(this.mDiskInfo.getTotalSizeBytes(), this.mDiskInfo.getFreeSpaceSizeBytes());
    }

    static long calculateGlobalLimit(long j, long j2) {
        if (j <= 0 || j2 <= 0 || j2 > j) {
            return 0L;
        }
        long min = (long) Math.min(Math.floor(j / (2 << ((int) Math.floor(Math.log10(j / 1048576))))), Math.floor(j2 / 2));
        if (min < 1048576) {
            return 0L;
        }
        return 1048576 * ((min / 1048576) + (min % 1048576 != 0 ? 1L : 0L));
    }

    private void scheduleOutOfSpaceNotification() {
        if (mLastOutOfSpaceNotificationTime == -1 || System.currentTimeMillis() - mLastOutOfSpaceNotificationTime > 300000) {
            String string = this.mContext.getString(R.string.webstorage_outofspace_notification_title);
            String string2 = this.mContext.getString(R.string.webstorage_outofspace_notification_text);
            long currentTimeMillis = System.currentTimeMillis();
            Intent intent = new Intent(this.mContext, BrowserPreferencesPage.class);
            intent.putExtra(":android:show_fragment", WebsiteSettingsFragment.class.getName());
            Notification build = new Notification.Builder(this.mContext).setContentTitle(string).setContentText(string2).setSmallIcon(17301642).setWhen(currentTimeMillis).setContentIntent(PendingIntent.getActivity(this.mContext, 0, intent, 0)).build();
            build.flags |= 16;
            NotificationManager notificationManager = (NotificationManager) this.mContext.getSystemService("notification");
            if (notificationManager != null) {
                mLastOutOfSpaceNotificationTime = System.currentTimeMillis();
                notificationManager.notify(1, build);
            }
        }
    }
}

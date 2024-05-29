package com.android.launcher3;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import com.android.launcher3.model.GridSizeMigrationTask;
import java.io.IOException;
/* loaded from: a.zip:com/android/launcher3/LauncherBackupAgentHelper.class */
public class LauncherBackupAgentHelper extends BackupAgentHelper {
    private LauncherBackupHelper mHelper;

    public static void dataChanged(Context context) {
        dataChanged(context, 0L);
    }

    public static void dataChanged(Context context, long j) {
        SharedPreferences prefs = Utilities.getPrefs(context);
        long currentTimeMillis = System.currentTimeMillis();
        long j2 = prefs.getLong("backup_manager_last_notified", 0L);
        if (currentTimeMillis < j2 || currentTimeMillis >= j2 + j) {
            BackupManager.dataChanged(context.getPackageName());
            prefs.edit().putLong("backup_manager_last_notified", currentTimeMillis).apply();
        }
    }

    @Override // android.app.backup.BackupAgent
    public void onCreate() {
        super.onCreate();
        this.mHelper = new LauncherBackupHelper(this);
        addHelper("L", this.mHelper);
    }

    @Override // android.app.backup.BackupAgentHelper, android.app.backup.BackupAgent
    public void onRestore(BackupDataInput backupDataInput, int i, ParcelFileDescriptor parcelFileDescriptor) throws IOException {
        boolean z;
        if (!Utilities.ATLEAST_LOLLIPOP) {
            Log.i("LauncherBAHelper", "You shall not pass!!!");
            Log.d("LauncherBAHelper", "Restore is only supported on devices running Lollipop and above.");
            return;
        }
        LauncherAppState.getLauncherProvider().createEmptyDB();
        try {
            super.onRestore(backupDataInput, i, parcelFileDescriptor);
            Cursor query = getContentResolver().query(LauncherSettings$Favorites.CONTENT_URI, null, null, null, null);
            z = query.moveToNext();
            query.close();
        } catch (Exception e) {
            Log.e("LauncherBAHelper", "Restore failed", e);
            z = false;
        }
        if (!z || !this.mHelper.restoreSuccessful) {
            LauncherAppState.getLauncherProvider().createEmptyDB();
            return;
        }
        LauncherAppState.getLauncherProvider().clearFlagEmptyDbCreated();
        LauncherClings.markFirstRunClingDismissed(this);
        if (this.mHelper.restoredBackupVersion <= 3) {
            LauncherAppState.getLauncherProvider().updateFolderItemsRank();
        }
        if (GridSizeMigrationTask.ENABLED && this.mHelper.shouldAttemptWorkspaceMigration()) {
            GridSizeMigrationTask.markForMigration(getApplicationContext(), this.mHelper.widgetSizes, this.mHelper.migrationCompatibleProfileData);
        }
        LauncherAppState.getLauncherProvider().convertShortcutsToLauncherActivities();
    }
}

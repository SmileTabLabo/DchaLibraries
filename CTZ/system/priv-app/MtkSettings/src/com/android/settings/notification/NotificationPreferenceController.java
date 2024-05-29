package com.android.settings.notification;

import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.UserManager;
import android.util.Log;
import com.android.settings.notification.NotificationBackend;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.core.AbstractPreferenceController;
import java.util.Objects;
/* loaded from: classes.dex */
public abstract class NotificationPreferenceController extends AbstractPreferenceController {
    protected RestrictedLockUtils.EnforcedAdmin mAdmin;
    protected NotificationBackend.AppRow mAppRow;
    protected final NotificationBackend mBackend;
    protected NotificationChannel mChannel;
    protected NotificationChannelGroup mChannelGroup;
    protected final Context mContext;
    protected final NotificationManager mNm;
    protected final PackageManager mPm;
    protected final UserManager mUm;

    public NotificationPreferenceController(Context context, NotificationBackend notificationBackend) {
        super(context);
        this.mContext = context;
        this.mNm = (NotificationManager) this.mContext.getSystemService("notification");
        this.mBackend = notificationBackend;
        this.mUm = (UserManager) this.mContext.getSystemService("user");
        this.mPm = this.mContext.getPackageManager();
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public boolean isAvailable() {
        if (this.mAppRow == null || this.mAppRow.banned) {
            return false;
        }
        if (this.mChannel != null) {
            return this.mChannel.getImportance() != 0;
        } else if (this.mChannelGroup != null) {
            return !this.mChannelGroup.isBlocked();
        } else {
            return true;
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onResume(NotificationBackend.AppRow appRow, NotificationChannel notificationChannel, NotificationChannelGroup notificationChannelGroup, RestrictedLockUtils.EnforcedAdmin enforcedAdmin) {
        this.mAppRow = appRow;
        this.mChannel = notificationChannel;
        this.mChannelGroup = notificationChannelGroup;
        this.mAdmin = enforcedAdmin;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean checkCanBeVisible(int i) {
        if (this.mChannel == null) {
            Log.w("ChannelPrefContr", "No channel");
            return false;
        }
        int importance = this.mChannel.getImportance();
        return importance == -1000 || importance >= i;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void saveChannel() {
        if (this.mChannel != null && this.mAppRow != null) {
            this.mBackend.updateChannel(this.mAppRow.pkg, this.mAppRow.uid, this.mChannel);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean isChannelConfigurable() {
        if (this.mChannel != null && this.mAppRow != null) {
            return !Objects.equals(this.mChannel.getId(), this.mAppRow.lockedChannelId);
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean isChannelBlockable() {
        if (this.mChannel == null || this.mAppRow == null) {
            return false;
        }
        if (this.mAppRow.systemApp) {
            return this.mChannel.isBlockableSystem() || this.mChannel.getImportance() == 0;
        }
        return true;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean isChannelGroupBlockable() {
        if (this.mChannelGroup != null && this.mAppRow != null) {
            if (!this.mAppRow.systemApp) {
                return true;
            }
            return this.mChannelGroup.isBlocked();
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean hasValidGroup() {
        return this.mChannelGroup != null;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public final boolean isDefaultChannel() {
        if (this.mChannel == null) {
            return false;
        }
        return Objects.equals("miscellaneous", this.mChannel.getId());
    }
}

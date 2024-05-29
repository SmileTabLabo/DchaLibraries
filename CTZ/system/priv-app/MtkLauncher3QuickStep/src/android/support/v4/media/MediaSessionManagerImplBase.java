package android.support.v4.media;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaSessionManager;
import android.support.v4.util.ObjectsCompat;
import android.text.TextUtils;
import android.util.Log;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class MediaSessionManagerImplBase implements MediaSessionManager.MediaSessionManagerImpl {
    private static final boolean DEBUG = MediaSessionManager.DEBUG;
    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String PERMISSION_MEDIA_CONTENT_CONTROL = "android.permission.MEDIA_CONTENT_CONTROL";
    private static final String PERMISSION_STATUS_BAR_SERVICE = "android.permission.STATUS_BAR_SERVICE";
    private static final String TAG = "MediaSessionManager";
    ContentResolver mContentResolver;
    Context mContext;

    /* JADX INFO: Access modifiers changed from: package-private */
    public MediaSessionManagerImplBase(Context context) {
        this.mContext = context;
        this.mContentResolver = this.mContext.getContentResolver();
    }

    @Override // android.support.v4.media.MediaSessionManager.MediaSessionManagerImpl
    public Context getContext() {
        return this.mContext;
    }

    @Override // android.support.v4.media.MediaSessionManager.MediaSessionManagerImpl
    public boolean isTrustedForMediaControl(@NonNull MediaSessionManager.RemoteUserInfoImpl userInfo) {
        try {
            ApplicationInfo applicationInfo = this.mContext.getPackageManager().getApplicationInfo(userInfo.getPackageName(), 0);
            if (applicationInfo.uid == userInfo.getUid()) {
                return isPermissionGranted(userInfo, PERMISSION_STATUS_BAR_SERVICE) || isPermissionGranted(userInfo, PERMISSION_MEDIA_CONTENT_CONTROL) || userInfo.getUid() == 1000 || isEnabledNotificationListener(userInfo);
            }
            if (DEBUG) {
                Log.d(TAG, "Package name " + userInfo.getPackageName() + " doesn't match with the uid " + userInfo.getUid());
            }
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            if (DEBUG) {
                Log.d(TAG, "Package " + userInfo.getPackageName() + " doesn't exist");
            }
            return false;
        }
    }

    private boolean isPermissionGranted(MediaSessionManager.RemoteUserInfoImpl userInfo, String permission) {
        return userInfo.getPid() < 0 ? this.mContext.getPackageManager().checkPermission(permission, userInfo.getPackageName()) == 0 : this.mContext.checkPermission(permission, userInfo.getPid(), userInfo.getUid()) == 0;
    }

    boolean isEnabledNotificationListener(@NonNull MediaSessionManager.RemoteUserInfoImpl userInfo) {
        String enabledNotifListeners = Settings.Secure.getString(this.mContentResolver, ENABLED_NOTIFICATION_LISTENERS);
        if (enabledNotifListeners != null) {
            String[] components = enabledNotifListeners.split(":");
            for (String str : components) {
                ComponentName component = ComponentName.unflattenFromString(str);
                if (component != null && component.getPackageName().equals(userInfo.getPackageName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /* loaded from: classes.dex */
    static class RemoteUserInfo implements MediaSessionManager.RemoteUserInfoImpl {
        private String mPackageName;
        private int mPid;
        private int mUid;

        /* JADX INFO: Access modifiers changed from: package-private */
        public RemoteUserInfo(String packageName, int pid, int uid) {
            this.mPackageName = packageName;
            this.mPid = pid;
            this.mUid = uid;
        }

        @Override // android.support.v4.media.MediaSessionManager.RemoteUserInfoImpl
        public String getPackageName() {
            return this.mPackageName;
        }

        @Override // android.support.v4.media.MediaSessionManager.RemoteUserInfoImpl
        public int getPid() {
            return this.mPid;
        }

        @Override // android.support.v4.media.MediaSessionManager.RemoteUserInfoImpl
        public int getUid() {
            return this.mUid;
        }

        public boolean equals(Object obj) {
            if (obj instanceof RemoteUserInfo) {
                RemoteUserInfo otherUserInfo = (RemoteUserInfo) obj;
                return TextUtils.equals(this.mPackageName, otherUserInfo.mPackageName) && this.mPid == otherUserInfo.mPid && this.mUid == otherUserInfo.mUid;
            }
            return false;
        }

        public int hashCode() {
            return ObjectsCompat.hash(this.mPackageName, Integer.valueOf(this.mPid), Integer.valueOf(this.mUid));
        }
    }
}

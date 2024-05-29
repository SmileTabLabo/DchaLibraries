package android.support.v4.media;

import android.support.v4.media.MediaSessionManager;
import android.support.v4.util.ObjectsCompat;
import android.text.TextUtils;
/* loaded from: classes.dex */
class MediaSessionManagerImplBase {
    private static final boolean DEBUG = MediaSessionManager.DEBUG;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static class RemoteUserInfo implements MediaSessionManager.RemoteUserInfoImpl {
        private String mPackageName;
        private int mPid;
        private int mUid;

        /* JADX INFO: Access modifiers changed from: package-private */
        public RemoteUserInfo(String packageName, int pid, int uid) {
            this.mPackageName = packageName;
            this.mPid = pid;
            this.mUid = uid;
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

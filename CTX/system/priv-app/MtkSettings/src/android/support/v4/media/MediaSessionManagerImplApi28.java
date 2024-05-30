package android.support.v4.media;

import android.media.session.MediaSessionManager;
import android.support.v4.media.MediaSessionManager;
/* loaded from: classes.dex */
class MediaSessionManagerImplApi28 extends MediaSessionManagerImplApi21 {

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static final class RemoteUserInfo implements MediaSessionManager.RemoteUserInfoImpl {
        MediaSessionManager.RemoteUserInfo mObject;

        /* JADX INFO: Access modifiers changed from: package-private */
        public RemoteUserInfo(String packageName, int pid, int uid) {
            this.mObject = new MediaSessionManager.RemoteUserInfo(packageName, pid, uid);
        }
    }
}

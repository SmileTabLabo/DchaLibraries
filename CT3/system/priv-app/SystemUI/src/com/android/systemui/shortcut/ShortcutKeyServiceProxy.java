package com.android.systemui.shortcut;

import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import com.android.internal.policy.IShortcutService;
/* loaded from: a.zip:com/android/systemui/shortcut/ShortcutKeyServiceProxy.class */
public class ShortcutKeyServiceProxy extends IShortcutService.Stub {
    private Callbacks mCallbacks;
    private final Object mLock = new Object();
    private final Handler mHandler = new H(this, null);

    /* loaded from: a.zip:com/android/systemui/shortcut/ShortcutKeyServiceProxy$Callbacks.class */
    public interface Callbacks {
        void onShortcutKeyPressed(long j);
    }

    /* loaded from: a.zip:com/android/systemui/shortcut/ShortcutKeyServiceProxy$H.class */
    private final class H extends Handler {
        final ShortcutKeyServiceProxy this$0;

        private H(ShortcutKeyServiceProxy shortcutKeyServiceProxy) {
            this.this$0 = shortcutKeyServiceProxy;
        }

        /* synthetic */ H(ShortcutKeyServiceProxy shortcutKeyServiceProxy, H h) {
            this(shortcutKeyServiceProxy);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    this.this$0.mCallbacks.onShortcutKeyPressed(((Long) message.obj).longValue());
                    return;
                default:
                    return;
            }
        }
    }

    public ShortcutKeyServiceProxy(Callbacks callbacks) {
        this.mCallbacks = callbacks;
    }

    public void notifyShortcutKeyPressed(long j) throws RemoteException {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(1, Long.valueOf(j)).sendToTarget();
        }
    }
}

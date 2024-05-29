package com.android.systemui.recents;

import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import com.android.internal.os.SomeArgs;
import com.android.systemui.recents.IRecentsNonSystemUserCallbacks;
/* loaded from: a.zip:com/android/systemui/recents/RecentsImplProxy.class */
public class RecentsImplProxy extends IRecentsNonSystemUserCallbacks.Stub {
    private final Handler mHandler = new Handler(this) { // from class: com.android.systemui.recents.RecentsImplProxy.1
        final RecentsImplProxy this$0;

        {
            this.this$0 = this;
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            boolean z = true;
            boolean z2 = true;
            switch (message.what) {
                case 1:
                    this.this$0.mImpl.preloadRecents();
                    break;
                case 2:
                    this.this$0.mImpl.cancelPreloadingRecents();
                    break;
                case 3:
                    SomeArgs someArgs = (SomeArgs) message.obj;
                    RecentsImpl recentsImpl = this.this$0.mImpl;
                    boolean z3 = someArgs.argi1 != 0;
                    boolean z4 = someArgs.argi2 != 0;
                    boolean z5 = someArgs.argi3 != 0;
                    boolean z6 = someArgs.argi4 != 0;
                    if (someArgs.argi5 == 0) {
                        z2 = false;
                    }
                    recentsImpl.showRecents(z3, z4, z5, z6, z2, someArgs.argi6);
                    break;
                case 4:
                    RecentsImpl recentsImpl2 = this.this$0.mImpl;
                    boolean z7 = message.arg1 != 0;
                    if (message.arg2 == 0) {
                        z = false;
                    }
                    recentsImpl2.hideRecents(z7, z);
                    break;
                case 5:
                    this.this$0.mImpl.toggleRecents(((SomeArgs) message.obj).argi1);
                    break;
                case 6:
                    this.this$0.mImpl.onConfigurationChanged();
                    break;
                case 7:
                    SomeArgs someArgs2 = (SomeArgs) message.obj;
                    RecentsImpl recentsImpl3 = this.this$0.mImpl;
                    int i = someArgs2.argi1;
                    int i2 = someArgs2.argi2;
                    someArgs2.argi3 = 0;
                    recentsImpl3.dockTopTask(i, i2, 0, (Rect) someArgs2.arg1);
                    break;
                case 8:
                    this.this$0.mImpl.onDraggingInRecents(((Float) message.obj).floatValue());
                    break;
                case 9:
                    this.this$0.mImpl.onDraggingInRecentsEnded(((Float) message.obj).floatValue());
                    break;
                default:
                    super.handleMessage(message);
                    break;
            }
            super.handleMessage(message);
        }
    };
    private RecentsImpl mImpl;

    public RecentsImplProxy(RecentsImpl recentsImpl) {
        this.mImpl = recentsImpl;
    }

    @Override // com.android.systemui.recents.IRecentsNonSystemUserCallbacks
    public void cancelPreloadingRecents() throws RemoteException {
        this.mHandler.sendEmptyMessage(2);
    }

    @Override // com.android.systemui.recents.IRecentsNonSystemUserCallbacks
    public void dockTopTask(int i, int i2, int i3, Rect rect) throws RemoteException {
        SomeArgs obtain = SomeArgs.obtain();
        obtain.argi1 = i;
        obtain.argi2 = i2;
        obtain.argi3 = i3;
        obtain.arg1 = rect;
        this.mHandler.sendMessage(this.mHandler.obtainMessage(7, obtain));
    }

    @Override // com.android.systemui.recents.IRecentsNonSystemUserCallbacks
    public void hideRecents(boolean z, boolean z2) throws RemoteException {
        int i = 1;
        Handler handler = this.mHandler;
        Handler handler2 = this.mHandler;
        int i2 = z ? 1 : 0;
        if (!z2) {
            i = 0;
        }
        handler.sendMessage(handler2.obtainMessage(4, i2, i));
    }

    @Override // com.android.systemui.recents.IRecentsNonSystemUserCallbacks
    public void onConfigurationChanged() throws RemoteException {
        this.mHandler.sendEmptyMessage(6);
    }

    @Override // com.android.systemui.recents.IRecentsNonSystemUserCallbacks
    public void onDraggingInRecents(float f) throws RemoteException {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(8, Float.valueOf(f)));
    }

    @Override // com.android.systemui.recents.IRecentsNonSystemUserCallbacks
    public void onDraggingInRecentsEnded(float f) throws RemoteException {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(9, Float.valueOf(f)));
    }

    @Override // com.android.systemui.recents.IRecentsNonSystemUserCallbacks
    public void preloadRecents() throws RemoteException {
        this.mHandler.sendEmptyMessage(1);
    }

    @Override // com.android.systemui.recents.IRecentsNonSystemUserCallbacks
    public void showRecents(boolean z, boolean z2, boolean z3, boolean z4, boolean z5, int i) throws RemoteException {
        SomeArgs obtain = SomeArgs.obtain();
        obtain.argi1 = z ? 1 : 0;
        obtain.argi2 = z2 ? 1 : 0;
        obtain.argi3 = z3 ? 1 : 0;
        obtain.argi4 = z4 ? 1 : 0;
        obtain.argi5 = z5 ? 1 : 0;
        obtain.argi6 = i;
        this.mHandler.sendMessage(this.mHandler.obtainMessage(3, obtain));
    }

    @Override // com.android.systemui.recents.IRecentsNonSystemUserCallbacks
    public void toggleRecents(int i) throws RemoteException {
        SomeArgs obtain = SomeArgs.obtain();
        obtain.argi1 = i;
        this.mHandler.sendMessage(this.mHandler.obtainMessage(5, obtain));
    }
}

package com.android.systemui.recents;

import android.content.Context;
import android.graphics.Rect;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.EventLog;
import android.util.Log;
import android.util.SparseArray;
import com.android.systemui.recents.IRecentsNonSystemUserCallbacks;
import com.android.systemui.recents.IRecentsSystemUserCallbacks;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.activity.DockedTopTaskEvent;
import com.android.systemui.recents.events.activity.RecentsActivityStartingEvent;
import com.android.systemui.recents.events.ui.RecentsDrawnEvent;
import com.android.systemui.recents.misc.ForegroundThread;
/* loaded from: a.zip:com/android/systemui/recents/RecentsSystemUser.class */
public class RecentsSystemUser extends IRecentsSystemUserCallbacks.Stub {
    private Context mContext;
    private RecentsImpl mImpl;
    private final SparseArray<IRecentsNonSystemUserCallbacks> mNonSystemUserRecents = new SparseArray<>();

    public RecentsSystemUser(Context context, RecentsImpl recentsImpl) {
        this.mContext = context;
        this.mImpl = recentsImpl;
    }

    /* renamed from: -com_android_systemui_recents_RecentsSystemUser_lambda$1  reason: not valid java name */
    /* synthetic */ void m1235com_android_systemui_recents_RecentsSystemUser_lambda$1(boolean z) {
        this.mImpl.onVisibilityChanged(this.mContext, z);
    }

    /* renamed from: -com_android_systemui_recents_RecentsSystemUser_lambda$2  reason: not valid java name */
    /* synthetic */ void m1236com_android_systemui_recents_RecentsSystemUser_lambda$2(int i) {
        this.mImpl.onStartScreenPinning(this.mContext, i);
    }

    public IRecentsNonSystemUserCallbacks getNonSystemUserRecentsForUser(int i) {
        return this.mNonSystemUserRecents.get(i);
    }

    @Override // com.android.systemui.recents.IRecentsSystemUserCallbacks
    public void registerNonSystemUserCallbacks(IBinder iBinder, int i) {
        try {
            IRecentsNonSystemUserCallbacks asInterface = IRecentsNonSystemUserCallbacks.Stub.asInterface(iBinder);
            iBinder.linkToDeath(new IBinder.DeathRecipient(this, asInterface, i) { // from class: com.android.systemui.recents.RecentsSystemUser.1
                final RecentsSystemUser this$0;
                final IRecentsNonSystemUserCallbacks val$callback;
                final int val$userId;

                {
                    this.this$0 = this;
                    this.val$callback = asInterface;
                    this.val$userId = i;
                }

                @Override // android.os.IBinder.DeathRecipient
                public void binderDied() {
                    this.this$0.mNonSystemUserRecents.removeAt(this.this$0.mNonSystemUserRecents.indexOfValue(this.val$callback));
                    EventLog.writeEvent(36060, 5, Integer.valueOf(this.val$userId));
                }
            }, 0);
            this.mNonSystemUserRecents.put(i, asInterface);
            EventLog.writeEvent(36060, 4, Integer.valueOf(i));
        } catch (RemoteException e) {
            Log.e("RecentsSystemUser", "Failed to register NonSystemUserCallbacks", e);
        }
    }

    @Override // com.android.systemui.recents.IRecentsSystemUserCallbacks
    public void sendDockingTopTaskEvent(int i, Rect rect) throws RemoteException {
        EventBus.getDefault().post(new DockedTopTaskEvent(i, rect));
    }

    @Override // com.android.systemui.recents.IRecentsSystemUserCallbacks
    public void sendLaunchRecentsEvent() throws RemoteException {
        EventBus.getDefault().post(new RecentsActivityStartingEvent());
    }

    @Override // com.android.systemui.recents.IRecentsSystemUserCallbacks
    public void sendRecentsDrawnEvent() {
        EventBus.getDefault().post(new RecentsDrawnEvent());
    }

    @Override // com.android.systemui.recents.IRecentsSystemUserCallbacks
    public void startScreenPinning(int i) {
        ForegroundThread.getHandler().post(new Runnable(this, i) { // from class: com.android.systemui.recents.RecentsSystemUser._void_startScreenPinning_int_taskId_LambdaImpl0
            private int val$taskId;
            private RecentsSystemUser val$this;

            {
                this.val$this = this;
                this.val$taskId = i;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.val$this.m1236com_android_systemui_recents_RecentsSystemUser_lambda$2(this.val$taskId);
            }
        });
    }

    @Override // com.android.systemui.recents.IRecentsSystemUserCallbacks
    public void updateRecentsVisibility(boolean z) {
        ForegroundThread.getHandler().post(new Runnable(this, z) { // from class: com.android.systemui.recents.RecentsSystemUser._void_updateRecentsVisibility_boolean_visible_LambdaImpl0
            private RecentsSystemUser val$this;
            private boolean val$visible;

            {
                this.val$this = this;
                this.val$visible = z;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.val$this.m1235com_android_systemui_recents_RecentsSystemUser_lambda$1(this.val$visible);
            }
        });
    }
}

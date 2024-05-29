package com.android.systemui;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import android.view.SurfaceControl;
import com.android.systemui.OverviewProxyService;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.activity.DockedFirstAnimationFrameEvent;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.shared.recents.IOverviewProxy;
import com.android.systemui.shared.recents.ISystemUiProxy;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.GraphicBufferCompat;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.CallbackController;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
/* loaded from: classes.dex */
public class OverviewProxyService implements Dumpable, CallbackController<OverviewProxyListener> {
    private final Context mContext;
    private int mInteractionFlags;
    private boolean mIsEnabled;
    private IOverviewProxy mOverviewProxy;
    private final Intent mQuickStepIntent;
    private final ComponentName mRecentsComponentName;
    private final Runnable mConnectionRunnable = new Runnable() { // from class: com.android.systemui.-$$Lambda$OverviewProxyService$iQ_AhE_jQV1-6xCMm0AowIu_bDY
        @Override // java.lang.Runnable
        public final void run() {
            OverviewProxyService.this.internalConnectToCurrentUser();
        }
    };
    private final DeviceProvisionedController mDeviceProvisionedController = (DeviceProvisionedController) Dependency.get(DeviceProvisionedController.class);
    private final List<OverviewProxyListener> mConnectionCallbacks = new ArrayList();
    private ISystemUiProxy mSysUiProxy = new AnonymousClass1();
    private final Runnable mDeferredConnectionCallback = new Runnable() { // from class: com.android.systemui.-$$Lambda$OverviewProxyService$SlBFfY-D9O-Us5sbpzzPXvtyKy4
        @Override // java.lang.Runnable
        public final void run() {
            OverviewProxyService.lambda$new$0(OverviewProxyService.this);
        }
    };
    private final BroadcastReceiver mLauncherStateChangedReceiver = new BroadcastReceiver() { // from class: com.android.systemui.OverviewProxyService.2
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            OverviewProxyService.this.updateEnabledState();
            if (!OverviewProxyService.this.isEnabled()) {
                OverviewProxyService.this.mInteractionFlags = 0;
                Prefs.remove(OverviewProxyService.this.mContext, "QuickStepInteractionFlags");
            }
            OverviewProxyService.this.startConnectionToCurrentUser();
        }
    };
    private final ServiceConnection mOverviewServiceConnection = new ServiceConnection() { // from class: com.android.systemui.OverviewProxyService.3
        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            OverviewProxyService.this.mHandler.removeCallbacks(OverviewProxyService.this.mDeferredConnectionCallback);
            OverviewProxyService.this.mConnectionBackoffAttempts = 0;
            OverviewProxyService.this.mOverviewProxy = IOverviewProxy.Stub.asInterface(iBinder);
            try {
                iBinder.linkToDeath(OverviewProxyService.this.mOverviewServiceDeathRcpt, 0);
            } catch (RemoteException e) {
                Log.e("OverviewProxyService", "Lost connection to launcher service", e);
            }
            try {
                OverviewProxyService.this.mOverviewProxy.onBind(OverviewProxyService.this.mSysUiProxy);
            } catch (RemoteException e2) {
                Log.e("OverviewProxyService", "Failed to call onBind()", e2);
            }
            OverviewProxyService.this.notifyConnectionChanged();
        }

        @Override // android.content.ServiceConnection
        public void onNullBinding(ComponentName componentName) {
            Log.w("OverviewProxyService", "Null binding of '" + componentName + "', try reconnecting");
            OverviewProxyService.this.internalConnectToCurrentUser();
        }

        @Override // android.content.ServiceConnection
        public void onBindingDied(ComponentName componentName) {
            Log.w("OverviewProxyService", "Binding died of '" + componentName + "', try reconnecting");
            OverviewProxyService.this.internalConnectToCurrentUser();
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };
    private final DeviceProvisionedController.DeviceProvisionedListener mDeviceProvisionedCallback = new DeviceProvisionedController.DeviceProvisionedListener() { // from class: com.android.systemui.OverviewProxyService.4
        @Override // com.android.systemui.statusbar.policy.DeviceProvisionedController.DeviceProvisionedListener
        public void onUserSetupChanged() {
            if (OverviewProxyService.this.mDeviceProvisionedController.isCurrentUserSetup()) {
                OverviewProxyService.this.internalConnectToCurrentUser();
            }
        }

        @Override // com.android.systemui.statusbar.policy.DeviceProvisionedController.DeviceProvisionedListener
        public void onUserSwitched() {
            OverviewProxyService.this.mConnectionBackoffAttempts = 0;
            OverviewProxyService.this.internalConnectToCurrentUser();
        }
    };
    private final IBinder.DeathRecipient mOverviewServiceDeathRcpt = new IBinder.DeathRecipient() { // from class: com.android.systemui.-$$Lambda$McHnOU5IdjMu78SRtgrSsSZOLVw
        @Override // android.os.IBinder.DeathRecipient
        public final void binderDied() {
            OverviewProxyService.this.startConnectionToCurrentUser();
        }
    };
    private final Handler mHandler = new Handler();
    private int mConnectionBackoffAttempts = 0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.android.systemui.OverviewProxyService$1  reason: invalid class name */
    /* loaded from: classes.dex */
    public class AnonymousClass1 extends ISystemUiProxy.Stub {
        AnonymousClass1() {
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public GraphicBufferCompat screenshot(Rect rect, int i, int i2, int i3, int i4, boolean z, int i5) {
            long clearCallingIdentity = Binder.clearCallingIdentity();
            try {
                return new GraphicBufferCompat(SurfaceControl.screenshotToBuffer(rect, i, i2, i3, i4, z, i5));
            } finally {
                Binder.restoreCallingIdentity(clearCallingIdentity);
            }
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void startScreenPinning(final int i) {
            long clearCallingIdentity = Binder.clearCallingIdentity();
            try {
                OverviewProxyService.this.mHandler.post(new Runnable() { // from class: com.android.systemui.-$$Lambda$OverviewProxyService$1$zYh14hGzctRPjhdkAv_VxvbnwC4
                    @Override // java.lang.Runnable
                    public final void run() {
                        OverviewProxyService.AnonymousClass1.lambda$startScreenPinning$0(OverviewProxyService.AnonymousClass1.this, i);
                    }
                });
            } finally {
                Binder.restoreCallingIdentity(clearCallingIdentity);
            }
        }

        public static /* synthetic */ void lambda$startScreenPinning$0(AnonymousClass1 anonymousClass1, int i) {
            StatusBar statusBar = (StatusBar) ((SystemUIApplication) OverviewProxyService.this.mContext).getComponent(StatusBar.class);
            if (statusBar != null) {
                statusBar.showScreenPinningRequest(i, false);
            }
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void onSplitScreenInvoked() {
            long clearCallingIdentity = Binder.clearCallingIdentity();
            try {
                EventBus.getDefault().post(new DockedFirstAnimationFrameEvent());
            } finally {
                Binder.restoreCallingIdentity(clearCallingIdentity);
            }
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void onOverviewShown(final boolean z) {
            long clearCallingIdentity = Binder.clearCallingIdentity();
            try {
                OverviewProxyService.this.mHandler.post(new Runnable() { // from class: com.android.systemui.-$$Lambda$OverviewProxyService$1$2pq3hQvAlKaOK9NXRBZWEO44FEQ
                    @Override // java.lang.Runnable
                    public final void run() {
                        OverviewProxyService.AnonymousClass1.lambda$onOverviewShown$1(OverviewProxyService.AnonymousClass1.this, z);
                    }
                });
            } finally {
                Binder.restoreCallingIdentity(clearCallingIdentity);
            }
        }

        public static /* synthetic */ void lambda$onOverviewShown$1(AnonymousClass1 anonymousClass1, boolean z) {
            for (int size = OverviewProxyService.this.mConnectionCallbacks.size() - 1; size >= 0; size--) {
                ((OverviewProxyListener) OverviewProxyService.this.mConnectionCallbacks.get(size)).onOverviewShown(z);
            }
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void setInteractionState(final int i) {
            long clearCallingIdentity = Binder.clearCallingIdentity();
            try {
                if (OverviewProxyService.this.mInteractionFlags != i) {
                    OverviewProxyService.this.mInteractionFlags = i;
                    OverviewProxyService.this.mHandler.post(new Runnable() { // from class: com.android.systemui.-$$Lambda$OverviewProxyService$1$OIMBB4z7B0rilnGoiEee2hjNc3M
                        @Override // java.lang.Runnable
                        public final void run() {
                            OverviewProxyService.AnonymousClass1.lambda$setInteractionState$2(OverviewProxyService.AnonymousClass1.this, i);
                        }
                    });
                }
                Prefs.putInt(OverviewProxyService.this.mContext, "QuickStepInteractionFlags", OverviewProxyService.this.mInteractionFlags);
                Binder.restoreCallingIdentity(clearCallingIdentity);
            } catch (Throwable th) {
                Prefs.putInt(OverviewProxyService.this.mContext, "QuickStepInteractionFlags", OverviewProxyService.this.mInteractionFlags);
                Binder.restoreCallingIdentity(clearCallingIdentity);
                throw th;
            }
        }

        public static /* synthetic */ void lambda$setInteractionState$2(AnonymousClass1 anonymousClass1, int i) {
            for (int size = OverviewProxyService.this.mConnectionCallbacks.size() - 1; size >= 0; size--) {
                ((OverviewProxyListener) OverviewProxyService.this.mConnectionCallbacks.get(size)).onInteractionFlagsChanged(i);
            }
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public Rect getNonMinimizedSplitScreenSecondaryBounds() {
            long clearCallingIdentity = Binder.clearCallingIdentity();
            try {
                Divider divider = (Divider) ((SystemUIApplication) OverviewProxyService.this.mContext).getComponent(Divider.class);
                if (divider != null) {
                    return divider.getView().getNonMinimizedSplitScreenSecondaryBounds();
                }
                return null;
            } finally {
                Binder.restoreCallingIdentity(clearCallingIdentity);
            }
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void setBackButtonAlpha(final float f, final boolean z) {
            long clearCallingIdentity = Binder.clearCallingIdentity();
            try {
                OverviewProxyService.this.mHandler.post(new Runnable() { // from class: com.android.systemui.-$$Lambda$OverviewProxyService$1$n8i2zr6lzYUUiPl7iRdxrhsa5Wk
                    @Override // java.lang.Runnable
                    public final void run() {
                        OverviewProxyService.this.notifyBackButtonAlphaChanged(f, z);
                    }
                });
            } finally {
                Binder.restoreCallingIdentity(clearCallingIdentity);
            }
        }
    }

    public static /* synthetic */ void lambda$new$0(OverviewProxyService overviewProxyService) {
        Log.w("OverviewProxyService", "Binder supposed established connection but actual connection to service timed out, trying again");
        overviewProxyService.internalConnectToCurrentUser();
    }

    public OverviewProxyService(Context context) {
        this.mContext = context;
        this.mRecentsComponentName = ComponentName.unflattenFromString(context.getString(17039708));
        this.mQuickStepIntent = new Intent("android.intent.action.QUICKSTEP_SERVICE").setPackage(this.mRecentsComponentName.getPackageName());
        this.mInteractionFlags = Prefs.getInt(this.mContext, "QuickStepInteractionFlags", 0);
        if (SystemServicesProxy.getInstance(context).isSystemUser(this.mDeviceProvisionedController.getCurrentUser())) {
            updateEnabledState();
            this.mDeviceProvisionedController.addCallback(this.mDeviceProvisionedCallback);
            IntentFilter intentFilter = new IntentFilter("android.intent.action.PACKAGE_ADDED");
            intentFilter.addDataScheme("package");
            intentFilter.addDataSchemeSpecificPart(this.mRecentsComponentName.getPackageName(), 0);
            intentFilter.addAction("android.intent.action.PACKAGE_CHANGED");
            this.mContext.registerReceiver(this.mLauncherStateChangedReceiver, intentFilter);
        }
    }

    public void startConnectionToCurrentUser() {
        if (this.mHandler.getLooper() != Looper.myLooper()) {
            this.mHandler.post(this.mConnectionRunnable);
        } else {
            internalConnectToCurrentUser();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void internalConnectToCurrentUser() {
        boolean z;
        disconnectFromLauncherService();
        if (!this.mDeviceProvisionedController.isCurrentUserSetup() || !isEnabled()) {
            Log.v("OverviewProxyService", "Cannot attempt connection, is setup " + this.mDeviceProvisionedController.isCurrentUserSetup() + ", is enabled " + isEnabled());
            return;
        }
        this.mHandler.removeCallbacks(this.mConnectionRunnable);
        try {
            z = this.mContext.bindServiceAsUser(new Intent("android.intent.action.QUICKSTEP_SERVICE").setPackage(this.mRecentsComponentName.getPackageName()), this.mOverviewServiceConnection, 1, UserHandle.of(this.mDeviceProvisionedController.getCurrentUser()));
        } catch (SecurityException e) {
            Log.e("OverviewProxyService", "Unable to bind because of security error", e);
            z = false;
        }
        if (z) {
            this.mHandler.postDelayed(this.mDeferredConnectionCallback, 5000L);
            return;
        }
        long scalb = Math.scalb(5000.0f, this.mConnectionBackoffAttempts);
        this.mHandler.postDelayed(this.mConnectionRunnable, scalb);
        this.mConnectionBackoffAttempts++;
        Log.w("OverviewProxyService", "Failed to connect on attempt " + this.mConnectionBackoffAttempts + " will try again in " + scalb + "ms");
    }

    @Override // com.android.systemui.statusbar.policy.CallbackController
    public void addCallback(OverviewProxyListener overviewProxyListener) {
        this.mConnectionCallbacks.add(overviewProxyListener);
        overviewProxyListener.onConnectionChanged(this.mOverviewProxy != null);
        overviewProxyListener.onInteractionFlagsChanged(this.mInteractionFlags);
    }

    @Override // com.android.systemui.statusbar.policy.CallbackController
    public void removeCallback(OverviewProxyListener overviewProxyListener) {
        this.mConnectionCallbacks.remove(overviewProxyListener);
    }

    public boolean shouldShowSwipeUpUI() {
        return isEnabled() && (this.mInteractionFlags & 1) == 0;
    }

    public boolean isEnabled() {
        return this.mIsEnabled;
    }

    public IOverviewProxy getProxy() {
        return this.mOverviewProxy;
    }

    public int getInteractionFlags() {
        return this.mInteractionFlags;
    }

    private void disconnectFromLauncherService() {
        if (this.mOverviewProxy != null) {
            this.mOverviewProxy.asBinder().unlinkToDeath(this.mOverviewServiceDeathRcpt, 0);
            this.mContext.unbindService(this.mOverviewServiceConnection);
            this.mOverviewProxy = null;
            notifyBackButtonAlphaChanged(1.0f, false);
            notifyConnectionChanged();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void notifyBackButtonAlphaChanged(float f, boolean z) {
        for (int size = this.mConnectionCallbacks.size() - 1; size >= 0; size--) {
            this.mConnectionCallbacks.get(size).onBackButtonAlphaChanged(f, z);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void notifyConnectionChanged() {
        for (int size = this.mConnectionCallbacks.size() - 1; size >= 0; size--) {
            this.mConnectionCallbacks.get(size).onConnectionChanged(this.mOverviewProxy != null);
        }
    }

    public void notifyQuickStepStarted() {
        for (int size = this.mConnectionCallbacks.size() - 1; size >= 0; size--) {
            this.mConnectionCallbacks.get(size).onQuickStepStarted();
        }
    }

    public void notifyQuickScrubStarted() {
        for (int size = this.mConnectionCallbacks.size() - 1; size >= 0; size--) {
            this.mConnectionCallbacks.get(size).onQuickScrubStarted();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateEnabledState() {
        this.mIsEnabled = this.mContext.getPackageManager().resolveServiceAsUser(this.mQuickStepIntent, 262144, ActivityManagerWrapper.getInstance().getCurrentUserId()) != null;
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("OverviewProxyService state:");
        printWriter.print("  mConnectionBackoffAttempts=");
        printWriter.println(this.mConnectionBackoffAttempts);
        printWriter.print("  isCurrentUserSetup=");
        printWriter.println(this.mDeviceProvisionedController.isCurrentUserSetup());
        printWriter.print("  isConnected=");
        printWriter.println(this.mOverviewProxy != null);
        printWriter.print("  mRecentsComponentName=");
        printWriter.println(this.mRecentsComponentName);
        printWriter.print("  mIsEnabled=");
        printWriter.println(isEnabled());
        printWriter.print("  mInteractionFlags=");
        printWriter.println(this.mInteractionFlags);
        printWriter.print("  mQuickStepIntent=");
        printWriter.println(this.mQuickStepIntent);
    }

    /* loaded from: classes.dex */
    public interface OverviewProxyListener {
        default void onConnectionChanged(boolean z) {
        }

        default void onQuickStepStarted() {
        }

        default void onInteractionFlagsChanged(int i) {
        }

        default void onOverviewShown(boolean z) {
        }

        default void onQuickScrubStarted() {
        }

        default void onBackButtonAlphaChanged(float f, boolean z) {
        }
    }
}

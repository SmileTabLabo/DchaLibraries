package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.os.Handler;
import com.android.systemui.Prefs;
import com.android.systemui.qs.SecureSetting;
import com.android.systemui.statusbar.phone.ManagedProfileController;
import com.android.systemui.statusbar.policy.DataSaverController;
import com.android.systemui.statusbar.policy.HotspotController;
import com.android.systemui.statusbar.policy.NightModeController;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/AutoTileManager.class */
public class AutoTileManager {
    private SecureSetting mColorsSetting;
    private final Context mContext;
    private final Handler mHandler;
    private final QSTileHost mHost;
    private final NightModeController.Listener mNightModeListener = new AnonymousClass1(this);
    private final ManagedProfileController.Callback mProfileCallback = new AnonymousClass2(this);
    private final DataSaverController.Listener mDataSaverListener = new AnonymousClass3(this);
    private final HotspotController.Callback mHotspotCallback = new AnonymousClass4(this);

    /* renamed from: com.android.systemui.statusbar.phone.AutoTileManager$1  reason: invalid class name */
    /* loaded from: a.zip:com/android/systemui/statusbar/phone/AutoTileManager$1.class */
    class AnonymousClass1 implements NightModeController.Listener {
        final AutoTileManager this$0;

        AnonymousClass1(AutoTileManager autoTileManager) {
            this.this$0 = autoTileManager;
        }

        @Override // com.android.systemui.statusbar.policy.NightModeController.Listener
        public void onNightModeChanged() {
            if (this.this$0.mHost.getNightModeController().isEnabled()) {
                this.this$0.mHost.addTile("night");
                Prefs.putBoolean(this.this$0.mContext, "QsNightAdded", true);
                this.this$0.mHandler.post(new Runnable(this) { // from class: com.android.systemui.statusbar.phone.AutoTileManager.1.1
                    final AnonymousClass1 this$1;

                    {
                        this.this$1 = this;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        this.this$1.this$0.mHost.getNightModeController().removeListener(this.this$1.this$0.mNightModeListener);
                    }
                });
            }
        }
    }

    /* renamed from: com.android.systemui.statusbar.phone.AutoTileManager$2  reason: invalid class name */
    /* loaded from: a.zip:com/android/systemui/statusbar/phone/AutoTileManager$2.class */
    class AnonymousClass2 implements ManagedProfileController.Callback {
        final AutoTileManager this$0;

        AnonymousClass2(AutoTileManager autoTileManager) {
            this.this$0 = autoTileManager;
        }

        @Override // com.android.systemui.statusbar.phone.ManagedProfileController.Callback
        public void onManagedProfileChanged() {
            if (this.this$0.mHost.getManagedProfileController().hasActiveProfile()) {
                this.this$0.mHost.addTile("work");
                Prefs.putBoolean(this.this$0.mContext, "QsWorkAdded", true);
                this.this$0.mHandler.post(new Runnable(this) { // from class: com.android.systemui.statusbar.phone.AutoTileManager.2.1
                    final AnonymousClass2 this$1;

                    {
                        this.this$1 = this;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        this.this$1.this$0.mHost.getManagedProfileController().removeCallback(this.this$1.this$0.mProfileCallback);
                    }
                });
            }
        }

        @Override // com.android.systemui.statusbar.phone.ManagedProfileController.Callback
        public void onManagedProfileRemoved() {
        }
    }

    /* renamed from: com.android.systemui.statusbar.phone.AutoTileManager$3  reason: invalid class name */
    /* loaded from: a.zip:com/android/systemui/statusbar/phone/AutoTileManager$3.class */
    class AnonymousClass3 implements DataSaverController.Listener {
        final AutoTileManager this$0;

        AnonymousClass3(AutoTileManager autoTileManager) {
            this.this$0 = autoTileManager;
        }

        @Override // com.android.systemui.statusbar.policy.DataSaverController.Listener
        public void onDataSaverChanged(boolean z) {
            if (z) {
                this.this$0.mHost.addTile("saver");
                Prefs.putBoolean(this.this$0.mContext, "QsDataSaverAdded", true);
                this.this$0.mHandler.post(new Runnable(this) { // from class: com.android.systemui.statusbar.phone.AutoTileManager.3.1
                    final AnonymousClass3 this$1;

                    {
                        this.this$1 = this;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        this.this$1.this$0.mHost.getNetworkController().getDataSaverController().remListener(this.this$1.this$0.mDataSaverListener);
                    }
                });
            }
        }
    }

    /* renamed from: com.android.systemui.statusbar.phone.AutoTileManager$4  reason: invalid class name */
    /* loaded from: a.zip:com/android/systemui/statusbar/phone/AutoTileManager$4.class */
    class AnonymousClass4 implements HotspotController.Callback {
        final AutoTileManager this$0;

        AnonymousClass4(AutoTileManager autoTileManager) {
            this.this$0 = autoTileManager;
        }

        @Override // com.android.systemui.statusbar.policy.HotspotController.Callback
        public void onHotspotChanged(boolean z) {
            if (z) {
                this.this$0.mHost.addTile("hotspot");
                Prefs.putBoolean(this.this$0.mContext, "QsHotspotAdded", true);
                this.this$0.mHandler.post(new Runnable(this) { // from class: com.android.systemui.statusbar.phone.AutoTileManager.4.1
                    final AnonymousClass4 this$1;

                    {
                        this.this$1 = this;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        this.this$1.this$0.mHost.getHotspotController().removeCallback(this.this$1.this$0.mHotspotCallback);
                    }
                });
            }
        }
    }

    /* renamed from: com.android.systemui.statusbar.phone.AutoTileManager$5  reason: invalid class name */
    /* loaded from: a.zip:com/android/systemui/statusbar/phone/AutoTileManager$5.class */
    class AnonymousClass5 extends SecureSetting {
        final AutoTileManager this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        AnonymousClass5(AutoTileManager autoTileManager, Context context, Handler handler, String str) {
            super(context, handler, str);
            this.this$0 = autoTileManager;
        }

        @Override // com.android.systemui.qs.SecureSetting
        protected void handleValueChanged(int i, boolean z) {
            if (i != 0) {
                this.this$0.mHost.addTile("inversion");
                Prefs.putBoolean(this.this$0.mContext, "QsInvertColorsAdded", true);
                this.this$0.mHandler.post(new Runnable(this) { // from class: com.android.systemui.statusbar.phone.AutoTileManager.5.1
                    final AnonymousClass5 this$1;

                    {
                        this.this$1 = this;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        this.this$1.this$0.mColorsSetting.setListening(false);
                    }
                });
            }
        }
    }

    public AutoTileManager(Context context, QSTileHost qSTileHost) {
        this.mContext = context;
        this.mHost = qSTileHost;
        this.mHandler = new Handler(this.mHost.getLooper());
        if (!Prefs.getBoolean(context, "QsHotspotAdded", false)) {
            qSTileHost.getHotspotController().addCallback(this.mHotspotCallback);
        }
        if (!Prefs.getBoolean(context, "QsDataSaverAdded", false)) {
            qSTileHost.getNetworkController().getDataSaverController().addListener(this.mDataSaverListener);
        }
        if (!Prefs.getBoolean(context, "QsInvertColorsAdded", false)) {
            this.mColorsSetting = new AnonymousClass5(this, this.mContext, this.mHandler, "accessibility_display_inversion_enabled");
            this.mColorsSetting.setListening(true);
        }
        if (!Prefs.getBoolean(context, "QsWorkAdded", false)) {
            qSTileHost.getManagedProfileController().addCallback(this.mProfileCallback);
        }
        if (Prefs.getBoolean(context, "QsNightAdded", false)) {
            return;
        }
        qSTileHost.getNightModeController().addListener(this.mNightModeListener);
    }

    public void destroy() {
    }
}

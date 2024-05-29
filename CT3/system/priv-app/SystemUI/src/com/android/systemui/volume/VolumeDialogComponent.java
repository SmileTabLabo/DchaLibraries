package com.android.systemui.volume;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.media.VolumePolicy;
import android.os.BenesseExtension;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import com.android.systemui.SystemUI;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.qs.tiles.DndTile;
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.volume.VolumeDialog;
import java.io.FileDescriptor;
import java.io.PrintWriter;
/* loaded from: a.zip:com/android/systemui/volume/VolumeDialogComponent.class */
public class VolumeDialogComponent implements VolumeComponent, TunerService.Tunable {
    private final Context mContext;
    private final VolumeDialogController mController;
    private final VolumeDialog mDialog;
    private final SystemUI mSysui;
    private final ZenModeController mZenModeController;
    private VolumePolicy mVolumePolicy = new VolumePolicy(true, true, true, 0);
    private final VolumeDialog.Callback mVolumeDialogCallback = new VolumeDialog.Callback(this) { // from class: com.android.systemui.volume.VolumeDialogComponent.1
        final VolumeDialogComponent this$0;

        {
            this.this$0 = this;
        }

        @Override // com.android.systemui.volume.VolumeDialog.Callback
        public void onZenPrioritySettingsClicked() {
            if (BenesseExtension.getDchaState() != 0) {
                return;
            }
            this.this$0.startSettings(ZenModePanel.ZEN_PRIORITY_SETTINGS);
        }
    };
    private Handler mHandler = new Handler(true);

    public VolumeDialogComponent(SystemUI systemUI, Context context, Handler handler, ZenModeController zenModeController) {
        this.mSysui = systemUI;
        this.mContext = context;
        this.mController = new VolumeDialogController(this, context, null) { // from class: com.android.systemui.volume.VolumeDialogComponent.2
            final VolumeDialogComponent this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.systemui.volume.VolumeDialogController
            protected void onUserActivityW() {
                this.this$0.sendUserActivity();
            }
        };
        context.getContentResolver().registerContentObserver(Settings.System.getUriFor("dcha_state"), false, new ContentObserver(this, this.mHandler) { // from class: com.android.systemui.volume.VolumeDialogComponent.3
            final VolumeDialogComponent this$0;

            {
                this.this$0 = this;
            }

            @Override // android.database.ContentObserver
            public void onChange(boolean z) {
                if (BenesseExtension.getDchaState() != 0) {
                    this.this$0.mVolumePolicy = new VolumePolicy(false, false, false, 0);
                } else {
                    this.this$0.mVolumePolicy = new VolumePolicy(true, true, true, 0);
                }
                this.this$0.mController.setVolumePolicy(this.this$0.mVolumePolicy);
            }
        }, -1);
        this.mZenModeController = zenModeController;
        this.mDialog = new VolumeDialog(context, 2020, this.mController, zenModeController, this.mVolumeDialogCallback);
        applyConfiguration();
        TunerService.get(this.mContext).addTunable(this, "sysui_volume_down_silent", "sysui_volume_up_silent", "sysui_do_not_disturb");
    }

    private void applyConfiguration() {
        this.mDialog.setStreamImportant(4, true);
        this.mDialog.setStreamImportant(1, false);
        this.mDialog.setShowHeaders(false);
        this.mDialog.setAutomute(true);
        this.mDialog.setSilentMode(false);
        this.mController.setVolumePolicy(this.mVolumePolicy);
        this.mController.showDndTile(true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendUserActivity() {
        KeyguardViewMediator keyguardViewMediator = (KeyguardViewMediator) this.mSysui.getComponent(KeyguardViewMediator.class);
        if (keyguardViewMediator != null) {
            keyguardViewMediator.userActivity();
        }
    }

    private void setVolumePolicy(boolean z, boolean z2, boolean z3, int i) {
        this.mVolumePolicy = new VolumePolicy(z, z2, z3, i);
        this.mController.setVolumePolicy(this.mVolumePolicy);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startSettings(Intent intent) {
        ((PhoneStatusBar) this.mSysui.getComponent(PhoneStatusBar.class)).startActivityDismissingKeyguard(intent, true, true);
    }

    @Override // com.android.systemui.volume.VolumeComponent
    public void dismissNow() {
        this.mController.dismiss();
    }

    @Override // com.android.systemui.DemoMode
    public void dispatchDemoCommand(String str, Bundle bundle) {
    }

    @Override // com.android.systemui.volume.VolumeComponent
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        this.mController.dump(fileDescriptor, printWriter, strArr);
        this.mDialog.dump(printWriter);
    }

    @Override // com.android.systemui.volume.VolumeComponent
    public ZenModeController getZenController() {
        return this.mZenModeController;
    }

    @Override // com.android.systemui.volume.VolumeComponent
    public void onConfigurationChanged(Configuration configuration) {
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        if ("sysui_volume_down_silent".equals(str)) {
            setVolumePolicy(str2 != null ? Integer.parseInt(str2) != 0 : true, this.mVolumePolicy.volumeUpToExitSilent, this.mVolumePolicy.doNotDisturbWhenSilent, this.mVolumePolicy.vibrateToSilentDebounce);
        } else if ("sysui_volume_up_silent".equals(str)) {
            setVolumePolicy(this.mVolumePolicy.volumeDownToEnterSilent, str2 != null ? Integer.parseInt(str2) != 0 : true, this.mVolumePolicy.doNotDisturbWhenSilent, this.mVolumePolicy.vibrateToSilentDebounce);
        } else if ("sysui_do_not_disturb".equals(str)) {
            setVolumePolicy(this.mVolumePolicy.volumeDownToEnterSilent, this.mVolumePolicy.volumeUpToExitSilent, str2 != null ? Integer.parseInt(str2) != 0 : true, this.mVolumePolicy.vibrateToSilentDebounce);
        }
    }

    @Override // com.android.systemui.volume.VolumeComponent
    public void register() {
        this.mController.register();
        DndTile.setCombinedIcon(this.mContext, true);
    }
}

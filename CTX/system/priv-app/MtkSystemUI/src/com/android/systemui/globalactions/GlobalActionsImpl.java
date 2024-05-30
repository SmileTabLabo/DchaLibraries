package com.android.systemui.globalactions;

import android.app.Dialog;
import android.app.KeyguardManager;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.android.internal.colorextraction.drawable.GradientDrawable;
import com.android.settingslib.Utils;
import com.android.systemui.Dependency;
import com.android.systemui.SysUiServiceProvider;
import com.android.systemui.colorextraction.SysuiColorExtractor;
import com.android.systemui.plugins.GlobalActions;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.statusbar.policy.KeyguardMonitor;
/* loaded from: classes.dex */
public class GlobalActionsImpl implements GlobalActions, CommandQueue.Callbacks {
    private final Context mContext;
    private boolean mDisabled;
    private GlobalActionsDialog mGlobalActions;
    private final KeyguardMonitor mKeyguardMonitor = (KeyguardMonitor) Dependency.get(KeyguardMonitor.class);
    private final DeviceProvisionedController mDeviceProvisionedController = (DeviceProvisionedController) Dependency.get(DeviceProvisionedController.class);

    public GlobalActionsImpl(Context context) {
        this.mContext = context;
        ((CommandQueue) SysUiServiceProvider.getComponent(context, CommandQueue.class)).addCallbacks(this);
    }

    @Override // com.android.systemui.plugins.GlobalActions
    public void destroy() {
        ((CommandQueue) SysUiServiceProvider.getComponent(this.mContext, CommandQueue.class)).removeCallbacks(this);
    }

    @Override // com.android.systemui.plugins.GlobalActions
    public void showGlobalActions(GlobalActions.GlobalActionsManager globalActionsManager) {
        if (this.mDisabled) {
            return;
        }
        if (this.mGlobalActions == null) {
            this.mGlobalActions = new GlobalActionsDialog(this.mContext, globalActionsManager);
        }
        this.mGlobalActions.showDialog(this.mKeyguardMonitor.isShowing(), this.mDeviceProvisionedController.isDeviceProvisioned());
    }

    @Override // com.android.systemui.plugins.GlobalActions
    public void showShutdownUi(boolean z, String str) {
        Drawable gradientDrawable = new GradientDrawable(this.mContext);
        gradientDrawable.setAlpha(242);
        Dialog dialog = new Dialog(this.mContext, R.style.Theme_SystemUI_Dialog_GlobalActions);
        Window window = dialog.getWindow();
        int i = 1;
        window.requestFeature(1);
        window.getAttributes().systemUiVisibility |= 1792;
        window.getDecorView();
        window.getAttributes().width = -1;
        window.getAttributes().height = -1;
        window.getAttributes().layoutInDisplayCutoutMode = 1;
        window.setType(2020);
        window.clearFlags(2);
        window.addFlags(17629472);
        window.setBackgroundDrawable(gradientDrawable);
        window.setWindowAnimations(16973828);
        dialog.setContentView(17367278);
        dialog.setCancelable(false);
        int colorAttr = Utils.getColorAttr(this.mContext, com.android.systemui.R.attr.wallpaperTextColor);
        boolean isKeyguardLocked = ((KeyguardManager) this.mContext.getSystemService(KeyguardManager.class)).isKeyguardLocked();
        ((ProgressBar) dialog.findViewById(16908301)).getIndeterminateDrawable().setTint(colorAttr);
        TextView textView = (TextView) dialog.findViewById(16908308);
        textView.setTextColor(colorAttr);
        if (z) {
            textView.setText(17040741);
        }
        Point point = new Point();
        this.mContext.getDisplay().getRealSize(point);
        SysuiColorExtractor sysuiColorExtractor = (SysuiColorExtractor) Dependency.get(SysuiColorExtractor.class);
        if (isKeyguardLocked) {
            i = 2;
        }
        gradientDrawable.setColors(sysuiColorExtractor.getColors(i), false);
        gradientDrawable.setScreenSize(point.x, point.y);
        dialog.show();
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void disable(int i, int i2, boolean z) {
        boolean z2 = (i2 & 8) != 0;
        if (z2 == this.mDisabled) {
            return;
        }
        this.mDisabled = z2;
        if (z2 && this.mGlobalActions != null) {
            this.mGlobalActions.dismissDialog();
        }
    }
}

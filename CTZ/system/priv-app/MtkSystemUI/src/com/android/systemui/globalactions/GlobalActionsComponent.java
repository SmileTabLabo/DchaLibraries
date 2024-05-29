package com.android.systemui.globalactions;

import android.os.RemoteException;
import android.os.ServiceManager;
import com.android.internal.statusbar.IStatusBarService;
import com.android.systemui.Dependency;
import com.android.systemui.SysUiServiceProvider;
import com.android.systemui.SystemUI;
import com.android.systemui.plugins.GlobalActions;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.policy.ExtensionController;
import java.util.function.Consumer;
import java.util.function.Supplier;
/* loaded from: classes.dex */
public class GlobalActionsComponent extends SystemUI implements GlobalActions.GlobalActionsManager, CommandQueue.Callbacks {
    private IStatusBarService mBarService;
    private ExtensionController.Extension<GlobalActions> mExtension;
    private GlobalActions mPlugin;

    @Override // com.android.systemui.SystemUI
    public void start() {
        this.mBarService = IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar"));
        this.mExtension = ((ExtensionController) Dependency.get(ExtensionController.class)).newExtension(GlobalActions.class).withPlugin(GlobalActions.class).withDefault(new Supplier() { // from class: com.android.systemui.globalactions.-$$Lambda$GlobalActionsComponent$YD1kfcxpItFZ4AniRUv_gcXk_Mo
            @Override // java.util.function.Supplier
            public final Object get() {
                return GlobalActionsComponent.lambda$start$0(GlobalActionsComponent.this);
            }
        }).withCallback(new Consumer() { // from class: com.android.systemui.globalactions.-$$Lambda$GlobalActionsComponent$bGplH0pcKhfpL1pOMBpgWKJntvw
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                GlobalActionsComponent.this.onExtensionCallback((GlobalActions) obj);
            }
        }).build();
        this.mPlugin = this.mExtension.get();
        ((CommandQueue) SysUiServiceProvider.getComponent(this.mContext, CommandQueue.class)).addCallbacks(this);
    }

    public static /* synthetic */ GlobalActions lambda$start$0(GlobalActionsComponent globalActionsComponent) {
        return new GlobalActionsImpl(globalActionsComponent.mContext);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onExtensionCallback(GlobalActions globalActions) {
        if (this.mPlugin != null) {
            this.mPlugin.destroy();
        }
        this.mPlugin = globalActions;
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void handleShowShutdownUi(boolean z, String str) {
        this.mExtension.get().showShutdownUi(z, str);
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void handleShowGlobalActionsMenu() {
        this.mExtension.get().showGlobalActions(this);
    }

    @Override // com.android.systemui.plugins.GlobalActions.GlobalActionsManager
    public void onGlobalActionsShown() {
        try {
            this.mBarService.onGlobalActionsShown();
        } catch (RemoteException e) {
        }
    }

    @Override // com.android.systemui.plugins.GlobalActions.GlobalActionsManager
    public void onGlobalActionsHidden() {
        try {
            this.mBarService.onGlobalActionsHidden();
        } catch (RemoteException e) {
        }
    }

    @Override // com.android.systemui.plugins.GlobalActions.GlobalActionsManager
    public void shutdown() {
        try {
            this.mBarService.shutdown();
        } catch (RemoteException e) {
        }
    }

    @Override // com.android.systemui.plugins.GlobalActions.GlobalActionsManager
    public void reboot(boolean z) {
        try {
            this.mBarService.reboot(z);
        } catch (RemoteException e) {
        }
    }
}

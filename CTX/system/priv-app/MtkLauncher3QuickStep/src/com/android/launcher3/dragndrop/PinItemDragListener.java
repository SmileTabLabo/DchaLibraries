package com.android.launcher3.dragndrop;

import android.annotation.TargetApi;
import android.content.pm.LauncherApps;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.view.DragEvent;
import android.view.View;
import android.widget.RemoteViews;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherAppWidgetProviderInfo;
import com.android.launcher3.uioverrides.UiFactory;
import com.android.launcher3.userevent.nano.LauncherLogProto;
import com.android.launcher3.widget.PendingAddShortcutInfo;
import com.android.launcher3.widget.PendingAddWidgetInfo;
import com.android.launcher3.widget.PendingItemDragHelper;
import com.android.launcher3.widget.WidgetAddFlowHandler;
@TargetApi(26)
/* loaded from: classes.dex */
public class PinItemDragListener extends BaseItemDragListener {
    private final CancellationSignal mCancelSignal;
    private final LauncherApps.PinItemRequest mRequest;

    public PinItemDragListener(LauncherApps.PinItemRequest pinItemRequest, Rect rect, int i, int i2) {
        super(rect, i, i2);
        this.mRequest = pinItemRequest;
        this.mCancelSignal = new CancellationSignal();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.launcher3.dragndrop.BaseItemDragListener
    public boolean onDragStart(DragEvent dragEvent) {
        if (!this.mRequest.isValid()) {
            return false;
        }
        return super.onDragStart(dragEvent);
    }

    @Override // com.android.launcher3.dragndrop.BaseItemDragListener, com.android.launcher3.states.InternalStateHandler
    public boolean init(Launcher launcher, boolean z) {
        super.init(launcher, z);
        if (!z) {
            UiFactory.useFadeOutAnimationForLauncherStart(launcher, this.mCancelSignal);
            return false;
        }
        return false;
    }

    @Override // com.android.launcher3.dragndrop.BaseItemDragListener
    protected PendingItemDragHelper createDragHelper() {
        Object obj;
        if (this.mRequest.getRequestType() == 1) {
            obj = new PendingAddShortcutInfo(new PinShortcutRequestActivityInfo(this.mRequest, this.mLauncher));
        } else {
            LauncherAppWidgetProviderInfo fromProviderInfo = LauncherAppWidgetProviderInfo.fromProviderInfo(this.mLauncher, this.mRequest.getAppWidgetProviderInfo(this.mLauncher));
            final PinWidgetFlowHandler pinWidgetFlowHandler = new PinWidgetFlowHandler(fromProviderInfo, this.mRequest);
            obj = new PendingAddWidgetInfo(fromProviderInfo) { // from class: com.android.launcher3.dragndrop.PinItemDragListener.1
                @Override // com.android.launcher3.widget.PendingAddWidgetInfo
                public WidgetAddFlowHandler getHandler() {
                    return pinWidgetFlowHandler;
                }
            };
        }
        View view = new View(this.mLauncher);
        view.setTag(obj);
        PendingItemDragHelper pendingItemDragHelper = new PendingItemDragHelper(view);
        if (this.mRequest.getRequestType() == 2) {
            pendingItemDragHelper.setPreview(getPreview(this.mRequest));
        }
        return pendingItemDragHelper;
    }

    @Override // com.android.launcher3.logging.UserEventDispatcher.LogContainerProvider
    public void fillInLogContainerData(View view, ItemInfo itemInfo, LauncherLogProto.Target target, LauncherLogProto.Target target2) {
        target2.containerType = 10;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.launcher3.dragndrop.BaseItemDragListener
    public void postCleanup() {
        super.postCleanup();
        this.mCancelSignal.cancel();
    }

    public static RemoteViews getPreview(LauncherApps.PinItemRequest pinItemRequest) {
        Bundle extras = pinItemRequest.getExtras();
        if (extras != null && (extras.get("appWidgetPreview") instanceof RemoteViews)) {
            return (RemoteViews) extras.get("appWidgetPreview");
        }
        return null;
    }
}

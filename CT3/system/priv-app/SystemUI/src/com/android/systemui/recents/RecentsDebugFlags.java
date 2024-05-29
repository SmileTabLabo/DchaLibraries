package com.android.systemui.recents;

import android.content.Context;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.activity.DebugFlagsChangedEvent;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.tuner.TunerService;
/* loaded from: a.zip:com/android/systemui/recents/RecentsDebugFlags.class */
public class RecentsDebugFlags implements TunerService.Tunable {
    public RecentsDebugFlags(Context context) {
    }

    public boolean isFastToggleRecentsEnabled() {
        SystemServicesProxy systemServices = Recents.getSystemServices();
        return (systemServices.hasFreeformWorkspaceSupport() || systemServices.isTouchExplorationEnabled()) ? false : false;
    }

    public boolean isPagingEnabled() {
        return false;
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        EventBus.getDefault().send(new DebugFlagsChangedEvent());
    }
}

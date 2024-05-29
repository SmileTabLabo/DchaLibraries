package com.android.systemui.statusbar.policy;

import com.android.systemui.Dumpable;
import java.util.Set;
/* loaded from: classes.dex */
public interface CastController extends Dumpable, CallbackController<Callback> {

    /* loaded from: classes.dex */
    public interface Callback {
        void onCastDevicesChanged();
    }

    /* loaded from: classes.dex */
    public static final class CastDevice {
        public String description;
        public String id;
        public String name;
        public int state = 0;
        public Object tag;
    }

    Set<CastDevice> getCastDevices();

    void setCurrentUserId(int i);

    void setDiscovering(boolean z);

    void startCasting(CastDevice castDevice);

    void stopCasting(CastDevice castDevice);
}

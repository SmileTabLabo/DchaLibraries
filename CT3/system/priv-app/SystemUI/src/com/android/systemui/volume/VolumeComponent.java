package com.android.systemui.volume;

import android.content.res.Configuration;
import com.android.systemui.DemoMode;
import com.android.systemui.statusbar.policy.ZenModeController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
/* loaded from: a.zip:com/android/systemui/volume/VolumeComponent.class */
public interface VolumeComponent extends DemoMode {
    void dismissNow();

    void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr);

    ZenModeController getZenController();

    void onConfigurationChanged(Configuration configuration);

    void register();
}

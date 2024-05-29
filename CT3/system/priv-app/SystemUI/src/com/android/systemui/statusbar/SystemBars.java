package com.android.systemui.statusbar;

import android.content.res.Configuration;
import android.util.Log;
import com.android.systemui.SystemUI;
import com.android.systemui.statusbar.ServiceMonitor;
import java.io.FileDescriptor;
import java.io.PrintWriter;
/* loaded from: a.zip:com/android/systemui/statusbar/SystemBars.class */
public class SystemBars extends SystemUI implements ServiceMonitor.Callbacks {
    private ServiceMonitor mServiceMonitor;
    private BaseStatusBar mStatusBar;

    private RuntimeException andLog(String str, Throwable th) {
        Log.w("SystemBars", str, th);
        throw new RuntimeException(str, th);
    }

    private void createStatusBarFromConfig() {
        String string = this.mContext.getString(2131493275);
        if (string == null || string.length() == 0) {
            throw andLog("No status bar component configured", null);
        }
        try {
            try {
                this.mStatusBar = (BaseStatusBar) this.mContext.getClassLoader().loadClass(string).newInstance();
                this.mStatusBar.mContext = this.mContext;
                this.mStatusBar.mComponents = this.mComponents;
                this.mStatusBar.start();
            } catch (Throwable th) {
                throw andLog("Error creating status bar component: " + string, th);
            }
        } catch (Throwable th2) {
            throw andLog("Error loading status bar component: " + string, th2);
        }
    }

    @Override // com.android.systemui.SystemUI
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        if (this.mStatusBar != null) {
            this.mStatusBar.dump(fileDescriptor, printWriter, strArr);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.SystemUI
    public void onConfigurationChanged(Configuration configuration) {
        if (this.mStatusBar != null) {
            this.mStatusBar.onConfigurationChanged(configuration);
        }
    }

    @Override // com.android.systemui.statusbar.ServiceMonitor.Callbacks
    public void onNoService() {
        createStatusBarFromConfig();
    }

    @Override // com.android.systemui.statusbar.ServiceMonitor.Callbacks
    public long onServiceStartAttempt() {
        if (this.mStatusBar != null) {
            this.mStatusBar.destroy();
            this.mStatusBar = null;
            return 500L;
        }
        return 0L;
    }

    @Override // com.android.systemui.SystemUI
    public void start() {
        this.mServiceMonitor = new ServiceMonitor("SystemBars", false, this.mContext, "bar_service_component", this);
        this.mServiceMonitor.start();
    }
}

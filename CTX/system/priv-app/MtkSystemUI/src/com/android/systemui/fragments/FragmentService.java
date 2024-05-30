package com.android.systemui.fragments;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.util.ArrayMap;
import android.view.View;
import com.android.systemui.ConfigurationChangedReceiver;
import com.android.systemui.Dumpable;
import com.android.systemui.fragments.FragmentService;
import java.io.FileDescriptor;
import java.io.PrintWriter;
/* loaded from: classes.dex */
public class FragmentService implements ConfigurationChangedReceiver, Dumpable {
    private final Context mContext;
    private final ArrayMap<View, FragmentHostState> mHosts = new ArrayMap<>();
    private final Handler mHandler = new Handler();

    public FragmentService(Context context) {
        this.mContext = context;
    }

    public FragmentHostManager getFragmentHostManager(View view) {
        View rootView = view.getRootView();
        FragmentHostState fragmentHostState = this.mHosts.get(rootView);
        if (fragmentHostState == null) {
            fragmentHostState = new FragmentHostState(rootView);
            this.mHosts.put(rootView, fragmentHostState);
        }
        return fragmentHostState.getFragmentHostManager();
    }

    public void destroyAll() {
        for (FragmentHostState fragmentHostState : this.mHosts.values()) {
            fragmentHostState.mFragmentHostManager.destroy();
        }
    }

    @Override // com.android.systemui.ConfigurationChangedReceiver
    public void onConfigurationChanged(Configuration configuration) {
        for (FragmentHostState fragmentHostState : this.mHosts.values()) {
            fragmentHostState.sendConfigurationChange(configuration);
        }
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("Dumping fragments:");
        for (FragmentHostState fragmentHostState : this.mHosts.values()) {
            fragmentHostState.mFragmentHostManager.getFragmentManager().dump("  ", fileDescriptor, printWriter, strArr);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class FragmentHostState {
        private FragmentHostManager mFragmentHostManager;
        private final View mView;

        public FragmentHostState(View view) {
            this.mView = view;
            this.mFragmentHostManager = new FragmentHostManager(FragmentService.this.mContext, FragmentService.this, this.mView);
        }

        public void sendConfigurationChange(final Configuration configuration) {
            FragmentService.this.mHandler.post(new Runnable() { // from class: com.android.systemui.fragments.-$$Lambda$FragmentService$FragmentHostState$kEJEvu5Mq9Z5e9srOLcsFn7Glto
                @Override // java.lang.Runnable
                public final void run() {
                    FragmentService.FragmentHostState.this.handleSendConfigurationChange(configuration);
                }
            });
        }

        public FragmentHostManager getFragmentHostManager() {
            return this.mFragmentHostManager;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void handleSendConfigurationChange(Configuration configuration) {
            this.mFragmentHostManager.onConfigurationChanged(configuration);
        }
    }
}

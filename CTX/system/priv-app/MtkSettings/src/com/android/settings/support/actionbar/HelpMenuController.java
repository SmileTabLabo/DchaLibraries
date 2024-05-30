package com.android.settings.support.actionbar;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import com.android.settingslib.HelpUtils;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.ObservableFragment;
import com.android.settingslib.core.lifecycle.ObservablePreferenceFragment;
import com.android.settingslib.core.lifecycle.events.OnCreateOptionsMenu;
/* loaded from: classes.dex */
public class HelpMenuController implements LifecycleObserver, OnCreateOptionsMenu {
    private final Fragment mHost;

    public static void init(ObservablePreferenceFragment observablePreferenceFragment) {
        observablePreferenceFragment.getLifecycle().addObserver(new HelpMenuController(observablePreferenceFragment));
    }

    public static void init(ObservableFragment observableFragment) {
        observableFragment.getLifecycle().addObserver(new HelpMenuController(observableFragment));
    }

    private HelpMenuController(Fragment fragment) {
        this.mHost = fragment;
    }

    @Override // com.android.settingslib.core.lifecycle.events.OnCreateOptionsMenu
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        int i;
        Bundle arguments = this.mHost.getArguments();
        if (arguments != null && arguments.containsKey("help_uri_resource")) {
            i = arguments.getInt("help_uri_resource");
        } else if (this.mHost instanceof HelpResourceProvider) {
            i = ((HelpResourceProvider) this.mHost).getHelpResource();
        } else {
            i = 0;
        }
        String string = i != 0 ? this.mHost.getContext().getString(i) : null;
        Activity activity = this.mHost.getActivity();
        if (string != null && activity != null) {
            HelpUtils.prepareHelpMenuItem(activity, menu, string, this.mHost.getClass().getName());
        }
    }
}

package com.android.settings.development.featureflags;

import android.content.Context;
import android.support.v7.preference.PreferenceScreen;
import android.util.FeatureFlagUtils;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import java.util.Map;
/* loaded from: classes.dex */
public class FeatureFlagsPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, LifecycleObserver, OnStart {
    private PreferenceScreen mScreen;

    public FeatureFlagsPreferenceController(Context context, Lifecycle lifecycle) {
        super(context);
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public boolean isAvailable() {
        return true;
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public String getPreferenceKey() {
        return null;
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public void displayPreference(PreferenceScreen preferenceScreen) {
        super.displayPreference(preferenceScreen);
        this.mScreen = preferenceScreen;
    }

    @Override // com.android.settingslib.core.lifecycle.events.OnStart
    public void onStart() {
        Map allFeatureFlags;
        if (this.mScreen == null || (allFeatureFlags = FeatureFlagUtils.getAllFeatureFlags()) == null) {
            return;
        }
        this.mScreen.removeAll();
        Context context = this.mScreen.getContext();
        for (String str : allFeatureFlags.keySet()) {
            this.mScreen.addPreference(new FeatureFlagPreference(context, str));
        }
    }
}

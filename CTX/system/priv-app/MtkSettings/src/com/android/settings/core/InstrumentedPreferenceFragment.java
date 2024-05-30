package com.android.settings.core;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.Log;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.survey.SurveyMixin;
import com.android.settingslib.core.instrumentation.Instrumentable;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;
import com.android.settingslib.core.instrumentation.VisibilityLoggerMixin;
import com.android.settingslib.core.lifecycle.ObservablePreferenceFragment;
/* loaded from: classes.dex */
public abstract class InstrumentedPreferenceFragment extends ObservablePreferenceFragment implements Instrumentable {
    private static final String TAG = "InstrumentedPrefFrag";
    protected final int PLACEHOLDER_METRIC = 10000;
    protected MetricsFeatureProvider mMetricsFeatureProvider;
    private VisibilityLoggerMixin mVisibilityLoggerMixin;

    @Override // com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onAttach(Context context) {
        this.mMetricsFeatureProvider = FeatureFactory.getFactory(context).getMetricsFeatureProvider();
        this.mVisibilityLoggerMixin = new VisibilityLoggerMixin(getMetricsCategory(), this.mMetricsFeatureProvider);
        getLifecycle().addObserver(this.mVisibilityLoggerMixin);
        getLifecycle().addObserver(new SurveyMixin(this, getClass().getSimpleName()));
        super.onAttach(context);
    }

    @Override // com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onResume() {
        this.mVisibilityLoggerMixin.setSourceMetricsCategory(getActivity());
        super.onResume();
    }

    @Override // android.support.v14.preference.PreferenceFragment
    public void onCreatePreferences(Bundle bundle, String str) {
        int preferenceScreenResId = getPreferenceScreenResId();
        if (preferenceScreenResId > 0) {
            addPreferencesFromResource(preferenceScreenResId);
        }
    }

    @Override // android.support.v14.preference.PreferenceFragment
    public void addPreferencesFromResource(int i) {
        super.addPreferencesFromResource(i);
        updateActivityTitleWithScreenTitle(getPreferenceScreen());
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public final Context getPrefContext() {
        return getPreferenceManager().getContext();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public final VisibilityLoggerMixin getVisibilityLogger() {
        return this.mVisibilityLoggerMixin;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public int getPreferenceScreenResId() {
        return -1;
    }

    private void updateActivityTitleWithScreenTitle(PreferenceScreen preferenceScreen) {
        if (preferenceScreen != null) {
            CharSequence title = preferenceScreen.getTitle();
            if (!TextUtils.isEmpty(title)) {
                getActivity().setTitle(title);
                return;
            }
            Log.w(TAG, "Screen title missing for fragment " + getClass().getName());
        }
    }
}

package com.android.settings;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.android.settings.notification.RedactionInterstitial;
import com.android.setupwizardlib.SetupWizardLayout;
import com.android.setupwizardlib.view.NavigationBar;
/* loaded from: classes.dex */
public class SetupRedactionInterstitial extends RedactionInterstitial {
    @Override // com.android.settings.notification.RedactionInterstitial, com.android.settings.SettingsActivity, android.app.Activity
    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(":settings:show_fragment", SetupRedactionInterstitialFragment.class.getName());
        return modIntent;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.notification.RedactionInterstitial, com.android.settings.SettingsActivity
    public boolean isValidFragment(String fragmentName) {
        return SetupRedactionInterstitialFragment.class.getName().equals(fragmentName);
    }

    @Override // android.app.Activity, android.view.ContextThemeWrapper
    protected void onApplyThemeResource(Resources.Theme theme, int resid, boolean first) {
        int resid2 = SetupWizardUtils.getTheme(getIntent());
        super.onApplyThemeResource(theme, resid2, first);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.SettingsActivity, com.android.settingslib.drawer.SettingsDrawerActivity, android.app.Activity
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        LinearLayout layout = (LinearLayout) findViewById(R.id.content_parent);
        layout.setFitsSystemWindows(false);
    }

    /* loaded from: classes.dex */
    public static class SetupRedactionInterstitialFragment extends RedactionInterstitial.RedactionInterstitialFragment implements NavigationBar.NavigationBarListener {
        @Override // com.android.settings.notification.RedactionInterstitial.RedactionInterstitialFragment, com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.setup_redaction_interstitial, container, false);
        }

        @Override // com.android.settings.notification.RedactionInterstitial.RedactionInterstitialFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            SetupWizardLayout layout = (SetupWizardLayout) view.findViewById(R.id.setup_wizard_layout);
            NavigationBar navigationBar = layout.getNavigationBar();
            navigationBar.setNavigationBarListener(this);
            navigationBar.getBackButton().setVisibility(8);
            SetupWizardUtils.setImmersiveMode(getActivity());
        }

        @Override // com.android.setupwizardlib.view.NavigationBar.NavigationBarListener
        public void onNavigateBack() {
            Activity activity = getActivity();
            if (activity == null) {
                return;
            }
            activity.onBackPressed();
        }

        @Override // com.android.setupwizardlib.view.NavigationBar.NavigationBarListener
        public void onNavigateNext() {
            SetupRedactionInterstitial activity = (SetupRedactionInterstitial) getActivity();
            if (activity == null) {
                return;
            }
            activity.setResult(-1, activity.getResultIntentData());
            finish();
        }
    }
}

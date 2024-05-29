package com.android.settings;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.android.settings.ChooseLockPassword;
import com.android.setupwizardlib.SetupWizardLayout;
import com.android.setupwizardlib.util.SystemBarHelper;
import com.android.setupwizardlib.view.NavigationBar;
/* loaded from: classes.dex */
public class SetupChooseLockPassword extends ChooseLockPassword {
    public static Intent createIntent(Context context, int quality, int minLength, int maxLength, boolean requirePasswordToDecrypt, String password) {
        Intent intent = ChooseLockPassword.createIntent(context, quality, minLength, maxLength, requirePasswordToDecrypt, password);
        intent.setClass(context, SetupChooseLockPassword.class);
        intent.putExtra("extra_prefs_show_button_bar", false);
        return intent;
    }

    public static Intent createIntent(Context context, int quality, int minLength, int maxLength, boolean requirePasswordToDecrypt, long challenge) {
        Intent intent = ChooseLockPassword.createIntent(context, quality, minLength, maxLength, requirePasswordToDecrypt, challenge);
        intent.setClass(context, SetupChooseLockPassword.class);
        intent.putExtra("extra_prefs_show_button_bar", false);
        return intent;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.ChooseLockPassword, com.android.settings.SettingsActivity
    public boolean isValidFragment(String fragmentName) {
        return SetupChooseLockPasswordFragment.class.getName().equals(fragmentName);
    }

    @Override // com.android.settings.ChooseLockPassword
    Class<? extends Fragment> getFragmentClass() {
        return SetupChooseLockPasswordFragment.class;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.ChooseLockPassword, com.android.settings.SettingsActivity, com.android.settingslib.drawer.SettingsDrawerActivity, android.app.Activity
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        LinearLayout layout = (LinearLayout) findViewById(R.id.content_parent);
        layout.setFitsSystemWindows(false);
    }

    @Override // android.app.Activity, android.view.ContextThemeWrapper
    protected void onApplyThemeResource(Resources.Theme theme, int resid, boolean first) {
        int resid2 = SetupWizardUtils.getTheme(getIntent());
        super.onApplyThemeResource(theme, resid2, first);
    }

    /* loaded from: classes.dex */
    public static class SetupChooseLockPasswordFragment extends ChooseLockPassword.ChooseLockPasswordFragment implements NavigationBar.NavigationBarListener {
        private SetupWizardLayout mLayout;
        private NavigationBar mNavigationBar;

        @Override // com.android.settings.ChooseLockPassword.ChooseLockPasswordFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            this.mLayout = (SetupWizardLayout) inflater.inflate(R.layout.setup_choose_lock_password, container, false);
            this.mNavigationBar = this.mLayout.getNavigationBar();
            this.mNavigationBar.setNavigationBarListener(this);
            return this.mLayout;
        }

        @Override // com.android.settings.ChooseLockPassword.ChooseLockPasswordFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            SystemBarHelper.setImeInsetView(this.mLayout);
            SetupWizardUtils.setImmersiveMode(getActivity());
            this.mLayout.setHeaderText(getActivity().getTitle());
        }

        @Override // com.android.settings.ChooseLockPassword.ChooseLockPasswordFragment
        protected Intent getRedactionInterstitialIntent(Context context) {
            return null;
        }

        @Override // com.android.settings.ChooseLockPassword.ChooseLockPasswordFragment
        protected void setNextEnabled(boolean enabled) {
            this.mNavigationBar.getNextButton().setEnabled(enabled);
        }

        @Override // com.android.settings.ChooseLockPassword.ChooseLockPasswordFragment
        protected void setNextText(int text) {
            this.mNavigationBar.getNextButton().setText(text);
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
            handleNext();
        }
    }
}

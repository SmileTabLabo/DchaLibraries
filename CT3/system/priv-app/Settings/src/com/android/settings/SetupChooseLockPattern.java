package com.android.settings;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import com.android.settings.ChooseLockPattern;
import com.android.setupwizardlib.SetupWizardLayout;
import com.android.setupwizardlib.view.NavigationBar;
/* loaded from: classes.dex */
public class SetupChooseLockPattern extends ChooseLockPattern {
    public static Intent createIntent(Context context, boolean requirePassword, String pattern) {
        Intent intent = ChooseLockPattern.createIntent(context, requirePassword, pattern, UserHandle.myUserId());
        intent.setClass(context, SetupChooseLockPattern.class);
        return intent;
    }

    public static Intent createIntent(Context context, boolean requirePassword, long challenge) {
        Intent intent = ChooseLockPattern.createIntent(context, requirePassword, challenge, UserHandle.myUserId());
        intent.setClass(context, SetupChooseLockPattern.class);
        return intent;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.ChooseLockPattern, com.android.settings.SettingsActivity
    public boolean isValidFragment(String fragmentName) {
        return SetupChooseLockPatternFragment.class.getName().equals(fragmentName);
    }

    @Override // com.android.settings.ChooseLockPattern
    Class<? extends Fragment> getFragmentClass() {
        return SetupChooseLockPatternFragment.class;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.ChooseLockPattern, com.android.settings.SettingsActivity, com.android.settingslib.drawer.SettingsDrawerActivity, android.app.Activity
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
    public static class SetupChooseLockPatternFragment extends ChooseLockPattern.ChooseLockPatternFragment implements NavigationBar.NavigationBarListener {

        /* renamed from: -com-android-settings-ChooseLockPattern$ChooseLockPatternFragment$StageSwitchesValues  reason: not valid java name */
        private static final /* synthetic */ int[] f5x9316aa93 = null;
        private NavigationBar mNavigationBar;
        private Button mRetryButton;

        /* renamed from: -getcom-android-settings-ChooseLockPattern$ChooseLockPatternFragment$StageSwitchesValues  reason: not valid java name */
        private static /* synthetic */ int[] m495xd0a3486f() {
            if (f5x9316aa93 != null) {
                return f5x9316aa93;
            }
            int[] iArr = new int[ChooseLockPattern.ChooseLockPatternFragment.Stage.valuesCustom().length];
            try {
                iArr[ChooseLockPattern.ChooseLockPatternFragment.Stage.ChoiceConfirmed.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[ChooseLockPattern.ChooseLockPatternFragment.Stage.ChoiceTooShort.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[ChooseLockPattern.ChooseLockPatternFragment.Stage.ConfirmWrong.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[ChooseLockPattern.ChooseLockPatternFragment.Stage.FirstChoiceValid.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[ChooseLockPattern.ChooseLockPatternFragment.Stage.HelpScreen.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[ChooseLockPattern.ChooseLockPatternFragment.Stage.Introduction.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                iArr[ChooseLockPattern.ChooseLockPatternFragment.Stage.NeedToConfirm.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            f5x9316aa93 = iArr;
            return iArr;
        }

        @Override // com.android.settings.ChooseLockPattern.ChooseLockPatternFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            SetupWizardLayout layout = (SetupWizardLayout) inflater.inflate(R.layout.setup_choose_lock_pattern, container, false);
            this.mNavigationBar = layout.getNavigationBar();
            this.mNavigationBar.setNavigationBarListener(this);
            layout.setHeaderText(getActivity().getTitle());
            return layout;
        }

        @Override // com.android.settings.ChooseLockPattern.ChooseLockPatternFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
        public void onViewCreated(View view, Bundle savedInstanceState) {
            this.mRetryButton = (Button) view.findViewById(R.id.retryButton);
            this.mRetryButton.setOnClickListener(this);
            super.onViewCreated(view, savedInstanceState);
            SetupWizardUtils.setImmersiveMode(getActivity());
        }

        @Override // com.android.settings.ChooseLockPattern.ChooseLockPatternFragment
        protected Intent getRedactionInterstitialIntent(Context context) {
            return null;
        }

        @Override // com.android.settings.ChooseLockPattern.ChooseLockPatternFragment, android.view.View.OnClickListener
        public void onClick(View v) {
            if (v == this.mRetryButton) {
                handleLeftButton();
            } else {
                super.onClick(v);
            }
        }

        @Override // com.android.settings.ChooseLockPattern.ChooseLockPatternFragment
        protected void setRightButtonEnabled(boolean enabled) {
            this.mNavigationBar.getNextButton().setEnabled(enabled);
        }

        @Override // com.android.settings.ChooseLockPattern.ChooseLockPatternFragment
        protected void setRightButtonText(int text) {
            this.mNavigationBar.getNextButton().setText(text);
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // com.android.settings.ChooseLockPattern.ChooseLockPatternFragment
        public void updateStage(ChooseLockPattern.ChooseLockPatternFragment.Stage stage) {
            super.updateStage(stage);
            this.mRetryButton.setEnabled(stage == ChooseLockPattern.ChooseLockPatternFragment.Stage.FirstChoiceValid);
            switch (m495xd0a3486f()[stage.ordinal()]) {
                case 1:
                case 3:
                case 7:
                    this.mRetryButton.setVisibility(4);
                    return;
                case 2:
                case 4:
                case 5:
                case 6:
                    this.mRetryButton.setVisibility(0);
                    return;
                default:
                    return;
            }
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
            handleRightButton();
        }
    }
}

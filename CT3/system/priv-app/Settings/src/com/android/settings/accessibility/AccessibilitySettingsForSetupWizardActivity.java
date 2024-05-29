package com.android.settings.accessibility;

import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.setupwizardlib.util.SystemBarHelper;
import com.android.setupwizardlib.view.NavigationBar;
/* loaded from: classes.dex */
public class AccessibilitySettingsForSetupWizardActivity extends SettingsActivity {
    private boolean mSendExtraWindowStateChanged;

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.SettingsActivity, com.android.settingslib.drawer.SettingsDrawerActivity, android.app.Activity
    public void onCreate(Bundle savedState) {
        setMainContentId(R.id.suw_main_content);
        super.onCreate(savedState);
        FrameLayout parentLayout = (FrameLayout) findViewById(R.id.main_content);
        LayoutInflater.from(this).inflate(R.layout.accessibility_settings_for_suw, parentLayout);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setIsDrawerPresent(false);
        SystemBarHelper.hideSystemBars(getWindow());
        final LinearLayout parentView = (LinearLayout) findViewById(R.id.content_parent);
        parentView.setFitsSystemWindows(false);
        parentView.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() { // from class: com.android.settings.accessibility.AccessibilitySettingsForSetupWizardActivity.1
            @Override // android.view.View.OnApplyWindowInsetsListener
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                parentView.setPadding(0, insets.getSystemWindowInsetTop(), 0, 0);
                return insets;
            }
        });
        NavigationBar navigationBar = (NavigationBar) findViewById(R.id.suw_navigation_bar);
        navigationBar.getNextButton().setVisibility(8);
        navigationBar.setNavigationBarListener(new NavigationBar.NavigationBarListener() { // from class: com.android.settings.accessibility.AccessibilitySettingsForSetupWizardActivity.2
            @Override // com.android.setupwizardlib.view.NavigationBar.NavigationBarListener
            public void onNavigateBack() {
                AccessibilitySettingsForSetupWizardActivity.this.onNavigateUp();
            }

            @Override // com.android.setupwizardlib.view.NavigationBar.NavigationBarListener
            public void onNavigateNext() {
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.SettingsActivity, android.app.Activity
    public void onSaveInstanceState(Bundle savedState) {
        savedState.putCharSequence("activity_title", getTitle());
        super.onSaveInstanceState(savedState);
    }

    @Override // android.app.Activity
    protected void onRestoreInstanceState(Bundle savedState) {
        super.onRestoreInstanceState(savedState);
        setTitle(savedState.getCharSequence("activity_title"));
    }

    @Override // com.android.settingslib.drawer.SettingsDrawerActivity, android.app.Activity
    public void onResume() {
        super.onResume();
        this.mSendExtraWindowStateChanged = false;
    }

    @Override // com.android.settings.SettingsActivity, android.app.Activity
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override // android.app.Activity
    public boolean onNavigateUp() {
        onBackPressed();
        getWindow().getDecorView().sendAccessibilityEvent(32);
        return true;
    }

    @Override // com.android.settings.SettingsActivity
    public void startPreferencePanel(String fragmentClass, Bundle args, int titleRes, CharSequence titleText, Fragment resultTo, int resultRequestCode) {
        if (!TextUtils.isEmpty(titleText)) {
            setTitle(titleText);
        } else if (titleRes > 0) {
            setTitle(getString(titleRes));
        }
        args.putInt(SettingsPreferenceFragment.HELP_URI_RESOURCE_KEY, 0);
        startPreferenceFragment(Fragment.instantiate(this, fragmentClass, args), true);
        this.mSendExtraWindowStateChanged = true;
    }

    @Override // android.app.Activity
    public void onAttachFragment(Fragment fragment) {
        if (!this.mSendExtraWindowStateChanged) {
            return;
        }
        getWindow().getDecorView().sendAccessibilityEvent(32);
    }
}

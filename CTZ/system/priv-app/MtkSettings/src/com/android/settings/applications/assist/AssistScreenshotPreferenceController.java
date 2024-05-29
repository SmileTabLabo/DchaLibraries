package com.android.settings.applications.assist;

import android.content.Context;
import android.net.Uri;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.TwoStatePreference;
import com.android.internal.app.AssistUtils;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;
import java.util.Arrays;
import java.util.List;
/* loaded from: classes.dex */
public class AssistScreenshotPreferenceController extends AbstractPreferenceController implements Preference.OnPreferenceChangeListener, PreferenceControllerMixin, LifecycleObserver, OnPause, OnResume {
    private final AssistUtils mAssistUtils;
    private Preference mPreference;
    private PreferenceScreen mScreen;
    private final SettingObserver mSettingObserver;

    public AssistScreenshotPreferenceController(Context context, Lifecycle lifecycle) {
        super(context);
        this.mAssistUtils = new AssistUtils(context);
        this.mSettingObserver = new SettingObserver();
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public boolean isAvailable() {
        return this.mAssistUtils.getAssistComponentForUser(UserHandle.myUserId()) != null;
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public void displayPreference(PreferenceScreen preferenceScreen) {
        this.mScreen = preferenceScreen;
        this.mPreference = preferenceScreen.findPreference(getPreferenceKey());
        super.displayPreference(preferenceScreen);
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public String getPreferenceKey() {
        return "screenshot";
    }

    @Override // com.android.settingslib.core.lifecycle.events.OnResume
    public void onResume() {
        this.mSettingObserver.register(this.mContext.getContentResolver(), true);
        updatePreference();
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public void updateState(Preference preference) {
        updatePreference();
    }

    @Override // com.android.settingslib.core.lifecycle.events.OnPause
    public void onPause() {
        this.mSettingObserver.register(this.mContext.getContentResolver(), false);
    }

    @Override // android.support.v7.preference.Preference.OnPreferenceChangeListener
    public boolean onPreferenceChange(Preference preference, Object obj) {
        Settings.Secure.putInt(this.mContext.getContentResolver(), "assist_screenshot_enabled", ((Boolean) obj).booleanValue() ? 1 : 0);
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updatePreference() {
        if (this.mPreference == null || !(this.mPreference instanceof TwoStatePreference)) {
            return;
        }
        if (isAvailable()) {
            if (this.mScreen.findPreference(getPreferenceKey()) == null) {
                this.mScreen.addPreference(this.mPreference);
            }
        } else {
            this.mScreen.removePreference(this.mPreference);
        }
        ((TwoStatePreference) this.mPreference).setChecked(Settings.Secure.getInt(this.mContext.getContentResolver(), "assist_screenshot_enabled", 1) != 0);
        this.mPreference.setEnabled(Settings.Secure.getInt(this.mContext.getContentResolver(), "assist_structure_enabled", 1) != 0);
    }

    /* loaded from: classes.dex */
    class SettingObserver extends AssistSettingObserver {
        private final Uri URI = Settings.Secure.getUriFor("assist_screenshot_enabled");
        private final Uri CONTEXT_URI = Settings.Secure.getUriFor("assist_structure_enabled");

        SettingObserver() {
        }

        @Override // com.android.settings.applications.assist.AssistSettingObserver
        protected List<Uri> getSettingUris() {
            return Arrays.asList(this.URI, this.CONTEXT_URI);
        }

        @Override // com.android.settings.applications.assist.AssistSettingObserver
        public void onSettingChange() {
            AssistScreenshotPreferenceController.this.updatePreference();
        }
    }
}

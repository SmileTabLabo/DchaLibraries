package com.android.settings.development;

import android.content.Context;
import android.content.Intent;
import android.support.v7.preference.Preference;
import android.util.Log;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;
import com.android.settingslib.wrapper.PackageManagerWrapper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
/* loaded from: classes.dex */
public class TouchPanelVersionPreferenceController extends DeveloperOptionsPreferenceController implements PreferenceControllerMixin, OnActivityResultListener {
    private final PackageManagerWrapper mPackageManager;

    public TouchPanelVersionPreferenceController(Context context) {
        super(context);
        this.mPackageManager = new PackageManagerWrapper(this.mContext.getPackageManager());
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public String getPreferenceKey() {
        return "touch_panel_ver";
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public boolean handlePreferenceTreeClick(Preference preference) {
        return false;
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public void updateState(Preference preference) {
        updatePreferenceSummary();
    }

    @Override // com.android.settings.development.OnActivityResultListener
    public boolean onActivityResult(int i, int i2, Intent intent) {
        updatePreferenceSummary();
        return true;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settingslib.development.DeveloperOptionsPreferenceController
    public void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
    }

    Intent getActivityStartIntent() {
        return new Intent(this.mContext, AppPicker.class);
    }

    private void updatePreferenceSummary() {
        String str = "";
        Log.e("PrefControllerMixin", "updatePreferenceSummary");
        File file = new File("/sys/devices/platform/soc/11007000.i2c/i2c-0/0-000a/tp_fwver");
        if (file.exists()) {
            try {
                FileReader fileReader = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                String readLine = bufferedReader.readLine();
                try {
                    bufferedReader.close();
                    fileReader.close();
                    str = readLine;
                } catch (IOException e) {
                    str = readLine;
                }
            } catch (IOException e2) {
            }
        }
        Log.e("PrefControllerMixin", "tmp_str : " + str);
        this.mPreference.setSummary(str);
    }
}

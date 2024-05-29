package com.android.settings;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
/* loaded from: classes.dex */
public class AppListPreferenceWithSettings extends AppListPreference {
    private ComponentName mSettingsComponent;
    private View mSettingsIcon;

    public AppListPreferenceWithSettings(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWidgetLayoutResource(R.layout.preference_widget_settings);
    }

    @Override // android.support.v7.preference.Preference
    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        this.mSettingsIcon = view.findViewById(R.id.settings_button);
        this.mSettingsIcon.setOnClickListener(new View.OnClickListener() { // from class: com.android.settings.AppListPreferenceWithSettings.1
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                Intent intent = new Intent("android.intent.action.MAIN");
                intent.setComponent(AppListPreferenceWithSettings.this.mSettingsComponent);
                AppListPreferenceWithSettings.this.getContext().startActivity(new Intent(intent));
            }
        });
        ViewGroup container = (ViewGroup) this.mSettingsIcon.getParent();
        container.setPaddingRelative(0, 0, 0, 0);
        updateSettingsVisibility();
    }

    private void updateSettingsVisibility() {
        if (this.mSettingsIcon == null) {
            return;
        }
        if (this.mSettingsComponent == null) {
            this.mSettingsIcon.setVisibility(8);
        } else {
            this.mSettingsIcon.setVisibility(0);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setSettingsComponent(ComponentName settings) {
        this.mSettingsComponent = settings;
        updateSettingsVisibility();
    }
}

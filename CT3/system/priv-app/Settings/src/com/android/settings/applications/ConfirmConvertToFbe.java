package com.android.settings.applications;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
/* loaded from: classes.dex */
public class ConfirmConvertToFbe extends SettingsPreferenceFragment {
    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.confirm_convert_fbe, (ViewGroup) null);
        Button button = (Button) rootView.findViewById(R.id.button_confirm_convert_fbe);
        button.setOnClickListener(new View.OnClickListener() { // from class: com.android.settings.applications.ConfirmConvertToFbe.1
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                Intent intent = new Intent("android.intent.action.MASTER_CLEAR");
                intent.addFlags(268435456);
                intent.putExtra("android.intent.extra.REASON", "convert_fbe");
                ConfirmConvertToFbe.this.getActivity().sendBroadcast(intent);
            }
        });
        return rootView;
    }

    @Override // com.android.settings.InstrumentedPreferenceFragment
    protected int getMetricsCategory() {
        return 403;
    }
}

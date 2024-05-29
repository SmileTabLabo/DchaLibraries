package com.android.systemui.tuner;

import android.app.Fragment;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import com.android.internal.logging.MetricsLogger;
/* loaded from: a.zip:com/android/systemui/tuner/PowerNotificationControlsFragment.class */
public class PowerNotificationControlsFragment extends Fragment {
    /* JADX INFO: Access modifiers changed from: private */
    public boolean isEnabled() {
        boolean z = true;
        if (Settings.Secure.getInt(getContext().getContentResolver(), "show_importance_slider", 0) != 1) {
            z = false;
        }
        return z;
    }

    @Override // android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    @Override // android.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(2130968736, viewGroup, false);
    }

    @Override // android.app.Fragment
    public void onPause() {
        super.onPause();
        MetricsLogger.visibility(getContext(), 392, false);
    }

    @Override // android.app.Fragment
    public void onResume() {
        super.onResume();
        MetricsLogger.visibility(getContext(), 392, true);
    }

    @Override // android.app.Fragment
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        View findViewById = view.findViewById(2131886716);
        Switch r0 = (Switch) findViewById.findViewById(16908352);
        TextView textView = (TextView) findViewById.findViewById(2131886717);
        r0.setChecked(isEnabled());
        textView.setText(isEnabled() ? getString(2131493855) : getString(2131493856));
        r0.setOnClickListener(new View.OnClickListener(this, r0, textView) { // from class: com.android.systemui.tuner.PowerNotificationControlsFragment.1
            final PowerNotificationControlsFragment this$0;
            final TextView val$switchText;
            final Switch val$switchWidget;

            {
                this.this$0 = this;
                this.val$switchWidget = r0;
                this.val$switchText = textView;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view2) {
                boolean z = !this.this$0.isEnabled();
                MetricsLogger.action(this.this$0.getContext(), 393, z);
                Settings.Secure.putInt(this.this$0.getContext().getContentResolver(), "show_importance_slider", z ? 1 : 0);
                this.val$switchWidget.setChecked(z);
                this.val$switchText.setText(z ? this.this$0.getString(2131493855) : this.this$0.getString(2131493856));
            }
        });
    }
}

package com.android.systemui.tuner;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v14.preference.PreferenceFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.android.internal.logging.MetricsLogger;
/* loaded from: a.zip:com/android/systemui/tuner/TunerFragment.class */
public class TunerFragment extends PreferenceFragment {

    /* loaded from: a.zip:com/android/systemui/tuner/TunerFragment$TunerWarningFragment.class */
    public static class TunerWarningFragment extends DialogFragment {
        @Override // android.app.DialogFragment
        public Dialog onCreateDialog(Bundle bundle) {
            return new AlertDialog.Builder(getContext()).setTitle(2131493731).setMessage(2131493732).setPositiveButton(2131493734, new DialogInterface.OnClickListener(this) { // from class: com.android.systemui.tuner.TunerFragment.TunerWarningFragment.1
                final TunerWarningFragment this$1;

                {
                    this.this$1 = this;
                }

                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i) {
                    Settings.Secure.putInt(this.this$1.getContext().getContentResolver(), "seen_tuner_warning", 1);
                }
            }).show();
        }
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setHasOptionsMenu(true);
    }

    @Override // android.app.Fragment
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menu.add(0, 2, 0, 2131493736);
    }

    @Override // android.support.v14.preference.PreferenceFragment
    public void onCreatePreferences(Bundle bundle, String str) {
        addPreferencesFromResource(2131296260);
        if (Settings.Secure.getInt(getContext().getContentResolver(), "seen_tuner_warning", 0) == 0 && getFragmentManager().findFragmentByTag("tuner_warning") == null) {
            new TunerWarningFragment().show(getFragmentManager(), "tuner_warning");
        }
    }

    @Override // android.app.Fragment
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case 2:
                TunerService.showResetRequest(getContext(), new Runnable(this) { // from class: com.android.systemui.tuner.TunerFragment.1
                    final TunerFragment this$0;

                    {
                        this.this$0 = this;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        this.this$0.getActivity().finish();
                    }
                });
                return true;
            case 16908332:
                getActivity().finish();
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override // android.app.Fragment
    public void onPause() {
        super.onPause();
        MetricsLogger.visibility(getContext(), 227, false);
    }

    @Override // android.app.Fragment
    public void onResume() {
        super.onResume();
        getActivity().setTitle(2131493708);
        MetricsLogger.visibility(getContext(), 227, true);
    }
}

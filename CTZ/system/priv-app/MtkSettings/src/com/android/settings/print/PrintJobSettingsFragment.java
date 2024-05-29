package com.android.settings.print;

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.print.PrintJob;
import android.print.PrintJobId;
import android.print.PrintJobInfo;
import android.print.PrintManager;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
/* loaded from: classes.dex */
public class PrintJobSettingsFragment extends SettingsPreferenceFragment {
    private static final String LOG_TAG = PrintJobSettingsFragment.class.getSimpleName();
    private Preference mMessagePreference;
    private PrintJobId mPrintJobId;
    private Preference mPrintJobPreference;
    private final PrintManager.PrintJobStateChangeListener mPrintJobStateChangeListener = new PrintManager.PrintJobStateChangeListener() { // from class: com.android.settings.print.PrintJobSettingsFragment.1
        public void onPrintJobStateChanged(PrintJobId printJobId) {
            PrintJobSettingsFragment.this.updateUi();
        }
    };
    private PrintManager mPrintManager;

    @Override // com.android.settingslib.core.instrumentation.Instrumentable
    public int getMetricsCategory() {
        return 78;
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View onCreateView = super.onCreateView(layoutInflater, viewGroup, bundle);
        addPreferencesFromResource(R.xml.print_job_settings);
        this.mPrintJobPreference = findPreference("print_job_preference");
        this.mMessagePreference = findPreference("print_job_message_preference");
        this.mPrintManager = ((PrintManager) getActivity().getSystemService("print")).getGlobalPrintManagerForUser(getActivity().getUserId());
        getActivity().getActionBar().setTitle(R.string.print_print_job);
        processArguments();
        setHasOptionsMenu(true);
        return onCreateView;
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        getListView().setEnabled(false);
    }

    @Override // com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onStart() {
        super.onStart();
        this.mPrintManager.addPrintJobStateChangeListener(this.mPrintJobStateChangeListener);
        updateUi();
    }

    @Override // com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onStop() {
        super.onStop();
        this.mPrintManager.removePrintJobStateChangeListener(this.mPrintJobStateChangeListener);
    }

    @Override // com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);
        PrintJob printJob = getPrintJob();
        if (printJob == null) {
            return;
        }
        if (!printJob.getInfo().isCancelling()) {
            menu.add(0, 1, 0, getString(R.string.print_cancel)).setShowAsAction(1);
        }
        if (printJob.isFailed()) {
            menu.add(0, 2, 0, getString(R.string.print_restart)).setShowAsAction(1);
        }
    }

    @Override // com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        PrintJob printJob = getPrintJob();
        if (printJob != null) {
            switch (menuItem.getItemId()) {
                case 1:
                    printJob.cancel();
                    finish();
                    return true;
                case 2:
                    printJob.restart();
                    finish();
                    return true;
            }
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void processArguments() {
        String string = getArguments().getString("EXTRA_PRINT_JOB_ID");
        if (string == null && (string = getIntent().getStringExtra("EXTRA_PRINT_JOB_ID")) == null) {
            Log.w(LOG_TAG, "EXTRA_PRINT_JOB_ID not set");
            finish();
            return;
        }
        this.mPrintJobId = PrintJobId.unflattenFromString(string);
    }

    private PrintJob getPrintJob() {
        return this.mPrintManager.getPrintJob(this.mPrintJobId);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Removed duplicated region for block: B:34:0x0134  */
    /* JADX WARN: Removed duplicated region for block: B:38:0x014f  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public void updateUi() {
        CharSequence status;
        PrintJob printJob = getPrintJob();
        if (printJob == null) {
            finish();
        } else if (printJob.isCancelled() || printJob.isCompleted()) {
            finish();
        } else {
            PrintJobInfo info = printJob.getInfo();
            int state = info.getState();
            if (state != 6) {
                switch (state) {
                    case 1:
                        this.mPrintJobPreference.setTitle(getString(R.string.print_configuring_state_title_template, new Object[]{info.getLabel()}));
                        break;
                    case 2:
                    case 3:
                        if (!printJob.getInfo().isCancelling()) {
                            this.mPrintJobPreference.setTitle(getString(R.string.print_printing_state_title_template, new Object[]{info.getLabel()}));
                            break;
                        } else {
                            this.mPrintJobPreference.setTitle(getString(R.string.print_cancelling_state_title_template, new Object[]{info.getLabel()}));
                            break;
                        }
                    case 4:
                        if (!printJob.getInfo().isCancelling()) {
                            this.mPrintJobPreference.setTitle(getString(R.string.print_blocked_state_title_template, new Object[]{info.getLabel()}));
                            break;
                        } else {
                            this.mPrintJobPreference.setTitle(getString(R.string.print_cancelling_state_title_template, new Object[]{info.getLabel()}));
                            break;
                        }
                }
            } else {
                this.mPrintJobPreference.setTitle(getString(R.string.print_failed_state_title_template, new Object[]{info.getLabel()}));
            }
            this.mPrintJobPreference.setSummary(getString(R.string.print_job_summary, new Object[]{info.getPrinterName(), DateUtils.formatSameDayTime(info.getCreationTime(), info.getCreationTime(), 3, 3)}));
            TypedArray obtainStyledAttributes = getActivity().obtainStyledAttributes(new int[]{16843817});
            int color = obtainStyledAttributes.getColor(0, 0);
            obtainStyledAttributes.recycle();
            int state2 = info.getState();
            if (state2 != 6) {
                switch (state2) {
                    case 2:
                    case 3:
                        Drawable drawable = getActivity().getDrawable(17302724);
                        drawable.setTint(color);
                        this.mPrintJobPreference.setIcon(drawable);
                        break;
                }
                status = info.getStatus(getPackageManager());
                if (TextUtils.isEmpty(status)) {
                    if (getPreferenceScreen().findPreference("print_job_message_preference") == null) {
                        getPreferenceScreen().addPreference(this.mMessagePreference);
                    }
                    this.mMessagePreference.setSummary(status);
                } else {
                    getPreferenceScreen().removePreference(this.mMessagePreference);
                }
                getActivity().invalidateOptionsMenu();
            }
            Drawable drawable2 = getActivity().getDrawable(17302725);
            drawable2.setTint(color);
            this.mPrintJobPreference.setIcon(drawable2);
            status = info.getStatus(getPackageManager());
            if (TextUtils.isEmpty(status)) {
            }
            getActivity().invalidateOptionsMenu();
        }
    }
}

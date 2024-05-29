package com.android.settings.applications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
/* loaded from: classes.dex */
public class RunningServices extends SettingsPreferenceFragment {
    private View mLoadingContainer;
    private Menu mOptionsMenu;
    private final Runnable mRunningProcessesAvail = new Runnable() { // from class: com.android.settings.applications.RunningServices.1
        @Override // java.lang.Runnable
        public void run() {
            Utils.handleLoadingContainer(RunningServices.this.mLoadingContainer, RunningServices.this.mRunningProcessesView, true, true);
        }
    };
    private RunningProcessesView mRunningProcessesView;

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.manage_applications_running, (ViewGroup) null);
        this.mRunningProcessesView = (RunningProcessesView) rootView.findViewById(R.id.running_processes);
        this.mRunningProcessesView.doCreate();
        this.mLoadingContainer = rootView.findViewById(R.id.loading_container);
        return rootView;
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.app.Fragment
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.mOptionsMenu = menu;
        menu.add(0, 1, 1, R.string.show_running_services).setShowAsAction(1);
        menu.add(0, 2, 2, R.string.show_background_processes).setShowAsAction(1);
        updateOptionsMenu();
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settings.InstrumentedPreferenceFragment, android.app.Fragment
    public void onResume() {
        super.onResume();
        boolean haveData = this.mRunningProcessesView.doResume(this, this.mRunningProcessesAvail);
        Utils.handleLoadingContainer(this.mLoadingContainer, this.mRunningProcessesView, haveData, false);
    }

    @Override // com.android.settings.InstrumentedPreferenceFragment, android.app.Fragment
    public void onPause() {
        super.onPause();
        this.mRunningProcessesView.doPause();
    }

    @Override // android.app.Fragment
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                this.mRunningProcessesView.mAdapter.setShowBackground(false);
                break;
            case 2:
                this.mRunningProcessesView.mAdapter.setShowBackground(true);
                break;
            default:
                return false;
        }
        updateOptionsMenu();
        return true;
    }

    @Override // android.app.Fragment
    public void onPrepareOptionsMenu(Menu menu) {
        updateOptionsMenu();
    }

    private void updateOptionsMenu() {
        boolean showingBackground = this.mRunningProcessesView.mAdapter.getShowBackground();
        this.mOptionsMenu.findItem(1).setVisible(showingBackground);
        this.mOptionsMenu.findItem(2).setVisible(showingBackground ? false : true);
    }

    @Override // com.android.settings.InstrumentedPreferenceFragment
    protected int getMetricsCategory() {
        return 404;
    }
}

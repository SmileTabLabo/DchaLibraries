package com.android.settings.applications;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ApplicationErrorReport;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.android.settings.InstrumentedFragment;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.applications.RunningProcessesView;
import com.android.settings.applications.RunningState;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
/* loaded from: classes.dex */
public class RunningServiceDetails extends InstrumentedFragment implements RunningState.OnRefreshUiListener {
    ViewGroup mAllDetails;
    ActivityManager mAm;
    boolean mHaveData;
    LayoutInflater mInflater;
    RunningState.MergedItem mMergedItem;
    int mNumProcesses;
    int mNumServices;
    String mProcessName;
    TextView mProcessesHeader;
    View mRootView;
    TextView mServicesHeader;
    boolean mShowBackground;
    ViewGroup mSnippet;
    RunningProcessesView.ActiveItem mSnippetActiveItem;
    RunningProcessesView.ViewHolder mSnippetViewHolder;
    RunningState mState;
    int mUid;
    int mUserId;
    final ArrayList<ActiveDetail> mActiveDetails = new ArrayList<>();
    StringBuilder mBuilder = new StringBuilder(128);

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public class ActiveDetail implements View.OnClickListener {
        RunningProcessesView.ActiveItem mActiveItem;
        ComponentName mInstaller;
        PendingIntent mManageIntent;
        Button mReportButton;
        View mRootView;
        RunningState.ServiceItem mServiceItem;
        Button mStopButton;
        RunningProcessesView.ViewHolder mViewHolder;

        ActiveDetail() {
        }

        void stopActiveService(boolean confirmed) {
            RunningState.ServiceItem si = this.mServiceItem;
            if (!confirmed && (si.mServiceInfo.applicationInfo.flags & 1) != 0) {
                RunningServiceDetails.this.showConfirmStopDialog(si.mRunningService.service);
                return;
            }
            RunningServiceDetails.this.getActivity().stopService(new Intent().setComponent(si.mRunningService.service));
            if (RunningServiceDetails.this.mMergedItem == null) {
                RunningServiceDetails.this.mState.updateNow();
                RunningServiceDetails.this.finish();
            } else if (!RunningServiceDetails.this.mShowBackground && RunningServiceDetails.this.mMergedItem.mServices.size() <= 1) {
                RunningServiceDetails.this.mState.updateNow();
                RunningServiceDetails.this.finish();
            } else {
                RunningServiceDetails.this.mState.updateNow();
            }
        }

        /* JADX WARN: Can't wrap try/catch for region: R(3:(4:13|14|(2:53|54)|16)|18|19) */
        /* JADX WARN: Code restructure failed: missing block: B:43:0x0173, code lost:
            r12 = e;
         */
        /* JADX WARN: Removed duplicated region for block: B:87:0x00e1 A[EXC_TOP_SPLITTER, SYNTHETIC] */
        @Override // android.view.View.OnClickListener
        /*
            Code decompiled incorrectly, please refer to instructions dump.
        */
        public void onClick(View v) {
            FileInputStream input;
            FileOutputStream output;
            if (v != this.mReportButton) {
                if (this.mManageIntent == null) {
                    if (this.mServiceItem != null) {
                        stopActiveService(false);
                        return;
                    } else if (this.mActiveItem.mItem.mBackground) {
                        RunningServiceDetails.this.mAm.killBackgroundProcesses(this.mActiveItem.mItem.mPackageInfo.packageName);
                        RunningServiceDetails.this.finish();
                        return;
                    } else {
                        RunningServiceDetails.this.mAm.forceStopPackage(this.mActiveItem.mItem.mPackageInfo.packageName);
                        RunningServiceDetails.this.finish();
                        return;
                    }
                }
                try {
                    RunningServiceDetails.this.getActivity().startIntentSender(this.mManageIntent.getIntentSender(), null, 268959744, 524288, 0);
                    return;
                } catch (ActivityNotFoundException e) {
                    Log.w("RunningServicesDetails", e);
                    return;
                } catch (IntentSender.SendIntentException e2) {
                    Log.w("RunningServicesDetails", e2);
                    return;
                } catch (IllegalArgumentException e3) {
                    Log.w("RunningServicesDetails", e3);
                    return;
                }
            }
            ApplicationErrorReport report = new ApplicationErrorReport();
            report.type = 5;
            report.packageName = this.mServiceItem.mServiceInfo.packageName;
            report.installerPackageName = this.mInstaller.getPackageName();
            report.processName = this.mServiceItem.mRunningService.process;
            report.time = System.currentTimeMillis();
            report.systemApp = (this.mServiceItem.mServiceInfo.applicationInfo.flags & 1) != 0;
            ApplicationErrorReport.RunningServiceInfo info = new ApplicationErrorReport.RunningServiceInfo();
            if (this.mActiveItem.mFirstRunTime >= 0) {
                info.durationMillis = SystemClock.elapsedRealtime() - this.mActiveItem.mFirstRunTime;
            } else {
                info.durationMillis = -1L;
            }
            ComponentName comp = new ComponentName(this.mServiceItem.mServiceInfo.packageName, this.mServiceItem.mServiceInfo.name);
            File filename = RunningServiceDetails.this.getActivity().getFileStreamPath("service_dump.txt");
            FileOutputStream output2 = null;
            try {
                try {
                    output = new FileOutputStream(filename);
                } catch (Throwable th) {
                    th = th;
                }
            } catch (IOException e4) {
                e = e4;
            }
            try {
                try {
                    Debug.dumpService("activity", output.getFD(), new String[]{"-a", "service", comp.flattenToString()});
                    if (output != null) {
                        try {
                            output.close();
                        } catch (IOException e5) {
                        }
                    }
                    output2 = output;
                } catch (IOException e6) {
                    e = e6;
                    output2 = output;
                    Log.w("RunningServicesDetails", "Can't dump service: " + comp, e);
                    if (output2 != null) {
                        try {
                            output2.close();
                        } catch (IOException e7) {
                        }
                    }
                    FileInputStream input2 = null;
                    input = new FileInputStream(filename);
                    byte[] buffer = new byte[(int) filename.length()];
                    input.read(buffer);
                    info.serviceDetails = new String(buffer);
                    if (input != null) {
                    }
                    filename.delete();
                    Log.i("RunningServicesDetails", "Details: " + info.serviceDetails);
                    report.runningServiceInfo = info;
                    Intent result = new Intent("android.intent.action.APP_ERROR");
                    result.setComponent(this.mInstaller);
                    result.putExtra("android.intent.extra.BUG_REPORT", report);
                    result.addFlags(268435456);
                    RunningServiceDetails.this.startActivity(result);
                } catch (Throwable th2) {
                    th = th2;
                    output2 = output;
                    if (output2 != null) {
                        try {
                            output2.close();
                        } catch (IOException e8) {
                        }
                    }
                    throw th;
                }
                input = new FileInputStream(filename);
            } catch (Throwable th3) {
                th = th3;
            }
            FileInputStream input22 = null;
            try {
                byte[] buffer2 = new byte[(int) filename.length()];
                input.read(buffer2);
                info.serviceDetails = new String(buffer2);
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e9) {
                    }
                }
            } catch (IOException e10) {
                e = e10;
                input22 = input;
                Log.w("RunningServicesDetails", "Can't read service dump: " + comp, e);
                if (input22 != null) {
                    try {
                        input22.close();
                    } catch (IOException e11) {
                    }
                }
                filename.delete();
                Log.i("RunningServicesDetails", "Details: " + info.serviceDetails);
                report.runningServiceInfo = info;
                Intent result2 = new Intent("android.intent.action.APP_ERROR");
                result2.setComponent(this.mInstaller);
                result2.putExtra("android.intent.extra.BUG_REPORT", report);
                result2.addFlags(268435456);
                RunningServiceDetails.this.startActivity(result2);
            } catch (Throwable th4) {
                th = th4;
                input22 = input;
                if (input22 != null) {
                    try {
                        input22.close();
                    } catch (IOException e12) {
                    }
                }
                throw th;
            }
            filename.delete();
            Log.i("RunningServicesDetails", "Details: " + info.serviceDetails);
            report.runningServiceInfo = info;
            Intent result22 = new Intent("android.intent.action.APP_ERROR");
            result22.setComponent(this.mInstaller);
            result22.putExtra("android.intent.extra.BUG_REPORT", report);
            result22.addFlags(268435456);
            RunningServiceDetails.this.startActivity(result22);
        }
    }

    boolean findMergedItem() {
        RunningState.MergedItem item = null;
        ArrayList<RunningState.MergedItem> newItems = this.mShowBackground ? this.mState.getCurrentBackgroundItems() : this.mState.getCurrentMergedItems();
        if (newItems != null) {
            for (int i = 0; i < newItems.size(); i++) {
                RunningState.MergedItem mi = newItems.get(i);
                if (mi.mUserId == this.mUserId && ((this.mUid < 0 || mi.mProcess == null || mi.mProcess.mUid == this.mUid) && (this.mProcessName == null || (mi.mProcess != null && this.mProcessName.equals(mi.mProcess.mProcessName))))) {
                    item = mi;
                    break;
                }
            }
        }
        if (this.mMergedItem != item) {
            this.mMergedItem = item;
            return true;
        }
        return false;
    }

    void addServicesHeader() {
        if (this.mNumServices == 0) {
            this.mServicesHeader = (TextView) this.mInflater.inflate(R.layout.separator_label, this.mAllDetails, false);
            this.mServicesHeader.setText(R.string.runningservicedetails_services_title);
            this.mAllDetails.addView(this.mServicesHeader);
        }
        this.mNumServices++;
    }

    void addProcessesHeader() {
        if (this.mNumProcesses == 0) {
            this.mProcessesHeader = (TextView) this.mInflater.inflate(R.layout.separator_label, this.mAllDetails, false);
            this.mProcessesHeader.setText(R.string.runningservicedetails_processes_title);
            this.mAllDetails.addView(this.mProcessesHeader);
        }
        this.mNumProcesses++;
    }

    /* JADX WARN: Multi-variable type inference failed */
    void addServiceDetailsView(RunningState.ServiceItem si, RunningState.MergedItem mi, boolean isService, boolean inclDetails) {
        int i;
        if (isService) {
            addServicesHeader();
        } else if (mi.mUserId != UserHandle.myUserId()) {
            addProcessesHeader();
        }
        RunningState.BaseItem bi = si != null ? si : mi;
        ActiveDetail detail = new ActiveDetail();
        View root = this.mInflater.inflate(R.layout.running_service_details_service, this.mAllDetails, false);
        this.mAllDetails.addView(root);
        detail.mRootView = root;
        detail.mServiceItem = si;
        detail.mViewHolder = new RunningProcessesView.ViewHolder(root);
        detail.mActiveItem = detail.mViewHolder.bind(this.mState, bi, this.mBuilder);
        if (!inclDetails) {
            root.findViewById(R.id.service).setVisibility(8);
        }
        if (si != null && si.mRunningService.clientLabel != 0) {
            detail.mManageIntent = this.mAm.getRunningServiceControlPanel(si.mRunningService.service);
        }
        TextView description = (TextView) root.findViewById(R.id.comp_description);
        detail.mStopButton = (Button) root.findViewById(R.id.left_button);
        detail.mReportButton = (Button) root.findViewById(R.id.right_button);
        if (isService && mi.mUserId != UserHandle.myUserId()) {
            description.setVisibility(8);
            root.findViewById(R.id.control_buttons_panel).setVisibility(8);
        } else {
            if (si != null && si.mServiceInfo.descriptionRes != 0) {
                description.setText(getActivity().getPackageManager().getText(si.mServiceInfo.packageName, si.mServiceInfo.descriptionRes, si.mServiceInfo.applicationInfo));
            } else if (mi.mBackground) {
                description.setText(R.string.background_process_stop_description);
            } else if (detail.mManageIntent != null) {
                try {
                    Resources clientr = getActivity().getPackageManager().getResourcesForApplication(si.mRunningService.clientPackage);
                    String label = clientr.getString(si.mRunningService.clientLabel);
                    description.setText(getActivity().getString(R.string.service_manage_description, new Object[]{label}));
                } catch (PackageManager.NameNotFoundException e) {
                }
            } else {
                Activity activity = getActivity();
                if (si != null) {
                    i = R.string.service_stop_description;
                } else {
                    i = R.string.heavy_weight_stop_description;
                }
                description.setText(activity.getText(i));
            }
            detail.mStopButton.setOnClickListener(detail);
            detail.mStopButton.setText(getActivity().getText(detail.mManageIntent != null ? R.string.service_manage : R.string.service_stop));
            detail.mReportButton.setOnClickListener(detail);
            detail.mReportButton.setText(17040283);
            int enabled = Settings.Global.getInt(getActivity().getContentResolver(), "send_action_app_error", 0);
            if (enabled != 0 && si != null) {
                detail.mInstaller = ApplicationErrorReport.getErrorReportReceiver(getActivity(), si.mServiceInfo.packageName, si.mServiceInfo.applicationInfo.flags);
                detail.mReportButton.setEnabled(detail.mInstaller != null);
            } else {
                detail.mReportButton.setEnabled(false);
            }
        }
        this.mActiveDetails.add(detail);
    }

    void addProcessDetailsView(RunningState.ProcessItem pi, boolean isMain) {
        addProcessesHeader();
        ActiveDetail detail = new ActiveDetail();
        View root = this.mInflater.inflate(R.layout.running_service_details_process, this.mAllDetails, false);
        this.mAllDetails.addView(root);
        detail.mRootView = root;
        detail.mViewHolder = new RunningProcessesView.ViewHolder(root);
        detail.mActiveItem = detail.mViewHolder.bind(this.mState, pi, this.mBuilder);
        TextView description = (TextView) root.findViewById(R.id.comp_description);
        if (pi.mUserId != UserHandle.myUserId()) {
            description.setVisibility(8);
        } else if (isMain) {
            description.setText(R.string.main_running_process_description);
        } else {
            int textid = 0;
            CharSequence label = null;
            ActivityManager.RunningAppProcessInfo rpi = pi.mRunningProcessInfo;
            ComponentName componentName = rpi.importanceReasonComponent;
            switch (rpi.importanceReasonCode) {
                case 1:
                    textid = R.string.process_provider_in_use_description;
                    if (rpi.importanceReasonComponent != null) {
                        try {
                            ProviderInfo prov = getActivity().getPackageManager().getProviderInfo(rpi.importanceReasonComponent, 0);
                            label = RunningState.makeLabel(getActivity().getPackageManager(), prov.name, prov);
                            break;
                        } catch (PackageManager.NameNotFoundException e) {
                            break;
                        }
                    }
                    break;
                case 2:
                    textid = R.string.process_service_in_use_description;
                    if (rpi.importanceReasonComponent != null) {
                        try {
                            ServiceInfo serv = getActivity().getPackageManager().getServiceInfo(rpi.importanceReasonComponent, 0);
                            label = RunningState.makeLabel(getActivity().getPackageManager(), serv.name, serv);
                            break;
                        } catch (PackageManager.NameNotFoundException e2) {
                            break;
                        }
                    }
                    break;
            }
            if (textid != 0 && label != null) {
                description.setText(getActivity().getString(textid, new Object[]{label}));
            }
        }
        this.mActiveDetails.add(detail);
    }

    void addDetailsViews(RunningState.MergedItem item, boolean inclServices, boolean inclProcesses) {
        if (item == null) {
            return;
        }
        if (inclServices) {
            for (int i = 0; i < item.mServices.size(); i++) {
                addServiceDetailsView(item.mServices.get(i), item, true, true);
            }
        }
        if (!inclProcesses) {
            return;
        }
        if (item.mServices.size() <= 0) {
            addServiceDetailsView(null, item, false, item.mUserId != UserHandle.myUserId());
            return;
        }
        int i2 = -1;
        while (i2 < item.mOtherProcesses.size()) {
            RunningState.ProcessItem pi = i2 < 0 ? item.mProcess : item.mOtherProcesses.get(i2);
            if (pi == null || pi.mPid > 0) {
                addProcessDetailsView(pi, i2 < 0);
            }
            i2++;
        }
    }

    void addDetailViews() {
        ArrayList<RunningState.MergedItem> items;
        for (int i = this.mActiveDetails.size() - 1; i >= 0; i--) {
            this.mAllDetails.removeView(this.mActiveDetails.get(i).mRootView);
        }
        this.mActiveDetails.clear();
        if (this.mServicesHeader != null) {
            this.mAllDetails.removeView(this.mServicesHeader);
            this.mServicesHeader = null;
        }
        if (this.mProcessesHeader != null) {
            this.mAllDetails.removeView(this.mProcessesHeader);
            this.mProcessesHeader = null;
        }
        this.mNumProcesses = 0;
        this.mNumServices = 0;
        if (this.mMergedItem == null) {
            return;
        }
        if (this.mMergedItem.mUser != null) {
            if (this.mShowBackground) {
                items = new ArrayList<>(this.mMergedItem.mChildren);
                Collections.sort(items, this.mState.mBackgroundComparator);
            } else {
                items = this.mMergedItem.mChildren;
            }
            for (int i2 = 0; i2 < items.size(); i2++) {
                addDetailsViews(items.get(i2), true, false);
            }
            for (int i3 = 0; i3 < items.size(); i3++) {
                addDetailsViews(items.get(i3), false, true);
            }
            return;
        }
        addDetailsViews(this.mMergedItem, true, true);
    }

    void refreshUi(boolean dataChanged) {
        if (findMergedItem()) {
            dataChanged = true;
        }
        if (!dataChanged) {
            return;
        }
        if (this.mMergedItem != null) {
            this.mSnippetActiveItem = this.mSnippetViewHolder.bind(this.mState, this.mMergedItem, this.mBuilder);
        } else if (this.mSnippetActiveItem != null) {
            this.mSnippetActiveItem.mHolder.size.setText("");
            this.mSnippetActiveItem.mHolder.uptime.setText("");
            this.mSnippetActiveItem.mHolder.description.setText(R.string.no_services);
        } else {
            finish();
            return;
        }
        addDetailViews();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void finish() {
        new Handler().post(new Runnable() { // from class: com.android.settings.applications.RunningServiceDetails.1
            @Override // java.lang.Runnable
            public void run() {
                Activity a = RunningServiceDetails.this.getActivity();
                if (a == null) {
                    return;
                }
                a.onBackPressed();
            }
        });
    }

    @Override // com.android.settings.InstrumentedFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mUid = getArguments().getInt("uid", -1);
        this.mUserId = getArguments().getInt("user_id", 0);
        this.mProcessName = getArguments().getString("process", null);
        this.mShowBackground = getArguments().getBoolean("background", false);
        this.mAm = (ActivityManager) getActivity().getSystemService("activity");
        this.mInflater = (LayoutInflater) getActivity().getSystemService("layout_inflater");
        this.mState = RunningState.getInstance(getActivity());
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.running_service_details, container, false);
        Utils.prepareCustomPreferencesList(container, view, view, false);
        this.mRootView = view;
        this.mAllDetails = (ViewGroup) view.findViewById(R.id.all_details);
        this.mSnippet = (ViewGroup) view.findViewById(R.id.snippet);
        this.mSnippetViewHolder = new RunningProcessesView.ViewHolder(this.mSnippet);
        ensureData();
        return view;
    }

    @Override // com.android.settings.InstrumentedFragment, android.app.Fragment
    public void onPause() {
        super.onPause();
        this.mHaveData = false;
        this.mState.pause();
    }

    @Override // com.android.settings.InstrumentedFragment
    protected int getMetricsCategory() {
        return 85;
    }

    @Override // com.android.settings.InstrumentedFragment, android.app.Fragment
    public void onResume() {
        super.onResume();
        ensureData();
    }

    ActiveDetail activeDetailForService(ComponentName comp) {
        for (int i = 0; i < this.mActiveDetails.size(); i++) {
            ActiveDetail ad = this.mActiveDetails.get(i);
            if (ad.mServiceItem != null && ad.mServiceItem.mRunningService != null && comp.equals(ad.mServiceItem.mRunningService.service)) {
                return ad;
            }
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showConfirmStopDialog(ComponentName comp) {
        DialogFragment newFragment = MyAlertDialogFragment.newConfirmStop(1, comp);
        newFragment.setTargetFragment(this, 0);
        newFragment.show(getFragmentManager(), "confirmstop");
    }

    /* loaded from: classes.dex */
    public static class MyAlertDialogFragment extends DialogFragment {
        public static MyAlertDialogFragment newConfirmStop(int id, ComponentName comp) {
            MyAlertDialogFragment frag = new MyAlertDialogFragment();
            Bundle args = new Bundle();
            args.putInt("id", id);
            args.putParcelable("comp", comp);
            frag.setArguments(args);
            return frag;
        }

        RunningServiceDetails getOwner() {
            return (RunningServiceDetails) getTargetFragment();
        }

        @Override // android.app.DialogFragment
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int id = getArguments().getInt("id");
            switch (id) {
                case 1:
                    final ComponentName comp = (ComponentName) getArguments().getParcelable("comp");
                    if (getOwner().activeDetailForService(comp) == null) {
                        return null;
                    }
                    return new AlertDialog.Builder(getActivity()).setTitle(getActivity().getString(R.string.runningservicedetails_stop_dlg_title)).setMessage(getActivity().getString(R.string.runningservicedetails_stop_dlg_text)).setPositiveButton(R.string.dlg_ok, new DialogInterface.OnClickListener() { // from class: com.android.settings.applications.RunningServiceDetails.MyAlertDialogFragment.1
                        @Override // android.content.DialogInterface.OnClickListener
                        public void onClick(DialogInterface dialog, int which) {
                            ActiveDetail ad = MyAlertDialogFragment.this.getOwner().activeDetailForService(comp);
                            if (ad == null) {
                                return;
                            }
                            ad.stopActiveService(true);
                        }
                    }).setNegativeButton(R.string.dlg_cancel, (DialogInterface.OnClickListener) null).create();
                default:
                    throw new IllegalArgumentException("unknown id " + id);
            }
        }
    }

    void ensureData() {
        if (this.mHaveData) {
            return;
        }
        this.mHaveData = true;
        this.mState.resume(this);
        this.mState.waitForData();
        refreshUi(true);
    }

    void updateTimes() {
        if (this.mSnippetActiveItem != null) {
            this.mSnippetActiveItem.updateTime(getActivity(), this.mBuilder);
        }
        for (int i = 0; i < this.mActiveDetails.size(); i++) {
            this.mActiveDetails.get(i).mActiveItem.updateTime(getActivity(), this.mBuilder);
        }
    }

    @Override // com.android.settings.applications.RunningState.OnRefreshUiListener
    public void onRefreshUi(int what) {
        if (getActivity() == null) {
            return;
        }
        switch (what) {
            case 0:
                updateTimes();
                return;
            case 1:
                refreshUi(false);
                updateTimes();
                return;
            case 2:
                refreshUi(true);
                updateTimes();
                return;
            default:
                return;
        }
    }
}

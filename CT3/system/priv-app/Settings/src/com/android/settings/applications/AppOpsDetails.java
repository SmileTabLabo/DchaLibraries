package com.android.settings.applications;

import android.app.AppOpsManager;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import com.android.settings.InstrumentedFragment;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.Utils;
import com.android.settings.applications.AppOpsState;
import java.util.List;
/* loaded from: classes.dex */
public class AppOpsDetails extends InstrumentedFragment {
    private AppOpsManager mAppOps;
    private LayoutInflater mInflater;
    private LinearLayout mOperationsSection;
    private PackageInfo mPackageInfo;
    private PackageManager mPm;
    private View mRootView;
    private AppOpsState mState;

    private void setAppLabelAndIcon(PackageInfo pkgInfo) {
        View appSnippet = this.mRootView.findViewById(R.id.app_snippet);
        CharSequence label = this.mPm.getApplicationLabel(pkgInfo.applicationInfo);
        Drawable icon = this.mPm.getApplicationIcon(pkgInfo.applicationInfo);
        InstalledAppDetails.setupAppSnippet(appSnippet, label, icon, pkgInfo != null ? pkgInfo.versionName : null);
    }

    private String retrieveAppEntry() {
        Bundle args = getArguments();
        String packageName = args != null ? args.getString("package") : null;
        if (packageName == null) {
            Intent intent = args == null ? getActivity().getIntent() : (Intent) args.getParcelable("intent");
            if (intent != null) {
                packageName = intent.getData().getSchemeSpecificPart();
            }
        }
        try {
            this.mPackageInfo = this.mPm.getPackageInfo(packageName, 8704);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("AppOpsDetails", "Exception when retrieving package:" + packageName, e);
            this.mPackageInfo = null;
        }
        return packageName;
    }

    private boolean refreshUi() {
        if (this.mPackageInfo == null) {
            return false;
        }
        setAppLabelAndIcon(this.mPackageInfo);
        Resources res = getActivity().getResources();
        this.mOperationsSection.removeAllViews();
        String lastPermGroup = "";
        AppOpsState.OpsTemplate[] opsTemplateArr = AppOpsState.ALL_TEMPLATES;
        int i = 0;
        int length = opsTemplateArr.length;
        while (true) {
            int i2 = i;
            if (i2 < length) {
                AppOpsState.OpsTemplate tpl = opsTemplateArr[i2];
                List<AppOpsState.AppOpEntry> entries = this.mState.buildState(tpl, this.mPackageInfo.applicationInfo.uid, this.mPackageInfo.packageName);
                for (final AppOpsState.AppOpEntry entry : entries) {
                    AppOpsManager.OpEntry firstOp = entry.getOpEntry(0);
                    View view = this.mInflater.inflate(R.layout.app_ops_details_item, (ViewGroup) this.mOperationsSection, false);
                    this.mOperationsSection.addView(view);
                    String perm = AppOpsManager.opToPermission(firstOp.getOp());
                    if (perm != null) {
                        try {
                            PermissionInfo pi = this.mPm.getPermissionInfo(perm, 0);
                            if (pi.group != null && !lastPermGroup.equals(pi.group)) {
                                lastPermGroup = pi.group;
                                PermissionGroupInfo pgi = this.mPm.getPermissionGroupInfo(pi.group, 0);
                                if (pgi.icon != 0) {
                                    ((ImageView) view.findViewById(R.id.op_icon)).setImageDrawable(pgi.loadIcon(this.mPm));
                                }
                            }
                        } catch (PackageManager.NameNotFoundException e) {
                        }
                    }
                    ((TextView) view.findViewById(R.id.op_name)).setText(entry.getSwitchText(this.mState));
                    ((TextView) view.findViewById(R.id.op_time)).setText(entry.getTimeText(res, true));
                    Switch sw = (Switch) view.findViewById(R.id.switchWidget);
                    final int switchOp = AppOpsManager.opToSwitch(firstOp.getOp());
                    sw.setChecked(this.mAppOps.checkOp(switchOp, entry.getPackageOps().getUid(), entry.getPackageOps().getPackageName()) == 0);
                    sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { // from class: com.android.settings.applications.AppOpsDetails.1
                        @Override // android.widget.CompoundButton.OnCheckedChangeListener
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            AppOpsDetails.this.mAppOps.setMode(switchOp, entry.getPackageOps().getUid(), entry.getPackageOps().getPackageName(), isChecked ? 0 : 1);
                        }
                    });
                }
                i = i2 + 1;
            } else {
                return true;
            }
        }
    }

    private void setIntentAndFinish(boolean finish, boolean appChanged) {
        Intent intent = new Intent();
        intent.putExtra("chg", appChanged);
        SettingsActivity sa = (SettingsActivity) getActivity();
        sa.finishPreferencePanel(this, -1, intent);
    }

    @Override // com.android.settings.InstrumentedFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mState = new AppOpsState(getActivity());
        this.mPm = getActivity().getPackageManager();
        this.mInflater = (LayoutInflater) getActivity().getSystemService("layout_inflater");
        this.mAppOps = (AppOpsManager) getActivity().getSystemService("appops");
        retrieveAppEntry();
        setHasOptionsMenu(true);
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.app_ops_details, container, false);
        Utils.prepareCustomPreferencesList(container, view, view, false);
        this.mRootView = view;
        this.mOperationsSection = (LinearLayout) view.findViewById(R.id.operations_section);
        return view;
    }

    @Override // com.android.settings.InstrumentedFragment
    protected int getMetricsCategory() {
        return 14;
    }

    @Override // com.android.settings.InstrumentedFragment, android.app.Fragment
    public void onResume() {
        super.onResume();
        if (refreshUi()) {
            return;
        }
        setIntentAndFinish(true, true);
    }
}

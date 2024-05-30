package com.android.settings.enterprise;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Process;
import android.os.UserHandle;
import android.util.IconDrawableFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.settings.DeviceAdminAdd;
import com.android.settings.R;
import com.android.settings.Settings;
import com.android.settings.Utils;
import com.android.settingslib.RestrictedLockUtils;
import java.util.Objects;
/* loaded from: classes.dex */
public class ActionDisabledByAdminDialogHelper {
    private static final String TAG = ActionDisabledByAdminDialogHelper.class.getName();
    private Activity mActivity;
    private ViewGroup mDialogView;
    private RestrictedLockUtils.EnforcedAdmin mEnforcedAdmin;
    private String mRestriction = null;

    public ActionDisabledByAdminDialogHelper(Activity activity) {
        this.mActivity = activity;
    }

    public AlertDialog.Builder prepareDialogBuilder(String str, RestrictedLockUtils.EnforcedAdmin enforcedAdmin) {
        this.mEnforcedAdmin = enforcedAdmin;
        this.mRestriction = str;
        AlertDialog.Builder builder = new AlertDialog.Builder(this.mActivity);
        this.mDialogView = (ViewGroup) LayoutInflater.from(builder.getContext()).inflate(R.layout.admin_support_details_dialog, (ViewGroup) null);
        initializeDialogViews(this.mDialogView, this.mEnforcedAdmin.component, this.mEnforcedAdmin.userId, this.mRestriction);
        return builder.setPositiveButton(R.string.okay, (DialogInterface.OnClickListener) null).setNeutralButton(R.string.learn_more, new DialogInterface.OnClickListener() { // from class: com.android.settings.enterprise.-$$Lambda$ActionDisabledByAdminDialogHelper$1vfAOqcacTgM-c2XJLB5Z1-4lQ4
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                ActionDisabledByAdminDialogHelper.lambda$prepareDialogBuilder$0(ActionDisabledByAdminDialogHelper.this, dialogInterface, i);
            }
        }).setView(this.mDialogView);
    }

    public static /* synthetic */ void lambda$prepareDialogBuilder$0(ActionDisabledByAdminDialogHelper actionDisabledByAdminDialogHelper, DialogInterface dialogInterface, int i) {
        actionDisabledByAdminDialogHelper.showAdminPolicies(actionDisabledByAdminDialogHelper.mEnforcedAdmin, actionDisabledByAdminDialogHelper.mActivity);
        actionDisabledByAdminDialogHelper.mActivity.finish();
    }

    public void updateDialog(String str, RestrictedLockUtils.EnforcedAdmin enforcedAdmin) {
        if (this.mEnforcedAdmin.equals(enforcedAdmin) && Objects.equals(this.mRestriction, str)) {
            return;
        }
        this.mEnforcedAdmin = enforcedAdmin;
        this.mRestriction = str;
        initializeDialogViews(this.mDialogView, this.mEnforcedAdmin.component, this.mEnforcedAdmin.userId, this.mRestriction);
    }

    private void initializeDialogViews(View view, ComponentName componentName, int i, String str) {
        if (componentName == null) {
            return;
        }
        if (!RestrictedLockUtils.isAdminInCurrentUserOrProfile(this.mActivity, componentName) || !RestrictedLockUtils.isCurrentUserOrProfile(this.mActivity, i)) {
            componentName = null;
        } else {
            ((ImageView) view.findViewById(R.id.admin_support_icon)).setImageDrawable(Utils.getBadgedIcon(IconDrawableFactory.newInstance(this.mActivity), this.mActivity.getPackageManager(), componentName.getPackageName(), i));
        }
        setAdminSupportTitle(view, str);
        setAdminSupportDetails(this.mActivity, view, new RestrictedLockUtils.EnforcedAdmin(componentName, i));
    }

    void setAdminSupportTitle(View view, String str) {
        TextView textView = (TextView) view.findViewById(R.id.admin_support_dialog_title);
        if (textView == null) {
            return;
        }
        if (str == null) {
            textView.setText(R.string.disabled_by_policy_title);
            return;
        }
        char c = 65535;
        switch (str.hashCode()) {
            case -1040305701:
                if (str.equals("no_sms")) {
                    c = 2;
                    break;
                }
                break;
            case -932215031:
                if (str.equals("policy_disable_camera")) {
                    c = 3;
                    break;
                }
                break;
            case 620339799:
                if (str.equals("policy_disable_screen_capture")) {
                    c = 4;
                    break;
                }
                break;
            case 1416425725:
                if (str.equals("policy_suspend_packages")) {
                    c = 6;
                    break;
                }
                break;
            case 1950494080:
                if (str.equals("no_outgoing_calls")) {
                    c = 1;
                    break;
                }
                break;
            case 2052329662:
                if (str.equals("policy_mandatory_backups")) {
                    c = 5;
                    break;
                }
                break;
            case 2135693260:
                if (str.equals("no_adjust_volume")) {
                    c = 0;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                textView.setText(R.string.disabled_by_policy_title_adjust_volume);
                return;
            case 1:
                textView.setText(R.string.disabled_by_policy_title_outgoing_calls);
                return;
            case 2:
                textView.setText(R.string.disabled_by_policy_title_sms);
                return;
            case 3:
                textView.setText(R.string.disabled_by_policy_title_camera);
                return;
            case 4:
                textView.setText(R.string.disabled_by_policy_title_screen_capture);
                return;
            case 5:
                textView.setText(R.string.disabled_by_policy_title_turn_off_backups);
                return;
            case 6:
                textView.setText(R.string.disabled_by_policy_title_suspend_packages);
                return;
            default:
                textView.setText(R.string.disabled_by_policy_title);
                return;
        }
    }

    void setAdminSupportDetails(Activity activity, View view, RestrictedLockUtils.EnforcedAdmin enforcedAdmin) {
        if (enforcedAdmin == null || enforcedAdmin.component == null) {
            return;
        }
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) activity.getSystemService("device_policy");
        if (!RestrictedLockUtils.isAdminInCurrentUserOrProfile(activity, enforcedAdmin.component) || !RestrictedLockUtils.isCurrentUserOrProfile(activity, enforcedAdmin.userId)) {
            enforcedAdmin.component = null;
            return;
        }
        if (enforcedAdmin.userId == -10000) {
            enforcedAdmin.userId = UserHandle.myUserId();
        }
        CharSequence shortSupportMessageForUser = UserHandle.isSameApp(Process.myUid(), 1000) ? devicePolicyManager.getShortSupportMessageForUser(enforcedAdmin.component, enforcedAdmin.userId) : null;
        if (shortSupportMessageForUser != null) {
            ((TextView) view.findViewById(R.id.admin_support_msg)).setText(shortSupportMessageForUser);
        }
    }

    void showAdminPolicies(RestrictedLockUtils.EnforcedAdmin enforcedAdmin, Activity activity) {
        Intent intent = new Intent();
        if (enforcedAdmin.component != null) {
            intent.setClass(activity, DeviceAdminAdd.class);
            intent.putExtra("android.app.extra.DEVICE_ADMIN", enforcedAdmin.component);
            intent.putExtra("android.app.extra.CALLED_FROM_SUPPORT_DIALOG", true);
            activity.startActivityAsUser(intent, new UserHandle(enforcedAdmin.userId));
            return;
        }
        intent.setClass(activity, Settings.DeviceAdminSettingsActivity.class);
        intent.addFlags(268435456);
        activity.startActivity(intent);
    }
}

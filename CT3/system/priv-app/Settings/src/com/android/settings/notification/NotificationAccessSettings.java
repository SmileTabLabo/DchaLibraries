package com.android.settings.notification;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import com.android.settings.R;
import com.android.settings.utils.ManagedServiceSettings;
/* loaded from: classes.dex */
public class NotificationAccessSettings extends ManagedServiceSettings {
    private NotificationManager mNm;
    private static final String TAG = NotificationAccessSettings.class.getSimpleName();
    private static final ManagedServiceSettings.Config CONFIG = getNotificationListenerConfig();

    @Override // com.android.settings.utils.ManagedServiceSettings, com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mNm = (NotificationManager) getSystemService("notification");
    }

    private static ManagedServiceSettings.Config getNotificationListenerConfig() {
        ManagedServiceSettings.Config c = new ManagedServiceSettings.Config();
        c.tag = TAG;
        c.setting = "enabled_notification_listeners";
        c.intentAction = "android.service.notification.NotificationListenerService";
        c.permission = "android.permission.BIND_NOTIFICATION_LISTENER_SERVICE";
        c.noun = "notification listener";
        c.warningDialogTitle = R.string.notification_listener_security_warning_title;
        c.warningDialogSummary = R.string.notification_listener_security_warning_summary;
        c.emptyText = R.string.no_notification_listeners;
        return c;
    }

    @Override // com.android.settings.InstrumentedPreferenceFragment
    protected int getMetricsCategory() {
        return 179;
    }

    @Override // com.android.settings.utils.ManagedServiceSettings
    protected ManagedServiceSettings.Config getConfig() {
        return CONFIG;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.utils.ManagedServiceSettings
    public boolean setEnabled(ComponentName service, String title, boolean enable) {
        if (!enable) {
            if (!this.mServiceListing.isEnabled(service)) {
                return true;
            }
            new FriendlyWarningDialogFragment().setServiceInfo(service, title).show(getFragmentManager(), "friendlydialog");
            return false;
        }
        return super.setEnabled(service, title, enable);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void deleteRules(final Context context, final String pkg) {
        AsyncTask.execute(new Runnable() { // from class: com.android.settings.notification.NotificationAccessSettings.1
            @Override // java.lang.Runnable
            public void run() {
                NotificationManager mgr = (NotificationManager) context.getSystemService(NotificationManager.class);
                mgr.removeAutomaticZenRules(pkg);
            }
        });
    }

    /* loaded from: classes.dex */
    public class FriendlyWarningDialogFragment extends DialogFragment {
        public FriendlyWarningDialogFragment() {
        }

        public FriendlyWarningDialogFragment setServiceInfo(ComponentName cn, String label) {
            Bundle args = new Bundle();
            args.putString("c", cn.flattenToString());
            args.putString("l", label);
            setArguments(args);
            return this;
        }

        @Override // android.app.DialogFragment
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Bundle args = getArguments();
            String label = args.getString("l");
            final ComponentName cn = ComponentName.unflattenFromString(args.getString("c"));
            String summary = getResources().getString(R.string.notification_listener_disable_warning_summary, label);
            return new AlertDialog.Builder(NotificationAccessSettings.this.mContext).setMessage(summary).setCancelable(true).setPositiveButton(R.string.notification_listener_disable_warning_confirm, new DialogInterface.OnClickListener() { // from class: com.android.settings.notification.NotificationAccessSettings.FriendlyWarningDialogFragment.1
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialog, int id) {
                    NotificationAccessSettings.this.mServiceListing.setEnabled(cn, false);
                    if (NotificationAccessSettings.this.mNm.isNotificationPolicyAccessGrantedForPackage(cn.getPackageName())) {
                        return;
                    }
                    NotificationAccessSettings.deleteRules(NotificationAccessSettings.this.mContext, cn.getPackageName());
                }
            }).setNegativeButton(R.string.notification_listener_disable_warning_cancel, new DialogInterface.OnClickListener() { // from class: com.android.settings.notification.NotificationAccessSettings.FriendlyWarningDialogFragment.2
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialog, int id) {
                }
            }).create();
        }
    }
}

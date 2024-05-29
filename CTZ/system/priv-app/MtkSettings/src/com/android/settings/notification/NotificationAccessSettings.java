package com.android.settings.notification;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Pair;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settings.notification.NotificationAccessSettings;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.utils.ManagedServiceSettings;
/* loaded from: classes.dex */
public class NotificationAccessSettings extends ManagedServiceSettings {
    private NotificationManager mNm;
    private static final String TAG = NotificationAccessSettings.class.getSimpleName();
    private static final ManagedServiceSettings.Config CONFIG = new ManagedServiceSettings.Config.Builder().setTag(TAG).setSetting("enabled_notification_listeners").setIntentAction("android.service.notification.NotificationListenerService").setPermission("android.permission.BIND_NOTIFICATION_LISTENER_SERVICE").setNoun("notification listener").setWarningDialogTitle(R.string.notification_listener_security_warning_title).setWarningDialogSummary(R.string.notification_listener_security_warning_summary).setEmptyText(R.string.no_notification_listeners).build();

    @Override // com.android.settingslib.core.instrumentation.Instrumentable
    public int getMetricsCategory() {
        return 179;
    }

    @Override // com.android.settings.core.InstrumentedPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mNm = (NotificationManager) context.getSystemService(NotificationManager.class);
    }

    @Override // com.android.settings.utils.ManagedServiceSettings
    protected ManagedServiceSettings.Config getConfig() {
        return CONFIG;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.utils.ManagedServiceSettings
    public boolean setEnabled(ComponentName componentName, String str, boolean z) {
        logSpecialPermissionChange(z, componentName.getPackageName());
        if (!z) {
            if (isServiceEnabled(componentName)) {
                new FriendlyWarningDialogFragment().setServiceInfo(componentName, str, this).show(getFragmentManager(), "friendlydialog");
                return false;
            }
            return true;
        } else if (isServiceEnabled(componentName)) {
            return true;
        } else {
            new ManagedServiceSettings.ScaryWarningDialogFragment().setServiceInfo(componentName, str, this).show(getFragmentManager(), "dialog");
            return false;
        }
    }

    @Override // com.android.settings.utils.ManagedServiceSettings
    protected boolean isServiceEnabled(ComponentName componentName) {
        return this.mNm.isNotificationListenerAccessGranted(componentName);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.utils.ManagedServiceSettings
    public void enable(ComponentName componentName) {
        this.mNm.setNotificationListenerAccessGranted(componentName, true);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.core.InstrumentedPreferenceFragment
    public int getPreferenceScreenResId() {
        return R.xml.notification_access_settings;
    }

    @VisibleForTesting
    void logSpecialPermissionChange(boolean z, String str) {
        FeatureFactory.getFactory(getContext()).getMetricsFeatureProvider().action(getContext(), z ? 776 : 777, str, new Pair[0]);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void disable(final NotificationAccessSettings notificationAccessSettings, final ComponentName componentName) {
        notificationAccessSettings.mNm.setNotificationListenerAccessGranted(componentName, false);
        AsyncTask.execute(new Runnable() { // from class: com.android.settings.notification.-$$Lambda$NotificationAccessSettings$5Getr2Y6VpjSaSB3qVPpmCZNr9A
            @Override // java.lang.Runnable
            public final void run() {
                NotificationAccessSettings.lambda$disable$0(NotificationAccessSettings.this, componentName);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ void lambda$disable$0(NotificationAccessSettings notificationAccessSettings, ComponentName componentName) {
        if (!notificationAccessSettings.mNm.isNotificationPolicyAccessGrantedForPackage(componentName.getPackageName())) {
            notificationAccessSettings.mNm.removeAutomaticZenRules(componentName.getPackageName());
        }
    }

    /* loaded from: classes.dex */
    public static class FriendlyWarningDialogFragment extends InstrumentedDialogFragment {
        public FriendlyWarningDialogFragment setServiceInfo(ComponentName componentName, String str, Fragment fragment) {
            Bundle bundle = new Bundle();
            bundle.putString("c", componentName.flattenToString());
            bundle.putString("l", str);
            setArguments(bundle);
            setTargetFragment(fragment, 0);
            return this;
        }

        @Override // com.android.settingslib.core.instrumentation.Instrumentable
        public int getMetricsCategory() {
            return 552;
        }

        @Override // android.app.DialogFragment
        public Dialog onCreateDialog(Bundle bundle) {
            Bundle arguments = getArguments();
            String string = arguments.getString("l");
            final ComponentName unflattenFromString = ComponentName.unflattenFromString(arguments.getString("c"));
            final NotificationAccessSettings notificationAccessSettings = (NotificationAccessSettings) getTargetFragment();
            return new AlertDialog.Builder(getContext()).setMessage(getResources().getString(R.string.notification_listener_disable_warning_summary, string)).setCancelable(true).setPositiveButton(R.string.notification_listener_disable_warning_confirm, new DialogInterface.OnClickListener() { // from class: com.android.settings.notification.-$$Lambda$NotificationAccessSettings$FriendlyWarningDialogFragment$ND5PkKgvmxdEIdAr9gHIhLyAwTU
                @Override // android.content.DialogInterface.OnClickListener
                public final void onClick(DialogInterface dialogInterface, int i) {
                    NotificationAccessSettings.disable(NotificationAccessSettings.this, unflattenFromString);
                }
            }).setNegativeButton(R.string.notification_listener_disable_warning_cancel, new DialogInterface.OnClickListener() { // from class: com.android.settings.notification.-$$Lambda$NotificationAccessSettings$FriendlyWarningDialogFragment$dxECkfkY-zLrkSsUm1OLKJMeIiE
                @Override // android.content.DialogInterface.OnClickListener
                public final void onClick(DialogInterface dialogInterface, int i) {
                    NotificationAccessSettings.FriendlyWarningDialogFragment.lambda$onCreateDialog$1(dialogInterface, i);
                }
            }).create();
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public static /* synthetic */ void lambda$onCreateDialog$1(DialogInterface dialogInterface, int i) {
        }
    }
}

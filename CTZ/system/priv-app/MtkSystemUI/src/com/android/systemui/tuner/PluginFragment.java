package com.android.systemui.tuner;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.BenesseExtension;
import android.os.Bundle;
import android.support.v14.preference.PreferenceFragment;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.view.View;
import com.android.systemui.R;
import com.android.systemui.plugins.PluginInstanceManager;
import com.android.systemui.plugins.PluginManager;
import com.android.systemui.plugins.PluginPrefs;
import com.android.systemui.tuner.PluginFragment;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;
/* loaded from: classes.dex */
public class PluginFragment extends PreferenceFragment {
    private PluginPrefs mPluginPrefs;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() { // from class: com.android.systemui.tuner.PluginFragment.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            PluginFragment.this.loadPrefs();
        }
    };

    @Override // android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        IntentFilter intentFilter = new IntentFilter("android.intent.action.PACKAGE_ADDED");
        intentFilter.addAction("android.intent.action.PACKAGE_CHANGED");
        intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        intentFilter.addDataScheme("package");
        getContext().registerReceiver(this.mReceiver, intentFilter);
        getContext().registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.USER_UNLOCKED"));
    }

    @Override // android.app.Fragment
    public void onDestroy() {
        super.onDestroy();
        getContext().unregisterReceiver(this.mReceiver);
    }

    @Override // android.support.v14.preference.PreferenceFragment
    public void onCreatePreferences(Bundle bundle, String str) {
        loadPrefs();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void loadPrefs() {
        final PreferenceScreen createPreferenceScreen = getPreferenceManager().createPreferenceScreen(getContext());
        createPreferenceScreen.setOrderingAsAdded(false);
        final Context context = getPreferenceManager().getContext();
        this.mPluginPrefs = new PluginPrefs(getContext());
        PackageManager packageManager = getContext().getPackageManager();
        Set<String> pluginList = this.mPluginPrefs.getPluginList();
        final ArrayMap arrayMap = new ArrayMap();
        for (String str : pluginList) {
            String name = toName(str);
            for (ResolveInfo resolveInfo : packageManager.queryIntentServices(new Intent(str), 512)) {
                String str2 = resolveInfo.serviceInfo.packageName;
                if (!arrayMap.containsKey(str2)) {
                    arrayMap.put(str2, new ArraySet());
                }
                ((ArraySet) arrayMap.get(str2)).add(name);
            }
        }
        packageManager.getPackagesHoldingPermissions(new String[]{PluginInstanceManager.PLUGIN_PERMISSION}, 516).forEach(new Consumer() { // from class: com.android.systemui.tuner.-$$Lambda$PluginFragment$-gWfSjbBwCV0_TE_pdcQVko9wUY
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                PluginFragment.lambda$loadPrefs$0(PluginFragment.this, arrayMap, context, createPreferenceScreen, (PackageInfo) obj);
            }
        });
        setPreferenceScreen(createPreferenceScreen);
    }

    public static /* synthetic */ void lambda$loadPrefs$0(PluginFragment pluginFragment, ArrayMap arrayMap, Context context, PreferenceScreen preferenceScreen, PackageInfo packageInfo) {
        if (arrayMap.containsKey(packageInfo.packageName)) {
            PluginPreference pluginPreference = new PluginPreference(context, packageInfo);
            pluginPreference.setSummary("Plugins: " + pluginFragment.toString((ArraySet) arrayMap.get(packageInfo.packageName)));
            preferenceScreen.addPreference(pluginPreference);
        }
    }

    private String toString(ArraySet<String> arraySet) {
        StringBuilder sb = new StringBuilder();
        Iterator<String> it = arraySet.iterator();
        while (it.hasNext()) {
            String next = it.next();
            if (sb.length() != 0) {
                sb.append(", ");
            }
            sb.append(next);
        }
        return sb.toString();
    }

    private String toName(String str) {
        String[] split;
        String replace = str.replace("com.android.systemui.action.PLUGIN_", "");
        StringBuilder sb = new StringBuilder();
        for (String str2 : replace.split("_")) {
            if (sb.length() != 0) {
                sb.append(' ');
            }
            sb.append(str2.substring(0, 1));
            sb.append(str2.substring(1).toLowerCase());
        }
        return sb.toString();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class PluginPreference extends SwitchPreference {
        private final boolean mHasSettings;
        private final PackageInfo mInfo;
        private final PackageManager mPm;

        public PluginPreference(Context context, PackageInfo packageInfo) {
            super(context);
            this.mPm = context.getPackageManager();
            this.mHasSettings = this.mPm.resolveActivity(new Intent("com.android.systemui.action.PLUGIN_SETTINGS").setPackage(packageInfo.packageName), 0) != null;
            this.mInfo = packageInfo;
            setTitle(packageInfo.applicationInfo.loadLabel(this.mPm));
            setChecked(isPluginEnabled());
            setWidgetLayoutResource(R.layout.tuner_widget_settings_switch);
        }

        private boolean isPluginEnabled() {
            for (int i = 0; i < this.mInfo.services.length; i++) {
                if (this.mPm.getComponentEnabledSetting(new ComponentName(this.mInfo.packageName, this.mInfo.services[i].name)) == 2) {
                    return false;
                }
            }
            return true;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.support.v7.preference.Preference
        public boolean persistBoolean(boolean z) {
            int i;
            if (!z) {
                i = 2;
            } else {
                i = 1;
            }
            boolean z2 = false;
            for (int i2 = 0; i2 < this.mInfo.services.length; i2++) {
                ComponentName componentName = new ComponentName(this.mInfo.packageName, this.mInfo.services[i2].name);
                if (this.mPm.getComponentEnabledSetting(componentName) != i) {
                    this.mPm.setComponentEnabledSetting(componentName, i, 1);
                    z2 = true;
                }
            }
            if (z2) {
                String str = this.mInfo.packageName;
                getContext().sendBroadcast(new Intent(PluginManager.PLUGIN_CHANGED, str != null ? Uri.fromParts("package", str, null) : null));
            }
            return true;
        }

        @Override // android.support.v14.preference.SwitchPreference, android.support.v7.preference.Preference
        public void onBindViewHolder(PreferenceViewHolder preferenceViewHolder) {
            super.onBindViewHolder(preferenceViewHolder);
            preferenceViewHolder.findViewById(R.id.settings).setVisibility(this.mHasSettings ? 0 : 8);
            preferenceViewHolder.findViewById(R.id.divider).setVisibility(this.mHasSettings ? 0 : 8);
            preferenceViewHolder.findViewById(R.id.settings).setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.tuner.-$$Lambda$PluginFragment$PluginPreference$Xt_y65tw1Tc7XykRWrNNbIDklTs
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    PluginFragment.PluginPreference.lambda$onBindViewHolder$0(PluginFragment.PluginPreference.this, view);
                }
            });
            preferenceViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() { // from class: com.android.systemui.tuner.-$$Lambda$PluginFragment$PluginPreference$hyhKFHxbkbEXGxqXV7_N3Il_7XE
                @Override // android.view.View.OnLongClickListener
                public final boolean onLongClick(View view) {
                    return PluginFragment.PluginPreference.lambda$onBindViewHolder$1(PluginFragment.PluginPreference.this, view);
                }
            });
        }

        public static /* synthetic */ void lambda$onBindViewHolder$0(PluginPreference pluginPreference, View view) {
            ResolveInfo resolveActivity = view.getContext().getPackageManager().resolveActivity(new Intent("com.android.systemui.action.PLUGIN_SETTINGS").setPackage(pluginPreference.mInfo.packageName), 0);
            if (resolveActivity != null) {
                view.getContext().startActivity(new Intent().setComponent(new ComponentName(resolveActivity.activityInfo.packageName, resolveActivity.activityInfo.name)));
            }
        }

        public static /* synthetic */ boolean lambda$onBindViewHolder$1(PluginPreference pluginPreference, View view) {
            if (BenesseExtension.getDchaState() != 0) {
                return true;
            }
            Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
            intent.setData(Uri.fromParts("package", pluginPreference.mInfo.packageName, null));
            pluginPreference.getContext().startActivity(intent);
            return true;
        }
    }
}

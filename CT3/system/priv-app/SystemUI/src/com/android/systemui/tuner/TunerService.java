package com.android.systemui.tuner;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.android.systemui.SystemUI;
import com.android.systemui.SystemUIApplication;
import com.android.systemui.settings.CurrentUserTracker;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.phone.SystemUIDialog;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
/* loaded from: a.zip:com/android/systemui/tuner/TunerService.class */
public class TunerService extends SystemUI {
    private static TunerService sInstance;
    private ContentResolver mContentResolver;
    private int mCurrentUser;
    private CurrentUserTracker mUserTracker;
    private final Observer mObserver = new Observer(this);
    private final ArrayMap<Uri, String> mListeningUris = new ArrayMap<>();
    private final HashMap<String, Set<Tunable>> mTunableLookup = new HashMap<>();

    /* loaded from: a.zip:com/android/systemui/tuner/TunerService$ClearReceiver.class */
    public static class ClearReceiver extends BroadcastReceiver {
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if ("com.android.systemui.action.CLEAR_TUNER".equals(intent.getAction())) {
                TunerService.get(context).clearAll();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/tuner/TunerService$Observer.class */
    public class Observer extends ContentObserver {
        final TunerService this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public Observer(TunerService tunerService) {
            super(new Handler(Looper.getMainLooper()));
            this.this$0 = tunerService;
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z, Uri uri, int i) {
            if (i == ActivityManager.getCurrentUser()) {
                this.this$0.reloadSetting(uri);
            }
        }
    }

    /* loaded from: a.zip:com/android/systemui/tuner/TunerService$Tunable.class */
    public interface Tunable {
        void onTuningChanged(String str, String str2);
    }

    private void addTunable(Tunable tunable, String str) {
        if (!this.mTunableLookup.containsKey(str)) {
            this.mTunableLookup.put(str, new ArraySet());
        }
        this.mTunableLookup.get(str).add(tunable);
        Uri uriFor = Settings.Secure.getUriFor(str);
        if (!this.mListeningUris.containsKey(uriFor)) {
            this.mListeningUris.put(uriFor, str);
            this.mContentResolver.registerContentObserver(uriFor, false, this.mObserver, this.mCurrentUser);
        }
        tunable.onTuningChanged(str, Settings.Secure.getStringForUser(this.mContentResolver, str, this.mCurrentUser));
    }

    public static TunerService get(Context context) {
        TunerService tunerService = null;
        if (context.getApplicationContext() instanceof SystemUIApplication) {
            tunerService = (TunerService) ((SystemUIApplication) context.getApplicationContext()).getComponent(TunerService.class);
        }
        return tunerService == null ? getStaticService(context) : tunerService;
    }

    private static TunerService getStaticService(Context context) {
        if (sInstance == null) {
            sInstance = new TunerService();
            sInstance.mContext = context.getApplicationContext();
            sInstance.mComponents = new HashMap();
            sInstance.start();
        }
        return sInstance;
    }

    public static final boolean isTunerEnabled(Context context) {
        boolean z = true;
        if (userContext(context).getPackageManager().getComponentEnabledSetting(new ComponentName(context, TunerActivity.class)) != 1) {
            z = false;
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void reloadAll() {
        for (String str : this.mTunableLookup.keySet()) {
            String stringForUser = Settings.Secure.getStringForUser(this.mContentResolver, str, this.mCurrentUser);
            for (Tunable tunable : this.mTunableLookup.get(str)) {
                tunable.onTuningChanged(str, stringForUser);
            }
        }
    }

    public static final void setTunerEnabled(Context context, boolean z) {
        userContext(context).getPackageManager().setComponentEnabledSetting(new ComponentName(context, TunerActivity.class), 2, 1);
    }

    public static final void showResetRequest(Context context, Runnable runnable) {
        SystemUIDialog systemUIDialog = new SystemUIDialog(context);
        systemUIDialog.setShowForAllUsers(true);
        systemUIDialog.setMessage(2131493737);
        systemUIDialog.setButton(-2, context.getString(2131493363), (DialogInterface.OnClickListener) null);
        systemUIDialog.setButton(-1, context.getString(2131493633), new DialogInterface.OnClickListener(context, runnable) { // from class: com.android.systemui.tuner.TunerService.2
            final Context val$context;
            final Runnable val$onDisabled;

            {
                this.val$context = context;
                this.val$onDisabled = runnable;
            }

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                this.val$context.sendBroadcast(new Intent("com.android.systemui.action.CLEAR_TUNER"));
                TunerService.setTunerEnabled(this.val$context, false);
                Settings.Secure.putInt(this.val$context.getContentResolver(), "seen_tuner_warning", 0);
                if (this.val$onDisabled != null) {
                    this.val$onDisabled.run();
                }
            }
        });
        systemUIDialog.show();
    }

    private void upgradeTuner(int i, int i2) {
        String value;
        if (i < 1 && (value = getValue("icon_blacklist")) != null) {
            ArraySet<String> iconBlacklist = StatusBarIconController.getIconBlacklist(value);
            iconBlacklist.add("rotate");
            iconBlacklist.add("headset");
            Settings.Secure.putStringForUser(this.mContentResolver, "icon_blacklist", TextUtils.join(",", iconBlacklist), this.mCurrentUser);
        }
        setValue("sysui_tuner_version", i2);
    }

    private static Context userContext(Context context) {
        try {
            return context.createPackageContextAsUser(context.getPackageName(), 0, new UserHandle(ActivityManager.getCurrentUser()));
        } catch (PackageManager.NameNotFoundException e) {
            return context;
        }
    }

    public void addTunable(Tunable tunable, String... strArr) {
        for (String str : strArr) {
            addTunable(tunable, str);
        }
    }

    public void clearAll() {
        Settings.Global.putString(this.mContentResolver, "sysui_demo_allowed", null);
        Settings.System.putString(this.mContentResolver, "status_bar_show_battery_percent", null);
        Intent intent = new Intent("com.android.systemui.demo");
        intent.putExtra("command", "exit");
        this.mContext.sendBroadcast(intent);
        for (String str : this.mTunableLookup.keySet()) {
            Settings.Secure.putString(this.mContentResolver, str, null);
        }
    }

    public int getValue(String str, int i) {
        return Settings.Secure.getIntForUser(this.mContentResolver, str, i, this.mCurrentUser);
    }

    public String getValue(String str) {
        return Settings.Secure.getStringForUser(this.mContentResolver, str, this.mCurrentUser);
    }

    public void reloadSetting(Uri uri) {
        String str = this.mListeningUris.get(uri);
        Set<Tunable> set = this.mTunableLookup.get(str);
        if (set == null) {
            return;
        }
        String stringForUser = Settings.Secure.getStringForUser(this.mContentResolver, str, this.mCurrentUser);
        for (Tunable tunable : set) {
            tunable.onTuningChanged(str, stringForUser);
        }
    }

    public void removeTunable(Tunable tunable) {
        Iterator<T> it = this.mTunableLookup.values().iterator();
        while (it.hasNext()) {
            ((Set) it.next()).remove(tunable);
        }
    }

    protected void reregisterAll() {
        if (this.mListeningUris.size() == 0) {
            return;
        }
        this.mContentResolver.unregisterContentObserver(this.mObserver);
        for (Uri uri : this.mListeningUris.keySet()) {
            this.mContentResolver.registerContentObserver(uri, false, this.mObserver, this.mCurrentUser);
        }
    }

    public void setValue(String str, int i) {
        Settings.Secure.putIntForUser(this.mContentResolver, str, i, this.mCurrentUser);
    }

    public void setValue(String str, String str2) {
        Settings.Secure.putStringForUser(this.mContentResolver, str, str2, this.mCurrentUser);
    }

    @Override // com.android.systemui.SystemUI
    public void start() {
        this.mContentResolver = this.mContext.getContentResolver();
        for (UserInfo userInfo : UserManager.get(this.mContext).getUsers()) {
            this.mCurrentUser = userInfo.getUserHandle().getIdentifier();
            if (getValue("sysui_tuner_version", 0) != 1) {
                upgradeTuner(getValue("sysui_tuner_version", 0), 1);
            }
        }
        putComponent(TunerService.class, this);
        this.mCurrentUser = ActivityManager.getCurrentUser();
        this.mUserTracker = new CurrentUserTracker(this, this.mContext) { // from class: com.android.systemui.tuner.TunerService.1
            final TunerService this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.systemui.settings.CurrentUserTracker
            public void onUserSwitched(int i) {
                this.this$0.mCurrentUser = i;
                this.this$0.reloadAll();
                this.this$0.reregisterAll();
            }
        };
        this.mUserTracker.startTracking();
    }
}

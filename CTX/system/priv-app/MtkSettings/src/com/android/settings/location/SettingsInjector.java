package com.android.settings.location;

import android.R;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.IconDrawableFactory;
import android.util.Log;
import android.util.Xml;
import com.android.settings.location.InjectedSetting;
import com.android.settings.widget.AppPreference;
import com.android.settings.widget.RestrictedAppPreference;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.xmlpull.v1.XmlPullParserException;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class SettingsInjector {
    private final Context mContext;
    private final Set<Setting> mSettings = new HashSet();
    private final Handler mHandler = new StatusLoadingHandler();

    public SettingsInjector(Context context) {
        this.mContext = context;
    }

    private List<InjectedSetting> getSettings(UserHandle userHandle) {
        PackageManager packageManager = this.mContext.getPackageManager();
        Intent intent = new Intent("android.location.SettingInjectorService");
        int identifier = userHandle.getIdentifier();
        List<ResolveInfo> queryIntentServicesAsUser = packageManager.queryIntentServicesAsUser(intent, 128, identifier);
        if (Log.isLoggable("SettingsInjector", 3)) {
            Log.d("SettingsInjector", "Found services for profile id " + identifier + ": " + queryIntentServicesAsUser);
        }
        ArrayList arrayList = new ArrayList(queryIntentServicesAsUser.size());
        for (ResolveInfo resolveInfo : queryIntentServicesAsUser) {
            try {
                InjectedSetting parseServiceInfo = parseServiceInfo(resolveInfo, userHandle, packageManager);
                if (parseServiceInfo == null) {
                    Log.w("SettingsInjector", "Unable to load service info " + resolveInfo);
                } else {
                    arrayList.add(parseServiceInfo);
                }
            } catch (IOException e) {
                Log.w("SettingsInjector", "Unable to load service info " + resolveInfo, e);
            } catch (XmlPullParserException e2) {
                Log.w("SettingsInjector", "Unable to load service info " + resolveInfo, e2);
            }
        }
        if (Log.isLoggable("SettingsInjector", 3)) {
            Log.d("SettingsInjector", "Loaded settings for profile id " + identifier + ": " + arrayList);
        }
        return arrayList;
    }

    private static InjectedSetting parseServiceInfo(ResolveInfo resolveInfo, UserHandle userHandle, PackageManager packageManager) throws XmlPullParserException, IOException {
        XmlResourceParser xmlResourceParser;
        ServiceInfo serviceInfo = resolveInfo.serviceInfo;
        if ((serviceInfo.applicationInfo.flags & 1) == 0 && Log.isLoggable("SettingsInjector", 5)) {
            Log.w("SettingsInjector", "Ignoring attempt to inject setting from app not in system image: " + resolveInfo);
            return null;
        }
        try {
            try {
                xmlResourceParser = serviceInfo.loadXmlMetaData(packageManager, "android.location.SettingInjectorService");
            } catch (Throwable th) {
                th = th;
                xmlResourceParser = null;
            }
        } catch (PackageManager.NameNotFoundException e) {
        }
        try {
            if (xmlResourceParser == null) {
                throw new XmlPullParserException("No android.location.SettingInjectorService meta-data for " + resolveInfo + ": " + serviceInfo);
            }
            AttributeSet asAttributeSet = Xml.asAttributeSet(xmlResourceParser);
            while (true) {
                int next = xmlResourceParser.next();
                if (next == 1 || next == 2) {
                    break;
                }
            }
            if ("injected-location-setting".equals(xmlResourceParser.getName())) {
                InjectedSetting parseAttributes = parseAttributes(serviceInfo.packageName, serviceInfo.name, userHandle, packageManager.getResourcesForApplicationAsUser(serviceInfo.packageName, userHandle.getIdentifier()), asAttributeSet);
                if (xmlResourceParser != null) {
                    xmlResourceParser.close();
                }
                return parseAttributes;
            }
            throw new XmlPullParserException("Meta-data does not start with injected-location-setting tag");
        } catch (PackageManager.NameNotFoundException e2) {
            throw new XmlPullParserException("Unable to load resources for package " + serviceInfo.packageName);
        } catch (Throwable th2) {
            th = th2;
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
            throw th;
        }
    }

    private static InjectedSetting parseAttributes(String str, String str2, UserHandle userHandle, Resources resources, AttributeSet attributeSet) {
        TypedArray obtainAttributes = resources.obtainAttributes(attributeSet, R.styleable.SettingInjectorService);
        try {
            String string = obtainAttributes.getString(1);
            int resourceId = obtainAttributes.getResourceId(0, 0);
            String string2 = obtainAttributes.getString(2);
            String string3 = obtainAttributes.getString(3);
            if (Log.isLoggable("SettingsInjector", 3)) {
                Log.d("SettingsInjector", "parsed title: " + string + ", iconId: " + resourceId + ", settingsActivity: " + string2);
            }
            return new InjectedSetting.Builder().setPackageName(str).setClassName(str2).setTitle(string).setIconId(resourceId).setUserHandle(userHandle).setSettingsActivity(string2).setUserRestriction(string3).build();
        } finally {
            obtainAttributes.recycle();
        }
    }

    public List<Preference> getInjectedSettings(Context context, int i) {
        List<UserHandle> userProfiles = ((UserManager) this.mContext.getSystemService("user")).getUserProfiles();
        ArrayList arrayList = new ArrayList();
        int size = userProfiles.size();
        for (int i2 = 0; i2 < size; i2++) {
            UserHandle userHandle = userProfiles.get(i2);
            if (i == -2 || i == userHandle.getIdentifier()) {
                for (InjectedSetting injectedSetting : getSettings(userHandle)) {
                    this.mSettings.add(new Setting(injectedSetting, addServiceSetting(context, arrayList, injectedSetting)));
                }
            }
        }
        reloadStatusMessages();
        return arrayList;
    }

    public boolean hasInjectedSettings(int i) {
        List<UserHandle> userProfiles = ((UserManager) this.mContext.getSystemService("user")).getUserProfiles();
        int size = userProfiles.size();
        for (int i2 = 0; i2 < size; i2++) {
            UserHandle userHandle = userProfiles.get(i2);
            if (i == -2 || i == userHandle.getIdentifier()) {
                Iterator<T> it = getSettings(userHandle).iterator();
                if (it.hasNext()) {
                    InjectedSetting injectedSetting = (InjectedSetting) it.next();
                    return true;
                }
            }
        }
        return false;
    }

    public void reloadStatusMessages() {
        if (Log.isLoggable("SettingsInjector", 3)) {
            Log.d("SettingsInjector", "reloadingStatusMessages: " + this.mSettings);
        }
        this.mHandler.sendMessage(this.mHandler.obtainMessage(1));
    }

    private Preference addServiceSetting(Context context, List<Preference> list, InjectedSetting injectedSetting) {
        Drawable drawable;
        Preference restrictedAppPreference;
        PackageManager packageManager = this.mContext.getPackageManager();
        try {
            PackageItemInfo packageItemInfo = new PackageItemInfo();
            packageItemInfo.icon = injectedSetting.iconId;
            packageItemInfo.packageName = injectedSetting.packageName;
            drawable = IconDrawableFactory.newInstance(this.mContext).getBadgedIcon(packageItemInfo, packageManager.getApplicationInfo(injectedSetting.packageName, 128), injectedSetting.mUserHandle.getIdentifier());
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("SettingsInjector", "Can't get ApplicationInfo for " + injectedSetting.packageName, e);
            drawable = null;
        }
        if (TextUtils.isEmpty(injectedSetting.userRestriction)) {
            restrictedAppPreference = new AppPreference(context);
        } else {
            restrictedAppPreference = new RestrictedAppPreference(context, injectedSetting.userRestriction);
        }
        restrictedAppPreference.setTitle(injectedSetting.title);
        restrictedAppPreference.setSummary((CharSequence) null);
        restrictedAppPreference.setIcon(drawable);
        restrictedAppPreference.setOnPreferenceClickListener(new ServiceSettingClickedListener(injectedSetting));
        list.add(restrictedAppPreference);
        return restrictedAppPreference;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class ServiceSettingClickedListener implements Preference.OnPreferenceClickListener {
        private InjectedSetting mInfo;

        public ServiceSettingClickedListener(InjectedSetting injectedSetting) {
            this.mInfo = injectedSetting;
        }

        @Override // android.support.v7.preference.Preference.OnPreferenceClickListener
        public boolean onPreferenceClick(Preference preference) {
            Intent intent = new Intent();
            intent.setClassName(this.mInfo.packageName, this.mInfo.settingsActivity);
            intent.setFlags(268468224);
            SettingsInjector.this.mContext.startActivityAsUser(intent, this.mInfo.mUserHandle);
            return true;
        }
    }

    /* loaded from: classes.dex */
    private final class StatusLoadingHandler extends Handler {
        private boolean mReloadRequested;
        private Set<Setting> mSettingsBeingLoaded;
        private Set<Setting> mSettingsToLoad;
        private Set<Setting> mTimedOutSettings;

        private StatusLoadingHandler() {
            super(Looper.getMainLooper());
            this.mSettingsToLoad = new HashSet();
            this.mSettingsBeingLoaded = new HashSet();
            this.mTimedOutSettings = new HashSet();
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            if (Log.isLoggable("SettingsInjector", 3)) {
                Log.d("SettingsInjector", "handleMessage start: " + message + ", " + this);
            }
            switch (message.what) {
                case 1:
                    this.mReloadRequested = true;
                    break;
                case 2:
                    Setting setting = (Setting) message.obj;
                    setting.maybeLogElapsedTime();
                    this.mSettingsBeingLoaded.remove(setting);
                    this.mTimedOutSettings.remove(setting);
                    removeMessages(3, setting);
                    break;
                case 3:
                    Setting setting2 = (Setting) message.obj;
                    this.mSettingsBeingLoaded.remove(setting2);
                    this.mTimedOutSettings.add(setting2);
                    if (Log.isLoggable("SettingsInjector", 5)) {
                        Log.w("SettingsInjector", "Timed out after " + setting2.getElapsedTime() + " millis trying to get status for: " + setting2);
                        break;
                    }
                    break;
                default:
                    Log.wtf("SettingsInjector", "Unexpected what: " + message);
                    break;
            }
            if (this.mSettingsBeingLoaded.size() > 0 || this.mTimedOutSettings.size() > 1) {
                if (Log.isLoggable("SettingsInjector", 2)) {
                    Log.v("SettingsInjector", "too many services already live for " + message + ", " + this);
                    return;
                }
                return;
            }
            if (this.mReloadRequested && this.mSettingsToLoad.isEmpty() && this.mSettingsBeingLoaded.isEmpty() && this.mTimedOutSettings.isEmpty()) {
                if (Log.isLoggable("SettingsInjector", 2)) {
                    Log.v("SettingsInjector", "reloading because idle and reload requesteed " + message + ", " + this);
                }
                this.mSettingsToLoad.addAll(SettingsInjector.this.mSettings);
                this.mReloadRequested = false;
            }
            Iterator<Setting> it = this.mSettingsToLoad.iterator();
            if (!it.hasNext()) {
                if (Log.isLoggable("SettingsInjector", 2)) {
                    Log.v("SettingsInjector", "nothing left to do for " + message + ", " + this);
                    return;
                }
                return;
            }
            Setting next = it.next();
            it.remove();
            next.startService();
            this.mSettingsBeingLoaded.add(next);
            sendMessageDelayed(obtainMessage(3, next), 1000L);
            if (Log.isLoggable("SettingsInjector", 3)) {
                Log.d("SettingsInjector", "handleMessage end " + message + ", " + this + ", started loading " + next);
            }
        }

        @Override // android.os.Handler
        public String toString() {
            return "StatusLoadingHandler{mSettingsToLoad=" + this.mSettingsToLoad + ", mSettingsBeingLoaded=" + this.mSettingsBeingLoaded + ", mTimedOutSettings=" + this.mTimedOutSettings + ", mReloadRequested=" + this.mReloadRequested + '}';
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public final class Setting {
        public final Preference preference;
        public final InjectedSetting setting;
        public long startMillis;

        private Setting(InjectedSetting injectedSetting, Preference preference) {
            this.setting = injectedSetting;
            this.preference = preference;
        }

        public String toString() {
            return "Setting{setting=" + this.setting + ", preference=" + this.preference + '}';
        }

        public boolean equals(Object obj) {
            return this == obj || ((obj instanceof Setting) && this.setting.equals(((Setting) obj).setting));
        }

        public int hashCode() {
            return this.setting.hashCode();
        }

        public void startService() {
            if (!((ActivityManager) SettingsInjector.this.mContext.getSystemService("activity")).isUserRunning(this.setting.mUserHandle.getIdentifier())) {
                if (Log.isLoggable("SettingsInjector", 2)) {
                    Log.v("SettingsInjector", "Cannot start service as user " + this.setting.mUserHandle.getIdentifier() + " is not running");
                    return;
                }
                return;
            }
            Handler handler = new Handler() { // from class: com.android.settings.location.SettingsInjector.Setting.1
                @Override // android.os.Handler
                public void handleMessage(Message message) {
                    Bundle data = message.getData();
                    boolean z = data.getBoolean("enabled", true);
                    if (Log.isLoggable("SettingsInjector", 3)) {
                        Log.d("SettingsInjector", Setting.this.setting + ": received " + message + ", bundle: " + data);
                    }
                    Setting.this.preference.setSummary((CharSequence) null);
                    Setting.this.preference.setEnabled(z);
                    SettingsInjector.this.mHandler.sendMessage(SettingsInjector.this.mHandler.obtainMessage(2, Setting.this));
                }
            };
            Messenger messenger = new Messenger(handler);
            Intent serviceIntent = this.setting.getServiceIntent();
            serviceIntent.putExtra("messenger", messenger);
            if (Log.isLoggable("SettingsInjector", 3)) {
                Log.d("SettingsInjector", this.setting + ": sending update intent: " + serviceIntent + ", handler: " + handler);
                this.startMillis = SystemClock.elapsedRealtime();
            } else {
                this.startMillis = 0L;
            }
            SettingsInjector.this.mContext.startServiceAsUser(serviceIntent, this.setting.mUserHandle);
        }

        public long getElapsedTime() {
            return SystemClock.elapsedRealtime() - this.startMillis;
        }

        public void maybeLogElapsedTime() {
            if (Log.isLoggable("SettingsInjector", 3) && this.startMillis != 0) {
                long elapsedTime = getElapsedTime();
                Log.d("SettingsInjector", this + " update took " + elapsedTime + " millis");
            }
        }
    }
}

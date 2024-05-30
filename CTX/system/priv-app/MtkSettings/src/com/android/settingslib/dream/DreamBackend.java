package com.android.settingslib.dream;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.service.dreams.IDreamManager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import com.android.internal.R;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.xmlpull.v1.XmlPullParserException;
/* loaded from: classes.dex */
public class DreamBackend {
    private static DreamBackend sInstance;
    private final Context mContext;
    private final boolean mDreamsActivatedOnDockByDefault;
    private final boolean mDreamsActivatedOnSleepByDefault;
    private final boolean mDreamsEnabledByDefault;
    private final IDreamManager mDreamManager = IDreamManager.Stub.asInterface(ServiceManager.getService("dreams"));
    private final DreamInfoComparator mComparator = new DreamInfoComparator(getDefaultDream());

    /* loaded from: classes.dex */
    public static class DreamInfo {
        public CharSequence caption;
        public ComponentName componentName;
        public Drawable icon;
        public boolean isActive;
        public ComponentName settingsComponentName;

        public String toString() {
            StringBuilder sb = new StringBuilder(DreamInfo.class.getSimpleName());
            sb.append('[');
            sb.append(this.caption);
            if (this.isActive) {
                sb.append(",active");
            }
            sb.append(',');
            sb.append(this.componentName);
            if (this.settingsComponentName != null) {
                sb.append("settings=");
                sb.append(this.settingsComponentName);
            }
            sb.append(']');
            return sb.toString();
        }
    }

    public static DreamBackend getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DreamBackend(context);
        }
        return sInstance;
    }

    public DreamBackend(Context context) {
        this.mContext = context;
        this.mDreamsEnabledByDefault = context.getResources().getBoolean(17956941);
        this.mDreamsActivatedOnSleepByDefault = context.getResources().getBoolean(17956940);
        this.mDreamsActivatedOnDockByDefault = context.getResources().getBoolean(17956939);
    }

    public List<DreamInfo> getDreamInfos() {
        logd("getDreamInfos()", new Object[0]);
        ComponentName activeDream = getActiveDream();
        PackageManager packageManager = this.mContext.getPackageManager();
        List<ResolveInfo> queryIntentServices = packageManager.queryIntentServices(new Intent("android.service.dreams.DreamService"), 128);
        ArrayList arrayList = new ArrayList(queryIntentServices.size());
        for (ResolveInfo resolveInfo : queryIntentServices) {
            if (resolveInfo.serviceInfo != null) {
                DreamInfo dreamInfo = new DreamInfo();
                dreamInfo.caption = resolveInfo.loadLabel(packageManager);
                dreamInfo.icon = resolveInfo.loadIcon(packageManager);
                dreamInfo.componentName = getDreamComponentName(resolveInfo);
                dreamInfo.isActive = dreamInfo.componentName.equals(activeDream);
                dreamInfo.settingsComponentName = getSettingsComponentName(packageManager, resolveInfo);
                arrayList.add(dreamInfo);
            }
        }
        Collections.sort(arrayList, this.mComparator);
        return arrayList;
    }

    public ComponentName getDefaultDream() {
        if (this.mDreamManager == null) {
            return null;
        }
        try {
            return this.mDreamManager.getDefaultDreamComponent();
        } catch (RemoteException e) {
            Log.w("DreamBackend", "Failed to get default dream", e);
            return null;
        }
    }

    public CharSequence getActiveDreamName() {
        ComponentName activeDream = getActiveDream();
        if (activeDream != null) {
            PackageManager packageManager = this.mContext.getPackageManager();
            try {
                ServiceInfo serviceInfo = packageManager.getServiceInfo(activeDream, 0);
                if (serviceInfo != null) {
                    return serviceInfo.loadLabel(packageManager);
                }
            } catch (PackageManager.NameNotFoundException e) {
                return null;
            }
        }
        return null;
    }

    public int getWhenToDreamSetting() {
        if (isEnabled()) {
            if (isActivatedOnDock() && isActivatedOnSleep()) {
                return 2;
            }
            if (isActivatedOnDock()) {
                return 1;
            }
            return isActivatedOnSleep() ? 0 : 3;
        }
        return 3;
    }

    public void setWhenToDream(int i) {
        setEnabled(i != 3);
        switch (i) {
            case 0:
                setActivatedOnDock(false);
                setActivatedOnSleep(true);
                return;
            case 1:
                setActivatedOnDock(true);
                setActivatedOnSleep(false);
                return;
            case 2:
                setActivatedOnDock(true);
                setActivatedOnSleep(true);
                return;
            default:
                return;
        }
    }

    public boolean isEnabled() {
        return getBoolean("screensaver_enabled", this.mDreamsEnabledByDefault);
    }

    public void setEnabled(boolean z) {
        logd("setEnabled(%s)", Boolean.valueOf(z));
        setBoolean("screensaver_enabled", z);
    }

    public boolean isActivatedOnDock() {
        return getBoolean("screensaver_activate_on_dock", this.mDreamsActivatedOnDockByDefault);
    }

    public void setActivatedOnDock(boolean z) {
        logd("setActivatedOnDock(%s)", Boolean.valueOf(z));
        setBoolean("screensaver_activate_on_dock", z);
    }

    public boolean isActivatedOnSleep() {
        return getBoolean("screensaver_activate_on_sleep", this.mDreamsActivatedOnSleepByDefault);
    }

    public void setActivatedOnSleep(boolean z) {
        logd("setActivatedOnSleep(%s)", Boolean.valueOf(z));
        setBoolean("screensaver_activate_on_sleep", z);
    }

    private boolean getBoolean(String str, boolean z) {
        return Settings.Secure.getInt(this.mContext.getContentResolver(), str, z ? 1 : 0) == 1;
    }

    private void setBoolean(String str, boolean z) {
        Settings.Secure.putInt(this.mContext.getContentResolver(), str, z ? 1 : 0);
    }

    public void setActiveDream(ComponentName componentName) {
        logd("setActiveDream(%s)", componentName);
        if (this.mDreamManager == null) {
            return;
        }
        try {
            ComponentName[] componentNameArr = {componentName};
            IDreamManager iDreamManager = this.mDreamManager;
            if (componentName == null) {
                componentNameArr = null;
            }
            iDreamManager.setDreamComponents(componentNameArr);
        } catch (RemoteException e) {
            Log.w("DreamBackend", "Failed to set active dream to " + componentName, e);
        }
    }

    public ComponentName getActiveDream() {
        if (this.mDreamManager == null) {
            return null;
        }
        try {
            ComponentName[] dreamComponents = this.mDreamManager.getDreamComponents();
            if (dreamComponents == null || dreamComponents.length <= 0) {
                return null;
            }
            return dreamComponents[0];
        } catch (RemoteException e) {
            Log.w("DreamBackend", "Failed to get active dream", e);
            return null;
        }
    }

    public void launchSettings(DreamInfo dreamInfo) {
        logd("launchSettings(%s)", dreamInfo);
        if (dreamInfo == null || dreamInfo.settingsComponentName == null) {
            return;
        }
        this.mContext.startActivity(new Intent().setComponent(dreamInfo.settingsComponentName));
    }

    public void startDreaming() {
        logd("startDreaming()", new Object[0]);
        if (this.mDreamManager == null) {
            return;
        }
        try {
            this.mDreamManager.dream();
        } catch (RemoteException e) {
            Log.w("DreamBackend", "Failed to dream", e);
        }
    }

    private static ComponentName getDreamComponentName(ResolveInfo resolveInfo) {
        if (resolveInfo == null || resolveInfo.serviceInfo == null) {
            return null;
        }
        return new ComponentName(resolveInfo.serviceInfo.packageName, resolveInfo.serviceInfo.name);
    }

    /* JADX WARN: Removed duplicated region for block: B:50:0x008a  */
    /* JADX WARN: Removed duplicated region for block: B:52:0x00a5  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private static ComponentName getSettingsComponentName(PackageManager packageManager, ResolveInfo resolveInfo) {
        XmlResourceParser xmlResourceParser;
        String str;
        if (resolveInfo == null || resolveInfo.serviceInfo == null || resolveInfo.serviceInfo.metaData == null) {
            return null;
        }
        try {
            xmlResourceParser = resolveInfo.serviceInfo.loadXmlMetaData(packageManager, "android.service.dream");
            try {
                try {
                } catch (Throwable th) {
                    th = th;
                    if (xmlResourceParser != null) {
                        xmlResourceParser.close();
                    }
                    throw th;
                }
            } catch (PackageManager.NameNotFoundException | IOException | XmlPullParserException e) {
                e = e;
                str = null;
            }
        } catch (PackageManager.NameNotFoundException | IOException | XmlPullParserException e2) {
            e = e2;
            xmlResourceParser = null;
            str = null;
        } catch (Throwable th2) {
            th = th2;
            xmlResourceParser = null;
        }
        if (xmlResourceParser == null) {
            Log.w("DreamBackend", "No android.service.dream meta-data");
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
            return null;
        }
        Resources resourcesForApplication = packageManager.getResourcesForApplication(resolveInfo.serviceInfo.applicationInfo);
        AttributeSet asAttributeSet = Xml.asAttributeSet(xmlResourceParser);
        while (true) {
            int next = xmlResourceParser.next();
            if (next == 1 || next == 2) {
                break;
            }
        }
        if (!"dream".equals(xmlResourceParser.getName())) {
            Log.w("DreamBackend", "Meta-data does not start with dream tag");
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
            return null;
        }
        TypedArray obtainAttributes = resourcesForApplication.obtainAttributes(asAttributeSet, R.styleable.Dream);
        str = obtainAttributes.getString(0);
        try {
            obtainAttributes.recycle();
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
            e = null;
        } catch (PackageManager.NameNotFoundException | IOException | XmlPullParserException e3) {
            e = e3;
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
            if (e == null) {
            }
        }
        if (e == null) {
            Log.w("DreamBackend", "Error parsing : " + resolveInfo.serviceInfo.packageName, e);
            return null;
        }
        if (str != null && str.indexOf(47) < 0) {
            str = resolveInfo.serviceInfo.packageName + "/" + str;
        }
        if (str == null) {
            return null;
        }
        return ComponentName.unflattenFromString(str);
    }

    private static void logd(String str, Object... objArr) {
    }

    /* loaded from: classes.dex */
    private static class DreamInfoComparator implements Comparator<DreamInfo> {
        private final ComponentName mDefaultDream;

        public DreamInfoComparator(ComponentName componentName) {
            this.mDefaultDream = componentName;
        }

        @Override // java.util.Comparator
        public int compare(DreamInfo dreamInfo, DreamInfo dreamInfo2) {
            return sortKey(dreamInfo).compareTo(sortKey(dreamInfo2));
        }

        private String sortKey(DreamInfo dreamInfo) {
            StringBuilder sb = new StringBuilder();
            sb.append(dreamInfo.componentName.equals(this.mDefaultDream) ? '0' : '1');
            sb.append(dreamInfo.caption);
            return sb.toString();
        }
    }
}

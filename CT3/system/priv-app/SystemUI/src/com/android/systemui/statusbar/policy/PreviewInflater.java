package com.android.systemui.statusbar.policy;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.statusbar.phone.KeyguardPreviewContainer;
import java.util.List;
/* loaded from: a.zip:com/android/systemui/statusbar/policy/PreviewInflater.class */
public class PreviewInflater {
    private Context mContext;
    private LockPatternUtils mLockPatternUtils;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/statusbar/policy/PreviewInflater$WidgetInfo.class */
    public static class WidgetInfo {
        String contextPackage;
        int layoutId;

        private WidgetInfo() {
        }

        /* synthetic */ WidgetInfo(WidgetInfo widgetInfo) {
            this();
        }
    }

    public PreviewInflater(Context context, LockPatternUtils lockPatternUtils) {
        this.mContext = context;
        this.mLockPatternUtils = lockPatternUtils;
    }

    public static ActivityInfo getTargetActivityInfo(Context context, Intent intent, int i) {
        ResolveInfo resolveActivityAsUser;
        PackageManager packageManager = context.getPackageManager();
        List queryIntentActivitiesAsUser = packageManager.queryIntentActivitiesAsUser(intent, 65536, i);
        if (queryIntentActivitiesAsUser.size() == 0 || (resolveActivityAsUser = packageManager.resolveActivityAsUser(intent, 65664, i)) == null || wouldLaunchResolverActivity(resolveActivityAsUser, queryIntentActivitiesAsUser)) {
            return null;
        }
        return resolveActivityAsUser.activityInfo;
    }

    private WidgetInfo getWidgetInfo(Intent intent) {
        PackageManager packageManager = this.mContext.getPackageManager();
        List queryIntentActivitiesAsUser = packageManager.queryIntentActivitiesAsUser(intent, 65536, KeyguardUpdateMonitor.getCurrentUser());
        if (queryIntentActivitiesAsUser.size() == 0) {
            return null;
        }
        ResolveInfo resolveActivityAsUser = packageManager.resolveActivityAsUser(intent, 65664, KeyguardUpdateMonitor.getCurrentUser());
        if (wouldLaunchResolverActivity(resolveActivityAsUser, queryIntentActivitiesAsUser) || resolveActivityAsUser == null || resolveActivityAsUser.activityInfo == null) {
            return null;
        }
        return getWidgetInfoFromMetaData(resolveActivityAsUser.activityInfo.packageName, resolveActivityAsUser.activityInfo.metaData);
    }

    private WidgetInfo getWidgetInfoFromMetaData(String str, Bundle bundle) {
        int i;
        if (bundle == null || (i = bundle.getInt("com.android.keyguard.layout")) == 0) {
            return null;
        }
        WidgetInfo widgetInfo = new WidgetInfo(null);
        widgetInfo.contextPackage = str;
        widgetInfo.layoutId = i;
        return widgetInfo;
    }

    private WidgetInfo getWidgetInfoFromService(ComponentName componentName) {
        try {
            return getWidgetInfoFromMetaData(componentName.getPackageName(), this.mContext.getPackageManager().getServiceInfo(componentName, 128).metaData);
        } catch (PackageManager.NameNotFoundException e) {
            Log.w("PreviewInflater", "Failed to load preview; " + componentName.flattenToShortString() + " not found", e);
            return null;
        }
    }

    private KeyguardPreviewContainer inflatePreview(WidgetInfo widgetInfo) {
        View inflateWidgetView;
        if (widgetInfo == null || (inflateWidgetView = inflateWidgetView(widgetInfo)) == null) {
            return null;
        }
        KeyguardPreviewContainer keyguardPreviewContainer = new KeyguardPreviewContainer(this.mContext, null);
        keyguardPreviewContainer.addView(inflateWidgetView);
        return keyguardPreviewContainer;
    }

    private View inflateWidgetView(WidgetInfo widgetInfo) {
        View view;
        try {
            Context createPackageContext = this.mContext.createPackageContext(widgetInfo.contextPackage, 4);
            view = ((LayoutInflater) createPackageContext.getSystemService("layout_inflater")).cloneInContext(createPackageContext).inflate(widgetInfo.layoutId, (ViewGroup) null, false);
        } catch (PackageManager.NameNotFoundException | RuntimeException e) {
            Log.w("PreviewInflater", "Error creating widget view", e);
            view = null;
        }
        return view;
    }

    public static boolean wouldLaunchResolverActivity(Context context, Intent intent, int i) {
        return getTargetActivityInfo(context, intent, i) == null;
    }

    private static boolean wouldLaunchResolverActivity(ResolveInfo resolveInfo, List<ResolveInfo> list) {
        for (int i = 0; i < list.size(); i++) {
            ResolveInfo resolveInfo2 = list.get(i);
            if (resolveInfo2.activityInfo.name.equals(resolveInfo.activityInfo.name) && resolveInfo2.activityInfo.packageName.equals(resolveInfo.activityInfo.packageName)) {
                return false;
            }
        }
        return true;
    }

    public View inflatePreview(Intent intent) {
        return inflatePreview(getWidgetInfo(intent));
    }

    public View inflatePreviewFromService(ComponentName componentName) {
        return inflatePreview(getWidgetInfoFromService(componentName));
    }
}

package com.android.launcher3.compat;

import android.annotation.TargetApi;
import android.app.Activity;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Process;
import android.os.UserHandle;
import android.os.UserManager;
import android.widget.Toast;
import com.android.launcher3.IconCache;
import com.android.launcher3.LauncherAppWidgetProviderInfo;
import com.android.launcher3.util.ComponentKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
/* JADX INFO: Access modifiers changed from: package-private */
@TargetApi(21)
/* loaded from: a.zip:com/android/launcher3/compat/AppWidgetManagerCompatVL.class */
public class AppWidgetManagerCompatVL extends AppWidgetManagerCompat {
    private final PackageManager mPm;
    private final UserManager mUserManager;

    /* JADX INFO: Access modifiers changed from: package-private */
    public AppWidgetManagerCompatVL(Context context) {
        super(context);
        this.mPm = context.getPackageManager();
        this.mUserManager = (UserManager) context.getSystemService("user");
    }

    @Override // com.android.launcher3.compat.AppWidgetManagerCompat
    public boolean bindAppWidgetIdIfAllowed(int i, AppWidgetProviderInfo appWidgetProviderInfo, Bundle bundle) {
        return this.mAppWidgetManager.bindAppWidgetIdIfAllowed(i, appWidgetProviderInfo.getProfile(), appWidgetProviderInfo.provider, bundle);
    }

    @Override // com.android.launcher3.compat.AppWidgetManagerCompat
    public LauncherAppWidgetProviderInfo findProvider(ComponentName componentName, UserHandleCompat userHandleCompat) {
        for (AppWidgetProviderInfo appWidgetProviderInfo : this.mAppWidgetManager.getInstalledProvidersForProfile(userHandleCompat.getUser())) {
            if (appWidgetProviderInfo.provider.equals(componentName)) {
                return LauncherAppWidgetProviderInfo.fromProviderInfo(this.mContext, appWidgetProviderInfo);
            }
        }
        return null;
    }

    @Override // com.android.launcher3.compat.AppWidgetManagerCompat
    public List<AppWidgetProviderInfo> getAllProviders() {
        ArrayList arrayList = new ArrayList();
        for (UserHandle userHandle : this.mUserManager.getUserProfiles()) {
            arrayList.addAll(this.mAppWidgetManager.getInstalledProvidersForProfile(userHandle));
        }
        return arrayList;
    }

    @Override // com.android.launcher3.compat.AppWidgetManagerCompat
    public HashMap<ComponentKey, AppWidgetProviderInfo> getAllProvidersMap() {
        HashMap<ComponentKey, AppWidgetProviderInfo> hashMap = new HashMap<>();
        for (UserHandle userHandle : this.mUserManager.getUserProfiles()) {
            UserHandleCompat fromUser = UserHandleCompat.fromUser(userHandle);
            for (AppWidgetProviderInfo appWidgetProviderInfo : this.mAppWidgetManager.getInstalledProvidersForProfile(userHandle)) {
                hashMap.put(new ComponentKey(appWidgetProviderInfo.provider, fromUser), appWidgetProviderInfo);
            }
        }
        return hashMap;
    }

    @Override // com.android.launcher3.compat.AppWidgetManagerCompat
    public Bitmap getBadgeBitmap(LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo, Bitmap bitmap, int i, int i2) {
        if (launcherAppWidgetProviderInfo.isCustomWidget || launcherAppWidgetProviderInfo.getProfile().equals(Process.myUserHandle())) {
            return bitmap;
        }
        Resources resources = this.mContext.getResources();
        int dimensionPixelSize = resources.getDimensionPixelSize(2131230805);
        int min = Math.min(resources.getDimensionPixelSize(2131230803), Math.min(i, i2 - dimensionPixelSize));
        Rect rect = new Rect(0, 0, min, min);
        int max = Math.max(i2 - min, dimensionPixelSize);
        if (resources.getConfiguration().getLayoutDirection() == 1) {
            rect.offset(0, max);
        } else {
            rect.offset(bitmap.getWidth() - min, max);
        }
        Drawable userBadgedDrawableForDensity = this.mPm.getUserBadgedDrawableForDensity(new BitmapDrawable(resources, bitmap), launcherAppWidgetProviderInfo.getProfile(), rect, 0);
        if (userBadgedDrawableForDensity instanceof BitmapDrawable) {
            return ((BitmapDrawable) userBadgedDrawableForDensity).getBitmap();
        }
        bitmap.eraseColor(0);
        Canvas canvas = new Canvas(bitmap);
        userBadgedDrawableForDensity.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
        userBadgedDrawableForDensity.draw(canvas);
        canvas.setBitmap(null);
        return bitmap;
    }

    @Override // com.android.launcher3.compat.AppWidgetManagerCompat
    public UserHandleCompat getUser(LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo) {
        return launcherAppWidgetProviderInfo.isCustomWidget ? UserHandleCompat.myUserHandle() : UserHandleCompat.fromUser(launcherAppWidgetProviderInfo.getProfile());
    }

    @Override // com.android.launcher3.compat.AppWidgetManagerCompat
    public Drawable loadIcon(LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo, IconCache iconCache) {
        return launcherAppWidgetProviderInfo.getIcon(this.mContext, iconCache);
    }

    @Override // com.android.launcher3.compat.AppWidgetManagerCompat
    public String loadLabel(LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo) {
        return launcherAppWidgetProviderInfo.getLabel(this.mPm);
    }

    @Override // com.android.launcher3.compat.AppWidgetManagerCompat
    public Drawable loadPreview(AppWidgetProviderInfo appWidgetProviderInfo) {
        return appWidgetProviderInfo.loadPreviewImage(this.mContext, 0);
    }

    @Override // com.android.launcher3.compat.AppWidgetManagerCompat
    public void startConfigActivity(AppWidgetProviderInfo appWidgetProviderInfo, int i, Activity activity, AppWidgetHost appWidgetHost, int i2) {
        try {
            appWidgetHost.startAppWidgetConfigureActivityForResult(activity, i, 0, i2, null);
        } catch (ActivityNotFoundException | SecurityException e) {
            Toast.makeText(activity, 2131558407, 0).show();
        }
    }
}

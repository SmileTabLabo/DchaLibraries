package com.android.launcher3;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import com.android.launcher3.compat.LauncherActivityInfoCompat;
import com.android.launcher3.compat.UserManagerCompat;
import java.util.Arrays;
/* loaded from: a.zip:com/android/launcher3/ShortcutInfo.class */
public class ShortcutInfo extends ItemInfo {
    public boolean customIcon;
    int flags;
    public Intent.ShortcutIconResource iconResource;
    Intent intent;
    int isDisabled;
    private Bitmap mIcon;
    private int mInstallProgress;
    Intent promisedIntent;
    int status;
    boolean usingFallbackIcon;
    boolean usingLowResIcon;

    /* JADX INFO: Access modifiers changed from: package-private */
    public ShortcutInfo() {
        this.isDisabled = 0;
        this.flags = 0;
        this.itemType = 1;
    }

    public ShortcutInfo(AppInfo appInfo) {
        super(appInfo);
        this.isDisabled = 0;
        this.flags = 0;
        this.title = Utilities.trim(appInfo.title);
        this.intent = new Intent(appInfo.intent);
        this.customIcon = false;
        this.flags = appInfo.flags;
        this.isDisabled = appInfo.isDisabled;
    }

    public static ShortcutInfo fromActivityInfo(LauncherActivityInfoCompat launcherActivityInfoCompat, Context context) {
        ShortcutInfo shortcutInfo = new ShortcutInfo();
        shortcutInfo.user = launcherActivityInfoCompat.getUser();
        shortcutInfo.title = Utilities.trim(launcherActivityInfoCompat.getLabel());
        shortcutInfo.contentDescription = UserManagerCompat.getInstance(context).getBadgedLabelForUser(launcherActivityInfoCompat.getLabel(), launcherActivityInfoCompat.getUser());
        shortcutInfo.customIcon = false;
        shortcutInfo.intent = AppInfo.makeLaunchIntent(context, launcherActivityInfoCompat, launcherActivityInfoCompat.getUser());
        shortcutInfo.itemType = 0;
        shortcutInfo.flags = AppInfo.initFlags(launcherActivityInfoCompat);
        return shortcutInfo;
    }

    public Bitmap getIcon(IconCache iconCache) {
        if (this.mIcon == null) {
            updateIcon(iconCache);
        }
        return this.mIcon;
    }

    public int getInstallProgress() {
        return this.mInstallProgress;
    }

    @Override // com.android.launcher3.ItemInfo
    public Intent getIntent() {
        return this.intent;
    }

    public ComponentName getTargetComponent() {
        return this.promisedIntent != null ? this.promisedIntent.getComponent() : this.intent.getComponent();
    }

    public boolean hasStatusFlag(int i) {
        boolean z = false;
        if ((this.status & i) != 0) {
            z = true;
        }
        return z;
    }

    @Override // com.android.launcher3.ItemInfo
    public boolean isDisabled() {
        boolean z = false;
        if (this.isDisabled != 0) {
            z = true;
        }
        return z;
    }

    public final boolean isPromise() {
        return hasStatusFlag(3);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.android.launcher3.ItemInfo
    public void onAddToDatabase(Context context, ContentValues contentValues) {
        super.onAddToDatabase(context, contentValues);
        contentValues.put("title", this.title != null ? this.title.toString() : null);
        contentValues.put("intent", this.promisedIntent != null ? this.promisedIntent.toUri(0) : this.intent != null ? this.intent.toUri(0) : null);
        contentValues.put("restored", Integer.valueOf(this.status));
        if (this.customIcon) {
            contentValues.put("iconType", (Integer) 1);
            writeBitmap(contentValues, this.mIcon);
            return;
        }
        if (!this.usingFallbackIcon) {
            writeBitmap(contentValues, this.mIcon);
        }
        if (this.iconResource != null) {
            contentValues.put("iconType", (Integer) 0);
            contentValues.put("iconPackage", this.iconResource.packageName);
            contentValues.put("iconResource", this.iconResource.resourceName);
        }
    }

    public void setIcon(Bitmap bitmap) {
        this.mIcon = bitmap;
    }

    public void setInstallProgress(int i) {
        this.mInstallProgress = i;
        this.status |= 4;
    }

    public boolean shouldUseLowResIcon() {
        boolean z = false;
        if (this.usingLowResIcon) {
            z = false;
            if (this.container >= 0) {
                z = false;
                if (this.rank >= 3) {
                    z = true;
                }
            }
        }
        return z;
    }

    @Override // com.android.launcher3.ItemInfo
    public String toString() {
        return "ShortcutInfo(title=" + this.title + "intent=" + this.intent + "id=" + this.id + " type=" + this.itemType + " container=" + this.container + " screen=" + this.screenId + " cellX=" + this.cellX + " cellY=" + this.cellY + " spanX=" + this.spanX + " spanY=" + this.spanY + " dropPos=" + Arrays.toString(this.dropPos) + " user=" + this.user + ")";
    }

    public void updateIcon(IconCache iconCache) {
        updateIcon(iconCache, shouldUseLowResIcon());
    }

    public void updateIcon(IconCache iconCache, boolean z) {
        if (this.itemType == 0) {
            iconCache.getTitleAndIcon(this, this.promisedIntent != null ? this.promisedIntent : this.intent, this.user, z);
        }
    }
}

package com.android.launcher3.util;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.text.TextUtils;
import com.android.launcher3.ShortcutInfo;
import com.android.launcher3.Utilities;
import com.android.launcher3.compat.PackageInstallerCompat;
/* loaded from: a.zip:com/android/launcher3/util/CursorIconInfo.class */
public class CursorIconInfo {
    public final int iconIndex;
    public final int iconPackageIndex;
    public final int iconResourceIndex;
    public final int iconTypeIndex;

    public CursorIconInfo(Cursor cursor) {
        this.iconTypeIndex = cursor.getColumnIndexOrThrow("iconType");
        this.iconIndex = cursor.getColumnIndexOrThrow("icon");
        this.iconPackageIndex = cursor.getColumnIndexOrThrow("iconPackage");
        this.iconResourceIndex = cursor.getColumnIndexOrThrow("iconResource");
    }

    public Bitmap loadIcon(Cursor cursor, ShortcutInfo shortcutInfo, Context context) {
        Bitmap bitmap;
        Bitmap bitmap2 = null;
        switch (cursor.getInt(this.iconTypeIndex)) {
            case PackageInstallerCompat.STATUS_INSTALLED /* 0 */:
                String string = cursor.getString(this.iconPackageIndex);
                String string2 = cursor.getString(this.iconResourceIndex);
                if (!TextUtils.isEmpty(string) || !TextUtils.isEmpty(string2)) {
                    shortcutInfo.iconResource = new Intent.ShortcutIconResource();
                    shortcutInfo.iconResource.packageName = string;
                    shortcutInfo.iconResource.resourceName = string2;
                    bitmap2 = Utilities.createIconBitmap(string, string2, context);
                }
                bitmap = bitmap2;
                if (bitmap2 == null) {
                    bitmap = Utilities.createIconBitmap(cursor, this.iconIndex, context);
                    break;
                }
                break;
            case 1:
                Bitmap createIconBitmap = Utilities.createIconBitmap(cursor, this.iconIndex, context);
                shortcutInfo.customIcon = createIconBitmap != null;
                bitmap = createIconBitmap;
                break;
            default:
                bitmap = null;
                break;
        }
        return bitmap;
    }
}

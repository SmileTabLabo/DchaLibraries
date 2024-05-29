package com.android.launcher3.model;

import android.graphics.Bitmap;
import com.android.launcher3.ItemInfo;
import java.util.Arrays;
/* loaded from: a.zip:com/android/launcher3/model/PackageItemInfo.class */
public class PackageItemInfo extends ItemInfo {
    int flags = 0;
    public Bitmap iconBitmap;
    public String packageName;
    public String titleSectionName;
    public boolean usingLowResIcon;

    /* JADX INFO: Access modifiers changed from: package-private */
    public PackageItemInfo(String str) {
        this.packageName = str;
    }

    @Override // com.android.launcher3.ItemInfo
    public String toString() {
        return "PackageItemInfo(title=" + this.title + " id=" + this.id + " type=" + this.itemType + " container=" + this.container + " screen=" + this.screenId + " cellX=" + this.cellX + " cellY=" + this.cellY + " spanX=" + this.spanX + " spanY=" + this.spanY + " dropPos=" + Arrays.toString(this.dropPos) + " user=" + this.user + ")";
    }
}

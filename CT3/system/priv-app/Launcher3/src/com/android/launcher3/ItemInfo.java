package com.android.launcher3;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import com.android.launcher3.compat.UserHandleCompat;
import com.android.launcher3.compat.UserManagerCompat;
import java.util.Arrays;
/* loaded from: a.zip:com/android/launcher3/ItemInfo.class */
public class ItemInfo {
    public int cellX;
    public int cellY;
    public long container;
    public CharSequence contentDescription;
    public int[] dropPos;
    public long id;
    public int itemType;
    public int minSpanX;
    public int minSpanY;
    public int rank;
    public boolean requiresDbUpdate;
    public long screenId;
    public int spanX;
    public int spanY;
    public CharSequence title;
    public UserHandleCompat user;

    public ItemInfo() {
        this.id = -1L;
        this.container = -1L;
        this.screenId = -1L;
        this.cellX = -1;
        this.cellY = -1;
        this.spanX = 1;
        this.spanY = 1;
        this.minSpanX = 1;
        this.minSpanY = 1;
        this.rank = 0;
        this.requiresDbUpdate = false;
        this.dropPos = null;
        this.user = UserHandleCompat.myUserHandle();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public ItemInfo(ItemInfo itemInfo) {
        this.id = -1L;
        this.container = -1L;
        this.screenId = -1L;
        this.cellX = -1;
        this.cellY = -1;
        this.spanX = 1;
        this.spanY = 1;
        this.minSpanX = 1;
        this.minSpanY = 1;
        this.rank = 0;
        this.requiresDbUpdate = false;
        this.dropPos = null;
        copyFrom(itemInfo);
        LauncherModel.checkItemInfo(this);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void writeBitmap(ContentValues contentValues, Bitmap bitmap) {
        if (bitmap != null) {
            contentValues.put("icon", Utilities.flattenBitmap(bitmap));
        }
    }

    public void copyFrom(ItemInfo itemInfo) {
        this.id = itemInfo.id;
        this.cellX = itemInfo.cellX;
        this.cellY = itemInfo.cellY;
        this.spanX = itemInfo.spanX;
        this.spanY = itemInfo.spanY;
        this.rank = itemInfo.rank;
        this.screenId = itemInfo.screenId;
        this.itemType = itemInfo.itemType;
        this.container = itemInfo.container;
        this.user = itemInfo.user;
        this.contentDescription = itemInfo.contentDescription;
    }

    public Intent getIntent() {
        throw new RuntimeException("Unexpected Intent");
    }

    public boolean isDisabled() {
        return false;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void onAddToDatabase(Context context, ContentValues contentValues) {
        contentValues.put("itemType", Integer.valueOf(this.itemType));
        contentValues.put("container", Long.valueOf(this.container));
        contentValues.put("screen", Long.valueOf(this.screenId));
        contentValues.put("cellX", Integer.valueOf(this.cellX));
        contentValues.put("cellY", Integer.valueOf(this.cellY));
        contentValues.put("spanX", Integer.valueOf(this.spanX));
        contentValues.put("spanY", Integer.valueOf(this.spanY));
        contentValues.put("rank", Integer.valueOf(this.rank));
        contentValues.put("profileId", Long.valueOf(UserManagerCompat.getInstance(context).getSerialNumberForUser(this.user)));
        if (this.screenId == -201) {
            throw new RuntimeException("Screen id should not be EXTRA_EMPTY_SCREEN_ID");
        }
    }

    public String toString() {
        return "Item(id=" + this.id + " type=" + this.itemType + " container=" + this.container + " screen=" + this.screenId + " cellX=" + this.cellX + " cellY=" + this.cellY + " spanX=" + this.spanX + " spanY=" + this.spanY + " dropPos=" + Arrays.toString(this.dropPos) + " user=" + this.user + ")";
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void unbind() {
    }
}

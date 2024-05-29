package com.android.launcher3;

import android.content.ContentValues;
import android.content.Context;
import com.android.launcher3.compat.UserHandleCompat;
import java.util.ArrayList;
import java.util.Arrays;
/* loaded from: a.zip:com/android/launcher3/FolderInfo.class */
public class FolderInfo extends ItemInfo {
    public ArrayList<ShortcutInfo> contents = new ArrayList<>();
    ArrayList<FolderListener> listeners = new ArrayList<>();
    boolean opened;
    public int options;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:com/android/launcher3/FolderInfo$FolderListener.class */
    public interface FolderListener {
        void onAdd(ShortcutInfo shortcutInfo);

        void onItemsChanged();

        void onRemove(ShortcutInfo shortcutInfo);

        void onTitleChanged(CharSequence charSequence);
    }

    public FolderInfo() {
        this.itemType = 2;
        this.user = UserHandleCompat.myUserHandle();
    }

    public void add(ShortcutInfo shortcutInfo) {
        this.contents.add(shortcutInfo);
        for (int i = 0; i < this.listeners.size(); i++) {
            this.listeners.get(i).onAdd(shortcutInfo);
        }
        itemsChanged();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void addListener(FolderListener folderListener) {
        this.listeners.add(folderListener);
    }

    public boolean hasOption(int i) {
        boolean z = false;
        if ((this.options & i) != 0) {
            z = true;
        }
        return z;
    }

    void itemsChanged() {
        for (int i = 0; i < this.listeners.size(); i++) {
            this.listeners.get(i).onItemsChanged();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.android.launcher3.ItemInfo
    public void onAddToDatabase(Context context, ContentValues contentValues) {
        super.onAddToDatabase(context, contentValues);
        contentValues.put("title", this.title.toString());
        contentValues.put("options", Integer.valueOf(this.options));
    }

    public void remove(ShortcutInfo shortcutInfo) {
        this.contents.remove(shortcutInfo);
        for (int i = 0; i < this.listeners.size(); i++) {
            this.listeners.get(i).onRemove(shortcutInfo);
        }
        itemsChanged();
    }

    public void setOption(int i, boolean z, Context context) {
        int i2 = this.options;
        if (z) {
            this.options |= i;
        } else {
            this.options &= i ^ (-1);
        }
        if (context == null || i2 == this.options) {
            return;
        }
        LauncherModel.updateItemInDatabase(context, this);
    }

    public void setTitle(CharSequence charSequence) {
        this.title = charSequence;
        for (int i = 0; i < this.listeners.size(); i++) {
            this.listeners.get(i).onTitleChanged(charSequence);
        }
    }

    @Override // com.android.launcher3.ItemInfo
    public String toString() {
        return "FolderInfo(id=" + this.id + " type=" + this.itemType + " container=" + this.container + " screen=" + this.screenId + " cellX=" + this.cellX + " cellY=" + this.cellY + " spanX=" + this.spanX + " spanY=" + this.spanY + " dropPos=" + Arrays.toString(this.dropPos) + ")";
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.android.launcher3.ItemInfo
    public void unbind() {
        super.unbind();
        this.listeners.clear();
    }
}

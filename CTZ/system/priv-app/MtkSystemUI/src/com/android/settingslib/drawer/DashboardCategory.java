package com.android.settingslib.drawer;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
/* loaded from: classes.dex */
public class DashboardCategory implements Parcelable {
    public String key;
    private List<Tile> mTiles = new ArrayList();
    public int priority;
    public CharSequence title;
    private static final boolean DEBUG = Log.isLoggable("DashboardCategory", 3);
    public static final Parcelable.Creator<DashboardCategory> CREATOR = new Parcelable.Creator<DashboardCategory>() { // from class: com.android.settingslib.drawer.DashboardCategory.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public DashboardCategory createFromParcel(Parcel parcel) {
            return new DashboardCategory(parcel);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public DashboardCategory[] newArray(int i) {
            return new DashboardCategory[i];
        }
    };
    public static final Comparator<Tile> TILE_COMPARATOR = new Comparator<Tile>() { // from class: com.android.settingslib.drawer.DashboardCategory.2
        @Override // java.util.Comparator
        public int compare(Tile tile, Tile tile2) {
            return tile2.priority - tile.priority;
        }
    };

    public DashboardCategory() {
    }

    public synchronized void addTile(Tile tile) {
        this.mTiles.add(tile);
    }

    public synchronized void removeTile(int i) {
        this.mTiles.remove(i);
    }

    public int getTilesCount() {
        return this.mTiles.size();
    }

    public Tile getTile(int i) {
        return this.mTiles.get(i);
    }

    public void sortTiles() {
        Collections.sort(this.mTiles, TILE_COMPARATOR);
    }

    public synchronized void sortTiles(final String str) {
        Collections.sort(this.mTiles, new Comparator() { // from class: com.android.settingslib.drawer.-$$Lambda$DashboardCategory$hMIMtvkEGTs2t-7RyY7SqwVmOgI
            @Override // java.util.Comparator
            public final int compare(Object obj, Object obj2) {
                return DashboardCategory.lambda$sortTiles$0(str, (Tile) obj, (Tile) obj2);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ int lambda$sortTiles$0(String str, Tile tile, Tile tile2) {
        String packageName = tile.intent.getComponent().getPackageName();
        String packageName2 = tile2.intent.getComponent().getPackageName();
        int compare = String.CASE_INSENSITIVE_ORDER.compare(packageName, packageName2);
        int i = tile2.priority - tile.priority;
        if (i != 0) {
            return i;
        }
        if (compare != 0) {
            if (TextUtils.equals(packageName, str)) {
                return -1;
            }
            if (TextUtils.equals(packageName2, str)) {
                return 1;
            }
        }
        return compare;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        TextUtils.writeToParcel(this.title, parcel, i);
        parcel.writeString(this.key);
        parcel.writeInt(this.priority);
        int size = this.mTiles.size();
        parcel.writeInt(size);
        for (int i2 = 0; i2 < size; i2++) {
            this.mTiles.get(i2).writeToParcel(parcel, i);
        }
    }

    public void readFromParcel(Parcel parcel) {
        this.title = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel);
        this.key = parcel.readString();
        this.priority = parcel.readInt();
        int readInt = parcel.readInt();
        for (int i = 0; i < readInt; i++) {
            this.mTiles.add(Tile.CREATOR.createFromParcel(parcel));
        }
    }

    DashboardCategory(Parcel parcel) {
        readFromParcel(parcel);
    }
}

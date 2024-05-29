package com.android.settingslib.drawer;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.List;
/* loaded from: a.zip:com/android/settingslib/drawer/DashboardCategory.class */
public class DashboardCategory implements Parcelable {
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
    public String key;
    public int priority;
    public List<Tile> tiles = new ArrayList();
    public CharSequence title;

    public DashboardCategory() {
    }

    DashboardCategory(Parcel parcel) {
        readFromParcel(parcel);
    }

    public void addTile(Tile tile) {
        this.tiles.add(tile);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public void readFromParcel(Parcel parcel) {
        this.title = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel);
        this.key = parcel.readString();
        this.priority = parcel.readInt();
        int readInt = parcel.readInt();
        for (int i = 0; i < readInt; i++) {
            this.tiles.add(Tile.CREATOR.createFromParcel(parcel));
        }
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        TextUtils.writeToParcel(this.title, parcel, i);
        parcel.writeString(this.key);
        parcel.writeInt(this.priority);
        int size = this.tiles.size();
        parcel.writeInt(size);
        for (int i2 = 0; i2 < size; i2++) {
            this.tiles.get(i2).writeToParcel(parcel, i);
        }
    }
}

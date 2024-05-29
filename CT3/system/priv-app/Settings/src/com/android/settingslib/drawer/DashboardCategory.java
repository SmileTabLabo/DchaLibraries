package com.android.settingslib.drawer;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.List;
/* loaded from: classes.dex */
public class DashboardCategory implements Parcelable {
    public static final Parcelable.Creator<DashboardCategory> CREATOR = new Parcelable.Creator<DashboardCategory>() { // from class: com.android.settingslib.drawer.DashboardCategory.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public DashboardCategory createFromParcel(Parcel source) {
            return new DashboardCategory(source);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public DashboardCategory[] newArray(int size) {
            return new DashboardCategory[size];
        }
    };
    public String key;
    public int priority;
    public List<Tile> tiles = new ArrayList();
    public CharSequence title;

    public DashboardCategory() {
    }

    public void addTile(Tile tile) {
        this.tiles.add(tile);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        TextUtils.writeToParcel(this.title, dest, flags);
        dest.writeString(this.key);
        dest.writeInt(this.priority);
        int count = this.tiles.size();
        dest.writeInt(count);
        for (int n = 0; n < count; n++) {
            Tile tile = this.tiles.get(n);
            tile.writeToParcel(dest, flags);
        }
    }

    public void readFromParcel(Parcel in) {
        this.title = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
        this.key = in.readString();
        this.priority = in.readInt();
        int count = in.readInt();
        for (int n = 0; n < count; n++) {
            Tile tile = Tile.CREATOR.createFromParcel(in);
            this.tiles.add(tile);
        }
    }

    DashboardCategory(Parcel in) {
        readFromParcel(in);
    }
}

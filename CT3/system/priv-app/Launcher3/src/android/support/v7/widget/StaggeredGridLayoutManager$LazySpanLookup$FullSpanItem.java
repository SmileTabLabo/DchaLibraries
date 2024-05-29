package android.support.v7.widget;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Arrays;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: a.zip:android/support/v7/widget/StaggeredGridLayoutManager$LazySpanLookup$FullSpanItem.class */
public class StaggeredGridLayoutManager$LazySpanLookup$FullSpanItem implements Parcelable {
    public static final Parcelable.Creator<StaggeredGridLayoutManager$LazySpanLookup$FullSpanItem> CREATOR = new Parcelable.Creator<StaggeredGridLayoutManager$LazySpanLookup$FullSpanItem>() { // from class: android.support.v7.widget.StaggeredGridLayoutManager$LazySpanLookup$FullSpanItem.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public StaggeredGridLayoutManager$LazySpanLookup$FullSpanItem createFromParcel(Parcel parcel) {
            return new StaggeredGridLayoutManager$LazySpanLookup$FullSpanItem(parcel);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public StaggeredGridLayoutManager$LazySpanLookup$FullSpanItem[] newArray(int i) {
            return new StaggeredGridLayoutManager$LazySpanLookup$FullSpanItem[i];
        }
    };
    int mGapDir;
    int[] mGapPerSpan;
    boolean mHasUnwantedGapAfter;
    int mPosition;

    public StaggeredGridLayoutManager$LazySpanLookup$FullSpanItem() {
    }

    public StaggeredGridLayoutManager$LazySpanLookup$FullSpanItem(Parcel parcel) {
        boolean z = true;
        this.mPosition = parcel.readInt();
        this.mGapDir = parcel.readInt();
        this.mHasUnwantedGapAfter = parcel.readInt() != 1 ? false : z;
        int readInt = parcel.readInt();
        if (readInt > 0) {
            this.mGapPerSpan = new int[readInt];
            parcel.readIntArray(this.mGapPerSpan);
        }
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public String toString() {
        return "FullSpanItem{mPosition=" + this.mPosition + ", mGapDir=" + this.mGapDir + ", mHasUnwantedGapAfter=" + this.mHasUnwantedGapAfter + ", mGapPerSpan=" + Arrays.toString(this.mGapPerSpan) + '}';
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.mPosition);
        parcel.writeInt(this.mGapDir);
        parcel.writeInt(this.mHasUnwantedGapAfter ? 1 : 0);
        if (this.mGapPerSpan == null || this.mGapPerSpan.length <= 0) {
            parcel.writeInt(0);
            return;
        }
        parcel.writeInt(this.mGapPerSpan.length);
        parcel.writeIntArray(this.mGapPerSpan);
    }
}

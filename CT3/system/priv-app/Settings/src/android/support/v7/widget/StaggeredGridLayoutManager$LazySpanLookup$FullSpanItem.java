package android.support.v7.widget;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Arrays;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class StaggeredGridLayoutManager$LazySpanLookup$FullSpanItem implements Parcelable {
    public static final Parcelable.Creator<StaggeredGridLayoutManager$LazySpanLookup$FullSpanItem> CREATOR = new Parcelable.Creator<StaggeredGridLayoutManager$LazySpanLookup$FullSpanItem>() { // from class: android.support.v7.widget.StaggeredGridLayoutManager$LazySpanLookup$FullSpanItem.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public StaggeredGridLayoutManager$LazySpanLookup$FullSpanItem createFromParcel(Parcel in) {
            return new StaggeredGridLayoutManager$LazySpanLookup$FullSpanItem(in);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public StaggeredGridLayoutManager$LazySpanLookup$FullSpanItem[] newArray(int size) {
            return new StaggeredGridLayoutManager$LazySpanLookup$FullSpanItem[size];
        }
    };
    int mGapDir;
    int[] mGapPerSpan;
    boolean mHasUnwantedGapAfter;
    int mPosition;

    public StaggeredGridLayoutManager$LazySpanLookup$FullSpanItem(Parcel in) {
        this.mPosition = in.readInt();
        this.mGapDir = in.readInt();
        this.mHasUnwantedGapAfter = in.readInt() == 1;
        int spanCount = in.readInt();
        if (spanCount <= 0) {
            return;
        }
        this.mGapPerSpan = new int[spanCount];
        in.readIntArray(this.mGapPerSpan);
    }

    public StaggeredGridLayoutManager$LazySpanLookup$FullSpanItem() {
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mPosition);
        dest.writeInt(this.mGapDir);
        dest.writeInt(this.mHasUnwantedGapAfter ? 1 : 0);
        if (this.mGapPerSpan != null && this.mGapPerSpan.length > 0) {
            dest.writeInt(this.mGapPerSpan.length);
            dest.writeIntArray(this.mGapPerSpan);
            return;
        }
        dest.writeInt(0);
    }

    public String toString() {
        return "FullSpanItem{mPosition=" + this.mPosition + ", mGapDir=" + this.mGapDir + ", mHasUnwantedGapAfter=" + this.mHasUnwantedGapAfter + ", mGapPerSpan=" + Arrays.toString(this.mGapPerSpan) + '}';
    }
}

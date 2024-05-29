package android.support.v7.widget;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.List;
/* loaded from: a.zip:android/support/v7/widget/StaggeredGridLayoutManager$SavedState.class */
public class StaggeredGridLayoutManager$SavedState implements Parcelable {
    public static final Parcelable.Creator<StaggeredGridLayoutManager$SavedState> CREATOR = new Parcelable.Creator<StaggeredGridLayoutManager$SavedState>() { // from class: android.support.v7.widget.StaggeredGridLayoutManager$SavedState.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public StaggeredGridLayoutManager$SavedState createFromParcel(Parcel parcel) {
            return new StaggeredGridLayoutManager$SavedState(parcel);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public StaggeredGridLayoutManager$SavedState[] newArray(int i) {
            return new StaggeredGridLayoutManager$SavedState[i];
        }
    };
    boolean mAnchorLayoutFromEnd;
    int mAnchorPosition;
    List<StaggeredGridLayoutManager$LazySpanLookup$FullSpanItem> mFullSpanItems;
    boolean mLastLayoutRTL;
    boolean mReverseLayout;
    int[] mSpanLookup;
    int mSpanLookupSize;
    int[] mSpanOffsets;
    int mSpanOffsetsSize;
    int mVisibleAnchorPosition;

    public StaggeredGridLayoutManager$SavedState() {
    }

    StaggeredGridLayoutManager$SavedState(Parcel parcel) {
        this.mAnchorPosition = parcel.readInt();
        this.mVisibleAnchorPosition = parcel.readInt();
        this.mSpanOffsetsSize = parcel.readInt();
        if (this.mSpanOffsetsSize > 0) {
            this.mSpanOffsets = new int[this.mSpanOffsetsSize];
            parcel.readIntArray(this.mSpanOffsets);
        }
        this.mSpanLookupSize = parcel.readInt();
        if (this.mSpanLookupSize > 0) {
            this.mSpanLookup = new int[this.mSpanLookupSize];
            parcel.readIntArray(this.mSpanLookup);
        }
        this.mReverseLayout = parcel.readInt() == 1;
        this.mAnchorLayoutFromEnd = parcel.readInt() == 1;
        this.mLastLayoutRTL = parcel.readInt() == 1;
        this.mFullSpanItems = parcel.readArrayList(StaggeredGridLayoutManager$LazySpanLookup$FullSpanItem.class.getClassLoader());
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.mAnchorPosition);
        parcel.writeInt(this.mVisibleAnchorPosition);
        parcel.writeInt(this.mSpanOffsetsSize);
        if (this.mSpanOffsetsSize > 0) {
            parcel.writeIntArray(this.mSpanOffsets);
        }
        parcel.writeInt(this.mSpanLookupSize);
        if (this.mSpanLookupSize > 0) {
            parcel.writeIntArray(this.mSpanLookup);
        }
        parcel.writeInt(this.mReverseLayout ? 1 : 0);
        parcel.writeInt(this.mAnchorLayoutFromEnd ? 1 : 0);
        parcel.writeInt(this.mLastLayoutRTL ? 1 : 0);
        parcel.writeList(this.mFullSpanItems);
    }
}

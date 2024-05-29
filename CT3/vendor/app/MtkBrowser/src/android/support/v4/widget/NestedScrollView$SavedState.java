package android.support.v4.widget;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
/* loaded from: b.zip:android/support/v4/widget/NestedScrollView$SavedState.class */
class NestedScrollView$SavedState extends View.BaseSavedState {
    public static final Parcelable.Creator<NestedScrollView$SavedState> CREATOR = new Parcelable.Creator<NestedScrollView$SavedState>() { // from class: android.support.v4.widget.NestedScrollView$SavedState.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public NestedScrollView$SavedState createFromParcel(Parcel parcel) {
            return new NestedScrollView$SavedState(parcel);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public NestedScrollView$SavedState[] newArray(int i) {
            return new NestedScrollView$SavedState[i];
        }
    };
    public int scrollPosition;

    public NestedScrollView$SavedState(Parcel parcel) {
        super(parcel);
        this.scrollPosition = parcel.readInt();
    }

    public String toString() {
        return "HorizontalScrollView.SavedState{" + Integer.toHexString(System.identityHashCode(this)) + " scrollPosition=" + this.scrollPosition + "}";
    }

    @Override // android.view.View.BaseSavedState, android.view.AbsSavedState, android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeInt(this.scrollPosition);
    }
}

package android.support.v4.widget;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.support.v4.view.AbsSavedState;
/* loaded from: b.zip:android/support/v4/widget/SlidingPaneLayout$SavedState.class */
class SlidingPaneLayout$SavedState extends AbsSavedState {
    public static final Parcelable.Creator<SlidingPaneLayout$SavedState> CREATOR = ParcelableCompat.newCreator(new ParcelableCompatCreatorCallbacks<SlidingPaneLayout$SavedState>() { // from class: android.support.v4.widget.SlidingPaneLayout$SavedState.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.support.v4.os.ParcelableCompatCreatorCallbacks
        public SlidingPaneLayout$SavedState createFromParcel(Parcel parcel, ClassLoader classLoader) {
            return new SlidingPaneLayout$SavedState(parcel, classLoader, null);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.support.v4.os.ParcelableCompatCreatorCallbacks
        public SlidingPaneLayout$SavedState[] newArray(int i) {
            return new SlidingPaneLayout$SavedState[i];
        }
    });
    boolean isOpen;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    private SlidingPaneLayout$SavedState(Parcel parcel, ClassLoader classLoader) {
        super(parcel, classLoader);
        boolean z = false;
        this.isOpen = parcel.readInt() != 0 ? true : z;
    }

    /* synthetic */ SlidingPaneLayout$SavedState(Parcel parcel, ClassLoader classLoader, SlidingPaneLayout$SavedState slidingPaneLayout$SavedState) {
        this(parcel, classLoader);
    }

    @Override // android.support.v4.view.AbsSavedState, android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeInt(this.isOpen ? 1 : 0);
    }
}

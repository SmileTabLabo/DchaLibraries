package android.support.v4.app;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
/* loaded from: a.zip:android/support/v4/app/FragmentTabHost$SavedState.class */
class FragmentTabHost$SavedState extends View.BaseSavedState {
    public static final Parcelable.Creator<FragmentTabHost$SavedState> CREATOR = new Parcelable.Creator<FragmentTabHost$SavedState>() { // from class: android.support.v4.app.FragmentTabHost$SavedState.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public FragmentTabHost$SavedState createFromParcel(Parcel parcel) {
            return new FragmentTabHost$SavedState(parcel, null);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public FragmentTabHost$SavedState[] newArray(int i) {
            return new FragmentTabHost$SavedState[i];
        }
    };
    String curTab;

    private FragmentTabHost$SavedState(Parcel parcel) {
        super(parcel);
        this.curTab = parcel.readString();
    }

    /* synthetic */ FragmentTabHost$SavedState(Parcel parcel, FragmentTabHost$SavedState fragmentTabHost$SavedState) {
        this(parcel);
    }

    public String toString() {
        return "FragmentTabHost.SavedState{" + Integer.toHexString(System.identityHashCode(this)) + " curTab=" + this.curTab + "}";
    }

    @Override // android.view.View.BaseSavedState, android.view.AbsSavedState, android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeString(this.curTab);
    }
}

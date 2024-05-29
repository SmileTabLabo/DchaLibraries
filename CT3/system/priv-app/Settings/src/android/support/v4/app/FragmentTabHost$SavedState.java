package android.support.v4.app;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
/* loaded from: classes.dex */
class FragmentTabHost$SavedState extends View.BaseSavedState {
    public static final Parcelable.Creator<FragmentTabHost$SavedState> CREATOR = new Parcelable.Creator<FragmentTabHost$SavedState>() { // from class: android.support.v4.app.FragmentTabHost$SavedState.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public FragmentTabHost$SavedState createFromParcel(Parcel in) {
            return new FragmentTabHost$SavedState(in, null);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public FragmentTabHost$SavedState[] newArray(int size) {
            return new FragmentTabHost$SavedState[size];
        }
    };
    String curTab;

    /* synthetic */ FragmentTabHost$SavedState(Parcel in, FragmentTabHost$SavedState fragmentTabHost$SavedState) {
        this(in);
    }

    private FragmentTabHost$SavedState(Parcel in) {
        super(in);
        this.curTab = in.readString();
    }

    @Override // android.view.View.BaseSavedState, android.view.AbsSavedState, android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeString(this.curTab);
    }

    public String toString() {
        return "FragmentTabHost.SavedState{" + Integer.toHexString(System.identityHashCode(this)) + " curTab=" + this.curTab + "}";
    }
}

package android.support.v4.view;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
/* loaded from: a.zip:android/support/v4/view/ViewPager$SavedState.class */
public class ViewPager$SavedState extends AbsSavedState {
    public static final Parcelable.Creator<ViewPager$SavedState> CREATOR = ParcelableCompat.newCreator(new ParcelableCompatCreatorCallbacks<ViewPager$SavedState>() { // from class: android.support.v4.view.ViewPager$SavedState.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.support.v4.os.ParcelableCompatCreatorCallbacks
        public ViewPager$SavedState createFromParcel(Parcel parcel, ClassLoader classLoader) {
            return new ViewPager$SavedState(parcel, classLoader);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.support.v4.os.ParcelableCompatCreatorCallbacks
        public ViewPager$SavedState[] newArray(int i) {
            return new ViewPager$SavedState[i];
        }
    });
    Parcelable adapterState;
    ClassLoader loader;
    int position;

    ViewPager$SavedState(Parcel parcel, ClassLoader classLoader) {
        super(parcel, classLoader);
        ClassLoader classLoader2 = classLoader == null ? getClass().getClassLoader() : classLoader;
        this.position = parcel.readInt();
        this.adapterState = parcel.readParcelable(classLoader2);
        this.loader = classLoader2;
    }

    public String toString() {
        return "FragmentPager.SavedState{" + Integer.toHexString(System.identityHashCode(this)) + " position=" + this.position + "}";
    }

    @Override // android.support.v4.view.AbsSavedState, android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeInt(this.position);
        parcel.writeParcelable(this.adapterState, i);
    }
}

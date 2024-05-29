package android.support.v4.widget;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.support.v4.view.AbsSavedState;
/* loaded from: b.zip:android/support/v4/widget/DrawerLayout$SavedState.class */
class DrawerLayout$SavedState extends AbsSavedState {
    public static final Parcelable.Creator<DrawerLayout$SavedState> CREATOR = ParcelableCompat.newCreator(new ParcelableCompatCreatorCallbacks<DrawerLayout$SavedState>() { // from class: android.support.v4.widget.DrawerLayout$SavedState.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.support.v4.os.ParcelableCompatCreatorCallbacks
        public DrawerLayout$SavedState createFromParcel(Parcel parcel, ClassLoader classLoader) {
            return new DrawerLayout$SavedState(parcel, classLoader);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.support.v4.os.ParcelableCompatCreatorCallbacks
        public DrawerLayout$SavedState[] newArray(int i) {
            return new DrawerLayout$SavedState[i];
        }
    });
    int lockModeEnd;
    int lockModeLeft;
    int lockModeRight;
    int lockModeStart;
    int openDrawerGravity;

    public DrawerLayout$SavedState(Parcel parcel, ClassLoader classLoader) {
        super(parcel, classLoader);
        this.openDrawerGravity = 0;
        this.openDrawerGravity = parcel.readInt();
        this.lockModeLeft = parcel.readInt();
        this.lockModeRight = parcel.readInt();
        this.lockModeStart = parcel.readInt();
        this.lockModeEnd = parcel.readInt();
    }

    @Override // android.support.v4.view.AbsSavedState, android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeInt(this.openDrawerGravity);
        parcel.writeInt(this.lockModeLeft);
        parcel.writeInt(this.lockModeRight);
        parcel.writeInt(this.lockModeStart);
        parcel.writeInt(this.lockModeEnd);
    }
}

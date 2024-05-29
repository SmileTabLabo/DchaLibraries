package android.support.v7.app;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: a.zip:android/support/v7/app/AppCompatDelegateImplV7$PanelFeatureState$SavedState.class */
public class AppCompatDelegateImplV7$PanelFeatureState$SavedState implements Parcelable {
    public static final Parcelable.Creator<AppCompatDelegateImplV7$PanelFeatureState$SavedState> CREATOR = ParcelableCompat.newCreator(new ParcelableCompatCreatorCallbacks<AppCompatDelegateImplV7$PanelFeatureState$SavedState>() { // from class: android.support.v7.app.AppCompatDelegateImplV7$PanelFeatureState$SavedState.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.support.v4.os.ParcelableCompatCreatorCallbacks
        public AppCompatDelegateImplV7$PanelFeatureState$SavedState createFromParcel(Parcel parcel, ClassLoader classLoader) {
            return AppCompatDelegateImplV7$PanelFeatureState$SavedState.readFromParcel(parcel, classLoader);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.support.v4.os.ParcelableCompatCreatorCallbacks
        public AppCompatDelegateImplV7$PanelFeatureState$SavedState[] newArray(int i) {
            return new AppCompatDelegateImplV7$PanelFeatureState$SavedState[i];
        }
    });
    int featureId;
    boolean isOpen;
    Bundle menuState;

    private AppCompatDelegateImplV7$PanelFeatureState$SavedState() {
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static AppCompatDelegateImplV7$PanelFeatureState$SavedState readFromParcel(Parcel parcel, ClassLoader classLoader) {
        boolean z = true;
        AppCompatDelegateImplV7$PanelFeatureState$SavedState appCompatDelegateImplV7$PanelFeatureState$SavedState = new AppCompatDelegateImplV7$PanelFeatureState$SavedState();
        appCompatDelegateImplV7$PanelFeatureState$SavedState.featureId = parcel.readInt();
        if (parcel.readInt() != 1) {
            z = false;
        }
        appCompatDelegateImplV7$PanelFeatureState$SavedState.isOpen = z;
        if (appCompatDelegateImplV7$PanelFeatureState$SavedState.isOpen) {
            appCompatDelegateImplV7$PanelFeatureState$SavedState.menuState = parcel.readBundle(classLoader);
        }
        return appCompatDelegateImplV7$PanelFeatureState$SavedState;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.featureId);
        parcel.writeInt(this.isOpen ? 1 : 0);
        if (this.isOpen) {
            parcel.writeBundle(this.menuState);
        }
    }
}

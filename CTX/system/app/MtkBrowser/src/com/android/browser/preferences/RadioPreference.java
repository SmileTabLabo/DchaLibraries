package com.android.browser.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.Checkable;
import com.android.browser.R;
/* loaded from: classes.dex */
public class RadioPreference extends Preference {
    private AccessibilityManager mAccessibilityManager;
    private boolean mChecked;
    private boolean mDisableDependentsState;
    private boolean mSendAccessibilityEventViewClickedType;

    public RadioPreference(Context context) {
        super(context);
        this.mAccessibilityManager = (AccessibilityManager) getContext().getSystemService("accessibility");
    }

    @Override // android.preference.Preference
    public boolean isPersistent() {
        return false;
    }

    @Override // android.preference.Preference
    protected void onBindView(View view) {
        super.onBindView(view);
        View findViewById = view.findViewById(R.id.radiobutton);
        if (findViewById != null && (findViewById instanceof Checkable)) {
            ((Checkable) findViewById).setChecked(this.mChecked);
            if (this.mSendAccessibilityEventViewClickedType && this.mAccessibilityManager.isEnabled() && findViewById.isEnabled()) {
                this.mSendAccessibilityEventViewClickedType = false;
                findViewById.sendAccessibilityEventUnchecked(AccessibilityEvent.obtain(1));
            }
        }
    }

    @Override // android.preference.Preference
    protected void onClick() {
        super.onClick();
        boolean z = !isChecked();
        this.mSendAccessibilityEventViewClickedType = true;
        if (!callChangeListener(Boolean.valueOf(z))) {
            return;
        }
        setChecked(z);
    }

    public void setChecked(boolean z) {
        if (this.mChecked != z) {
            this.mChecked = z;
            persistBoolean(z);
            notifyDependencyChange(shouldDisableDependents());
            notifyChanged();
        }
    }

    public boolean isChecked() {
        return this.mChecked;
    }

    @Override // android.preference.Preference
    public boolean shouldDisableDependents() {
        boolean z;
        if (this.mDisableDependentsState) {
            z = this.mChecked;
        } else {
            z = !this.mChecked;
        }
        return z || super.shouldDisableDependents();
    }

    @Override // android.preference.Preference
    protected Object onGetDefaultValue(TypedArray typedArray, int i) {
        return Boolean.valueOf(typedArray.getBoolean(i, false));
    }

    @Override // android.preference.Preference
    protected void onSetInitialValue(boolean z, Object obj) {
        setChecked(z ? getPersistedBoolean(this.mChecked) : ((Boolean) obj).booleanValue());
    }

    @Override // android.preference.Preference
    protected Parcelable onSaveInstanceState() {
        Parcelable onSaveInstanceState = super.onSaveInstanceState();
        if (isPersistent()) {
            return onSaveInstanceState;
        }
        SavedState savedState = new SavedState(onSaveInstanceState);
        savedState.mSaveStateChecked = isChecked();
        return savedState;
    }

    @Override // android.preference.Preference
    protected void onRestoreInstanceState(Parcelable parcelable) {
        if (parcelable == null || !parcelable.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(parcelable);
            return;
        }
        SavedState savedState = (SavedState) parcelable;
        super.onRestoreInstanceState(savedState.getSuperState());
        setChecked(savedState.mSaveStateChecked);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class SavedState extends Preference.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() { // from class: com.android.browser.preferences.RadioPreference.SavedState.1
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.Creator
            public SavedState createFromParcel(Parcel parcel) {
                return new SavedState(parcel);
            }

            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.Creator
            public SavedState[] newArray(int i) {
                return new SavedState[i];
            }
        };
        boolean mSaveStateChecked;

        public SavedState(Parcel parcel) {
            super(parcel);
            this.mSaveStateChecked = parcel.readInt() == 1;
        }

        @Override // android.view.AbsSavedState, android.os.Parcelable
        public void writeToParcel(Parcel parcel, int i) {
            super.writeToParcel(parcel, i);
            parcel.writeInt(this.mSaveStateChecked ? 1 : 0);
        }

        public SavedState(Parcelable parcelable) {
            super(parcelable);
        }
    }
}

package com.android.settings;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.hardware.input.InputManager;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
/* loaded from: classes.dex */
public class PointerSpeedPreference extends SeekBarDialogPreference implements SeekBar.OnSeekBarChangeListener {
    private final InputManager mIm;
    private int mOldSpeed;
    private boolean mRestoredOldState;
    private SeekBar mSeekBar;
    private ContentObserver mSpeedObserver;
    private boolean mTouchInProgress;

    public PointerSpeedPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mSpeedObserver = new ContentObserver(new Handler()) { // from class: com.android.settings.PointerSpeedPreference.1
            @Override // android.database.ContentObserver
            public void onChange(boolean z) {
                PointerSpeedPreference.this.onSpeedChanged();
            }
        };
        this.mIm = (InputManager) getContext().getSystemService("input");
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.preference.DialogPreference, android.support.v7.preference.Preference
    public void onClick() {
        super.onClick();
        getContext().getContentResolver().registerContentObserver(Settings.System.getUriFor("pointer_speed"), true, this.mSpeedObserver);
        this.mRestoredOldState = false;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.SeekBarDialogPreference, com.android.settingslib.CustomDialogPreference
    public void onBindDialogView(View view) {
        super.onBindDialogView(view);
        this.mSeekBar = getSeekBar(view);
        this.mSeekBar.setMax(14);
        this.mOldSpeed = this.mIm.getPointerSpeed(getContext());
        this.mSeekBar.setProgress(this.mOldSpeed + 7);
        this.mSeekBar.setOnSeekBarChangeListener(this);
    }

    @Override // android.widget.SeekBar.OnSeekBarChangeListener
    public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
        if (!this.mTouchInProgress) {
            this.mIm.tryPointerSpeed(i - 7);
        }
    }

    @Override // android.widget.SeekBar.OnSeekBarChangeListener
    public void onStartTrackingTouch(SeekBar seekBar) {
        this.mTouchInProgress = true;
    }

    @Override // android.widget.SeekBar.OnSeekBarChangeListener
    public void onStopTrackingTouch(SeekBar seekBar) {
        this.mTouchInProgress = false;
        this.mIm.tryPointerSpeed(seekBar.getProgress() - 7);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onSpeedChanged() {
        this.mSeekBar.setProgress(this.mIm.getPointerSpeed(getContext()) + 7);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settingslib.CustomDialogPreference
    public void onDialogClosed(boolean z) {
        super.onDialogClosed(z);
        ContentResolver contentResolver = getContext().getContentResolver();
        if (z) {
            this.mIm.setPointerSpeed(getContext(), this.mSeekBar.getProgress() - 7);
        } else {
            restoreOldState();
        }
        contentResolver.unregisterContentObserver(this.mSpeedObserver);
    }

    private void restoreOldState() {
        if (this.mRestoredOldState) {
            return;
        }
        this.mIm.tryPointerSpeed(this.mOldSpeed);
        this.mRestoredOldState = true;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.preference.Preference
    public Parcelable onSaveInstanceState() {
        Parcelable onSaveInstanceState = super.onSaveInstanceState();
        if (getDialog() == null || !getDialog().isShowing()) {
            return onSaveInstanceState;
        }
        SavedState savedState = new SavedState(onSaveInstanceState);
        savedState.progress = this.mSeekBar.getProgress();
        savedState.oldSpeed = this.mOldSpeed;
        restoreOldState();
        return savedState;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.preference.Preference
    public void onRestoreInstanceState(Parcelable parcelable) {
        if (parcelable == null || !parcelable.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(parcelable);
            return;
        }
        SavedState savedState = (SavedState) parcelable;
        super.onRestoreInstanceState(savedState.getSuperState());
        this.mOldSpeed = savedState.oldSpeed;
        this.mIm.tryPointerSpeed(savedState.progress - 7);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class SavedState extends Preference.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() { // from class: com.android.settings.PointerSpeedPreference.SavedState.1
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
        int oldSpeed;
        int progress;

        public SavedState(Parcel parcel) {
            super(parcel);
            this.progress = parcel.readInt();
            this.oldSpeed = parcel.readInt();
        }

        @Override // android.view.AbsSavedState, android.os.Parcelable
        public void writeToParcel(Parcel parcel, int i) {
            super.writeToParcel(parcel, i);
            parcel.writeInt(this.progress);
            parcel.writeInt(this.oldSpeed);
        }

        public SavedState(Parcelable parcelable) {
            super(parcelable);
        }
    }
}

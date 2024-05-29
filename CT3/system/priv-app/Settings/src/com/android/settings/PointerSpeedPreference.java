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

    public PointerSpeedPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mSpeedObserver = new ContentObserver(new Handler()) { // from class: com.android.settings.PointerSpeedPreference.1
            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
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
    @Override // com.android.settings.SeekBarDialogPreference, com.android.settings.CustomDialogPreference
    public void onBindDialogView(View view) {
        super.onBindDialogView(view);
        this.mSeekBar = getSeekBar(view);
        this.mSeekBar.setMax(14);
        this.mOldSpeed = this.mIm.getPointerSpeed(getContext());
        this.mSeekBar.setProgress(this.mOldSpeed + 7);
        this.mSeekBar.setOnSeekBarChangeListener(this);
    }

    @Override // android.widget.SeekBar.OnSeekBarChangeListener
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
        if (this.mTouchInProgress) {
            return;
        }
        this.mIm.tryPointerSpeed(progress - 7);
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
        int speed = this.mIm.getPointerSpeed(getContext());
        this.mSeekBar.setProgress(speed + 7);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.CustomDialogPreference
    public void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        ContentResolver resolver = getContext().getContentResolver();
        if (positiveResult) {
            this.mIm.setPointerSpeed(getContext(), this.mSeekBar.getProgress() - 7);
        } else {
            restoreOldState();
        }
        resolver.unregisterContentObserver(this.mSpeedObserver);
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
        Parcelable superState = super.onSaveInstanceState();
        if (getDialog() == null || !getDialog().isShowing()) {
            return superState;
        }
        SavedState myState = new SavedState(superState);
        myState.progress = this.mSeekBar.getProgress();
        myState.oldSpeed = this.mOldSpeed;
        restoreOldState();
        return myState;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.preference.Preference
    public void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        this.mOldSpeed = myState.oldSpeed;
        this.mIm.tryPointerSpeed(myState.progress - 7);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class SavedState extends Preference.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() { // from class: com.android.settings.PointerSpeedPreference.SavedState.1
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.Creator
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.Creator
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        int oldSpeed;
        int progress;

        public SavedState(Parcel source) {
            super(source);
            this.progress = source.readInt();
            this.oldSpeed = source.readInt();
        }

        @Override // android.view.AbsSavedState, android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.progress);
            dest.writeInt(this.oldSpeed);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }
    }
}

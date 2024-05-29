package com.android.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.SeekBar;
import com.android.internal.R;
import com.android.settingslib.RestrictedPreference;
/* loaded from: classes.dex */
public class SeekBarPreference extends RestrictedPreference implements SeekBar.OnSeekBarChangeListener, View.OnKeyListener {
    private int mMax;
    private int mProgress;
    private boolean mTrackingTouch;

    public SeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ProgressBar, defStyleAttr, defStyleRes);
        setMax(a.getInt(2, this.mMax));
        a.recycle();
        TypedArray a2 = context.obtainStyledAttributes(attrs, R.styleable.SeekBarPreference, defStyleAttr, defStyleRes);
        int layoutResId = a2.getResourceId(0, 17367226);
        a2.recycle();
        setLayoutResource(layoutResId);
    }

    public SeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SeekBarPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 18219046);
    }

    public SeekBarPreference(Context context) {
        this(context, null);
    }

    @Override // com.android.settingslib.RestrictedPreference, android.support.v7.preference.Preference
    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        view.itemView.setOnKeyListener(this);
        SeekBar seekBar = (SeekBar) view.findViewById(16909261);
        seekBar.setOnSeekBarChangeListener(this);
        seekBar.setMax(this.mMax);
        seekBar.setProgress(this.mProgress);
        seekBar.setEnabled(isEnabled());
    }

    @Override // android.support.v7.preference.Preference
    public CharSequence getSummary() {
        return null;
    }

    @Override // android.support.v7.preference.Preference
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setProgress(restoreValue ? getPersistedInt(this.mProgress) : ((Integer) defaultValue).intValue());
    }

    @Override // android.support.v7.preference.Preference
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return Integer.valueOf(a.getInt(index, 0));
    }

    @Override // android.view.View.OnKeyListener
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        SeekBar seekBar;
        if (event.getAction() == 0 && (seekBar = (SeekBar) v.findViewById(16909261)) != null) {
            return seekBar.onKeyDown(keyCode, event);
        }
        return false;
    }

    public void setMax(int max) {
        if (max == this.mMax) {
            return;
        }
        this.mMax = max;
        notifyChanged();
    }

    public void setProgress(int progress) {
        setProgress(progress, true);
    }

    private void setProgress(int progress, boolean notifyChanged) {
        if (progress > this.mMax) {
            progress = this.mMax;
        }
        if (progress < 0) {
            progress = 0;
        }
        if (progress == this.mProgress) {
            return;
        }
        this.mProgress = progress;
        persistInt(progress);
        if (!notifyChanged) {
            return;
        }
        notifyChanged();
    }

    void syncProgress(SeekBar seekBar) {
        int progress = seekBar.getProgress();
        if (progress == this.mProgress) {
            return;
        }
        if (callChangeListener(Integer.valueOf(progress))) {
            setProgress(progress, false);
        } else {
            seekBar.setProgress(this.mProgress);
        }
    }

    @Override // android.widget.SeekBar.OnSeekBarChangeListener
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (!fromUser || this.mTrackingTouch) {
            return;
        }
        syncProgress(seekBar);
    }

    @Override // android.widget.SeekBar.OnSeekBarChangeListener
    public void onStartTrackingTouch(SeekBar seekBar) {
        this.mTrackingTouch = true;
    }

    @Override // android.widget.SeekBar.OnSeekBarChangeListener
    public void onStopTrackingTouch(SeekBar seekBar) {
        this.mTrackingTouch = false;
        if (seekBar.getProgress() == this.mProgress) {
            return;
        }
        syncProgress(seekBar);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.preference.Preference
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            return superState;
        }
        SavedState myState = new SavedState(superState);
        myState.progress = this.mProgress;
        myState.max = this.mMax;
        return myState;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.preference.Preference
    public void onRestoreInstanceState(Parcelable state) {
        if (!state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        this.mProgress = myState.progress;
        this.mMax = myState.max;
        notifyChanged();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class SavedState extends Preference.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() { // from class: com.android.settings.SeekBarPreference.SavedState.1
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
        int max;
        int progress;

        public SavedState(Parcel source) {
            super(source);
            this.progress = source.readInt();
            this.max = source.readInt();
        }

        @Override // android.view.AbsSavedState, android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.progress);
            dest.writeInt(this.max);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }
    }
}

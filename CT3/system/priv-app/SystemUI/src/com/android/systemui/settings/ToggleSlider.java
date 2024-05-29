package com.android.systemui.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import com.android.systemui.R$styleable;
import com.android.systemui.statusbar.policy.BrightnessMirrorController;
/* loaded from: a.zip:com/android/systemui/settings/ToggleSlider.class */
public class ToggleSlider extends RelativeLayout {
    private final CompoundButton.OnCheckedChangeListener mCheckListener;
    private TextView mLabel;
    private Listener mListener;
    private ToggleSlider mMirror;
    private BrightnessMirrorController mMirrorController;
    private final SeekBar.OnSeekBarChangeListener mSeekListener;
    private ToggleSeekBar mSlider;
    private CompoundButton mToggle;
    private boolean mTracking;

    /* loaded from: a.zip:com/android/systemui/settings/ToggleSlider$Listener.class */
    public interface Listener {
        void onChanged(ToggleSlider toggleSlider, boolean z, boolean z2, int i, boolean z3);

        void onInit(ToggleSlider toggleSlider);
    }

    public ToggleSlider(Context context) {
        this(context, null);
    }

    public ToggleSlider(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ToggleSlider(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mCheckListener = new CompoundButton.OnCheckedChangeListener(this) { // from class: com.android.systemui.settings.ToggleSlider.1
            final ToggleSlider this$0;

            {
                this.this$0 = this;
            }

            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                this.this$0.mSlider.setEnabled(!z);
                if (this.this$0.mListener != null) {
                    this.this$0.mListener.onChanged(this.this$0, this.this$0.mTracking, z, this.this$0.mSlider.getProgress(), false);
                }
                if (this.this$0.mMirror != null) {
                    this.this$0.mMirror.mToggle.setChecked(z);
                }
            }
        };
        this.mSeekListener = new SeekBar.OnSeekBarChangeListener(this) { // from class: com.android.systemui.settings.ToggleSlider.2
            final ToggleSlider this$0;

            {
                this.this$0 = this;
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onProgressChanged(SeekBar seekBar, int i2, boolean z) {
                if (this.this$0.mListener != null) {
                    this.this$0.mListener.onChanged(this.this$0, this.this$0.mTracking, this.this$0.mToggle.isChecked(), i2, false);
                }
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStartTrackingTouch(SeekBar seekBar) {
                this.this$0.mTracking = true;
                if (this.this$0.mListener != null) {
                    this.this$0.mListener.onChanged(this.this$0, this.this$0.mTracking, this.this$0.mToggle.isChecked(), this.this$0.mSlider.getProgress(), false);
                }
                this.this$0.mToggle.setChecked(false);
                if (this.this$0.mMirrorController != null) {
                    this.this$0.mMirrorController.showMirror();
                    this.this$0.mMirrorController.setLocation((View) this.this$0.getParent());
                }
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStopTrackingTouch(SeekBar seekBar) {
                this.this$0.mTracking = false;
                if (this.this$0.mListener != null) {
                    this.this$0.mListener.onChanged(this.this$0, this.this$0.mTracking, this.this$0.mToggle.isChecked(), this.this$0.mSlider.getProgress(), true);
                }
                if (this.this$0.mMirrorController != null) {
                    this.this$0.mMirrorController.hideMirror();
                }
            }
        };
        View.inflate(context, 2130968817, this);
        context.getResources();
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.ToggleSlider, i, 0);
        this.mToggle = (CompoundButton) findViewById(2131886707);
        this.mToggle.setOnCheckedChangeListener(this.mCheckListener);
        this.mSlider = (ToggleSeekBar) findViewById(2131886708);
        this.mSlider.setOnSeekBarChangeListener(this.mSeekListener);
        this.mLabel = (TextView) findViewById(2131886369);
        this.mLabel.setText(obtainStyledAttributes.getString(0));
        this.mSlider.setAccessibilityLabel(getContentDescription().toString());
        obtainStyledAttributes.recycle();
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (this.mMirror != null) {
            MotionEvent copy = motionEvent.copy();
            this.mMirror.dispatchTouchEvent(copy);
            copy.recycle();
        }
        return super.dispatchTouchEvent(motionEvent);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mListener != null) {
            this.mListener.onInit(this);
        }
    }

    public void setChecked(boolean z) {
        this.mToggle.setChecked(z);
    }

    public void setMax(int i) {
        this.mSlider.setMax(i);
        if (this.mMirror != null) {
            this.mMirror.setMax(i);
        }
    }

    public void setMirror(ToggleSlider toggleSlider) {
        this.mMirror = toggleSlider;
        if (this.mMirror != null) {
            this.mMirror.setChecked(this.mToggle.isChecked());
            this.mMirror.setMax(this.mSlider.getMax());
            this.mMirror.setValue(this.mSlider.getProgress());
        }
    }

    public void setMirrorController(BrightnessMirrorController brightnessMirrorController) {
        this.mMirrorController = brightnessMirrorController;
    }

    public void setOnChangedListener(Listener listener) {
        this.mListener = listener;
    }

    public void setValue(int i) {
        this.mSlider.setProgress(i);
        if (this.mMirror != null) {
            this.mMirror.setValue(i);
        }
    }
}

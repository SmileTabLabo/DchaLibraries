package com.android.keyguard;

import android.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.internal.widget.LockPatternUtils;
/* loaded from: a.zip:com/android/keyguard/NumPadKey.class */
public class NumPadKey extends ViewGroup {
    static String[] sKlondike;
    private int mDigit;
    private TextView mDigitText;
    private boolean mEnableHaptics;
    private TextView mKlondikeText;
    private View.OnClickListener mListener;
    private PowerManager mPM;
    private PasswordTextView mTextView;
    private int mTextViewResId;

    public NumPadKey(Context context) {
        this(context, null);
    }

    public NumPadKey(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public NumPadKey(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, R$layout.keyguard_num_pad_key);
    }

    protected NumPadKey(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i);
        this.mDigit = -1;
        this.mListener = new View.OnClickListener(this) { // from class: com.android.keyguard.NumPadKey.1
            final NumPadKey this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                View findViewById;
                if (this.this$0.mTextView == null && this.this$0.mTextViewResId > 0 && (findViewById = this.this$0.getRootView().findViewById(this.this$0.mTextViewResId)) != null && (findViewById instanceof PasswordTextView)) {
                    this.this$0.mTextView = (PasswordTextView) findViewById;
                }
                if (this.this$0.mTextView != null && this.this$0.mTextView.isEnabled()) {
                    this.this$0.mTextView.append(Character.forDigit(this.this$0.mDigit, 10));
                }
                this.this$0.userActivity();
                this.this$0.doHapticKeyClick();
            }
        };
        setFocusable(true);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.NumPadKey);
        try {
            this.mDigit = obtainStyledAttributes.getInt(R$styleable.NumPadKey_digit, this.mDigit);
            this.mTextViewResId = obtainStyledAttributes.getResourceId(R$styleable.NumPadKey_textView, 0);
            obtainStyledAttributes.recycle();
            setOnClickListener(this.mListener);
            setOnHoverListener(new LiftToActivateListener(context));
            setAccessibilityDelegate(new ObscureSpeechDelegate(context));
            this.mEnableHaptics = new LockPatternUtils(context).isTactileFeedbackEnabled();
            this.mPM = (PowerManager) this.mContext.getSystemService("power");
            ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(i2, (ViewGroup) this, true);
            this.mDigitText = (TextView) findViewById(R$id.digit_text);
            this.mDigitText.setText(Integer.toString(this.mDigit));
            this.mKlondikeText = (TextView) findViewById(R$id.klondike_text);
            if (this.mDigit >= 0) {
                if (sKlondike == null) {
                    sKlondike = getResources().getStringArray(R$array.lockscreen_num_pad_klondike);
                }
                if (sKlondike != null && sKlondike.length > this.mDigit) {
                    String str = sKlondike[this.mDigit];
                    if (str.length() > 0) {
                        this.mKlondikeText.setText(str);
                    } else {
                        this.mKlondikeText.setVisibility(4);
                    }
                }
            }
            TypedArray obtainStyledAttributes2 = context.obtainStyledAttributes(attributeSet, R.styleable.View);
            if (!obtainStyledAttributes2.hasValueOrEmpty(13)) {
                setBackground(this.mContext.getDrawable(R$drawable.ripple_drawable));
            }
            obtainStyledAttributes2.recycle();
            setContentDescription(this.mDigitText.getText().toString());
        } catch (Throwable th) {
            obtainStyledAttributes.recycle();
            throw th;
        }
    }

    public void doHapticKeyClick() {
        if (this.mEnableHaptics) {
            performHapticFeedback(1, 3);
        }
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    @Override // android.view.ViewGroup, android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ObscureSpeechDelegate.sAnnouncedHeadset = false;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int measuredHeight = this.mDigitText.getMeasuredHeight();
        int measuredHeight2 = this.mKlondikeText.getMeasuredHeight();
        int height = (getHeight() / 2) - ((measuredHeight + measuredHeight2) / 2);
        int width = getWidth() / 2;
        int measuredWidth = width - (this.mDigitText.getMeasuredWidth() / 2);
        int i5 = height + measuredHeight;
        this.mDigitText.layout(measuredWidth, height, this.mDigitText.getMeasuredWidth() + measuredWidth, i5);
        int i6 = (int) (i5 - (measuredHeight2 * 0.35f));
        int measuredWidth2 = width - (this.mKlondikeText.getMeasuredWidth() / 2);
        this.mKlondikeText.layout(measuredWidth2, i6, this.mKlondikeText.getMeasuredWidth() + measuredWidth2, i6 + measuredHeight2);
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        measureChildren(i, i2);
    }

    public void userActivity() {
        this.mPM.userActivity(SystemClock.uptimeMillis(), false);
    }
}

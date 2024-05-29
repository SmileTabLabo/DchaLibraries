package com.android.systemui.tv.pip;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
/* loaded from: a.zip:com/android/systemui/tv/pip/PipControlButtonView.class */
public class PipControlButtonView extends RelativeLayout {
    private Animator mButtonFocusGainAnimator;
    private Animator mButtonFocusLossAnimator;
    ImageView mButtonImageView;
    private TextView mDescriptionTextView;
    private View.OnFocusChangeListener mFocusChangeListener;
    private ImageView mIconImageView;
    private final View.OnFocusChangeListener mInternalFocusChangeListener;
    private Animator mTextFocusGainAnimator;
    private Animator mTextFocusLossAnimator;

    public PipControlButtonView(Context context) {
        this(context, null, 0, 0);
    }

    public PipControlButtonView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0, 0);
    }

    public PipControlButtonView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public PipControlButtonView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mInternalFocusChangeListener = new View.OnFocusChangeListener(this) { // from class: com.android.systemui.tv.pip.PipControlButtonView.1
            final PipControlButtonView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnFocusChangeListener
            public void onFocusChange(View view, boolean z) {
                if (z) {
                    this.this$0.startFocusGainAnimation();
                } else {
                    this.this$0.startFocusLossAnimation();
                }
                if (this.this$0.mFocusChangeListener != null) {
                    this.this$0.mFocusChangeListener.onFocusChange(this.this$0, z);
                }
            }
        };
        ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(2130968825, this);
        this.mIconImageView = (ImageView) findViewById(2131886211);
        this.mButtonImageView = (ImageView) findViewById(2131886368);
        this.mDescriptionTextView = (TextView) findViewById(2131886729);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, new int[]{16843033, 16843087}, i, i2);
        setImageResource(obtainStyledAttributes.getResourceId(0, 0));
        setText(obtainStyledAttributes.getResourceId(1, 0));
        obtainStyledAttributes.recycle();
    }

    private static void cancelAnimator(Animator animator) {
        if (animator.isStarted()) {
            animator.cancel();
        }
    }

    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mButtonImageView.setOnFocusChangeListener(this.mInternalFocusChangeListener);
        this.mTextFocusGainAnimator = AnimatorInflater.loadAnimator(getContext(), 2131034314);
        this.mTextFocusGainAnimator.setTarget(this.mDescriptionTextView);
        this.mButtonFocusGainAnimator = AnimatorInflater.loadAnimator(getContext(), 2131034314);
        this.mButtonFocusGainAnimator.setTarget(this.mButtonImageView);
        this.mTextFocusLossAnimator = AnimatorInflater.loadAnimator(getContext(), 2131034315);
        this.mTextFocusLossAnimator.setTarget(this.mDescriptionTextView);
        this.mButtonFocusLossAnimator = AnimatorInflater.loadAnimator(getContext(), 2131034315);
        this.mButtonFocusLossAnimator.setTarget(this.mButtonImageView);
    }

    public void reset() {
        float f = 1.0f;
        cancelAnimator(this.mButtonFocusGainAnimator);
        cancelAnimator(this.mTextFocusGainAnimator);
        cancelAnimator(this.mButtonFocusLossAnimator);
        cancelAnimator(this.mTextFocusLossAnimator);
        this.mButtonImageView.setAlpha(1.0f);
        TextView textView = this.mDescriptionTextView;
        if (!this.mButtonImageView.hasFocus()) {
            f = 0.0f;
        }
        textView.setAlpha(f);
    }

    public void setImageResource(int i) {
        this.mIconImageView.setImageResource(i);
    }

    @Override // android.view.View
    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.mButtonImageView.setOnClickListener(onClickListener);
    }

    @Override // android.view.View
    public void setOnFocusChangeListener(View.OnFocusChangeListener onFocusChangeListener) {
        this.mFocusChangeListener = onFocusChangeListener;
    }

    public void setText(int i) {
        this.mButtonImageView.setContentDescription(getContext().getString(i));
        this.mDescriptionTextView.setText(i);
    }

    public void startFocusGainAnimation() {
        cancelAnimator(this.mButtonFocusLossAnimator);
        cancelAnimator(this.mTextFocusLossAnimator);
        this.mTextFocusGainAnimator.start();
        if (this.mButtonImageView.getAlpha() < 1.0f) {
            this.mButtonFocusGainAnimator.start();
        }
    }

    public void startFocusLossAnimation() {
        cancelAnimator(this.mButtonFocusGainAnimator);
        cancelAnimator(this.mTextFocusGainAnimator);
        this.mTextFocusLossAnimator.start();
        if (this.mButtonImageView.hasFocus()) {
            this.mButtonFocusLossAnimator.start();
        }
    }
}

package android.support.v17.leanback.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v17.leanback.R$styleable;
import android.text.Layout;
import android.util.AttributeSet;
import android.widget.TextView;
/* loaded from: a.zip:android/support/v17/leanback/widget/ResizingTextView.class */
class ResizingTextView extends TextView {
    private float mDefaultLineSpacingExtra;
    private int mDefaultPaddingBottom;
    private int mDefaultPaddingTop;
    private int mDefaultTextSize;
    private boolean mDefaultsInitialized;
    private boolean mIsResized;
    private boolean mMaintainLineSpacing;
    private int mResizedPaddingAdjustmentBottom;
    private int mResizedPaddingAdjustmentTop;
    private int mResizedTextSize;
    private int mTriggerConditions;

    public ResizingTextView(Context context) {
        this(context, null);
    }

    public ResizingTextView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 16842884);
    }

    public ResizingTextView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public ResizingTextView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i);
        this.mIsResized = false;
        this.mDefaultsInitialized = false;
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.lbResizingTextView, i, i2);
        try {
            this.mTriggerConditions = obtainStyledAttributes.getInt(R$styleable.lbResizingTextView_resizeTrigger, 1);
            this.mResizedTextSize = obtainStyledAttributes.getDimensionPixelSize(R$styleable.lbResizingTextView_resizedTextSize, -1);
            this.mMaintainLineSpacing = obtainStyledAttributes.getBoolean(R$styleable.lbResizingTextView_maintainLineSpacing, false);
            this.mResizedPaddingAdjustmentTop = obtainStyledAttributes.getDimensionPixelOffset(R$styleable.lbResizingTextView_resizedPaddingAdjustmentTop, 0);
            this.mResizedPaddingAdjustmentBottom = obtainStyledAttributes.getDimensionPixelOffset(R$styleable.lbResizingTextView_resizedPaddingAdjustmentBottom, 0);
        } finally {
            obtainStyledAttributes.recycle();
        }
    }

    private void setPaddingTopAndBottom(int i, int i2) {
        if (isPaddingRelative()) {
            setPaddingRelative(getPaddingStart(), i, getPaddingEnd(), i2);
        } else {
            setPadding(getPaddingLeft(), i, getPaddingRight(), i2);
        }
    }

    /* JADX WARN: Code restructure failed: missing block: B:49:0x01b3, code lost:
        if (getPaddingBottom() != r4.mDefaultPaddingBottom) goto L51;
     */
    @Override // android.widget.TextView, android.view.View
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    protected void onMeasure(int i, int i2) {
        boolean z;
        if (!this.mDefaultsInitialized) {
            this.mDefaultTextSize = (int) getTextSize();
            this.mDefaultLineSpacingExtra = getLineSpacingExtra();
            this.mDefaultPaddingTop = getPaddingTop();
            this.mDefaultPaddingBottom = getPaddingBottom();
            this.mDefaultsInitialized = true;
        }
        setTextSize(0, this.mDefaultTextSize);
        setLineSpacing(this.mDefaultLineSpacingExtra, getLineSpacingMultiplier());
        setPaddingTopAndBottom(this.mDefaultPaddingTop, this.mDefaultPaddingBottom);
        super.onMeasure(i, i2);
        Layout layout = getLayout();
        boolean z2 = false;
        if (layout != null) {
            z2 = false;
            if ((this.mTriggerConditions & 1) > 0) {
                int lineCount = layout.getLineCount();
                int maxLines = getMaxLines();
                z2 = false;
                if (maxLines > 1) {
                    z2 = lineCount == maxLines;
                }
            }
        }
        int textSize = (int) getTextSize();
        if (z2) {
            boolean z3 = false;
            if (this.mResizedTextSize != -1) {
                z3 = false;
                if (textSize != this.mResizedTextSize) {
                    setTextSize(0, this.mResizedTextSize);
                    z3 = true;
                }
            }
            float f = (this.mDefaultLineSpacingExtra + this.mDefaultTextSize) - this.mResizedTextSize;
            z = z3;
            if (this.mMaintainLineSpacing) {
                z = z3;
                if (getLineSpacingExtra() != f) {
                    setLineSpacing(f, getLineSpacingMultiplier());
                    z = true;
                }
            }
            int i3 = this.mDefaultPaddingTop + this.mResizedPaddingAdjustmentTop;
            int i4 = this.mDefaultPaddingBottom + this.mResizedPaddingAdjustmentBottom;
            if (getPaddingTop() != i3 || getPaddingBottom() != i4) {
                setPaddingTopAndBottom(i3, i4);
                z = true;
            }
        } else {
            boolean z4 = false;
            if (this.mResizedTextSize != -1) {
                z4 = false;
                if (textSize != this.mDefaultTextSize) {
                    setTextSize(0, this.mDefaultTextSize);
                    z4 = true;
                }
            }
            boolean z5 = z4;
            if (this.mMaintainLineSpacing) {
                z5 = z4;
                if (getLineSpacingExtra() != this.mDefaultLineSpacingExtra) {
                    setLineSpacing(this.mDefaultLineSpacingExtra, getLineSpacingMultiplier());
                    z5 = true;
                }
            }
            if (getPaddingTop() == this.mDefaultPaddingTop) {
                z = z5;
            }
            setPaddingTopAndBottom(this.mDefaultPaddingTop, this.mDefaultPaddingBottom);
            z = true;
        }
        this.mIsResized = z2;
        if (z) {
            super.onMeasure(i, i2);
        }
    }
}

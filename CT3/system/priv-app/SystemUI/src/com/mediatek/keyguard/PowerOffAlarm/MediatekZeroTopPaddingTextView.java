package com.mediatek.keyguard.PowerOffAlarm;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;
/* loaded from: a.zip:com/mediatek/keyguard/PowerOffAlarm/MediatekZeroTopPaddingTextView.class */
public class MediatekZeroTopPaddingTextView extends TextView {
    private static final Typeface SAN_SERIF_BOLD = Typeface.create("san-serif", 1);
    private int mPaddingRight;

    public MediatekZeroTopPaddingTextView(Context context) {
        this(context, null);
    }

    public MediatekZeroTopPaddingTextView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public MediatekZeroTopPaddingTextView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mPaddingRight = 0;
        setIncludeFontPadding(false);
        updatePadding();
    }

    public void updatePadding() {
        if (getTypeface().equals(SAN_SERIF_BOLD)) {
        }
        setPadding(0, 0, this.mPaddingRight, 0);
    }
}

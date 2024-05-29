package android.support.v7.internal.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.R$styleable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
/* loaded from: a.zip:android/support/v7/internal/widget/PreferenceImageView.class */
public class PreferenceImageView extends ImageView {
    private int mMaxHeight;
    private int mMaxWidth;

    public PreferenceImageView(Context context) {
        this(context, null);
    }

    public PreferenceImageView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public PreferenceImageView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mMaxWidth = Integer.MAX_VALUE;
        this.mMaxHeight = Integer.MAX_VALUE;
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.PreferenceImageView, i, 0);
        setMaxWidth(obtainStyledAttributes.getDimensionPixelSize(R$styleable.PreferenceImageView_maxWidth, Integer.MAX_VALUE));
        setMaxHeight(obtainStyledAttributes.getDimensionPixelSize(R$styleable.PreferenceImageView_maxHeight, Integer.MAX_VALUE));
        obtainStyledAttributes.recycle();
    }

    @Override // android.widget.ImageView
    public int getMaxHeight() {
        return this.mMaxHeight;
    }

    @Override // android.widget.ImageView
    public int getMaxWidth() {
        return this.mMaxWidth;
    }

    /* JADX WARN: Code restructure failed: missing block: B:11:0x0033, code lost:
        if (r0 == 0) goto L25;
     */
    /* JADX WARN: Code restructure failed: missing block: B:16:0x004d, code lost:
        if (r0 == 0) goto L12;
     */
    /* JADX WARN: Code restructure failed: missing block: B:22:0x006f, code lost:
        if (r0 == 0) goto L18;
     */
    /* JADX WARN: Code restructure failed: missing block: B:5:0x000f, code lost:
        if (r0 == 0) goto L19;
     */
    @Override // android.widget.ImageView, android.view.View
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    protected void onMeasure(int i, int i2) {
        int i3;
        int i4;
        int mode = View.MeasureSpec.getMode(i);
        if (mode != Integer.MIN_VALUE) {
            i3 = i;
        }
        int size = View.MeasureSpec.getSize(i);
        int maxWidth = getMaxWidth();
        i3 = i;
        if (maxWidth != Integer.MAX_VALUE) {
            if (maxWidth >= size) {
                i3 = i;
            }
            i3 = View.MeasureSpec.makeMeasureSpec(maxWidth, Integer.MIN_VALUE);
        }
        int mode2 = View.MeasureSpec.getMode(i2);
        if (mode2 != Integer.MIN_VALUE) {
            i4 = i2;
        }
        int size2 = View.MeasureSpec.getSize(i2);
        int maxHeight = getMaxHeight();
        i4 = i2;
        if (maxHeight != Integer.MAX_VALUE) {
            if (maxHeight >= size2) {
                i4 = i2;
            }
            i4 = View.MeasureSpec.makeMeasureSpec(maxHeight, Integer.MIN_VALUE);
        }
        super.onMeasure(i3, i4);
    }

    @Override // android.widget.ImageView
    public void setMaxHeight(int i) {
        this.mMaxHeight = i;
        super.setMaxHeight(i);
    }

    @Override // android.widget.ImageView
    public void setMaxWidth(int i) {
        this.mMaxWidth = i;
        super.setMaxWidth(i);
    }
}

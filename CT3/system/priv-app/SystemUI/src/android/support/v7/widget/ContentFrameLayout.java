package android.support.v7.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
/* loaded from: a.zip:android/support/v7/widget/ContentFrameLayout.class */
public class ContentFrameLayout extends FrameLayout {
    private OnAttachListener mAttachListener;
    private final Rect mDecorPadding;
    private TypedValue mFixedHeightMajor;
    private TypedValue mFixedHeightMinor;
    private TypedValue mFixedWidthMajor;
    private TypedValue mFixedWidthMinor;
    private TypedValue mMinWidthMajor;
    private TypedValue mMinWidthMinor;

    /* loaded from: a.zip:android/support/v7/widget/ContentFrameLayout$OnAttachListener.class */
    public interface OnAttachListener {
        void onAttachedFromWindow();

        void onDetachedFromWindow();
    }

    public ContentFrameLayout(Context context) {
        this(context, null);
    }

    public ContentFrameLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ContentFrameLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mDecorPadding = new Rect();
    }

    public void dispatchFitSystemWindows(Rect rect) {
        fitSystemWindows(rect);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mAttachListener != null) {
            this.mAttachListener.onAttachedFromWindow();
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mAttachListener != null) {
            this.mAttachListener.onDetachedFromWindow();
        }
    }

    @Override // android.widget.FrameLayout, android.view.View
    protected void onMeasure(int i, int i2) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        boolean z = displayMetrics.widthPixels < displayMetrics.heightPixels;
        int mode = View.MeasureSpec.getMode(i);
        int mode2 = View.MeasureSpec.getMode(i2);
        boolean z2 = false;
        int i3 = i;
        if (mode == Integer.MIN_VALUE) {
            TypedValue typedValue = z ? this.mFixedWidthMinor : this.mFixedWidthMajor;
            z2 = false;
            i3 = i;
            if (typedValue != null) {
                z2 = false;
                i3 = i;
                if (typedValue.type != 0) {
                    int i4 = 0;
                    if (typedValue.type == 5) {
                        i4 = (int) typedValue.getDimension(displayMetrics);
                    } else if (typedValue.type == 6) {
                        i4 = (int) typedValue.getFraction(displayMetrics.widthPixels, displayMetrics.widthPixels);
                    }
                    z2 = false;
                    i3 = i;
                    if (i4 > 0) {
                        i3 = View.MeasureSpec.makeMeasureSpec(Math.min(i4 - (this.mDecorPadding.left + this.mDecorPadding.right), View.MeasureSpec.getSize(i)), 1073741824);
                        z2 = true;
                    }
                }
            }
        }
        int i5 = i2;
        if (mode2 == Integer.MIN_VALUE) {
            TypedValue typedValue2 = z ? this.mFixedHeightMajor : this.mFixedHeightMinor;
            i5 = i2;
            if (typedValue2 != null) {
                i5 = i2;
                if (typedValue2.type != 0) {
                    int i6 = 0;
                    if (typedValue2.type == 5) {
                        i6 = (int) typedValue2.getDimension(displayMetrics);
                    } else if (typedValue2.type == 6) {
                        i6 = (int) typedValue2.getFraction(displayMetrics.heightPixels, displayMetrics.heightPixels);
                    }
                    i5 = i2;
                    if (i6 > 0) {
                        i5 = View.MeasureSpec.makeMeasureSpec(Math.min(i6 - (this.mDecorPadding.top + this.mDecorPadding.bottom), View.MeasureSpec.getSize(i2)), 1073741824);
                    }
                }
            }
        }
        super.onMeasure(i3, i5);
        int measuredWidth = getMeasuredWidth();
        int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(measuredWidth, 1073741824);
        boolean z3 = false;
        int i7 = makeMeasureSpec;
        if (!z2) {
            z3 = false;
            i7 = makeMeasureSpec;
            if (mode == Integer.MIN_VALUE) {
                TypedValue typedValue3 = z ? this.mMinWidthMinor : this.mMinWidthMajor;
                z3 = false;
                i7 = makeMeasureSpec;
                if (typedValue3 != null) {
                    z3 = false;
                    i7 = makeMeasureSpec;
                    if (typedValue3.type != 0) {
                        int i8 = 0;
                        if (typedValue3.type == 5) {
                            i8 = (int) typedValue3.getDimension(displayMetrics);
                        } else if (typedValue3.type == 6) {
                            i8 = (int) typedValue3.getFraction(displayMetrics.widthPixels, displayMetrics.widthPixels);
                        }
                        int i9 = i8;
                        if (i8 > 0) {
                            i9 = i8 - (this.mDecorPadding.left + this.mDecorPadding.right);
                        }
                        z3 = false;
                        i7 = makeMeasureSpec;
                        if (measuredWidth < i9) {
                            i7 = View.MeasureSpec.makeMeasureSpec(i9, 1073741824);
                            z3 = true;
                        }
                    }
                }
            }
        }
        if (z3) {
            super.onMeasure(i7, i5);
        }
    }
}

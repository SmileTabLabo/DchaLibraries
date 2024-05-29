package android.support.v17.leanback.widget.picker;
/* loaded from: a.zip:android/support/v17/leanback/widget/picker/PickerColumn.class */
public class PickerColumn {
    private int mCurrentValue;
    private String mLabelFormat;
    private int mMaxValue;
    private int mMinValue;
    private CharSequence[] mStaticLabels;

    public int getCount() {
        return (this.mMaxValue - this.mMinValue) + 1;
    }

    public int getCurrentValue() {
        return this.mCurrentValue;
    }

    public CharSequence getLabelFor(int i) {
        return this.mStaticLabels == null ? String.format(this.mLabelFormat, Integer.valueOf(i)) : this.mStaticLabels[i];
    }

    public int getMaxValue() {
        return this.mMaxValue;
    }

    public int getMinValue() {
        return this.mMinValue;
    }

    public void setCurrentValue(int i) {
        this.mCurrentValue = i;
    }

    public void setLabelFormat(String str) {
        this.mLabelFormat = str;
    }

    public void setMaxValue(int i) {
        this.mMaxValue = i;
    }

    public void setMinValue(int i) {
        this.mMinValue = i;
    }

    public void setStaticLabels(CharSequence[] charSequenceArr) {
        this.mStaticLabels = charSequenceArr;
    }
}

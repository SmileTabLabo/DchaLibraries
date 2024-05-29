package android.support.v17.leanback.widget.picker;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v17.leanback.R$styleable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
/* loaded from: a.zip:android/support/v17/leanback/widget/picker/DatePicker.class */
public class DatePicker extends Picker {
    private static int[] DATE_FIELDS = {5, 2, 1};
    int mColDayIndex;
    int mColMonthIndex;
    int mColYearIndex;
    PickerConstant mConstant;
    Calendar mCurrentDate;
    final DateFormat mDateFormat;
    private String mDatePickerFormat;
    PickerColumn mDayColumn;
    Calendar mMaxDate;
    Calendar mMinDate;
    PickerColumn mMonthColumn;
    Calendar mTempDate;
    PickerColumn mYearColumn;

    public DatePicker(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public DatePicker(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        updateCurrentLocale();
        setSeparator(this.mConstant.dateSeparator);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.lbDatePicker);
        String string = obtainStyledAttributes.getString(R$styleable.lbDatePicker_android_minDate);
        String string2 = obtainStyledAttributes.getString(R$styleable.lbDatePicker_android_maxDate);
        this.mTempDate.clear();
        if (TextUtils.isEmpty(string)) {
            this.mTempDate.set(1900, 0, 1);
        } else if (!parseDate(string, this.mTempDate)) {
            this.mTempDate.set(1900, 0, 1);
        }
        this.mMinDate.setTimeInMillis(this.mTempDate.getTimeInMillis());
        this.mTempDate.clear();
        if (TextUtils.isEmpty(string2)) {
            this.mTempDate.set(2100, 0, 1);
        } else if (!parseDate(string2, this.mTempDate)) {
            this.mTempDate.set(2100, 0, 1);
        }
        this.mMaxDate.setTimeInMillis(this.mTempDate.getTimeInMillis());
        String string3 = obtainStyledAttributes.getString(R$styleable.lbDatePicker_datePickerFormat);
        setDatePickerFormat(TextUtils.isEmpty(string3) ? new String(android.text.format.DateFormat.getDateFormatOrder(context)) : string3);
    }

    private Calendar getCalendarForLocale(Calendar calendar, Locale locale) {
        if (calendar == null) {
            return Calendar.getInstance(locale);
        }
        long timeInMillis = calendar.getTimeInMillis();
        Calendar calendar2 = Calendar.getInstance(locale);
        calendar2.setTimeInMillis(timeInMillis);
        return calendar2;
    }

    private boolean parseDate(String str, Calendar calendar) {
        try {
            calendar.setTime(this.mDateFormat.parse(str));
            return true;
        } catch (ParseException e) {
            Log.w("DatePicker", "Date: " + str + " not in format: MM/dd/yyyy");
            return false;
        }
    }

    private void setDate(int i, int i2, int i3) {
        this.mCurrentDate.set(i, i2, i3);
        if (this.mCurrentDate.before(this.mMinDate)) {
            this.mCurrentDate.setTimeInMillis(this.mMinDate.getTimeInMillis());
        } else if (this.mCurrentDate.after(this.mMaxDate)) {
            this.mCurrentDate.setTimeInMillis(this.mMaxDate.getTimeInMillis());
        }
    }

    private void updateCurrentLocale() {
        this.mConstant = new PickerConstant(Locale.getDefault(), getContext().getResources());
        this.mTempDate = getCalendarForLocale(this.mTempDate, this.mConstant.locale);
        this.mMinDate = getCalendarForLocale(this.mMinDate, this.mConstant.locale);
        this.mMaxDate = getCalendarForLocale(this.mMaxDate, this.mConstant.locale);
        this.mCurrentDate = getCalendarForLocale(this.mCurrentDate, this.mConstant.locale);
        if (this.mMonthColumn != null) {
            this.mMonthColumn.setStaticLabels(this.mConstant.months);
            setColumnAt(this.mColMonthIndex, this.mMonthColumn);
        }
    }

    private static boolean updateMax(PickerColumn pickerColumn, int i) {
        if (i != pickerColumn.getMaxValue()) {
            pickerColumn.setMaxValue(i);
            return true;
        }
        return false;
    }

    private static boolean updateMin(PickerColumn pickerColumn, int i) {
        if (i != pickerColumn.getMinValue()) {
            pickerColumn.setMinValue(i);
            return true;
        }
        return false;
    }

    private void updateSpinners(boolean z) {
        post(new Runnable(this, z) { // from class: android.support.v17.leanback.widget.picker.DatePicker.1
            final DatePicker this$0;
            final boolean val$animation;

            {
                this.this$0 = this;
                this.val$animation = z;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.updateSpinnersImpl(this.val$animation);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateSpinnersImpl(boolean z) {
        int[] iArr = {this.mColDayIndex, this.mColMonthIndex, this.mColYearIndex};
        boolean z2 = true;
        boolean z3 = true;
        for (int length = DATE_FIELDS.length - 1; length >= 0; length--) {
            if (iArr[length] >= 0) {
                int i = DATE_FIELDS[length];
                PickerColumn columnAt = getColumnAt(iArr[length]);
                boolean updateMin = z2 ? updateMin(columnAt, this.mMinDate.get(i)) : updateMin(columnAt, this.mCurrentDate.getActualMinimum(i));
                boolean updateMax = z3 ? updateMin | updateMax(columnAt, this.mMaxDate.get(i)) : updateMin | updateMax(columnAt, this.mCurrentDate.getActualMaximum(i));
                boolean z4 = z2 & (this.mCurrentDate.get(i) == this.mMinDate.get(i));
                z3 &= this.mCurrentDate.get(i) == this.mMaxDate.get(i);
                if (updateMax) {
                    setColumnAt(iArr[length], columnAt);
                }
                setColumnValue(iArr[length], this.mCurrentDate.get(i), z);
                z2 = z4;
            }
        }
    }

    @Override // android.support.v17.leanback.widget.picker.Picker
    public final void onColumnValueChanged(int i, int i2) {
        this.mTempDate.setTimeInMillis(this.mCurrentDate.getTimeInMillis());
        int currentValue = getColumnAt(i).getCurrentValue();
        if (i == this.mColDayIndex) {
            this.mTempDate.add(5, i2 - currentValue);
        } else if (i == this.mColMonthIndex) {
            this.mTempDate.add(2, i2 - currentValue);
        } else if (i != this.mColYearIndex) {
            throw new IllegalArgumentException();
        } else {
            this.mTempDate.add(1, i2 - currentValue);
        }
        setDate(this.mTempDate.get(1), this.mTempDate.get(2), this.mTempDate.get(5));
        updateSpinners(false);
    }

    public void setDatePickerFormat(String str) {
        String str2 = str;
        if (TextUtils.isEmpty(str)) {
            str2 = new String(android.text.format.DateFormat.getDateFormatOrder(getContext()));
        }
        String upperCase = str2.toUpperCase();
        if (TextUtils.equals(this.mDatePickerFormat, upperCase)) {
            return;
        }
        this.mDatePickerFormat = upperCase;
        this.mDayColumn = null;
        this.mMonthColumn = null;
        this.mYearColumn = null;
        this.mColMonthIndex = -1;
        this.mColDayIndex = -1;
        this.mColYearIndex = -1;
        ArrayList arrayList = new ArrayList(3);
        for (int i = 0; i < upperCase.length(); i++) {
            switch (upperCase.charAt(i)) {
                case 'D':
                    if (this.mDayColumn != null) {
                        throw new IllegalArgumentException("datePicker format error");
                    }
                    PickerColumn pickerColumn = new PickerColumn();
                    this.mDayColumn = pickerColumn;
                    arrayList.add(pickerColumn);
                    this.mDayColumn.setLabelFormat("%02d");
                    this.mColDayIndex = i;
                    break;
                case 'M':
                    if (this.mMonthColumn != null) {
                        throw new IllegalArgumentException("datePicker format error");
                    }
                    PickerColumn pickerColumn2 = new PickerColumn();
                    this.mMonthColumn = pickerColumn2;
                    arrayList.add(pickerColumn2);
                    this.mMonthColumn.setStaticLabels(this.mConstant.months);
                    this.mColMonthIndex = i;
                    break;
                case 'Y':
                    if (this.mYearColumn != null) {
                        throw new IllegalArgumentException("datePicker format error");
                    }
                    PickerColumn pickerColumn3 = new PickerColumn();
                    this.mYearColumn = pickerColumn3;
                    arrayList.add(pickerColumn3);
                    this.mColYearIndex = i;
                    this.mYearColumn.setLabelFormat("%d");
                    break;
                default:
                    throw new IllegalArgumentException("datePicker format error");
            }
        }
        setColumns(arrayList);
        updateSpinners(false);
    }
}

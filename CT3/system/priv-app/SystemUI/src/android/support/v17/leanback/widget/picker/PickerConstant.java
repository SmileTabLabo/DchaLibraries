package android.support.v17.leanback.widget.picker;

import android.content.res.Resources;
import android.support.v17.leanback.R$string;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Locale;
/* loaded from: a.zip:android/support/v17/leanback/widget/picker/PickerConstant.class */
class PickerConstant {
    public final String[] ampm;
    public final String dateSeparator;
    public final String[] days;
    public final String[] hours12;
    public final String[] hours24;
    public final Locale locale;
    public final String[] minutes;
    public final String[] months;
    public final String timeSeparator;

    public PickerConstant(Locale locale, Resources resources) {
        this.locale = locale;
        DateFormatSymbols dateFormatSymbols = DateFormatSymbols.getInstance(locale);
        this.months = dateFormatSymbols.getShortMonths();
        Calendar calendar = Calendar.getInstance(locale);
        this.days = createStringIntArrays(calendar.getMinimum(5), calendar.getMaximum(5), "%02d");
        this.hours12 = createStringIntArrays(1, 12, "%02d");
        this.hours24 = createStringIntArrays(0, 23, "%02d");
        this.minutes = createStringIntArrays(0, 59, "%02d");
        this.ampm = dateFormatSymbols.getAmPmStrings();
        this.dateSeparator = resources.getString(R$string.lb_date_separator);
        this.timeSeparator = resources.getString(R$string.lb_time_separator);
    }

    public static String[] createStringIntArrays(int i, int i2, String str) {
        String[] strArr = new String[(i2 - i) + 1];
        for (int i3 = i; i3 <= i2; i3++) {
            if (str != null) {
                strArr[i3 - i] = String.format(str, Integer.valueOf(i3));
            } else {
                strArr[i3 - i] = String.valueOf(i3);
            }
        }
        return strArr;
    }
}

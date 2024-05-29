package com.android.ex.editstyledtext;

import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.util.Log;
/* loaded from: b.zip:com/android/ex/editstyledtext/EditStyledText$EditStyledTextSpans$MarqueeSpan.class */
public class EditStyledText$EditStyledTextSpans$MarqueeSpan extends CharacterStyle {
    private int mMarqueeColor;
    private int mType;

    private int getMarqueeColor(int i, int i2) {
        int i3;
        int i4;
        int alpha = Color.alpha(i2);
        int red = Color.red(i2);
        int green = Color.green(i2);
        int blue = Color.blue(i2);
        int i5 = alpha;
        if (alpha == 0) {
            i5 = 128;
        }
        switch (i) {
            case 0:
                if (red <= 128) {
                    i4 = (255 - red) / 2;
                    i3 = green;
                    break;
                } else {
                    i4 = red / 2;
                    i3 = green;
                    break;
                }
            case 1:
                if (green <= 128) {
                    i3 = (255 - green) / 2;
                    i4 = red;
                    break;
                } else {
                    i3 = green / 2;
                    i4 = red;
                    break;
                }
            case 2:
                return 16777215;
            default:
                Log.e("EditStyledText", "--- getMarqueeColor: got illigal marquee ID.");
                return 16777215;
        }
        return Color.argb(i5, i4, i3, blue);
    }

    public void resetColor(int i) {
        this.mMarqueeColor = getMarqueeColor(this.mType, i);
    }

    @Override // android.text.style.CharacterStyle
    public void updateDrawState(TextPaint textPaint) {
        textPaint.bgColor = this.mMarqueeColor;
    }
}

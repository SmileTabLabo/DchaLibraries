package com.android.ex.editstyledtext;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
/* loaded from: b.zip:com/android/ex/editstyledtext/EditStyledText$EditStyledTextSpans$HorizontalLineDrawable.class */
public class EditStyledText$EditStyledTextSpans$HorizontalLineDrawable extends ShapeDrawable {
    private static boolean DBG_HL = false;
    private Spannable mSpannable;
    private int mWidth;

    private EditStyledText$EditStyledTextSpans$HorizontalLineSpan getParentSpan() {
        Spannable spannable = this.mSpannable;
        EditStyledText$EditStyledTextSpans$HorizontalLineSpan[] editStyledText$EditStyledTextSpans$HorizontalLineSpanArr = (EditStyledText$EditStyledTextSpans$HorizontalLineSpan[]) spannable.getSpans(0, spannable.length(), EditStyledText$EditStyledTextSpans$HorizontalLineSpan.class);
        if (editStyledText$EditStyledTextSpans$HorizontalLineSpanArr.length > 0) {
            for (EditStyledText$EditStyledTextSpans$HorizontalLineSpan editStyledText$EditStyledTextSpans$HorizontalLineSpan : editStyledText$EditStyledTextSpans$HorizontalLineSpanArr) {
                if (editStyledText$EditStyledTextSpans$HorizontalLineSpan.getDrawable() == this) {
                    return editStyledText$EditStyledTextSpans$HorizontalLineSpan;
                }
            }
        }
        Log.e("EditStyledTextSpan", "---renewBounds: Couldn't find");
        return null;
    }

    private void renewColor() {
        EditStyledText$EditStyledTextSpans$HorizontalLineSpan parentSpan = getParentSpan();
        Spannable spannable = this.mSpannable;
        ForegroundColorSpan[] foregroundColorSpanArr = (ForegroundColorSpan[]) spannable.getSpans(spannable.getSpanStart(parentSpan), spannable.getSpanEnd(parentSpan), ForegroundColorSpan.class);
        if (DBG_HL) {
            Log.d("EditStyledTextSpan", "--- renewColor:" + foregroundColorSpanArr.length);
        }
        if (foregroundColorSpanArr.length > 0) {
            renewColor(foregroundColorSpanArr[foregroundColorSpanArr.length - 1].getForegroundColor());
        }
    }

    private void renewColor(int i) {
        if (DBG_HL) {
            Log.d("EditStyledTextSpan", "--- renewColor:" + i);
        }
        getPaint().setColor(i);
    }

    @Override // android.graphics.drawable.ShapeDrawable, android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        renewColor();
        canvas.drawRect(new Rect(0, 9, this.mWidth, 11), getPaint());
    }

    public void renewBounds(int i) {
        if (DBG_HL) {
            Log.d("EditStyledTextSpan", "--- renewBounds:" + i);
        }
        int i2 = i;
        if (i > 20) {
            i2 = i - 20;
        }
        this.mWidth = i2;
        setBounds(0, 0, i2, 20);
    }
}

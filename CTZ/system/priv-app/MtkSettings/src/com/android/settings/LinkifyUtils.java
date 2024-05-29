package com.android.settings;

import android.text.Spannable;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;
/* loaded from: classes.dex */
public class LinkifyUtils {

    /* loaded from: classes.dex */
    public interface OnClickListener {
        void onClick();
    }

    public static boolean linkify(TextView textView, StringBuilder sb, final OnClickListener onClickListener) {
        int indexOf = sb.indexOf("LINK_BEGIN");
        if (indexOf == -1) {
            textView.setText(sb);
            return false;
        }
        sb.delete(indexOf, "LINK_BEGIN".length() + indexOf);
        int indexOf2 = sb.indexOf("LINK_END");
        if (indexOf2 == -1) {
            textView.setText(sb);
            return false;
        }
        sb.delete(indexOf2, "LINK_END".length() + indexOf2);
        textView.setText(sb.toString(), TextView.BufferType.SPANNABLE);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        ((Spannable) textView.getText()).setSpan(new ClickableSpan() { // from class: com.android.settings.LinkifyUtils.1
            @Override // android.text.style.ClickableSpan
            public void onClick(View view) {
                OnClickListener.this.onClick();
            }

            @Override // android.text.style.ClickableSpan, android.text.style.CharacterStyle
            public void updateDrawState(TextPaint textPaint) {
                super.updateDrawState(textPaint);
                textPaint.setUnderlineText(false);
            }
        }, indexOf, indexOf2, 33);
        return true;
    }
}

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

    private LinkifyUtils() {
    }

    public static boolean linkify(TextView textView, StringBuilder text, final OnClickListener listener) {
        int beginIndex = text.indexOf("LINK_BEGIN");
        if (beginIndex == -1) {
            textView.setText(text);
            return false;
        }
        text.delete(beginIndex, "LINK_BEGIN".length() + beginIndex);
        int endIndex = text.indexOf("LINK_END");
        if (endIndex == -1) {
            textView.setText(text);
            return false;
        }
        text.delete(endIndex, "LINK_END".length() + endIndex);
        textView.setText(text.toString(), TextView.BufferType.SPANNABLE);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        Spannable spannableContent = (Spannable) textView.getText();
        ClickableSpan spannableLink = new ClickableSpan() { // from class: com.android.settings.LinkifyUtils.1
            @Override // android.text.style.ClickableSpan
            public void onClick(View widget) {
                OnClickListener.this.onClick();
            }

            @Override // android.text.style.ClickableSpan, android.text.style.CharacterStyle
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };
        spannableContent.setSpan(spannableLink, beginIndex, endIndex, 33);
        return true;
    }
}

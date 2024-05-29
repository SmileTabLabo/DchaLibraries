package com.android.setupwizardlib.span;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
/* loaded from: classes.dex */
public class LinkSpan extends ClickableSpan {
    private static final Typeface TYPEFACE_MEDIUM = Typeface.create("sans-serif-medium", 0);
    private final String mId;

    @Deprecated
    /* loaded from: classes.dex */
    public interface OnClickListener {
        void onClick(LinkSpan linkSpan);
    }

    /* loaded from: classes.dex */
    public interface OnLinkClickListener {
        boolean onLinkClick(LinkSpan linkSpan);
    }

    public LinkSpan(String str) {
        this.mId = str;
    }

    @Override // android.text.style.ClickableSpan
    public void onClick(View view) {
        if (dispatchClick(view)) {
            if (Build.VERSION.SDK_INT >= 19) {
                view.cancelPendingInputEvents();
            }
        } else {
            Log.w("LinkSpan", "Dropping click event. No listener attached.");
        }
        if (view instanceof TextView) {
            CharSequence text = ((TextView) view).getText();
            if (text instanceof Spannable) {
                Selection.setSelection((Spannable) text, 0);
            }
        }
    }

    private boolean dispatchClick(View view) {
        boolean z;
        OnClickListener legacyListenerFromContext;
        if (view instanceof OnLinkClickListener) {
            z = ((OnLinkClickListener) view).onLinkClick(this);
        } else {
            z = false;
        }
        if (!z && (legacyListenerFromContext = getLegacyListenerFromContext(view.getContext())) != null) {
            legacyListenerFromContext.onClick(this);
            return true;
        }
        return z;
    }

    @Deprecated
    private OnClickListener getLegacyListenerFromContext(Context context) {
        while (!(context instanceof OnClickListener)) {
            if (context instanceof ContextWrapper) {
                context = ((ContextWrapper) context).getBaseContext();
            } else {
                return null;
            }
        }
        return (OnClickListener) context;
    }

    @Override // android.text.style.ClickableSpan, android.text.style.CharacterStyle
    public void updateDrawState(TextPaint textPaint) {
        super.updateDrawState(textPaint);
        textPaint.setUnderlineText(false);
        textPaint.setTypeface(TYPEFACE_MEDIUM);
    }

    public String getId() {
        return this.mId;
    }
}

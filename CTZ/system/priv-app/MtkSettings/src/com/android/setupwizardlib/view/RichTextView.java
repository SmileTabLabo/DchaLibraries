package com.android.setupwizardlib.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.AppCompatTextView;
import android.text.Annotation;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;
import com.android.setupwizardlib.span.LinkSpan;
import com.android.setupwizardlib.span.SpanHelper;
import com.android.setupwizardlib.util.LinkAccessibilityHelper;
import com.android.setupwizardlib.view.TouchableMovementMethod;
/* loaded from: classes.dex */
public class RichTextView extends AppCompatTextView implements LinkSpan.OnLinkClickListener {
    private LinkAccessibilityHelper mAccessibilityHelper;
    private LinkSpan.OnLinkClickListener mOnLinkClickListener;

    public static CharSequence getRichText(Context context, CharSequence charSequence) {
        Annotation[] annotationArr;
        if (charSequence instanceof Spanned) {
            SpannableString spannableString = new SpannableString(charSequence);
            for (Annotation annotation : (Annotation[]) spannableString.getSpans(0, spannableString.length(), Annotation.class)) {
                String key = annotation.getKey();
                if ("textAppearance".equals(key)) {
                    int identifier = context.getResources().getIdentifier(annotation.getValue(), "style", context.getPackageName());
                    if (identifier == 0) {
                        Log.w("RichTextView", "Cannot find resource: " + identifier);
                    }
                    SpanHelper.replaceSpan(spannableString, annotation, new TextAppearanceSpan(context, identifier));
                } else if ("link".equals(key)) {
                    SpanHelper.replaceSpan(spannableString, annotation, new LinkSpan(annotation.getValue()));
                }
            }
            return spannableString;
        }
        return charSequence;
    }

    public RichTextView(Context context) {
        super(context);
        init();
    }

    public RichTextView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    private void init() {
        this.mAccessibilityHelper = new LinkAccessibilityHelper(this);
        ViewCompat.setAccessibilityDelegate(this, this.mAccessibilityHelper);
    }

    @Override // android.widget.TextView
    public void setText(CharSequence charSequence, TextView.BufferType bufferType) {
        CharSequence richText = getRichText(getContext(), charSequence);
        super.setText(richText, bufferType);
        boolean hasLinks = hasLinks(richText);
        if (hasLinks) {
            setMovementMethod(TouchableMovementMethod.TouchableLinkMovementMethod.getInstance());
        } else {
            setMovementMethod(null);
        }
        setFocusable(hasLinks);
        if (Build.VERSION.SDK_INT >= 25) {
            setRevealOnFocusHint(false);
            setFocusableInTouchMode(hasLinks);
        }
    }

    private boolean hasLinks(CharSequence charSequence) {
        return (charSequence instanceof Spanned) && ((ClickableSpan[]) ((Spanned) charSequence).getSpans(0, charSequence.length(), ClickableSpan.class)).length > 0;
    }

    @Override // android.widget.TextView, android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        boolean onTouchEvent = super.onTouchEvent(motionEvent);
        MovementMethod movementMethod = getMovementMethod();
        if (movementMethod instanceof TouchableMovementMethod) {
            TouchableMovementMethod touchableMovementMethod = (TouchableMovementMethod) movementMethod;
            if (touchableMovementMethod.getLastTouchEvent() == motionEvent) {
                return touchableMovementMethod.isLastTouchEventHandled();
            }
        }
        return onTouchEvent;
    }

    @Override // android.view.View
    protected boolean dispatchHoverEvent(MotionEvent motionEvent) {
        if (this.mAccessibilityHelper != null && this.mAccessibilityHelper.dispatchHoverEvent(motionEvent)) {
            return true;
        }
        return super.dispatchHoverEvent(motionEvent);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.widget.AppCompatTextView, android.widget.TextView, android.view.View
    public void drawableStateChanged() {
        Drawable[] compoundDrawablesRelative;
        super.drawableStateChanged();
        if (Build.VERSION.SDK_INT >= 17) {
            int[] drawableState = getDrawableState();
            for (Drawable drawable : getCompoundDrawablesRelative()) {
                if (drawable != null && drawable.setState(drawableState)) {
                    invalidateDrawable(drawable);
                }
            }
        }
    }

    public void setOnLinkClickListener(LinkSpan.OnLinkClickListener onLinkClickListener) {
        this.mOnLinkClickListener = onLinkClickListener;
    }

    public LinkSpan.OnLinkClickListener getOnLinkClickListener() {
        return this.mOnLinkClickListener;
    }

    @Override // com.android.setupwizardlib.span.LinkSpan.OnLinkClickListener
    public boolean onLinkClick(LinkSpan linkSpan) {
        if (this.mOnLinkClickListener != null) {
            return this.mOnLinkClickListener.onLinkClick(linkSpan);
        }
        return false;
    }
}

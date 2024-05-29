package android.support.v17.leanback.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.EditText;
import android.widget.TextView;
/* loaded from: a.zip:android/support/v17/leanback/widget/GuidedActionEditText.class */
public class GuidedActionEditText extends EditText {
    private ImeKeyMonitor$ImeKeyListener mKeyListener;
    private final Drawable mNoPaddingDrawable;
    private final Drawable mSavedBackground;

    /* loaded from: a.zip:android/support/v17/leanback/widget/GuidedActionEditText$NoPaddingDrawable.class */
    static final class NoPaddingDrawable extends Drawable {
        NoPaddingDrawable() {
        }

        @Override // android.graphics.drawable.Drawable
        public void draw(Canvas canvas) {
        }

        @Override // android.graphics.drawable.Drawable
        public int getOpacity() {
            return -2;
        }

        @Override // android.graphics.drawable.Drawable
        public boolean getPadding(Rect rect) {
            rect.set(0, 0, 0, 0);
            return true;
        }

        @Override // android.graphics.drawable.Drawable
        public void setAlpha(int i) {
        }

        @Override // android.graphics.drawable.Drawable
        public void setColorFilter(ColorFilter colorFilter) {
        }
    }

    public GuidedActionEditText(Context context) {
        this(context, null);
    }

    public GuidedActionEditText(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 16842862);
    }

    public GuidedActionEditText(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mSavedBackground = getBackground();
        this.mNoPaddingDrawable = new NoPaddingDrawable();
        setBackground(this.mNoPaddingDrawable);
    }

    @Override // android.widget.TextView, android.view.View
    protected void onFocusChanged(boolean z, int i, Rect rect) {
        super.onFocusChanged(z, i, rect);
        if (z) {
            setBackground(this.mSavedBackground);
        } else {
            setBackground(this.mNoPaddingDrawable);
        }
        if (z) {
            return;
        }
        setFocusable(false);
    }

    @Override // android.view.View
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        accessibilityNodeInfo.setClassName(isFocused() ? EditText.class.getName() : TextView.class.getName());
    }

    @Override // android.widget.TextView, android.view.View
    public boolean onKeyPreIme(int i, KeyEvent keyEvent) {
        boolean z = false;
        if (this.mKeyListener != null) {
            z = this.mKeyListener.onKeyPreIme(this, i, keyEvent);
        }
        boolean z2 = z;
        if (!z) {
            z2 = super.onKeyPreIme(i, keyEvent);
        }
        return z2;
    }
}

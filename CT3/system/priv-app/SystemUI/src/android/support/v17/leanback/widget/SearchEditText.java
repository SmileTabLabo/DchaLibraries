package android.support.v17.leanback.widget;

import android.content.Context;
import android.support.v17.leanback.R$style;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityNodeInfo;
/* loaded from: a.zip:android/support/v17/leanback/widget/SearchEditText.class */
public class SearchEditText extends StreamingTextView {
    private static final String TAG = SearchEditText.class.getSimpleName();
    private OnKeyboardDismissListener mKeyboardDismissListener;

    /* loaded from: a.zip:android/support/v17/leanback/widget/SearchEditText$OnKeyboardDismissListener.class */
    public interface OnKeyboardDismissListener {
        void onKeyboardDismiss();
    }

    public SearchEditText(Context context) {
        this(context, null);
    }

    public SearchEditText(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, R$style.TextAppearance_Leanback_SearchTextEdit);
    }

    public SearchEditText(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    @Override // android.support.v17.leanback.widget.StreamingTextView, android.view.View
    public /* bridge */ /* synthetic */ void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
    }

    @Override // android.widget.TextView, android.view.View
    public boolean onKeyPreIme(int i, KeyEvent keyEvent) {
        if (keyEvent.getKeyCode() == 4) {
            this.mKeyboardDismissListener.onKeyboardDismiss();
            return false;
        }
        return super.onKeyPreIme(i, keyEvent);
    }

    @Override // android.support.v17.leanback.widget.StreamingTextView
    public /* bridge */ /* synthetic */ void reset() {
        super.reset();
    }

    public void setOnKeyboardDismissListener(OnKeyboardDismissListener onKeyboardDismissListener) {
        this.mKeyboardDismissListener = onKeyboardDismissListener;
    }

    @Override // android.support.v17.leanback.widget.StreamingTextView
    public /* bridge */ /* synthetic */ void updateRecognizedText(String str, String str2) {
        super.updateRecognizedText(str, str2);
    }
}

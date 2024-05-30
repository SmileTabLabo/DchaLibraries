package android.support.v17.leanback.widget;

import android.content.Context;
import android.support.v17.leanback.R;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityNodeInfo;
/* loaded from: classes.dex */
public class SearchEditText extends StreamingTextView {
    private static final String TAG = SearchEditText.class.getSimpleName();
    private OnKeyboardDismissListener mKeyboardDismissListener;

    /* loaded from: classes.dex */
    public interface OnKeyboardDismissListener {
        void onKeyboardDismiss();
    }

    @Override // android.support.v17.leanback.widget.StreamingTextView, android.view.View
    public /* bridge */ /* synthetic */ void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
    }

    @Override // android.support.v17.leanback.widget.StreamingTextView
    public /* bridge */ /* synthetic */ void reset() {
        super.reset();
    }

    @Override // android.support.v17.leanback.widget.StreamingTextView, android.widget.TextView
    public /* bridge */ /* synthetic */ void setCustomSelectionActionModeCallback(ActionMode.Callback callback) {
        super.setCustomSelectionActionModeCallback(callback);
    }

    @Override // android.support.v17.leanback.widget.StreamingTextView
    public /* bridge */ /* synthetic */ void updateRecognizedText(String str, String str2) {
        super.updateRecognizedText(str, str2);
    }

    public SearchEditText(Context context) {
        this(context, null);
    }

    public SearchEditText(Context context, AttributeSet attrs) {
        this(context, attrs, R.style.TextAppearance_Leanback_SearchTextEdit);
    }

    public SearchEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override // android.widget.TextView, android.view.View
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == 4) {
            if (this.mKeyboardDismissListener != null) {
                this.mKeyboardDismissListener.onKeyboardDismiss();
                return false;
            }
            return false;
        }
        return super.onKeyPreIme(keyCode, event);
    }

    public void setOnKeyboardDismissListener(OnKeyboardDismissListener listener) {
        this.mKeyboardDismissListener = listener;
    }
}

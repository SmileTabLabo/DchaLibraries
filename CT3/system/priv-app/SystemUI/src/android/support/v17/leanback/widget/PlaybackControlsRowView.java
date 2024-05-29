package android.support.v17.leanback.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
/* loaded from: a.zip:android/support/v17/leanback/widget/PlaybackControlsRowView.class */
class PlaybackControlsRowView extends LinearLayout {
    private OnUnhandledKeyListener mOnUnhandledKeyListener;

    /* loaded from: a.zip:android/support/v17/leanback/widget/PlaybackControlsRowView$OnUnhandledKeyListener.class */
    public interface OnUnhandledKeyListener {
        boolean onUnhandledKey(KeyEvent keyEvent);
    }

    public PlaybackControlsRowView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public PlaybackControlsRowView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        if (super.dispatchKeyEvent(keyEvent)) {
            return true;
        }
        return this.mOnUnhandledKeyListener != null && this.mOnUnhandledKeyListener.onUnhandledKey(keyEvent);
    }

    @Override // android.view.ViewGroup
    protected boolean onRequestFocusInDescendants(int i, Rect rect) {
        View findFocus = findFocus();
        if (findFocus == null || !findFocus.requestFocus(i, rect)) {
            return super.onRequestFocusInDescendants(i, rect);
        }
        return true;
    }
}

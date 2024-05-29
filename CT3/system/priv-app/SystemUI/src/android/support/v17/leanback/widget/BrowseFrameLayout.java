package android.support.v17.leanback.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
/* loaded from: a.zip:android/support/v17/leanback/widget/BrowseFrameLayout.class */
public class BrowseFrameLayout extends FrameLayout {
    private OnFocusSearchListener mListener;
    private OnChildFocusListener mOnChildFocusListener;

    /* loaded from: a.zip:android/support/v17/leanback/widget/BrowseFrameLayout$OnChildFocusListener.class */
    public interface OnChildFocusListener {
        void onRequestChildFocus(View view, View view2);

        boolean onRequestFocusInDescendants(int i, Rect rect);
    }

    /* loaded from: a.zip:android/support/v17/leanback/widget/BrowseFrameLayout$OnFocusSearchListener.class */
    public interface OnFocusSearchListener {
        View onFocusSearch(View view, int i);
    }

    public BrowseFrameLayout(Context context) {
        this(context, null, 0);
    }

    public BrowseFrameLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public BrowseFrameLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public View focusSearch(View view, int i) {
        View onFocusSearch;
        return (this.mListener == null || (onFocusSearch = this.mListener.onFocusSearch(view, i)) == null) ? super.focusSearch(view, i) : onFocusSearch;
    }

    @Override // android.view.ViewGroup
    protected boolean onRequestFocusInDescendants(int i, Rect rect) {
        return this.mOnChildFocusListener != null ? this.mOnChildFocusListener.onRequestFocusInDescendants(i, rect) : super.onRequestFocusInDescendants(i, rect);
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public void requestChildFocus(View view, View view2) {
        super.requestChildFocus(view, view2);
        if (this.mOnChildFocusListener != null) {
            this.mOnChildFocusListener.onRequestChildFocus(view, view2);
        }
    }
}

package android.support.v17.leanback.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import java.util.ArrayList;
/* loaded from: classes.dex */
public class NonOverlappingLinearLayout extends LinearLayout {
    boolean mDeferFocusableViewAvailableInLayout;
    boolean mFocusableViewAvailableFixEnabled;
    final ArrayList<ArrayList<View>> mSortedAvailableViews;

    public NonOverlappingLinearLayout(Context context) {
        this(context, null);
    }

    public NonOverlappingLinearLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NonOverlappingLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mFocusableViewAvailableFixEnabled = false;
        this.mSortedAvailableViews = new ArrayList<>();
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v0, types: [boolean] */
    /* JADX WARN: Type inference failed for: r0v1, types: [int] */
    /* JADX WARN: Type inference failed for: r0v6 */
    @Override // android.widget.LinearLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        boolean z;
        int size;
        ?? r0 = 0;
        int i = 0;
        try {
            this.mDeferFocusableViewAvailableInLayout = this.mFocusableViewAvailableFixEnabled && getOrientation() == 0 && getLayoutDirection() == 1;
            if (this.mDeferFocusableViewAvailableInLayout) {
                while (this.mSortedAvailableViews.size() > getChildCount()) {
                    this.mSortedAvailableViews.remove(this.mSortedAvailableViews.size() - 1);
                }
                while (this.mSortedAvailableViews.size() < getChildCount()) {
                    this.mSortedAvailableViews.add(new ArrayList<>());
                }
            }
            super.onLayout(changed, l, t, r, b);
            if (this.mDeferFocusableViewAvailableInLayout) {
                for (int i2 = 0; i2 < this.mSortedAvailableViews.size(); i2++) {
                    for (int j = 0; j < this.mSortedAvailableViews.get(i2).size(); j++) {
                        super.focusableViewAvailable(this.mSortedAvailableViews.get(i2).get(j));
                    }
                }
            }
            if (!z) {
                return;
            }
            while (true) {
                if (i >= size) {
                    return;
                }
            }
        } finally {
            if (this.mDeferFocusableViewAvailableInLayout) {
                this.mDeferFocusableViewAvailableInLayout = false;
                while (r0 < this.mSortedAvailableViews.size()) {
                    this.mSortedAvailableViews.get(r0).clear();
                    r0++;
                }
            }
        }
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public void focusableViewAvailable(View v) {
        if (this.mDeferFocusableViewAvailableInLayout) {
            View i = v;
            int index = -1;
            while (true) {
                if (i == this || i == null) {
                    break;
                } else if (i.getParent() == this) {
                    index = indexOfChild(i);
                    break;
                } else {
                    i = (View) i.getParent();
                }
            }
            if (index != -1) {
                this.mSortedAvailableViews.get(index).add(v);
                return;
            }
            return;
        }
        super.focusableViewAvailable(v);
    }
}

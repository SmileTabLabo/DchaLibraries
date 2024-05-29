package android.support.v7.preference;

import android.support.annotation.IdRes;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
/* loaded from: a.zip:android/support/v7/preference/PreferenceViewHolder.class */
public class PreferenceViewHolder extends RecyclerView.ViewHolder {
    private final SparseArray<View> mCachedViews;
    private boolean mDividerAllowedAbove;
    private boolean mDividerAllowedBelow;

    /* JADX INFO: Access modifiers changed from: package-private */
    public PreferenceViewHolder(View view) {
        super(view);
        this.mCachedViews = new SparseArray<>(4);
        this.mCachedViews.put(16908310, view.findViewById(16908310));
        this.mCachedViews.put(16908304, view.findViewById(16908304));
        this.mCachedViews.put(16908294, view.findViewById(16908294));
        this.mCachedViews.put(R$id.icon_frame, view.findViewById(R$id.icon_frame));
        this.mCachedViews.put(16908350, view.findViewById(16908350));
    }

    public View findViewById(@IdRes int i) {
        View view = this.mCachedViews.get(i);
        if (view != null) {
            return view;
        }
        View findViewById = this.itemView.findViewById(i);
        if (findViewById != null) {
            this.mCachedViews.put(i, findViewById);
        }
        return findViewById;
    }

    public boolean isDividerAllowedAbove() {
        return this.mDividerAllowedAbove;
    }

    public boolean isDividerAllowedBelow() {
        return this.mDividerAllowedBelow;
    }

    public void setDividerAllowedAbove(boolean z) {
        this.mDividerAllowedAbove = z;
    }

    public void setDividerAllowedBelow(boolean z) {
        this.mDividerAllowedBelow = z;
    }
}

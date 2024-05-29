package com.android.settings.dashboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import com.android.settings.R;
/* loaded from: classes.dex */
public class DashboardDecorator extends RecyclerView.ItemDecoration {
    private final Context mContext;
    private final Drawable mDivider;

    public DashboardDecorator(Context context) {
        this.mContext = context;
        TypedValue value = new TypedValue();
        this.mContext.getTheme().resolveAttribute(16843284, value, true);
        this.mDivider = this.mContext.getDrawable(value.resourceId);
    }

    @Override // android.support.v7.widget.RecyclerView.ItemDecoration
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int i;
        int childCount = parent.getChildCount();
        while (i < childCount) {
            View child = parent.getChildAt(i);
            RecyclerView.ViewHolder holder = parent.getChildViewHolder(child);
            if (holder.getItemViewType() == R.layout.dashboard_category) {
                i = parent.getChildViewHolder(parent.getChildAt(i + (-1))).getItemViewType() != R.layout.dashboard_tile ? i + 1 : 1;
                int top = getChildTop(child);
                this.mDivider.setBounds(child.getLeft(), top, child.getRight(), this.mDivider.getIntrinsicHeight() + top);
                this.mDivider.draw(c);
            } else {
                if (holder.getItemViewType() != R.layout.condition_card) {
                }
                int top2 = getChildTop(child);
                this.mDivider.setBounds(child.getLeft(), top2, child.getRight(), this.mDivider.getIntrinsicHeight() + top2);
                this.mDivider.draw(c);
            }
        }
    }

    private int getChildTop(View child) {
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
        return child.getTop() + params.topMargin + Math.round(ViewCompat.getTranslationY(child));
    }
}

package android.support.v7.widget.helper;

import android.graphics.Canvas;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
/* loaded from: classes.dex */
class ItemTouchUIUtilImpl$Honeycomb implements ItemTouchUIUtil {
    @Override // android.support.v7.widget.helper.ItemTouchUIUtil
    public void clearView(View view) {
        ViewCompat.setTranslationX(view, 0.0f);
        ViewCompat.setTranslationY(view, 0.0f);
    }

    @Override // android.support.v7.widget.helper.ItemTouchUIUtil
    public void onSelected(View view) {
    }

    @Override // android.support.v7.widget.helper.ItemTouchUIUtil
    public void onDraw(Canvas c, RecyclerView recyclerView, View view, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        ViewCompat.setTranslationX(view, dX);
        ViewCompat.setTranslationY(view, dY);
    }

    @Override // android.support.v7.widget.helper.ItemTouchUIUtil
    public void onDrawOver(Canvas c, RecyclerView recyclerView, View view, float dX, float dY, int actionState, boolean isCurrentlyActive) {
    }
}

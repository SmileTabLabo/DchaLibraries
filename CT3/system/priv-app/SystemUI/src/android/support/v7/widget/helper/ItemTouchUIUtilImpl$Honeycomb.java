package android.support.v7.widget.helper;

import android.graphics.Canvas;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
/* loaded from: a.zip:android/support/v7/widget/helper/ItemTouchUIUtilImpl$Honeycomb.class */
class ItemTouchUIUtilImpl$Honeycomb implements ItemTouchUIUtil {
    @Override // android.support.v7.widget.helper.ItemTouchUIUtil
    public void clearView(View view) {
        ViewCompat.setTranslationX(view, 0.0f);
        ViewCompat.setTranslationY(view, 0.0f);
    }

    @Override // android.support.v7.widget.helper.ItemTouchUIUtil
    public void onDraw(Canvas canvas, RecyclerView recyclerView, View view, float f, float f2, int i, boolean z) {
        ViewCompat.setTranslationX(view, f);
        ViewCompat.setTranslationY(view, f2);
    }

    @Override // android.support.v7.widget.helper.ItemTouchUIUtil
    public void onDrawOver(Canvas canvas, RecyclerView recyclerView, View view, float f, float f2, int i, boolean z) {
    }

    @Override // android.support.v7.widget.helper.ItemTouchUIUtil
    public void onSelected(View view) {
    }
}

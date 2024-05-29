package android.support.v17.leanback.widget;

import android.graphics.Rect;
import android.support.v17.leanback.widget.GridLayoutManager;
import android.support.v17.leanback.widget.ItemAlignmentFacet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: a.zip:android/support/v17/leanback/widget/ItemAlignmentFacetHelper.class */
public class ItemAlignmentFacetHelper {
    private static Rect sRect = new Rect();

    ItemAlignmentFacetHelper() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int getAlignmentPosition(View view, ItemAlignmentFacet.ItemAlignmentDef itemAlignmentDef, int i) {
        int i2;
        int i3;
        int i4;
        GridLayoutManager.LayoutParams layoutParams = (GridLayoutManager.LayoutParams) view.getLayoutParams();
        View view2 = view;
        if (itemAlignmentDef.mViewId != 0) {
            View findViewById = view.findViewById(itemAlignmentDef.mViewId);
            view2 = findViewById;
            if (findViewById == null) {
                view2 = view;
            }
        }
        int i5 = itemAlignmentDef.mOffset;
        if (i == 0) {
            if (itemAlignmentDef.mOffset >= 0) {
                i4 = i5;
                if (itemAlignmentDef.mOffsetWithPadding) {
                    i4 = i5 + view2.getPaddingLeft();
                }
            } else {
                i4 = i5;
                if (itemAlignmentDef.mOffsetWithPadding) {
                    i4 = i5 - view2.getPaddingRight();
                }
            }
            int i6 = i4;
            if (itemAlignmentDef.mOffsetPercent != -1.0f) {
                i6 = (int) ((((view2 == view ? layoutParams.getOpticalWidth(view2) : view2.getWidth()) * itemAlignmentDef.mOffsetPercent) / 100.0f) + i4);
            }
            i3 = i6;
            if (view != view2) {
                sRect.left = i6;
                ((ViewGroup) view).offsetDescendantRectToMyCoords(view2, sRect);
                i3 = sRect.left - layoutParams.getOpticalLeftInset();
            }
        } else {
            if (itemAlignmentDef.mOffset >= 0) {
                i2 = i5;
                if (itemAlignmentDef.mOffsetWithPadding) {
                    i2 = i5 + view2.getPaddingTop();
                }
            } else {
                i2 = i5;
                if (itemAlignmentDef.mOffsetWithPadding) {
                    i2 = i5 - view2.getPaddingBottom();
                }
            }
            int i7 = i2;
            if (itemAlignmentDef.mOffsetPercent != -1.0f) {
                i7 = (int) ((((view2 == view ? layoutParams.getOpticalHeight(view2) : view2.getHeight()) * itemAlignmentDef.mOffsetPercent) / 100.0f) + i2);
            }
            int i8 = i7;
            if (view != view2) {
                sRect.top = i7;
                ((ViewGroup) view).offsetDescendantRectToMyCoords(view2, sRect);
                i8 = sRect.top - layoutParams.getOpticalTopInset();
            }
            i3 = i8;
            if (view2 instanceof TextView) {
                i3 = i8;
                if (itemAlignmentDef.isAlignedToTextViewBaseLine()) {
                    i3 = i8 + (-((TextView) view2).getPaint().getFontMetricsInt().top);
                }
            }
        }
        return i3;
    }
}

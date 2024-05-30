package android.support.v17.leanback.widget;

import android.support.v17.leanback.widget.ItemAlignmentFacet;
import android.view.View;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class ItemAlignment {
    private int mOrientation = 0;
    public final Axis vertical = new Axis(1);
    public final Axis horizontal = new Axis(0);
    private Axis mMainAxis = this.horizontal;
    private Axis mSecondAxis = this.vertical;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static final class Axis extends ItemAlignmentFacet.ItemAlignmentDef {
        private int mOrientation;

        Axis(int orientation) {
            this.mOrientation = orientation;
        }

        public int getAlignmentPosition(View itemView) {
            return ItemAlignmentFacetHelper.getAlignmentPosition(itemView, this, this.mOrientation);
        }
    }

    public final void setOrientation(int orientation) {
        this.mOrientation = orientation;
        if (this.mOrientation == 0) {
            this.mMainAxis = this.horizontal;
            this.mSecondAxis = this.vertical;
            return;
        }
        this.mMainAxis = this.vertical;
        this.mSecondAxis = this.horizontal;
    }
}

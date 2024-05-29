package android.support.v17.leanback.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v17.leanback.R$id;
import android.support.v17.leanback.R$layout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
/* loaded from: a.zip:android/support/v17/leanback/widget/RowContainerView.class */
final class RowContainerView extends LinearLayout {
    private Drawable mForeground;
    private boolean mForegroundBoundsChanged;
    private ViewGroup mHeaderDock;

    public RowContainerView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public RowContainerView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mForegroundBoundsChanged = true;
        setOrientation(1);
        LayoutInflater.from(context).inflate(R$layout.lb_row_container, this);
        this.mHeaderDock = (ViewGroup) findViewById(R$id.lb_row_container_header_dock);
        setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
    }

    @Override // android.view.View
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (this.mForeground != null) {
            if (this.mForegroundBoundsChanged) {
                this.mForegroundBoundsChanged = false;
                this.mForeground.setBounds(0, 0, getWidth(), getHeight());
            }
            this.mForeground.draw(canvas);
        }
    }

    @Override // android.view.View
    public Drawable getForeground() {
        return this.mForeground;
    }

    @Override // android.view.View
    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        this.mForegroundBoundsChanged = true;
    }

    @Override // android.view.View
    public void setForeground(Drawable drawable) {
        this.mForeground = drawable;
        setWillNotDraw(this.mForeground == null);
        invalidate();
    }
}

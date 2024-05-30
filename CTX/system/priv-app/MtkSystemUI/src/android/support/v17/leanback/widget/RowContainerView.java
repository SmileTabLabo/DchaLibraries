package android.support.v17.leanback.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v17.leanback.R;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
/* loaded from: classes.dex */
final class RowContainerView extends LinearLayout {
    private Drawable mForeground;
    private boolean mForegroundBoundsChanged;
    private ViewGroup mHeaderDock;

    public RowContainerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RowContainerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mForegroundBoundsChanged = true;
        setOrientation(1);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.lb_row_container, this);
        this.mHeaderDock = (ViewGroup) findViewById(R.id.lb_row_container_header_dock);
        setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
    }

    @Override // android.view.View
    public void setForeground(Drawable d) {
        this.mForeground = d;
        setWillNotDraw(this.mForeground == null);
        invalidate();
    }

    @Override // android.view.View
    public Drawable getForeground() {
        return this.mForeground;
    }

    @Override // android.view.View
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mForegroundBoundsChanged = true;
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
    public boolean hasOverlappingRendering() {
        return false;
    }
}

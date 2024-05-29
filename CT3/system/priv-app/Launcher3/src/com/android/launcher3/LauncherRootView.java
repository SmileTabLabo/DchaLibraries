package com.android.launcher3;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
/* loaded from: a.zip:com/android/launcher3/LauncherRootView.class */
public class LauncherRootView extends InsettableFrameLayout {
    private View mAlignedView;
    private boolean mDrawRightInsetBar;
    private final Paint mOpaquePaint;
    private int mRightInsetBarWidth;

    public LauncherRootView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mOpaquePaint = new Paint(1);
        this.mOpaquePaint.setColor(-16777216);
        this.mOpaquePaint.setStyle(Paint.Style.FILL);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (this.mDrawRightInsetBar) {
            int width = getWidth();
            canvas.drawRect(width - this.mRightInsetBarWidth, 0.0f, width, getHeight(), this.mOpaquePaint);
        }
    }

    @Override // android.view.View
    @TargetApi(23)
    protected boolean fitSystemWindows(Rect rect) {
        this.mDrawRightInsetBar = rect.right > 0 ? Utilities.ATLEAST_MARSHMALLOW ? ((ActivityManager) getContext().getSystemService(ActivityManager.class)).isLowRamDevice() : true : false;
        this.mRightInsetBarWidth = rect.right;
        setInsets(this.mDrawRightInsetBar ? new Rect(0, rect.top, 0, rect.bottom) : rect);
        if (this.mAlignedView == null || !this.mDrawRightInsetBar) {
            return true;
        }
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) this.mAlignedView.getLayoutParams();
        if (marginLayoutParams.leftMargin == rect.left && marginLayoutParams.rightMargin == rect.right) {
            return true;
        }
        marginLayoutParams.leftMargin = rect.left;
        marginLayoutParams.rightMargin = rect.right;
        this.mAlignedView.setLayoutParams(marginLayoutParams);
        return true;
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        if (getChildCount() > 0) {
            this.mAlignedView = getChildAt(0);
        }
        super.onFinishInflate();
    }
}

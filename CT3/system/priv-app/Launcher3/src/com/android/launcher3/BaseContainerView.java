package com.android.launcher3;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
/* loaded from: a.zip:com/android/launcher3/BaseContainerView.class */
public abstract class BaseContainerView extends FrameLayout implements Insettable {
    private final int mContainerBoundsInset;
    private View mContent;
    protected final Rect mContentPadding;
    protected final int mHorizontalPadding;
    private final Rect mInsets;
    private final Drawable mRevealDrawable;
    private View mRevealView;

    public BaseContainerView(Context context) {
        this(context, null);
    }

    public BaseContainerView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public BaseContainerView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mInsets = new Rect();
        this.mContentPadding = new Rect();
        this.mContainerBoundsInset = getResources().getDimensionPixelSize(2131230755);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.BaseContainerView, i, 0);
        this.mRevealDrawable = obtainStyledAttributes.getDrawable(0);
        obtainStyledAttributes.recycle();
        int dimensionPixelSize = getResources().getDimensionPixelSize(2131230754);
        int dimensionPixelSize2 = getResources().getDimensionPixelSize(2131230753);
        int i2 = ((Launcher) context).getDeviceProfile().availableWidthPx;
        if (dimensionPixelSize > 0) {
            this.mHorizontalPadding = Math.max(dimensionPixelSize2, (i2 - dimensionPixelSize) / 2);
        } else {
            this.mHorizontalPadding = Math.max(dimensionPixelSize2, (int) getResources().getFraction(2131689472, i2, 1));
        }
    }

    private void onUpdateBackgroundAndPaddings(Rect rect) {
        setPadding(0, rect.top, 0, rect.bottom);
        InsetDrawable insetDrawable = new InsetDrawable(this.mRevealDrawable, rect.left, 0, rect.right, 0);
        this.mRevealView.setBackground(insetDrawable.getConstantState().newDrawable());
        this.mContent.setBackground(insetDrawable);
        this.mContent.setPadding(0, 0, 0, 0);
        Rect rect2 = new Rect();
        insetDrawable.getPadding(rect2);
        onUpdateBgPadding(rect, rect2);
    }

    public final View getContentView() {
        return this.mContent;
    }

    public final View getRevealView() {
        return this.mRevealView;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mContent = findViewById(2131296275);
        this.mRevealView = findViewById(2131296274);
    }

    protected abstract void onUpdateBgPadding(Rect rect, Rect rect2);

    @Override // com.android.launcher3.Insettable
    public final void setInsets(Rect rect) {
        this.mInsets.set(rect);
        updateBackgroundAndPaddings();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void updateBackgroundAndPaddings() {
        Rect rect = new Rect(this.mHorizontalPadding, this.mInsets.top + this.mContainerBoundsInset, this.mHorizontalPadding, this.mInsets.bottom + this.mContainerBoundsInset);
        if (rect.equals(this.mContentPadding)) {
            return;
        }
        this.mContentPadding.set(rect);
        onUpdateBackgroundAndPaddings(rect);
    }
}

package com.android.systemui.qs;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.android.systemui.qs.QSTile;
import java.util.Objects;
/* loaded from: a.zip:com/android/systemui/qs/QSIconView.class */
public class QSIconView extends ViewGroup {
    private boolean mAnimationEnabled;
    protected final View mIcon;
    protected final int mIconSizePx;
    protected final int mTilePaddingBelowIconPx;

    public QSIconView(Context context) {
        super(context);
        this.mAnimationEnabled = true;
        Resources resources = context.getResources();
        this.mIconSizePx = resources.getDimensionPixelSize(2131689835);
        this.mTilePaddingBelowIconPx = resources.getDimensionPixelSize(2131689844);
        this.mIcon = createIcon();
        addView(this.mIcon);
    }

    protected View createIcon() {
        ImageView imageView = new ImageView(this.mContext);
        imageView.setId(16908294);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        return imageView;
    }

    public void disableAnimation() {
        this.mAnimationEnabled = false;
    }

    protected final int exactly(int i) {
        return View.MeasureSpec.makeMeasureSpec(i, 1073741824);
    }

    protected int getIconMeasureMode() {
        return 1073741824;
    }

    public View getIconView() {
        return this.mIcon;
    }

    protected final void layout(View view, int i, int i2) {
        view.layout(i, i2, view.getMeasuredWidth() + i, view.getMeasuredHeight() + i2);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.ViewGroup, android.view.View
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int measuredWidth = getMeasuredWidth();
        getMeasuredHeight();
        layout(this.mIcon, (measuredWidth - this.mIcon.getMeasuredWidth()) / 2, 0);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.View
    public void onMeasure(int i, int i2) {
        int size = View.MeasureSpec.getSize(i);
        this.mIcon.measure(View.MeasureSpec.makeMeasureSpec(size, getIconMeasureMode()), exactly(this.mIconSizePx));
        setMeasuredDimension(size, this.mIcon.getMeasuredHeight() + this.mTilePaddingBelowIconPx);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setIcon(ImageView imageView, QSTile.State state) {
        if (!Objects.equals(state.icon, imageView.getTag(2131886127))) {
            Drawable drawable = state.icon != null ? (imageView.isShown() && this.mAnimationEnabled) ? state.icon.getDrawable(this.mContext) : state.icon.getInvisibleDrawable(this.mContext) : null;
            int padding = state.icon != null ? state.icon.getPadding() : 0;
            if (drawable != null && state.autoMirrorDrawable) {
                drawable.setAutoMirrored(true);
            }
            imageView.setImageDrawable(drawable);
            imageView.setTag(2131886127, state.icon);
            imageView.setPadding(0, padding, 0, padding);
            if ((drawable instanceof Animatable) && imageView.isShown()) {
                Animatable animatable = (Animatable) drawable;
                animatable.start();
                if (!imageView.isShown()) {
                    animatable.stop();
                }
            }
        }
        if (state.disabledByPolicy) {
            imageView.setColorFilter(getContext().getColor(2131558535));
        } else {
            imageView.clearColorFilter();
        }
    }

    public void setIcon(QSTile.State state) {
        setIcon((ImageView) this.mIcon, state);
    }
}

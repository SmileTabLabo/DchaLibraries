package com.android.systemui.qs.tileimpl;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import com.android.systemui.R;
import com.android.systemui.plugins.qs.QSIconView;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.AlphaControlledSignalTileView;
import java.util.Objects;
/* loaded from: classes.dex */
public class QSIconViewImpl extends QSIconView {
    private boolean mAnimationEnabled;
    protected final View mIcon;
    protected final int mIconSizePx;
    private int mState;
    protected final int mTilePaddingBelowIconPx;
    private int mTint;

    public QSIconViewImpl(Context context) {
        super(context);
        this.mAnimationEnabled = true;
        this.mState = -1;
        Resources resources = context.getResources();
        this.mIconSizePx = resources.getDimensionPixelSize(R.dimen.qs_tile_icon_size);
        this.mTilePaddingBelowIconPx = resources.getDimensionPixelSize(R.dimen.qs_tile_padding_below_icon);
        this.mIcon = createIcon();
        addView(this.mIcon);
    }

    @Override // com.android.systemui.plugins.qs.QSIconView
    public void disableAnimation() {
        this.mAnimationEnabled = false;
    }

    @Override // com.android.systemui.plugins.qs.QSIconView
    public View getIconView() {
        return this.mIcon;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.View
    public void onMeasure(int i, int i2) {
        int size = View.MeasureSpec.getSize(i);
        this.mIcon.measure(View.MeasureSpec.makeMeasureSpec(size, getIconMeasureMode()), exactly(this.mIconSizePx));
        setMeasuredDimension(size, this.mIcon.getMeasuredHeight() + this.mTilePaddingBelowIconPx);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.ViewGroup, android.view.View
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        layout(this.mIcon, (getMeasuredWidth() - this.mIcon.getMeasuredWidth()) / 2, 0);
    }

    @Override // com.android.systemui.plugins.qs.QSIconView
    public void setIcon(QSTile.State state) {
        setIcon((ImageView) this.mIcon, state);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void updateIcon(ImageView imageView, QSTile.State state) {
        Drawable drawable;
        QSTile.Icon icon = state.iconSupplier != null ? state.iconSupplier.get() : state.icon;
        if (!Objects.equals(icon, imageView.getTag(R.id.qs_icon_tag)) || !Objects.equals(state.slash, imageView.getTag(R.id.qs_slash_tag))) {
            boolean z = imageView.isShown() && this.mAnimationEnabled && imageView.getDrawable() != null;
            if (icon != null) {
                drawable = z ? icon.getDrawable(this.mContext) : icon.getInvisibleDrawable(this.mContext);
            } else {
                drawable = null;
            }
            int padding = icon != null ? icon.getPadding() : 0;
            if (drawable != null) {
                drawable.setAutoMirrored(false);
                drawable.setLayoutDirection(getLayoutDirection());
            }
            if (imageView instanceof SlashImageView) {
                SlashImageView slashImageView = (SlashImageView) imageView;
                slashImageView.setAnimationEnabled(z);
                slashImageView.setState(null, drawable);
            } else {
                imageView.setImageDrawable(drawable);
            }
            imageView.setTag(R.id.qs_icon_tag, icon);
            imageView.setTag(R.id.qs_slash_tag, state.slash);
            imageView.setPadding(0, padding, 0, padding);
            if (drawable instanceof Animatable2) {
                final Animatable2 animatable2 = (Animatable2) drawable;
                animatable2.start();
                if (state.isTransient) {
                    animatable2.registerAnimationCallback(new Animatable2.AnimationCallback() { // from class: com.android.systemui.qs.tileimpl.QSIconViewImpl.1
                        @Override // android.graphics.drawable.Animatable2.AnimationCallback
                        public void onAnimationEnd(Drawable drawable2) {
                            animatable2.start();
                        }
                    });
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setIcon(final ImageView imageView, final QSTile.State state) {
        if (state.disabledByPolicy) {
            imageView.setColorFilter(getContext().getColor(R.color.qs_tile_disabled_color));
        } else {
            imageView.clearColorFilter();
        }
        if (state.state != this.mState) {
            int color = getColor(state.state);
            this.mState = state.state;
            if (imageView.isShown() && this.mTint != 0) {
                animateGrayScale(this.mTint, color, imageView, new Runnable() { // from class: com.android.systemui.qs.tileimpl.-$$Lambda$QSIconViewImpl$J1UpdvvSiFAmzZuy0PTr_V3YTn0
                    @Override // java.lang.Runnable
                    public final void run() {
                        QSIconViewImpl.this.updateIcon(imageView, state);
                    }
                });
                this.mTint = color;
                return;
            }
            if (imageView instanceof AlphaControlledSignalTileView.AlphaControlledSlashImageView) {
                ((AlphaControlledSignalTileView.AlphaControlledSlashImageView) imageView).setFinalImageTintList(ColorStateList.valueOf(color));
            } else {
                setTint(imageView, color);
            }
            this.mTint = color;
            updateIcon(imageView, state);
            return;
        }
        updateIcon(imageView, state);
    }

    protected int getColor(int i) {
        return QSTileImpl.getColorForState(getContext(), i);
    }

    private void animateGrayScale(int i, int i2, final ImageView imageView, final Runnable runnable) {
        if (imageView instanceof AlphaControlledSignalTileView.AlphaControlledSlashImageView) {
            ((AlphaControlledSignalTileView.AlphaControlledSlashImageView) imageView).setFinalImageTintList(ColorStateList.valueOf(i2));
        }
        if (this.mAnimationEnabled && ValueAnimator.areAnimatorsEnabled()) {
            final float alpha = Color.alpha(i);
            final float alpha2 = Color.alpha(i2);
            final float red = Color.red(i);
            final float red2 = Color.red(i2);
            ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
            ofFloat.setDuration(350L);
            ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.qs.tileimpl.-$$Lambda$QSIconViewImpl$CeqSBPdIhNYTow_6QM6a9ZwQyb8
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    QSIconViewImpl.lambda$animateGrayScale$1(alpha, alpha2, red, red2, imageView, valueAnimator);
                }
            });
            ofFloat.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.qs.tileimpl.QSIconViewImpl.2
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    runnable.run();
                }
            });
            ofFloat.start();
            return;
        }
        setTint(imageView, i2);
        runnable.run();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ void lambda$animateGrayScale$1(float f, float f2, float f3, float f4, ImageView imageView, ValueAnimator valueAnimator) {
        float animatedFraction = valueAnimator.getAnimatedFraction();
        int i = (int) (f3 + ((f4 - f3) * animatedFraction));
        setTint(imageView, Color.argb((int) (f + ((f2 - f) * animatedFraction)), i, i, i));
    }

    public static void setTint(ImageView imageView, int i) {
        imageView.setImageTintList(ColorStateList.valueOf(i));
    }

    protected int getIconMeasureMode() {
        return 1073741824;
    }

    protected View createIcon() {
        SlashImageView slashImageView = new SlashImageView(this.mContext);
        slashImageView.setId(16908294);
        slashImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        return slashImageView;
    }

    protected final int exactly(int i) {
        return View.MeasureSpec.makeMeasureSpec(i, 1073741824);
    }

    protected final void layout(View view, int i, int i2) {
        view.layout(i, i2, view.getMeasuredWidth() + i, view.getMeasuredHeight() + i2);
    }
}

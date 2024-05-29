package android.support.v17.leanback.widget;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.ColorInt;
import android.support.v17.leanback.R$attr;
import android.support.v17.leanback.R$color;
import android.support.v17.leanback.R$dimen;
import android.support.v17.leanback.R$drawable;
import android.support.v17.leanback.R$fraction;
import android.support.v17.leanback.R$id;
import android.support.v17.leanback.R$integer;
import android.support.v17.leanback.R$layout;
import android.support.v17.leanback.R$styleable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
/* loaded from: a.zip:android/support/v17/leanback/widget/SearchOrbView.class */
public class SearchOrbView extends FrameLayout implements View.OnClickListener {
    private boolean mAttachedToWindow;
    private boolean mColorAnimationEnabled;
    private ValueAnimator mColorAnimator;
    private final ArgbEvaluator mColorEvaluator;
    private Colors mColors;
    private final ValueAnimator.AnimatorUpdateListener mFocusUpdateListener;
    private final float mFocusedZ;
    private final float mFocusedZoom;
    private ImageView mIcon;
    private Drawable mIconDrawable;
    private View.OnClickListener mListener;
    private final int mPulseDurationMs;
    private View mRootView;
    private final int mScaleDurationMs;
    private View mSearchOrbView;
    private ValueAnimator mShadowFocusAnimator;
    private final float mUnfocusedZ;
    private final ValueAnimator.AnimatorUpdateListener mUpdateListener;

    /* loaded from: a.zip:android/support/v17/leanback/widget/SearchOrbView$Colors.class */
    public static class Colors {
        @ColorInt
        public int brightColor;
        @ColorInt
        public int color;
        @ColorInt
        public int iconColor;

        public Colors(@ColorInt int i, @ColorInt int i2, @ColorInt int i3) {
            this.color = i;
            this.brightColor = i2 == i ? getBrightColor(i) : i2;
            this.iconColor = i3;
        }

        public static int getBrightColor(int i) {
            return Color.argb((int) ((Color.alpha(i) * 0.85f) + 38.25f), (int) ((Color.red(i) * 0.85f) + 38.25f), (int) ((Color.green(i) * 0.85f) + 38.25f), (int) ((Color.blue(i) * 0.85f) + 38.25f));
        }
    }

    public SearchOrbView(Context context) {
        this(context, null);
    }

    public SearchOrbView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, R$attr.searchOrbViewStyle);
    }

    public SearchOrbView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mColorEvaluator = new ArgbEvaluator();
        this.mUpdateListener = new ValueAnimator.AnimatorUpdateListener(this) { // from class: android.support.v17.leanback.widget.SearchOrbView.1
            final SearchOrbView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.this$0.setOrbViewColor(((Integer) valueAnimator.getAnimatedValue()).intValue());
            }
        };
        this.mFocusUpdateListener = new ValueAnimator.AnimatorUpdateListener(this) { // from class: android.support.v17.leanback.widget.SearchOrbView.2
            final SearchOrbView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.this$0.setSearchOrbZ(valueAnimator.getAnimatedFraction());
            }
        };
        Resources resources = context.getResources();
        this.mRootView = ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(getLayoutResourceId(), (ViewGroup) this, true);
        this.mSearchOrbView = this.mRootView.findViewById(R$id.search_orb);
        this.mIcon = (ImageView) this.mRootView.findViewById(R$id.icon);
        this.mFocusedZoom = context.getResources().getFraction(R$fraction.lb_search_orb_focused_zoom, 1, 1);
        this.mPulseDurationMs = context.getResources().getInteger(R$integer.lb_search_orb_pulse_duration_ms);
        this.mScaleDurationMs = context.getResources().getInteger(R$integer.lb_search_orb_scale_duration_ms);
        this.mFocusedZ = context.getResources().getDimensionPixelSize(R$dimen.lb_search_orb_focused_z);
        this.mUnfocusedZ = context.getResources().getDimensionPixelSize(R$dimen.lb_search_orb_unfocused_z);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.lbSearchOrbView, i, 0);
        Drawable drawable = obtainStyledAttributes.getDrawable(R$styleable.lbSearchOrbView_searchOrbIcon);
        setOrbIcon(drawable == null ? resources.getDrawable(R$drawable.lb_ic_in_app_search) : drawable);
        int color = obtainStyledAttributes.getColor(R$styleable.lbSearchOrbView_searchOrbColor, resources.getColor(R$color.lb_default_search_color));
        setOrbColors(new Colors(color, obtainStyledAttributes.getColor(R$styleable.lbSearchOrbView_searchOrbBrightColor, color), obtainStyledAttributes.getColor(R$styleable.lbSearchOrbView_searchOrbIconColor, 0)));
        obtainStyledAttributes.recycle();
        setFocusable(true);
        setClipChildren(false);
        setOnClickListener(this);
        setSoundEffectsEnabled(false);
        setSearchOrbZ(0.0f);
        ShadowHelper.getInstance().setZ(this.mIcon, this.mFocusedZ);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setOrbViewColor(int i) {
        if (this.mSearchOrbView.getBackground() instanceof GradientDrawable) {
            ((GradientDrawable) this.mSearchOrbView.getBackground()).setColor(i);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setSearchOrbZ(float f) {
        ShadowHelper.getInstance().setZ(this.mSearchOrbView, this.mUnfocusedZ + ((this.mFocusedZ - this.mUnfocusedZ) * f));
    }

    private void startShadowFocusAnimation(boolean z, int i) {
        if (this.mShadowFocusAnimator == null) {
            this.mShadowFocusAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
            this.mShadowFocusAnimator.addUpdateListener(this.mFocusUpdateListener);
        }
        if (z) {
            this.mShadowFocusAnimator.start();
        } else {
            this.mShadowFocusAnimator.reverse();
        }
        this.mShadowFocusAnimator.setDuration(i);
    }

    private void updateColorAnimator() {
        if (this.mColorAnimator != null) {
            this.mColorAnimator.end();
            this.mColorAnimator = null;
        }
        if (this.mColorAnimationEnabled && this.mAttachedToWindow) {
            this.mColorAnimator = ValueAnimator.ofObject(this.mColorEvaluator, Integer.valueOf(this.mColors.color), Integer.valueOf(this.mColors.brightColor), Integer.valueOf(this.mColors.color));
            this.mColorAnimator.setRepeatCount(-1);
            this.mColorAnimator.setDuration(this.mPulseDurationMs * 2);
            this.mColorAnimator.addUpdateListener(this.mUpdateListener);
            this.mColorAnimator.start();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void animateOnFocus(boolean z) {
        float f = z ? this.mFocusedZoom : 1.0f;
        this.mRootView.animate().scaleX(f).scaleY(f).setDuration(this.mScaleDurationMs).start();
        startShadowFocusAnimation(z, this.mScaleDurationMs);
        enableOrbColorAnimation(z);
    }

    public void enableOrbColorAnimation(boolean z) {
        this.mColorAnimationEnabled = z;
        updateColorAnimator();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public float getFocusedZoom() {
        return this.mFocusedZoom;
    }

    int getLayoutResourceId() {
        return R$layout.lb_search_orb;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mAttachedToWindow = true;
        updateColorAnimator();
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (this.mListener != null) {
            this.mListener.onClick(view);
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        this.mAttachedToWindow = false;
        updateColorAnimator();
        super.onDetachedFromWindow();
    }

    @Override // android.view.View
    protected void onFocusChanged(boolean z, int i, Rect rect) {
        super.onFocusChanged(z, i, rect);
        animateOnFocus(z);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void scaleOrbViewOnly(float f) {
        this.mSearchOrbView.setScaleX(f);
        this.mSearchOrbView.setScaleY(f);
    }

    public void setOnOrbClickedListener(View.OnClickListener onClickListener) {
        this.mListener = onClickListener;
    }

    public void setOrbColors(Colors colors) {
        this.mColors = colors;
        this.mIcon.setColorFilter(this.mColors.iconColor);
        if (this.mColorAnimator == null) {
            setOrbViewColor(this.mColors.color);
        } else {
            enableOrbColorAnimation(true);
        }
    }

    public void setOrbIcon(Drawable drawable) {
        this.mIconDrawable = drawable;
        this.mIcon.setImageDrawable(this.mIconDrawable);
    }
}

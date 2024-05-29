package com.android.systemui.qs;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.android.systemui.qs.QSTile;
/* loaded from: a.zip:com/android/systemui/qs/SignalTileView.class */
public final class SignalTileView extends QSIconView {
    private static final long DEFAULT_DURATION = new ValueAnimator().getDuration();
    private static final long SHORT_DURATION = DEFAULT_DURATION / 3;
    private FrameLayout mIconFrame;
    private ImageView mIn;
    private ImageView mOut;
    private ImageView mOverlay;
    private ImageView mSignal;
    private int mWideOverlayIconStartPadding;

    public SignalTileView(Context context) {
        super(context);
        this.mIn = addTrafficView(2130837749);
        this.mOut = addTrafficView(2130837752);
        this.mWideOverlayIconStartPadding = context.getResources().getDimensionPixelSize(2131689949);
    }

    private ImageView addTrafficView(int i) {
        ImageView imageView = new ImageView(this.mContext);
        imageView.setImageResource(i);
        imageView.setAlpha(0.0f);
        addView(imageView);
        return imageView;
    }

    private void layoutIndicator(View view) {
        int right;
        int measuredWidth;
        boolean z = true;
        if (getLayoutDirection() != 1) {
            z = false;
        }
        if (z) {
            measuredWidth = this.mIconFrame.getLeft();
            right = measuredWidth - view.getMeasuredWidth();
        } else {
            right = this.mIconFrame.getRight();
            measuredWidth = right + view.getMeasuredWidth();
        }
        view.layout(right, this.mIconFrame.getBottom() - view.getMeasuredHeight(), measuredWidth, this.mIconFrame.getBottom());
    }

    private void setVisibility(View view, boolean z, boolean z2) {
        float f = (z && z2) ? 1 : 0;
        if (view.getAlpha() == f) {
            return;
        }
        if (z) {
            view.animate().setDuration(z2 ? SHORT_DURATION : DEFAULT_DURATION).alpha(f).start();
        } else {
            view.setAlpha(f);
        }
    }

    @Override // com.android.systemui.qs.QSIconView
    protected View createIcon() {
        this.mIconFrame = new FrameLayout(this.mContext);
        this.mSignal = new ImageView(this.mContext);
        this.mIconFrame.addView(this.mSignal);
        this.mOverlay = new ImageView(this.mContext);
        this.mIconFrame.addView(this.mOverlay, -2, -2);
        return this.mIconFrame;
    }

    @Override // com.android.systemui.qs.QSIconView
    protected int getIconMeasureMode() {
        return Integer.MIN_VALUE;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.QSIconView, android.view.ViewGroup, android.view.View
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        layoutIndicator(this.mIn);
        layoutIndicator(this.mOut);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.QSIconView, android.view.View
    public void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(this.mIconFrame.getMeasuredHeight(), 1073741824);
        int makeMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(this.mIconFrame.getMeasuredHeight(), Integer.MIN_VALUE);
        this.mIn.measure(makeMeasureSpec2, makeMeasureSpec);
        this.mOut.measure(makeMeasureSpec2, makeMeasureSpec);
    }

    @Override // com.android.systemui.qs.QSIconView
    public void setIcon(QSTile.State state) {
        QSTile.SignalState signalState = (QSTile.SignalState) state;
        setIcon(this.mSignal, signalState);
        if (signalState.overlayIconId > 0) {
            this.mOverlay.setVisibility(0);
            this.mOverlay.setImageResource(signalState.overlayIconId);
        } else {
            this.mOverlay.setVisibility(8);
        }
        if (signalState.overlayIconId <= 0 || !signalState.isOverlayIconWide) {
            this.mSignal.setPaddingRelative(0, 0, 0, 0);
        } else {
            this.mSignal.setPaddingRelative(this.mWideOverlayIconStartPadding, 0, 0, 0);
        }
        Drawable drawable = this.mSignal.getDrawable();
        if (state.autoMirrorDrawable && drawable != null) {
            drawable.setAutoMirrored(true);
        }
        boolean isShown = isShown();
        setVisibility(this.mIn, isShown, signalState.activityIn);
        setVisibility(this.mOut, isShown, signalState.activityOut);
    }
}

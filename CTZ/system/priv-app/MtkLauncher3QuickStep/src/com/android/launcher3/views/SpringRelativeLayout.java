package com.android.launcher3.views;

import android.content.Context;
import android.graphics.Canvas;
import android.support.animation.DynamicAnimation;
import android.support.animation.FloatPropertyCompat;
import android.support.animation.SpringAnimation;
import android.support.animation.SpringForce;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.EdgeEffect;
import android.widget.RelativeLayout;
import com.android.launcher3.LauncherSettings;
/* loaded from: classes.dex */
public class SpringRelativeLayout extends RelativeLayout {
    private static final FloatPropertyCompat<SpringRelativeLayout> DAMPED_SCROLL = new FloatPropertyCompat<SpringRelativeLayout>(LauncherSettings.Settings.EXTRA_VALUE) { // from class: com.android.launcher3.views.SpringRelativeLayout.1
        @Override // android.support.animation.FloatPropertyCompat
        public float getValue(SpringRelativeLayout springRelativeLayout) {
            return springRelativeLayout.mDampedScrollShift;
        }

        @Override // android.support.animation.FloatPropertyCompat
        public void setValue(SpringRelativeLayout springRelativeLayout, float f) {
            springRelativeLayout.setDampedScrollShift(f);
        }
    };
    private static final float DAMPING_RATIO = 0.5f;
    private static final float STIFFNESS = 850.0f;
    private static final float VELOCITY_MULTIPLIER = 0.3f;
    private SpringEdgeEffect mActiveEdge;
    private float mDampedScrollShift;
    private final SpringAnimation mSpring;
    private final SparseBooleanArray mSpringViews;

    public SpringRelativeLayout(Context context) {
        this(context, null);
    }

    public SpringRelativeLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public SpringRelativeLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mSpringViews = new SparseBooleanArray();
        this.mDampedScrollShift = 0.0f;
        this.mSpring = new SpringAnimation(this, DAMPED_SCROLL, 0.0f);
        this.mSpring.setSpring(new SpringForce(0.0f).setStiffness(STIFFNESS).setDampingRatio(0.5f));
    }

    public void addSpringView(int i) {
        this.mSpringViews.put(i, true);
    }

    public void removeSpringView(int i) {
        this.mSpringViews.delete(i);
        invalidate();
    }

    @Override // android.view.ViewGroup
    protected boolean drawChild(Canvas canvas, View view, long j) {
        if (this.mDampedScrollShift != 0.0f && this.mSpringViews.get(view.getId())) {
            canvas.translate(0.0f, this.mDampedScrollShift);
            boolean drawChild = super.drawChild(canvas, view, j);
            canvas.translate(0.0f, -this.mDampedScrollShift);
            return drawChild;
        }
        return super.drawChild(canvas, view, j);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setActiveEdge(SpringEdgeEffect springEdgeEffect) {
        if (this.mActiveEdge != springEdgeEffect && this.mActiveEdge != null) {
            this.mActiveEdge.mDistance = 0.0f;
        }
        this.mActiveEdge = springEdgeEffect;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setDampedScrollShift(float f) {
        if (f != this.mDampedScrollShift) {
            this.mDampedScrollShift = f;
            invalidate();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void finishScrollWithVelocity(float f) {
        this.mSpring.setStartVelocity(f);
        this.mSpring.setStartValue(this.mDampedScrollShift);
        this.mSpring.start();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void finishWithShiftAndVelocity(float f, float f2, DynamicAnimation.OnAnimationEndListener onAnimationEndListener) {
        setDampedScrollShift(f);
        this.mSpring.addEndListener(onAnimationEndListener);
        finishScrollWithVelocity(f2);
    }

    public RecyclerView.EdgeEffectFactory createEdgeEffectFactory() {
        return new SpringEdgeEffectFactory();
    }

    /* loaded from: classes.dex */
    private class SpringEdgeEffectFactory extends RecyclerView.EdgeEffectFactory {
        private SpringEdgeEffectFactory() {
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.support.v7.widget.RecyclerView.EdgeEffectFactory
        @NonNull
        public EdgeEffect createEdgeEffect(RecyclerView recyclerView, int i) {
            if (i != 1) {
                if (i == 3) {
                    return new SpringEdgeEffect(SpringRelativeLayout.this.getContext(), -0.3f);
                }
                return super.createEdgeEffect(recyclerView, i);
            }
            return new SpringEdgeEffect(SpringRelativeLayout.this.getContext(), SpringRelativeLayout.VELOCITY_MULTIPLIER);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class SpringEdgeEffect extends EdgeEffect {
        private float mDistance;
        private final float mVelocityMultiplier;

        public SpringEdgeEffect(Context context, float f) {
            super(context);
            this.mVelocityMultiplier = f;
        }

        @Override // android.widget.EdgeEffect
        public boolean draw(Canvas canvas) {
            return false;
        }

        @Override // android.widget.EdgeEffect
        public void onAbsorb(int i) {
            SpringRelativeLayout.this.finishScrollWithVelocity(i * this.mVelocityMultiplier);
        }

        @Override // android.widget.EdgeEffect
        public void onPull(float f, float f2) {
            SpringRelativeLayout.this.setActiveEdge(this);
            this.mDistance += f * (this.mVelocityMultiplier / 3.0f);
            SpringRelativeLayout.this.setDampedScrollShift(this.mDistance * SpringRelativeLayout.this.getHeight());
        }

        @Override // android.widget.EdgeEffect
        public void onRelease() {
            this.mDistance = 0.0f;
            SpringRelativeLayout.this.finishScrollWithVelocity(0.0f);
        }
    }
}

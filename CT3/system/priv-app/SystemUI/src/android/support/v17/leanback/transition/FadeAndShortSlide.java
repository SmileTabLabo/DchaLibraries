package android.support.v17.leanback.transition;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.v17.leanback.R$styleable;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionValues;
import android.transition.Visibility;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
/* loaded from: a.zip:android/support/v17/leanback/transition/FadeAndShortSlide.class */
public class FadeAndShortSlide extends Visibility {
    private float mDistance;
    private Visibility mFade;
    private CalculateSlide mSlideCalculator;
    final CalculateSlide sCalculateTopBottom;
    private static final TimeInterpolator sDecelerate = new DecelerateInterpolator();
    static final CalculateSlide sCalculateStart = new CalculateSlide() { // from class: android.support.v17.leanback.transition.FadeAndShortSlide.1
        @Override // android.support.v17.leanback.transition.FadeAndShortSlide.CalculateSlide
        public float getGoneX(FadeAndShortSlide fadeAndShortSlide, ViewGroup viewGroup, View view, int[] iArr) {
            boolean z = true;
            if (viewGroup.getLayoutDirection() != 1) {
                z = false;
            }
            return z ? view.getTranslationX() + fadeAndShortSlide.getHorizontalDistance(viewGroup) : view.getTranslationX() - fadeAndShortSlide.getHorizontalDistance(viewGroup);
        }
    };
    static final CalculateSlide sCalculateEnd = new CalculateSlide() { // from class: android.support.v17.leanback.transition.FadeAndShortSlide.2
        @Override // android.support.v17.leanback.transition.FadeAndShortSlide.CalculateSlide
        public float getGoneX(FadeAndShortSlide fadeAndShortSlide, ViewGroup viewGroup, View view, int[] iArr) {
            boolean z = true;
            if (viewGroup.getLayoutDirection() != 1) {
                z = false;
            }
            return z ? view.getTranslationX() - fadeAndShortSlide.getHorizontalDistance(viewGroup) : view.getTranslationX() + fadeAndShortSlide.getHorizontalDistance(viewGroup);
        }
    };
    static final CalculateSlide sCalculateStartEnd = new CalculateSlide() { // from class: android.support.v17.leanback.transition.FadeAndShortSlide.3
        @Override // android.support.v17.leanback.transition.FadeAndShortSlide.CalculateSlide
        public float getGoneX(FadeAndShortSlide fadeAndShortSlide, ViewGroup viewGroup, View view, int[] iArr) {
            int i = iArr[0];
            int width = view.getWidth() / 2;
            viewGroup.getLocationOnScreen(iArr);
            Rect epicenter = fadeAndShortSlide.getEpicenter();
            return i + width < (epicenter == null ? iArr[0] + (viewGroup.getWidth() / 2) : epicenter.centerX()) ? view.getTranslationX() - fadeAndShortSlide.getHorizontalDistance(viewGroup) : view.getTranslationX() + fadeAndShortSlide.getHorizontalDistance(viewGroup);
        }
    };
    static final CalculateSlide sCalculateBottom = new CalculateSlide() { // from class: android.support.v17.leanback.transition.FadeAndShortSlide.4
        @Override // android.support.v17.leanback.transition.FadeAndShortSlide.CalculateSlide
        public float getGoneY(FadeAndShortSlide fadeAndShortSlide, ViewGroup viewGroup, View view, int[] iArr) {
            return view.getTranslationY() + fadeAndShortSlide.getVerticalDistance(viewGroup);
        }
    };
    static final CalculateSlide sCalculateTop = new CalculateSlide() { // from class: android.support.v17.leanback.transition.FadeAndShortSlide.5
        @Override // android.support.v17.leanback.transition.FadeAndShortSlide.CalculateSlide
        public float getGoneY(FadeAndShortSlide fadeAndShortSlide, ViewGroup viewGroup, View view, int[] iArr) {
            return view.getTranslationY() - fadeAndShortSlide.getVerticalDistance(viewGroup);
        }
    };

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:android/support/v17/leanback/transition/FadeAndShortSlide$CalculateSlide.class */
    public static abstract class CalculateSlide {
        private CalculateSlide() {
        }

        /* synthetic */ CalculateSlide(CalculateSlide calculateSlide) {
            this();
        }

        float getGoneX(FadeAndShortSlide fadeAndShortSlide, ViewGroup viewGroup, View view, int[] iArr) {
            return view.getTranslationX();
        }

        float getGoneY(FadeAndShortSlide fadeAndShortSlide, ViewGroup viewGroup, View view, int[] iArr) {
            return view.getTranslationY();
        }
    }

    public FadeAndShortSlide() {
        this(8388611);
    }

    public FadeAndShortSlide(int i) {
        this.mFade = new Fade();
        this.mDistance = -1.0f;
        this.sCalculateTopBottom = new CalculateSlide(this) { // from class: android.support.v17.leanback.transition.FadeAndShortSlide.6
            final FadeAndShortSlide this$0;

            {
                this.this$0 = this;
            }

            @Override // android.support.v17.leanback.transition.FadeAndShortSlide.CalculateSlide
            public float getGoneY(FadeAndShortSlide fadeAndShortSlide, ViewGroup viewGroup, View view, int[] iArr) {
                int i2 = iArr[1];
                int height = view.getHeight() / 2;
                viewGroup.getLocationOnScreen(iArr);
                Rect epicenter = this.this$0.getEpicenter();
                return i2 + height < (epicenter == null ? iArr[1] + (viewGroup.getHeight() / 2) : epicenter.centerY()) ? view.getTranslationY() - fadeAndShortSlide.getVerticalDistance(viewGroup) : view.getTranslationY() + fadeAndShortSlide.getVerticalDistance(viewGroup);
            }
        };
        setSlideEdge(i);
    }

    public FadeAndShortSlide(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mFade = new Fade();
        this.mDistance = -1.0f;
        this.sCalculateTopBottom = new CalculateSlide(this) { // from class: android.support.v17.leanback.transition.FadeAndShortSlide.6
            final FadeAndShortSlide this$0;

            {
                this.this$0 = this;
            }

            @Override // android.support.v17.leanback.transition.FadeAndShortSlide.CalculateSlide
            public float getGoneY(FadeAndShortSlide fadeAndShortSlide, ViewGroup viewGroup, View view, int[] iArr) {
                int i2 = iArr[1];
                int height = view.getHeight() / 2;
                viewGroup.getLocationOnScreen(iArr);
                Rect epicenter = this.this$0.getEpicenter();
                return i2 + height < (epicenter == null ? iArr[1] + (viewGroup.getHeight() / 2) : epicenter.centerY()) ? view.getTranslationY() - fadeAndShortSlide.getVerticalDistance(viewGroup) : view.getTranslationY() + fadeAndShortSlide.getVerticalDistance(viewGroup);
            }
        };
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.lbSlide);
        setSlideEdge(obtainStyledAttributes.getInt(R$styleable.lbSlide_lb_slideEdge, 8388611));
        obtainStyledAttributes.recycle();
    }

    private void captureValues(TransitionValues transitionValues) {
        int[] iArr = new int[2];
        transitionValues.view.getLocationOnScreen(iArr);
        transitionValues.values.put("android:fadeAndShortSlideTransition:screenPosition", iArr);
    }

    @Override // android.transition.Transition
    public Transition addListener(Transition.TransitionListener transitionListener) {
        this.mFade.addListener(transitionListener);
        return super.addListener(transitionListener);
    }

    @Override // android.transition.Visibility, android.transition.Transition
    public void captureEndValues(TransitionValues transitionValues) {
        this.mFade.captureEndValues(transitionValues);
        super.captureEndValues(transitionValues);
        captureValues(transitionValues);
    }

    @Override // android.transition.Visibility, android.transition.Transition
    public void captureStartValues(TransitionValues transitionValues) {
        this.mFade.captureStartValues(transitionValues);
        super.captureStartValues(transitionValues);
        captureValues(transitionValues);
    }

    @Override // android.transition.Transition
    public Transition clone() {
        FadeAndShortSlide fadeAndShortSlide = (FadeAndShortSlide) super.clone();
        fadeAndShortSlide.mFade = (Visibility) this.mFade.clone();
        return fadeAndShortSlide;
    }

    float getHorizontalDistance(ViewGroup viewGroup) {
        return this.mDistance >= 0.0f ? this.mDistance : viewGroup.getWidth() / 4;
    }

    float getVerticalDistance(ViewGroup viewGroup) {
        return this.mDistance >= 0.0f ? this.mDistance : viewGroup.getHeight() / 4;
    }

    @Override // android.transition.Visibility
    public Animator onAppear(ViewGroup viewGroup, View view, TransitionValues transitionValues, TransitionValues transitionValues2) {
        if (transitionValues2 == null || viewGroup == view) {
            return null;
        }
        int[] iArr = (int[]) transitionValues2.values.get("android:fadeAndShortSlideTransition:screenPosition");
        int i = iArr[0];
        int i2 = iArr[1];
        float translationX = view.getTranslationX();
        Animator createAnimation = TranslationAnimationCreator.createAnimation(view, transitionValues2, i, i2, this.mSlideCalculator.getGoneX(this, viewGroup, view, iArr), this.mSlideCalculator.getGoneY(this, viewGroup, view, iArr), translationX, view.getTranslationY(), sDecelerate, this);
        Animator onAppear = this.mFade.onAppear(viewGroup, view, transitionValues, transitionValues2);
        if (createAnimation == null) {
            return onAppear;
        }
        if (onAppear == null) {
            return createAnimation;
        }
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(createAnimation).with(onAppear);
        return animatorSet;
    }

    @Override // android.transition.Visibility
    public Animator onDisappear(ViewGroup viewGroup, View view, TransitionValues transitionValues, TransitionValues transitionValues2) {
        if (transitionValues == null || viewGroup == view) {
            return null;
        }
        int[] iArr = (int[]) transitionValues.values.get("android:fadeAndShortSlideTransition:screenPosition");
        Animator createAnimation = TranslationAnimationCreator.createAnimation(view, transitionValues, iArr[0], iArr[1], view.getTranslationX(), view.getTranslationY(), this.mSlideCalculator.getGoneX(this, viewGroup, view, iArr), this.mSlideCalculator.getGoneY(this, viewGroup, view, iArr), sDecelerate, this);
        Animator onDisappear = this.mFade.onDisappear(viewGroup, view, transitionValues, transitionValues2);
        if (createAnimation == null) {
            return onDisappear;
        }
        if (onDisappear == null) {
            return createAnimation;
        }
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(createAnimation).with(onDisappear);
        return animatorSet;
    }

    @Override // android.transition.Transition
    public Transition removeListener(Transition.TransitionListener transitionListener) {
        this.mFade.removeListener(transitionListener);
        return super.removeListener(transitionListener);
    }

    @Override // android.transition.Transition
    public void setEpicenterCallback(Transition.EpicenterCallback epicenterCallback) {
        this.mFade.setEpicenterCallback(epicenterCallback);
        super.setEpicenterCallback(epicenterCallback);
    }

    public void setSlideEdge(int i) {
        switch (i) {
            case 48:
                this.mSlideCalculator = sCalculateTop;
                return;
            case 80:
                this.mSlideCalculator = sCalculateBottom;
                return;
            case 112:
                this.mSlideCalculator = this.sCalculateTopBottom;
                return;
            case 8388611:
                this.mSlideCalculator = sCalculateStart;
                return;
            case 8388613:
                this.mSlideCalculator = sCalculateEnd;
                return;
            case 8388615:
                this.mSlideCalculator = sCalculateStartEnd;
                return;
            default:
                throw new IllegalArgumentException("Invalid slide direction");
        }
    }
}

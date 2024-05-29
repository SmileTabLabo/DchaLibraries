package com.android.systemui.qs;

import android.util.FloatProperty;
import android.util.MathUtils;
import android.util.Property;
import android.view.View;
import android.view.animation.Interpolator;
import java.util.ArrayList;
import java.util.List;
/* loaded from: classes.dex */
public class TouchAnimator {
    private static final FloatProperty<TouchAnimator> POSITION = new FloatProperty<TouchAnimator>("position") { // from class: com.android.systemui.qs.TouchAnimator.1
        @Override // android.util.FloatProperty
        public void setValue(TouchAnimator touchAnimator, float f) {
            touchAnimator.setPosition(f);
        }

        @Override // android.util.Property
        public Float get(TouchAnimator touchAnimator) {
            return Float.valueOf(touchAnimator.mLastT);
        }
    };
    private final float mEndDelay;
    private final Interpolator mInterpolator;
    private final KeyframeSet[] mKeyframeSets;
    private float mLastT;
    private final Listener mListener;
    private final float mSpan;
    private final float mStartDelay;
    private final Object[] mTargets;

    /* loaded from: classes.dex */
    public interface Listener {
        void onAnimationAtEnd();

        void onAnimationAtStart();

        void onAnimationStarted();
    }

    private TouchAnimator(Object[] objArr, KeyframeSet[] keyframeSetArr, float f, float f2, Interpolator interpolator, Listener listener) {
        this.mLastT = -1.0f;
        this.mTargets = objArr;
        this.mKeyframeSets = keyframeSetArr;
        this.mStartDelay = f;
        this.mEndDelay = f2;
        this.mSpan = (1.0f - this.mEndDelay) - this.mStartDelay;
        this.mInterpolator = interpolator;
        this.mListener = listener;
    }

    public void setPosition(float f) {
        float constrain = MathUtils.constrain((f - this.mStartDelay) / this.mSpan, 0.0f, 1.0f);
        if (this.mInterpolator != null) {
            constrain = this.mInterpolator.getInterpolation(constrain);
        }
        if (constrain == this.mLastT) {
            return;
        }
        if (this.mListener != null) {
            if (constrain == 1.0f) {
                this.mListener.onAnimationAtEnd();
            } else if (constrain == 0.0f) {
                this.mListener.onAnimationAtStart();
            } else if (this.mLastT <= 0.0f || this.mLastT == 1.0f) {
                this.mListener.onAnimationStarted();
            }
            this.mLastT = constrain;
        }
        for (int i = 0; i < this.mTargets.length; i++) {
            this.mKeyframeSets[i].setValue(constrain, this.mTargets[i]);
        }
    }

    /* loaded from: classes.dex */
    public static class ListenerAdapter implements Listener {
        @Override // com.android.systemui.qs.TouchAnimator.Listener
        public void onAnimationAtStart() {
        }

        @Override // com.android.systemui.qs.TouchAnimator.Listener
        public void onAnimationAtEnd() {
        }

        @Override // com.android.systemui.qs.TouchAnimator.Listener
        public void onAnimationStarted() {
        }
    }

    /* loaded from: classes.dex */
    public static class Builder {
        private float mEndDelay;
        private Interpolator mInterpolator;
        private Listener mListener;
        private float mStartDelay;
        private List<Object> mTargets = new ArrayList();
        private List<KeyframeSet> mValues = new ArrayList();

        public Builder addFloat(Object obj, String str, float... fArr) {
            add(obj, KeyframeSet.ofFloat(getProperty(obj, str, Float.TYPE), fArr));
            return this;
        }

        private void add(Object obj, KeyframeSet keyframeSet) {
            this.mTargets.add(obj);
            this.mValues.add(keyframeSet);
        }

        private static Property getProperty(Object obj, String str, Class<?> cls) {
            if (obj instanceof View) {
                char c = 65535;
                switch (str.hashCode()) {
                    case -1225497657:
                        if (str.equals("translationX")) {
                            c = 0;
                            break;
                        }
                        break;
                    case -1225497656:
                        if (str.equals("translationY")) {
                            c = 1;
                            break;
                        }
                        break;
                    case -1225497655:
                        if (str.equals("translationZ")) {
                            c = 2;
                            break;
                        }
                        break;
                    case -908189618:
                        if (str.equals("scaleX")) {
                            c = 7;
                            break;
                        }
                        break;
                    case -908189617:
                        if (str.equals("scaleY")) {
                            c = '\b';
                            break;
                        }
                        break;
                    case -40300674:
                        if (str.equals("rotation")) {
                            c = 4;
                            break;
                        }
                        break;
                    case 120:
                        if (str.equals("x")) {
                            c = 5;
                            break;
                        }
                        break;
                    case 121:
                        if (str.equals("y")) {
                            c = 6;
                            break;
                        }
                        break;
                    case 92909918:
                        if (str.equals("alpha")) {
                            c = 3;
                            break;
                        }
                        break;
                }
                switch (c) {
                    case 0:
                        return View.TRANSLATION_X;
                    case 1:
                        return View.TRANSLATION_Y;
                    case 2:
                        return View.TRANSLATION_Z;
                    case 3:
                        return View.ALPHA;
                    case 4:
                        return View.ROTATION;
                    case 5:
                        return View.X;
                    case 6:
                        return View.Y;
                    case 7:
                        return View.SCALE_X;
                    case '\b':
                        return View.SCALE_Y;
                }
            }
            if ((obj instanceof TouchAnimator) && "position".equals(str)) {
                return TouchAnimator.POSITION;
            }
            return Property.of(obj.getClass(), cls, str);
        }

        public Builder setStartDelay(float f) {
            this.mStartDelay = f;
            return this;
        }

        public Builder setEndDelay(float f) {
            this.mEndDelay = f;
            return this;
        }

        public Builder setInterpolator(Interpolator interpolator) {
            this.mInterpolator = interpolator;
            return this;
        }

        public Builder setListener(Listener listener) {
            this.mListener = listener;
            return this;
        }

        public TouchAnimator build() {
            return new TouchAnimator(this.mTargets.toArray(new Object[this.mTargets.size()]), (KeyframeSet[]) this.mValues.toArray(new KeyframeSet[this.mValues.size()]), this.mStartDelay, this.mEndDelay, this.mInterpolator, this.mListener);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static abstract class KeyframeSet {
        private final float mFrameWidth;
        private final int mSize;

        protected abstract void interpolate(int i, float f, Object obj);

        public KeyframeSet(int i) {
            this.mSize = i;
            this.mFrameWidth = 1.0f / (i - 1);
        }

        void setValue(float f, Object obj) {
            int constrain = MathUtils.constrain((int) Math.ceil(f / this.mFrameWidth), 1, this.mSize - 1);
            interpolate(constrain, (f - (this.mFrameWidth * (constrain - 1))) / this.mFrameWidth, obj);
        }

        public static KeyframeSet ofFloat(Property property, float... fArr) {
            return new FloatKeyframeSet(property, fArr);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class FloatKeyframeSet<T> extends KeyframeSet {
        private final Property<T, Float> mProperty;
        private final float[] mValues;

        public FloatKeyframeSet(Property<T, Float> property, float[] fArr) {
            super(fArr.length);
            this.mProperty = property;
            this.mValues = fArr;
        }

        @Override // com.android.systemui.qs.TouchAnimator.KeyframeSet
        protected void interpolate(int i, float f, Object obj) {
            float f2 = this.mValues[i - 1];
            this.mProperty.set(obj, Float.valueOf(f2 + ((this.mValues[i] - f2) * f)));
        }
    }
}

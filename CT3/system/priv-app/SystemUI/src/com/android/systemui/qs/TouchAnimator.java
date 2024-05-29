package com.android.systemui.qs;

import android.util.FloatProperty;
import android.util.MathUtils;
import android.util.Property;
import android.view.View;
import android.view.animation.Interpolator;
import java.util.ArrayList;
import java.util.List;
/* loaded from: a.zip:com/android/systemui/qs/TouchAnimator.class */
public class TouchAnimator {
    private static final FloatProperty<TouchAnimator> POSITION = new FloatProperty<TouchAnimator>("position") { // from class: com.android.systemui.qs.TouchAnimator.1
        @Override // android.util.Property
        public Float get(TouchAnimator touchAnimator) {
            return Float.valueOf(touchAnimator.mLastT);
        }

        @Override // android.util.FloatProperty
        public void setValue(TouchAnimator touchAnimator, float f) {
            touchAnimator.setPosition(f);
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

    /* loaded from: a.zip:com/android/systemui/qs/TouchAnimator$Builder.class */
    public static class Builder {
        private float mEndDelay;
        private Interpolator mInterpolator;
        private Listener mListener;
        private float mStartDelay;
        private List<Object> mTargets = new ArrayList();
        private List<KeyframeSet> mValues = new ArrayList();

        private void add(Object obj, KeyframeSet keyframeSet) {
            this.mTargets.add(obj);
            this.mValues.add(keyframeSet);
        }

        private static Property getProperty(Object obj, String str, Class<?> cls) {
            if (obj instanceof View) {
                if (str.equals("translationX")) {
                    return View.TRANSLATION_X;
                }
                if (str.equals("translationY")) {
                    return View.TRANSLATION_Y;
                }
                if (str.equals("translationZ")) {
                    return View.TRANSLATION_Z;
                }
                if (str.equals("alpha")) {
                    return View.ALPHA;
                }
                if (str.equals("rotation")) {
                    return View.ROTATION;
                }
                if (str.equals("x")) {
                    return View.X;
                }
                if (str.equals("y")) {
                    return View.Y;
                }
                if (str.equals("scaleX")) {
                    return View.SCALE_X;
                }
                if (str.equals("scaleY")) {
                    return View.SCALE_Y;
                }
            }
            return ((obj instanceof TouchAnimator) && "position".equals(str)) ? TouchAnimator.POSITION : Property.of(obj.getClass(), cls, str);
        }

        public Builder addFloat(Object obj, String str, float... fArr) {
            add(obj, KeyframeSet.ofFloat(getProperty(obj, str, Float.TYPE), fArr));
            return this;
        }

        public TouchAnimator build() {
            return new TouchAnimator(this.mTargets.toArray(new Object[this.mTargets.size()]), (KeyframeSet[]) this.mValues.toArray(new KeyframeSet[this.mValues.size()]), this.mStartDelay, this.mEndDelay, this.mInterpolator, this.mListener, null);
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

        public Builder setStartDelay(float f) {
            this.mStartDelay = f;
            return this;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/qs/TouchAnimator$FloatKeyframeSet.class */
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
            this.mProperty.set(obj, Float.valueOf(((this.mValues[i] - f2) * f) + f2));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/qs/TouchAnimator$KeyframeSet.class */
    public static abstract class KeyframeSet {
        private final float mFrameWidth;
        private final int mSize;

        public KeyframeSet(int i) {
            this.mSize = i;
            this.mFrameWidth = 1.0f / (i - 1);
        }

        public static KeyframeSet ofFloat(Property property, float... fArr) {
            return new FloatKeyframeSet(property, fArr);
        }

        protected abstract void interpolate(int i, float f, Object obj);

        void setValue(float f, Object obj) {
            int i = 1;
            while (i < this.mSize - 1 && f > this.mFrameWidth) {
                i++;
            }
            interpolate(i, f / this.mFrameWidth, obj);
        }
    }

    /* loaded from: a.zip:com/android/systemui/qs/TouchAnimator$Listener.class */
    public interface Listener {
        void onAnimationAtEnd();

        void onAnimationAtStart();

        void onAnimationStarted();
    }

    /* loaded from: a.zip:com/android/systemui/qs/TouchAnimator$ListenerAdapter.class */
    public static class ListenerAdapter implements Listener {
        @Override // com.android.systemui.qs.TouchAnimator.Listener
        public void onAnimationAtEnd() {
        }

        @Override // com.android.systemui.qs.TouchAnimator.Listener
        public void onAnimationAtStart() {
        }

        @Override // com.android.systemui.qs.TouchAnimator.Listener
        public void onAnimationStarted() {
        }
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

    /* synthetic */ TouchAnimator(Object[] objArr, KeyframeSet[] keyframeSetArr, float f, float f2, Interpolator interpolator, Listener listener, TouchAnimator touchAnimator) {
        this(objArr, keyframeSetArr, f, f2, interpolator, listener);
    }

    public void setPosition(float f) {
        float constrain = MathUtils.constrain((f - this.mStartDelay) / this.mSpan, 0.0f, 1.0f);
        float f2 = constrain;
        if (this.mInterpolator != null) {
            f2 = this.mInterpolator.getInterpolation(constrain);
        }
        if (f2 == this.mLastT) {
            return;
        }
        if (this.mListener != null) {
            if (f2 == 1.0f) {
                this.mListener.onAnimationAtEnd();
            } else if (f2 == 0.0f) {
                this.mListener.onAnimationAtStart();
            } else if (this.mLastT <= 0.0f || this.mLastT == 1.0f) {
                this.mListener.onAnimationStarted();
            }
            this.mLastT = f2;
        }
        for (int i = 0; i < this.mTargets.length; i++) {
            this.mKeyframeSets[i].setValue(f2, this.mTargets[i]);
        }
    }
}

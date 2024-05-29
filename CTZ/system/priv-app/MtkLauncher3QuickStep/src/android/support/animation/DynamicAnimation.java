package android.support.animation;

import android.os.Looper;
import android.support.animation.AnimationHandler;
import android.support.animation.DynamicAnimation;
import android.support.annotation.FloatRange;
import android.support.annotation.RestrictTo;
import android.support.v4.view.ViewCompat;
import android.util.AndroidRuntimeException;
import android.view.View;
import java.util.ArrayList;
/* loaded from: classes.dex */
public abstract class DynamicAnimation<T extends DynamicAnimation<T>> implements AnimationHandler.AnimationFrameCallback {
    public static final float MIN_VISIBLE_CHANGE_ALPHA = 0.00390625f;
    public static final float MIN_VISIBLE_CHANGE_PIXELS = 1.0f;
    public static final float MIN_VISIBLE_CHANGE_ROTATION_DEGREES = 0.1f;
    public static final float MIN_VISIBLE_CHANGE_SCALE = 0.002f;
    private static final float THRESHOLD_MULTIPLIER = 0.75f;
    private static final float UNSET = Float.MAX_VALUE;
    private final ArrayList<OnAnimationEndListener> mEndListeners;
    private long mLastFrameTime;
    float mMaxValue;
    float mMinValue;
    private float mMinVisibleChange;
    final FloatPropertyCompat mProperty;
    boolean mRunning;
    boolean mStartValueIsSet;
    final Object mTarget;
    private final ArrayList<OnAnimationUpdateListener> mUpdateListeners;
    float mValue;
    float mVelocity;
    public static final ViewProperty TRANSLATION_X = new ViewProperty("translationX") { // from class: android.support.animation.DynamicAnimation.1
        @Override // android.support.animation.FloatPropertyCompat
        public void setValue(View view, float value) {
            view.setTranslationX(value);
        }

        @Override // android.support.animation.FloatPropertyCompat
        public float getValue(View view) {
            return view.getTranslationX();
        }
    };
    public static final ViewProperty TRANSLATION_Y = new ViewProperty("translationY") { // from class: android.support.animation.DynamicAnimation.2
        @Override // android.support.animation.FloatPropertyCompat
        public void setValue(View view, float value) {
            view.setTranslationY(value);
        }

        @Override // android.support.animation.FloatPropertyCompat
        public float getValue(View view) {
            return view.getTranslationY();
        }
    };
    public static final ViewProperty TRANSLATION_Z = new ViewProperty("translationZ") { // from class: android.support.animation.DynamicAnimation.3
        @Override // android.support.animation.FloatPropertyCompat
        public void setValue(View view, float value) {
            ViewCompat.setTranslationZ(view, value);
        }

        @Override // android.support.animation.FloatPropertyCompat
        public float getValue(View view) {
            return ViewCompat.getTranslationZ(view);
        }
    };
    public static final ViewProperty SCALE_X = new ViewProperty("scaleX") { // from class: android.support.animation.DynamicAnimation.4
        @Override // android.support.animation.FloatPropertyCompat
        public void setValue(View view, float value) {
            view.setScaleX(value);
        }

        @Override // android.support.animation.FloatPropertyCompat
        public float getValue(View view) {
            return view.getScaleX();
        }
    };
    public static final ViewProperty SCALE_Y = new ViewProperty("scaleY") { // from class: android.support.animation.DynamicAnimation.5
        @Override // android.support.animation.FloatPropertyCompat
        public void setValue(View view, float value) {
            view.setScaleY(value);
        }

        @Override // android.support.animation.FloatPropertyCompat
        public float getValue(View view) {
            return view.getScaleY();
        }
    };
    public static final ViewProperty ROTATION = new ViewProperty("rotation") { // from class: android.support.animation.DynamicAnimation.6
        @Override // android.support.animation.FloatPropertyCompat
        public void setValue(View view, float value) {
            view.setRotation(value);
        }

        @Override // android.support.animation.FloatPropertyCompat
        public float getValue(View view) {
            return view.getRotation();
        }
    };
    public static final ViewProperty ROTATION_X = new ViewProperty("rotationX") { // from class: android.support.animation.DynamicAnimation.7
        @Override // android.support.animation.FloatPropertyCompat
        public void setValue(View view, float value) {
            view.setRotationX(value);
        }

        @Override // android.support.animation.FloatPropertyCompat
        public float getValue(View view) {
            return view.getRotationX();
        }
    };
    public static final ViewProperty ROTATION_Y = new ViewProperty("rotationY") { // from class: android.support.animation.DynamicAnimation.8
        @Override // android.support.animation.FloatPropertyCompat
        public void setValue(View view, float value) {
            view.setRotationY(value);
        }

        @Override // android.support.animation.FloatPropertyCompat
        public float getValue(View view) {
            return view.getRotationY();
        }
    };
    public static final ViewProperty X = new ViewProperty("x") { // from class: android.support.animation.DynamicAnimation.9
        @Override // android.support.animation.FloatPropertyCompat
        public void setValue(View view, float value) {
            view.setX(value);
        }

        @Override // android.support.animation.FloatPropertyCompat
        public float getValue(View view) {
            return view.getX();
        }
    };
    public static final ViewProperty Y = new ViewProperty("y") { // from class: android.support.animation.DynamicAnimation.10
        @Override // android.support.animation.FloatPropertyCompat
        public void setValue(View view, float value) {
            view.setY(value);
        }

        @Override // android.support.animation.FloatPropertyCompat
        public float getValue(View view) {
            return view.getY();
        }
    };
    public static final ViewProperty Z = new ViewProperty("z") { // from class: android.support.animation.DynamicAnimation.11
        @Override // android.support.animation.FloatPropertyCompat
        public void setValue(View view, float value) {
            ViewCompat.setZ(view, value);
        }

        @Override // android.support.animation.FloatPropertyCompat
        public float getValue(View view) {
            return ViewCompat.getZ(view);
        }
    };
    public static final ViewProperty ALPHA = new ViewProperty("alpha") { // from class: android.support.animation.DynamicAnimation.12
        @Override // android.support.animation.FloatPropertyCompat
        public void setValue(View view, float value) {
            view.setAlpha(value);
        }

        @Override // android.support.animation.FloatPropertyCompat
        public float getValue(View view) {
            return view.getAlpha();
        }
    };
    public static final ViewProperty SCROLL_X = new ViewProperty("scrollX") { // from class: android.support.animation.DynamicAnimation.13
        @Override // android.support.animation.FloatPropertyCompat
        public void setValue(View view, float value) {
            view.setScrollX((int) value);
        }

        @Override // android.support.animation.FloatPropertyCompat
        public float getValue(View view) {
            return view.getScrollX();
        }
    };
    public static final ViewProperty SCROLL_Y = new ViewProperty("scrollY") { // from class: android.support.animation.DynamicAnimation.14
        @Override // android.support.animation.FloatPropertyCompat
        public void setValue(View view, float value) {
            view.setScrollY((int) value);
        }

        @Override // android.support.animation.FloatPropertyCompat
        public float getValue(View view) {
            return view.getScrollY();
        }
    };

    /* loaded from: classes.dex */
    static class MassState {
        float mValue;
        float mVelocity;
    }

    /* loaded from: classes.dex */
    public interface OnAnimationEndListener {
        void onAnimationEnd(DynamicAnimation dynamicAnimation, boolean z, float f, float f2);
    }

    /* loaded from: classes.dex */
    public interface OnAnimationUpdateListener {
        void onAnimationUpdate(DynamicAnimation dynamicAnimation, float f, float f2);
    }

    abstract float getAcceleration(float f, float f2);

    abstract boolean isAtEquilibrium(float f, float f2);

    abstract void setValueThreshold(float f);

    abstract boolean updateValueAndVelocity(long j);

    /* loaded from: classes.dex */
    public static abstract class ViewProperty extends FloatPropertyCompat<View> {
        private ViewProperty(String name) {
            super(name);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public DynamicAnimation(final FloatValueHolder floatValueHolder) {
        this.mVelocity = 0.0f;
        this.mValue = Float.MAX_VALUE;
        this.mStartValueIsSet = false;
        this.mRunning = false;
        this.mMaxValue = Float.MAX_VALUE;
        this.mMinValue = -this.mMaxValue;
        this.mLastFrameTime = 0L;
        this.mEndListeners = new ArrayList<>();
        this.mUpdateListeners = new ArrayList<>();
        this.mTarget = null;
        this.mProperty = new FloatPropertyCompat("FloatValueHolder") { // from class: android.support.animation.DynamicAnimation.15
            @Override // android.support.animation.FloatPropertyCompat
            public float getValue(Object object) {
                return floatValueHolder.getValue();
            }

            @Override // android.support.animation.FloatPropertyCompat
            public void setValue(Object object, float value) {
                floatValueHolder.setValue(value);
            }
        };
        this.mMinVisibleChange = 1.0f;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public <K> DynamicAnimation(K object, FloatPropertyCompat<K> property) {
        this.mVelocity = 0.0f;
        this.mValue = Float.MAX_VALUE;
        this.mStartValueIsSet = false;
        this.mRunning = false;
        this.mMaxValue = Float.MAX_VALUE;
        this.mMinValue = -this.mMaxValue;
        this.mLastFrameTime = 0L;
        this.mEndListeners = new ArrayList<>();
        this.mUpdateListeners = new ArrayList<>();
        this.mTarget = object;
        this.mProperty = property;
        if (this.mProperty == ROTATION || this.mProperty == ROTATION_X || this.mProperty == ROTATION_Y) {
            this.mMinVisibleChange = 0.1f;
        } else if (this.mProperty == ALPHA) {
            this.mMinVisibleChange = 0.00390625f;
        } else if (this.mProperty == SCALE_X || this.mProperty == SCALE_Y) {
            this.mMinVisibleChange = 0.00390625f;
        } else {
            this.mMinVisibleChange = 1.0f;
        }
    }

    public T setStartValue(float startValue) {
        this.mValue = startValue;
        this.mStartValueIsSet = true;
        return this;
    }

    public T setStartVelocity(float startVelocity) {
        this.mVelocity = startVelocity;
        return this;
    }

    public T setMaxValue(float max) {
        this.mMaxValue = max;
        return this;
    }

    public T setMinValue(float min) {
        this.mMinValue = min;
        return this;
    }

    public T addEndListener(OnAnimationEndListener listener) {
        if (!this.mEndListeners.contains(listener)) {
            this.mEndListeners.add(listener);
        }
        return this;
    }

    public void removeEndListener(OnAnimationEndListener listener) {
        removeEntry(this.mEndListeners, listener);
    }

    public T addUpdateListener(OnAnimationUpdateListener listener) {
        if (isRunning()) {
            throw new UnsupportedOperationException("Error: Update listeners must be added beforethe animation.");
        }
        if (!this.mUpdateListeners.contains(listener)) {
            this.mUpdateListeners.add(listener);
        }
        return this;
    }

    public void removeUpdateListener(OnAnimationUpdateListener listener) {
        removeEntry(this.mUpdateListeners, listener);
    }

    public T setMinimumVisibleChange(@FloatRange(from = 0.0d, fromInclusive = false) float minimumVisibleChange) {
        if (minimumVisibleChange <= 0.0f) {
            throw new IllegalArgumentException("Minimum visible change must be positive.");
        }
        this.mMinVisibleChange = minimumVisibleChange;
        setValueThreshold(0.75f * minimumVisibleChange);
        return this;
    }

    public float getMinimumVisibleChange() {
        return this.mMinVisibleChange;
    }

    private static <T> void removeNullEntries(ArrayList<T> list) {
        for (int i = list.size() - 1; i >= 0; i--) {
            if (list.get(i) == null) {
                list.remove(i);
            }
        }
    }

    private static <T> void removeEntry(ArrayList<T> list, T entry) {
        int id = list.indexOf(entry);
        if (id >= 0) {
            list.set(id, null);
        }
    }

    public void start() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new AndroidRuntimeException("Animations may only be started on the main thread");
        }
        if (!this.mRunning) {
            startAnimationInternal();
        }
    }

    public void cancel() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new AndroidRuntimeException("Animations may only be canceled on the main thread");
        }
        if (this.mRunning) {
            endAnimationInternal(true);
        }
    }

    public boolean isRunning() {
        return this.mRunning;
    }

    private void startAnimationInternal() {
        if (!this.mRunning) {
            this.mRunning = true;
            if (!this.mStartValueIsSet) {
                this.mValue = getPropertyValue();
            }
            if (this.mValue > this.mMaxValue || this.mValue < this.mMinValue) {
                throw new IllegalArgumentException("Starting value need to be in between min value and max value");
            }
            AnimationHandler.getInstance().addAnimationFrameCallback(this, 0L);
        }
    }

    @Override // android.support.animation.AnimationHandler.AnimationFrameCallback
    @RestrictTo({RestrictTo.Scope.LIBRARY})
    public boolean doAnimationFrame(long frameTime) {
        if (this.mLastFrameTime == 0) {
            this.mLastFrameTime = frameTime;
            setPropertyValue(this.mValue);
            return false;
        }
        long deltaT = frameTime - this.mLastFrameTime;
        this.mLastFrameTime = frameTime;
        boolean finished = updateValueAndVelocity(deltaT);
        this.mValue = Math.min(this.mValue, this.mMaxValue);
        this.mValue = Math.max(this.mValue, this.mMinValue);
        setPropertyValue(this.mValue);
        if (finished) {
            endAnimationInternal(false);
        }
        return finished;
    }

    private void endAnimationInternal(boolean canceled) {
        this.mRunning = false;
        AnimationHandler.getInstance().removeCallback(this);
        this.mLastFrameTime = 0L;
        this.mStartValueIsSet = false;
        for (int i = 0; i < this.mEndListeners.size(); i++) {
            if (this.mEndListeners.get(i) != null) {
                this.mEndListeners.get(i).onAnimationEnd(this, canceled, this.mValue, this.mVelocity);
            }
        }
        removeNullEntries(this.mEndListeners);
    }

    void setPropertyValue(float value) {
        this.mProperty.setValue(this.mTarget, value);
        for (int i = 0; i < this.mUpdateListeners.size(); i++) {
            if (this.mUpdateListeners.get(i) != null) {
                this.mUpdateListeners.get(i).onAnimationUpdate(this, this.mValue, this.mVelocity);
            }
        }
        removeNullEntries(this.mUpdateListeners);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public float getValueThreshold() {
        return this.mMinVisibleChange * 0.75f;
    }

    private float getPropertyValue() {
        return this.mProperty.getValue(this.mTarget);
    }
}

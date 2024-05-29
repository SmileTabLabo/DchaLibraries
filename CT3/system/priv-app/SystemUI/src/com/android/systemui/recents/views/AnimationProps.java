package com.android.systemui.recents.views;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.util.SparseArray;
import android.util.SparseLongArray;
import android.view.animation.Interpolator;
import com.android.systemui.Interpolators;
import java.util.List;
/* loaded from: a.zip:com/android/systemui/recents/views/AnimationProps.class */
public class AnimationProps {
    public static final AnimationProps IMMEDIATE = new AnimationProps(0, Interpolators.LINEAR);
    private Animator.AnimatorListener mListener;
    private SparseLongArray mPropDuration;
    private SparseLongArray mPropInitialPlayTime;
    private SparseArray<Interpolator> mPropInterpolators;
    private SparseLongArray mPropStartDelay;

    public AnimationProps() {
    }

    public AnimationProps(int i, int i2, Interpolator interpolator) {
        this(i, i2, interpolator, null);
    }

    public AnimationProps(int i, int i2, Interpolator interpolator, Animator.AnimatorListener animatorListener) {
        setStartDelay(0, i);
        setDuration(0, i2);
        setInterpolator(0, interpolator);
        setListener(animatorListener);
    }

    public AnimationProps(int i, Interpolator interpolator) {
        this(0, i, interpolator, null);
    }

    public AnimationProps(int i, Interpolator interpolator, Animator.AnimatorListener animatorListener) {
        this(0, i, interpolator, animatorListener);
    }

    public <T extends ValueAnimator> T apply(int i, T t) {
        t.setStartDelay(getStartDelay(i));
        t.setDuration(getDuration(i));
        t.setInterpolator(getInterpolator(i));
        long initialPlayTime = getInitialPlayTime(i);
        if (initialPlayTime != 0) {
            t.setCurrentPlayTime(initialPlayTime);
        }
        return t;
    }

    public AnimatorSet createAnimator(List<Animator> list) {
        AnimatorSet animatorSet = new AnimatorSet();
        if (this.mListener != null) {
            animatorSet.addListener(this.mListener);
        }
        animatorSet.playTogether(list);
        return animatorSet;
    }

    public long getDuration(int i) {
        if (this.mPropDuration != null) {
            long j = this.mPropDuration.get(i, -1L);
            return j != -1 ? j : this.mPropDuration.get(0, 0L);
        }
        return 0L;
    }

    public long getInitialPlayTime(int i) {
        if (this.mPropInitialPlayTime != null) {
            return this.mPropInitialPlayTime.indexOfKey(i) != -1 ? this.mPropInitialPlayTime.get(i) : this.mPropInitialPlayTime.get(0, 0L);
        }
        return 0L;
    }

    public Interpolator getInterpolator(int i) {
        if (this.mPropInterpolators != null) {
            Interpolator interpolator = this.mPropInterpolators.get(i);
            return interpolator != null ? interpolator : this.mPropInterpolators.get(0, Interpolators.LINEAR);
        }
        return Interpolators.LINEAR;
    }

    public Animator.AnimatorListener getListener() {
        return this.mListener;
    }

    public long getStartDelay(int i) {
        if (this.mPropStartDelay != null) {
            long j = this.mPropStartDelay.get(i, -1L);
            return j != -1 ? j : this.mPropStartDelay.get(0, 0L);
        }
        return 0L;
    }

    public boolean isImmediate() {
        int size = this.mPropDuration.size();
        for (int i = 0; i < size; i++) {
            if (this.mPropDuration.valueAt(i) > 0) {
                return false;
            }
        }
        return true;
    }

    public AnimationProps setDuration(int i, int i2) {
        if (this.mPropDuration == null) {
            this.mPropDuration = new SparseLongArray();
        }
        this.mPropDuration.append(i, i2);
        return this;
    }

    public AnimationProps setInitialPlayTime(int i, int i2) {
        if (this.mPropInitialPlayTime == null) {
            this.mPropInitialPlayTime = new SparseLongArray();
        }
        this.mPropInitialPlayTime.append(i, i2);
        return this;
    }

    public AnimationProps setInterpolator(int i, Interpolator interpolator) {
        if (this.mPropInterpolators == null) {
            this.mPropInterpolators = new SparseArray<>();
        }
        this.mPropInterpolators.append(i, interpolator);
        return this;
    }

    public AnimationProps setListener(Animator.AnimatorListener animatorListener) {
        this.mListener = animatorListener;
        return this;
    }

    public AnimationProps setStartDelay(int i, int i2) {
        if (this.mPropStartDelay == null) {
            this.mPropStartDelay = new SparseLongArray();
        }
        this.mPropStartDelay.append(i, i2);
        return this;
    }
}

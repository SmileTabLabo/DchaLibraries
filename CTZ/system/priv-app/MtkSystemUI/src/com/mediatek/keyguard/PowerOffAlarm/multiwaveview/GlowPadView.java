package com.mediatek.keyguard.PowerOffAlarm.multiwaveview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import com.android.keyguard.R;
import com.mediatek.keyguard.PowerOffAlarm.multiwaveview.Ease;
import java.util.ArrayList;
/* loaded from: classes.dex */
public class GlowPadView extends View {
    private int mActiveTarget;
    private boolean mAllowScaling;
    private boolean mAlwaysTrackFinger;
    private boolean mAnimatingTargets;
    private Tweener mBackgroundAnimator;
    private ArrayList<String> mDirectionDescriptions;
    private int mDirectionDescriptionsResourceId;
    private boolean mDragging;
    private int mFeedbackCount;
    private AnimationBundle mGlowAnimations;
    private float mGlowRadius;
    private int mGrabbedState;
    private int mGravity;
    private TargetDrawable mHandleDrawable;
    private int mHorizontalInset;
    private boolean mInitialLayout;
    private float mInnerRadius;
    private int mMaxTargetHeight;
    private int mMaxTargetWidth;
    private int mNewTargetResources;
    private OnTriggerListener mOnTriggerListener;
    private float mOuterRadius;
    private TargetDrawable mOuterRing;
    private PointCloud mPointCloud;
    private int mPointerId;
    private Animator.AnimatorListener mResetListener;
    private Animator.AnimatorListener mResetListenerWithPing;
    private float mRingScaleFactor;
    private float mSnapMargin;
    private AnimationBundle mTargetAnimations;
    private ArrayList<String> mTargetDescriptions;
    private int mTargetDescriptionsResourceId;
    private ArrayList<TargetDrawable> mTargetDrawables;
    private int mTargetResourceId;
    private Animator.AnimatorListener mTargetUpdateListener;
    private ValueAnimator.AnimatorUpdateListener mUpdateListener;
    private int mVerticalInset;
    private int mVibrationDuration;
    private Vibrator mVibrator;
    private AnimationBundle mWaveAnimations;
    private float mWaveCenterX;
    private float mWaveCenterY;

    /* loaded from: classes.dex */
    public interface OnTriggerListener {
        void onFinishFinalAnimation();

        void onGrabbed(View view, int i);

        void onGrabbedStateChange(View view, int i);

        void onReleased(View view, int i);

        void onTrigger(View view, int i);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class AnimationBundle extends ArrayList<Tweener> {
        private static final long serialVersionUID = -6319262269245852568L;
        private boolean mSuspended;

        private AnimationBundle() {
        }

        public void start() {
            if (this.mSuspended) {
                return;
            }
            int size = size();
            for (int i = 0; i < size; i++) {
                get(i).animator.start();
            }
        }

        public void cancel() {
            int size = size();
            for (int i = 0; i < size; i++) {
                get(i).animator.cancel();
            }
            clear();
        }

        public void stop() {
            int size = size();
            for (int i = 0; i < size; i++) {
                get(i).animator.end();
            }
            clear();
        }
    }

    public GlowPadView(Context context) {
        this(context, null);
    }

    public GlowPadView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mTargetDrawables = new ArrayList<>();
        this.mWaveAnimations = new AnimationBundle();
        this.mTargetAnimations = new AnimationBundle();
        this.mGlowAnimations = new AnimationBundle();
        this.mFeedbackCount = 3;
        this.mVibrationDuration = 0;
        this.mActiveTarget = -1;
        this.mRingScaleFactor = 1.0f;
        this.mOuterRadius = 0.0f;
        this.mSnapMargin = 0.0f;
        this.mResetListener = new AnimatorListenerAdapter() { // from class: com.mediatek.keyguard.PowerOffAlarm.multiwaveview.GlowPadView.1
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                GlowPadView.this.switchToState(0, GlowPadView.this.mWaveCenterX, GlowPadView.this.mWaveCenterY);
                GlowPadView.this.dispatchOnFinishFinalAnimation();
            }
        };
        this.mResetListenerWithPing = new AnimatorListenerAdapter() { // from class: com.mediatek.keyguard.PowerOffAlarm.multiwaveview.GlowPadView.2
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                GlowPadView.this.ping();
                GlowPadView.this.switchToState(0, GlowPadView.this.mWaveCenterX, GlowPadView.this.mWaveCenterY);
                GlowPadView.this.dispatchOnFinishFinalAnimation();
            }
        };
        this.mUpdateListener = new ValueAnimator.AnimatorUpdateListener() { // from class: com.mediatek.keyguard.PowerOffAlarm.multiwaveview.GlowPadView.3
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                GlowPadView.this.invalidate();
            }
        };
        this.mTargetUpdateListener = new AnimatorListenerAdapter() { // from class: com.mediatek.keyguard.PowerOffAlarm.multiwaveview.GlowPadView.4
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (GlowPadView.this.mNewTargetResources != 0) {
                    GlowPadView.this.internalSetTargetResources(GlowPadView.this.mNewTargetResources);
                    GlowPadView.this.mNewTargetResources = 0;
                    GlowPadView.this.hideTargets(false, false);
                }
                GlowPadView.this.mAnimatingTargets = false;
            }
        };
        this.mGravity = 48;
        this.mInitialLayout = true;
        Resources resources = context.getResources();
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.GlowPadView);
        this.mInnerRadius = obtainStyledAttributes.getDimension(7, this.mInnerRadius);
        this.mOuterRadius = obtainStyledAttributes.getDimension(8, this.mOuterRadius);
        this.mSnapMargin = obtainStyledAttributes.getDimension(11, this.mSnapMargin);
        this.mVibrationDuration = obtainStyledAttributes.getInt(14, this.mVibrationDuration);
        this.mFeedbackCount = obtainStyledAttributes.getInt(4, this.mFeedbackCount);
        this.mAllowScaling = obtainStyledAttributes.getBoolean(1, false);
        TypedValue peekValue = obtainStyledAttributes.peekValue(6);
        this.mHandleDrawable = new TargetDrawable(resources, peekValue != null ? peekValue.resourceId : 0, 1);
        this.mHandleDrawable.setState(TargetDrawable.STATE_INACTIVE);
        this.mOuterRing = new TargetDrawable(resources, getResourceId(obtainStyledAttributes, 9), 1);
        this.mAlwaysTrackFinger = obtainStyledAttributes.getBoolean(2, false);
        int resourceId = getResourceId(obtainStyledAttributes, 10);
        Drawable drawable = resourceId != 0 ? resources.getDrawable(resourceId) : null;
        this.mGlowRadius = obtainStyledAttributes.getDimension(5, 0.0f);
        TypedValue typedValue = new TypedValue();
        if (obtainStyledAttributes.getValue(13, typedValue)) {
            internalSetTargetResources(typedValue.resourceId);
        }
        if (this.mTargetDrawables == null || this.mTargetDrawables.size() == 0) {
            throw new IllegalStateException("Must specify at least one target drawable");
        }
        if (obtainStyledAttributes.getValue(12, typedValue)) {
            int i = typedValue.resourceId;
            if (i == 0) {
                throw new IllegalStateException("Must specify target descriptions");
            }
            setTargetDescriptionsResourceId(i);
        }
        if (obtainStyledAttributes.getValue(3, typedValue)) {
            int i2 = typedValue.resourceId;
            if (i2 == 0) {
                throw new IllegalStateException("Must specify direction descriptions");
            }
            setDirectionDescriptionsResourceId(i2);
        }
        this.mGravity = obtainStyledAttributes.getInt(0, 48);
        obtainStyledAttributes.recycle();
        setVibrateEnabled(this.mVibrationDuration > 0);
        assignDefaultsIfNeeded();
        this.mPointCloud = new PointCloud(drawable);
        this.mPointCloud.makePointCloud(this.mInnerRadius, this.mOuterRadius);
        this.mPointCloud.glowManager.setRadius(this.mGlowRadius);
    }

    private int getResourceId(TypedArray typedArray, int i) {
        TypedValue peekValue = typedArray.peekValue(i);
        if (peekValue == null) {
            return 0;
        }
        return peekValue.resourceId;
    }

    @Override // android.view.View
    protected int getSuggestedMinimumWidth() {
        return (int) (Math.max(this.mOuterRing.getWidth(), 2.0f * this.mOuterRadius) + this.mMaxTargetWidth);
    }

    @Override // android.view.View
    protected int getSuggestedMinimumHeight() {
        return (int) (Math.max(this.mOuterRing.getHeight(), 2.0f * this.mOuterRadius) + this.mMaxTargetHeight);
    }

    protected int getScaledSuggestedMinimumWidth() {
        return (int) ((this.mRingScaleFactor * Math.max(this.mOuterRing.getWidth(), 2.0f * this.mOuterRadius)) + this.mMaxTargetWidth);
    }

    protected int getScaledSuggestedMinimumHeight() {
        return (int) ((this.mRingScaleFactor * Math.max(this.mOuterRing.getHeight(), 2.0f * this.mOuterRadius)) + this.mMaxTargetHeight);
    }

    private int resolveMeasured(int i, int i2) {
        int size = View.MeasureSpec.getSize(i);
        int mode = View.MeasureSpec.getMode(i);
        if (mode != Integer.MIN_VALUE) {
            return mode != 0 ? size : i2;
        }
        return Math.min(size, i2);
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        int suggestedMinimumWidth = getSuggestedMinimumWidth();
        int suggestedMinimumHeight = getSuggestedMinimumHeight();
        int resolveMeasured = resolveMeasured(i, suggestedMinimumWidth);
        int resolveMeasured2 = resolveMeasured(i2, suggestedMinimumHeight);
        this.mRingScaleFactor = computeScaleFactor(suggestedMinimumWidth, suggestedMinimumHeight, resolveMeasured, resolveMeasured2);
        computeInsets(resolveMeasured - getScaledSuggestedMinimumWidth(), resolveMeasured2 - getScaledSuggestedMinimumHeight());
        setMeasuredDimension(resolveMeasured, resolveMeasured2);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void switchToState(int i, float f, float f2) {
        switch (i) {
            case 0:
                deactivateTargets();
                hideTargets(false, false);
                hideGlow(0, 0, 0.0f, null);
                startBackgroundAnimation(0, 0.0f);
                this.mHandleDrawable.setState(TargetDrawable.STATE_INACTIVE);
                this.mHandleDrawable.setAlpha(1.0f);
                return;
            case 1:
                startBackgroundAnimation(0, 0.0f);
                return;
            case 2:
                this.mHandleDrawable.setAlpha(0.0f);
                deactivateTargets();
                showTargets(true);
                startBackgroundAnimation(200, 1.0f);
                setGrabbedState(1);
                if (((AccessibilityManager) getContext().getSystemService("accessibility")).isEnabled()) {
                    announceTargets();
                    return;
                }
                return;
            case 3:
                this.mHandleDrawable.setAlpha(0.0f);
                return;
            case 4:
                this.mHandleDrawable.setAlpha(0.0f);
                showGlow(0, 0, 0.0f, null);
                return;
            case 5:
                doFinish();
                return;
            default:
                return;
        }
    }

    private void showGlow(int i, int i2, float f, Animator.AnimatorListener animatorListener) {
        this.mGlowAnimations.cancel();
        this.mGlowAnimations.add(Tweener.to(this.mPointCloud.glowManager, i, "ease", Ease.Cubic.easeIn, "delay", Integer.valueOf(i2), "alpha", Float.valueOf(f), "onUpdate", this.mUpdateListener, "onComplete", animatorListener));
        this.mGlowAnimations.start();
    }

    private void hideGlow(int i, int i2, float f, Animator.AnimatorListener animatorListener) {
        this.mGlowAnimations.cancel();
        this.mGlowAnimations.add(Tweener.to(this.mPointCloud.glowManager, i, "ease", Ease.Quart.easeOut, "delay", Integer.valueOf(i2), "alpha", Float.valueOf(f), "x", Float.valueOf(0.0f), "y", Float.valueOf(0.0f), "onUpdate", this.mUpdateListener, "onComplete", animatorListener));
        this.mGlowAnimations.start();
    }

    private void deactivateTargets() {
        int size = this.mTargetDrawables.size();
        for (int i = 0; i < size; i++) {
            this.mTargetDrawables.get(i).setState(TargetDrawable.STATE_INACTIVE);
        }
        this.mActiveTarget = -1;
    }

    private void dispatchTriggerEvent(int i) {
        vibrate();
        if (this.mOnTriggerListener != null) {
            this.mOnTriggerListener.onTrigger(this, i);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dispatchOnFinishFinalAnimation() {
        if (this.mOnTriggerListener != null) {
            this.mOnTriggerListener.onFinishFinalAnimation();
        }
    }

    private void doFinish() {
        int i = this.mActiveTarget;
        if (i != -1) {
            highlightSelected(i);
            hideGlow(200, 1200, 0.0f, this.mResetListener);
            dispatchTriggerEvent(i);
            if (!this.mAlwaysTrackFinger) {
                this.mTargetAnimations.stop();
            }
        } else {
            hideGlow(200, 0, 0.0f, this.mResetListenerWithPing);
            hideTargets(true, false);
        }
        setGrabbedState(0);
    }

    private void highlightSelected(int i) {
        this.mTargetDrawables.get(i).setState(TargetDrawable.STATE_ACTIVE);
        hideUnselected(i);
    }

    private void hideUnselected(int i) {
        for (int i2 = 0; i2 < this.mTargetDrawables.size(); i2++) {
            if (i2 != i) {
                this.mTargetDrawables.get(i2).setAlpha(0.0f);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void hideTargets(boolean z, boolean z2) {
        float f;
        this.mTargetAnimations.cancel();
        this.mAnimatingTargets = z;
        int i = 200;
        int i2 = z ? 200 : 0;
        if (!z) {
            i = 0;
        }
        if (!z2) {
            f = 0.8f;
        } else {
            f = 1.0f;
        }
        int size = this.mTargetDrawables.size();
        TimeInterpolator timeInterpolator = Ease.Cubic.easeOut;
        for (int i3 = 0; i3 < size; i3++) {
            TargetDrawable targetDrawable = this.mTargetDrawables.get(i3);
            targetDrawable.setState(TargetDrawable.STATE_INACTIVE);
            this.mTargetAnimations.add(Tweener.to(targetDrawable, i2, "ease", timeInterpolator, "alpha", Float.valueOf(0.0f), "scaleX", Float.valueOf(f), "scaleY", Float.valueOf(f), "delay", Integer.valueOf(i), "onUpdate", this.mUpdateListener));
        }
        float f2 = this.mRingScaleFactor * (z2 ? 1.0f : 0.5f);
        this.mTargetAnimations.add(Tweener.to(this.mOuterRing, i2, "ease", timeInterpolator, "alpha", Float.valueOf(0.0f), "scaleX", Float.valueOf(f2), "scaleY", Float.valueOf(f2), "delay", Integer.valueOf(i), "onUpdate", this.mUpdateListener, "onComplete", this.mTargetUpdateListener));
        this.mTargetAnimations.start();
    }

    private void showTargets(boolean z) {
        this.mTargetAnimations.stop();
        this.mAnimatingTargets = z;
        int i = z ? 50 : 0;
        int i2 = z ? 200 : 0;
        int size = this.mTargetDrawables.size();
        for (int i3 = 0; i3 < size; i3++) {
            TargetDrawable targetDrawable = this.mTargetDrawables.get(i3);
            targetDrawable.setState(TargetDrawable.STATE_INACTIVE);
            this.mTargetAnimations.add(Tweener.to(targetDrawable, i2, "ease", Ease.Cubic.easeOut, "alpha", Float.valueOf(1.0f), "scaleX", Float.valueOf(1.0f), "scaleY", Float.valueOf(1.0f), "delay", Integer.valueOf(i), "onUpdate", this.mUpdateListener));
        }
        float f = this.mRingScaleFactor * 1.0f;
        this.mTargetAnimations.add(Tweener.to(this.mOuterRing, i2, "ease", Ease.Cubic.easeOut, "alpha", Float.valueOf(1.0f), "scaleX", Float.valueOf(f), "scaleY", Float.valueOf(f), "delay", Integer.valueOf(i), "onUpdate", this.mUpdateListener, "onComplete", this.mTargetUpdateListener));
        this.mTargetAnimations.start();
    }

    private void vibrate() {
        if (this.mVibrator != null) {
            this.mVibrator.vibrate(this.mVibrationDuration);
        }
    }

    private ArrayList<TargetDrawable> loadDrawableArray(int i) {
        Resources resources = getContext().getResources();
        TypedArray obtainTypedArray = resources.obtainTypedArray(i);
        int length = obtainTypedArray.length();
        ArrayList<TargetDrawable> arrayList = new ArrayList<>(length);
        for (int i2 = 0; i2 < length; i2++) {
            TypedValue peekValue = obtainTypedArray.peekValue(i2);
            arrayList.add(new TargetDrawable(resources, peekValue != null ? peekValue.resourceId : 0, 3));
        }
        obtainTypedArray.recycle();
        return arrayList;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void internalSetTargetResources(int i) {
        ArrayList<TargetDrawable> loadDrawableArray = loadDrawableArray(i);
        this.mTargetDrawables = loadDrawableArray;
        this.mTargetResourceId = i;
        int width = this.mHandleDrawable.getWidth();
        int height = this.mHandleDrawable.getHeight();
        int size = loadDrawableArray.size();
        for (int i2 = 0; i2 < size; i2++) {
            TargetDrawable targetDrawable = loadDrawableArray.get(i2);
            width = Math.max(width, targetDrawable.getWidth());
            height = Math.max(height, targetDrawable.getHeight());
        }
        if (this.mMaxTargetWidth != width || this.mMaxTargetHeight != height) {
            this.mMaxTargetWidth = width;
            this.mMaxTargetHeight = height;
            requestLayout();
            return;
        }
        updateTargetPositions(this.mWaveCenterX, this.mWaveCenterY);
        updatePointCloudPosition(this.mWaveCenterX, this.mWaveCenterY);
    }

    public void setTargetDescriptionsResourceId(int i) {
        this.mTargetDescriptionsResourceId = i;
        if (this.mTargetDescriptions != null) {
            this.mTargetDescriptions.clear();
        }
    }

    public void setDirectionDescriptionsResourceId(int i) {
        this.mDirectionDescriptionsResourceId = i;
        if (this.mDirectionDescriptions != null) {
            this.mDirectionDescriptions.clear();
        }
    }

    public void setVibrateEnabled(boolean z) {
        if (z && this.mVibrator == null) {
            this.mVibrator = (Vibrator) getContext().getSystemService("vibrator");
        } else {
            this.mVibrator = null;
        }
    }

    public void ping() {
        if (this.mFeedbackCount > 0) {
            boolean z = true;
            AnimationBundle animationBundle = this.mWaveAnimations;
            if (animationBundle.size() > 0 && animationBundle.get(0).animator.isRunning() && animationBundle.get(0).animator.getCurrentPlayTime() < 675) {
                z = false;
            }
            if (z) {
                startWaveAnimation();
            }
        }
    }

    private void stopAndHideWaveAnimation() {
        this.mWaveAnimations.cancel();
        this.mPointCloud.waveManager.setAlpha(0.0f);
    }

    private void startWaveAnimation() {
        this.mWaveAnimations.cancel();
        this.mPointCloud.waveManager.setAlpha(1.0f);
        this.mPointCloud.waveManager.setRadius(this.mHandleDrawable.getWidth() / 2.0f);
        this.mWaveAnimations.add(Tweener.to(this.mPointCloud.waveManager, 1350L, "ease", Ease.Quad.easeOut, "delay", 0, "radius", Float.valueOf(2.0f * this.mOuterRadius), "onUpdate", this.mUpdateListener, "onComplete", new AnimatorListenerAdapter() { // from class: com.mediatek.keyguard.PowerOffAlarm.multiwaveview.GlowPadView.5
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                GlowPadView.this.mPointCloud.waveManager.setRadius(0.0f);
                GlowPadView.this.mPointCloud.waveManager.setAlpha(0.0f);
            }
        }));
        this.mWaveAnimations.start();
    }

    private void startBackgroundAnimation(int i, float f) {
        Drawable background = getBackground();
        if (this.mAlwaysTrackFinger && background != null) {
            if (this.mBackgroundAnimator != null) {
                this.mBackgroundAnimator.animator.cancel();
            }
            this.mBackgroundAnimator = Tweener.to(background, i, "ease", Ease.Cubic.easeIn, "alpha", Integer.valueOf((int) (255.0f * f)), "delay", 50);
            this.mBackgroundAnimator.animator.start();
        }
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        boolean z;
        switch (motionEvent.getActionMasked()) {
            case 0:
            case 5:
                handleDown(motionEvent);
                handleMove(motionEvent);
                z = true;
                break;
            case 1:
            case 6:
                handleMove(motionEvent);
                handleUp(motionEvent);
                z = true;
                break;
            case 2:
                handleMove(motionEvent);
                z = true;
                break;
            case 3:
                handleMove(motionEvent);
                handleCancel(motionEvent);
                z = true;
                break;
            case 4:
            default:
                z = false;
                break;
        }
        invalidate();
        if (z) {
            return true;
        }
        return super.onTouchEvent(motionEvent);
    }

    private void updateGlowPosition(float f, float f2) {
        float x = f - this.mOuterRing.getX();
        float y = f2 - this.mOuterRing.getY();
        float f3 = x * (1.0f / this.mRingScaleFactor);
        float f4 = y * (1.0f / this.mRingScaleFactor);
        this.mPointCloud.glowManager.setX(this.mOuterRing.getX() + f3);
        this.mPointCloud.glowManager.setY(this.mOuterRing.getY() + f4);
    }

    private void handleDown(MotionEvent motionEvent) {
        int actionIndex = motionEvent.getActionIndex();
        float x = motionEvent.getX(actionIndex);
        float y = motionEvent.getY(actionIndex);
        switchToState(1, x, y);
        if (!trySwitchToFirstTouchState(x, y)) {
            this.mDragging = false;
            return;
        }
        this.mPointerId = motionEvent.getPointerId(actionIndex);
        updateGlowPosition(x, y);
    }

    private void handleUp(MotionEvent motionEvent) {
        int actionIndex = motionEvent.getActionIndex();
        if (motionEvent.getPointerId(actionIndex) == this.mPointerId) {
            switchToState(5, motionEvent.getX(actionIndex), motionEvent.getY(actionIndex));
        }
    }

    private void handleCancel(MotionEvent motionEvent) {
        int findPointerIndex = motionEvent.findPointerIndex(this.mPointerId);
        if (findPointerIndex == -1) {
            findPointerIndex = 0;
        }
        switchToState(5, motionEvent.getX(findPointerIndex), motionEvent.getY(findPointerIndex));
    }

    private void handleMove(MotionEvent motionEvent) {
        boolean z;
        MotionEvent motionEvent2 = motionEvent;
        int historySize = motionEvent.getHistorySize();
        ArrayList<TargetDrawable> arrayList = this.mTargetDrawables;
        int size = arrayList.size();
        int findPointerIndex = motionEvent2.findPointerIndex(this.mPointerId);
        if (findPointerIndex != -1) {
            int i = -1;
            float f = 0.0f;
            float f2 = 0.0f;
            int i2 = 0;
            while (i2 < historySize + 1) {
                float historicalX = i2 < historySize ? motionEvent2.getHistoricalX(findPointerIndex, i2) : motionEvent2.getX(findPointerIndex);
                float historicalY = i2 < historySize ? motionEvent2.getHistoricalY(findPointerIndex, i2) : motionEvent2.getY(findPointerIndex);
                float f3 = historicalX - this.mWaveCenterX;
                float f4 = historicalY - this.mWaveCenterY;
                double d = f3;
                int i3 = i;
                float hypot = (float) Math.hypot(d, f4);
                float f5 = hypot > this.mOuterRadius ? this.mOuterRadius / hypot : 1.0f;
                float f6 = f3 * f5;
                float f7 = f5 * f4;
                int i4 = historySize;
                double atan2 = Math.atan2(-f4, d);
                if (!this.mDragging) {
                    trySwitchToFirstTouchState(historicalX, historicalY);
                }
                if (this.mDragging) {
                    float f8 = (this.mRingScaleFactor * this.mOuterRadius) - this.mSnapMargin;
                    float f9 = f8 * f8;
                    int i5 = 0;
                    while (i5 < size) {
                        double d2 = i5;
                        float f10 = f6;
                        float f11 = f7;
                        double d3 = size;
                        double d4 = (((d2 - 0.5d) * 2.0d) * 3.141592653589793d) / d3;
                        double d5 = (((d2 + 0.5d) * 2.0d) * 3.141592653589793d) / d3;
                        if (arrayList.get(i5).isEnabled()) {
                            if (atan2 <= d4 || atan2 > d5) {
                                double d6 = 6.283185307179586d + atan2;
                                if (d6 <= d4 || d6 > d5) {
                                    z = false;
                                    if (z && dist2(f3, f4) > f9) {
                                        i3 = i5;
                                    }
                                }
                            }
                            z = true;
                            if (z) {
                                i3 = i5;
                            }
                        }
                        i5++;
                        f6 = f10;
                        f7 = f11;
                    }
                }
                float f12 = f7;
                i = i3;
                i2++;
                historySize = i4;
                f = f6;
                f2 = f12;
                motionEvent2 = motionEvent;
            }
            int i6 = i;
            if (!this.mDragging) {
                return;
            }
            if (i6 != -1) {
                switchToState(4, f, f2);
                updateGlowPosition(f, f2);
            } else {
                switchToState(3, f, f2);
                updateGlowPosition(f, f2);
            }
            if (this.mActiveTarget != i6) {
                if (this.mActiveTarget != -1) {
                    arrayList.get(this.mActiveTarget).setState(TargetDrawable.STATE_INACTIVE);
                }
                if (i6 != -1) {
                    arrayList.get(i6).setState(TargetDrawable.STATE_FOCUSED);
                    if (((AccessibilityManager) getContext().getSystemService("accessibility")).isEnabled()) {
                        announceForAccessibility(getTargetDescription(i6));
                    }
                }
            }
            this.mActiveTarget = i6;
        }
    }

    @Override // android.view.View
    public boolean onHoverEvent(MotionEvent motionEvent) {
        if (((AccessibilityManager) getContext().getSystemService("accessibility")).isTouchExplorationEnabled()) {
            int action = motionEvent.getAction();
            if (action != 7) {
                switch (action) {
                    case 9:
                        motionEvent.setAction(0);
                        break;
                    case 10:
                        motionEvent.setAction(1);
                        break;
                }
            } else {
                motionEvent.setAction(2);
            }
            onTouchEvent(motionEvent);
            motionEvent.setAction(action);
        }
        super.onHoverEvent(motionEvent);
        return true;
    }

    private void setGrabbedState(int i) {
        if (i != this.mGrabbedState) {
            if (i != 0) {
                vibrate();
            }
            this.mGrabbedState = i;
            if (this.mOnTriggerListener != null) {
                if (i == 0) {
                    this.mOnTriggerListener.onReleased(this, 1);
                } else {
                    this.mOnTriggerListener.onGrabbed(this, 1);
                }
                this.mOnTriggerListener.onGrabbedStateChange(this, i);
            }
        }
    }

    private boolean trySwitchToFirstTouchState(float f, float f2) {
        float f3 = f - this.mWaveCenterX;
        float f4 = f2 - this.mWaveCenterY;
        if (this.mAlwaysTrackFinger || dist2(f3, f4) <= getScaledGlowRadiusSquared()) {
            switchToState(2, f, f2);
            updateGlowPosition(f3, f4);
            this.mDragging = true;
            return true;
        }
        return false;
    }

    private void assignDefaultsIfNeeded() {
        if (this.mOuterRadius == 0.0f) {
            this.mOuterRadius = Math.max(this.mOuterRing.getWidth(), this.mOuterRing.getHeight()) / 2.0f;
        }
        if (this.mSnapMargin == 0.0f) {
            this.mSnapMargin = TypedValue.applyDimension(1, 20.0f, getContext().getResources().getDisplayMetrics());
        }
        if (this.mInnerRadius == 0.0f) {
            this.mInnerRadius = this.mHandleDrawable.getWidth() / 10.0f;
        }
    }

    private void computeInsets(int i, int i2) {
        int absoluteGravity = Gravity.getAbsoluteGravity(this.mGravity, getLayoutDirection());
        int i3 = absoluteGravity & 7;
        if (i3 == 3) {
            this.mHorizontalInset = 0;
        } else if (i3 == 5) {
            this.mHorizontalInset = i;
        } else {
            this.mHorizontalInset = i / 2;
        }
        int i4 = absoluteGravity & 112;
        if (i4 == 48) {
            this.mVerticalInset = 0;
        } else if (i4 == 80) {
            this.mVerticalInset = i2;
        } else {
            this.mVerticalInset = i2 / 2;
        }
    }

    private float computeScaleFactor(int i, int i2, int i3, int i4) {
        float f;
        float f2 = 1.0f;
        if (this.mAllowScaling) {
            int absoluteGravity = Gravity.getAbsoluteGravity(this.mGravity, getLayoutDirection());
            int i5 = absoluteGravity & 7;
            if (i5 != 3 && i5 != 5 && i > i3) {
                f = ((i3 * 1.0f) - this.mMaxTargetWidth) / (i - this.mMaxTargetWidth);
            } else {
                f = 1.0f;
            }
            int i6 = absoluteGravity & 112;
            if (i6 != 48 && i6 != 80 && i2 > i4) {
                f2 = ((1.0f * i4) - this.mMaxTargetHeight) / (i2 - this.mMaxTargetHeight);
            }
            return Math.min(f, f2);
        }
        return 1.0f;
    }

    private float getRingWidth() {
        return this.mRingScaleFactor * Math.max(this.mOuterRing.getWidth(), 2.0f * this.mOuterRadius);
    }

    private float getRingHeight() {
        return this.mRingScaleFactor * Math.max(this.mOuterRing.getHeight(), 2.0f * this.mOuterRadius);
    }

    @Override // android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        float ringWidth = getRingWidth();
        float ringHeight = getRingHeight();
        float f = this.mHorizontalInset + ((this.mMaxTargetWidth + ringWidth) / 2.0f);
        float f2 = this.mVerticalInset + ((this.mMaxTargetHeight + ringHeight) / 2.0f);
        if (this.mInitialLayout) {
            stopAndHideWaveAnimation();
            hideTargets(false, false);
            this.mInitialLayout = false;
        }
        this.mOuterRing.setPositionX(f);
        this.mOuterRing.setPositionY(f2);
        this.mPointCloud.setScale(this.mRingScaleFactor);
        this.mHandleDrawable.setPositionX(f);
        this.mHandleDrawable.setPositionY(f2);
        updateTargetPositions(f, f2);
        updatePointCloudPosition(f, f2);
        updateGlowPosition(f, f2);
        this.mWaveCenterX = f;
        this.mWaveCenterY = f2;
    }

    private void updateTargetPositions(float f, float f2) {
        ArrayList<TargetDrawable> arrayList = this.mTargetDrawables;
        int size = arrayList.size();
        float f3 = (float) ((-6.283185307179586d) / size);
        for (int i = 0; i < size; i++) {
            TargetDrawable targetDrawable = arrayList.get(i);
            targetDrawable.setPositionX(f);
            targetDrawable.setPositionY(f2);
            double d = i * f3;
            targetDrawable.setX((getRingWidth() / 2.0f) * ((float) Math.cos(d)));
            targetDrawable.setY((getRingHeight() / 2.0f) * ((float) Math.sin(d)));
        }
    }

    private void updatePointCloudPosition(float f, float f2) {
        this.mPointCloud.setCenter(f, f2);
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        this.mPointCloud.draw(canvas);
        this.mOuterRing.draw(canvas);
        int size = this.mTargetDrawables.size();
        for (int i = 0; i < size; i++) {
            TargetDrawable targetDrawable = this.mTargetDrawables.get(i);
            if (targetDrawable != null) {
                targetDrawable.draw(canvas);
            }
        }
        this.mHandleDrawable.draw(canvas);
    }

    public void setOnTriggerListener(OnTriggerListener onTriggerListener) {
        this.mOnTriggerListener = onTriggerListener;
    }

    private float square(float f) {
        return f * f;
    }

    private float dist2(float f, float f2) {
        return (f * f) + (f2 * f2);
    }

    private float getScaledGlowRadiusSquared() {
        float f;
        if (((AccessibilityManager) getContext().getSystemService("accessibility")).isEnabled()) {
            f = 1.3f * this.mGlowRadius;
        } else {
            f = this.mGlowRadius;
        }
        return square(f);
    }

    private void announceTargets() {
        StringBuilder sb = new StringBuilder();
        int size = this.mTargetDrawables.size();
        for (int i = 0; i < size; i++) {
            String targetDescription = getTargetDescription(i);
            String directionDescription = getDirectionDescription(i);
            if (!TextUtils.isEmpty(targetDescription) && !TextUtils.isEmpty(directionDescription)) {
                sb.append(String.format(directionDescription, targetDescription));
            }
        }
        if (sb.length() > 0) {
            announceForAccessibility(sb.toString());
        }
    }

    private String getTargetDescription(int i) {
        if (this.mTargetDescriptions == null || this.mTargetDescriptions.isEmpty()) {
            this.mTargetDescriptions = loadDescriptions(this.mTargetDescriptionsResourceId);
            if (this.mTargetDrawables.size() != this.mTargetDescriptions.size()) {
                Log.w("GlowPadView", "The number of target drawables must be equal to the number of target descriptions.");
                return null;
            }
        }
        return this.mTargetDescriptions.get(i);
    }

    private String getDirectionDescription(int i) {
        if (this.mDirectionDescriptions == null || this.mDirectionDescriptions.isEmpty()) {
            this.mDirectionDescriptions = loadDescriptions(this.mDirectionDescriptionsResourceId);
            if (this.mTargetDrawables.size() != this.mDirectionDescriptions.size()) {
                Log.w("GlowPadView", "The number of target drawables must be equal to the number of direction descriptions.");
                return null;
            }
        }
        return this.mDirectionDescriptions.get(i);
    }

    private ArrayList<String> loadDescriptions(int i) {
        TypedArray obtainTypedArray = getContext().getResources().obtainTypedArray(i);
        int length = obtainTypedArray.length();
        ArrayList<String> arrayList = new ArrayList<>(length);
        for (int i2 = 0; i2 < length; i2++) {
            arrayList.add(obtainTypedArray.getString(i2));
        }
        obtainTypedArray.recycle();
        return arrayList;
    }

    public int getResourceIdForTarget(int i) {
        TargetDrawable targetDrawable = this.mTargetDrawables.get(i);
        if (targetDrawable == null) {
            return 0;
        }
        return targetDrawable.getResourceId();
    }
}

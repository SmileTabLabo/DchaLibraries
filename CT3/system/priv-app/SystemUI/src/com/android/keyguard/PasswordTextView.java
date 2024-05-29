package com.android.keyguard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import java.util.ArrayList;
import java.util.Stack;
/* loaded from: a.zip:com/android/keyguard/PasswordTextView.class */
public class PasswordTextView extends View {
    private Interpolator mAppearInterpolator;
    private int mCharPadding;
    private Stack<CharState> mCharPool;
    private Interpolator mDisappearInterpolator;
    private int mDotSize;
    private final Paint mDrawPaint;
    private Interpolator mFastOutSlowInInterpolator;
    private final int mGravity;
    private PowerManager mPM;
    private boolean mShowPassword;
    private String mText;
    private ArrayList<CharState> mTextChars;
    private final int mTextHeightRaw;
    private UserActivityListener mUserActivityListener;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/keyguard/PasswordTextView$CharState.class */
    public class CharState {
        float currentDotSizeFactor;
        float currentTextSizeFactor;
        float currentTextTranslationY;
        float currentWidthFactor;
        boolean dotAnimationIsGrowing;
        Animator dotAnimator;
        Animator.AnimatorListener dotFinishListener;
        private ValueAnimator.AnimatorUpdateListener dotSizeUpdater;
        private Runnable dotSwapperRunnable;
        boolean isDotSwapPending;
        Animator.AnimatorListener removeEndListener;
        boolean textAnimationIsGrowing;
        ValueAnimator textAnimator;
        Animator.AnimatorListener textFinishListener;
        private ValueAnimator.AnimatorUpdateListener textSizeUpdater;
        ValueAnimator textTranslateAnimator;
        Animator.AnimatorListener textTranslateFinishListener;
        private ValueAnimator.AnimatorUpdateListener textTranslationUpdater;
        final PasswordTextView this$0;
        char whichChar;
        boolean widthAnimationIsGrowing;
        ValueAnimator widthAnimator;
        Animator.AnimatorListener widthFinishListener;
        private ValueAnimator.AnimatorUpdateListener widthUpdater;

        private CharState(PasswordTextView passwordTextView) {
            this.this$0 = passwordTextView;
            this.currentTextTranslationY = 1.0f;
            this.removeEndListener = new AnimatorListenerAdapter(this) { // from class: com.android.keyguard.PasswordTextView.CharState.1
                private boolean mCancelled;
                final CharState this$1;

                {
                    this.this$1 = this;
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationCancel(Animator animator) {
                    this.mCancelled = true;
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    if (this.mCancelled) {
                        return;
                    }
                    this.this$1.this$0.mTextChars.remove(this.this$1);
                    this.this$1.this$0.mCharPool.push(this.this$1);
                    this.this$1.reset();
                    this.this$1.cancelAnimator(this.this$1.textTranslateAnimator);
                    this.this$1.textTranslateAnimator = null;
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationStart(Animator animator) {
                    this.mCancelled = false;
                }
            };
            this.dotFinishListener = new AnimatorListenerAdapter(this) { // from class: com.android.keyguard.PasswordTextView.CharState.2
                final CharState this$1;

                {
                    this.this$1 = this;
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    this.this$1.dotAnimator = null;
                }
            };
            this.textFinishListener = new AnimatorListenerAdapter(this) { // from class: com.android.keyguard.PasswordTextView.CharState.3
                final CharState this$1;

                {
                    this.this$1 = this;
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    this.this$1.textAnimator = null;
                }
            };
            this.textTranslateFinishListener = new AnimatorListenerAdapter(this) { // from class: com.android.keyguard.PasswordTextView.CharState.4
                final CharState this$1;

                {
                    this.this$1 = this;
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    this.this$1.textTranslateAnimator = null;
                }
            };
            this.widthFinishListener = new AnimatorListenerAdapter(this) { // from class: com.android.keyguard.PasswordTextView.CharState.5
                final CharState this$1;

                {
                    this.this$1 = this;
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    this.this$1.widthAnimator = null;
                }
            };
            this.dotSizeUpdater = new ValueAnimator.AnimatorUpdateListener(this) { // from class: com.android.keyguard.PasswordTextView.CharState.6
                final CharState this$1;

                {
                    this.this$1 = this;
                }

                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    this.this$1.currentDotSizeFactor = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                    this.this$1.this$0.invalidate();
                }
            };
            this.textSizeUpdater = new ValueAnimator.AnimatorUpdateListener(this) { // from class: com.android.keyguard.PasswordTextView.CharState.7
                final CharState this$1;

                {
                    this.this$1 = this;
                }

                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    this.this$1.currentTextSizeFactor = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                    this.this$1.this$0.invalidate();
                }
            };
            this.textTranslationUpdater = new ValueAnimator.AnimatorUpdateListener(this) { // from class: com.android.keyguard.PasswordTextView.CharState.8
                final CharState this$1;

                {
                    this.this$1 = this;
                }

                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    this.this$1.currentTextTranslationY = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                    this.this$1.this$0.invalidate();
                }
            };
            this.widthUpdater = new ValueAnimator.AnimatorUpdateListener(this) { // from class: com.android.keyguard.PasswordTextView.CharState.9
                final CharState this$1;

                {
                    this.this$1 = this;
                }

                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    this.this$1.currentWidthFactor = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                    this.this$1.this$0.invalidate();
                }
            };
            this.dotSwapperRunnable = new Runnable(this) { // from class: com.android.keyguard.PasswordTextView.CharState.10
                final CharState this$1;

                {
                    this.this$1 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$1.performSwap();
                    this.this$1.isDotSwapPending = false;
                }
            };
        }

        /* synthetic */ CharState(PasswordTextView passwordTextView, CharState charState) {
            this(passwordTextView);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void cancelAnimator(Animator animator) {
            if (animator != null) {
                animator.cancel();
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void performSwap() {
            startTextDisappearAnimation(0L);
            startDotAppearAnimation(30L);
        }

        private void postDotSwap(long j) {
            removeDotSwapCallbacks();
            this.this$0.postDelayed(this.dotSwapperRunnable, j);
            this.isDotSwapPending = true;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void removeDotSwapCallbacks() {
            this.this$0.removeCallbacks(this.dotSwapperRunnable);
            this.isDotSwapPending = false;
        }

        private void startDotAppearAnimation(long j) {
            cancelAnimator(this.dotAnimator);
            if (this.this$0.mShowPassword) {
                ValueAnimator ofFloat = ValueAnimator.ofFloat(this.currentDotSizeFactor, 1.0f);
                ofFloat.addUpdateListener(this.dotSizeUpdater);
                ofFloat.setDuration((1.0f - this.currentDotSizeFactor) * 160.0f);
                ofFloat.addListener(this.dotFinishListener);
                ofFloat.setStartDelay(j);
                ofFloat.start();
                this.dotAnimator = ofFloat;
            } else {
                ValueAnimator ofFloat2 = ValueAnimator.ofFloat(this.currentDotSizeFactor, 1.5f);
                ofFloat2.addUpdateListener(this.dotSizeUpdater);
                ofFloat2.setInterpolator(this.this$0.mAppearInterpolator);
                ofFloat2.setDuration(160L);
                ValueAnimator ofFloat3 = ValueAnimator.ofFloat(1.5f, 1.0f);
                ofFloat3.addUpdateListener(this.dotSizeUpdater);
                ofFloat3.setDuration(160L);
                ofFloat3.addListener(this.dotFinishListener);
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playSequentially(ofFloat2, ofFloat3);
                animatorSet.setStartDelay(j);
                animatorSet.start();
                this.dotAnimator = animatorSet;
            }
            this.dotAnimationIsGrowing = true;
        }

        private void startDotDisappearAnimation(long j) {
            cancelAnimator(this.dotAnimator);
            ValueAnimator ofFloat = ValueAnimator.ofFloat(this.currentDotSizeFactor, 0.0f);
            ofFloat.addUpdateListener(this.dotSizeUpdater);
            ofFloat.addListener(this.dotFinishListener);
            ofFloat.setInterpolator(this.this$0.mDisappearInterpolator);
            ofFloat.setDuration(Math.min(this.currentDotSizeFactor, 1.0f) * 160.0f);
            ofFloat.setStartDelay(j);
            ofFloat.start();
            this.dotAnimator = ofFloat;
            this.dotAnimationIsGrowing = false;
        }

        private void startTextAppearAnimation() {
            cancelAnimator(this.textAnimator);
            this.textAnimator = ValueAnimator.ofFloat(this.currentTextSizeFactor, 1.0f);
            this.textAnimator.addUpdateListener(this.textSizeUpdater);
            this.textAnimator.addListener(this.textFinishListener);
            this.textAnimator.setInterpolator(this.this$0.mAppearInterpolator);
            this.textAnimator.setDuration((1.0f - this.currentTextSizeFactor) * 160.0f);
            this.textAnimator.start();
            this.textAnimationIsGrowing = true;
            if (this.textTranslateAnimator == null) {
                this.textTranslateAnimator = ValueAnimator.ofFloat(1.0f, 0.0f);
                this.textTranslateAnimator.addUpdateListener(this.textTranslationUpdater);
                this.textTranslateAnimator.addListener(this.textTranslateFinishListener);
                this.textTranslateAnimator.setInterpolator(this.this$0.mAppearInterpolator);
                this.textTranslateAnimator.setDuration(160L);
                this.textTranslateAnimator.start();
            }
        }

        private void startTextDisappearAnimation(long j) {
            cancelAnimator(this.textAnimator);
            this.textAnimator = ValueAnimator.ofFloat(this.currentTextSizeFactor, 0.0f);
            this.textAnimator.addUpdateListener(this.textSizeUpdater);
            this.textAnimator.addListener(this.textFinishListener);
            this.textAnimator.setInterpolator(this.this$0.mDisappearInterpolator);
            this.textAnimator.setDuration(this.currentTextSizeFactor * 160.0f);
            this.textAnimator.setStartDelay(j);
            this.textAnimator.start();
            this.textAnimationIsGrowing = false;
        }

        private void startWidthAppearAnimation() {
            cancelAnimator(this.widthAnimator);
            this.widthAnimator = ValueAnimator.ofFloat(this.currentWidthFactor, 1.0f);
            this.widthAnimator.addUpdateListener(this.widthUpdater);
            this.widthAnimator.addListener(this.widthFinishListener);
            this.widthAnimator.setDuration((1.0f - this.currentWidthFactor) * 160.0f);
            this.widthAnimator.start();
            this.widthAnimationIsGrowing = true;
        }

        private void startWidthDisappearAnimation(long j) {
            cancelAnimator(this.widthAnimator);
            this.widthAnimator = ValueAnimator.ofFloat(this.currentWidthFactor, 0.0f);
            this.widthAnimator.addUpdateListener(this.widthUpdater);
            this.widthAnimator.addListener(this.widthFinishListener);
            this.widthAnimator.addListener(this.removeEndListener);
            this.widthAnimator.setDuration(this.currentWidthFactor * 160.0f);
            this.widthAnimator.setStartDelay(j);
            this.widthAnimator.start();
            this.widthAnimationIsGrowing = false;
        }

        public float draw(Canvas canvas, float f, int i, float f2, float f3) {
            boolean z = this.currentTextSizeFactor > 0.0f;
            boolean z2 = this.currentDotSizeFactor > 0.0f;
            float f4 = f3 * this.currentWidthFactor;
            if (z) {
                float f5 = this.currentTextSizeFactor;
                float f6 = this.currentTextTranslationY;
                canvas.save();
                canvas.translate(f + (f4 / 2.0f), ((i / 2.0f) * f5) + f2 + (i * f6 * 0.8f));
                canvas.scale(this.currentTextSizeFactor, this.currentTextSizeFactor);
                canvas.drawText(Character.toString(this.whichChar), 0.0f, 0.0f, this.this$0.mDrawPaint);
                canvas.restore();
            }
            if (z2) {
                canvas.save();
                canvas.translate(f + (f4 / 2.0f), f2);
                canvas.drawCircle(0.0f, 0.0f, (this.this$0.mDotSize / 2) * this.currentDotSizeFactor, this.this$0.mDrawPaint);
                canvas.restore();
            }
            return (this.this$0.mCharPadding * this.currentWidthFactor) + f4;
        }

        void reset() {
            this.whichChar = (char) 0;
            this.currentTextSizeFactor = 0.0f;
            this.currentDotSizeFactor = 0.0f;
            this.currentWidthFactor = 0.0f;
            cancelAnimator(this.textAnimator);
            this.textAnimator = null;
            cancelAnimator(this.dotAnimator);
            this.dotAnimator = null;
            cancelAnimator(this.widthAnimator);
            this.widthAnimator = null;
            this.currentTextTranslationY = 1.0f;
            removeDotSwapCallbacks();
        }

        void startAppearAnimation() {
            boolean z = !this.this$0.mShowPassword ? this.dotAnimator == null || !this.dotAnimationIsGrowing : false;
            boolean z2 = this.this$0.mShowPassword ? this.textAnimator == null || !this.textAnimationIsGrowing : false;
            boolean z3 = this.widthAnimator == null || !this.widthAnimationIsGrowing;
            if (z) {
                startDotAppearAnimation(0L);
            }
            if (z2) {
                startTextAppearAnimation();
            }
            if (z3) {
                startWidthAppearAnimation();
            }
            if (this.this$0.mShowPassword) {
                postDotSwap(1300L);
            }
        }

        void startRemoveAnimation(long j, long j2) {
            boolean z = (this.currentDotSizeFactor <= 0.0f || this.dotAnimator != null) ? this.dotAnimator != null ? this.dotAnimationIsGrowing : false : true;
            boolean z2 = (this.currentTextSizeFactor <= 0.0f || this.textAnimator != null) ? this.textAnimator != null ? this.textAnimationIsGrowing : false : true;
            boolean z3 = (this.currentWidthFactor <= 0.0f || this.widthAnimator != null) ? this.widthAnimator != null ? this.widthAnimationIsGrowing : false : true;
            if (z) {
                startDotDisappearAnimation(j);
            }
            if (z2) {
                startTextDisappearAnimation(j);
            }
            if (z3) {
                startWidthDisappearAnimation(j2);
            }
        }

        void swapToDotWhenAppearFinished() {
            removeDotSwapCallbacks();
            if (this.textAnimator != null) {
                postDotSwap(100 + (this.textAnimator.getDuration() - this.textAnimator.getCurrentPlayTime()));
            } else {
                performSwap();
            }
        }
    }

    /* loaded from: a.zip:com/android/keyguard/PasswordTextView$UserActivityListener.class */
    public interface UserActivityListener {
        void onUserActivity();
    }

    public PasswordTextView(Context context) {
        this(context, null);
    }

    public PasswordTextView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public PasswordTextView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public PasswordTextView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        boolean z = true;
        this.mTextChars = new ArrayList<>();
        this.mText = "";
        this.mCharPool = new Stack<>();
        this.mDrawPaint = new Paint();
        setFocusableInTouchMode(true);
        setFocusable(true);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.PasswordTextView);
        try {
            this.mTextHeightRaw = obtainStyledAttributes.getInt(R$styleable.PasswordTextView_scaledTextSize, 0);
            this.mGravity = obtainStyledAttributes.getInt(R$styleable.PasswordTextView_android_gravity, 17);
            this.mDotSize = obtainStyledAttributes.getDimensionPixelSize(R$styleable.PasswordTextView_dotSize, getContext().getResources().getDimensionPixelSize(R$dimen.password_dot_size));
            this.mCharPadding = obtainStyledAttributes.getDimensionPixelSize(R$styleable.PasswordTextView_charPadding, getContext().getResources().getDimensionPixelSize(R$dimen.password_char_padding));
            obtainStyledAttributes.recycle();
            this.mDrawPaint.setFlags(129);
            this.mDrawPaint.setTextAlign(Paint.Align.CENTER);
            this.mDrawPaint.setColor(-1);
            this.mDrawPaint.setTypeface(Typeface.create("sans-serif-light", 0));
            this.mShowPassword = Settings.System.getInt(this.mContext.getContentResolver(), "show_password", 1) != 1 ? false : z;
            this.mAppearInterpolator = AnimationUtils.loadInterpolator(this.mContext, 17563662);
            this.mDisappearInterpolator = AnimationUtils.loadInterpolator(this.mContext, 17563663);
            this.mFastOutSlowInInterpolator = AnimationUtils.loadInterpolator(this.mContext, 17563661);
            this.mPM = (PowerManager) this.mContext.getSystemService("power");
        } catch (Throwable th) {
            obtainStyledAttributes.recycle();
            throw th;
        }
    }

    private Rect getCharBounds() {
        this.mDrawPaint.setTextSize(this.mTextHeightRaw * getResources().getDisplayMetrics().scaledDensity);
        Rect rect = new Rect();
        this.mDrawPaint.getTextBounds("0", 0, 1, rect);
        return rect;
    }

    private float getDrawingWidth() {
        int i = 0;
        int size = this.mTextChars.size();
        Rect charBounds = getCharBounds();
        int i2 = charBounds.right;
        int i3 = charBounds.left;
        for (int i4 = 0; i4 < size; i4++) {
            CharState charState = this.mTextChars.get(i4);
            int i5 = i;
            if (i4 != 0) {
                i5 = (int) (i + (this.mCharPadding * charState.currentWidthFactor));
            }
            i = (int) (i5 + ((i2 - i3) * charState.currentWidthFactor));
        }
        return i;
    }

    private CharState obtainCharState(char c) {
        CharState pop;
        if (this.mCharPool.isEmpty()) {
            pop = new CharState(this, null);
        } else {
            pop = this.mCharPool.pop();
            pop.reset();
        }
        pop.whichChar = c;
        return pop;
    }

    private boolean shouldSpeakPasswordsForAccessibility() {
        boolean z = true;
        if (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "speak_password", 0, -3) != 1) {
            z = false;
        }
        return z;
    }

    private void userActivity() {
        this.mPM.userActivity(SystemClock.uptimeMillis(), false);
        if (this.mUserActivityListener != null) {
            this.mUserActivityListener.onUserActivity();
        }
    }

    public void append(char c) {
        CharState charState;
        int size = this.mTextChars.size();
        String str = this.mText;
        this.mText += c;
        int length = this.mText.length();
        if (length > size) {
            charState = obtainCharState(c);
            this.mTextChars.add(charState);
        } else {
            charState = this.mTextChars.get(length - 1);
            charState.whichChar = c;
        }
        charState.startAppearAnimation();
        if (length > 1) {
            CharState charState2 = this.mTextChars.get(length - 2);
            if (charState2.isDotSwapPending) {
                charState2.swapToDotWhenAppearFinished();
            }
        }
        userActivity();
        sendAccessibilityEventTypeViewTextChanged(str, str.length(), 0, 1);
    }

    public void deleteLastChar() {
        int length = this.mText.length();
        String str = this.mText;
        if (length > 0) {
            this.mText = this.mText.substring(0, length - 1);
            this.mTextChars.get(length - 1).startRemoveAnimation(0L, 0L);
        }
        userActivity();
        sendAccessibilityEventTypeViewTextChanged(str, str.length() - 1, 1, 0);
    }

    public String getText() {
        return this.mText;
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        float drawingWidth = getDrawingWidth();
        float paddingLeft = (this.mGravity & 7) == 3 ? ((this.mGravity & 8388608) == 0 || getLayoutDirection() != 1) ? getPaddingLeft() : (getWidth() - getPaddingRight()) - drawingWidth : (getWidth() / 2) - (drawingWidth / 2.0f);
        int size = this.mTextChars.size();
        Rect charBounds = getCharBounds();
        int i = charBounds.bottom;
        int i2 = charBounds.top;
        float height = (((getHeight() - getPaddingBottom()) - getPaddingTop()) / 2) + getPaddingTop();
        canvas.clipRect(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(), getHeight() - getPaddingBottom());
        float f = charBounds.right - charBounds.left;
        for (int i3 = 0; i3 < size; i3++) {
            paddingLeft += this.mTextChars.get(i3).draw(canvas, paddingLeft, i - i2, height, f);
        }
    }

    @Override // android.view.View
    public void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        super.onInitializeAccessibilityEvent(accessibilityEvent);
        accessibilityEvent.setClassName(PasswordTextView.class.getName());
        accessibilityEvent.setPassword(true);
    }

    @Override // android.view.View
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        accessibilityNodeInfo.setClassName(PasswordTextView.class.getName());
        accessibilityNodeInfo.setPassword(true);
        if (shouldSpeakPasswordsForAccessibility()) {
            accessibilityNodeInfo.setText(this.mText);
        }
        accessibilityNodeInfo.setEditable(true);
        accessibilityNodeInfo.setInputType(16);
    }

    @Override // android.view.View
    public void onPopulateAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        super.onPopulateAccessibilityEvent(accessibilityEvent);
        if (shouldSpeakPasswordsForAccessibility()) {
            String str = this.mText;
            if (TextUtils.isEmpty(str)) {
                return;
            }
            accessibilityEvent.getText().add(str);
        }
    }

    public void reset(boolean z, boolean z2) {
        Log.d("PasswordTextView", "reset() is called, set PwEntry true.");
        String str = this.mText;
        this.mText = "";
        int size = this.mTextChars.size();
        int i = (size - 1) / 2;
        int i2 = 0;
        while (i2 < size) {
            CharState charState = this.mTextChars.get(i2);
            if (z) {
                charState.startRemoveAnimation(Math.min((i2 <= i ? i2 * 2 : (size - 1) - (((i2 - i) - 1) * 2)) * 40, 200L), Math.min(40 * (size - 1), 200L) + 160);
                charState.removeDotSwapCallbacks();
            } else {
                this.mCharPool.push(charState);
            }
            i2++;
        }
        if (!z) {
            this.mTextChars.clear();
        }
        if (z2) {
            sendAccessibilityEventTypeViewTextChanged(str, 0, str.length(), 0);
        }
    }

    void sendAccessibilityEventTypeViewTextChanged(String str, int i, int i2, int i3) {
        if (AccessibilityManager.getInstance(this.mContext).isEnabled()) {
            if (isFocused() || (isSelected() && isShown())) {
                if (!shouldSpeakPasswordsForAccessibility()) {
                    str = null;
                }
                AccessibilityEvent obtain = AccessibilityEvent.obtain(16);
                obtain.setFromIndex(i);
                obtain.setRemovedCount(i2);
                obtain.setAddedCount(i3);
                obtain.setBeforeText(str);
                obtain.setPassword(true);
                sendAccessibilityEventUnchecked(obtain);
            }
        }
    }

    public void setUserActivityListener(UserActivityListener userActivityListener) {
        this.mUserActivityListener = userActivityListener;
    }
}

package android.support.v7.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.appcompat.R$attr;
import android.support.v7.appcompat.R$styleable;
import android.support.v7.text.AllCapsTransformationMethod;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.TransformationMethod;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.CompoundButton;
/* loaded from: a.zip:android/support/v7/widget/SwitchCompat.class */
public class SwitchCompat extends CompoundButton {
    private static final int[] CHECKED_STATE_SET = {16842912};
    private final AppCompatDrawableManager mDrawableManager;
    private boolean mHasThumbTint;
    private boolean mHasThumbTintMode;
    private boolean mHasTrackTint;
    private boolean mHasTrackTintMode;
    private int mMinFlingVelocity;
    private Layout mOffLayout;
    private Layout mOnLayout;
    private ThumbAnimation mPositionAnimator;
    private boolean mShowText;
    private boolean mSplitTrack;
    private int mSwitchBottom;
    private int mSwitchHeight;
    private int mSwitchLeft;
    private int mSwitchMinWidth;
    private int mSwitchPadding;
    private int mSwitchRight;
    private int mSwitchTop;
    private TransformationMethod mSwitchTransformationMethod;
    private int mSwitchWidth;
    private final Rect mTempRect;
    private ColorStateList mTextColors;
    private CharSequence mTextOff;
    private CharSequence mTextOn;
    private TextPaint mTextPaint;
    private Drawable mThumbDrawable;
    private float mThumbPosition;
    private int mThumbTextPadding;
    private ColorStateList mThumbTintList;
    private PorterDuff.Mode mThumbTintMode;
    private int mThumbWidth;
    private int mTouchMode;
    private int mTouchSlop;
    private float mTouchX;
    private float mTouchY;
    private Drawable mTrackDrawable;
    private ColorStateList mTrackTintList;
    private PorterDuff.Mode mTrackTintMode;
    private VelocityTracker mVelocityTracker;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:android/support/v7/widget/SwitchCompat$ThumbAnimation.class */
    public class ThumbAnimation extends Animation {
        final float mDiff;
        final float mEndPosition;
        final float mStartPosition;
        final SwitchCompat this$0;

        private ThumbAnimation(SwitchCompat switchCompat, float f, float f2) {
            this.this$0 = switchCompat;
            this.mStartPosition = f;
            this.mEndPosition = f2;
            this.mDiff = f2 - f;
        }

        /* synthetic */ ThumbAnimation(SwitchCompat switchCompat, float f, float f2, ThumbAnimation thumbAnimation) {
            this(switchCompat, f, f2);
        }

        @Override // android.view.animation.Animation
        protected void applyTransformation(float f, Transformation transformation) {
            this.this$0.setThumbPosition(this.mStartPosition + (this.mDiff * f));
        }
    }

    public SwitchCompat(Context context) {
        this(context, null);
    }

    public SwitchCompat(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, R$attr.switchStyle);
    }

    public SwitchCompat(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mThumbTintList = null;
        this.mThumbTintMode = null;
        this.mHasThumbTint = false;
        this.mHasThumbTintMode = false;
        this.mTrackTintList = null;
        this.mTrackTintMode = null;
        this.mHasTrackTint = false;
        this.mHasTrackTintMode = false;
        this.mVelocityTracker = VelocityTracker.obtain();
        this.mTempRect = new Rect();
        this.mTextPaint = new TextPaint(1);
        Resources resources = getResources();
        this.mTextPaint.density = resources.getDisplayMetrics().density;
        TintTypedArray obtainStyledAttributes = TintTypedArray.obtainStyledAttributes(context, attributeSet, R$styleable.SwitchCompat, i, 0);
        this.mThumbDrawable = obtainStyledAttributes.getDrawable(R$styleable.SwitchCompat_android_thumb);
        if (this.mThumbDrawable != null) {
            this.mThumbDrawable.setCallback(this);
        }
        this.mTrackDrawable = obtainStyledAttributes.getDrawable(R$styleable.SwitchCompat_track);
        if (this.mTrackDrawable != null) {
            this.mTrackDrawable.setCallback(this);
        }
        this.mTextOn = obtainStyledAttributes.getText(R$styleable.SwitchCompat_android_textOn);
        this.mTextOff = obtainStyledAttributes.getText(R$styleable.SwitchCompat_android_textOff);
        this.mShowText = obtainStyledAttributes.getBoolean(R$styleable.SwitchCompat_showText, true);
        this.mThumbTextPadding = obtainStyledAttributes.getDimensionPixelSize(R$styleable.SwitchCompat_thumbTextPadding, 0);
        this.mSwitchMinWidth = obtainStyledAttributes.getDimensionPixelSize(R$styleable.SwitchCompat_switchMinWidth, 0);
        this.mSwitchPadding = obtainStyledAttributes.getDimensionPixelSize(R$styleable.SwitchCompat_switchPadding, 0);
        this.mSplitTrack = obtainStyledAttributes.getBoolean(R$styleable.SwitchCompat_splitTrack, false);
        ColorStateList colorStateList = obtainStyledAttributes.getColorStateList(R$styleable.SwitchCompat_thumbTint);
        if (colorStateList != null) {
            this.mThumbTintList = colorStateList;
            this.mHasThumbTint = true;
        }
        PorterDuff.Mode parseTintMode = DrawableUtils.parseTintMode(obtainStyledAttributes.getInt(R$styleable.SwitchCompat_thumbTintMode, -1), null);
        if (this.mThumbTintMode != parseTintMode) {
            this.mThumbTintMode = parseTintMode;
            this.mHasThumbTintMode = true;
        }
        if (this.mHasThumbTint || this.mHasThumbTintMode) {
            applyThumbTint();
        }
        ColorStateList colorStateList2 = obtainStyledAttributes.getColorStateList(R$styleable.SwitchCompat_trackTint);
        if (colorStateList2 != null) {
            this.mTrackTintList = colorStateList2;
            this.mHasTrackTint = true;
        }
        PorterDuff.Mode parseTintMode2 = DrawableUtils.parseTintMode(obtainStyledAttributes.getInt(R$styleable.SwitchCompat_trackTintMode, -1), null);
        if (this.mTrackTintMode != parseTintMode2) {
            this.mTrackTintMode = parseTintMode2;
            this.mHasTrackTintMode = true;
        }
        if (this.mHasTrackTint || this.mHasTrackTintMode) {
            applyTrackTint();
        }
        int resourceId = obtainStyledAttributes.getResourceId(R$styleable.SwitchCompat_switchTextAppearance, 0);
        if (resourceId != 0) {
            setSwitchTextAppearance(context, resourceId);
        }
        this.mDrawableManager = AppCompatDrawableManager.get();
        obtainStyledAttributes.recycle();
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        this.mTouchSlop = viewConfiguration.getScaledTouchSlop();
        this.mMinFlingVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
        refreshDrawableState();
        setChecked(isChecked());
    }

    private void animateThumbToCheckedState(boolean z) {
        if (this.mPositionAnimator != null) {
            cancelPositionAnimator();
        }
        this.mPositionAnimator = new ThumbAnimation(this, this.mThumbPosition, z ? 1.0f : 0.0f, null);
        this.mPositionAnimator.setDuration(250L);
        this.mPositionAnimator.setAnimationListener(new Animation.AnimationListener(this, z) { // from class: android.support.v7.widget.SwitchCompat.1
            final SwitchCompat this$0;
            final boolean val$newCheckedState;

            {
                this.this$0 = this;
                this.val$newCheckedState = z;
            }

            @Override // android.view.animation.Animation.AnimationListener
            public void onAnimationEnd(Animation animation) {
                if (this.this$0.mPositionAnimator == animation) {
                    this.this$0.setThumbPosition(this.val$newCheckedState ? 1.0f : 0.0f);
                    this.this$0.mPositionAnimator = null;
                }
            }

            @Override // android.view.animation.Animation.AnimationListener
            public void onAnimationRepeat(Animation animation) {
            }

            @Override // android.view.animation.Animation.AnimationListener
            public void onAnimationStart(Animation animation) {
            }
        });
        startAnimation(this.mPositionAnimator);
    }

    private void applyThumbTint() {
        if (this.mThumbDrawable != null) {
            if (this.mHasThumbTint || this.mHasThumbTintMode) {
                this.mThumbDrawable = this.mThumbDrawable.mutate();
                if (this.mHasThumbTint) {
                    DrawableCompat.setTintList(this.mThumbDrawable, this.mThumbTintList);
                }
                if (this.mHasThumbTintMode) {
                    DrawableCompat.setTintMode(this.mThumbDrawable, this.mThumbTintMode);
                }
                if (this.mThumbDrawable.isStateful()) {
                    this.mThumbDrawable.setState(getDrawableState());
                }
            }
        }
    }

    private void applyTrackTint() {
        if (this.mTrackDrawable != null) {
            if (this.mHasTrackTint || this.mHasTrackTintMode) {
                this.mTrackDrawable = this.mTrackDrawable.mutate();
                if (this.mHasTrackTint) {
                    DrawableCompat.setTintList(this.mTrackDrawable, this.mTrackTintList);
                }
                if (this.mHasTrackTintMode) {
                    DrawableCompat.setTintMode(this.mTrackDrawable, this.mTrackTintMode);
                }
                if (this.mTrackDrawable.isStateful()) {
                    this.mTrackDrawable.setState(getDrawableState());
                }
            }
        }
    }

    private void cancelPositionAnimator() {
        if (this.mPositionAnimator != null) {
            clearAnimation();
            this.mPositionAnimator = null;
        }
    }

    private void cancelSuperTouch(MotionEvent motionEvent) {
        MotionEvent obtain = MotionEvent.obtain(motionEvent);
        obtain.setAction(3);
        super.onTouchEvent(obtain);
        obtain.recycle();
    }

    private static float constrain(float f, float f2, float f3) {
        if (f >= f2) {
            f2 = f > f3 ? f3 : f;
        }
        return f2;
    }

    private boolean getTargetCheckedState() {
        return this.mThumbPosition > 0.5f;
    }

    private int getThumbOffset() {
        return (int) ((getThumbScrollRange() * (ViewUtils.isLayoutRtl(this) ? 1.0f - this.mThumbPosition : this.mThumbPosition)) + 0.5f);
    }

    private int getThumbScrollRange() {
        if (this.mTrackDrawable != null) {
            Rect rect = this.mTempRect;
            this.mTrackDrawable.getPadding(rect);
            Rect opticalBounds = this.mThumbDrawable != null ? DrawableUtils.getOpticalBounds(this.mThumbDrawable) : DrawableUtils.INSETS_NONE;
            return ((((this.mSwitchWidth - this.mThumbWidth) - rect.left) - rect.right) - opticalBounds.left) - opticalBounds.right;
        }
        return 0;
    }

    private boolean hitThumb(float f, float f2) {
        if (this.mThumbDrawable == null) {
            return false;
        }
        int thumbOffset = getThumbOffset();
        this.mThumbDrawable.getPadding(this.mTempRect);
        int i = this.mSwitchTop;
        int i2 = this.mTouchSlop;
        int i3 = (this.mSwitchLeft + thumbOffset) - this.mTouchSlop;
        int i4 = this.mThumbWidth;
        int i5 = this.mTempRect.left;
        int i6 = this.mTempRect.right;
        int i7 = this.mTouchSlop;
        int i8 = this.mSwitchBottom;
        int i9 = this.mTouchSlop;
        boolean z = false;
        if (f > i3) {
            z = false;
            if (f < i4 + i3 + i5 + i6 + i7) {
                z = false;
                if (f2 > i - i2) {
                    z = false;
                    if (f2 < i8 + i9) {
                        z = true;
                    }
                }
            }
        }
        return z;
    }

    private Layout makeLayout(CharSequence charSequence) {
        if (this.mSwitchTransformationMethod != null) {
            charSequence = this.mSwitchTransformationMethod.getTransformation(charSequence, this);
        }
        return new StaticLayout(charSequence, this.mTextPaint, charSequence != null ? (int) Math.ceil(Layout.getDesiredWidth(charSequence, this.mTextPaint)) : 0, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true);
    }

    private void setSwitchTypefaceByIndex(int i, int i2) {
        Typeface typeface = null;
        switch (i) {
            case 1:
                typeface = Typeface.SANS_SERIF;
                break;
            case 2:
                typeface = Typeface.SERIF;
                break;
            case 3:
                typeface = Typeface.MONOSPACE;
                break;
        }
        setSwitchTypeface(typeface, i2);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setThumbPosition(float f) {
        this.mThumbPosition = f;
        invalidate();
    }

    private void stopDrag(MotionEvent motionEvent) {
        boolean z;
        this.mTouchMode = 0;
        boolean isEnabled = motionEvent.getAction() == 1 ? isEnabled() : false;
        boolean isChecked = isChecked();
        if (isEnabled) {
            this.mVelocityTracker.computeCurrentVelocity(1000);
            float xVelocity = this.mVelocityTracker.getXVelocity();
            z = Math.abs(xVelocity) > ((float) this.mMinFlingVelocity) ? !ViewUtils.isLayoutRtl(this) ? xVelocity <= 0.0f : xVelocity >= 0.0f : getTargetCheckedState();
        } else {
            z = isChecked;
        }
        if (z != isChecked) {
            playSoundEffect(0);
        }
        setChecked(z);
        cancelSuperTouch(motionEvent);
    }

    @Override // android.view.View
    public void draw(Canvas canvas) {
        Rect rect = this.mTempRect;
        int i = this.mSwitchLeft;
        int i2 = this.mSwitchTop;
        int i3 = this.mSwitchRight;
        int i4 = this.mSwitchBottom;
        int thumbOffset = i + getThumbOffset();
        Rect opticalBounds = this.mThumbDrawable != null ? DrawableUtils.getOpticalBounds(this.mThumbDrawable) : DrawableUtils.INSETS_NONE;
        int i5 = thumbOffset;
        if (this.mTrackDrawable != null) {
            this.mTrackDrawable.getPadding(rect);
            int i6 = thumbOffset + rect.left;
            int i7 = i;
            int i8 = i2;
            int i9 = i3;
            int i10 = i4;
            int i11 = i7;
            int i12 = i9;
            int i13 = i8;
            if (opticalBounds != null) {
                if (opticalBounds.left > rect.left) {
                    i7 = i + (opticalBounds.left - rect.left);
                }
                if (opticalBounds.top > rect.top) {
                    i8 = i2 + (opticalBounds.top - rect.top);
                }
                if (opticalBounds.right > rect.right) {
                    i9 = i3 - (opticalBounds.right - rect.right);
                }
                i10 = i4;
                i11 = i7;
                i12 = i9;
                i13 = i8;
                if (opticalBounds.bottom > rect.bottom) {
                    i10 = i4 - (opticalBounds.bottom - rect.bottom);
                    i13 = i8;
                    i12 = i9;
                    i11 = i7;
                }
            }
            this.mTrackDrawable.setBounds(i11, i13, i12, i10);
            i5 = i6;
        }
        if (this.mThumbDrawable != null) {
            this.mThumbDrawable.getPadding(rect);
            int i14 = i5 - rect.left;
            int i15 = this.mThumbWidth + i5 + rect.right;
            this.mThumbDrawable.setBounds(i14, i2, i15, i4);
            Drawable background = getBackground();
            if (background != null) {
                DrawableCompat.setHotspotBounds(background, i14, i2, i15, i4);
            }
        }
        super.draw(canvas);
    }

    @Override // android.widget.CompoundButton, android.widget.TextView, android.view.View
    public void drawableHotspotChanged(float f, float f2) {
        if (Build.VERSION.SDK_INT >= 21) {
            super.drawableHotspotChanged(f, f2);
        }
        if (this.mThumbDrawable != null) {
            DrawableCompat.setHotspot(this.mThumbDrawable, f, f2);
        }
        if (this.mTrackDrawable != null) {
            DrawableCompat.setHotspot(this.mTrackDrawable, f, f2);
        }
    }

    @Override // android.widget.CompoundButton, android.widget.TextView, android.view.View
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        int[] drawableState = getDrawableState();
        Drawable drawable = this.mThumbDrawable;
        boolean z = false;
        if (drawable != null) {
            z = false;
            if (drawable.isStateful()) {
                z = drawable.setState(drawableState);
            }
        }
        Drawable drawable2 = this.mTrackDrawable;
        boolean z2 = z;
        if (drawable2 != null) {
            z2 = z;
            if (drawable2.isStateful()) {
                z2 = z | drawable2.setState(drawableState);
            }
        }
        if (z2) {
            invalidate();
        }
    }

    @Override // android.widget.CompoundButton, android.widget.TextView
    public int getCompoundPaddingLeft() {
        if (ViewUtils.isLayoutRtl(this)) {
            int compoundPaddingLeft = super.getCompoundPaddingLeft() + this.mSwitchWidth;
            int i = compoundPaddingLeft;
            if (!TextUtils.isEmpty(getText())) {
                i = compoundPaddingLeft + this.mSwitchPadding;
            }
            return i;
        }
        return super.getCompoundPaddingLeft();
    }

    @Override // android.widget.CompoundButton, android.widget.TextView
    public int getCompoundPaddingRight() {
        if (ViewUtils.isLayoutRtl(this)) {
            return super.getCompoundPaddingRight();
        }
        int compoundPaddingRight = super.getCompoundPaddingRight() + this.mSwitchWidth;
        int i = compoundPaddingRight;
        if (!TextUtils.isEmpty(getText())) {
            i = compoundPaddingRight + this.mSwitchPadding;
        }
        return i;
    }

    @Override // android.widget.CompoundButton, android.widget.TextView, android.view.View
    public void jumpDrawablesToCurrentState() {
        if (Build.VERSION.SDK_INT >= 11) {
            super.jumpDrawablesToCurrentState();
            if (this.mThumbDrawable != null) {
                this.mThumbDrawable.jumpToCurrentState();
            }
            if (this.mTrackDrawable != null) {
                this.mTrackDrawable.jumpToCurrentState();
            }
            cancelPositionAnimator();
            setThumbPosition(isChecked() ? 1 : 0);
        }
    }

    @Override // android.widget.CompoundButton, android.widget.TextView, android.view.View
    protected int[] onCreateDrawableState(int i) {
        int[] onCreateDrawableState = super.onCreateDrawableState(i + 1);
        if (isChecked()) {
            mergeDrawableStates(onCreateDrawableState, CHECKED_STATE_SET);
        }
        return onCreateDrawableState;
    }

    @Override // android.widget.CompoundButton, android.widget.TextView, android.view.View
    protected void onDraw(Canvas canvas) {
        int width;
        super.onDraw(canvas);
        Rect rect = this.mTempRect;
        Drawable drawable = this.mTrackDrawable;
        if (drawable != null) {
            drawable.getPadding(rect);
        } else {
            rect.setEmpty();
        }
        int i = this.mSwitchTop;
        int i2 = this.mSwitchBottom;
        int i3 = rect.top;
        int i4 = rect.bottom;
        Drawable drawable2 = this.mThumbDrawable;
        if (drawable != null) {
            if (!this.mSplitTrack || drawable2 == null) {
                drawable.draw(canvas);
            } else {
                Rect opticalBounds = DrawableUtils.getOpticalBounds(drawable2);
                drawable2.copyBounds(rect);
                rect.left += opticalBounds.left;
                rect.right -= opticalBounds.right;
                int save = canvas.save();
                canvas.clipRect(rect, Region.Op.DIFFERENCE);
                drawable.draw(canvas);
                canvas.restoreToCount(save);
            }
        }
        int save2 = canvas.save();
        if (drawable2 != null) {
            drawable2.draw(canvas);
        }
        Layout layout = getTargetCheckedState() ? this.mOnLayout : this.mOffLayout;
        if (layout != null) {
            int[] drawableState = getDrawableState();
            if (this.mTextColors != null) {
                this.mTextPaint.setColor(this.mTextColors.getColorForState(drawableState, 0));
            }
            this.mTextPaint.drawableState = drawableState;
            if (drawable2 != null) {
                Rect bounds = drawable2.getBounds();
                width = bounds.left + bounds.right;
            } else {
                width = getWidth();
            }
            canvas.translate((width / 2) - (layout.getWidth() / 2), (((i + i3) + (i2 - i4)) / 2) - (layout.getHeight() / 2));
            layout.draw(canvas);
        }
        canvas.restoreToCount(save2);
    }

    @Override // android.view.View
    @TargetApi(14)
    public void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        super.onInitializeAccessibilityEvent(accessibilityEvent);
        accessibilityEvent.setClassName("android.widget.Switch");
    }

    @Override // android.view.View
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        if (Build.VERSION.SDK_INT >= 14) {
            super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
            accessibilityNodeInfo.setClassName("android.widget.Switch");
            CharSequence charSequence = isChecked() ? this.mTextOn : this.mTextOff;
            if (TextUtils.isEmpty(charSequence)) {
                return;
            }
            CharSequence text = accessibilityNodeInfo.getText();
            if (TextUtils.isEmpty(text)) {
                accessibilityNodeInfo.setText(charSequence);
                return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append(text).append(' ').append(charSequence);
            accessibilityNodeInfo.setText(sb);
        }
    }

    @Override // android.widget.TextView, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int width;
        int i5;
        int height;
        int i6;
        super.onLayout(z, i, i2, i3, i4);
        int i7 = 0;
        int i8 = 0;
        if (this.mThumbDrawable != null) {
            Rect rect = this.mTempRect;
            if (this.mTrackDrawable != null) {
                this.mTrackDrawable.getPadding(rect);
            } else {
                rect.setEmpty();
            }
            Rect opticalBounds = DrawableUtils.getOpticalBounds(this.mThumbDrawable);
            i7 = Math.max(0, opticalBounds.left - rect.left);
            i8 = Math.max(0, opticalBounds.right - rect.right);
        }
        if (ViewUtils.isLayoutRtl(this)) {
            i5 = getPaddingLeft() + i7;
            width = ((this.mSwitchWidth + i5) - i7) - i8;
        } else {
            width = (getWidth() - getPaddingRight()) - i8;
            i5 = (width - this.mSwitchWidth) + i7 + i8;
        }
        switch (getGravity() & 112) {
            case 16:
                i6 = (((getPaddingTop() + getHeight()) - getPaddingBottom()) / 2) - (this.mSwitchHeight / 2);
                height = i6 + this.mSwitchHeight;
                break;
            case 48:
            default:
                i6 = getPaddingTop();
                height = i6 + this.mSwitchHeight;
                break;
            case 80:
                height = getHeight() - getPaddingBottom();
                i6 = height - this.mSwitchHeight;
                break;
        }
        this.mSwitchLeft = i5;
        this.mSwitchTop = i6;
        this.mSwitchBottom = height;
        this.mSwitchRight = width;
    }

    @Override // android.widget.TextView, android.view.View
    public void onMeasure(int i, int i2) {
        int i3;
        int i4;
        int i5;
        if (this.mShowText) {
            if (this.mOnLayout == null) {
                this.mOnLayout = makeLayout(this.mTextOn);
            }
            if (this.mOffLayout == null) {
                this.mOffLayout = makeLayout(this.mTextOff);
            }
        }
        Rect rect = this.mTempRect;
        if (this.mThumbDrawable != null) {
            this.mThumbDrawable.getPadding(rect);
            i3 = (this.mThumbDrawable.getIntrinsicWidth() - rect.left) - rect.right;
            i4 = this.mThumbDrawable.getIntrinsicHeight();
        } else {
            i3 = 0;
            i4 = 0;
        }
        this.mThumbWidth = Math.max(this.mShowText ? Math.max(this.mOnLayout.getWidth(), this.mOffLayout.getWidth()) + (this.mThumbTextPadding * 2) : 0, i3);
        if (this.mTrackDrawable != null) {
            this.mTrackDrawable.getPadding(rect);
            i5 = this.mTrackDrawable.getIntrinsicHeight();
        } else {
            rect.setEmpty();
            i5 = 0;
        }
        int i6 = rect.left;
        int i7 = rect.right;
        int i8 = i6;
        int i9 = i7;
        if (this.mThumbDrawable != null) {
            Rect opticalBounds = DrawableUtils.getOpticalBounds(this.mThumbDrawable);
            i8 = Math.max(i6, opticalBounds.left);
            i9 = Math.max(i7, opticalBounds.right);
        }
        int max = Math.max(this.mSwitchMinWidth, (this.mThumbWidth * 2) + i8 + i9);
        int max2 = Math.max(i5, i4);
        this.mSwitchWidth = max;
        this.mSwitchHeight = max2;
        super.onMeasure(i, i2);
        if (getMeasuredHeight() < max2) {
            setMeasuredDimension(ViewCompat.getMeasuredWidthAndState(this), max2);
        }
    }

    @Override // android.view.View
    @TargetApi(14)
    public void onPopulateAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        super.onPopulateAccessibilityEvent(accessibilityEvent);
        CharSequence charSequence = isChecked() ? this.mTextOn : this.mTextOff;
        if (charSequence != null) {
            accessibilityEvent.getText().add(charSequence);
        }
    }

    @Override // android.widget.TextView, android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        float f;
        this.mVelocityTracker.addMovement(motionEvent);
        switch (MotionEventCompat.getActionMasked(motionEvent)) {
            case 0:
                float x = motionEvent.getX();
                float y = motionEvent.getY();
                if (isEnabled() && hitThumb(x, y)) {
                    this.mTouchMode = 1;
                    this.mTouchX = x;
                    this.mTouchY = y;
                    break;
                }
                break;
            case 1:
            case 3:
                if (this.mTouchMode != 2) {
                    this.mTouchMode = 0;
                    this.mVelocityTracker.clear();
                    break;
                } else {
                    stopDrag(motionEvent);
                    super.onTouchEvent(motionEvent);
                    return true;
                }
            case 2:
                switch (this.mTouchMode) {
                    case 2:
                        float x2 = motionEvent.getX();
                        int thumbScrollRange = getThumbScrollRange();
                        float f2 = x2 - this.mTouchX;
                        if (thumbScrollRange != 0) {
                            f = f2 / thumbScrollRange;
                        } else {
                            f = f2 > 0.0f ? 1 : -1;
                        }
                        float f3 = f;
                        if (ViewUtils.isLayoutRtl(this)) {
                            f3 = -f;
                        }
                        float constrain = constrain(this.mThumbPosition + f3, 0.0f, 1.0f);
                        if (constrain != this.mThumbPosition) {
                            this.mTouchX = x2;
                            setThumbPosition(constrain);
                            return true;
                        }
                        return true;
                    case 1:
                        float x3 = motionEvent.getX();
                        float y2 = motionEvent.getY();
                        if (Math.abs(x3 - this.mTouchX) > this.mTouchSlop || Math.abs(y2 - this.mTouchY) > this.mTouchSlop) {
                            this.mTouchMode = 2;
                            getParent().requestDisallowInterceptTouchEvent(true);
                            this.mTouchX = x3;
                            this.mTouchY = y2;
                            return true;
                        }
                        break;
                }
        }
        return super.onTouchEvent(motionEvent);
    }

    @Override // android.widget.CompoundButton, android.widget.Checkable
    public void setChecked(boolean z) {
        super.setChecked(z);
        boolean isChecked = isChecked();
        if (getWindowToken() != null && ViewCompat.isLaidOut(this) && isShown()) {
            animateThumbToCheckedState(isChecked);
            return;
        }
        cancelPositionAnimator();
        setThumbPosition(isChecked ? 1 : 0);
    }

    public void setSwitchTextAppearance(Context context, int i) {
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(i, R$styleable.TextAppearance);
        ColorStateList colorStateList = obtainStyledAttributes.getColorStateList(R$styleable.TextAppearance_android_textColor);
        if (colorStateList != null) {
            this.mTextColors = colorStateList;
        } else {
            this.mTextColors = getTextColors();
        }
        int dimensionPixelSize = obtainStyledAttributes.getDimensionPixelSize(R$styleable.TextAppearance_android_textSize, 0);
        if (dimensionPixelSize != 0 && dimensionPixelSize != this.mTextPaint.getTextSize()) {
            this.mTextPaint.setTextSize(dimensionPixelSize);
            requestLayout();
        }
        setSwitchTypefaceByIndex(obtainStyledAttributes.getInt(R$styleable.TextAppearance_android_typeface, -1), obtainStyledAttributes.getInt(R$styleable.TextAppearance_android_textStyle, -1));
        if (obtainStyledAttributes.getBoolean(R$styleable.TextAppearance_textAllCaps, false)) {
            this.mSwitchTransformationMethod = new AllCapsTransformationMethod(getContext());
        } else {
            this.mSwitchTransformationMethod = null;
        }
        obtainStyledAttributes.recycle();
    }

    public void setSwitchTypeface(Typeface typeface) {
        if (this.mTextPaint.getTypeface() != typeface) {
            this.mTextPaint.setTypeface(typeface);
            requestLayout();
            invalidate();
        }
    }

    public void setSwitchTypeface(Typeface typeface, int i) {
        boolean z = false;
        if (i <= 0) {
            this.mTextPaint.setFakeBoldText(false);
            this.mTextPaint.setTextSkewX(0.0f);
            setSwitchTypeface(typeface);
            return;
        }
        Typeface defaultFromStyle = typeface == null ? Typeface.defaultFromStyle(i) : Typeface.create(typeface, i);
        setSwitchTypeface(defaultFromStyle);
        int style = i & ((defaultFromStyle != null ? defaultFromStyle.getStyle() : 0) ^ (-1));
        TextPaint textPaint = this.mTextPaint;
        if ((style & 1) != 0) {
            z = true;
        }
        textPaint.setFakeBoldText(z);
        this.mTextPaint.setTextSkewX((style & 2) != 0 ? -0.25f : 0.0f);
    }

    public void setTextOff(CharSequence charSequence) {
        this.mTextOff = charSequence;
        requestLayout();
    }

    public void setTextOn(CharSequence charSequence) {
        this.mTextOn = charSequence;
        requestLayout();
    }

    @Override // android.widget.CompoundButton, android.widget.Checkable
    public void toggle() {
        setChecked(!isChecked());
    }

    @Override // android.widget.CompoundButton, android.widget.TextView, android.view.View
    protected boolean verifyDrawable(Drawable drawable) {
        boolean z = true;
        if (!super.verifyDrawable(drawable)) {
            if (drawable == this.mThumbDrawable) {
                z = true;
            } else {
                z = true;
                if (drawable != this.mTrackDrawable) {
                    z = false;
                }
            }
        }
        return z;
    }
}

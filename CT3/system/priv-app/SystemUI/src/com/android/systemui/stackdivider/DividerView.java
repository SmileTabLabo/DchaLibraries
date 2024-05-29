package com.android.systemui.stackdivider;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.Region;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.PointerIcon;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.policy.DividerSnapAlgorithm;
import com.android.internal.policy.DockedDividerUtils;
import com.android.systemui.Interpolators;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.activity.DockedTopTaskEvent;
import com.android.systemui.recents.events.activity.RecentsActivityStartingEvent;
import com.android.systemui.recents.events.activity.UndockingTaskEvent;
import com.android.systemui.recents.events.ui.RecentsDrawnEvent;
import com.android.systemui.recents.events.ui.RecentsGrowingEvent;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.stackdivider.events.StartedDragingEvent;
import com.android.systemui.stackdivider.events.StoppedDragingEvent;
import com.android.systemui.statusbar.FlingAnimationUtils;
/* loaded from: a.zip:com/android/systemui/stackdivider/DividerView.class */
public class DividerView extends FrameLayout implements View.OnTouchListener, ViewTreeObserver.OnComputeInternalInsetsListener {
    private boolean mAdjustedForIme;
    private View mBackground;
    private boolean mBackgroundLifted;
    private ValueAnimator mCurrentAnimator;
    private int mDisplayHeight;
    private final Rect mDisplayRect;
    private int mDisplayWidth;
    private int mDividerInsets;
    private int mDividerSize;
    private int mDividerWindowWidth;
    private int mDockSide;
    private final Rect mDockedInsetRect;
    private final Rect mDockedRect;
    private boolean mDockedStackMinimized;
    private final Rect mDockedTaskRect;
    private boolean mEntranceAnimationRunning;
    private boolean mExitAnimationRunning;
    private int mExitStartPosition;
    private FlingAnimationUtils mFlingAnimationUtils;
    private GestureDetector mGestureDetector;
    private boolean mGrowRecents;
    private DividerHandleView mHandle;
    private final View.AccessibilityDelegate mHandleDelegate;
    private final Handler mHandler;
    private final Rect mLastResizeRect;
    private int mLongPressEntraceAnimDuration;
    private MinimizedDockShadow mMinimizedShadow;
    private boolean mMoving;
    private final Rect mOtherInsetRect;
    private final Rect mOtherRect;
    private final Rect mOtherTaskRect;
    private final Runnable mResetBackgroundRunnable;
    private DividerSnapAlgorithm mSnapAlgorithm;
    private final Rect mStableInsets;
    private int mStartPosition;
    private int mStartX;
    private int mStartY;
    private DividerState mState;
    private final int[] mTempInt2;
    private int mTouchElevation;
    private int mTouchSlop;
    private VelocityTracker mVelocityTracker;
    private DividerWindowManager mWindowManager;
    private final WindowManagerProxy mWindowManagerProxy;
    private static final PathInterpolator SLOWDOWN_INTERPOLATOR = new PathInterpolator(0.5f, 1.0f, 0.5f, 1.0f);
    private static final PathInterpolator DIM_INTERPOLATOR = new PathInterpolator(0.23f, 0.87f, 0.52f, -0.11f);
    private static final Interpolator IME_ADJUST_INTERPOLATOR = new PathInterpolator(0.2f, 0.0f, 0.1f, 1.0f);

    public DividerView(Context context) {
        super(context);
        this.mTempInt2 = new int[2];
        this.mDockedRect = new Rect();
        this.mDockedTaskRect = new Rect();
        this.mOtherTaskRect = new Rect();
        this.mOtherRect = new Rect();
        this.mDockedInsetRect = new Rect();
        this.mOtherInsetRect = new Rect();
        this.mLastResizeRect = new Rect();
        this.mDisplayRect = new Rect();
        this.mWindowManagerProxy = WindowManagerProxy.getInstance();
        this.mStableInsets = new Rect();
        this.mHandler = new Handler();
        this.mHandleDelegate = new View.AccessibilityDelegate(this) { // from class: com.android.systemui.stackdivider.DividerView.1
            final DividerView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.AccessibilityDelegate
            public void onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfo accessibilityNodeInfo) {
                super.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfo);
                if (this.this$0.isHorizontalDivision()) {
                    accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(2131886149, this.this$0.mContext.getString(2131493888)));
                    if (this.this$0.mSnapAlgorithm.isFirstSplitTargetAvailable()) {
                        accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(2131886150, this.this$0.mContext.getString(2131493889)));
                    }
                    accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(2131886151, this.this$0.mContext.getString(2131493890)));
                    if (this.this$0.mSnapAlgorithm.isLastSplitTargetAvailable()) {
                        accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(2131886152, this.this$0.mContext.getString(2131493891)));
                    }
                    accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(2131886153, this.this$0.mContext.getString(2131493892)));
                    return;
                }
                accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(2131886149, this.this$0.mContext.getString(2131493883)));
                if (this.this$0.mSnapAlgorithm.isFirstSplitTargetAvailable()) {
                    accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(2131886150, this.this$0.mContext.getString(2131493884)));
                }
                accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(2131886151, this.this$0.mContext.getString(2131493885)));
                if (this.this$0.mSnapAlgorithm.isLastSplitTargetAvailable()) {
                    accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(2131886152, this.this$0.mContext.getString(2131493886)));
                }
                accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(2131886153, this.this$0.mContext.getString(2131493887)));
            }

            @Override // android.view.View.AccessibilityDelegate
            public boolean performAccessibilityAction(View view, int i, Bundle bundle) {
                int currentPosition = this.this$0.getCurrentPosition();
                DividerSnapAlgorithm.SnapTarget snapTarget = null;
                switch (i) {
                    case 2131886149:
                        snapTarget = this.this$0.mSnapAlgorithm.getDismissEndTarget();
                        break;
                    case 2131886150:
                        snapTarget = this.this$0.mSnapAlgorithm.getLastSplitTarget();
                        break;
                    case 2131886151:
                        snapTarget = this.this$0.mSnapAlgorithm.getMiddleTarget();
                        break;
                    case 2131886152:
                        snapTarget = this.this$0.mSnapAlgorithm.getFirstSplitTarget();
                        break;
                    case 2131886153:
                        snapTarget = this.this$0.mSnapAlgorithm.getDismissStartTarget();
                        break;
                }
                if (snapTarget != null) {
                    this.this$0.startDragging(true, false);
                    this.this$0.stopDragging(currentPosition, snapTarget, 250L, Interpolators.FAST_OUT_SLOW_IN);
                    return true;
                }
                return super.performAccessibilityAction(view, i, bundle);
            }
        };
        this.mResetBackgroundRunnable = new Runnable(this) { // from class: com.android.systemui.stackdivider.DividerView.2
            final DividerView this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.resetBackground();
            }
        };
    }

    public DividerView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mTempInt2 = new int[2];
        this.mDockedRect = new Rect();
        this.mDockedTaskRect = new Rect();
        this.mOtherTaskRect = new Rect();
        this.mOtherRect = new Rect();
        this.mDockedInsetRect = new Rect();
        this.mOtherInsetRect = new Rect();
        this.mLastResizeRect = new Rect();
        this.mDisplayRect = new Rect();
        this.mWindowManagerProxy = WindowManagerProxy.getInstance();
        this.mStableInsets = new Rect();
        this.mHandler = new Handler();
        this.mHandleDelegate = new View.AccessibilityDelegate(this) { // from class: com.android.systemui.stackdivider.DividerView.1
            final DividerView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.AccessibilityDelegate
            public void onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfo accessibilityNodeInfo) {
                super.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfo);
                if (this.this$0.isHorizontalDivision()) {
                    accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(2131886149, this.this$0.mContext.getString(2131493888)));
                    if (this.this$0.mSnapAlgorithm.isFirstSplitTargetAvailable()) {
                        accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(2131886150, this.this$0.mContext.getString(2131493889)));
                    }
                    accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(2131886151, this.this$0.mContext.getString(2131493890)));
                    if (this.this$0.mSnapAlgorithm.isLastSplitTargetAvailable()) {
                        accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(2131886152, this.this$0.mContext.getString(2131493891)));
                    }
                    accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(2131886153, this.this$0.mContext.getString(2131493892)));
                    return;
                }
                accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(2131886149, this.this$0.mContext.getString(2131493883)));
                if (this.this$0.mSnapAlgorithm.isFirstSplitTargetAvailable()) {
                    accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(2131886150, this.this$0.mContext.getString(2131493884)));
                }
                accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(2131886151, this.this$0.mContext.getString(2131493885)));
                if (this.this$0.mSnapAlgorithm.isLastSplitTargetAvailable()) {
                    accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(2131886152, this.this$0.mContext.getString(2131493886)));
                }
                accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(2131886153, this.this$0.mContext.getString(2131493887)));
            }

            @Override // android.view.View.AccessibilityDelegate
            public boolean performAccessibilityAction(View view, int i, Bundle bundle) {
                int currentPosition = this.this$0.getCurrentPosition();
                DividerSnapAlgorithm.SnapTarget snapTarget = null;
                switch (i) {
                    case 2131886149:
                        snapTarget = this.this$0.mSnapAlgorithm.getDismissEndTarget();
                        break;
                    case 2131886150:
                        snapTarget = this.this$0.mSnapAlgorithm.getLastSplitTarget();
                        break;
                    case 2131886151:
                        snapTarget = this.this$0.mSnapAlgorithm.getMiddleTarget();
                        break;
                    case 2131886152:
                        snapTarget = this.this$0.mSnapAlgorithm.getFirstSplitTarget();
                        break;
                    case 2131886153:
                        snapTarget = this.this$0.mSnapAlgorithm.getDismissStartTarget();
                        break;
                }
                if (snapTarget != null) {
                    this.this$0.startDragging(true, false);
                    this.this$0.stopDragging(currentPosition, snapTarget, 250L, Interpolators.FAST_OUT_SLOW_IN);
                    return true;
                }
                return super.performAccessibilityAction(view, i, bundle);
            }
        };
        this.mResetBackgroundRunnable = new Runnable(this) { // from class: com.android.systemui.stackdivider.DividerView.2
            final DividerView this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.resetBackground();
            }
        };
    }

    public DividerView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mTempInt2 = new int[2];
        this.mDockedRect = new Rect();
        this.mDockedTaskRect = new Rect();
        this.mOtherTaskRect = new Rect();
        this.mOtherRect = new Rect();
        this.mDockedInsetRect = new Rect();
        this.mOtherInsetRect = new Rect();
        this.mLastResizeRect = new Rect();
        this.mDisplayRect = new Rect();
        this.mWindowManagerProxy = WindowManagerProxy.getInstance();
        this.mStableInsets = new Rect();
        this.mHandler = new Handler();
        this.mHandleDelegate = new View.AccessibilityDelegate(this) { // from class: com.android.systemui.stackdivider.DividerView.1
            final DividerView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.AccessibilityDelegate
            public void onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfo accessibilityNodeInfo) {
                super.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfo);
                if (this.this$0.isHorizontalDivision()) {
                    accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(2131886149, this.this$0.mContext.getString(2131493888)));
                    if (this.this$0.mSnapAlgorithm.isFirstSplitTargetAvailable()) {
                        accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(2131886150, this.this$0.mContext.getString(2131493889)));
                    }
                    accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(2131886151, this.this$0.mContext.getString(2131493890)));
                    if (this.this$0.mSnapAlgorithm.isLastSplitTargetAvailable()) {
                        accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(2131886152, this.this$0.mContext.getString(2131493891)));
                    }
                    accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(2131886153, this.this$0.mContext.getString(2131493892)));
                    return;
                }
                accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(2131886149, this.this$0.mContext.getString(2131493883)));
                if (this.this$0.mSnapAlgorithm.isFirstSplitTargetAvailable()) {
                    accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(2131886150, this.this$0.mContext.getString(2131493884)));
                }
                accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(2131886151, this.this$0.mContext.getString(2131493885)));
                if (this.this$0.mSnapAlgorithm.isLastSplitTargetAvailable()) {
                    accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(2131886152, this.this$0.mContext.getString(2131493886)));
                }
                accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(2131886153, this.this$0.mContext.getString(2131493887)));
            }

            @Override // android.view.View.AccessibilityDelegate
            public boolean performAccessibilityAction(View view, int i2, Bundle bundle) {
                int currentPosition = this.this$0.getCurrentPosition();
                DividerSnapAlgorithm.SnapTarget snapTarget = null;
                switch (i2) {
                    case 2131886149:
                        snapTarget = this.this$0.mSnapAlgorithm.getDismissEndTarget();
                        break;
                    case 2131886150:
                        snapTarget = this.this$0.mSnapAlgorithm.getLastSplitTarget();
                        break;
                    case 2131886151:
                        snapTarget = this.this$0.mSnapAlgorithm.getMiddleTarget();
                        break;
                    case 2131886152:
                        snapTarget = this.this$0.mSnapAlgorithm.getFirstSplitTarget();
                        break;
                    case 2131886153:
                        snapTarget = this.this$0.mSnapAlgorithm.getDismissStartTarget();
                        break;
                }
                if (snapTarget != null) {
                    this.this$0.startDragging(true, false);
                    this.this$0.stopDragging(currentPosition, snapTarget, 250L, Interpolators.FAST_OUT_SLOW_IN);
                    return true;
                }
                return super.performAccessibilityAction(view, i2, bundle);
            }
        };
        this.mResetBackgroundRunnable = new Runnable(this) { // from class: com.android.systemui.stackdivider.DividerView.2
            final DividerView this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.resetBackground();
            }
        };
    }

    public DividerView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mTempInt2 = new int[2];
        this.mDockedRect = new Rect();
        this.mDockedTaskRect = new Rect();
        this.mOtherTaskRect = new Rect();
        this.mOtherRect = new Rect();
        this.mDockedInsetRect = new Rect();
        this.mOtherInsetRect = new Rect();
        this.mLastResizeRect = new Rect();
        this.mDisplayRect = new Rect();
        this.mWindowManagerProxy = WindowManagerProxy.getInstance();
        this.mStableInsets = new Rect();
        this.mHandler = new Handler();
        this.mHandleDelegate = new View.AccessibilityDelegate(this) { // from class: com.android.systemui.stackdivider.DividerView.1
            final DividerView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.AccessibilityDelegate
            public void onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfo accessibilityNodeInfo) {
                super.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfo);
                if (this.this$0.isHorizontalDivision()) {
                    accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(2131886149, this.this$0.mContext.getString(2131493888)));
                    if (this.this$0.mSnapAlgorithm.isFirstSplitTargetAvailable()) {
                        accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(2131886150, this.this$0.mContext.getString(2131493889)));
                    }
                    accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(2131886151, this.this$0.mContext.getString(2131493890)));
                    if (this.this$0.mSnapAlgorithm.isLastSplitTargetAvailable()) {
                        accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(2131886152, this.this$0.mContext.getString(2131493891)));
                    }
                    accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(2131886153, this.this$0.mContext.getString(2131493892)));
                    return;
                }
                accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(2131886149, this.this$0.mContext.getString(2131493883)));
                if (this.this$0.mSnapAlgorithm.isFirstSplitTargetAvailable()) {
                    accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(2131886150, this.this$0.mContext.getString(2131493884)));
                }
                accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(2131886151, this.this$0.mContext.getString(2131493885)));
                if (this.this$0.mSnapAlgorithm.isLastSplitTargetAvailable()) {
                    accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(2131886152, this.this$0.mContext.getString(2131493886)));
                }
                accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(2131886153, this.this$0.mContext.getString(2131493887)));
            }

            @Override // android.view.View.AccessibilityDelegate
            public boolean performAccessibilityAction(View view, int i22, Bundle bundle) {
                int currentPosition = this.this$0.getCurrentPosition();
                DividerSnapAlgorithm.SnapTarget snapTarget = null;
                switch (i22) {
                    case 2131886149:
                        snapTarget = this.this$0.mSnapAlgorithm.getDismissEndTarget();
                        break;
                    case 2131886150:
                        snapTarget = this.this$0.mSnapAlgorithm.getLastSplitTarget();
                        break;
                    case 2131886151:
                        snapTarget = this.this$0.mSnapAlgorithm.getMiddleTarget();
                        break;
                    case 2131886152:
                        snapTarget = this.this$0.mSnapAlgorithm.getFirstSplitTarget();
                        break;
                    case 2131886153:
                        snapTarget = this.this$0.mSnapAlgorithm.getDismissStartTarget();
                        break;
                }
                if (snapTarget != null) {
                    this.this$0.startDragging(true, false);
                    this.this$0.stopDragging(currentPosition, snapTarget, 250L, Interpolators.FAST_OUT_SLOW_IN);
                    return true;
                }
                return super.performAccessibilityAction(view, i22, bundle);
            }
        };
        this.mResetBackgroundRunnable = new Runnable(this) { // from class: com.android.systemui.stackdivider.DividerView.2
            final DividerView this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.resetBackground();
            }
        };
    }

    private void alignBottomRight(Rect rect, Rect rect2) {
        rect2.set(rect.right - rect2.width(), rect.bottom - rect2.height(), rect.right, rect.bottom);
    }

    private void alignTopLeft(Rect rect, Rect rect2) {
        rect2.set(rect.left, rect.top, rect.left + rect2.width(), rect.top + rect2.height());
    }

    private void applyDismissingParallax(Rect rect, int i, DividerSnapAlgorithm.SnapTarget snapTarget, int i2, int i3) {
        DividerSnapAlgorithm.SnapTarget snapTarget2;
        DividerSnapAlgorithm.SnapTarget snapTarget3;
        float min = Math.min(1.0f, Math.max(0.0f, this.mSnapAlgorithm.calculateDismissingFraction(i2)));
        if (i2 > this.mSnapAlgorithm.getLastSplitTarget().position || !dockSideTopLeft(i)) {
            snapTarget2 = null;
            snapTarget3 = null;
            i3 = 0;
            if (i2 >= this.mSnapAlgorithm.getLastSplitTarget().position) {
                snapTarget2 = null;
                snapTarget3 = null;
                i3 = 0;
                if (dockSideBottomRight(i)) {
                    snapTarget2 = this.mSnapAlgorithm.getDismissEndTarget();
                    snapTarget3 = this.mSnapAlgorithm.getLastSplitTarget();
                    i3 = snapTarget3.position;
                }
            }
        } else {
            snapTarget2 = this.mSnapAlgorithm.getDismissStartTarget();
            snapTarget3 = this.mSnapAlgorithm.getFirstSplitTarget();
        }
        if (snapTarget2 == null || min <= 0.0f || !isDismissing(snapTarget3, i2, i)) {
            return;
        }
        int calculateParallaxDismissingFraction = (int) (i3 + ((snapTarget2.position - snapTarget3.position) * calculateParallaxDismissingFraction(min, i)));
        int width = rect.width();
        int height = rect.height();
        switch (i) {
            case 1:
                rect.left = calculateParallaxDismissingFraction - width;
                rect.right = calculateParallaxDismissingFraction;
                return;
            case 2:
                rect.top = calculateParallaxDismissingFraction - height;
                rect.bottom = calculateParallaxDismissingFraction;
                return;
            case 3:
                rect.left = this.mDividerSize + calculateParallaxDismissingFraction;
                rect.right = calculateParallaxDismissingFraction + width + this.mDividerSize;
                return;
            case 4:
                rect.top = this.mDividerSize + calculateParallaxDismissingFraction;
                rect.bottom = calculateParallaxDismissingFraction + height + this.mDividerSize;
                return;
            default:
                return;
        }
    }

    private void applyExitAnimationParallax(Rect rect, int i) {
        if (this.mDockSide == 2) {
            rect.offset(0, (int) ((i - this.mExitStartPosition) * 0.25f));
        } else if (this.mDockSide == 1) {
            rect.offset((int) ((i - this.mExitStartPosition) * 0.25f), 0);
        } else if (this.mDockSide == 3) {
            rect.offset((int) ((this.mExitStartPosition - i) * 0.25f), 0);
        }
    }

    private static float calculateParallaxDismissingFraction(float f, int i) {
        float interpolation = SLOWDOWN_INTERPOLATOR.getInterpolation(f) / 3.5f;
        float f2 = interpolation;
        if (i == 2) {
            f2 = interpolation / 2.0f;
        }
        return f2;
    }

    private int calculatePosition(int i, int i2) {
        return isHorizontalDivision() ? calculateYPosition(i2) : calculateXPosition(i);
    }

    private int calculateXPosition(int i) {
        return (this.mStartPosition + i) - this.mStartX;
    }

    private int calculateYPosition(int i) {
        return (this.mStartPosition + i) - this.mStartY;
    }

    private void cancelFlingAnimation() {
        if (this.mCurrentAnimator != null) {
            this.mCurrentAnimator.cancel();
        }
    }

    private void commitSnapFlags(DividerSnapAlgorithm.SnapTarget snapTarget) {
        if (snapTarget.flag == 0) {
            return;
        }
        if (snapTarget.flag == 1 ? this.mDockSide != 1 ? this.mDockSide == 2 : true : this.mDockSide != 3 ? this.mDockSide == 4 : true) {
            this.mWindowManagerProxy.dismissDockedStack();
        } else {
            this.mWindowManagerProxy.maximizeDockedStack();
        }
        this.mWindowManagerProxy.setResizeDimLayer(false, -1, 0.0f);
    }

    private void convertToScreenCoordinates(MotionEvent motionEvent) {
        motionEvent.setLocation(motionEvent.getRawX(), motionEvent.getRawY());
    }

    private static boolean dockSideBottomRight(int i) {
        boolean z = true;
        if (i != 4) {
            z = i == 3;
        }
        return z;
    }

    private static boolean dockSideTopLeft(int i) {
        boolean z = true;
        if (i != 2) {
            z = i == 1;
        }
        return z;
    }

    private void fling(int i, float f, boolean z, boolean z2) {
        DividerSnapAlgorithm.SnapTarget calculateSnapTarget = this.mSnapAlgorithm.calculateSnapTarget(i, f);
        DividerSnapAlgorithm.SnapTarget snapTarget = calculateSnapTarget;
        if (z) {
            snapTarget = calculateSnapTarget;
            if (calculateSnapTarget == this.mSnapAlgorithm.getDismissStartTarget()) {
                snapTarget = this.mSnapAlgorithm.getFirstSplitTarget();
            }
        }
        if (z2) {
            logResizeEvent(snapTarget);
        }
        ValueAnimator flingAnimator = getFlingAnimator(i, snapTarget, 0L);
        this.mFlingAnimationUtils.apply(flingAnimator, i, snapTarget.position, f);
        flingAnimator.start();
    }

    private void flingTo(int i, DividerSnapAlgorithm.SnapTarget snapTarget, long j, long j2, long j3, Interpolator interpolator) {
        ValueAnimator flingAnimator = getFlingAnimator(i, snapTarget, j3);
        flingAnimator.setDuration(j);
        flingAnimator.setStartDelay(j2);
        flingAnimator.setInterpolator(interpolator);
        flingAnimator.start();
    }

    private float getDimFraction(int i, DividerSnapAlgorithm.SnapTarget snapTarget) {
        if (this.mEntranceAnimationRunning) {
            return 0.0f;
        }
        float interpolation = DIM_INTERPOLATOR.getInterpolation(Math.max(0.0f, Math.min(this.mSnapAlgorithm.calculateDismissingFraction(i), 1.0f)));
        float f = interpolation;
        if (hasInsetsAtDismissTarget(snapTarget)) {
            f = interpolation * 0.8f;
        }
        return f;
    }

    private ValueAnimator getFlingAnimator(int i, final DividerSnapAlgorithm.SnapTarget snapTarget, long j) {
        boolean z = snapTarget.flag == 0;
        ValueAnimator ofInt = ValueAnimator.ofInt(i, snapTarget.position);
        final boolean z2 = z;
        ofInt.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this, z2, snapTarget) { // from class: com.android.systemui.stackdivider.DividerView$_android_animation_ValueAnimator_getFlingAnimator_int_position_com_android_internal_policy_DividerSnapAlgorithm$SnapTarget_snapTarget_long_endDelay_LambdaImpl0
            private DividerSnapAlgorithm.SnapTarget val$snapTarget;
            private boolean val$taskPositionSameAtEnd;
            private DividerView val$this;

            {
                this.val$this = this;
                this.val$taskPositionSameAtEnd = z2;
                this.val$snapTarget = snapTarget;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.val$this.m1364com_android_systemui_stackdivider_DividerView_lambda$1(this.val$taskPositionSameAtEnd, this.val$snapTarget, valueAnimator);
            }
        });
        ofInt.addListener(new AnimatorListenerAdapter(this, j, new Runnable(this, snapTarget) { // from class: com.android.systemui.stackdivider.DividerView$_android_animation_ValueAnimator_getFlingAnimator_int_position_com_android_internal_policy_DividerSnapAlgorithm$SnapTarget_snapTarget_long_endDelay_LambdaImpl1
            private DividerSnapAlgorithm.SnapTarget val$snapTarget;
            private DividerView val$this;

            {
                this.val$this = this;
                this.val$snapTarget = snapTarget;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.val$this.m1365com_android_systemui_stackdivider_DividerView_lambda$2(this.val$snapTarget);
            }
        }) { // from class: com.android.systemui.stackdivider.DividerView.4
            private boolean mCancelled;
            final DividerView this$0;
            final Runnable val$endAction;
            final long val$endDelay;

            {
                this.this$0 = this;
                this.val$endDelay = j;
                this.val$endAction = r8;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
                this.mCancelled = true;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (this.val$endDelay == 0 || this.mCancelled) {
                    this.val$endAction.run();
                } else {
                    this.this$0.mHandler.postDelayed(this.val$endAction, this.val$endDelay);
                }
            }
        });
        this.mCurrentAnimator = ofInt;
        return ofInt;
    }

    private int getStackIdForDismissTarget(DividerSnapAlgorithm.SnapTarget snapTarget) {
        if (snapTarget.flag == 1 && dockSideTopLeft(this.mDockSide)) {
            return 3;
        }
        return (snapTarget.flag == 2 && dockSideBottomRight(this.mDockSide)) ? 3 : 0;
    }

    private boolean hasInsetsAtDismissTarget(DividerSnapAlgorithm.SnapTarget snapTarget) {
        boolean z = true;
        if (!isHorizontalDivision()) {
            if (snapTarget == this.mSnapAlgorithm.getDismissStartTarget()) {
                return this.mStableInsets.left != 0;
            }
            return this.mStableInsets.right != 0;
        } else if (snapTarget != this.mSnapAlgorithm.getDismissStartTarget()) {
            return this.mStableInsets.bottom != 0;
        } else {
            if (this.mStableInsets.top == 0) {
                z = false;
            }
            return z;
        }
    }

    private void initializeSnapAlgorithm() {
        if (this.mSnapAlgorithm == null) {
            this.mSnapAlgorithm = new DividerSnapAlgorithm(getContext().getResources(), this.mDisplayWidth, this.mDisplayHeight, this.mDividerSize, isHorizontalDivision(), this.mStableInsets);
        }
    }

    private static boolean isDismissing(DividerSnapAlgorithm.SnapTarget snapTarget, int i, int i2) {
        boolean z = true;
        if (i2 != 2 && i2 != 1) {
            return i > snapTarget.position;
        }
        if (i >= snapTarget.position) {
            z = false;
        }
        return z;
    }

    private void liftBackground() {
        if (this.mBackgroundLifted) {
            return;
        }
        if (isHorizontalDivision()) {
            this.mBackground.animate().scaleY(1.4f);
        } else {
            this.mBackground.animate().scaleX(1.4f);
        }
        this.mBackground.animate().setInterpolator(Interpolators.TOUCH_RESPONSE).setDuration(150L).translationZ(this.mTouchElevation).start();
        this.mHandle.animate().setInterpolator(Interpolators.TOUCH_RESPONSE).setDuration(150L).translationZ(this.mTouchElevation).start();
        this.mBackgroundLifted = true;
    }

    private void logResizeEvent(DividerSnapAlgorithm.SnapTarget snapTarget) {
        int i = 1;
        if (snapTarget == this.mSnapAlgorithm.getDismissStartTarget()) {
            Context context = this.mContext;
            if (!dockSideTopLeft(this.mDockSide)) {
                i = 0;
            }
            MetricsLogger.action(context, 390, i);
        } else if (snapTarget == this.mSnapAlgorithm.getDismissEndTarget()) {
            MetricsLogger.action(this.mContext, 390, dockSideBottomRight(this.mDockSide) ? 1 : 0);
        } else if (snapTarget == this.mSnapAlgorithm.getMiddleTarget()) {
            MetricsLogger.action(this.mContext, 389, 0);
        } else if (snapTarget == this.mSnapAlgorithm.getFirstSplitTarget()) {
            MetricsLogger.action(this.mContext, 389, dockSideTopLeft(this.mDockSide) ? 1 : 2);
        } else if (snapTarget == this.mSnapAlgorithm.getLastSplitTarget()) {
            MetricsLogger.action(this.mContext, 389, dockSideTopLeft(this.mDockSide) ? 2 : 1);
        }
    }

    private void releaseBackground() {
        if (this.mBackgroundLifted) {
            this.mBackground.animate().setInterpolator(Interpolators.FAST_OUT_SLOW_IN).setDuration(200L).translationZ(0.0f).scaleX(1.0f).scaleY(1.0f).start();
            this.mHandle.animate().setInterpolator(Interpolators.FAST_OUT_SLOW_IN).setDuration(200L).translationZ(0.0f).start();
            this.mBackgroundLifted = false;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void resetBackground() {
        this.mBackground.setPivotX(this.mBackground.getWidth() / 2);
        this.mBackground.setPivotY(this.mBackground.getHeight() / 2);
        this.mBackground.setScaleX(1.0f);
        this.mBackground.setScaleY(1.0f);
        this.mMinimizedShadow.setAlpha(0.0f);
    }

    private int restrictDismissingTaskPosition(int i, int i2, DividerSnapAlgorithm.SnapTarget snapTarget) {
        return (snapTarget.flag == 1 && dockSideTopLeft(i2)) ? Math.max(this.mSnapAlgorithm.getFirstSplitTarget().position, this.mStartPosition) : (snapTarget.flag == 2 && dockSideBottomRight(i2)) ? Math.min(this.mSnapAlgorithm.getLastSplitTarget().position, this.mStartPosition) : i;
    }

    private void stopDragging() {
        this.mHandle.setTouching(false, true);
        this.mWindowManager.setSlippery(true);
        releaseBackground();
    }

    private void updateDisplayInfo() {
        Display display = ((DisplayManager) this.mContext.getSystemService("display")).getDisplay(0);
        DisplayInfo displayInfo = new DisplayInfo();
        display.getDisplayInfo(displayInfo);
        this.mDisplayWidth = displayInfo.logicalWidth;
        this.mDisplayHeight = displayInfo.logicalHeight;
        this.mSnapAlgorithm = null;
        initializeSnapAlgorithm();
    }

    private void updateDockSide() {
        this.mDockSide = this.mWindowManagerProxy.getDockSide();
        this.mMinimizedShadow.setDockSide(this.mDockSide);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: -com_android_systemui_stackdivider_DividerView_lambda$1  reason: not valid java name */
    public /* synthetic */ void m1364com_android_systemui_stackdivider_DividerView_lambda$1(boolean z, DividerSnapAlgorithm.SnapTarget snapTarget, ValueAnimator valueAnimator) {
        resizeStack(((Integer) valueAnimator.getAnimatedValue()).intValue(), (z && valueAnimator.getAnimatedFraction() == 1.0f) ? Integer.MAX_VALUE : snapTarget.taskPosition, snapTarget);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: -com_android_systemui_stackdivider_DividerView_lambda$2  reason: not valid java name */
    public /* synthetic */ void m1365com_android_systemui_stackdivider_DividerView_lambda$2(DividerSnapAlgorithm.SnapTarget snapTarget) {
        commitSnapFlags(snapTarget);
        this.mWindowManagerProxy.setResizing(false);
        this.mDockSide = -1;
        this.mCurrentAnimator = null;
        this.mEntranceAnimationRunning = false;
        this.mExitAnimationRunning = false;
        EventBus.getDefault().send(new StoppedDragingEvent());
    }

    /* renamed from: -com_android_systemui_stackdivider_DividerView_lambda$3  reason: not valid java name */
    /* synthetic */ void m1366com_android_systemui_stackdivider_DividerView_lambda$3() {
        stopDragging(getCurrentPosition(), this.mSnapAlgorithm.getMiddleTarget(), this.mLongPressEntraceAnimDuration, Interpolators.FAST_OUT_SLOW_IN, 200L);
    }

    public void calculateBoundsForPosition(int i, int i2, Rect rect) {
        DockedDividerUtils.calculateBoundsForPosition(i, i2, rect, this.mDisplayWidth, this.mDisplayHeight, this.mDividerSize);
    }

    public int getCurrentPosition() {
        getLocationOnScreen(this.mTempInt2);
        return isHorizontalDivision() ? this.mTempInt2[1] + this.mDividerInsets : this.mTempInt2[0] + this.mDividerInsets;
    }

    public DividerSnapAlgorithm getSnapAlgorithm() {
        initializeSnapAlgorithm();
        return this.mSnapAlgorithm;
    }

    public WindowManagerProxy getWindowManagerProxy() {
        return this.mWindowManagerProxy;
    }

    public int growsRecents() {
        boolean z = false;
        if (!this.mGrowRecents || this.mWindowManagerProxy.getDockSide() != 2) {
            z = false;
        } else if (getCurrentPosition() == getSnapAlgorithm().getLastSplitTarget().position) {
            z = true;
        }
        if (z) {
            return getSnapAlgorithm().getMiddleTarget().position;
        }
        return -1;
    }

    public void injectDependencies(DividerWindowManager dividerWindowManager, DividerState dividerState) {
        this.mWindowManager = dividerWindowManager;
        this.mState = dividerState;
    }

    public boolean isHorizontalDivision() {
        boolean z = true;
        if (getResources().getConfiguration().orientation != 1) {
            z = false;
        }
        return z;
    }

    public void notifyDockSideChanged(int i) {
        this.mDockSide = i;
        this.mMinimizedShadow.setDockSide(this.mDockSide);
        requestLayout();
    }

    @Override // android.view.View
    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        if (this.mStableInsets.left != windowInsets.getStableInsetLeft() || this.mStableInsets.top != windowInsets.getStableInsetTop() || this.mStableInsets.right != windowInsets.getStableInsetRight() || this.mStableInsets.bottom != windowInsets.getStableInsetBottom()) {
            this.mStableInsets.set(windowInsets.getStableInsetLeft(), windowInsets.getStableInsetTop(), windowInsets.getStableInsetRight(), windowInsets.getStableInsetBottom());
            if (this.mSnapAlgorithm != null) {
                this.mSnapAlgorithm = null;
                initializeSnapAlgorithm();
            }
        }
        return super.onApplyWindowInsets(windowInsets);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        EventBus.getDefault().register(this);
    }

    public final void onBusEvent(DockedTopTaskEvent dockedTopTaskEvent) {
        if (dockedTopTaskEvent.dragMode == -1) {
            this.mState.growAfterRecentsDrawn = false;
            this.mState.animateAfterRecentsDrawn = true;
            startDragging(false, false);
        }
        updateDockSide();
        int calculatePositionForBounds = DockedDividerUtils.calculatePositionForBounds(dockedTopTaskEvent.initialRect, this.mDockSide, this.mDividerSize);
        this.mEntranceAnimationRunning = true;
        if (this.mStableInsets.isEmpty()) {
            SystemServicesProxy.getInstance(this.mContext).getStableInsets(this.mStableInsets);
            this.mSnapAlgorithm = null;
            initializeSnapAlgorithm();
        }
        resizeStack(calculatePositionForBounds, this.mSnapAlgorithm.getMiddleTarget().position, this.mSnapAlgorithm.getMiddleTarget());
    }

    public final void onBusEvent(RecentsActivityStartingEvent recentsActivityStartingEvent) {
        if (this.mGrowRecents && getWindowManagerProxy().getDockSide() == 2 && getCurrentPosition() == getSnapAlgorithm().getLastSplitTarget().position) {
            this.mState.growAfterRecentsDrawn = true;
            startDragging(false, false);
        }
    }

    public final void onBusEvent(UndockingTaskEvent undockingTaskEvent) {
        int dockSide = this.mWindowManagerProxy.getDockSide();
        if (dockSide == -1 || this.mDockedStackMinimized) {
            return;
        }
        startDragging(false, false);
        DividerSnapAlgorithm.SnapTarget dismissEndTarget = dockSideTopLeft(dockSide) ? this.mSnapAlgorithm.getDismissEndTarget() : this.mSnapAlgorithm.getDismissStartTarget();
        this.mExitAnimationRunning = true;
        this.mExitStartPosition = getCurrentPosition();
        stopDragging(this.mExitStartPosition, dismissEndTarget, 336L, 100L, 0L, Interpolators.FAST_OUT_SLOW_IN);
    }

    public final void onBusEvent(RecentsDrawnEvent recentsDrawnEvent) {
        if (this.mState.animateAfterRecentsDrawn) {
            this.mState.animateAfterRecentsDrawn = false;
            updateDockSide();
            this.mHandler.post(new Runnable(this) { // from class: com.android.systemui.stackdivider.DividerView._void_onBusEvent_com_android_systemui_recents_events_ui_RecentsDrawnEvent_drawnEvent_LambdaImpl0
                private DividerView val$this;

                {
                    this.val$this = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.val$this.m1366com_android_systemui_stackdivider_DividerView_lambda$3();
                }
            });
        }
        if (this.mState.growAfterRecentsDrawn) {
            this.mState.growAfterRecentsDrawn = false;
            updateDockSide();
            EventBus.getDefault().send(new RecentsGrowingEvent());
            stopDragging(getCurrentPosition(), this.mSnapAlgorithm.getMiddleTarget(), 336L, Interpolators.FAST_OUT_SLOW_IN);
        }
    }

    public void onComputeInternalInsets(ViewTreeObserver.InternalInsetsInfo internalInsetsInfo) {
        internalInsetsInfo.setTouchableInsets(3);
        internalInsetsInfo.touchableRegion.set(this.mHandle.getLeft(), this.mHandle.getTop(), this.mHandle.getRight(), this.mHandle.getBottom());
        internalInsetsInfo.touchableRegion.op(this.mBackground.getLeft(), this.mBackground.getTop(), this.mBackground.getRight(), this.mBackground.getBottom(), Region.Op.UNION);
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        updateDisplayInfo();
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mHandle = (DividerHandleView) findViewById(2131886278);
        this.mBackground = findViewById(2131886276);
        this.mMinimizedShadow = (MinimizedDockShadow) findViewById(2131886277);
        this.mHandle.setOnTouchListener(this);
        this.mDividerWindowWidth = getResources().getDimensionPixelSize(17104929);
        this.mDividerInsets = getResources().getDimensionPixelSize(17104930);
        this.mDividerSize = this.mDividerWindowWidth - (this.mDividerInsets * 2);
        this.mTouchElevation = getResources().getDimensionPixelSize(2131689979);
        this.mLongPressEntraceAnimDuration = getResources().getInteger(2131755071);
        this.mGrowRecents = getResources().getBoolean(2131623956);
        this.mTouchSlop = ViewConfiguration.get(this.mContext).getScaledTouchSlop();
        this.mFlingAnimationUtils = new FlingAnimationUtils(getContext(), 0.3f);
        updateDisplayInfo();
        this.mHandle.setPointerIcon(PointerIcon.getSystemIcon(getContext(), getResources().getConfiguration().orientation == 2 ? 1014 : 1015));
        getViewTreeObserver().addOnComputeInternalInsetsListener(this);
        this.mHandle.setAccessibilityDelegate(this.mHandleDelegate);
        this.mGestureDetector = new GestureDetector(this.mContext, new GestureDetector.SimpleOnGestureListener(this) { // from class: com.android.systemui.stackdivider.DividerView.3
            final DividerView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnGestureListener
            public boolean onSingleTapUp(MotionEvent motionEvent) {
                return false;
            }
        });
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int i5;
        super.onLayout(z, i, i2, i3, i4);
        int i6 = 0;
        if (this.mDockSide == 2) {
            i5 = this.mBackground.getTop();
        } else if (this.mDockSide == 1) {
            i6 = this.mBackground.getLeft();
            i5 = 0;
        } else {
            i5 = 0;
            if (this.mDockSide == 3) {
                i6 = this.mBackground.getRight() - this.mMinimizedShadow.getWidth();
                i5 = 0;
            }
        }
        this.mMinimizedShadow.layout(i6, i5, this.mMinimizedShadow.getMeasuredWidth() + i6, this.mMinimizedShadow.getMeasuredHeight() + i5);
        if (z) {
            this.mWindowManagerProxy.setTouchRegion(new Rect(this.mHandle.getLeft(), this.mHandle.getTop(), this.mHandle.getRight(), this.mHandle.getBottom()));
        }
    }

    @Override // android.view.View.OnTouchListener
    public boolean onTouch(View view, MotionEvent motionEvent) {
        convertToScreenCoordinates(motionEvent);
        this.mGestureDetector.onTouchEvent(motionEvent);
        switch (motionEvent.getAction() & 255) {
            case 0:
                this.mVelocityTracker = VelocityTracker.obtain();
                this.mVelocityTracker.addMovement(motionEvent);
                this.mStartX = (int) motionEvent.getX();
                this.mStartY = (int) motionEvent.getY();
                boolean startDragging = startDragging(true, true);
                if (!startDragging) {
                    stopDragging();
                }
                this.mStartPosition = getCurrentPosition();
                this.mMoving = false;
                return startDragging;
            case 1:
            case 3:
                this.mVelocityTracker.addMovement(motionEvent);
                int rawX = (int) motionEvent.getRawX();
                int rawY = (int) motionEvent.getRawY();
                this.mVelocityTracker.computeCurrentVelocity(1000);
                stopDragging(calculatePosition(rawX, rawY), isHorizontalDivision() ? this.mVelocityTracker.getYVelocity() : this.mVelocityTracker.getXVelocity(), false, true);
                this.mMoving = false;
                return true;
            case 2:
                this.mVelocityTracker.addMovement(motionEvent);
                int x = (int) motionEvent.getX();
                int y = (int) motionEvent.getY();
                boolean z = (!isHorizontalDivision() || Math.abs(y - this.mStartY) <= this.mTouchSlop) ? !isHorizontalDivision() && Math.abs(x - this.mStartX) > this.mTouchSlop : true;
                if (!this.mMoving && z) {
                    this.mStartX = x;
                    this.mStartY = y;
                    this.mMoving = true;
                }
                if (!this.mMoving || this.mDockSide == -1) {
                    return true;
                }
                resizeStack(calculatePosition(x, y), this.mStartPosition, this.mSnapAlgorithm.calculateSnapTarget(this.mStartPosition, 0.0f, false));
                return true;
            default:
                return true;
        }
    }

    public void resizeStack(int i, int i2, DividerSnapAlgorithm.SnapTarget snapTarget) {
        calculateBoundsForPosition(i, this.mDockSide, this.mDockedRect);
        if (!this.mDockedRect.equals(this.mLastResizeRect) || this.mEntranceAnimationRunning) {
            if (this.mBackground.getZ() > 0.0f) {
                this.mBackground.invalidate();
            }
            this.mLastResizeRect.set(this.mDockedRect);
            if (this.mEntranceAnimationRunning && i2 != Integer.MAX_VALUE) {
                if (this.mCurrentAnimator != null) {
                    calculateBoundsForPosition(i2, this.mDockSide, this.mDockedTaskRect);
                } else {
                    calculateBoundsForPosition(isHorizontalDivision() ? this.mDisplayHeight : this.mDisplayWidth, this.mDockSide, this.mDockedTaskRect);
                }
                calculateBoundsForPosition(i2, DockedDividerUtils.invertDockSide(this.mDockSide), this.mOtherTaskRect);
                this.mWindowManagerProxy.resizeDockedStack(this.mDockedRect, this.mDockedTaskRect, null, this.mOtherTaskRect, null);
            } else if (this.mExitAnimationRunning && i2 != Integer.MAX_VALUE) {
                calculateBoundsForPosition(i2, this.mDockSide, this.mDockedTaskRect);
                calculateBoundsForPosition(this.mExitStartPosition, DockedDividerUtils.invertDockSide(this.mDockSide), this.mOtherTaskRect);
                this.mOtherInsetRect.set(this.mOtherTaskRect);
                applyExitAnimationParallax(this.mOtherTaskRect, i);
                this.mWindowManagerProxy.resizeDockedStack(this.mDockedRect, this.mDockedTaskRect, null, this.mOtherTaskRect, this.mOtherInsetRect);
            } else if (i2 != Integer.MAX_VALUE) {
                calculateBoundsForPosition(i, DockedDividerUtils.invertDockSide(this.mDockSide), this.mOtherRect);
                int invertDockSide = DockedDividerUtils.invertDockSide(this.mDockSide);
                int restrictDismissingTaskPosition = restrictDismissingTaskPosition(i2, this.mDockSide, snapTarget);
                int restrictDismissingTaskPosition2 = restrictDismissingTaskPosition(i2, invertDockSide, snapTarget);
                calculateBoundsForPosition(restrictDismissingTaskPosition, this.mDockSide, this.mDockedTaskRect);
                calculateBoundsForPosition(restrictDismissingTaskPosition2, invertDockSide, this.mOtherTaskRect);
                this.mDisplayRect.set(0, 0, this.mDisplayWidth, this.mDisplayHeight);
                alignTopLeft(this.mDockedRect, this.mDockedTaskRect);
                alignTopLeft(this.mOtherRect, this.mOtherTaskRect);
                this.mDockedInsetRect.set(this.mDockedTaskRect);
                this.mOtherInsetRect.set(this.mOtherTaskRect);
                if (dockSideTopLeft(this.mDockSide)) {
                    alignTopLeft(this.mDisplayRect, this.mDockedInsetRect);
                    alignBottomRight(this.mDisplayRect, this.mOtherInsetRect);
                } else {
                    alignBottomRight(this.mDisplayRect, this.mDockedInsetRect);
                    alignTopLeft(this.mDisplayRect, this.mOtherInsetRect);
                }
                applyDismissingParallax(this.mDockedTaskRect, this.mDockSide, snapTarget, i, restrictDismissingTaskPosition);
                applyDismissingParallax(this.mOtherTaskRect, invertDockSide, snapTarget, i, restrictDismissingTaskPosition2);
                this.mWindowManagerProxy.resizeDockedStack(this.mDockedRect, this.mDockedTaskRect, this.mDockedInsetRect, this.mOtherTaskRect, this.mOtherInsetRect);
            } else {
                this.mWindowManagerProxy.resizeDockedStack(this.mDockedRect, null, null, null, null);
            }
            DividerSnapAlgorithm.SnapTarget closestDismissTarget = this.mSnapAlgorithm.getClosestDismissTarget(i);
            float dimFraction = getDimFraction(i, closestDismissTarget);
            this.mWindowManagerProxy.setResizeDimLayer(dimFraction != 0.0f, getStackIdForDismissTarget(closestDismissTarget), dimFraction);
        }
    }

    public void setAdjustedForIme(boolean z) {
        updateDockSide();
        this.mHandle.setAlpha(z ? 0.0f : 1.0f);
        if (!z) {
            resetBackground();
        } else if (this.mDockSide == 2) {
            this.mBackground.setPivotY(0.0f);
            this.mBackground.setScaleY(0.5f);
        }
        this.mAdjustedForIme = z;
    }

    public void setAdjustedForIme(boolean z, long j) {
        updateDockSide();
        this.mHandle.animate().setInterpolator(IME_ADJUST_INTERPOLATOR).setDuration(j).alpha(z ? 0.0f : 1.0f).start();
        if (this.mDockSide == 2) {
            this.mBackground.setPivotY(0.0f);
            ViewPropertyAnimator animate = this.mBackground.animate();
            float f = 1.0f;
            if (z) {
                f = 0.5f;
            }
            animate.scaleY(f);
        }
        if (!z) {
            this.mBackground.animate().withEndAction(this.mResetBackgroundRunnable);
        }
        this.mBackground.animate().setInterpolator(IME_ADJUST_INTERPOLATOR).setDuration(j).start();
        this.mAdjustedForIme = z;
    }

    public void setMinimizedDockStack(boolean z) {
        updateDockSide();
        this.mHandle.setAlpha(z ? 0.0f : 1.0f);
        if (!z) {
            resetBackground();
        } else if (this.mDockSide == 2) {
            this.mBackground.setPivotY(0.0f);
            this.mBackground.setScaleY(0.0f);
        } else if (this.mDockSide == 1 || this.mDockSide == 3) {
            this.mBackground.setPivotX(this.mDockSide == 1 ? 0 : this.mBackground.getWidth());
            this.mBackground.setScaleX(0.0f);
        }
        this.mMinimizedShadow.setAlpha(z ? 1.0f : 0.0f);
        this.mDockedStackMinimized = z;
    }

    public void setMinimizedDockStack(boolean z, long j) {
        updateDockSide();
        this.mHandle.animate().setInterpolator(Interpolators.FAST_OUT_SLOW_IN).setDuration(j).alpha(z ? 0.0f : 1.0f).start();
        if (this.mDockSide == 2) {
            this.mBackground.setPivotY(0.0f);
            this.mBackground.animate().scaleY(z ? 0.0f : 1.0f);
        } else if (this.mDockSide == 1 || this.mDockSide == 3) {
            this.mBackground.setPivotX(this.mDockSide == 1 ? 0 : this.mBackground.getWidth());
            this.mBackground.animate().scaleX(z ? 0.0f : 1.0f);
        }
        if (!z) {
            this.mBackground.animate().withEndAction(this.mResetBackgroundRunnable);
        }
        this.mMinimizedShadow.animate().alpha(z ? 1.0f : 0.0f).setInterpolator(Interpolators.ALPHA_IN).setDuration(j).start();
        this.mBackground.animate().setInterpolator(Interpolators.FAST_OUT_SLOW_IN).setDuration(j).start();
        this.mDockedStackMinimized = z;
    }

    public boolean startDragging(boolean z, boolean z2) {
        cancelFlingAnimation();
        if (z2) {
            this.mHandle.setTouching(true, z);
        }
        this.mDockSide = this.mWindowManagerProxy.getDockSide();
        initializeSnapAlgorithm();
        this.mWindowManagerProxy.setResizing(true);
        if (z2) {
            this.mWindowManager.setSlippery(false);
            liftBackground();
        }
        EventBus.getDefault().send(new StartedDragingEvent());
        return this.mDockSide != -1;
    }

    public void stopDragging(int i, float f, boolean z, boolean z2) {
        this.mHandle.setTouching(false, true);
        fling(i, f, z, z2);
        this.mWindowManager.setSlippery(true);
        releaseBackground();
    }

    public void stopDragging(int i, DividerSnapAlgorithm.SnapTarget snapTarget, long j, long j2, long j3, Interpolator interpolator) {
        this.mHandle.setTouching(false, true);
        flingTo(i, snapTarget, j, j2, j3, interpolator);
        this.mWindowManager.setSlippery(true);
        releaseBackground();
    }

    public void stopDragging(int i, DividerSnapAlgorithm.SnapTarget snapTarget, long j, Interpolator interpolator) {
        stopDragging(i, snapTarget, j, 0L, 0L, interpolator);
    }

    public void stopDragging(int i, DividerSnapAlgorithm.SnapTarget snapTarget, long j, Interpolator interpolator, long j2) {
        stopDragging(i, snapTarget, j, 0L, j2, interpolator);
    }
}

package android.support.v4.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.support.v4.view.AbsSavedState;
import android.support.v4.view.AccessibilityDelegateCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
/* loaded from: a.zip:android/support/v4/widget/SlidingPaneLayout.class */
public class SlidingPaneLayout extends ViewGroup {
    static final SlidingPanelLayoutImpl IMPL;
    private boolean mCanSlide;
    private int mCoveredFadeColor;
    private final ViewDragHelper mDragHelper;
    private boolean mFirstLayout;
    private float mInitialMotionX;
    private float mInitialMotionY;
    private boolean mIsUnableToDrag;
    private final int mOverhangSize;
    private PanelSlideListener mPanelSlideListener;
    private int mParallaxBy;
    private float mParallaxOffset;
    private final ArrayList<DisableLayerRunnable> mPostedRunnables;
    private boolean mPreservedOpenState;
    private Drawable mShadowDrawableLeft;
    private Drawable mShadowDrawableRight;
    private float mSlideOffset;
    private int mSlideRange;
    private View mSlideableView;
    private int mSliderFadeColor;
    private final Rect mTmpRect;

    /* loaded from: a.zip:android/support/v4/widget/SlidingPaneLayout$AccessibilityDelegate.class */
    class AccessibilityDelegate extends AccessibilityDelegateCompat {
        private final Rect mTmpRect = new Rect();
        final SlidingPaneLayout this$0;

        AccessibilityDelegate(SlidingPaneLayout slidingPaneLayout) {
            this.this$0 = slidingPaneLayout;
        }

        private void copyNodeInfoNoChildren(AccessibilityNodeInfoCompat accessibilityNodeInfoCompat, AccessibilityNodeInfoCompat accessibilityNodeInfoCompat2) {
            Rect rect = this.mTmpRect;
            accessibilityNodeInfoCompat2.getBoundsInParent(rect);
            accessibilityNodeInfoCompat.setBoundsInParent(rect);
            accessibilityNodeInfoCompat2.getBoundsInScreen(rect);
            accessibilityNodeInfoCompat.setBoundsInScreen(rect);
            accessibilityNodeInfoCompat.setVisibleToUser(accessibilityNodeInfoCompat2.isVisibleToUser());
            accessibilityNodeInfoCompat.setPackageName(accessibilityNodeInfoCompat2.getPackageName());
            accessibilityNodeInfoCompat.setClassName(accessibilityNodeInfoCompat2.getClassName());
            accessibilityNodeInfoCompat.setContentDescription(accessibilityNodeInfoCompat2.getContentDescription());
            accessibilityNodeInfoCompat.setEnabled(accessibilityNodeInfoCompat2.isEnabled());
            accessibilityNodeInfoCompat.setClickable(accessibilityNodeInfoCompat2.isClickable());
            accessibilityNodeInfoCompat.setFocusable(accessibilityNodeInfoCompat2.isFocusable());
            accessibilityNodeInfoCompat.setFocused(accessibilityNodeInfoCompat2.isFocused());
            accessibilityNodeInfoCompat.setAccessibilityFocused(accessibilityNodeInfoCompat2.isAccessibilityFocused());
            accessibilityNodeInfoCompat.setSelected(accessibilityNodeInfoCompat2.isSelected());
            accessibilityNodeInfoCompat.setLongClickable(accessibilityNodeInfoCompat2.isLongClickable());
            accessibilityNodeInfoCompat.addAction(accessibilityNodeInfoCompat2.getActions());
            accessibilityNodeInfoCompat.setMovementGranularities(accessibilityNodeInfoCompat2.getMovementGranularities());
        }

        public boolean filter(View view) {
            return this.this$0.isDimmed(view);
        }

        @Override // android.support.v4.view.AccessibilityDelegateCompat
        public void onInitializeAccessibilityEvent(View view, AccessibilityEvent accessibilityEvent) {
            super.onInitializeAccessibilityEvent(view, accessibilityEvent);
            accessibilityEvent.setClassName(SlidingPaneLayout.class.getName());
        }

        @Override // android.support.v4.view.AccessibilityDelegateCompat
        public void onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
            AccessibilityNodeInfoCompat obtain = AccessibilityNodeInfoCompat.obtain(accessibilityNodeInfoCompat);
            super.onInitializeAccessibilityNodeInfo(view, obtain);
            copyNodeInfoNoChildren(accessibilityNodeInfoCompat, obtain);
            obtain.recycle();
            accessibilityNodeInfoCompat.setClassName(SlidingPaneLayout.class.getName());
            accessibilityNodeInfoCompat.setSource(view);
            ViewParent parentForAccessibility = ViewCompat.getParentForAccessibility(view);
            if (parentForAccessibility instanceof View) {
                accessibilityNodeInfoCompat.setParent((View) parentForAccessibility);
            }
            int childCount = this.this$0.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childAt = this.this$0.getChildAt(i);
                if (!filter(childAt) && childAt.getVisibility() == 0) {
                    ViewCompat.setImportantForAccessibility(childAt, 1);
                    accessibilityNodeInfoCompat.addChild(childAt);
                }
            }
        }

        @Override // android.support.v4.view.AccessibilityDelegateCompat
        public boolean onRequestSendAccessibilityEvent(ViewGroup viewGroup, View view, AccessibilityEvent accessibilityEvent) {
            if (filter(view)) {
                return false;
            }
            return super.onRequestSendAccessibilityEvent(viewGroup, view, accessibilityEvent);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:android/support/v4/widget/SlidingPaneLayout$DisableLayerRunnable.class */
    public class DisableLayerRunnable implements Runnable {
        final View mChildView;
        final SlidingPaneLayout this$0;

        DisableLayerRunnable(SlidingPaneLayout slidingPaneLayout, View view) {
            this.this$0 = slidingPaneLayout;
            this.mChildView = view;
        }

        @Override // java.lang.Runnable
        public void run() {
            if (this.mChildView.getParent() == this.this$0) {
                ViewCompat.setLayerType(this.mChildView, 0, null);
                this.this$0.invalidateChildRegion(this.mChildView);
            }
            this.this$0.mPostedRunnables.remove(this);
        }
    }

    /* loaded from: a.zip:android/support/v4/widget/SlidingPaneLayout$DragHelperCallback.class */
    private class DragHelperCallback extends ViewDragHelper.Callback {
        final SlidingPaneLayout this$0;

        private DragHelperCallback(SlidingPaneLayout slidingPaneLayout) {
            this.this$0 = slidingPaneLayout;
        }

        /* synthetic */ DragHelperCallback(SlidingPaneLayout slidingPaneLayout, DragHelperCallback dragHelperCallback) {
            this(slidingPaneLayout);
        }

        @Override // android.support.v4.widget.ViewDragHelper.Callback
        public int clampViewPositionHorizontal(View view, int i, int i2) {
            int min;
            LayoutParams layoutParams = (LayoutParams) this.this$0.mSlideableView.getLayoutParams();
            if (this.this$0.isLayoutRtlSupport()) {
                int width = this.this$0.getWidth() - ((this.this$0.getPaddingRight() + layoutParams.rightMargin) + this.this$0.mSlideableView.getWidth());
                min = Math.max(Math.min(i, width), width - this.this$0.mSlideRange);
            } else {
                int paddingLeft = this.this$0.getPaddingLeft() + layoutParams.leftMargin;
                min = Math.min(Math.max(i, paddingLeft), paddingLeft + this.this$0.mSlideRange);
            }
            return min;
        }

        @Override // android.support.v4.widget.ViewDragHelper.Callback
        public int clampViewPositionVertical(View view, int i, int i2) {
            return view.getTop();
        }

        @Override // android.support.v4.widget.ViewDragHelper.Callback
        public int getViewHorizontalDragRange(View view) {
            return this.this$0.mSlideRange;
        }

        @Override // android.support.v4.widget.ViewDragHelper.Callback
        public void onEdgeDragStarted(int i, int i2) {
            this.this$0.mDragHelper.captureChildView(this.this$0.mSlideableView, i2);
        }

        @Override // android.support.v4.widget.ViewDragHelper.Callback
        public void onViewCaptured(View view, int i) {
            this.this$0.setAllChildrenVisible();
        }

        @Override // android.support.v4.widget.ViewDragHelper.Callback
        public void onViewDragStateChanged(int i) {
            if (this.this$0.mDragHelper.getViewDragState() == 0) {
                if (this.this$0.mSlideOffset != 0.0f) {
                    this.this$0.dispatchOnPanelOpened(this.this$0.mSlideableView);
                    this.this$0.mPreservedOpenState = true;
                    return;
                }
                this.this$0.updateObscuredViewsVisibility(this.this$0.mSlideableView);
                this.this$0.dispatchOnPanelClosed(this.this$0.mSlideableView);
                this.this$0.mPreservedOpenState = false;
            }
        }

        @Override // android.support.v4.widget.ViewDragHelper.Callback
        public void onViewPositionChanged(View view, int i, int i2, int i3, int i4) {
            this.this$0.onPanelDragged(i);
            this.this$0.invalidate();
        }

        /* JADX WARN: Code restructure failed: missing block: B:19:0x00b0, code lost:
            if (r4.this$0.mSlideOffset > 0.5f) goto L20;
         */
        /* JADX WARN: Code restructure failed: missing block: B:9:0x0040, code lost:
            if (r4.this$0.mSlideOffset > 0.5f) goto L13;
         */
        @Override // android.support.v4.widget.ViewDragHelper.Callback
        /*
            Code decompiled incorrectly, please refer to instructions dump.
        */
        public void onViewReleased(View view, float f, float f2) {
            int i;
            int i2;
            LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
            if (this.this$0.isLayoutRtlSupport()) {
                int paddingRight = this.this$0.getPaddingRight() + layoutParams.rightMargin;
                if (f >= 0.0f) {
                    i2 = paddingRight;
                    if (f == 0.0f) {
                        i2 = paddingRight;
                    }
                    i = (this.this$0.getWidth() - i2) - this.this$0.mSlideableView.getWidth();
                }
                i2 = paddingRight + this.this$0.mSlideRange;
                i = (this.this$0.getWidth() - i2) - this.this$0.mSlideableView.getWidth();
            } else {
                int paddingLeft = this.this$0.getPaddingLeft() + layoutParams.leftMargin;
                if (f <= 0.0f) {
                    i = paddingLeft;
                    if (f == 0.0f) {
                        i = paddingLeft;
                    }
                }
                i = paddingLeft + this.this$0.mSlideRange;
            }
            this.this$0.mDragHelper.settleCapturedViewAt(i, view.getTop());
            this.this$0.invalidate();
        }

        @Override // android.support.v4.widget.ViewDragHelper.Callback
        public boolean tryCaptureView(View view, int i) {
            if (this.this$0.mIsUnableToDrag) {
                return false;
            }
            return ((LayoutParams) view.getLayoutParams()).slideable;
        }
    }

    /* loaded from: a.zip:android/support/v4/widget/SlidingPaneLayout$LayoutParams.class */
    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        private static final int[] ATTRS = {16843137};
        Paint dimPaint;
        boolean dimWhenOffset;
        boolean slideable;
        public float weight;

        public LayoutParams() {
            super(-1, -1);
            this.weight = 0.0f;
        }

        public LayoutParams(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            this.weight = 0.0f;
            TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, ATTRS);
            this.weight = obtainStyledAttributes.getFloat(0, 0.0f);
            obtainStyledAttributes.recycle();
        }

        public LayoutParams(ViewGroup.LayoutParams layoutParams) {
            super(layoutParams);
            this.weight = 0.0f;
        }

        public LayoutParams(ViewGroup.MarginLayoutParams marginLayoutParams) {
            super(marginLayoutParams);
            this.weight = 0.0f;
        }
    }

    /* loaded from: a.zip:android/support/v4/widget/SlidingPaneLayout$PanelSlideListener.class */
    public interface PanelSlideListener {
        void onPanelClosed(View view);

        void onPanelOpened(View view);

        void onPanelSlide(View view, float f);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:android/support/v4/widget/SlidingPaneLayout$SavedState.class */
    public static class SavedState extends AbsSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = ParcelableCompat.newCreator(new ParcelableCompatCreatorCallbacks<SavedState>() { // from class: android.support.v4.widget.SlidingPaneLayout.SavedState.1
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.support.v4.os.ParcelableCompatCreatorCallbacks
            public SavedState createFromParcel(Parcel parcel, ClassLoader classLoader) {
                return new SavedState(parcel, classLoader, null);
            }

            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.support.v4.os.ParcelableCompatCreatorCallbacks
            public SavedState[] newArray(int i) {
                return new SavedState[i];
            }
        });
        boolean isOpen;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        private SavedState(Parcel parcel, ClassLoader classLoader) {
            super(parcel, classLoader);
            boolean z = false;
            this.isOpen = parcel.readInt() != 0 ? true : z;
        }

        /* synthetic */ SavedState(Parcel parcel, ClassLoader classLoader, SavedState savedState) {
            this(parcel, classLoader);
        }

        SavedState(Parcelable parcelable) {
            super(parcelable);
        }

        @Override // android.support.v4.view.AbsSavedState, android.os.Parcelable
        public void writeToParcel(Parcel parcel, int i) {
            super.writeToParcel(parcel, i);
            parcel.writeInt(this.isOpen ? 1 : 0);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:android/support/v4/widget/SlidingPaneLayout$SlidingPanelLayoutImpl.class */
    public interface SlidingPanelLayoutImpl {
        void invalidateChildRegion(SlidingPaneLayout slidingPaneLayout, View view);
    }

    /* loaded from: a.zip:android/support/v4/widget/SlidingPaneLayout$SlidingPanelLayoutImplBase.class */
    static class SlidingPanelLayoutImplBase implements SlidingPanelLayoutImpl {
        SlidingPanelLayoutImplBase() {
        }

        @Override // android.support.v4.widget.SlidingPaneLayout.SlidingPanelLayoutImpl
        public void invalidateChildRegion(SlidingPaneLayout slidingPaneLayout, View view) {
            ViewCompat.postInvalidateOnAnimation(slidingPaneLayout, view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
        }
    }

    /* loaded from: a.zip:android/support/v4/widget/SlidingPaneLayout$SlidingPanelLayoutImplJB.class */
    static class SlidingPanelLayoutImplJB extends SlidingPanelLayoutImplBase {
        private Method mGetDisplayList;
        private Field mRecreateDisplayList;

        SlidingPanelLayoutImplJB() {
            try {
                this.mGetDisplayList = View.class.getDeclaredMethod("getDisplayList", null);
            } catch (NoSuchMethodException e) {
                Log.e("SlidingPaneLayout", "Couldn't fetch getDisplayList method; dimming won't work right.", e);
            }
            try {
                this.mRecreateDisplayList = View.class.getDeclaredField("mRecreateDisplayList");
                this.mRecreateDisplayList.setAccessible(true);
            } catch (NoSuchFieldException e2) {
                Log.e("SlidingPaneLayout", "Couldn't fetch mRecreateDisplayList field; dimming will be slow.", e2);
            }
        }

        @Override // android.support.v4.widget.SlidingPaneLayout.SlidingPanelLayoutImplBase, android.support.v4.widget.SlidingPaneLayout.SlidingPanelLayoutImpl
        public void invalidateChildRegion(SlidingPaneLayout slidingPaneLayout, View view) {
            if (this.mGetDisplayList == null || this.mRecreateDisplayList == null) {
                view.invalidate();
                return;
            }
            try {
                this.mRecreateDisplayList.setBoolean(view, true);
                this.mGetDisplayList.invoke(view, null);
            } catch (Exception e) {
                Log.e("SlidingPaneLayout", "Error refreshing display list state", e);
            }
            super.invalidateChildRegion(slidingPaneLayout, view);
        }
    }

    /* loaded from: a.zip:android/support/v4/widget/SlidingPaneLayout$SlidingPanelLayoutImplJBMR1.class */
    static class SlidingPanelLayoutImplJBMR1 extends SlidingPanelLayoutImplBase {
        SlidingPanelLayoutImplJBMR1() {
        }

        @Override // android.support.v4.widget.SlidingPaneLayout.SlidingPanelLayoutImplBase, android.support.v4.widget.SlidingPaneLayout.SlidingPanelLayoutImpl
        public void invalidateChildRegion(SlidingPaneLayout slidingPaneLayout, View view) {
            ViewCompat.setLayerPaint(view, ((LayoutParams) view.getLayoutParams()).dimPaint);
        }
    }

    static {
        int i = Build.VERSION.SDK_INT;
        if (i >= 17) {
            IMPL = new SlidingPanelLayoutImplJBMR1();
        } else if (i >= 16) {
            IMPL = new SlidingPanelLayoutImplJB();
        } else {
            IMPL = new SlidingPanelLayoutImplBase();
        }
    }

    public SlidingPaneLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public SlidingPaneLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mSliderFadeColor = -858993460;
        this.mFirstLayout = true;
        this.mTmpRect = new Rect();
        this.mPostedRunnables = new ArrayList<>();
        float f = context.getResources().getDisplayMetrics().density;
        this.mOverhangSize = (int) ((32.0f * f) + 0.5f);
        ViewConfiguration.get(context);
        setWillNotDraw(false);
        ViewCompat.setAccessibilityDelegate(this, new AccessibilityDelegate(this));
        ViewCompat.setImportantForAccessibility(this, 1);
        this.mDragHelper = ViewDragHelper.create(this, 0.5f, new DragHelperCallback(this, null));
        this.mDragHelper.setMinVelocity(400.0f * f);
    }

    private boolean closePane(View view, int i) {
        if (this.mFirstLayout || smoothSlideTo(0.0f, i)) {
            this.mPreservedOpenState = false;
            return true;
        }
        return false;
    }

    private void dimChildView(View view, float f, int i) {
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        if (f <= 0.0f || i == 0) {
            if (ViewCompat.getLayerType(view) != 0) {
                if (layoutParams.dimPaint != null) {
                    layoutParams.dimPaint.setColorFilter(null);
                }
                DisableLayerRunnable disableLayerRunnable = new DisableLayerRunnable(this, view);
                this.mPostedRunnables.add(disableLayerRunnable);
                ViewCompat.postOnAnimation(this, disableLayerRunnable);
                return;
            }
            return;
        }
        int i2 = (int) ((((-16777216) & i) >>> 24) * f);
        if (layoutParams.dimPaint == null) {
            layoutParams.dimPaint = new Paint();
        }
        layoutParams.dimPaint.setColorFilter(new PorterDuffColorFilter((i2 << 24) | (16777215 & i), PorterDuff.Mode.SRC_OVER));
        if (ViewCompat.getLayerType(view) != 2) {
            ViewCompat.setLayerType(view, 2, layoutParams.dimPaint);
        }
        invalidateChildRegion(view);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void invalidateChildRegion(View view) {
        IMPL.invalidateChildRegion(this, view);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isLayoutRtlSupport() {
        boolean z = true;
        if (ViewCompat.getLayoutDirection(this) != 1) {
            z = false;
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onPanelDragged(int i) {
        if (this.mSlideableView == null) {
            this.mSlideOffset = 0.0f;
            return;
        }
        boolean isLayoutRtlSupport = isLayoutRtlSupport();
        LayoutParams layoutParams = (LayoutParams) this.mSlideableView.getLayoutParams();
        int width = this.mSlideableView.getWidth();
        if (isLayoutRtlSupport) {
            i = (getWidth() - i) - width;
        }
        this.mSlideOffset = (i - ((isLayoutRtlSupport ? getPaddingRight() : getPaddingLeft()) + (isLayoutRtlSupport ? layoutParams.rightMargin : layoutParams.leftMargin))) / this.mSlideRange;
        if (this.mParallaxBy != 0) {
            parallaxOtherViews(this.mSlideOffset);
        }
        if (layoutParams.dimWhenOffset) {
            dimChildView(this.mSlideableView, this.mSlideOffset, this.mSliderFadeColor);
        }
        dispatchOnPanelSlide(this.mSlideableView);
    }

    private boolean openPane(View view, int i) {
        if (this.mFirstLayout || smoothSlideTo(1.0f, i)) {
            this.mPreservedOpenState = true;
            return true;
        }
        return false;
    }

    private void parallaxOtherViews(float f) {
        boolean isLayoutRtlSupport = isLayoutRtlSupport();
        LayoutParams layoutParams = (LayoutParams) this.mSlideableView.getLayoutParams();
        boolean z = layoutParams.dimWhenOffset ? (isLayoutRtlSupport ? layoutParams.rightMargin : layoutParams.leftMargin) <= 0 : false;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = getChildAt(i);
            if (childAt != this.mSlideableView) {
                this.mParallaxOffset = f;
                int i2 = ((int) ((1.0f - this.mParallaxOffset) * this.mParallaxBy)) - ((int) ((1.0f - f) * this.mParallaxBy));
                int i3 = i2;
                if (isLayoutRtlSupport) {
                    i3 = -i2;
                }
                childAt.offsetLeftAndRight(i3);
                if (z) {
                    dimChildView(childAt, isLayoutRtlSupport ? this.mParallaxOffset - 1.0f : 1.0f - this.mParallaxOffset, this.mCoveredFadeColor);
                }
            }
        }
    }

    private static boolean viewIsOpaque(View view) {
        Drawable background;
        boolean z = true;
        if (ViewCompat.isOpaque(view)) {
            return true;
        }
        if (Build.VERSION.SDK_INT < 18 && (background = view.getBackground()) != null) {
            if (background.getOpacity() != -1) {
                z = false;
            }
            return z;
        }
        return false;
    }

    @Override // android.view.ViewGroup
    protected boolean checkLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return layoutParams instanceof LayoutParams ? super.checkLayoutParams(layoutParams) : false;
    }

    public boolean closePane() {
        return closePane(this.mSlideableView, 0);
    }

    @Override // android.view.View
    public void computeScroll() {
        if (this.mDragHelper.continueSettling(true)) {
            if (this.mCanSlide) {
                ViewCompat.postInvalidateOnAnimation(this);
            } else {
                this.mDragHelper.abort();
            }
        }
    }

    void dispatchOnPanelClosed(View view) {
        if (this.mPanelSlideListener != null) {
            this.mPanelSlideListener.onPanelClosed(view);
        }
        sendAccessibilityEvent(32);
    }

    void dispatchOnPanelOpened(View view) {
        if (this.mPanelSlideListener != null) {
            this.mPanelSlideListener.onPanelOpened(view);
        }
        sendAccessibilityEvent(32);
    }

    void dispatchOnPanelSlide(View view) {
        if (this.mPanelSlideListener != null) {
            this.mPanelSlideListener.onPanelSlide(view, this.mSlideOffset);
        }
    }

    @Override // android.view.View
    public void draw(Canvas canvas) {
        int left;
        int i;
        View view = null;
        super.draw(canvas);
        Drawable drawable = isLayoutRtlSupport() ? this.mShadowDrawableRight : this.mShadowDrawableLeft;
        if (getChildCount() > 1) {
            view = getChildAt(1);
        }
        if (view == null || drawable == null) {
            return;
        }
        int top = view.getTop();
        int bottom = view.getBottom();
        int intrinsicWidth = drawable.getIntrinsicWidth();
        if (isLayoutRtlSupport()) {
            i = view.getRight();
            left = i + intrinsicWidth;
        } else {
            left = view.getLeft();
            i = left - intrinsicWidth;
        }
        drawable.setBounds(i, top, left, bottom);
        drawable.draw(canvas);
    }

    @Override // android.view.ViewGroup
    protected boolean drawChild(Canvas canvas, View view, long j) {
        boolean drawChild;
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        int save = canvas.save(2);
        if (this.mCanSlide && !layoutParams.slideable && this.mSlideableView != null) {
            canvas.getClipBounds(this.mTmpRect);
            if (isLayoutRtlSupport()) {
                this.mTmpRect.left = Math.max(this.mTmpRect.left, this.mSlideableView.getRight());
            } else {
                this.mTmpRect.right = Math.min(this.mTmpRect.right, this.mSlideableView.getLeft());
            }
            canvas.clipRect(this.mTmpRect);
        }
        if (Build.VERSION.SDK_INT >= 11) {
            drawChild = super.drawChild(canvas, view, j);
        } else if (!layoutParams.dimWhenOffset || this.mSlideOffset <= 0.0f) {
            if (view.isDrawingCacheEnabled()) {
                view.setDrawingCacheEnabled(false);
            }
            drawChild = super.drawChild(canvas, view, j);
        } else {
            if (!view.isDrawingCacheEnabled()) {
                view.setDrawingCacheEnabled(true);
            }
            Bitmap drawingCache = view.getDrawingCache();
            if (drawingCache != null) {
                canvas.drawBitmap(drawingCache, view.getLeft(), view.getTop(), layoutParams.dimPaint);
                drawChild = false;
            } else {
                Log.e("SlidingPaneLayout", "drawChild: child view " + view + " returned null drawing cache");
                drawChild = super.drawChild(canvas, view, j);
            }
        }
        canvas.restoreToCount(save);
        return drawChild;
    }

    @Override // android.view.ViewGroup
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams();
    }

    @Override // android.view.ViewGroup
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attributeSet) {
        return new LayoutParams(getContext(), attributeSet);
    }

    @Override // android.view.ViewGroup
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return layoutParams instanceof ViewGroup.MarginLayoutParams ? new LayoutParams((ViewGroup.MarginLayoutParams) layoutParams) : new LayoutParams(layoutParams);
    }

    boolean isDimmed(View view) {
        if (view == null) {
            return false;
        }
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        boolean z = false;
        if (this.mCanSlide) {
            z = false;
            if (layoutParams.dimWhenOffset) {
                z = false;
                if (this.mSlideOffset > 0.0f) {
                    z = true;
                }
            }
        }
        return z;
    }

    public boolean isOpen() {
        boolean z = true;
        if (this.mCanSlide) {
            z = this.mSlideOffset == 1.0f;
        }
        return z;
    }

    public boolean isSlideable() {
        return this.mCanSlide;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mFirstLayout = true;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mFirstLayout = true;
        int size = this.mPostedRunnables.size();
        for (int i = 0; i < size; i++) {
            this.mPostedRunnables.get(i).run();
        }
        this.mPostedRunnables.clear();
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        View childAt;
        boolean z = true;
        int actionMasked = MotionEventCompat.getActionMasked(motionEvent);
        if (!this.mCanSlide && actionMasked == 0 && getChildCount() > 1 && (childAt = getChildAt(1)) != null) {
            this.mPreservedOpenState = !this.mDragHelper.isViewUnder(childAt, (int) motionEvent.getX(), (int) motionEvent.getY());
        }
        if (!this.mCanSlide || (this.mIsUnableToDrag && actionMasked != 0)) {
            this.mDragHelper.cancel();
            return super.onInterceptTouchEvent(motionEvent);
        } else if (actionMasked == 3 || actionMasked == 1) {
            this.mDragHelper.cancel();
            return false;
        } else {
            boolean z2 = false;
            switch (actionMasked) {
                case 0:
                    this.mIsUnableToDrag = false;
                    float x = motionEvent.getX();
                    float y = motionEvent.getY();
                    this.mInitialMotionX = x;
                    this.mInitialMotionY = y;
                    z2 = false;
                    if (this.mDragHelper.isViewUnder(this.mSlideableView, (int) x, (int) y)) {
                        z2 = false;
                        if (isDimmed(this.mSlideableView)) {
                            z2 = true;
                            break;
                        }
                    }
                    break;
                case 1:
                    break;
                case 2:
                    float x2 = motionEvent.getX();
                    float y2 = motionEvent.getY();
                    float abs = Math.abs(x2 - this.mInitialMotionX);
                    float abs2 = Math.abs(y2 - this.mInitialMotionY);
                    z2 = false;
                    if (abs > this.mDragHelper.getTouchSlop()) {
                        z2 = false;
                        if (abs2 > abs) {
                            this.mDragHelper.cancel();
                            this.mIsUnableToDrag = true;
                            return false;
                        }
                    }
                    break;
                default:
                    z2 = false;
                    break;
            }
            if (!this.mDragHelper.shouldInterceptTouchEvent(motionEvent)) {
                z = z2;
            }
            return z;
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int i5;
        int i6;
        int i7;
        boolean isLayoutRtlSupport = isLayoutRtlSupport();
        if (isLayoutRtlSupport) {
            this.mDragHelper.setEdgeTrackingEnabled(2);
        } else {
            this.mDragHelper.setEdgeTrackingEnabled(1);
        }
        int i8 = i3 - i;
        int paddingRight = isLayoutRtlSupport ? getPaddingRight() : getPaddingLeft();
        int paddingLeft = isLayoutRtlSupport ? getPaddingLeft() : getPaddingRight();
        int paddingTop = getPaddingTop();
        int childCount = getChildCount();
        int i9 = paddingRight;
        if (this.mFirstLayout) {
            this.mSlideOffset = (this.mCanSlide && this.mPreservedOpenState) ? 1.0f : 0.0f;
        }
        for (int i10 = 0; i10 < childCount; i10++) {
            View childAt = getChildAt(i10);
            if (childAt.getVisibility() != 8) {
                LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
                int measuredWidth = childAt.getMeasuredWidth();
                if (layoutParams.slideable) {
                    int min = (Math.min(paddingRight, (i8 - paddingLeft) - this.mOverhangSize) - i9) - (layoutParams.leftMargin + layoutParams.rightMargin);
                    this.mSlideRange = min;
                    int i11 = isLayoutRtlSupport ? layoutParams.rightMargin : layoutParams.leftMargin;
                    layoutParams.dimWhenOffset = ((i9 + i11) + min) + (measuredWidth / 2) > i8 - paddingLeft;
                    int i12 = (int) (min * this.mSlideOffset);
                    i9 += i12 + i11;
                    this.mSlideOffset = i12 / this.mSlideRange;
                    i5 = 0;
                } else if (!this.mCanSlide || this.mParallaxBy == 0) {
                    i9 = paddingRight;
                    i5 = 0;
                } else {
                    i5 = (int) ((1.0f - this.mSlideOffset) * this.mParallaxBy);
                    i9 = paddingRight;
                }
                if (isLayoutRtlSupport) {
                    i7 = (i8 - i9) + i5;
                    i6 = i7 - measuredWidth;
                } else {
                    i6 = i9 - i5;
                    i7 = i6 + measuredWidth;
                }
                childAt.layout(i6, paddingTop, i7, paddingTop + childAt.getMeasuredHeight());
                paddingRight += childAt.getWidth();
            }
        }
        if (this.mFirstLayout) {
            if (this.mCanSlide) {
                if (this.mParallaxBy != 0) {
                    parallaxOtherViews(this.mSlideOffset);
                }
                if (((LayoutParams) this.mSlideableView.getLayoutParams()).dimWhenOffset) {
                    dimChildView(this.mSlideableView, this.mSlideOffset, this.mSliderFadeColor);
                }
            } else {
                for (int i13 = 0; i13 < childCount; i13++) {
                    dimChildView(getChildAt(i13), 0.0f, this.mSliderFadeColor);
                }
            }
            updateObscuredViewsVisibility(this.mSlideableView);
        }
        this.mFirstLayout = false;
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        int i3;
        int i4;
        int i5;
        boolean z;
        int i6;
        int i7;
        int mode = View.MeasureSpec.getMode(i);
        int size = View.MeasureSpec.getSize(i);
        int mode2 = View.MeasureSpec.getMode(i2);
        int size2 = View.MeasureSpec.getSize(i2);
        if (mode == 1073741824) {
            i3 = mode2;
            i4 = size2;
            i5 = size;
            if (mode2 == 0) {
                if (!isInEditMode()) {
                    throw new IllegalStateException("Height must not be UNSPECIFIED");
                }
                i3 = mode2;
                i4 = size2;
                i5 = size;
                if (mode2 == 0) {
                    i3 = Integer.MIN_VALUE;
                    i4 = 300;
                    i5 = size;
                }
            }
        } else if (!isInEditMode()) {
            throw new IllegalStateException("Width must have an exact value or MATCH_PARENT");
        } else {
            if (mode == Integer.MIN_VALUE) {
                i5 = size;
                i4 = size2;
                i3 = mode2;
            } else {
                i3 = mode2;
                i4 = size2;
                i5 = size;
                if (mode == 0) {
                    i5 = 300;
                    i3 = mode2;
                    i4 = size2;
                }
            }
        }
        int i8 = 0;
        int i9 = -1;
        switch (i3) {
            case Integer.MIN_VALUE:
                i9 = (i4 - getPaddingTop()) - getPaddingBottom();
                break;
            case 1073741824:
                i9 = (i4 - getPaddingTop()) - getPaddingBottom();
                i8 = i9;
                break;
        }
        float f = 0.0f;
        boolean z2 = false;
        int paddingLeft = (i5 - getPaddingLeft()) - getPaddingRight();
        int i10 = paddingLeft;
        int childCount = getChildCount();
        if (childCount > 2) {
            Log.e("SlidingPaneLayout", "onMeasure: More than two child views are not supported.");
        }
        this.mSlideableView = null;
        int i11 = 0;
        while (i11 < childCount) {
            View childAt = getChildAt(i11);
            LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
            if (childAt.getVisibility() == 8) {
                layoutParams.dimWhenOffset = false;
                i7 = i10;
                i6 = i8;
                z = z2;
            } else {
                float f2 = f;
                if (layoutParams.weight > 0.0f) {
                    f2 = f + layoutParams.weight;
                    z = z2;
                    i6 = i8;
                    f = f2;
                    i7 = i10;
                    if (layoutParams.width == 0) {
                    }
                }
                int i12 = layoutParams.leftMargin + layoutParams.rightMargin;
                childAt.measure(layoutParams.width == -2 ? View.MeasureSpec.makeMeasureSpec(paddingLeft - i12, Integer.MIN_VALUE) : layoutParams.width == -1 ? View.MeasureSpec.makeMeasureSpec(paddingLeft - i12, 1073741824) : View.MeasureSpec.makeMeasureSpec(layoutParams.width, 1073741824), layoutParams.height == -2 ? View.MeasureSpec.makeMeasureSpec(i9, Integer.MIN_VALUE) : layoutParams.height == -1 ? View.MeasureSpec.makeMeasureSpec(i9, 1073741824) : View.MeasureSpec.makeMeasureSpec(layoutParams.height, 1073741824));
                int measuredWidth = childAt.getMeasuredWidth();
                int measuredHeight = childAt.getMeasuredHeight();
                int i13 = i8;
                if (i3 == Integer.MIN_VALUE) {
                    i13 = i8;
                    if (measuredHeight > i8) {
                        i13 = Math.min(measuredHeight, i9);
                    }
                }
                int i14 = i10 - measuredWidth;
                boolean z3 = i14 < 0;
                layoutParams.slideable = z3;
                boolean z4 = z2 | z3;
                z = z4;
                i6 = i13;
                f = f2;
                i7 = i14;
                if (layoutParams.slideable) {
                    this.mSlideableView = childAt;
                    z = z4;
                    i6 = i13;
                    f = f2;
                    i7 = i14;
                }
            }
            i11++;
            z2 = z;
            i8 = i6;
            i10 = i7;
        }
        if (z2 || f > 0.0f) {
            int i15 = paddingLeft - this.mOverhangSize;
            for (int i16 = 0; i16 < childCount; i16++) {
                View childAt2 = getChildAt(i16);
                if (childAt2.getVisibility() != 8) {
                    LayoutParams layoutParams2 = (LayoutParams) childAt2.getLayoutParams();
                    if (childAt2.getVisibility() != 8) {
                        boolean z5 = layoutParams2.width == 0 && layoutParams2.weight > 0.0f;
                        int measuredWidth2 = z5 ? 0 : childAt2.getMeasuredWidth();
                        if (!z2 || childAt2 == this.mSlideableView) {
                            if (layoutParams2.weight > 0.0f) {
                                int makeMeasureSpec = layoutParams2.width == 0 ? layoutParams2.height == -2 ? View.MeasureSpec.makeMeasureSpec(i9, Integer.MIN_VALUE) : layoutParams2.height == -1 ? View.MeasureSpec.makeMeasureSpec(i9, 1073741824) : View.MeasureSpec.makeMeasureSpec(layoutParams2.height, 1073741824) : View.MeasureSpec.makeMeasureSpec(childAt2.getMeasuredHeight(), 1073741824);
                                if (z2) {
                                    int i17 = paddingLeft - (layoutParams2.leftMargin + layoutParams2.rightMargin);
                                    int makeMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(i17, 1073741824);
                                    if (measuredWidth2 != i17) {
                                        childAt2.measure(makeMeasureSpec2, makeMeasureSpec);
                                    }
                                } else {
                                    childAt2.measure(View.MeasureSpec.makeMeasureSpec(measuredWidth2 + ((int) ((layoutParams2.weight * Math.max(0, i10)) / f)), 1073741824), makeMeasureSpec);
                                }
                            }
                        } else if (layoutParams2.width < 0 && (measuredWidth2 > i15 || layoutParams2.weight > 0.0f)) {
                            childAt2.measure(View.MeasureSpec.makeMeasureSpec(i15, 1073741824), z5 ? layoutParams2.height == -2 ? View.MeasureSpec.makeMeasureSpec(i9, Integer.MIN_VALUE) : layoutParams2.height == -1 ? View.MeasureSpec.makeMeasureSpec(i9, 1073741824) : View.MeasureSpec.makeMeasureSpec(layoutParams2.height, 1073741824) : View.MeasureSpec.makeMeasureSpec(childAt2.getMeasuredHeight(), 1073741824));
                        }
                    }
                }
            }
        }
        setMeasuredDimension(i5, getPaddingTop() + i8 + getPaddingBottom());
        this.mCanSlide = z2;
        if (this.mDragHelper.getViewDragState() == 0 || z2) {
            return;
        }
        this.mDragHelper.abort();
    }

    @Override // android.view.View
    protected void onRestoreInstanceState(Parcelable parcelable) {
        if (!(parcelable instanceof SavedState)) {
            super.onRestoreInstanceState(parcelable);
            return;
        }
        SavedState savedState = (SavedState) parcelable;
        super.onRestoreInstanceState(savedState.getSuperState());
        if (savedState.isOpen) {
            openPane();
        } else {
            closePane();
        }
        this.mPreservedOpenState = savedState.isOpen;
    }

    @Override // android.view.View
    protected Parcelable onSaveInstanceState() {
        SavedState savedState = new SavedState(super.onSaveInstanceState());
        savedState.isOpen = isSlideable() ? isOpen() : this.mPreservedOpenState;
        return savedState;
    }

    @Override // android.view.View
    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        if (i != i3) {
            this.mFirstLayout = true;
        }
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (this.mCanSlide) {
            this.mDragHelper.processTouchEvent(motionEvent);
            switch (motionEvent.getAction() & 255) {
                case 0:
                    float x = motionEvent.getX();
                    float y = motionEvent.getY();
                    this.mInitialMotionX = x;
                    this.mInitialMotionY = y;
                    return true;
                case 1:
                    if (isDimmed(this.mSlideableView)) {
                        float x2 = motionEvent.getX();
                        float y2 = motionEvent.getY();
                        float f = x2 - this.mInitialMotionX;
                        float f2 = y2 - this.mInitialMotionY;
                        int touchSlop = this.mDragHelper.getTouchSlop();
                        if ((f * f) + (f2 * f2) >= touchSlop * touchSlop || !this.mDragHelper.isViewUnder(this.mSlideableView, (int) x2, (int) y2)) {
                            return true;
                        }
                        closePane(this.mSlideableView, 0);
                        return true;
                    }
                    return true;
                default:
                    return true;
            }
        }
        return super.onTouchEvent(motionEvent);
    }

    public boolean openPane() {
        return openPane(this.mSlideableView, 0);
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public void requestChildFocus(View view, View view2) {
        super.requestChildFocus(view, view2);
        if (isInTouchMode() || this.mCanSlide) {
            return;
        }
        this.mPreservedOpenState = view == this.mSlideableView;
    }

    void setAllChildrenVisible() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = getChildAt(i);
            if (childAt.getVisibility() == 4) {
                childAt.setVisibility(0);
            }
        }
    }

    boolean smoothSlideTo(float f, int i) {
        int paddingLeft;
        if (this.mCanSlide) {
            boolean isLayoutRtlSupport = isLayoutRtlSupport();
            LayoutParams layoutParams = (LayoutParams) this.mSlideableView.getLayoutParams();
            if (isLayoutRtlSupport) {
                paddingLeft = (int) (getWidth() - (((getPaddingRight() + layoutParams.rightMargin) + (this.mSlideRange * f)) + this.mSlideableView.getWidth()));
            } else {
                paddingLeft = (int) (getPaddingLeft() + layoutParams.leftMargin + (this.mSlideRange * f));
            }
            if (this.mDragHelper.smoothSlideViewTo(this.mSlideableView, paddingLeft, this.mSlideableView.getTop())) {
                setAllChildrenVisible();
                ViewCompat.postInvalidateOnAnimation(this);
                return true;
            }
            return false;
        }
        return false;
    }

    void updateObscuredViewsVisibility(View view) {
        int i;
        int i2;
        int i3;
        int i4;
        View childAt;
        boolean isLayoutRtlSupport = isLayoutRtlSupport();
        int width = isLayoutRtlSupport ? getWidth() - getPaddingRight() : getPaddingLeft();
        int paddingLeft = isLayoutRtlSupport ? getPaddingLeft() : getWidth() - getPaddingRight();
        int paddingTop = getPaddingTop();
        int height = getHeight();
        int paddingBottom = getPaddingBottom();
        if (view == null || !viewIsOpaque(view)) {
            i = 0;
            i2 = 0;
            i3 = 0;
            i4 = 0;
        } else {
            i4 = view.getLeft();
            i3 = view.getRight();
            i2 = view.getTop();
            i = view.getBottom();
        }
        int childCount = getChildCount();
        for (int i5 = 0; i5 < childCount && (childAt = getChildAt(i5)) != view; i5++) {
            childAt.setVisibility((Math.max(isLayoutRtlSupport ? paddingLeft : width, childAt.getLeft()) < i4 || Math.max(paddingTop, childAt.getTop()) < i2 || Math.min(isLayoutRtlSupport ? width : paddingLeft, childAt.getRight()) > i3 || Math.min(height - paddingBottom, childAt.getBottom()) > i) ? 0 : 4);
        }
    }
}

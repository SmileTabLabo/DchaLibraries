package com.android.systemui.statusbar.stack;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TimeAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.FloatProperty;
import android.util.Log;
import android.util.Pair;
import android.util.Property;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.AnimationUtils;
import android.widget.OverScroller;
import android.widget.ScrollView;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.ExpandHelper;
import com.android.systemui.Interpolators;
import com.android.systemui.SwipeHelper;
import com.android.systemui.classifier.FalsingManager;
import com.android.systemui.statusbar.ActivatableNotificationView;
import com.android.systemui.statusbar.DismissView;
import com.android.systemui.statusbar.EmptyShadeView;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.ExpandableView;
import com.android.systemui.statusbar.NotificationGuts;
import com.android.systemui.statusbar.NotificationOverflowContainer;
import com.android.systemui.statusbar.NotificationSettingsIconRow;
import com.android.systemui.statusbar.StackScrollerDecorView;
import com.android.systemui.statusbar.notification.NotificationUtils;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import com.android.systemui.statusbar.phone.ScrimController;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.policy.ScrollAdapter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
/* loaded from: a.zip:com/android/systemui/statusbar/stack/NotificationStackScrollLayout.class */
public class NotificationStackScrollLayout extends ViewGroup implements SwipeHelper.Callback, ExpandHelper.Callback, ScrollAdapter, ExpandableView.OnHeightChangedListener, NotificationGroupManager.OnGroupChangeListener, NotificationSettingsIconRow.SettingsIconRowListener, ScrollContainer {
    private static final Property<NotificationStackScrollLayout, Float> BACKGROUND_FADE = new FloatProperty<NotificationStackScrollLayout>("backgroundFade") { // from class: com.android.systemui.statusbar.stack.NotificationStackScrollLayout.7
        @Override // android.util.Property
        public Float get(NotificationStackScrollLayout notificationStackScrollLayout) {
            return Float.valueOf(notificationStackScrollLayout.getBackgroundFadeAmount());
        }

        @Override // android.util.FloatProperty
        public void setValue(NotificationStackScrollLayout notificationStackScrollLayout, float f) {
            notificationStackScrollLayout.setBackgroundFadeAmount(f);
        }
    };
    private boolean mActivateNeedsAnimation;
    private int mActivePointerId;
    private ArrayList<View> mAddedHeadsUpChildren;
    private AmbientState mAmbientState;
    private boolean mAnimateNextBackgroundBottom;
    private boolean mAnimateNextBackgroundTop;
    private ArrayList<AnimationEvent> mAnimationEvents;
    private HashSet<Runnable> mAnimationFinishedRunnables;
    private boolean mAnimationRunning;
    private boolean mAnimationsEnabled;
    private Rect mBackgroundBounds;
    private float mBackgroundFadeAmount;
    private final Paint mBackgroundPaint;
    private ViewTreeObserver.OnPreDrawListener mBackgroundUpdater;
    private int mBgColor;
    private ObjectAnimator mBottomAnimator;
    private int mBottomInset;
    private int mBottomStackPeekSize;
    private int mBottomStackSlowDownHeight;
    private boolean mChangePositionInProgress;
    private boolean mChildTransferInProgress;
    private ArrayList<View> mChildrenChangingPositions;
    private HashSet<View> mChildrenToAddAnimated;
    private ArrayList<View> mChildrenToRemoveAnimated;
    private boolean mChildrenUpdateRequested;
    private ViewTreeObserver.OnPreDrawListener mChildrenUpdater;
    private HashSet<View> mClearOverlayViewsWhenFinished;
    private int mCollapsedSize;
    private int mContentHeight;
    private boolean mContinuousShadowUpdate;
    private NotificationSettingsIconRow mCurrIconRow;
    private Rect mCurrentBounds;
    private int mCurrentStackHeight;
    private StackScrollState mCurrentStackScrollState;
    private int mDarkAnimationOriginIndex;
    private boolean mDarkNeedsAnimation;
    private float mDimAmount;
    private ValueAnimator mDimAnimator;
    private Animator.AnimatorListener mDimEndListener;
    private ValueAnimator.AnimatorUpdateListener mDimUpdateListener;
    private boolean mDimmedNeedsAnimation;
    private boolean mDisallowDismissInThisMotion;
    private boolean mDisallowScrollingInThisMotion;
    private boolean mDismissAllInProgress;
    private DismissView mDismissView;
    private boolean mDontClampNextScroll;
    private boolean mDontReportNextOverScroll;
    private int mDownX;
    private ArrayList<View> mDragAnimPendingChildren;
    private boolean mDrawBackgroundAsSrc;
    private EmptyShadeView mEmptyShadeView;
    private Rect mEndAnimationRect;
    private boolean mEverythingNeedsAnimation;
    private ExpandHelper mExpandHelper;
    private View mExpandedGroupView;
    private boolean mExpandedInThisMotion;
    private boolean mExpandingNotification;
    private boolean mFadingOut;
    private FalsingManager mFalsingManager;
    private Runnable mFinishScrollingCallback;
    private ActivatableNotificationView mFirstVisibleBackgroundChild;
    private boolean mForceNoOverlappingRendering;
    private View mForcedScroll;
    private HashSet<View> mFromMoreCardAdditions;
    private View mGearExposedView;
    private boolean mGenerateChildOrderChangedEvent;
    private long mGoToFullShadeDelay;
    private boolean mGoToFullShadeNeedsAnimation;
    private boolean mGroupExpandedForMeasure;
    private NotificationGroupManager mGroupManager;
    private HashSet<Pair<ExpandableNotificationRow, Boolean>> mHeadsUpChangeAnimations;
    private HeadsUpManager mHeadsUpManager;
    private boolean mHideSensitiveNeedsAnimation;
    private int mIncreasedPaddingBetweenElements;
    private float mInitialTouchX;
    private float mInitialTouchY;
    private int mIntrinsicPadding;
    private boolean mIsBeingDragged;
    private boolean mIsExpanded;
    private boolean mIsExpansionChanging;
    private int mLastMotionY;
    private float mLastSetStackHeight;
    private ActivatableNotificationView mLastVisibleBackgroundChild;
    private OnChildLocationsChangedListener mListener;
    private SwipeHelper.LongPressListener mLongPressListener;
    private int mMaxLayoutHeight;
    private float mMaxOverScroll;
    private int mMaxScrollAfterExpand;
    private int mMaximumVelocity;
    private float mMinTopOverScrollToEscape;
    private int mMinimumVelocity;
    private boolean mNeedViewResizeAnimation;
    private boolean mNeedsAnimation;
    private OnEmptySpaceClickListener mOnEmptySpaceClickListener;
    private ExpandableView.OnHeightChangedListener mOnHeightChangedListener;
    private boolean mOnlyScrollingInThisMotion;
    private float mOverScrolledBottomPixels;
    private float mOverScrolledTopPixels;
    private int mOverflingDistance;
    private NotificationOverflowContainer mOverflowContainer;
    private OnOverscrollTopChangedListener mOverscrollTopChangedListener;
    private int mOwnScrollY;
    private int mPaddingBetweenElements;
    private boolean mPanelTracking;
    private boolean mParentFadingOut;
    private PhoneStatusBar mPhoneStatusBar;
    private boolean mPulsing;
    protected ViewGroup mQsContainer;
    private Runnable mReclamp;
    private boolean mRequestViewResizeAnimationOnLayout;
    private ScrimController mScrimController;
    private boolean mScrollable;
    private boolean mScrolledToTopOnFirstDown;
    private OverScroller mScroller;
    private boolean mScrollingEnabled;
    private ViewTreeObserver.OnPreDrawListener mShadowUpdater;
    private ArrayList<View> mSnappedBackChildren;
    private PorterDuffXfermode mSrcMode;
    private final StackScrollAlgorithm mStackScrollAlgorithm;
    private float mStackTranslation;
    private Rect mStartAnimationRect;
    private final StackStateAnimator mStateAnimator;
    private NotificationSwipeHelper mSwipeHelper;
    private ArrayList<View> mSwipedOutViews;
    private boolean mSwipingInProgress;
    private int[] mTempInt2;
    private final ArrayList<Pair<ExpandableNotificationRow, Boolean>> mTmpList;
    private ArrayList<ExpandableView> mTmpSortedChildren;
    private ObjectAnimator mTopAnimator;
    private int mTopPadding;
    private boolean mTopPaddingNeedsAnimation;
    private float mTopPaddingOverflow;
    private boolean mTouchIsClick;
    private int mTouchSlop;
    private boolean mTrackingHeadsUp;
    private View mTranslatingParentView;
    private VelocityTracker mVelocityTracker;
    private Comparator<ExpandableView> mViewPositionComparator;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:com/android/systemui/statusbar/stack/NotificationStackScrollLayout$AnimationEvent.class */
    public static class AnimationEvent {
        static AnimationFilter[] FILTERS = {new AnimationFilter().animateShadowAlpha().animateHeight().animateTopInset().animateY().animateZ().hasDelays(), new AnimationFilter().animateShadowAlpha().animateHeight().animateTopInset().animateY().animateZ().hasDelays(), new AnimationFilter().animateShadowAlpha().animateHeight().animateTopInset().animateY().animateZ().hasDelays(), new AnimationFilter().animateShadowAlpha().animateHeight().animateTopInset().animateY().animateDimmed().animateZ(), new AnimationFilter().animateShadowAlpha(), new AnimationFilter().animateShadowAlpha().animateHeight(), new AnimationFilter().animateZ(), new AnimationFilter().animateDimmed(), new AnimationFilter().animateAlpha().animateShadowAlpha().animateHeight().animateTopInset().animateY().animateZ(), new AnimationFilter().animateDark().hasDelays(), new AnimationFilter().animateShadowAlpha().animateHeight().animateTopInset().animateY().animateDimmed().animateZ().hasDelays(), new AnimationFilter().animateHideSensitive(), new AnimationFilter().animateShadowAlpha().animateHeight().animateTopInset().animateY().animateZ(), new AnimationFilter().animateAlpha().animateShadowAlpha().animateHeight().animateTopInset().animateY().animateZ(), new AnimationFilter().animateShadowAlpha().animateHeight().animateTopInset().animateY().animateZ(), new AnimationFilter().animateShadowAlpha().animateHeight().animateTopInset().animateY().animateZ(), new AnimationFilter().animateShadowAlpha().animateHeight().animateTopInset().animateY().animateZ().hasDelays(), new AnimationFilter().animateShadowAlpha().animateHeight().animateTopInset().animateY().animateZ(), new AnimationFilter().animateAlpha().animateShadowAlpha().animateDark().animateDimmed().animateHideSensitive().animateHeight().animateTopInset().animateY().animateZ()};
        static int[] LENGTHS = {464, 464, 360, 360, 360, 360, 220, 220, 360, 360, 448, 360, 360, 360, 650, 230, 230, 360, 360};
        final int animationType;
        final View changingView;
        int darkAnimationOriginIndex;
        final long eventStartTime;
        final AnimationFilter filter;
        boolean headsUpFromBottom;
        final long length;
        View viewAfterChangingView;

        AnimationEvent(View view, int i) {
            this(view, i, LENGTHS[i]);
        }

        AnimationEvent(View view, int i, long j) {
            this.eventStartTime = AnimationUtils.currentAnimationTimeMillis();
            this.changingView = view;
            this.animationType = i;
            this.filter = FILTERS[i];
            this.length = j;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public static long combineLength(ArrayList<AnimationEvent> arrayList) {
            long j = 0;
            int size = arrayList.size();
            for (int i = 0; i < size; i++) {
                AnimationEvent animationEvent = arrayList.get(i);
                j = Math.max(j, animationEvent.length);
                if (animationEvent.animationType == 10) {
                    return animationEvent.length;
                }
            }
            return j;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/statusbar/stack/NotificationStackScrollLayout$NotificationSwipeHelper.class */
    public class NotificationSwipeHelper extends SwipeHelper {
        private CheckForDrag mCheckForDrag;
        private Runnable mFalsingCheck;
        private boolean mGearSnappedOnLeft;
        private boolean mGearSnappedTo;
        private Handler mHandler;
        final NotificationStackScrollLayout this$0;

        /* JADX INFO: Access modifiers changed from: private */
        /* loaded from: a.zip:com/android/systemui/statusbar/stack/NotificationStackScrollLayout$NotificationSwipeHelper$CheckForDrag.class */
        public final class CheckForDrag implements Runnable {
            final NotificationSwipeHelper this$1;

            private CheckForDrag(NotificationSwipeHelper notificationSwipeHelper) {
                this.this$1 = notificationSwipeHelper;
            }

            /* synthetic */ CheckForDrag(NotificationSwipeHelper notificationSwipeHelper, CheckForDrag checkForDrag) {
                this(notificationSwipeHelper);
            }

            @Override // java.lang.Runnable
            public void run() {
                if (this.this$1.this$0.mTranslatingParentView == null) {
                    return;
                }
                float translation = this.this$1.getTranslation(this.this$1.this$0.mTranslatingParentView);
                float abs = Math.abs(translation);
                float spaceForGear = this.this$1.getSpaceForGear(this.this$1.this$0.mTranslatingParentView);
                float size = this.this$1.getSize(this.this$1.this$0.mTranslatingParentView) * 0.4f;
                if (this.this$1.this$0.mCurrIconRow != null) {
                    if ((!this.this$1.this$0.mCurrIconRow.isVisible() || this.this$1.this$0.mCurrIconRow.isIconLocationChange(translation)) && abs >= spaceForGear * 0.4d && abs < size) {
                        this.this$1.this$0.mCurrIconRow.fadeInSettings(translation > 0.0f, translation, size);
                    }
                }
            }
        }

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public NotificationSwipeHelper(NotificationStackScrollLayout notificationStackScrollLayout, int i, SwipeHelper.Callback callback, Context context) {
            super(i, callback, context);
            this.this$0 = notificationStackScrollLayout;
            this.mHandler = new Handler();
            this.mFalsingCheck = new Runnable(this) { // from class: com.android.systemui.statusbar.stack.NotificationStackScrollLayout.NotificationSwipeHelper.1
                final NotificationSwipeHelper this$1;

                {
                    this.this$1 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$1.resetExposedGearView(true, true);
                }
            };
        }

        private void cancelCheckForDrag() {
            if (this.this$0.mCurrIconRow != null) {
                this.this$0.mCurrIconRow.cancelFadeAnimator();
            }
            this.mHandler.removeCallbacks(this.mCheckForDrag);
        }

        private void checkForDrag() {
            if (this.mCheckForDrag == null || !this.mHandler.hasCallbacks(this.mCheckForDrag)) {
                this.mCheckForDrag = new CheckForDrag(this, null);
                this.mHandler.postDelayed(this.mCheckForDrag, 60L);
            }
        }

        private void dismissOrSnapBack(View view, float f, MotionEvent motionEvent) {
            if (isDismissGesture(motionEvent)) {
                dismissChild(view, f, !swipedFastEnough());
            } else {
                snapChild(view, 0.0f, f);
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public float getSpaceForGear(View view) {
            if (view instanceof ExpandableNotificationRow) {
                return ((ExpandableNotificationRow) view).getSpaceForGear();
            }
            return 0.0f;
        }

        private void handleGearCoveredOrDismissed() {
            cancelCheckForDrag();
            setSnappedToGear(false);
            if (this.this$0.mGearExposedView == null || this.this$0.mGearExposedView != this.this$0.mTranslatingParentView) {
                return;
            }
            this.this$0.mGearExposedView = null;
        }

        /* JADX WARN: Code restructure failed: missing block: B:18:0x0032, code lost:
            if (r4 < 0.0f) goto L18;
         */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
        */
        private boolean isTowardsGear(float f, boolean z) {
            boolean z2;
            if (this.this$0.mCurrIconRow == null) {
                return false;
            }
            if (!this.this$0.mCurrIconRow.isVisible()) {
                z2 = false;
            } else if (!z || f > 0.0f) {
                if (!z) {
                    z2 = true;
                }
                z2 = false;
            } else {
                z2 = true;
            }
            return z2;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void setSnappedToGear(boolean z) {
            this.mGearSnappedOnLeft = this.this$0.mCurrIconRow != null ? this.this$0.mCurrIconRow.isIconOnLeft() : false;
            boolean z2 = false;
            if (z) {
                z2 = false;
                if (this.this$0.mCurrIconRow != null) {
                    z2 = true;
                }
            }
            this.mGearSnappedTo = z2;
        }

        private void snapToGear(View view, float f) {
            float spaceForGear = getSpaceForGear(view);
            if (!this.this$0.mCurrIconRow.isIconOnLeft()) {
                spaceForGear = -spaceForGear;
            }
            this.this$0.mGearExposedView = this.this$0.mTranslatingParentView;
            if (view instanceof ExpandableNotificationRow) {
                MetricsLogger.action(this.this$0.mContext, 332, ((ExpandableNotificationRow) view).getStatusBarNotification().getPackageName());
            }
            if (this.this$0.mCurrIconRow != null) {
                this.this$0.mCurrIconRow.setSnapping(true);
                setSnappedToGear(true);
            }
            this.this$0.onDragCancelled(view);
            if (this.this$0.mPhoneStatusBar.getBarState() == 1) {
                this.mHandler.removeCallbacks(this.mFalsingCheck);
                this.mHandler.postDelayed(this.mFalsingCheck, 4000L);
            }
            super.snapChild(view, spaceForGear, f);
        }

        private boolean swipedEnoughToShowGear(View view) {
            boolean z = true;
            if (this.this$0.mTranslatingParentView == null) {
                return false;
            }
            float spaceForGear = getSpaceForGear(view) * (this.this$0.canChildBeDismissed(view) ? 0.4f : 0.2f);
            float translation = getTranslation(view);
            if (translation > 0.0f) {
            }
            Math.abs(translation);
            float size = getSize(this.this$0.mTranslatingParentView) * 0.4f;
            if (!this.this$0.mCurrIconRow.isVisible()) {
                z = false;
            } else if (this.this$0.mCurrIconRow.isIconOnLeft()) {
                if (translation <= spaceForGear || translation > size) {
                    z = false;
                }
            } else if (translation >= (-spaceForGear) || translation < (-size)) {
                z = false;
            }
            return z;
        }

        /* JADX WARN: Multi-variable type inference failed */
        /* JADX WARN: Type inference failed for: r0v25, types: [android.view.View] */
        public void closeControlsIfOutsideTouch(MotionEvent motionEvent) {
            int i;
            NotificationGuts notificationGuts;
            NotificationGuts exposedGuts = this.this$0.mPhoneStatusBar.getExposedGuts();
            if (exposedGuts != null) {
                notificationGuts = exposedGuts;
                i = exposedGuts.getActualHeight();
            } else {
                i = 0;
                notificationGuts = null;
                if (this.this$0.mCurrIconRow != null) {
                    i = 0;
                    notificationGuts = null;
                    if (this.this$0.mCurrIconRow.isVisible()) {
                        i = 0;
                        notificationGuts = null;
                        if (this.this$0.mTranslatingParentView != null) {
                            notificationGuts = this.this$0.mTranslatingParentView;
                            i = ((ExpandableView) this.this$0.mTranslatingParentView).getActualHeight();
                        }
                    }
                }
            }
            if (notificationGuts != null) {
                int rawX = (int) motionEvent.getRawX();
                int rawY = (int) motionEvent.getRawY();
                this.this$0.getLocationOnScreen(this.this$0.mTempInt2);
                int[] iArr = new int[2];
                notificationGuts.getLocationOnScreen(iArr);
                int i2 = iArr[0] - this.this$0.mTempInt2[0];
                int i3 = iArr[1] - this.this$0.mTempInt2[1];
                if (new Rect(i2, i3, notificationGuts.getWidth() + i2, i3 + i).contains(rawX, rawY)) {
                    return;
                }
                this.this$0.mPhoneStatusBar.dismissPopups(-1, -1, true, true);
            }
        }

        @Override // com.android.systemui.SwipeHelper
        public void dismissChild(View view, float f, boolean z) {
            super.dismissChild(view, f, z);
            if (this.this$0.mIsExpanded) {
                this.this$0.handleChildDismissed(view);
            }
            handleGearCoveredOrDismissed();
        }

        @Override // com.android.systemui.SwipeHelper
        public float getTranslation(View view) {
            return ((ExpandableView) view).getTranslation();
        }

        @Override // com.android.systemui.SwipeHelper
        public Animator getViewTranslationAnimator(View view, float f, ValueAnimator.AnimatorUpdateListener animatorUpdateListener) {
            return view instanceof ExpandableNotificationRow ? ((ExpandableNotificationRow) view).getTranslateViewAnimator(f, animatorUpdateListener) : super.getViewTranslationAnimator(view, f, animatorUpdateListener);
        }

        @Override // com.android.systemui.SwipeHelper
        public boolean handleUpEvent(MotionEvent motionEvent, View view, float f, float f2) {
            boolean z = false;
            if (this.this$0.mCurrIconRow == null) {
                cancelCheckForDrag();
                return false;
            }
            boolean isTowardsGear = isTowardsGear(f, this.this$0.mCurrIconRow.isIconOnLeft());
            boolean z2 = Math.abs(f) > getEscapeVelocity();
            if (!this.mGearSnappedTo || !this.this$0.mCurrIconRow.isVisible()) {
                if ((z2 || !swipedEnoughToShowGear(view)) && !isTowardsGear) {
                    dismissOrSnapBack(view, f, motionEvent);
                    return true;
                }
                snapToGear(view, f);
                return true;
            } else if (this.mGearSnappedOnLeft != this.this$0.mCurrIconRow.isIconOnLeft()) {
                if ((z2 || !swipedEnoughToShowGear(view)) && (!isTowardsGear || swipedFarEnough())) {
                    dismissOrSnapBack(view, f, motionEvent);
                    return true;
                }
                snapToGear(view, f);
                return true;
            } else {
                boolean z3 = Math.abs(getTranslation(view)) <= getSpaceForGear(view) * 0.6f;
                if (isTowardsGear || z3) {
                    snapChild(view, 0.0f, f);
                    return true;
                } else if (!isDismissGesture(motionEvent)) {
                    snapToGear(view, f);
                    return true;
                } else {
                    if (!swipedFastEnough()) {
                        z = true;
                    }
                    dismissChild(view, f, z);
                    return true;
                }
            }
        }

        @Override // com.android.systemui.SwipeHelper
        public void onDownUpdate(View view) {
            this.this$0.mTranslatingParentView = view;
            cancelCheckForDrag();
            if (this.this$0.mCurrIconRow != null) {
                this.this$0.mCurrIconRow.setSnapping(false);
            }
            this.mCheckForDrag = null;
            this.this$0.mCurrIconRow = null;
            this.mHandler.removeCallbacks(this.mFalsingCheck);
            resetExposedGearView(true, false);
            if (view instanceof ExpandableNotificationRow) {
                this.this$0.mCurrIconRow = ((ExpandableNotificationRow) view).getSettingsRow();
                this.this$0.mCurrIconRow.setGearListener(this.this$0);
            }
        }

        @Override // com.android.systemui.SwipeHelper
        public void onMoveUpdate(View view, float f, float f2) {
            this.mHandler.removeCallbacks(this.mFalsingCheck);
            if (this.this$0.mCurrIconRow != null) {
                this.this$0.mCurrIconRow.setSnapping(false);
                if (isTowardsGear(f, this.mGearSnappedTo ? this.mGearSnappedOnLeft : this.this$0.mCurrIconRow.isIconOnLeft()) ? false : this.this$0.mCurrIconRow.isIconLocationChange(f)) {
                    setSnappedToGear(false);
                    if (this.mHandler.hasCallbacks(this.mCheckForDrag)) {
                        this.this$0.mCurrIconRow.setGearAlpha(0.0f);
                        NotificationSettingsIconRow notificationSettingsIconRow = this.this$0.mCurrIconRow;
                        boolean z = false;
                        if (f > 0.0f) {
                            z = true;
                        }
                        notificationSettingsIconRow.setIconLocation(z);
                    } else {
                        this.mCheckForDrag = null;
                    }
                }
            }
            boolean areGutsExposed = view instanceof ExpandableNotificationRow ? ((ExpandableNotificationRow) view).areGutsExposed() : false;
            if (NotificationStackScrollLayout.isPinnedHeadsUp(view) || areGutsExposed) {
                return;
            }
            checkForDrag();
        }

        public void resetExposedGearView(boolean z, boolean z2) {
            if (this.this$0.mGearExposedView != null) {
                if (z2 || this.this$0.mGearExposedView != this.this$0.mTranslatingParentView) {
                    View view = this.this$0.mGearExposedView;
                    if (z) {
                        Animator viewTranslationAnimator = getViewTranslationAnimator(view, 0.0f, null);
                        if (viewTranslationAnimator != null) {
                            viewTranslationAnimator.start();
                        }
                    } else if (this.this$0.mGearExposedView instanceof ExpandableNotificationRow) {
                        ((ExpandableNotificationRow) this.this$0.mGearExposedView).resetTranslation();
                    }
                    this.this$0.mGearExposedView = null;
                    this.mGearSnappedTo = false;
                }
            }
        }

        @Override // com.android.systemui.SwipeHelper
        public void setTranslation(View view, float f) {
            ((ExpandableView) view).setTranslation(f);
        }

        @Override // com.android.systemui.SwipeHelper
        public void snapChild(View view, float f, float f2) {
            super.snapChild(view, f, f2);
            this.this$0.onDragCancelled(view);
            if (f == 0.0f) {
                handleGearCoveredOrDismissed();
            }
        }
    }

    /* loaded from: a.zip:com/android/systemui/statusbar/stack/NotificationStackScrollLayout$OnChildLocationsChangedListener.class */
    public interface OnChildLocationsChangedListener {
        void onChildLocationsChanged(NotificationStackScrollLayout notificationStackScrollLayout);
    }

    /* loaded from: a.zip:com/android/systemui/statusbar/stack/NotificationStackScrollLayout$OnEmptySpaceClickListener.class */
    public interface OnEmptySpaceClickListener {
        void onEmptySpaceClicked(float f, float f2);
    }

    /* loaded from: a.zip:com/android/systemui/statusbar/stack/NotificationStackScrollLayout$OnOverscrollTopChangedListener.class */
    public interface OnOverscrollTopChangedListener {
        void flingTopOverscroll(float f, boolean z);

        void onOverscrollTopChanged(float f, boolean z);
    }

    public NotificationStackScrollLayout(Context context) {
        this(context, null);
    }

    public NotificationStackScrollLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public NotificationStackScrollLayout(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public NotificationStackScrollLayout(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mCurrentStackHeight = Integer.MAX_VALUE;
        this.mBackgroundPaint = new Paint();
        this.mBottomInset = 0;
        this.mCurrentStackScrollState = new StackScrollState(this);
        this.mAmbientState = new AmbientState();
        this.mChildrenToAddAnimated = new HashSet<>();
        this.mAddedHeadsUpChildren = new ArrayList<>();
        this.mChildrenToRemoveAnimated = new ArrayList<>();
        this.mSnappedBackChildren = new ArrayList<>();
        this.mDragAnimPendingChildren = new ArrayList<>();
        this.mChildrenChangingPositions = new ArrayList<>();
        this.mFromMoreCardAdditions = new HashSet<>();
        this.mAnimationEvents = new ArrayList<>();
        this.mSwipedOutViews = new ArrayList<>();
        this.mStateAnimator = new StackStateAnimator(this);
        this.mIsExpanded = true;
        this.mChildrenUpdater = new ViewTreeObserver.OnPreDrawListener(this) { // from class: com.android.systemui.statusbar.stack.NotificationStackScrollLayout.1
            final NotificationStackScrollLayout this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.ViewTreeObserver.OnPreDrawListener
            public boolean onPreDraw() {
                this.this$0.updateForcedScroll();
                this.this$0.updateChildren();
                this.this$0.mChildrenUpdateRequested = false;
                this.this$0.getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
        };
        this.mTempInt2 = new int[2];
        this.mAnimationFinishedRunnables = new HashSet<>();
        this.mClearOverlayViewsWhenFinished = new HashSet<>();
        this.mHeadsUpChangeAnimations = new HashSet<>();
        this.mTmpList = new ArrayList<>();
        this.mBackgroundUpdater = new ViewTreeObserver.OnPreDrawListener(this) { // from class: com.android.systemui.statusbar.stack.NotificationStackScrollLayout.2
            final NotificationStackScrollLayout this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.ViewTreeObserver.OnPreDrawListener
            public boolean onPreDraw() {
                if (this.this$0.mNeedsAnimation || this.this$0.mChildrenUpdateRequested) {
                    return true;
                }
                this.this$0.updateBackground();
                return true;
            }
        };
        this.mBackgroundBounds = new Rect();
        this.mStartAnimationRect = new Rect();
        this.mEndAnimationRect = new Rect();
        this.mCurrentBounds = new Rect(-1, -1, -1, -1);
        this.mBottomAnimator = null;
        this.mTopAnimator = null;
        this.mFirstVisibleBackgroundChild = null;
        this.mLastVisibleBackgroundChild = null;
        this.mTmpSortedChildren = new ArrayList<>();
        this.mDimEndListener = new AnimatorListenerAdapter(this) { // from class: com.android.systemui.statusbar.stack.NotificationStackScrollLayout.3
            final NotificationStackScrollLayout this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                this.this$0.mDimAnimator = null;
            }
        };
        this.mDimUpdateListener = new ValueAnimator.AnimatorUpdateListener(this) { // from class: com.android.systemui.statusbar.stack.NotificationStackScrollLayout.4
            final NotificationStackScrollLayout this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.this$0.setDimAmount(((Float) valueAnimator.getAnimatedValue()).floatValue());
            }
        };
        this.mShadowUpdater = new ViewTreeObserver.OnPreDrawListener(this) { // from class: com.android.systemui.statusbar.stack.NotificationStackScrollLayout.5
            final NotificationStackScrollLayout this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.ViewTreeObserver.OnPreDrawListener
            public boolean onPreDraw() {
                this.this$0.updateViewShadows();
                return true;
            }
        };
        this.mViewPositionComparator = new Comparator<ExpandableView>(this) { // from class: com.android.systemui.statusbar.stack.NotificationStackScrollLayout.6
            final NotificationStackScrollLayout this$0;

            {
                this.this$0 = this;
            }

            @Override // java.util.Comparator
            public int compare(ExpandableView expandableView, ExpandableView expandableView2) {
                float translationY = expandableView.getTranslationY() + expandableView.getActualHeight();
                float translationY2 = expandableView2.getTranslationY() + expandableView2.getActualHeight();
                if (translationY < translationY2) {
                    return -1;
                }
                return translationY > translationY2 ? 1 : 0;
            }
        };
        this.mSrcMode = new PorterDuffXfermode(PorterDuff.Mode.SRC);
        this.mBackgroundFadeAmount = 1.0f;
        this.mReclamp = new Runnable(this) { // from class: com.android.systemui.statusbar.stack.NotificationStackScrollLayout.8
            final NotificationStackScrollLayout this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.mScroller.startScroll(this.this$0.mScrollX, this.this$0.mOwnScrollY, 0, this.this$0.getScrollRange() - this.this$0.mOwnScrollY);
                this.this$0.mDontReportNextOverScroll = true;
                this.this$0.mDontClampNextScroll = true;
                this.this$0.postInvalidateOnAnimation();
            }
        };
        this.mBgColor = context.getColor(2131558552);
        this.mExpandHelper = new ExpandHelper(getContext(), this, getResources().getDimensionPixelSize(2131689785), getResources().getDimensionPixelSize(2131689787));
        this.mExpandHelper.setEventSource(this);
        this.mExpandHelper.setScrollAdapter(this);
        this.mSwipeHelper = new NotificationSwipeHelper(this, 0, this, getContext());
        this.mSwipeHelper.setLongPressListener(this.mLongPressListener);
        this.mStackScrollAlgorithm = new StackScrollAlgorithm(context);
        initView(context);
        setWillNotDraw(false);
        this.mFalsingManager = FalsingManager.getInstance(context);
    }

    private void animateDimmed(boolean z) {
        if (this.mDimAnimator != null) {
            this.mDimAnimator.cancel();
        }
        float f = z ? 1.0f : 0.0f;
        if (f == this.mDimAmount) {
            return;
        }
        this.mDimAnimator = TimeAnimator.ofFloat(this.mDimAmount, f);
        this.mDimAnimator.setDuration(220L);
        this.mDimAnimator.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
        this.mDimAnimator.addListener(this.mDimEndListener);
        this.mDimAnimator.addUpdateListener(this.mDimUpdateListener);
        this.mDimAnimator.start();
    }

    private void applyCurrentBackgroundBounds() {
        if (!this.mFadingOut) {
            this.mScrimController.setExcludedBackgroundArea(this.mCurrentBounds);
        }
        invalidate();
    }

    private void applyCurrentState() {
        this.mCurrentStackScrollState.apply();
        if (this.mListener != null) {
            this.mListener.onChildLocationsChanged(this);
        }
        runAnimationFinishedRunnables();
        setAnimationRunning(false);
        updateBackground();
        updateViewShadows();
    }

    private boolean areBoundsAnimating() {
        boolean z = true;
        if (this.mBottomAnimator == null) {
            z = this.mTopAnimator != null;
        }
        return z;
    }

    private int clampPadding(int i) {
        return Math.max(i, this.mIntrinsicPadding);
    }

    private void clampScrollPosition() {
        int scrollRange = getScrollRange();
        if (scrollRange < this.mOwnScrollY) {
            this.mOwnScrollY = scrollRange;
        }
    }

    private void clearHeadsUpDisappearRunning() {
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            if (childAt instanceof ExpandableNotificationRow) {
                ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) childAt;
                expandableNotificationRow.setHeadsupDisappearRunning(false);
                if (expandableNotificationRow.isSummaryWithChildren()) {
                    for (ExpandableNotificationRow expandableNotificationRow2 : expandableNotificationRow.getNotificationChildren()) {
                        expandableNotificationRow2.setHeadsupDisappearRunning(false);
                    }
                }
            }
        }
    }

    private void clearTemporaryViews(ViewGroup viewGroup) {
        while (viewGroup != null && viewGroup.getTransientViewCount() != 0) {
            viewGroup.removeTransientView(viewGroup.getTransientView(0));
        }
        if (viewGroup != null) {
            viewGroup.getOverlay().clear();
        }
    }

    private void clearViewOverlays() {
        for (View view : this.mClearOverlayViewsWhenFinished) {
            StackStateAnimator.removeFromOverlay(view);
        }
    }

    private void customScrollTo(int i) {
        this.mOwnScrollY = i;
        updateChildren();
    }

    private void dispatchDownEventToScroller(MotionEvent motionEvent) {
        MotionEvent obtain = MotionEvent.obtain(motionEvent);
        obtain.setAction(0);
        onScrollTouch(obtain);
        obtain.recycle();
    }

    private void endDrag() {
        setIsBeingDragged(false);
        recycleVelocityTracker();
        if (getCurrentOverScrollAmount(true) > 0.0f) {
            setOverScrollAmount(0.0f, true, true);
        }
        if (getCurrentOverScrollAmount(false) > 0.0f) {
            setOverScrollAmount(0.0f, false, true);
        }
    }

    private int findDarkAnimationOriginIndex(PointF pointF) {
        if (pointF == null || pointF.y < this.mTopPadding + this.mTopPaddingOverflow) {
            return -1;
        }
        if (pointF.y > getBottomMostNotificationBottom()) {
            return -2;
        }
        ExpandableView closestChildAtRawPosition = getClosestChildAtRawPosition(pointF.x, pointF.y);
        if (closestChildAtRawPosition != null) {
            return getNotGoneIndex(closestChildAtRawPosition);
        }
        return -1;
    }

    private void fling(int i) {
        if (getChildCount() > 0) {
            int scrollRange = getScrollRange();
            float currentOverScrollAmount = getCurrentOverScrollAmount(true);
            float currentOverScrollAmount2 = getCurrentOverScrollAmount(false);
            if (i < 0 && currentOverScrollAmount > 0.0f) {
                this.mOwnScrollY -= (int) currentOverScrollAmount;
                this.mDontReportNextOverScroll = true;
                setOverScrollAmount(0.0f, true, false);
                this.mMaxOverScroll = ((Math.abs(i) / 1000.0f) * getRubberBandFactor(true) * this.mOverflingDistance) + currentOverScrollAmount;
            } else if (i <= 0 || currentOverScrollAmount2 <= 0.0f) {
                this.mMaxOverScroll = 0.0f;
            } else {
                this.mOwnScrollY = (int) (this.mOwnScrollY + currentOverScrollAmount2);
                setOverScrollAmount(0.0f, false, false);
                this.mMaxOverScroll = ((Math.abs(i) / 1000.0f) * getRubberBandFactor(false) * this.mOverflingDistance) + currentOverScrollAmount2;
            }
            int max = Math.max(0, scrollRange);
            int i2 = max;
            if (this.mExpandedInThisMotion) {
                i2 = Math.min(max, this.mMaxScrollAfterExpand);
            }
            this.mScroller.fling(this.mScrollX, this.mOwnScrollY, 1, i, 0, 0, 0, i2, 0, (!this.mExpandedInThisMotion || this.mOwnScrollY < 0) ? 1073741823 : 0);
            postInvalidateOnAnimation();
        }
    }

    private void focusNextViewIfFocused(View view) {
        if (view instanceof ExpandableNotificationRow) {
            ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) view;
            if (expandableNotificationRow.shouldRefocusOnDismiss()) {
                View childAfterViewWhenDismissed = expandableNotificationRow.getChildAfterViewWhenDismissed();
                View view2 = childAfterViewWhenDismissed;
                if (childAfterViewWhenDismissed == null) {
                    View groupParentWhenDismissed = expandableNotificationRow.getGroupParentWhenDismissed();
                    view2 = getFirstChildBelowTranlsationY(groupParentWhenDismissed != null ? groupParentWhenDismissed.getTranslationY() : view.getTranslationY());
                }
                if (view2 != null) {
                    view2.requestAccessibilityFocus();
                }
            }
        }
    }

    private void generateActivateEvent() {
        if (this.mActivateNeedsAnimation) {
            this.mAnimationEvents.add(new AnimationEvent(null, 6));
        }
        this.mActivateNeedsAnimation = false;
    }

    private void generateAnimateEverythingEvent() {
        if (this.mEverythingNeedsAnimation) {
            this.mAnimationEvents.add(new AnimationEvent(null, 18));
        }
        this.mEverythingNeedsAnimation = false;
    }

    private void generateChildAdditionEvents() {
        for (View view : this.mChildrenToAddAnimated) {
            if (this.mFromMoreCardAdditions.contains(view)) {
                this.mAnimationEvents.add(new AnimationEvent(view, 0, 360L));
            } else {
                this.mAnimationEvents.add(new AnimationEvent(view, 0));
            }
        }
        this.mChildrenToAddAnimated.clear();
        this.mFromMoreCardAdditions.clear();
    }

    private void generateChildHierarchyEvents() {
        generateHeadsUpAnimationEvents();
        generateChildRemovalEvents();
        generateChildAdditionEvents();
        generatePositionChangeEvents();
        generateSnapBackEvents();
        generateDragEvents();
        generateTopPaddingEvent();
        generateActivateEvent();
        generateDimmedEvent();
        generateHideSensitiveEvent();
        generateDarkEvent();
        generateGoToFullShadeEvent();
        generateViewResizeEvent();
        generateGroupExpansionEvent();
        generateAnimateEverythingEvent();
        this.mNeedsAnimation = false;
    }

    private void generateChildRemovalEvents() {
        for (View view : this.mChildrenToRemoveAnimated) {
            AnimationEvent animationEvent = new AnimationEvent(view, this.mSwipedOutViews.contains(view) ? 2 : 1);
            animationEvent.viewAfterChangingView = getFirstChildBelowTranlsationY(view.getTranslationY());
            this.mAnimationEvents.add(animationEvent);
            this.mSwipedOutViews.remove(view);
        }
        this.mChildrenToRemoveAnimated.clear();
    }

    private void generateDarkEvent() {
        if (this.mDarkNeedsAnimation) {
            AnimationEvent animationEvent = new AnimationEvent(null, 9);
            animationEvent.darkAnimationOriginIndex = this.mDarkAnimationOriginIndex;
            this.mAnimationEvents.add(animationEvent);
            startBackgroundFadeIn();
        }
        this.mDarkNeedsAnimation = false;
    }

    private void generateDimmedEvent() {
        if (this.mDimmedNeedsAnimation) {
            this.mAnimationEvents.add(new AnimationEvent(null, 7));
        }
        this.mDimmedNeedsAnimation = false;
    }

    private void generateDragEvents() {
        for (View view : this.mDragAnimPendingChildren) {
            this.mAnimationEvents.add(new AnimationEvent(view, 4));
        }
        this.mDragAnimPendingChildren.clear();
    }

    private void generateGoToFullShadeEvent() {
        if (this.mGoToFullShadeNeedsAnimation) {
            this.mAnimationEvents.add(new AnimationEvent(null, 10));
        }
        this.mGoToFullShadeNeedsAnimation = false;
    }

    private void generateGroupExpansionEvent() {
        if (this.mExpandedGroupView != null) {
            this.mAnimationEvents.add(new AnimationEvent(this.mExpandedGroupView, 13));
            this.mExpandedGroupView = null;
        }
    }

    /* JADX WARN: Code restructure failed: missing block: B:21:0x0084, code lost:
        if (r12 != false) goto L33;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private void generateHeadsUpAnimationEvents() {
        boolean z;
        int i;
        Iterator<T> it = this.mHeadsUpChangeAnimations.iterator();
        while (it.hasNext()) {
            Pair pair = (Pair) it.next();
            ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) pair.first;
            boolean booleanValue = ((Boolean) pair.second).booleanValue();
            boolean z2 = expandableNotificationRow.isPinned() && !this.mIsExpanded;
            if (this.mIsExpanded || booleanValue) {
                StackViewState viewStateForView = this.mCurrentStackScrollState.getViewStateForView(expandableNotificationRow);
                if (viewStateForView != null) {
                    z = false;
                    i = 17;
                    if (booleanValue) {
                        if (!this.mAddedHeadsUpChildren.contains(expandableNotificationRow)) {
                            z = false;
                            i = 17;
                        }
                        i = (z2 || shouldHunAppearFromBottom(viewStateForView)) ? 14 : 0;
                        z = !z2;
                    }
                }
            } else {
                int i2 = expandableNotificationRow.wasJustClicked() ? 16 : 15;
                z = false;
                i = i2;
                if (expandableNotificationRow.isChildInGroup()) {
                    expandableNotificationRow.setHeadsupDisappearRunning(false);
                    z = false;
                    i = i2;
                }
            }
            AnimationEvent animationEvent = new AnimationEvent(expandableNotificationRow, i);
            animationEvent.headsUpFromBottom = z;
            this.mAnimationEvents.add(animationEvent);
        }
        this.mHeadsUpChangeAnimations.clear();
        this.mAddedHeadsUpChildren.clear();
    }

    private void generateHideSensitiveEvent() {
        if (this.mHideSensitiveNeedsAnimation) {
            this.mAnimationEvents.add(new AnimationEvent(null, 11));
        }
        this.mHideSensitiveNeedsAnimation = false;
    }

    private void generatePositionChangeEvents() {
        for (View view : this.mChildrenChangingPositions) {
            this.mAnimationEvents.add(new AnimationEvent(view, 8));
        }
        this.mChildrenChangingPositions.clear();
        if (this.mGenerateChildOrderChangedEvent) {
            this.mAnimationEvents.add(new AnimationEvent(null, 8));
            this.mGenerateChildOrderChangedEvent = false;
        }
    }

    private boolean generateRemoveAnimation(View view) {
        if (removeRemovedChildFromHeadsUpChangeAnimations(view)) {
            this.mAddedHeadsUpChildren.remove(view);
            return false;
        } else if (isClickedHeadsUp(view)) {
            this.mClearOverlayViewsWhenFinished.add(view);
            return true;
        } else if (this.mIsExpanded && this.mAnimationsEnabled && !isChildInInvisibleGroup(view)) {
            if (this.mChildrenToAddAnimated.contains(view)) {
                this.mChildrenToAddAnimated.remove(view);
                this.mFromMoreCardAdditions.remove(view);
                return false;
            }
            this.mChildrenToRemoveAnimated.add(view);
            this.mNeedsAnimation = true;
            return true;
        } else {
            return false;
        }
    }

    private void generateSnapBackEvents() {
        for (View view : this.mSnappedBackChildren) {
            this.mAnimationEvents.add(new AnimationEvent(view, 5));
        }
        this.mSnappedBackChildren.clear();
    }

    private void generateTopPaddingEvent() {
        if (this.mTopPaddingNeedsAnimation) {
            this.mAnimationEvents.add(new AnimationEvent(null, 3));
        }
        this.mTopPaddingNeedsAnimation = false;
    }

    private void generateViewResizeEvent() {
        if (this.mNeedViewResizeAnimation) {
            this.mAnimationEvents.add(new AnimationEvent(null, 12));
        }
        this.mNeedViewResizeAnimation = false;
    }

    private View getFirstChildBelowTranlsationY(float f) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = getChildAt(i);
            if (childAt.getVisibility() != 8 && childAt.getTranslationY() >= f) {
                return childAt;
            }
        }
        return null;
    }

    private ActivatableNotificationView getFirstChildWithBackground() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = getChildAt(i);
            if (childAt.getVisibility() != 8 && (childAt instanceof ActivatableNotificationView)) {
                return (ActivatableNotificationView) childAt;
            }
        }
        return null;
    }

    private int getImeInset() {
        return Math.max(0, this.mBottomInset - (getRootView().getHeight() - getHeight()));
    }

    private int getIntrinsicHeight(View view) {
        return view instanceof ExpandableView ? ((ExpandableView) view).getIntrinsicHeight() : view.getHeight();
    }

    private ActivatableNotificationView getLastChildWithBackground() {
        for (int childCount = getChildCount() - 1; childCount >= 0; childCount--) {
            View childAt = getChildAt(childCount);
            if (childAt.getVisibility() != 8 && (childAt instanceof ActivatableNotificationView)) {
                return (ActivatableNotificationView) childAt;
            }
        }
        return null;
    }

    private int getLayoutHeight() {
        return Math.min(this.mMaxLayoutHeight, this.mCurrentStackHeight);
    }

    private int getNotGoneIndex(View view) {
        int childCount = getChildCount();
        int i = 0;
        int i2 = 0;
        while (i2 < childCount) {
            View childAt = getChildAt(i2);
            if (view == childAt) {
                return i;
            }
            int i3 = i;
            if (childAt.getVisibility() != 8) {
                i3 = i + 1;
            }
            i2++;
            i = i3;
        }
        return -1;
    }

    private int getPositionInLinearLayout(View view) {
        ExpandableNotificationRow expandableNotificationRow = null;
        ExpandableNotificationRow expandableNotificationRow2 = null;
        ExpandableNotificationRow expandableNotificationRow3 = view;
        if (isChildInGroup(view)) {
            expandableNotificationRow = (ExpandableNotificationRow) view;
            expandableNotificationRow2 = expandableNotificationRow.getNotificationParent();
            expandableNotificationRow3 = expandableNotificationRow2;
        }
        int i = 0;
        float f = 0.0f;
        int i2 = 0;
        while (i2 < getChildCount()) {
            ExpandableView expandableView = (ExpandableView) getChildAt(i2);
            boolean z = expandableView.getVisibility() != 8;
            int i3 = i;
            float f2 = f;
            if (z) {
                f2 = expandableView.getIncreasedPaddingAmount();
                i3 = i;
                if (i != 0) {
                    i3 = i + ((int) NotificationUtils.interpolate(this.mPaddingBetweenElements, this.mIncreasedPaddingBetweenElements, Math.max(f, f2)));
                }
            }
            if (expandableView == expandableNotificationRow3) {
                int i4 = i3;
                if (expandableNotificationRow2 != null) {
                    i4 = i3 + expandableNotificationRow2.getPositionOfChild(expandableNotificationRow);
                }
                return i4;
            }
            i = i3;
            if (z) {
                i = i3 + getIntrinsicHeight(expandableView);
            }
            i2++;
            f = f2;
        }
        return 0;
    }

    private float getRubberBandFactor(boolean z) {
        if (z) {
            if (this.mExpandedInThisMotion) {
                return 0.15f;
            }
            if (this.mIsExpansionChanging || this.mPanelTracking) {
                return 0.21f;
            }
            return this.mScrolledToTopOnFirstDown ? 1.0f : 0.35f;
        }
        return 0.35f;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int getScrollRange() {
        int max = Math.max(0, (getContentHeight() - this.mMaxLayoutHeight) + this.mBottomStackPeekSize + this.mBottomStackSlowDownHeight);
        int imeInset = getImeInset();
        return max + Math.min(imeInset, Math.max(0, getContentHeight() - (getHeight() - imeInset)));
    }

    private int getStackEndPosition() {
        return ((this.mMaxLayoutHeight - this.mBottomStackPeekSize) - this.mBottomStackSlowDownHeight) + this.mPaddingBetweenElements + ((int) this.mStackTranslation);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleChildDismissed(View view) {
        if (this.mDismissAllInProgress) {
            return;
        }
        setSwipingInProgress(false);
        if (this.mDragAnimPendingChildren.contains(view)) {
            this.mDragAnimPendingChildren.remove(view);
        }
        this.mSwipedOutViews.add(view);
        this.mAmbientState.onDragFinished(view);
        updateContinuousShadowDrawing();
        if (view instanceof ExpandableNotificationRow) {
            ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) view;
            if (expandableNotificationRow.isHeadsUp()) {
                this.mHeadsUpManager.addSwipedOutNotification(expandableNotificationRow.getStatusBarNotification().getKey());
            }
        }
        performDismiss(view, this.mGroupManager, false);
        this.mFalsingManager.onNotificationDismissed();
        if (this.mFalsingManager.shouldEnforceBouncer()) {
            this.mPhoneStatusBar.executeRunnableDismissingKeyguard(null, null, false, true, false);
        }
    }

    private void handleDismissAllClipping() {
        int childCount = getChildCount();
        boolean z = false;
        for (int i = 0; i < childCount; i++) {
            ExpandableView expandableView = (ExpandableView) getChildAt(i);
            if (expandableView.getVisibility() != 8) {
                if (this.mDismissAllInProgress && z) {
                    expandableView.setMinClipTopAmount(expandableView.getClipTopAmount());
                } else {
                    expandableView.setMinClipTopAmount(0);
                }
                z = canChildBeDismissed(expandableView);
            }
        }
    }

    private void handleEmptySpaceClick(MotionEvent motionEvent) {
        switch (motionEvent.getActionMasked()) {
            case 1:
                if (this.mPhoneStatusBar.getBarState() != 1 && this.mTouchIsClick && isBelowLastNotification(this.mInitialTouchX, this.mInitialTouchY)) {
                    this.mOnEmptySpaceClickListener.onEmptySpaceClicked(this.mInitialTouchX, this.mInitialTouchY);
                    return;
                }
                return;
            case 2:
                if (this.mTouchIsClick) {
                    if (Math.abs(motionEvent.getY() - this.mInitialTouchY) > this.mTouchSlop || Math.abs(motionEvent.getX() - this.mInitialTouchX) > this.mTouchSlop) {
                        this.mTouchIsClick = false;
                        return;
                    }
                    return;
                }
                return;
            default:
                return;
        }
    }

    private void initDownStates(MotionEvent motionEvent) {
        if (motionEvent.getAction() == 0) {
            this.mExpandedInThisMotion = false;
            this.mOnlyScrollingInThisMotion = !this.mScroller.isFinished();
            this.mDisallowScrollingInThisMotion = false;
            this.mDisallowDismissInThisMotion = false;
            this.mTouchIsClick = true;
            this.mInitialTouchX = motionEvent.getX();
            this.mInitialTouchY = motionEvent.getY();
        }
    }

    private void initOrResetVelocityTracker() {
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        } else {
            this.mVelocityTracker.clear();
        }
    }

    private void initVelocityTrackerIfNotExists() {
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void initView(Context context) {
        this.mScroller = new OverScroller(getContext());
        setDescendantFocusability(262144);
        setClipChildren(false);
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        this.mTouchSlop = viewConfiguration.getScaledTouchSlop();
        this.mMinimumVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
        this.mMaximumVelocity = viewConfiguration.getScaledMaximumFlingVelocity();
        this.mOverflingDistance = viewConfiguration.getScaledOverflingDistance();
        this.mCollapsedSize = context.getResources().getDimensionPixelSize(2131689785);
        this.mBottomStackPeekSize = context.getResources().getDimensionPixelSize(2131689872);
        this.mStackScrollAlgorithm.initView(context);
        this.mPaddingBetweenElements = Math.max(1, context.getResources().getDimensionPixelSize(2131689876));
        this.mIncreasedPaddingBetweenElements = context.getResources().getDimensionPixelSize(2131689878);
        this.mBottomStackSlowDownHeight = this.mStackScrollAlgorithm.getBottomStackSlowDownLength();
        this.mMinTopOverScrollToEscape = getResources().getDimensionPixelSize(2131689879);
    }

    private boolean isChildInGroup(View view) {
        return view instanceof ExpandableNotificationRow ? this.mGroupManager.isChildInGroupWithSummary(((ExpandableNotificationRow) view).getStatusBarNotification()) : false;
    }

    private boolean isChildInInvisibleGroup(View view) {
        boolean z = false;
        if (view instanceof ExpandableNotificationRow) {
            ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) view;
            ExpandableNotificationRow groupSummary = this.mGroupManager.getGroupSummary(expandableNotificationRow.getStatusBarNotification());
            if (groupSummary == null || groupSummary == expandableNotificationRow) {
                return false;
            }
            if (expandableNotificationRow.getVisibility() == 4) {
                z = true;
            }
            return z;
        }
        return false;
    }

    private boolean isClickedHeadsUp(View view) {
        return HeadsUpManager.isClickedHeadsUpNotification(view);
    }

    private boolean isCurrentlyAnimating() {
        return this.mStateAnimator.isRunning();
    }

    private boolean isHeadsUp(View view) {
        if (view instanceof ExpandableNotificationRow) {
            return ((ExpandableNotificationRow) view).isHeadsUp();
        }
        return false;
    }

    private boolean isInContentBounds(MotionEvent motionEvent) {
        return isInContentBounds(motionEvent.getY());
    }

    public static boolean isPinnedHeadsUp(View view) {
        boolean z = false;
        if (view instanceof ExpandableNotificationRow) {
            ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) view;
            if (expandableNotificationRow.isHeadsUp()) {
                z = expandableNotificationRow.isPinned();
            }
            return z;
        }
        return false;
    }

    private boolean isRubberbanded(boolean z) {
        boolean z2 = true;
        if (z) {
            z2 = true;
            if (!this.mExpandedInThisMotion) {
                z2 = true;
                if (!this.mIsExpansionChanging) {
                    z2 = true;
                    if (!this.mPanelTracking) {
                        z2 = true;
                        if (this.mScrolledToTopOnFirstDown) {
                            z2 = false;
                        }
                    }
                }
            }
        }
        return z2;
    }

    private boolean isScrollingEnabled() {
        return this.mScrollingEnabled;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void notifyHeightChangeListener(ExpandableView expandableView) {
        if (this.mOnHeightChangedListener != null) {
            this.mOnHeightChangedListener.onHeightChanged(expandableView, false);
        }
    }

    private void notifyOverscrollTopListener(float f, boolean z) {
        this.mExpandHelper.onlyObserveMovements(f > 1.0f);
        if (this.mDontReportNextOverScroll) {
            this.mDontReportNextOverScroll = false;
        } else if (this.mOverscrollTopChangedListener != null) {
            this.mOverscrollTopChangedListener.onOverscrollTopChanged(f, z);
        }
    }

    private boolean onInterceptTouchEventScroll(MotionEvent motionEvent) {
        if (isScrollingEnabled()) {
            int action = motionEvent.getAction();
            if (action == 2 && this.mIsBeingDragged) {
                return true;
            }
            switch (action & 255) {
                case 0:
                    int y = (int) motionEvent.getY();
                    this.mScrolledToTopOnFirstDown = isScrolledToTop();
                    if (getChildAtPosition(motionEvent.getX(), y) != null) {
                        this.mLastMotionY = y;
                        this.mDownX = (int) motionEvent.getX();
                        this.mActivePointerId = motionEvent.getPointerId(0);
                        initOrResetVelocityTracker();
                        this.mVelocityTracker.addMovement(motionEvent);
                        setIsBeingDragged(!this.mScroller.isFinished());
                        break;
                    } else {
                        setIsBeingDragged(false);
                        recycleVelocityTracker();
                        break;
                    }
                case 1:
                case 3:
                    setIsBeingDragged(false);
                    this.mActivePointerId = -1;
                    recycleVelocityTracker();
                    if (this.mScroller.springBack(this.mScrollX, this.mOwnScrollY, 0, 0, 0, getScrollRange())) {
                        postInvalidateOnAnimation();
                        break;
                    }
                    break;
                case 2:
                    int i = this.mActivePointerId;
                    if (i != -1) {
                        int findPointerIndex = motionEvent.findPointerIndex(i);
                        if (findPointerIndex != -1) {
                            int y2 = (int) motionEvent.getY(findPointerIndex);
                            int x = (int) motionEvent.getX(findPointerIndex);
                            int abs = Math.abs(y2 - this.mLastMotionY);
                            int abs2 = Math.abs(x - this.mDownX);
                            if (abs > this.mTouchSlop && abs > abs2) {
                                setIsBeingDragged(true);
                                this.mLastMotionY = y2;
                                this.mDownX = x;
                                initVelocityTrackerIfNotExists();
                                this.mVelocityTracker.addMovement(motionEvent);
                                break;
                            }
                        } else {
                            Log.e("StackScroller", "Invalid pointerId=" + i + " in onInterceptTouchEvent");
                            break;
                        }
                    }
                    break;
                case 6:
                    onSecondaryPointerUp(motionEvent);
                    break;
            }
            return this.mIsBeingDragged;
        }
        return false;
    }

    private void onOverScrollFling(boolean z, int i) {
        if (this.mOverscrollTopChangedListener != null) {
            this.mOverscrollTopChangedListener.flingTopOverscroll(i, z);
        }
        this.mDontReportNextOverScroll = true;
        setOverScrollAmount(0.0f, true, false);
    }

    private boolean onScrollTouch(MotionEvent motionEvent) {
        if (isScrollingEnabled() && motionEvent.getY() >= this.mQsContainer.getBottom()) {
            this.mForcedScroll = null;
            initVelocityTrackerIfNotExists();
            this.mVelocityTracker.addMovement(motionEvent);
            switch (motionEvent.getAction() & 255) {
                case 0:
                    if (getChildCount() == 0 || !isInContentBounds(motionEvent)) {
                        return false;
                    }
                    setIsBeingDragged(!this.mScroller.isFinished());
                    if (!this.mScroller.isFinished()) {
                        this.mScroller.forceFinished(true);
                    }
                    this.mLastMotionY = (int) motionEvent.getY();
                    this.mDownX = (int) motionEvent.getX();
                    this.mActivePointerId = motionEvent.getPointerId(0);
                    return true;
                case 1:
                    if (this.mIsBeingDragged) {
                        VelocityTracker velocityTracker = this.mVelocityTracker;
                        velocityTracker.computeCurrentVelocity(1000, this.mMaximumVelocity);
                        int yVelocity = (int) velocityTracker.getYVelocity(this.mActivePointerId);
                        if (shouldOverScrollFling(yVelocity)) {
                            onOverScrollFling(true, yVelocity);
                        } else if (getChildCount() > 0) {
                            if (Math.abs(yVelocity) > this.mMinimumVelocity) {
                                if (getCurrentOverScrollAmount(true) == 0.0f || yVelocity > 0) {
                                    fling(-yVelocity);
                                } else {
                                    onOverScrollFling(false, yVelocity);
                                }
                            } else if (this.mScroller.springBack(this.mScrollX, this.mOwnScrollY, 0, 0, 0, getScrollRange())) {
                                postInvalidateOnAnimation();
                            }
                        }
                        this.mActivePointerId = -1;
                        endDrag();
                        return true;
                    }
                    return true;
                case 2:
                    int findPointerIndex = motionEvent.findPointerIndex(this.mActivePointerId);
                    if (findPointerIndex == -1) {
                        Log.e("StackScroller", "Invalid pointerId=" + this.mActivePointerId + " in onTouchEvent");
                        return true;
                    }
                    int y = (int) motionEvent.getY(findPointerIndex);
                    int x = (int) motionEvent.getX(findPointerIndex);
                    int i = this.mLastMotionY - y;
                    int abs = Math.abs(x - this.mDownX);
                    int abs2 = Math.abs(i);
                    int i2 = i;
                    if (!this.mIsBeingDragged) {
                        i2 = i;
                        if (abs2 > this.mTouchSlop) {
                            i2 = i;
                            if (abs2 > abs) {
                                setIsBeingDragged(true);
                                i2 = i > 0 ? i - this.mTouchSlop : i + this.mTouchSlop;
                            }
                        }
                    }
                    if (this.mIsBeingDragged) {
                        this.mLastMotionY = y;
                        int scrollRange = getScrollRange();
                        int i3 = scrollRange;
                        if (this.mExpandedInThisMotion) {
                            i3 = Math.min(scrollRange, this.mMaxScrollAfterExpand);
                        }
                        float overScrollDown = i2 < 0 ? overScrollDown(i2) : overScrollUp(i2, i3);
                        if (overScrollDown != 0.0f) {
                            overScrollBy(0, (int) overScrollDown, 0, this.mOwnScrollY, 0, i3, 0, getHeight() / 2, true);
                            return true;
                        }
                        return true;
                    }
                    return true;
                case 3:
                    if (!this.mIsBeingDragged || getChildCount() <= 0) {
                        return true;
                    }
                    if (this.mScroller.springBack(this.mScrollX, this.mOwnScrollY, 0, 0, 0, getScrollRange())) {
                        postInvalidateOnAnimation();
                    }
                    this.mActivePointerId = -1;
                    endDrag();
                    return true;
                case 4:
                default:
                    return true;
                case 5:
                    int actionIndex = motionEvent.getActionIndex();
                    this.mLastMotionY = (int) motionEvent.getY(actionIndex);
                    this.mDownX = (int) motionEvent.getX(actionIndex);
                    this.mActivePointerId = motionEvent.getPointerId(actionIndex);
                    return true;
                case 6:
                    onSecondaryPointerUp(motionEvent);
                    this.mLastMotionY = (int) motionEvent.getY(motionEvent.findPointerIndex(this.mActivePointerId));
                    this.mDownX = (int) motionEvent.getX(motionEvent.findPointerIndex(this.mActivePointerId));
                    return true;
            }
        }
        return false;
    }

    private void onSecondaryPointerUp(MotionEvent motionEvent) {
        int i = 0;
        int action = (motionEvent.getAction() & 65280) >> 8;
        if (motionEvent.getPointerId(action) == this.mActivePointerId) {
            if (action == 0) {
                i = 1;
            }
            this.mLastMotionY = (int) motionEvent.getY(i);
            this.mActivePointerId = motionEvent.getPointerId(i);
            if (this.mVelocityTracker != null) {
                this.mVelocityTracker.clear();
            }
        }
    }

    private void onViewAddedInternal(View view) {
        updateHideSensitiveForChild(view);
        ((ExpandableView) view).setOnHeightChangedListener(this);
        generateAddAnimation(view, false);
        updateAnimationState(view);
        updateChronometerForChild(view);
    }

    private void onViewRemovedInternal(View view, ViewGroup viewGroup) {
        if (this.mChangePositionInProgress) {
            return;
        }
        ExpandableView expandableView = (ExpandableView) view;
        expandableView.setOnHeightChangedListener(null);
        this.mCurrentStackScrollState.removeViewStateForView(view);
        updateScrollStateForRemovedChild(expandableView);
        if (!generateRemoveAnimation(view)) {
            this.mSwipedOutViews.remove(view);
        } else if (!this.mSwipedOutViews.contains(view)) {
            viewGroup.getOverlay().add(view);
        } else if (Math.abs(expandableView.getTranslation()) != expandableView.getWidth()) {
            viewGroup.addTransientView(view, 0);
            expandableView.setTransientContainer(viewGroup);
        }
        updateAnimationState(false, view);
        expandableView.setClipTopAmount(0);
        focusNextViewIfFocused(view);
    }

    private float overScrollDown(int i) {
        int min = Math.min(i, 0);
        float currentOverScrollAmount = getCurrentOverScrollAmount(false);
        float f = currentOverScrollAmount + min;
        if (currentOverScrollAmount > 0.0f) {
            setOverScrollAmount(f, false, false);
        }
        if (f >= 0.0f) {
            f = 0.0f;
        }
        float f2 = this.mOwnScrollY + f;
        if (f2 < 0.0f) {
            setOverScrolledPixels(getCurrentOverScrolledPixels(true) - f2, true, false);
            this.mOwnScrollY = 0;
            f = 0.0f;
        }
        return f;
    }

    private float overScrollUp(int i, int i2) {
        int max = Math.max(i, 0);
        float currentOverScrollAmount = getCurrentOverScrollAmount(true);
        float f = currentOverScrollAmount - max;
        if (currentOverScrollAmount > 0.0f) {
            setOverScrollAmount(f, true, false);
        }
        float f2 = f < 0.0f ? -f : 0.0f;
        float f3 = this.mOwnScrollY + f2;
        if (f3 > i2) {
            if (!this.mExpandedInThisMotion) {
                setOverScrolledPixels((getCurrentOverScrolledPixels(false) + f3) - i2, false, false);
            }
            this.mOwnScrollY = i2;
            f2 = 0.0f;
        }
        return f2;
    }

    public static void performDismiss(View view, NotificationGroupManager notificationGroupManager, boolean z) {
        if (view instanceof ExpandableNotificationRow) {
            ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) view;
            if (notificationGroupManager.isOnlyChildInGroup(expandableNotificationRow.getStatusBarNotification())) {
                ExpandableNotificationRow logicalGroupSummary = notificationGroupManager.getLogicalGroupSummary(expandableNotificationRow.getStatusBarNotification());
                if (logicalGroupSummary.isClearable()) {
                    performDismiss(logicalGroupSummary, notificationGroupManager, z);
                }
            }
            expandableNotificationRow.setDismissed(true, z);
            if (expandableNotificationRow.isClearable()) {
                expandableNotificationRow.performDismiss();
            }
        }
    }

    private void recycleVelocityTracker() {
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.recycle();
            this.mVelocityTracker = null;
        }
    }

    private boolean removeRemovedChildFromHeadsUpChangeAnimations(View view) {
        boolean z = false;
        for (Pair<ExpandableNotificationRow, Boolean> pair : this.mHeadsUpChangeAnimations) {
            ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) pair.first;
            boolean booleanValue = ((Boolean) pair.second).booleanValue();
            if (view == expandableNotificationRow) {
                this.mTmpList.add(pair);
                z |= booleanValue;
            }
        }
        if (z) {
            this.mHeadsUpChangeAnimations.removeAll(this.mTmpList);
            ((ExpandableNotificationRow) view).setHeadsupDisappearRunning(false);
        }
        this.mTmpList.clear();
        return z;
    }

    private void requestAnimateEverything() {
        if (this.mIsExpanded && this.mAnimationsEnabled) {
            this.mEverythingNeedsAnimation = true;
            this.mNeedsAnimation = true;
            requestChildrenUpdate();
        }
    }

    private void requestAnimationOnViewResize(ExpandableNotificationRow expandableNotificationRow) {
        if (this.mAnimationsEnabled) {
            if (this.mIsExpanded || (expandableNotificationRow != null && expandableNotificationRow.isPinned())) {
                this.mNeedViewResizeAnimation = true;
                this.mNeedsAnimation = true;
            }
        }
    }

    private void requestChildrenUpdate() {
        if (this.mChildrenUpdateRequested) {
            return;
        }
        getViewTreeObserver().addOnPreDrawListener(this.mChildrenUpdater);
        this.mChildrenUpdateRequested = true;
        invalidate();
    }

    private void runAnimationFinishedRunnables() {
        for (Runnable runnable : this.mAnimationFinishedRunnables) {
            runnable.run();
        }
        this.mAnimationFinishedRunnables.clear();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setBackgroundFadeAmount(float f) {
        this.mBackgroundFadeAmount = f;
        updateBackgroundDimming();
    }

    private void setBackgroundTop(int i) {
        this.mCurrentBounds.top = i;
        applyCurrentBackgroundBounds();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setDimAmount(float f) {
        this.mDimAmount = f;
        updateBackgroundDimming();
    }

    private void setIsBeingDragged(boolean z) {
        this.mIsBeingDragged = z;
        if (z) {
            requestDisallowInterceptTouchEvent(true);
            removeLongPressCallback();
        }
    }

    private void setIsExpanded(boolean z) {
        boolean z2 = z != this.mIsExpanded;
        this.mIsExpanded = z;
        this.mStackScrollAlgorithm.setIsExpanded(z);
        if (z2) {
            if (!this.mIsExpanded) {
                this.mGroupManager.collapseAllGroups();
            }
            updateNotificationAnimationStates();
            updateChronometers();
        }
    }

    private void setMaxLayoutHeight(int i) {
        this.mMaxLayoutHeight = i;
        updateAlgorithmHeightAndPadding();
    }

    private void setOverScrollAmountInternal(float f, boolean z, boolean z2, boolean z3) {
        float max = Math.max(0.0f, f);
        if (z2) {
            this.mStateAnimator.animateOverScrollToAmount(max, z, z3);
            return;
        }
        setOverScrolledPixels(max / getRubberBandFactor(z), z);
        this.mAmbientState.setOverScrollAmount(max, z);
        if (z) {
            notifyOverscrollTopListener(max, z3);
        }
        requestChildrenUpdate();
    }

    private void setOverScrolledPixels(float f, boolean z) {
        if (z) {
            this.mOverScrolledTopPixels = f;
        } else {
            this.mOverScrolledBottomPixels = f;
        }
    }

    private void setStackTranslation(float f) {
        if (f != this.mStackTranslation) {
            this.mStackTranslation = f;
            this.mAmbientState.setStackTranslation(f);
            requestChildrenUpdate();
        }
    }

    private void setSwipingInProgress(boolean z) {
        this.mSwipingInProgress = z;
        if (z) {
            requestDisallowInterceptTouchEvent(true);
        }
    }

    private void setTopPadding(int i, boolean z) {
        if (this.mTopPadding != i) {
            this.mTopPadding = i;
            updateAlgorithmHeightAndPadding();
            updateContentHeight();
            if (z && this.mAnimationsEnabled && this.mIsExpanded) {
                this.mTopPaddingNeedsAnimation = true;
                this.mNeedsAnimation = true;
            }
            requestChildrenUpdate();
            notifyHeightChangeListener(null);
        }
    }

    private boolean shouldHunAppearFromBottom(StackViewState stackViewState) {
        return stackViewState.yTranslation + ((float) stackViewState.height) >= this.mAmbientState.getMaxHeadsUpTranslation();
    }

    private boolean shouldOverScrollFling(int i) {
        boolean z = true;
        float currentOverScrollAmount = getCurrentOverScrollAmount(true);
        if (!this.mScrolledToTopOnFirstDown || this.mExpandedInThisMotion || currentOverScrollAmount <= this.mMinTopOverScrollToEscape) {
            z = false;
        } else if (i <= 0) {
            z = false;
        }
        return z;
    }

    private void springBack() {
        boolean z;
        float f;
        int scrollRange = getScrollRange();
        boolean z2 = this.mOwnScrollY <= 0;
        boolean z3 = this.mOwnScrollY >= scrollRange;
        if (z2 || z3) {
            if (z2) {
                z = true;
                f = -this.mOwnScrollY;
                this.mOwnScrollY = 0;
                this.mDontReportNextOverScroll = true;
            } else {
                z = false;
                f = this.mOwnScrollY - scrollRange;
                this.mOwnScrollY = scrollRange;
            }
            setOverScrollAmount(f, z, false);
            setOverScrollAmount(0.0f, z, true);
            this.mScroller.forceFinished(true);
        }
    }

    private void startAnimationToState() {
        if (this.mNeedsAnimation) {
            generateChildHierarchyEvents();
            this.mNeedsAnimation = false;
        }
        if (!this.mAnimationEvents.isEmpty() || isCurrentlyAnimating()) {
            setAnimationRunning(true);
            this.mStateAnimator.startAnimationForEvents(this.mAnimationEvents, this.mCurrentStackScrollState, this.mGoToFullShadeDelay);
            this.mAnimationEvents.clear();
            updateBackground();
            updateViewShadows();
        } else {
            applyCurrentState();
        }
        this.mGoToFullShadeDelay = 0L;
    }

    private void startBackgroundAnimation() {
        this.mCurrentBounds.left = this.mBackgroundBounds.left;
        this.mCurrentBounds.right = this.mBackgroundBounds.right;
        startBottomAnimation();
        startTopAnimation();
    }

    private void startBackgroundFadeIn() {
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this, BACKGROUND_FADE, 0.0f, 1.0f);
        ofFloat.setStartDelay(Math.max(0, (this.mDarkAnimationOriginIndex == -1 || this.mDarkAnimationOriginIndex == -2) ? getNotGoneChildCount() - 1 : Math.max(this.mDarkAnimationOriginIndex, (getNotGoneChildCount() - this.mDarkAnimationOriginIndex) - 1)) * 24);
        ofFloat.setDuration(360L);
        ofFloat.setInterpolator(Interpolators.ALPHA_IN);
        ofFloat.start();
    }

    private void startBottomAnimation() {
        int i = this.mStartAnimationRect.bottom;
        int i2 = this.mEndAnimationRect.bottom;
        int i3 = this.mBackgroundBounds.bottom;
        ObjectAnimator objectAnimator = this.mBottomAnimator;
        if (objectAnimator == null || i2 != i3) {
            if (!this.mAnimateNextBackgroundBottom) {
                if (objectAnimator == null) {
                    setBackgroundBottom(i3);
                    return;
                }
                objectAnimator.getValues()[0].setIntValues(i, i3);
                this.mStartAnimationRect.bottom = i;
                this.mEndAnimationRect.bottom = i3;
                objectAnimator.setCurrentPlayTime(objectAnimator.getCurrentPlayTime());
                return;
            }
            if (objectAnimator != null) {
                objectAnimator.cancel();
            }
            ObjectAnimator ofInt = ObjectAnimator.ofInt(this, "backgroundBottom", this.mCurrentBounds.bottom, i3);
            ofInt.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
            ofInt.setDuration(360L);
            ofInt.addListener(new AnimatorListenerAdapter(this) { // from class: com.android.systemui.statusbar.stack.NotificationStackScrollLayout.10
                final NotificationStackScrollLayout this$0;

                {
                    this.this$0 = this;
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    this.this$0.mStartAnimationRect.bottom = -1;
                    this.this$0.mEndAnimationRect.bottom = -1;
                    this.this$0.mBottomAnimator = null;
                }
            });
            ofInt.start();
            this.mStartAnimationRect.bottom = this.mCurrentBounds.bottom;
            this.mEndAnimationRect.bottom = i3;
            this.mBottomAnimator = ofInt;
        }
    }

    private void startTopAnimation() {
        int i = this.mEndAnimationRect.top;
        int i2 = this.mBackgroundBounds.top;
        ObjectAnimator objectAnimator = this.mTopAnimator;
        if (objectAnimator == null || i != i2) {
            if (!this.mAnimateNextBackgroundTop) {
                if (objectAnimator == null) {
                    setBackgroundTop(i2);
                    return;
                }
                int i3 = this.mStartAnimationRect.top;
                objectAnimator.getValues()[0].setIntValues(i3, i2);
                this.mStartAnimationRect.top = i3;
                this.mEndAnimationRect.top = i2;
                objectAnimator.setCurrentPlayTime(objectAnimator.getCurrentPlayTime());
                return;
            }
            if (objectAnimator != null) {
                objectAnimator.cancel();
            }
            ObjectAnimator ofInt = ObjectAnimator.ofInt(this, "backgroundTop", this.mCurrentBounds.top, i2);
            ofInt.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
            ofInt.setDuration(360L);
            ofInt.addListener(new AnimatorListenerAdapter(this) { // from class: com.android.systemui.statusbar.stack.NotificationStackScrollLayout.9
                final NotificationStackScrollLayout this$0;

                {
                    this.this$0 = this;
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    this.this$0.mStartAnimationRect.top = -1;
                    this.this$0.mEndAnimationRect.top = -1;
                    this.this$0.mTopAnimator = null;
                }
            });
            ofInt.start();
            this.mStartAnimationRect.top = this.mCurrentBounds.top;
            this.mEndAnimationRect.top = i2;
            this.mTopAnimator = ofInt;
        }
    }

    private int targetScrollForView(ExpandableView expandableView, int i) {
        return (((expandableView.getIntrinsicHeight() + i) + getImeInset()) - getHeight()) + getTopPadding();
    }

    private void updateAlgorithmHeightAndPadding() {
        this.mAmbientState.setLayoutHeight(getLayoutHeight());
        this.mAmbientState.setTopPadding(this.mTopPadding);
    }

    private void updateAnimationState(View view) {
        updateAnimationState((this.mAnimationsEnabled || this.mPulsing) ? !this.mIsExpanded ? isPinnedHeadsUp(view) : true : false, view);
    }

    private void updateAnimationState(boolean z, View view) {
        if (view instanceof ExpandableNotificationRow) {
            ((ExpandableNotificationRow) view).setIconAnimationRunning(z);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateBackground() {
        if (this.mAmbientState.isDark()) {
            return;
        }
        updateBackgroundBounds();
        if (this.mCurrentBounds.equals(this.mBackgroundBounds)) {
            if (this.mBottomAnimator != null) {
                this.mBottomAnimator.cancel();
            }
            if (this.mTopAnimator != null) {
                this.mTopAnimator.cancel();
            }
        } else if (this.mAnimateNextBackgroundTop || this.mAnimateNextBackgroundBottom || areBoundsAnimating()) {
            startBackgroundAnimation();
        } else {
            this.mCurrentBounds.set(this.mBackgroundBounds);
            applyCurrentBackgroundBounds();
        }
        this.mAnimateNextBackgroundBottom = false;
        this.mAnimateNextBackgroundTop = false;
    }

    private void updateBackgroundBounds() {
        int i;
        this.mBackgroundBounds.left = (int) getX();
        this.mBackgroundBounds.right = (int) (getX() + getWidth());
        if (!this.mIsExpanded) {
            this.mBackgroundBounds.top = 0;
            this.mBackgroundBounds.bottom = 0;
        }
        ActivatableNotificationView activatableNotificationView = this.mFirstVisibleBackgroundChild;
        int i2 = 0;
        if (activatableNotificationView != null) {
            i2 = (int) StackStateAnimator.getFinalTranslationY(activatableNotificationView);
            if (!this.mAnimateNextBackgroundTop && ((this.mTopAnimator != null || this.mCurrentBounds.top != i2) && (this.mTopAnimator == null || this.mEndAnimationRect.top != i2))) {
                i2 = (int) activatableNotificationView.getTranslationY();
            }
        }
        ActivatableNotificationView activatableNotificationView2 = this.mLastVisibleBackgroundChild;
        if (activatableNotificationView2 != null) {
            i = Math.min(((int) StackStateAnimator.getFinalTranslationY(activatableNotificationView2)) + StackStateAnimator.getFinalActualHeight(activatableNotificationView2), getHeight());
            if (!this.mAnimateNextBackgroundBottom && ((this.mBottomAnimator != null || this.mCurrentBounds.bottom != i) && (this.mBottomAnimator == null || this.mEndAnimationRect.bottom != i))) {
                i = Math.min((int) (activatableNotificationView2.getTranslationY() + activatableNotificationView2.getActualHeight()), getHeight());
            }
        } else {
            i2 = (int) (this.mTopPadding + this.mStackTranslation);
            i = i2;
        }
        if (this.mPhoneStatusBar.getBarState() != 1) {
            this.mBackgroundBounds.top = (int) Math.max(this.mTopPadding + this.mStackTranslation, i2);
        } else {
            this.mBackgroundBounds.top = Math.max(0, i2);
        }
        this.mBackgroundBounds.bottom = Math.min(getHeight(), Math.max(i, i2));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateBackgroundDimming() {
        float f = (0.7f + ((1.0f - this.mDimAmount) * 0.3f)) * this.mBackgroundFadeAmount;
        int scrimBehindColor = this.mScrimController.getScrimBehindColor();
        float f2 = 1.0f - f;
        this.mBackgroundPaint.setColor(Color.argb((int) ((255.0f * f) + (Color.alpha(scrimBehindColor) * f2)), (int) ((this.mBackgroundFadeAmount * Color.red(this.mBgColor)) + (Color.red(scrimBehindColor) * f2)), (int) ((this.mBackgroundFadeAmount * Color.green(this.mBgColor)) + (Color.green(scrimBehindColor) * f2)), (int) ((this.mBackgroundFadeAmount * Color.blue(this.mBgColor)) + (Color.blue(scrimBehindColor) * f2))));
        invalidate();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateChildren() {
        updateScrollStateForAddedChildren();
        this.mAmbientState.setScrollY(this.mOwnScrollY);
        this.mStackScrollAlgorithm.getStackScrollState(this.mAmbientState, this.mCurrentStackScrollState);
        if (isCurrentlyAnimating() || this.mNeedsAnimation) {
            startAnimationToState();
        } else {
            applyCurrentState();
        }
    }

    private void updateChronometerForChild(View view) {
        if (view instanceof ExpandableNotificationRow) {
            ((ExpandableNotificationRow) view).setChronometerRunning(this.mIsExpanded);
        }
    }

    private void updateChronometers() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            updateChronometerForChild(getChildAt(i));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateContentHeight() {
        int i = 0;
        float f = 0.0f;
        int i2 = 0;
        while (i2 < getChildCount()) {
            ExpandableView expandableView = (ExpandableView) getChildAt(i2);
            int i3 = i;
            float f2 = f;
            if (expandableView.getVisibility() != 8) {
                f2 = expandableView.getIncreasedPaddingAmount();
                int i4 = i;
                if (i != 0) {
                    i4 = i + ((int) NotificationUtils.interpolate(this.mPaddingBetweenElements, this.mIncreasedPaddingBetweenElements, Math.max(f, f2)));
                }
                i3 = i4 + expandableView.getIntrinsicHeight();
            }
            i2++;
            i = i3;
            f = f2;
        }
        this.mContentHeight = this.mTopPadding + i;
        updateScrollability();
    }

    private void updateContinuousShadowDrawing() {
        boolean z = !this.mAnimationRunning ? !this.mAmbientState.getDraggedViews().isEmpty() : true;
        if (z != this.mContinuousShadowUpdate) {
            if (z) {
                getViewTreeObserver().addOnPreDrawListener(this.mShadowUpdater);
            } else {
                getViewTreeObserver().removeOnPreDrawListener(this.mShadowUpdater);
            }
            this.mContinuousShadowUpdate = z;
        }
    }

    private void updateFadingState() {
        if (this.mFadingOut || this.mParentFadingOut || this.mAmbientState.isDark()) {
            this.mScrimController.setExcludedBackgroundArea(null);
        } else {
            applyCurrentBackgroundBounds();
        }
        updateSrcDrawing();
    }

    private void updateFirstAndLastBackgroundViews() {
        ActivatableNotificationView firstChildWithBackground = getFirstChildWithBackground();
        ActivatableNotificationView lastChildWithBackground = getLastChildWithBackground();
        if (this.mAnimationsEnabled && this.mIsExpanded) {
            this.mAnimateNextBackgroundTop = firstChildWithBackground != this.mFirstVisibleBackgroundChild;
            this.mAnimateNextBackgroundBottom = lastChildWithBackground != this.mLastVisibleBackgroundChild;
        } else {
            this.mAnimateNextBackgroundTop = false;
            this.mAnimateNextBackgroundBottom = false;
        }
        this.mFirstVisibleBackgroundChild = firstChildWithBackground;
        this.mLastVisibleBackgroundChild = lastChildWithBackground;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateForcedScroll() {
        if (this.mForcedScroll != null && (!this.mForcedScroll.hasFocus() || !this.mForcedScroll.isAttachedToWindow())) {
            this.mForcedScroll = null;
        }
        if (this.mForcedScroll != null) {
            ExpandableView expandableView = (ExpandableView) this.mForcedScroll;
            int positionInLinearLayout = getPositionInLinearLayout(expandableView);
            int targetScrollForView = targetScrollForView(expandableView, positionInLinearLayout);
            int intrinsicHeight = expandableView.getIntrinsicHeight();
            int max = Math.max(0, Math.min(targetScrollForView, getScrollRange()));
            if (this.mOwnScrollY < max || positionInLinearLayout + intrinsicHeight < this.mOwnScrollY) {
                this.mOwnScrollY = max;
            }
        }
    }

    private void updateHideSensitiveForChild(View view) {
        if (view instanceof ExpandableView) {
            ((ExpandableView) view).setHideSensitiveForIntrinsicHeight(this.mAmbientState.isHideSensitive());
        }
    }

    private void updateNotificationAnimationStates() {
        boolean z = !this.mAnimationsEnabled ? this.mPulsing : true;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = getChildAt(i);
            z &= !this.mIsExpanded ? isPinnedHeadsUp(childAt) : true;
            updateAnimationState(z, childAt);
        }
    }

    private void updateScrollPositionOnExpandInBottom(ExpandableView expandableView) {
        if (expandableView instanceof ExpandableNotificationRow) {
            ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) expandableView;
            if (!expandableNotificationRow.isUserLocked() || expandableNotificationRow == getFirstChildNotGone() || expandableNotificationRow.isSummaryWithChildren()) {
                return;
            }
            float translationY = expandableNotificationRow.getTranslationY() + expandableNotificationRow.getActualHeight();
            float f = translationY;
            if (expandableNotificationRow.isChildInGroup()) {
                f = translationY + expandableNotificationRow.getNotificationParent().getTranslationY();
            }
            int stackEndPosition = getStackEndPosition();
            if (f > stackEndPosition) {
                this.mOwnScrollY = (int) (this.mOwnScrollY + (f - stackEndPosition));
                this.mDisallowScrollingInThisMotion = true;
            }
        }
    }

    private void updateScrollStateForAddedChildren() {
        if (this.mChildrenToAddAnimated.isEmpty()) {
            return;
        }
        for (int i = 0; i < getChildCount(); i++) {
            ExpandableView expandableView = (ExpandableView) getChildAt(i);
            if (this.mChildrenToAddAnimated.contains(expandableView)) {
                int positionInLinearLayout = getPositionInLinearLayout(expandableView);
                int i2 = expandableView.getIncreasedPaddingAmount() == 1.0f ? this.mIncreasedPaddingBetweenElements : this.mPaddingBetweenElements;
                int intrinsicHeight = getIntrinsicHeight(expandableView);
                if (positionInLinearLayout < this.mOwnScrollY) {
                    this.mOwnScrollY += intrinsicHeight + i2;
                }
            }
        }
        clampScrollPosition();
    }

    private void updateScrollStateForRemovedChild(ExpandableView expandableView) {
        int positionInLinearLayout = getPositionInLinearLayout(expandableView);
        int intrinsicHeight = getIntrinsicHeight(expandableView) + ((int) NotificationUtils.interpolate(this.mPaddingBetweenElements, this.mIncreasedPaddingBetweenElements, expandableView.getIncreasedPaddingAmount()));
        if (positionInLinearLayout + intrinsicHeight <= this.mOwnScrollY) {
            this.mOwnScrollY -= intrinsicHeight;
        } else if (positionInLinearLayout < this.mOwnScrollY) {
            this.mOwnScrollY = positionInLinearLayout;
        }
    }

    private void updateScrollability() {
        boolean z = getScrollRange() > 0;
        if (z != this.mScrollable) {
            this.mScrollable = z;
            setFocusable(z);
        }
    }

    private void updateSrcDrawing() {
        this.mBackgroundPaint.setXfermode((!this.mDrawBackgroundAsSrc || this.mFadingOut || this.mParentFadingOut) ? null : this.mSrcMode);
        invalidate();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateViewShadows() {
        for (int i = 0; i < getChildCount(); i++) {
            ExpandableView expandableView = (ExpandableView) getChildAt(i);
            if (expandableView.getVisibility() != 8) {
                this.mTmpSortedChildren.add(expandableView);
            }
        }
        Collections.sort(this.mTmpSortedChildren, this.mViewPositionComparator);
        ExpandableView expandableView2 = null;
        for (int i2 = 0; i2 < this.mTmpSortedChildren.size(); i2++) {
            ExpandableView expandableView3 = this.mTmpSortedChildren.get(i2);
            float translationZ = expandableView3.getTranslationZ();
            float translationZ2 = (expandableView2 == null ? translationZ : expandableView2.getTranslationZ()) - translationZ;
            if (translationZ2 <= 0.0f || translationZ2 >= 0.1f) {
                expandableView3.setFakeShadowIntensity(0.0f, 0.0f, 0, 0);
            } else {
                expandableView3.setFakeShadowIntensity(translationZ2 / 0.1f, expandableView2.getOutlineAlpha(), (int) (((expandableView2.getTranslationY() + expandableView2.getActualHeight()) - expandableView3.getTranslationY()) - expandableView2.getExtraBottomPadding()), expandableView2.getOutlineTranslation());
            }
            expandableView2 = expandableView3;
        }
        this.mTmpSortedChildren.clear();
    }

    @Override // com.android.systemui.SwipeHelper.Callback
    public boolean canChildBeDismissed(View view) {
        return StackScrollAlgorithm.canChildBeDismissed(view);
    }

    @Override // com.android.systemui.ExpandHelper.Callback
    public boolean canChildBeExpanded(View view) {
        return ((view instanceof ExpandableNotificationRow) && ((ExpandableNotificationRow) view).isExpandable() && !((ExpandableNotificationRow) view).areGutsExposed()) ? this.mIsExpanded || !((ExpandableNotificationRow) view).isPinned() : false;
    }

    public void cancelExpandHelper() {
        this.mExpandHelper.cancel();
    }

    public void changeViewPosition(View view, int i) {
        int indexOfChild = indexOfChild(view);
        if (view == null || view.getParent() != this || indexOfChild == i) {
            return;
        }
        this.mChangePositionInProgress = true;
        ((ExpandableView) view).setChangingPosition(true);
        removeView(view);
        addView(view, i);
        ((ExpandableView) view).setChangingPosition(false);
        this.mChangePositionInProgress = false;
        if (this.mIsExpanded && this.mAnimationsEnabled && view.getVisibility() != 8) {
            this.mChildrenChangingPositions.add(view);
            this.mNeedsAnimation = true;
        }
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public void clearChildFocus(View view) {
        super.clearChildFocus(view);
        if (this.mForcedScroll == view) {
            this.mForcedScroll = null;
        }
    }

    public void closeControlsIfOutsideTouch(MotionEvent motionEvent) {
        this.mSwipeHelper.closeControlsIfOutsideTouch(motionEvent);
    }

    @Override // android.view.View
    public void computeScroll() {
        if (!this.mScroller.computeScrollOffset()) {
            this.mDontClampNextScroll = false;
            if (this.mFinishScrollingCallback != null) {
                this.mFinishScrollingCallback.run();
                return;
            }
            return;
        }
        int i = this.mScrollX;
        int i2 = this.mOwnScrollY;
        int currX = this.mScroller.getCurrX();
        int currY = this.mScroller.getCurrY();
        if (i != currX || i2 != currY) {
            int scrollRange = getScrollRange();
            if ((currY < 0 && i2 >= 0) || (currY > scrollRange && i2 <= scrollRange)) {
                float currVelocity = this.mScroller.getCurrVelocity();
                if (currVelocity >= this.mMinimumVelocity) {
                    this.mMaxOverScroll = (Math.abs(currVelocity) / 1000.0f) * this.mOverflingDistance;
                }
            }
            int i3 = scrollRange;
            if (this.mDontClampNextScroll) {
                i3 = Math.max(scrollRange, i2);
            }
            overScrollBy(currX - i, currY - i2, i, i2, 0, i3, 0, (int) this.mMaxOverScroll, false);
            onScrollChanged(this.mScrollX, this.mOwnScrollY, i, i2);
        }
        postInvalidateOnAnimation();
    }

    public void dismissViewAnimated(View view, Runnable runnable, int i, long j) {
        this.mSwipeHelper.dismissChild(view, 0.0f, runnable, i, true, j, true);
    }

    @Override // com.android.systemui.ExpandHelper.Callback
    public void expansionStateChanged(boolean z) {
        this.mExpandingNotification = z;
        if (this.mExpandedInThisMotion) {
            return;
        }
        this.mMaxScrollAfterExpand = this.mOwnScrollY;
        this.mExpandedInThisMotion = true;
    }

    public void forceNoOverlappingRendering(boolean z) {
        this.mForceNoOverlappingRendering = z;
    }

    public void generateAddAnimation(View view, boolean z) {
        if (this.mIsExpanded && this.mAnimationsEnabled && !this.mChangePositionInProgress) {
            this.mChildrenToAddAnimated.add(view);
            if (z) {
                this.mFromMoreCardAdditions.add(view);
            }
            this.mNeedsAnimation = true;
        }
        if (!isHeadsUp(view) || this.mChangePositionInProgress) {
            return;
        }
        this.mAddedHeadsUpChildren.add(view);
        this.mChildrenToAddAnimated.remove(view);
    }

    public void generateChildOrderChangedEvent() {
        if (this.mIsExpanded && this.mAnimationsEnabled) {
            this.mGenerateChildOrderChangedEvent = true;
            this.mNeedsAnimation = true;
            requestChildrenUpdate();
        }
    }

    public void generateHeadsUpAnimation(ExpandableNotificationRow expandableNotificationRow, boolean z) {
        if (this.mAnimationsEnabled) {
            this.mHeadsUpChangeAnimations.add(new Pair<>(expandableNotificationRow, Boolean.valueOf(z)));
            this.mNeedsAnimation = true;
            if (!this.mIsExpanded && !z) {
                expandableNotificationRow.setHeadsupDisappearRunning(true);
            }
            requestChildrenUpdate();
        }
    }

    public ActivatableNotificationView getActivatedChild() {
        return this.mAmbientState.getActivatedChild();
    }

    public float getBackgroundFadeAmount() {
        return this.mBackgroundFadeAmount;
    }

    public float getBottomMostNotificationBottom() {
        float f;
        int childCount = getChildCount();
        float f2 = 0.0f;
        int i = 0;
        while (i < childCount) {
            ExpandableView expandableView = (ExpandableView) getChildAt(i);
            if (expandableView.getVisibility() == 8) {
                f = f2;
            } else {
                float translationY = expandableView.getTranslationY() + expandableView.getActualHeight();
                f = f2;
                if (translationY > f2) {
                    f = translationY;
                }
            }
            i++;
            f2 = f;
        }
        return getStackTranslation() + f2;
    }

    public int getBottomStackPeekSize() {
        return this.mBottomStackPeekSize;
    }

    public int getBottomStackSlowDownHeight() {
        return this.mBottomStackSlowDownHeight;
    }

    /* JADX WARN: Code restructure failed: missing block: B:18:0x0055, code lost:
        if (r0.isClearable() != false) goto L15;
     */
    @Override // com.android.systemui.SwipeHelper.Callback
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public View getChildAtPosition(MotionEvent motionEvent) {
        ExpandableView childAtPosition = getChildAtPosition(motionEvent.getX(), motionEvent.getY());
        ExpandableNotificationRow expandableNotificationRow = childAtPosition;
        if (childAtPosition instanceof ExpandableNotificationRow) {
            ExpandableNotificationRow notificationParent = ((ExpandableNotificationRow) childAtPosition).getNotificationParent();
            expandableNotificationRow = childAtPosition;
            if (notificationParent != null) {
                expandableNotificationRow = childAtPosition;
                if (notificationParent.areChildrenExpanded()) {
                    if (!notificationParent.areGutsExposed() && this.mGearExposedView != notificationParent) {
                        expandableNotificationRow = childAtPosition;
                        if (notificationParent.getNotificationChildren().size() == 1) {
                            expandableNotificationRow = childAtPosition;
                        }
                    }
                    expandableNotificationRow = notificationParent;
                }
            }
        }
        return expandableNotificationRow;
    }

    @Override // com.android.systemui.ExpandHelper.Callback
    public ExpandableView getChildAtPosition(float f, float f2) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            ExpandableView expandableView = (ExpandableView) getChildAt(i);
            if (expandableView.getVisibility() != 8 && !(expandableView instanceof StackScrollerDecorView)) {
                float translationY = expandableView.getTranslationY();
                float clipTopAmount = expandableView.getClipTopAmount();
                float actualHeight = expandableView.getActualHeight();
                int width = getWidth();
                if (f2 >= translationY + clipTopAmount && f2 <= translationY + actualHeight && f >= 0.0f && f <= width) {
                    if (!(expandableView instanceof ExpandableNotificationRow)) {
                        return expandableView;
                    }
                    ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) expandableView;
                    if (this.mIsExpanded || !expandableNotificationRow.isHeadsUp() || !expandableNotificationRow.isPinned() || this.mHeadsUpManager.getTopEntry().entry.row == expandableNotificationRow || this.mGroupManager.getGroupSummary(this.mHeadsUpManager.getTopEntry().entry.row.getStatusBarNotification()) == expandableNotificationRow) {
                        return expandableNotificationRow.getViewAtPosition(f2 - translationY);
                    }
                }
            }
        }
        return null;
    }

    @Override // com.android.systemui.ExpandHelper.Callback
    public ExpandableView getChildAtRawPosition(float f, float f2) {
        getLocationOnScreen(this.mTempInt2);
        return getChildAtPosition(f - this.mTempInt2[0], f2 - this.mTempInt2[1]);
    }

    public int getChildLocation(View view) {
        StackViewState viewStateForView = this.mCurrentStackScrollState.getViewStateForView(view);
        if (viewStateForView == null) {
            return 0;
        }
        if (viewStateForView.gone) {
            return 64;
        }
        return viewStateForView.location;
    }

    public ExpandableView getClosestChildAtRawPosition(float f, float f2) {
        getLocationOnScreen(this.mTempInt2);
        float f3 = f2 - this.mTempInt2[1];
        ExpandableView expandableView = null;
        float f4 = Float.MAX_VALUE;
        int childCount = getChildCount();
        int i = 0;
        while (i < childCount) {
            ExpandableView expandableView2 = (ExpandableView) getChildAt(i);
            ExpandableView expandableView3 = expandableView;
            float f5 = f4;
            if (expandableView2.getVisibility() != 8) {
                if (expandableView2 instanceof StackScrollerDecorView) {
                    f5 = f4;
                    expandableView3 = expandableView;
                } else {
                    float translationY = expandableView2.getTranslationY();
                    float min = Math.min(Math.abs((translationY + expandableView2.getClipTopAmount()) - f3), Math.abs((translationY + expandableView2.getActualHeight()) - f3));
                    expandableView3 = expandableView;
                    f5 = f4;
                    if (min < f4) {
                        expandableView3 = expandableView2;
                        f5 = min;
                    }
                }
            }
            i++;
            expandableView = expandableView3;
            f4 = f5;
        }
        return expandableView;
    }

    public int getContentHeight() {
        return this.mContentHeight;
    }

    public float getCurrentOverScrollAmount(boolean z) {
        return this.mAmbientState.getOverScrollAmount(z);
    }

    public float getCurrentOverScrolledPixels(boolean z) {
        return z ? this.mOverScrolledTopPixels : this.mOverScrolledBottomPixels;
    }

    public int getDismissViewHeight() {
        return this.mDismissView.getHeight() + this.mPaddingBetweenElements;
    }

    public int getEmptyBottomMargin() {
        return Math.max(((this.mMaxLayoutHeight - this.mContentHeight) - this.mBottomStackPeekSize) - this.mBottomStackSlowDownHeight, 0);
    }

    public int getEmptyShadeViewHeight() {
        return this.mEmptyShadeView.getHeight();
    }

    @Override // com.android.systemui.SwipeHelper.Callback
    public float getFalsingThresholdFactor() {
        return this.mPhoneStatusBar.isWakeUpComingFromTouch() ? 1.5f : 1.0f;
    }

    public ExpandableView getFirstChildNotGone() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = getChildAt(i);
            if (childAt.getVisibility() != 8) {
                return (ExpandableView) childAt;
            }
        }
        return null;
    }

    public int getFirstItemMinHeight() {
        ExpandableView firstChildNotGone = getFirstChildNotGone();
        return firstChildNotGone != null ? firstChildNotGone.getMinHeight() : this.mCollapsedSize;
    }

    @Override // com.android.systemui.statusbar.policy.ScrollAdapter
    public View getHostView() {
        return this;
    }

    public int getIntrinsicPadding() {
        return this.mIntrinsicPadding;
    }

    public float getKeyguardBottomStackSize() {
        return this.mBottomStackPeekSize + getResources().getDimensionPixelSize(2131689874);
    }

    public View getLastChildNotGone() {
        for (int childCount = getChildCount() - 1; childCount >= 0; childCount--) {
            View childAt = getChildAt(childCount);
            if (childAt.getVisibility() != 8) {
                return childAt;
            }
        }
        return null;
    }

    public int getLayoutMinHeight() {
        ExpandableView firstChildNotGone = getFirstChildNotGone();
        int intrinsicHeight = firstChildNotGone != null ? firstChildNotGone.getIntrinsicHeight() : this.mEmptyShadeView != null ? this.mEmptyShadeView.getMinHeight() : this.mCollapsedSize;
        int i = intrinsicHeight;
        if (this.mOwnScrollY > 0) {
            i = Math.max(intrinsicHeight - this.mOwnScrollY, this.mCollapsedSize);
        }
        return Math.min(this.mBottomStackPeekSize + i + this.mBottomStackSlowDownHeight, this.mMaxLayoutHeight - this.mTopPadding);
    }

    @Override // com.android.systemui.ExpandHelper.Callback
    public int getMaxExpandHeight(ExpandableView expandableView) {
        int maxContentHeight = expandableView.getMaxContentHeight();
        if (expandableView.isSummaryWithChildren()) {
            this.mGroupExpandedForMeasure = true;
            ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) expandableView;
            this.mGroupManager.toggleGroupExpansion(expandableNotificationRow.getStatusBarNotification());
            expandableNotificationRow.setForceUnlocked(true);
            this.mAmbientState.setLayoutHeight(this.mMaxLayoutHeight);
            this.mStackScrollAlgorithm.getStackScrollState(this.mAmbientState, this.mCurrentStackScrollState);
            this.mAmbientState.setLayoutHeight(getLayoutHeight());
            this.mGroupManager.toggleGroupExpansion(expandableNotificationRow.getStatusBarNotification());
            this.mGroupExpandedForMeasure = false;
            expandableNotificationRow.setForceUnlocked(false);
            return Math.min(this.mCurrentStackScrollState.getViewStateForView(expandableView).height, maxContentHeight);
        }
        return maxContentHeight;
    }

    public int getNotGoneChildCount() {
        int childCount = getChildCount();
        int i = 0;
        int i2 = 0;
        while (i2 < childCount) {
            ExpandableView expandableView = (ExpandableView) getChildAt(i2);
            int i3 = i;
            if (expandableView.getVisibility() != 8) {
                i3 = expandableView.willBeGone() ? i : i + 1;
            }
            i2++;
            i = i3;
        }
        return i;
    }

    public float getNotificationsTopY() {
        return this.mTopPadding + getStackTranslation();
    }

    public int getPeekHeight() {
        ExpandableView firstChildNotGone = getFirstChildNotGone();
        return this.mIntrinsicPadding + (firstChildNotGone != null ? firstChildNotGone.getCollapsedHeight() : this.mCollapsedSize) + this.mBottomStackPeekSize + this.mBottomStackSlowDownHeight;
    }

    public float getStackTranslation() {
        return this.mStackTranslation;
    }

    public int getTopPadding() {
        return this.mTopPadding;
    }

    public float getTopPaddingOverflow() {
        return this.mTopPaddingOverflow;
    }

    public void goToFullShade(long j) {
        this.mDismissView.setInvisible();
        this.mEmptyShadeView.setInvisible();
        this.mGoToFullShadeNeedsAnimation = true;
        this.mGoToFullShadeDelay = j;
        this.mNeedsAnimation = true;
        requestChildrenUpdate();
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return !this.mForceNoOverlappingRendering ? super.hasOverlappingRendering() : false;
    }

    public boolean isAddOrRemoveAnimationPending() {
        boolean z;
        if (this.mNeedsAnimation) {
            z = true;
            if (this.mChildrenToAddAnimated.isEmpty()) {
                z = true;
                if (this.mChildrenToRemoveAnimated.isEmpty()) {
                    z = false;
                }
            }
        } else {
            z = false;
        }
        return z;
    }

    @Override // com.android.systemui.SwipeHelper.Callback
    public boolean isAntiFalsingNeeded() {
        boolean z = true;
        if (this.mPhoneStatusBar.getBarState() != 1) {
            z = false;
        }
        return z;
    }

    public boolean isBelowLastNotification(float f, float f2) {
        for (int childCount = getChildCount() - 1; childCount >= 0; childCount--) {
            ExpandableView expandableView = (ExpandableView) getChildAt(childCount);
            if (expandableView.getVisibility() != 8) {
                float y = expandableView.getY();
                if (y > f2) {
                    return false;
                }
                boolean z = f2 > ((float) expandableView.getActualHeight()) + y;
                if (expandableView == this.mDismissView) {
                    if (!z && !this.mDismissView.isOnEmptySpace(f - this.mDismissView.getX(), f2 - y)) {
                        return false;
                    }
                } else if (expandableView == this.mEmptyShadeView) {
                    return true;
                } else {
                    if (!z) {
                        return false;
                    }
                }
            }
        }
        return f2 > this.mTopPadding + this.mStackTranslation;
    }

    public boolean isDismissViewNotGone() {
        boolean z = false;
        if (this.mDismissView.getVisibility() != 8) {
            z = !this.mDismissView.willBeGone();
        }
        return z;
    }

    public boolean isDismissViewVisible() {
        return this.mDismissView.isVisible();
    }

    public boolean isExpanded() {
        return this.mIsExpanded;
    }

    public boolean isInContentBounds(float f) {
        return f < ((float) (getHeight() - getEmptyBottomMargin()));
    }

    public boolean isScrolledToBottom() {
        return this.mOwnScrollY >= getScrollRange();
    }

    @Override // com.android.systemui.statusbar.policy.ScrollAdapter
    public boolean isScrolledToTop() {
        boolean z = false;
        if (this.mOwnScrollY == 0) {
            z = true;
        }
        return z;
    }

    @Override // com.android.systemui.statusbar.stack.ScrollContainer
    public void lockScrollTo(View view) {
        if (this.mForcedScroll == view) {
            return;
        }
        this.mForcedScroll = view;
        scrollTo(view);
    }

    public void notifyGroupChildAdded(View view) {
        onViewAddedInternal(view);
    }

    public void notifyGroupChildRemoved(View view, ViewGroup viewGroup) {
        onViewRemovedInternal(view, viewGroup);
    }

    @Override // android.view.View
    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        this.mBottomInset = windowInsets.getSystemWindowInsetBottom();
        if (this.mOwnScrollY > getScrollRange()) {
            removeCallbacks(this.mReclamp);
            postDelayed(this.mReclamp, 50L);
        } else if (this.mForcedScroll != null) {
            scrollTo(this.mForcedScroll);
        }
        return windowInsets;
    }

    @Override // com.android.systemui.SwipeHelper.Callback
    public void onBeginDrag(View view) {
        this.mFalsingManager.onNotificatonStartDismissing();
        setSwipingInProgress(true);
        this.mAmbientState.onBeginDrag(view);
        updateContinuousShadowDrawing();
        if (this.mAnimationsEnabled && (this.mIsExpanded || !isPinnedHeadsUp(view))) {
            this.mDragAnimPendingChildren.add(view);
            this.mNeedsAnimation = true;
        }
        requestChildrenUpdate();
    }

    public void onChildAnimationFinished() {
        setAnimationRunning(false);
        requestChildrenUpdate();
        runAnimationFinishedRunnables();
        clearViewOverlays();
        clearHeadsUpDisappearRunning();
    }

    @Override // com.android.systemui.SwipeHelper.Callback
    public void onChildDismissed(View view) {
        ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) view;
        if (!expandableNotificationRow.isDismissed()) {
            handleChildDismissed(view);
        }
        ViewGroup transientContainer = expandableNotificationRow.getTransientContainer();
        if (transientContainer != null) {
            transientContainer.removeTransientView(view);
        }
    }

    @Override // com.android.systemui.SwipeHelper.Callback
    public void onChildSnappedBack(View view, float f) {
        this.mAmbientState.onDragFinished(view);
        updateContinuousShadowDrawing();
        if (this.mDragAnimPendingChildren.contains(view)) {
            this.mDragAnimPendingChildren.remove(view);
        } else {
            if (this.mAnimationsEnabled) {
                this.mSnappedBackChildren.add(view);
                this.mNeedsAnimation = true;
            }
            requestChildrenUpdate();
        }
        if (this.mCurrIconRow == null || f != 0.0f) {
            return;
        }
        this.mCurrIconRow.resetState();
        this.mCurrIconRow = null;
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        this.mSwipeHelper.setDensityScale(getResources().getDisplayMetrics().density);
        this.mSwipeHelper.setPagingTouchSlop(ViewConfiguration.get(getContext()).getScaledPagingTouchSlop());
        initView(getContext());
    }

    @Override // com.android.systemui.SwipeHelper.Callback
    public void onDragCancelled(View view) {
        this.mFalsingManager.onNotificatonStopDismissing();
        setSwipingInProgress(false);
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        canvas.drawRect(0.0f, this.mCurrentBounds.top, getWidth(), this.mCurrentBounds.bottom, this.mBackgroundPaint);
    }

    public void onExpansionStarted() {
        this.mIsExpansionChanging = true;
    }

    public void onExpansionStopped() {
        this.mIsExpansionChanging = false;
        if (this.mIsExpanded) {
            return;
        }
        this.mOwnScrollY = 0;
        this.mPhoneStatusBar.resetUserExpandedStates();
        clearTemporaryViews(this);
        for (int i = 0; i < getChildCount(); i++) {
            ExpandableView expandableView = (ExpandableView) getChildAt(i);
            if (expandableView instanceof ExpandableNotificationRow) {
                clearTemporaryViews(((ExpandableNotificationRow) expandableView).getChildrenContainer());
            }
        }
    }

    @Override // com.android.systemui.statusbar.NotificationSettingsIconRow.SettingsIconRowListener
    public void onGearTouched(ExpandableNotificationRow expandableNotificationRow, int i, int i2) {
        if (this.mLongPressListener != null) {
            MetricsLogger.action(this.mContext, 333, expandableNotificationRow.getStatusBarNotification().getPackageName());
            this.mLongPressListener.onLongPress(expandableNotificationRow, i, i2);
        }
    }

    public void onGoToKeyguard() {
        requestAnimateEverything();
    }

    @Override // com.android.systemui.statusbar.phone.NotificationGroupManager.OnGroupChangeListener
    public void onGroupCreatedFromChildren(NotificationGroupManager.NotificationGroup notificationGroup) {
        this.mPhoneStatusBar.requestNotificationUpdate();
    }

    @Override // com.android.systemui.statusbar.phone.NotificationGroupManager.OnGroupChangeListener
    public void onGroupExpansionChanged(ExpandableNotificationRow expandableNotificationRow, boolean z) {
        boolean isPinned = (this.mGroupExpandedForMeasure || !this.mAnimationsEnabled) ? false : !this.mIsExpanded ? expandableNotificationRow.isPinned() : true;
        if (isPinned) {
            this.mExpandedGroupView = expandableNotificationRow;
            this.mNeedsAnimation = true;
        }
        expandableNotificationRow.setChildrenExpanded(z, isPinned);
        if (!this.mGroupExpandedForMeasure) {
            onHeightChanged(expandableNotificationRow, false);
        }
        runAfterAnimationFinished(new Runnable(this, expandableNotificationRow) { // from class: com.android.systemui.statusbar.stack.NotificationStackScrollLayout.14
            final NotificationStackScrollLayout this$0;
            final ExpandableNotificationRow val$changedRow;

            {
                this.this$0 = this;
                this.val$changedRow = expandableNotificationRow;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.val$changedRow.onFinishedExpansionChange();
            }
        });
    }

    @Override // com.android.systemui.statusbar.phone.NotificationGroupManager.OnGroupChangeListener
    public void onGroupsChanged() {
        this.mPhoneStatusBar.requestNotificationUpdate();
    }

    @Override // com.android.systemui.statusbar.ExpandableView.OnHeightChangedListener
    public void onHeightChanged(ExpandableView expandableView, boolean z) {
        updateContentHeight();
        updateScrollPositionOnExpandInBottom(expandableView);
        clampScrollPosition();
        notifyHeightChangeListener(expandableView);
        if (z) {
            requestAnimationOnViewResize(expandableView instanceof ExpandableNotificationRow ? (ExpandableNotificationRow) expandableView : null);
        }
        requestChildrenUpdate();
    }

    public void onInitializeAccessibilityEventInternal(AccessibilityEvent accessibilityEvent) {
        super.onInitializeAccessibilityEventInternal(accessibilityEvent);
        accessibilityEvent.setScrollable(this.mScrollable);
        accessibilityEvent.setScrollX(this.mScrollX);
        accessibilityEvent.setScrollY(this.mOwnScrollY);
        accessibilityEvent.setMaxScrollX(this.mScrollX);
        accessibilityEvent.setMaxScrollY(getScrollRange());
    }

    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfoInternal(accessibilityNodeInfo);
        int scrollRange = getScrollRange();
        if (scrollRange > 0) {
            accessibilityNodeInfo.setScrollable(true);
            if (this.mScrollY > 0) {
                accessibilityNodeInfo.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD);
                accessibilityNodeInfo.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_UP);
            }
            if (this.mScrollY < scrollRange) {
                accessibilityNodeInfo.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD);
                accessibilityNodeInfo.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_DOWN);
            }
        }
        accessibilityNodeInfo.setClassName(ScrollView.class.getName());
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        initDownStates(motionEvent);
        handleEmptySpaceClick(motionEvent);
        boolean z = false;
        if (!this.mSwipingInProgress) {
            z = this.mOnlyScrollingInThisMotion ? false : this.mExpandHelper.onInterceptTouchEvent(motionEvent);
        }
        boolean z2 = false;
        if (!this.mSwipingInProgress) {
            z2 = this.mExpandingNotification ? false : onInterceptTouchEventScroll(motionEvent);
        }
        boolean z3 = false;
        if (!this.mIsBeingDragged) {
            if (this.mExpandingNotification) {
                z3 = false;
            } else {
                z3 = false;
                if (!this.mExpandedInThisMotion) {
                    z3 = false;
                    if (!this.mOnlyScrollingInThisMotion) {
                        z3 = false;
                        if (!this.mDisallowDismissInThisMotion) {
                            z3 = this.mSwipeHelper.onInterceptTouchEvent(motionEvent);
                        }
                    }
                }
            }
        }
        return (z3 || z2 || z) ? true : super.onInterceptTouchEvent(motionEvent);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        float width = getWidth() / 2.0f;
        for (int i5 = 0; i5 < getChildCount(); i5++) {
            View childAt = getChildAt(i5);
            float measuredWidth = childAt.getMeasuredWidth();
            childAt.layout((int) (width - (measuredWidth / 2.0f)), 0, (int) ((measuredWidth / 2.0f) + width), childAt.getMeasuredHeight());
        }
        setMaxLayoutHeight(getHeight());
        updateContentHeight();
        clampScrollPosition();
        if (this.mRequestViewResizeAnimationOnLayout) {
            requestAnimationOnViewResize(null);
            this.mRequestViewResizeAnimationOnLayout = false;
        }
        requestChildrenUpdate();
        updateFirstAndLastBackgroundViews();
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        int childCount = getChildCount();
        for (int i3 = 0; i3 < childCount; i3++) {
            measureChild(getChildAt(i3), i, i2);
        }
    }

    @Override // android.view.View
    protected void onOverScrolled(int i, int i2, boolean z, boolean z2) {
        if (this.mScroller.isFinished()) {
            customScrollTo(i2);
            scrollTo(i, this.mScrollY);
            return;
        }
        int i3 = this.mScrollX;
        int i4 = this.mOwnScrollY;
        this.mScrollX = i;
        this.mOwnScrollY = i2;
        if (z2) {
            springBack();
            return;
        }
        onScrollChanged(this.mScrollX, this.mOwnScrollY, i3, i4);
        invalidateParentIfNeeded();
        updateChildren();
        float currentOverScrollAmount = getCurrentOverScrollAmount(true);
        if (this.mOwnScrollY < 0) {
            notifyOverscrollTopListener(-this.mOwnScrollY, isRubberbanded(true));
        } else {
            notifyOverscrollTopListener(currentOverScrollAmount, isRubberbanded(true));
        }
    }

    public void onPanelTrackingStarted() {
        this.mPanelTracking = true;
    }

    public void onPanelTrackingStopped() {
        this.mPanelTracking = false;
    }

    @Override // com.android.systemui.statusbar.ExpandableView.OnHeightChangedListener
    public void onReset(ExpandableView expandableView) {
        if (this.mIsExpanded && this.mAnimationsEnabled) {
            this.mRequestViewResizeAnimationOnLayout = true;
        }
        updateAnimationState(expandableView);
        updateChronometerForChild(expandableView);
    }

    @Override // com.android.systemui.statusbar.NotificationSettingsIconRow.SettingsIconRowListener
    public void onSettingsIconRowReset(ExpandableNotificationRow expandableNotificationRow) {
        if (this.mTranslatingParentView == null || expandableNotificationRow != this.mTranslatingParentView) {
            return;
        }
        this.mSwipeHelper.setSnappedToGear(false);
        this.mGearExposedView = null;
        this.mTranslatingParentView = null;
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        boolean z = motionEvent.getActionMasked() != 3 ? motionEvent.getActionMasked() == 1 : true;
        handleEmptySpaceClick(motionEvent);
        boolean z2 = false;
        if (this.mIsExpanded) {
            if (this.mSwipingInProgress) {
                z2 = false;
            } else {
                z2 = false;
                if (!this.mOnlyScrollingInThisMotion) {
                    if (z) {
                        this.mExpandHelper.onlyObserveMovements(false);
                    }
                    boolean z3 = this.mExpandingNotification;
                    boolean onTouchEvent = this.mExpandHelper.onTouchEvent(motionEvent);
                    z2 = onTouchEvent;
                    if (this.mExpandedInThisMotion) {
                        z2 = onTouchEvent;
                        if (!this.mExpandingNotification) {
                            z2 = onTouchEvent;
                            if (z3) {
                                z2 = onTouchEvent;
                                if (!this.mDisallowScrollingInThisMotion) {
                                    dispatchDownEventToScroller(motionEvent);
                                    z2 = onTouchEvent;
                                }
                            }
                        }
                    }
                }
            }
        }
        boolean z4 = false;
        if (this.mIsExpanded) {
            if (this.mSwipingInProgress) {
                z4 = false;
            } else {
                z4 = false;
                if (!this.mExpandingNotification) {
                    z4 = false;
                    if (!this.mDisallowScrollingInThisMotion) {
                        z4 = onScrollTouch(motionEvent);
                    }
                }
            }
        }
        boolean z5 = false;
        if (!this.mIsBeingDragged) {
            if (this.mExpandingNotification) {
                z5 = false;
            } else {
                z5 = false;
                if (!this.mExpandedInThisMotion) {
                    z5 = false;
                    if (!this.mOnlyScrollingInThisMotion) {
                        z5 = false;
                        if (!this.mDisallowDismissInThisMotion) {
                            z5 = this.mSwipeHelper.onTouchEvent(motionEvent);
                        }
                    }
                }
            }
        }
        boolean z6 = true;
        if (!z5) {
            z6 = true;
            if (!z4) {
                z6 = true;
                if (!z2) {
                    z6 = super.onTouchEvent(motionEvent);
                }
            }
        }
        return z6;
    }

    @Override // android.view.ViewGroup
    public void onViewAdded(View view) {
        super.onViewAdded(view);
        onViewAddedInternal(view);
    }

    @Override // android.view.ViewGroup
    public void onViewRemoved(View view) {
        super.onViewRemoved(view);
        if (this.mChildTransferInProgress) {
            return;
        }
        onViewRemovedInternal(view, this);
    }

    @Override // android.view.View
    public void onWindowFocusChanged(boolean z) {
        super.onWindowFocusChanged(z);
        if (z) {
            return;
        }
        removeLongPressCallback();
    }

    @Override // android.view.View
    protected boolean overScrollBy(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, boolean z) {
        int i9 = i4 + i2;
        int i10 = -i8;
        int i11 = i8 + i6;
        boolean z2 = false;
        if (i9 > i11) {
            z2 = true;
        } else {
            i11 = i9;
            if (i9 < i10) {
                i11 = i10;
                z2 = true;
            }
        }
        onOverScrolled(0, i11, false, z2);
        return z2;
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    public boolean performAccessibilityActionInternal(int i, Bundle bundle) {
        if (super.performAccessibilityActionInternal(i, bundle)) {
            return true;
        }
        if (isEnabled()) {
            int i2 = -1;
            switch (i) {
                case 4096:
                case 16908346:
                    i2 = 1;
                    break;
                case 8192:
                case 16908344:
                    break;
                default:
                    return false;
            }
            int height = getHeight();
            int i3 = this.mPaddingBottom;
            int i4 = this.mTopPadding;
            int i5 = this.mPaddingTop;
            int max = Math.max(0, Math.min(this.mOwnScrollY + (i2 * (((((height - i3) - i4) - i5) - this.mBottomStackPeekSize) - this.mBottomStackSlowDownHeight)), getScrollRange()));
            if (max != this.mOwnScrollY) {
                this.mScroller.startScroll(this.mScrollX, this.mOwnScrollY, 0, max - this.mOwnScrollY);
                postInvalidateOnAnimation();
                return true;
            }
            return false;
        }
        return false;
    }

    public void removeLongPressCallback() {
        this.mSwipeHelper.removeLongPressCallback();
    }

    public void removeViewStateForView(View view) {
        this.mCurrentStackScrollState.removeViewStateForView(view);
    }

    @Override // com.android.systemui.statusbar.stack.ScrollContainer
    public void requestDisallowDismiss() {
        this.mDisallowDismissInThisMotion = true;
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public void requestDisallowInterceptTouchEvent(boolean z) {
        super.requestDisallowInterceptTouchEvent(z);
        if (z) {
            this.mSwipeHelper.removeLongPressCallback();
        }
    }

    @Override // com.android.systemui.statusbar.stack.ScrollContainer
    public void requestDisallowLongPress() {
        removeLongPressCallback();
    }

    public void resetExposedGearView(boolean z, boolean z2) {
        this.mSwipeHelper.resetExposedGearView(z, z2);
    }

    public void resetScrollPosition() {
        this.mScroller.abortAnimation();
        this.mOwnScrollY = 0;
    }

    public void runAfterAnimationFinished(Runnable runnable) {
        this.mAnimationFinishedRunnables.add(runnable);
    }

    public boolean scrollTo(View view) {
        ExpandableView expandableView = (ExpandableView) view;
        int positionInLinearLayout = getPositionInLinearLayout(view);
        int targetScrollForView = targetScrollForView(expandableView, positionInLinearLayout);
        int intrinsicHeight = expandableView.getIntrinsicHeight();
        if (this.mOwnScrollY < targetScrollForView || positionInLinearLayout + intrinsicHeight < this.mOwnScrollY) {
            this.mScroller.startScroll(this.mScrollX, this.mOwnScrollY, 0, targetScrollForView - this.mOwnScrollY);
            this.mDontReportNextOverScroll = true;
            postInvalidateOnAnimation();
            return true;
        }
        return false;
    }

    public void setActivatedChild(ActivatableNotificationView activatableNotificationView) {
        this.mAmbientState.setActivatedChild(activatableNotificationView);
        if (this.mAnimationsEnabled) {
            this.mActivateNeedsAnimation = true;
            this.mNeedsAnimation = true;
        }
        requestChildrenUpdate();
    }

    @Override // android.view.View
    public void setAlpha(float f) {
        super.setAlpha(f);
        setFadingOut(f != 1.0f);
    }

    public void setAnimationRunning(boolean z) {
        if (z != this.mAnimationRunning) {
            if (z) {
                getViewTreeObserver().addOnPreDrawListener(this.mBackgroundUpdater);
            } else {
                getViewTreeObserver().removeOnPreDrawListener(this.mBackgroundUpdater);
            }
            this.mAnimationRunning = z;
            updateContinuousShadowDrawing();
        }
    }

    public void setAnimationsEnabled(boolean z) {
        this.mAnimationsEnabled = z;
        updateNotificationAnimationStates();
    }

    public void setBackgroundBottom(int i) {
        this.mCurrentBounds.bottom = i;
        applyCurrentBackgroundBounds();
    }

    public void setChildLocationsChangedListener(OnChildLocationsChangedListener onChildLocationsChangedListener) {
        this.mListener = onChildLocationsChangedListener;
    }

    public void setChildTransferInProgress(boolean z) {
        this.mChildTransferInProgress = z;
    }

    public void setDark(boolean z, boolean z2, PointF pointF) {
        this.mAmbientState.setDark(z);
        if (z2 && this.mAnimationsEnabled) {
            this.mDarkNeedsAnimation = true;
            this.mDarkAnimationOriginIndex = findDarkAnimationOriginIndex(pointF);
            this.mNeedsAnimation = true;
            setBackgroundFadeAmount(0.0f);
        } else if (!z) {
            setBackgroundFadeAmount(1.0f);
        }
        requestChildrenUpdate();
        if (z) {
            setWillNotDraw(true);
            this.mScrimController.setExcludedBackgroundArea(null);
            return;
        }
        updateBackground();
        setWillNotDraw(false);
    }

    public void setDimmed(boolean z, boolean z2) {
        this.mAmbientState.setDimmed(z);
        if (z2 && this.mAnimationsEnabled) {
            this.mDimmedNeedsAnimation = true;
            this.mNeedsAnimation = true;
            animateDimmed(z);
        } else {
            setDimAmount(z ? 1.0f : 0.0f);
        }
        requestChildrenUpdate();
    }

    public void setDismissAllInProgress(boolean z) {
        this.mDismissAllInProgress = z;
        this.mAmbientState.setDismissAllInProgress(z);
        handleDismissAllClipping();
    }

    public void setDismissView(DismissView dismissView) {
        int i = -1;
        if (this.mDismissView != null) {
            i = indexOfChild(this.mDismissView);
            removeView(this.mDismissView);
        }
        this.mDismissView = dismissView;
        addView(this.mDismissView, i);
    }

    public void setDrawBackgroundAsSrc(boolean z) {
        this.mDrawBackgroundAsSrc = z;
        updateSrcDrawing();
    }

    public void setEmptyShadeView(EmptyShadeView emptyShadeView) {
        int i = -1;
        if (this.mEmptyShadeView != null) {
            i = indexOfChild(this.mEmptyShadeView);
            removeView(this.mEmptyShadeView);
        }
        this.mEmptyShadeView = emptyShadeView;
        addView(this.mEmptyShadeView, i);
    }

    public void setExpandingEnabled(boolean z) {
        this.mExpandHelper.setEnabled(z);
    }

    @Override // com.android.systemui.ExpandHelper.Callback
    public void setExpansionCancelled(View view) {
        if (view instanceof ExpandableNotificationRow) {
            ((ExpandableNotificationRow) view).setGroupExpansionChanging(false);
        }
    }

    public void setFadingOut(boolean z) {
        if (z != this.mFadingOut) {
            this.mFadingOut = z;
            updateFadingState();
        }
    }

    public void setFinishScrollingCallback(Runnable runnable) {
        this.mFinishScrollingCallback = runnable;
    }

    public void setGroupManager(NotificationGroupManager notificationGroupManager) {
        this.mGroupManager = notificationGroupManager;
    }

    public void setHeadsUpBoundaries(int i, int i2) {
        this.mAmbientState.setMaxHeadsUpTranslation(i - i2);
        this.mStateAnimator.setHeadsUpAppearHeightBottom(i);
        requestChildrenUpdate();
    }

    public void setHeadsUpManager(HeadsUpManager headsUpManager) {
        this.mHeadsUpManager = headsUpManager;
        this.mAmbientState.setHeadsUpManager(headsUpManager);
    }

    public void setHideSensitive(boolean z, boolean z2) {
        if (z != this.mAmbientState.isHideSensitive()) {
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                ((ExpandableView) getChildAt(i)).setHideSensitiveForIntrinsicHeight(z);
            }
            this.mAmbientState.setHideSensitive(z);
            if (z2 && this.mAnimationsEnabled) {
                this.mHideSensitiveNeedsAnimation = true;
                this.mNeedsAnimation = true;
            }
            requestChildrenUpdate();
        }
    }

    public void setIntrinsicPadding(int i) {
        this.mIntrinsicPadding = i;
    }

    public void setLongPressListener(SwipeHelper.LongPressListener longPressListener) {
        this.mSwipeHelper.setLongPressListener(longPressListener);
        this.mLongPressListener = longPressListener;
    }

    public void setOnEmptySpaceClickListener(OnEmptySpaceClickListener onEmptySpaceClickListener) {
        this.mOnEmptySpaceClickListener = onEmptySpaceClickListener;
    }

    public void setOnHeightChangedListener(ExpandableView.OnHeightChangedListener onHeightChangedListener) {
        this.mOnHeightChangedListener = onHeightChangedListener;
    }

    public void setOverScrollAmount(float f, boolean z, boolean z2) {
        setOverScrollAmount(f, z, z2, true);
    }

    public void setOverScrollAmount(float f, boolean z, boolean z2, boolean z3) {
        setOverScrollAmount(f, z, z2, z3, isRubberbanded(z));
    }

    public void setOverScrollAmount(float f, boolean z, boolean z2, boolean z3, boolean z4) {
        if (z3) {
            this.mStateAnimator.cancelOverScrollAnimators(z);
        }
        setOverScrollAmountInternal(f, z, z2, z4);
    }

    public void setOverScrolledPixels(float f, boolean z, boolean z2) {
        setOverScrollAmount(getRubberBandFactor(z) * f, z, z2, true);
    }

    public void setOverflowContainer(NotificationOverflowContainer notificationOverflowContainer) {
        int i = -1;
        if (this.mOverflowContainer != null) {
            i = indexOfChild(this.mOverflowContainer);
            removeView(this.mOverflowContainer);
        }
        this.mOverflowContainer = notificationOverflowContainer;
        addView(this.mOverflowContainer, i);
    }

    public void setOverscrollTopChangedListener(OnOverscrollTopChangedListener onOverscrollTopChangedListener) {
        this.mOverscrollTopChangedListener = onOverscrollTopChangedListener;
    }

    public void setParentFadingOut(boolean z) {
        if (z != this.mParentFadingOut) {
            this.mParentFadingOut = z;
            updateFadingState();
        }
    }

    public void setPhoneStatusBar(PhoneStatusBar phoneStatusBar) {
        this.mPhoneStatusBar = phoneStatusBar;
    }

    public void setPulsing(boolean z) {
        this.mPulsing = z;
        updateNotificationAnimationStates();
    }

    public void setQsContainer(ViewGroup viewGroup) {
        this.mQsContainer = viewGroup;
    }

    public void setScrimController(ScrimController scrimController) {
        this.mScrimController = scrimController;
        this.mScrimController.setScrimBehindChangeRunnable(new Runnable(this) { // from class: com.android.systemui.statusbar.stack.NotificationStackScrollLayout.15
            final NotificationStackScrollLayout this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.updateBackgroundDimming();
            }
        });
    }

    public void setScrollingEnabled(boolean z) {
        this.mScrollingEnabled = z;
    }

    public void setShadeExpanded(boolean z) {
        this.mAmbientState.setShadeExpanded(z);
        this.mStateAnimator.setShadeExpanded(z);
    }

    public void setStackHeight(float f) {
        float f2;
        int i;
        int i2;
        boolean z = false;
        this.mLastSetStackHeight = f;
        if (f > 0.0f) {
            z = true;
        }
        setIsExpanded(z);
        int i3 = (int) f;
        int layoutMinHeight = getLayoutMinHeight();
        if (!this.mTrackingHeadsUp ? this.mHeadsUpManager.hasPinnedHeadsUp() : true) {
            layoutMinHeight = this.mHeadsUpManager.getTopHeadsUpPinnedHeight();
        }
        if ((i3 - this.mTopPadding) - this.mTopPaddingOverflow >= layoutMinHeight || getNotGoneChildCount() == 0) {
            f2 = this.mTopPaddingOverflow;
            i = i3;
        } else {
            i = (int) (f - (i2 - this.mTopPadding));
            f2 = (i3 - layoutMinHeight) - this.mTopPadding;
        }
        if (i != this.mCurrentStackHeight) {
            this.mCurrentStackHeight = i;
            updateAlgorithmHeightAndPadding();
            requestChildrenUpdate();
        }
        setStackTranslation(f2);
    }

    public void setTrackingHeadsUp(boolean z) {
        this.mTrackingHeadsUp = z;
    }

    @Override // com.android.systemui.ExpandHelper.Callback
    public void setUserExpandedChild(View view, boolean z) {
        if (view instanceof ExpandableNotificationRow) {
            ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) view;
            expandableNotificationRow.setUserExpanded(z, true);
            expandableNotificationRow.onExpandedByGesture(z);
        }
    }

    @Override // com.android.systemui.ExpandHelper.Callback
    public void setUserLockedChild(View view, boolean z) {
        if (view instanceof ExpandableNotificationRow) {
            ((ExpandableNotificationRow) view).setUserLocked(z);
        }
        removeLongPressCallback();
        requestDisallowInterceptTouchEvent(true);
    }

    @Override // android.view.ViewGroup
    public boolean shouldDelayChildPressedState() {
        return true;
    }

    public void snapViewIfNeeded(ExpandableNotificationRow expandableNotificationRow) {
        this.mSwipeHelper.snapChildIfNeeded(expandableNotificationRow, !this.mIsExpanded ? isPinnedHeadsUp(expandableNotificationRow) : true, expandableNotificationRow.getSettingsRow().isVisible() ? expandableNotificationRow.getTranslation() : 0.0f);
    }

    public void updateDismissView(boolean z) {
        int visibility = this.mDismissView.willBeGone() ? 8 : this.mDismissView.getVisibility();
        int i = z ? 0 : 8;
        if (visibility != i) {
            if (i != 8) {
                if (this.mDismissView.willBeGone()) {
                    this.mDismissView.cancelAnimation();
                } else {
                    this.mDismissView.setInvisible();
                }
                this.mDismissView.setVisibility(i);
                this.mDismissView.setWillBeGone(false);
                updateContentHeight();
                notifyHeightChangeListener(this.mDismissView);
                return;
            }
            Runnable runnable = new Runnable(this) { // from class: com.android.systemui.statusbar.stack.NotificationStackScrollLayout.13
                final NotificationStackScrollLayout this$0;

                {
                    this.this$0 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$0.mDismissView.setVisibility(8);
                    this.this$0.mDismissView.setWillBeGone(false);
                    this.this$0.updateContentHeight();
                    this.this$0.notifyHeightChangeListener(this.this$0.mDismissView);
                }
            };
            if (!this.mDismissView.isButtonVisible() || !this.mIsExpanded || !this.mAnimationsEnabled) {
                runnable.run();
                return;
            }
            this.mDismissView.setWillBeGone(true);
            this.mDismissView.performVisibilityAnimation(false, runnable);
        }
    }

    public void updateEmptyShadeView(boolean z) {
        int visibility = this.mEmptyShadeView.willBeGone() ? 8 : this.mEmptyShadeView.getVisibility();
        int i = z ? 0 : 8;
        if (visibility != i) {
            if (i != 8) {
                if (this.mEmptyShadeView.willBeGone()) {
                    this.mEmptyShadeView.cancelAnimation();
                } else {
                    this.mEmptyShadeView.setInvisible();
                }
                this.mEmptyShadeView.setVisibility(i);
                this.mEmptyShadeView.setWillBeGone(false);
                updateContentHeight();
                notifyHeightChangeListener(this.mEmptyShadeView);
                return;
            }
            Runnable runnable = new Runnable(this) { // from class: com.android.systemui.statusbar.stack.NotificationStackScrollLayout.11
                final NotificationStackScrollLayout this$0;

                {
                    this.this$0 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$0.mEmptyShadeView.setVisibility(8);
                    this.this$0.mEmptyShadeView.setWillBeGone(false);
                    this.this$0.updateContentHeight();
                    this.this$0.notifyHeightChangeListener(this.this$0.mEmptyShadeView);
                }
            };
            if (this.mAnimationsEnabled) {
                this.mEmptyShadeView.setWillBeGone(true);
                this.mEmptyShadeView.performVisibilityAnimation(false, runnable);
                return;
            }
            this.mEmptyShadeView.setInvisible();
            runnable.run();
        }
    }

    public void updateOverflowContainerVisibility(boolean z) {
        int visibility = this.mOverflowContainer.willBeGone() ? 8 : this.mOverflowContainer.getVisibility();
        int i = z ? 0 : 8;
        if (visibility != i) {
            Runnable runnable = new Runnable(this, i) { // from class: com.android.systemui.statusbar.stack.NotificationStackScrollLayout.12
                final NotificationStackScrollLayout this$0;
                final int val$newVisibility;

                {
                    this.this$0 = this;
                    this.val$newVisibility = i;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$0.mOverflowContainer.setVisibility(this.val$newVisibility);
                    this.this$0.mOverflowContainer.setWillBeGone(false);
                    this.this$0.updateContentHeight();
                    this.this$0.notifyHeightChangeListener(this.this$0.mOverflowContainer);
                }
            };
            if (!this.mAnimationsEnabled || !this.mIsExpanded) {
                this.mOverflowContainer.cancelAppearDrawing();
                runnable.run();
            } else if (i == 8) {
                this.mOverflowContainer.performRemoveAnimation(360L, 0.0f, runnable);
                this.mOverflowContainer.setWillBeGone(true);
            } else {
                this.mOverflowContainer.performAddAnimation(0L, 360L);
                this.mOverflowContainer.setVisibility(i);
                this.mOverflowContainer.setWillBeGone(false);
                updateContentHeight();
                notifyHeightChangeListener(this.mOverflowContainer);
            }
        }
    }

    public void updateSpeedBumpIndex(int i) {
        this.mAmbientState.setSpeedBumpIndex(i);
    }

    @Override // com.android.systemui.SwipeHelper.Callback
    public boolean updateSwipeProgress(View view, boolean z, float f) {
        if (!this.mIsExpanded && isPinnedHeadsUp(view) && canChildBeDismissed(view)) {
            this.mScrimController.setTopHeadsUpDragAmount(view, Math.min(Math.abs((f / 2.0f) - 1.0f), 1.0f));
            return true;
        }
        return true;
    }

    public void updateTopPadding(float f, boolean z, boolean z2) {
        float f2 = f;
        float height = getHeight() - f;
        int layoutMinHeight = getLayoutMinHeight();
        if (height <= layoutMinHeight) {
            f2 = getHeight() - layoutMinHeight;
            this.mTopPaddingOverflow = layoutMinHeight - height;
        } else {
            this.mTopPaddingOverflow = 0.0f;
        }
        setTopPadding(z2 ? (int) f2 : clampPadding((int) f2), z);
        setStackHeight(this.mLastSetStackHeight);
    }
}

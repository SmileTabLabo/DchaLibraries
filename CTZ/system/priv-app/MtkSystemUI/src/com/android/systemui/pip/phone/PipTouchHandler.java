package com.android.systemui.pip.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.IActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.util.Size;
import android.view.IPinnedStackController;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import com.android.internal.os.logging.MetricsLoggerWrapper;
import com.android.internal.policy.PipSnapAlgorithm;
import com.android.systemui.R;
import com.android.systemui.pip.phone.PipAccessibilityInteractionConnection;
import com.android.systemui.pip.phone.PipMenuActivityController;
import com.android.systemui.shared.system.InputConsumerController;
import com.android.systemui.statusbar.FlingAnimationUtils;
import java.io.PrintWriter;
/* loaded from: classes.dex */
public class PipTouchHandler {
    private final AccessibilityManager mAccessibilityManager;
    private final IActivityManager mActivityManager;
    private final Context mContext;
    private final PipDismissViewController mDismissViewController;
    private int mDisplayRotation;
    private int mExpandedShortestEdgeSize;
    private final FlingAnimationUtils mFlingAnimationUtils;
    private final PipTouchGesture[] mGestures;
    private int mImeHeight;
    private int mImeOffset;
    private boolean mIsImeShowing;
    private boolean mIsMinimized;
    private boolean mIsShelfShowing;
    private final PipMenuActivityController mMenuController;
    private final PipMotionHelper mMotionHelper;
    private boolean mMovementWithinDismiss;
    private boolean mMovementWithinMinimize;
    private IPinnedStackController mPinnedStackController;
    private boolean mSendingHoverAccessibilityEvents;
    private int mShelfHeight;
    private final PipSnapAlgorithm mSnapAlgorithm;
    private final PipTouchState mTouchState;
    private final ViewConfiguration mViewConfig;
    private final PipMenuListener mMenuListener = new PipMenuListener();
    private boolean mShowPipMenuOnAnimationEnd = false;
    private Rect mMovementBounds = new Rect();
    private Rect mInsetBounds = new Rect();
    private Rect mNormalBounds = new Rect();
    private Rect mNormalMovementBounds = new Rect();
    private Rect mExpandedBounds = new Rect();
    private Rect mExpandedMovementBounds = new Rect();
    private int mDeferResizeToNormalBoundsUntilRotation = -1;
    private Handler mHandler = new Handler();
    private Runnable mShowDismissAffordance = new Runnable() { // from class: com.android.systemui.pip.phone.PipTouchHandler.1
        @Override // java.lang.Runnable
        public void run() {
            PipTouchHandler.this.mDismissViewController.showDismissTarget();
        }
    };
    private ValueAnimator.AnimatorUpdateListener mUpdateScrimListener = new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.pip.phone.PipTouchHandler.2
        @Override // android.animation.ValueAnimator.AnimatorUpdateListener
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            PipTouchHandler.this.updateDismissFraction();
        }
    };
    private int mMenuState = 0;
    private float mSavedSnapFraction = -1.0f;
    private final Rect mTmpBounds = new Rect();
    private PipTouchGesture mDefaultMovementGesture = new PipTouchGesture() { // from class: com.android.systemui.pip.phone.PipTouchHandler.3
        private boolean mStartedOnLeft;
        private final Point mStartPosition = new Point();
        private final PointF mDelta = new PointF();

        @Override // com.android.systemui.pip.phone.PipTouchGesture
        public void onDown(PipTouchState pipTouchState) {
            if (pipTouchState.isUserInteracting()) {
                Rect bounds = PipTouchHandler.this.mMotionHelper.getBounds();
                this.mDelta.set(0.0f, 0.0f);
                this.mStartPosition.set(bounds.left, bounds.top);
                this.mStartedOnLeft = bounds.left < PipTouchHandler.this.mMovementBounds.centerX();
                PipTouchHandler.this.mMovementWithinMinimize = true;
                PipTouchHandler.this.mMovementWithinDismiss = pipTouchState.getDownTouchPosition().y >= ((float) PipTouchHandler.this.mMovementBounds.bottom);
                if (PipTouchHandler.this.mMenuState != 0 && !PipTouchHandler.this.mIsMinimized) {
                    PipTouchHandler.this.mMenuController.pokeMenu();
                }
                PipTouchHandler.this.mDismissViewController.createDismissTarget();
                PipTouchHandler.this.mHandler.postDelayed(PipTouchHandler.this.mShowDismissAffordance, 225L);
            }
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        @Override // com.android.systemui.pip.phone.PipTouchGesture
        public boolean onMove(PipTouchState pipTouchState) {
            if (pipTouchState.isUserInteracting()) {
                if (pipTouchState.startedDragging()) {
                    PipTouchHandler.this.mSavedSnapFraction = -1.0f;
                    PipTouchHandler.this.mHandler.removeCallbacks(PipTouchHandler.this.mShowDismissAffordance);
                    PipTouchHandler.this.mDismissViewController.showDismissTarget();
                }
                if (pipTouchState.isDragging()) {
                    PointF lastTouchDelta = pipTouchState.getLastTouchDelta();
                    float f = this.mStartPosition.x + this.mDelta.x;
                    float f2 = this.mStartPosition.y + this.mDelta.y;
                    pipTouchState.allowDraggingOffscreen();
                    float max = Math.max(PipTouchHandler.this.mMovementBounds.left, Math.min(PipTouchHandler.this.mMovementBounds.right, lastTouchDelta.x + f));
                    float max2 = Math.max(PipTouchHandler.this.mMovementBounds.top, lastTouchDelta.y + f2);
                    this.mDelta.x += max - f;
                    this.mDelta.y += max2 - f2;
                    PipTouchHandler.this.mTmpBounds.set(PipTouchHandler.this.mMotionHelper.getBounds());
                    PipTouchHandler.this.mTmpBounds.offsetTo((int) max, (int) max2);
                    PipTouchHandler.this.mMotionHelper.movePip(PipTouchHandler.this.mTmpBounds);
                    PipTouchHandler.this.updateDismissFraction();
                    PointF lastTouchPosition = pipTouchState.getLastTouchPosition();
                    if (PipTouchHandler.this.mMovementWithinMinimize) {
                        PipTouchHandler.this.mMovementWithinMinimize = !this.mStartedOnLeft ? lastTouchPosition.x < ((float) PipTouchHandler.this.mMovementBounds.right) : lastTouchPosition.x > ((float) (PipTouchHandler.this.mMovementBounds.left + PipTouchHandler.this.mTmpBounds.width()));
                    }
                    if (PipTouchHandler.this.mMovementWithinDismiss) {
                        PipTouchHandler.this.mMovementWithinDismiss = lastTouchPosition.y >= ((float) PipTouchHandler.this.mMovementBounds.bottom);
                    }
                    return true;
                }
                return false;
            }
            return false;
        }

        @Override // com.android.systemui.pip.phone.PipTouchGesture
        public boolean onUp(PipTouchState pipTouchState) {
            AnimatorListenerAdapter animatorListenerAdapter;
            PipTouchHandler.this.cleanUpDismissTarget();
            if (pipTouchState.isUserInteracting()) {
                PointF velocity = pipTouchState.getVelocity();
                boolean z = Math.abs(velocity.x) > Math.abs(velocity.y);
                float length = PointF.length(velocity.x, velocity.y);
                boolean z2 = length > PipTouchHandler.this.mFlingAnimationUtils.getMinVelocityPxPerSecond();
                boolean z3 = z2 && velocity.y > 0.0f && !z && PipTouchHandler.this.mMovementWithinDismiss;
                if (PipTouchHandler.this.mMotionHelper.shouldDismissPip() || z3) {
                    MetricsLoggerWrapper.logPictureInPictureDismissByDrag(PipTouchHandler.this.mContext, PipUtils.getTopPinnedActivity(PipTouchHandler.this.mContext, PipTouchHandler.this.mActivityManager));
                    PipTouchHandler.this.mMotionHelper.animateDismiss(PipTouchHandler.this.mMotionHelper.getBounds(), velocity.x, velocity.y, PipTouchHandler.this.mUpdateScrimListener);
                    return true;
                }
                if (!pipTouchState.isDragging()) {
                    if (PipTouchHandler.this.mIsMinimized) {
                        PipTouchHandler.this.mMotionHelper.animateToClosestSnapTarget(PipTouchHandler.this.mMovementBounds, null, null);
                        PipTouchHandler.this.setMinimizedStateInternal(false);
                    } else if (PipTouchHandler.this.mMenuState != 2) {
                        if (PipTouchHandler.this.mTouchState.isDoubleTap()) {
                            PipTouchHandler.this.mMotionHelper.expandPip();
                        } else if (PipTouchHandler.this.mTouchState.isWaitingForDoubleTap()) {
                            PipTouchHandler.this.mTouchState.scheduleDoubleTapTimeoutCallback();
                        } else {
                            PipTouchHandler.this.mMenuController.showMenu(2, PipTouchHandler.this.mMotionHelper.getBounds(), PipTouchHandler.this.mMovementBounds, true, PipTouchHandler.this.willResizeMenu());
                        }
                    } else {
                        PipTouchHandler.this.mMenuController.hideMenu();
                        PipTouchHandler.this.mMotionHelper.expandPip();
                    }
                } else {
                    if (z2 && z && PipTouchHandler.this.mMovementWithinMinimize) {
                        if (this.mStartedOnLeft) {
                            int i = (velocity.x > 0.0f ? 1 : (velocity.x == 0.0f ? 0 : -1));
                        } else {
                            int i2 = (velocity.x > 0.0f ? 1 : (velocity.x == 0.0f ? 0 : -1));
                        }
                    }
                    if (PipTouchHandler.this.mIsMinimized) {
                        PipTouchHandler.this.setMinimizedStateInternal(false);
                    }
                    if (PipTouchHandler.this.mMenuState != 0) {
                        PipTouchHandler.this.mMenuController.showMenu(PipTouchHandler.this.mMenuState, PipTouchHandler.this.mMotionHelper.getBounds(), PipTouchHandler.this.mMovementBounds, true, PipTouchHandler.this.willResizeMenu());
                        animatorListenerAdapter = null;
                    } else {
                        animatorListenerAdapter = new AnimatorListenerAdapter() { // from class: com.android.systemui.pip.phone.PipTouchHandler.3.1
                            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                            public void onAnimationEnd(Animator animator) {
                                PipTouchHandler.this.mMenuController.hideMenu();
                            }
                        };
                    }
                    if (!z2) {
                        PipTouchHandler.this.mMotionHelper.animateToClosestSnapTarget(PipTouchHandler.this.mMovementBounds, PipTouchHandler.this.mUpdateScrimListener, animatorListenerAdapter);
                    } else {
                        PipTouchHandler.this.mMotionHelper.flingToSnapTarget(length, velocity.x, velocity.y, PipTouchHandler.this.mMovementBounds, PipTouchHandler.this.mUpdateScrimListener, animatorListenerAdapter, this.mStartPosition);
                    }
                }
                return true;
            }
            return false;
        }
    };

    /* loaded from: classes.dex */
    private class PipMenuListener implements PipMenuActivityController.Listener {
        private PipMenuListener() {
        }

        @Override // com.android.systemui.pip.phone.PipMenuActivityController.Listener
        public void onPipMenuStateChanged(int i, boolean z) {
            PipTouchHandler.this.setMenuState(i, z);
        }

        @Override // com.android.systemui.pip.phone.PipMenuActivityController.Listener
        public void onPipExpand() {
            if (!PipTouchHandler.this.mIsMinimized) {
                PipTouchHandler.this.mMotionHelper.expandPip();
            }
        }

        @Override // com.android.systemui.pip.phone.PipMenuActivityController.Listener
        public void onPipMinimize() {
            PipTouchHandler.this.setMinimizedStateInternal(true);
            PipTouchHandler.this.mMotionHelper.animateToClosestMinimizedState(PipTouchHandler.this.mMovementBounds, null);
        }

        @Override // com.android.systemui.pip.phone.PipMenuActivityController.Listener
        public void onPipDismiss() {
            MetricsLoggerWrapper.logPictureInPictureDismissByTap(PipTouchHandler.this.mContext, PipUtils.getTopPinnedActivity(PipTouchHandler.this.mContext, PipTouchHandler.this.mActivityManager));
            PipTouchHandler.this.mMotionHelper.dismissPip();
        }

        @Override // com.android.systemui.pip.phone.PipMenuActivityController.Listener
        public void onPipShowMenu() {
            PipTouchHandler.this.mMenuController.showMenu(2, PipTouchHandler.this.mMotionHelper.getBounds(), PipTouchHandler.this.mMovementBounds, true, PipTouchHandler.this.willResizeMenu());
        }
    }

    public PipTouchHandler(Context context, IActivityManager iActivityManager, PipMenuActivityController pipMenuActivityController, InputConsumerController inputConsumerController) {
        this.mContext = context;
        this.mActivityManager = iActivityManager;
        this.mAccessibilityManager = (AccessibilityManager) context.getSystemService(AccessibilityManager.class);
        this.mViewConfig = ViewConfiguration.get(context);
        this.mMenuController = pipMenuActivityController;
        this.mMenuController.addListener(this.mMenuListener);
        this.mDismissViewController = new PipDismissViewController(context);
        this.mSnapAlgorithm = new PipSnapAlgorithm(this.mContext);
        this.mFlingAnimationUtils = new FlingAnimationUtils(context, 2.5f);
        this.mGestures = new PipTouchGesture[]{this.mDefaultMovementGesture};
        this.mMotionHelper = new PipMotionHelper(this.mContext, this.mActivityManager, this.mMenuController, this.mSnapAlgorithm, this.mFlingAnimationUtils);
        this.mTouchState = new PipTouchState(this.mViewConfig, this.mHandler, new Runnable() { // from class: com.android.systemui.pip.phone.-$$Lambda$PipTouchHandler$Uq5M9Md512Sfgd22VAeFpot25E0
            @Override // java.lang.Runnable
            public final void run() {
                r0.mMenuController.showMenu(2, r0.mMotionHelper.getBounds(), r0.mMovementBounds, true, PipTouchHandler.this.willResizeMenu());
            }
        });
        Resources resources = context.getResources();
        this.mExpandedShortestEdgeSize = resources.getDimensionPixelSize(R.dimen.pip_expanded_shortest_edge_size);
        this.mImeOffset = resources.getDimensionPixelSize(R.dimen.pip_ime_offset);
        inputConsumerController.setTouchListener(new InputConsumerController.TouchListener() { // from class: com.android.systemui.pip.phone.-$$Lambda$PipTouchHandler$6VeR24MB4-xnOTUaDMT0CXNQVjE
            @Override // com.android.systemui.shared.system.InputConsumerController.TouchListener
            public final boolean onTouchEvent(MotionEvent motionEvent) {
                boolean handleTouchEvent;
                handleTouchEvent = PipTouchHandler.this.handleTouchEvent(motionEvent);
                return handleTouchEvent;
            }
        });
        inputConsumerController.setRegistrationListener(new InputConsumerController.RegistrationListener() { // from class: com.android.systemui.pip.phone.-$$Lambda$PipTouchHandler$NVpciZTELe-GnxXPZeY5rYMmqJQ
            @Override // com.android.systemui.shared.system.InputConsumerController.RegistrationListener
            public final void onRegistrationChanged(boolean z) {
                PipTouchHandler.this.onRegistrationChanged(z);
            }
        });
        onRegistrationChanged(inputConsumerController.isRegistered());
    }

    public void setTouchEnabled(boolean z) {
        this.mTouchState.setAllowTouches(z);
    }

    public void showPictureInPictureMenu() {
        if (!this.mTouchState.isUserInteracting()) {
            this.mMenuController.showMenu(2, this.mMotionHelper.getBounds(), this.mMovementBounds, false, willResizeMenu());
        }
    }

    public void onActivityPinned() {
        cleanUp();
        this.mShowPipMenuOnAnimationEnd = true;
    }

    public void onActivityUnpinned(ComponentName componentName) {
        if (componentName == null) {
            cleanUp();
        }
    }

    public void onPinnedStackAnimationEnded() {
        this.mMotionHelper.synchronizePinnedStackBounds();
        if (this.mShowPipMenuOnAnimationEnd) {
            this.mMenuController.showMenu(1, this.mMotionHelper.getBounds(), this.mMovementBounds, true, false);
            this.mShowPipMenuOnAnimationEnd = false;
        }
    }

    public void onConfigurationChanged() {
        this.mMotionHelper.onConfigurationChanged();
        this.mMotionHelper.synchronizePinnedStackBounds();
    }

    public void onImeVisibilityChanged(boolean z, int i) {
        this.mIsImeShowing = z;
        this.mImeHeight = i;
    }

    public void onShelfVisibilityChanged(boolean z, int i) {
        this.mIsShelfShowing = z;
        this.mShelfHeight = i;
    }

    public void onMovementBoundsChanged(Rect rect, Rect rect2, Rect rect3, boolean z, boolean z2, int i) {
        int i2 = this.mIsImeShowing ? this.mImeHeight : 0;
        this.mNormalBounds = rect2;
        Rect rect4 = new Rect();
        this.mSnapAlgorithm.getMovementBounds(this.mNormalBounds, rect, rect4, i2);
        float width = rect2.width() / rect2.height();
        Point point = new Point();
        this.mContext.getDisplay().getRealSize(point);
        Size sizeForAspectRatio = this.mSnapAlgorithm.getSizeForAspectRatio(width, this.mExpandedShortestEdgeSize, point.x, point.y);
        this.mExpandedBounds.set(0, 0, sizeForAspectRatio.getWidth(), sizeForAspectRatio.getHeight());
        Rect rect5 = new Rect();
        this.mSnapAlgorithm.getMovementBounds(this.mExpandedBounds, rect, rect5, i2);
        if ((z || z2) && !this.mTouchState.isUserInteracting()) {
            int max = Math.max(this.mIsImeShowing ? this.mImeHeight + this.mImeOffset : 0, this.mIsShelfShowing ? this.mShelfHeight : 0);
            Rect rect6 = new Rect();
            this.mSnapAlgorithm.getMovementBounds(this.mNormalBounds, rect, rect6, max);
            Rect rect7 = new Rect();
            this.mSnapAlgorithm.getMovementBounds(this.mExpandedBounds, rect, rect7, max);
            if (this.mMenuState == 2) {
                rect6 = rect7;
            }
            Rect rect8 = this.mMenuState == 2 ? rect5 : rect4;
            if (rect6.bottom < this.mMovementBounds.bottom && rect3.top < rect6.bottom) {
                return;
            }
            int i3 = rect8.bottom - this.mMovementBounds.bottom;
            int i4 = z ? this.mImeOffset : this.mShelfHeight;
            if (rect6.bottom >= this.mMovementBounds.bottom && rect3.top < (rect6.bottom - i3) - i4) {
                return;
            }
            animateToOffset(rect3, rect6);
        }
        this.mNormalMovementBounds = rect4;
        this.mExpandedMovementBounds = rect5;
        this.mDisplayRotation = i;
        this.mInsetBounds.set(rect);
        updateMovementBounds(this.mMenuState);
        if (this.mDeferResizeToNormalBoundsUntilRotation == i) {
            this.mMotionHelper.animateToUnexpandedState(rect2, this.mSavedSnapFraction, this.mNormalMovementBounds, this.mMovementBounds, this.mIsMinimized, true);
            this.mSavedSnapFraction = -1.0f;
            this.mDeferResizeToNormalBoundsUntilRotation = -1;
        }
    }

    private void animateToOffset(Rect rect, Rect rect2) {
        Rect rect3 = new Rect(rect);
        rect3.offset(0, rect2.bottom - rect3.top);
        rect3.offset(0, Math.max(0, this.mMovementBounds.top - rect3.top));
        this.mMotionHelper.animateToOffset(rect3);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onRegistrationChanged(boolean z) {
        PipAccessibilityInteractionConnection pipAccessibilityInteractionConnection;
        AccessibilityManager accessibilityManager = this.mAccessibilityManager;
        if (z) {
            pipAccessibilityInteractionConnection = new PipAccessibilityInteractionConnection(this.mMotionHelper, new PipAccessibilityInteractionConnection.AccessibilityCallbacks() { // from class: com.android.systemui.pip.phone.-$$Lambda$PipTouchHandler$1nY3kLe318Fm3UtIAbDmSK80h7w
                @Override // com.android.systemui.pip.phone.PipAccessibilityInteractionConnection.AccessibilityCallbacks
                public final void onAccessibilityShowMenu() {
                    PipTouchHandler.this.onAccessibilityShowMenu();
                }
            }, this.mHandler);
        } else {
            pipAccessibilityInteractionConnection = null;
        }
        accessibilityManager.setPictureInPictureActionReplacingConnection(pipAccessibilityInteractionConnection);
        if (!z && this.mTouchState.isUserInteracting()) {
            cleanUpDismissTarget();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onAccessibilityShowMenu() {
        this.mMenuController.showMenu(2, this.mMotionHelper.getBounds(), this.mMovementBounds, false, willResizeMenu());
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    public boolean handleTouchEvent(MotionEvent motionEvent) {
        if (this.mPinnedStackController == null) {
            return true;
        }
        this.mTouchState.onTouchEvent(motionEvent);
        switch (motionEvent.getAction()) {
            case 0:
                this.mMotionHelper.synchronizePinnedStackBounds();
                for (PipTouchGesture pipTouchGesture : this.mGestures) {
                    pipTouchGesture.onDown(this.mTouchState);
                }
                break;
            case 1:
                updateMovementBounds(this.mMenuState);
                PipTouchGesture[] pipTouchGestureArr = this.mGestures;
                int length = pipTouchGestureArr.length;
                for (int i = 0; i < length && !pipTouchGestureArr[i].onUp(this.mTouchState); i++) {
                }
                this.mTouchState.reset();
                break;
            case 2:
                PipTouchGesture[] pipTouchGestureArr2 = this.mGestures;
                int length2 = pipTouchGestureArr2.length;
                for (int i2 = 0; i2 < length2 && !pipTouchGestureArr2[i2].onMove(this.mTouchState); i2++) {
                }
                break;
            case 3:
                this.mTouchState.reset();
                break;
            case 7:
            case 9:
                if (this.mAccessibilityManager.isEnabled() && !this.mSendingHoverAccessibilityEvents) {
                    AccessibilityEvent obtain = AccessibilityEvent.obtain(128);
                    obtain.setImportantForAccessibility(true);
                    obtain.setSourceNodeId(AccessibilityNodeInfo.ROOT_NODE_ID);
                    obtain.setWindowId(-3);
                    this.mAccessibilityManager.sendAccessibilityEvent(obtain);
                    this.mSendingHoverAccessibilityEvents = true;
                    break;
                }
                break;
            case 10:
                if (this.mAccessibilityManager.isEnabled() && this.mSendingHoverAccessibilityEvents) {
                    AccessibilityEvent obtain2 = AccessibilityEvent.obtain(256);
                    obtain2.setImportantForAccessibility(true);
                    obtain2.setSourceNodeId(AccessibilityNodeInfo.ROOT_NODE_ID);
                    obtain2.setWindowId(-3);
                    this.mAccessibilityManager.sendAccessibilityEvent(obtain2);
                    this.mSendingHoverAccessibilityEvents = false;
                    break;
                }
                break;
        }
        return this.mMenuState == 0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateDismissFraction() {
        float f;
        if (this.mMenuController != null && !this.mIsImeShowing) {
            Rect bounds = this.mMotionHelper.getBounds();
            float f2 = this.mInsetBounds.bottom;
            if (bounds.bottom > f2) {
                f = Math.min((bounds.bottom - f2) / bounds.height(), 1.0f);
            } else {
                f = 0.0f;
            }
            if (Float.compare(f, 0.0f) != 0 || this.mMenuController.isMenuActivityVisible()) {
                this.mMenuController.setDismissFraction(f);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setPinnedStackController(IPinnedStackController iPinnedStackController) {
        this.mPinnedStackController = iPinnedStackController;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setMinimizedStateInternal(boolean z) {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setMinimizedState(boolean z, boolean z2) {
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setMenuState(int i, boolean z) {
        if (i == 2) {
            Rect rect = new Rect(this.mExpandedBounds);
            if (z) {
                this.mSavedSnapFraction = this.mMotionHelper.animateToExpandedState(rect, this.mMovementBounds, this.mExpandedMovementBounds);
            }
        } else if (i == 0) {
            if (z) {
                if (this.mDeferResizeToNormalBoundsUntilRotation == -1) {
                    try {
                        int displayRotation = this.mPinnedStackController.getDisplayRotation();
                        if (this.mDisplayRotation != displayRotation) {
                            this.mDeferResizeToNormalBoundsUntilRotation = displayRotation;
                        }
                    } catch (RemoteException e) {
                        Log.e("PipTouchHandler", "Could not get display rotation from controller");
                    }
                }
                if (this.mDeferResizeToNormalBoundsUntilRotation == -1) {
                    this.mMotionHelper.animateToUnexpandedState(new Rect(this.mNormalBounds), this.mSavedSnapFraction, this.mNormalMovementBounds, this.mMovementBounds, this.mIsMinimized, false);
                    this.mSavedSnapFraction = -1.0f;
                }
            } else {
                setTouchEnabled(false);
                this.mSavedSnapFraction = -1.0f;
            }
        }
        this.mMenuState = i;
        updateMovementBounds(i);
        boolean z2 = true;
        if (i != 1) {
            Context context = this.mContext;
            if (i != 2) {
                z2 = false;
            }
            MetricsLoggerWrapper.logPictureInPictureMenuVisible(context, z2);
        }
    }

    public PipMotionHelper getMotionHelper() {
        return this.mMotionHelper;
    }

    private void updateMovementBounds(int i) {
        Rect rect;
        boolean z = i == 2;
        if (z) {
            rect = this.mExpandedMovementBounds;
        } else {
            rect = this.mNormalMovementBounds;
        }
        this.mMovementBounds = rect;
        try {
            this.mPinnedStackController.setMinEdgeSize(z ? this.mExpandedShortestEdgeSize : 0);
        } catch (RemoteException e) {
            Log.e("PipTouchHandler", "Could not set minimized state", e);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void cleanUpDismissTarget() {
        this.mHandler.removeCallbacks(this.mShowDismissAffordance);
        this.mDismissViewController.destroyDismissTarget();
    }

    private void cleanUp() {
        if (this.mIsMinimized) {
            setMinimizedStateInternal(false);
        }
        cleanUpDismissTarget();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean willResizeMenu() {
        return (this.mExpandedBounds.width() == this.mNormalBounds.width() && this.mExpandedBounds.height() == this.mNormalBounds.height()) ? false : true;
    }

    public void dump(PrintWriter printWriter, String str) {
        String str2 = str + "  ";
        printWriter.println(str + "PipTouchHandler");
        printWriter.println(str2 + "mMovementBounds=" + this.mMovementBounds);
        printWriter.println(str2 + "mNormalBounds=" + this.mNormalBounds);
        printWriter.println(str2 + "mNormalMovementBounds=" + this.mNormalMovementBounds);
        printWriter.println(str2 + "mExpandedBounds=" + this.mExpandedBounds);
        printWriter.println(str2 + "mExpandedMovementBounds=" + this.mExpandedMovementBounds);
        printWriter.println(str2 + "mMenuState=" + this.mMenuState);
        printWriter.println(str2 + "mIsMinimized=" + this.mIsMinimized);
        printWriter.println(str2 + "mIsImeShowing=" + this.mIsImeShowing);
        printWriter.println(str2 + "mImeHeight=" + this.mImeHeight);
        printWriter.println(str2 + "mIsShelfShowing=" + this.mIsShelfShowing);
        printWriter.println(str2 + "mShelfHeight=" + this.mShelfHeight);
        printWriter.println(str2 + "mSavedSnapFraction=" + this.mSavedSnapFraction);
        printWriter.println(str2 + "mEnableDragToEdgeDismiss=true");
        printWriter.println(str2 + "mEnableMinimize=false");
        this.mSnapAlgorithm.dump(printWriter, str2);
        this.mTouchState.dump(printWriter, str2);
        this.mMotionHelper.dump(printWriter, str2);
    }
}

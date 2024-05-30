package com.android.launcher3.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.RectEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.widget.ExploreByTouchHelper;
import android.util.AttributeSet;
import android.util.Property;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import com.android.launcher3.DeviceProfile;
import com.android.launcher3.Insettable;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherState;
import com.android.launcher3.LauncherStateManager;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.anim.Interpolators;
import com.android.launcher3.uioverrides.WallpaperColorInfo;
import com.android.launcher3.util.Themes;
import java.util.List;
/* loaded from: classes.dex */
public class ScrimView extends View implements Insettable, WallpaperColorInfo.OnChangeListener, AccessibilityManager.AccessibilityStateChangeListener, LauncherStateManager.StateListener {
    public static final Property<ScrimView, Integer> DRAG_HANDLE_ALPHA = new Property<ScrimView, Integer>(Integer.TYPE, "dragHandleAlpha") { // from class: com.android.launcher3.views.ScrimView.1
        @Override // android.util.Property
        public Integer get(ScrimView scrimView) {
            return Integer.valueOf(scrimView.mDragHandleAlpha);
        }

        @Override // android.util.Property
        public void set(ScrimView scrimView, Integer num) {
            scrimView.setDragHandleAlpha(num.intValue());
        }
    };
    private static final int SETTINGS = 2131820662;
    private static final int WALLPAPERS = 2131820674;
    private static final int WIDGETS = 2131820677;
    private final AccessibilityManager mAM;
    private final AccessibilityHelper mAccessibilityHelper;
    protected int mCurrentFlatColor;
    @Nullable
    protected Drawable mDragHandle;
    private int mDragHandleAlpha;
    private final Rect mDragHandleBounds;
    protected final int mDragHandleSize;
    protected int mEndFlatColor;
    protected int mEndFlatColorAlpha;
    protected final int mEndScrim;
    private final RectF mHitRect;
    protected final Launcher mLauncher;
    protected float mMaxScrimAlpha;
    protected float mProgress;
    protected int mScrimColor;
    private final int[] mTempPos;
    private final Rect mTempRect;
    private final WallpaperColorInfo mWallpaperColorInfo;

    public ScrimView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mTempRect = new Rect();
        this.mTempPos = new int[2];
        this.mProgress = 1.0f;
        this.mHitRect = new RectF();
        this.mDragHandleAlpha = 255;
        this.mLauncher = Launcher.getLauncher(context);
        this.mWallpaperColorInfo = WallpaperColorInfo.getInstance(context);
        this.mEndScrim = Themes.getAttrColor(context, R.attr.allAppsScrimColor);
        this.mMaxScrimAlpha = 0.7f;
        this.mDragHandleSize = context.getResources().getDimensionPixelSize(R.dimen.vertical_drag_handle_size);
        this.mDragHandleBounds = new Rect(0, 0, this.mDragHandleSize, this.mDragHandleSize);
        this.mAccessibilityHelper = createAccessibilityHelper();
        ViewCompat.setAccessibilityDelegate(this, this.mAccessibilityHelper);
        this.mAM = (AccessibilityManager) context.getSystemService("accessibility");
        setFocusable(false);
    }

    @NonNull
    protected AccessibilityHelper createAccessibilityHelper() {
        return new AccessibilityHelper();
    }

    @Override // com.android.launcher3.Insettable
    public void setInsets(Rect rect) {
        updateDragHandleBounds();
        updateDragHandleVisibility(null);
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        updateDragHandleBounds();
    }

    @Override // android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mWallpaperColorInfo.addOnChangeListener(this);
        onExtractedColorsChanged(this.mWallpaperColorInfo);
        this.mAM.addAccessibilityStateChangeListener(this);
        onAccessibilityStateChanged(this.mAM.isEnabled());
    }

    @Override // android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mWallpaperColorInfo.removeOnChangeListener(this);
        this.mAM.removeAccessibilityStateChangeListener(this);
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    @Override // com.android.launcher3.uioverrides.WallpaperColorInfo.OnChangeListener
    public void onExtractedColorsChanged(WallpaperColorInfo wallpaperColorInfo) {
        this.mScrimColor = wallpaperColorInfo.getMainColor();
        this.mEndFlatColor = ColorUtils.compositeColors(this.mEndScrim, ColorUtils.setAlphaComponent(this.mScrimColor, Math.round(this.mMaxScrimAlpha * 255.0f)));
        this.mEndFlatColorAlpha = Color.alpha(this.mEndFlatColor);
        updateColors();
        invalidate();
    }

    public void setProgress(float f) {
        if (this.mProgress != f) {
            this.mProgress = f;
            updateColors();
            updateDragHandleAlpha();
            invalidate();
        }
    }

    public void reInitUi() {
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void updateColors() {
        this.mCurrentFlatColor = this.mProgress >= 1.0f ? 0 : ColorUtils.setAlphaComponent(this.mEndFlatColor, Math.round((1.0f - this.mProgress) * this.mEndFlatColorAlpha));
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void updateDragHandleAlpha() {
        if (this.mDragHandle != null) {
            this.mDragHandle.setAlpha(this.mDragHandleAlpha);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setDragHandleAlpha(int i) {
        if (i != this.mDragHandleAlpha) {
            this.mDragHandleAlpha = i;
            if (this.mDragHandle != null) {
                this.mDragHandle.setAlpha(this.mDragHandleAlpha);
                invalidate();
            }
        }
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        if (this.mCurrentFlatColor != 0) {
            canvas.drawColor(this.mCurrentFlatColor);
        }
        if (this.mDragHandle != null) {
            this.mDragHandle.draw(canvas);
        }
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        boolean onTouchEvent = super.onTouchEvent(motionEvent);
        if (!onTouchEvent && this.mDragHandle != null && motionEvent.getAction() == 0 && this.mDragHandle.getAlpha() == 255 && this.mHitRect.contains(motionEvent.getX(), motionEvent.getY())) {
            final Drawable drawable = this.mDragHandle;
            this.mDragHandle = null;
            drawable.setBounds(this.mDragHandleBounds);
            Rect rect = new Rect(this.mDragHandleBounds);
            rect.offset(0, (-this.mDragHandleBounds.height()) / 2);
            final Rect rect2 = new Rect(this.mDragHandleBounds);
            rect2.top = rect.top;
            Keyframe ofObject = Keyframe.ofObject(0.6f, rect);
            ofObject.setInterpolator(Interpolators.DEACCEL);
            Keyframe ofObject2 = Keyframe.ofObject(1.0f, this.mDragHandleBounds);
            ofObject2.setInterpolator(Interpolators.ACCEL);
            PropertyValuesHolder ofKeyframe = PropertyValuesHolder.ofKeyframe("bounds", Keyframe.ofObject(0.0f, this.mDragHandleBounds), ofObject, ofObject2);
            ofKeyframe.setEvaluator(new RectEvaluator());
            ObjectAnimator ofPropertyValuesHolder = ObjectAnimator.ofPropertyValuesHolder(drawable, ofKeyframe);
            ofPropertyValuesHolder.addListener(new AnimatorListenerAdapter() { // from class: com.android.launcher3.views.ScrimView.2
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    ScrimView.this.getOverlay().remove(drawable);
                    ScrimView.this.updateDragHandleVisibility(drawable);
                }
            });
            ofPropertyValuesHolder.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.launcher3.views.-$$Lambda$ScrimView$_h9CkBt1Jm9GRe9w8OUSZ-wzPrY
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    ScrimView.this.invalidate(rect2);
                }
            });
            getOverlay().add(drawable);
            ofPropertyValuesHolder.start();
        }
        return onTouchEvent;
    }

    protected void updateDragHandleBounds() {
        int i;
        int i2;
        DeviceProfile deviceProfile = this.mLauncher.getDeviceProfile();
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = (getMeasuredHeight() - this.mDragHandleSize) - deviceProfile.getInsets().bottom;
        if (deviceProfile.isVerticalBarLayout()) {
            i2 = deviceProfile.workspacePadding.bottom;
            if (deviceProfile.isSeascape()) {
                i = (measuredWidth - deviceProfile.getInsets().right) - this.mDragHandleSize;
            } else {
                i = this.mDragHandleSize + deviceProfile.getInsets().left;
            }
        } else {
            i = (measuredWidth - this.mDragHandleSize) / 2;
            i2 = deviceProfile.hotseatBarSizePx;
        }
        this.mDragHandleBounds.offsetTo(i, measuredHeight - i2);
        this.mHitRect.set(this.mDragHandleBounds);
        float f = (-this.mDragHandleSize) / 2;
        this.mHitRect.inset(f, f);
        if (this.mDragHandle != null) {
            this.mDragHandle.setBounds(this.mDragHandleBounds);
        }
    }

    @Override // android.view.accessibility.AccessibilityManager.AccessibilityStateChangeListener
    public void onAccessibilityStateChanged(boolean z) {
        LauncherStateManager stateManager = this.mLauncher.getStateManager();
        stateManager.removeStateListener(this);
        if (z) {
            stateManager.addStateListener(this);
            onStateSetImmediately(this.mLauncher.getStateManager().getState());
        } else {
            setImportantForAccessibility(4);
        }
        updateDragHandleVisibility(null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateDragHandleVisibility(Drawable drawable) {
        boolean z = this.mLauncher.getDeviceProfile().isVerticalBarLayout() || this.mAM.isEnabled();
        if (z != (this.mDragHandle != null)) {
            if (z) {
                if (drawable == null) {
                    drawable = this.mLauncher.getDrawable(R.drawable.drag_handle_indicator);
                }
                this.mDragHandle = drawable;
                this.mDragHandle.setBounds(this.mDragHandleBounds);
                updateDragHandleAlpha();
            } else {
                this.mDragHandle = null;
            }
            invalidate();
        }
    }

    @Override // android.view.View
    public boolean dispatchHoverEvent(MotionEvent motionEvent) {
        return this.mAccessibilityHelper.dispatchHoverEvent(motionEvent) || super.dispatchHoverEvent(motionEvent);
    }

    @Override // android.view.View
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        return this.mAccessibilityHelper.dispatchKeyEvent(keyEvent) || super.dispatchKeyEvent(keyEvent);
    }

    @Override // android.view.View
    public void onFocusChanged(boolean z, int i, Rect rect) {
        super.onFocusChanged(z, i, rect);
        this.mAccessibilityHelper.onFocusChanged(z, i, rect);
    }

    @Override // com.android.launcher3.LauncherStateManager.StateListener
    public void onStateTransitionStart(LauncherState launcherState) {
    }

    @Override // com.android.launcher3.LauncherStateManager.StateListener
    public void onStateTransitionComplete(LauncherState launcherState) {
        onStateSetImmediately(launcherState);
    }

    @Override // com.android.launcher3.LauncherStateManager.StateListener
    public void onStateSetImmediately(LauncherState launcherState) {
        int i;
        if (launcherState == LauncherState.ALL_APPS) {
            i = 4;
        } else {
            i = 0;
        }
        setImportantForAccessibility(i);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* loaded from: classes.dex */
    public class AccessibilityHelper extends ExploreByTouchHelper {
        private static final int DRAG_HANDLE_ID = 1;

        public AccessibilityHelper() {
            super(ScrimView.this);
        }

        @Override // android.support.v4.widget.ExploreByTouchHelper
        protected int getVirtualViewAt(float f, float f2) {
            return ScrimView.this.mDragHandleBounds.contains((int) f, (int) f2) ? 1 : Integer.MIN_VALUE;
        }

        @Override // android.support.v4.widget.ExploreByTouchHelper
        protected void getVisibleVirtualViews(List<Integer> list) {
            list.add(1);
        }

        @Override // android.support.v4.widget.ExploreByTouchHelper
        protected void onPopulateNodeForVirtualView(int i, AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
            accessibilityNodeInfoCompat.setContentDescription(ScrimView.this.getContext().getString(R.string.all_apps_button_label));
            accessibilityNodeInfoCompat.setBoundsInParent(ScrimView.this.mDragHandleBounds);
            ScrimView.this.getLocationOnScreen(ScrimView.this.mTempPos);
            ScrimView.this.mTempRect.set(ScrimView.this.mDragHandleBounds);
            ScrimView.this.mTempRect.offset(ScrimView.this.mTempPos[0], ScrimView.this.mTempPos[1]);
            accessibilityNodeInfoCompat.setBoundsInScreen(ScrimView.this.mTempRect);
            accessibilityNodeInfoCompat.addAction(16);
            accessibilityNodeInfoCompat.setClickable(true);
            accessibilityNodeInfoCompat.setFocusable(true);
            if (ScrimView.this.mLauncher.isInState(LauncherState.NORMAL)) {
                Context context = ScrimView.this.getContext();
                if (Utilities.isWallpaperAllowed(context)) {
                    accessibilityNodeInfoCompat.addAction(new AccessibilityNodeInfoCompat.AccessibilityActionCompat(R.string.wallpaper_button_text, context.getText(R.string.wallpaper_button_text)));
                }
                accessibilityNodeInfoCompat.addAction(new AccessibilityNodeInfoCompat.AccessibilityActionCompat(R.string.widget_button_text, context.getText(R.string.widget_button_text)));
                accessibilityNodeInfoCompat.addAction(new AccessibilityNodeInfoCompat.AccessibilityActionCompat(R.string.settings_button_text, context.getText(R.string.settings_button_text)));
            }
        }

        @Override // android.support.v4.widget.ExploreByTouchHelper
        protected boolean onPerformActionForVirtualView(int i, int i2, Bundle bundle) {
            if (i2 == 16) {
                ScrimView.this.mLauncher.getUserEventDispatcher().logActionOnControl(0, 1, ScrimView.this.mLauncher.getStateManager().getState().containerType);
                ScrimView.this.mLauncher.getStateManager().goToState(LauncherState.ALL_APPS);
                return true;
            } else if (i2 == R.string.wallpaper_button_text) {
                return OptionsPopupView.startWallpaperPicker(ScrimView.this);
            } else {
                if (i2 == R.string.widget_button_text) {
                    return OptionsPopupView.onWidgetsClicked(ScrimView.this);
                }
                if (i2 != R.string.settings_button_text) {
                    return false;
                }
                return OptionsPopupView.startSettings(ScrimView.this);
            }
        }
    }

    public int getDragHandleSize() {
        return this.mDragHandleSize;
    }
}

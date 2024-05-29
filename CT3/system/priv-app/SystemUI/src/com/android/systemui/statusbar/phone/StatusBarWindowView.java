package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.session.MediaSessionLegacyHelper;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.InputQueue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.widget.FrameLayout;
import com.android.internal.view.FloatingActionMode;
import com.android.internal.widget.FloatingToolbar;
import com.android.systemui.R$styleable;
import com.android.systemui.classifier.FalsingManager;
import com.android.systemui.statusbar.DragDownHelper;
import com.android.systemui.statusbar.stack.NotificationStackScrollLayout;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/StatusBarWindowView.class */
public class StatusBarWindowView extends FrameLayout {
    private View mBrightnessMirror;
    private DragDownHelper mDragDownHelper;
    private Window mFakeWindow;
    private FalsingManager mFalsingManager;
    private ActionMode mFloatingActionMode;
    private View mFloatingActionModeOriginatingView;
    private FloatingToolbar mFloatingToolbar;
    private ViewTreeObserver.OnPreDrawListener mFloatingToolbarPreDrawListener;
    private NotificationPanelView mNotificationPanel;
    private int mRightInset;
    private PhoneStatusBar mService;
    private NotificationStackScrollLayout mStackScrollLayout;
    private final Paint mTransparentSrcPaint;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/statusbar/phone/StatusBarWindowView$ActionModeCallback2Wrapper.class */
    public class ActionModeCallback2Wrapper extends ActionMode.Callback2 {
        private final ActionMode.Callback mWrapped;
        final StatusBarWindowView this$0;

        public ActionModeCallback2Wrapper(StatusBarWindowView statusBarWindowView, ActionMode.Callback callback) {
            this.this$0 = statusBarWindowView;
            this.mWrapped = callback;
        }

        @Override // android.view.ActionMode.Callback
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            return this.mWrapped.onActionItemClicked(actionMode, menuItem);
        }

        @Override // android.view.ActionMode.Callback
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            return this.mWrapped.onCreateActionMode(actionMode, menu);
        }

        @Override // android.view.ActionMode.Callback
        public void onDestroyActionMode(ActionMode actionMode) {
            this.mWrapped.onDestroyActionMode(actionMode);
            if (actionMode == this.this$0.mFloatingActionMode) {
                this.this$0.cleanupFloatingActionModeViews();
                this.this$0.mFloatingActionMode = null;
            }
            this.this$0.requestFitSystemWindows();
        }

        @Override // android.view.ActionMode.Callback2
        public void onGetContentRect(ActionMode actionMode, View view, Rect rect) {
            if (this.mWrapped instanceof ActionMode.Callback2) {
                ((ActionMode.Callback2) this.mWrapped).onGetContentRect(actionMode, view, rect);
            } else {
                super.onGetContentRect(actionMode, view, rect);
            }
        }

        @Override // android.view.ActionMode.Callback
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            this.this$0.requestFitSystemWindows();
            return this.mWrapped.onPrepareActionMode(actionMode, menu);
        }
    }

    /* loaded from: a.zip:com/android/systemui/statusbar/phone/StatusBarWindowView$LayoutParams.class */
    public class LayoutParams extends FrameLayout.LayoutParams {
        public boolean ignoreRightInset;
        final StatusBarWindowView this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public LayoutParams(StatusBarWindowView statusBarWindowView, int i, int i2) {
            super(i, i2);
            this.this$0 = statusBarWindowView;
        }

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public LayoutParams(StatusBarWindowView statusBarWindowView, Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            this.this$0 = statusBarWindowView;
            TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.StatusBarWindowView_Layout);
            this.ignoreRightInset = obtainStyledAttributes.getBoolean(0, false);
            obtainStyledAttributes.recycle();
        }
    }

    public StatusBarWindowView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mRightInset = 0;
        this.mTransparentSrcPaint = new Paint();
        this.mFakeWindow = new Window(this, this.mContext) { // from class: com.android.systemui.statusbar.phone.StatusBarWindowView.1
            final StatusBarWindowView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.Window
            public void addContentView(View view, ViewGroup.LayoutParams layoutParams) {
            }

            public void alwaysReadCloseOnTouchAttr() {
            }

            public void clearContentView() {
            }

            @Override // android.view.Window
            public void closeAllPanels() {
            }

            @Override // android.view.Window
            public void closePanel(int i) {
            }

            @Override // android.view.Window
            public View getCurrentFocus() {
                return null;
            }

            @Override // android.view.Window
            public View getDecorView() {
                return this.this$0;
            }

            @Override // android.view.Window
            public LayoutInflater getLayoutInflater() {
                return null;
            }

            @Override // android.view.Window
            public int getNavigationBarColor() {
                return 0;
            }

            @Override // android.view.Window
            public int getStatusBarColor() {
                return 0;
            }

            @Override // android.view.Window
            public int getVolumeControlStream() {
                return 0;
            }

            @Override // android.view.Window
            public void invalidatePanelMenu(int i) {
            }

            @Override // android.view.Window
            public boolean isFloating() {
                return false;
            }

            @Override // android.view.Window
            public boolean isShortcutKey(int i, KeyEvent keyEvent) {
                return false;
            }

            @Override // android.view.Window
            protected void onActive() {
            }

            @Override // android.view.Window
            public void onConfigurationChanged(Configuration configuration) {
            }

            public void onMultiWindowModeChanged() {
            }

            @Override // android.view.Window
            public void openPanel(int i, KeyEvent keyEvent) {
            }

            @Override // android.view.Window
            public View peekDecorView() {
                return null;
            }

            @Override // android.view.Window
            public boolean performContextMenuIdentifierAction(int i, int i2) {
                return false;
            }

            @Override // android.view.Window
            public boolean performPanelIdentifierAction(int i, int i2, int i3) {
                return false;
            }

            @Override // android.view.Window
            public boolean performPanelShortcut(int i, int i2, KeyEvent keyEvent, int i3) {
                return false;
            }

            public void reportActivityRelaunched() {
            }

            @Override // android.view.Window
            public void restoreHierarchyState(Bundle bundle) {
            }

            @Override // android.view.Window
            public Bundle saveHierarchyState() {
                return null;
            }

            @Override // android.view.Window
            public void setBackgroundDrawable(Drawable drawable) {
            }

            @Override // android.view.Window
            public void setChildDrawable(int i, Drawable drawable) {
            }

            @Override // android.view.Window
            public void setChildInt(int i, int i2) {
            }

            @Override // android.view.Window
            public void setContentView(int i) {
            }

            @Override // android.view.Window
            public void setContentView(View view) {
            }

            @Override // android.view.Window
            public void setContentView(View view, ViewGroup.LayoutParams layoutParams) {
            }

            @Override // android.view.Window
            public void setDecorCaptionShade(int i) {
            }

            @Override // android.view.Window
            public void setFeatureDrawable(int i, Drawable drawable) {
            }

            @Override // android.view.Window
            public void setFeatureDrawableAlpha(int i, int i2) {
            }

            @Override // android.view.Window
            public void setFeatureDrawableResource(int i, int i2) {
            }

            @Override // android.view.Window
            public void setFeatureDrawableUri(int i, Uri uri) {
            }

            @Override // android.view.Window
            public void setFeatureInt(int i, int i2) {
            }

            @Override // android.view.Window
            public void setNavigationBarColor(int i) {
            }

            @Override // android.view.Window
            public void setResizingCaptionDrawable(Drawable drawable) {
            }

            @Override // android.view.Window
            public void setStatusBarColor(int i) {
            }

            @Override // android.view.Window
            public void setTitle(CharSequence charSequence) {
            }

            @Override // android.view.Window
            public void setTitleColor(int i) {
            }

            @Override // android.view.Window
            public void setVolumeControlStream(int i) {
            }

            @Override // android.view.Window
            public boolean superDispatchGenericMotionEvent(MotionEvent motionEvent) {
                return false;
            }

            @Override // android.view.Window
            public boolean superDispatchKeyEvent(KeyEvent keyEvent) {
                return false;
            }

            @Override // android.view.Window
            public boolean superDispatchKeyShortcutEvent(KeyEvent keyEvent) {
                return false;
            }

            @Override // android.view.Window
            public boolean superDispatchTouchEvent(MotionEvent motionEvent) {
                return false;
            }

            @Override // android.view.Window
            public boolean superDispatchTrackballEvent(MotionEvent motionEvent) {
                return false;
            }

            @Override // android.view.Window
            public void takeInputQueue(InputQueue.Callback callback) {
            }

            @Override // android.view.Window
            public void takeKeyEvents(boolean z) {
            }

            @Override // android.view.Window
            public void takeSurface(SurfaceHolder.Callback2 callback2) {
            }

            @Override // android.view.Window
            public void togglePanel(int i, KeyEvent keyEvent) {
            }
        };
        setMotionEventSplittingEnabled(false);
        this.mTransparentSrcPaint.setColor(0);
        this.mTransparentSrcPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        this.mFalsingManager = FalsingManager.getInstance(context);
    }

    private void applyMargins() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = getChildAt(i);
            if (childAt.getLayoutParams() instanceof LayoutParams) {
                LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
                if (!layoutParams.ignoreRightInset && layoutParams.rightMargin != this.mRightInset) {
                    layoutParams.rightMargin = this.mRightInset;
                    childAt.requestLayout();
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void cleanupFloatingActionModeViews() {
        if (this.mFloatingToolbar != null) {
            this.mFloatingToolbar.dismiss();
            this.mFloatingToolbar = null;
        }
        if (this.mFloatingActionModeOriginatingView != null) {
            if (this.mFloatingToolbarPreDrawListener != null) {
                this.mFloatingActionModeOriginatingView.getViewTreeObserver().removeOnPreDrawListener(this.mFloatingToolbarPreDrawListener);
                this.mFloatingToolbarPreDrawListener = null;
            }
            this.mFloatingActionModeOriginatingView = null;
        }
    }

    private ActionMode createFloatingActionMode(View view, ActionMode.Callback2 callback2) {
        if (this.mFloatingActionMode != null) {
            this.mFloatingActionMode.finish();
        }
        cleanupFloatingActionModeViews();
        FloatingActionMode floatingActionMode = new FloatingActionMode(this.mContext, callback2, view);
        this.mFloatingActionModeOriginatingView = view;
        this.mFloatingToolbarPreDrawListener = new ViewTreeObserver.OnPreDrawListener(this, floatingActionMode) { // from class: com.android.systemui.statusbar.phone.StatusBarWindowView.2
            final StatusBarWindowView this$0;
            final FloatingActionMode val$mode;

            {
                this.this$0 = this;
                this.val$mode = floatingActionMode;
            }

            @Override // android.view.ViewTreeObserver.OnPreDrawListener
            public boolean onPreDraw() {
                this.val$mode.updateViewLocationInWindow();
                return true;
            }
        };
        return floatingActionMode;
    }

    private void setHandledFloatingActionMode(ActionMode actionMode) {
        this.mFloatingActionMode = actionMode;
        this.mFloatingToolbar = new FloatingToolbar(this.mContext, this.mFakeWindow);
        this.mFloatingActionMode.setFloatingToolbar(this.mFloatingToolbar);
        this.mFloatingActionMode.invalidate();
        this.mFloatingActionModeOriginatingView.getViewTreeObserver().addOnPreDrawListener(this.mFloatingToolbarPreDrawListener);
    }

    private ActionMode startActionMode(View view, ActionMode.Callback callback, int i) {
        ActionModeCallback2Wrapper actionModeCallback2Wrapper = new ActionModeCallback2Wrapper(this, callback);
        ActionMode createFloatingActionMode = createFloatingActionMode(view, actionModeCallback2Wrapper);
        if (createFloatingActionMode == null || !actionModeCallback2Wrapper.onCreateActionMode(createFloatingActionMode, createFloatingActionMode.getMenu())) {
            createFloatingActionMode = null;
        } else {
            setHandledFloatingActionMode(createFloatingActionMode);
        }
        return createFloatingActionMode;
    }

    public void cancelExpandHelper() {
        if (this.mStackScrollLayout != null) {
            this.mStackScrollLayout.cancelExpandHelper();
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:23:0x0070  */
    /* JADX WARN: Removed duplicated region for block: B:29:0x0090  */
    /* JADX WARN: Removed duplicated region for block: B:9:0x004b A[RETURN] */
    @Override // android.view.ViewGroup, android.view.View
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        boolean z = keyEvent.getAction() == 0;
        switch (keyEvent.getKeyCode()) {
            case 4:
                if (z) {
                    return true;
                }
                this.mService.onBackPressed();
                return true;
            case 24:
            case 25:
                if (this.mService.isDozing()) {
                    MediaSessionLegacyHelper.getHelper(this.mContext).sendVolumeKeyEvent(keyEvent, true);
                    return true;
                }
                if (this.mService.interceptMediaKey(keyEvent)) {
                    return super.dispatchKeyEvent(keyEvent);
                }
                return true;
            case 62:
                if (!z) {
                    return this.mService.onSpacePressed();
                }
                if (this.mService.interceptMediaKey(keyEvent)) {
                }
                break;
            case 82:
                if (!z) {
                    return this.mService.onMenuPressed();
                }
                if (!z) {
                }
                if (this.mService.interceptMediaKey(keyEvent)) {
                }
                break;
            default:
                if (this.mService.interceptMediaKey(keyEvent)) {
                }
                break;
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        this.mFalsingManager.onTouchEvent(motionEvent, getWidth(), getHeight());
        if (this.mBrightnessMirror != null && this.mBrightnessMirror.getVisibility() == 0 && motionEvent.getActionMasked() == 5) {
            return false;
        }
        if (motionEvent.getActionMasked() == 0) {
            this.mStackScrollLayout.closeControlsIfOutsideTouch(motionEvent);
        }
        return super.dispatchTouchEvent(motionEvent);
    }

    @Override // android.view.View
    protected boolean fitSystemWindows(Rect rect) {
        if (getFitsSystemWindows()) {
            boolean z = (rect.left == getPaddingLeft() && rect.top == getPaddingTop()) ? rect.bottom != getPaddingBottom() : true;
            if (rect.right != this.mRightInset) {
                this.mRightInset = rect.right;
                applyMargins();
            }
            if (z) {
                setPadding(rect.left, 0, 0, 0);
            }
            rect.left = 0;
            rect.top = 0;
            rect.right = 0;
            return false;
        }
        if (this.mRightInset != 0) {
            this.mRightInset = 0;
            applyMargins();
        }
        boolean z2 = true;
        if (getPaddingLeft() == 0) {
            if (getPaddingRight() != 0) {
                z2 = true;
            } else {
                z2 = true;
                if (getPaddingTop() == 0) {
                    z2 = true;
                    if (getPaddingBottom() == 0) {
                        z2 = false;
                    }
                }
            }
        }
        if (z2) {
            setPadding(0, 0, 0, 0);
        }
        rect.top = 0;
        return false;
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup
    protected FrameLayout.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(this, -1, -1);
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup
    public FrameLayout.LayoutParams generateLayoutParams(AttributeSet attributeSet) {
        return new LayoutParams(this, getContext(), attributeSet);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!this.mService.isScrimSrcModeEnabled()) {
            setWillNotDraw(true);
            return;
        }
        IBinder windowToken = getWindowToken();
        WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) getLayoutParams();
        layoutParams.token = windowToken;
        setLayoutParams(layoutParams);
        WindowManagerGlobal.getInstance().changeCanvasOpacity(windowToken, true);
        setWillNotDraw(false);
    }

    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mService.isScrimSrcModeEnabled()) {
            int height = getHeight() - getPaddingBottom();
            int width = getWidth();
            int paddingRight = getPaddingRight();
            if (getPaddingTop() != 0) {
                canvas.drawRect(0.0f, 0.0f, getWidth(), getPaddingTop(), this.mTransparentSrcPaint);
            }
            if (getPaddingBottom() != 0) {
                canvas.drawRect(0.0f, height, getWidth(), getHeight(), this.mTransparentSrcPaint);
            }
            if (getPaddingLeft() != 0) {
                canvas.drawRect(0.0f, getPaddingTop(), getPaddingLeft(), height, this.mTransparentSrcPaint);
            }
            if (getPaddingRight() != 0) {
                canvas.drawRect(width - paddingRight, getPaddingTop(), getWidth(), height, this.mTransparentSrcPaint);
            }
        }
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mStackScrollLayout = (NotificationStackScrollLayout) findViewById(2131886684);
        this.mNotificationPanel = (NotificationPanelView) findViewById(2131886681);
        this.mBrightnessMirror = findViewById(2131886259);
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        boolean z = false;
        if (this.mNotificationPanel.isFullyExpanded()) {
            z = false;
            if (this.mStackScrollLayout.getVisibility() == 0) {
                z = false;
                if (this.mService.getBarState() == 1) {
                    if (this.mService.isBouncerShowing()) {
                        z = false;
                    } else {
                        boolean onInterceptTouchEvent = this.mDragDownHelper.onInterceptTouchEvent(motionEvent);
                        z = onInterceptTouchEvent;
                        if (motionEvent.getActionMasked() == 0) {
                            this.mService.wakeUpIfDozing(motionEvent.getEventTime(), motionEvent);
                            z = onInterceptTouchEvent;
                        }
                    }
                }
            }
        }
        if (!z) {
            super.onInterceptTouchEvent(motionEvent);
        }
        if (z) {
            MotionEvent obtain = MotionEvent.obtain(motionEvent);
            obtain.setAction(3);
            this.mStackScrollLayout.onInterceptTouchEvent(obtain);
            this.mNotificationPanel.onInterceptTouchEvent(obtain);
            obtain.recycle();
        }
        return z;
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        boolean z = false;
        if (this.mService.getBarState() == 1) {
            z = this.mDragDownHelper.onTouchEvent(motionEvent);
        }
        boolean z2 = z;
        if (!z) {
            z2 = super.onTouchEvent(motionEvent);
        }
        int action = motionEvent.getAction();
        if (!z2 && (action == 1 || action == 3)) {
            this.mService.setInteracting(1, false);
        }
        return z2;
    }

    public void setService(PhoneStatusBar phoneStatusBar) {
        this.mService = phoneStatusBar;
        this.mDragDownHelper = new DragDownHelper(getContext(), this, this.mStackScrollLayout, this.mService);
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public ActionMode startActionModeForChild(View view, ActionMode.Callback callback, int i) {
        return i == 1 ? startActionMode(view, callback, i) : super.startActionModeForChild(view, callback, i);
    }
}

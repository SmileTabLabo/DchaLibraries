package com.android.systemui;

import android.app.Fragment;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Region;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.os.SystemProperties;
import android.util.DisplayMetrics;
import android.view.DisplayCutout;
import android.view.DisplayInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.android.systemui.RegionInterceptingFrameLayout;
import com.android.systemui.fragments.FragmentHostManager;
import com.android.systemui.plugins.qs.QS;
import com.android.systemui.qs.SecureSetting;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.phone.StatusBarWindowView;
import com.android.systemui.tuner.TunablePadding;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.util.leak.RotationUtils;
/* loaded from: classes.dex */
public class ScreenDecorations extends SystemUI implements TunerService.Tunable {
    private static final boolean DEBUG_SCREENSHOT_ROUNDED_CORNERS = SystemProperties.getBoolean("debug.screenshot_rounded_corners", false);
    private View mBottomOverlay;
    private float mDensity;
    private DisplayManager.DisplayListener mDisplayListener;
    private DisplayManager mDisplayManager;
    private View mOverlay;
    private int mRotation;
    private int mRoundedDefault;
    private int mRoundedDefaultBottom;
    private int mRoundedDefaultTop;
    private WindowManager mWindowManager;

    @Override // com.android.systemui.SystemUI
    public void start() {
        this.mWindowManager = (WindowManager) this.mContext.getSystemService(WindowManager.class);
        this.mRoundedDefault = this.mContext.getResources().getDimensionPixelSize(R.dimen.rounded_corner_radius);
        this.mRoundedDefaultTop = this.mContext.getResources().getDimensionPixelSize(R.dimen.rounded_corner_radius_top);
        this.mRoundedDefaultBottom = this.mContext.getResources().getDimensionPixelSize(R.dimen.rounded_corner_radius_bottom);
        if (hasRoundedCorners() || shouldDrawCutout()) {
            setupDecorations();
        }
        int dimensionPixelSize = this.mContext.getResources().getDimensionPixelSize(R.dimen.rounded_corner_content_padding);
        if (dimensionPixelSize != 0) {
            setupPadding(dimensionPixelSize);
        }
        this.mDisplayListener = new DisplayManager.DisplayListener() { // from class: com.android.systemui.ScreenDecorations.1
            @Override // android.hardware.display.DisplayManager.DisplayListener
            public void onDisplayAdded(int i) {
            }

            @Override // android.hardware.display.DisplayManager.DisplayListener
            public void onDisplayRemoved(int i) {
            }

            @Override // android.hardware.display.DisplayManager.DisplayListener
            public void onDisplayChanged(int i) {
                ScreenDecorations.this.updateOrientation();
            }
        };
        this.mRotation = -1;
        this.mDisplayManager = (DisplayManager) this.mContext.getSystemService("display");
        this.mDisplayManager.registerDisplayListener(this.mDisplayListener, null);
    }

    private void setupDecorations() {
        this.mOverlay = LayoutInflater.from(this.mContext).inflate(R.layout.rounded_corners, (ViewGroup) null);
        final DisplayCutoutView displayCutoutView = new DisplayCutoutView(this.mContext, true, new Runnable() { // from class: com.android.systemui.-$$Lambda$ScreenDecorations$aq1MVJyy_LkZ11q5t5cPVZOqbG0
            @Override // java.lang.Runnable
            public final void run() {
                ScreenDecorations.this.updateWindowVisibilities();
            }
        });
        ((ViewGroup) this.mOverlay).addView(displayCutoutView);
        this.mBottomOverlay = LayoutInflater.from(this.mContext).inflate(R.layout.rounded_corners, (ViewGroup) null);
        final DisplayCutoutView displayCutoutView2 = new DisplayCutoutView(this.mContext, false, new Runnable() { // from class: com.android.systemui.-$$Lambda$ScreenDecorations$aq1MVJyy_LkZ11q5t5cPVZOqbG0
            @Override // java.lang.Runnable
            public final void run() {
                ScreenDecorations.this.updateWindowVisibilities();
            }
        });
        ((ViewGroup) this.mBottomOverlay).addView(displayCutoutView2);
        this.mOverlay.setSystemUiVisibility(256);
        this.mOverlay.setAlpha(0.0f);
        this.mBottomOverlay.setSystemUiVisibility(256);
        this.mBottomOverlay.setAlpha(0.0f);
        updateViews();
        this.mWindowManager.addView(this.mOverlay, getWindowLayoutParams());
        this.mWindowManager.addView(this.mBottomOverlay, getBottomLayoutParams());
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);
        this.mDensity = displayMetrics.density;
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "sysui_rounded_size");
        SecureSetting secureSetting = new SecureSetting(this.mContext, (Handler) Dependency.get(Dependency.MAIN_HANDLER), "accessibility_display_inversion_enabled") { // from class: com.android.systemui.ScreenDecorations.2
            @Override // com.android.systemui.qs.SecureSetting
            protected void handleValueChanged(int i, boolean z) {
                int i2 = i != 0 ? -1 : -16777216;
                ColorStateList valueOf = ColorStateList.valueOf(i2);
                ((ImageView) ScreenDecorations.this.mOverlay.findViewById(R.id.left)).setImageTintList(valueOf);
                ((ImageView) ScreenDecorations.this.mOverlay.findViewById(R.id.right)).setImageTintList(valueOf);
                ((ImageView) ScreenDecorations.this.mBottomOverlay.findViewById(R.id.left)).setImageTintList(valueOf);
                ((ImageView) ScreenDecorations.this.mBottomOverlay.findViewById(R.id.right)).setImageTintList(valueOf);
                displayCutoutView.setColor(i2);
                displayCutoutView2.setColor(i2);
            }
        };
        secureSetting.setListening(true);
        secureSetting.onChange(false);
        this.mOverlay.addOnLayoutChangeListener(new View.OnLayoutChangeListener() { // from class: com.android.systemui.ScreenDecorations.3
            @Override // android.view.View.OnLayoutChangeListener
            public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                ScreenDecorations.this.mOverlay.removeOnLayoutChangeListener(this);
                ScreenDecorations.this.mOverlay.animate().alpha(1.0f).setDuration(1000L).start();
                ScreenDecorations.this.mBottomOverlay.animate().alpha(1.0f).setDuration(1000L).start();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.SystemUI
    public void onConfigurationChanged(Configuration configuration) {
        updateOrientation();
        if (shouldDrawCutout() && this.mOverlay == null) {
            setupDecorations();
        }
    }

    protected void updateOrientation() {
        int exactRotation = RotationUtils.getExactRotation(this.mContext);
        if (exactRotation != this.mRotation) {
            this.mRotation = exactRotation;
            if (this.mOverlay != null) {
                updateLayoutParams();
                updateViews();
            }
        }
    }

    private void updateViews() {
        View findViewById = this.mOverlay.findViewById(R.id.left);
        View findViewById2 = this.mOverlay.findViewById(R.id.right);
        View findViewById3 = this.mBottomOverlay.findViewById(R.id.left);
        View findViewById4 = this.mBottomOverlay.findViewById(R.id.right);
        if (this.mRotation == 0) {
            updateView(findViewById, 51, 0);
            updateView(findViewById2, 53, 90);
            updateView(findViewById3, 83, 270);
            updateView(findViewById4, 85, 180);
        } else if (this.mRotation == 1) {
            updateView(findViewById, 51, 0);
            updateView(findViewById2, 83, 270);
            updateView(findViewById3, 53, 90);
            updateView(findViewById4, 85, 180);
        } else if (this.mRotation == 3) {
            updateView(findViewById, 83, 270);
            updateView(findViewById2, 85, 180);
            updateView(findViewById3, 51, 0);
            updateView(findViewById4, 53, 90);
        } else if (this.mRotation == 2) {
            updateView(findViewById, 85, 180);
            updateView(findViewById2, 53, 90);
            updateView(findViewById3, 83, 270);
            updateView(findViewById4, 51, 0);
        }
        updateWindowVisibilities();
    }

    private void updateView(View view, int i, int i2) {
        ((FrameLayout.LayoutParams) view.getLayoutParams()).gravity = i;
        view.setRotation(i2);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateWindowVisibilities() {
        updateWindowVisibility(this.mOverlay);
        updateWindowVisibility(this.mBottomOverlay);
    }

    private void updateWindowVisibility(View view) {
        int i = 0;
        boolean z = shouldDrawCutout() && view.findViewById(R.id.display_cutout).getVisibility() == 0;
        boolean hasRoundedCorners = hasRoundedCorners();
        if (!z && !hasRoundedCorners) {
            i = 8;
        }
        view.setVisibility(i);
    }

    private boolean hasRoundedCorners() {
        return this.mRoundedDefault > 0 || this.mRoundedDefaultBottom > 0 || this.mRoundedDefaultTop > 0;
    }

    private boolean shouldDrawCutout() {
        return shouldDrawCutout(this.mContext);
    }

    static boolean shouldDrawCutout(Context context) {
        return context.getResources().getBoolean(17956972);
    }

    private void setupPadding(int i) {
        StatusBar statusBar = (StatusBar) getComponent(StatusBar.class);
        StatusBarWindowView statusBarWindow = statusBar != null ? statusBar.getStatusBarWindow() : null;
        if (statusBarWindow != null) {
            TunablePadding.addTunablePadding(statusBarWindow.findViewById(R.id.keyguard_header), "sysui_rounded_content_padding", i, 2);
            FragmentHostManager fragmentHostManager = FragmentHostManager.get(statusBarWindow);
            fragmentHostManager.addTagListener("CollapsedStatusBarFragment", new TunablePaddingTagListener(i, R.id.status_bar));
            fragmentHostManager.addTagListener(QS.TAG, new TunablePaddingTagListener(i, R.id.header));
        }
    }

    WindowManager.LayoutParams getWindowLayoutParams() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-1, -2, 2024, 545259816, -3);
        layoutParams.privateFlags |= 80;
        if (!DEBUG_SCREENSHOT_ROUNDED_CORNERS) {
            layoutParams.privateFlags |= 1048576;
        }
        layoutParams.setTitle("ScreenDecorOverlay");
        if (this.mRotation == 2 || this.mRotation == 3) {
            layoutParams.gravity = 85;
        } else {
            layoutParams.gravity = 51;
        }
        layoutParams.layoutInDisplayCutoutMode = 1;
        if (isLandscape(this.mRotation)) {
            layoutParams.width = -2;
            layoutParams.height = -1;
        }
        return layoutParams;
    }

    private WindowManager.LayoutParams getBottomLayoutParams() {
        WindowManager.LayoutParams windowLayoutParams = getWindowLayoutParams();
        windowLayoutParams.setTitle("ScreenDecorOverlayBottom");
        if (this.mRotation == 2 || this.mRotation == 3) {
            windowLayoutParams.gravity = 51;
        } else {
            windowLayoutParams.gravity = 85;
        }
        return windowLayoutParams;
    }

    private void updateLayoutParams() {
        this.mWindowManager.updateViewLayout(this.mOverlay, getWindowLayoutParams());
        this.mWindowManager.updateViewLayout(this.mBottomOverlay, getBottomLayoutParams());
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        if (this.mOverlay != null && "sysui_rounded_size".equals(str)) {
            int i = this.mRoundedDefault;
            int i2 = this.mRoundedDefaultTop;
            int i3 = this.mRoundedDefaultBottom;
            if (str2 != null) {
                try {
                    i = (int) (Integer.parseInt(str2) * this.mDensity);
                } catch (Exception e) {
                }
            }
            if (i2 == 0) {
                i2 = i;
            }
            if (i3 != 0) {
                i = i3;
            }
            setSize(this.mOverlay.findViewById(R.id.left), i2);
            setSize(this.mOverlay.findViewById(R.id.right), i2);
            setSize(this.mBottomOverlay.findViewById(R.id.left), i);
            setSize(this.mBottomOverlay.findViewById(R.id.right), i);
        }
    }

    private void setSize(View view, int i) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = i;
        layoutParams.height = i;
        view.setLayoutParams(layoutParams);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static class TunablePaddingTagListener implements FragmentHostManager.FragmentListener {
        private final int mId;
        private final int mPadding;
        private TunablePadding mTunablePadding;

        public TunablePaddingTagListener(int i, int i2) {
            this.mPadding = i;
            this.mId = i2;
        }

        @Override // com.android.systemui.fragments.FragmentHostManager.FragmentListener
        public void onFragmentViewCreated(String str, Fragment fragment) {
            if (this.mTunablePadding != null) {
                this.mTunablePadding.destroy();
            }
            View view = fragment.getView();
            if (this.mId != 0) {
                view = view.findViewById(this.mId);
            }
            this.mTunablePadding = TunablePadding.addTunablePadding(view, "sysui_rounded_content_padding", this.mPadding, 3);
        }
    }

    /* loaded from: classes.dex */
    public static class DisplayCutoutView extends View implements DisplayManager.DisplayListener, RegionInterceptingFrameLayout.RegionInterceptableView {
        private final Path mBoundingPath;
        private final Rect mBoundingRect;
        private final Region mBounds;
        private int mColor;
        private final DisplayInfo mInfo;
        private final int[] mLocation;
        private final Paint mPaint;
        private final boolean mStart;
        private final Runnable mVisibilityChangedListener;

        public DisplayCutoutView(Context context, boolean z, Runnable runnable) {
            super(context);
            this.mInfo = new DisplayInfo();
            this.mPaint = new Paint();
            this.mBounds = new Region();
            this.mBoundingRect = new Rect();
            this.mBoundingPath = new Path();
            this.mLocation = new int[2];
            this.mColor = -16777216;
            this.mStart = z;
            this.mVisibilityChangedListener = runnable;
            setId(R.id.display_cutout);
        }

        public void setColor(int i) {
            this.mColor = i;
            invalidate();
        }

        @Override // android.view.View
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            ((DisplayManager) this.mContext.getSystemService(DisplayManager.class)).registerDisplayListener(this, getHandler());
            update();
        }

        @Override // android.view.View
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            ((DisplayManager) this.mContext.getSystemService(DisplayManager.class)).unregisterDisplayListener(this);
        }

        @Override // android.view.View
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            getLocationOnScreen(this.mLocation);
            canvas.translate(-this.mLocation[0], -this.mLocation[1]);
            if (!this.mBoundingPath.isEmpty()) {
                this.mPaint.setColor(this.mColor);
                this.mPaint.setStyle(Paint.Style.FILL);
                this.mPaint.setAntiAlias(true);
                canvas.drawPath(this.mBoundingPath, this.mPaint);
            }
        }

        @Override // android.hardware.display.DisplayManager.DisplayListener
        public void onDisplayAdded(int i) {
        }

        @Override // android.hardware.display.DisplayManager.DisplayListener
        public void onDisplayRemoved(int i) {
        }

        @Override // android.hardware.display.DisplayManager.DisplayListener
        public void onDisplayChanged(int i) {
            if (i == getDisplay().getDisplayId()) {
                update();
            }
        }

        private void update() {
            int i;
            requestLayout();
            getDisplay().getDisplayInfo(this.mInfo);
            this.mBounds.setEmpty();
            this.mBoundingRect.setEmpty();
            this.mBoundingPath.reset();
            if (ScreenDecorations.shouldDrawCutout(getContext()) && hasCutout()) {
                this.mBounds.set(this.mInfo.displayCutout.getBounds());
                localBounds(this.mBoundingRect);
                updateBoundingPath();
                invalidate();
                i = 0;
            } else {
                i = 8;
            }
            if (i != getVisibility()) {
                setVisibility(i);
                this.mVisibilityChangedListener.run();
            }
        }

        private void updateBoundingPath() {
            int i = this.mInfo.logicalWidth;
            int i2 = this.mInfo.logicalHeight;
            boolean z = true;
            if (this.mInfo.rotation != 1 && this.mInfo.rotation != 3) {
                z = false;
            }
            int i3 = z ? i2 : i;
            if (!z) {
                i = i2;
            }
            this.mBoundingPath.set(DisplayCutout.pathFromResources(getResources(), i3, i));
            Matrix matrix = new Matrix();
            transformPhysicalToLogicalCoordinates(this.mInfo.rotation, i3, i, matrix);
            this.mBoundingPath.transform(matrix);
        }

        private static void transformPhysicalToLogicalCoordinates(int i, int i2, int i3, Matrix matrix) {
            switch (i) {
                case 0:
                    matrix.reset();
                    return;
                case 1:
                    matrix.setRotate(270.0f);
                    matrix.postTranslate(0.0f, i2);
                    return;
                case 2:
                    matrix.setRotate(180.0f);
                    matrix.postTranslate(i2, i3);
                    return;
                case 3:
                    matrix.setRotate(90.0f);
                    matrix.postTranslate(i3, 0.0f);
                    return;
                default:
                    throw new IllegalArgumentException("Unknown rotation: " + i);
            }
        }

        private boolean hasCutout() {
            DisplayCutout displayCutout = this.mInfo.displayCutout;
            if (displayCutout == null) {
                return false;
            }
            return this.mStart ? displayCutout.getSafeInsetLeft() > 0 || displayCutout.getSafeInsetTop() > 0 : displayCutout.getSafeInsetRight() > 0 || displayCutout.getSafeInsetBottom() > 0;
        }

        @Override // android.view.View
        protected void onMeasure(int i, int i2) {
            if (this.mBounds.isEmpty()) {
                super.onMeasure(i, i2);
            } else {
                setMeasuredDimension(resolveSizeAndState(this.mBoundingRect.width(), i, 0), resolveSizeAndState(this.mBoundingRect.height(), i2, 0));
            }
        }

        public static void boundsFromDirection(DisplayCutout displayCutout, int i, Rect rect) {
            Region bounds = displayCutout.getBounds();
            if (i == 3) {
                bounds.op(0, 0, displayCutout.getSafeInsetLeft(), Integer.MAX_VALUE, Region.Op.INTERSECT);
                rect.set(bounds.getBounds());
            } else if (i == 5) {
                bounds.op(displayCutout.getSafeInsetLeft() + 1, 0, Integer.MAX_VALUE, Integer.MAX_VALUE, Region.Op.INTERSECT);
                rect.set(bounds.getBounds());
            } else if (i == 48) {
                bounds.op(0, 0, Integer.MAX_VALUE, displayCutout.getSafeInsetTop(), Region.Op.INTERSECT);
                rect.set(bounds.getBounds());
            } else if (i == 80) {
                bounds.op(0, displayCutout.getSafeInsetTop() + 1, Integer.MAX_VALUE, Integer.MAX_VALUE, Region.Op.INTERSECT);
                rect.set(bounds.getBounds());
            }
            bounds.recycle();
        }

        private void localBounds(Rect rect) {
            DisplayCutout displayCutout = this.mInfo.displayCutout;
            if (this.mStart) {
                if (displayCutout.getSafeInsetLeft() > 0) {
                    boundsFromDirection(displayCutout, 3, rect);
                } else if (displayCutout.getSafeInsetTop() > 0) {
                    boundsFromDirection(displayCutout, 48, rect);
                }
            } else if (displayCutout.getSafeInsetRight() > 0) {
                boundsFromDirection(displayCutout, 5, rect);
            } else if (displayCutout.getSafeInsetBottom() > 0) {
                boundsFromDirection(displayCutout, 80, rect);
            }
        }

        @Override // com.android.systemui.RegionInterceptingFrameLayout.RegionInterceptableView
        public boolean shouldInterceptTouch() {
            return this.mInfo.displayCutout != null && getVisibility() == 0;
        }

        @Override // com.android.systemui.RegionInterceptingFrameLayout.RegionInterceptableView
        public Region getInterceptRegion() {
            if (this.mInfo.displayCutout == null) {
                return null;
            }
            View rootView = getRootView();
            Region bounds = this.mInfo.displayCutout.getBounds();
            rootView.getLocationOnScreen(this.mLocation);
            bounds.translate(-this.mLocation[0], -this.mLocation[1]);
            bounds.op(rootView.getLeft(), rootView.getTop(), rootView.getRight(), rootView.getBottom(), Region.Op.INTERSECT);
            return bounds;
        }
    }

    private boolean isLandscape(int i) {
        return i == 1 || i == 2;
    }
}

package com.android.launcher3;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Region;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.widget.TextView;
import com.android.launcher3.BaseRecyclerViewFastScrollBar;
import com.android.launcher3.FastBitmapDrawable;
import com.android.launcher3.IconCache;
import com.android.launcher3.compat.PackageInstallerCompat;
import com.android.launcher3.model.PackageItemInfo;
import java.text.NumberFormat;
/* loaded from: a.zip:com/android/launcher3/BubbleTextView.class */
public class BubbleTextView extends TextView implements BaseRecyclerViewFastScrollBar.FastScrollFocusableView {

    /* renamed from: -com-android-launcher3-FastBitmapDrawable$StateSwitchesValues  reason: not valid java name */
    private static final int[] f1comandroidlauncher3FastBitmapDrawable$StateSwitchesValues = null;
    private static SparseArray<Resources.Theme> sPreloaderThemes = new SparseArray<>(2);
    private final Drawable mBackground;
    private boolean mBackgroundSizeChanged;
    private final boolean mCustomShadowsEnabled;
    private final boolean mDeferShadowGenerationOnTouch;
    private boolean mDisableRelayout;
    private Drawable mIcon;
    private IconCache.IconLoadRequest mIconLoadRequest;
    private final int mIconSize;
    private boolean mIgnorePressedStateChange;
    private final Launcher mLauncher;
    private final boolean mLayoutHorizontal;
    private final CheckLongPressHelper mLongPressHelper;
    private final HolographicOutlineHelper mOutlineHelper;
    private Bitmap mPressedBackground;
    private float mSlop;
    private boolean mStayPressed;
    private final StylusEventHelper mStylusEventHelper;
    private int mTextColor;

    /* loaded from: a.zip:com/android/launcher3/BubbleTextView$BubbleTextShadowHandler.class */
    public interface BubbleTextShadowHandler {
        void setPressedIcon(BubbleTextView bubbleTextView, Bitmap bitmap);
    }

    /* renamed from: -getcom-android-launcher3-FastBitmapDrawable$StateSwitchesValues  reason: not valid java name */
    private static /* synthetic */ int[] m157getcomandroidlauncher3FastBitmapDrawable$StateSwitchesValues() {
        if (f1comandroidlauncher3FastBitmapDrawable$StateSwitchesValues != null) {
            return f1comandroidlauncher3FastBitmapDrawable$StateSwitchesValues;
        }
        int[] iArr = new int[FastBitmapDrawable.State.valuesCustom().length];
        try {
            iArr[FastBitmapDrawable.State.DISABLED.ordinal()] = 3;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[FastBitmapDrawable.State.FAST_SCROLL_HIGHLIGHTED.ordinal()] = 1;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[FastBitmapDrawable.State.FAST_SCROLL_UNHIGHLIGHTED.ordinal()] = 4;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[FastBitmapDrawable.State.NORMAL.ordinal()] = 2;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[FastBitmapDrawable.State.PRESSED.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        f1comandroidlauncher3FastBitmapDrawable$StateSwitchesValues = iArr;
        return iArr;
    }

    public BubbleTextView(Context context) {
        this(context, null, 0);
    }

    public BubbleTextView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public BubbleTextView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mDisableRelayout = false;
        this.mLauncher = (Launcher) context;
        DeviceProfile deviceProfile = this.mLauncher.getDeviceProfile();
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.BubbleTextView, i, 0);
        this.mCustomShadowsEnabled = obtainStyledAttributes.getBoolean(4, true);
        this.mLayoutHorizontal = obtainStyledAttributes.getBoolean(0, false);
        this.mDeferShadowGenerationOnTouch = obtainStyledAttributes.getBoolean(3, false);
        int integer = obtainStyledAttributes.getInteger(2, 0);
        int i2 = deviceProfile.iconSizePx;
        if (integer == 0) {
            setTextSize(0, deviceProfile.iconTextSizePx);
        } else if (integer == 1) {
            setTextSize(2, deviceProfile.allAppsIconTextSizeSp);
            i2 = deviceProfile.allAppsIconSizePx;
        }
        this.mIconSize = obtainStyledAttributes.getDimensionPixelSize(1, i2);
        obtainStyledAttributes.recycle();
        if (this.mCustomShadowsEnabled) {
            this.mBackground = getBackground();
            setBackground(null);
        } else {
            this.mBackground = null;
        }
        this.mLongPressHelper = new CheckLongPressHelper(this);
        this.mStylusEventHelper = new StylusEventHelper(this);
        this.mOutlineHelper = HolographicOutlineHelper.obtain(getContext());
        if (this.mCustomShadowsEnabled) {
            setShadowLayer(4.0f, 0.0f, 2.0f, -587202560);
        }
        setAccessibilityDelegate(LauncherAppState.getInstance().getAccessibilityDelegate());
    }

    private Resources.Theme getPreloaderTheme() {
        Object tag = getTag();
        int i = (tag == null || !(tag instanceof ShortcutInfo) || ((ShortcutInfo) tag).container < 0) ? 2131755017 : 2131755018;
        Resources.Theme theme = sPreloaderThemes.get(i);
        Resources.Theme theme2 = theme;
        if (theme == null) {
            theme2 = getResources().newTheme();
            theme2.applyStyle(i, true);
            sPreloaderThemes.put(i, theme2);
        }
        return theme2;
    }

    private static int getStartDelayForStateChange(FastBitmapDrawable.State state, FastBitmapDrawable.State state2) {
        switch (m157getcomandroidlauncher3FastBitmapDrawable$StateSwitchesValues()[state2.ordinal()]) {
            case 2:
                switch (m157getcomandroidlauncher3FastBitmapDrawable$StateSwitchesValues()[state.ordinal()]) {
                    case 1:
                        return 68;
                    default:
                        return 0;
                }
            default:
                return 0;
        }
    }

    @TargetApi(17)
    private Drawable setIcon(Drawable drawable, int i) {
        this.mIcon = drawable;
        if (i != -1) {
            this.mIcon.setBounds(0, 0, i, i);
        }
        if (!this.mLayoutHorizontal) {
            setCompoundDrawables(null, this.mIcon, null, null);
        } else if (Utilities.ATLEAST_JB_MR1) {
            setCompoundDrawablesRelative(this.mIcon, null, null, null);
        } else {
            setCompoundDrawables(this.mIcon, null, null, null);
        }
        return drawable;
    }

    private void updateIconState() {
        if (this.mIcon instanceof FastBitmapDrawable) {
            FastBitmapDrawable fastBitmapDrawable = (FastBitmapDrawable) this.mIcon;
            if ((getTag() instanceof ItemInfo) && ((ItemInfo) getTag()).isDisabled()) {
                fastBitmapDrawable.animateState(FastBitmapDrawable.State.DISABLED);
            } else if (isPressed() || this.mStayPressed) {
                fastBitmapDrawable.animateState(FastBitmapDrawable.State.PRESSED);
            } else {
                fastBitmapDrawable.animateState(FastBitmapDrawable.State.NORMAL);
            }
        }
    }

    public void applyDummyInfo() {
        setIcon(this.mLauncher.resizeIconDrawable(new ColorDrawable()), this.mIconSize);
        setText("");
    }

    public void applyFromApplicationInfo(AppInfo appInfo) {
        FastBitmapDrawable createIconDrawable = this.mLauncher.createIconDrawable(appInfo.iconBitmap);
        if (appInfo.isDisabled()) {
            createIconDrawable.setState(FastBitmapDrawable.State.DISABLED);
        }
        setIcon(createIconDrawable, this.mIconSize);
        setText(appInfo.title);
        if (appInfo.contentDescription != null) {
            setContentDescription(appInfo.contentDescription);
        }
        super.setTag(appInfo);
        verifyHighRes();
    }

    public void applyFromPackageItemInfo(PackageItemInfo packageItemInfo) {
        setIcon(this.mLauncher.createIconDrawable(packageItemInfo.iconBitmap), this.mIconSize);
        setText(packageItemInfo.title);
        if (packageItemInfo.contentDescription != null) {
            setContentDescription(packageItemInfo.contentDescription);
        }
        super.setTag(packageItemInfo);
        verifyHighRes();
    }

    public void applyFromShortcutInfo(ShortcutInfo shortcutInfo, IconCache iconCache) {
        applyFromShortcutInfo(shortcutInfo, iconCache, false);
    }

    public void applyFromShortcutInfo(ShortcutInfo shortcutInfo, IconCache iconCache, boolean z) {
        FastBitmapDrawable createIconDrawable = this.mLauncher.createIconDrawable(shortcutInfo.getIcon(iconCache));
        if (shortcutInfo.isDisabled()) {
            createIconDrawable.setState(FastBitmapDrawable.State.DISABLED);
        }
        setIcon(createIconDrawable, this.mIconSize);
        if (shortcutInfo.contentDescription != null) {
            setContentDescription(shortcutInfo.contentDescription);
        }
        setText(shortcutInfo.title);
        setTag(shortcutInfo);
        if (z || shortcutInfo.isPromise()) {
            applyState(z);
        }
    }

    public void applyState(boolean z) {
        PreloadIconDrawable preloadIconDrawable;
        if (getTag() instanceof ShortcutInfo) {
            ShortcutInfo shortcutInfo = (ShortcutInfo) getTag();
            int installProgress = shortcutInfo.isPromise() ? shortcutInfo.hasStatusFlag(4) ? shortcutInfo.getInstallProgress() : 0 : 100;
            setContentDescription(installProgress > 0 ? getContext().getString(2131558463, shortcutInfo.title, NumberFormat.getPercentInstance().format(installProgress * 0.01d)) : getContext().getString(2131558464, shortcutInfo.title));
            if (this.mIcon != null) {
                if (this.mIcon instanceof PreloadIconDrawable) {
                    preloadIconDrawable = (PreloadIconDrawable) this.mIcon;
                } else {
                    preloadIconDrawable = new PreloadIconDrawable(this.mIcon, getPreloaderTheme());
                    setIcon(preloadIconDrawable, this.mIconSize);
                }
                preloadIconDrawable.setLevel(installProgress);
                if (z) {
                    preloadIconDrawable.maybePerformFinishedAnimation();
                }
            }
        }
    }

    @Override // android.widget.TextView, android.view.View
    public void cancelLongPress() {
        super.cancelLongPress();
        this.mLongPressHelper.cancelLongPress();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void clearPressedBackground() {
        setPressed(false);
        setStayPressed(false);
    }

    @Override // android.view.View
    public void draw(Canvas canvas) {
        if (!this.mCustomShadowsEnabled) {
            super.draw(canvas);
            return;
        }
        Drawable drawable = this.mBackground;
        if (drawable != null) {
            int scrollX = getScrollX();
            int scrollY = getScrollY();
            if (this.mBackgroundSizeChanged) {
                drawable.setBounds(0, 0, getRight() - getLeft(), getBottom() - getTop());
                this.mBackgroundSizeChanged = false;
            }
            if ((scrollX | scrollY) == 0) {
                drawable.draw(canvas);
            } else {
                canvas.translate(scrollX, scrollY);
                drawable.draw(canvas);
                canvas.translate(-scrollX, -scrollY);
            }
        }
        if (getCurrentTextColor() == getResources().getColor(17170445)) {
            getPaint().clearShadowLayer();
            super.draw(canvas);
            return;
        }
        getPaint().setShadowLayer(4.0f, 0.0f, 2.0f, -587202560);
        super.draw(canvas);
        canvas.save(2);
        canvas.clipRect(getScrollX(), getScrollY() + getExtendedPaddingTop(), getScrollX() + getWidth(), getScrollY() + getHeight(), Region.Op.INTERSECT);
        getPaint().setShadowLayer(1.75f, 0.0f, 0.0f, -872415232);
        super.draw(canvas);
        canvas.restore();
    }

    public Drawable getIcon() {
        return this.mIcon;
    }

    public boolean isLayoutHorizontal() {
        return this.mLayoutHorizontal;
    }

    @Override // android.widget.TextView, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mBackground != null) {
            this.mBackground.setCallback(this);
        }
        if (this.mIcon instanceof PreloadIconDrawable) {
            ((PreloadIconDrawable) this.mIcon).applyPreloaderTheme(getPreloaderTheme());
        }
        this.mSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    @Override // android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mBackground != null) {
            this.mBackground.setCallback(null);
        }
    }

    @Override // android.widget.TextView, android.view.View, android.view.KeyEvent.Callback
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        if (super.onKeyDown(i, keyEvent)) {
            if (this.mPressedBackground == null) {
                this.mPressedBackground = this.mOutlineHelper.createMediumDropShadow(this);
                return true;
            }
            return true;
        }
        return false;
    }

    @Override // android.widget.TextView, android.view.View, android.view.KeyEvent.Callback
    public boolean onKeyUp(int i, KeyEvent keyEvent) {
        this.mIgnorePressedStateChange = true;
        boolean onKeyUp = super.onKeyUp(i, keyEvent);
        this.mPressedBackground = null;
        this.mIgnorePressedStateChange = false;
        updateIconState();
        return onKeyUp;
    }

    @Override // android.widget.TextView, android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        boolean onTouchEvent = super.onTouchEvent(motionEvent);
        if (this.mStylusEventHelper.checkAndPerformStylusEvent(motionEvent)) {
            this.mLongPressHelper.cancelLongPress();
            onTouchEvent = true;
        }
        switch (motionEvent.getAction()) {
            case PackageInstallerCompat.STATUS_INSTALLED /* 0 */:
                if (!this.mDeferShadowGenerationOnTouch && this.mPressedBackground == null) {
                    this.mPressedBackground = this.mOutlineHelper.createMediumDropShadow(this);
                }
                if (!this.mStylusEventHelper.inStylusButtonPressed()) {
                    this.mLongPressHelper.postCheckForLongPress();
                    break;
                }
                break;
            case 1:
            case 3:
                if (!isPressed()) {
                    this.mPressedBackground = null;
                }
                this.mLongPressHelper.cancelLongPress();
                break;
            case 2:
                if (!Utilities.pointInView(this, motionEvent.getX(), motionEvent.getY(), this.mSlop)) {
                    this.mLongPressHelper.cancelLongPress();
                    break;
                }
                break;
        }
        return onTouchEvent;
    }

    public void reapplyItemInfo(ItemInfo itemInfo) {
        View homescreenIconByItemId;
        if (getTag() == itemInfo) {
            FastBitmapDrawable.State state = FastBitmapDrawable.State.NORMAL;
            if (this.mIcon instanceof FastBitmapDrawable) {
                state = ((FastBitmapDrawable) this.mIcon).getCurrentState();
            }
            this.mIconLoadRequest = null;
            this.mDisableRelayout = true;
            if (itemInfo instanceof AppInfo) {
                applyFromApplicationInfo((AppInfo) itemInfo);
            } else if (itemInfo instanceof ShortcutInfo) {
                applyFromShortcutInfo((ShortcutInfo) itemInfo, LauncherAppState.getInstance().getIconCache());
                if (itemInfo.rank < 3 && itemInfo.container >= 0 && (homescreenIconByItemId = this.mLauncher.getWorkspace().getHomescreenIconByItemId(itemInfo.container)) != null) {
                    homescreenIconByItemId.invalidate();
                }
            } else if (itemInfo instanceof PackageItemInfo) {
                applyFromPackageItemInfo((PackageItemInfo) itemInfo);
            }
            if (this.mIcon instanceof FastBitmapDrawable) {
                ((FastBitmapDrawable) this.mIcon).setState(state);
            }
            this.mDisableRelayout = false;
        }
    }

    @Override // android.view.View
    public void requestLayout() {
        if (this.mDisableRelayout) {
            return;
        }
        super.requestLayout();
    }

    @Override // com.android.launcher3.BaseRecyclerViewFastScrollBar.FastScrollFocusableView
    public void setFastScrollFocusState(FastBitmapDrawable.State state, boolean z) {
        if (this.mIcon instanceof FastBitmapDrawable) {
            FastBitmapDrawable fastBitmapDrawable = (FastBitmapDrawable) this.mIcon;
            if (z) {
                FastBitmapDrawable.State currentState = fastBitmapDrawable.getCurrentState();
                if (fastBitmapDrawable.animateState(state)) {
                    animate().scaleX(state.viewScale).scaleY(state.viewScale).setStartDelay(getStartDelayForStateChange(currentState, state)).setDuration(FastBitmapDrawable.getDurationForStateChange(currentState, state)).start();
                }
            } else if (fastBitmapDrawable.setState(state)) {
                animate().cancel();
                setScaleX(state.viewScale);
                setScaleY(state.viewScale);
            }
        }
    }

    @Override // android.widget.TextView
    protected boolean setFrame(int i, int i2, int i3, int i4) {
        if (getLeft() != i || getRight() != i3 || getTop() != i2 || getBottom() != i4) {
            this.mBackgroundSizeChanged = true;
        }
        return super.setFrame(i, i2, i3, i4);
    }

    public void setLongPressTimeout(int i) {
        this.mLongPressHelper.setLongPressTimeout(i);
    }

    @Override // android.view.View
    public void setPressed(boolean z) {
        super.setPressed(z);
        if (this.mIgnorePressedStateChange) {
            return;
        }
        updateIconState();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setStayPressed(boolean z) {
        this.mStayPressed = z;
        if (!z) {
            this.mPressedBackground = null;
        } else if (this.mPressedBackground == null) {
            this.mPressedBackground = this.mOutlineHelper.createMediumDropShadow(this);
        }
        ViewParent parent = getParent();
        if (parent != null && (parent.getParent() instanceof BubbleTextShadowHandler)) {
            ((BubbleTextShadowHandler) parent.getParent()).setPressedIcon(this, this.mPressedBackground);
        }
        updateIconState();
    }

    @Override // android.view.View
    public void setTag(Object obj) {
        if (obj != null) {
            LauncherModel.checkItemInfo((ItemInfo) obj);
        }
        super.setTag(obj);
    }

    @Override // android.widget.TextView
    public void setTextColor(int i) {
        this.mTextColor = i;
        super.setTextColor(i);
    }

    @Override // android.widget.TextView
    public void setTextColor(ColorStateList colorStateList) {
        this.mTextColor = colorStateList.getDefaultColor();
        super.setTextColor(colorStateList);
    }

    public void setTextVisibility(boolean z) {
        Resources resources = getResources();
        if (z) {
            super.setTextColor(this.mTextColor);
        } else {
            super.setTextColor(resources.getColor(17170445));
        }
    }

    @Override // android.widget.TextView, android.view.View
    protected boolean verifyDrawable(Drawable drawable) {
        return drawable != this.mBackground ? super.verifyDrawable(drawable) : true;
    }

    public void verifyHighRes() {
        if (this.mIconLoadRequest != null) {
            this.mIconLoadRequest.cancel();
            this.mIconLoadRequest = null;
        }
        if (getTag() instanceof AppInfo) {
            AppInfo appInfo = (AppInfo) getTag();
            if (appInfo.usingLowResIcon) {
                this.mIconLoadRequest = LauncherAppState.getInstance().getIconCache().updateIconInBackground(this, appInfo);
            }
        } else if (getTag() instanceof ShortcutInfo) {
            ShortcutInfo shortcutInfo = (ShortcutInfo) getTag();
            if (shortcutInfo.usingLowResIcon) {
                this.mIconLoadRequest = LauncherAppState.getInstance().getIconCache().updateIconInBackground(this, shortcutInfo);
            }
        } else if (getTag() instanceof PackageItemInfo) {
            PackageItemInfo packageItemInfo = (PackageItemInfo) getTag();
            if (packageItemInfo.usingLowResIcon) {
                this.mIconLoadRequest = LauncherAppState.getInstance().getIconCache().updateIconInBackground(this, packageItemInfo);
            }
        }
    }
}

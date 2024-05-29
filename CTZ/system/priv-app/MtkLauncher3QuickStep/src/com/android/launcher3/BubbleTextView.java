package com.android.launcher3;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.ColorUtils;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Property;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewDebug;
import android.widget.TextView;
import com.android.launcher3.IconCache;
import com.android.launcher3.Launcher;
import com.android.launcher3.badge.BadgeInfo;
import com.android.launcher3.badge.BadgeRenderer;
import com.android.launcher3.folder.FolderIcon;
import com.android.launcher3.graphics.DrawableFactory;
import com.android.launcher3.graphics.IconPalette;
import com.android.launcher3.graphics.PreloadIconDrawable;
import com.android.launcher3.model.PackageItemInfo;
import java.text.NumberFormat;
/* loaded from: classes.dex */
public class BubbleTextView extends TextView implements IconCache.ItemInfoUpdateReceiver, Launcher.OnResumeCallback {
    private static final int DISPLAY_ALL_APPS = 1;
    private static final int DISPLAY_FOLDER = 2;
    private static final int DISPLAY_WORKSPACE = 0;
    private final BaseDraggingActivity mActivity;
    private int mBadgeColor;
    private BadgeInfo mBadgeInfo;
    private BadgeRenderer mBadgeRenderer;
    private float mBadgeScale;
    private final boolean mCenterVertically;
    @ViewDebug.ExportedProperty(category = "launcher")
    private boolean mDisableRelayout;
    private boolean mForceHideBadge;
    private Drawable mIcon;
    private IconCache.IconLoadRequest mIconLoadRequest;
    private final int mIconSize;
    @ViewDebug.ExportedProperty(category = "launcher")
    private boolean mIgnorePressedStateChange;
    @ViewDebug.ExportedProperty(category = "launcher")
    private boolean mIsIconVisible;
    private final boolean mLayoutHorizontal;
    private final CheckLongPressHelper mLongPressHelper;
    private final float mSlop;
    @ViewDebug.ExportedProperty(category = "launcher")
    private boolean mStayPressed;
    private final StylusEventHelper mStylusEventHelper;
    private Rect mTempIconBounds;
    private Point mTempSpaceForBadgeOffset;
    @ViewDebug.ExportedProperty(category = "launcher")
    private float mTextAlpha;
    @ViewDebug.ExportedProperty(category = "launcher")
    private int mTextColor;
    private static final int[] STATE_PRESSED = {16842919};
    private static final Property<BubbleTextView, Float> BADGE_SCALE_PROPERTY = new Property<BubbleTextView, Float>(Float.TYPE, "badgeScale") { // from class: com.android.launcher3.BubbleTextView.1
        @Override // android.util.Property
        public Float get(BubbleTextView bubbleTextView) {
            return Float.valueOf(bubbleTextView.mBadgeScale);
        }

        @Override // android.util.Property
        public void set(BubbleTextView bubbleTextView, Float f) {
            bubbleTextView.mBadgeScale = f.floatValue();
            bubbleTextView.invalidate();
        }
    };
    public static final Property<BubbleTextView, Float> TEXT_ALPHA_PROPERTY = new Property<BubbleTextView, Float>(Float.class, "textAlpha") { // from class: com.android.launcher3.BubbleTextView.2
        @Override // android.util.Property
        public Float get(BubbleTextView bubbleTextView) {
            return Float.valueOf(bubbleTextView.mTextAlpha);
        }

        @Override // android.util.Property
        public void set(BubbleTextView bubbleTextView, Float f) {
            bubbleTextView.setTextAlpha(f.floatValue());
        }
    };

    public BubbleTextView(Context context) {
        this(context, null, 0);
    }

    public BubbleTextView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public BubbleTextView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mIsIconVisible = true;
        this.mTextAlpha = 1.0f;
        this.mTempSpaceForBadgeOffset = new Point();
        this.mTempIconBounds = new Rect();
        this.mDisableRelayout = false;
        this.mActivity = BaseDraggingActivity.fromContext(context);
        DeviceProfile deviceProfile = this.mActivity.getDeviceProfile();
        this.mSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.BubbleTextView, i, 0);
        this.mLayoutHorizontal = obtainStyledAttributes.getBoolean(3, false);
        int integer = obtainStyledAttributes.getInteger(1, 0);
        int i2 = deviceProfile.iconSizePx;
        if (integer == 0) {
            setTextSize(0, deviceProfile.iconTextSizePx);
            setCompoundDrawablePadding(deviceProfile.iconDrawablePaddingPx);
        } else if (integer == 1) {
            setTextSize(0, deviceProfile.allAppsIconTextSizePx);
            setCompoundDrawablePadding(deviceProfile.allAppsIconDrawablePaddingPx);
            i2 = deviceProfile.allAppsIconSizePx;
        } else if (integer == 2) {
            setTextSize(0, deviceProfile.folderChildTextSizePx);
            setCompoundDrawablePadding(deviceProfile.folderChildDrawablePaddingPx);
            i2 = deviceProfile.folderChildIconSizePx;
        }
        this.mCenterVertically = obtainStyledAttributes.getBoolean(0, false);
        this.mIconSize = obtainStyledAttributes.getDimensionPixelSize(2, i2);
        obtainStyledAttributes.recycle();
        this.mLongPressHelper = new CheckLongPressHelper(this);
        this.mStylusEventHelper = new StylusEventHelper(new SimpleOnStylusPressListener(this), this);
        setEllipsize(TextUtils.TruncateAt.END);
        setAccessibilityDelegate(this.mActivity.getAccessibilityDelegate());
        setTextAlpha(1.0f);
    }

    @Override // android.widget.TextView, android.view.View
    protected void onFocusChanged(boolean z, int i, Rect rect) {
        setEllipsize(z ? TextUtils.TruncateAt.MARQUEE : TextUtils.TruncateAt.END);
        super.onFocusChanged(z, i, rect);
    }

    public void reset() {
        this.mBadgeInfo = null;
        this.mBadgeColor = 0;
        this.mBadgeScale = 0.0f;
        this.mForceHideBadge = false;
    }

    public void applyFromShortcutInfo(ShortcutInfo shortcutInfo) {
        applyFromShortcutInfo(shortcutInfo, false);
    }

    public void applyFromShortcutInfo(ShortcutInfo shortcutInfo, boolean z) {
        applyIconAndLabel(shortcutInfo);
        setTag(shortcutInfo);
        if (z || shortcutInfo.hasPromiseIconUi()) {
            applyPromiseState(z);
        }
        applyBadgeState(shortcutInfo, false);
    }

    public void applyFromApplicationInfo(AppInfo appInfo) {
        applyIconAndLabel(appInfo);
        super.setTag(appInfo);
        verifyHighRes();
        if (appInfo instanceof PromiseAppInfo) {
            applyProgressLevel(((PromiseAppInfo) appInfo).level);
        }
        applyBadgeState(appInfo, false);
    }

    public void applyFromPackageItemInfo(PackageItemInfo packageItemInfo) {
        applyIconAndLabel(packageItemInfo);
        super.setTag(packageItemInfo);
        verifyHighRes();
    }

    private void applyIconAndLabel(ItemInfoWithIcon itemInfoWithIcon) {
        CharSequence charSequence;
        FastBitmapDrawable newIcon = DrawableFactory.get(getContext()).newIcon(itemInfoWithIcon);
        this.mBadgeColor = IconPalette.getMutedColor(itemInfoWithIcon.iconColor, 0.54f);
        setIcon(newIcon);
        setText(itemInfoWithIcon.title);
        if (itemInfoWithIcon.contentDescription != null) {
            if (itemInfoWithIcon.isDisabled()) {
                charSequence = getContext().getString(R.string.disabled_app_label, itemInfoWithIcon.contentDescription);
            } else {
                charSequence = itemInfoWithIcon.contentDescription;
            }
            setContentDescription(charSequence);
        }
    }

    public void setLongPressTimeout(int i) {
        this.mLongPressHelper.setLongPressTimeout(i);
    }

    @Override // android.view.View
    public void setTag(Object obj) {
        if (obj != null) {
            LauncherModel.checkItemInfo((ItemInfo) obj);
        }
        super.setTag(obj);
    }

    @Override // android.view.View
    public void refreshDrawableState() {
        if (!this.mIgnorePressedStateChange) {
            super.refreshDrawableState();
        }
    }

    @Override // android.widget.TextView, android.view.View
    protected int[] onCreateDrawableState(int i) {
        int[] onCreateDrawableState = super.onCreateDrawableState(i + 1);
        if (this.mStayPressed) {
            mergeDrawableStates(onCreateDrawableState, STATE_PRESSED);
        }
        return onCreateDrawableState;
    }

    public Drawable getIcon() {
        return this.mIcon;
    }

    @Override // android.widget.TextView, android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        boolean onTouchEvent = super.onTouchEvent(motionEvent);
        if (this.mStylusEventHelper.onMotionEvent(motionEvent)) {
            this.mLongPressHelper.cancelLongPress();
            onTouchEvent = true;
        }
        switch (motionEvent.getAction()) {
            case 0:
                if (!this.mStylusEventHelper.inStylusButtonPressed()) {
                    this.mLongPressHelper.postCheckForLongPress();
                    break;
                }
                break;
            case 1:
            case 3:
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

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setStayPressed(boolean z) {
        this.mStayPressed = z;
        refreshDrawableState();
    }

    @Override // com.android.launcher3.Launcher.OnResumeCallback
    public void onLauncherResume() {
        setStayPressed(false);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void clearPressedBackground() {
        setPressed(false);
        setStayPressed(false);
    }

    @Override // android.widget.TextView, android.view.View, android.view.KeyEvent.Callback
    public boolean onKeyUp(int i, KeyEvent keyEvent) {
        this.mIgnorePressedStateChange = true;
        boolean onKeyUp = super.onKeyUp(i, keyEvent);
        this.mIgnorePressedStateChange = false;
        refreshDrawableState();
        return onKeyUp;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void drawWithoutBadge(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override // android.widget.TextView, android.view.View
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBadgeIfNecessary(canvas);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void drawBadgeIfNecessary(Canvas canvas) {
        if (this.mForceHideBadge) {
            return;
        }
        if (hasBadge() || this.mBadgeScale > 0.0f) {
            getIconBounds(this.mTempIconBounds);
            this.mTempSpaceForBadgeOffset.set((getWidth() - this.mIconSize) / 2, getPaddingTop());
            int scrollX = getScrollX();
            int scrollY = getScrollY();
            canvas.translate(scrollX, scrollY);
            this.mBadgeRenderer.draw(canvas, this.mBadgeColor, this.mTempIconBounds, this.mBadgeScale, this.mTempSpaceForBadgeOffset);
            canvas.translate(-scrollX, -scrollY);
        }
    }

    public void forceHideBadge(boolean z) {
        if (this.mForceHideBadge == z) {
            return;
        }
        this.mForceHideBadge = z;
        if (z) {
            invalidate();
        } else if (hasBadge()) {
            ObjectAnimator.ofFloat(this, BADGE_SCALE_PROPERTY, 0.0f, 1.0f).start();
        }
    }

    private boolean hasBadge() {
        return this.mBadgeInfo != null;
    }

    public void getIconBounds(Rect rect) {
        int paddingTop = getPaddingTop();
        int width = (getWidth() - this.mIconSize) / 2;
        rect.set(width, paddingTop, this.mIconSize + width, this.mIconSize + paddingTop);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.widget.TextView, android.view.View
    public void onMeasure(int i, int i2) {
        if (this.mCenterVertically) {
            Paint.FontMetrics fontMetrics = getPaint().getFontMetrics();
            int compoundDrawablePadding = this.mIconSize + getCompoundDrawablePadding() + ((int) Math.ceil(fontMetrics.bottom - fontMetrics.top));
            setPadding(getPaddingLeft(), (View.MeasureSpec.getSize(i2) - compoundDrawablePadding) / 2, getPaddingRight(), getPaddingBottom());
        }
        super.onMeasure(i, i2);
    }

    @Override // android.widget.TextView
    public void setTextColor(int i) {
        this.mTextColor = i;
        super.setTextColor(getModifiedColor());
    }

    @Override // android.widget.TextView
    public void setTextColor(ColorStateList colorStateList) {
        this.mTextColor = colorStateList.getDefaultColor();
        if (Float.compare(this.mTextAlpha, 1.0f) == 0) {
            super.setTextColor(colorStateList);
        } else {
            super.setTextColor(getModifiedColor());
        }
    }

    public boolean shouldTextBeVisible() {
        Object tag = getParent() instanceof FolderIcon ? ((View) getParent()).getTag() : getTag();
        ItemInfo itemInfo = tag instanceof ItemInfo ? (ItemInfo) tag : null;
        return itemInfo == null || itemInfo.container != -101;
    }

    public void setTextVisibility(boolean z) {
        setTextAlpha(z ? 1.0f : 0.0f);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setTextAlpha(float f) {
        this.mTextAlpha = f;
        super.setTextColor(getModifiedColor());
    }

    private int getModifiedColor() {
        if (this.mTextAlpha == 0.0f) {
            return 0;
        }
        return ColorUtils.setAlphaComponent(this.mTextColor, Math.round(Color.alpha(this.mTextColor) * this.mTextAlpha));
    }

    public ObjectAnimator createTextAlphaAnimator(boolean z) {
        return ObjectAnimator.ofFloat(this, TEXT_ALPHA_PROPERTY, (shouldTextBeVisible() && z) ? 1.0f : 0.0f);
    }

    @Override // android.widget.TextView, android.view.View
    public void cancelLongPress() {
        super.cancelLongPress();
        this.mLongPressHelper.cancelLongPress();
    }

    public void applyPromiseState(boolean z) {
        int i;
        if (getTag() instanceof ShortcutInfo) {
            ShortcutInfo shortcutInfo = (ShortcutInfo) getTag();
            if (shortcutInfo.hasPromiseIconUi()) {
                i = shortcutInfo.hasStatusFlag(4) ? shortcutInfo.getInstallProgress() : 0;
            } else {
                i = 100;
            }
            PreloadIconDrawable applyProgressLevel = applyProgressLevel(i);
            if (applyProgressLevel != null && z) {
                applyProgressLevel.maybePerformFinishedAnimation();
            }
        }
    }

    public PreloadIconDrawable applyProgressLevel(int i) {
        if (getTag() instanceof ItemInfoWithIcon) {
            ItemInfoWithIcon itemInfoWithIcon = (ItemInfoWithIcon) getTag();
            if (i >= 100) {
                setContentDescription(itemInfoWithIcon.contentDescription != null ? itemInfoWithIcon.contentDescription : "");
            } else if (i > 0) {
                setContentDescription(getContext().getString(R.string.app_downloading_title, itemInfoWithIcon.title, NumberFormat.getPercentInstance().format(i * 0.01d)));
            } else {
                setContentDescription(getContext().getString(R.string.app_waiting_download_title, itemInfoWithIcon.title));
            }
            if (this.mIcon != null) {
                if (this.mIcon instanceof PreloadIconDrawable) {
                    PreloadIconDrawable preloadIconDrawable = (PreloadIconDrawable) this.mIcon;
                    preloadIconDrawable.setLevel(i);
                    return preloadIconDrawable;
                }
                PreloadIconDrawable newPendingIcon = DrawableFactory.get(getContext()).newPendingIcon(itemInfoWithIcon, getContext());
                newPendingIcon.setLevel(i);
                setIcon(newPendingIcon);
                return newPendingIcon;
            }
            return null;
        }
        return null;
    }

    public void applyBadgeState(ItemInfo itemInfo, boolean z) {
        if (this.mIcon instanceof FastBitmapDrawable) {
            boolean z2 = this.mBadgeInfo != null;
            this.mBadgeInfo = this.mActivity.getBadgeInfoForItem(itemInfo);
            boolean z3 = this.mBadgeInfo != null;
            float f = z3 ? 1.0f : 0.0f;
            this.mBadgeRenderer = this.mActivity.getDeviceProfile().mBadgeRenderer;
            if (z2 || z3) {
                if (z && (z2 ^ z3) && isShown()) {
                    ObjectAnimator.ofFloat(this, BADGE_SCALE_PROPERTY, f).start();
                } else {
                    this.mBadgeScale = f;
                    invalidate();
                }
            }
            if (itemInfo.contentDescription != null) {
                if (hasBadge()) {
                    int notificationCount = this.mBadgeInfo.getNotificationCount();
                    setContentDescription(getContext().getResources().getQuantityString(R.plurals.badged_app_label, notificationCount, itemInfo.contentDescription, Integer.valueOf(notificationCount)));
                    return;
                }
                setContentDescription(itemInfo.contentDescription);
            }
        }
    }

    private void setIcon(Drawable drawable) {
        if (this.mIsIconVisible) {
            applyCompoundDrawables(drawable);
        }
        this.mIcon = drawable;
    }

    public void setIconVisible(boolean z) {
        this.mIsIconVisible = z;
        applyCompoundDrawables(z ? this.mIcon : new ColorDrawable(0));
    }

    protected void applyCompoundDrawables(Drawable drawable) {
        this.mDisableRelayout = this.mIcon != null;
        drawable.setBounds(0, 0, this.mIconSize, this.mIconSize);
        if (this.mLayoutHorizontal) {
            setCompoundDrawablesRelative(drawable, null, null, null);
        } else {
            setCompoundDrawables(null, drawable, null, null);
        }
        this.mDisableRelayout = false;
    }

    @Override // android.view.View
    public void requestLayout() {
        if (!this.mDisableRelayout) {
            super.requestLayout();
        }
    }

    @Override // com.android.launcher3.IconCache.ItemInfoUpdateReceiver
    public void reapplyItemInfo(ItemInfoWithIcon itemInfoWithIcon) {
        if (getTag() == itemInfoWithIcon) {
            this.mIconLoadRequest = null;
            this.mDisableRelayout = true;
            itemInfoWithIcon.iconBitmap.prepareToDraw();
            if (itemInfoWithIcon instanceof AppInfo) {
                applyFromApplicationInfo((AppInfo) itemInfoWithIcon);
            } else if (itemInfoWithIcon instanceof ShortcutInfo) {
                applyFromShortcutInfo((ShortcutInfo) itemInfoWithIcon);
                this.mActivity.invalidateParent(itemInfoWithIcon);
            } else if (itemInfoWithIcon instanceof PackageItemInfo) {
                applyFromPackageItemInfo((PackageItemInfo) itemInfoWithIcon);
            }
            this.mDisableRelayout = false;
        }
    }

    public void verifyHighRes() {
        if (this.mIconLoadRequest != null) {
            this.mIconLoadRequest.cancel();
            this.mIconLoadRequest = null;
        }
        if (getTag() instanceof ItemInfoWithIcon) {
            ItemInfoWithIcon itemInfoWithIcon = (ItemInfoWithIcon) getTag();
            if (itemInfoWithIcon.usingLowResIcon) {
                this.mIconLoadRequest = LauncherAppState.getInstance(getContext()).getIconCache().updateIconInBackground(this, itemInfoWithIcon);
            }
        }
    }

    public int getIconSize() {
        return this.mIconSize;
    }
}

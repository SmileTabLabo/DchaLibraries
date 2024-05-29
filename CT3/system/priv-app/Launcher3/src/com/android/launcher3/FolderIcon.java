package com.android.launcher3;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.launcher3.CellLayout;
import com.android.launcher3.DropTarget;
import com.android.launcher3.FolderInfo;
import com.android.launcher3.compat.PackageInstallerCompat;
import com.mediatek.launcher3.LauncherLog;
import java.util.ArrayList;
/* loaded from: a.zip:com/android/launcher3/FolderIcon.class */
public class FolderIcon extends FrameLayout implements FolderInfo.FolderListener {
    PreviewItemDrawingParams mAnimParams;
    boolean mAnimating;
    private int mAvailableSpaceInPreview;
    private float mBaselineIconScale;
    private int mBaselineIconSize;
    ItemInfo mDragInfo;
    Folder mFolder;
    BubbleTextView mFolderName;
    FolderRingAnimator mFolderRingAnimator;
    ArrayList<ShortcutInfo> mHiddenItems;
    private FolderInfo mInfo;
    private int mIntrinsicIconSize;
    Launcher mLauncher;
    private CheckLongPressHelper mLongPressHelper;
    private float mMaxPerspectiveShift;
    private Rect mOldBounds;
    OnAlarmListener mOnOpenListener;
    private Alarm mOpenAlarm;
    private PreviewItemDrawingParams mParams;
    ImageView mPreviewBackground;
    private int mPreviewOffsetX;
    private int mPreviewOffsetY;
    private float mSlop;
    private StylusEventHelper mStylusEventHelper;
    private int mTotalWidth;
    static boolean sStaticValuesDirty = true;
    public static Drawable sSharedFolderLeaveBehind = null;

    /* loaded from: a.zip:com/android/launcher3/FolderIcon$FolderRingAnimator.class */
    public static class FolderRingAnimator {
        private ValueAnimator mAcceptAnimator;
        CellLayout mCellLayout;
        public int mCellX;
        public int mCellY;
        public FolderIcon mFolderIcon;
        public float mInnerRingSize;
        private ValueAnimator mNeutralAnimator;
        public float mOuterRingSize;
        public static Drawable sSharedOuterRingDrawable = null;
        public static Drawable sSharedInnerRingDrawable = null;
        public static int sPreviewSize = -1;
        public static int sPreviewPadding = -1;

        public FolderRingAnimator(Launcher launcher, FolderIcon folderIcon) {
            this.mFolderIcon = null;
            this.mFolderIcon = folderIcon;
            Resources resources = launcher.getResources();
            if (FolderIcon.sStaticValuesDirty) {
                if (Looper.myLooper() != Looper.getMainLooper()) {
                    throw new RuntimeException("FolderRingAnimator loading drawables on non-UI thread " + Thread.currentThread());
                }
                sPreviewSize = launcher.getDeviceProfile().folderIconSizePx;
                sPreviewPadding = resources.getDimensionPixelSize(2131230802);
                sSharedOuterRingDrawable = resources.getDrawable(2130837547);
                sSharedInnerRingDrawable = resources.getDrawable(2130837546);
                FolderIcon.sSharedFolderLeaveBehind = resources.getDrawable(2130837548);
                FolderIcon.sStaticValuesDirty = false;
            }
        }

        public void animateToAcceptState() {
            if (this.mNeutralAnimator != null) {
                this.mNeutralAnimator.cancel();
            }
            this.mAcceptAnimator = LauncherAnimUtils.ofFloat(this.mCellLayout, 0.0f, 1.0f);
            this.mAcceptAnimator.setDuration(100L);
            this.mAcceptAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this, sPreviewSize) { // from class: com.android.launcher3.FolderIcon.FolderRingAnimator.1
                final FolderRingAnimator this$1;
                final int val$previewSize;

                {
                    this.this$1 = this;
                    this.val$previewSize = r5;
                }

                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                    this.this$1.mOuterRingSize = ((0.3f * floatValue) + 1.0f) * this.val$previewSize;
                    this.this$1.mInnerRingSize = ((0.15f * floatValue) + 1.0f) * this.val$previewSize;
                    if (this.this$1.mCellLayout != null) {
                        this.this$1.mCellLayout.invalidate();
                    }
                }
            });
            this.mAcceptAnimator.addListener(new AnimatorListenerAdapter(this) { // from class: com.android.launcher3.FolderIcon.FolderRingAnimator.2
                final FolderRingAnimator this$1;

                {
                    this.this$1 = this;
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationStart(Animator animator) {
                    if (this.this$1.mFolderIcon != null) {
                        this.this$1.mFolderIcon.mPreviewBackground.setVisibility(4);
                    }
                }
            });
            this.mAcceptAnimator.start();
        }

        public void animateToNaturalState() {
            if (this.mAcceptAnimator != null) {
                this.mAcceptAnimator.cancel();
            }
            this.mNeutralAnimator = LauncherAnimUtils.ofFloat(this.mCellLayout, 0.0f, 1.0f);
            this.mNeutralAnimator.setDuration(100L);
            this.mNeutralAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this, sPreviewSize) { // from class: com.android.launcher3.FolderIcon.FolderRingAnimator.3
                final FolderRingAnimator this$1;
                final int val$previewSize;

                {
                    this.this$1 = this;
                    this.val$previewSize = r5;
                }

                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                    this.this$1.mOuterRingSize = (((1.0f - floatValue) * 0.3f) + 1.0f) * this.val$previewSize;
                    this.this$1.mInnerRingSize = (((1.0f - floatValue) * 0.15f) + 1.0f) * this.val$previewSize;
                    if (this.this$1.mCellLayout != null) {
                        this.this$1.mCellLayout.invalidate();
                    }
                }
            });
            this.mNeutralAnimator.addListener(new AnimatorListenerAdapter(this) { // from class: com.android.launcher3.FolderIcon.FolderRingAnimator.4
                final FolderRingAnimator this$1;

                {
                    this.this$1 = this;
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    if (this.this$1.mCellLayout != null) {
                        this.this$1.mCellLayout.hideFolderAccept(this.this$1);
                    }
                    if (this.this$1.mFolderIcon != null) {
                        this.this$1.mFolderIcon.mPreviewBackground.setVisibility(0);
                    }
                }
            });
            this.mNeutralAnimator.start();
        }

        public float getInnerRingSize() {
            return this.mInnerRingSize;
        }

        public float getOuterRingSize() {
            return this.mOuterRingSize;
        }

        public void setCell(int i, int i2) {
            this.mCellX = i;
            this.mCellY = i2;
        }

        public void setCellLayout(CellLayout cellLayout) {
            this.mCellLayout = cellLayout;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:com/android/launcher3/FolderIcon$PreviewItemDrawingParams.class */
    public class PreviewItemDrawingParams {
        Drawable drawable;
        float overlayAlpha;
        float scale;
        final FolderIcon this$0;
        float transX;
        float transY;

        PreviewItemDrawingParams(FolderIcon folderIcon, float f, float f2, float f3, float f4) {
            this.this$0 = folderIcon;
            this.transX = f;
            this.transY = f2;
            this.scale = f3;
            this.overlayAlpha = f4;
        }
    }

    public FolderIcon(Context context) {
        super(context);
        this.mFolderRingAnimator = null;
        this.mTotalWidth = -1;
        this.mAnimating = false;
        this.mOldBounds = new Rect();
        this.mParams = new PreviewItemDrawingParams(this, 0.0f, 0.0f, 0.0f, 0.0f);
        this.mAnimParams = new PreviewItemDrawingParams(this, 0.0f, 0.0f, 0.0f, 0.0f);
        this.mHiddenItems = new ArrayList<>();
        this.mOpenAlarm = new Alarm();
        this.mOnOpenListener = new OnAlarmListener(this) { // from class: com.android.launcher3.FolderIcon.1
            final FolderIcon this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.launcher3.OnAlarmListener
            public void onAlarm(Alarm alarm) {
                ShortcutInfo shortcutInfo;
                if (this.this$0.mDragInfo instanceof AppInfo) {
                    shortcutInfo = ((AppInfo) this.this$0.mDragInfo).makeShortcut();
                    shortcutInfo.spanX = 1;
                    shortcutInfo.spanY = 1;
                } else if (this.this$0.mDragInfo instanceof PendingAddItemInfo) {
                    if (LauncherLog.DEBUG) {
                        LauncherLog.d("FolderIcon", "onAlarm: mDragInfo instanceof PendingAddItemInfo");
                        return;
                    }
                    return;
                } else {
                    shortcutInfo = (ShortcutInfo) this.this$0.mDragInfo;
                }
                this.this$0.mFolder.beginExternalDrag(shortcutInfo);
                this.this$0.mLauncher.openFolder(this.this$0);
            }
        };
        init();
    }

    public FolderIcon(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mFolderRingAnimator = null;
        this.mTotalWidth = -1;
        this.mAnimating = false;
        this.mOldBounds = new Rect();
        this.mParams = new PreviewItemDrawingParams(this, 0.0f, 0.0f, 0.0f, 0.0f);
        this.mAnimParams = new PreviewItemDrawingParams(this, 0.0f, 0.0f, 0.0f, 0.0f);
        this.mHiddenItems = new ArrayList<>();
        this.mOpenAlarm = new Alarm();
        this.mOnOpenListener = new OnAlarmListener(this) { // from class: com.android.launcher3.FolderIcon.1
            final FolderIcon this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.launcher3.OnAlarmListener
            public void onAlarm(Alarm alarm) {
                ShortcutInfo shortcutInfo;
                if (this.this$0.mDragInfo instanceof AppInfo) {
                    shortcutInfo = ((AppInfo) this.this$0.mDragInfo).makeShortcut();
                    shortcutInfo.spanX = 1;
                    shortcutInfo.spanY = 1;
                } else if (this.this$0.mDragInfo instanceof PendingAddItemInfo) {
                    if (LauncherLog.DEBUG) {
                        LauncherLog.d("FolderIcon", "onAlarm: mDragInfo instanceof PendingAddItemInfo");
                        return;
                    }
                    return;
                } else {
                    shortcutInfo = (ShortcutInfo) this.this$0.mDragInfo;
                }
                this.this$0.mFolder.beginExternalDrag(shortcutInfo);
                this.this$0.mLauncher.openFolder(this.this$0);
            }
        };
        init();
    }

    private void animateFirstItem(Drawable drawable, int i, boolean z, Runnable runnable) {
        PreviewItemDrawingParams computePreviewItemDrawingParams = computePreviewItemDrawingParams(0, null);
        float f = this.mLauncher.getDeviceProfile().iconSizePx;
        float intrinsicWidth = f / drawable.getIntrinsicWidth();
        float f2 = (this.mAvailableSpaceInPreview - f) / 2.0f;
        float f3 = (this.mAvailableSpaceInPreview - f) / 2.0f;
        float paddingTop = getPaddingTop();
        this.mAnimParams.drawable = drawable;
        ValueAnimator ofFloat = LauncherAnimUtils.ofFloat(this, 0.0f, 1.0f);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this, z, f2, computePreviewItemDrawingParams, f3 + paddingTop, intrinsicWidth) { // from class: com.android.launcher3.FolderIcon.3
            final FolderIcon this$0;
            final PreviewItemDrawingParams val$finalParams;
            final boolean val$reverse;
            final float val$scale0;
            final float val$transX0;
            final float val$transY0;

            {
                this.this$0 = this;
                this.val$reverse = z;
                this.val$transX0 = f2;
                this.val$finalParams = computePreviewItemDrawingParams;
                this.val$transY0 = r8;
                this.val$scale0 = intrinsicWidth;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                float f4 = floatValue;
                if (this.val$reverse) {
                    f4 = 1.0f - floatValue;
                    this.this$0.mPreviewBackground.setAlpha(f4);
                }
                this.this$0.mAnimParams.transX = this.val$transX0 + ((this.val$finalParams.transX - this.val$transX0) * f4);
                this.this$0.mAnimParams.transY = this.val$transY0 + ((this.val$finalParams.transY - this.val$transY0) * f4);
                this.this$0.mAnimParams.scale = this.val$scale0 + ((this.val$finalParams.scale - this.val$scale0) * f4);
                this.this$0.invalidate();
            }
        });
        ofFloat.addListener(new AnimatorListenerAdapter(this, runnable) { // from class: com.android.launcher3.FolderIcon.4
            final FolderIcon this$0;
            final Runnable val$onCompleteRunnable;

            {
                this.this$0 = this;
                this.val$onCompleteRunnable = runnable;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                this.this$0.mAnimating = false;
                if (this.val$onCompleteRunnable != null) {
                    this.val$onCompleteRunnable.run();
                }
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                this.this$0.mAnimating = true;
            }
        });
        ofFloat.setDuration(i);
        ofFloat.start();
    }

    private void computePreviewDrawingParams(int i, int i2) {
        if (this.mIntrinsicIconSize == i && this.mTotalWidth == i2) {
            return;
        }
        DeviceProfile deviceProfile = this.mLauncher.getDeviceProfile();
        this.mIntrinsicIconSize = i;
        this.mTotalWidth = i2;
        int i3 = this.mPreviewBackground.getLayoutParams().height;
        int i4 = FolderRingAnimator.sPreviewPadding;
        this.mAvailableSpaceInPreview = i3 - (i4 * 2);
        this.mBaselineIconScale = (((int) ((this.mAvailableSpaceInPreview / 2) * 1.8f)) * 1.0f) / ((int) (this.mIntrinsicIconSize * 1.1800001f));
        this.mBaselineIconSize = (int) (this.mIntrinsicIconSize * this.mBaselineIconScale);
        this.mMaxPerspectiveShift = this.mBaselineIconSize * 0.18f;
        this.mPreviewOffsetX = (this.mTotalWidth - this.mAvailableSpaceInPreview) / 2;
        this.mPreviewOffsetY = deviceProfile.folderBackgroundOffset + i4;
    }

    private void computePreviewDrawingParams(Drawable drawable) {
        computePreviewDrawingParams(drawable.getIntrinsicWidth(), getMeasuredWidth());
    }

    private PreviewItemDrawingParams computePreviewItemDrawingParams(int i, PreviewItemDrawingParams previewItemDrawingParams) {
        float f = (((3 - i) - 1) * 1.0f) / 2.0f;
        float f2 = 1.0f - ((1.0f - f) * 0.35f);
        float f3 = this.mMaxPerspectiveShift;
        float f4 = f2 * this.mBaselineIconSize;
        float paddingTop = (this.mAvailableSpaceInPreview - ((((1.0f - f) * f3) + f4) + ((1.0f - f2) * this.mBaselineIconSize))) + getPaddingTop();
        float f5 = (this.mAvailableSpaceInPreview - f4) / 2.0f;
        float f6 = this.mBaselineIconScale * f2;
        float f7 = ((1.0f - f) * 80.0f) / 255.0f;
        if (previewItemDrawingParams == null) {
            previewItemDrawingParams = new PreviewItemDrawingParams(this, f5, paddingTop, f6, f7);
        } else {
            previewItemDrawingParams.transX = f5;
            previewItemDrawingParams.transY = paddingTop;
            previewItemDrawingParams.scale = f6;
            previewItemDrawingParams.overlayAlpha = f7;
        }
        return previewItemDrawingParams;
    }

    private void drawPreviewItem(Canvas canvas, PreviewItemDrawingParams previewItemDrawingParams) {
        canvas.save();
        canvas.translate(previewItemDrawingParams.transX + this.mPreviewOffsetX, previewItemDrawingParams.transY + this.mPreviewOffsetY);
        canvas.scale(previewItemDrawingParams.scale, previewItemDrawingParams.scale);
        Drawable drawable = previewItemDrawingParams.drawable;
        if (drawable != null) {
            this.mOldBounds.set(drawable.getBounds());
            drawable.setBounds(0, 0, this.mIntrinsicIconSize, this.mIntrinsicIconSize);
            if (drawable instanceof FastBitmapDrawable) {
                FastBitmapDrawable fastBitmapDrawable = (FastBitmapDrawable) drawable;
                float brightness = fastBitmapDrawable.getBrightness();
                fastBitmapDrawable.setBrightness(previewItemDrawingParams.overlayAlpha);
                drawable.draw(canvas);
                fastBitmapDrawable.setBrightness(brightness);
            } else {
                drawable.setColorFilter(Color.argb((int) (previewItemDrawingParams.overlayAlpha * 255.0f), 255, 255, 255), PorterDuff.Mode.SRC_ATOP);
                drawable.draw(canvas);
                drawable.clearColorFilter();
            }
            drawable.setBounds(this.mOldBounds);
        }
        canvas.restore();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static FolderIcon fromXml(int i, Launcher launcher, ViewGroup viewGroup, FolderInfo folderInfo, IconCache iconCache) {
        return fromXml(i, launcher, viewGroup, folderInfo, iconCache, false);
    }

    static FolderIcon fromXml(int i, Launcher launcher, ViewGroup viewGroup, FolderInfo folderInfo, IconCache iconCache, boolean z) {
        DeviceProfile deviceProfile = launcher.getDeviceProfile();
        FolderIcon folderIcon = (FolderIcon) LayoutInflater.from(launcher).inflate(i, viewGroup, false);
        folderIcon.setClipToPadding(false);
        folderIcon.mFolderName = (BubbleTextView) folderIcon.findViewById(2131296285);
        folderIcon.mFolderName.setText(folderInfo.title);
        folderIcon.mFolderName.setCompoundDrawablePadding(0);
        ((FrameLayout.LayoutParams) folderIcon.mFolderName.getLayoutParams()).topMargin = deviceProfile.iconSizePx + deviceProfile.iconDrawablePaddingPx;
        folderIcon.mPreviewBackground = (ImageView) folderIcon.findViewById(2131296284);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) folderIcon.mPreviewBackground.getLayoutParams();
        layoutParams.topMargin = deviceProfile.folderBackgroundOffset;
        layoutParams.width = deviceProfile.folderIconSizePx;
        layoutParams.height = deviceProfile.folderIconSizePx;
        folderIcon.setTag(folderInfo);
        folderIcon.setOnClickListener(launcher);
        folderIcon.mInfo = folderInfo;
        folderIcon.mLauncher = launcher;
        folderIcon.setContentDescription(String.format(launcher.getString(2131558452), folderInfo.title));
        Folder fromXml = Folder.fromXml(launcher);
        fromXml.setDragController(launcher.getDragController());
        fromXml.setFolderIcon(folderIcon);
        fromXml.bind(folderInfo);
        folderIcon.mFolder = fromXml;
        folderIcon.mFolderRingAnimator = new FolderRingAnimator(launcher, folderIcon);
        folderInfo.addListener(folderIcon);
        folderIcon.setOnFocusChangeListener(launcher.mFocusHandler);
        return folderIcon;
    }

    private float getLocalCenterForIndex(int i, int[] iArr) {
        this.mParams = computePreviewItemDrawingParams(Math.min(3, i), this.mParams);
        this.mParams.transX += this.mPreviewOffsetX;
        this.mParams.transY += this.mPreviewOffsetY;
        iArr[0] = Math.round(this.mParams.transX + ((this.mParams.scale * this.mIntrinsicIconSize) / 2.0f));
        iArr[1] = Math.round(this.mParams.transY + ((this.mParams.scale * this.mIntrinsicIconSize) / 2.0f));
        return this.mParams.scale;
    }

    private Drawable getTopDrawable(TextView textView) {
        Drawable drawable = textView.getCompoundDrawables()[1];
        Drawable drawable2 = drawable;
        if (drawable instanceof PreloadIconDrawable) {
            drawable2 = ((PreloadIconDrawable) drawable).mIcon;
        }
        return drawable2;
    }

    private void init() {
        this.mLongPressHelper = new CheckLongPressHelper(this);
        this.mStylusEventHelper = new StylusEventHelper(this);
        setAccessibilityDelegate(LauncherAppState.getInstance().getAccessibilityDelegate());
    }

    private void onDrop(ShortcutInfo shortcutInfo, DragView dragView, Rect rect, float f, int i, Runnable runnable, DropTarget.DragObject dragObject) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d("FolderIcon", "onDrop: item = " + shortcutInfo + ", animateView = " + dragView + ", finalRect = " + rect + ", scaleRelativeToDragLayer = " + f + ", index = " + i + ", d = " + dragObject);
        }
        shortcutInfo.cellX = -1;
        shortcutInfo.cellY = -1;
        if (dragView == null) {
            addItem(shortcutInfo);
            return;
        }
        DragLayer dragLayer = this.mLauncher.getDragLayer();
        Rect rect2 = new Rect();
        dragLayer.getViewRectRelativeToSelf(dragView, rect2);
        Rect rect3 = rect;
        if (rect == null) {
            rect3 = new Rect();
            Workspace workspace = this.mLauncher.getWorkspace();
            workspace.setFinalTransitionTransform((CellLayout) getParent().getParent());
            float scaleX = getScaleX();
            float scaleY = getScaleY();
            setScaleX(1.0f);
            setScaleY(1.0f);
            f = dragLayer.getDescendantRectRelativeToSelf(this, rect3);
            setScaleX(scaleX);
            setScaleY(scaleY);
            workspace.resetTransitionTransform((CellLayout) getParent().getParent());
        }
        float localCenterForIndex = getLocalCenterForIndex(i, r0);
        int[] iArr = {Math.round(iArr[0] * f), Math.round(iArr[1] * f)};
        rect3.offset(iArr[0] - (dragView.getMeasuredWidth() / 2), iArr[1] - (dragView.getMeasuredHeight() / 2));
        float f2 = localCenterForIndex * f;
        dragLayer.animateView(dragView, rect2, rect3, i < 3 ? 0.5f : 0.0f, 1.0f, 1.0f, f2, f2, 400, new DecelerateInterpolator(2.0f), new AccelerateInterpolator(2.0f), runnable, 0, null);
        addItem(shortcutInfo);
        this.mHiddenItems.add(shortcutInfo);
        this.mFolder.hideItem(shortcutInfo);
        postDelayed(new Runnable(this, shortcutInfo) { // from class: com.android.launcher3.FolderIcon.2
            final FolderIcon this$0;
            final ShortcutInfo val$item;

            {
                this.this$0 = this;
                this.val$item = shortcutInfo;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.mHiddenItems.remove(this.val$item);
                this.this$0.mFolder.showItem(this.val$item);
                this.this$0.invalidate();
            }
        }, 400L);
    }

    /* JADX WARN: Code restructure failed: missing block: B:5:0x0010, code lost:
        if (r0 == 1) goto L8;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private boolean willAcceptItem(ItemInfo itemInfo) {
        boolean z;
        int i = itemInfo.itemType;
        if (i != 0) {
            z = false;
        }
        if (this.mFolder.isFull()) {
            z = false;
        } else {
            z = false;
            if (itemInfo != this.mInfo) {
                z = false;
                if (!this.mInfo.opened) {
                    z = true;
                }
            }
        }
        return z;
    }

    public boolean acceptDrop(Object obj) {
        return !this.mFolder.isDestroyed() ? willAcceptItem((ItemInfo) obj) : false;
    }

    public void addItem(ShortcutInfo shortcutInfo) {
        this.mInfo.add(shortcutInfo);
    }

    @Override // android.view.View
    public void cancelLongPress() {
        super.cancelLongPress();
        this.mLongPressHelper.cancelLongPress();
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (this.mFolder == null) {
            return;
        }
        if (this.mFolder.getItemCount() == 0 && !this.mAnimating) {
            return;
        }
        ArrayList<View> itemsInReadingOrder = this.mFolder.getItemsInReadingOrder();
        if (this.mAnimating) {
            computePreviewDrawingParams(this.mAnimParams.drawable);
        } else {
            computePreviewDrawingParams(getTopDrawable((TextView) itemsInReadingOrder.get(0)));
        }
        int min = Math.min(itemsInReadingOrder.size(), 3);
        if (this.mAnimating) {
            drawPreviewItem(canvas, this.mAnimParams);
            return;
        }
        while (true) {
            min--;
            if (min < 0) {
                return;
            }
            TextView textView = (TextView) itemsInReadingOrder.get(min);
            if (!this.mHiddenItems.contains(textView.getTag())) {
                Drawable topDrawable = getTopDrawable(textView);
                this.mParams = computePreviewItemDrawingParams(min, this.mParams);
                this.mParams.drawable = topDrawable;
                drawPreviewItem(canvas, this.mParams);
            }
        }
    }

    public Folder getFolder() {
        return this.mFolder;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public FolderInfo getFolderInfo() {
        return this.mInfo;
    }

    public boolean getTextVisible() {
        boolean z = false;
        if (this.mFolderName.getVisibility() == 0) {
            z = true;
        }
        return z;
    }

    @Override // com.android.launcher3.FolderInfo.FolderListener
    public void onAdd(ShortcutInfo shortcutInfo) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d("FolderIcon", "onAdd item = " + shortcutInfo);
        }
        invalidate();
        requestLayout();
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    public void onDragEnter(Object obj) {
        if (this.mFolder.isDestroyed() || !willAcceptItem((ItemInfo) obj)) {
            return;
        }
        CellLayout.LayoutParams layoutParams = (CellLayout.LayoutParams) getLayoutParams();
        CellLayout cellLayout = (CellLayout) getParent().getParent();
        this.mFolderRingAnimator.setCell(layoutParams.cellX, layoutParams.cellY);
        this.mFolderRingAnimator.setCellLayout(cellLayout);
        this.mFolderRingAnimator.animateToAcceptState();
        cellLayout.showFolderAccept(this.mFolderRingAnimator);
        this.mOpenAlarm.setOnAlarmListener(this.mOnOpenListener);
        if ((obj instanceof AppInfo) || (obj instanceof ShortcutInfo)) {
            this.mOpenAlarm.setAlarm(800L);
        }
        this.mDragInfo = (ItemInfo) obj;
    }

    public void onDragExit() {
        this.mFolderRingAnimator.animateToNaturalState();
        this.mOpenAlarm.cancelAlarm();
    }

    public void onDragExit(Object obj) {
        onDragExit();
    }

    public void onDrop(DropTarget.DragObject dragObject) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d("FolderIcon", "onDrop: DragObject = " + dragObject);
        }
        ShortcutInfo makeShortcut = dragObject.dragInfo instanceof AppInfo ? ((AppInfo) dragObject.dragInfo).makeShortcut() : (ShortcutInfo) dragObject.dragInfo;
        this.mFolder.notifyDrop();
        onDrop(makeShortcut, dragObject.dragView, null, 1.0f, this.mInfo.contents.size(), dragObject.postAnimationRunnable, dragObject);
    }

    @Override // com.android.launcher3.FolderInfo.FolderListener
    public void onItemsChanged() {
        invalidate();
        requestLayout();
    }

    @Override // com.android.launcher3.FolderInfo.FolderListener
    public void onRemove(ShortcutInfo shortcutInfo) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d("FolderIcon", "onRemove item = " + shortcutInfo);
        }
        invalidate();
        requestLayout();
    }

    @Override // android.view.View
    protected Parcelable onSaveInstanceState() {
        sStaticValuesDirty = true;
        return super.onSaveInstanceState();
    }

    @Override // com.android.launcher3.FolderInfo.FolderListener
    public void onTitleChanged(CharSequence charSequence) {
        this.mFolderName.setText(charSequence);
        setContentDescription(String.format(getContext().getString(2131558452), charSequence));
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        boolean onTouchEvent = super.onTouchEvent(motionEvent);
        if (this.mStylusEventHelper.checkAndPerformStylusEvent(motionEvent)) {
            this.mLongPressHelper.cancelLongPress();
            return true;
        }
        switch (motionEvent.getAction()) {
            case PackageInstallerCompat.STATUS_INSTALLED /* 0 */:
                this.mLongPressHelper.postCheckForLongPress();
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

    public void performCreateAnimation(ShortcutInfo shortcutInfo, View view, ShortcutInfo shortcutInfo2, DragView dragView, Rect rect, float f, Runnable runnable) {
        Drawable topDrawable = getTopDrawable((TextView) view);
        computePreviewDrawingParams(topDrawable.getIntrinsicWidth(), view.getMeasuredWidth());
        animateFirstItem(topDrawable, 350, false, null);
        addItem(shortcutInfo);
        onDrop(shortcutInfo2, dragView, rect, f, 1, runnable, null);
    }

    public void performDestroyAnimation(View view, Runnable runnable) {
        Drawable topDrawable = getTopDrawable((TextView) view);
        computePreviewDrawingParams(topDrawable.getIntrinsicWidth(), view.getMeasuredWidth());
        animateFirstItem(topDrawable, 200, true, runnable);
    }

    public void setTextVisible(boolean z) {
        if (z) {
            this.mFolderName.setVisibility(0);
        } else {
            this.mFolderName.setVisibility(4);
        }
    }
}

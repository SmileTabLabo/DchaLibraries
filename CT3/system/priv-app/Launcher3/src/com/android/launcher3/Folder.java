package com.android.launcher3;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Selection;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.launcher3.CellLayout;
import com.android.launcher3.DragController;
import com.android.launcher3.DragLayer;
import com.android.launcher3.DropTarget;
import com.android.launcher3.ExtendedEditText;
import com.android.launcher3.FolderInfo;
import com.android.launcher3.Stats;
import com.android.launcher3.UninstallDropTarget;
import com.android.launcher3.Workspace;
import com.android.launcher3.accessibility.LauncherAccessibilityDelegate;
import com.android.launcher3.config.FeatureFlags;
import com.android.launcher3.util.UiThreadCircularReveal;
import com.mediatek.launcher3.LauncherLog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
/* loaded from: a.zip:com/android/launcher3/Folder.class */
public class Folder extends LinearLayout implements DragSource, View.OnClickListener, View.OnLongClickListener, DropTarget, FolderInfo.FolderListener, TextView.OnEditorActionListener, View.OnFocusChangeListener, DragController.DragListener, UninstallDropTarget.UninstallSource, LauncherAccessibilityDelegate.AccessibilityDragSource, Stats.LaunchSourceProvider {
    private static String sDefaultFolderName;
    private static String sHintText;
    FolderPagedView mContent;
    View mContentWrapper;
    private ShortcutInfo mCurrentDragInfo;
    private View mCurrentDragView;
    int mCurrentScrollDir;
    private boolean mDeferDropAfterUninstall;
    Runnable mDeferredAction;
    private boolean mDeleteFolderOnDropCompleted;
    private boolean mDestroyed;
    protected DragController mDragController;
    private boolean mDragInProgress;
    int mEmptyCellRank;
    private final int mExpandDuration;
    FolderIcon mFolderIcon;
    float mFolderIconPivotX;
    float mFolderIconPivotY;
    ExtendedEditText mFolderName;
    private View mFooter;
    private int mFooterHeight;
    protected FolderInfo mInfo;
    private final InputMethodManager mInputMethodManager;
    private boolean mIsEditingName;
    private boolean mIsExternalDrag;
    private boolean mItemAddedBackToSelfViaIcon;
    final ArrayList<View> mItemsInReadingOrder;
    boolean mItemsInvalidated;
    protected final Launcher mLauncher;
    private final int mMaterialExpandDuration;
    private final int mMaterialExpandStagger;
    private final Alarm mOnExitAlarm;
    OnAlarmListener mOnExitAlarmListener;
    private final Alarm mOnScrollHintAlarm;
    int mPrevTargetRank;
    private boolean mRearrangeOnClose;
    private final Alarm mReorderAlarm;
    OnAlarmListener mReorderAlarmListener;
    private int mScrollAreaOffset;
    int mScrollHintDir;
    final Alarm mScrollPauseAlarm;
    int mState;
    private boolean mSuppressFolderDeletion;
    boolean mSuppressOnAdd;
    int mTargetRank;
    private boolean mUninstallSuccessful;
    private static final Rect sTempRect = new Rect();
    public static final Comparator<ItemInfo> ITEM_POS_COMPARATOR = new Comparator<ItemInfo>() { // from class: com.android.launcher3.Folder.3
        @Override // java.util.Comparator
        public int compare(ItemInfo itemInfo, ItemInfo itemInfo2) {
            return itemInfo.rank != itemInfo2.rank ? itemInfo.rank - itemInfo2.rank : itemInfo.cellY != itemInfo2.cellY ? itemInfo.cellY - itemInfo2.cellY : itemInfo.cellX - itemInfo2.cellX;
        }
    };

    /* loaded from: a.zip:com/android/launcher3/Folder$OnScrollFinishedListener.class */
    private class OnScrollFinishedListener implements OnAlarmListener {
        private final DropTarget.DragObject mDragObject;
        final Folder this$0;

        OnScrollFinishedListener(Folder folder, DropTarget.DragObject dragObject) {
            this.this$0 = folder;
            this.mDragObject = dragObject;
        }

        @Override // com.android.launcher3.OnAlarmListener
        public void onAlarm(Alarm alarm) {
            this.this$0.onDragOver(this.mDragObject, 1);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/launcher3/Folder$OnScrollHintListener.class */
    public class OnScrollHintListener implements OnAlarmListener {
        private final DropTarget.DragObject mDragObject;
        final Folder this$0;

        OnScrollHintListener(Folder folder, DropTarget.DragObject dragObject) {
            this.this$0 = folder;
            this.mDragObject = dragObject;
        }

        @Override // com.android.launcher3.OnAlarmListener
        public void onAlarm(Alarm alarm) {
            if (this.this$0.mCurrentScrollDir == 0) {
                this.this$0.mContent.scrollLeft();
                this.this$0.mScrollHintDir = -1;
            } else if (this.this$0.mCurrentScrollDir != 1) {
                return;
            } else {
                this.this$0.mContent.scrollRight();
                this.this$0.mScrollHintDir = -1;
            }
            this.this$0.mCurrentScrollDir = -1;
            this.this$0.mScrollPauseAlarm.setOnAlarmListener(new OnScrollFinishedListener(this.this$0, this.mDragObject));
            this.this$0.mScrollPauseAlarm.setAlarm(900L);
        }
    }

    public Folder(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mReorderAlarm = new Alarm();
        this.mOnExitAlarm = new Alarm();
        this.mOnScrollHintAlarm = new Alarm();
        this.mScrollPauseAlarm = new Alarm();
        this.mItemsInReadingOrder = new ArrayList<>();
        this.mState = -1;
        this.mRearrangeOnClose = false;
        this.mItemsInvalidated = false;
        this.mSuppressOnAdd = false;
        this.mDragInProgress = false;
        this.mDeleteFolderOnDropCompleted = false;
        this.mSuppressFolderDeletion = false;
        this.mItemAddedBackToSelfViaIcon = false;
        this.mIsEditingName = false;
        this.mScrollHintDir = -1;
        this.mCurrentScrollDir = -1;
        this.mReorderAlarmListener = new OnAlarmListener(this) { // from class: com.android.launcher3.Folder.1
            final Folder this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.launcher3.OnAlarmListener
            public void onAlarm(Alarm alarm) {
                this.this$0.mContent.realTimeReorder(this.this$0.mEmptyCellRank, this.this$0.mTargetRank);
                this.this$0.mEmptyCellRank = this.this$0.mTargetRank;
            }
        };
        this.mOnExitAlarmListener = new OnAlarmListener(this) { // from class: com.android.launcher3.Folder.2
            final Folder this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.launcher3.OnAlarmListener
            public void onAlarm(Alarm alarm) {
                this.this$0.completeDragExit();
            }
        };
        setAlwaysDrawnWithCacheEnabled(false);
        this.mInputMethodManager = (InputMethodManager) getContext().getSystemService("input_method");
        Resources resources = getResources();
        this.mExpandDuration = resources.getInteger(2131427345);
        this.mMaterialExpandDuration = resources.getInteger(2131427346);
        this.mMaterialExpandStagger = resources.getInteger(2131427347);
        if (sDefaultFolderName == null) {
            sDefaultFolderName = resources.getString(2131558405);
        }
        if (sHintText == null) {
            sHintText = resources.getString(2131558435);
        }
        this.mLauncher = (Launcher) context;
        setFocusableInTouchMode(true);
    }

    private boolean beginDrag(View view, boolean z) {
        Object tag = view.getTag();
        if (tag instanceof ShortcutInfo) {
            ShortcutInfo shortcutInfo = (ShortcutInfo) tag;
            if (view.isInTouchMode()) {
                this.mLauncher.getWorkspace().beginDragShared(view, new Point(), this, z);
                this.mCurrentDragInfo = shortcutInfo;
                this.mEmptyCellRank = shortcutInfo.rank;
                this.mCurrentDragView = view;
                this.mContent.removeItem(this.mCurrentDragView);
                this.mInfo.remove(this.mCurrentDragInfo);
                this.mDragInProgress = true;
                this.mItemAddedBackToSelfViaIcon = false;
                return true;
            }
            return false;
        }
        return true;
    }

    private void centerAboutIcon() {
        DragLayer.LayoutParams layoutParams = (DragLayer.LayoutParams) getLayoutParams();
        DragLayer dragLayer = (DragLayer) this.mLauncher.findViewById(2131296288);
        int paddingLeft = getPaddingLeft() + getPaddingRight() + this.mContent.getDesiredWidth();
        int folderHeight = getFolderHeight();
        float descendantRectRelativeToSelf = dragLayer.getDescendantRectRelativeToSelf(this.mFolderIcon, sTempRect);
        DeviceProfile deviceProfile = this.mLauncher.getDeviceProfile();
        int width = (int) (sTempRect.left + ((sTempRect.width() * descendantRectRelativeToSelf) / 2.0f));
        int height = (int) (sTempRect.top + ((sTempRect.height() * descendantRectRelativeToSelf) / 2.0f));
        int i = width - (paddingLeft / 2);
        int i2 = height - (folderHeight / 2);
        this.mLauncher.getWorkspace().getPageAreaRelativeToDragLayer(sTempRect);
        int min = Math.min(Math.max(sTempRect.left, i), (sTempRect.left + sTempRect.width()) - paddingLeft);
        int min2 = Math.min(Math.max(sTempRect.top, i2), (sTempRect.top + sTempRect.height()) - folderHeight);
        if (deviceProfile.isPhone && deviceProfile.availableWidthPx - paddingLeft < deviceProfile.iconSizePx) {
            min = (deviceProfile.availableWidthPx - paddingLeft) / 2;
        } else if (paddingLeft >= sTempRect.width()) {
            min = sTempRect.left + ((sTempRect.width() - paddingLeft) / 2);
        }
        if (folderHeight >= sTempRect.height()) {
            min2 = sTempRect.top + ((sTempRect.height() - folderHeight) / 2);
        }
        Log.d("Launcher.Folder", "centerAboutIcon now, after sTempRect = " + sTempRect + "mFolderIcon = " + this.mFolderIcon);
        int i3 = (paddingLeft / 2) + (i - min);
        int i4 = (folderHeight / 2) + (i2 - min2);
        setPivotX(i3);
        setPivotY(i4);
        this.mFolderIconPivotX = (int) (this.mFolderIcon.getMeasuredWidth() * ((i3 * 1.0f) / paddingLeft));
        this.mFolderIconPivotY = (int) (this.mFolderIcon.getMeasuredHeight() * ((i4 * 1.0f) / folderHeight));
        layoutParams.width = paddingLeft;
        layoutParams.height = folderHeight;
        layoutParams.x = min;
        layoutParams.y = min2;
    }

    private void clearDragInfo() {
        this.mCurrentDragInfo = null;
        this.mCurrentDragView = null;
        this.mSuppressOnAdd = false;
        this.mIsExternalDrag = false;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @SuppressLint({"InflateParams"})
    public static Folder fromXml(Launcher launcher) {
        return (Folder) launcher.getLayoutInflater().inflate(FeatureFlags.LAUNCHER3_ICON_NORMALIZATION ? 2130968602 : 2130968601, (ViewGroup) null);
    }

    private int getContentAreaHeight() {
        DeviceProfile deviceProfile = this.mLauncher.getDeviceProfile();
        Rect workspacePadding = deviceProfile.getWorkspacePadding(this.mContent.mIsRtl);
        return Math.max(Math.min(((deviceProfile.availableHeightPx - workspacePadding.top) - workspacePadding.bottom) - this.mFooterHeight, this.mContent.getDesiredHeight()), 5);
    }

    private int getContentAreaWidth() {
        return Math.max(this.mContent.getDesiredWidth(), 5);
    }

    private int getFolderHeight() {
        return getFolderHeight(getContentAreaHeight());
    }

    private int getFolderHeight(int i) {
        return getPaddingTop() + getPaddingBottom() + i + this.mFooterHeight;
    }

    private int getTargetRank(DropTarget.DragObject dragObject, float[] fArr) {
        float[] visualCenter = dragObject.getVisualCenter(fArr);
        return this.mContent.findNearestArea(((int) visualCenter[0]) - getPaddingLeft(), ((int) visualCenter[1]) - getPaddingTop());
    }

    private View getViewForInfo(ShortcutInfo shortcutInfo) {
        return this.mContent.iterateOverItems(new Workspace.ItemOperator(this, shortcutInfo) { // from class: com.android.launcher3.Folder.16
            final Folder this$0;
            final ShortcutInfo val$item;

            {
                this.this$0 = this;
                this.val$item = shortcutInfo;
            }

            @Override // com.android.launcher3.Workspace.ItemOperator
            public boolean evaluate(ItemInfo itemInfo, View view, View view2) {
                return itemInfo == this.val$item;
            }
        });
    }

    private void positionAndSizeAsIcon() {
        if (getParent() instanceof DragLayer) {
            setScaleX(0.8f);
            setScaleY(0.8f);
            setAlpha(0.0f);
            this.mState = 0;
        }
    }

    private void prepareReveal() {
        setScaleX(1.0f);
        setScaleY(1.0f);
        setAlpha(1.0f);
        this.mState = 0;
    }

    private void showScrollHint(int i, DropTarget.DragObject dragObject) {
        if (this.mScrollHintDir != i) {
            this.mContent.showScrollHint(i);
            this.mScrollHintDir = i;
        }
        if (this.mOnScrollHintAlarm.alarmPending() && this.mCurrentScrollDir == i) {
            return;
        }
        this.mCurrentScrollDir = i;
        this.mOnScrollHintAlarm.cancelAlarm();
        this.mOnScrollHintAlarm.setOnAlarmListener(new OnScrollHintListener(this, dragObject));
        this.mOnScrollHintAlarm.setAlarm(500L);
        this.mReorderAlarm.cancelAlarm();
        this.mTargetRank = this.mEmptyCellRank;
    }

    private void updateItemLocationsInDatabaseBatch() {
        ArrayList<View> itemsInReadingOrder = getItemsInReadingOrder();
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < itemsInReadingOrder.size(); i++) {
            ItemInfo itemInfo = (ItemInfo) itemsInReadingOrder.get(i).getTag();
            itemInfo.rank = i;
            arrayList.add(itemInfo);
        }
        LauncherModel.moveItemsInDatabase(this.mLauncher, arrayList, this.mInfo.id, 0);
    }

    /* JADX WARN: Code restructure failed: missing block: B:8:0x0036, code lost:
        if (r0 == 1) goto L11;
     */
    @Override // com.android.launcher3.DropTarget
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public boolean acceptDrop(DropTarget.DragObject dragObject) {
        boolean z;
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher.Folder", "acceptDrop: DragObject = " + dragObject);
        }
        int i = ((ItemInfo) dragObject.dragInfo).itemType;
        if (i != 0) {
            z = false;
        }
        z = !isFull();
        return z;
    }

    public void animateClosed() {
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher.Folder", "animateClosed: parent = " + getParent());
        }
        if (getParent() instanceof DragLayer) {
            ObjectAnimator ofPropertyValuesHolder = LauncherAnimUtils.ofPropertyValuesHolder(this, PropertyValuesHolder.ofFloat("alpha", 0.0f), PropertyValuesHolder.ofFloat("scaleX", 0.9f), PropertyValuesHolder.ofFloat("scaleY", 0.9f));
            ofPropertyValuesHolder.addListener(new AnimatorListenerAdapter(this) { // from class: com.android.launcher3.Folder.11
                final Folder this$0;

                {
                    this.this$0 = this;
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    this.this$0.setLayerType(0, null);
                    this.this$0.close(true);
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationStart(Animator animator) {
                    this.this$0.sendCustomAccessibilityEvent(32, this.this$0.getContext().getString(2131558450));
                    this.this$0.mState = 1;
                }
            });
            ofPropertyValuesHolder.setDuration(this.mExpandDuration);
            setLayerType(2, null);
            ofPropertyValuesHolder.start();
        }
    }

    public void animateOpen() {
        ObjectAnimator objectAnimator;
        Runnable runnable;
        if (getParent() instanceof DragLayer) {
            this.mContent.completePendingPageChanges();
            if (!this.mDragInProgress) {
                this.mContent.snapToPageImmediately(0);
            }
            this.mDeleteFolderOnDropCompleted = false;
            if (Utilities.ATLEAST_LOLLIPOP) {
                prepareReveal();
                centerAboutIcon();
                AnimatorSet createAnimatorSet = LauncherAnimUtils.createAnimatorSet();
                int paddingLeft = getPaddingLeft() + getPaddingRight() + this.mContent.getDesiredWidth();
                int folderHeight = getFolderHeight();
                float pivotX = (-0.075f) * ((paddingLeft / 2) - getPivotX());
                float pivotY = (-0.075f) * ((folderHeight / 2) - getPivotY());
                setTranslationX(pivotX);
                setTranslationY(pivotY);
                ObjectAnimator ofPropertyValuesHolder = ObjectAnimator.ofPropertyValuesHolder(this, PropertyValuesHolder.ofFloat("translationX", pivotX, 0.0f), PropertyValuesHolder.ofFloat("translationY", pivotY, 0.0f));
                ofPropertyValuesHolder.setDuration(this.mMaterialExpandDuration);
                ofPropertyValuesHolder.setStartDelay(this.mMaterialExpandStagger);
                ofPropertyValuesHolder.setInterpolator(new LogDecelerateInterpolator(100, 0));
                float hypot = (float) Math.hypot((int) Math.max(Math.max(paddingLeft - getPivotX(), 0.0f), getPivotX()), (int) Math.max(Math.max(folderHeight - getPivotY(), 0.0f), getPivotY()));
                boolean isHardwareAccelerated = isHardwareAccelerated();
                ValueAnimator valueAnimator = null;
                if (isHardwareAccelerated) {
                    valueAnimator = UiThreadCircularReveal.createCircularReveal(this, (int) getPivotX(), (int) getPivotY(), 0.0f, hypot);
                    valueAnimator.setDuration(this.mMaterialExpandDuration);
                    valueAnimator.setInterpolator(new LogDecelerateInterpolator(100, 0));
                }
                this.mContentWrapper.setAlpha(0.0f);
                ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this.mContentWrapper, "alpha", 0.0f, 1.0f);
                ofFloat.setDuration(this.mMaterialExpandDuration);
                ofFloat.setStartDelay(this.mMaterialExpandStagger);
                ofFloat.setInterpolator(new AccelerateInterpolator(1.5f));
                this.mFooter.setAlpha(0.0f);
                ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(this.mFooter, "alpha", 0.0f, 1.0f);
                ofFloat2.setDuration(this.mMaterialExpandDuration);
                ofFloat2.setStartDelay(this.mMaterialExpandStagger);
                ofFloat2.setInterpolator(new AccelerateInterpolator(1.5f));
                createAnimatorSet.play(ofPropertyValuesHolder);
                createAnimatorSet.play(ofFloat);
                createAnimatorSet.play(ofFloat2);
                if (isHardwareAccelerated) {
                    createAnimatorSet.play(valueAnimator);
                }
                objectAnimator = createAnimatorSet;
                this.mContentWrapper.setLayerType(2, null);
                this.mFooter.setLayerType(2, null);
                runnable = new Runnable(this) { // from class: com.android.launcher3.Folder.8
                    final Folder this$0;

                    {
                        this.this$0 = this;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        this.this$0.mContentWrapper.setLayerType(0, null);
                        this.this$0.mContentWrapper.setLayerType(0, null);
                    }
                };
            } else {
                positionAndSizeAsIcon();
                centerAboutIcon();
                ObjectAnimator ofPropertyValuesHolder2 = LauncherAnimUtils.ofPropertyValuesHolder(this, PropertyValuesHolder.ofFloat("alpha", 1.0f), PropertyValuesHolder.ofFloat("scaleX", 1.0f), PropertyValuesHolder.ofFloat("scaleY", 1.0f));
                ofPropertyValuesHolder2.setDuration(this.mExpandDuration);
                setLayerType(2, null);
                runnable = new Runnable(this) { // from class: com.android.launcher3.Folder.7
                    final Folder this$0;

                    {
                        this.this$0 = this;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        this.this$0.setLayerType(0, null);
                    }
                };
                objectAnimator = ofPropertyValuesHolder2;
            }
            objectAnimator.addListener(new AnimatorListenerAdapter(this, runnable) { // from class: com.android.launcher3.Folder.9
                final Folder this$0;
                final Runnable val$onCompleteRunnable;

                {
                    this.this$0 = this;
                    this.val$onCompleteRunnable = runnable;
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    this.this$0.mState = 2;
                    this.val$onCompleteRunnable.run();
                    this.this$0.mContent.setFocusOnFirstChild();
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationStart(Animator animator) {
                    this.this$0.sendCustomAccessibilityEvent(32, this.this$0.mContent.getAccessibilityDescription());
                    this.this$0.mState = 1;
                }
            });
            if (this.mContent.getPageCount() <= 1 || this.mInfo.hasOption(4)) {
                this.mFolderName.setTranslationX(0.0f);
                this.mContent.setMarkerScale(1.0f);
            } else {
                float desiredWidth = (((this.mContent.getDesiredWidth() - this.mFooter.getPaddingLeft()) - this.mFooter.getPaddingRight()) - this.mFolderName.getPaint().measureText(this.mFolderName.getText().toString())) / 2.0f;
                ExtendedEditText extendedEditText = this.mFolderName;
                float f = desiredWidth;
                if (this.mContent.mIsRtl) {
                    f = -desiredWidth;
                }
                extendedEditText.setTranslationX(f);
                this.mContent.setMarkerScale(0.0f);
                objectAnimator.addListener(new AnimatorListenerAdapter(this, !this.mDragInProgress) { // from class: com.android.launcher3.Folder.10
                    final Folder this$0;
                    final boolean val$updateAnimationFlag;

                    {
                        this.this$0 = this;
                        this.val$updateAnimationFlag = r5;
                    }

                    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                    public void onAnimationEnd(Animator animator) {
                        this.this$0.mFolderName.animate().setDuration(633L).translationX(0.0f).setInterpolator(Utilities.ATLEAST_LOLLIPOP ? AnimationUtils.loadInterpolator(this.this$0.mLauncher, 17563661) : new LogDecelerateInterpolator(100, 0));
                        this.this$0.mContent.animateMarkers();
                        if (this.val$updateAnimationFlag) {
                            this.this$0.mInfo.setOption(4, true, this.this$0.mLauncher);
                        }
                    }
                });
            }
            objectAnimator.start();
            if (this.mDragController.isDragging()) {
                this.mDragController.forceTouchMove();
            }
            FolderPagedView folderPagedView = this.mContent;
            folderPagedView.verifyVisibleHighResIcons(folderPagedView.getNextPage());
        }
    }

    public void beginExternalDrag(ShortcutInfo shortcutInfo) {
        this.mCurrentDragInfo = shortcutInfo;
        this.mEmptyCellRank = this.mContent.allocateRankForNewItem(shortcutInfo);
        this.mIsExternalDrag = true;
        this.mDragInProgress = true;
        this.mDragController.addDragListener(this);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void bind(FolderInfo folderInfo) {
        this.mInfo = folderInfo;
        ArrayList<ShortcutInfo> arrayList = folderInfo.contents;
        Collections.sort(arrayList, ITEM_POS_COMPARATOR);
        for (ShortcutInfo shortcutInfo : this.mContent.bindItems(arrayList)) {
            this.mInfo.remove(shortcutInfo);
            LauncherModel.deleteItemFromDatabase(this.mLauncher, shortcutInfo);
        }
        if (((DragLayer.LayoutParams) getLayoutParams()) == null) {
            DragLayer.LayoutParams layoutParams = new DragLayer.LayoutParams(0, 0);
            layoutParams.customPosition = true;
            setLayoutParams(layoutParams);
        }
        centerAboutIcon();
        this.mItemsInvalidated = true;
        updateTextViewFocus();
        this.mInfo.addListener(this);
        if (sDefaultFolderName.contentEquals(this.mInfo.title)) {
            this.mFolderName.setText("");
        } else {
            this.mFolderName.setText(this.mInfo.title);
        }
        this.mFolderIcon.post(new Runnable(this) { // from class: com.android.launcher3.Folder.6
            final Folder this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                if (this.this$0.getItemCount() <= 1) {
                    this.this$0.replaceFolderWithFinalItem();
                }
            }
        });
    }

    public void close(boolean z) {
        DragLayer dragLayer = (DragLayer) getParent();
        if (dragLayer != null) {
            dragLayer.removeView(this);
        }
        this.mDragController.removeDropTarget(this);
        clearFocus();
        if (z) {
            this.mFolderIcon.requestFocus();
        }
        if (this.mRearrangeOnClose) {
            rearrangeChildren();
            this.mRearrangeOnClose = false;
        }
        if (getItemCount() <= 1) {
            if (!this.mDragInProgress && !this.mSuppressFolderDeletion) {
                replaceFolderWithFinalItem();
            } else if (this.mDragInProgress) {
                this.mDeleteFolderOnDropCompleted = true;
            }
        }
        this.mSuppressFolderDeletion = false;
        clearDragInfo();
        this.mState = 0;
    }

    public void completeDragExit() {
        if (this.mInfo.opened) {
            this.mLauncher.closeFolder();
            this.mRearrangeOnClose = true;
        } else if (this.mState == 1) {
            this.mRearrangeOnClose = true;
        } else {
            rearrangeChildren();
            clearDragInfo();
        }
    }

    @Override // com.android.launcher3.UninstallDropTarget.UninstallSource
    public void deferCompleteDropAfterUninstallActivity() {
        this.mDeferDropAfterUninstall = true;
    }

    public void dismissEditingName() {
        this.mInputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
        doneEditingFolderName(true);
    }

    @Override // android.view.View
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        return true;
    }

    public void doneEditingFolderName(boolean z) {
        this.mFolderName.setHint(sHintText);
        String editable = this.mFolderName.getText().toString();
        this.mInfo.setTitle(editable);
        LauncherModel.updateItemInDatabase(this.mLauncher, this.mInfo);
        if (z) {
            sendCustomAccessibilityEvent(32, String.format(getContext().getString(2131558451), editable));
        }
        this.mFolderName.clearFocus();
        Selection.setSelection(this.mFolderName.getText(), 0, 0);
        this.mIsEditingName = false;
    }

    @Override // com.android.launcher3.accessibility.LauncherAccessibilityDelegate.AccessibilityDragSource
    public void enableAccessibleDrag(boolean z) {
        this.mLauncher.getSearchDropTargetBar().enableAccessibleDrag(z);
        for (int i = 0; i < this.mContent.getChildCount(); i++) {
            this.mContent.getPageAt(i).enableAccessibleDrag(z, 1);
        }
        this.mFooter.setImportantForAccessibility(z ? 4 : 0);
        this.mLauncher.getWorkspace().setAddNewPageOnDrag(!z);
    }

    @Override // com.android.launcher3.Stats.LaunchSourceProvider
    public void fillInLaunchSourceData(View view, Bundle bundle) {
        Stats.LaunchSourceUtils.populateSourceDataFromAncestorProvider(this.mFolderIcon, bundle);
        bundle.putString("sub_container", "folder");
        bundle.putInt("sub_container_page", this.mContent.getCurrentPage());
    }

    public View getEditTextRegion() {
        return this.mFolderName;
    }

    @Override // com.android.launcher3.DropTarget
    public void getHitRectRelativeToDragLayer(Rect rect) {
        getHitRect(rect);
        rect.left -= this.mScrollAreaOffset;
        rect.right += this.mScrollAreaOffset;
    }

    public FolderInfo getInfo() {
        return this.mInfo;
    }

    @Override // com.android.launcher3.DragSource
    public float getIntrinsicIconScaleFactor() {
        return 1.0f;
    }

    public int getItemCount() {
        return this.mContent.getItemCount();
    }

    public ArrayList<View> getItemsInReadingOrder() {
        if (this.mItemsInvalidated) {
            this.mItemsInReadingOrder.clear();
            this.mContent.iterateOverItems(new Workspace.ItemOperator(this) { // from class: com.android.launcher3.Folder.17
                final Folder this$0;

                {
                    this.this$0 = this;
                }

                @Override // com.android.launcher3.Workspace.ItemOperator
                public boolean evaluate(ItemInfo itemInfo, View view, View view2) {
                    this.this$0.mItemsInReadingOrder.add(view);
                    return false;
                }
            });
            this.mItemsInvalidated = false;
        }
        return this.mItemsInReadingOrder;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public float getPivotXForIconAnimation() {
        return this.mFolderIconPivotX;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public float getPivotYForIconAnimation() {
        return this.mFolderIconPivotY;
    }

    public void hideItem(ShortcutInfo shortcutInfo) {
        getViewForInfo(shortcutInfo).setVisibility(4);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean isDestroyed() {
        return this.mDestroyed;
    }

    @Override // com.android.launcher3.DropTarget
    public boolean isDropEnabled() {
        return true;
    }

    public boolean isEditingName() {
        return this.mIsEditingName;
    }

    public boolean isFull() {
        return this.mContent.isFull();
    }

    @TargetApi(17)
    public boolean isLayoutRtl() {
        boolean z = true;
        if (getLayoutDirection() != 1) {
            z = false;
        }
        return z;
    }

    public void notifyDrop() {
        if (this.mDragInProgress) {
            this.mItemAddedBackToSelfViaIcon = true;
        }
    }

    @Override // com.android.launcher3.FolderInfo.FolderListener
    public void onAdd(ShortcutInfo shortcutInfo) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher.Folder", "onAdd item = " + shortcutInfo);
        }
        if (this.mSuppressOnAdd) {
            return;
        }
        this.mContent.createAndAddViewForRank(shortcutInfo, this.mContent.allocateRankForNewItem(shortcutInfo));
        this.mItemsInvalidated = true;
        LauncherModel.addOrMoveItemInDatabase(this.mLauncher, shortcutInfo, this.mInfo.id, 0L, shortcutInfo.cellX, shortcutInfo.cellY);
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        Object tag = view.getTag();
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher.Folder", "onClick: v = " + view + ", tag = " + tag);
        }
        if (tag instanceof ShortcutInfo) {
            this.mLauncher.onClick(view);
        }
    }

    @Override // com.android.launcher3.DragController.DragListener
    public void onDragEnd() {
        if (this.mIsExternalDrag && this.mDragInProgress) {
            completeDragExit();
        }
        this.mDragController.removeDragListener(this);
    }

    @Override // com.android.launcher3.DropTarget
    public void onDragEnter(DropTarget.DragObject dragObject) {
        this.mPrevTargetRank = -1;
        this.mOnExitAlarm.cancelAlarm();
        this.mScrollAreaOffset = (dragObject.dragView.getDragRegionWidth() / 2) - dragObject.xOffset;
    }

    @Override // com.android.launcher3.DropTarget
    public void onDragExit(DropTarget.DragObject dragObject) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher.Folder", "onDragExit: DragObject = " + dragObject);
        }
        if (!dragObject.dragComplete) {
            this.mOnExitAlarm.setOnAlarmListener(this.mOnExitAlarmListener);
            this.mOnExitAlarm.setAlarm(400L);
        }
        this.mReorderAlarm.cancelAlarm();
        this.mOnScrollHintAlarm.cancelAlarm();
        this.mScrollPauseAlarm.cancelAlarm();
        if (this.mScrollHintDir != -1) {
            this.mContent.clearScrollHint();
            this.mScrollHintDir = -1;
        }
    }

    @Override // com.android.launcher3.DropTarget
    public void onDragOver(DropTarget.DragObject dragObject) {
        onDragOver(dragObject, 250);
    }

    void onDragOver(DropTarget.DragObject dragObject, int i) {
        if (this.mScrollPauseAlarm.alarmPending()) {
            return;
        }
        float[] fArr = new float[2];
        this.mTargetRank = getTargetRank(dragObject, fArr);
        if (this.mTargetRank != this.mPrevTargetRank) {
            this.mReorderAlarm.cancelAlarm();
            this.mReorderAlarm.setOnAlarmListener(this.mReorderAlarmListener);
            this.mReorderAlarm.setAlarm(250L);
            this.mPrevTargetRank = this.mTargetRank;
            if (dragObject.stateAnnouncer != null) {
                dragObject.stateAnnouncer.announce(getContext().getString(2131558471, Integer.valueOf(this.mTargetRank + 1)));
            }
        }
        float f = fArr[0];
        int nextPage = this.mContent.getNextPage();
        float cellWidth = this.mContent.getCurrentCellLayout().getCellWidth() * 0.45f;
        boolean z = f < cellWidth;
        boolean z2 = f > ((float) getWidth()) - cellWidth;
        if (nextPage > 0) {
            if (this.mContent.mIsRtl ? z2 : z) {
                showScrollHint(0, dragObject);
                return;
            }
        }
        if (nextPage < this.mContent.getPageCount() - 1) {
            if (!this.mContent.mIsRtl) {
                z = z2;
            }
            if (z) {
                showScrollHint(1, dragObject);
                return;
            }
        }
        this.mOnScrollHintAlarm.cancelAlarm();
        if (this.mScrollHintDir != -1) {
            this.mContent.clearScrollHint();
            this.mScrollHintDir = -1;
        }
    }

    @Override // com.android.launcher3.DragController.DragListener
    public void onDragStart(DragSource dragSource, Object obj, int i) {
    }

    @Override // com.android.launcher3.DropTarget
    public void onDrop(DropTarget.DragObject dragObject) {
        View view;
        Runnable runnable = null;
        if (dragObject.dragSource != this.mLauncher.getWorkspace()) {
            runnable = dragObject.dragSource instanceof Folder ? null : new Runnable(this) { // from class: com.android.launcher3.Folder.15
                final Folder this$0;

                {
                    this.this$0 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$0.mLauncher.exitSpringLoadedDragModeDelayed(true, 300, null);
                }
            };
        }
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher.Folder", "onDrop: DragObject = " + dragObject);
        }
        if (!this.mContent.rankOnCurrentPage(this.mEmptyCellRank)) {
            this.mTargetRank = getTargetRank(dragObject, null);
            this.mReorderAlarmListener.onAlarm(this.mReorderAlarm);
            this.mOnScrollHintAlarm.cancelAlarm();
            this.mScrollPauseAlarm.cancelAlarm();
        }
        this.mContent.completePendingPageChanges();
        ShortcutInfo shortcutInfo = this.mCurrentDragInfo;
        if (this.mIsExternalDrag) {
            view = this.mContent.createAndAddViewForRank(shortcutInfo, this.mEmptyCellRank);
            LauncherModel.addOrMoveItemInDatabase(this.mLauncher, shortcutInfo, this.mInfo.id, 0L, shortcutInfo.cellX, shortcutInfo.cellY);
            if (dragObject.dragSource != this) {
                updateItemLocationsInDatabaseBatch();
            }
            this.mIsExternalDrag = false;
        } else {
            view = this.mCurrentDragView;
            this.mContent.addViewForRank(view, shortcutInfo, this.mEmptyCellRank);
        }
        if (dragObject.dragView.hasDrawn()) {
            float scaleX = getScaleX();
            float scaleY = getScaleY();
            setScaleX(1.0f);
            setScaleY(1.0f);
            this.mLauncher.getDragLayer().animateViewIntoPosition(dragObject.dragView, view, runnable, null);
            setScaleX(scaleX);
            setScaleY(scaleY);
        } else {
            dragObject.deferDragViewCleanupPostAnimation = false;
            view.setVisibility(0);
        }
        this.mItemsInvalidated = true;
        rearrangeChildren();
        this.mSuppressOnAdd = true;
        this.mInfo.add(shortcutInfo);
        this.mSuppressOnAdd = false;
        this.mCurrentDragInfo = null;
        this.mDragInProgress = false;
        if (this.mContent.getPageCount() > 1) {
            this.mInfo.setOption(4, true, this.mLauncher);
        }
    }

    @Override // com.android.launcher3.DragSource
    public void onDropCompleted(View view, DropTarget.DragObject dragObject, boolean z, boolean z2) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher.Folder", "onDropCompleted: View = " + view + ", DragObject = " + dragObject + ", isFlingToDelete = " + z + ", success = " + z2);
        }
        if (this.mDeferDropAfterUninstall) {
            Log.d("Launcher.Folder", "Deferred handling drop because waiting for uninstall.");
            this.mDeferredAction = new Runnable(this, view, dragObject, z, z2) { // from class: com.android.launcher3.Folder.12
                final Folder this$0;
                final DropTarget.DragObject val$d;
                final boolean val$isFlingToDelete;
                final boolean val$success;
                final View val$target;

                {
                    this.this$0 = this;
                    this.val$target = view;
                    this.val$d = dragObject;
                    this.val$isFlingToDelete = z;
                    this.val$success = z2;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$0.onDropCompleted(this.val$target, this.val$d, this.val$isFlingToDelete, this.val$success);
                    this.this$0.mDeferredAction = null;
                }
            };
            return;
        }
        boolean z3 = z2 ? this.mDeferredAction != null ? this.mUninstallSuccessful : true : false;
        if (!z3) {
            ShortcutInfo shortcutInfo = (ShortcutInfo) dragObject.dragInfo;
            View createNewView = (this.mCurrentDragView == null || this.mCurrentDragView.getTag() != shortcutInfo) ? this.mContent.createNewView(shortcutInfo) : this.mCurrentDragView;
            ArrayList<View> itemsInReadingOrder = getItemsInReadingOrder();
            itemsInReadingOrder.add(shortcutInfo.rank, createNewView);
            this.mContent.arrangeChildren(itemsInReadingOrder, itemsInReadingOrder.size());
            this.mItemsInvalidated = true;
            this.mSuppressOnAdd = true;
            this.mFolderIcon.onDrop(dragObject);
            this.mSuppressOnAdd = false;
        } else if (this.mDeleteFolderOnDropCompleted && !this.mItemAddedBackToSelfViaIcon && view != this) {
            replaceFolderWithFinalItem();
        }
        if (view != this && this.mOnExitAlarm.alarmPending()) {
            this.mOnExitAlarm.cancelAlarm();
            if (!z3) {
                this.mSuppressFolderDeletion = true;
            }
            this.mScrollPauseAlarm.cancelAlarm();
            completeDragExit();
        }
        this.mDeleteFolderOnDropCompleted = false;
        this.mDragInProgress = false;
        this.mItemAddedBackToSelfViaIcon = false;
        this.mCurrentDragInfo = null;
        this.mCurrentDragView = null;
        this.mSuppressOnAdd = false;
        updateItemLocationsInDatabaseBatch();
        if (getItemCount() <= this.mContent.itemsPerPage()) {
            this.mInfo.setOption(4, false, this.mLauncher);
        }
    }

    @Override // android.widget.TextView.OnEditorActionListener
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        if (i == 6) {
            dismissEditingName();
            return true;
        }
        return false;
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mContentWrapper = findViewById(2131296311);
        this.mContent = (FolderPagedView) findViewById(2131296312);
        this.mContent.setFolder(this);
        this.mFolderName = (ExtendedEditText) findViewById(2131296315);
        this.mFolderName.setOnBackKeyListener(new ExtendedEditText.OnBackKeyListener(this) { // from class: com.android.launcher3.Folder.4
            final Folder this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.launcher3.ExtendedEditText.OnBackKeyListener
            public boolean onBackKey() {
                this.this$0.doneEditingFolderName(true);
                return false;
            }
        });
        this.mFolderName.setOnFocusChangeListener(this);
        if (!Utilities.ATLEAST_MARSHMALLOW) {
            this.mFolderName.setCustomSelectionActionModeCallback(new ActionMode.Callback(this) { // from class: com.android.launcher3.Folder.5
                final Folder this$0;

                {
                    this.this$0 = this;
                }

                @Override // android.view.ActionMode.Callback
                public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                    return false;
                }

                @Override // android.view.ActionMode.Callback
                public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                    return false;
                }

                @Override // android.view.ActionMode.Callback
                public void onDestroyActionMode(ActionMode actionMode) {
                }

                @Override // android.view.ActionMode.Callback
                public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                    return false;
                }
            });
        }
        this.mFolderName.setOnEditorActionListener(this);
        this.mFolderName.setSelectAllOnFocus(true);
        this.mFolderName.setInputType(this.mFolderName.getInputType() | 524288 | 8192);
        this.mFooter = findViewById(2131296314);
        this.mFooter.measure(0, 0);
        this.mFooterHeight = this.mFooter.getMeasuredHeight();
    }

    @Override // com.android.launcher3.DropTarget
    public void onFlingToDelete(DropTarget.DragObject dragObject, PointF pointF) {
    }

    @Override // com.android.launcher3.DragSource
    public void onFlingToDeleteCompleted() {
    }

    @Override // android.view.View.OnFocusChangeListener
    public void onFocusChange(View view, boolean z) {
        if (view == this.mFolderName) {
            if (z) {
                startEditingFolderName();
            } else {
                dismissEditingName();
            }
        }
    }

    @Override // com.android.launcher3.FolderInfo.FolderListener
    public void onItemsChanged() {
        updateTextViewFocus();
    }

    @Override // android.view.View.OnLongClickListener
    public boolean onLongClick(View view) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher.Folder", "onLongClick: v = " + view + ", tag = " + view.getTag());
        }
        if (this.mLauncher.isDraggingEnabled()) {
            return beginDrag(view, false);
        }
        return true;
    }

    @Override // android.widget.LinearLayout, android.view.View
    protected void onMeasure(int i, int i2) {
        int contentAreaWidth = getContentAreaWidth();
        int contentAreaHeight = getContentAreaHeight();
        int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(contentAreaWidth, 1073741824);
        int makeMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(contentAreaHeight, 1073741824);
        this.mContent.setFixedSize(contentAreaWidth, contentAreaHeight);
        this.mContentWrapper.measure(makeMeasureSpec, makeMeasureSpec2);
        if (this.mContent.getChildCount() > 0) {
            int cellWidth = (this.mContent.getPageAt(0).getCellWidth() - this.mLauncher.getDeviceProfile().iconSizePx) / 2;
            this.mFooter.setPadding(this.mContent.getPaddingLeft() + cellWidth, this.mFooter.getPaddingTop(), this.mContent.getPaddingRight() + cellWidth, this.mFooter.getPaddingBottom());
        }
        this.mFooter.measure(makeMeasureSpec, View.MeasureSpec.makeMeasureSpec(this.mFooterHeight, 1073741824));
        setMeasuredDimension(getPaddingLeft() + getPaddingRight() + contentAreaWidth, getFolderHeight(contentAreaHeight));
    }

    @Override // com.android.launcher3.FolderInfo.FolderListener
    public void onRemove(ShortcutInfo shortcutInfo) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher.Folder", "onRemove item = " + shortcutInfo);
        }
        this.mItemsInvalidated = true;
        if (shortcutInfo == this.mCurrentDragInfo) {
            return;
        }
        this.mContent.removeItem(getViewForInfo(shortcutInfo));
        if (this.mState == 1) {
            this.mRearrangeOnClose = true;
        } else {
            rearrangeChildren();
        }
        if (getItemCount() <= 1) {
            if (this.mInfo.opened) {
                this.mLauncher.closeFolder(this, true);
            } else {
                replaceFolderWithFinalItem();
            }
        }
    }

    @Override // com.android.launcher3.FolderInfo.FolderListener
    public void onTitleChanged(CharSequence charSequence) {
    }

    @Override // android.view.View
    @SuppressLint({"ClickableViewAccessibility"})
    public boolean onTouchEvent(MotionEvent motionEvent) {
        return true;
    }

    @Override // com.android.launcher3.UninstallDropTarget.UninstallSource
    public void onUninstallActivityReturned(boolean z) {
        this.mDeferDropAfterUninstall = false;
        this.mUninstallSuccessful = z;
        if (this.mDeferredAction != null) {
            this.mDeferredAction.run();
        }
    }

    @Override // com.android.launcher3.DropTarget
    public void prepareAccessibilityDrop() {
        if (this.mReorderAlarm.alarmPending()) {
            this.mReorderAlarm.cancelAlarm();
            this.mReorderAlarmListener.onAlarm(this.mReorderAlarm);
        }
    }

    public void rearrangeChildren() {
        rearrangeChildren(-1);
    }

    public void rearrangeChildren(int i) {
        ArrayList<View> itemsInReadingOrder = getItemsInReadingOrder();
        this.mContent.arrangeChildren(itemsInReadingOrder, Math.max(i, itemsInReadingOrder.size()));
        this.mItemsInvalidated = true;
    }

    void replaceFolderWithFinalItem() {
        Runnable runnable = new Runnable(this) { // from class: com.android.launcher3.Folder.13
            final Folder this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                int size = this.this$0.mInfo.contents.size();
                if (size <= 1) {
                    View view = null;
                    if (size == 1) {
                        CellLayout cellLayout = this.this$0.mLauncher.getCellLayout(this.this$0.mInfo.container, this.this$0.mInfo.screenId);
                        ShortcutInfo remove = this.this$0.mInfo.contents.remove(0);
                        view = this.this$0.mLauncher.createShortcut(cellLayout, remove);
                        LauncherModel.addOrMoveItemInDatabase(this.this$0.mLauncher, remove, this.this$0.mInfo.container, this.this$0.mInfo.screenId, this.this$0.mInfo.cellX, this.this$0.mInfo.cellY);
                    }
                    this.this$0.mLauncher.removeItem(this.this$0.mFolderIcon, this.this$0.mInfo, true);
                    if (this.this$0.mFolderIcon instanceof DropTarget) {
                        this.this$0.mDragController.removeDropTarget((DropTarget) this.this$0.mFolderIcon);
                    }
                    if (view != null) {
                        this.this$0.mLauncher.getWorkspace().addInScreenFromBind(view, this.this$0.mInfo.container, this.this$0.mInfo.screenId, this.this$0.mInfo.cellX, this.this$0.mInfo.cellY, this.this$0.mInfo.spanX, this.this$0.mInfo.spanY);
                        view.requestFocus();
                    }
                }
            }
        };
        View lastItem = this.mContent.getLastItem();
        if (lastItem != null) {
            this.mFolderIcon.performDestroyAnimation(lastItem, runnable);
        } else {
            runnable.run();
        }
        this.mDestroyed = true;
    }

    void sendCustomAccessibilityEvent(int i, String str) {
        AccessibilityManager accessibilityManager = (AccessibilityManager) getContext().getSystemService("accessibility");
        if (accessibilityManager.isEnabled()) {
            AccessibilityEvent obtain = AccessibilityEvent.obtain(i);
            onInitializeAccessibilityEvent(obtain);
            obtain.getText().add(str);
            accessibilityManager.sendAccessibilityEvent(obtain);
        }
    }

    public void setDragController(DragController dragController) {
        this.mDragController = dragController;
    }

    public void setFolderIcon(FolderIcon folderIcon) {
        this.mFolderIcon = folderIcon;
    }

    public void showItem(ShortcutInfo shortcutInfo) {
        getViewForInfo(shortcutInfo).setVisibility(0);
    }

    @Override // com.android.launcher3.accessibility.LauncherAccessibilityDelegate.AccessibilityDragSource
    public void startDrag(CellLayout.CellInfo cellInfo, boolean z) {
        beginDrag(cellInfo.cell, z);
    }

    public void startEditingFolderName() {
        this.mFolderName.setHint("");
        this.mIsEditingName = true;
    }

    @Override // com.android.launcher3.DragSource
    public boolean supportsAppInfoDropTarget() {
        return false;
    }

    @Override // com.android.launcher3.DragSource
    public boolean supportsDeleteDropTarget() {
        return true;
    }

    @Override // com.android.launcher3.DragSource
    public boolean supportsFlingToDelete() {
        return true;
    }

    public void updateTextViewFocus() {
        View firstItem = this.mContent.getFirstItem();
        View lastItem = this.mContent.getLastItem();
        if (firstItem == null || lastItem == null) {
            return;
        }
        this.mFolderName.setNextFocusDownId(lastItem.getId());
        this.mFolderName.setNextFocusRightId(lastItem.getId());
        this.mFolderName.setNextFocusLeftId(lastItem.getId());
        this.mFolderName.setNextFocusUpId(lastItem.getId());
        this.mFolderName.setNextFocusForwardId(firstItem.getId());
        setNextFocusDownId(firstItem.getId());
        setNextFocusRightId(firstItem.getId());
        setNextFocusLeftId(firstItem.getId());
        setNextFocusUpId(firstItem.getId());
        setOnKeyListener(new View.OnKeyListener(this, lastItem) { // from class: com.android.launcher3.Folder.14
            final Folder this$0;
            final View val$lastChild;

            {
                this.this$0 = this;
                this.val$lastChild = lastItem;
            }

            @Override // android.view.View.OnKeyListener
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if ((i == 61 ? keyEvent.hasModifiers(1) : false) && this.this$0.isFocused()) {
                    return this.val$lastChild.requestFocus();
                }
                return false;
            }
        });
    }
}

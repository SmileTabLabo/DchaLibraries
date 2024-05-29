package com.android.systemui.recents.tv.views;

import android.animation.Animator;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Outline;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.tv.RecentsTvActivity;
import com.android.systemui.recents.tv.animations.DismissAnimationsHolder;
import com.android.systemui.recents.tv.animations.RecentsRowFocusAnimationHolder;
import com.android.systemui.recents.tv.animations.ViewFocusAnimator;
/* loaded from: a.zip:com/android/systemui/recents/tv/views/TaskCardView.class */
public class TaskCardView extends LinearLayout {
    private ImageView mBadgeView;
    private int mCornerRadius;
    private DismissAnimationsHolder mDismissAnimationsHolder;
    private View mDismissIconView;
    private boolean mDismissState;
    private View mInfoFieldView;
    private RecentsRowFocusAnimationHolder mRecentsRowFocusAnimationHolder;
    private Task mTask;
    private View mThumbnailView;
    private TextView mTitleTextView;
    private boolean mTouchExplorationEnabled;
    private ViewFocusAnimator mViewFocusAnimator;

    public TaskCardView(Context context) {
        this(context, null);
    }

    public TaskCardView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public TaskCardView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mDismissState = false;
        setLayoutDirection(getResources().getConfiguration().getLayoutDirection());
    }

    public static int getNumberOfVisibleTasks(Context context) {
        Resources resources = context.getResources();
        Display defaultDisplay = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        Point point = new Point();
        defaultDisplay.getSize(point);
        return (int) (Math.ceil(point.x / (resources.getDimensionPixelSize(2131690044) + (resources.getDimensionPixelSize(2131690058) * 2.0d))) + 1.0d);
    }

    public static Rect getStartingCardThumbnailRect(Context context, boolean z, int i) {
        return i > 1 ? getStartingCardThumbnailRectForStartPosition(context, z) : getStartingCardThumbnailRectForFocusedPosition(context, z);
    }

    private static Rect getStartingCardThumbnailRectForFocusedPosition(Context context, boolean z) {
        Resources resources = context.getResources();
        TypedValue typedValue = new TypedValue();
        resources.getValue(2131755103, typedValue, true);
        float f = z ? typedValue.getFloat() : 1.0f;
        int dimensionPixelOffset = resources.getDimensionPixelOffset(2131690044);
        int i = (int) ((dimensionPixelOffset * f) - dimensionPixelOffset);
        int dimensionPixelOffset2 = resources.getDimensionPixelOffset(2131690045);
        int i2 = (int) ((dimensionPixelOffset2 * f) - dimensionPixelOffset2);
        int dimensionPixelOffset3 = resources.getDimensionPixelOffset(2131690056);
        int dimensionPixelOffset4 = resources.getDimensionPixelOffset(2131690046) + resources.getDimensionPixelOffset(2131690052);
        int i3 = (int) ((dimensionPixelOffset4 * f) - dimensionPixelOffset4);
        int dimensionPixelOffset5 = resources.getDimensionPixelOffset(2131690066) + resources.getDimensionPixelOffset(2131690067) + resources.getDimensionPixelOffset(2131690065) + resources.getDimensionPixelOffset(2131690068);
        int i4 = i2 + i3 + ((int) ((dimensionPixelOffset5 * f) - dimensionPixelOffset5));
        Display defaultDisplay = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        Point point = new Point();
        defaultDisplay.getSize(point);
        int i5 = point.x;
        return new Rect(((i5 / 2) - (dimensionPixelOffset / 2)) - (i / 2), (dimensionPixelOffset3 - (i4 / 2)) + ((int) (dimensionPixelOffset4 * f)), (i5 / 2) + (dimensionPixelOffset / 2) + (i / 2), (dimensionPixelOffset3 - (i4 / 2)) + ((int) (dimensionPixelOffset4 * f)) + ((int) (dimensionPixelOffset2 * f)));
    }

    private static Rect getStartingCardThumbnailRectForStartPosition(Context context, boolean z) {
        Resources resources = context.getResources();
        int dimensionPixelOffset = resources.getDimensionPixelOffset(2131690044);
        int dimensionPixelOffset2 = resources.getDimensionPixelOffset(2131690058) * 2;
        int i = dimensionPixelOffset2;
        if (z) {
            i = dimensionPixelOffset2 + resources.getDimensionPixelOffset(2131690059);
        }
        int dimensionPixelOffset3 = resources.getDimensionPixelOffset(2131690045);
        int dimensionPixelOffset4 = resources.getDimensionPixelOffset(2131690056);
        int dimensionPixelOffset5 = resources.getDimensionPixelOffset(2131690046) + resources.getDimensionPixelOffset(2131690052);
        Display defaultDisplay = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        Point point = new Point();
        defaultDisplay.getSize(point);
        int i2 = point.x;
        return new Rect((i2 / 2) + (dimensionPixelOffset / 2) + i, dimensionPixelOffset4 + dimensionPixelOffset5, (i2 / 2) + (dimensionPixelOffset / 2) + i + dimensionPixelOffset, dimensionPixelOffset4 + dimensionPixelOffset5 + dimensionPixelOffset3);
    }

    private void setAsBannerView(Drawable drawable, ImageView imageView) {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) imageView.getLayoutParams();
        layoutParams.width = getResources().getDimensionPixelSize(2131690047);
        layoutParams.height = getResources().getDimensionPixelSize(2131690048);
        imageView.setLayoutParams(layoutParams);
        imageView.setImageDrawable(drawable);
    }

    private void setAsIconView(Drawable drawable, ImageView imageView) {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) imageView.getLayoutParams();
        layoutParams.width = getResources().getDimensionPixelSize(2131690049);
        layoutParams.height = getResources().getDimensionPixelSize(2131690050);
        imageView.setLayoutParams(layoutParams);
        imageView.setImageDrawable(drawable);
    }

    private void setAsScreenShotView(Bitmap bitmap, ImageView imageView) {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) imageView.getLayoutParams();
        layoutParams.width = -1;
        layoutParams.height = -1;
        imageView.setLayoutParams(layoutParams);
        imageView.setClipToOutline(true);
        imageView.setOutlineProvider(new ViewOutlineProvider(this) { // from class: com.android.systemui.recents.tv.views.TaskCardView.1
            final TaskCardView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.ViewOutlineProvider
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), this.this$0.mCornerRadius);
            }
        });
        imageView.setImageBitmap(bitmap);
    }

    private void setDismissState(boolean z) {
        if (this.mDismissState != z) {
            this.mDismissState = z;
            if (this.mTouchExplorationEnabled) {
                return;
            }
            if (z) {
                this.mDismissAnimationsHolder.startEnterAnimation();
            } else {
                this.mDismissAnimationsHolder.startExitAnimation();
            }
        }
    }

    private void setThumbnailView() {
        ImageView imageView = (ImageView) findViewById(2131886630);
        PackageManager packageManager = getContext().getPackageManager();
        if (this.mTask.thumbnail != null) {
            setAsScreenShotView(this.mTask.thumbnail, imageView);
            return;
        }
        Drawable drawable = null;
        try {
            if (this.mTask.key != null) {
                drawable = packageManager.getActivityBanner(this.mTask.key.baseIntent);
            }
            if (drawable != null) {
                setAsBannerView(drawable, imageView);
            } else {
                setAsIconView(this.mTask.icon, imageView);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("TaskCardView", "Package not found : " + e);
            setAsIconView(this.mTask.icon, imageView);
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        switch (keyEvent.getKeyCode()) {
            case 19:
                if (keyEvent.getAction() == 0) {
                    if (isInDismissState()) {
                        setDismissState(false);
                        return true;
                    }
                    ((RecentsTvActivity) getContext()).requestPipControlsFocus();
                    return true;
                }
                return true;
            case 20:
                if (!isInDismissState() && keyEvent.getAction() == 0) {
                    setDismissState(true);
                    return true;
                }
                break;
            case 21:
            case 22:
                if (isInDismissState()) {
                    return true;
                }
                break;
        }
        return super.dispatchKeyEvent(keyEvent);
    }

    public View getDismissIconView() {
        return this.mDismissIconView;
    }

    @Override // android.view.View
    public void getFocusedRect(Rect rect) {
        this.mThumbnailView.getFocusedRect(rect);
    }

    public Rect getFocusedThumbnailRect() {
        Rect rect = new Rect();
        this.mThumbnailView.getGlobalVisibleRect(rect);
        return rect;
    }

    public View getInfoFieldView() {
        return this.mInfoFieldView;
    }

    public RecentsRowFocusAnimationHolder getRecentsRowFocusAnimationHolder() {
        return this.mRecentsRowFocusAnimationHolder;
    }

    public Task getTask() {
        return this.mTask;
    }

    public View getThumbnailView() {
        return this.mThumbnailView;
    }

    public ViewFocusAnimator getViewFocusAnimator() {
        return this.mViewFocusAnimator;
    }

    public void init(Task task) {
        this.mTask = task;
        this.mTitleTextView.setText(task.title);
        this.mBadgeView.setImageDrawable(task.icon);
        setThumbnailView();
        setContentDescription(task.titleDescription);
        this.mDismissState = false;
        this.mDismissAnimationsHolder.reset();
        this.mRecentsRowFocusAnimationHolder.reset();
    }

    public boolean isInDismissState() {
        return this.mDismissState;
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mThumbnailView = findViewById(2131886629);
        this.mInfoFieldView = findViewById(2131886626);
        this.mTitleTextView = (TextView) findViewById(2131886628);
        this.mBadgeView = (ImageView) findViewById(2131886627);
        this.mDismissIconView = findViewById(2131886631);
        this.mDismissAnimationsHolder = new DismissAnimationsHolder(this);
        this.mCornerRadius = getResources().getDimensionPixelSize(2131690016);
        this.mRecentsRowFocusAnimationHolder = new RecentsRowFocusAnimationHolder(this, this.mInfoFieldView);
        this.mTouchExplorationEnabled = Recents.getSystemServices().isTouchExplorationEnabled();
        if (this.mTouchExplorationEnabled) {
            this.mDismissIconView.setVisibility(8);
        } else {
            this.mDismissIconView.setVisibility(0);
        }
        this.mViewFocusAnimator = new ViewFocusAnimator(this);
    }

    public void startDismissTaskAnimation(Animator.AnimatorListener animatorListener) {
        this.mDismissState = false;
        this.mDismissAnimationsHolder.startDismissAnimation(animatorListener);
    }
}

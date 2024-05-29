package com.android.launcher3;

import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import com.android.launcher3.FastBitmapDrawable;
import com.android.launcher3.compat.PackageInstallerCompat;
/* loaded from: a.zip:com/android/launcher3/BaseRecyclerViewFastScrollBar.class */
public class BaseRecyclerViewFastScrollBar {
    private boolean mCanThumbDetach;
    private boolean mIgnoreDragGesture;
    private boolean mIsDragging;
    private boolean mIsThumbDetached;
    private float mLastTouchY;
    private BaseRecyclerViewFastScrollPopup mPopup;
    BaseRecyclerView mRv;
    private AnimatorSet mScrollbarAnimator;
    private int mThumbActiveColor;
    private int mThumbCurvature;
    int mThumbHeight;
    private int mThumbInactiveColor;
    private int mThumbMaxWidth;
    private int mThumbMinWidth;
    Paint mThumbPaint;
    int mThumbWidth;
    private int mTouchInset;
    private int mTouchOffset;
    private int mTrackWidth;
    Point mThumbOffset = new Point(-1, -1);
    private Path mThumbPath = new Path();
    private Rect mInvalidateRect = new Rect();
    private Rect mTmpRect = new Rect();
    private Paint mTrackPaint = new Paint();

    /* loaded from: a.zip:com/android/launcher3/BaseRecyclerViewFastScrollBar$FastScrollFocusableView.class */
    public interface FastScrollFocusableView {
        void setFastScrollFocusState(FastBitmapDrawable.State state, boolean z);
    }

    public BaseRecyclerViewFastScrollBar(BaseRecyclerView baseRecyclerView, Resources resources) {
        this.mRv = baseRecyclerView;
        this.mPopup = new BaseRecyclerViewFastScrollPopup(baseRecyclerView, resources);
        this.mTrackPaint.setColor(baseRecyclerView.getFastScrollerTrackColor(-16777216));
        this.mTrackPaint.setAlpha(30);
        this.mThumbInactiveColor = baseRecyclerView.getFastScrollerThumbInactiveColor(resources.getColor(2131361804));
        this.mThumbActiveColor = resources.getColor(2131361805);
        this.mThumbPaint = new Paint();
        this.mThumbPaint.setAntiAlias(true);
        this.mThumbPaint.setColor(this.mThumbInactiveColor);
        this.mThumbPaint.setStyle(Paint.Style.FILL);
        int dimensionPixelSize = resources.getDimensionPixelSize(2131230757);
        this.mThumbMinWidth = dimensionPixelSize;
        this.mThumbWidth = dimensionPixelSize;
        this.mThumbMaxWidth = resources.getDimensionPixelSize(2131230758);
        this.mThumbHeight = resources.getDimensionPixelSize(2131230759);
        this.mThumbCurvature = this.mThumbMaxWidth - this.mThumbMinWidth;
        this.mTouchInset = resources.getDimensionPixelSize(2131230760);
    }

    private boolean isNearThumb(int i, int i2) {
        this.mTmpRect.set(this.mThumbOffset.x, this.mThumbOffset.y, this.mThumbOffset.x + this.mThumbWidth, this.mThumbOffset.y + this.mThumbHeight);
        this.mTmpRect.inset(this.mTouchInset, this.mTouchInset);
        return this.mTmpRect.contains(i, i2);
    }

    private void showActiveScrollbar(boolean z) {
        if (this.mScrollbarAnimator != null) {
            this.mScrollbarAnimator.cancel();
        }
        this.mScrollbarAnimator = new AnimatorSet();
        this.mScrollbarAnimator.playTogether(ObjectAnimator.ofInt(this, "trackWidth", z ? this.mThumbMaxWidth : this.mThumbMinWidth), ObjectAnimator.ofInt(this, "thumbWidth", z ? this.mThumbMaxWidth : this.mThumbMinWidth));
        if (this.mThumbActiveColor != this.mThumbInactiveColor) {
            ValueAnimator ofObject = ValueAnimator.ofObject(new ArgbEvaluator(), Integer.valueOf(this.mThumbPaint.getColor()), Integer.valueOf(z ? this.mThumbActiveColor : this.mThumbInactiveColor));
            ofObject.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this) { // from class: com.android.launcher3.BaseRecyclerViewFastScrollBar.1
                final BaseRecyclerViewFastScrollBar this$0;

                {
                    this.this$0 = this;
                }

                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    this.this$0.mThumbPaint.setColor(((Integer) valueAnimator.getAnimatedValue()).intValue());
                    this.this$0.mRv.invalidate(this.this$0.mThumbOffset.x, this.this$0.mThumbOffset.y, this.this$0.mThumbOffset.x + this.this$0.mThumbWidth, this.this$0.mThumbOffset.y + this.this$0.mThumbHeight);
                }
            });
            this.mScrollbarAnimator.play(ofObject);
        }
        this.mScrollbarAnimator.setDuration(150L);
        this.mScrollbarAnimator.start();
    }

    private void updateThumbPath() {
        this.mThumbCurvature = this.mThumbMaxWidth - this.mThumbWidth;
        this.mThumbPath.reset();
        this.mThumbPath.moveTo(this.mThumbOffset.x + this.mThumbWidth, this.mThumbOffset.y);
        this.mThumbPath.lineTo(this.mThumbOffset.x + this.mThumbWidth, this.mThumbOffset.y + this.mThumbHeight);
        this.mThumbPath.lineTo(this.mThumbOffset.x, this.mThumbOffset.y + this.mThumbHeight);
        this.mThumbPath.cubicTo(this.mThumbOffset.x, this.mThumbOffset.y + this.mThumbHeight, this.mThumbOffset.x - this.mThumbCurvature, this.mThumbOffset.y + (this.mThumbHeight / 2), this.mThumbOffset.x, this.mThumbOffset.y);
        this.mThumbPath.close();
    }

    public void draw(Canvas canvas) {
        if (this.mThumbOffset.x < 0 || this.mThumbOffset.y < 0) {
            return;
        }
        if (this.mTrackPaint.getAlpha() > 0) {
            canvas.drawRect(this.mThumbOffset.x, 0.0f, this.mThumbOffset.x + this.mThumbWidth, this.mRv.getHeight(), this.mTrackPaint);
        }
        canvas.drawPath(this.mThumbPath, this.mThumbPaint);
        this.mPopup.draw(canvas);
    }

    public float getLastTouchY() {
        return this.mLastTouchY;
    }

    public int getThumbHeight() {
        return this.mThumbHeight;
    }

    public int getThumbMaxWidth() {
        return this.mThumbMaxWidth;
    }

    public Point getThumbOffset() {
        return this.mThumbOffset;
    }

    public int getThumbWidth() {
        return this.mThumbWidth;
    }

    public int getTrackWidth() {
        return this.mTrackWidth;
    }

    public void handleTouchEvent(MotionEvent motionEvent, int i, int i2, int i3) {
        int height;
        ViewConfiguration viewConfiguration = ViewConfiguration.get(this.mRv.getContext());
        int action = motionEvent.getAction();
        int y = (int) motionEvent.getY();
        switch (action) {
            case PackageInstallerCompat.STATUS_INSTALLED /* 0 */:
                if (isNearThumb(i, i2)) {
                    this.mTouchOffset = i2 - this.mThumbOffset.y;
                    return;
                }
                return;
            case 1:
            case 3:
                this.mTouchOffset = 0;
                this.mLastTouchY = 0.0f;
                this.mIgnoreDragGesture = false;
                if (this.mIsDragging) {
                    this.mIsDragging = false;
                    this.mPopup.animateVisibility(false);
                    showActiveScrollbar(false);
                    return;
                }
                return;
            case 2:
                this.mIgnoreDragGesture = (Math.abs(y - i2) > viewConfiguration.getScaledPagingTouchSlop()) | this.mIgnoreDragGesture;
                if (!this.mIsDragging && !this.mIgnoreDragGesture && this.mRv.supportsFastScrolling() && isNearThumb(i, i3) && Math.abs(y - i2) > viewConfiguration.getScaledTouchSlop()) {
                    this.mRv.getParent().requestDisallowInterceptTouchEvent(true);
                    this.mIsDragging = true;
                    if (this.mCanThumbDetach) {
                        this.mIsThumbDetached = true;
                    }
                    this.mTouchOffset += i3 - i2;
                    this.mPopup.animateVisibility(true);
                    showActiveScrollbar(true);
                }
                if (this.mIsDragging) {
                    int i4 = this.mRv.getBackgroundPadding().top;
                    float max = Math.max(i4, Math.min((this.mRv.getHeight() - this.mRv.getBackgroundPadding().bottom) - this.mThumbHeight, y - this.mTouchOffset));
                    String scrollToPositionAtProgress = this.mRv.scrollToPositionAtProgress((max - i4) / (height - i4));
                    this.mPopup.setSectionName(scrollToPositionAtProgress);
                    this.mPopup.animateVisibility(!scrollToPositionAtProgress.isEmpty());
                    this.mRv.invalidate(this.mPopup.updateFastScrollerBounds(i3));
                    this.mLastTouchY = max;
                    return;
                }
                return;
            default:
                return;
        }
    }

    public boolean isDraggingThumb() {
        return this.mIsDragging;
    }

    public boolean isThumbDetached() {
        return this.mIsThumbDetached;
    }

    public void reattachThumbToScroll() {
        this.mIsThumbDetached = false;
    }

    public void setDetachThumbOnFastScroll() {
        this.mCanThumbDetach = true;
    }

    public void setThumbOffset(int i, int i2) {
        if (this.mThumbOffset.x == i && this.mThumbOffset.y == i2) {
            return;
        }
        this.mInvalidateRect.set(this.mThumbOffset.x - this.mThumbCurvature, this.mThumbOffset.y, this.mThumbOffset.x + this.mThumbWidth, this.mThumbOffset.y + this.mThumbHeight);
        this.mThumbOffset.set(i, i2);
        updateThumbPath();
        this.mInvalidateRect.union(this.mThumbOffset.x - this.mThumbCurvature, this.mThumbOffset.y, this.mThumbOffset.x + this.mThumbWidth, this.mThumbOffset.y + this.mThumbHeight);
        this.mRv.invalidate(this.mInvalidateRect);
    }

    public void setThumbWidth(int i) {
        this.mInvalidateRect.set(this.mThumbOffset.x - this.mThumbCurvature, this.mThumbOffset.y, this.mThumbOffset.x + this.mThumbWidth, this.mThumbOffset.y + this.mThumbHeight);
        this.mThumbWidth = i;
        updateThumbPath();
        this.mInvalidateRect.union(this.mThumbOffset.x - this.mThumbCurvature, this.mThumbOffset.y, this.mThumbOffset.x + this.mThumbWidth, this.mThumbOffset.y + this.mThumbHeight);
        this.mRv.invalidate(this.mInvalidateRect);
    }

    public void setTrackWidth(int i) {
        this.mInvalidateRect.set(this.mThumbOffset.x - this.mThumbCurvature, 0, this.mThumbOffset.x + this.mThumbWidth, this.mRv.getHeight());
        this.mTrackWidth = i;
        updateThumbPath();
        this.mInvalidateRect.union(this.mThumbOffset.x - this.mThumbCurvature, 0, this.mThumbOffset.x + this.mThumbWidth, this.mRv.getHeight());
        this.mRv.invalidate(this.mInvalidateRect);
    }
}

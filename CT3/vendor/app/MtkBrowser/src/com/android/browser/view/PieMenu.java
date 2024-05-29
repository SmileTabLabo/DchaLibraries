package com.android.browser.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import java.util.ArrayList;
import java.util.List;
/* loaded from: b.zip:com/android/browser/view/PieMenu.class */
public class PieMenu extends FrameLayout {
    private boolean mAnimating;
    private Drawable mBackground;
    private Point mCenter;
    private PieController mController;
    private int[] mCounts;
    private PieItem mCurrentItem;
    private List<PieItem> mCurrentItems;
    private List<PieItem> mItems;
    private int mLevels;
    private Paint mNormalPaint;
    private boolean mOpen;
    private PieItem mOpenItem;
    private Path mPath;
    private PieView mPieView;
    private int mRadius;
    private int mRadiusInc;
    private Paint mSelectedPaint;
    private int mSlop;
    private Paint mSubPaint;
    private int mTouchOffset;
    private boolean mUseBackground;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.android.browser.view.PieMenu$4  reason: invalid class name */
    /* loaded from: b.zip:com/android/browser/view/PieMenu$4.class */
    public class AnonymousClass4 extends AnimatorListenerAdapter {
        final PieMenu this$0;
        final PieItem val$item;

        AnonymousClass4(PieMenu pieMenu, PieItem pieItem) {
            this.this$0 = pieMenu;
            this.val$item = pieItem;
        }

        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animator) {
            for (PieItem pieItem : this.this$0.mCurrentItems) {
                pieItem.setAnimationAngle(0.0f);
            }
            this.this$0.mCurrentItems = new ArrayList(this.this$0.mItems.size());
            int i = 0;
            for (int i2 = 0; i2 < this.this$0.mItems.size(); i2++) {
                if (this.this$0.mItems.get(i2) == this.val$item) {
                    this.this$0.mCurrentItems.add(this.val$item);
                } else {
                    this.this$0.mCurrentItems.add(this.val$item.getItems().get(i));
                    i++;
                }
            }
            this.this$0.layoutPie();
            this.this$0.animateIn(this.val$item, new AnimatorListenerAdapter(this) { // from class: com.android.browser.view.PieMenu.4.1
                final AnonymousClass4 this$1;

                {
                    this.this$1 = this;
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator2) {
                    for (PieItem pieItem2 : this.this$1.this$0.mCurrentItems) {
                        pieItem2.setAnimationAngle(0.0f);
                    }
                    this.this$1.this$0.mAnimating = false;
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.android.browser.view.PieMenu$5  reason: invalid class name */
    /* loaded from: b.zip:com/android/browser/view/PieMenu$5.class */
    public class AnonymousClass5 extends AnimatorListenerAdapter {
        final PieMenu this$0;

        AnonymousClass5(PieMenu pieMenu) {
            this.this$0 = pieMenu;
        }

        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animator) {
            for (PieItem pieItem : this.this$0.mCurrentItems) {
                pieItem.setAnimationAngle(0.0f);
            }
            this.this$0.mCurrentItems = this.this$0.mItems;
            this.this$0.mPieView = null;
            this.this$0.animateIn(this.this$0.mOpenItem, new AnimatorListenerAdapter(this) { // from class: com.android.browser.view.PieMenu.5.1
                final AnonymousClass5 this$1;

                {
                    this.this$1 = this;
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator2) {
                    for (PieItem pieItem2 : this.this$1.this$0.mCurrentItems) {
                        pieItem2.setAnimationAngle(0.0f);
                    }
                    this.this$1.this$0.mAnimating = false;
                    this.this$1.this$0.mOpenItem = null;
                    this.this$1.this$0.mCurrentItem = null;
                }
            });
        }
    }

    /* loaded from: b.zip:com/android/browser/view/PieMenu$PieController.class */
    public interface PieController {
        boolean onOpen();

        void stopEditingUrl();
    }

    /* loaded from: b.zip:com/android/browser/view/PieMenu$PieView.class */
    public interface PieView {

        /* loaded from: b.zip:com/android/browser/view/PieMenu$PieView$OnLayoutListener.class */
        public interface OnLayoutListener {
            void onLayout(int i, int i2, boolean z);
        }

        void draw(Canvas canvas);

        void layout(int i, int i2, boolean z, float f, int i3);

        boolean onTouchEvent(MotionEvent motionEvent);
    }

    public PieMenu(Context context) {
        super(context);
        this.mPieView = null;
        init(context);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void animateIn(PieItem pieItem, Animator.AnimatorListener animatorListener) {
        if (this.mCurrentItems == null || pieItem == null) {
            return;
        }
        float startAngle = pieItem.getStartAngle();
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this, pieItem, startAngle) { // from class: com.android.browser.view.PieMenu.3
            final PieMenu this$0;
            final PieItem val$fixed;
            final float val$target;

            {
                this.this$0 = this;
                this.val$fixed = pieItem;
                this.val$target = startAngle;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                for (PieItem pieItem2 : this.this$0.mCurrentItems) {
                    if (pieItem2 != this.val$fixed) {
                        pieItem2.setAnimationAngle((1.0f - valueAnimator.getAnimatedFraction()) * (this.val$target - pieItem2.getStart()));
                    }
                }
                this.this$0.invalidate();
            }
        });
        ofFloat.setDuration(80L);
        ofFloat.addListener(animatorListener);
        ofFloat.start();
    }

    private void animateOpen() {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this) { // from class: com.android.browser.view.PieMenu.1
            final PieMenu this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                for (PieItem pieItem : this.this$0.mCurrentItems) {
                    pieItem.setAnimationAngle((1.0f - valueAnimator.getAnimatedFraction()) * (-pieItem.getStart()));
                }
                this.this$0.invalidate();
            }
        });
        ofFloat.setDuration(160L);
        ofFloat.start();
    }

    private void animateOut(PieItem pieItem, Animator.AnimatorListener animatorListener) {
        if (this.mCurrentItems == null || pieItem == null) {
            return;
        }
        float startAngle = pieItem.getStartAngle();
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this, pieItem, startAngle) { // from class: com.android.browser.view.PieMenu.2
            final PieMenu this$0;
            final PieItem val$fixed;
            final float val$target;

            {
                this.this$0 = this;
                this.val$fixed = pieItem;
                this.val$target = startAngle;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                for (PieItem pieItem2 : this.this$0.mCurrentItems) {
                    if (pieItem2 != this.val$fixed) {
                        pieItem2.setAnimationAngle(valueAnimator.getAnimatedFraction() * (this.val$target - pieItem2.getStart()));
                    }
                }
                this.this$0.invalidate();
            }
        });
        ofFloat.setDuration(80L);
        ofFloat.addListener(animatorListener);
        ofFloat.start();
    }

    private void closeSub() {
        this.mAnimating = true;
        if (this.mCurrentItem != null) {
            this.mCurrentItem.setSelected(false);
        }
        animateOut(this.mOpenItem, new AnonymousClass5(this));
    }

    private void deselect() {
        if (this.mCurrentItem != null) {
            this.mCurrentItem.setSelected(false);
        }
        if (this.mOpenItem != null) {
            this.mOpenItem = null;
            this.mCurrentItems = this.mItems;
        }
        this.mCurrentItem = null;
        this.mPieView = null;
    }

    private void drawItem(Canvas canvas, PieItem pieItem) {
        if (pieItem.getView() != null) {
            Paint paint = pieItem.isSelected() ? this.mSelectedPaint : this.mNormalPaint;
            if (!this.mItems.contains(pieItem)) {
                paint = pieItem.isSelected() ? this.mSelectedPaint : this.mSubPaint;
            }
            int save = canvas.save();
            if (onTheLeft()) {
                canvas.scale(-1.0f, 1.0f);
            }
            canvas.rotate(getDegrees(pieItem.getStartAngle()) - 270.0f, this.mCenter.x, this.mCenter.y);
            canvas.drawPath(this.mPath, paint);
            canvas.restoreToCount(save);
            View view = pieItem.getView();
            int save2 = canvas.save();
            canvas.translate(view.getX(), view.getY());
            view.draw(canvas);
            canvas.restoreToCount(save2);
        }
    }

    private PieItem findItem(PointF pointF) {
        for (PieItem pieItem : this.mCurrentItems) {
            if (inside(pointF, this.mTouchOffset, pieItem)) {
                return pieItem;
            }
        }
        return null;
    }

    private float getDegrees(double d) {
        return (float) (270.0d - ((180.0d * d) / 3.141592653589793d));
    }

    private PointF getPolar(float f, float f2) {
        PointF pointF = new PointF();
        pointF.x = 1.5707964f;
        float f3 = this.mCenter.x - f;
        float f4 = f3;
        if (this.mCenter.x < this.mSlop) {
            f4 = -f3;
        }
        float f5 = this.mCenter.y - f2;
        pointF.y = (float) Math.sqrt((f4 * f4) + (f5 * f5));
        if (f5 > 0.0f) {
            pointF.x = (float) Math.asin(f4 / pointF.y);
        } else if (f5 < 0.0f) {
            pointF.x = (float) (3.141592653589793d - Math.asin(f4 / pointF.y));
        }
        return pointF;
    }

    private void init(Context context) {
        this.mItems = new ArrayList();
        this.mLevels = 0;
        this.mCounts = new int[5];
        Resources resources = context.getResources();
        this.mRadius = (int) resources.getDimension(2131427343);
        this.mRadiusInc = (int) resources.getDimension(2131427344);
        this.mSlop = (int) resources.getDimension(2131427345);
        this.mTouchOffset = (int) resources.getDimension(2131427346);
        this.mOpen = false;
        setWillNotDraw(false);
        setDrawingCacheEnabled(false);
        this.mCenter = new Point(0, 0);
        this.mBackground = resources.getDrawable(2130837603);
        this.mNormalPaint = new Paint();
        this.mNormalPaint.setAntiAlias(true);
        this.mSelectedPaint = new Paint();
        this.mSelectedPaint.setColor(resources.getColor(2131361804));
        this.mSelectedPaint.setAntiAlias(true);
        this.mSubPaint = new Paint();
        this.mSubPaint.setAntiAlias(true);
        this.mSubPaint.setColor(resources.getColor(2131361805));
    }

    private boolean inside(PointF pointF, float f, PieItem pieItem) {
        boolean z = false;
        if (pieItem.getInnerRadius() - f < pointF.y) {
            z = false;
            if (pieItem.getOuterRadius() - f > pointF.y) {
                z = false;
                if (pieItem.getStartAngle() < pointF.x) {
                    z = false;
                    if (pieItem.getStartAngle() + pieItem.getSweep() > pointF.x) {
                        z = true;
                    }
                }
            }
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void layoutPie() {
        int i = this.mRadius + 2;
        int i2 = (this.mRadius + this.mRadiusInc) - 2;
        for (int i3 = 0; i3 < this.mLevels; i3++) {
            int i4 = i3 + 1;
            float f = ((float) (3.141592653589793d - 0.3926991f)) / this.mCounts[i4];
            float f2 = 0.19634955f + (f / 2.0f);
            this.mPath = makeSlice(getDegrees(0.0d) - 1.0f, 1.0f + getDegrees(f), i2, i, this.mCenter);
            for (PieItem pieItem : this.mCurrentItems) {
                if (pieItem.getLevel() == i4) {
                    View view = pieItem.getView();
                    if (view != null) {
                        view.measure(view.getLayoutParams().width, view.getLayoutParams().height);
                        int measuredWidth = view.getMeasuredWidth();
                        int measuredHeight = view.getMeasuredHeight();
                        int i5 = i + (((i2 - i) * 2) / 3);
                        int sin = (int) (i5 * Math.sin(f2));
                        int cos = (this.mCenter.y - ((int) (i5 * Math.cos(f2)))) - (measuredHeight / 2);
                        int i6 = onTheLeft() ? (this.mCenter.x + sin) - (measuredWidth / 2) : (this.mCenter.x - sin) - (measuredWidth / 2);
                        view.layout(i6, cos, i6 + measuredWidth, cos + measuredHeight);
                    }
                    pieItem.setGeometry(f2 - (f / 2.0f), f, i, i2);
                    f2 += f;
                }
            }
            i += this.mRadiusInc;
            i2 += this.mRadiusInc;
        }
    }

    private void layoutPieView(PieView pieView, int i, int i2, float f) {
        pieView.layout(i, i2, onTheLeft(), f, getHeight());
    }

    private Path makeSlice(float f, float f2, int i, int i2, Point point) {
        RectF rectF = new RectF(point.x - i, point.y - i, point.x + i, point.y + i);
        RectF rectF2 = new RectF(point.x - i2, point.y - i2, point.x + i2, point.y + i2);
        Path path = new Path();
        path.arcTo(rectF, f, f2 - f, true);
        path.arcTo(rectF2, f2, f - f2);
        path.close();
        return path;
    }

    private void onEnter(PieItem pieItem) {
        if (this.mCurrentItem != null) {
            this.mCurrentItem.setSelected(false);
        }
        if (pieItem == null) {
            this.mCurrentItem = null;
            return;
        }
        playSoundEffect(0);
        pieItem.setSelected(true);
        this.mPieView = null;
        this.mCurrentItem = pieItem;
        if (this.mCurrentItem == this.mOpenItem || !this.mCurrentItem.hasItems()) {
            return;
        }
        openSub(this.mCurrentItem);
        this.mOpenItem = pieItem;
    }

    private boolean onTheLeft() {
        return this.mCenter.x < this.mSlop;
    }

    private void openSub(PieItem pieItem) {
        this.mAnimating = true;
        animateOut(pieItem, new AnonymousClass4(this, pieItem));
    }

    private void setCenter(int i, int i2) {
        if (i < this.mSlop) {
            this.mCenter.x = 0;
        } else {
            this.mCenter.x = getWidth();
        }
        this.mCenter.y = i2;
    }

    private void show(boolean z) {
        this.mOpen = z;
        if (this.mOpen) {
            this.mAnimating = false;
            this.mCurrentItem = null;
            this.mOpenItem = null;
            this.mPieView = null;
            this.mController.stopEditingUrl();
            this.mCurrentItems = this.mItems;
            for (PieItem pieItem : this.mCurrentItems) {
                pieItem.setSelected(false);
            }
            if (this.mController != null) {
                this.mController.onOpen();
            }
            layoutPie();
            animateOpen();
        }
        invalidate();
    }

    public void addItem(PieItem pieItem) {
        this.mItems.add(pieItem);
        int level = pieItem.getLevel();
        this.mLevels = Math.max(this.mLevels, level);
        int[] iArr = this.mCounts;
        iArr[level] = iArr[level] + 1;
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        if (this.mOpen) {
            if (this.mUseBackground) {
                int intrinsicWidth = this.mBackground.getIntrinsicWidth();
                int intrinsicHeight = this.mBackground.getIntrinsicHeight();
                int i = this.mCenter.x - intrinsicWidth;
                int i2 = this.mCenter.y - (intrinsicHeight / 2);
                this.mBackground.setBounds(i, i2, i + intrinsicWidth, i2 + intrinsicHeight);
                int save = canvas.save();
                if (onTheLeft()) {
                    canvas.scale(-1.0f, 1.0f);
                }
                this.mBackground.draw(canvas);
                canvas.restoreToCount(save);
            }
            PieItem pieItem = this.mCurrentItem;
            if (this.mOpenItem != null) {
                pieItem = this.mOpenItem;
            }
            for (PieItem pieItem2 : this.mCurrentItems) {
                if (pieItem2 != pieItem) {
                    drawItem(canvas, pieItem2);
                }
            }
            if (pieItem != null) {
                drawItem(canvas, pieItem);
            }
            if (this.mPieView != null) {
                this.mPieView.draw(canvas);
            }
        }
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        float x = motionEvent.getX();
        float y = motionEvent.getY();
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 0) {
            if (x > getWidth() - this.mSlop || x < this.mSlop) {
                setCenter((int) x, (int) y);
                show(true);
                return true;
            }
            return false;
        } else if (1 == actionMasked) {
            if (this.mOpen) {
                boolean z = false;
                if (this.mPieView != null) {
                    z = this.mPieView.onTouchEvent(motionEvent);
                }
                PieItem pieItem = this.mCurrentItem;
                if (!this.mAnimating) {
                    deselect();
                }
                show(false);
                if (z || pieItem == null || pieItem.getView() == null) {
                    return true;
                }
                if (pieItem == this.mOpenItem || !this.mAnimating) {
                    pieItem.getView().performClick();
                    return true;
                }
                return true;
            }
            return false;
        } else if (3 == actionMasked) {
            if (this.mOpen) {
                show(false);
            }
            if (this.mAnimating) {
                return false;
            }
            deselect();
            invalidate();
            return false;
        } else if (2 != actionMasked || this.mAnimating) {
            return false;
        } else {
            boolean z2 = false;
            PointF polar = getPolar(x, y);
            int i = this.mRadius;
            int i2 = this.mLevels;
            int i3 = this.mRadiusInc;
            if (this.mPieView != null) {
                z2 = this.mPieView.onTouchEvent(motionEvent);
            }
            if (z2) {
                invalidate();
                return false;
            } else if (polar.y < this.mRadius) {
                if (this.mOpenItem != null) {
                    closeSub();
                    return false;
                } else if (this.mAnimating) {
                    return false;
                } else {
                    deselect();
                    invalidate();
                    return false;
                }
            } else if (polar.y > i + (i2 * i3) + 50) {
                deselect();
                show(false);
                motionEvent.setAction(0);
                if (getParent() != null) {
                    ((ViewGroup) getParent()).dispatchTouchEvent(motionEvent);
                    return false;
                }
                return false;
            } else {
                PieItem findItem = findItem(polar);
                if (findItem == null || this.mCurrentItem == findItem) {
                    return false;
                }
                onEnter(findItem);
                if (findItem != null && findItem.isPieView() && findItem.getView() != null) {
                    int left = findItem.getView().getLeft();
                    int width = onTheLeft() ? findItem.getView().getWidth() : 0;
                    int top = findItem.getView().getTop();
                    this.mPieView = findItem.getPieView();
                    layoutPieView(this.mPieView, left + width, top, (findItem.getStartAngle() + findItem.getSweep()) / 2.0f);
                }
                invalidate();
                return false;
            }
        }
    }

    public void setController(PieController pieController) {
        this.mController = pieController;
    }
}

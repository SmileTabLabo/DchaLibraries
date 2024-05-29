package com.android.systemui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import java.util.HashSet;
import java.util.Set;
/* loaded from: a.zip:com/android/systemui/DessertCaseView.class */
public class DessertCaseView extends FrameLayout {
    float[] hsv;
    private int mCellSize;
    private View[] mCells;
    private int mColumns;
    private SparseArray<Drawable> mDrawables;
    private final Set<Point> mFreeList;
    private final Handler mHandler;
    private int mHeight;
    private final Runnable mJuggle;
    private int mRows;
    private boolean mStarted;
    private int mWidth;
    private final HashSet<View> tmpSet;
    private static final String TAG = DessertCaseView.class.getSimpleName();
    private static final int[] PASTRIES = {2130837607, 2130837593};
    private static final int[] RARE_PASTRIES = {2130837594, 2130837596, 2130837598, 2130837600, 2130837601, 2130837602, 2130837603, 2130837605};
    private static final int[] XRARE_PASTRIES = {2130837608, 2130837597, 2130837599, 2130837606};
    private static final int[] XXRARE_PASTRIES = {2130837609, 2130837595, 2130837604};
    private static final int NUM_PASTRIES = ((PASTRIES.length + RARE_PASTRIES.length) + XRARE_PASTRIES.length) + XXRARE_PASTRIES.length;
    private static final float[] MASK = {0.0f, 0.0f, 0.0f, 0.0f, 255.0f, 0.0f, 0.0f, 0.0f, 0.0f, 255.0f, 0.0f, 0.0f, 0.0f, 0.0f, 255.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f};
    private static final float[] ALPHA_MASK = {0.0f, 0.0f, 0.0f, 0.0f, 255.0f, 0.0f, 0.0f, 0.0f, 0.0f, 255.0f, 0.0f, 0.0f, 0.0f, 0.0f, 255.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f};
    private static final float[] WHITE_MASK = {0.0f, 0.0f, 0.0f, 0.0f, 255.0f, 0.0f, 0.0f, 0.0f, 0.0f, 255.0f, 0.0f, 0.0f, 0.0f, 0.0f, 255.0f, -1.0f, 0.0f, 0.0f, 0.0f, 255.0f};

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.android.systemui.DessertCaseView$2  reason: invalid class name */
    /* loaded from: a.zip:com/android/systemui/DessertCaseView$2.class */
    public class AnonymousClass2 implements View.OnClickListener {
        final DessertCaseView this$0;
        final ImageView val$v;

        AnonymousClass2(DessertCaseView dessertCaseView, ImageView imageView) {
            this.this$0 = dessertCaseView;
            this.val$v = imageView;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            this.this$0.place(this.val$v, true);
            this.this$0.postDelayed(new Runnable(this) { // from class: com.android.systemui.DessertCaseView.2.1
                final AnonymousClass2 this$1;

                {
                    this.this$1 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$1.this$0.fillFreeList();
                }
            }, 250L);
        }
    }

    /* loaded from: a.zip:com/android/systemui/DessertCaseView$RescalingContainer.class */
    public static class RescalingContainer extends FrameLayout {
        private DessertCaseView mView;

        public RescalingContainer(Context context) {
            super(context);
            setSystemUiVisibility(5638);
        }

        @Override // android.widget.FrameLayout, android.view.ViewGroup, android.view.View
        protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
            float f = i3 - i;
            float f2 = i4 - i2;
            int i5 = (int) ((f / 0.25f) / 2.0f);
            int i6 = (int) ((f2 / 0.25f) / 2.0f);
            int i7 = (int) (i + (0.5f * f));
            int i8 = (int) (i2 + (0.5f * f2));
            this.mView.layout(i7 - i5, i8 - i6, i7 + i5, i8 + i6);
        }

        public void setView(DessertCaseView dessertCaseView) {
            addView(dessertCaseView);
            this.mView = dessertCaseView;
        }
    }

    public DessertCaseView(Context context) {
        this(context, null);
    }

    public DessertCaseView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    /* JADX WARN: Multi-variable type inference failed */
    public DessertCaseView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        int[] iArr;
        this.mDrawables = new SparseArray<>(NUM_PASTRIES);
        this.mFreeList = new HashSet();
        this.mHandler = new Handler();
        this.mJuggle = new Runnable(this) { // from class: com.android.systemui.DessertCaseView.1
            final DessertCaseView this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                int childCount = this.this$0.getChildCount();
                for (int i2 = 0; i2 < 1; i2++) {
                    this.this$0.place(this.this$0.getChildAt((int) (Math.random() * childCount)), true);
                }
                this.this$0.fillFreeList();
                if (this.this$0.mStarted) {
                    this.this$0.mHandler.postDelayed(this.this$0.mJuggle, 2000L);
                }
            }
        };
        this.hsv = new float[]{0.0f, 1.0f, 0.85f};
        this.tmpSet = new HashSet<>();
        Resources resources = getResources();
        this.mStarted = false;
        this.mCellSize = resources.getDimensionPixelSize(2131689869);
        BitmapFactory.Options options = new BitmapFactory.Options();
        if (this.mCellSize < 512) {
            options.inSampleSize = 2;
        }
        options.inMutable = true;
        Bitmap bitmap = null;
        for (Object[] objArr : new int[]{PASTRIES, RARE_PASTRIES, XRARE_PASTRIES, XXRARE_PASTRIES}) {
            for (char c : objArr) {
                options.inBitmap = bitmap;
                bitmap = BitmapFactory.decodeResource(resources, c, options);
                BitmapDrawable bitmapDrawable = new BitmapDrawable(resources, convertToAlphaMask(bitmap));
                bitmapDrawable.setColorFilter(new ColorMatrixColorFilter(ALPHA_MASK));
                bitmapDrawable.setBounds(0, 0, this.mCellSize, this.mCellSize);
                this.mDrawables.append(c, bitmapDrawable);
            }
        }
    }

    private static Bitmap convertToAlphaMask(Bitmap bitmap) {
        Bitmap createBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ALPHA_8);
        Canvas canvas = new Canvas(createBitmap);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(MASK));
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, paint);
        return createBitmap;
    }

    static float frand() {
        return (float) Math.random();
    }

    static float frand(float f, float f2) {
        return (frand() * (f2 - f)) + f;
    }

    private Point[] getOccupied(View view) {
        int intValue = ((Integer) view.getTag(33554434)).intValue();
        Point point = (Point) view.getTag(33554433);
        if (point == null || intValue == 0) {
            return new Point[0];
        }
        Point[] pointArr = new Point[intValue * intValue];
        int i = 0;
        for (int i2 = 0; i2 < intValue; i2++) {
            int i3 = 0;
            while (i3 < intValue) {
                pointArr[i] = new Point(point.x + i2, point.y + i3);
                i3++;
                i++;
            }
        }
        return pointArr;
    }

    static int irand(int i, int i2) {
        return (int) frand(i, i2);
    }

    private final Animator.AnimatorListener makeHardwareLayerListener(View view) {
        return new AnimatorListenerAdapter(this, view) { // from class: com.android.systemui.DessertCaseView.3
            final DessertCaseView this$0;
            final View val$v;

            {
                this.this$0 = this;
                this.val$v = view;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (this.val$v.isAttachedToWindow()) {
                    this.val$v.setLayerType(0, null);
                }
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                if (this.val$v.isAttachedToWindow()) {
                    this.val$v.setLayerType(2, null);
                    this.val$v.buildLayer();
                }
            }
        };
    }

    public void fillFreeList() {
        fillFreeList(500);
    }

    public void fillFreeList(int i) {
        synchronized (this) {
            Context context = getContext();
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(this.mCellSize, this.mCellSize);
            while (!this.mFreeList.isEmpty()) {
                Point next = this.mFreeList.iterator().next();
                this.mFreeList.remove(next);
                if (this.mCells[(this.mColumns * next.y) + next.x] == null) {
                    ImageView imageView = new ImageView(context);
                    imageView.setOnClickListener(new AnonymousClass2(this, imageView));
                    imageView.setBackgroundColor(random_color());
                    float frand = frand();
                    Drawable drawable = frand < 5.0E-4f ? this.mDrawables.get(pick(XXRARE_PASTRIES)) : frand < 0.005f ? this.mDrawables.get(pick(XRARE_PASTRIES)) : frand < 0.5f ? this.mDrawables.get(pick(RARE_PASTRIES)) : frand < 0.7f ? this.mDrawables.get(pick(PASTRIES)) : null;
                    if (drawable != null) {
                        imageView.getOverlay().add(drawable);
                    }
                    int i2 = this.mCellSize;
                    layoutParams.height = i2;
                    layoutParams.width = i2;
                    addView(imageView, layoutParams);
                    place(imageView, next, false);
                    if (i > 0) {
                        float intValue = ((Integer) imageView.getTag(33554434)).intValue();
                        imageView.setScaleX(0.5f * intValue);
                        imageView.setScaleY(0.5f * intValue);
                        imageView.setAlpha(0.0f);
                        imageView.animate().withLayer().scaleX(intValue).scaleY(intValue).alpha(1.0f).setDuration(i);
                    }
                }
            }
        }
    }

    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override // android.view.View
    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        synchronized (this) {
            super.onSizeChanged(i, i2, i3, i4);
            if (this.mWidth == i && this.mHeight == i2) {
                return;
            }
            boolean z = this.mStarted;
            if (z) {
                stop();
            }
            this.mWidth = i;
            this.mHeight = i2;
            this.mCells = null;
            removeAllViewsInLayout();
            this.mFreeList.clear();
            this.mRows = this.mHeight / this.mCellSize;
            this.mColumns = this.mWidth / this.mCellSize;
            this.mCells = new View[this.mRows * this.mColumns];
            setScaleX(0.25f);
            setScaleY(0.25f);
            setTranslationX((this.mWidth - (this.mCellSize * this.mColumns)) * 0.5f * 0.25f);
            setTranslationY((this.mHeight - (this.mCellSize * this.mRows)) * 0.5f * 0.25f);
            for (int i5 = 0; i5 < this.mRows; i5++) {
                for (int i6 = 0; i6 < this.mColumns; i6++) {
                    this.mFreeList.add(new Point(i6, i5));
                }
            }
            if (z) {
                start();
            }
        }
    }

    int pick(int[] iArr) {
        return iArr[(int) (Math.random() * iArr.length)];
    }

    public void place(View view, Point point, boolean z) {
        int i;
        Point[] occupied;
        Point[] occupied2;
        synchronized (this) {
            int i2 = point.x;
            int i3 = point.y;
            float frand = frand();
            if (view.getTag(33554433) != null) {
                for (Point point2 : getOccupied(view)) {
                    this.mFreeList.add(point2);
                    this.mCells[(point2.y * this.mColumns) + point2.x] = null;
                }
            }
            if (frand < 0.01f) {
                i = 1;
                if (i2 < this.mColumns - 3) {
                    i = 1;
                    if (i3 < this.mRows - 3) {
                        i = 4;
                    }
                }
            } else if (frand < 0.1f) {
                i = 1;
                if (i2 < this.mColumns - 2) {
                    i = 1;
                    if (i3 < this.mRows - 2) {
                        i = 3;
                    }
                }
            } else {
                i = 1;
                if (frand < 0.33f) {
                    i = 1;
                    if (i2 != this.mColumns - 1) {
                        i = 1;
                        if (i3 != this.mRows - 1) {
                            i = 2;
                        }
                    }
                }
            }
            view.setTag(33554433, point);
            view.setTag(33554434, Integer.valueOf(i));
            this.tmpSet.clear();
            Point[] occupied3 = getOccupied(view);
            for (Point point3 : occupied3) {
                View view2 = this.mCells[(point3.y * this.mColumns) + point3.x];
                if (view2 != null) {
                    this.tmpSet.add(view2);
                }
            }
            for (View view3 : this.tmpSet) {
                for (Point point4 : getOccupied(view3)) {
                    this.mFreeList.add(point4);
                    this.mCells[(point4.y * this.mColumns) + point4.x] = null;
                }
                if (view3 != view) {
                    view3.setTag(33554433, null);
                    if (z) {
                        view3.animate().withLayer().scaleX(0.5f).scaleY(0.5f).alpha(0.0f).setDuration(500L).setInterpolator(new AccelerateInterpolator()).setListener(new Animator.AnimatorListener(this, view3) { // from class: com.android.systemui.DessertCaseView.4
                            final DessertCaseView this$0;
                            final View val$squatter;

                            {
                                this.this$0 = this;
                                this.val$squatter = view3;
                            }

                            @Override // android.animation.Animator.AnimatorListener
                            public void onAnimationCancel(Animator animator) {
                            }

                            @Override // android.animation.Animator.AnimatorListener
                            public void onAnimationEnd(Animator animator) {
                                this.this$0.removeView(this.val$squatter);
                            }

                            @Override // android.animation.Animator.AnimatorListener
                            public void onAnimationRepeat(Animator animator) {
                            }

                            @Override // android.animation.Animator.AnimatorListener
                            public void onAnimationStart(Animator animator) {
                            }
                        }).start();
                    } else {
                        removeView(view3);
                    }
                }
            }
            for (Point point5 : occupied3) {
                this.mCells[(point5.y * this.mColumns) + point5.x] = view;
                this.mFreeList.remove(point5);
            }
            float irand = irand(0, 4) * 90.0f;
            if (z) {
                view.bringToFront();
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playTogether(ObjectAnimator.ofFloat(view, View.SCALE_X, i), ObjectAnimator.ofFloat(view, View.SCALE_Y, i));
                animatorSet.setInterpolator(new AnticipateOvershootInterpolator());
                animatorSet.setDuration(500L);
                AnimatorSet animatorSet2 = new AnimatorSet();
                animatorSet2.playTogether(ObjectAnimator.ofFloat(view, View.ROTATION, irand), ObjectAnimator.ofFloat(view, View.X, (this.mCellSize * i2) + (((i - 1) * this.mCellSize) / 2)), ObjectAnimator.ofFloat(view, View.Y, (this.mCellSize * i3) + (((i - 1) * this.mCellSize) / 2)));
                animatorSet2.setInterpolator(new DecelerateInterpolator());
                animatorSet2.setDuration(500L);
                animatorSet.addListener(makeHardwareLayerListener(view));
                animatorSet.start();
                animatorSet2.start();
            } else {
                view.setX((this.mCellSize * i2) + (((i - 1) * this.mCellSize) / 2));
                view.setY((this.mCellSize * i3) + (((i - 1) * this.mCellSize) / 2));
                view.setScaleX(i);
                view.setScaleY(i);
                view.setRotation(irand);
            }
        }
    }

    public void place(View view, boolean z) {
        place(view, new Point(irand(0, this.mColumns), irand(0, this.mRows)), z);
    }

    int random_color() {
        this.hsv[0] = irand(0, 12) * 30.0f;
        return Color.HSVToColor(this.hsv);
    }

    public void start() {
        if (!this.mStarted) {
            this.mStarted = true;
            fillFreeList(2000);
        }
        this.mHandler.postDelayed(this.mJuggle, 5000L);
    }

    public void stop() {
        this.mStarted = false;
        this.mHandler.removeCallbacks(this.mJuggle);
    }
}

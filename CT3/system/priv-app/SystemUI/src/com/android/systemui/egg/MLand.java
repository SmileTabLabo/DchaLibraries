package com.android.systemui.egg;

import android.animation.LayoutTransition;
import android.animation.TimeAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.internal.logging.MetricsLogger;
import java.util.ArrayList;
/* loaded from: a.zip:com/android/systemui/egg/MLand.class */
public class MLand extends FrameLayout {
    private static Params PARAMS;
    private float dt;
    private TimeAnimator mAnim;
    private boolean mAnimating;
    private final AudioAttributes mAudioAttrs;
    private AudioManager mAudioManager;
    private int mCountdown;
    private int mCurrentPipeId;
    private boolean mFlipped;
    private boolean mFrozen;
    private ArrayList<Integer> mGameControllers;
    private int mHeight;
    private float mLastPipeTime;
    private ArrayList<Obstacle> mObstaclesInPlay;
    private Paint mPlayerTracePaint;
    private ArrayList<Player> mPlayers;
    private boolean mPlaying;
    private int mScene;
    private ViewGroup mScoreFields;
    private View mSplash;
    private int mTaps;
    private int mTimeOfDay;
    private Paint mTouchPaint;
    private Vibrator mVibrator;
    private int mWidth;
    private float t;
    public static final boolean DEBUG = Log.isLoggable("MLand", 3);
    public static final boolean DEBUG_IDDQD = Log.isLoggable("MLand.iddqd", 3);
    private static final int[][] SKIES = {new int[]{-4144897, -6250241}, new int[]{-16777200, -16777216}, new int[]{-16777152, -16777200}, new int[]{-6258656, -14663552}};
    private static float dp = 1.0f;
    static final float[] hsv = {0.0f, 0.0f, 0.0f};
    static final Rect sTmpRect = new Rect();
    static final int[] ANTENNAE = {2130837912, 2130837913};
    static final int[] EYES = {2130837914, 2130837915};
    static final int[] MOUTHS = {2130837917, 2130837918, 2130837919, 2130837920};
    static final int[] CACTI = {2130837585, 2130837586, 2130837587};
    static final int[] MOUNTAINS = {2130837922, 2130837923, 2130837924};

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/egg/MLand$Building.class */
    public class Building extends Scenery {
        final MLand this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public Building(MLand mLand, Context context) {
            super(mLand, context);
            this.this$0 = mLand;
            this.w = MLand.irand(MLand.PARAMS.BUILDING_WIDTH_MIN, MLand.PARAMS.BUILDING_WIDTH_MAX);
            this.h = 0;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/egg/MLand$Cactus.class */
    public class Cactus extends Building {
        final MLand this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public Cactus(MLand mLand, Context context) {
            super(mLand, context);
            this.this$0 = mLand;
            setBackgroundResource(MLand.pick(MLand.CACTI));
            int irand = MLand.irand(MLand.PARAMS.BUILDING_WIDTH_MAX / 4, MLand.PARAMS.BUILDING_WIDTH_MAX / 2);
            this.h = irand;
            this.w = irand;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/egg/MLand$Cloud.class */
    public class Cloud extends Scenery {
        final MLand this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public Cloud(MLand mLand, Context context) {
            super(mLand, context);
            this.this$0 = mLand;
            setBackgroundResource(MLand.frand() < 0.01f ? 2130837592 : 2130837591);
            getBackground().setAlpha(64);
            int irand = MLand.irand(MLand.PARAMS.CLOUD_SIZE_MIN, MLand.PARAMS.CLOUD_SIZE_MAX);
            this.h = irand;
            this.w = irand;
            this.z = 0.0f;
            this.v = MLand.frand(0.15f, 0.5f);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/egg/MLand$GameView.class */
    public interface GameView {
        void step(long j, long j2, float f, float f2);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/egg/MLand$Mountain.class */
    public class Mountain extends Building {
        final MLand this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public Mountain(MLand mLand, Context context) {
            super(mLand, context);
            this.this$0 = mLand;
            setBackgroundResource(MLand.pick(MLand.MOUNTAINS));
            int irand = MLand.irand(MLand.PARAMS.BUILDING_WIDTH_MAX / 2, MLand.PARAMS.BUILDING_WIDTH_MAX);
            this.h = irand;
            this.w = irand;
            this.z = 0.0f;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/egg/MLand$Obstacle.class */
    public class Obstacle extends View implements GameView {
        public float h;
        public final Rect hitRect;
        final MLand this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public Obstacle(MLand mLand, Context context, float f) {
            super(context);
            this.this$0 = mLand;
            this.hitRect = new Rect();
            setBackgroundColor(-65536);
            this.h = f;
        }

        public boolean cleared(Player player) {
            int length = player.corners.length / 2;
            for (int i = 0; i < length; i++) {
                if (this.hitRect.right >= ((int) player.corners[i * 2])) {
                    return false;
                }
            }
            return true;
        }

        public boolean intersects(Player player) {
            int length = player.corners.length / 2;
            for (int i = 0; i < length; i++) {
                if (this.hitRect.contains((int) player.corners[i * 2], (int) player.corners[(i * 2) + 1])) {
                    return true;
                }
            }
            return false;
        }

        @Override // com.android.systemui.egg.MLand.GameView
        public void step(long j, long j2, float f, float f2) {
            setTranslationX(getTranslationX() - (MLand.PARAMS.TRANSLATION_PER_SEC * f2));
            getHitRect(this.hitRect);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/egg/MLand$Params.class */
    public static class Params {
        public int BOOST_DV;
        public int BUILDING_HEIGHT_MIN;
        public int BUILDING_WIDTH_MAX;
        public int BUILDING_WIDTH_MIN;
        public int CLOUD_SIZE_MAX;
        public int CLOUD_SIZE_MIN;
        public int G;
        public float HUD_Z;
        public int MAX_V;
        public int OBSTACLE_GAP;
        public int OBSTACLE_MIN;
        public int OBSTACLE_PERIOD;
        public int OBSTACLE_SPACING;
        public int OBSTACLE_STEM_WIDTH;
        public int OBSTACLE_WIDTH;
        public float OBSTACLE_Z;
        public int PLAYER_HIT_SIZE;
        public int PLAYER_SIZE;
        public float PLAYER_Z;
        public float PLAYER_Z_BOOST;
        public float SCENERY_Z;
        public int STAR_SIZE_MAX;
        public int STAR_SIZE_MIN;
        public float TRANSLATION_PER_SEC;

        public Params(Resources resources) {
            this.TRANSLATION_PER_SEC = resources.getDimension(2131690075);
            this.OBSTACLE_SPACING = resources.getDimensionPixelSize(2131690074);
            this.OBSTACLE_PERIOD = (int) (this.OBSTACLE_SPACING / this.TRANSLATION_PER_SEC);
            this.BOOST_DV = resources.getDimensionPixelSize(2131690076);
            this.PLAYER_HIT_SIZE = resources.getDimensionPixelSize(2131690077);
            this.PLAYER_SIZE = resources.getDimensionPixelSize(2131690078);
            this.OBSTACLE_WIDTH = resources.getDimensionPixelSize(2131690079);
            this.OBSTACLE_STEM_WIDTH = resources.getDimensionPixelSize(2131690080);
            this.OBSTACLE_GAP = resources.getDimensionPixelSize(2131690081);
            this.OBSTACLE_MIN = resources.getDimensionPixelSize(2131690082);
            this.BUILDING_HEIGHT_MIN = resources.getDimensionPixelSize(2131690085);
            this.BUILDING_WIDTH_MIN = resources.getDimensionPixelSize(2131690083);
            this.BUILDING_WIDTH_MAX = resources.getDimensionPixelSize(2131690084);
            this.CLOUD_SIZE_MIN = resources.getDimensionPixelSize(2131690086);
            this.CLOUD_SIZE_MAX = resources.getDimensionPixelSize(2131690087);
            this.STAR_SIZE_MIN = resources.getDimensionPixelSize(2131690090);
            this.STAR_SIZE_MAX = resources.getDimensionPixelSize(2131690091);
            this.G = resources.getDimensionPixelSize(2131690092);
            this.MAX_V = resources.getDimensionPixelSize(2131690093);
            this.SCENERY_Z = resources.getDimensionPixelSize(2131690094);
            this.OBSTACLE_Z = resources.getDimensionPixelSize(2131690095);
            this.PLAYER_Z = resources.getDimensionPixelSize(2131690096);
            this.PLAYER_Z_BOOST = resources.getDimensionPixelSize(2131690097);
            this.HUD_Z = resources.getDimensionPixelSize(2131690098);
            if (this.OBSTACLE_MIN <= this.OBSTACLE_WIDTH / 2) {
                MLand.L("error: obstacles might be too short, adjusting", new Object[0]);
                this.OBSTACLE_MIN = (this.OBSTACLE_WIDTH / 2) + 1;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/egg/MLand$Player.class */
    public static class Player extends ImageView implements GameView {
        static int sNextColor = 0;
        public int color;
        public final float[] corners;
        public float dv;
        private boolean mAlive;
        private boolean mBoosting;
        private MLand mLand;
        private int mScore;
        private TextView mScoreField;
        private float mTouchX;
        private float mTouchY;
        private final int[] sColors;
        private final float[] sHull;

        public Player(Context context) {
            super(context);
            this.mTouchX = -1.0f;
            this.mTouchY = -1.0f;
            this.sColors = new int[]{-2407369, -12879641, -740352, -15753896, -8710016, -6381922};
            this.sHull = new float[]{0.3f, 0.0f, 0.7f, 0.0f, 0.92f, 0.33f, 0.92f, 0.75f, 0.6f, 1.0f, 0.4f, 1.0f, 0.08f, 0.75f, 0.08f, 0.33f};
            this.corners = new float[this.sHull.length];
            setBackgroundResource(2130837580);
            getBackground().setTintMode(PorterDuff.Mode.SRC_ATOP);
            int[] iArr = this.sColors;
            int i = sNextColor;
            sNextColor = i + 1;
            this.color = iArr[i % this.sColors.length];
            getBackground().setTint(this.color);
            setOutlineProvider(new ViewOutlineProvider(this) { // from class: com.android.systemui.egg.MLand.Player.1
                final Player this$1;

                {
                    this.this$1 = this;
                }

                @Override // android.view.ViewOutlineProvider
                public void getOutline(View view, Outline outline) {
                    int width = view.getWidth();
                    int height = view.getHeight();
                    int i2 = (int) (width * 0.3f);
                    int i3 = (int) (height * 0.2f);
                    outline.setRect(i2, i3, width - i2, height - i3);
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void addScore(int i) {
            setScore(this.mScore + i);
        }

        public static Player create(MLand mLand) {
            Player player = new Player(mLand.getContext());
            player.mLand = mLand;
            player.reset();
            player.setVisibility(4);
            mLand.addView(player, new FrameLayout.LayoutParams(MLand.PARAMS.PLAYER_SIZE, MLand.PARAMS.PLAYER_SIZE));
            return player;
        }

        private void setScore(int i) {
            this.mScore = i;
            if (this.mScoreField != null) {
                this.mScoreField.setText(MLand.DEBUG_IDDQD ? "??" : String.valueOf(i));
            }
        }

        public boolean below(int i) {
            int length = this.corners.length / 2;
            for (int i2 = 0; i2 < length; i2++) {
                if (((int) this.corners[(i2 * 2) + 1]) >= i) {
                    return true;
                }
            }
            return false;
        }

        public void boost() {
            this.mBoosting = true;
            this.dv = -MLand.PARAMS.BOOST_DV;
            animate().cancel();
            animate().scaleX(1.25f).scaleY(1.25f).translationZ(MLand.PARAMS.PLAYER_Z_BOOST).setDuration(100L);
            setScaleX(1.25f);
            setScaleY(1.25f);
        }

        public void boost(float f, float f2) {
            this.mTouchX = f;
            this.mTouchY = f2;
            boost();
        }

        public void die() {
            this.mAlive = false;
            if (this.mScoreField != null) {
            }
        }

        public int getScore() {
            return this.mScore;
        }

        public void prepareCheckIntersections() {
            int i = (MLand.PARAMS.PLAYER_SIZE - MLand.PARAMS.PLAYER_HIT_SIZE) / 2;
            int i2 = MLand.PARAMS.PLAYER_HIT_SIZE;
            int length = this.sHull.length / 2;
            for (int i3 = 0; i3 < length; i3++) {
                this.corners[i3 * 2] = (i2 * this.sHull[i3 * 2]) + i;
                this.corners[(i3 * 2) + 1] = (i2 * this.sHull[(i3 * 2) + 1]) + i;
            }
            getMatrix().mapPoints(this.corners);
        }

        public void reset() {
            setY(((this.mLand.mHeight / 2) + ((int) (Math.random() * MLand.PARAMS.PLAYER_SIZE))) - (MLand.PARAMS.PLAYER_SIZE / 2));
            setScore(0);
            setScoreField(this.mScoreField);
            this.mBoosting = false;
            this.dv = 0.0f;
        }

        public void setScoreField(TextView textView) {
            this.mScoreField = textView;
            if (textView != null) {
                setScore(this.mScore);
                this.mScoreField.getBackground().setColorFilter(this.color, PorterDuff.Mode.SRC_ATOP);
                this.mScoreField.setTextColor(MLand.luma(this.color) > 0.7f ? -16777216 : -1);
            }
        }

        public void start() {
            this.mAlive = true;
        }

        @Override // com.android.systemui.egg.MLand.GameView
        public void step(long j, long j2, float f, float f2) {
            if (!this.mAlive) {
                setTranslationX(getTranslationX() - (MLand.PARAMS.TRANSLATION_PER_SEC * f2));
                return;
            }
            if (this.mBoosting) {
                this.dv = -MLand.PARAMS.BOOST_DV;
            } else {
                this.dv += MLand.PARAMS.G;
            }
            if (this.dv < (-MLand.PARAMS.MAX_V)) {
                this.dv = -MLand.PARAMS.MAX_V;
            } else if (this.dv > MLand.PARAMS.MAX_V) {
                this.dv = MLand.PARAMS.MAX_V;
            }
            float translationY = getTranslationY() + (this.dv * f2);
            float f3 = translationY;
            if (translationY < 0.0f) {
                f3 = 0.0f;
            }
            setTranslationY(f3);
            setRotation(MLand.lerp(MLand.clamp(MLand.rlerp(this.dv, MLand.PARAMS.MAX_V, MLand.PARAMS.MAX_V * (-1))), 90.0f, -90.0f) + 90.0f);
            prepareCheckIntersections();
        }

        public void unboost() {
            this.mBoosting = false;
            this.mTouchY = -1.0f;
            this.mTouchX = -1.0f;
            animate().cancel();
            animate().scaleX(1.0f).scaleY(1.0f).translationZ(MLand.PARAMS.PLAYER_Z).setDuration(200L);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/egg/MLand$Pop.class */
    public class Pop extends Obstacle {
        Drawable antenna;
        int cx;
        int cy;
        Drawable eyes;
        int mRotate;
        Drawable mouth;
        int r;
        final MLand this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public Pop(MLand mLand, Context context, float f) {
            super(mLand, context, f);
            this.this$0 = mLand;
            setBackgroundResource(2130837916);
            this.antenna = context.getDrawable(MLand.pick(MLand.ANTENNAE));
            if (MLand.frand() > 0.5f) {
                this.eyes = context.getDrawable(MLand.pick(MLand.EYES));
                if (MLand.frand() > 0.8f) {
                    this.mouth = context.getDrawable(MLand.pick(MLand.MOUTHS));
                }
            }
            setOutlineProvider(new ViewOutlineProvider(this) { // from class: com.android.systemui.egg.MLand.Pop.1
                final Pop this$1;

                {
                    this.this$1 = this;
                }

                @Override // android.view.ViewOutlineProvider
                public void getOutline(View view, Outline outline) {
                    int width = (int) ((this.this$1.getWidth() * 1.0f) / 6.0f);
                    outline.setOval(width, width, this.this$1.getWidth() - width, this.this$1.getHeight() - width);
                }
            });
        }

        @Override // com.android.systemui.egg.MLand.Obstacle
        public boolean intersects(Player player) {
            int length = player.corners.length / 2;
            for (int i = 0; i < length; i++) {
                if (Math.hypot(((int) player.corners[i * 2]) - this.cx, ((int) player.corners[(i * 2) + 1]) - this.cy) <= this.r) {
                    return true;
                }
            }
            return false;
        }

        @Override // android.view.View
        public void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (this.antenna != null) {
                this.antenna.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                this.antenna.draw(canvas);
            }
            if (this.eyes != null) {
                this.eyes.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                this.eyes.draw(canvas);
            }
            if (this.mouth != null) {
                this.mouth.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                this.mouth.draw(canvas);
            }
        }

        @Override // com.android.systemui.egg.MLand.Obstacle, com.android.systemui.egg.MLand.GameView
        public void step(long j, long j2, float f, float f2) {
            super.step(j, j2, f, f2);
            if (this.mRotate != 0) {
                setRotation(getRotation() + (45.0f * f2 * this.mRotate));
            }
            this.cx = (this.hitRect.left + this.hitRect.right) / 2;
            this.cy = (this.hitRect.top + this.hitRect.bottom) / 2;
            this.r = getWidth() / 3;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/egg/MLand$Scenery.class */
    public class Scenery extends FrameLayout implements GameView {
        public int h;
        final MLand this$0;
        public float v;
        public int w;
        public float z;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public Scenery(MLand mLand, Context context) {
            super(context);
            this.this$0 = mLand;
        }

        @Override // com.android.systemui.egg.MLand.GameView
        public void step(long j, long j2, float f, float f2) {
            setTranslationX(getTranslationX() - ((MLand.PARAMS.TRANSLATION_PER_SEC * f2) * this.v));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/egg/MLand$Star.class */
    public class Star extends Scenery {
        final MLand this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public Star(MLand mLand, Context context) {
            super(mLand, context);
            this.this$0 = mLand;
            setBackgroundResource(2130838248);
            int irand = MLand.irand(MLand.PARAMS.STAR_SIZE_MIN, MLand.PARAMS.STAR_SIZE_MAX);
            this.h = irand;
            this.w = irand;
            this.z = 0.0f;
            this.v = 0.0f;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/egg/MLand$Stem.class */
    public class Stem extends Obstacle {
        int id;
        boolean mDrawShadow;
        GradientDrawable mGradient;
        Path mJandystripe;
        Paint mPaint;
        Paint mPaint2;
        Path mShadow;
        final MLand this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public Stem(MLand mLand, Context context, float f, boolean z) {
            super(mLand, context, f);
            this.this$0 = mLand;
            this.mPaint = new Paint();
            this.mShadow = new Path();
            this.mGradient = new GradientDrawable();
            this.id = mLand.mCurrentPipeId;
            this.mDrawShadow = z;
            setBackground(null);
            this.mGradient.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
            this.mPaint.setColor(-16777216);
            this.mPaint.setColorFilter(new PorterDuffColorFilter(570425344, PorterDuff.Mode.MULTIPLY));
            if (MLand.frand() >= 0.01f) {
                this.mGradient.setColors(new int[]{-4412764, -6190977});
                return;
            }
            this.mGradient.setColors(new int[]{-1, -2236963});
            this.mJandystripe = new Path();
            this.mPaint2 = new Paint();
            this.mPaint2.setColor(-65536);
            this.mPaint2.setColorFilter(new PorterDuffColorFilter(-65536, PorterDuff.Mode.MULTIPLY));
        }

        @Override // android.view.View
        public void onAttachedToWindow() {
            super.onAttachedToWindow();
            setWillNotDraw(false);
            setOutlineProvider(new ViewOutlineProvider(this) { // from class: com.android.systemui.egg.MLand.Stem.1
                final Stem this$1;

                {
                    this.this$1 = this;
                }

                @Override // android.view.ViewOutlineProvider
                public void getOutline(View view, Outline outline) {
                    outline.setRect(0, 0, this.this$1.getWidth(), this.this$1.getHeight());
                }
            });
        }

        @Override // android.view.View
        public void onDraw(Canvas canvas) {
            int width = canvas.getWidth();
            int height = canvas.getHeight();
            this.mGradient.setGradientCenter(width * 0.75f, 0.0f);
            this.mGradient.setBounds(0, 0, width, height);
            this.mGradient.draw(canvas);
            if (this.mJandystripe != null) {
                this.mJandystripe.reset();
                this.mJandystripe.moveTo(0.0f, width);
                this.mJandystripe.lineTo(width, 0.0f);
                this.mJandystripe.lineTo(width, width * 2);
                this.mJandystripe.lineTo(0.0f, width * 3);
                this.mJandystripe.close();
                int i = 0;
                while (true) {
                    int i2 = i;
                    if (i2 >= height) {
                        break;
                    }
                    canvas.drawPath(this.mJandystripe, this.mPaint2);
                    this.mJandystripe.offset(0.0f, width * 4);
                    i = i2 + (width * 4);
                }
            }
            if (this.mDrawShadow) {
                this.mShadow.reset();
                this.mShadow.moveTo(0.0f, 0.0f);
                this.mShadow.lineTo(width, 0.0f);
                this.mShadow.lineTo(width, (MLand.PARAMS.OBSTACLE_WIDTH * 0.4f) + (width * 1.5f));
                this.mShadow.lineTo(0.0f, MLand.PARAMS.OBSTACLE_WIDTH * 0.4f);
                this.mShadow.close();
                canvas.drawPath(this.mShadow, this.mPaint);
            }
        }
    }

    public MLand(Context context) {
        this(context, null);
    }

    public MLand(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public MLand(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mAudioAttrs = new AudioAttributes.Builder().setUsage(14).build();
        this.mPlayers = new ArrayList<>();
        this.mObstaclesInPlay = new ArrayList<>();
        this.mCountdown = 0;
        this.mGameControllers = new ArrayList<>();
        this.mVibrator = (Vibrator) context.getSystemService("vibrator");
        this.mAudioManager = (AudioManager) context.getSystemService("audio");
        setFocusable(true);
        PARAMS = new Params(getResources());
        this.mTimeOfDay = irand(0, SKIES.length - 1);
        this.mScene = irand(0, 3);
        this.mTouchPaint = new Paint(1);
        this.mTouchPaint.setColor(-2130706433);
        this.mTouchPaint.setStyle(Paint.Style.FILL);
        this.mPlayerTracePaint = new Paint(1);
        this.mPlayerTracePaint.setColor(-2130706433);
        this.mPlayerTracePaint.setStyle(Paint.Style.STROKE);
        this.mPlayerTracePaint.setStrokeWidth(dp * 2.0f);
        setLayoutDirection(0);
        setupPlayers(1);
        MetricsLogger.count(getContext(), "egg_mland_create", 1);
    }

    public static void L(String str, Object... objArr) {
        if (DEBUG) {
            if (objArr.length != 0) {
                str = String.format(str, objArr);
            }
            Log.d("MLand", str);
        }
    }

    private int addPlayerInternal(Player player) {
        this.mPlayers.add(player);
        realignPlayers();
        TextView textView = (TextView) LayoutInflater.from(getContext()).inflate(2130968705, (ViewGroup) null);
        if (this.mScoreFields != null) {
            this.mScoreFields.addView(textView, new ViewGroup.MarginLayoutParams(-2, -1));
        }
        player.setScoreField(textView);
        return this.mPlayers.size() - 1;
    }

    public static final float clamp(float f) {
        float f2;
        if (f < 0.0f) {
            f2 = 0.0f;
        } else {
            f2 = f;
            if (f > 1.0f) {
                f2 = 1.0f;
            }
        }
        return f2;
    }

    private void clearPlayers() {
        while (this.mPlayers.size() > 0) {
            removePlayerInternal(this.mPlayers.get(0));
        }
    }

    public static final float frand() {
        return (float) Math.random();
    }

    public static final float frand(float f, float f2) {
        return lerp(frand(), f, f2);
    }

    public static final int irand(int i, int i2) {
        return Math.round(frand(i, i2));
    }

    public static boolean isGamePad(InputDevice inputDevice) {
        int sources = inputDevice.getSources();
        boolean z = true;
        if ((sources & 1025) != 1025) {
            z = (sources & 16777232) == 16777232;
        }
        return z;
    }

    public static final float lerp(float f, float f2, float f3) {
        return ((f3 - f2) * f) + f2;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static float luma(int i) {
        return (((16711680 & i) * 0.2126f) / 1.671168E7f) + (((65280 & i) * 0.7152f) / 65280.0f) + (((i & 255) * 0.0722f) / 255.0f);
    }

    public static int pick(int[] iArr) {
        return iArr[irand(0, iArr.length - 1)];
    }

    private void poke(int i) {
        poke(i, -1.0f, -1.0f);
    }

    private void poke(int i, float f, float f2) {
        L("poke(%d)", Integer.valueOf(i));
        if (this.mFrozen) {
            return;
        }
        if (!this.mAnimating) {
            reset();
        }
        if (!this.mPlaying) {
            start(true);
            return;
        }
        Player player = getPlayer(i);
        if (player == null) {
            return;
        }
        player.boost(f, f2);
        this.mTaps++;
        if (DEBUG) {
            player.dv *= 0.5f;
            player.animate().setDuration(400L);
        }
    }

    private void realignPlayers() {
        int size = this.mPlayers.size();
        float f = (this.mWidth - ((size - 1) * PARAMS.PLAYER_SIZE)) / 2;
        for (int i = 0; i < size; i++) {
            this.mPlayers.get(i).setX(f);
            f += PARAMS.PLAYER_SIZE;
        }
    }

    private void removePlayerInternal(Player player) {
        if (this.mPlayers.remove(player)) {
            removeView(player);
            this.mScoreFields.removeView(player.mScoreField);
            realignPlayers();
        }
    }

    public static final float rlerp(float f, float f2, float f3) {
        return (f - f2) / (f3 - f2);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void step(long j, long j2) {
        int i;
        this.t = ((float) j) / 1000.0f;
        this.dt = ((float) j2) / 1000.0f;
        if (DEBUG) {
            this.t *= 0.5f;
            this.dt *= 0.5f;
        }
        int childCount = getChildCount();
        int i2 = 0;
        while (i2 < childCount) {
            View childAt = getChildAt(i2);
            if (childAt instanceof GameView) {
                ((GameView) childAt).step(j, j2, this.t, this.dt);
            }
            i2++;
        }
        if (this.mPlaying) {
            int i3 = 0;
            int i4 = 0;
            while (i4 < this.mPlayers.size()) {
                Player player = getPlayer(i4);
                if (player.mAlive) {
                    if (player.below(this.mHeight)) {
                        if (DEBUG_IDDQD) {
                            poke(i4);
                            unpoke(i4);
                        } else {
                            L("player %d hit the floor", Integer.valueOf(i4));
                            thump(i4, 80L);
                            player.die();
                        }
                    }
                    int i5 = 0;
                    int size = this.mObstaclesInPlay.size();
                    while (true) {
                        int i6 = size - 1;
                        if (size <= 0) {
                            break;
                        }
                        Obstacle obstacle = this.mObstaclesInPlay.get(i6);
                        if (!obstacle.intersects(player) || DEBUG_IDDQD) {
                            i = i5;
                            if (obstacle.cleared(player)) {
                                i = i5;
                                if (obstacle instanceof Stem) {
                                    i = Math.max(i5, ((Stem) obstacle).id);
                                }
                            }
                        } else {
                            L("player hit an obstacle", new Object[0]);
                            thump(i4, 80L);
                            player.die();
                            i = i5;
                        }
                        size = i6;
                        i5 = i;
                    }
                    if (i5 > player.mScore) {
                        player.addScore(1);
                    }
                }
                int i7 = i3;
                if (player.mAlive) {
                    i7 = i3 + 1;
                }
                i4++;
                i3 = i7;
            }
            i2 = i4;
            if (i3 == 0) {
                stop();
                MetricsLogger.count(getContext(), "egg_mland_taps", this.mTaps);
                this.mTaps = 0;
                int size2 = this.mPlayers.size();
                int i8 = 0;
                while (true) {
                    i2 = i4;
                    if (i8 >= size2) {
                        break;
                    }
                    MetricsLogger.histogram(getContext(), "egg_mland_score", this.mPlayers.get(i8).getScore());
                    i8++;
                }
            }
        }
        while (true) {
            int i9 = i2 - 1;
            if (i2 <= 0) {
                break;
            }
            View childAt2 = getChildAt(i9);
            if (childAt2 instanceof Obstacle) {
                i2 = i9;
                if (childAt2.getTranslationX() + childAt2.getWidth() < 0.0f) {
                    removeViewAt(i9);
                    this.mObstaclesInPlay.remove(childAt2);
                    i2 = i9;
                }
            } else {
                i2 = i9;
                if (childAt2 instanceof Scenery) {
                    i2 = i9;
                    if (childAt2.getTranslationX() + ((Scenery) childAt2).w < 0.0f) {
                        childAt2.setTranslationX(getWidth());
                        i2 = i9;
                    }
                }
            }
        }
        if (this.mPlaying && this.t - this.mLastPipeTime > PARAMS.OBSTACLE_PERIOD) {
            this.mLastPipeTime = this.t;
            this.mCurrentPipeId++;
            int frand = ((int) (frand() * ((this.mHeight - (PARAMS.OBSTACLE_MIN * 2)) - PARAMS.OBSTACLE_GAP))) + PARAMS.OBSTACLE_MIN;
            int i10 = (PARAMS.OBSTACLE_WIDTH - PARAMS.OBSTACLE_STEM_WIDTH) / 2;
            int i11 = PARAMS.OBSTACLE_WIDTH / 2;
            int irand = irand(0, 250);
            Stem stem = new Stem(this, getContext(), frand - i11, false);
            addView(stem, new FrameLayout.LayoutParams(PARAMS.OBSTACLE_STEM_WIDTH, (int) stem.h, 51));
            stem.setTranslationX(this.mWidth + i10);
            stem.setTranslationY((-stem.h) - i11);
            stem.setTranslationZ(PARAMS.OBSTACLE_Z * 0.75f);
            stem.animate().translationY(0.0f).setStartDelay(irand).setDuration(250L);
            this.mObstaclesInPlay.add(stem);
            Pop pop = new Pop(this, getContext(), PARAMS.OBSTACLE_WIDTH);
            addView(pop, new FrameLayout.LayoutParams(PARAMS.OBSTACLE_WIDTH, PARAMS.OBSTACLE_WIDTH, 51));
            pop.setTranslationX(this.mWidth);
            pop.setTranslationY(-PARAMS.OBSTACLE_WIDTH);
            pop.setTranslationZ(PARAMS.OBSTACLE_Z);
            pop.setScaleX(0.25f);
            pop.setScaleY(-0.25f);
            pop.animate().translationY(stem.h - i10).scaleX(1.0f).scaleY(-1.0f).setStartDelay(irand).setDuration(250L);
            this.mObstaclesInPlay.add(pop);
            int irand2 = irand(0, 250);
            Stem stem2 = new Stem(this, getContext(), ((this.mHeight - frand) - PARAMS.OBSTACLE_GAP) - i11, true);
            addView(stem2, new FrameLayout.LayoutParams(PARAMS.OBSTACLE_STEM_WIDTH, (int) stem2.h, 51));
            stem2.setTranslationX(this.mWidth + i10);
            stem2.setTranslationY(this.mHeight + i11);
            stem2.setTranslationZ(PARAMS.OBSTACLE_Z * 0.75f);
            stem2.animate().translationY(this.mHeight - stem2.h).setStartDelay(irand2).setDuration(400L);
            this.mObstaclesInPlay.add(stem2);
            Pop pop2 = new Pop(this, getContext(), PARAMS.OBSTACLE_WIDTH);
            addView(pop2, new FrameLayout.LayoutParams(PARAMS.OBSTACLE_WIDTH, PARAMS.OBSTACLE_WIDTH, 51));
            pop2.setTranslationX(this.mWidth);
            pop2.setTranslationY(this.mHeight);
            pop2.setTranslationZ(PARAMS.OBSTACLE_Z);
            pop2.setScaleX(0.25f);
            pop2.setScaleY(0.25f);
            pop2.animate().translationY((this.mHeight - stem2.h) - i11).scaleX(1.0f).scaleY(1.0f).setStartDelay(irand2).setDuration(400L);
            this.mObstaclesInPlay.add(pop2);
        }
        invalidate();
    }

    private void thump(int i, long j) {
        InputDevice device;
        if (this.mAudioManager.getRingerMode() == 0) {
            return;
        }
        if (i >= this.mGameControllers.size() || (device = InputDevice.getDevice(this.mGameControllers.get(i).intValue())) == null || !device.getVibrator().hasVibrator()) {
            this.mVibrator.vibrate(j, this.mAudioAttrs);
        } else {
            device.getVibrator().vibrate(((float) j) * 2.0f, this.mAudioAttrs);
        }
    }

    private void unpoke(int i) {
        Player player;
        L("unboost(%d)", Integer.valueOf(i));
        if (this.mFrozen || !this.mAnimating || !this.mPlaying || (player = getPlayer(i)) == null) {
            return;
        }
        player.unboost();
    }

    public void addPlayer() {
        if (getNumPlayers() == 6) {
            return;
        }
        addPlayerInternal(Player.create(this));
    }

    public int getControllerPlayer(int i) {
        int indexOf = this.mGameControllers.indexOf(Integer.valueOf(i));
        if (indexOf < 0 || indexOf >= this.mPlayers.size()) {
            return 0;
        }
        return indexOf;
    }

    public ArrayList getGameControllers() {
        int[] deviceIds;
        this.mGameControllers.clear();
        for (int i : InputDevice.getDeviceIds()) {
            if (isGamePad(InputDevice.getDevice(i)) && !this.mGameControllers.contains(Integer.valueOf(i))) {
                this.mGameControllers.add(Integer.valueOf(i));
            }
        }
        return this.mGameControllers;
    }

    public float getGameTime() {
        return this.t;
    }

    public int getNumPlayers() {
        return this.mPlayers.size();
    }

    public Player getPlayer(int i) {
        return i < this.mPlayers.size() ? this.mPlayers.get(i) : null;
    }

    public void hideSplash() {
        if (this.mSplash == null || this.mSplash.getVisibility() != 0) {
            return;
        }
        this.mSplash.setClickable(false);
        this.mSplash.animate().alpha(0.0f).translationZ(0.0f).setDuration(300L).withEndAction(new Runnable(this) { // from class: com.android.systemui.egg.MLand.3
            final MLand this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.mSplash.setVisibility(8);
            }
        });
    }

    @Override // android.view.ViewGroup, android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        dp = getResources().getDisplayMetrics().density;
        reset();
        start(false);
    }

    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (Player player : this.mPlayers) {
            if (player.mTouchX > 0.0f) {
                this.mTouchPaint.setColor(player.color & (-2130706433));
                this.mPlayerTracePaint.setColor(player.color & (-2130706433));
                float f = player.mTouchX;
                float f2 = player.mTouchY;
                canvas.drawCircle(f, f2, 100.0f, this.mTouchPaint);
                float x = player.getX() + player.getPivotX();
                float y = player.getY() + player.getPivotY();
                float atan2 = 1.5707964f - ((float) Math.atan2(x - f, y - f2));
                canvas.drawLine((float) (f + (Math.cos(atan2) * 100.0d)), (float) (f2 + (Math.sin(atan2) * 100.0d)), x, y, this.mPlayerTracePaint);
            }
        }
    }

    @Override // android.view.View
    public boolean onGenericMotionEvent(MotionEvent motionEvent) {
        L("generic: %s", motionEvent);
        return false;
    }

    @Override // android.view.View, android.view.KeyEvent.Callback
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        L("keyDown: %d", Integer.valueOf(i));
        switch (i) {
            case 19:
            case 23:
            case 62:
            case 66:
            case 96:
                poke(getControllerPlayer(keyEvent.getDeviceId()));
                return true;
            default:
                return false;
        }
    }

    @Override // android.view.View, android.view.KeyEvent.Callback
    public boolean onKeyUp(int i, KeyEvent keyEvent) {
        L("keyDown: %d", Integer.valueOf(i));
        switch (i) {
            case 19:
            case 23:
            case 62:
            case 66:
            case 96:
                unpoke(getControllerPlayer(keyEvent.getDeviceId()));
                return true;
            default:
                return false;
        }
    }

    @Override // android.view.View
    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        dp = getResources().getDisplayMetrics().density;
        stop();
        reset();
        start(false);
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        L("touch: %s", motionEvent);
        int actionIndex = motionEvent.getActionIndex();
        float x = motionEvent.getX(actionIndex);
        float y = motionEvent.getY(actionIndex);
        int numPlayers = (int) (getNumPlayers() * (x / getWidth()));
        int i = numPlayers;
        if (this.mFlipped) {
            i = (getNumPlayers() - 1) - numPlayers;
        }
        switch (motionEvent.getActionMasked()) {
            case 0:
            case 5:
                poke(i, x, y);
                return true;
            case 1:
            case 6:
                unpoke(i);
                return true;
            case 2:
            case 3:
            case 4:
            default:
                return false;
        }
    }

    @Override // android.view.View
    public boolean onTrackballEvent(MotionEvent motionEvent) {
        L("trackball: %s", motionEvent);
        switch (motionEvent.getAction()) {
            case 0:
                poke(0);
                return true;
            case 1:
                unpoke(0);
                return true;
            default:
                return false;
        }
    }

    public void removePlayer() {
        if (getNumPlayers() == 1) {
            return;
        }
        removePlayerInternal(this.mPlayers.get(this.mPlayers.size() - 1));
    }

    public void reset() {
        View cactus;
        View view;
        L("reset", new Object[0]);
        Drawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, SKIES[this.mTimeOfDay]);
        gradientDrawable.setDither(true);
        setBackground(gradientDrawable);
        this.mFlipped = frand() > 0.5f;
        setScaleX(this.mFlipped ? -1 : 1);
        int childCount = getChildCount();
        while (true) {
            int i = childCount;
            int i2 = i - 1;
            if (i <= 0) {
                break;
            }
            if (getChildAt(i2) instanceof GameView) {
                removeViewAt(i2);
            }
            childCount = i2;
        }
        this.mObstaclesInPlay.clear();
        this.mCurrentPipeId = 0;
        this.mWidth = getWidth();
        this.mHeight = getHeight();
        boolean z = (this.mTimeOfDay == 0 || this.mTimeOfDay == 3) ? ((double) frand()) > 0.25d : false;
        if (z) {
            Star star = new Star(this, getContext());
            star.setBackgroundResource(2130838336);
            int dimensionPixelSize = getResources().getDimensionPixelSize(2131690088);
            star.setTranslationX(frand(dimensionPixelSize, this.mWidth - dimensionPixelSize));
            if (this.mTimeOfDay == 0) {
                star.setTranslationY(frand(dimensionPixelSize, this.mHeight * 0.66f));
                star.getBackground().setTint(0);
            } else {
                star.setTranslationY(frand(this.mHeight * 0.66f, this.mHeight - dimensionPixelSize));
                star.getBackground().setTintMode(PorterDuff.Mode.SRC_ATOP);
                star.getBackground().setTint(-1056997376);
            }
            addView(star, new FrameLayout.LayoutParams(dimensionPixelSize, dimensionPixelSize));
        }
        if (!z) {
            boolean z2 = this.mTimeOfDay == 1 || this.mTimeOfDay == 2;
            float frand = frand();
            if ((z2 && frand < 0.75f) || frand < 0.5f) {
                Star star2 = new Star(this, getContext());
                star2.setBackgroundResource(2130837921);
                star2.getBackground().setAlpha(z2 ? 255 : 128);
                star2.setScaleX(((double) frand()) > 0.5d ? -1 : 1);
                star2.setRotation(star2.getScaleX() * frand(5.0f, 30.0f));
                int dimensionPixelSize2 = getResources().getDimensionPixelSize(2131690088);
                star2.setTranslationX(frand(dimensionPixelSize2, this.mWidth - dimensionPixelSize2));
                star2.setTranslationY(frand(dimensionPixelSize2, this.mHeight - dimensionPixelSize2));
                addView(star2, new FrameLayout.LayoutParams(dimensionPixelSize2, dimensionPixelSize2));
            }
        }
        int i3 = this.mHeight / 6;
        boolean z3 = ((double) frand()) < 0.25d;
        for (int i4 = 0; i4 < 20; i4++) {
            float frand2 = frand();
            if (frand2 < 0.3d && this.mTimeOfDay != 0) {
                view = new Star(this, getContext());
            } else if (frand2 >= 0.6d || z3) {
                switch (this.mScene) {
                    case 1:
                        cactus = new Cactus(this, getContext());
                        break;
                    case 2:
                        cactus = new Mountain(this, getContext());
                        break;
                    default:
                        cactus = new Building(this, getContext());
                        break;
                }
                cactus.z = i4 / 20.0f;
                cactus.v = cactus.z * 0.85f;
                if (this.mScene == 0) {
                    cactus.setBackgroundColor(-7829368);
                    cactus.h = irand(PARAMS.BUILDING_HEIGHT_MIN, i3);
                }
                int i5 = (int) (cactus.z * 255.0f);
                Drawable background = cactus.getBackground();
                view = cactus;
                if (background != null) {
                    background.setColorFilter(Color.rgb(i5, i5, i5), PorterDuff.Mode.MULTIPLY);
                    view = cactus;
                }
            } else {
                view = new Cloud(this, getContext());
            }
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(view.w, view.h);
            if (view instanceof Building) {
                layoutParams.gravity = 80;
            } else {
                layoutParams.gravity = 48;
                float frand3 = frand();
                if (view instanceof Star) {
                    layoutParams.topMargin = (int) (frand3 * frand3 * this.mHeight);
                } else {
                    layoutParams.topMargin = ((int) (1.0f - (((frand3 * frand3) * this.mHeight) / 2.0f))) + (this.mHeight / 2);
                }
            }
            addView(view, layoutParams);
            view.setTranslationX(frand(-layoutParams.width, this.mWidth + layoutParams.width));
        }
        for (Player player : this.mPlayers) {
            addView(player);
            player.reset();
        }
        realignPlayers();
        if (this.mAnim != null) {
            this.mAnim.cancel();
        }
        this.mAnim = new TimeAnimator();
        this.mAnim.setTimeListener(new TimeAnimator.TimeListener(this) { // from class: com.android.systemui.egg.MLand.1
            final MLand this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.TimeAnimator.TimeListener
            public void onTimeUpdate(TimeAnimator timeAnimator, long j, long j2) {
                this.this$0.step(j, j2);
            }
        });
    }

    public void setScoreFieldHolder(ViewGroup viewGroup) {
        this.mScoreFields = viewGroup;
        if (viewGroup != null) {
            LayoutTransition layoutTransition = new LayoutTransition();
            layoutTransition.setDuration(250L);
            this.mScoreFields.setLayoutTransition(layoutTransition);
        }
        for (Player player : this.mPlayers) {
            this.mScoreFields.addView(player.mScoreField, new ViewGroup.MarginLayoutParams(-2, -1));
        }
    }

    public void setSplash(View view) {
        this.mSplash = view;
    }

    public void setupPlayers(int i) {
        clearPlayers();
        for (int i2 = 0; i2 < i; i2++) {
            addPlayerInternal(Player.create(this));
        }
    }

    public void showSplash() {
        if (this.mSplash == null || this.mSplash.getVisibility() == 0) {
            return;
        }
        this.mSplash.setClickable(true);
        this.mSplash.setAlpha(0.0f);
        this.mSplash.setVisibility(0);
        this.mSplash.animate().alpha(1.0f).setDuration(1000L);
        this.mSplash.findViewById(2131886485).setAlpha(1.0f);
        this.mSplash.findViewById(2131886486).setAlpha(0.0f);
        this.mSplash.findViewById(2131886484).setEnabled(true);
        this.mSplash.findViewById(2131886484).requestFocus();
    }

    public void start(boolean z) {
        L("start(startPlaying=%s)", z ? "true" : "false");
        if (z && this.mCountdown <= 0) {
            showSplash();
            this.mSplash.findViewById(2131886484).setEnabled(false);
            View findViewById = this.mSplash.findViewById(2131886485);
            TextView textView = (TextView) this.mSplash.findViewById(2131886486);
            findViewById.animate().alpha(0.0f);
            textView.animate().alpha(1.0f);
            this.mCountdown = 3;
            post(new Runnable(this, textView) { // from class: com.android.systemui.egg.MLand.2
                final MLand this$0;
                final TextView val$playText;

                {
                    this.this$0 = this;
                    this.val$playText = textView;
                }

                @Override // java.lang.Runnable
                public void run() {
                    if (this.this$0.mCountdown == 0) {
                        this.this$0.startPlaying();
                    } else {
                        this.this$0.postDelayed(this, 500L);
                    }
                    this.val$playText.setText(String.valueOf(this.this$0.mCountdown));
                    this.this$0.mCountdown--;
                }
            });
        }
        for (Player player : this.mPlayers) {
            player.setVisibility(4);
        }
        if (this.mAnimating) {
            return;
        }
        this.mAnim.start();
        this.mAnimating = true;
    }

    public void startPlaying() {
        this.mPlaying = true;
        this.t = 0.0f;
        this.mLastPipeTime = getGameTime() - PARAMS.OBSTACLE_PERIOD;
        hideSplash();
        realignPlayers();
        this.mTaps = 0;
        int size = this.mPlayers.size();
        MetricsLogger.histogram(getContext(), "egg_mland_players", size);
        for (int i = 0; i < size; i++) {
            Player player = this.mPlayers.get(i);
            player.setVisibility(0);
            player.reset();
            player.start();
            player.boost(-1.0f, -1.0f);
            player.unboost();
        }
    }

    public void stop() {
        if (this.mAnimating) {
            this.mAnim.cancel();
            this.mAnim = null;
            this.mAnimating = false;
            this.mPlaying = false;
            this.mTimeOfDay = irand(0, SKIES.length - 1);
            this.mScene = irand(0, 3);
            this.mFrozen = true;
            for (Player player : this.mPlayers) {
                player.die();
            }
            postDelayed(new Runnable(this) { // from class: com.android.systemui.egg.MLand.4
                final MLand this$0;

                {
                    this.this$0 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$0.mFrozen = false;
                }
            }, 250L);
        }
    }

    @Override // android.view.View
    public boolean willNotDraw() {
        return !DEBUG;
    }
}

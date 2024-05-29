package com.android.systemui.screenshot;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.media.MediaActionSound;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceControl;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import com.android.systemui.SystemUI;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: a.zip:com/android/systemui/screenshot/GlobalScreenshot.class */
public class GlobalScreenshot {
    private ImageView mBackgroundView;
    private float mBgPadding;
    private float mBgPaddingScale;
    private MediaActionSound mCameraSound;
    private Context mContext;
    private Display mDisplay;
    private Matrix mDisplayMatrix;
    private DisplayMetrics mDisplayMetrics;
    private int mNotificationIconSize;
    private NotificationManager mNotificationManager;
    private final int mPreviewHeight;
    private final int mPreviewWidth;
    private AsyncTask<Void, Void, Void> mSaveInBgTask;
    private Bitmap mScreenBitmap;
    private AnimatorSet mScreenshotAnimation;
    private ImageView mScreenshotFlash;
    private View mScreenshotLayout;
    private ScreenshotSelectorView mScreenshotSelectorView;
    private ImageView mScreenshotView;
    private WindowManager.LayoutParams mWindowLayoutParams;
    private WindowManager mWindowManager;

    /* renamed from: com.android.systemui.screenshot.GlobalScreenshot$2  reason: invalid class name */
    /* loaded from: a.zip:com/android/systemui/screenshot/GlobalScreenshot$2.class */
    class AnonymousClass2 implements View.OnTouchListener {
        final GlobalScreenshot this$0;
        final Runnable val$finisher;
        final boolean val$navBarVisible;
        final boolean val$statusBarVisible;

        AnonymousClass2(GlobalScreenshot globalScreenshot, Runnable runnable, boolean z, boolean z2) {
            this.this$0 = globalScreenshot;
            this.val$finisher = runnable;
            this.val$statusBarVisible = z;
            this.val$navBarVisible = z2;
        }

        @Override // android.view.View.OnTouchListener
        public boolean onTouch(View view, MotionEvent motionEvent) {
            ScreenshotSelectorView screenshotSelectorView = (ScreenshotSelectorView) view;
            switch (motionEvent.getAction()) {
                case 0:
                    screenshotSelectorView.startSelection((int) motionEvent.getX(), (int) motionEvent.getY());
                    return true;
                case 1:
                    screenshotSelectorView.setVisibility(8);
                    this.this$0.mWindowManager.removeView(this.this$0.mScreenshotLayout);
                    Rect selectionRect = screenshotSelectorView.getSelectionRect();
                    if (selectionRect != null && selectionRect.width() != 0 && selectionRect.height() != 0) {
                        this.this$0.mScreenshotLayout.post(new Runnable(this, this.val$finisher, this.val$statusBarVisible, this.val$navBarVisible, selectionRect) { // from class: com.android.systemui.screenshot.GlobalScreenshot.2.1
                            final AnonymousClass2 this$1;
                            final Runnable val$finisher;
                            final boolean val$navBarVisible;
                            final Rect val$rect;
                            final boolean val$statusBarVisible;

                            {
                                this.this$1 = this;
                                this.val$finisher = r5;
                                this.val$statusBarVisible = r6;
                                this.val$navBarVisible = r7;
                                this.val$rect = selectionRect;
                            }

                            @Override // java.lang.Runnable
                            public void run() {
                                this.this$1.this$0.takeScreenshot(this.val$finisher, this.val$statusBarVisible, this.val$navBarVisible, this.val$rect.left, this.val$rect.top, this.val$rect.width(), this.val$rect.height());
                            }
                        });
                    }
                    screenshotSelectorView.stopSelection();
                    return true;
                case 2:
                    screenshotSelectorView.updateSelection((int) motionEvent.getX(), (int) motionEvent.getY());
                    return true;
                default:
                    return false;
            }
        }
    }

    /* loaded from: a.zip:com/android/systemui/screenshot/GlobalScreenshot$DeleteScreenshotReceiver.class */
    public static class DeleteScreenshotReceiver extends BroadcastReceiver {
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("android:screenshot_uri_id")) {
                NotificationManager notificationManager = (NotificationManager) context.getSystemService("notification");
                Uri parse = Uri.parse(intent.getStringExtra("android:screenshot_uri_id"));
                notificationManager.cancel(2131886133);
                new DeleteImageInBackgroundTask(context).execute(parse);
            }
        }
    }

    /* loaded from: a.zip:com/android/systemui/screenshot/GlobalScreenshot$TargetChosenReceiver.class */
    public static class TargetChosenReceiver extends BroadcastReceiver {
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            ((NotificationManager) context.getSystemService("notification")).cancel(2131886133);
        }
    }

    public GlobalScreenshot(Context context) {
        Resources resources = context.getResources();
        this.mContext = context;
        this.mDisplayMatrix = new Matrix();
        this.mScreenshotLayout = ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(2130968619, (ViewGroup) null);
        this.mBackgroundView = (ImageView) this.mScreenshotLayout.findViewById(2131886280);
        this.mScreenshotView = (ImageView) this.mScreenshotLayout.findViewById(2131886281);
        this.mScreenshotFlash = (ImageView) this.mScreenshotLayout.findViewById(2131886282);
        this.mScreenshotSelectorView = (ScreenshotSelectorView) this.mScreenshotLayout.findViewById(2131886283);
        this.mScreenshotLayout.setFocusable(true);
        this.mScreenshotSelectorView.setFocusable(true);
        this.mScreenshotSelectorView.setFocusableInTouchMode(true);
        this.mScreenshotLayout.setOnTouchListener(new View.OnTouchListener(this) { // from class: com.android.systemui.screenshot.GlobalScreenshot.1
            final GlobalScreenshot this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
        this.mWindowLayoutParams = new WindowManager.LayoutParams(-1, -1, 0, 0, 2036, 17302784, -3);
        this.mWindowLayoutParams.setTitle("ScreenshotAnimation");
        this.mWindowManager = (WindowManager) context.getSystemService("window");
        this.mNotificationManager = (NotificationManager) context.getSystemService("notification");
        this.mDisplay = this.mWindowManager.getDefaultDisplay();
        this.mDisplayMetrics = new DisplayMetrics();
        this.mDisplay.getRealMetrics(this.mDisplayMetrics);
        this.mNotificationIconSize = resources.getDimensionPixelSize(17104902);
        this.mBgPadding = resources.getDimensionPixelSize(2131689797);
        this.mBgPaddingScale = this.mBgPadding / this.mDisplayMetrics.widthPixels;
        int i = 0;
        try {
            i = resources.getDimensionPixelSize(2131689816);
        } catch (Resources.NotFoundException e) {
        }
        this.mPreviewWidth = i <= 0 ? this.mDisplayMetrics.widthPixels : i;
        this.mPreviewHeight = resources.getDimensionPixelSize(2131689787);
        this.mCameraSound = new MediaActionSound();
        this.mCameraSound.load(0);
    }

    private ValueAnimator createScreenshotDropInAnimation() {
        Interpolator interpolator = new Interpolator(this) { // from class: com.android.systemui.screenshot.GlobalScreenshot.6
            final GlobalScreenshot this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.TimeInterpolator
            public float getInterpolation(float f) {
                if (f <= 0.60465115f) {
                    return (float) Math.sin((f / 0.60465115f) * 3.141592653589793d);
                }
                return 0.0f;
            }
        };
        Interpolator interpolator2 = new Interpolator(this) { // from class: com.android.systemui.screenshot.GlobalScreenshot.7
            final GlobalScreenshot this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.TimeInterpolator
            public float getInterpolation(float f) {
                if (f < 0.30232558f) {
                    return 0.0f;
                }
                return (f - 0.60465115f) / 0.39534885f;
            }
        };
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        ofFloat.setDuration(430L);
        ofFloat.addListener(new AnimatorListenerAdapter(this) { // from class: com.android.systemui.screenshot.GlobalScreenshot.8
            final GlobalScreenshot this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                this.this$0.mScreenshotFlash.setVisibility(8);
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                this.this$0.mBackgroundView.setAlpha(0.0f);
                this.this$0.mBackgroundView.setVisibility(0);
                this.this$0.mScreenshotView.setAlpha(0.0f);
                this.this$0.mScreenshotView.setTranslationX(0.0f);
                this.this$0.mScreenshotView.setTranslationY(0.0f);
                this.this$0.mScreenshotView.setScaleX(this.this$0.mBgPaddingScale + 1.0f);
                this.this$0.mScreenshotView.setScaleY(this.this$0.mBgPaddingScale + 1.0f);
                this.this$0.mScreenshotView.setVisibility(0);
                this.this$0.mScreenshotFlash.setAlpha(0.0f);
                this.this$0.mScreenshotFlash.setVisibility(0);
            }
        });
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this, interpolator2, interpolator) { // from class: com.android.systemui.screenshot.GlobalScreenshot.9
            final GlobalScreenshot this$0;
            final Interpolator val$flashAlphaInterpolator;
            final Interpolator val$scaleInterpolator;

            {
                this.this$0 = this;
                this.val$scaleInterpolator = interpolator2;
                this.val$flashAlphaInterpolator = interpolator;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                float interpolation = (this.this$0.mBgPaddingScale + 1.0f) - (this.val$scaleInterpolator.getInterpolation(floatValue) * 0.27499998f);
                this.this$0.mBackgroundView.setAlpha(this.val$scaleInterpolator.getInterpolation(floatValue) * 0.5f);
                this.this$0.mScreenshotView.setAlpha(floatValue);
                this.this$0.mScreenshotView.setScaleX(interpolation);
                this.this$0.mScreenshotView.setScaleY(interpolation);
                this.this$0.mScreenshotFlash.setAlpha(this.val$flashAlphaInterpolator.getInterpolation(floatValue));
            }
        });
        return ofFloat;
    }

    private ValueAnimator createScreenshotDropOutAnimation(int i, int i2, boolean z, boolean z2) {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        ofFloat.setStartDelay(500L);
        ofFloat.addListener(new AnimatorListenerAdapter(this) { // from class: com.android.systemui.screenshot.GlobalScreenshot.10
            final GlobalScreenshot this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                this.this$0.mBackgroundView.setVisibility(8);
                this.this$0.mScreenshotView.setVisibility(8);
                this.this$0.mScreenshotView.setLayerType(0, null);
            }
        });
        if (z && z2) {
            Interpolator interpolator = new Interpolator(this) { // from class: com.android.systemui.screenshot.GlobalScreenshot.12
                final GlobalScreenshot this$0;

                {
                    this.this$0 = this;
                }

                @Override // android.animation.TimeInterpolator
                public float getInterpolation(float f) {
                    if (f < 0.8604651f) {
                        return (float) (1.0d - Math.pow(1.0f - (f / 0.8604651f), 2.0d));
                    }
                    return 1.0f;
                }
            };
            float f = (i - (this.mBgPadding * 2.0f)) / 2.0f;
            float f2 = (i2 - (this.mBgPadding * 2.0f)) / 2.0f;
            PointF pointF = new PointF((-f) + (0.45f * f), (-f2) + (0.45f * f2));
            ofFloat.setDuration(430L);
            ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this, interpolator, pointF) { // from class: com.android.systemui.screenshot.GlobalScreenshot.13
                final GlobalScreenshot this$0;
                final PointF val$finalPos;
                final Interpolator val$scaleInterpolator;

                {
                    this.this$0 = this;
                    this.val$scaleInterpolator = interpolator;
                    this.val$finalPos = pointF;
                }

                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                    float interpolation = (this.this$0.mBgPaddingScale + 0.725f) - (this.val$scaleInterpolator.getInterpolation(floatValue) * 0.27500004f);
                    this.this$0.mBackgroundView.setAlpha((1.0f - floatValue) * 0.5f);
                    this.this$0.mScreenshotView.setAlpha(1.0f - this.val$scaleInterpolator.getInterpolation(floatValue));
                    this.this$0.mScreenshotView.setScaleX(interpolation);
                    this.this$0.mScreenshotView.setScaleY(interpolation);
                    this.this$0.mScreenshotView.setTranslationX(this.val$finalPos.x * floatValue);
                    this.this$0.mScreenshotView.setTranslationY(this.val$finalPos.y * floatValue);
                }
            });
        } else {
            ofFloat.setDuration(320L);
            ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this) { // from class: com.android.systemui.screenshot.GlobalScreenshot.11
                final GlobalScreenshot this$0;

                {
                    this.this$0 = this;
                }

                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                    float f3 = (this.this$0.mBgPaddingScale + 0.725f) - (0.125f * floatValue);
                    this.this$0.mBackgroundView.setAlpha((1.0f - floatValue) * 0.5f);
                    this.this$0.mScreenshotView.setAlpha(1.0f - floatValue);
                    this.this$0.mScreenshotView.setScaleX(f3);
                    this.this$0.mScreenshotView.setScaleY(f3);
                }
            });
        }
        return ofFloat;
    }

    private float getDegreesForRotation(int i) {
        switch (i) {
            case 1:
                return 270.0f;
            case 2:
                return 180.0f;
            case 3:
                return 90.0f;
            default:
                return 0.0f;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void notifyScreenshotError(Context context, NotificationManager notificationManager, int i) {
        Resources resources = context.getResources();
        String string = resources.getString(i);
        Notification.Builder color = new Notification.Builder(context).setTicker(resources.getString(2131493339)).setContentTitle(resources.getString(2131493339)).setContentText(string).setSmallIcon(2130838250).setWhen(System.currentTimeMillis()).setVisibility(1).setCategory("err").setAutoCancel(true).setColor(context.getColor(17170521));
        SystemUI.overrideNotificationAppName(context, color);
        notificationManager.notify(2131886133, new Notification.BigTextStyle(color).bigText(string).build());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void saveScreenshotInWorkerThread(Runnable runnable) {
        SaveImageInBackgroundData saveImageInBackgroundData = new SaveImageInBackgroundData();
        saveImageInBackgroundData.context = this.mContext;
        saveImageInBackgroundData.image = this.mScreenBitmap;
        saveImageInBackgroundData.iconSize = this.mNotificationIconSize;
        saveImageInBackgroundData.finisher = runnable;
        saveImageInBackgroundData.previewWidth = this.mPreviewWidth;
        saveImageInBackgroundData.previewheight = this.mPreviewHeight;
        if (this.mSaveInBgTask != null) {
            this.mSaveInBgTask.cancel(false);
        }
        if (saveImageInBackgroundData.image == null) {
            Log.d("saveScreenshotInWorkerThread", "The image is null before saving it!");
        } else {
            this.mSaveInBgTask = new SaveImageInBackgroundTask(this.mContext, saveImageInBackgroundData, this.mNotificationManager).execute(new Void[0]);
        }
    }

    private void startAnimation(Runnable runnable, int i, int i2, boolean z, boolean z2) {
        this.mScreenshotView.setImageBitmap(this.mScreenBitmap);
        this.mScreenshotLayout.requestFocus();
        if (this.mScreenshotAnimation != null) {
            if (this.mScreenshotAnimation.isStarted()) {
                this.mScreenshotAnimation.end();
            }
            this.mScreenshotAnimation.removeAllListeners();
        }
        this.mWindowManager.addView(this.mScreenshotLayout, this.mWindowLayoutParams);
        ValueAnimator createScreenshotDropInAnimation = createScreenshotDropInAnimation();
        ValueAnimator createScreenshotDropOutAnimation = createScreenshotDropOutAnimation(i, i2, z, z2);
        this.mScreenshotAnimation = new AnimatorSet();
        this.mScreenshotAnimation.playSequentially(createScreenshotDropInAnimation, createScreenshotDropOutAnimation);
        this.mScreenshotAnimation.addListener(new AnimatorListenerAdapter(this, runnable) { // from class: com.android.systemui.screenshot.GlobalScreenshot.4
            final GlobalScreenshot this$0;
            final Runnable val$finisher;

            {
                this.this$0 = this;
                this.val$finisher = runnable;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                this.this$0.saveScreenshotInWorkerThread(this.val$finisher);
                this.this$0.mWindowManager.removeView(this.this$0.mScreenshotLayout);
                this.this$0.mScreenBitmap = null;
                this.this$0.mScreenshotView.setImageBitmap(null);
            }
        });
        this.mScreenshotLayout.post(new Runnable(this) { // from class: com.android.systemui.screenshot.GlobalScreenshot.5
            final GlobalScreenshot this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.mCameraSound.play(0);
                this.this$0.mScreenshotView.setLayerType(2, null);
                this.this$0.mScreenshotView.buildLayer();
                this.this$0.mScreenshotAnimation.start();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void stopScreenshot() {
        if (this.mScreenshotSelectorView.getSelectionRect() != null) {
            this.mWindowManager.removeView(this.mScreenshotLayout);
            this.mScreenshotSelectorView.stopSelection();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void takeScreenshot(Runnable runnable, boolean z, boolean z2) {
        this.mDisplay.getRealMetrics(this.mDisplayMetrics);
        takeScreenshot(runnable, z, z2, 0, 0, this.mDisplayMetrics.widthPixels, this.mDisplayMetrics.heightPixels);
    }

    void takeScreenshot(Runnable runnable, boolean z, boolean z2, int i, int i2, int i3, int i4) {
        this.mDisplay.getRealMetrics(this.mDisplayMetrics);
        float[] fArr = {this.mDisplayMetrics.widthPixels, this.mDisplayMetrics.heightPixels};
        float degreesForRotation = getDegreesForRotation(this.mDisplay.getRotation());
        boolean z3 = degreesForRotation > 0.0f;
        if (z3) {
            this.mDisplayMatrix.reset();
            this.mDisplayMatrix.preRotate(-degreesForRotation);
            this.mDisplayMatrix.mapPoints(fArr);
            fArr[0] = Math.abs(fArr[0]);
            fArr[1] = Math.abs(fArr[1]);
        }
        this.mScreenBitmap = SurfaceControl.screenshot((int) fArr[0], (int) fArr[1]);
        if (this.mScreenBitmap == null) {
            notifyScreenshotError(this.mContext, this.mNotificationManager, 2131493342);
            runnable.run();
            return;
        }
        if (z3) {
            Bitmap createBitmap = Bitmap.createBitmap(this.mDisplayMetrics.widthPixels, this.mDisplayMetrics.heightPixels, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(createBitmap);
            canvas.translate(createBitmap.getWidth() / 2, createBitmap.getHeight() / 2);
            canvas.rotate(degreesForRotation);
            canvas.translate((-fArr[0]) / 2.0f, (-fArr[1]) / 2.0f);
            canvas.drawBitmap(this.mScreenBitmap, 0.0f, 0.0f, (Paint) null);
            canvas.setBitmap(null);
            this.mScreenBitmap.recycle();
            this.mScreenBitmap = createBitmap;
        }
        if (i3 != this.mDisplayMetrics.widthPixels || i4 != this.mDisplayMetrics.heightPixels) {
            Bitmap createBitmap2 = Bitmap.createBitmap(this.mScreenBitmap, i, i2, i3, i4);
            this.mScreenBitmap.recycle();
            this.mScreenBitmap = createBitmap2;
        }
        this.mScreenBitmap.setHasAlpha(false);
        this.mScreenBitmap.prepareToDraw();
        startAnimation(runnable, this.mDisplayMetrics.widthPixels, this.mDisplayMetrics.heightPixels, z, z2);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void takeScreenshotPartial(Runnable runnable, boolean z, boolean z2) {
        this.mWindowManager.addView(this.mScreenshotLayout, this.mWindowLayoutParams);
        this.mScreenshotSelectorView.setOnTouchListener(new AnonymousClass2(this, runnable, z, z2));
        this.mScreenshotLayout.post(new Runnable(this) { // from class: com.android.systemui.screenshot.GlobalScreenshot.3
            final GlobalScreenshot this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.mScreenshotSelectorView.setVisibility(0);
                this.this$0.mScreenshotSelectorView.requestFocus();
            }
        });
    }
}

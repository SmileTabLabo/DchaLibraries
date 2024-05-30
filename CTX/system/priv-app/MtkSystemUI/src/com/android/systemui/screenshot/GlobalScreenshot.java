package com.android.systemui.screenshot;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.media.MediaActionSound;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceControl;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.Toast;
import com.android.systemui.R;
import com.android.systemui.SystemUI;
import com.android.systemui.util.NotificationChannels;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
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

    public GlobalScreenshot(Context context) {
        int i;
        Resources resources = context.getResources();
        this.mContext = context;
        this.mDisplayMatrix = new Matrix();
        this.mScreenshotLayout = ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.global_screenshot, (ViewGroup) null);
        this.mBackgroundView = (ImageView) this.mScreenshotLayout.findViewById(R.id.global_screenshot_background);
        this.mScreenshotView = (ImageView) this.mScreenshotLayout.findViewById(R.id.global_screenshot);
        this.mScreenshotFlash = (ImageView) this.mScreenshotLayout.findViewById(R.id.global_screenshot_flash);
        this.mScreenshotSelectorView = (ScreenshotSelectorView) this.mScreenshotLayout.findViewById(R.id.global_screenshot_selector);
        this.mScreenshotLayout.setFocusable(true);
        this.mScreenshotSelectorView.setFocusable(true);
        this.mScreenshotSelectorView.setFocusableInTouchMode(true);
        this.mScreenshotLayout.setOnTouchListener(new View.OnTouchListener() { // from class: com.android.systemui.screenshot.GlobalScreenshot.1
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
        this.mWindowLayoutParams = new WindowManager.LayoutParams(-1, -1, 0, 0, 2036, 525568, -3);
        this.mWindowLayoutParams.setTitle("ScreenshotAnimation");
        this.mWindowLayoutParams.layoutInDisplayCutoutMode = 1;
        this.mWindowManager = (WindowManager) context.getSystemService("window");
        this.mNotificationManager = (NotificationManager) context.getSystemService("notification");
        this.mDisplay = this.mWindowManager.getDefaultDisplay();
        this.mDisplayMetrics = new DisplayMetrics();
        this.mDisplay.getRealMetrics(this.mDisplayMetrics);
        this.mNotificationIconSize = resources.getDimensionPixelSize(17104902);
        this.mBgPadding = resources.getDimensionPixelSize(R.dimen.global_screenshot_bg_padding);
        this.mBgPaddingScale = this.mBgPadding / this.mDisplayMetrics.widthPixels;
        try {
            i = resources.getDimensionPixelSize(R.dimen.notification_panel_width);
        } catch (Resources.NotFoundException e) {
            i = 0;
        }
        this.mPreviewWidth = i <= 0 ? this.mDisplayMetrics.widthPixels : i;
        this.mPreviewHeight = resources.getDimensionPixelSize(R.dimen.notification_max_height);
        this.mCameraSound = new MediaActionSound();
        this.mCameraSound.load(0);
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
        this.mSaveInBgTask = new SaveImageInBackgroundTask(this.mContext, saveImageInBackgroundData, this.mNotificationManager).execute(new Void[0]);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void takeScreenshot(Runnable runnable, boolean z, boolean z2, Rect rect) {
        this.mScreenBitmap = SurfaceControl.screenshot(rect, rect.width(), rect.height(), this.mDisplay.getRotation());
        if (this.mScreenBitmap == null) {
            notifyScreenshotError(this.mContext, this.mNotificationManager, R.string.screenshot_failed_to_capture_text);
            runnable.run();
            return;
        }
        this.mScreenBitmap.setHasAlpha(false);
        this.mScreenBitmap.prepareToDraw();
        startAnimation(runnable, this.mDisplayMetrics.widthPixels, this.mDisplayMetrics.heightPixels, z, z2);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void takeScreenshot(Runnable runnable, boolean z, boolean z2) {
        this.mDisplay.getRealMetrics(this.mDisplayMetrics);
        takeScreenshot(runnable, z, z2, new Rect(0, 0, this.mDisplayMetrics.widthPixels, this.mDisplayMetrics.heightPixels));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void takeScreenshotPartial(final Runnable runnable, final boolean z, final boolean z2) {
        if (this.mScreenshotLayout.isAttachedToWindow()) {
            return;
        }
        this.mWindowManager.addView(this.mScreenshotLayout, this.mWindowLayoutParams);
        this.mScreenshotSelectorView.setOnTouchListener(new View.OnTouchListener() { // from class: com.android.systemui.screenshot.GlobalScreenshot.2
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view, MotionEvent motionEvent) {
                ScreenshotSelectorView screenshotSelectorView = (ScreenshotSelectorView) view;
                switch (motionEvent.getAction()) {
                    case 0:
                        screenshotSelectorView.startSelection((int) motionEvent.getX(), (int) motionEvent.getY());
                        return true;
                    case 1:
                        screenshotSelectorView.setVisibility(8);
                        GlobalScreenshot.this.mWindowManager.removeView(GlobalScreenshot.this.mScreenshotLayout);
                        final Rect selectionRect = screenshotSelectorView.getSelectionRect();
                        if (selectionRect != null && selectionRect.width() != 0 && selectionRect.height() != 0) {
                            GlobalScreenshot.this.mScreenshotLayout.post(new Runnable() { // from class: com.android.systemui.screenshot.GlobalScreenshot.2.1
                                @Override // java.lang.Runnable
                                public void run() {
                                    GlobalScreenshot.this.takeScreenshot(runnable, z, z2, selectionRect);
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
        });
        this.mScreenshotLayout.post(new Runnable() { // from class: com.android.systemui.screenshot.GlobalScreenshot.3
            @Override // java.lang.Runnable
            public void run() {
                GlobalScreenshot.this.mScreenshotSelectorView.setVisibility(0);
                GlobalScreenshot.this.mScreenshotSelectorView.requestFocus();
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

    private void startAnimation(final Runnable runnable, int i, int i2, boolean z, boolean z2) {
        if (((PowerManager) this.mContext.getSystemService("power")).isPowerSaveMode()) {
            Toast.makeText(this.mContext, (int) R.string.screenshot_saved_title, 0).show();
        }
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
        this.mScreenshotAnimation.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.screenshot.GlobalScreenshot.4
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                GlobalScreenshot.this.saveScreenshotInWorkerThread(runnable);
                GlobalScreenshot.this.mWindowManager.removeView(GlobalScreenshot.this.mScreenshotLayout);
                GlobalScreenshot.this.mScreenBitmap = null;
                GlobalScreenshot.this.mScreenshotView.setImageBitmap(null);
            }
        });
        this.mScreenshotLayout.post(new Runnable() { // from class: com.android.systemui.screenshot.GlobalScreenshot.5
            @Override // java.lang.Runnable
            public void run() {
                GlobalScreenshot.this.mCameraSound.play(0);
                GlobalScreenshot.this.mScreenshotView.setLayerType(2, null);
                GlobalScreenshot.this.mScreenshotView.buildLayer();
                GlobalScreenshot.this.mScreenshotAnimation.start();
            }
        });
    }

    private ValueAnimator createScreenshotDropInAnimation() {
        final Interpolator interpolator = new Interpolator() { // from class: com.android.systemui.screenshot.GlobalScreenshot.6
            @Override // android.animation.TimeInterpolator
            public float getInterpolation(float f) {
                if (f <= 0.60465115f) {
                    return (float) Math.sin(3.141592653589793d * (f / 0.60465115f));
                }
                return 0.0f;
            }
        };
        final Interpolator interpolator2 = new Interpolator() { // from class: com.android.systemui.screenshot.GlobalScreenshot.7
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
        ofFloat.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.screenshot.GlobalScreenshot.8
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                GlobalScreenshot.this.mBackgroundView.setAlpha(0.0f);
                GlobalScreenshot.this.mBackgroundView.setVisibility(0);
                GlobalScreenshot.this.mScreenshotView.setAlpha(0.0f);
                GlobalScreenshot.this.mScreenshotView.setTranslationX(0.0f);
                GlobalScreenshot.this.mScreenshotView.setTranslationY(0.0f);
                GlobalScreenshot.this.mScreenshotView.setScaleX(GlobalScreenshot.this.mBgPaddingScale + 1.0f);
                GlobalScreenshot.this.mScreenshotView.setScaleY(1.0f + GlobalScreenshot.this.mBgPaddingScale);
                GlobalScreenshot.this.mScreenshotView.setVisibility(0);
                GlobalScreenshot.this.mScreenshotFlash.setAlpha(0.0f);
                GlobalScreenshot.this.mScreenshotFlash.setVisibility(0);
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                GlobalScreenshot.this.mScreenshotFlash.setVisibility(8);
            }
        });
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.screenshot.GlobalScreenshot.9
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                float interpolation = (1.0f + GlobalScreenshot.this.mBgPaddingScale) - (interpolator2.getInterpolation(floatValue) * 0.27499998f);
                GlobalScreenshot.this.mBackgroundView.setAlpha(interpolator2.getInterpolation(floatValue) * 0.5f);
                GlobalScreenshot.this.mScreenshotView.setAlpha(floatValue);
                GlobalScreenshot.this.mScreenshotView.setScaleX(interpolation);
                GlobalScreenshot.this.mScreenshotView.setScaleY(interpolation);
                GlobalScreenshot.this.mScreenshotFlash.setAlpha(interpolator.getInterpolation(floatValue));
            }
        });
        return ofFloat;
    }

    private ValueAnimator createScreenshotDropOutAnimation(int i, int i2, boolean z, boolean z2) {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        ofFloat.setStartDelay(500L);
        ofFloat.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.screenshot.GlobalScreenshot.10
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                GlobalScreenshot.this.mBackgroundView.setVisibility(8);
                GlobalScreenshot.this.mScreenshotView.setVisibility(8);
                GlobalScreenshot.this.mScreenshotView.setLayerType(0, null);
            }
        });
        if (!z || !z2) {
            ofFloat.setDuration(320L);
            ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.screenshot.GlobalScreenshot.11
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                    float f = (0.725f + GlobalScreenshot.this.mBgPaddingScale) - (0.125f * floatValue);
                    float f2 = 1.0f - floatValue;
                    GlobalScreenshot.this.mBackgroundView.setAlpha(0.5f * f2);
                    GlobalScreenshot.this.mScreenshotView.setAlpha(f2);
                    GlobalScreenshot.this.mScreenshotView.setScaleX(f);
                    GlobalScreenshot.this.mScreenshotView.setScaleY(f);
                }
            });
        } else {
            final Interpolator interpolator = new Interpolator() { // from class: com.android.systemui.screenshot.GlobalScreenshot.12
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
            final PointF pointF = new PointF((-f) + (f * 0.45f), (-f2) + (0.45f * f2));
            ofFloat.setDuration(430L);
            ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.screenshot.GlobalScreenshot.13
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                    float interpolation = (0.725f + GlobalScreenshot.this.mBgPaddingScale) - (interpolator.getInterpolation(floatValue) * 0.27500004f);
                    GlobalScreenshot.this.mBackgroundView.setAlpha((1.0f - floatValue) * 0.5f);
                    GlobalScreenshot.this.mScreenshotView.setAlpha(1.0f - interpolator.getInterpolation(floatValue));
                    GlobalScreenshot.this.mScreenshotView.setScaleX(interpolation);
                    GlobalScreenshot.this.mScreenshotView.setScaleY(interpolation);
                    GlobalScreenshot.this.mScreenshotView.setTranslationX(pointF.x * floatValue);
                    GlobalScreenshot.this.mScreenshotView.setTranslationY(floatValue * pointF.y);
                }
            });
        }
        return ofFloat;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void notifyScreenshotError(Context context, NotificationManager notificationManager, int i) {
        Resources resources = context.getResources();
        String string = resources.getString(i);
        Notification.Builder color = new Notification.Builder(context, NotificationChannels.ALERTS).setTicker(resources.getString(R.string.screenshot_failed_title)).setContentTitle(resources.getString(R.string.screenshot_failed_title)).setContentText(string).setSmallIcon(R.drawable.stat_notify_image_error).setWhen(System.currentTimeMillis()).setVisibility(1).setCategory("err").setAutoCancel(true).setColor(context.getColor(17170774));
        Intent createAdminSupportIntent = ((DevicePolicyManager) context.getSystemService("device_policy")).createAdminSupportIntent("policy_disable_screen_capture");
        if (createAdminSupportIntent != null) {
            color.setContentIntent(PendingIntent.getActivityAsUser(context, 0, createAdminSupportIntent, 67108864, null, UserHandle.CURRENT));
        }
        SystemUI.overrideNotificationAppName(context, color, true);
        notificationManager.notify(1, new Notification.BigTextStyle(color).bigText(string).build());
    }

    /* loaded from: classes.dex */
    public static class ScreenshotActionReceiver extends BroadcastReceiver {
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            try {
                ActivityManager.getService().closeSystemDialogs("screenshot");
            } catch (RemoteException e) {
            }
            Intent intent2 = (Intent) intent.getParcelableExtra("android:screenshot_sharing_intent");
            String string = context.getResources().getString(R.string.config_screenshotEditor);
            if (intent2.getAction() == "android.intent.action.EDIT" && string != null && string.length() > 0) {
                intent2.setComponent(ComponentName.unflattenFromString(string));
                ((NotificationManager) context.getSystemService("notification")).cancel(1);
            } else {
                intent2 = Intent.createChooser(intent2, null, PendingIntent.getBroadcast(context, 0, new Intent(context, TargetChosenReceiver.class), 1342177280).getIntentSender()).addFlags(268468224);
            }
            ActivityOptions makeBasic = ActivityOptions.makeBasic();
            makeBasic.setDisallowEnterPictureInPictureWhileLaunching(true);
            context.startActivityAsUser(intent2, makeBasic.toBundle(), UserHandle.CURRENT);
        }
    }

    /* loaded from: classes.dex */
    public static class TargetChosenReceiver extends BroadcastReceiver {
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            ((NotificationManager) context.getSystemService("notification")).cancel(1);
        }
    }

    /* loaded from: classes.dex */
    public static class DeleteScreenshotReceiver extends BroadcastReceiver {
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (!intent.hasExtra("android:screenshot_uri_id")) {
                return;
            }
            Uri parse = Uri.parse(intent.getStringExtra("android:screenshot_uri_id"));
            ((NotificationManager) context.getSystemService("notification")).cancel(1);
            new DeleteImageInBackgroundTask(context).execute(parse);
        }
    }
}

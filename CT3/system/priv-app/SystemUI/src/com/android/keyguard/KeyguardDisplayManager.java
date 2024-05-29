package com.android.keyguard;

import android.app.Presentation;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.media.MediaRouter;
import android.os.Bundle;
import android.util.Slog;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
/* loaded from: a.zip:com/android/keyguard/KeyguardDisplayManager.class */
public class KeyguardDisplayManager {
    private static boolean DEBUG = true;
    private Context mContext;
    private MediaRouter mMediaRouter;
    private final MediaRouter.SimpleCallback mMediaRouterCallback = new MediaRouter.SimpleCallback(this) { // from class: com.android.keyguard.KeyguardDisplayManager.1
        final KeyguardDisplayManager this$0;

        {
            this.this$0 = this;
        }

        @Override // android.media.MediaRouter.Callback
        public void onRoutePresentationDisplayChanged(MediaRouter mediaRouter, MediaRouter.RouteInfo routeInfo) {
            if (KeyguardDisplayManager.DEBUG) {
                Slog.d("KeyguardDisplayManager", "onRoutePresentationDisplayChanged: info=" + routeInfo);
            }
            this.this$0.updateDisplays(this.this$0.mShowing);
        }

        @Override // android.media.MediaRouter.SimpleCallback, android.media.MediaRouter.Callback
        public void onRouteSelected(MediaRouter mediaRouter, int i, MediaRouter.RouteInfo routeInfo) {
            if (KeyguardDisplayManager.DEBUG) {
                Slog.d("KeyguardDisplayManager", "onRouteSelected: type=" + i + ", info=" + routeInfo);
            }
            this.this$0.updateDisplays(this.this$0.mShowing);
        }

        @Override // android.media.MediaRouter.SimpleCallback, android.media.MediaRouter.Callback
        public void onRouteUnselected(MediaRouter mediaRouter, int i, MediaRouter.RouteInfo routeInfo) {
            if (KeyguardDisplayManager.DEBUG) {
                Slog.d("KeyguardDisplayManager", "onRouteUnselected: type=" + i + ", info=" + routeInfo);
            }
            this.this$0.updateDisplays(this.this$0.mShowing);
        }
    };
    private DialogInterface.OnDismissListener mOnDismissListener = new DialogInterface.OnDismissListener(this) { // from class: com.android.keyguard.KeyguardDisplayManager.2
        final KeyguardDisplayManager this$0;

        {
            this.this$0 = this;
        }

        @Override // android.content.DialogInterface.OnDismissListener
        public void onDismiss(DialogInterface dialogInterface) {
            this.this$0.mPresentation = null;
        }
    };
    Presentation mPresentation;
    private boolean mShowing;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/keyguard/KeyguardDisplayManager$KeyguardPresentation.class */
    public static final class KeyguardPresentation extends Presentation {
        private View mClock;
        private int mMarginLeft;
        private int mMarginTop;
        Runnable mMoveTextRunnable;
        private int mUsableHeight;
        private int mUsableWidth;

        public KeyguardPresentation(Context context, Display display, int i) {
            super(context, display, i);
            this.mMoveTextRunnable = new Runnable(this) { // from class: com.android.keyguard.KeyguardDisplayManager.KeyguardPresentation.1
                final KeyguardPresentation this$1;

                {
                    this.this$1 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    int random = this.this$1.mMarginLeft + ((int) (Math.random() * (this.this$1.mUsableWidth - this.this$1.mClock.getWidth())));
                    int random2 = this.this$1.mMarginTop + ((int) (Math.random() * (this.this$1.mUsableHeight - this.this$1.mClock.getHeight())));
                    if (KeyguardDisplayManager.DEBUG) {
                        Slog.d("KeyguardDisplayManager", "mMarginLeft = " + this.this$1.mMarginLeft + ", mUsableWidth = " + this.this$1.mUsableWidth + " , mClock.getWidth() = " + this.this$1.mClock.getWidth() + " and final X = " + random);
                        Slog.d("KeyguardDisplayManager", "mMarginTop = " + this.this$1.mMarginTop + ", mUsableHeight = " + this.this$1.mUsableHeight + " , mClock.getHeight() = " + this.this$1.mClock.getHeight() + " and final y = " + random2);
                    }
                    this.this$1.mClock.setX(random);
                    this.this$1.mClock.setY(random2);
                    this.this$1.mClock.postDelayed(this.this$1.mMoveTextRunnable, 10000L);
                }
            };
            getWindow().setType(2009);
        }

        @Override // android.app.Dialog
        protected void onCreate(Bundle bundle) {
            super.onCreate(bundle);
            Point point = new Point();
            getDisplay().getSize(point);
            this.mUsableWidth = (point.x * 80) / 100;
            this.mUsableHeight = (point.y * 80) / 100;
            this.mMarginLeft = (point.x * 20) / 200;
            this.mMarginTop = (point.y * 20) / 200;
            setContentView(R$layout.keyguard_presentation);
            this.mClock = findViewById(R$id.clock);
            this.mClock.post(this.mMoveTextRunnable);
        }

        @Override // android.app.Dialog, android.view.Window.Callback
        public void onDetachedFromWindow() {
            this.mClock.removeCallbacks(this.mMoveTextRunnable);
        }
    }

    public KeyguardDisplayManager(Context context) {
        this.mContext = context;
        this.mMediaRouter = (MediaRouter) this.mContext.getSystemService("media_router");
    }

    public void hide() {
        if (this.mShowing) {
            if (DEBUG) {
                Slog.v("KeyguardDisplayManager", "hide");
            }
            this.mMediaRouter.removeCallback(this.mMediaRouterCallback);
            updateDisplays(false);
        }
        this.mShowing = false;
    }

    public void show() {
        if (!this.mShowing) {
            if (DEBUG) {
                Slog.v("KeyguardDisplayManager", "show");
            }
            this.mMediaRouter.addCallback(4, this.mMediaRouterCallback, 8);
            updateDisplays(true);
        }
        this.mShowing = true;
    }

    protected void updateDisplays(boolean z) {
        boolean z2 = true;
        if (!z) {
            if (this.mPresentation != null) {
                this.mPresentation.dismiss();
                this.mPresentation = null;
                return;
            }
            return;
        }
        MediaRouter.RouteInfo selectedRoute = this.mMediaRouter.getSelectedRoute(4);
        if (selectedRoute == null) {
            z2 = false;
        } else if (selectedRoute.getPlaybackType() != 1) {
            z2 = false;
        }
        Display presentationDisplay = z2 ? selectedRoute.getPresentationDisplay() : null;
        if (this.mPresentation != null && this.mPresentation.getDisplay() != presentationDisplay) {
            if (DEBUG) {
                Slog.v("KeyguardDisplayManager", "Display gone: " + this.mPresentation.getDisplay());
            }
            this.mPresentation.dismiss();
            this.mPresentation = null;
        }
        if (this.mPresentation != null || presentationDisplay == null) {
            return;
        }
        if (DEBUG) {
            Slog.i("KeyguardDisplayManager", "Keyguard enabled on display: " + presentationDisplay);
        }
        this.mPresentation = new KeyguardPresentation(this.mContext, presentationDisplay, R$style.keyguard_presentation_theme);
        this.mPresentation.setOnDismissListener(this.mOnDismissListener);
        try {
            this.mPresentation.show();
        } catch (WindowManager.InvalidDisplayException e) {
            Slog.w("KeyguardDisplayManager", "Invalid display:", e);
            this.mPresentation = null;
        }
    }
}

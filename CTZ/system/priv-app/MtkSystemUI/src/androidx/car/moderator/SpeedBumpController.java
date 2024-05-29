package androidx.car.moderator;

import android.car.Car;
import android.car.CarNotConnectedException;
import android.car.drivingstate.CarUxRestrictions;
import android.car.drivingstate.CarUxRestrictionsManager;
import android.car.settings.CarConfigurationManager;
import android.car.settings.SpeedBumpConfiguration;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import androidx.car.R;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class SpeedBumpController {
    private final Car mCar;
    private CarUxRestrictionsManager mCarUxRestrictionsManager;
    private final Context mContext;
    private final int mLockOutMessageDurationMs;
    private final ImageView mLockoutImageView;
    private final View mLockoutMessageView;
    private final ContentRateLimiter mContentRateLimiter = new ContentRateLimiter(0.5d, 5.0d, 600);
    private boolean mInteractionPermitted = true;
    private final Handler mHandler = new Handler();
    private final ServiceConnection mServiceConnection = new AnonymousClass3();

    /* JADX INFO: Access modifiers changed from: package-private */
    public SpeedBumpController(SpeedBumpView speedBumpView) {
        this.mContext = speedBumpView.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(this.mContext);
        this.mLockoutMessageView = layoutInflater.inflate(R.layout.lock_out_message, (ViewGroup) speedBumpView, false);
        this.mLockoutImageView = (ImageView) this.mLockoutMessageView.findViewById(R.id.lock_out_drawable);
        this.mLockOutMessageDurationMs = this.mContext.getResources().getInteger(R.integer.speed_bump_lock_out_duration_ms);
        this.mCar = Car.createCar(this.mContext, this.mServiceConnection);
        this.mContentRateLimiter.setUnlimitedMode(true);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void start() {
        try {
            if (this.mCar != null && !this.mCar.isConnected()) {
                this.mCar.connect();
            }
        } catch (IllegalStateException e) {
            Log.w("SpeedBumpController", "start(); cannot connect to Car");
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void stop() {
        if (this.mCarUxRestrictionsManager != null) {
            try {
                this.mCarUxRestrictionsManager.unregisterListener();
            } catch (CarNotConnectedException e) {
                Log.w("SpeedBumpController", "stop(); cannot unregister listener.");
            }
            this.mCarUxRestrictionsManager = null;
        }
        try {
            if (this.mCar != null && this.mCar.isConnected()) {
                this.mCar.disconnect();
            }
        } catch (IllegalStateException e2) {
            Log.w("SpeedBumpController", "stop(); cannot disconnect from Car.");
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public View getLockoutMessageView() {
        return this.mLockoutMessageView;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getActionMasked();
        if (action == 3 || action == 1) {
            boolean nextActionPermitted = this.mContentRateLimiter.tryAcquire();
            if (this.mInteractionPermitted && !nextActionPermitted) {
                this.mInteractionPermitted = false;
                showLockOutMessage();
                return true;
            }
        }
        return this.mInteractionPermitted;
    }

    private void showLockOutMessage() {
        if (this.mLockoutMessageView.getVisibility() == 0) {
            return;
        }
        Animation lockOutMessageIn = AnimationUtils.loadAnimation(this.mContext, R.anim.lock_out_message_in);
        lockOutMessageIn.setAnimationListener(new AnonymousClass1());
        this.mLockoutMessageView.clearAnimation();
        this.mLockoutMessageView.startAnimation(lockOutMessageIn);
        ((AnimatedVectorDrawable) this.mLockoutImageView.getDrawable()).start();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: androidx.car.moderator.SpeedBumpController$1  reason: invalid class name */
    /* loaded from: classes.dex */
    public class AnonymousClass1 implements Animation.AnimationListener {
        AnonymousClass1() {
        }

        @Override // android.view.animation.Animation.AnimationListener
        public void onAnimationStart(Animation animation) {
            SpeedBumpController.this.mLockoutMessageView.setVisibility(0);
        }

        @Override // android.view.animation.Animation.AnimationListener
        public void onAnimationEnd(Animation animation) {
            Handler handler = SpeedBumpController.this.mHandler;
            final SpeedBumpController speedBumpController = SpeedBumpController.this;
            handler.postDelayed(new Runnable() { // from class: androidx.car.moderator.-$$Lambda$SpeedBumpController$1$JWqfApEE4LaLtMDQ0Yg16o_M7TE
                @Override // java.lang.Runnable
                public final void run() {
                    SpeedBumpController.this.hideLockOutMessage();
                }
            }, SpeedBumpController.this.mLockOutMessageDurationMs);
        }

        @Override // android.view.animation.Animation.AnimationListener
        public void onAnimationRepeat(Animation animation) {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void hideLockOutMessage() {
        if (this.mLockoutMessageView.getVisibility() != 0) {
            return;
        }
        Animation lockOutMessageOut = AnimationUtils.loadAnimation(this.mContext, R.anim.lock_out_message_out);
        lockOutMessageOut.setAnimationListener(new Animation.AnimationListener() { // from class: androidx.car.moderator.SpeedBumpController.2
            @Override // android.view.animation.Animation.AnimationListener
            public void onAnimationStart(Animation animation) {
            }

            @Override // android.view.animation.Animation.AnimationListener
            public void onAnimationEnd(Animation animation) {
                SpeedBumpController.this.mLockoutMessageView.setVisibility(8);
                SpeedBumpController.this.mInteractionPermitted = true;
            }

            @Override // android.view.animation.Animation.AnimationListener
            public void onAnimationRepeat(Animation animation) {
            }
        });
        this.mLockoutMessageView.startAnimation(lockOutMessageOut);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateUnlimitedModeEnabled(CarUxRestrictions restrictions) {
        this.mContentRateLimiter.setUnlimitedMode(!restrictions.isRequiresDistractionOptimization());
    }

    /* renamed from: androidx.car.moderator.SpeedBumpController$3  reason: invalid class name */
    /* loaded from: classes.dex */
    class AnonymousClass3 implements ServiceConnection {
        AnonymousClass3() {
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                SpeedBumpController.this.mCarUxRestrictionsManager = (CarUxRestrictionsManager) SpeedBumpController.this.mCar.getCarManager("uxrestriction");
                CarUxRestrictionsManager carUxRestrictionsManager = SpeedBumpController.this.mCarUxRestrictionsManager;
                final SpeedBumpController speedBumpController = SpeedBumpController.this;
                carUxRestrictionsManager.registerListener(new CarUxRestrictionsManager.OnUxRestrictionsChangedListener() { // from class: androidx.car.moderator.-$$Lambda$SpeedBumpController$3$DiJB13knXa5lry4ASBohbIcj-zc
                    public final void onUxRestrictionsChanged(CarUxRestrictions carUxRestrictions) {
                        SpeedBumpController.this.updateUnlimitedModeEnabled(carUxRestrictions);
                    }
                });
                SpeedBumpController.this.updateUnlimitedModeEnabled(SpeedBumpController.this.mCarUxRestrictionsManager.getCurrentCarUxRestrictions());
                CarConfigurationManager configManager = (CarConfigurationManager) SpeedBumpController.this.mCar.getCarManager("configuration");
                SpeedBumpConfiguration speedBumpConfiguration = configManager.getSpeedBumpConfiguration();
                SpeedBumpController.this.mContentRateLimiter.setAcquiredPermitsRate(speedBumpConfiguration.getAcquiredPermitsPerSecond());
                SpeedBumpController.this.mContentRateLimiter.setMaxStoredPermits(speedBumpConfiguration.getMaxPermitPool());
                SpeedBumpController.this.mContentRateLimiter.setPermitFillDelay(speedBumpConfiguration.getPermitFillDelay());
            } catch (CarNotConnectedException e) {
                e.printStackTrace();
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            SpeedBumpController.this.mCarUxRestrictionsManager = null;
        }
    }
}

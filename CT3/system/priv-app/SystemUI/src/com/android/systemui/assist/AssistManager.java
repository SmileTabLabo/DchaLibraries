package com.android.systemui.assist;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import com.android.internal.app.AssistUtils;
import com.android.internal.app.IVoiceInteractionSessionShowCallback;
import com.android.systemui.statusbar.BaseStatusBar;
/* loaded from: a.zip:com/android/systemui/assist/AssistManager.class */
public class AssistManager {
    private final AssistDisclosure mAssistDisclosure;
    private final AssistUtils mAssistUtils;
    private final BaseStatusBar mBar;
    private final Context mContext;
    private AssistOrbContainer mView;
    private final WindowManager mWindowManager;
    private IVoiceInteractionSessionShowCallback mShowCallback = new IVoiceInteractionSessionShowCallback.Stub(this) { // from class: com.android.systemui.assist.AssistManager.1
        final AssistManager this$0;

        {
            this.this$0 = this;
        }

        public void onFailed() throws RemoteException {
            this.this$0.mView.post(this.this$0.mHideRunnable);
        }

        public void onShown() throws RemoteException {
            this.this$0.mView.post(this.this$0.mHideRunnable);
        }
    };
    private Runnable mHideRunnable = new Runnable(this) { // from class: com.android.systemui.assist.AssistManager.2
        final AssistManager this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            this.this$0.mView.removeCallbacks(this);
            this.this$0.mView.show(false, true);
        }
    };

    public AssistManager(BaseStatusBar baseStatusBar, Context context) {
        this.mContext = context;
        this.mBar = baseStatusBar;
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        this.mAssistUtils = new AssistUtils(context);
        this.mAssistDisclosure = new AssistDisclosure(context, new Handler());
    }

    private ComponentName getAssistInfo() {
        return this.mAssistUtils.getAssistComponentForUser(-2);
    }

    private WindowManager.LayoutParams getLayoutParams() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-1, this.mContext.getResources().getDimensionPixelSize(2131689931), 2033, 280, -3);
        if (ActivityManager.isHighEndGfx()) {
            layoutParams.flags |= 16777216;
        }
        layoutParams.gravity = 8388691;
        layoutParams.setTitle("AssistPreviewPanel");
        layoutParams.softInputMode = 49;
        return layoutParams;
    }

    private boolean isVoiceSessionRunning() {
        return this.mAssistUtils.isSessionRunning();
    }

    private void maybeSwapSearchIcon(ComponentName componentName, boolean z) {
        replaceDrawable(this.mView.getOrb().getLogo(), componentName, "com.android.systemui.action_assist_icon", z);
    }

    private void showOrb(ComponentName componentName, boolean z) {
        maybeSwapSearchIcon(componentName, z);
        this.mView.show(true, true);
    }

    private void startAssistActivity(Bundle bundle, ComponentName componentName) {
        if (this.mBar.isDeviceProvisioned()) {
            this.mBar.animateCollapsePanels(3);
            boolean z = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "assist_structure_enabled", 1, -2) != 0;
            Intent assistIntent = ((SearchManager) this.mContext.getSystemService("search")).getAssistIntent(z);
            if (assistIntent == null) {
                return;
            }
            assistIntent.setComponent(componentName);
            assistIntent.putExtras(bundle);
            if (z) {
                showDisclosure();
            }
            try {
                ActivityOptions makeCustomAnimation = ActivityOptions.makeCustomAnimation(this.mContext, 2131034300, 2131034301);
                assistIntent.addFlags(268435456);
                AsyncTask.execute(new Runnable(this, assistIntent, makeCustomAnimation) { // from class: com.android.systemui.assist.AssistManager.3
                    final AssistManager this$0;
                    final Intent val$intent;
                    final ActivityOptions val$opts;

                    {
                        this.this$0 = this;
                        this.val$intent = assistIntent;
                        this.val$opts = makeCustomAnimation;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        this.this$0.mContext.startActivityAsUser(this.val$intent, this.val$opts.toBundle(), new UserHandle(-2));
                    }
                });
            } catch (ActivityNotFoundException e) {
                Log.w("AssistManager", "Activity not found for " + assistIntent.getAction());
            }
        }
    }

    private void startAssistInternal(Bundle bundle, ComponentName componentName, boolean z) {
        if (z) {
            startVoiceInteractor(bundle);
        } else {
            startAssistActivity(bundle, componentName);
        }
    }

    private void startVoiceInteractor(Bundle bundle) {
        this.mAssistUtils.showSessionForActiveService(bundle, 4, this.mShowCallback, (IBinder) null);
    }

    public boolean canVoiceAssistBeLaunchedFromKeyguard() {
        return this.mAssistUtils.activeServiceSupportsLaunchFromKeyguard();
    }

    public void destroy() {
        this.mWindowManager.removeViewImmediate(this.mView);
    }

    public ComponentName getVoiceInteractorComponentName() {
        return this.mAssistUtils.getActiveServiceComponentName();
    }

    public void hideAssist() {
        this.mAssistUtils.hideCurrentSession();
    }

    public void launchVoiceAssistFromKeyguard() {
        this.mAssistUtils.launchVoiceAssistFromKeyguard();
    }

    public void onConfigurationChanged() {
        boolean z = false;
        if (this.mView != null) {
            z = this.mView.isShowing();
            this.mWindowManager.removeView(this.mView);
        }
        this.mView = (AssistOrbContainer) LayoutInflater.from(this.mContext).inflate(2130968602, (ViewGroup) null);
        this.mView.setVisibility(8);
        this.mView.setSystemUiVisibility(1792);
        this.mWindowManager.addView(this.mView, getLayoutParams());
        if (z) {
            this.mView.show(true, false);
        }
    }

    public void onLockscreenShown() {
        this.mAssistUtils.onLockscreenShown();
    }

    public void replaceDrawable(ImageView imageView, ComponentName componentName, String str, boolean z) {
        int i;
        if (componentName != null) {
            try {
                PackageManager packageManager = this.mContext.getPackageManager();
                Bundle bundle = z ? packageManager.getServiceInfo(componentName, 128).metaData : packageManager.getActivityInfo(componentName, 128).metaData;
                if (bundle != null && (i = bundle.getInt(str)) != 0) {
                    imageView.setImageDrawable(packageManager.getResourcesForApplication(componentName.getPackageName()).getDrawable(i));
                    return;
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.v("AssistManager", "Assistant component " + componentName.flattenToShortString() + " not found");
            } catch (Resources.NotFoundException e2) {
                Log.w("AssistManager", "Failed to swap drawable from " + componentName.flattenToShortString(), e2);
            }
        }
        imageView.setImageDrawable(null);
    }

    public void showDisclosure() {
        this.mAssistDisclosure.postShow();
    }

    public void startAssist(Bundle bundle) {
        ComponentName assistInfo = getAssistInfo();
        if (assistInfo == null) {
            return;
        }
        boolean equals = assistInfo.equals(getVoiceInteractorComponentName());
        if (!equals || !isVoiceSessionRunning()) {
            showOrb(assistInfo, equals);
            this.mView.postDelayed(this.mHideRunnable, equals ? 2500L : 1000L);
        }
        startAssistInternal(bundle, assistInfo, equals);
    }
}

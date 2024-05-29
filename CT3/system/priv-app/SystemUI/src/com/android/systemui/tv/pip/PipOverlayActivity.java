package com.android.systemui.tv.pip;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import com.android.systemui.tv.pip.PipManager;
/* loaded from: a.zip:com/android/systemui/tv/pip/PipOverlayActivity.class */
public class PipOverlayActivity extends Activity implements PipManager.Listener {
    private static boolean sActivityCreated;
    private Animator mFadeInAnimation;
    private Animator mFadeOutAnimation;
    private View mGuideOverlayView;
    private final PipManager mPipManager = PipManager.getInstance();
    private final Handler mHandler = new Handler();
    private final Runnable mHideGuideOverlayRunnable = new Runnable(this) { // from class: com.android.systemui.tv.pip.PipOverlayActivity.1
        final PipOverlayActivity this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            this.this$0.mFadeOutAnimation.start();
        }
    };

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void showPipOverlay(Context context) {
        if (sActivityCreated) {
            return;
        }
        Intent intent = new Intent(context, PipOverlayActivity.class);
        intent.setFlags(268435456);
        ActivityOptions makeBasic = ActivityOptions.makeBasic();
        makeBasic.setLaunchStackId(4);
        context.startActivity(intent, makeBasic.toBundle());
    }

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        sActivityCreated = true;
        setContentView(2130968829);
        this.mGuideOverlayView = findViewById(2131886737);
        this.mPipManager.addListener(this);
        this.mFadeInAnimation = AnimatorInflater.loadAnimator(this, 2131034326);
        this.mFadeInAnimation.setTarget(this.mGuideOverlayView);
        this.mFadeOutAnimation = AnimatorInflater.loadAnimator(this, 2131034327);
        this.mFadeOutAnimation.setTarget(this.mGuideOverlayView);
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        sActivityCreated = false;
        this.mHandler.removeCallbacksAndMessages(null);
        this.mPipManager.removeListener(this);
        this.mPipManager.resumePipResizing(2);
    }

    @Override // com.android.systemui.tv.pip.PipManager.Listener
    public void onMoveToFullscreen() {
        finish();
    }

    @Override // com.android.systemui.tv.pip.PipManager.Listener
    public void onPipActivityClosed() {
        finish();
    }

    @Override // com.android.systemui.tv.pip.PipManager.Listener
    public void onPipEntered() {
    }

    @Override // com.android.systemui.tv.pip.PipManager.Listener
    public void onPipResizeAboutToStart() {
        finish();
        this.mPipManager.suspendPipResizing(2);
    }

    @Override // android.app.Activity
    protected void onResume() {
        super.onResume();
        this.mFadeInAnimation.start();
        this.mHandler.removeCallbacks(this.mHideGuideOverlayRunnable);
        this.mHandler.postDelayed(this.mHideGuideOverlayRunnable, 4000L);
    }

    @Override // com.android.systemui.tv.pip.PipManager.Listener
    public void onShowPipMenu() {
        finish();
    }

    @Override // android.app.Activity
    protected void onStop() {
        super.onStop();
        this.mHandler.removeCallbacks(this.mHideGuideOverlayRunnable);
        finish();
    }
}

package com.android.systemui.tv.pip;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import com.android.systemui.tv.pip.PipManager;
/* loaded from: a.zip:com/android/systemui/tv/pip/PipMenuActivity.class */
public class PipMenuActivity extends Activity implements PipManager.Listener {
    private Animator mFadeInAnimation;
    private Animator mFadeOutAnimation;
    private View mPipControlsView;
    private final PipManager mPipManager = PipManager.getInstance();
    private boolean mRestorePipSizeWhenClose;

    private void restorePipAndFinish() {
        if (this.mRestorePipSizeWhenClose) {
            this.mPipManager.resizePinnedStack(1);
        }
        finish();
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        restorePipAndFinish();
    }

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(2130968827);
        this.mPipManager.addListener(this);
        this.mRestorePipSizeWhenClose = true;
        this.mPipControlsView = (PipControlsView) findViewById(2131886733);
        this.mFadeInAnimation = AnimatorInflater.loadAnimator(this, 2131034319);
        this.mFadeInAnimation.setTarget(this.mPipControlsView);
        this.mFadeOutAnimation = AnimatorInflater.loadAnimator(this, 2131034320);
        this.mFadeOutAnimation.setTarget(this.mPipControlsView);
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        this.mPipManager.removeListener(this);
        this.mPipManager.resumePipResizing(1);
    }

    @Override // com.android.systemui.tv.pip.PipManager.Listener
    public void onMoveToFullscreen() {
        this.mRestorePipSizeWhenClose = false;
        finish();
    }

    @Override // android.app.Activity
    public void onPause() {
        super.onPause();
        this.mFadeOutAnimation.start();
        restorePipAndFinish();
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
        this.mPipManager.suspendPipResizing(1);
    }

    @Override // android.app.Activity
    public void onResume() {
        super.onResume();
        this.mFadeInAnimation.start();
    }

    @Override // com.android.systemui.tv.pip.PipManager.Listener
    public void onShowPipMenu() {
    }
}

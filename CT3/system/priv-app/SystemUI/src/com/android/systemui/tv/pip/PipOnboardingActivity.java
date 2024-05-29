package com.android.systemui.tv.pip;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v17.leanback.R$id;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import com.android.systemui.tv.pip.PipManager;
/* loaded from: a.zip:com/android/systemui/tv/pip/PipOnboardingActivity.class */
public class PipOnboardingActivity extends Activity implements PipManager.Listener {
    private AnimatorSet mEnterAnimator;
    private final PipManager mPipManager = PipManager.getInstance();

    private Animator loadAnimator(int i, int i2) {
        Animator loadAnimator = AnimatorInflater.loadAnimator(this, i2);
        loadAnimator.setTarget(findViewById(i));
        return loadAnimator;
    }

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(2130968828);
        findViewById(2131886368).setOnClickListener(new View.OnClickListener(this) { // from class: com.android.systemui.tv.pip.PipOnboardingActivity.1
            final PipOnboardingActivity this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                this.this$0.finish();
            }
        });
        this.mPipManager.addListener(this);
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        this.mPipManager.removeListener(this);
    }

    @Override // android.app.Activity, android.view.KeyEvent.Callback
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        if (this.mEnterAnimator.isStarted()) {
            return true;
        }
        return super.onKeyDown(i, keyEvent);
    }

    @Override // android.app.Activity, android.view.KeyEvent.Callback
    public boolean onKeyUp(int i, KeyEvent keyEvent) {
        if (this.mEnterAnimator.isStarted()) {
            return true;
        }
        return super.onKeyUp(i, keyEvent);
    }

    @Override // com.android.systemui.tv.pip.PipManager.Listener
    public void onMoveToFullscreen() {
        finish();
    }

    @Override // android.app.Activity
    public void onPause() {
        super.onPause();
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
    }

    @Override // android.app.Activity
    public void onResume() {
        super.onResume();
        this.mEnterAnimator = new AnimatorSet();
        this.mEnterAnimator.playTogether(loadAnimator(2131886455, 2131034321), loadAnimator(2131886735, 2131034324), loadAnimator(2131886736, 2131034324), loadAnimator(2131886212, 2131034325), loadAnimator(R$id.description, 2131034323), loadAnimator(2131886368, 2131034322));
        this.mEnterAnimator.addListener(new AnimatorListenerAdapter(this) { // from class: com.android.systemui.tv.pip.PipOnboardingActivity.2
            final PipOnboardingActivity this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                ((AnimationDrawable) ((ImageView) this.this$0.findViewById(2131886736)).getDrawable()).start();
            }
        });
        this.mEnterAnimator.setStartDelay(getResources().getInteger(2131755100));
        this.mEnterAnimator.start();
    }

    @Override // com.android.systemui.tv.pip.PipManager.Listener
    public void onShowPipMenu() {
        finish();
    }
}

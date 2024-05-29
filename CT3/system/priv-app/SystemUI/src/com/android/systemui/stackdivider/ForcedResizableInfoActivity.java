package com.android.systemui.stackdivider;

import android.app.Activity;
import android.app.ActivityManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
/* loaded from: a.zip:com/android/systemui/stackdivider/ForcedResizableInfoActivity.class */
public class ForcedResizableInfoActivity extends Activity implements View.OnTouchListener {
    private final Runnable mFinishRunnable = new Runnable(this) { // from class: com.android.systemui.stackdivider.ForcedResizableInfoActivity.1
        final ForcedResizableInfoActivity this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            this.this$0.finish();
        }
    };

    @Override // android.app.Activity
    public void finish() {
        super.finish();
        overridePendingTransition(0, 2131034140);
    }

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(2130968618);
        ((TextView) findViewById(16908299)).setText(2131493903);
        getWindow().setTitle(getString(2131493903));
        getWindow().getDecorView().setOnTouchListener(this);
    }

    @Override // android.app.Activity, android.view.KeyEvent.Callback
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        finish();
        return true;
    }

    @Override // android.app.Activity
    protected void onStart() {
        super.onStart();
        getWindow().getDecorView().postDelayed(this.mFinishRunnable, 2500L);
    }

    @Override // android.app.Activity
    protected void onStop() {
        super.onStop();
        finish();
    }

    @Override // android.view.View.OnTouchListener
    public boolean onTouch(View view, MotionEvent motionEvent) {
        finish();
        return true;
    }

    @Override // android.app.Activity
    public void setTaskDescription(ActivityManager.TaskDescription taskDescription) {
    }
}

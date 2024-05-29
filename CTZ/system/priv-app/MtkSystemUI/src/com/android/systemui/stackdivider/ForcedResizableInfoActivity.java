package com.android.systemui.stackdivider;

import android.app.Activity;
import android.app.ActivityManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import com.android.systemui.R;
/* loaded from: classes.dex */
public class ForcedResizableInfoActivity extends Activity implements View.OnTouchListener {
    private final Runnable mFinishRunnable = new Runnable() { // from class: com.android.systemui.stackdivider.ForcedResizableInfoActivity.1
        @Override // java.lang.Runnable
        public void run() {
            ForcedResizableInfoActivity.this.finish();
        }
    };

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        String string;
        super.onCreate(bundle);
        setContentView(R.layout.forced_resizable_activity);
        TextView textView = (TextView) findViewById(16908299);
        int intExtra = getIntent().getIntExtra("extra_forced_resizeable_reason", -1);
        switch (intExtra) {
            case 1:
                string = getString(R.string.dock_forced_resizable);
                break;
            case 2:
                string = getString(R.string.forced_resizable_secondary_display);
                break;
            default:
                throw new IllegalArgumentException("Unexpected forced resizeable reason: " + intExtra);
        }
        textView.setText(string);
        getWindow().setTitle(string);
        getWindow().getDecorView().setOnTouchListener(this);
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

    @Override // android.app.Activity, android.view.KeyEvent.Callback
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        finish();
        return true;
    }

    @Override // android.app.Activity
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.forced_resizable_exit);
    }

    @Override // android.app.Activity
    public void setTaskDescription(ActivityManager.TaskDescription taskDescription) {
    }
}

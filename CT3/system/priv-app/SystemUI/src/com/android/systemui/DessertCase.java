package com.android.systemui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.util.Slog;
import com.android.systemui.DessertCaseView;
/* loaded from: a.zip:com/android/systemui/DessertCase.class */
public class DessertCase extends Activity {
    DessertCaseView mView;

    @Override // android.app.Activity
    public void onPause() {
        super.onPause();
        this.mView.stop();
    }

    @Override // android.app.Activity
    public void onResume() {
        super.onResume();
        this.mView.postDelayed(new Runnable(this) { // from class: com.android.systemui.DessertCase.1
            final DessertCase this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.mView.start();
            }
        }, 1000L);
    }

    @Override // android.app.Activity
    public void onStart() {
        super.onStart();
        PackageManager packageManager = getPackageManager();
        ComponentName componentName = new ComponentName(this, DessertCaseDream.class);
        if (packageManager.getComponentEnabledSetting(componentName) != 1) {
            Slog.v("DessertCase", "ACHIEVEMENT UNLOCKED");
            packageManager.setComponentEnabledSetting(componentName, 1, 1);
        }
        this.mView = new DessertCaseView(this);
        DessertCaseView.RescalingContainer rescalingContainer = new DessertCaseView.RescalingContainer(this);
        rescalingContainer.setView(this.mView);
        setContentView(rescalingContainer);
    }
}

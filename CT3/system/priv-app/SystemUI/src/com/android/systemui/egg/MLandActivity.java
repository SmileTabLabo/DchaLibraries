package com.android.systemui.egg;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
/* loaded from: a.zip:com/android/systemui/egg/MLandActivity.class */
public class MLandActivity extends Activity {
    MLand mLand;

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(2130968704);
        this.mLand = (MLand) findViewById(2131886482);
        this.mLand.setScoreFieldHolder((ViewGroup) findViewById(2131886489));
        this.mLand.setSplash(findViewById(2131886483));
        int size = this.mLand.getGameControllers().size();
        if (size > 0) {
            this.mLand.setupPlayers(size);
        }
    }

    @Override // android.app.Activity
    public void onPause() {
        this.mLand.stop();
        super.onPause();
    }

    @Override // android.app.Activity
    public void onResume() {
        super.onResume();
        this.mLand.onAttachedToWindow();
        updateSplashPlayers();
        this.mLand.showSplash();
    }

    public void playerMinus(View view) {
        this.mLand.removePlayer();
        updateSplashPlayers();
    }

    public void playerPlus(View view) {
        this.mLand.addPlayer();
        updateSplashPlayers();
    }

    public void startButtonPressed(View view) {
        findViewById(2131886488).setVisibility(4);
        findViewById(2131886490).setVisibility(4);
        this.mLand.start(true);
    }

    public void updateSplashPlayers() {
        int numPlayers = this.mLand.getNumPlayers();
        View findViewById = findViewById(2131886488);
        View findViewById2 = findViewById(2131886490);
        if (numPlayers == 1) {
            findViewById.setVisibility(4);
            findViewById2.setVisibility(0);
            findViewById2.requestFocus();
        } else if (numPlayers != 6) {
            findViewById.setVisibility(0);
            findViewById2.setVisibility(0);
        } else {
            findViewById.setVisibility(0);
            findViewById2.setVisibility(4);
            findViewById.requestFocus();
        }
    }
}

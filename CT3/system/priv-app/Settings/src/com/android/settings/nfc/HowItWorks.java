package com.android.settings.nfc;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.android.settings.R;
/* loaded from: classes.dex */
public class HowItWorks extends Activity {
    @Override // android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nfc_payment_how_it_works);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Button gotIt = (Button) findViewById(R.id.nfc_how_it_works_button);
        gotIt.setOnClickListener(new View.OnClickListener() { // from class: com.android.settings.nfc.HowItWorks.1
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                HowItWorks.this.finish();
            }
        });
    }

    @Override // android.app.Activity
    public boolean onNavigateUp() {
        finish();
        return true;
    }
}

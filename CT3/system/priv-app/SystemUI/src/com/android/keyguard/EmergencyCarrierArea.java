package com.android.keyguard;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
/* loaded from: a.zip:com/android/keyguard/EmergencyCarrierArea.class */
public class EmergencyCarrierArea extends AlphaOptimizedLinearLayout {
    private CarrierText mCarrierText;
    private EmergencyButton mEmergencyButton;

    public EmergencyCarrierArea(Context context) {
        super(context);
    }

    public EmergencyCarrierArea(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mCarrierText = (CarrierText) findViewById(R$id.carrier_text);
        this.mEmergencyButton = (EmergencyButton) findViewById(R$id.emergency_call_button);
        this.mEmergencyButton.setOnTouchListener(new View.OnTouchListener(this) { // from class: com.android.keyguard.EmergencyCarrierArea.1
            final EmergencyCarrierArea this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (this.this$0.mCarrierText.getVisibility() != 0) {
                    return false;
                }
                switch (motionEvent.getAction()) {
                    case 0:
                        this.this$0.mCarrierText.animate().alpha(0.0f);
                        return false;
                    case 1:
                        this.this$0.mCarrierText.animate().alpha(1.0f);
                        return false;
                    default:
                        return false;
                }
            }
        });
    }

    public void setCarrierTextVisible(boolean z) {
        this.mCarrierText.setVisibility(z ? 0 : 8);
    }
}

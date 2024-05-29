package com.android.systemui.volume;

import android.view.MotionEvent;
import android.view.View;
/* loaded from: a.zip:com/android/systemui/volume/Interaction.class */
public class Interaction {

    /* loaded from: a.zip:com/android/systemui/volume/Interaction$Callback.class */
    public interface Callback {
        void onInteraction();
    }

    public static void register(View view, Callback callback) {
        view.setOnTouchListener(new View.OnTouchListener(callback) { // from class: com.android.systemui.volume.Interaction.1
            final Callback val$callback;

            {
                this.val$callback = callback;
            }

            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view2, MotionEvent motionEvent) {
                this.val$callback.onInteraction();
                return false;
            }
        });
        view.setOnGenericMotionListener(new View.OnGenericMotionListener(callback) { // from class: com.android.systemui.volume.Interaction.2
            final Callback val$callback;

            {
                this.val$callback = callback;
            }

            @Override // android.view.View.OnGenericMotionListener
            public boolean onGenericMotion(View view2, MotionEvent motionEvent) {
                this.val$callback.onInteraction();
                return false;
            }
        });
    }
}

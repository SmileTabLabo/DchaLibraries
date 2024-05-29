package android.support.v4.app;

import android.content.Intent;
import android.content.IntentSender;
import android.support.annotation.Nullable;
/* loaded from: a.zip:android/support/v4/app/BaseFragmentActivityEclair.class */
abstract class BaseFragmentActivityEclair extends BaseFragmentActivityDonut {
    boolean mStartedIntentSenderFromFragment;

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void checkForValidRequestCode(int i) {
        if (((-65536) & i) != 0) {
            throw new IllegalArgumentException("Can only use lower 16 bits for requestCode");
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void onBackPressedNotHandled() {
        super.onBackPressed();
    }

    @Override // android.app.Activity
    public void startIntentSenderForResult(IntentSender intentSender, int i, @Nullable Intent intent, int i2, int i3, int i4) throws IntentSender.SendIntentException {
        if (!this.mStartedIntentSenderFromFragment && i != -1) {
            checkForValidRequestCode(i);
        }
        super.startIntentSenderForResult(intentSender, i, intent, i2, i3, i4);
    }
}

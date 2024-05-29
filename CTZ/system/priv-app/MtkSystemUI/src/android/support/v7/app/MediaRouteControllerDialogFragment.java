package android.support.v7.app;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
/* loaded from: classes.dex */
public class MediaRouteControllerDialogFragment extends DialogFragment {
    private MediaRouteControllerDialog mDialog;

    public MediaRouteControllerDialogFragment() {
        setCancelable(true);
    }

    public MediaRouteControllerDialog onCreateControllerDialog(Context context, Bundle savedInstanceState) {
        return new MediaRouteControllerDialog(context);
    }

    @Override // android.support.v4.app.DialogFragment
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.mDialog = onCreateControllerDialog(getContext(), savedInstanceState);
        return this.mDialog;
    }

    @Override // android.support.v4.app.DialogFragment, android.support.v4.app.Fragment
    public void onStop() {
        super.onStop();
        if (this.mDialog != null) {
            this.mDialog.clearGroupListAnimation(false);
        }
    }

    @Override // android.support.v4.app.Fragment, android.content.ComponentCallbacks
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.mDialog != null) {
            this.mDialog.updateLayout();
        }
    }
}

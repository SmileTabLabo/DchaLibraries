package com.android.launcher3.allapps;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Process;
import android.os.UserHandle;
import android.util.AttributeSet;
import android.widget.Switch;
import com.android.launcher3.compat.UserManagerCompat;
/* loaded from: classes.dex */
public class WorkModeSwitch extends Switch {
    public WorkModeSwitch(Context context) {
        super(context);
    }

    public WorkModeSwitch(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public WorkModeSwitch(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    @Override // android.widget.Switch, android.widget.CompoundButton, android.widget.Checkable
    public void setChecked(boolean z) {
    }

    @Override // android.widget.Switch, android.widget.CompoundButton, android.widget.Checkable
    public void toggle() {
        trySetQuietModeEnabledToAllProfilesAsync(isChecked());
    }

    private void setCheckedInternal(boolean z) {
        super.setChecked(z);
    }

    public void refresh() {
        setCheckedInternal(!UserManagerCompat.getInstance(getContext()).isAnyProfileQuietModeEnabled());
        setEnabled(true);
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.launcher3.allapps.WorkModeSwitch$1] */
    private void trySetQuietModeEnabledToAllProfilesAsync(final boolean z) {
        new AsyncTask<Void, Void, Boolean>() { // from class: com.android.launcher3.allapps.WorkModeSwitch.1
            @Override // android.os.AsyncTask
            protected void onPreExecute() {
                super.onPreExecute();
                WorkModeSwitch.this.setEnabled(false);
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public Boolean doInBackground(Void... voidArr) {
                UserManagerCompat userManagerCompat = UserManagerCompat.getInstance(WorkModeSwitch.this.getContext());
                boolean z2 = false;
                for (UserHandle userHandle : userManagerCompat.getUserProfiles()) {
                    if (!Process.myUserHandle().equals(userHandle)) {
                        z2 |= !userManagerCompat.requestQuietModeEnabled(z, userHandle);
                    }
                }
                return Boolean.valueOf(z2);
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public void onPostExecute(Boolean bool) {
                if (bool.booleanValue()) {
                    WorkModeSwitch.this.setEnabled(true);
                }
            }
        }.execute(new Void[0]);
    }
}

package com.android.settings.password;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserManager;
import android.util.Log;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.Utils;
/* loaded from: classes.dex */
public class ConfirmDeviceCredentialActivity extends Activity {
    public static final String TAG = ConfirmDeviceCredentialActivity.class.getSimpleName();

    /* loaded from: classes.dex */
    public static class InternalActivity extends ConfirmDeviceCredentialActivity {
    }

    public static Intent createIntent(CharSequence charSequence, CharSequence charSequence2) {
        Intent intent = new Intent();
        intent.setClassName("com.android.settings", ConfirmDeviceCredentialActivity.class.getName());
        intent.putExtra("android.app.extra.TITLE", charSequence);
        intent.putExtra("android.app.extra.DESCRIPTION", charSequence2);
        return intent;
    }

    /* JADX WARN: Removed duplicated region for block: B:15:0x005c  */
    /* JADX WARN: Removed duplicated region for block: B:16:0x0062  */
    /* JADX WARN: Removed duplicated region for block: B:24:0x0088  */
    @Override // android.app.Activity
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public void onCreate(Bundle bundle) {
        int userIdFromBundle;
        boolean launchConfirmationActivity;
        super.onCreate(bundle);
        Intent intent = getIntent();
        String stringExtra = intent.getStringExtra("android.app.extra.TITLE");
        String stringExtra2 = intent.getStringExtra("android.app.extra.DESCRIPTION");
        String stringExtra3 = intent.getStringExtra("android.app.extra.ALTERNATE_BUTTON_LABEL");
        boolean equals = "android.app.action.CONFIRM_FRP_CREDENTIAL".equals(intent.getAction());
        int credentialOwnerUserId = Utils.getCredentialOwnerUserId(this);
        if (isInternalActivity()) {
            try {
                userIdFromBundle = Utils.getUserIdFromBundle(this, intent.getExtras());
            } catch (SecurityException e) {
                Log.e(TAG, "Invalid intent extra", e);
            }
            boolean isManagedProfile = UserManager.get(this).isManagedProfile(userIdFromBundle);
            if (stringExtra == null && isManagedProfile) {
                stringExtra = getTitleFromOrganizationName(userIdFromBundle);
            }
            String str = stringExtra;
            ChooseLockSettingsHelper chooseLockSettingsHelper = new ChooseLockSettingsHelper(this);
            LockPatternUtils lockPatternUtils = new LockPatternUtils(this);
            if (!equals) {
                launchConfirmationActivity = chooseLockSettingsHelper.launchFrpConfirmationActivity(0, str, stringExtra2, stringExtra3);
            } else if (isManagedProfile && isInternalActivity() && !lockPatternUtils.isSeparateProfileChallengeEnabled(userIdFromBundle)) {
                launchConfirmationActivity = chooseLockSettingsHelper.launchConfirmationActivityWithExternalAndChallenge(0, null, str, stringExtra2, true, 0L, userIdFromBundle);
            } else {
                launchConfirmationActivity = chooseLockSettingsHelper.launchConfirmationActivity(0, null, str, stringExtra2, false, true, userIdFromBundle);
            }
            if (!launchConfirmationActivity) {
                Log.d(TAG, "No pattern, password or PIN set.");
                setResult(-1);
            }
            finish();
        }
        userIdFromBundle = credentialOwnerUserId;
        boolean isManagedProfile2 = UserManager.get(this).isManagedProfile(userIdFromBundle);
        if (stringExtra == null) {
            stringExtra = getTitleFromOrganizationName(userIdFromBundle);
        }
        String str2 = stringExtra;
        ChooseLockSettingsHelper chooseLockSettingsHelper2 = new ChooseLockSettingsHelper(this);
        LockPatternUtils lockPatternUtils2 = new LockPatternUtils(this);
        if (!equals) {
        }
        if (!launchConfirmationActivity) {
        }
        finish();
    }

    private boolean isInternalActivity() {
        return this instanceof InternalActivity;
    }

    private String getTitleFromOrganizationName(int i) {
        CharSequence charSequence;
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService("device_policy");
        if (devicePolicyManager != null) {
            charSequence = devicePolicyManager.getOrganizationNameForUser(i);
        } else {
            charSequence = null;
        }
        if (charSequence != null) {
            return charSequence.toString();
        }
        return null;
    }
}

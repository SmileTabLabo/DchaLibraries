package com.android.settings.accounts;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import android.widget.Toast;
import com.android.settings.ChooseLockSettingsHelper;
import com.android.settings.R;
import com.android.settings.Settings;
import com.android.settings.Utils;
import java.io.IOException;
/* loaded from: classes.dex */
public class AddAccountSettings extends Activity {
    private PendingIntent mPendingIntent;
    private UserHandle mUserHandle;
    private final AccountManagerCallback<Bundle> mCallback = new AccountManagerCallback<Bundle>() { // from class: com.android.settings.accounts.AddAccountSettings.1
        @Override // android.accounts.AccountManagerCallback
        public void run(AccountManagerFuture<Bundle> future) {
            Log.v("AccountSettings", "callback called");
            AddAccountSettings.this.mAddAccountCallbackCalled = true;
            boolean done = true;
            try {
                try {
                    try {
                        try {
                            Bundle bundle = future.getResult();
                            Intent intent = (Intent) bundle.get("intent");
                            if (intent != null) {
                                done = false;
                                Bundle addAccountOptions = new Bundle();
                                addAccountOptions.putParcelable("pendingIntent", AddAccountSettings.this.mPendingIntent);
                                addAccountOptions.putBoolean("hasMultipleUsers", Utils.hasMultipleUsers(AddAccountSettings.this));
                                addAccountOptions.putParcelable("android.intent.extra.USER", AddAccountSettings.this.mUserHandle);
                                intent.putExtras(addAccountOptions);
                                intent.addFlags(268435456);
                                AddAccountSettings.this.startActivityForResultAsUser(intent, 2, AddAccountSettings.this.mUserHandle);
                            } else {
                                AddAccountSettings.this.setResult(-1);
                                if (AddAccountSettings.this.mPendingIntent != null) {
                                    AddAccountSettings.this.mPendingIntent.cancel();
                                    AddAccountSettings.this.mPendingIntent = null;
                                }
                            }
                            if (Log.isLoggable("AccountSettings", 2)) {
                                Log.v("AccountSettings", "account added: " + bundle);
                            }
                            if (done) {
                                AddAccountSettings.this.finish();
                            }
                        } catch (OperationCanceledException e) {
                            if (Log.isLoggable("AccountSettings", 2)) {
                                Log.v("AccountSettings", "addAccount was canceled");
                            }
                            if (done) {
                                AddAccountSettings.this.finish();
                            }
                        }
                    } catch (IOException e2) {
                        if (Log.isLoggable("AccountSettings", 2)) {
                            Log.v("AccountSettings", "addAccount failed: " + e2);
                        }
                        if (done) {
                            AddAccountSettings.this.finish();
                        }
                    }
                } catch (AuthenticatorException e3) {
                    if (Log.isLoggable("AccountSettings", 2)) {
                        Log.v("AccountSettings", "addAccount failed: " + e3);
                    }
                    if (done) {
                        AddAccountSettings.this.finish();
                    }
                }
            } catch (Throwable th) {
                if (done) {
                    AddAccountSettings.this.finish();
                }
                throw th;
            }
        }
    };
    private boolean mAddAccountCalled = false;
    private boolean mAddAccountCallbackCalled = false;
    private boolean mPreventEmptyActivity = false;

    @Override // android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            this.mAddAccountCalled = savedInstanceState.getBoolean("AddAccountCalled");
            if (Log.isLoggable("AccountSettings", 2)) {
                Log.v("AccountSettings", "restored");
            }
        }
        UserManager um = (UserManager) getSystemService("user");
        this.mUserHandle = Utils.getSecureTargetUser(getActivityToken(), um, null, getIntent().getExtras());
        if (um.hasUserRestriction("no_modify_accounts", this.mUserHandle)) {
            Toast.makeText(this, (int) R.string.user_cannot_add_accounts_message, 1).show();
            finish();
        } else if (this.mAddAccountCalled) {
            finish();
        } else if (Utils.startQuietModeDialogIfNecessary(this, um, this.mUserHandle.getIdentifier())) {
            finish();
        } else if (um.isUserUnlocked(this.mUserHandle)) {
            requestChooseAccount();
        } else {
            ChooseLockSettingsHelper helper = new ChooseLockSettingsHelper(this);
            if (helper.launchConfirmationActivity(3, getString(R.string.unlock_set_unlock_launch_picker_title), false, this.mUserHandle.getIdentifier())) {
                return;
            }
            requestChooseAccount();
        }
    }

    @Override // android.app.Activity
    protected void onResume() {
        super.onResume();
        if (!this.mAddAccountCalled || this.mAddAccountCallbackCalled || !this.mPreventEmptyActivity) {
            return;
        }
        Log.v("AccountSettings", "finish empty activity");
        finish();
    }

    @Override // android.app.Activity
    protected void onStop() {
        super.onStop();
        if (!this.mAddAccountCalled || this.mAddAccountCallbackCalled || this.mPreventEmptyActivity) {
            return;
        }
        Log.v("AccountSettings", "prepare to prevent empty activity");
        this.mPreventEmptyActivity = true;
    }

    @Override // android.app.Activity
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == 0) {
                    if (data != null) {
                        startActivityAsUser(data, this.mUserHandle);
                    }
                    setResult(resultCode);
                    finish();
                    return;
                }
                addAccount(data.getStringExtra("selected_account"));
                return;
            case 2:
                setResult(resultCode);
                if (this.mPendingIntent != null) {
                    this.mPendingIntent.cancel();
                    this.mPendingIntent = null;
                }
                finish();
                return;
            case 3:
                if (resultCode == -1) {
                    requestChooseAccount();
                    return;
                } else {
                    finish();
                    return;
                }
            default:
                return;
        }
    }

    @Override // android.app.Activity
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("AddAccountCalled", this.mAddAccountCalled);
        if (Log.isLoggable("AccountSettings", 2)) {
            Log.v("AccountSettings", "saved");
        }
    }

    private void requestChooseAccount() {
        String[] authorities = getIntent().getStringArrayExtra("authorities");
        String[] accountTypes = getIntent().getStringArrayExtra("account_types");
        Intent intent = new Intent(this, Settings.ChooseAccountActivity.class);
        if (authorities != null) {
            intent.putExtra("authorities", authorities);
        }
        if (accountTypes != null) {
            intent.putExtra("account_types", accountTypes);
        }
        intent.putExtra("android.intent.extra.USER", this.mUserHandle);
        startActivityForResult(intent, 1);
    }

    private void addAccount(String accountType) {
        Bundle addAccountOptions = new Bundle();
        Intent identityIntent = new Intent();
        identityIntent.setComponent(new ComponentName("SHOULDN'T RESOLVE!", "SHOULDN'T RESOLVE!"));
        identityIntent.setAction("SHOULDN'T RESOLVE!");
        identityIntent.addCategory("SHOULDN'T RESOLVE!");
        this.mPendingIntent = PendingIntent.getBroadcast(this, 0, identityIntent, 0);
        addAccountOptions.putParcelable("pendingIntent", this.mPendingIntent);
        addAccountOptions.putBoolean("hasMultipleUsers", Utils.hasMultipleUsers(this));
        AccountManager.get(this).addAccountAsUser(accountType, null, null, addAccountOptions, null, this.mCallback, null, this.mUserHandle);
        this.mAddAccountCalled = true;
    }
}

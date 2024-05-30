package com.android.settings;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.telephony.euicc.EuiccManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.android.settings.core.InstrumentedFragment;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.enterprise.ActionDisabledByAdminDialogHelper;
import com.android.settings.password.ChooseLockSettingsHelper;
import com.android.settingslib.RestrictedLockUtils;
import java.util.List;
/* loaded from: classes.dex */
public class MasterClear extends InstrumentedFragment implements ViewTreeObserver.OnGlobalLayoutListener {
    static final int CREDENTIAL_CONFIRM_REQUEST = 56;
    static final int KEYGUARD_REQUEST = 55;
    private View mContentView;
    CheckBox mEsimStorage;
    private View mEsimStorageContainer;
    CheckBox mExternalStorage;
    private View mExternalStorageContainer;
    Button mInitiateButton;
    protected final View.OnClickListener mInitiateListener = new View.OnClickListener() { // from class: com.android.settings.MasterClear.1
        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            Context context = view.getContext();
            if (!Utils.isDemoUser(context)) {
                if (MasterClear.this.runKeyguardConfirmation(MasterClear.KEYGUARD_REQUEST)) {
                    return;
                }
                Intent accountConfirmationIntent = MasterClear.this.getAccountConfirmationIntent();
                if (accountConfirmationIntent != null) {
                    MasterClear.this.showAccountCredentialConfirmation(accountConfirmationIntent);
                    return;
                } else {
                    MasterClear.this.showFinalConfirmation();
                    return;
                }
            }
            ComponentName deviceOwnerComponent = Utils.getDeviceOwnerComponent(context);
            if (deviceOwnerComponent != null) {
                context.startActivity(new Intent().setPackage(deviceOwnerComponent.getPackageName()).setAction("android.intent.action.FACTORY_RESET"));
            }
        }
    };
    ScrollView mScrollView;

    @Override // android.view.ViewTreeObserver.OnGlobalLayoutListener
    public void onGlobalLayout() {
        this.mInitiateButton.setEnabled(hasReachedBottom(this.mScrollView));
    }

    @Override // com.android.settingslib.core.lifecycle.ObservableFragment, android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getActivity().setTitle(R.string.master_clear_short_title);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean runKeyguardConfirmation(int i) {
        return new ChooseLockSettingsHelper(getActivity(), this).launchConfirmationActivity(i, getActivity().getResources().getText(R.string.master_clear_short_title));
    }

    boolean isValidRequestCode(int i) {
        return i == KEYGUARD_REQUEST || i == CREDENTIAL_CONFIRM_REQUEST;
    }

    @Override // android.app.Fragment
    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        onActivityResultInternal(i, i2, intent);
    }

    void onActivityResultInternal(int i, int i2, Intent intent) {
        Intent accountConfirmationIntent;
        if (!isValidRequestCode(i)) {
            return;
        }
        if (i2 != -1) {
            establishInitialState();
        } else if (CREDENTIAL_CONFIRM_REQUEST != i && (accountConfirmationIntent = getAccountConfirmationIntent()) != null) {
            showAccountCredentialConfirmation(accountConfirmationIntent);
        } else {
            showFinalConfirmation();
        }
    }

    void showFinalConfirmation() {
        Bundle bundle = new Bundle();
        bundle.putBoolean("erase_sd", this.mExternalStorage.isChecked());
        bundle.putBoolean("erase_esim", this.mEsimStorage.isChecked());
        new SubSettingLauncher(getContext()).setDestination(MasterClearConfirm.class.getName()).setArguments(bundle).setTitle(R.string.master_clear_confirm_title).setSourceMetricsCategory(getMetricsCategory()).launch();
    }

    void showAccountCredentialConfirmation(Intent intent) {
        startActivityForResult(intent, CREDENTIAL_CONFIRM_REQUEST);
    }

    Intent getAccountConfirmationIntent() {
        Activity activity = getActivity();
        String string = activity.getString(R.string.account_type);
        String string2 = activity.getString(R.string.account_confirmation_package);
        String string3 = activity.getString(R.string.account_confirmation_class);
        if (TextUtils.isEmpty(string) || TextUtils.isEmpty(string2) || TextUtils.isEmpty(string3)) {
            Log.i("MasterClear", "Resources not set for account confirmation.");
            return null;
        }
        Account[] accountsByType = AccountManager.get(activity).getAccountsByType(string);
        if (accountsByType != null && accountsByType.length > 0) {
            Intent component = new Intent().setPackage(string2).setComponent(new ComponentName(string2, string3));
            ResolveInfo resolveActivity = activity.getPackageManager().resolveActivity(component, 0);
            if (resolveActivity != null && resolveActivity.activityInfo != null && string2.equals(resolveActivity.activityInfo.packageName)) {
                return component;
            }
            Log.i("MasterClear", "Unable to resolve Activity: " + string2 + "/" + string3);
        } else {
            Log.d("MasterClear", "No " + string + " accounts installed!");
        }
        return null;
    }

    void establishInitialState() {
        this.mInitiateButton = (Button) this.mContentView.findViewById(R.id.initiate_master_clear);
        this.mInitiateButton.setOnClickListener(this.mInitiateListener);
        this.mExternalStorageContainer = this.mContentView.findViewById(R.id.erase_external_container);
        this.mExternalStorage = (CheckBox) this.mContentView.findViewById(R.id.erase_external);
        this.mEsimStorageContainer = this.mContentView.findViewById(R.id.erase_esim_container);
        this.mEsimStorage = (CheckBox) this.mContentView.findViewById(R.id.erase_esim);
        if (this.mScrollView != null) {
            this.mScrollView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }
        this.mScrollView = (ScrollView) this.mContentView.findViewById(R.id.master_clear_scrollview);
        boolean isExternalStorageEmulated = Environment.isExternalStorageEmulated();
        if (isExternalStorageEmulated || (!Environment.isExternalStorageRemovable() && isExtStorageEncrypted())) {
            this.mExternalStorageContainer.setVisibility(8);
            this.mContentView.findViewById(R.id.erase_external_option_text).setVisibility(8);
            this.mContentView.findViewById(R.id.also_erases_external).setVisibility(0);
            this.mExternalStorage.setChecked(!isExternalStorageEmulated);
        } else {
            this.mExternalStorageContainer.setOnClickListener(new View.OnClickListener() { // from class: com.android.settings.MasterClear.2
                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    MasterClear.this.mExternalStorage.toggle();
                }
            });
        }
        if (showWipeEuicc()) {
            if (showWipeEuiccCheckbox()) {
                ((TextView) this.mContentView.findViewById(R.id.erase_esim_title)).setText(R.string.erase_esim_storage);
                this.mEsimStorageContainer.setVisibility(0);
                this.mEsimStorageContainer.setOnClickListener(new View.OnClickListener() { // from class: com.android.settings.MasterClear.3
                    @Override // android.view.View.OnClickListener
                    public void onClick(View view) {
                        MasterClear.this.mEsimStorage.toggle();
                    }
                });
            } else {
                this.mContentView.findViewById(R.id.also_erases_esim).setVisibility(0);
                this.mContentView.findViewById(R.id.no_cancel_mobile_plan).setVisibility(0);
                this.mEsimStorage.setChecked(true);
            }
        }
        loadAccountList((UserManager) getActivity().getSystemService("user"));
        StringBuffer stringBuffer = new StringBuffer();
        View findViewById = this.mContentView.findViewById(R.id.master_clear_container);
        getContentDescription(findViewById, stringBuffer);
        findViewById.setContentDescription(stringBuffer);
        this.mScrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() { // from class: com.android.settings.MasterClear.4
            @Override // android.view.View.OnScrollChangeListener
            public void onScrollChange(View view, int i, int i2, int i3, int i4) {
                if ((view instanceof ScrollView) && MasterClear.this.hasReachedBottom((ScrollView) view)) {
                    MasterClear.this.mInitiateButton.setEnabled(true);
                    MasterClear.this.mScrollView.setOnScrollChangeListener(null);
                }
            }
        });
        this.mScrollView.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    boolean showWipeEuicc() {
        Context context = getContext();
        if (isEuiccEnabled(context)) {
            ContentResolver contentResolver = context.getContentResolver();
            return (Settings.Global.getInt(contentResolver, "euicc_provisioned", 0) == 0 && Settings.Global.getInt(contentResolver, "development_settings_enabled", 0) == 0) ? false : true;
        }
        return false;
    }

    boolean showWipeEuiccCheckbox() {
        return SystemProperties.getBoolean("masterclear.allow_retain_esim_profiles_after_fdr", false);
    }

    protected boolean isEuiccEnabled(Context context) {
        return ((EuiccManager) context.getSystemService("euicc")).isEnabled();
    }

    boolean hasReachedBottom(ScrollView scrollView) {
        return scrollView.getChildCount() < 1 || scrollView.getChildAt(0).getBottom() - (scrollView.getHeight() + scrollView.getScrollY()) <= 0;
    }

    private void getContentDescription(View view, StringBuffer stringBuffer) {
        if (view.getVisibility() != 0) {
            return;
        }
        if (!(view instanceof ViewGroup)) {
            if (view instanceof TextView) {
                stringBuffer.append(((TextView) view).getText());
                stringBuffer.append(",");
                return;
            }
            return;
        }
        ViewGroup viewGroup = (ViewGroup) view;
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            getContentDescription(viewGroup.getChildAt(i), stringBuffer);
        }
    }

    private boolean isExtStorageEncrypted() {
        return !"".equals(SystemProperties.get("vold.decrypt"));
    }

    /* JADX WARN: Removed duplicated region for block: B:46:0x014e  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private void loadAccountList(UserManager userManager) {
        int i;
        List list;
        AccountManager accountManager;
        Drawable drawable;
        int i2;
        Account[] accountArr;
        AuthenticatorDescription authenticatorDescription;
        int i3;
        AuthenticatorDescription[] authenticatorDescriptionArr;
        View findViewById = this.mContentView.findViewById(R.id.accounts_label);
        LinearLayout linearLayout = (LinearLayout) this.mContentView.findViewById(R.id.accounts);
        linearLayout.removeAllViews();
        Activity activity = getActivity();
        List profiles = userManager.getProfiles(UserHandle.myUserId());
        int size = profiles.size();
        AccountManager accountManager2 = AccountManager.get(activity);
        LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService("layout_inflater");
        int i4 = 0;
        int i5 = 0;
        while (i5 < size) {
            UserInfo userInfo = (UserInfo) profiles.get(i5);
            int i6 = userInfo.id;
            UserHandle userHandle = new UserHandle(i6);
            Account[] accountsAsUser = accountManager2.getAccountsAsUser(i6);
            int length = accountsAsUser.length;
            if (length == 0) {
                list = profiles;
                accountManager = accountManager2;
            } else {
                int i7 = i4 + length;
                AuthenticatorDescription[] authenticatorTypesAsUser = AccountManager.get(activity).getAuthenticatorTypesAsUser(i6);
                list = profiles;
                int length2 = authenticatorTypesAsUser.length;
                accountManager = accountManager2;
                if (size > 1) {
                    View inflateCategoryHeader = Utils.inflateCategoryHeader(layoutInflater, linearLayout);
                    ((TextView) inflateCategoryHeader.findViewById(16908310)).setText(userInfo.isManagedProfile() ? R.string.category_work : R.string.category_personal);
                    linearLayout.addView(inflateCategoryHeader);
                }
                int i8 = 0;
                while (i8 < length) {
                    Account account = accountsAsUser[i8];
                    int i9 = 0;
                    while (true) {
                        drawable = null;
                        if (i9 < length2) {
                            i2 = length2;
                            accountArr = accountsAsUser;
                            if (!account.type.equals(authenticatorTypesAsUser[i9].type)) {
                                i9++;
                                length2 = i2;
                                accountsAsUser = accountArr;
                            } else {
                                authenticatorDescription = authenticatorTypesAsUser[i9];
                                break;
                            }
                        } else {
                            i2 = length2;
                            accountArr = accountsAsUser;
                            authenticatorDescription = null;
                            break;
                        }
                    }
                    if (authenticatorDescription == null) {
                        Log.w("MasterClear", "No descriptor for account name=" + account.name + " type=" + account.type);
                        i3 = length;
                        authenticatorDescriptionArr = authenticatorTypesAsUser;
                    } else {
                        try {
                            if (authenticatorDescription.iconId != 0) {
                                i3 = length;
                                try {
                                    drawable = activity.getPackageManager().getUserBadgedIcon(activity.createPackageContextAsUser(authenticatorDescription.packageName, 0, userHandle).getDrawable(authenticatorDescription.iconId), userHandle);
                                } catch (PackageManager.NameNotFoundException e) {
                                    authenticatorDescriptionArr = authenticatorTypesAsUser;
                                    Log.w("MasterClear", "Bad package name for account type " + authenticatorDescription.type);
                                    if (drawable == null) {
                                    }
                                    View inflate = layoutInflater.inflate(R.layout.master_clear_account, (ViewGroup) linearLayout, false);
                                    ((ImageView) inflate.findViewById(16908294)).setImageDrawable(drawable);
                                    ((TextView) inflate.findViewById(16908310)).setText(account.name);
                                    linearLayout.addView(inflate);
                                    i8++;
                                    length2 = i2;
                                    accountsAsUser = accountArr;
                                    length = i3;
                                    authenticatorTypesAsUser = authenticatorDescriptionArr;
                                } catch (Resources.NotFoundException e2) {
                                    e = e2;
                                    StringBuilder sb = new StringBuilder();
                                    authenticatorDescriptionArr = authenticatorTypesAsUser;
                                    sb.append("Invalid icon id for account type ");
                                    sb.append(authenticatorDescription.type);
                                    Log.w("MasterClear", sb.toString(), e);
                                    if (drawable == null) {
                                    }
                                    View inflate2 = layoutInflater.inflate(R.layout.master_clear_account, (ViewGroup) linearLayout, false);
                                    ((ImageView) inflate2.findViewById(16908294)).setImageDrawable(drawable);
                                    ((TextView) inflate2.findViewById(16908310)).setText(account.name);
                                    linearLayout.addView(inflate2);
                                    i8++;
                                    length2 = i2;
                                    accountsAsUser = accountArr;
                                    length = i3;
                                    authenticatorTypesAsUser = authenticatorDescriptionArr;
                                }
                            } else {
                                i3 = length;
                            }
                            authenticatorDescriptionArr = authenticatorTypesAsUser;
                        } catch (PackageManager.NameNotFoundException e3) {
                            i3 = length;
                        } catch (Resources.NotFoundException e4) {
                            e = e4;
                            i3 = length;
                        }
                        if (drawable == null) {
                            drawable = activity.getPackageManager().getDefaultActivityIcon();
                        }
                        View inflate22 = layoutInflater.inflate(R.layout.master_clear_account, (ViewGroup) linearLayout, false);
                        ((ImageView) inflate22.findViewById(16908294)).setImageDrawable(drawable);
                        ((TextView) inflate22.findViewById(16908310)).setText(account.name);
                        linearLayout.addView(inflate22);
                    }
                    i8++;
                    length2 = i2;
                    accountsAsUser = accountArr;
                    length = i3;
                    authenticatorTypesAsUser = authenticatorDescriptionArr;
                }
                i4 = i7;
            }
            i5++;
            profiles = list;
            accountManager2 = accountManager;
        }
        int i10 = 1;
        if (i4 > 0) {
            i = 0;
            findViewById.setVisibility(0);
            linearLayout.setVisibility(0);
        } else {
            i = 0;
        }
        View findViewById2 = this.mContentView.findViewById(R.id.other_users_present);
        if (userManager.getUserCount() - size <= 0) {
            i10 = i;
        }
        if (i10 == 0) {
            i = 8;
        }
        findViewById2.setVisibility(i);
    }

    @Override // android.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        Context context = getContext();
        RestrictedLockUtils.EnforcedAdmin checkIfRestrictionEnforced = RestrictedLockUtils.checkIfRestrictionEnforced(context, "no_factory_reset", UserHandle.myUserId());
        if ((!UserManager.get(context).isAdminUser() || RestrictedLockUtils.hasBaseUserRestriction(context, "no_factory_reset", UserHandle.myUserId())) && !Utils.isDemoUser(context)) {
            return layoutInflater.inflate(R.layout.master_clear_disallowed_screen, (ViewGroup) null);
        }
        if (checkIfRestrictionEnforced == null) {
            this.mContentView = layoutInflater.inflate(R.layout.master_clear, (ViewGroup) null);
            establishInitialState();
            return this.mContentView;
        }
        new ActionDisabledByAdminDialogHelper(getActivity()).prepareDialogBuilder("no_factory_reset", checkIfRestrictionEnforced).setOnDismissListener(new DialogInterface.OnDismissListener() { // from class: com.android.settings.-$$Lambda$MasterClear$Z9cDw51Fmk6mKl61MZqDIXg0-34
            @Override // android.content.DialogInterface.OnDismissListener
            public final void onDismiss(DialogInterface dialogInterface) {
                MasterClear.this.getActivity().finish();
            }
        }).show();
        return new View(getContext());
    }

    @Override // com.android.settingslib.core.instrumentation.Instrumentable
    public int getMetricsCategory() {
        return 66;
    }
}

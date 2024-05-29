package com.android.settings;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.EventLog;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
import com.android.internal.telephony.SmsApplication;
import java.util.ArrayList;
import java.util.List;
/* loaded from: classes.dex */
public final class SmsDefaultDialog extends AlertActivity implements DialogInterface.OnClickListener {
    private SmsApplication.SmsApplicationData mNewSmsApplicationData;

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        String stringExtra = getIntent().getStringExtra("package");
        setResult(0);
        if (!buildDialog(stringExtra)) {
            finish();
        }
    }

    protected void onStart() {
        super.onStart();
        getWindow().addPrivateFlags(524288);
        EventLog.writeEvent(1397638484, "120484087", -1, "");
    }

    protected void onStop() {
        super.onStop();
        Window window = getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.privateFlags &= -524289;
        window.setAttributes(attributes);
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // android.content.DialogInterface.OnClickListener
    public void onClick(DialogInterface dialogInterface, int i) {
        switch (i) {
            case -2:
                return;
            case -1:
                SmsApplication.setDefaultApplication(this.mNewSmsApplicationData.mPackageName, this);
                setResult(-1);
                return;
            default:
                if (i >= 0) {
                    AppListAdapter appListAdapter = (AppListAdapter) this.mAlertParams.mAdapter;
                    if (!appListAdapter.isSelected(i)) {
                        String packageName = appListAdapter.getPackageName(i);
                        if (!TextUtils.isEmpty(packageName)) {
                            SmsApplication.setDefaultApplication(packageName, this);
                            setResult(-1);
                            return;
                        }
                        return;
                    }
                    return;
                }
                return;
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    private boolean buildDialog(String str) {
        if (((TelephonyManager) getSystemService("phone")).isSmsCapable()) {
            AlertController.AlertParams alertParams = this.mAlertParams;
            alertParams.mTitle = getString(R.string.sms_change_default_dialog_title);
            this.mNewSmsApplicationData = SmsApplication.getSmsApplicationData(str, this);
            if (this.mNewSmsApplicationData != null) {
                SmsApplication.SmsApplicationData smsApplicationData = null;
                ComponentName defaultSmsApplication = SmsApplication.getDefaultSmsApplication(this, true);
                if (defaultSmsApplication != null) {
                    smsApplicationData = SmsApplication.getSmsApplicationData(defaultSmsApplication.getPackageName(), this);
                    if (smsApplicationData.mPackageName.equals(this.mNewSmsApplicationData.mPackageName)) {
                        return false;
                    }
                }
                if (smsApplicationData != null) {
                    alertParams.mMessage = getString(R.string.sms_change_default_dialog_text, new Object[]{this.mNewSmsApplicationData.getApplicationName(this), smsApplicationData.getApplicationName(this)});
                } else {
                    alertParams.mMessage = getString(R.string.sms_change_default_no_previous_dialog_text, new Object[]{this.mNewSmsApplicationData.getApplicationName(this)});
                }
                alertParams.mPositiveButtonText = getString(R.string.yes);
                alertParams.mNegativeButtonText = getString(R.string.no);
                alertParams.mPositiveButtonListener = this;
                alertParams.mNegativeButtonListener = this;
            } else {
                alertParams.mAdapter = new AppListAdapter();
                alertParams.mOnClickListener = this;
                alertParams.mNegativeButtonText = getString(R.string.cancel);
                alertParams.mNegativeButtonListener = this;
                if (alertParams.mAdapter.isEmpty()) {
                    return false;
                }
            }
            setupAlert();
            return true;
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class AppListAdapter extends BaseAdapter {
        private final List<Item> mItems = getItems();
        private final int mSelectedIndex;

        /* JADX INFO: Access modifiers changed from: private */
        /* loaded from: classes.dex */
        public class Item {
            final Drawable icon;
            final String label;
            final String packgeName;

            public Item(String str, Drawable drawable, String str2) {
                this.label = str;
                this.icon = drawable;
                this.packgeName = str2;
            }
        }

        public AppListAdapter() {
            int selectedIndex = getSelectedIndex();
            if (selectedIndex > 0) {
                this.mItems.add(0, this.mItems.remove(selectedIndex));
                selectedIndex = 0;
            }
            this.mSelectedIndex = selectedIndex;
        }

        @Override // android.widget.Adapter
        public int getCount() {
            if (this.mItems != null) {
                return this.mItems.size();
            }
            return 0;
        }

        @Override // android.widget.Adapter
        public Object getItem(int i) {
            if (this.mItems == null || i >= this.mItems.size()) {
                return null;
            }
            return this.mItems.get(i);
        }

        @Override // android.widget.Adapter
        public long getItemId(int i) {
            return i;
        }

        @Override // android.widget.Adapter
        public View getView(int i, View view, ViewGroup viewGroup) {
            Item item = (Item) getItem(i);
            View inflate = SmsDefaultDialog.this.getLayoutInflater().inflate(R.layout.app_preference_item, viewGroup, false);
            ((TextView) inflate.findViewById(16908310)).setText(item.label);
            if (i == this.mSelectedIndex) {
                inflate.findViewById(R.id.default_label).setVisibility(0);
            } else {
                inflate.findViewById(R.id.default_label).setVisibility(8);
            }
            ((ImageView) inflate.findViewById(16908294)).setImageDrawable(item.icon);
            return inflate;
        }

        public String getPackageName(int i) {
            Item item = (Item) getItem(i);
            if (item != null) {
                return item.packgeName;
            }
            return null;
        }

        public boolean isSelected(int i) {
            return i == this.mSelectedIndex;
        }

        private List<Item> getItems() {
            PackageManager packageManager = SmsDefaultDialog.this.getPackageManager();
            ArrayList arrayList = new ArrayList();
            for (SmsApplication.SmsApplicationData smsApplicationData : SmsApplication.getApplicationCollection(SmsDefaultDialog.this)) {
                try {
                    String str = smsApplicationData.mPackageName;
                    ApplicationInfo applicationInfo = packageManager.getApplicationInfo(str, 0);
                    if (applicationInfo != null) {
                        arrayList.add(new Item(applicationInfo.loadLabel(packageManager).toString(), applicationInfo.loadIcon(packageManager), str));
                    }
                } catch (PackageManager.NameNotFoundException e) {
                }
            }
            return arrayList;
        }

        private int getSelectedIndex() {
            ComponentName defaultSmsApplication = SmsApplication.getDefaultSmsApplication(SmsDefaultDialog.this, true);
            if (defaultSmsApplication != null) {
                String packageName = defaultSmsApplication.getPackageName();
                if (!TextUtils.isEmpty(packageName)) {
                    for (int i = 0; i < this.mItems.size(); i++) {
                        if (TextUtils.equals(this.mItems.get(i).packgeName, packageName)) {
                            return i;
                        }
                    }
                    return -1;
                }
                return -1;
            }
            return -1;
        }
    }
}

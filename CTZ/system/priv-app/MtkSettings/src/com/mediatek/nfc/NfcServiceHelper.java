package com.mediatek.nfc;

import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.util.Log;
import com.mediatek.nfcsettingsadapter.NfcSettingsAdapter;
import com.mediatek.nfcsettingsadapter.ServiceEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
/* loaded from: classes.dex */
public class NfcServiceHelper {
    private static final Comparator<ServiceEntry> sServiceCompare = new Comparator<ServiceEntry>() { // from class: com.mediatek.nfc.NfcServiceHelper.1
        @Override // java.util.Comparator
        public int compare(ServiceEntry serviceEntry, ServiceEntry serviceEntry2) {
            if (serviceEntry.getWasEnabled().equals(serviceEntry2.getWasEnabled())) {
                return serviceEntry.getTitle().compareTo(serviceEntry2.getTitle());
            }
            if (serviceEntry.getWasEnabled().booleanValue()) {
                return -1;
            }
            if (serviceEntry2.getWasEnabled().booleanValue()) {
                return 1;
            }
            return 0;
        }
    };
    private ArrayList<String> mCheckedServices;
    private Context mContext;
    private boolean mEditMode;
    private NfcSettingsAdapter mNfcSettingsAdapter;
    private List<ServiceEntry> mServiceEntryList;

    public NfcServiceHelper(Context context) {
        this.mContext = context;
        this.mNfcSettingsAdapter = NfcSettingsAdapter.getDefaultAdapter(this.mContext);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void initServiceList() {
        if (this.mNfcSettingsAdapter != null) {
            this.mServiceEntryList = this.mNfcSettingsAdapter.getServiceEntryList(UserHandle.myUserId());
            if (this.mServiceEntryList == null) {
                Log.e("NfcServiceHelper", "Cannot get ServiceEntry list");
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void sortList() {
        if (this.mServiceEntryList != null) {
            Collections.sort(this.mServiceEntryList, sServiceCompare);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void saveChange() {
        if (this.mNfcSettingsAdapter != null && this.mServiceEntryList != null) {
            this.mNfcSettingsAdapter.commitServiceEntryList(this.mServiceEntryList);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void saveState(Bundle bundle) {
        if (bundle != null && this.mEditMode && this.mServiceEntryList != null) {
            this.mCheckedServices = new ArrayList<>();
            for (ServiceEntry serviceEntry : this.mServiceEntryList) {
                if (serviceEntry.getWantEnabled().booleanValue()) {
                    this.mCheckedServices.add(serviceEntry.getComponent().flattenToString());
                }
            }
            bundle.putStringArrayList("checked_services", this.mCheckedServices);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void restoreState(Bundle bundle) {
        Log.d("NfcServiceHelper", "restoreState mEditMode = " + this.mEditMode);
        if (bundle != null && this.mEditMode) {
            this.mCheckedServices = bundle.getStringArrayList("checked_services");
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void restoreCheckedState() {
        if (this.mCheckedServices != null && this.mServiceEntryList != null && this.mEditMode) {
            for (ServiceEntry serviceEntry : this.mServiceEntryList) {
                serviceEntry.setWantEnabled(new Boolean(this.mCheckedServices.contains(serviceEntry.getComponent().flattenToString())));
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setEditMode(boolean z) {
        this.mEditMode = z;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public List<ServiceEntry> getServiceList() {
        return this.mServiceEntryList;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getAllServiceCount() {
        if (this.mServiceEntryList != null) {
            return this.mServiceEntryList.size();
        }
        return 0;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getSelectServiceCount() {
        int i = 0;
        if (this.mServiceEntryList != null) {
            for (ServiceEntry serviceEntry : this.mServiceEntryList) {
                if (serviceEntry.getWasEnabled().booleanValue()) {
                    i++;
                }
            }
        }
        return i;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean setEnabled(NfcServicePreference nfcServicePreference, boolean z) {
        for (ServiceEntry serviceEntry : this.mServiceEntryList) {
            if (nfcServicePreference.mComponent.equals(serviceEntry.getComponent())) {
                Boolean wantEnabled = serviceEntry.getWantEnabled();
                serviceEntry.setWantEnabled(new Boolean(z));
                if (canDoAction()) {
                    return true;
                }
                serviceEntry.setWantEnabled(wantEnabled);
                return false;
            }
        }
        return false;
    }

    private boolean canDoAction() {
        boolean z;
        if (this.mNfcSettingsAdapter != null && this.mServiceEntryList != null) {
            z = this.mNfcSettingsAdapter.testServiceEntryList(this.mServiceEntryList);
        } else {
            z = false;
        }
        Log.d("NfcServiceHelper", "Can do this action ? " + z);
        return z;
    }
}

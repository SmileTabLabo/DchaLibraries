package com.android.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.UserHandle;
import android.security.KeyChain;
import android.security.KeyStore;
import android.security.keymaster.KeyCharacteristics;
import android.security.keymaster.KeymasterBlob;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settingslib.RestrictedLockUtils;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
/* loaded from: classes.dex */
public class UserCredentialsSettings extends SettingsPreferenceFragment implements View.OnClickListener {
    private static final SparseArray<Credential.Type> credentialViewTypes = new SparseArray<>();

    @Override // com.android.settingslib.core.instrumentation.Instrumentable
    public int getMetricsCategory() {
        return 285;
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settings.core.InstrumentedPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onResume() {
        super.onResume();
        refreshItems();
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        Credential credential = (Credential) view.getTag();
        if (credential != null) {
            CredentialDialogFragment.show(this, credential);
        }
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getActivity().setTitle(R.string.user_credentials);
    }

    protected void announceRemoval(String str) {
        if (!isAdded()) {
            return;
        }
        getListView().announceForAccessibility(getString(R.string.user_credential_removed, new Object[]{str}));
    }

    protected void refreshItems() {
        if (isAdded()) {
            new AliasLoader().execute(new Void[0]);
        }
    }

    /* loaded from: classes.dex */
    public static class CredentialDialogFragment extends InstrumentedDialogFragment {
        public static void show(Fragment fragment, Credential credential) {
            Bundle bundle = new Bundle();
            bundle.putParcelable("credential", credential);
            if (fragment.getFragmentManager().findFragmentByTag("CredentialDialogFragment") == null) {
                CredentialDialogFragment credentialDialogFragment = new CredentialDialogFragment();
                credentialDialogFragment.setTargetFragment(fragment, -1);
                credentialDialogFragment.setArguments(bundle);
                credentialDialogFragment.show(fragment.getFragmentManager(), "CredentialDialogFragment");
            }
        }

        @Override // android.app.DialogFragment
        public Dialog onCreateDialog(Bundle bundle) {
            final Credential credential = (Credential) getArguments().getParcelable("credential");
            View inflate = getActivity().getLayoutInflater().inflate(R.layout.user_credential_dialog, (ViewGroup) null);
            ViewGroup viewGroup = (ViewGroup) inflate.findViewById(R.id.credential_container);
            viewGroup.addView(UserCredentialsSettings.getCredentialView(credential, R.layout.user_credential, null, viewGroup, true));
            AlertDialog.Builder positiveButton = new AlertDialog.Builder(getActivity()).setView(inflate).setTitle(R.string.user_credential_title).setPositiveButton(R.string.done, (DialogInterface.OnClickListener) null);
            final int myUserId = UserHandle.myUserId();
            if (!RestrictedLockUtils.hasBaseUserRestriction(getContext(), "no_config_credentials", myUserId)) {
                DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() { // from class: com.android.settings.UserCredentialsSettings.CredentialDialogFragment.1
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i) {
                        RestrictedLockUtils.EnforcedAdmin checkIfRestrictionEnforced = RestrictedLockUtils.checkIfRestrictionEnforced(CredentialDialogFragment.this.getContext(), "no_config_credentials", myUserId);
                        if (checkIfRestrictionEnforced != null) {
                            RestrictedLockUtils.sendShowAdminSupportDetailsIntent(CredentialDialogFragment.this.getContext(), checkIfRestrictionEnforced);
                        } else {
                            new RemoveCredentialsTask(CredentialDialogFragment.this.getContext(), CredentialDialogFragment.this.getTargetFragment()).execute(credential);
                        }
                        dialogInterface.dismiss();
                    }
                };
                if (credential.isSystem()) {
                    positiveButton.setNegativeButton(R.string.trusted_credentials_remove_label, onClickListener);
                }
            }
            return positiveButton.create();
        }

        @Override // com.android.settingslib.core.instrumentation.Instrumentable
        public int getMetricsCategory() {
            return 533;
        }

        /* loaded from: classes.dex */
        private class RemoveCredentialsTask extends AsyncTask<Credential, Void, Credential[]> {
            private Context context;
            private Fragment targetFragment;

            public RemoveCredentialsTask(Context context, Fragment fragment) {
                this.context = context;
                this.targetFragment = fragment;
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public Credential[] doInBackground(Credential... credentialArr) {
                for (Credential credential : credentialArr) {
                    if (credential.isSystem()) {
                        removeGrantsAndDelete(credential);
                    } else {
                        throw new UnsupportedOperationException("Not implemented for wifi certificates. This should not be reachable.");
                    }
                }
                return credentialArr;
            }

            private void removeGrantsAndDelete(Credential credential) {
                try {
                    KeyChain.KeyChainConnection bind = KeyChain.bind(CredentialDialogFragment.this.getContext());
                    try {
                        try {
                            bind.getService().removeKeyPair(credential.alias);
                        } catch (RemoteException e) {
                            Log.w("CredentialDialogFragment", "Removing credentials", e);
                        }
                    } finally {
                        bind.close();
                    }
                } catch (InterruptedException e2) {
                    Log.w("CredentialDialogFragment", "Connecting to KeyChain", e2);
                }
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public void onPostExecute(Credential... credentialArr) {
                if ((this.targetFragment instanceof UserCredentialsSettings) && this.targetFragment.isAdded()) {
                    UserCredentialsSettings userCredentialsSettings = (UserCredentialsSettings) this.targetFragment;
                    for (Credential credential : credentialArr) {
                        userCredentialsSettings.announceRemoval(credential.alias);
                    }
                    userCredentialsSettings.refreshItems();
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class AliasLoader extends AsyncTask<Void, Void, List<Credential>> {
        private AliasLoader() {
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public List<Credential> doInBackground(Void... voidArr) {
            KeyStore keyStore = KeyStore.getInstance();
            int myUserId = UserHandle.myUserId();
            int uid = UserHandle.getUid(myUserId, 1000);
            int uid2 = UserHandle.getUid(myUserId, 1010);
            ArrayList arrayList = new ArrayList();
            arrayList.addAll(getCredentialsForUid(keyStore, uid).values());
            arrayList.addAll(getCredentialsForUid(keyStore, uid2).values());
            return arrayList;
        }

        private boolean isAsymmetric(KeyStore keyStore, String str, int i) throws UnrecoverableKeyException {
            KeyCharacteristics keyCharacteristics = new KeyCharacteristics();
            int keyCharacteristics2 = keyStore.getKeyCharacteristics(str, (KeymasterBlob) null, (KeymasterBlob) null, i, keyCharacteristics);
            if (keyCharacteristics2 != 1) {
                throw ((UnrecoverableKeyException) new UnrecoverableKeyException("Failed to obtain information about key").initCause(KeyStore.getKeyStoreException(keyCharacteristics2)));
            }
            Integer num = keyCharacteristics.getEnum(268435458);
            if (num != null) {
                return num.intValue() == 1 || num.intValue() == 3;
            }
            throw new UnrecoverableKeyException("Key algorithm unknown");
        }

        private SortedMap<String, Credential> getCredentialsForUid(KeyStore keyStore, int i) {
            KeyStore keyStore2 = keyStore;
            int i2 = i;
            TreeMap treeMap = new TreeMap();
            Credential.Type[] values = Credential.Type.values();
            int length = values.length;
            int i3 = 0;
            while (i3 < length) {
                Credential.Type type = values[i3];
                String[] strArr = type.prefix;
                int length2 = strArr.length;
                int i4 = 0;
                while (i4 < length2) {
                    String str = strArr[i4];
                    String[] list = keyStore2.list(str, i2);
                    int length3 = list.length;
                    int i5 = 0;
                    while (i5 < length3) {
                        String str2 = list[i5];
                        Credential.Type[] typeArr = values;
                        if (UserHandle.getAppId(i) != 1000 || (!str2.startsWith("profile_key_name_encrypt_") && !str2.startsWith("profile_key_name_decrypt_") && !str2.startsWith("synthetic_password_"))) {
                            try {
                                if (type == Credential.Type.USER_KEY) {
                                    try {
                                        if (!isAsymmetric(keyStore2, str + str2, i2)) {
                                        }
                                    } catch (UnrecoverableKeyException e) {
                                        e = e;
                                        Log.e("UserCredentialsSettings", "Unable to determine algorithm of key: " + str + str2, e);
                                        i5++;
                                        values = typeArr;
                                        keyStore2 = keyStore;
                                        i2 = i;
                                    }
                                }
                                Credential credential = (Credential) treeMap.get(str2);
                                if (credential == null) {
                                    credential = new Credential(str2, i2);
                                    treeMap.put(str2, credential);
                                }
                                credential.storedTypes.add(type);
                            } catch (UnrecoverableKeyException e2) {
                                e = e2;
                            }
                        }
                        i5++;
                        values = typeArr;
                        keyStore2 = keyStore;
                        i2 = i;
                    }
                    i4++;
                    keyStore2 = keyStore;
                    i2 = i;
                }
                i3++;
                keyStore2 = keyStore;
                i2 = i;
            }
            return treeMap;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onPostExecute(List<Credential> list) {
            if (!UserCredentialsSettings.this.isAdded()) {
                return;
            }
            if (list == null || list.size() == 0) {
                TextView textView = (TextView) UserCredentialsSettings.this.getActivity().findViewById(16908292);
                textView.setText(R.string.user_credential_none_installed);
                UserCredentialsSettings.this.setEmptyView(textView);
            } else {
                UserCredentialsSettings.this.setEmptyView(null);
            }
            UserCredentialsSettings.this.getListView().setAdapter(new CredentialAdapter(list, UserCredentialsSettings.this));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class CredentialAdapter extends RecyclerView.Adapter<ViewHolder> {
        private final List<Credential> mItems;
        private final View.OnClickListener mListener;

        public CredentialAdapter(List<Credential> list, View.OnClickListener onClickListener) {
            this.mItems = list;
            this.mListener = onClickListener;
        }

        @Override // android.support.v7.widget.RecyclerView.Adapter
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_credential_preference, viewGroup, false));
        }

        @Override // android.support.v7.widget.RecyclerView.Adapter
        public void onBindViewHolder(ViewHolder viewHolder, int i) {
            UserCredentialsSettings.getCredentialView(this.mItems.get(i), R.layout.user_credential_preference, viewHolder.itemView, null, false);
            viewHolder.itemView.setTag(this.mItems.get(i));
            viewHolder.itemView.setOnClickListener(this.mListener);
        }

        @Override // android.support.v7.widget.RecyclerView.Adapter
        public int getItemCount() {
            return this.mItems.size();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View view) {
            super(view);
        }
    }

    static {
        credentialViewTypes.put(R.id.contents_userkey, Credential.Type.USER_KEY);
        credentialViewTypes.put(R.id.contents_usercrt, Credential.Type.USER_CERTIFICATE);
        credentialViewTypes.put(R.id.contents_cacrt, Credential.Type.CA_CERTIFICATE);
    }

    protected static View getCredentialView(Credential credential, int i, View view, ViewGroup viewGroup, boolean z) {
        int i2;
        if (view == null) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(i, viewGroup, false);
        }
        ((TextView) view.findViewById(R.id.alias)).setText(credential.alias);
        TextView textView = (TextView) view.findViewById(R.id.purpose);
        if (credential.isSystem()) {
            i2 = R.string.credential_for_vpn_and_apps;
        } else {
            i2 = R.string.credential_for_wifi;
        }
        textView.setText(i2);
        view.findViewById(R.id.contents).setVisibility(z ? 0 : 8);
        if (z) {
            for (int i3 = 0; i3 < credentialViewTypes.size(); i3++) {
                view.findViewById(credentialViewTypes.keyAt(i3)).setVisibility(credential.storedTypes.contains(credentialViewTypes.valueAt(i3)) ? 0 : 8);
            }
        }
        return view;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static class Credential implements Parcelable {
        public static final Parcelable.Creator<Credential> CREATOR = new Parcelable.Creator<Credential>() { // from class: com.android.settings.UserCredentialsSettings.Credential.1
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.Creator
            public Credential createFromParcel(Parcel parcel) {
                return new Credential(parcel);
            }

            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.Creator
            public Credential[] newArray(int i) {
                return new Credential[i];
            }
        };
        final String alias;
        final EnumSet<Type> storedTypes;
        final int uid;

        /* JADX INFO: Access modifiers changed from: package-private */
        /* loaded from: classes.dex */
        public enum Type {
            CA_CERTIFICATE("CACERT_"),
            USER_CERTIFICATE("USRCERT_"),
            USER_KEY("USRPKEY_", "USRSKEY_");
            
            final String[] prefix;

            Type(String... strArr) {
                this.prefix = strArr;
            }
        }

        Credential(String str, int i) {
            this.storedTypes = EnumSet.noneOf(Type.class);
            this.alias = str;
            this.uid = i;
        }

        Credential(Parcel parcel) {
            this(parcel.readString(), parcel.readInt());
            Type[] values;
            long readLong = parcel.readLong();
            for (Type type : Type.values()) {
                if (((1 << type.ordinal()) & readLong) != 0) {
                    this.storedTypes.add(type);
                }
            }
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(this.alias);
            parcel.writeInt(this.uid);
            Iterator it = this.storedTypes.iterator();
            long j = 0;
            while (it.hasNext()) {
                j |= 1 << ((Type) it.next()).ordinal();
            }
            parcel.writeLong(j);
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        public boolean isSystem() {
            return UserHandle.getAppId(this.uid) == 1000;
        }
    }
}

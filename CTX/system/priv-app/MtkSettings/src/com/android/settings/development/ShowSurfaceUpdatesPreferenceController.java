package com.android.settings.development;

import android.content.Context;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;
/* loaded from: classes.dex */
public class ShowSurfaceUpdatesPreferenceController extends DeveloperOptionsPreferenceController implements Preference.OnPreferenceChangeListener, PreferenceControllerMixin {
    static final int SURFACE_FLINGER_READ_CODE = 1010;
    static final String SURFACE_FLINGER_SERVICE_KEY = "SurfaceFlinger";
    private final IBinder mSurfaceFlinger;

    public ShowSurfaceUpdatesPreferenceController(Context context) {
        super(context);
        this.mSurfaceFlinger = ServiceManager.getService(SURFACE_FLINGER_SERVICE_KEY);
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public String getPreferenceKey() {
        return "show_screen_updates";
    }

    @Override // android.support.v7.preference.Preference.OnPreferenceChangeListener
    public boolean onPreferenceChange(Preference preference, Object obj) {
        writeShowUpdatesSetting(((Boolean) obj).booleanValue());
        return true;
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public void updateState(Preference preference) {
        updateShowUpdatesSetting();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settingslib.development.DeveloperOptionsPreferenceController
    public void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        SwitchPreference switchPreference = (SwitchPreference) this.mPreference;
        if (switchPreference.isChecked()) {
            writeShowUpdatesSetting(false);
            switchPreference.setChecked(false);
        }
    }

    void updateShowUpdatesSetting() {
        try {
            if (this.mSurfaceFlinger != null) {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                obtain.writeInterfaceToken("android.ui.ISurfaceComposer");
                this.mSurfaceFlinger.transact(SURFACE_FLINGER_READ_CODE, obtain, obtain2, 0);
                obtain2.readInt();
                obtain2.readInt();
                ((SwitchPreference) this.mPreference).setChecked(obtain2.readInt() != 0);
                obtain2.recycle();
                obtain.recycle();
            }
        } catch (RemoteException e) {
        }
    }

    void writeShowUpdatesSetting(boolean z) {
        try {
            if (this.mSurfaceFlinger != null) {
                Parcel obtain = Parcel.obtain();
                obtain.writeInterfaceToken("android.ui.ISurfaceComposer");
                obtain.writeInt(z ? 1 : 0);
                this.mSurfaceFlinger.transact(1002, obtain, null, 0);
                obtain.recycle();
            }
        } catch (RemoteException e) {
        }
        updateShowUpdatesSetting();
    }
}

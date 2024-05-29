package com.android.settings.deviceinfo;

import android.content.Context;
import android.content.Intent;
import android.os.storage.VolumeInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.android.settings.R;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnCreateOptionsMenu;
import com.android.settingslib.core.lifecycle.events.OnOptionsItemSelected;
import com.android.settingslib.core.lifecycle.events.OnPrepareOptionsMenu;
import com.android.settingslib.wrapper.PackageManagerWrapper;
import java.util.Objects;
/* loaded from: classes.dex */
public class PrivateVolumeOptionMenuController implements LifecycleObserver, OnCreateOptionsMenu, OnOptionsItemSelected, OnPrepareOptionsMenu {
    private Context mContext;
    private PackageManagerWrapper mPm;
    private VolumeInfo mVolumeInfo;

    public PrivateVolumeOptionMenuController(Context context, VolumeInfo volumeInfo, PackageManagerWrapper packageManagerWrapper) {
        this.mContext = context;
        this.mVolumeInfo = volumeInfo;
        this.mPm = packageManagerWrapper;
    }

    @Override // com.android.settingslib.core.lifecycle.events.OnCreateOptionsMenu
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menu.add(0, 100, 0, R.string.storage_menu_migrate);
    }

    @Override // com.android.settingslib.core.lifecycle.events.OnPrepareOptionsMenu
    public void onPrepareOptionsMenu(Menu menu) {
        if (this.mVolumeInfo == null) {
            return;
        }
        VolumeInfo primaryStorageCurrentVolume = this.mPm.getPrimaryStorageCurrentVolume();
        MenuItem findItem = menu.findItem(100);
        if (findItem != null) {
            boolean z = true;
            findItem.setVisible((primaryStorageCurrentVolume == null || primaryStorageCurrentVolume.getType() != 1 || Objects.equals(this.mVolumeInfo, primaryStorageCurrentVolume)) ? false : false);
        }
    }

    @Override // com.android.settingslib.core.lifecycle.events.OnOptionsItemSelected
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == 100) {
            Intent intent = new Intent(this.mContext, StorageWizardMigrateConfirm.class);
            intent.putExtra("android.os.storage.extra.VOLUME_ID", this.mVolumeInfo.getId());
            this.mContext.startActivity(intent);
            return true;
        }
        return false;
    }
}

package com.android.settingslib.deviceinfo;

import android.app.usage.ExternalStorageStats;
import android.app.usage.StorageStats;
import android.app.usage.StorageStatsManager;
import android.content.Context;
import android.content.pm.UserInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.VolumeInfo;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseLongArray;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
/* loaded from: classes.dex */
public class StorageMeasurement {
    private final Context mContext;
    private WeakReference<MeasurementReceiver> mReceiver;
    private final VolumeInfo mSharedVolume;
    private final StorageStatsManager mStats;
    private final UserManager mUser;
    private final VolumeInfo mVolume;

    /* loaded from: classes.dex */
    public interface MeasurementReceiver {
        void onDetailsChanged(MeasurementDetails measurementDetails);
    }

    /* loaded from: classes.dex */
    public static class MeasurementDetails {
        public long availSize;
        public long cacheSize;
        public long totalSize;
        public SparseLongArray usersSize = new SparseLongArray();
        public SparseLongArray appsSize = new SparseLongArray();
        public SparseArray<HashMap<String, Long>> mediaSize = new SparseArray<>();
        public SparseLongArray miscSize = new SparseLongArray();

        public String toString() {
            return "MeasurementDetails: [totalSize: " + this.totalSize + " availSize: " + this.availSize + " cacheSize: " + this.cacheSize + " mediaSize: " + this.mediaSize + " miscSize: " + this.miscSize + "usersSize: " + this.usersSize + "]";
        }
    }

    public StorageMeasurement(Context context, VolumeInfo volumeInfo, VolumeInfo volumeInfo2) {
        this.mContext = context.getApplicationContext();
        this.mUser = (UserManager) this.mContext.getSystemService(UserManager.class);
        this.mStats = (StorageStatsManager) this.mContext.getSystemService(StorageStatsManager.class);
        this.mVolume = volumeInfo;
        this.mSharedVolume = volumeInfo2;
    }

    public void setReceiver(MeasurementReceiver measurementReceiver) {
        if (this.mReceiver == null || this.mReceiver.get() == null) {
            this.mReceiver = new WeakReference<>(measurementReceiver);
        }
    }

    public void forceMeasure() {
        measure();
    }

    public void measure() {
        new MeasureTask().execute(new Void[0]);
    }

    public void onDestroy() {
        this.mReceiver = null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class MeasureTask extends AsyncTask<Void, Void, MeasurementDetails> {
        private MeasureTask() {
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public MeasurementDetails doInBackground(Void... voidArr) {
            return StorageMeasurement.this.measureExactStorage();
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onPostExecute(MeasurementDetails measurementDetails) {
            MeasurementReceiver measurementReceiver = StorageMeasurement.this.mReceiver != null ? (MeasurementReceiver) StorageMeasurement.this.mReceiver.get() : null;
            if (measurementReceiver != null) {
                measurementReceiver.onDetailsChanged(measurementDetails);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public MeasurementDetails measureExactStorage() {
        List<UserInfo> users = this.mUser.getUsers();
        long elapsedRealtime = SystemClock.elapsedRealtime();
        MeasurementDetails measurementDetails = new MeasurementDetails();
        if (this.mVolume == null) {
            return measurementDetails;
        }
        if (this.mVolume.getType() == 0) {
            measurementDetails.totalSize = this.mVolume.getPath().getTotalSpace();
            measurementDetails.availSize = this.mVolume.getPath().getUsableSpace();
            return measurementDetails;
        }
        try {
            measurementDetails.totalSize = this.mStats.getTotalBytes(this.mVolume.fsUuid);
            measurementDetails.availSize = this.mStats.getFreeBytes(this.mVolume.fsUuid);
            long elapsedRealtime2 = SystemClock.elapsedRealtime();
            Log.d("StorageMeasurement", "Measured total storage in " + (elapsedRealtime2 - elapsedRealtime) + "ms");
            if (this.mSharedVolume != null && this.mSharedVolume.isMountedReadable()) {
                for (UserInfo userInfo : users) {
                    HashMap<String, Long> hashMap = new HashMap<>();
                    measurementDetails.mediaSize.put(userInfo.id, hashMap);
                    try {
                        ExternalStorageStats queryExternalStatsForUser = this.mStats.queryExternalStatsForUser(this.mSharedVolume.fsUuid, UserHandle.of(userInfo.id));
                        addValue(measurementDetails.usersSize, userInfo.id, queryExternalStatsForUser.getTotalBytes());
                        hashMap.put(Environment.DIRECTORY_MUSIC, Long.valueOf(queryExternalStatsForUser.getAudioBytes()));
                        hashMap.put(Environment.DIRECTORY_MOVIES, Long.valueOf(queryExternalStatsForUser.getVideoBytes()));
                        hashMap.put(Environment.DIRECTORY_PICTURES, Long.valueOf(queryExternalStatsForUser.getImageBytes()));
                        addValue(measurementDetails.miscSize, userInfo.id, ((queryExternalStatsForUser.getTotalBytes() - queryExternalStatsForUser.getAudioBytes()) - queryExternalStatsForUser.getVideoBytes()) - queryExternalStatsForUser.getImageBytes());
                    } catch (IOException e) {
                        Log.w("StorageMeasurement", e);
                    }
                }
            }
            long elapsedRealtime3 = SystemClock.elapsedRealtime();
            Log.d("StorageMeasurement", "Measured shared storage in " + (elapsedRealtime3 - elapsedRealtime2) + "ms");
            if (this.mVolume.getType() == 1 && this.mVolume.isMountedReadable()) {
                for (UserInfo userInfo2 : users) {
                    try {
                        StorageStats queryStatsForUser = this.mStats.queryStatsForUser(this.mVolume.fsUuid, UserHandle.of(userInfo2.id));
                        if (userInfo2.id == UserHandle.myUserId()) {
                            addValue(measurementDetails.usersSize, userInfo2.id, queryStatsForUser.getCodeBytes());
                        }
                        addValue(measurementDetails.usersSize, userInfo2.id, queryStatsForUser.getDataBytes());
                        addValue(measurementDetails.appsSize, userInfo2.id, queryStatsForUser.getCodeBytes() + queryStatsForUser.getDataBytes());
                        measurementDetails.cacheSize += queryStatsForUser.getCacheBytes();
                    } catch (IOException e2) {
                        Log.w("StorageMeasurement", e2);
                    }
                }
            }
            long elapsedRealtime4 = SystemClock.elapsedRealtime();
            Log.d("StorageMeasurement", "Measured private storage in " + (elapsedRealtime4 - elapsedRealtime3) + "ms");
            return measurementDetails;
        } catch (IOException e3) {
            Log.w("StorageMeasurement", e3);
            return measurementDetails;
        }
    }

    private static void addValue(SparseLongArray sparseLongArray, int i, long j) {
        sparseLongArray.put(i, sparseLongArray.get(i) + j);
    }
}

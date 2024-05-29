package com.android.settings.datetime.timezone.model;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import com.android.settingslib.utils.AsyncLoader;
/* loaded from: classes.dex */
public class TimeZoneDataLoader extends AsyncLoader<TimeZoneData> {

    /* loaded from: classes.dex */
    public interface OnDataReadyCallback {
        void onTimeZoneDataReady(TimeZoneData timeZoneData);
    }

    public TimeZoneDataLoader(Context context) {
        super(context);
    }

    @Override // android.content.AsyncTaskLoader
    public TimeZoneData loadInBackground() {
        return TimeZoneData.getInstance();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settingslib.utils.AsyncLoader
    public void onDiscardResult(TimeZoneData timeZoneData) {
    }

    /* loaded from: classes.dex */
    public static class LoaderCreator implements LoaderManager.LoaderCallbacks<TimeZoneData> {
        private final OnDataReadyCallback mCallback;
        private final Context mContext;

        public LoaderCreator(Context context, OnDataReadyCallback onDataReadyCallback) {
            this.mContext = context;
            this.mCallback = onDataReadyCallback;
        }

        @Override // android.app.LoaderManager.LoaderCallbacks
        public Loader<TimeZoneData> onCreateLoader(int i, Bundle bundle) {
            return new TimeZoneDataLoader(this.mContext);
        }

        @Override // android.app.LoaderManager.LoaderCallbacks
        public void onLoadFinished(Loader<TimeZoneData> loader, TimeZoneData timeZoneData) {
            if (this.mCallback != null) {
                this.mCallback.onTimeZoneDataReady(timeZoneData);
            }
        }

        @Override // android.app.LoaderManager.LoaderCallbacks
        public void onLoaderReset(Loader<TimeZoneData> loader) {
        }
    }
}

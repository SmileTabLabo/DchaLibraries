package com.android.settingslib.net;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.net.INetworkStatsSession;
import android.net.NetworkStats;
import android.net.NetworkTemplate;
import android.os.Bundle;
import android.os.RemoteException;
/* loaded from: classes.dex */
public class SummaryForAllUidLoader extends AsyncTaskLoader<NetworkStats> {
    private final Bundle mArgs;
    private final INetworkStatsSession mSession;

    public static Bundle buildArgs(NetworkTemplate template, long start, long end) {
        Bundle args = new Bundle();
        args.putParcelable("template", template);
        args.putLong("start", start);
        args.putLong("end", end);
        return args;
    }

    public SummaryForAllUidLoader(Context context, INetworkStatsSession session, Bundle args) {
        super(context);
        this.mSession = session;
        this.mArgs = args;
    }

    @Override // android.content.Loader
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // android.content.AsyncTaskLoader
    public NetworkStats loadInBackground() {
        NetworkTemplate template = this.mArgs.getParcelable("template");
        long start = this.mArgs.getLong("start");
        long end = this.mArgs.getLong("end");
        try {
            return this.mSession.getSummaryForAllUid(template, start, end, false);
        } catch (RemoteException e) {
            return null;
        }
    }

    @Override // android.content.Loader
    protected void onStopLoading() {
        super.onStopLoading();
        cancelLoad();
    }

    @Override // android.content.Loader
    protected void onReset() {
        super.onReset();
        cancelLoad();
    }
}

package com.android.settings.search;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import androidx.slice.Slice;
import androidx.slice.SliceItem;
import androidx.slice.SliceMetadata;
import androidx.slice.SliceViewManager;
import androidx.slice.core.SliceQuery;
import androidx.slice.widget.ListContent;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.overlay.FeatureFactory;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
/* loaded from: classes.dex */
public class DeviceIndexUpdateJobService extends JobService {
    @VisibleForTesting
    protected boolean mRunningJob;

    @Override // android.app.job.JobService
    public boolean onStartJob(final JobParameters jobParameters) {
        if (!this.mRunningJob) {
            this.mRunningJob = true;
            Thread thread = new Thread(new Runnable() { // from class: com.android.settings.search.-$$Lambda$DeviceIndexUpdateJobService$CyjXGsZVpAu5iTckScg1Ee8_bGU
                @Override // java.lang.Runnable
                public final void run() {
                    DeviceIndexUpdateJobService.this.updateIndex(jobParameters);
                }
            });
            thread.setPriority(1);
            thread.start();
        }
        return true;
    }

    @Override // android.app.job.JobService
    public boolean onStopJob(JobParameters jobParameters) {
        if (this.mRunningJob) {
            this.mRunningJob = false;
            return true;
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @VisibleForTesting
    public void updateIndex(JobParameters jobParameters) {
        DeviceIndexFeatureProvider deviceIndexFeatureProvider = FeatureFactory.getFactory(this).getDeviceIndexFeatureProvider();
        SliceViewManager sliceViewManager = getSliceViewManager();
        Uri build = new Uri.Builder().scheme("content").authority("com.android.settings.slices").build();
        Uri build2 = new Uri.Builder().scheme("content").authority("android.settings.slices").build();
        Collection<Uri> sliceDescendants = sliceViewManager.getSliceDescendants(build);
        sliceDescendants.addAll(sliceViewManager.getSliceDescendants(build2));
        deviceIndexFeatureProvider.clearIndex(this);
        for (Uri uri : sliceDescendants) {
            if (!this.mRunningJob) {
                return;
            }
            Slice bindSliceSynchronous = bindSliceSynchronous(sliceViewManager, uri);
            SliceMetadata metadata = getMetadata(bindSliceSynchronous);
            CharSequence findTitle = findTitle(bindSliceSynchronous, metadata);
            if (findTitle != null) {
                deviceIndexFeatureProvider.index(this, findTitle, uri, DeviceIndexFeatureProvider.createDeepLink(new Intent("com.android.settings.action.VIEW_SLICE").setPackage(getPackageName()).putExtra("slice", uri.toString()).toUri(2)), metadata.getSliceKeywords());
            }
        }
        jobFinished(jobParameters, false);
    }

    protected SliceViewManager getSliceViewManager() {
        return SliceViewManager.getInstance(this);
    }

    protected SliceMetadata getMetadata(Slice slice) {
        return SliceMetadata.from(this, slice);
    }

    protected CharSequence findTitle(Slice slice, SliceMetadata sliceMetadata) {
        ListContent listContent = new ListContent(null, slice);
        SliceItem headerItem = listContent.getHeaderItem();
        if (headerItem == null) {
            if (listContent.getRowItems().size() == 0) {
                return null;
            }
            headerItem = listContent.getRowItems().get(0);
        }
        SliceItem find = SliceQuery.find(headerItem, "text", "title", (String) null);
        if (find != null) {
            return find.getText();
        }
        SliceItem find2 = SliceQuery.find(headerItem, "text", "large", (String) null);
        if (find2 != null) {
            return find2.getText();
        }
        SliceItem find3 = SliceQuery.find(headerItem, "text");
        if (find3 != null) {
            return find3.getText();
        }
        return null;
    }

    protected Slice bindSliceSynchronous(final SliceViewManager sliceViewManager, final Uri uri) {
        final Slice[] sliceArr = new Slice[1];
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        SliceViewManager.SliceCallback sliceCallback = new SliceViewManager.SliceCallback() { // from class: com.android.settings.search.DeviceIndexUpdateJobService.1
            @Override // androidx.slice.SliceViewManager.SliceCallback
            public void onSliceUpdated(Slice slice) {
                try {
                    if (SliceMetadata.from(DeviceIndexUpdateJobService.this, slice).getLoadingState() == 2) {
                        sliceArr[0] = slice;
                        countDownLatch.countDown();
                        sliceViewManager.unregisterSliceCallback(uri, this);
                    }
                } catch (Exception e) {
                    Log.w("DeviceIndexUpdate", uri + " cannot be indexed", e);
                    sliceArr[0] = slice;
                }
            }
        };
        sliceViewManager.registerSliceCallback(uri, sliceCallback);
        sliceCallback.onSliceUpdated(sliceViewManager.bindSlice(uri));
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
        }
        return sliceArr[0];
    }
}

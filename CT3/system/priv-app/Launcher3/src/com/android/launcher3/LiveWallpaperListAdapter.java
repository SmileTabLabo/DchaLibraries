package com.android.launcher3;

import android.app.WallpaperInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import com.android.launcher3.WallpaperPickerActivity;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.xmlpull.v1.XmlPullParserException;
/* loaded from: a.zip:com/android/launcher3/LiveWallpaperListAdapter.class */
public class LiveWallpaperListAdapter extends BaseAdapter implements ListAdapter {
    private final LayoutInflater mInflater;
    private final PackageManager mPackageManager;
    List<LiveWallpaperTile> mWallpapers;

    /* loaded from: a.zip:com/android/launcher3/LiveWallpaperListAdapter$LiveWallpaperEnumerator.class */
    private class LiveWallpaperEnumerator extends AsyncTask<List<ResolveInfo>, LiveWallpaperTile, Void> {
        private Context mContext;
        private int mWallpaperPosition = 0;
        final LiveWallpaperListAdapter this$0;

        public LiveWallpaperEnumerator(LiveWallpaperListAdapter liveWallpaperListAdapter, Context context) {
            this.this$0 = liveWallpaperListAdapter;
            this.mContext = context;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public Void doInBackground(List<ResolveInfo>... listArr) {
            PackageManager packageManager = this.mContext.getPackageManager();
            List<ResolveInfo> list = listArr[0];
            Collections.sort(list, new Comparator<ResolveInfo>(this, packageManager) { // from class: com.android.launcher3.LiveWallpaperListAdapter.LiveWallpaperEnumerator.1
                final Collator mCollator = Collator.getInstance();
                final LiveWallpaperEnumerator this$1;
                final PackageManager val$packageManager;

                {
                    this.this$1 = this;
                    this.val$packageManager = packageManager;
                }

                @Override // java.util.Comparator
                public int compare(ResolveInfo resolveInfo, ResolveInfo resolveInfo2) {
                    return this.mCollator.compare(resolveInfo.loadLabel(this.val$packageManager), resolveInfo2.loadLabel(this.val$packageManager));
                }
            });
            for (ResolveInfo resolveInfo : list) {
                try {
                    WallpaperInfo wallpaperInfo = new WallpaperInfo(this.mContext, resolveInfo);
                    Drawable loadThumbnail = wallpaperInfo.loadThumbnail(packageManager);
                    Intent intent = new Intent("android.service.wallpaper.WallpaperService");
                    intent.setClassName(wallpaperInfo.getPackageName(), wallpaperInfo.getServiceName());
                    publishProgress(new LiveWallpaperTile(loadThumbnail, wallpaperInfo, intent));
                } catch (IOException e) {
                    Log.w("LiveWallpaperListAdapter", "Skipping wallpaper " + resolveInfo.serviceInfo, e);
                } catch (XmlPullParserException e2) {
                    Log.w("LiveWallpaperListAdapter", "Skipping wallpaper " + resolveInfo.serviceInfo, e2);
                }
            }
            publishProgress(null);
            return null;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onProgressUpdate(LiveWallpaperTile... liveWallpaperTileArr) {
            for (LiveWallpaperTile liveWallpaperTile : liveWallpaperTileArr) {
                if (liveWallpaperTile == null) {
                    this.this$0.notifyDataSetChanged();
                    return;
                }
                if (liveWallpaperTile.mThumbnail != null) {
                    liveWallpaperTile.mThumbnail.setDither(true);
                }
                if (this.mWallpaperPosition < this.this$0.mWallpapers.size()) {
                    this.this$0.mWallpapers.set(this.mWallpaperPosition, liveWallpaperTile);
                } else {
                    this.this$0.mWallpapers.add(liveWallpaperTile);
                }
                this.mWallpaperPosition++;
            }
        }
    }

    /* loaded from: a.zip:com/android/launcher3/LiveWallpaperListAdapter$LiveWallpaperTile.class */
    public static class LiveWallpaperTile extends WallpaperPickerActivity.WallpaperTileInfo {
        WallpaperInfo mInfo;
        Drawable mThumbnail;

        public LiveWallpaperTile(Drawable drawable, WallpaperInfo wallpaperInfo, Intent intent) {
            this.mThumbnail = drawable;
            this.mInfo = wallpaperInfo;
        }

        @Override // com.android.launcher3.WallpaperPickerActivity.WallpaperTileInfo
        public void onClick(WallpaperPickerActivity wallpaperPickerActivity) {
            Intent intent = new Intent("android.service.wallpaper.CHANGE_LIVE_WALLPAPER");
            intent.putExtra("android.service.wallpaper.extra.LIVE_WALLPAPER_COMPONENT", this.mInfo.getComponent());
            wallpaperPickerActivity.startActivityForResultSafely(intent, 6);
        }
    }

    public LiveWallpaperListAdapter(Context context) {
        this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
        this.mPackageManager = context.getPackageManager();
        List<ResolveInfo> queryIntentServices = this.mPackageManager.queryIntentServices(new Intent("android.service.wallpaper.WallpaperService"), 128);
        this.mWallpapers = new ArrayList();
        new LiveWallpaperEnumerator(this, context).execute(queryIntentServices);
    }

    @Override // android.widget.Adapter
    public int getCount() {
        if (this.mWallpapers == null) {
            return 0;
        }
        return this.mWallpapers.size();
    }

    @Override // android.widget.Adapter
    public LiveWallpaperTile getItem(int i) {
        return this.mWallpapers.get(i);
    }

    @Override // android.widget.Adapter
    public long getItemId(int i) {
        return i;
    }

    @Override // android.widget.Adapter
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = this.mInflater.inflate(2130968607, viewGroup, false);
        }
        LiveWallpaperTile liveWallpaperTile = this.mWallpapers.get(i);
        liveWallpaperTile.setView(view);
        ImageView imageView = (ImageView) view.findViewById(2131296324);
        ImageView imageView2 = (ImageView) view.findViewById(2131296326);
        if (liveWallpaperTile.mThumbnail != null) {
            imageView.setImageDrawable(liveWallpaperTile.mThumbnail);
            imageView2.setVisibility(8);
        } else {
            imageView2.setImageDrawable(liveWallpaperTile.mInfo.loadIcon(this.mPackageManager));
            imageView2.setVisibility(0);
        }
        ((TextView) view.findViewById(2131296325)).setText(liveWallpaperTile.mInfo.loadLabel(this.mPackageManager));
        return view;
    }
}

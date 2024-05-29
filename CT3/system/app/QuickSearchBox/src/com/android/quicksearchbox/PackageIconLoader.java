package com.android.quicksearchbox;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import com.android.quicksearchbox.util.CachedLater;
import com.android.quicksearchbox.util.NamedTask;
import com.android.quicksearchbox.util.NamedTaskExecutor;
import com.android.quicksearchbox.util.Now;
import com.android.quicksearchbox.util.NowOrLater;
import com.android.quicksearchbox.util.Util;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
/* loaded from: a.zip:com/android/quicksearchbox/PackageIconLoader.class */
public class PackageIconLoader implements IconLoader {
    private final Context mContext;
    private final NamedTaskExecutor mIconLoaderExecutor;
    private Context mPackageContext;
    private final String mPackageName;
    private final Handler mUiThread;

    /* loaded from: a.zip:com/android/quicksearchbox/PackageIconLoader$IconLaterTask.class */
    private class IconLaterTask extends CachedLater<Drawable> implements NamedTask {
        private final Uri mUri;
        final PackageIconLoader this$0;

        public IconLaterTask(PackageIconLoader packageIconLoader, Uri uri) {
            this.this$0 = packageIconLoader;
            this.mUri = uri;
        }

        private Drawable getIcon() {
            try {
                return this.this$0.getDrawable(this.mUri);
            } catch (Throwable th) {
                Log.e("QSB.PackageIconLoader", "Failed to load icon " + this.mUri, th);
                return null;
            }
        }

        @Override // com.android.quicksearchbox.util.CachedLater
        protected void create() {
            this.this$0.mIconLoaderExecutor.execute(this);
        }

        @Override // com.android.quicksearchbox.util.NamedTask
        public String getName() {
            return this.this$0.mPackageName;
        }

        @Override // java.lang.Runnable
        public void run() {
            this.this$0.mUiThread.post(new Runnable(this, getIcon()) { // from class: com.android.quicksearchbox.PackageIconLoader.IconLaterTask.1
                final IconLaterTask this$1;
                final Drawable val$icon;

                {
                    this.this$1 = this;
                    this.val$icon = r5;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$1.store(this.val$icon);
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/quicksearchbox/PackageIconLoader$OpenResourceIdResult.class */
    public class OpenResourceIdResult {
        public int id;
        public Resources r;
        final PackageIconLoader this$0;

        private OpenResourceIdResult(PackageIconLoader packageIconLoader) {
            this.this$0 = packageIconLoader;
        }

        /* synthetic */ OpenResourceIdResult(PackageIconLoader packageIconLoader, OpenResourceIdResult openResourceIdResult) {
            this(packageIconLoader);
        }
    }

    public PackageIconLoader(Context context, String str, Handler handler, NamedTaskExecutor namedTaskExecutor) {
        this.mContext = context;
        this.mPackageName = str;
        this.mUiThread = handler;
        this.mIconLoaderExecutor = namedTaskExecutor;
    }

    private boolean ensurePackageContext() {
        if (this.mPackageContext == null) {
            try {
                this.mPackageContext = this.mContext.createPackageContext(this.mPackageName, 4);
                return true;
            } catch (PackageManager.NameNotFoundException e) {
                Log.e("QSB.PackageIconLoader", "Application not found " + this.mPackageName);
                return false;
            }
        }
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Drawable getDrawable(Uri uri) {
        try {
            if ("android.resource".equals(uri.getScheme())) {
                OpenResourceIdResult resourceId = getResourceId(uri);
                try {
                    return resourceId.r.getDrawable(resourceId.id);
                } catch (Resources.NotFoundException e) {
                    throw new FileNotFoundException("Resource does not exist: " + uri);
                }
            }
            InputStream openInputStream = this.mPackageContext.getContentResolver().openInputStream(uri);
            if (openInputStream == null) {
                throw new FileNotFoundException("Failed to open " + uri);
            }
            Drawable createFromStream = Drawable.createFromStream(openInputStream, null);
            try {
                openInputStream.close();
            } catch (IOException e2) {
                Log.e("QSB.PackageIconLoader", "Error closing icon stream for " + uri, e2);
            }
            return createFromStream;
        } catch (FileNotFoundException e3) {
            Log.w("QSB.PackageIconLoader", "Icon not found: " + uri + ", " + e3.getMessage());
            return null;
        }
        Log.w("QSB.PackageIconLoader", "Icon not found: " + uri + ", " + e3.getMessage());
        return null;
    }

    private OpenResourceIdResult getResourceId(Uri uri) throws FileNotFoundException {
        int parseInt;
        String authority = uri.getAuthority();
        if (TextUtils.isEmpty(authority)) {
            throw new FileNotFoundException("No authority: " + uri);
        }
        try {
            Resources resourcesForApplication = this.mPackageContext.getPackageManager().getResourcesForApplication(authority);
            List<String> pathSegments = uri.getPathSegments();
            if (pathSegments == null) {
                throw new FileNotFoundException("No path: " + uri);
            }
            int size = pathSegments.size();
            if (size == 1) {
                try {
                    parseInt = Integer.parseInt(pathSegments.get(0));
                } catch (NumberFormatException e) {
                    throw new FileNotFoundException("Single path segment is not a resource ID: " + uri);
                }
            } else if (size != 2) {
                throw new FileNotFoundException("More than two path segments: " + uri);
            } else {
                parseInt = resourcesForApplication.getIdentifier(pathSegments.get(1), pathSegments.get(0), authority);
            }
            if (parseInt == 0) {
                throw new FileNotFoundException("No resource found for: " + uri);
            }
            OpenResourceIdResult openResourceIdResult = new OpenResourceIdResult(this, null);
            openResourceIdResult.r = resourcesForApplication;
            openResourceIdResult.id = parseInt;
            return openResourceIdResult;
        } catch (PackageManager.NameNotFoundException e2) {
            throw new FileNotFoundException("Failed to get resources: " + e2);
        }
    }

    @Override // com.android.quicksearchbox.IconLoader
    public NowOrLater<Drawable> getIcon(String str) {
        NowOrLater now;
        if (TextUtils.isEmpty(str) || "0".equals(str)) {
            return new Now(null);
        }
        if (ensurePackageContext()) {
            try {
                now = new Now(this.mPackageContext.getResources().getDrawable(Integer.parseInt(str)));
            } catch (Resources.NotFoundException e) {
                Log.w("QSB.PackageIconLoader", "Icon resource not found: " + str);
                now = new Now(null);
            } catch (NumberFormatException e2) {
                Uri parse = Uri.parse(str);
                now = "android.resource".equals(parse.getScheme()) ? new Now(getDrawable(parse)) : new IconLaterTask(this, parse);
            }
            return now;
        }
        return new Now(null);
    }

    @Override // com.android.quicksearchbox.IconLoader
    public Uri getIconUri(String str) {
        if (TextUtils.isEmpty(str) || "0".equals(str) || !ensurePackageContext()) {
            return null;
        }
        try {
            return Util.getResourceUri(this.mPackageContext, Integer.parseInt(str));
        } catch (NumberFormatException e) {
            return Uri.parse(str);
        }
    }
}

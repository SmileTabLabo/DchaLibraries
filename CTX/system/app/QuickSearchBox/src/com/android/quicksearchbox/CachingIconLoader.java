package com.android.quicksearchbox;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import com.android.quicksearchbox.util.CachedLater;
import com.android.quicksearchbox.util.Consumer;
import com.android.quicksearchbox.util.Now;
import com.android.quicksearchbox.util.NowOrLater;
import com.android.quicksearchbox.util.NowOrLaterWrapper;
import java.util.WeakHashMap;
/* loaded from: classes.dex */
public class CachingIconLoader implements IconLoader {
    private final WeakHashMap<String, Entry> mIconCache = new WeakHashMap<>();
    private final IconLoader mWrapped;

    public CachingIconLoader(IconLoader iconLoader) {
        this.mWrapped = iconLoader;
    }

    @Override // com.android.quicksearchbox.IconLoader
    public NowOrLater<Drawable> getIcon(String str) {
        NowOrLater<Drawable.ConstantState> queryCache;
        Entry entry = null;
        if (TextUtils.isEmpty(str) || "0".equals(str)) {
            return new Now(null);
        }
        synchronized (this) {
            queryCache = queryCache(str);
            if (queryCache == null) {
                entry = new Entry();
                storeInIconCache(str, entry);
            }
        }
        if (queryCache != null) {
            return new NowOrLaterWrapper<Drawable.ConstantState, Drawable>(queryCache) { // from class: com.android.quicksearchbox.CachingIconLoader.1
                @Override // com.android.quicksearchbox.util.NowOrLaterWrapper
                public Drawable get(Drawable.ConstantState constantState) {
                    if (constantState == null) {
                        return null;
                    }
                    return constantState.newDrawable();
                }
            };
        }
        NowOrLater<Drawable> icon = this.mWrapped.getIcon(str);
        entry.set(icon);
        storeInIconCache(str, entry);
        return icon;
    }

    @Override // com.android.quicksearchbox.IconLoader
    public Uri getIconUri(String str) {
        return this.mWrapped.getIconUri(str);
    }

    private synchronized NowOrLater<Drawable.ConstantState> queryCache(String str) {
        return this.mIconCache.get(str);
    }

    private synchronized void storeInIconCache(String str, Entry entry) {
        if (entry != null) {
            this.mIconCache.put(str, entry);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class Entry extends CachedLater<Drawable.ConstantState> implements Consumer<Drawable> {
        private boolean mCreateRequested;
        private NowOrLater<Drawable> mDrawable;
        private boolean mGotDrawable;

        public synchronized void set(NowOrLater<Drawable> nowOrLater) {
            if (this.mGotDrawable) {
                throw new IllegalStateException("set() may only be called once.");
            }
            this.mGotDrawable = true;
            this.mDrawable = nowOrLater;
            if (this.mCreateRequested) {
                getLater();
            }
        }

        @Override // com.android.quicksearchbox.util.CachedLater
        protected synchronized void create() {
            if (!this.mCreateRequested) {
                this.mCreateRequested = true;
                if (this.mGotDrawable) {
                    getLater();
                }
            }
        }

        private void getLater() {
            NowOrLater<Drawable> nowOrLater = this.mDrawable;
            this.mDrawable = null;
            nowOrLater.getLater(this);
        }

        @Override // com.android.quicksearchbox.util.Consumer
        public boolean consume(Drawable drawable) {
            store(drawable == null ? null : drawable.getConstantState());
            return true;
        }
    }
}

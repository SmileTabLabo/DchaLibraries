package android.support.v4.media;

import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompatApi23;
import android.support.v4.os.ResultReceiver;
import android.util.Log;
/* loaded from: classes.dex */
public final class MediaBrowserCompat {
    private static final boolean DEBUG = Log.isLoggable("MediaBrowserCompat", 3);

    /* loaded from: classes.dex */
    public static class MediaItem implements Parcelable {
        public static final Parcelable.Creator<MediaItem> CREATOR = new Parcelable.Creator<MediaItem>() { // from class: android.support.v4.media.MediaBrowserCompat.MediaItem.1
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.Creator
            public MediaItem createFromParcel(Parcel in) {
                return new MediaItem(in, null);
            }

            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.Creator
            public MediaItem[] newArray(int size) {
                return new MediaItem[size];
            }
        };
        private final MediaDescriptionCompat mDescription;
        private final int mFlags;

        /* synthetic */ MediaItem(Parcel in, MediaItem mediaItem) {
            this(in);
        }

        private MediaItem(Parcel in) {
            this.mFlags = in.readInt();
            this.mDescription = MediaDescriptionCompat.CREATOR.createFromParcel(in);
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel out, int flags) {
            out.writeInt(this.mFlags);
            this.mDescription.writeToParcel(out, flags);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder("MediaItem{");
            sb.append("mFlags=").append(this.mFlags);
            sb.append(", mDescription=").append(this.mDescription);
            sb.append('}');
            return sb.toString();
        }
    }

    /* loaded from: classes.dex */
    public static abstract class ItemCallback {
        final Object mItemCallbackObj;

        public ItemCallback() {
            if (Build.VERSION.SDK_INT >= 23) {
                this.mItemCallbackObj = MediaBrowserCompatApi23.createItemCallback(new StubApi23(this, null));
            } else {
                this.mItemCallbackObj = null;
            }
        }

        public void onItemLoaded(MediaItem item) {
        }

        public void onError(@NonNull String itemId) {
        }

        /* loaded from: classes.dex */
        private class StubApi23 implements MediaBrowserCompatApi23.ItemCallback {
            /* synthetic */ StubApi23(ItemCallback this$1, StubApi23 stubApi23) {
                this();
            }

            private StubApi23() {
            }

            @Override // android.support.v4.media.MediaBrowserCompatApi23.ItemCallback
            public void onItemLoaded(Parcel itemParcel) {
                itemParcel.setDataPosition(0);
                MediaItem item = MediaItem.CREATOR.createFromParcel(itemParcel);
                itemParcel.recycle();
                ItemCallback.this.onItemLoaded(item);
            }

            @Override // android.support.v4.media.MediaBrowserCompatApi23.ItemCallback
            public void onError(@NonNull String itemId) {
                ItemCallback.this.onError(itemId);
            }
        }
    }

    /* loaded from: classes.dex */
    private static class ItemReceiver extends ResultReceiver {
        private final ItemCallback mCallback;
        private final String mMediaId;

        @Override // android.support.v4.os.ResultReceiver
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            resultData.setClassLoader(MediaBrowserCompat.class.getClassLoader());
            if (resultCode != 0 || resultData == null || !resultData.containsKey("media_item")) {
                this.mCallback.onError(this.mMediaId);
                return;
            }
            Parcelable item = resultData.getParcelable("media_item");
            if (item instanceof MediaItem) {
                this.mCallback.onItemLoaded((MediaItem) item);
            } else {
                this.mCallback.onError(this.mMediaId);
            }
        }
    }
}

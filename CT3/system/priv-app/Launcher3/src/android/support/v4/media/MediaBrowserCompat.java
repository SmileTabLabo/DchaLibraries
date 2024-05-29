package android.support.v4.media;

import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompatApi23;
import android.support.v4.os.ResultReceiver;
import android.util.Log;
/* loaded from: a.zip:android/support/v4/media/MediaBrowserCompat.class */
public final class MediaBrowserCompat {
    private static final boolean DEBUG = Log.isLoggable("MediaBrowserCompat", 3);

    /* loaded from: a.zip:android/support/v4/media/MediaBrowserCompat$ItemCallback.class */
    public static abstract class ItemCallback {
        final Object mItemCallbackObj;

        /* loaded from: a.zip:android/support/v4/media/MediaBrowserCompat$ItemCallback$StubApi23.class */
        private class StubApi23 implements MediaBrowserCompatApi23.ItemCallback {
            final ItemCallback this$1;

            private StubApi23(ItemCallback itemCallback) {
                this.this$1 = itemCallback;
            }

            /* synthetic */ StubApi23(ItemCallback itemCallback, StubApi23 stubApi23) {
                this(itemCallback);
            }

            @Override // android.support.v4.media.MediaBrowserCompatApi23.ItemCallback
            public void onError(@NonNull String str) {
                this.this$1.onError(str);
            }

            @Override // android.support.v4.media.MediaBrowserCompatApi23.ItemCallback
            public void onItemLoaded(Parcel parcel) {
                parcel.setDataPosition(0);
                MediaItem createFromParcel = MediaItem.CREATOR.createFromParcel(parcel);
                parcel.recycle();
                this.this$1.onItemLoaded(createFromParcel);
            }
        }

        public ItemCallback() {
            if (Build.VERSION.SDK_INT >= 23) {
                this.mItemCallbackObj = MediaBrowserCompatApi23.createItemCallback(new StubApi23(this, null));
            } else {
                this.mItemCallbackObj = null;
            }
        }

        public void onError(@NonNull String str) {
        }

        public void onItemLoaded(MediaItem mediaItem) {
        }
    }

    /* loaded from: a.zip:android/support/v4/media/MediaBrowserCompat$ItemReceiver.class */
    private static class ItemReceiver extends ResultReceiver {
        private final ItemCallback mCallback;
        private final String mMediaId;

        @Override // android.support.v4.os.ResultReceiver
        protected void onReceiveResult(int i, Bundle bundle) {
            bundle.setClassLoader(MediaBrowserCompat.class.getClassLoader());
            if (i != 0 || bundle == null || !bundle.containsKey("media_item")) {
                this.mCallback.onError(this.mMediaId);
                return;
            }
            Parcelable parcelable = bundle.getParcelable("media_item");
            if (parcelable instanceof MediaItem) {
                this.mCallback.onItemLoaded((MediaItem) parcelable);
            } else {
                this.mCallback.onError(this.mMediaId);
            }
        }
    }

    /* loaded from: a.zip:android/support/v4/media/MediaBrowserCompat$MediaItem.class */
    public static class MediaItem implements Parcelable {
        public static final Parcelable.Creator<MediaItem> CREATOR = new Parcelable.Creator<MediaItem>() { // from class: android.support.v4.media.MediaBrowserCompat.MediaItem.1
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.Creator
            public MediaItem createFromParcel(Parcel parcel) {
                return new MediaItem(parcel, null);
            }

            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.Creator
            public MediaItem[] newArray(int i) {
                return new MediaItem[i];
            }
        };
        private final MediaDescriptionCompat mDescription;
        private final int mFlags;

        private MediaItem(Parcel parcel) {
            this.mFlags = parcel.readInt();
            this.mDescription = MediaDescriptionCompat.CREATOR.createFromParcel(parcel);
        }

        /* synthetic */ MediaItem(Parcel parcel, MediaItem mediaItem) {
            this(parcel);
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder("MediaItem{");
            sb.append("mFlags=").append(this.mFlags);
            sb.append(", mDescription=").append(this.mDescription);
            sb.append('}');
            return sb.toString();
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeInt(this.mFlags);
            this.mDescription.writeToParcel(parcel, i);
        }
    }
}

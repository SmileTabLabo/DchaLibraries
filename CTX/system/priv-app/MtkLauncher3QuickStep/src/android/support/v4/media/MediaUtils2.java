package android.support.v4.media;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.mediacompat.Rating2;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaItem2;
import android.support.v4.media.MediaMetadata2;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.MediaSession2;
import java.util.ArrayList;
import java.util.List;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class MediaUtils2 {
    static final String TAG = "MediaUtils2";
    static final MediaBrowserServiceCompat.BrowserRoot sDefaultBrowserRoot = new MediaBrowserServiceCompat.BrowserRoot(MediaLibraryService2.SERVICE_INTERFACE, null);

    private MediaUtils2() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static MediaBrowserCompat.MediaItem convertToMediaItem(MediaItem2 item2) {
        MediaDescriptionCompat descCompat;
        if (item2 == null) {
            return null;
        }
        MediaMetadata2 metadata = item2.getMetadata();
        if (metadata == null) {
            descCompat = new MediaDescriptionCompat.Builder().setMediaId(item2.getMediaId()).build();
        } else {
            MediaDescriptionCompat.Builder builder = new MediaDescriptionCompat.Builder().setMediaId(item2.getMediaId()).setSubtitle(metadata.getText("android.media.metadata.DISPLAY_SUBTITLE")).setDescription(metadata.getText("android.media.metadata.DISPLAY_DESCRIPTION")).setIconBitmap(metadata.getBitmap("android.media.metadata.DISPLAY_ICON")).setExtras(metadata.getExtras());
            String title = metadata.getString("android.media.metadata.TITLE");
            if (title != null) {
                builder.setTitle(title);
            } else {
                builder.setTitle(metadata.getString("android.media.metadata.DISPLAY_TITLE"));
            }
            String displayIconUri = metadata.getString("android.media.metadata.DISPLAY_ICON_URI");
            if (displayIconUri != null) {
                builder.setIconUri(Uri.parse(displayIconUri));
            }
            String mediaUri = metadata.getString("android.media.metadata.MEDIA_URI");
            if (mediaUri != null) {
                builder.setMediaUri(Uri.parse(mediaUri));
            }
            descCompat = builder.build();
        }
        return new MediaBrowserCompat.MediaItem(descCompat, item2.getFlags());
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static List<MediaBrowserCompat.MediaItem> convertToMediaItemList(List<MediaItem2> items) {
        if (items == null) {
            return null;
        }
        List<MediaBrowserCompat.MediaItem> result = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            result.add(convertToMediaItem(items.get(i)));
        }
        return result;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static MediaItem2 convertToMediaItem2(MediaBrowserCompat.MediaItem item) {
        if (item == null || item.getMediaId() == null) {
            return null;
        }
        MediaMetadata2 metadata2 = convertToMediaMetadata2(item.getDescription());
        return new MediaItem2.Builder(item.getFlags()).setMediaId(item.getMediaId()).setMetadata(metadata2).build();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static List<MediaItem2> convertToMediaItem2List(Parcelable[] itemParcelableList) {
        MediaItem2 item;
        List<MediaItem2> playlist = new ArrayList<>();
        if (itemParcelableList != null) {
            for (int i = 0; i < itemParcelableList.length; i++) {
                if ((itemParcelableList[i] instanceof Bundle) && (item = MediaItem2.fromBundle((Bundle) itemParcelableList[i])) != null) {
                    playlist.add(item);
                }
            }
        }
        return playlist;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static List<MediaItem2> convertMediaItemListToMediaItem2List(List<MediaBrowserCompat.MediaItem> items) {
        if (items == null) {
            return null;
        }
        List<MediaItem2> result = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            result.add(convertToMediaItem2(items.get(i)));
        }
        return result;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static List<MediaItem2> convertBundleListToMediaItem2List(List<Bundle> itemBundleList) {
        if (itemBundleList == null) {
            return null;
        }
        List<MediaItem2> playlist = new ArrayList<>();
        for (int i = 0; i < itemBundleList.size(); i++) {
            Bundle itemBundle = itemBundleList.get(i);
            if (itemBundle != null) {
                playlist.add(MediaItem2.fromBundle(itemBundle));
            }
        }
        return playlist;
    }

    static MediaMetadata2 convertToMediaMetadata2(MediaDescriptionCompat descCompat) {
        if (descCompat == null) {
            return null;
        }
        MediaMetadata2.Builder metadata2Builder = new MediaMetadata2.Builder();
        metadata2Builder.putString("android.media.metadata.MEDIA_ID", descCompat.getMediaId());
        CharSequence title = descCompat.getTitle();
        if (title != null) {
            metadata2Builder.putText("android.media.metadata.DISPLAY_TITLE", title);
        }
        CharSequence description = descCompat.getDescription();
        if (description != null) {
            metadata2Builder.putText("android.media.metadata.DISPLAY_DESCRIPTION", descCompat.getDescription());
        }
        CharSequence subtitle = descCompat.getSubtitle();
        if (subtitle != null) {
            metadata2Builder.putText("android.media.metadata.DISPLAY_SUBTITLE", subtitle);
        }
        Bitmap icon = descCompat.getIconBitmap();
        if (icon != null) {
            metadata2Builder.putBitmap("android.media.metadata.DISPLAY_ICON", icon);
        }
        Uri iconUri = descCompat.getIconUri();
        if (iconUri != null) {
            metadata2Builder.putText("android.media.metadata.DISPLAY_ICON_URI", iconUri.toString());
        }
        Bundle bundle = descCompat.getExtras();
        if (bundle != null) {
            metadata2Builder.setExtras(descCompat.getExtras());
        }
        Uri mediaUri = descCompat.getMediaUri();
        if (mediaUri != null) {
            metadata2Builder.putText("android.media.metadata.MEDIA_URI", mediaUri.toString());
        }
        return metadata2Builder.build();
    }

    static MediaMetadata2 convertToMediaMetadata2(MediaMetadataCompat metadataCompat) {
        if (metadataCompat == null) {
            return null;
        }
        return new MediaMetadata2(metadataCompat.getBundle());
    }

    static MediaMetadataCompat convertToMediaMetadataCompat(MediaMetadata2 metadata2) {
        if (metadata2 == null) {
            return null;
        }
        MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder();
        List<String> skippedKeys = new ArrayList<>();
        Bundle bundle = metadata2.toBundle();
        for (String key : bundle.keySet()) {
            Object value = bundle.get(key);
            if (value instanceof CharSequence) {
                builder.putText(key, (CharSequence) value);
            } else if (value instanceof Rating2) {
                builder.putRating(key, convertToRatingCompat((Rating2) value));
            } else if (value instanceof Bitmap) {
                builder.putBitmap(key, (Bitmap) value);
            } else if (value instanceof Long) {
                builder.putLong(key, ((Long) value).longValue());
            } else {
                skippedKeys.add(key);
            }
        }
        MediaMetadataCompat result = builder.build();
        for (String key2 : skippedKeys) {
            Object value2 = bundle.get(key2);
            if (value2 instanceof Float) {
                result.getBundle().putFloat(key2, ((Float) value2).floatValue());
            } else if (MediaMetadata2.METADATA_KEY_EXTRAS.equals(value2)) {
                result.getBundle().putBundle(key2, (Bundle) value2);
            }
        }
        return result;
    }

    static Rating2 convertToRating2(RatingCompat ratingCompat) {
        if (ratingCompat == null) {
            return null;
        }
        if (!ratingCompat.isRated()) {
            return Rating2.newUnratedRating(ratingCompat.getRatingStyle());
        }
        switch (ratingCompat.getRatingStyle()) {
            case 1:
                return Rating2.newHeartRating(ratingCompat.hasHeart());
            case 2:
                return Rating2.newThumbRating(ratingCompat.isThumbUp());
            case 3:
            case 4:
            case 5:
                return Rating2.newStarRating(ratingCompat.getRatingStyle(), ratingCompat.getStarRating());
            case 6:
                return Rating2.newPercentageRating(ratingCompat.getPercentRating());
            default:
                return null;
        }
    }

    static RatingCompat convertToRatingCompat(Rating2 rating2) {
        if (rating2 == null) {
            return null;
        }
        if (!rating2.isRated()) {
            return RatingCompat.newUnratedRating(rating2.getRatingStyle());
        }
        switch (rating2.getRatingStyle()) {
            case 1:
                return RatingCompat.newHeartRating(rating2.hasHeart());
            case 2:
                return RatingCompat.newThumbRating(rating2.isThumbUp());
            case 3:
            case 4:
            case 5:
                return RatingCompat.newStarRating(rating2.getRatingStyle(), rating2.getStarRating());
            case 6:
                return RatingCompat.newPercentageRating(rating2.getPercentRating());
            default:
                return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static List<Bundle> convertToBundleList(Parcelable[] array) {
        if (array == null) {
            return null;
        }
        List<Bundle> bundleList = new ArrayList<>();
        for (Parcelable p : array) {
            bundleList.add((Bundle) p);
        }
        return bundleList;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static List<Bundle> convertMediaItem2ListToBundleList(List<MediaItem2> playlist) {
        Bundle itemBundle;
        if (playlist == null) {
            return null;
        }
        List<Bundle> itemBundleList = new ArrayList<>();
        for (int i = 0; i < playlist.size(); i++) {
            MediaItem2 item = playlist.get(i);
            if (item != null && (itemBundle = item.toBundle()) != null) {
                itemBundleList.add(itemBundle);
            }
        }
        return itemBundleList;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static List<Bundle> convertCommandButtonListToBundleList(List<MediaSession2.CommandButton> commandButtonList) {
        List<Bundle> commandButtonBundleList = new ArrayList<>();
        for (int i = 0; i < commandButtonList.size(); i++) {
            Bundle bundle = commandButtonList.get(i).toBundle();
            if (bundle != null) {
                commandButtonBundleList.add(bundle);
            }
        }
        return commandButtonBundleList;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static Parcelable[] convertMediaItem2ListToParcelableArray(List<MediaItem2> playlist) {
        Parcelable itemBundle;
        if (playlist == null) {
            return null;
        }
        List<Parcelable> parcelableList = new ArrayList<>();
        for (int i = 0; i < playlist.size(); i++) {
            MediaItem2 item = playlist.get(i);
            if (item != null && (itemBundle = item.toBundle()) != null) {
                parcelableList.add(itemBundle);
            }
        }
        return (Parcelable[]) parcelableList.toArray(new Parcelable[0]);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static Parcelable[] convertCommandButtonListToParcelableArray(List<MediaSession2.CommandButton> layout) {
        if (layout == null) {
            return null;
        }
        List<Bundle> layoutBundles = new ArrayList<>();
        for (int i = 0; i < layout.size(); i++) {
            Bundle bundle = layout.get(i).toBundle();
            if (bundle != null) {
                layoutBundles.add(bundle);
            }
        }
        return (Parcelable[]) layoutBundles.toArray(new Parcelable[0]);
    }

    static List<MediaSession2.CommandButton> convertToCommandButtonList(List<Bundle> commandButtonBundleList) {
        List<MediaSession2.CommandButton> commandButtonList = new ArrayList<>();
        for (int i = 0; i < commandButtonBundleList.size(); i++) {
            Bundle bundle = commandButtonBundleList.get(i);
            if (bundle != null) {
                commandButtonList.add(MediaSession2.CommandButton.fromBundle(bundle));
            }
        }
        return commandButtonList;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static List<MediaSession2.CommandButton> convertToCommandButtonList(Parcelable[] list) {
        MediaSession2.CommandButton button;
        List<MediaSession2.CommandButton> layout = new ArrayList<>();
        for (int i = 0; i < list.length; i++) {
            if ((list[i] instanceof Bundle) && (button = MediaSession2.CommandButton.fromBundle((Bundle) list[i])) != null) {
                layout.add(button);
            }
        }
        return layout;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int convertToPlaybackStateCompatState(int playerState, int bufferingState) {
        switch (playerState) {
            case 0:
                return 0;
            case 1:
                return 2;
            case 2:
                if (bufferingState == 2) {
                    return 6;
                }
                return 3;
            case 3:
                return 7;
            default:
                return 7;
        }
    }

    static int convertToPlayerState(int playbackStateCompatState) {
        switch (playbackStateCompatState) {
            case 0:
                return 0;
            case 1:
            case 2:
            case 6:
                return 1;
            case 3:
            case 4:
            case 5:
            case 8:
            case 9:
            case 10:
            case 11:
                return 2;
            case 7:
                return 3;
            default:
                return 3;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static boolean isDefaultLibraryRootHint(Bundle bundle) {
        return bundle != null && bundle.getBoolean("android.support.v4.media.root_default_root", false);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static Bundle createBundle(Bundle bundle) {
        return bundle == null ? new Bundle() : new Bundle(bundle);
    }
}

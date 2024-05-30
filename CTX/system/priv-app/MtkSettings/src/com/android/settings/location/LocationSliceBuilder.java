package com.android.settings.location;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.graphics.drawable.IconCompat;
import android.support.v4.util.Consumer;
import androidx.slice.Slice;
import androidx.slice.builders.ListBuilder;
import androidx.slice.builders.SliceAction;
import com.android.settings.R;
import com.android.settings.SubSettings;
import com.android.settings.Utils;
import com.android.settings.search.DatabaseIndexingUtils;
/* loaded from: classes.dex */
public class LocationSliceBuilder {
    public static final Uri LOCATION_URI = new Uri.Builder().scheme("content").authority("android.settings.slices").appendPath("action").appendPath("location").build();

    public static Slice getSlice(Context context) {
        final IconCompat createWithResource = IconCompat.createWithResource(context, (int) R.drawable.ic_signal_location);
        final String string = context.getString(R.string.location_settings_title);
        int colorAccent = Utils.getColorAccent(context);
        final SliceAction sliceAction = new SliceAction(getPrimaryAction(context), createWithResource, string);
        return new ListBuilder(context, LOCATION_URI, -1L).setAccentColor(colorAccent).addRow(new Consumer() { // from class: com.android.settings.location.-$$Lambda$LocationSliceBuilder$b_EpqAhS4ORYylfhNREU0o0sGYE
            @Override // android.support.v4.util.Consumer
            public final void accept(Object obj) {
                String str = string;
                ((ListBuilder.RowBuilder) obj).setTitle(str).setTitleItem(createWithResource, 0).setPrimaryAction(sliceAction);
            }
        }).build();
    }

    public static Intent getIntent(Context context) {
        String charSequence = context.getText(R.string.location_settings_title).toString();
        return DatabaseIndexingUtils.buildSearchResultPageIntent(context, LocationSettings.class.getName(), "location", charSequence, 63).setClassName(context.getPackageName(), SubSettings.class.getName()).setData(new Uri.Builder().appendPath("location").build());
    }

    private static PendingIntent getPrimaryAction(Context context) {
        return PendingIntent.getActivity(context, 0, getIntent(context), 0);
    }
}

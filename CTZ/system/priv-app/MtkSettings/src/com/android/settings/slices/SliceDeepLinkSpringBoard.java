package com.android.settings.slices;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import com.android.settings.bluetooth.BluetoothSliceBuilder;
import com.android.settings.location.LocationSliceBuilder;
import com.android.settings.notification.ZenModeSliceBuilder;
import com.android.settings.wifi.WifiSliceBuilder;
import java.net.URISyntaxException;
/* loaded from: classes.dex */
public class SliceDeepLinkSpringBoard extends Activity {
    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        Intent contentIntent;
        super.onCreate(bundle);
        Uri data = getIntent().getData();
        if (data == null) {
            Log.e("DeeplinkSpringboard", "No data found");
            finish();
            return;
        }
        try {
            Intent parse = parse(data, getPackageName());
            if ("com.android.settings.action.VIEW_SLICE".equals(parse.getAction())) {
                Uri parse2 = Uri.parse(parse.getStringExtra("slice"));
                if (WifiSliceBuilder.WIFI_URI.equals(parse2)) {
                    contentIntent = WifiSliceBuilder.getIntent(this);
                } else if (ZenModeSliceBuilder.ZEN_MODE_URI.equals(parse2)) {
                    contentIntent = ZenModeSliceBuilder.getIntent(this);
                } else if (BluetoothSliceBuilder.BLUETOOTH_URI.equals(parse2)) {
                    contentIntent = BluetoothSliceBuilder.getIntent(this);
                } else if (LocationSliceBuilder.LOCATION_URI.equals(parse2)) {
                    contentIntent = LocationSliceBuilder.getIntent(this);
                } else {
                    contentIntent = SliceBuilderUtils.getContentIntent(this, new SlicesDatabaseAccessor(this).getSliceDataFromUri(parse2));
                }
                startActivity(contentIntent);
            } else {
                startActivity(parse);
            }
            finish();
        } catch (IllegalStateException e) {
            Log.w("DeeplinkSpringboard", "Couldn't launch Slice intent", e);
            startActivity(new Intent("android.settings.SETTINGS"));
            finish();
        } catch (URISyntaxException e2) {
            Log.e("DeeplinkSpringboard", "Error decoding uri", e2);
            finish();
        }
    }

    public static Intent parse(Uri uri, String str) throws URISyntaxException {
        Intent parseUri = Intent.parseUri(uri.getQueryParameter("intent"), 2);
        parseUri.setComponent(null);
        if (parseUri.getExtras() != null) {
            parseUri.getExtras().clear();
        }
        parseUri.setPackage(str);
        return parseUri;
    }
}

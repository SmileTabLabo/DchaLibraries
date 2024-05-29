package com.mediatek.browser.hotknot;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
/* loaded from: b.zip:com/mediatek/browser/hotknot/HotKnotActivity.class */
public class HotKnotActivity extends Activity {
    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Log.d("browser/HotKnotActivity", "HotKnotActivity onCreate.");
        Intent intent = getIntent();
        if (!intent.getAction().equals("com.mediatek.hotknot.action.MESSAGE_DISCOVERED")) {
            Log.w("browser/HotKnotActivity", "Invalid intent:" + intent);
            finish();
            return;
        }
        String type = intent.getType();
        if (type == null || !"com.mediatek.browser.hotknot/com.mediatek.browser.hotknot.MIME_TYPE".equalsIgnoreCase(type)) {
            StringBuilder append = new StringBuilder().append("Invalid mimeType:");
            String str = type;
            if (type == null) {
                str = "null";
            }
            Log.w("browser/HotKnotActivity", append.append(str).toString());
            finish();
            return;
        }
        byte[] byteArrayExtra = intent.getByteArrayExtra("com.mediatek.hotknot.extra.DATA");
        if (byteArrayExtra == null || byteArrayExtra.length == 0) {
            Log.w("browser/HotKnotActivity", "Invalid url:" + (byteArrayExtra == null ? "null" : ""));
            finish();
            return;
        }
        Intent intent2 = new Intent("android.intent.action.VIEW", Uri.parse(new String(byteArrayExtra)));
        intent2.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
        intent2.putExtra("HotKnot_Intent", true);
        startActivity(intent2);
        finish();
    }
}

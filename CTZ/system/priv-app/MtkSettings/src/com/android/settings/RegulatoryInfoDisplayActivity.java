package com.android.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.Locale;
/* loaded from: classes.dex */
public class RegulatoryInfoDisplayActivity extends Activity implements DialogInterface.OnDismissListener {
    private final String REGULATORY_INFO_RESOURCE = "regulatory_info";

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        int i;
        super.onCreate(bundle);
        Resources resources = getResources();
        if (!resources.getBoolean(R.bool.config_show_regulatory_info)) {
            finish();
        }
        AlertDialog.Builder onDismissListener = new AlertDialog.Builder(this).setTitle(R.string.regulatory_labels).setOnDismissListener(this);
        Bitmap decodeFile = BitmapFactory.decodeFile(getRegulatoryInfoImageFileName());
        boolean z = false;
        boolean z2 = decodeFile != null;
        if (!z2) {
            i = getResourceId();
        } else {
            i = 0;
        }
        if (i != 0) {
            try {
                Drawable drawable = getDrawable(i);
                if (drawable.getIntrinsicWidth() > 2) {
                    if (drawable.getIntrinsicHeight() > 2) {
                        z = true;
                    }
                }
            } catch (Resources.NotFoundException e) {
            }
        } else {
            z = z2;
        }
        CharSequence text = resources.getText(R.string.regulatory_info_text);
        if (!z) {
            if (text.length() > 0) {
                onDismissListener.setMessage(text);
                ((TextView) onDismissListener.show().findViewById(16908299)).setGravity(17);
                return;
            }
            finish();
            return;
        }
        View inflate = getLayoutInflater().inflate(R.layout.regulatory_info, (ViewGroup) null);
        ImageView imageView = (ImageView) inflate.findViewById(R.id.regulatoryInfo);
        if (decodeFile != null) {
            imageView.setImageBitmap(decodeFile);
        } else {
            imageView.setImageResource(i);
        }
        onDismissListener.setView(inflate);
        onDismissListener.show();
    }

    private int getResourceId() {
        int identifier = getResources().getIdentifier("regulatory_info", "drawable", getPackageName());
        String sku = getSku();
        if (TextUtils.isEmpty(sku)) {
            return identifier;
        }
        int identifier2 = getResources().getIdentifier("regulatory_info_" + sku.toLowerCase(), "drawable", getPackageName());
        return identifier2 != 0 ? identifier2 : identifier;
    }

    @Override // android.content.DialogInterface.OnDismissListener
    public void onDismiss(DialogInterface dialogInterface) {
        finish();
    }

    public static String getSku() {
        return SystemProperties.get("ro.boot.hardware.sku", "");
    }

    public static String getRegulatoryInfoImageFileName() {
        String sku = getSku();
        if (TextUtils.isEmpty(sku)) {
            return "/data/misc/elabel/regulatory_info.png";
        }
        return String.format(Locale.US, "/data/misc/elabel/regulatory_info_%s.png", sku.toLowerCase());
    }
}

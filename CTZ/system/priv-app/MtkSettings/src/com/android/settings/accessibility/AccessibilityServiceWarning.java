package com.android.settings.accessibility;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.storage.StorageManager;
import android.text.BidiFormatter;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.settings.R;
import java.util.List;
import java.util.Locale;
/* loaded from: classes.dex */
public class AccessibilityServiceWarning {
    public static Dialog createCapabilitiesDialog(Activity activity, AccessibilityServiceInfo accessibilityServiceInfo, DialogInterface.OnClickListener onClickListener) {
        AlertDialog create = new AlertDialog.Builder(activity).setTitle(activity.getString(R.string.enable_service_title, new Object[]{getServiceName(activity, accessibilityServiceInfo)})).setView(createEnableDialogContentView(activity, accessibilityServiceInfo)).setPositiveButton(17039370, onClickListener).setNegativeButton(17039360, onClickListener).create();
        $$Lambda$AccessibilityServiceWarning$D3xqJyTKInilYjQAxG1fpVU1D1M __lambda_accessibilityservicewarning_d3xqjytkinilyjqaxg1fpvu1d1m = new View.OnTouchListener() { // from class: com.android.settings.accessibility.-$$Lambda$AccessibilityServiceWarning$D3xqJyTKInilYjQAxG1fpVU1D1M
            @Override // android.view.View.OnTouchListener
            public final boolean onTouch(View view, MotionEvent motionEvent) {
                return AccessibilityServiceWarning.lambda$createCapabilitiesDialog$0(view, motionEvent);
            }
        };
        Window window = create.getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.privateFlags |= 524288;
        window.setAttributes(attributes);
        create.create();
        create.getButton(-1).setOnTouchListener(__lambda_accessibilityservicewarning_d3xqjytkinilyjqaxg1fpvu1d1m);
        create.setCanceledOnTouchOutside(true);
        return create;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ boolean lambda$createCapabilitiesDialog$0(View view, MotionEvent motionEvent) {
        if ((motionEvent.getFlags() & 1) == 0 && (motionEvent.getFlags() & 2) == 0) {
            return false;
        }
        if (motionEvent.getAction() == 1) {
            Toast.makeText(view.getContext(), (int) R.string.touch_filtered_warning, 0).show();
        }
        return true;
    }

    private static boolean isFullDiskEncrypted() {
        return StorageManager.isNonDefaultBlockEncrypted();
    }

    private static View createEnableDialogContentView(Context context, AccessibilityServiceInfo accessibilityServiceInfo) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService("layout_inflater");
        ViewGroup viewGroup = null;
        View inflate = layoutInflater.inflate(R.layout.enable_accessibility_service_dialog_content, (ViewGroup) null);
        TextView textView = (TextView) inflate.findViewById(R.id.encryption_warning);
        int i = 0;
        if (isFullDiskEncrypted()) {
            textView.setText(context.getString(R.string.enable_service_encryption_warning, getServiceName(context, accessibilityServiceInfo)));
            textView.setVisibility(0);
        } else {
            textView.setVisibility(8);
        }
        ((TextView) inflate.findViewById(R.id.capabilities_header)).setText(context.getString(R.string.capabilities_list_title, getServiceName(context, accessibilityServiceInfo)));
        LinearLayout linearLayout = (LinearLayout) inflate.findViewById(R.id.capabilities);
        View inflate2 = layoutInflater.inflate(17367097, (ViewGroup) null);
        ((ImageView) inflate2.findViewById(16909166)).setImageDrawable(context.getDrawable(17302764));
        ((TextView) inflate2.findViewById(16909170)).setText(context.getString(R.string.capability_title_receiveAccessibilityEvents));
        ((TextView) inflate2.findViewById(16909172)).setText(context.getString(R.string.capability_desc_receiveAccessibilityEvents));
        List capabilityInfos = accessibilityServiceInfo.getCapabilityInfos(context);
        linearLayout.addView(inflate2);
        int size = capabilityInfos.size();
        while (i < size) {
            AccessibilityServiceInfo.CapabilityInfo capabilityInfo = (AccessibilityServiceInfo.CapabilityInfo) capabilityInfos.get(i);
            View inflate3 = layoutInflater.inflate(17367097, viewGroup);
            ((ImageView) inflate3.findViewById(16909166)).setImageDrawable(context.getDrawable(17302764));
            ((TextView) inflate3.findViewById(16909170)).setText(context.getString(capabilityInfo.titleResId));
            ((TextView) inflate3.findViewById(16909172)).setText(context.getString(capabilityInfo.descResId));
            linearLayout.addView(inflate3);
            i++;
            viewGroup = null;
        }
        return inflate;
    }

    private static CharSequence getServiceName(Context context, AccessibilityServiceInfo accessibilityServiceInfo) {
        Locale locale = context.getResources().getConfiguration().getLocales().get(0);
        return BidiFormatter.getInstance(locale).unicodeWrap(accessibilityServiceInfo.getResolveInfo().loadLabel(context.getPackageManager()));
    }
}

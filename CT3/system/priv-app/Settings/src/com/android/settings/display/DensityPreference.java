package com.android.settings.display;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Slog;
import android.view.View;
import android.widget.EditText;
import com.android.settings.CustomEditTextPreference;
import com.android.settings.R;
import com.android.settingslib.display.DisplayDensityUtils;
/* loaded from: classes.dex */
public class DensityPreference extends CustomEditTextPreference {
    public DensityPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override // android.support.v7.preference.Preference
    public void onAttached() {
        super.onAttached();
        setSummary(getContext().getString(R.string.developer_density_summary, Integer.valueOf(getCurrentSwDp())));
    }

    private int getCurrentSwDp() {
        Resources res = getContext().getResources();
        DisplayMetrics metrics = res.getDisplayMetrics();
        float density = metrics.density;
        int minDimensionPx = Math.min(metrics.widthPixels, metrics.heightPixels);
        return (int) (minDimensionPx / density);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.CustomEditTextPreference
    public void onBindDialogView(View view) {
        super.onBindDialogView(view);
        EditText editText = (EditText) view.findViewById(16908291);
        if (editText == null) {
            return;
        }
        editText.setInputType(2);
        editText.setText(getCurrentSwDp() + "");
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.CustomEditTextPreference
    public void onDialogClosed(boolean positiveResult) {
        if (!positiveResult) {
            return;
        }
        try {
            Resources res = getContext().getResources();
            DisplayMetrics metrics = res.getDisplayMetrics();
            int newSwDp = Math.max(Integer.parseInt(getText()), 320);
            int minDimensionPx = Math.min(metrics.widthPixels, metrics.heightPixels);
            int newDensity = (minDimensionPx * 160) / newSwDp;
            int densityDpi = Math.max(newDensity, 120);
            DisplayDensityUtils.setForcedDisplayDensity(0, densityDpi);
        } catch (Exception e) {
            Slog.e("DensityPreference", "Couldn't save density", e);
        }
    }
}

package com.android.settings.display;

import android.content.Context;
import android.text.BidiFormatter;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Slog;
import android.view.View;
import android.widget.EditText;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settingslib.CustomEditTextPreference;
import com.android.settingslib.display.DisplayDensityUtils;
import java.text.NumberFormat;
/* loaded from: classes.dex */
public class DensityPreference extends CustomEditTextPreference {
    public DensityPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override // android.support.v7.preference.Preference
    public void onAttached() {
        super.onAttached();
        setSummary(getContext().getString(R.string.density_pixel_summary, BidiFormatter.getInstance().unicodeWrap(NumberFormat.getInstance().format(getCurrentSwDp()))));
    }

    private int getCurrentSwDp() {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        return (int) (Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels) / displayMetrics.density);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settingslib.CustomEditTextPreference
    public void onBindDialogView(View view) {
        super.onBindDialogView(view);
        EditText editText = (EditText) view.findViewById(16908291);
        if (editText != null) {
            editText.setInputType(2);
            editText.setText(getCurrentSwDp() + "");
            Utils.setEditTextCursorPosition(editText);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settingslib.CustomEditTextPreference
    public void onDialogClosed(boolean z) {
        if (z) {
            try {
                DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
                DisplayDensityUtils.setForcedDisplayDensity(0, Math.max((160 * Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels)) / Math.max(Integer.parseInt(getText()), 320), (int) android.support.v7.appcompat.R.styleable.AppCompatTheme_windowNoTitle));
            } catch (Exception e) {
                Slog.e("DensityPreference", "Couldn't save density", e);
            }
        }
    }
}

package com.android.settings;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
/* loaded from: classes.dex */
public class SeekBarDialogPreference extends CustomDialogPreference {
    private final Drawable mMyIcon;

    public SeekBarDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.preference_dialog_seekbar_material);
        createActionButtons();
        this.mMyIcon = getDialogIcon();
        setDialogIcon(null);
    }

    public SeekBarDialogPreference(Context context) {
        this(context, null);
    }

    public void createActionButtons() {
        setPositiveButtonText(17039370);
        setNegativeButtonText(17039360);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.CustomDialogPreference
    public void onBindDialogView(View view) {
        super.onBindDialogView(view);
        ImageView iconView = (ImageView) view.findViewById(16908294);
        if (this.mMyIcon != null) {
            iconView.setImageDrawable(this.mMyIcon);
        } else {
            iconView.setVisibility(8);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public static SeekBar getSeekBar(View dialogView) {
        return (SeekBar) dialogView.findViewById(R.id.seekbar);
    }
}

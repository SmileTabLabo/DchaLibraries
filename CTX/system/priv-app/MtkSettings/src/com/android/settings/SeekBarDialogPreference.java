package com.android.settings;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import com.android.settingslib.CustomDialogPreference;
/* loaded from: classes.dex */
public class SeekBarDialogPreference extends CustomDialogPreference {
    private final Drawable mMyIcon;

    public SeekBarDialogPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
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
    @Override // com.android.settingslib.CustomDialogPreference
    public void onBindDialogView(View view) {
        super.onBindDialogView(view);
        ImageView imageView = (ImageView) view.findViewById(16908294);
        if (this.mMyIcon != null) {
            imageView.setImageDrawable(this.mMyIcon);
        } else {
            imageView.setVisibility(8);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public static SeekBar getSeekBar(View view) {
        return (SeekBar) view.findViewById(R.id.seekbar);
    }
}

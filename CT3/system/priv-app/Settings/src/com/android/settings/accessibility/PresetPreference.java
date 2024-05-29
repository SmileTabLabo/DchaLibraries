package com.android.settings.accessibility;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.CaptioningManager;
import android.widget.TextView;
import com.android.internal.widget.SubtitleView;
import com.android.settings.R;
/* loaded from: classes.dex */
public class PresetPreference extends ListDialogPreference {
    private final CaptioningManager mCaptioningManager;

    public PresetPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.grid_picker_dialog);
        setListItemLayoutResource(R.layout.preset_picker_item);
        this.mCaptioningManager = (CaptioningManager) context.getSystemService("captioning");
    }

    @Override // android.support.v7.preference.Preference
    public boolean shouldDisableDependents() {
        if (getValue() == -1) {
            return super.shouldDisableDependents();
        }
        return true;
    }

    @Override // com.android.settings.accessibility.ListDialogPreference
    protected void onBindListItem(View view, int index) {
        View previewViewport = view.findViewById(R.id.preview_viewport);
        SubtitleView previewText = view.findViewById(R.id.preview);
        int value = getValueAt(index);
        CaptionPropertiesFragment.applyCaptionProperties(this.mCaptioningManager, previewText, previewViewport, value);
        float density = getContext().getResources().getDisplayMetrics().density;
        previewText.setTextSize(32.0f * density);
        CharSequence title = getTitleAt(index);
        if (title == null) {
            return;
        }
        TextView summary = (TextView) view.findViewById(R.id.summary);
        summary.setText(title);
    }
}

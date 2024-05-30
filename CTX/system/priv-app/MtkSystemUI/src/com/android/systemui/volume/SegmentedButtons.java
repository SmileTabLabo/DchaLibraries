package com.android.systemui.volume;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.systemui.R;
import com.android.systemui.volume.Interaction;
import java.util.Objects;
/* loaded from: classes.dex */
public class SegmentedButtons extends LinearLayout {
    private Callback mCallback;
    private final View.OnClickListener mClick;
    private final ConfigurableTexts mConfigurableTexts;
    private final Context mContext;
    protected final LayoutInflater mInflater;
    protected Object mSelectedValue;
    private static final Typeface REGULAR = Typeface.create("sans-serif", 0);
    private static final Typeface MEDIUM = Typeface.create("sans-serif-medium", 0);

    /* loaded from: classes.dex */
    public interface Callback extends Interaction.Callback {
        void onSelected(Object obj, boolean z);
    }

    public SegmentedButtons(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mClick = new View.OnClickListener() { // from class: com.android.systemui.volume.SegmentedButtons.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                SegmentedButtons.this.setSelectedValue(view.getTag(), true);
            }
        };
        this.mContext = context;
        this.mInflater = LayoutInflater.from(this.mContext);
        setOrientation(0);
        this.mConfigurableTexts = new ConfigurableTexts(this.mContext);
    }

    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    public Object getSelectedValue() {
        return this.mSelectedValue;
    }

    public void setSelectedValue(Object obj, boolean z) {
        if (Objects.equals(obj, this.mSelectedValue)) {
            return;
        }
        this.mSelectedValue = obj;
        for (int i = 0; i < getChildCount(); i++) {
            TextView textView = (TextView) getChildAt(i);
            boolean equals = Objects.equals(this.mSelectedValue, textView.getTag());
            textView.setSelected(equals);
            setSelectedStyle(textView, equals);
        }
        fireOnSelected(z);
    }

    protected void setSelectedStyle(TextView textView, boolean z) {
        textView.setTypeface(z ? MEDIUM : REGULAR);
    }

    public Button inflateButton() {
        return (Button) this.mInflater.inflate(R.layout.segmented_button, (ViewGroup) this, false);
    }

    public void addButton(int i, int i2, Object obj) {
        Button inflateButton = inflateButton();
        inflateButton.setTag(R.id.label, Integer.valueOf(i));
        inflateButton.setText(i);
        inflateButton.setContentDescription(getResources().getString(i2));
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) inflateButton.getLayoutParams();
        if (getChildCount() == 0) {
            layoutParams.rightMargin = 0;
            layoutParams.leftMargin = 0;
        }
        inflateButton.setLayoutParams(layoutParams);
        addView(inflateButton);
        inflateButton.setTag(obj);
        inflateButton.setOnClickListener(this.mClick);
        Interaction.register(inflateButton, new Interaction.Callback() { // from class: com.android.systemui.volume.SegmentedButtons.1
            @Override // com.android.systemui.volume.Interaction.Callback
            public void onInteraction() {
                SegmentedButtons.this.fireInteraction();
            }
        });
        this.mConfigurableTexts.add(inflateButton, i);
    }

    public void update() {
        this.mConfigurableTexts.update();
    }

    private void fireOnSelected(boolean z) {
        if (this.mCallback != null) {
            this.mCallback.onSelected(this.mSelectedValue, z);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void fireInteraction() {
        if (this.mCallback != null) {
            this.mCallback.onInteraction();
        }
    }
}

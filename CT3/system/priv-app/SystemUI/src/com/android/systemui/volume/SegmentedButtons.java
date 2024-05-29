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
import com.android.systemui.volume.Interaction;
import java.util.Objects;
/* loaded from: a.zip:com/android/systemui/volume/SegmentedButtons.class */
public class SegmentedButtons extends LinearLayout {
    private Callback mCallback;
    private final View.OnClickListener mClick;
    private final Context mContext;
    protected final LayoutInflater mInflater;
    protected Object mSelectedValue;
    private final SpTexts mSpTexts;
    private static final Typeface REGULAR = Typeface.create("sans-serif", 0);
    private static final Typeface MEDIUM = Typeface.create("sans-serif-medium", 0);

    /* loaded from: a.zip:com/android/systemui/volume/SegmentedButtons$Callback.class */
    public interface Callback extends Interaction.Callback {
        void onSelected(Object obj, boolean z);
    }

    public SegmentedButtons(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mClick = new View.OnClickListener(this) { // from class: com.android.systemui.volume.SegmentedButtons.1
            final SegmentedButtons this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                this.this$0.setSelectedValue(view.getTag(), true);
            }
        };
        this.mContext = context;
        this.mInflater = LayoutInflater.from(this.mContext);
        setOrientation(0);
        this.mSpTexts = new SpTexts(this.mContext);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void fireInteraction() {
        if (this.mCallback != null) {
            this.mCallback.onInteraction();
        }
    }

    private void fireOnSelected(boolean z) {
        if (this.mCallback != null) {
            this.mCallback.onSelected(this.mSelectedValue, z);
        }
    }

    public void addButton(int i, int i2, Object obj) {
        Button inflateButton = inflateButton();
        inflateButton.setTag(2131886369, Integer.valueOf(i));
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
        Interaction.register(inflateButton, new Interaction.Callback(this) { // from class: com.android.systemui.volume.SegmentedButtons.2
            final SegmentedButtons this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.systemui.volume.Interaction.Callback
            public void onInteraction() {
                this.this$0.fireInteraction();
            }
        });
        this.mSpTexts.add(inflateButton);
    }

    public Object getSelectedValue() {
        return this.mSelectedValue;
    }

    public Button inflateButton() {
        return (Button) this.mInflater.inflate(2130968800, (ViewGroup) this, false);
    }

    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    protected void setSelectedStyle(TextView textView, boolean z) {
        textView.setTypeface(z ? MEDIUM : REGULAR);
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

    public void updateLocale() {
        for (int i = 0; i < getChildCount(); i++) {
            Button button = (Button) getChildAt(i);
            button.setText(((Integer) button.getTag(2131886369)).intValue());
        }
    }
}

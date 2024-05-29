package com.android.systemui.volume;

import android.content.Context;
import android.content.res.Resources;
import android.util.ArrayMap;
import android.view.View;
import android.widget.TextView;
/* loaded from: a.zip:com/android/systemui/volume/SpTexts.class */
public class SpTexts {
    private final Context mContext;
    private final ArrayMap<TextView, Integer> mTexts = new ArrayMap<>();
    private final Runnable mUpdateAll = new Runnable(this) { // from class: com.android.systemui.volume.SpTexts.1
        final SpTexts this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            for (int i = 0; i < this.this$0.mTexts.size(); i++) {
                this.this$0.setTextSizeH((TextView) this.this$0.mTexts.keyAt(i), ((Integer) this.this$0.mTexts.valueAt(i)).intValue());
            }
        }
    };

    public SpTexts(Context context) {
        this.mContext = context;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setTextSizeH(TextView textView, int i) {
        textView.setTextSize(2, i);
    }

    public int add(TextView textView) {
        if (textView == null) {
            return 0;
        }
        Resources resources = this.mContext.getResources();
        float f = resources.getConfiguration().fontScale;
        int textSize = (int) ((textView.getTextSize() / f) / resources.getDisplayMetrics().density);
        this.mTexts.put(textView, Integer.valueOf(textSize));
        textView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener(this, textView, textSize) { // from class: com.android.systemui.volume.SpTexts.2
            final SpTexts this$0;
            final int val$sp;
            final TextView val$text;

            {
                this.this$0 = this;
                this.val$text = textView;
                this.val$sp = textSize;
            }

            @Override // android.view.View.OnAttachStateChangeListener
            public void onViewAttachedToWindow(View view) {
                this.this$0.setTextSizeH(this.val$text, this.val$sp);
            }

            @Override // android.view.View.OnAttachStateChangeListener
            public void onViewDetachedFromWindow(View view) {
            }
        });
        return textSize;
    }

    public void update() {
        if (this.mTexts.isEmpty()) {
            return;
        }
        this.mTexts.keyAt(0).post(this.mUpdateAll);
    }
}

package com.android.systemui.statusbar.phone;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import com.android.systemui.statusbar.policy.KeyButtonView;
import java.util.ArrayList;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/ButtonDispatcher.class */
public class ButtonDispatcher {
    private Integer mAlpha;
    private View.OnClickListener mClickListener;
    private View mCurrentView;
    private final int mId;
    private Drawable mImageDrawable;
    private View.OnLongClickListener mLongClickListener;
    private Boolean mLongClickable;
    private View.OnTouchListener mTouchListener;
    private final ArrayList<View> mViews = new ArrayList<>();
    private Integer mVisibility = -1;
    private int mImageResource = -1;

    public ButtonDispatcher(int i) {
        this.mId = i;
    }

    public void abortCurrentGesture() {
        int size = this.mViews.size();
        for (int i = 0; i < size; i++) {
            ((KeyButtonView) this.mViews.get(i)).abortCurrentGesture();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void addView(View view) {
        this.mViews.add(view);
        view.setOnClickListener(this.mClickListener);
        view.setOnTouchListener(this.mTouchListener);
        view.setOnLongClickListener(this.mLongClickListener);
        if (this.mLongClickable != null) {
            view.setLongClickable(this.mLongClickable.booleanValue());
        }
        if (this.mAlpha != null) {
            view.setAlpha(this.mAlpha.intValue());
        }
        if (this.mVisibility != null) {
            view.setVisibility(this.mVisibility.intValue());
        }
        if (this.mImageResource > 0) {
            ((ImageView) view).setImageResource(this.mImageResource);
        } else if (this.mImageDrawable != null) {
            ((ImageView) view).setImageDrawable(this.mImageDrawable);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void clear() {
        this.mViews.clear();
    }

    public float getAlpha() {
        return this.mAlpha != null ? this.mAlpha.intValue() : 1;
    }

    public View getCurrentView() {
        return this.mCurrentView;
    }

    public int getId() {
        return this.mId;
    }

    public int getVisibility() {
        return this.mVisibility != null ? this.mVisibility.intValue() : 0;
    }

    public void setAlpha(int i) {
        this.mAlpha = Integer.valueOf(i);
        int size = this.mViews.size();
        for (int i2 = 0; i2 < size; i2++) {
            this.mViews.get(i2).setAlpha(i);
        }
    }

    public void setCurrentView(View view) {
        this.mCurrentView = view.findViewById(this.mId);
    }

    public void setImageDrawable(Drawable drawable) {
        this.mImageDrawable = drawable;
        this.mImageResource = -1;
        int size = this.mViews.size();
        for (int i = 0; i < size; i++) {
            ((ImageView) this.mViews.get(i)).setImageDrawable(this.mImageDrawable);
        }
    }

    public void setLongClickable(boolean z) {
        this.mLongClickable = Boolean.valueOf(z);
        int size = this.mViews.size();
        for (int i = 0; i < size; i++) {
            this.mViews.get(i).setLongClickable(this.mLongClickable.booleanValue());
        }
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.mClickListener = onClickListener;
        int size = this.mViews.size();
        for (int i = 0; i < size; i++) {
            this.mViews.get(i).setOnClickListener(this.mClickListener);
        }
    }

    public void setOnLongClickListener(View.OnLongClickListener onLongClickListener) {
        this.mLongClickListener = onLongClickListener;
        int size = this.mViews.size();
        for (int i = 0; i < size; i++) {
            this.mViews.get(i).setOnLongClickListener(this.mLongClickListener);
        }
    }

    public void setOnTouchListener(View.OnTouchListener onTouchListener) {
        this.mTouchListener = onTouchListener;
        int size = this.mViews.size();
        for (int i = 0; i < size; i++) {
            this.mViews.get(i).setOnTouchListener(this.mTouchListener);
        }
    }

    public void setVisibility(int i) {
        if (this.mVisibility.intValue() == i) {
            return;
        }
        this.mVisibility = Integer.valueOf(i);
        int size = this.mViews.size();
        for (int i2 = 0; i2 < size; i2++) {
            this.mViews.get(i2).setVisibility(this.mVisibility.intValue());
        }
    }
}

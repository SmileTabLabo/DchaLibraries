package android.support.v7.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v4.widget.PopupWindowCompat;
import android.support.v7.appcompat.R$styleable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.PopupWindow;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
/* loaded from: a.zip:android/support/v7/widget/AppCompatPopupWindow.class */
class AppCompatPopupWindow extends PopupWindow {
    private static final boolean COMPAT_OVERLAP_ANCHOR;
    private boolean mOverlapAnchor;

    static {
        COMPAT_OVERLAP_ANCHOR = Build.VERSION.SDK_INT < 21;
    }

    public AppCompatPopupWindow(@NonNull Context context, @Nullable AttributeSet attributeSet, @AttrRes int i) {
        super(context, attributeSet, i);
        init(context, attributeSet, i, 0);
    }

    @TargetApi(11)
    public AppCompatPopupWindow(@NonNull Context context, @Nullable AttributeSet attributeSet, @AttrRes int i, @StyleRes int i2) {
        super(context, attributeSet, i, i2);
        init(context, attributeSet, i, i2);
    }

    private void init(Context context, AttributeSet attributeSet, int i, int i2) {
        TintTypedArray obtainStyledAttributes = TintTypedArray.obtainStyledAttributes(context, attributeSet, R$styleable.PopupWindow, i, i2);
        if (obtainStyledAttributes.hasValue(R$styleable.PopupWindow_overlapAnchor)) {
            setSupportOverlapAnchor(obtainStyledAttributes.getBoolean(R$styleable.PopupWindow_overlapAnchor, false));
        }
        setBackgroundDrawable(obtainStyledAttributes.getDrawable(R$styleable.PopupWindow_android_popupBackground));
        int i3 = Build.VERSION.SDK_INT;
        if (i2 != 0 && i3 < 11 && i3 >= 9 && obtainStyledAttributes.hasValue(R$styleable.PopupWindow_android_popupAnimationStyle)) {
            setAnimationStyle(obtainStyledAttributes.getResourceId(R$styleable.PopupWindow_android_popupAnimationStyle, -1));
        }
        obtainStyledAttributes.recycle();
        if (Build.VERSION.SDK_INT < 14) {
            wrapOnScrollChangedListener(this);
        }
    }

    private static void wrapOnScrollChangedListener(PopupWindow popupWindow) {
        try {
            Field declaredField = PopupWindow.class.getDeclaredField("mAnchor");
            declaredField.setAccessible(true);
            Field declaredField2 = PopupWindow.class.getDeclaredField("mOnScrollChangedListener");
            declaredField2.setAccessible(true);
            declaredField2.set(popupWindow, new ViewTreeObserver.OnScrollChangedListener(declaredField, popupWindow, (ViewTreeObserver.OnScrollChangedListener) declaredField2.get(popupWindow)) { // from class: android.support.v7.widget.AppCompatPopupWindow.1
                final Field val$fieldAnchor;
                final ViewTreeObserver.OnScrollChangedListener val$originalListener;
                final PopupWindow val$popup;

                {
                    this.val$fieldAnchor = declaredField;
                    this.val$popup = popupWindow;
                    this.val$originalListener = r6;
                }

                @Override // android.view.ViewTreeObserver.OnScrollChangedListener
                public void onScrollChanged() {
                    try {
                        WeakReference weakReference = (WeakReference) this.val$fieldAnchor.get(this.val$popup);
                        if (weakReference == null || weakReference.get() == null) {
                            return;
                        }
                        this.val$originalListener.onScrollChanged();
                    } catch (IllegalAccessException e) {
                    }
                }
            });
        } catch (Exception e) {
            Log.d("AppCompatPopupWindow", "Exception while installing workaround OnScrollChangedListener", e);
        }
    }

    public void setSupportOverlapAnchor(boolean z) {
        if (COMPAT_OVERLAP_ANCHOR) {
            this.mOverlapAnchor = z;
        } else {
            PopupWindowCompat.setOverlapAnchor(this, z);
        }
    }

    @Override // android.widget.PopupWindow
    public void showAsDropDown(View view, int i, int i2) {
        int i3 = i2;
        if (COMPAT_OVERLAP_ANCHOR) {
            i3 = i2;
            if (this.mOverlapAnchor) {
                i3 = i2 - view.getHeight();
            }
        }
        super.showAsDropDown(view, i, i3);
    }

    @Override // android.widget.PopupWindow
    @TargetApi(19)
    public void showAsDropDown(View view, int i, int i2, int i3) {
        int i4 = i2;
        if (COMPAT_OVERLAP_ANCHOR) {
            i4 = i2;
            if (this.mOverlapAnchor) {
                i4 = i2 - view.getHeight();
            }
        }
        super.showAsDropDown(view, i, i4, i3);
    }

    @Override // android.widget.PopupWindow
    public void update(View view, int i, int i2, int i3, int i4) {
        int i5 = i2;
        if (COMPAT_OVERLAP_ANCHOR) {
            i5 = i2;
            if (this.mOverlapAnchor) {
                i5 = i2 - view.getHeight();
            }
        }
        super.update(view, i, i5, i3, i4);
    }
}

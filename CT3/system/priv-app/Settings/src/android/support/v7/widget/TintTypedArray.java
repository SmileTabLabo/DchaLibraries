package android.support.v7.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v7.content.res.AppCompatResources;
import android.util.AttributeSet;
/* loaded from: classes.dex */
public class TintTypedArray {
    private final Context mContext;
    private final TypedArray mWrapped;

    public static TintTypedArray obtainStyledAttributes(Context context, AttributeSet set, int[] attrs) {
        return new TintTypedArray(context, context.obtainStyledAttributes(set, attrs));
    }

    public static TintTypedArray obtainStyledAttributes(Context context, AttributeSet set, int[] attrs, int defStyleAttr, int defStyleRes) {
        return new TintTypedArray(context, context.obtainStyledAttributes(set, attrs, defStyleAttr, defStyleRes));
    }

    public static TintTypedArray obtainStyledAttributes(Context context, int resid, int[] attrs) {
        return new TintTypedArray(context, context.obtainStyledAttributes(resid, attrs));
    }

    private TintTypedArray(Context context, TypedArray array) {
        this.mContext = context;
        this.mWrapped = array;
    }

    public Drawable getDrawable(int index) {
        int resourceId;
        if (this.mWrapped.hasValue(index) && (resourceId = this.mWrapped.getResourceId(index, 0)) != 0) {
            return AppCompatDrawableManager.get().getDrawable(this.mContext, resourceId);
        }
        return this.mWrapped.getDrawable(index);
    }

    public CharSequence getText(int index) {
        return this.mWrapped.getText(index);
    }

    public boolean getBoolean(int index, boolean defValue) {
        return this.mWrapped.getBoolean(index, defValue);
    }

    public int getInt(int index, int defValue) {
        return this.mWrapped.getInt(index, defValue);
    }

    public float getFloat(int index, float defValue) {
        return this.mWrapped.getFloat(index, defValue);
    }

    public int getColor(int index, int defValue) {
        return this.mWrapped.getColor(index, defValue);
    }

    public ColorStateList getColorStateList(int index) {
        int resourceId;
        ColorStateList value;
        if (this.mWrapped.hasValue(index) && (resourceId = this.mWrapped.getResourceId(index, 0)) != 0 && (value = AppCompatResources.getColorStateList(this.mContext, resourceId)) != null) {
            return value;
        }
        return this.mWrapped.getColorStateList(index);
    }

    public int getInteger(int index, int defValue) {
        return this.mWrapped.getInteger(index, defValue);
    }

    public int getDimensionPixelOffset(int index, int defValue) {
        return this.mWrapped.getDimensionPixelOffset(index, defValue);
    }

    public int getDimensionPixelSize(int index, int defValue) {
        return this.mWrapped.getDimensionPixelSize(index, defValue);
    }

    public int getLayoutDimension(int index, int defValue) {
        return this.mWrapped.getLayoutDimension(index, defValue);
    }

    public int getResourceId(int index, int defValue) {
        return this.mWrapped.getResourceId(index, defValue);
    }

    public boolean hasValue(int index) {
        return this.mWrapped.hasValue(index);
    }

    public void recycle() {
        this.mWrapped.recycle();
    }
}

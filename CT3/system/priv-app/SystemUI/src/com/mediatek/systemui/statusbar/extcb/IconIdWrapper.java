package com.mediatek.systemui.statusbar.extcb;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
/* loaded from: a.zip:com/mediatek/systemui/statusbar/extcb/IconIdWrapper.class */
public class IconIdWrapper implements Cloneable {
    private int mIconId;
    private Resources mResources;

    public IconIdWrapper() {
        this(null, 0);
    }

    public IconIdWrapper(int i) {
        this(null, i);
    }

    public IconIdWrapper(Resources resources, int i) {
        this.mResources = null;
        this.mIconId = 0;
        this.mResources = resources;
        this.mIconId = i;
    }

    /* renamed from: clone */
    public IconIdWrapper m2398clone() {
        IconIdWrapper iconIdWrapper;
        try {
            iconIdWrapper = (IconIdWrapper) super.clone();
            iconIdWrapper.mResources = this.mResources;
            iconIdWrapper.mIconId = this.mIconId;
        } catch (CloneNotSupportedException e) {
            iconIdWrapper = null;
        }
        return iconIdWrapper;
    }

    public void copyFrom(IconIdWrapper iconIdWrapper) {
        this.mResources = iconIdWrapper.mResources;
        this.mIconId = iconIdWrapper.mIconId;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null && getClass() == obj.getClass()) {
            IconIdWrapper iconIdWrapper = (IconIdWrapper) obj;
            if (this.mIconId != iconIdWrapper.mIconId) {
                return false;
            }
            return this.mResources == null ? iconIdWrapper.mResources == null : this.mResources.equals(iconIdWrapper.mResources);
        }
        return false;
    }

    public Drawable getDrawable() {
        if (this.mResources == null || this.mIconId == 0) {
            return null;
        }
        return this.mResources.getDrawable(this.mIconId);
    }

    public int getIconId() {
        return this.mIconId;
    }

    public Resources getResources() {
        return this.mResources;
    }

    public int hashCode() {
        return ((this.mIconId + 31) * 31) + (this.mResources == null ? 0 : this.mResources.hashCode());
    }

    public void setIconId(int i) {
        this.mIconId = i;
    }

    public void setResources(Resources resources) {
        this.mResources = resources;
    }

    public String toString() {
        return getResources() == null ? "IconIdWrapper [mResources == null, mIconId=" + this.mIconId + "]" : "IconIdWrapper [mResources != null, mIconId=" + this.mIconId + "]";
    }
}

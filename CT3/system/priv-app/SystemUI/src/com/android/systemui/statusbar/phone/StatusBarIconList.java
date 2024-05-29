package com.android.systemui.statusbar.phone;

import com.android.internal.statusbar.StatusBarIcon;
import java.util.ArrayList;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/StatusBarIconList.class */
public class StatusBarIconList {
    private ArrayList<String> mSlots = new ArrayList<>();
    private ArrayList<StatusBarIcon> mIcons = new ArrayList<>();

    public StatusBarIconList(String[] strArr) {
        for (String str : strArr) {
            this.mSlots.add(str);
            this.mIcons.add(null);
        }
    }

    public StatusBarIcon getIcon(int i) {
        return this.mIcons.get(i);
    }

    public String getSlot(int i) {
        return this.mSlots.get(i);
    }

    public int getSlotIndex(String str) {
        int size = this.mSlots.size();
        for (int i = 0; i < size; i++) {
            if (str.equals(this.mSlots.get(i))) {
                return i;
            }
        }
        this.mSlots.add(0, str);
        this.mIcons.add(0, null);
        return 0;
    }

    public int getViewIndex(int i) {
        int i2 = 0;
        int i3 = 0;
        while (i3 < i) {
            int i4 = i2;
            if (this.mIcons.get(i3) != null) {
                i4 = i2 + 1;
            }
            i3++;
            i2 = i4;
        }
        return i2;
    }

    public void removeIcon(int i) {
        this.mIcons.set(i, null);
    }

    public void setIcon(int i, StatusBarIcon statusBarIcon) {
        this.mIcons.set(i, statusBarIcon);
    }
}

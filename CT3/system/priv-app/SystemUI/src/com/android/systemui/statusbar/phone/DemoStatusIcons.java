package com.android.systemui.statusbar.phone;

import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.UserHandle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.systemui.DemoMode;
import com.android.systemui.statusbar.StatusBarIconView;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/DemoStatusIcons.class */
public class DemoStatusIcons extends LinearLayout implements DemoMode {
    private boolean mDemoMode;
    private final int mIconSize;
    private final LinearLayout mStatusIcons;

    public DemoStatusIcons(LinearLayout linearLayout, int i) {
        super(linearLayout.getContext());
        this.mStatusIcons = linearLayout;
        this.mIconSize = i;
        setLayoutParams(this.mStatusIcons.getLayoutParams());
        setOrientation(this.mStatusIcons.getOrientation());
        setGravity(16);
        ViewGroup viewGroup = (ViewGroup) this.mStatusIcons.getParent();
        viewGroup.addView(this, viewGroup.indexOfChild(this.mStatusIcons));
    }

    private void updateSlot(String str, String str2, int i) {
        int i2;
        if (this.mDemoMode) {
            String str3 = str2;
            if (str2 == null) {
                str3 = this.mContext.getPackageName();
            }
            int i3 = 0;
            while (true) {
                i2 = -1;
                if (i3 >= getChildCount()) {
                    break;
                }
                StatusBarIconView statusBarIconView = (StatusBarIconView) getChildAt(i3);
                if (!str.equals(statusBarIconView.getTag())) {
                    i3++;
                } else if (i != 0) {
                    StatusBarIcon statusBarIcon = statusBarIconView.getStatusBarIcon();
                    statusBarIcon.icon = Icon.createWithResource(statusBarIcon.icon.getResPackage(), i);
                    statusBarIconView.set(statusBarIcon);
                    statusBarIconView.updateDrawable();
                    return;
                } else {
                    i2 = i3;
                }
            }
            if (i == 0) {
                if (i2 != -1) {
                    removeViewAt(i2);
                    return;
                }
                return;
            }
            StatusBarIcon statusBarIcon2 = new StatusBarIcon(str3, UserHandle.SYSTEM, i, 0, 0, "Demo");
            StatusBarIconView statusBarIconView2 = new StatusBarIconView(getContext(), null, null);
            statusBarIconView2.setTag(str);
            statusBarIconView2.set(statusBarIcon2);
            addView(statusBarIconView2, 0, new LinearLayout.LayoutParams(this.mIconSize, this.mIconSize));
        }
    }

    @Override // com.android.systemui.DemoMode
    public void dispatchDemoCommand(String str, Bundle bundle) {
        if (!this.mDemoMode && str.equals("enter")) {
            this.mDemoMode = true;
            this.mStatusIcons.setVisibility(8);
            setVisibility(0);
        } else if (this.mDemoMode && str.equals("exit")) {
            this.mDemoMode = false;
            this.mStatusIcons.setVisibility(0);
            setVisibility(8);
        } else if (this.mDemoMode && str.equals("status")) {
            String string = bundle.getString("volume");
            if (string != null) {
                updateSlot("volume", null, string.equals("vibrate") ? 2130838290 : 0);
            }
            String string2 = bundle.getString("zen");
            if (string2 != null) {
                updateSlot("zen", null, string2.equals("important") ? 2130838333 : string2.equals("none") ? 2130838334 : 0);
            }
            String string3 = bundle.getString("bluetooth");
            if (string3 != null) {
                updateSlot("bluetooth", null, string3.equals("disconnected") ? 2130838258 : string3.equals("connected") ? 2130838259 : 0);
            }
            String string4 = bundle.getString("location");
            if (string4 != null) {
                updateSlot("location", null, string4.equals("show") ? 2130838278 : 0);
            }
            String string5 = bundle.getString("alarm");
            if (string5 != null) {
                updateSlot("alarm_clock", null, string5.equals("show") ? 2130838253 : 0);
            }
            String string6 = bundle.getString("tty");
            if (string6 != null) {
                updateSlot("tty", null, string6.equals("show") ? 2130838306 : 0);
            }
            String string7 = bundle.getString("mute");
            if (string7 != null) {
                updateSlot("mute", null, string7.equals("show") ? 17301622 : 0);
            }
            String string8 = bundle.getString("speakerphone");
            if (string8 != null) {
                updateSlot("speakerphone", null, string8.equals("show") ? 17301639 : 0);
            }
            String string9 = bundle.getString("cast");
            if (string9 != null) {
                updateSlot("cast", null, string9.equals("show") ? 2130838257 : 0);
            }
            String string10 = bundle.getString("hotspot");
            if (string10 != null) {
                updateSlot("hotspot", null, string10.equals("show") ? 2130838277 : 0);
            }
        }
    }
}

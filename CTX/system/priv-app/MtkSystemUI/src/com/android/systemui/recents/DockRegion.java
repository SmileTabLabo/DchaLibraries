package com.android.systemui.recents;

import com.android.systemui.recents.views.DockState;
/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: RecentsConfiguration.java */
/* loaded from: classes.dex */
public class DockRegion {
    public static DockState[] PHONE_LANDSCAPE = {DockState.LEFT};
    public static DockState[] PHONE_PORTRAIT = {DockState.TOP};
    public static DockState[] TABLET_LANDSCAPE = {DockState.LEFT, DockState.RIGHT};
    public static DockState[] TABLET_PORTRAIT = PHONE_PORTRAIT;
}

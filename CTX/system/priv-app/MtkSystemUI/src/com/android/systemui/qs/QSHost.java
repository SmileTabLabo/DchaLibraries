package com.android.systemui.qs;

import android.content.Context;
import com.android.systemui.qs.external.TileServices;
/* loaded from: classes.dex */
public interface QSHost {

    /* loaded from: classes.dex */
    public interface Callback {
        void onTilesChanged();
    }

    void collapsePanels();

    Context getContext();

    TileServices getTileServices();

    int indexOf(String str);

    void openPanels();

    void removeTile(String str);

    void warn(String str, Throwable th);
}

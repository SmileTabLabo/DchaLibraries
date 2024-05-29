package android.support.v4.widget;

import android.os.Build;
import android.support.annotation.NonNull;
import android.widget.ListView;
/* loaded from: a.zip:android/support/v4/widget/ListViewCompat.class */
public final class ListViewCompat {
    private ListViewCompat() {
    }

    public static void scrollListBy(@NonNull ListView listView, int i) {
        if (Build.VERSION.SDK_INT >= 19) {
            ListViewCompatKitKat.scrollListBy(listView, i);
        } else {
            ListViewCompatDonut.scrollListBy(listView, i);
        }
    }
}

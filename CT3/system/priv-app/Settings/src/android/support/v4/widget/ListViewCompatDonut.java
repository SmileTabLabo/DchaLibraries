package android.support.v4.widget;

import android.view.View;
import android.widget.ListView;
/* loaded from: classes.dex */
class ListViewCompatDonut {
    ListViewCompatDonut() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void scrollListBy(ListView listView, int y) {
        View firstView;
        int firstPosition = listView.getFirstVisiblePosition();
        if (firstPosition == -1 || (firstView = listView.getChildAt(0)) == null) {
            return;
        }
        int newTop = firstView.getTop() - y;
        listView.setSelectionFromTop(firstPosition, newTop);
    }
}

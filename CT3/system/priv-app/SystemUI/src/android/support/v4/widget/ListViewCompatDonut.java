package android.support.v4.widget;

import android.view.View;
import android.widget.ListView;
/* loaded from: a.zip:android/support/v4/widget/ListViewCompatDonut.class */
class ListViewCompatDonut {
    ListViewCompatDonut() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void scrollListBy(ListView listView, int i) {
        View childAt;
        int firstVisiblePosition = listView.getFirstVisiblePosition();
        if (firstVisiblePosition == -1 || (childAt = listView.getChildAt(0)) == null) {
            return;
        }
        listView.setSelectionFromTop(firstVisiblePosition, childAt.getTop() - i);
    }
}

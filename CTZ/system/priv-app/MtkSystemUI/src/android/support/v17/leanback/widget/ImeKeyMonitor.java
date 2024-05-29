package android.support.v17.leanback.widget;

import android.view.KeyEvent;
import android.widget.EditText;
/* loaded from: classes.dex */
public interface ImeKeyMonitor {

    /* loaded from: classes.dex */
    public interface ImeKeyListener {
        boolean onKeyPreIme(EditText editText, int i, KeyEvent keyEvent);
    }
}

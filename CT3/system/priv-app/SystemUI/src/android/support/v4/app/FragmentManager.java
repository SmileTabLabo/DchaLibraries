package android.support.v4.app;

import java.io.FileDescriptor;
import java.io.PrintWriter;
/* loaded from: a.zip:android/support/v4/app/FragmentManager.class */
public abstract class FragmentManager {

    /* loaded from: a.zip:android/support/v4/app/FragmentManager$OnBackStackChangedListener.class */
    public interface OnBackStackChangedListener {
        void onBackStackChanged();
    }

    public abstract FragmentTransaction beginTransaction();

    public abstract void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr);

    public abstract boolean executePendingTransactions();

    public abstract Fragment findFragmentByTag(String str);

    public abstract boolean popBackStackImmediate();
}

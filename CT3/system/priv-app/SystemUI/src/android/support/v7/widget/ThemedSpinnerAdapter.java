package android.support.v7.widget;

import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.widget.SpinnerAdapter;
/* loaded from: a.zip:android/support/v7/widget/ThemedSpinnerAdapter.class */
public interface ThemedSpinnerAdapter extends SpinnerAdapter {
    @Nullable
    Resources.Theme getDropDownViewTheme();

    void setDropDownViewTheme(@Nullable Resources.Theme theme);
}

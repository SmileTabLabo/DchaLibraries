package android.support.v4.view;

import android.view.WindowInsets;
/* loaded from: a.zip:android/support/v4/view/WindowInsetsCompatApi21.class */
class WindowInsetsCompatApi21 extends WindowInsetsCompat {
    private final WindowInsets mSource;

    /* JADX INFO: Access modifiers changed from: package-private */
    public WindowInsetsCompatApi21(WindowInsets windowInsets) {
        this.mSource = windowInsets;
    }

    @Override // android.support.v4.view.WindowInsetsCompat
    public int getSystemWindowInsetBottom() {
        return this.mSource.getSystemWindowInsetBottom();
    }

    @Override // android.support.v4.view.WindowInsetsCompat
    public int getSystemWindowInsetLeft() {
        return this.mSource.getSystemWindowInsetLeft();
    }

    @Override // android.support.v4.view.WindowInsetsCompat
    public int getSystemWindowInsetRight() {
        return this.mSource.getSystemWindowInsetRight();
    }

    @Override // android.support.v4.view.WindowInsetsCompat
    public int getSystemWindowInsetTop() {
        return this.mSource.getSystemWindowInsetTop();
    }

    @Override // android.support.v4.view.WindowInsetsCompat
    public boolean isConsumed() {
        return this.mSource.isConsumed();
    }

    @Override // android.support.v4.view.WindowInsetsCompat
    public WindowInsetsCompat replaceSystemWindowInsets(int i, int i2, int i3, int i4) {
        return new WindowInsetsCompatApi21(this.mSource.replaceSystemWindowInsets(i, i2, i3, i4));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public WindowInsets unwrap() {
        return this.mSource;
    }
}

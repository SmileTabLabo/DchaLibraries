package android.support.v17.leanback.widget;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
/* loaded from: a.zip:android/support/v17/leanback/widget/ForegroundHelper.class */
final class ForegroundHelper {
    static final ForegroundHelper sInstance = new ForegroundHelper();
    ForegroundHelperVersionImpl mImpl;

    /* loaded from: a.zip:android/support/v17/leanback/widget/ForegroundHelper$ForegroundHelperApi23Impl.class */
    private static final class ForegroundHelperApi23Impl implements ForegroundHelperVersionImpl {
        private ForegroundHelperApi23Impl() {
        }

        /* synthetic */ ForegroundHelperApi23Impl(ForegroundHelperApi23Impl foregroundHelperApi23Impl) {
            this();
        }

        @Override // android.support.v17.leanback.widget.ForegroundHelper.ForegroundHelperVersionImpl
        public void setForeground(View view, Drawable drawable) {
            ForegroundHelperApi23.setForeground(view, drawable);
        }
    }

    /* loaded from: a.zip:android/support/v17/leanback/widget/ForegroundHelper$ForegroundHelperStubImpl.class */
    private static final class ForegroundHelperStubImpl implements ForegroundHelperVersionImpl {
        private ForegroundHelperStubImpl() {
        }

        /* synthetic */ ForegroundHelperStubImpl(ForegroundHelperStubImpl foregroundHelperStubImpl) {
            this();
        }

        @Override // android.support.v17.leanback.widget.ForegroundHelper.ForegroundHelperVersionImpl
        public void setForeground(View view, Drawable drawable) {
        }
    }

    /* loaded from: a.zip:android/support/v17/leanback/widget/ForegroundHelper$ForegroundHelperVersionImpl.class */
    interface ForegroundHelperVersionImpl {
        void setForeground(View view, Drawable drawable);
    }

    private ForegroundHelper() {
        if (supportsForeground()) {
            this.mImpl = new ForegroundHelperApi23Impl(null);
        } else {
            this.mImpl = new ForegroundHelperStubImpl(null);
        }
    }

    public static ForegroundHelper getInstance() {
        return sInstance;
    }

    public static boolean supportsForeground() {
        return Build.VERSION.SDK_INT >= 23;
    }

    public void setForeground(View view, Drawable drawable) {
        this.mImpl.setForeground(view, drawable);
    }
}

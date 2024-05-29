package android.support.v17.leanback.widget;

import android.os.Build;
import android.view.View;
/* loaded from: a.zip:android/support/v17/leanback/widget/ShadowHelper.class */
final class ShadowHelper {
    static final ShadowHelper sInstance = new ShadowHelper();
    ShadowHelperVersionImpl mImpl;
    boolean mSupportsDynamicShadow;

    /* loaded from: a.zip:android/support/v17/leanback/widget/ShadowHelper$ShadowHelperApi21Impl.class */
    private static final class ShadowHelperApi21Impl implements ShadowHelperVersionImpl {
        private ShadowHelperApi21Impl() {
        }

        /* synthetic */ ShadowHelperApi21Impl(ShadowHelperApi21Impl shadowHelperApi21Impl) {
            this();
        }

        @Override // android.support.v17.leanback.widget.ShadowHelper.ShadowHelperVersionImpl
        public void setZ(View view, float f) {
            ShadowHelperApi21.setZ(view, f);
        }
    }

    /* loaded from: a.zip:android/support/v17/leanback/widget/ShadowHelper$ShadowHelperStubImpl.class */
    private static final class ShadowHelperStubImpl implements ShadowHelperVersionImpl {
        private ShadowHelperStubImpl() {
        }

        /* synthetic */ ShadowHelperStubImpl(ShadowHelperStubImpl shadowHelperStubImpl) {
            this();
        }

        @Override // android.support.v17.leanback.widget.ShadowHelper.ShadowHelperVersionImpl
        public void setZ(View view, float f) {
        }
    }

    /* loaded from: a.zip:android/support/v17/leanback/widget/ShadowHelper$ShadowHelperVersionImpl.class */
    interface ShadowHelperVersionImpl {
        void setZ(View view, float f);
    }

    private ShadowHelper() {
        if (Build.VERSION.SDK_INT < 21) {
            this.mImpl = new ShadowHelperStubImpl(null);
            return;
        }
        this.mSupportsDynamicShadow = true;
        this.mImpl = new ShadowHelperApi21Impl(null);
    }

    public static ShadowHelper getInstance() {
        return sInstance;
    }

    public void setZ(View view, float f) {
        this.mImpl.setZ(view, f);
    }

    public boolean supportsDynamicShadow() {
        return this.mSupportsDynamicShadow;
    }
}

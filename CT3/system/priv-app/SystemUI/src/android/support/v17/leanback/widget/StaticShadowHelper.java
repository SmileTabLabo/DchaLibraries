package android.support.v17.leanback.widget;

import android.os.Build;
/* loaded from: a.zip:android/support/v17/leanback/widget/StaticShadowHelper.class */
final class StaticShadowHelper {
    static final StaticShadowHelper sInstance = new StaticShadowHelper();
    ShadowHelperVersionImpl mImpl;
    boolean mSupportsShadow;

    /* loaded from: a.zip:android/support/v17/leanback/widget/StaticShadowHelper$ShadowHelperJbmr2Impl.class */
    private static final class ShadowHelperJbmr2Impl implements ShadowHelperVersionImpl {
        private ShadowHelperJbmr2Impl() {
        }

        /* synthetic */ ShadowHelperJbmr2Impl(ShadowHelperJbmr2Impl shadowHelperJbmr2Impl) {
            this();
        }
    }

    /* loaded from: a.zip:android/support/v17/leanback/widget/StaticShadowHelper$ShadowHelperStubImpl.class */
    private static final class ShadowHelperStubImpl implements ShadowHelperVersionImpl {
        private ShadowHelperStubImpl() {
        }

        /* synthetic */ ShadowHelperStubImpl(ShadowHelperStubImpl shadowHelperStubImpl) {
            this();
        }
    }

    /* loaded from: a.zip:android/support/v17/leanback/widget/StaticShadowHelper$ShadowHelperVersionImpl.class */
    interface ShadowHelperVersionImpl {
    }

    private StaticShadowHelper() {
        if (Build.VERSION.SDK_INT >= 18) {
            this.mSupportsShadow = true;
            this.mImpl = new ShadowHelperJbmr2Impl(null);
            return;
        }
        this.mSupportsShadow = false;
        this.mImpl = new ShadowHelperStubImpl(null);
    }

    public static StaticShadowHelper getInstance() {
        return sInstance;
    }

    public boolean supportsShadow() {
        return this.mSupportsShadow;
    }
}

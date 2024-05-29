package android.support.v4.content.res;

import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.NonNull;
/* loaded from: a.zip:android/support/v4/content/res/ConfigurationHelper.class */
public final class ConfigurationHelper {
    private static final ConfigurationHelperImpl IMPL;

    /* loaded from: a.zip:android/support/v4/content/res/ConfigurationHelper$ConfigurationHelperImpl.class */
    private interface ConfigurationHelperImpl {
        int getScreenHeightDp(@NonNull Resources resources);

        int getScreenWidthDp(@NonNull Resources resources);

        int getSmallestScreenWidthDp(@NonNull Resources resources);
    }

    /* loaded from: a.zip:android/support/v4/content/res/ConfigurationHelper$DonutImpl.class */
    private static class DonutImpl implements ConfigurationHelperImpl {
        private DonutImpl() {
        }

        /* synthetic */ DonutImpl(DonutImpl donutImpl) {
            this();
        }

        @Override // android.support.v4.content.res.ConfigurationHelper.ConfigurationHelperImpl
        public int getScreenHeightDp(@NonNull Resources resources) {
            return ConfigurationHelperDonut.getScreenHeightDp(resources);
        }

        @Override // android.support.v4.content.res.ConfigurationHelper.ConfigurationHelperImpl
        public int getScreenWidthDp(@NonNull Resources resources) {
            return ConfigurationHelperDonut.getScreenWidthDp(resources);
        }

        @Override // android.support.v4.content.res.ConfigurationHelper.ConfigurationHelperImpl
        public int getSmallestScreenWidthDp(@NonNull Resources resources) {
            return ConfigurationHelperDonut.getSmallestScreenWidthDp(resources);
        }
    }

    /* loaded from: a.zip:android/support/v4/content/res/ConfigurationHelper$HoneycombMr2Impl.class */
    private static class HoneycombMr2Impl extends DonutImpl {
        private HoneycombMr2Impl() {
            super(null);
        }

        /* synthetic */ HoneycombMr2Impl(HoneycombMr2Impl honeycombMr2Impl) {
            this();
        }

        @Override // android.support.v4.content.res.ConfigurationHelper.DonutImpl, android.support.v4.content.res.ConfigurationHelper.ConfigurationHelperImpl
        public int getScreenHeightDp(@NonNull Resources resources) {
            return ConfigurationHelperHoneycombMr2.getScreenHeightDp(resources);
        }

        @Override // android.support.v4.content.res.ConfigurationHelper.DonutImpl, android.support.v4.content.res.ConfigurationHelper.ConfigurationHelperImpl
        public int getScreenWidthDp(@NonNull Resources resources) {
            return ConfigurationHelperHoneycombMr2.getScreenWidthDp(resources);
        }

        @Override // android.support.v4.content.res.ConfigurationHelper.DonutImpl, android.support.v4.content.res.ConfigurationHelper.ConfigurationHelperImpl
        public int getSmallestScreenWidthDp(@NonNull Resources resources) {
            return ConfigurationHelperHoneycombMr2.getSmallestScreenWidthDp(resources);
        }
    }

    /* loaded from: a.zip:android/support/v4/content/res/ConfigurationHelper$JellybeanMr1Impl.class */
    private static class JellybeanMr1Impl extends HoneycombMr2Impl {
        private JellybeanMr1Impl() {
            super(null);
        }

        /* synthetic */ JellybeanMr1Impl(JellybeanMr1Impl jellybeanMr1Impl) {
            this();
        }
    }

    static {
        int i = Build.VERSION.SDK_INT;
        if (i >= 17) {
            IMPL = new JellybeanMr1Impl(null);
        } else if (i >= 13) {
            IMPL = new HoneycombMr2Impl(null);
        } else {
            IMPL = new DonutImpl(null);
        }
    }

    private ConfigurationHelper() {
    }

    public static int getScreenHeightDp(@NonNull Resources resources) {
        return IMPL.getScreenHeightDp(resources);
    }

    public static int getScreenWidthDp(@NonNull Resources resources) {
        return IMPL.getScreenWidthDp(resources);
    }

    public static int getSmallestScreenWidthDp(@NonNull Resources resources) {
        return IMPL.getSmallestScreenWidthDp(resources);
    }
}

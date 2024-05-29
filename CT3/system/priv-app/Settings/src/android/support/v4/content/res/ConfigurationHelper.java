package android.support.v4.content.res;

import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.NonNull;
/* loaded from: classes.dex */
public final class ConfigurationHelper {
    private static final ConfigurationHelperImpl IMPL;

    /* loaded from: classes.dex */
    private interface ConfigurationHelperImpl {
        int getScreenHeightDp(@NonNull Resources resources);

        int getScreenWidthDp(@NonNull Resources resources);

        int getSmallestScreenWidthDp(@NonNull Resources resources);
    }

    static {
        int sdk = Build.VERSION.SDK_INT;
        if (sdk >= 17) {
            IMPL = new JellybeanMr1Impl(null);
        } else if (sdk >= 13) {
            IMPL = new HoneycombMr2Impl(null);
        } else {
            IMPL = new DonutImpl(null);
        }
    }

    private ConfigurationHelper() {
    }

    /* loaded from: classes.dex */
    private static class DonutImpl implements ConfigurationHelperImpl {
        /* synthetic */ DonutImpl(DonutImpl donutImpl) {
            this();
        }

        private DonutImpl() {
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

    /* loaded from: classes.dex */
    private static class HoneycombMr2Impl extends DonutImpl {
        /* synthetic */ HoneycombMr2Impl(HoneycombMr2Impl honeycombMr2Impl) {
            this();
        }

        private HoneycombMr2Impl() {
            super(null);
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

    /* loaded from: classes.dex */
    private static class JellybeanMr1Impl extends HoneycombMr2Impl {
        /* synthetic */ JellybeanMr1Impl(JellybeanMr1Impl jellybeanMr1Impl) {
            this();
        }

        private JellybeanMr1Impl() {
            super(null);
        }
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

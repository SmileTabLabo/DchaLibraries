package android.support.v7.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.util.ArrayMap;
import android.support.v4.util.LongSparseArray;
import android.support.v4.util.LruCache;
import android.support.v7.appcompat.R$attr;
import android.support.v7.appcompat.R$color;
import android.support.v7.appcompat.R$drawable;
import android.support.v7.content.res.AppCompatResources;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.util.Xml;
import java.lang.ref.WeakReference;
import java.util.WeakHashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
/* loaded from: a.zip:android/support/v7/widget/AppCompatDrawableManager.class */
public final class AppCompatDrawableManager {
    private static AppCompatDrawableManager INSTANCE;
    private ArrayMap<String, InflateDelegate> mDelegates;
    private final Object mDrawableCacheLock = new Object();
    private final WeakHashMap<Context, LongSparseArray<WeakReference<Drawable.ConstantState>>> mDrawableCaches = new WeakHashMap<>(0);
    private boolean mHasCheckedVectorDrawableSetup;
    private SparseArray<String> mKnownDrawableIdTags;
    private WeakHashMap<Context, SparseArray<ColorStateList>> mTintLists;
    private TypedValue mTypedValue;
    private static final PorterDuff.Mode DEFAULT_MODE = PorterDuff.Mode.SRC_IN;
    private static final ColorFilterLruCache COLOR_FILTER_CACHE = new ColorFilterLruCache(6);
    private static final int[] COLORFILTER_TINT_COLOR_CONTROL_NORMAL = {R$drawable.abc_textfield_search_default_mtrl_alpha, R$drawable.abc_textfield_default_mtrl_alpha, R$drawable.abc_ab_share_pack_mtrl_alpha};
    private static final int[] TINT_COLOR_CONTROL_NORMAL = {R$drawable.abc_ic_commit_search_api_mtrl_alpha, R$drawable.abc_seekbar_tick_mark_material, R$drawable.abc_ic_menu_share_mtrl_alpha, R$drawable.abc_ic_menu_copy_mtrl_am_alpha, R$drawable.abc_ic_menu_cut_mtrl_alpha, R$drawable.abc_ic_menu_selectall_mtrl_alpha, R$drawable.abc_ic_menu_paste_mtrl_am_alpha};
    private static final int[] COLORFILTER_COLOR_CONTROL_ACTIVATED = {R$drawable.abc_textfield_activated_mtrl_alpha, R$drawable.abc_textfield_search_activated_mtrl_alpha, R$drawable.abc_cab_background_top_mtrl_alpha, R$drawable.abc_text_cursor_material};
    private static final int[] COLORFILTER_COLOR_BACKGROUND_MULTIPLY = {R$drawable.abc_popup_background_mtrl_mult, R$drawable.abc_cab_background_internal_bg, R$drawable.abc_menu_hardkey_panel_mtrl_mult};
    private static final int[] TINT_COLOR_CONTROL_STATE_LIST = {R$drawable.abc_tab_indicator_material, R$drawable.abc_textfield_search_material};
    private static final int[] TINT_CHECKABLE_BUTTON_LIST = {R$drawable.abc_btn_check_material, R$drawable.abc_btn_radio_material};

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:android/support/v7/widget/AppCompatDrawableManager$AvdcInflateDelegate.class */
    public static class AvdcInflateDelegate implements InflateDelegate {
        private AvdcInflateDelegate() {
        }

        /* synthetic */ AvdcInflateDelegate(AvdcInflateDelegate avdcInflateDelegate) {
            this();
        }

        @Override // android.support.v7.widget.AppCompatDrawableManager.InflateDelegate
        public Drawable createFromXmlInner(@NonNull Context context, @NonNull XmlPullParser xmlPullParser, @NonNull AttributeSet attributeSet, @Nullable Resources.Theme theme) {
            try {
                return AnimatedVectorDrawableCompat.createFromXmlInner(context, context.getResources(), xmlPullParser, attributeSet, theme);
            } catch (Exception e) {
                Log.e("AvdcInflateDelegate", "Exception while inflating <animated-vector>", e);
                return null;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:android/support/v7/widget/AppCompatDrawableManager$ColorFilterLruCache.class */
    public static class ColorFilterLruCache extends LruCache<Integer, PorterDuffColorFilter> {
        public ColorFilterLruCache(int i) {
            super(i);
        }

        private static int generateCacheKey(int i, PorterDuff.Mode mode) {
            return ((i + 31) * 31) + mode.hashCode();
        }

        PorterDuffColorFilter get(int i, PorterDuff.Mode mode) {
            return get(Integer.valueOf(generateCacheKey(i, mode)));
        }

        PorterDuffColorFilter put(int i, PorterDuff.Mode mode, PorterDuffColorFilter porterDuffColorFilter) {
            return put(Integer.valueOf(generateCacheKey(i, mode)), porterDuffColorFilter);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:android/support/v7/widget/AppCompatDrawableManager$InflateDelegate.class */
    public interface InflateDelegate {
        Drawable createFromXmlInner(@NonNull Context context, @NonNull XmlPullParser xmlPullParser, @NonNull AttributeSet attributeSet, @Nullable Resources.Theme theme);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:android/support/v7/widget/AppCompatDrawableManager$VdcInflateDelegate.class */
    public static class VdcInflateDelegate implements InflateDelegate {
        private VdcInflateDelegate() {
        }

        /* synthetic */ VdcInflateDelegate(VdcInflateDelegate vdcInflateDelegate) {
            this();
        }

        @Override // android.support.v7.widget.AppCompatDrawableManager.InflateDelegate
        public Drawable createFromXmlInner(@NonNull Context context, @NonNull XmlPullParser xmlPullParser, @NonNull AttributeSet attributeSet, @Nullable Resources.Theme theme) {
            try {
                return VectorDrawableCompat.createFromXmlInner(context.getResources(), xmlPullParser, attributeSet, theme);
            } catch (Exception e) {
                Log.e("VdcInflateDelegate", "Exception while inflating <vector>", e);
                return null;
            }
        }
    }

    private void addDelegate(@NonNull String str, @NonNull InflateDelegate inflateDelegate) {
        if (this.mDelegates == null) {
            this.mDelegates = new ArrayMap<>();
        }
        this.mDelegates.put(str, inflateDelegate);
    }

    private boolean addDrawableToCache(@NonNull Context context, long j, @NonNull Drawable drawable) {
        Drawable.ConstantState constantState = drawable.getConstantState();
        if (constantState != null) {
            synchronized (this.mDrawableCacheLock) {
                LongSparseArray<WeakReference<Drawable.ConstantState>> longSparseArray = this.mDrawableCaches.get(context);
                LongSparseArray<WeakReference<Drawable.ConstantState>> longSparseArray2 = longSparseArray;
                if (longSparseArray == null) {
                    longSparseArray2 = new LongSparseArray<>();
                    this.mDrawableCaches.put(context, longSparseArray2);
                }
                longSparseArray2.put(j, new WeakReference<>(constantState));
            }
            return true;
        }
        return false;
    }

    private void addTintListToCache(@NonNull Context context, @DrawableRes int i, @NonNull ColorStateList colorStateList) {
        if (this.mTintLists == null) {
            this.mTintLists = new WeakHashMap<>();
        }
        SparseArray<ColorStateList> sparseArray = this.mTintLists.get(context);
        SparseArray<ColorStateList> sparseArray2 = sparseArray;
        if (sparseArray == null) {
            sparseArray2 = new SparseArray<>();
            this.mTintLists.put(context, sparseArray2);
        }
        sparseArray2.append(i, colorStateList);
    }

    private static boolean arrayContains(int[] iArr, int i) {
        for (int i2 : iArr) {
            if (i2 == i) {
                return true;
            }
        }
        return false;
    }

    private void checkVectorDrawableSetup(@NonNull Context context) {
        if (this.mHasCheckedVectorDrawableSetup) {
            return;
        }
        this.mHasCheckedVectorDrawableSetup = true;
        Drawable drawable = getDrawable(context, R$drawable.abc_ic_ab_back_material);
        if (drawable == null || !isVectorDrawable(drawable)) {
            this.mHasCheckedVectorDrawableSetup = false;
            throw new IllegalStateException("This app has been built with an incorrect configuration. Please configure your build for VectorDrawableCompat.");
        }
    }

    private ColorStateList createBorderlessButtonColorStateList(Context context) {
        return createButtonColorStateList(context, 0);
    }

    /* JADX WARN: Type inference failed for: r0v1, types: [int[], int[][]] */
    private ColorStateList createButtonColorStateList(Context context, @ColorInt int i) {
        ?? r0 = new int[4];
        int[] iArr = new int[4];
        int themeAttrColor = ThemeUtils.getThemeAttrColor(context, R$attr.colorControlHighlight);
        r0[0] = ThemeUtils.DISABLED_STATE_SET;
        iArr[0] = ThemeUtils.getDisabledThemeAttrColor(context, R$attr.colorButtonNormal);
        r0[1] = ThemeUtils.PRESSED_STATE_SET;
        iArr[1] = ColorUtils.compositeColors(themeAttrColor, i);
        int i2 = 1 + 1;
        r0[i2] = ThemeUtils.FOCUSED_STATE_SET;
        iArr[i2] = ColorUtils.compositeColors(themeAttrColor, i);
        int i3 = i2 + 1;
        r0[i3] = ThemeUtils.EMPTY_STATE_SET;
        iArr[i3] = i;
        return new ColorStateList(r0, iArr);
    }

    private static long createCacheKey(TypedValue typedValue) {
        return (typedValue.assetCookie << 32) | typedValue.data;
    }

    private ColorStateList createColoredButtonColorStateList(Context context) {
        return createButtonColorStateList(context, ThemeUtils.getThemeAttrColor(context, R$attr.colorAccent));
    }

    private ColorStateList createDefaultButtonColorStateList(Context context) {
        return createButtonColorStateList(context, ThemeUtils.getThemeAttrColor(context, R$attr.colorButtonNormal));
    }

    private Drawable createDrawableIfNeeded(@NonNull Context context, @DrawableRes int i) {
        if (this.mTypedValue == null) {
            this.mTypedValue = new TypedValue();
        }
        TypedValue typedValue = this.mTypedValue;
        context.getResources().getValue(i, typedValue, true);
        long createCacheKey = createCacheKey(typedValue);
        LayerDrawable cachedDrawable = getCachedDrawable(context, createCacheKey);
        if (cachedDrawable != null) {
            return cachedDrawable;
        }
        if (i == R$drawable.abc_cab_background_top_material) {
            cachedDrawable = new LayerDrawable(new Drawable[]{getDrawable(context, R$drawable.abc_cab_background_internal_bg), getDrawable(context, R$drawable.abc_cab_background_top_mtrl_alpha)});
        }
        if (cachedDrawable != null) {
            cachedDrawable.setChangingConfigurations(typedValue.changingConfigurations);
            addDrawableToCache(context, createCacheKey, cachedDrawable);
        }
        return cachedDrawable;
    }

    private static PorterDuffColorFilter createTintFilter(ColorStateList colorStateList, PorterDuff.Mode mode, int[] iArr) {
        if (colorStateList == null || mode == null) {
            return null;
        }
        return getPorterDuffColorFilter(colorStateList.getColorForState(iArr, 0), mode);
    }

    public static AppCompatDrawableManager get() {
        if (INSTANCE == null) {
            INSTANCE = new AppCompatDrawableManager();
            installDefaultInflateDelegates(INSTANCE);
        }
        return INSTANCE;
    }

    private Drawable getCachedDrawable(@NonNull Context context, long j) {
        synchronized (this.mDrawableCacheLock) {
            LongSparseArray<WeakReference<Drawable.ConstantState>> longSparseArray = this.mDrawableCaches.get(context);
            if (longSparseArray == null) {
                return null;
            }
            WeakReference<Drawable.ConstantState> weakReference = longSparseArray.get(j);
            if (weakReference != null) {
                Drawable.ConstantState constantState = weakReference.get();
                if (constantState != null) {
                    return constantState.newDrawable(context.getResources());
                }
                longSparseArray.delete(j);
            }
            return null;
        }
    }

    public static PorterDuffColorFilter getPorterDuffColorFilter(int i, PorterDuff.Mode mode) {
        PorterDuffColorFilter porterDuffColorFilter = COLOR_FILTER_CACHE.get(i, mode);
        PorterDuffColorFilter porterDuffColorFilter2 = porterDuffColorFilter;
        if (porterDuffColorFilter == null) {
            porterDuffColorFilter2 = new PorterDuffColorFilter(i, mode);
            COLOR_FILTER_CACHE.put(i, mode, porterDuffColorFilter2);
        }
        return porterDuffColorFilter2;
    }

    private ColorStateList getTintListFromCache(@NonNull Context context, @DrawableRes int i) {
        if (this.mTintLists != null) {
            SparseArray<ColorStateList> sparseArray = this.mTintLists.get(context);
            ColorStateList colorStateList = null;
            if (sparseArray != null) {
                colorStateList = sparseArray.get(i);
            }
            return colorStateList;
        }
        return null;
    }

    private static void installDefaultInflateDelegates(@NonNull AppCompatDrawableManager appCompatDrawableManager) {
        int i = Build.VERSION.SDK_INT;
        if (i < 23) {
            appCompatDrawableManager.addDelegate("vector", new VdcInflateDelegate(null));
            if (i >= 11) {
                appCompatDrawableManager.addDelegate("animated-vector", new AvdcInflateDelegate(null));
            }
        }
    }

    private static boolean isVectorDrawable(@NonNull Drawable drawable) {
        return !(drawable instanceof VectorDrawableCompat) ? "android.graphics.drawable.VectorDrawable".equals(drawable.getClass().getName()) : true;
    }

    private Drawable loadDrawableFromDelegates(@NonNull Context context, @DrawableRes int i) {
        int next;
        if (this.mDelegates == null || this.mDelegates.isEmpty()) {
            return null;
        }
        if (this.mKnownDrawableIdTags != null) {
            String str = this.mKnownDrawableIdTags.get(i);
            if ("appcompat_skip_skip".equals(str)) {
                return null;
            }
            if (str != null && this.mDelegates.get(str) == null) {
                return null;
            }
        } else {
            this.mKnownDrawableIdTags = new SparseArray<>();
        }
        if (this.mTypedValue == null) {
            this.mTypedValue = new TypedValue();
        }
        TypedValue typedValue = this.mTypedValue;
        Resources resources = context.getResources();
        resources.getValue(i, typedValue, true);
        long createCacheKey = createCacheKey(typedValue);
        Drawable cachedDrawable = getCachedDrawable(context, createCacheKey);
        if (cachedDrawable != null) {
            return cachedDrawable;
        }
        Drawable drawable = cachedDrawable;
        if (typedValue.string != null) {
            drawable = cachedDrawable;
            if (typedValue.string.toString().endsWith(".xml")) {
                drawable = cachedDrawable;
                try {
                    XmlResourceParser xml = resources.getXml(i);
                    AttributeSet asAttributeSet = Xml.asAttributeSet(xml);
                    do {
                        next = xml.next();
                        if (next == 2) {
                            break;
                        }
                    } while (next != 1);
                    if (next != 2) {
                        throw new XmlPullParserException("No start tag found");
                    }
                    String name = xml.getName();
                    this.mKnownDrawableIdTags.append(i, name);
                    InflateDelegate inflateDelegate = this.mDelegates.get(name);
                    Drawable drawable2 = cachedDrawable;
                    if (inflateDelegate != null) {
                        drawable2 = inflateDelegate.createFromXmlInner(context, xml, asAttributeSet, context.getTheme());
                    }
                    drawable = drawable2;
                    if (drawable2 != null) {
                        drawable2.setChangingConfigurations(typedValue.changingConfigurations);
                        Drawable drawable3 = drawable2;
                        drawable = drawable2;
                        if (addDrawableToCache(context, createCacheKey, drawable2)) {
                            drawable = drawable2;
                        }
                    }
                } catch (Exception e) {
                    Log.e("AppCompatDrawableManager", "Exception while inflating drawable", e);
                }
            }
        }
        if (drawable == null) {
            this.mKnownDrawableIdTags.append(i, "appcompat_skip_skip");
        }
        return drawable;
    }

    private static void setPorterDuffColorFilter(Drawable drawable, int i, PorterDuff.Mode mode) {
        Drawable drawable2 = drawable;
        if (DrawableUtils.canSafelyMutateDrawable(drawable)) {
            drawable2 = drawable.mutate();
        }
        PorterDuff.Mode mode2 = mode;
        if (mode == null) {
            mode2 = DEFAULT_MODE;
        }
        drawable2.setColorFilter(getPorterDuffColorFilter(i, mode2));
    }

    private Drawable tintDrawable(@NonNull Context context, @DrawableRes int i, boolean z, @NonNull Drawable drawable) {
        Drawable drawable2;
        ColorStateList tintList = getTintList(context, i);
        if (tintList != null) {
            Drawable drawable3 = drawable;
            if (DrawableUtils.canSafelyMutateDrawable(drawable)) {
                drawable3 = drawable.mutate();
            }
            Drawable wrap = DrawableCompat.wrap(drawable3);
            DrawableCompat.setTintList(wrap, tintList);
            PorterDuff.Mode tintMode = getTintMode(i);
            drawable2 = wrap;
            if (tintMode != null) {
                DrawableCompat.setTintMode(wrap, tintMode);
                drawable2 = wrap;
            }
        } else if (i == R$drawable.abc_seekbar_track_material) {
            LayerDrawable layerDrawable = (LayerDrawable) drawable;
            setPorterDuffColorFilter(layerDrawable.findDrawableByLayerId(16908288), ThemeUtils.getThemeAttrColor(context, R$attr.colorControlNormal), DEFAULT_MODE);
            setPorterDuffColorFilter(layerDrawable.findDrawableByLayerId(16908303), ThemeUtils.getThemeAttrColor(context, R$attr.colorControlNormal), DEFAULT_MODE);
            setPorterDuffColorFilter(layerDrawable.findDrawableByLayerId(16908301), ThemeUtils.getThemeAttrColor(context, R$attr.colorControlActivated), DEFAULT_MODE);
            drawable2 = drawable;
        } else if (i == R$drawable.abc_ratingbar_material || i == R$drawable.abc_ratingbar_indicator_material || i == R$drawable.abc_ratingbar_small_material) {
            LayerDrawable layerDrawable2 = (LayerDrawable) drawable;
            setPorterDuffColorFilter(layerDrawable2.findDrawableByLayerId(16908288), ThemeUtils.getDisabledThemeAttrColor(context, R$attr.colorControlNormal), DEFAULT_MODE);
            setPorterDuffColorFilter(layerDrawable2.findDrawableByLayerId(16908303), ThemeUtils.getThemeAttrColor(context, R$attr.colorControlActivated), DEFAULT_MODE);
            setPorterDuffColorFilter(layerDrawable2.findDrawableByLayerId(16908301), ThemeUtils.getThemeAttrColor(context, R$attr.colorControlActivated), DEFAULT_MODE);
            drawable2 = drawable;
        } else {
            drawable2 = drawable;
            if (!tintDrawableUsingColorFilter(context, i, drawable)) {
                drawable2 = drawable;
                if (z) {
                    drawable2 = null;
                }
            }
        }
        return drawable2;
    }

    public static void tintDrawable(Drawable drawable, TintInfo tintInfo, int[] iArr) {
        if (DrawableUtils.canSafelyMutateDrawable(drawable) && drawable.mutate() != drawable) {
            Log.d("AppCompatDrawableManager", "Mutated drawable is not the same instance as the input.");
            return;
        }
        if (tintInfo.mHasTintList || tintInfo.mHasTintMode) {
            drawable.setColorFilter(createTintFilter(tintInfo.mHasTintList ? tintInfo.mTintList : null, tintInfo.mHasTintMode ? tintInfo.mTintMode : DEFAULT_MODE, iArr));
        } else {
            drawable.clearColorFilter();
        }
        if (Build.VERSION.SDK_INT <= 23) {
            drawable.invalidateSelf();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static boolean tintDrawableUsingColorFilter(@NonNull Context context, @DrawableRes int i, @NonNull Drawable drawable) {
        int i2;
        PorterDuff.Mode mode;
        PorterDuff.Mode mode2 = DEFAULT_MODE;
        boolean z = false;
        int i3 = 0;
        if (arrayContains(COLORFILTER_TINT_COLOR_CONTROL_NORMAL, i)) {
            i3 = R$attr.colorControlNormal;
            z = true;
            mode = mode2;
            i2 = -1;
        } else if (arrayContains(COLORFILTER_COLOR_CONTROL_ACTIVATED, i)) {
            i3 = R$attr.colorControlActivated;
            z = true;
            i2 = -1;
            mode = mode2;
        } else if (arrayContains(COLORFILTER_COLOR_BACKGROUND_MULTIPLY, i)) {
            i3 = 16842801;
            z = true;
            mode = PorterDuff.Mode.MULTIPLY;
            i2 = -1;
        } else if (i == R$drawable.abc_list_divider_mtrl_alpha) {
            i3 = 16842800;
            z = true;
            i2 = Math.round(40.8f);
            mode = mode2;
        } else {
            i2 = -1;
            mode = mode2;
            if (i == R$drawable.abc_dialog_material_background) {
                i3 = 16842801;
                z = true;
                i2 = -1;
                mode = mode2;
            }
        }
        if (z) {
            Drawable drawable2 = drawable;
            if (DrawableUtils.canSafelyMutateDrawable(drawable)) {
                drawable2 = drawable.mutate();
            }
            drawable2.setColorFilter(getPorterDuffColorFilter(ThemeUtils.getThemeAttrColor(context, i3), mode));
            if (i2 != -1) {
                drawable2.setAlpha(i2);
                return true;
            }
            return true;
        }
        return false;
    }

    public Drawable getDrawable(@NonNull Context context, @DrawableRes int i) {
        return getDrawable(context, i, false);
    }

    public Drawable getDrawable(@NonNull Context context, @DrawableRes int i, boolean z) {
        checkVectorDrawableSetup(context);
        Drawable loadDrawableFromDelegates = loadDrawableFromDelegates(context, i);
        Drawable drawable = loadDrawableFromDelegates;
        if (loadDrawableFromDelegates == null) {
            drawable = createDrawableIfNeeded(context, i);
        }
        Drawable drawable2 = drawable;
        if (drawable == null) {
            drawable2 = ContextCompat.getDrawable(context, i);
        }
        Drawable drawable3 = drawable2;
        if (drawable2 != null) {
            drawable3 = tintDrawable(context, i, z, drawable2);
        }
        if (drawable3 != null) {
            DrawableUtils.fixDrawable(drawable3);
        }
        return drawable3;
    }

    public final ColorStateList getTintList(@NonNull Context context, @DrawableRes int i) {
        ColorStateList tintListFromCache = getTintListFromCache(context, i);
        ColorStateList colorStateList = tintListFromCache;
        if (tintListFromCache == null) {
            if (i == R$drawable.abc_edit_text_material) {
                tintListFromCache = AppCompatResources.getColorStateList(context, R$color.abc_tint_edittext);
            } else if (i == R$drawable.abc_switch_track_mtrl_alpha) {
                tintListFromCache = AppCompatResources.getColorStateList(context, R$color.abc_tint_switch_track);
            } else if (i == R$drawable.abc_switch_thumb_material) {
                tintListFromCache = AppCompatResources.getColorStateList(context, R$color.abc_tint_switch_thumb);
            } else if (i == R$drawable.abc_btn_default_mtrl_shape) {
                tintListFromCache = createDefaultButtonColorStateList(context);
            } else if (i == R$drawable.abc_btn_borderless_material) {
                tintListFromCache = createBorderlessButtonColorStateList(context);
            } else if (i == R$drawable.abc_btn_colored_material) {
                tintListFromCache = createColoredButtonColorStateList(context);
            } else if (i == R$drawable.abc_spinner_mtrl_am_alpha || i == R$drawable.abc_spinner_textfield_background_material) {
                tintListFromCache = AppCompatResources.getColorStateList(context, R$color.abc_tint_spinner);
            } else if (arrayContains(TINT_COLOR_CONTROL_NORMAL, i)) {
                tintListFromCache = ThemeUtils.getThemeAttrColorStateList(context, R$attr.colorControlNormal);
            } else if (arrayContains(TINT_COLOR_CONTROL_STATE_LIST, i)) {
                tintListFromCache = AppCompatResources.getColorStateList(context, R$color.abc_tint_default);
            } else if (arrayContains(TINT_CHECKABLE_BUTTON_LIST, i)) {
                tintListFromCache = AppCompatResources.getColorStateList(context, R$color.abc_tint_btn_checkable);
            } else if (i == R$drawable.abc_seekbar_thumb_material) {
                tintListFromCache = AppCompatResources.getColorStateList(context, R$color.abc_tint_seek_thumb);
            }
            colorStateList = tintListFromCache;
            if (tintListFromCache != null) {
                addTintListToCache(context, i, tintListFromCache);
                colorStateList = tintListFromCache;
            }
        }
        return colorStateList;
    }

    final PorterDuff.Mode getTintMode(int i) {
        PorterDuff.Mode mode = null;
        if (i == R$drawable.abc_switch_thumb_material) {
            mode = PorterDuff.Mode.MULTIPLY;
        }
        return mode;
    }

    public final Drawable onDrawableLoadedFromResources(@NonNull Context context, @NonNull VectorEnabledTintResources vectorEnabledTintResources, @DrawableRes int i) {
        Drawable loadDrawableFromDelegates = loadDrawableFromDelegates(context, i);
        Drawable drawable = loadDrawableFromDelegates;
        if (loadDrawableFromDelegates == null) {
            drawable = vectorEnabledTintResources.superGetDrawable(i);
        }
        if (drawable != null) {
            return tintDrawable(context, i, false, drawable);
        }
        return null;
    }
}

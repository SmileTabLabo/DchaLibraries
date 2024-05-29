package com.android.launcher3;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.SearchManager;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Process;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TtsSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;
import com.android.launcher3.compat.UserHandleCompat;
import com.android.launcher3.config.FeatureFlags;
import com.android.launcher3.util.IconNormalizer;
import com.mediatek.launcher3.LauncherLog;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
/* loaded from: a.zip:com/android/launcher3/Utilities.class */
public final class Utilities {
    public static final boolean ATLEAST_JB_MR1;
    public static final boolean ATLEAST_JB_MR2;
    public static final boolean ATLEAST_KITKAT;
    public static final boolean ATLEAST_LOLLIPOP;
    public static final boolean ATLEAST_LOLLIPOP_MR1;
    public static final boolean ATLEAST_MARSHMALLOW;
    public static final boolean ATLEAST_N;
    private static final int CORE_POOL_SIZE;
    private static final int CPU_COUNT;
    private static final int MAXIMUM_POOL_SIZE;
    public static final Executor THREAD_POOL_EXECUTOR;
    static int sColorIndex;
    static int[] sColors;
    private static final int[] sLoc0;
    private static final int[] sLoc1;
    private static final Rect sOldBounds = new Rect();
    private static final Canvas sCanvas = new Canvas();
    private static final Pattern sTrimPattern = Pattern.compile("^[\\s|\\p{javaSpaceChar}]*(.*)[\\s|\\p{javaSpaceChar}]*$");

    /* loaded from: a.zip:com/android/launcher3/Utilities$FixedSizeBitmapDrawable.class */
    private static class FixedSizeBitmapDrawable extends BitmapDrawable {
        public FixedSizeBitmapDrawable(Bitmap bitmap) {
            super((Resources) null, bitmap);
        }

        @Override // android.graphics.drawable.BitmapDrawable, android.graphics.drawable.Drawable
        public int getIntrinsicHeight() {
            return getBitmap().getWidth();
        }

        @Override // android.graphics.drawable.BitmapDrawable, android.graphics.drawable.Drawable
        public int getIntrinsicWidth() {
            return getBitmap().getWidth();
        }
    }

    static {
        sCanvas.setDrawFilter(new PaintFlagsDrawFilter(4, 2));
        sColors = new int[]{-65536, -16711936, -16776961};
        sColorIndex = 0;
        sLoc0 = new int[2];
        sLoc1 = new int[2];
        ATLEAST_N = Build.VERSION.SDK_INT >= 24;
        ATLEAST_MARSHMALLOW = Build.VERSION.SDK_INT >= 23;
        ATLEAST_LOLLIPOP_MR1 = Build.VERSION.SDK_INT >= 22;
        ATLEAST_LOLLIPOP = Build.VERSION.SDK_INT >= 21;
        ATLEAST_KITKAT = Build.VERSION.SDK_INT >= 19;
        ATLEAST_JB_MR1 = Build.VERSION.SDK_INT >= 17;
        ATLEAST_JB_MR2 = Build.VERSION.SDK_INT >= 18;
        CPU_COUNT = Runtime.getRuntime().availableProcessors();
        CORE_POOL_SIZE = CPU_COUNT + 1;
        MAXIMUM_POOL_SIZE = (CPU_COUNT * 2) + 1;
        THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, 1L, TimeUnit.SECONDS, new LinkedBlockingQueue());
    }

    public static void assertWorkerThread() {
        if (LauncherAppState.isDogfoodBuild() && LauncherModel.sWorkerThread.getThreadId() != Process.myTid()) {
            throw new IllegalStateException();
        }
    }

    @TargetApi(21)
    public static Bitmap createBadgedIconBitmap(Drawable drawable, UserHandleCompat userHandleCompat, Context context) {
        Bitmap createIconBitmap = createIconBitmap(drawable, context, FeatureFlags.LAUNCHER3_ICON_NORMALIZATION ? IconNormalizer.getInstance().getScale(drawable) : 1.0f);
        if (!ATLEAST_LOLLIPOP || userHandleCompat == null || UserHandleCompat.myUserHandle().equals(userHandleCompat)) {
            return createIconBitmap;
        }
        Drawable userBadgedIcon = context.getPackageManager().getUserBadgedIcon(new FixedSizeBitmapDrawable(createIconBitmap), userHandleCompat.getUser());
        return userBadgedIcon instanceof BitmapDrawable ? ((BitmapDrawable) userBadgedIcon).getBitmap() : createIconBitmap(userBadgedIcon, context);
    }

    public static String createDbSelectionQuery(String str, Iterable<?> iterable) {
        return String.format(Locale.ENGLISH, "%s IN (%s)", str, TextUtils.join(", ", iterable));
    }

    public static Bitmap createIconBitmap(Cursor cursor, int i, Context context) {
        byte[] blob = cursor.getBlob(i);
        try {
            return createIconBitmap(BitmapFactory.decodeByteArray(blob, 0, blob.length), context);
        } catch (Exception e) {
            return null;
        }
    }

    public static Bitmap createIconBitmap(Bitmap bitmap, Context context) {
        int iconBitmapSize = getIconBitmapSize();
        return (iconBitmapSize == bitmap.getWidth() && iconBitmapSize == bitmap.getHeight()) ? bitmap : createIconBitmap(new BitmapDrawable(context.getResources(), bitmap), context);
    }

    public static Bitmap createIconBitmap(Drawable drawable, Context context) {
        return createIconBitmap(drawable, context, 1.0f);
    }

    public static Bitmap createIconBitmap(Drawable drawable, Context context, float f) {
        BitmapDrawable bitmapDrawable;
        Bitmap bitmap;
        Bitmap createBitmap;
        synchronized (sCanvas) {
            int iconBitmapSize = getIconBitmapSize();
            if (drawable instanceof PaintDrawable) {
                PaintDrawable paintDrawable = (PaintDrawable) drawable;
                paintDrawable.setIntrinsicWidth(iconBitmapSize);
                paintDrawable.setIntrinsicHeight(iconBitmapSize);
            } else if ((drawable instanceof BitmapDrawable) && (bitmap = (bitmapDrawable = (BitmapDrawable) drawable).getBitmap()) != null && bitmap.getDensity() == 0) {
                bitmapDrawable.setTargetDensity(context.getResources().getDisplayMetrics());
            }
            int intrinsicWidth = drawable.getIntrinsicWidth();
            int intrinsicHeight = drawable.getIntrinsicHeight();
            int i = iconBitmapSize;
            int i2 = iconBitmapSize;
            if (intrinsicWidth > 0) {
                i = iconBitmapSize;
                i2 = iconBitmapSize;
                if (intrinsicHeight > 0) {
                    float f2 = intrinsicWidth / intrinsicHeight;
                    if (intrinsicWidth > intrinsicHeight) {
                        i = (int) (iconBitmapSize / f2);
                        i2 = iconBitmapSize;
                    } else {
                        i = iconBitmapSize;
                        i2 = iconBitmapSize;
                        if (intrinsicHeight > intrinsicWidth) {
                            i2 = (int) (iconBitmapSize * f2);
                            i = iconBitmapSize;
                        }
                    }
                }
            }
            createBitmap = Bitmap.createBitmap(iconBitmapSize, iconBitmapSize, Bitmap.Config.ARGB_8888);
            Canvas canvas = sCanvas;
            canvas.setBitmap(createBitmap);
            int i3 = (iconBitmapSize - i2) / 2;
            int i4 = (iconBitmapSize - i) / 2;
            sOldBounds.set(drawable.getBounds());
            drawable.setBounds(i3, i4, i3 + i2, i4 + i);
            canvas.save(1);
            canvas.scale(f, f, iconBitmapSize / 2, iconBitmapSize / 2);
            drawable.draw(canvas);
            canvas.restore();
            drawable.setBounds(sOldBounds);
            canvas.setBitmap(null);
        }
        return createBitmap;
    }

    public static Bitmap createIconBitmap(String str, String str2, Context context) {
        try {
            Resources resourcesForApplication = context.getPackageManager().getResourcesForApplication(str);
            if (resourcesForApplication != null) {
                return createIconBitmap(resourcesForApplication.getDrawableForDensity(resourcesForApplication.getIdentifier(str2, null, null), LauncherAppState.getInstance().getInvariantDeviceProfile().fillResIconDpi), context);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public static float dpiFromPx(int i, DisplayMetrics displayMetrics) {
        return i / (displayMetrics.densityDpi / 160.0f);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int findDominantColorByHue(Bitmap bitmap, int i) {
        int i2;
        float f;
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        int sqrt = (int) Math.sqrt((height * width) / i);
        int i3 = sqrt;
        if (sqrt < 1) {
            i3 = 1;
        }
        float[] fArr = new float[3];
        float[] fArr2 = new float[360];
        float f2 = -1.0f;
        int i4 = -1;
        int i5 = 0;
        while (true) {
            int i6 = i5;
            if (i6 >= height) {
                break;
            }
            int i7 = 0;
            while (i7 < width) {
                int pixel = bitmap.getPixel(i7, i6);
                if (((pixel >> 24) & 255) < 128) {
                    f = f2;
                    i2 = i4;
                } else {
                    Color.colorToHSV(pixel | (-16777216), fArr);
                    int i8 = (int) fArr[0];
                    i2 = i4;
                    f = f2;
                    if (i8 >= 0) {
                        i2 = i4;
                        f = f2;
                        if (i8 < fArr2.length) {
                            fArr2[i8] = fArr2[i8] + (fArr[1] * fArr[2]);
                            i2 = i4;
                            f = f2;
                            if (fArr2[i8] > f2) {
                                f = fArr2[i8];
                                i2 = i8;
                            }
                        }
                    }
                }
                i7 += i3;
                i4 = i2;
                f2 = f;
            }
            i5 = i6 + i3;
        }
        SparseArray sparseArray = new SparseArray();
        int i9 = -16777216;
        float f3 = -1.0f;
        int i10 = 0;
        while (true) {
            int i11 = i10;
            if (i11 >= height) {
                return i9;
            }
            int i12 = 0;
            while (i12 < width) {
                int pixel2 = bitmap.getPixel(i12, i11) | (-16777216);
                Color.colorToHSV(pixel2, fArr);
                int i13 = i9;
                float f4 = f3;
                if (((int) fArr[0]) == i4) {
                    float f5 = fArr[1];
                    float f6 = fArr[2];
                    int i14 = ((int) (100.0f * f5)) + ((int) (10000.0f * f6));
                    float f7 = f5 * f6;
                    Float f8 = (Float) sparseArray.get(i14);
                    if (f8 != null) {
                        f7 = f8.floatValue() + f7;
                    }
                    sparseArray.put(i14, Float.valueOf(f7));
                    i13 = i9;
                    f4 = f3;
                    if (f7 > f3) {
                        i13 = pixel2;
                        f4 = f7;
                    }
                }
                i12 += i3;
                i9 = i13;
                f3 = f4;
            }
            i10 = i11 + i3;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static Pair<String, Resources> findSystemApk(String str, PackageManager packageManager) {
        for (ResolveInfo resolveInfo : packageManager.queryBroadcastReceivers(new Intent(str), 0)) {
            if (resolveInfo.activityInfo != null && (resolveInfo.activityInfo.applicationInfo.flags & 1) != 0) {
                String str2 = resolveInfo.activityInfo.packageName;
                try {
                    return Pair.create(str2, packageManager.getResourcesForApplication(str2));
                } catch (PackageManager.NameNotFoundException e) {
                    Log.w("Launcher.Utilities", "Failed to find resources for " + str2);
                }
            }
        }
        return null;
    }

    public static boolean findVacantCell(int[] iArr, int i, int i2, int i3, int i4, boolean[][] zArr) {
        boolean z;
        for (int i5 = 0; i5 + i2 <= i4; i5++) {
            for (int i6 = 0; i6 + i <= i3; i6++) {
                boolean z2 = !zArr[i6][i5];
                int i7 = i6;
                while (true) {
                    z = z2;
                    if (i7 >= i6 + i) {
                        break;
                    }
                    for (int i8 = i5; i8 < i5 + i2; i8++) {
                        z2 = z2 && !zArr[i7][i8];
                        if (!z2) {
                            z = z2;
                            break;
                        }
                    }
                    i7++;
                }
                if (z) {
                    iArr[0] = i6;
                    iArr[1] = i5;
                    return true;
                }
            }
        }
        return false;
    }

    public static byte[] flattenBitmap(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(bitmap.getWidth() * bitmap.getHeight() * 4);
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byteArrayOutputStream.flush();
            byteArrayOutputStream.close();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            Log.w("Launcher.Utilities", "Could not write bitmap");
            return null;
        }
    }

    public static int[] getCenterDeltaInScreenSpace(View view, View view2, int[] iArr) {
        view.getLocationInWindow(sLoc0);
        view2.getLocationInWindow(sLoc1);
        int[] iArr2 = sLoc0;
        iArr2[0] = (int) (iArr2[0] + ((view.getMeasuredWidth() * view.getScaleX()) / 2.0f));
        int[] iArr3 = sLoc0;
        iArr3[1] = (int) (iArr3[1] + ((view.getMeasuredHeight() * view.getScaleY()) / 2.0f));
        int[] iArr4 = sLoc1;
        iArr4[0] = (int) (iArr4[0] + ((view2.getMeasuredWidth() * view2.getScaleX()) / 2.0f));
        int[] iArr5 = sLoc1;
        iArr5[1] = (int) (iArr5[1] + ((view2.getMeasuredHeight() * view2.getScaleY()) / 2.0f));
        int[] iArr6 = iArr;
        if (iArr == null) {
            iArr6 = new int[2];
        }
        iArr6[0] = sLoc1[0] - sLoc0[0];
        iArr6[1] = sLoc1[1] - sLoc0[1];
        return iArr6;
    }

    public static float getDescendantCoordRelativeToParent(View view, View view2, int[] iArr, boolean z) {
        ArrayList arrayList = new ArrayList();
        float[] fArr = {iArr[0], iArr[1]};
        View view3 = view;
        while (true) {
            View view4 = view3;
            if (view4 == view2 || view4 == null) {
                break;
            }
            arrayList.add(view4);
            view3 = (View) view4.getParent();
        }
        arrayList.add(view2);
        float f = 1.0f;
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            View view5 = (View) arrayList.get(i);
            if (view5 != view || z) {
                fArr[0] = fArr[0] - view5.getScrollX();
                fArr[1] = fArr[1] - view5.getScrollY();
            }
            view5.getMatrix().mapPoints(fArr);
            fArr[0] = fArr[0] + view5.getLeft();
            fArr[1] = fArr[1] + view5.getTop();
            f *= view5.getScaleX();
        }
        iArr[0] = Math.round(fArr[0]);
        iArr[1] = Math.round(fArr[1]);
        return f;
    }

    private static int getIconBitmapSize() {
        return LauncherAppState.getInstance().getInvariantDeviceProfile().iconBitmapSize;
    }

    public static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences("com.android.launcher3.prefs", 0);
    }

    @TargetApi(17)
    public static AppWidgetProviderInfo getSearchWidgetProvider(Context context) {
        ComponentName globalSearchActivity = ((SearchManager) context.getSystemService("search")).getGlobalSearchActivity();
        if (globalSearchActivity == null) {
            return null;
        }
        String packageName = globalSearchActivity.getPackageName();
        AppWidgetProviderInfo appWidgetProviderInfo = null;
        for (AppWidgetProviderInfo appWidgetProviderInfo2 : AppWidgetManager.getInstance(context).getInstalledProviders()) {
            if (appWidgetProviderInfo2.provider.getPackageName().equals(packageName)) {
                if (ATLEAST_JB_MR1 && (appWidgetProviderInfo2.widgetCategory & 4) == 0) {
                    if (appWidgetProviderInfo == null) {
                        appWidgetProviderInfo = appWidgetProviderInfo2;
                    }
                }
                return appWidgetProviderInfo2;
            }
        }
        return appWidgetProviderInfo;
    }

    public static boolean isAllowRotationPrefEnabled(Context context) {
        boolean z = false;
        if (ATLEAST_N) {
            int i = DisplayMetrics.DENSITY_DEVICE_STABLE;
            Resources resources = context.getResources();
            z = (resources.getConfiguration().smallestScreenWidthDp * resources.getDisplayMetrics().densityDpi) / i >= 600;
        }
        return getPrefs(context).getBoolean("pref_allowRotation", z);
    }

    public static boolean isLauncherAppTarget(Intent intent) {
        boolean z = false;
        if (intent != null && "android.intent.action.MAIN".equals(intent.getAction()) && intent.getComponent() != null && intent.getCategories() != null && intent.getCategories().size() == 1 && intent.hasCategory("android.intent.category.LAUNCHER") && TextUtils.isEmpty(intent.getDataString())) {
            Bundle extras = intent.getExtras();
            if (extras == null) {
                return true;
            }
            Set<String> keySet = extras.keySet();
            if (keySet.size() == 1) {
                z = keySet.contains("profile");
            }
            return z;
        }
        return false;
    }

    @TargetApi(21)
    public static boolean isPowerSaverOn(Context context) {
        return ATLEAST_LOLLIPOP ? ((PowerManager) context.getSystemService("power")).isPowerSaveMode() : false;
    }

    public static boolean isPropertyEnabled(String str) {
        return "1".equals(LauncherLog.getSysProperty(str));
    }

    @TargetApi(17)
    public static boolean isRtl(Resources resources) {
        boolean z = true;
        if (!ATLEAST_JB_MR1) {
            z = false;
        } else if (resources.getConfiguration().getLayoutDirection() != 1) {
            z = false;
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static boolean isSystemApp(Context context, Intent intent) {
        String packageName;
        PackageManager packageManager = context.getPackageManager();
        ComponentName component = intent.getComponent();
        if (component == null) {
            ResolveInfo resolveActivity = packageManager.resolveActivity(intent, 65536);
            packageName = null;
            if (resolveActivity != null) {
                packageName = null;
                if (resolveActivity.activityInfo != null) {
                    packageName = resolveActivity.activityInfo.packageName;
                }
            }
        } else {
            packageName = component.getPackageName();
        }
        if (packageName != null) {
            try {
                PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
                boolean z = false;
                if (packageInfo != null) {
                    z = false;
                    if (packageInfo.applicationInfo != null) {
                        z = false;
                        if ((packageInfo.applicationInfo.flags & 1) != 0) {
                            z = true;
                        }
                    }
                }
                return z;
            } catch (PackageManager.NameNotFoundException e) {
                return false;
            }
        }
        return false;
    }

    @TargetApi(19)
    public static boolean isViewAttachedToWindow(View view) {
        if (ATLEAST_KITKAT) {
            return view.isAttachedToWindow();
        }
        return view.getKeyDispatcherState() != null;
    }

    public static boolean isWallapaperAllowed(Context context) {
        if (ATLEAST_N) {
            return ((WallpaperManager) context.getSystemService(WallpaperManager.class)).isSetWallpaperAllowed();
        }
        return true;
    }

    public static int longCompare(long j, long j2) {
        return j < j2 ? -1 : j == j2 ? 0 : 1;
    }

    public static float mapCoordInSelfToDescendent(View view, View view2, int[] iArr) {
        ArrayList arrayList = new ArrayList();
        float[] fArr = {iArr[0], iArr[1]};
        while (view != view2) {
            arrayList.add(view);
            view = (View) view.getParent();
        }
        arrayList.add(view2);
        float f = 1.0f;
        Matrix matrix = new Matrix();
        int size = arrayList.size() - 1;
        while (size >= 0) {
            View view3 = (View) arrayList.get(size);
            View view4 = size > 0 ? (View) arrayList.get(size - 1) : null;
            fArr[0] = fArr[0] + view3.getScrollX();
            fArr[1] = fArr[1] + view3.getScrollY();
            float f2 = f;
            if (view4 != null) {
                fArr[0] = fArr[0] - view4.getLeft();
                fArr[1] = fArr[1] - view4.getTop();
                view4.getMatrix().invert(matrix);
                matrix.mapPoints(fArr);
                f2 = f * view4.getScaleX();
            }
            size--;
            f = f2;
        }
        iArr[0] = Math.round(fArr[0]);
        iArr[1] = Math.round(fArr[1]);
        return f;
    }

    public static boolean pointInView(View view, float f, float f2, float f3) {
        boolean z = false;
        if (f >= (-f3)) {
            z = false;
            if (f2 >= (-f3)) {
                z = false;
                if (f < view.getWidth() + f3) {
                    z = false;
                    if (f2 < view.getHeight() + f3) {
                        z = true;
                    }
                }
            }
        }
        return z;
    }

    public static int pxFromDp(float f, DisplayMetrics displayMetrics) {
        return Math.round(TypedValue.applyDimension(1, f, displayMetrics));
    }

    public static int pxFromSp(float f, DisplayMetrics displayMetrics) {
        return Math.round(TypedValue.applyDimension(2, f, displayMetrics));
    }

    public static void scaleRect(Rect rect, float f) {
        if (f != 1.0f) {
            rect.left = (int) ((rect.left * f) + 0.5f);
            rect.top = (int) ((rect.top * f) + 0.5f);
            rect.right = (int) ((rect.right * f) + 0.5f);
            rect.bottom = (int) ((rect.bottom * f) + 0.5f);
        }
    }

    public static void scaleRectAboutCenter(Rect rect, float f) {
        int centerX = rect.centerX();
        int centerY = rect.centerY();
        rect.offset(-centerX, -centerY);
        scaleRect(rect, f);
        rect.offset(centerX, centerY);
    }

    public static void startActivityForResultSafely(Activity activity, Intent intent, int i) {
        try {
            activity.startActivityForResult(intent, i);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, 2131558407, 0).show();
        } catch (SecurityException e2) {
            Toast.makeText(activity, 2131558407, 0).show();
            Log.e("Launcher.Utilities", "Launcher does not have the permission to launch " + intent + ". Make sure to create a MAIN intent-filter for the corresponding activity or use the exported attribute for this activity.", e2);
        }
    }

    public static String trim(CharSequence charSequence) {
        if (charSequence == null) {
            return null;
        }
        return sTrimPattern.matcher(charSequence).replaceAll("$1");
    }

    @TargetApi(21)
    public static CharSequence wrapForTts(CharSequence charSequence, String str) {
        if (ATLEAST_LOLLIPOP) {
            SpannableString spannableString = new SpannableString(charSequence);
            spannableString.setSpan(new TtsSpan.TextBuilder(str).build(), 0, spannableString.length(), 18);
            return spannableString;
        }
        return charSequence;
    }
}

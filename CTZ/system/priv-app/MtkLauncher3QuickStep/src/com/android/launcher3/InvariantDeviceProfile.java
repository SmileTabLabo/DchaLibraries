package com.android.launcher3;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Point;
import android.support.annotation.VisibleForTesting;
import android.util.DisplayMetrics;
import android.util.Xml;
import android.view.Display;
import android.view.WindowManager;
import com.android.quickstep.QuickScrubController;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import org.xmlpull.v1.XmlPullParserException;
/* loaded from: classes.dex */
public class InvariantDeviceProfile {
    private static final float ICON_SIZE_DEFINED_IN_APP_DP = 48.0f;
    int defaultLayoutId;
    public Point defaultWallpaperSize;
    int demoModeLayoutId;
    public int fillResIconDpi;
    public int iconBitmapSize;
    public float iconSize;
    public float iconTextSize;
    public float landscapeIconSize;
    public DeviceProfile landscapeProfile;
    float minHeightDps;
    float minWidthDps;
    String name;
    public int numColumns;
    public int numFolderColumns;
    public int numFolderRows;
    public int numHotseatIcons;
    public int numRows;
    public DeviceProfile portraitProfile;
    private static float DEFAULT_ICON_SIZE_DP = 60.0f;
    private static float KNEARESTNEIGHBOR = 3.0f;
    private static float WEIGHT_POWER = 5.0f;
    private static float WEIGHT_EFFICIENT = 100000.0f;

    @VisibleForTesting
    public InvariantDeviceProfile() {
    }

    private InvariantDeviceProfile(InvariantDeviceProfile invariantDeviceProfile) {
        this(invariantDeviceProfile.name, invariantDeviceProfile.minWidthDps, invariantDeviceProfile.minHeightDps, invariantDeviceProfile.numRows, invariantDeviceProfile.numColumns, invariantDeviceProfile.numFolderRows, invariantDeviceProfile.numFolderColumns, invariantDeviceProfile.iconSize, invariantDeviceProfile.landscapeIconSize, invariantDeviceProfile.iconTextSize, invariantDeviceProfile.numHotseatIcons, invariantDeviceProfile.defaultLayoutId, invariantDeviceProfile.demoModeLayoutId);
    }

    private InvariantDeviceProfile(String str, float f, float f2, int i, int i2, int i3, int i4, float f3, float f4, float f5, int i5, int i6, int i7) {
        this.name = str;
        this.minWidthDps = f;
        this.minHeightDps = f2;
        this.numRows = i;
        this.numColumns = i2;
        this.numFolderRows = i3;
        this.numFolderColumns = i4;
        this.iconSize = f3;
        this.landscapeIconSize = f4;
        this.iconTextSize = f5;
        this.numHotseatIcons = i5;
        this.defaultLayoutId = i6;
        this.demoModeLayoutId = i7;
    }

    @TargetApi(23)
    public InvariantDeviceProfile(Context context) {
        Display defaultDisplay = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        defaultDisplay.getMetrics(displayMetrics);
        Point point = new Point();
        Point point2 = new Point();
        defaultDisplay.getCurrentSizeRange(point, point2);
        this.minWidthDps = Utilities.dpiFromPx(Math.min(point.x, point.y), displayMetrics);
        this.minHeightDps = Utilities.dpiFromPx(Math.min(point2.x, point2.y), displayMetrics);
        ArrayList<InvariantDeviceProfile> findClosestDeviceProfiles = findClosestDeviceProfiles(this.minWidthDps, this.minHeightDps, getPredefinedDeviceProfiles(context));
        InvariantDeviceProfile invDistWeightedInterpolate = invDistWeightedInterpolate(this.minWidthDps, this.minHeightDps, findClosestDeviceProfiles);
        InvariantDeviceProfile invariantDeviceProfile = findClosestDeviceProfiles.get(0);
        this.numRows = invariantDeviceProfile.numRows;
        this.numColumns = invariantDeviceProfile.numColumns;
        this.numHotseatIcons = invariantDeviceProfile.numHotseatIcons;
        this.defaultLayoutId = invariantDeviceProfile.defaultLayoutId;
        this.demoModeLayoutId = invariantDeviceProfile.demoModeLayoutId;
        this.numFolderRows = invariantDeviceProfile.numFolderRows;
        this.numFolderColumns = invariantDeviceProfile.numFolderColumns;
        this.iconSize = invDistWeightedInterpolate.iconSize;
        this.landscapeIconSize = invDistWeightedInterpolate.landscapeIconSize;
        this.iconBitmapSize = Utilities.pxFromDp(this.iconSize, displayMetrics);
        this.iconTextSize = invDistWeightedInterpolate.iconTextSize;
        this.fillResIconDpi = getLauncherIconDensity(this.iconBitmapSize);
        applyPartnerDeviceProfileOverrides(context, displayMetrics);
        Point point3 = new Point();
        defaultDisplay.getRealSize(point3);
        int min = Math.min(point3.x, point3.y);
        int max = Math.max(point3.x, point3.y);
        this.landscapeProfile = new DeviceProfile(context, this, point, point2, max, min, true, false);
        this.portraitProfile = new DeviceProfile(context, this, point, point2, min, max, false, false);
        if (context.getResources().getConfiguration().smallestScreenWidthDp >= 720) {
            this.defaultWallpaperSize = new Point((int) (max * wallpaperTravelToScreenWidthRatio(max, min)), max);
        } else {
            this.defaultWallpaperSize = new Point(Math.max(min * 2, max), max);
        }
    }

    ArrayList<InvariantDeviceProfile> getPredefinedDeviceProfiles(Context context) {
        ArrayList<InvariantDeviceProfile> arrayList = new ArrayList<>();
        try {
            XmlResourceParser xml = context.getResources().getXml(R.xml.device_profiles);
            int depth = xml.getDepth();
            while (true) {
                int next = xml.next();
                if ((next != 3 || xml.getDepth() > depth) && next != 1) {
                    if (next == 2 && "profile".equals(xml.getName())) {
                        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(Xml.asAttributeSet(xml), R.styleable.InvariantDeviceProfile);
                        int i = obtainStyledAttributes.getInt(12, 0);
                        int i2 = obtainStyledAttributes.getInt(8, 0);
                        float f = obtainStyledAttributes.getFloat(2, 0.0f);
                        arrayList.add(new InvariantDeviceProfile(obtainStyledAttributes.getString(7), obtainStyledAttributes.getFloat(6, 0.0f), obtainStyledAttributes.getFloat(5, 0.0f), i, i2, obtainStyledAttributes.getInt(10, i), obtainStyledAttributes.getInt(9, i2), f, obtainStyledAttributes.getFloat(4, f), obtainStyledAttributes.getFloat(3, 0.0f), obtainStyledAttributes.getInt(11, i2), obtainStyledAttributes.getResourceId(0, 0), obtainStyledAttributes.getResourceId(1, 0)));
                        obtainStyledAttributes.recycle();
                    }
                }
            }
            if (xml != null) {
                xml.close();
            }
            return arrayList;
        } catch (IOException | XmlPullParserException e) {
            throw new RuntimeException(e);
        }
    }

    private int getLauncherIconDensity(int i) {
        int[] iArr = {120, 160, 213, QuickScrubController.QUICK_SCRUB_FROM_APP_START_DURATION, LauncherAnimUtils.ALL_APPS_TRANSITION_MS, 480, 640};
        int i2 = 640;
        for (int length = iArr.length - 1; length >= 0; length--) {
            if ((ICON_SIZE_DEFINED_IN_APP_DP * iArr[length]) / 160.0f >= i) {
                i2 = iArr[length];
            }
        }
        return i2;
    }

    private void applyPartnerDeviceProfileOverrides(Context context, DisplayMetrics displayMetrics) {
        Partner partner = Partner.get(context.getPackageManager());
        if (partner != null) {
            partner.applyInvariantDeviceProfileOverrides(this, displayMetrics);
        }
    }

    float dist(float f, float f2, float f3, float f4) {
        return (float) Math.hypot(f3 - f, f4 - f2);
    }

    ArrayList<InvariantDeviceProfile> findClosestDeviceProfiles(final float f, final float f2, ArrayList<InvariantDeviceProfile> arrayList) {
        Collections.sort(arrayList, new Comparator<InvariantDeviceProfile>() { // from class: com.android.launcher3.InvariantDeviceProfile.1
            @Override // java.util.Comparator
            public int compare(InvariantDeviceProfile invariantDeviceProfile, InvariantDeviceProfile invariantDeviceProfile2) {
                return Float.compare(InvariantDeviceProfile.this.dist(f, f2, invariantDeviceProfile.minWidthDps, invariantDeviceProfile.minHeightDps), InvariantDeviceProfile.this.dist(f, f2, invariantDeviceProfile2.minWidthDps, invariantDeviceProfile2.minHeightDps));
            }
        });
        return arrayList;
    }

    InvariantDeviceProfile invDistWeightedInterpolate(float f, float f2, ArrayList<InvariantDeviceProfile> arrayList) {
        InvariantDeviceProfile invariantDeviceProfile = arrayList.get(0);
        float f3 = 0.0f;
        if (dist(f, f2, invariantDeviceProfile.minWidthDps, invariantDeviceProfile.minHeightDps) == 0.0f) {
            return invariantDeviceProfile;
        }
        InvariantDeviceProfile invariantDeviceProfile2 = new InvariantDeviceProfile();
        for (int i = 0; i < arrayList.size() && i < KNEARESTNEIGHBOR; i++) {
            InvariantDeviceProfile invariantDeviceProfile3 = new InvariantDeviceProfile(arrayList.get(i));
            float weight = weight(f, f2, invariantDeviceProfile3.minWidthDps, invariantDeviceProfile3.minHeightDps, WEIGHT_POWER);
            f3 += weight;
            invariantDeviceProfile2.add(invariantDeviceProfile3.multiply(weight));
        }
        return invariantDeviceProfile2.multiply(1.0f / f3);
    }

    private void add(InvariantDeviceProfile invariantDeviceProfile) {
        this.iconSize += invariantDeviceProfile.iconSize;
        this.landscapeIconSize += invariantDeviceProfile.landscapeIconSize;
        this.iconTextSize += invariantDeviceProfile.iconTextSize;
    }

    private InvariantDeviceProfile multiply(float f) {
        this.iconSize *= f;
        this.landscapeIconSize *= f;
        this.iconTextSize *= f;
        return this;
    }

    public int getAllAppsButtonRank() {
        return this.numHotseatIcons / 2;
    }

    public boolean isAllAppsButtonRank(int i) {
        return i == getAllAppsButtonRank();
    }

    public DeviceProfile getDeviceProfile(Context context) {
        return context.getResources().getConfiguration().orientation == 2 ? this.landscapeProfile : this.portraitProfile;
    }

    private float weight(float f, float f2, float f3, float f4, float f5) {
        float dist = dist(f, f2, f3, f4);
        if (Float.compare(dist, 0.0f) == 0) {
            return Float.POSITIVE_INFINITY;
        }
        return (float) (WEIGHT_EFFICIENT / Math.pow(dist, f5));
    }

    private static float wallpaperTravelToScreenWidthRatio(int i, int i2) {
        return (0.30769226f * (i / i2)) + 1.0076923f;
    }
}

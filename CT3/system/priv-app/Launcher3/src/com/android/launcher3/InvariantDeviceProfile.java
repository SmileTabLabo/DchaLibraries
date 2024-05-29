package com.android.launcher3;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
/* loaded from: a.zip:com/android/launcher3/InvariantDeviceProfile.class */
public class InvariantDeviceProfile {
    int defaultLayoutId;
    public int fillResIconDpi;
    public int hotseatAllAppsRank;
    float hotseatIconSize;
    public int iconBitmapSize;
    public float iconSize;
    public float iconTextSize;
    DeviceProfile landscapeProfile;
    int minAllAppsPredictionColumns;
    float minHeightDps;
    float minWidthDps;
    String name;
    public int numColumns;
    public int numFolderColumns;
    public int numFolderRows;
    public int numHotseatIcons;
    public int numRows;
    DeviceProfile portraitProfile;
    private static float DEFAULT_ICON_SIZE_DP = 60.0f;
    private static float KNEARESTNEIGHBOR = 3.0f;
    private static float WEIGHT_POWER = 5.0f;
    private static float WEIGHT_EFFICIENT = 100000.0f;

    public InvariantDeviceProfile() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
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
        ArrayList<InvariantDeviceProfile> findClosestDeviceProfiles = findClosestDeviceProfiles(this.minWidthDps, this.minHeightDps, getPredefinedDeviceProfiles());
        InvariantDeviceProfile invDistWeightedInterpolate = invDistWeightedInterpolate(this.minWidthDps, this.minHeightDps, findClosestDeviceProfiles);
        InvariantDeviceProfile invariantDeviceProfile = findClosestDeviceProfiles.get(0);
        this.numRows = invariantDeviceProfile.numRows;
        this.numColumns = invariantDeviceProfile.numColumns;
        this.numHotseatIcons = invariantDeviceProfile.numHotseatIcons;
        this.hotseatAllAppsRank = this.numHotseatIcons / 2;
        this.defaultLayoutId = invariantDeviceProfile.defaultLayoutId;
        this.numFolderRows = invariantDeviceProfile.numFolderRows;
        this.numFolderColumns = invariantDeviceProfile.numFolderColumns;
        this.minAllAppsPredictionColumns = invariantDeviceProfile.minAllAppsPredictionColumns;
        this.iconSize = invDistWeightedInterpolate.iconSize;
        this.iconBitmapSize = Utilities.pxFromDp(this.iconSize, displayMetrics);
        this.iconTextSize = invDistWeightedInterpolate.iconTextSize;
        this.hotseatIconSize = invDistWeightedInterpolate.hotseatIconSize;
        this.fillResIconDpi = getLauncherIconDensity(this.iconBitmapSize);
        applyPartnerDeviceProfileOverrides(context, displayMetrics);
        Point point3 = new Point();
        defaultDisplay.getRealSize(point3);
        int min = Math.min(point3.x, point3.y);
        int max = Math.max(point3.x, point3.y);
        this.landscapeProfile = new DeviceProfile(context, this, point, point2, max, min, true);
        this.portraitProfile = new DeviceProfile(context, this, point, point2, min, max, false);
    }

    public InvariantDeviceProfile(InvariantDeviceProfile invariantDeviceProfile) {
        this(invariantDeviceProfile.name, invariantDeviceProfile.minWidthDps, invariantDeviceProfile.minHeightDps, invariantDeviceProfile.numRows, invariantDeviceProfile.numColumns, invariantDeviceProfile.numFolderRows, invariantDeviceProfile.numFolderColumns, invariantDeviceProfile.minAllAppsPredictionColumns, invariantDeviceProfile.iconSize, invariantDeviceProfile.iconTextSize, invariantDeviceProfile.numHotseatIcons, invariantDeviceProfile.hotseatIconSize, invariantDeviceProfile.defaultLayoutId);
    }

    InvariantDeviceProfile(String str, float f, float f2, int i, int i2, int i3, int i4, int i5, float f3, float f4, int i6, float f5, int i7) {
        if (i6 % 2 == 0) {
            throw new RuntimeException("All Device Profiles must have an odd number of hotseat spaces");
        }
        this.name = str;
        this.minWidthDps = f;
        this.minHeightDps = f2;
        this.numRows = i;
        this.numColumns = i2;
        this.numFolderRows = i3;
        this.numFolderColumns = i4;
        this.minAllAppsPredictionColumns = i5;
        this.iconSize = f3;
        this.iconTextSize = f4;
        this.numHotseatIcons = i6;
        this.hotseatIconSize = f5;
        this.defaultLayoutId = i7;
    }

    private void add(InvariantDeviceProfile invariantDeviceProfile) {
        this.iconSize += invariantDeviceProfile.iconSize;
        this.iconTextSize += invariantDeviceProfile.iconTextSize;
        this.hotseatIconSize += invariantDeviceProfile.hotseatIconSize;
    }

    private void applyPartnerDeviceProfileOverrides(Context context, DisplayMetrics displayMetrics) {
        Partner partner = Partner.get(context.getPackageManager());
        if (partner != null) {
            partner.applyInvariantDeviceProfileOverrides(this, displayMetrics);
        }
    }

    private int getLauncherIconDensity(int i) {
        int[] iArr = {120, 160, 213, 240, 320, 480, 640};
        int i2 = 640;
        for (int length = iArr.length - 1; length >= 0; length--) {
            if ((iArr[length] * 48.0f) / 160.0f >= i) {
                i2 = iArr[length];
            }
        }
        return i2;
    }

    private InvariantDeviceProfile multiply(float f) {
        this.iconSize *= f;
        this.iconTextSize *= f;
        this.hotseatIconSize *= f;
        return this;
    }

    private float weight(float f, float f2, float f3, float f4, float f5) {
        float dist = dist(f, f2, f3, f4);
        if (Float.compare(dist, 0.0f) == 0) {
            return Float.POSITIVE_INFINITY;
        }
        return (float) (WEIGHT_EFFICIENT / Math.pow(dist, f5));
    }

    float dist(float f, float f2, float f3, float f4) {
        return (float) Math.hypot(f3 - f, f4 - f2);
    }

    ArrayList<InvariantDeviceProfile> findClosestDeviceProfiles(float f, float f2, ArrayList<InvariantDeviceProfile> arrayList) {
        Collections.sort(arrayList, new Comparator<InvariantDeviceProfile>(this, f, f2) { // from class: com.android.launcher3.InvariantDeviceProfile.1
            final InvariantDeviceProfile this$0;
            final float val$height;
            final float val$width;

            {
                this.this$0 = this;
                this.val$width = f;
                this.val$height = f2;
            }

            @Override // java.util.Comparator
            public int compare(InvariantDeviceProfile invariantDeviceProfile, InvariantDeviceProfile invariantDeviceProfile2) {
                return Float.compare(this.this$0.dist(this.val$width, this.val$height, invariantDeviceProfile.minWidthDps, invariantDeviceProfile.minHeightDps), this.this$0.dist(this.val$width, this.val$height, invariantDeviceProfile2.minWidthDps, invariantDeviceProfile2.minHeightDps));
            }
        });
        return arrayList;
    }

    ArrayList<InvariantDeviceProfile> getPredefinedDeviceProfiles() {
        ArrayList<InvariantDeviceProfile> arrayList = new ArrayList<>();
        arrayList.add(new InvariantDeviceProfile("Super Short Stubby", 255.0f, 300.0f, 2, 3, 2, 3, 3, 48.0f, 13.0f, 3, 48.0f, 2131165191));
        arrayList.add(new InvariantDeviceProfile("Shorter Stubby", 255.0f, 400.0f, 3, 3, 3, 3, 3, 48.0f, 13.0f, 3, 48.0f, 2131165191));
        arrayList.add(new InvariantDeviceProfile("Short Stubby", 275.0f, 420.0f, 3, 4, 3, 4, 4, 48.0f, 13.0f, 5, 48.0f, 2131165192));
        arrayList.add(new InvariantDeviceProfile("Stubby", 255.0f, 450.0f, 3, 4, 3, 4, 4, 48.0f, 13.0f, 5, 48.0f, 2131165192));
        arrayList.add(new InvariantDeviceProfile("Nexus S", 296.0f, 491.33f, 4, 4, 4, 4, 4, 48.0f, 13.0f, 5, 48.0f, 2131165192));
        arrayList.add(new InvariantDeviceProfile("Nexus 4", 359.0f, 567.0f, 4, 4, 4, 4, 4, DEFAULT_ICON_SIZE_DP, 13.0f, 5, 56.0f, 2131165192));
        arrayList.add(new InvariantDeviceProfile("Nexus 5", 335.0f, 567.0f, 4, 4, 4, 4, 4, DEFAULT_ICON_SIZE_DP, 13.0f, 5, 56.0f, 2131165192));
        arrayList.add(new InvariantDeviceProfile("Large Phone", 406.0f, 694.0f, 5, 5, 4, 4, 4, 64.0f, 14.4f, 5, 56.0f, 2131165193));
        arrayList.add(new InvariantDeviceProfile("Nexus 7", 575.0f, 904.0f, 5, 6, 4, 5, 4, 72.0f, 14.4f, 7, 60.0f, 2131165194));
        arrayList.add(new InvariantDeviceProfile("Nexus 10", 727.0f, 1207.0f, 5, 6, 4, 5, 4, 76.0f, 14.4f, 7, 76.0f, 2131165194));
        arrayList.add(new InvariantDeviceProfile("20-inch Tablet", 1527.0f, 2527.0f, 7, 7, 6, 6, 4, 100.0f, 20.0f, 7, 72.0f, 2131165194));
        return arrayList;
    }

    InvariantDeviceProfile invDistWeightedInterpolate(float f, float f2, ArrayList<InvariantDeviceProfile> arrayList) {
        float f3 = 0.0f;
        InvariantDeviceProfile invariantDeviceProfile = arrayList.get(0);
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
}

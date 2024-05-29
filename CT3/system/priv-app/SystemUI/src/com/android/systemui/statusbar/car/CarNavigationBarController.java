package com.android.systemui.statusbar.car;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v4.util.SimpleArrayMap;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.LinearLayout;
import com.android.systemui.statusbar.phone.ActivityStarter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: a.zip:com/android/systemui/statusbar/car/CarNavigationBarController.class */
public class CarNavigationBarController {
    private ActivityStarter mActivityStarter;
    private Context mContext;
    private int mCurrentFacetIndex;
    private List<Intent> mIntents;
    private List<Intent> mLongPressIntents;
    private CarNavigationBarView mNavBar;
    private List<String[]> mFacetCategories = new ArrayList();
    private List<String[]> mFacetPackages = new ArrayList();
    private SimpleArrayMap<String, Integer> mFacetCategoryMap = new SimpleArrayMap<>();
    private SimpleArrayMap<String, Integer> mFacetPackageMap = new SimpleArrayMap<>();
    private List<CarNavigationButton> mNavButtons = new ArrayList();
    private SparseBooleanArray mFacetHasMultipleAppsCache = new SparseBooleanArray();

    public CarNavigationBarController(Context context, CarNavigationBarView carNavigationBarView, ActivityStarter activityStarter) {
        this.mContext = context;
        this.mNavBar = carNavigationBarView;
        this.mActivityStarter = activityStarter;
        bind();
    }

    private void bind() {
        Resources resources = this.mContext.getResources();
        TypedArray obtainTypedArray = resources.obtainTypedArray(2131427370);
        TypedArray obtainTypedArray2 = resources.obtainTypedArray(2131427371);
        TypedArray obtainTypedArray3 = resources.obtainTypedArray(2131427372);
        TypedArray obtainTypedArray4 = resources.obtainTypedArray(2131427373);
        TypedArray obtainTypedArray5 = resources.obtainTypedArray(2131427374);
        if (obtainTypedArray.length() != obtainTypedArray2.length() || obtainTypedArray.length() != obtainTypedArray3.length() || obtainTypedArray.length() != obtainTypedArray4.length() || obtainTypedArray.length() != obtainTypedArray5.length()) {
            throw new RuntimeException("car_facet array lengths do not match");
        }
        this.mIntents = createEmptyIntentList(obtainTypedArray.length());
        this.mLongPressIntents = createEmptyIntentList(obtainTypedArray.length());
        for (int i = 0; i < obtainTypedArray.length(); i++) {
            Drawable drawable = obtainTypedArray.getDrawable(i);
            try {
                this.mIntents.set(i, Intent.parseUri(obtainTypedArray2.getString(i), 1));
                String string = obtainTypedArray3.getString(i);
                boolean z = !string.isEmpty();
                if (z) {
                    this.mLongPressIntents.set(i, Intent.parseUri(string, 1));
                }
                CarNavigationButton createNavButton = createNavButton(drawable, i, z);
                this.mNavButtons.add(createNavButton);
                this.mNavBar.addButton(createNavButton, createNavButton(drawable, i, z));
                initFacetFilterMaps(i, obtainTypedArray4.getString(i).split(";"), obtainTypedArray5.getString(i).split(";"));
                this.mFacetHasMultipleAppsCache.put(i, facetHasMultiplePackages(i));
            } catch (URISyntaxException e) {
                throw new RuntimeException("Malformed intent uri", e);
            }
        }
    }

    private List<Intent> createEmptyIntentList(int i) {
        return Arrays.asList(new Intent[i]);
    }

    private CarNavigationButton createNavButton(Drawable drawable, int i, boolean z) {
        CarNavigationButton carNavigationButton = (CarNavigationButton) View.inflate(this.mContext, 2130968610, null);
        carNavigationButton.setResources(drawable);
        carNavigationButton.setLayoutParams(new LinearLayout.LayoutParams(0, -1, 1.0f));
        carNavigationButton.setOnClickListener(new View.OnClickListener(this, i) { // from class: com.android.systemui.statusbar.car.CarNavigationBarController.1
            final CarNavigationBarController this$0;
            final int val$id;

            {
                this.this$0 = this;
                this.val$id = i;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                this.this$0.onFacetClicked(this.val$id);
            }
        });
        if (z) {
            carNavigationButton.setLongClickable(true);
            carNavigationButton.setOnLongClickListener(new View.OnLongClickListener(this, i) { // from class: com.android.systemui.statusbar.car.CarNavigationBarController.2
                final CarNavigationBarController this$0;
                final int val$id;

                {
                    this.this$0 = this;
                    this.val$id = i;
                }

                @Override // android.view.View.OnLongClickListener
                public boolean onLongClick(View view) {
                    this.this$0.onFacetLongClicked(this.val$id);
                    return true;
                }
            });
        } else {
            carNavigationButton.setLongClickable(false);
        }
        return carNavigationButton;
    }

    private boolean facetHasMultiplePackages(int i) {
        PackageManager packageManager = this.mContext.getPackageManager();
        String[] strArr = this.mFacetPackages.get(i);
        if (strArr.length > 1) {
            int i2 = 0;
            for (String str : strArr) {
                i2 += packageManager.getLaunchIntentForPackage(str) != null ? 1 : 0;
                if (i2 > 1) {
                    return true;
                }
            }
        }
        String[] strArr2 = this.mFacetCategories.get(i);
        int i3 = 0;
        for (String str2 : strArr2) {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.MAIN");
            intent.addCategory(str2);
            i3 += packageManager.queryIntentActivities(intent, 0).size();
            if (i3 > 1) {
                return true;
            }
        }
        return false;
    }

    private String getPackageCategory(String str) {
        String[] strArr;
        PackageManager packageManager = this.mContext.getPackageManager();
        int size = this.mFacetCategories.size();
        for (int i = 0; i < size; i++) {
            for (String str2 : this.mFacetCategories.get(i)) {
                Intent intent = new Intent();
                intent.setPackage(str);
                intent.setAction("android.intent.action.MAIN");
                intent.addCategory(str2);
                if (packageManager.queryIntentActivities(intent, 0).size() > 0) {
                    this.mFacetPackageMap.put(str, this.mFacetCategoryMap.get(str2));
                    return str2;
                }
            }
        }
        return null;
    }

    private void initFacetFilterMaps(int i, String[] strArr, String[] strArr2) {
        this.mFacetCategories.add(strArr2);
        for (String str : strArr2) {
            this.mFacetCategoryMap.put(str, Integer.valueOf(i));
        }
        this.mFacetPackages.add(strArr);
        for (String str2 : strArr) {
            this.mFacetPackageMap.put(str2, Integer.valueOf(i));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onFacetClicked(int i) {
        Intent intent = this.mIntents.get(i);
        if (intent.getPackage() == null) {
            return;
        }
        intent.putExtra("categories", this.mFacetCategories.get(i));
        intent.putExtra("packages", this.mFacetPackages.get(i));
        intent.putExtra("filter_id", Integer.toString(i));
        intent.putExtra("launch_picker", i == this.mCurrentFacetIndex);
        setCurrentFacet(i);
        startActivity(intent);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onFacetLongClicked(int i) {
        setCurrentFacet(i);
        startActivity(this.mLongPressIntents.get(i));
    }

    private void setCurrentFacet(int i) {
        if (i == this.mCurrentFacetIndex) {
            return;
        }
        if (this.mNavButtons.get(this.mCurrentFacetIndex) != null) {
            this.mNavButtons.get(this.mCurrentFacetIndex).setSelected(false, false);
        }
        if (this.mNavButtons.get(i) != null) {
            this.mNavButtons.get(i).setSelected(true, this.mFacetHasMultipleAppsCache.get(i));
        }
        this.mCurrentFacetIndex = i;
    }

    private void startActivity(Intent intent) {
        if (this.mActivityStarter == null || intent == null) {
            return;
        }
        this.mActivityStarter.startActivity(intent, false);
    }

    public void onPackageChange(String str) {
        if (this.mFacetPackageMap.containsKey(str)) {
            int intValue = this.mFacetPackageMap.get(str).intValue();
            this.mFacetHasMultipleAppsCache.put(intValue, facetHasMultiplePackages(intValue));
            return;
        }
        String packageCategory = getPackageCategory(str);
        if (this.mFacetCategoryMap.containsKey(packageCategory)) {
            int intValue2 = this.mFacetCategoryMap.get(packageCategory).intValue();
            this.mFacetHasMultipleAppsCache.put(intValue2, facetHasMultiplePackages(intValue2));
        }
    }

    public void taskChanged(String str) {
        if (this.mFacetPackageMap.containsKey(str)) {
            setCurrentFacet(this.mFacetPackageMap.get(str).intValue());
        }
        String packageCategory = getPackageCategory(str);
        if (packageCategory != null) {
            setCurrentFacet(this.mFacetCategoryMap.get(packageCategory).intValue());
        }
    }
}

package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Icon;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Space;
import com.android.systemui.Dependency;
import com.android.systemui.OverviewProxyService;
import com.android.systemui.R;
import com.android.systemui.plugins.PluginListener;
import com.android.systemui.plugins.PluginManager;
import com.android.systemui.plugins.statusbar.phone.NavBarButtonProvider;
import com.android.systemui.statusbar.phone.ReverseLinearLayout;
import com.android.systemui.statusbar.policy.KeyButtonView;
import com.android.systemui.tuner.TunerService;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
/* loaded from: classes.dex */
public class NavigationBarInflaterView extends FrameLayout implements PluginListener<NavBarButtonProvider>, TunerService.Tunable {
    private boolean isRot0Landscape;
    private boolean mAlternativeOrder;
    private SparseArray<ButtonDispatcher> mButtonDispatchers;
    private String mCurrentLayout;
    private final Display mDisplay;
    protected LayoutInflater mLandscapeInflater;
    private View mLastLandscape;
    private View mLastPortrait;
    protected LayoutInflater mLayoutInflater;
    private OverviewProxyService mOverviewProxyService;
    private final List<NavBarButtonProvider> mPlugins;
    protected FrameLayout mRot0;
    protected FrameLayout mRot90;
    private boolean mUsingCustomLayout;

    public NavigationBarInflaterView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mPlugins = new ArrayList();
        createInflaters();
        this.mDisplay = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        Display.Mode mode = this.mDisplay.getMode();
        this.isRot0Landscape = mode.getPhysicalWidth() > mode.getPhysicalHeight();
        this.mOverviewProxyService = (OverviewProxyService) Dependency.get(OverviewProxyService.class);
    }

    private void createInflaters() {
        this.mLayoutInflater = LayoutInflater.from(this.mContext);
        Configuration configuration = new Configuration();
        configuration.setTo(this.mContext.getResources().getConfiguration());
        configuration.orientation = 2;
        this.mLandscapeInflater = LayoutInflater.from(this.mContext.createConfigurationContext(configuration));
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        inflateChildren();
        clearViews();
        inflateLayout(getDefaultLayout());
    }

    private void inflateChildren() {
        removeAllViews();
        this.mRot0 = (FrameLayout) this.mLayoutInflater.inflate(R.layout.navigation_layout, (ViewGroup) this, false);
        this.mRot0.setId(R.id.rot0);
        addView(this.mRot0);
        this.mRot90 = (FrameLayout) this.mLayoutInflater.inflate(R.layout.navigation_layout_rot90, (ViewGroup) this, false);
        this.mRot90.setId(R.id.rot90);
        addView(this.mRot90);
        updateAlternativeOrder();
    }

    protected String getDefaultLayout() {
        int i;
        if (this.mOverviewProxyService.shouldShowSwipeUpUI()) {
            i = R.string.config_navBarLayoutQuickstep;
        } else {
            i = R.string.config_navBarLayout;
        }
        return this.mContext.getString(i);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.ViewGroup, android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "sysui_nav_bar", "sysui_nav_bar_left", "sysui_nav_bar_right");
        ((PluginManager) Dependency.get(PluginManager.class)).addPluginListener((PluginListener) this, NavBarButtonProvider.class, true);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        ((TunerService) Dependency.get(TunerService.class)).removeTunable(this);
        ((PluginManager) Dependency.get(PluginManager.class)).removePluginListener(this);
        super.onDetachedFromWindow();
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        if ("sysui_nav_bar".equals(str)) {
            if (!Objects.equals(this.mCurrentLayout, str2)) {
                this.mUsingCustomLayout = str2 != null;
                clearViews();
                inflateLayout(str2);
            }
        } else if ("sysui_nav_bar_left".equals(str) || "sysui_nav_bar_right".equals(str)) {
            clearViews();
            inflateLayout(this.mCurrentLayout);
        }
    }

    public void onLikelyDefaultLayoutChange() {
        if (this.mUsingCustomLayout) {
            return;
        }
        String defaultLayout = getDefaultLayout();
        if (!Objects.equals(this.mCurrentLayout, defaultLayout)) {
            clearViews();
            inflateLayout(defaultLayout);
        }
    }

    public void setButtonDispatchers(SparseArray<ButtonDispatcher> sparseArray) {
        this.mButtonDispatchers = sparseArray;
        for (int i = 0; i < sparseArray.size(); i++) {
            initiallyFill(sparseArray.valueAt(i));
        }
    }

    public void updateButtonDispatchersCurrentView() {
        if (this.mButtonDispatchers != null) {
            int rotation = this.mDisplay.getRotation();
            FrameLayout frameLayout = rotation == 0 || rotation == 2 ? this.mRot0 : this.mRot90;
            for (int i = 0; i < this.mButtonDispatchers.size(); i++) {
                this.mButtonDispatchers.valueAt(i).setCurrentView(frameLayout);
            }
        }
    }

    public void setAlternativeOrder(boolean z) {
        if (z != this.mAlternativeOrder) {
            this.mAlternativeOrder = z;
            updateAlternativeOrder();
        }
    }

    private void updateAlternativeOrder() {
        updateAlternativeOrder(this.mRot0.findViewById(R.id.ends_group));
        updateAlternativeOrder(this.mRot0.findViewById(R.id.center_group));
        updateAlternativeOrder(this.mRot90.findViewById(R.id.ends_group));
        updateAlternativeOrder(this.mRot90.findViewById(R.id.center_group));
    }

    private void updateAlternativeOrder(View view) {
        if (view instanceof ReverseLinearLayout) {
            ((ReverseLinearLayout) view).setAlternativeOrder(this.mAlternativeOrder);
        }
    }

    private void initiallyFill(ButtonDispatcher buttonDispatcher) {
        addAll(buttonDispatcher, (ViewGroup) this.mRot0.findViewById(R.id.ends_group));
        addAll(buttonDispatcher, (ViewGroup) this.mRot0.findViewById(R.id.center_group));
        addAll(buttonDispatcher, (ViewGroup) this.mRot90.findViewById(R.id.ends_group));
        addAll(buttonDispatcher, (ViewGroup) this.mRot90.findViewById(R.id.center_group));
    }

    private void addAll(ButtonDispatcher buttonDispatcher, ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            if (viewGroup.getChildAt(i).getId() == buttonDispatcher.getId()) {
                buttonDispatcher.addView(viewGroup.getChildAt(i));
            }
            if (viewGroup.getChildAt(i) instanceof ViewGroup) {
                addAll(buttonDispatcher, (ViewGroup) viewGroup.getChildAt(i));
            }
        }
    }

    protected void inflateLayout(String str) {
        this.mCurrentLayout = str;
        if (str == null) {
            str = getDefaultLayout();
        }
        String[] split = str.split(";", 3);
        if (split.length != 3) {
            Log.d("NavBarInflater", "Invalid layout.");
            split = getDefaultLayout().split(";", 3);
        }
        String[] split2 = split[0].split(",");
        String[] split3 = split[1].split(",");
        String[] split4 = split[2].split(",");
        inflateButtons(split2, (ViewGroup) this.mRot0.findViewById(R.id.ends_group), this.isRot0Landscape, true);
        inflateButtons(split2, (ViewGroup) this.mRot90.findViewById(R.id.ends_group), !this.isRot0Landscape, true);
        inflateButtons(split3, (ViewGroup) this.mRot0.findViewById(R.id.center_group), this.isRot0Landscape, false);
        inflateButtons(split3, (ViewGroup) this.mRot90.findViewById(R.id.center_group), !this.isRot0Landscape, false);
        addGravitySpacer((LinearLayout) this.mRot0.findViewById(R.id.ends_group));
        addGravitySpacer((LinearLayout) this.mRot90.findViewById(R.id.ends_group));
        inflateButtons(split4, (ViewGroup) this.mRot0.findViewById(R.id.ends_group), this.isRot0Landscape, false);
        inflateButtons(split4, (ViewGroup) this.mRot90.findViewById(R.id.ends_group), true ^ this.isRot0Landscape, false);
        updateButtonDispatchersCurrentView();
    }

    private void addGravitySpacer(LinearLayout linearLayout) {
        linearLayout.addView(new Space(this.mContext), new LinearLayout.LayoutParams(0, 0, 1.0f));
    }

    private void inflateButtons(String[] strArr, ViewGroup viewGroup, boolean z, boolean z2) {
        for (String str : strArr) {
            inflateButton(str, viewGroup, z, z2);
        }
    }

    protected View inflateButton(String str, ViewGroup viewGroup, boolean z, boolean z2) {
        View view;
        View createView = createView(str, viewGroup, z ? this.mLandscapeInflater : this.mLayoutInflater);
        if (createView == null) {
            return null;
        }
        View applySize = applySize(createView, str, z, z2);
        viewGroup.addView(applySize);
        addToDispatchers(applySize);
        View view2 = z ? this.mLastLandscape : this.mLastPortrait;
        if (applySize instanceof ReverseLinearLayout.ReverseRelativeLayout) {
            view = ((ReverseLinearLayout.ReverseRelativeLayout) applySize).getChildAt(0);
        } else {
            view = applySize;
        }
        if (view2 != null) {
            view.setAccessibilityTraversalAfter(view2.getId());
        }
        if (z) {
            this.mLastLandscape = view;
        } else {
            this.mLastPortrait = view;
        }
        return applySize;
    }

    private View applySize(View view, String str, boolean z, boolean z2) {
        int i;
        String extractSize = extractSize(str);
        if (extractSize == null) {
            return view;
        }
        if (extractSize.contains("W")) {
            float parseFloat = Float.parseFloat(extractSize.substring(0, extractSize.indexOf("W")));
            ReverseLinearLayout.ReverseRelativeLayout reverseRelativeLayout = new ReverseLinearLayout.ReverseRelativeLayout(this.mContext);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(view.getLayoutParams());
            if (z) {
                i = z2 ? 48 : 80;
            } else {
                i = z2 ? 8388611 : 8388613;
            }
            if (extractSize.endsWith("WC")) {
                i = 17;
            }
            reverseRelativeLayout.setDefaultGravity(i);
            reverseRelativeLayout.setGravity(i);
            reverseRelativeLayout.addView(view, layoutParams);
            reverseRelativeLayout.setLayoutParams(new LinearLayout.LayoutParams(0, -1, parseFloat));
            reverseRelativeLayout.setClipChildren(false);
            reverseRelativeLayout.setClipToPadding(false);
            return reverseRelativeLayout;
        }
        float parseFloat2 = Float.parseFloat(extractSize);
        ViewGroup.LayoutParams layoutParams2 = view.getLayoutParams();
        layoutParams2.width = (int) (layoutParams2.width * parseFloat2);
        return view;
    }

    private View createView(String str, ViewGroup viewGroup, LayoutInflater layoutInflater) {
        String extractButton = extractButton(str);
        if ("left".equals(extractButton)) {
            extractButton = extractButton(((TunerService) Dependency.get(TunerService.class)).getValue("sysui_nav_bar_left", "space"));
        } else if ("right".equals(extractButton)) {
            extractButton = extractButton(((TunerService) Dependency.get(TunerService.class)).getValue("sysui_nav_bar_right", "menu_ime"));
        }
        View view = null;
        for (NavBarButtonProvider navBarButtonProvider : this.mPlugins) {
            view = navBarButtonProvider.createView(str, viewGroup);
            if (view != null) {
                return view;
            }
        }
        if ("home".equals(extractButton)) {
            return layoutInflater.inflate(R.layout.home, viewGroup, false);
        }
        if ("back".equals(extractButton)) {
            return layoutInflater.inflate(R.layout.back, viewGroup, false);
        }
        if ("recent".equals(extractButton)) {
            return layoutInflater.inflate(R.layout.recent_apps, viewGroup, false);
        }
        if ("menu_ime".equals(extractButton)) {
            return layoutInflater.inflate(R.layout.menu_ime, viewGroup, false);
        }
        if ("space".equals(extractButton)) {
            return layoutInflater.inflate(R.layout.nav_key_space, viewGroup, false);
        }
        if ("clipboard".equals(extractButton)) {
            return layoutInflater.inflate(R.layout.clipboard, viewGroup, false);
        }
        if ("contextual".equals(extractButton)) {
            return layoutInflater.inflate(R.layout.contextual, viewGroup, false);
        }
        if (extractButton.startsWith("key")) {
            String extractImage = extractImage(extractButton);
            int extractKeycode = extractKeycode(extractButton);
            View inflate = layoutInflater.inflate(R.layout.custom_key, viewGroup, false);
            KeyButtonView keyButtonView = (KeyButtonView) inflate;
            keyButtonView.setCode(extractKeycode);
            if (extractImage != null) {
                if (extractImage.contains(":")) {
                    keyButtonView.loadAsync(Icon.createWithContentUri(extractImage));
                    return inflate;
                } else if (extractImage.contains("/")) {
                    int indexOf = extractImage.indexOf(47);
                    keyButtonView.loadAsync(Icon.createWithResource(extractImage.substring(0, indexOf), Integer.parseInt(extractImage.substring(indexOf + 1))));
                    return inflate;
                } else {
                    return inflate;
                }
            }
            return inflate;
        }
        return view;
    }

    public static String extractImage(String str) {
        if (!str.contains(":")) {
            return null;
        }
        return str.substring(str.indexOf(":") + 1, str.indexOf(")"));
    }

    public static int extractKeycode(String str) {
        if (str.contains("(")) {
            return Integer.parseInt(str.substring(str.indexOf("(") + 1, str.indexOf(":")));
        }
        return 1;
    }

    public static String extractSize(String str) {
        if (!str.contains("[")) {
            return null;
        }
        return str.substring(str.indexOf("[") + 1, str.indexOf("]"));
    }

    public static String extractButton(String str) {
        if (!str.contains("[")) {
            return str;
        }
        return str.substring(0, str.indexOf("["));
    }

    private void addToDispatchers(View view) {
        if (this.mButtonDispatchers != null) {
            int indexOfKey = this.mButtonDispatchers.indexOfKey(view.getId());
            if (indexOfKey >= 0) {
                this.mButtonDispatchers.valueAt(indexOfKey).addView(view);
            }
            if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                int childCount = viewGroup.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    addToDispatchers(viewGroup.getChildAt(i));
                }
            }
        }
    }

    private void clearViews() {
        if (this.mButtonDispatchers != null) {
            for (int i = 0; i < this.mButtonDispatchers.size(); i++) {
                this.mButtonDispatchers.valueAt(i).clear();
            }
        }
        clearAllChildren((ViewGroup) this.mRot0.findViewById(R.id.nav_buttons));
        clearAllChildren((ViewGroup) this.mRot90.findViewById(R.id.nav_buttons));
    }

    private void clearAllChildren(ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            ((ViewGroup) viewGroup.getChildAt(i)).removeAllViews();
        }
    }

    @Override // com.android.systemui.plugins.PluginListener
    public void onPluginConnected(NavBarButtonProvider navBarButtonProvider, Context context) {
        this.mPlugins.add(navBarButtonProvider);
        clearViews();
        inflateLayout(this.mCurrentLayout);
    }

    @Override // com.android.systemui.plugins.PluginListener
    public void onPluginDisconnected(NavBarButtonProvider navBarButtonProvider) {
        this.mPlugins.remove(navBarButtonProvider);
        clearViews();
        inflateLayout(this.mCurrentLayout);
    }
}

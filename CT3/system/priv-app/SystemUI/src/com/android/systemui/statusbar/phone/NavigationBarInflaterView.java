package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Space;
import com.android.systemui.statusbar.policy.KeyButtonView;
import com.android.systemui.tuner.TunerService;
import com.mediatek.multiwindow.MultiWindowManager;
import java.util.Objects;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/NavigationBarInflaterView.class */
public class NavigationBarInflaterView extends FrameLayout implements TunerService.Tunable {
    private SparseArray<ButtonDispatcher> mButtonDispatchers;
    private String mCurrentLayout;
    private int mDensity;
    protected LayoutInflater mLandscapeInflater;
    private View mLastRot0;
    private View mLastRot90;
    protected LayoutInflater mLayoutInflater;
    protected FrameLayout mRot0;
    protected FrameLayout mRot90;

    public NavigationBarInflaterView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mDensity = context.getResources().getConfiguration().densityDpi;
        createInflaters();
    }

    private void addAll(ButtonDispatcher buttonDispatcher, ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            if (viewGroup.getChildAt(i).getId() == buttonDispatcher.getId()) {
                buttonDispatcher.addView(viewGroup.getChildAt(i));
            } else if (viewGroup.getChildAt(i) instanceof ViewGroup) {
                addAll(buttonDispatcher, (ViewGroup) viewGroup.getChildAt(i));
            }
        }
    }

    private void addGravitySpacer(LinearLayout linearLayout) {
        linearLayout.addView(new Space(this.mContext), new LinearLayout.LayoutParams(0, 0, 1.0f));
    }

    private void addToDispatchers(View view) {
        if (this.mButtonDispatchers != null) {
            int indexOfKey = this.mButtonDispatchers.indexOfKey(view.getId());
            if (indexOfKey >= 0) {
                this.mButtonDispatchers.valueAt(indexOfKey).addView(view);
            } else if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                int childCount = viewGroup.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    addToDispatchers(viewGroup.getChildAt(i));
                }
            }
        }
    }

    private void clearAllChildren(ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            ((ViewGroup) viewGroup.getChildAt(i)).removeAllViews();
        }
    }

    private void clearViews() {
        if (this.mButtonDispatchers != null) {
            for (int i = 0; i < this.mButtonDispatchers.size(); i++) {
                this.mButtonDispatchers.valueAt(i).clear();
            }
        }
        clearAllChildren((ViewGroup) this.mRot0.findViewById(2131886266));
        clearAllChildren((ViewGroup) this.mRot90.findViewById(2131886266));
    }

    private void createInflaters() {
        this.mLayoutInflater = LayoutInflater.from(this.mContext);
        Configuration configuration = new Configuration();
        configuration.setTo(this.mContext.getResources().getConfiguration());
        configuration.orientation = 2;
        this.mLandscapeInflater = LayoutInflater.from(this.mContext.createConfigurationContext(configuration));
    }

    public static String extractButton(String str) {
        return !str.contains("[") ? str : str.substring(0, str.indexOf("["));
    }

    public static String extractImage(String str) {
        if (str.contains(":")) {
            return str.substring(str.indexOf(":") + 1, str.indexOf(")"));
        }
        return null;
    }

    public static int extractKeycode(String str) {
        if (str.contains("(")) {
            return Integer.parseInt(str.substring(str.indexOf("(") + 1, str.indexOf(":")));
        }
        return 1;
    }

    public static float extractSize(String str) {
        if (str.contains("[")) {
            return Float.parseFloat(str.substring(str.indexOf("[") + 1, str.indexOf("]")));
        }
        return 1.0f;
    }

    private void inflateButtons(String[] strArr, ViewGroup viewGroup, boolean z) {
        for (int i = 0; i < strArr.length; i++) {
            inflateButton(strArr[i], viewGroup, z, i);
        }
    }

    private void inflateChildren() {
        removeAllViews();
        this.mRot0 = (FrameLayout) this.mLayoutInflater.inflate(2130968720, (ViewGroup) this, false);
        this.mRot0.setId(2131886532);
        addView(this.mRot0);
        this.mRot90 = (FrameLayout) this.mLayoutInflater.inflate(2130968721, (ViewGroup) this, false);
        this.mRot90.setId(2131886533);
        addView(this.mRot90);
        if (getParent() instanceof NavigationBarView) {
            ((NavigationBarView) getParent()).updateRotatedViews();
        }
    }

    private void initiallyFill(ButtonDispatcher buttonDispatcher) {
        addAll(buttonDispatcher, (ViewGroup) this.mRot0.findViewById(2131886539));
        addAll(buttonDispatcher, (ViewGroup) this.mRot0.findViewById(2131886540));
        addAll(buttonDispatcher, (ViewGroup) this.mRot90.findViewById(2131886539));
        addAll(buttonDispatcher, (ViewGroup) this.mRot90.findViewById(2131886540));
    }

    private boolean isSw600Dp() {
        return this.mContext.getResources().getConfiguration().smallestScreenWidthDp >= 600;
    }

    private void setupLandButton(View view) {
        Resources resources = this.mContext.getResources();
        view.getLayoutParams().width = resources.getDimensionPixelOffset(2131689800);
        int dimensionPixelOffset = resources.getDimensionPixelOffset(2131689801);
        view.setPadding(dimensionPixelOffset, view.getPaddingTop(), dimensionPixelOffset, view.getPaddingBottom());
    }

    protected String getDefaultLayout() {
        return MultiWindowManager.isSupported() ? this.mContext.getString(2131493267) : this.mContext.getString(2131493281);
    }

    protected View inflateButton(String str, ViewGroup viewGroup, boolean z, int i) {
        View view;
        LayoutInflater layoutInflater = z ? this.mLandscapeInflater : this.mLayoutInflater;
        float extractSize = extractSize(str);
        String extractButton = extractButton(str);
        if ("home".equals(extractButton)) {
            View inflate = layoutInflater.inflate(2130968620, viewGroup, false);
            view = inflate;
            if (z) {
                view = inflate;
                if (isSw600Dp()) {
                    setupLandButton(inflate);
                    view = inflate;
                }
            }
        } else if ("back".equals(extractButton)) {
            View inflate2 = layoutInflater.inflate(2130968603, viewGroup, false);
            view = inflate2;
            if (z) {
                view = inflate2;
                if (isSw600Dp()) {
                    setupLandButton(inflate2);
                    view = inflate2;
                }
            }
        } else if ("recent".equals(extractButton)) {
            View inflate3 = layoutInflater.inflate(2130968775, viewGroup, false);
            view = inflate3;
            if (z) {
                view = inflate3;
                if (isSw600Dp()) {
                    setupLandButton(inflate3);
                    view = inflate3;
                }
            }
        } else if ("menu_ime".equals(extractButton)) {
            view = layoutInflater.inflate(2130968703, viewGroup, false);
        } else if ("space".equals(extractButton)) {
            View inflate4 = layoutInflater.inflate(2130968716, viewGroup, false);
            view = inflate4;
            if (MultiWindowManager.isSupported()) {
                view = inflate4;
                if (z) {
                    view = inflate4;
                    if (isSw600Dp()) {
                        setupLandButton(inflate4);
                        view = inflate4;
                    }
                }
            }
        } else if ("clipboard".equals(extractButton)) {
            view = layoutInflater.inflate(2130968611, viewGroup, false);
        } else if (MultiWindowManager.isSupported() && "restore".equals(extractButton)) {
            View inflate5 = layoutInflater.inflate(2130968791, viewGroup, false);
            view = inflate5;
            if (z) {
                view = inflate5;
                if (isSw600Dp()) {
                    setupLandButton(inflate5);
                    view = inflate5;
                }
            }
        } else if (!extractButton.startsWith("key")) {
            return null;
        } else {
            String extractImage = extractImage(extractButton);
            int extractKeycode = extractKeycode(extractButton);
            View inflate6 = layoutInflater.inflate(2130968612, viewGroup, false);
            ((KeyButtonView) inflate6).setCode(extractKeycode);
            view = inflate6;
            if (extractImage != null) {
                ((KeyButtonView) inflate6).loadAsync(extractImage);
                view = inflate6;
            }
        }
        if (extractSize != 0.0f) {
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.width = (int) (layoutParams.width * extractSize);
        }
        viewGroup.addView(view);
        addToDispatchers(view);
        View view2 = z ? this.mLastRot90 : this.mLastRot0;
        if (view2 != null) {
            view.setAccessibilityTraversalAfter(view2.getId());
        }
        if (z) {
            this.mLastRot90 = view;
        } else {
            this.mLastRot0 = view;
        }
        return view;
    }

    protected void inflateLayout(String str) {
        this.mCurrentLayout = str;
        String str2 = str;
        if (str == null) {
            str2 = getDefaultLayout();
        }
        String[] split = str2.split(";", 3);
        String[] split2 = split[0].split(",");
        String[] split3 = split[1].split(",");
        String[] split4 = split[2].split(",");
        inflateButtons(split2, (ViewGroup) this.mRot0.findViewById(2131886539), false);
        inflateButtons(split2, (ViewGroup) this.mRot90.findViewById(2131886539), true);
        inflateButtons(split3, (ViewGroup) this.mRot0.findViewById(2131886540), false);
        inflateButtons(split3, (ViewGroup) this.mRot90.findViewById(2131886540), true);
        addGravitySpacer((LinearLayout) this.mRot0.findViewById(2131886539));
        addGravitySpacer((LinearLayout) this.mRot90.findViewById(2131886539));
        inflateButtons(split4, (ViewGroup) this.mRot0.findViewById(2131886539), false);
        inflateButtons(split4, (ViewGroup) this.mRot90.findViewById(2131886539), true);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.ViewGroup, android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        TunerService.get(getContext()).addTunable(this, "sysui_nav_bar");
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        if (this.mDensity != configuration.densityDpi) {
            this.mDensity = configuration.densityDpi;
            createInflaters();
            inflateChildren();
            clearViews();
            inflateLayout(this.mCurrentLayout);
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        TunerService.get(getContext()).removeTunable(this);
        super.onDetachedFromWindow();
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        inflateChildren();
        clearViews();
        inflateLayout(getDefaultLayout());
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        if (!"sysui_nav_bar".equals(str) || Objects.equals(this.mCurrentLayout, str2)) {
            return;
        }
        clearViews();
        inflateLayout(str2);
    }

    public void setButtonDispatchers(SparseArray<ButtonDispatcher> sparseArray) {
        this.mButtonDispatchers = sparseArray;
        for (int i = 0; i < sparseArray.size(); i++) {
            initiallyFill(sparseArray.valueAt(i));
        }
    }
}

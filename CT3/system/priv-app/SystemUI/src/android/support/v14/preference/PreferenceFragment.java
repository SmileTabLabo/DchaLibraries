package android.support.v14.preference;

import android.app.Fragment;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.XmlRes;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v4.view.ViewCompat;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroupAdapter;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceRecyclerViewAccessibilityDelegate;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.PreferenceViewHolder;
import android.support.v7.preference.R$attr;
import android.support.v7.preference.R$layout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
/* loaded from: a.zip:android/support/v14/preference/PreferenceFragment.class */
public abstract class PreferenceFragment extends Fragment implements PreferenceManager.OnPreferenceTreeClickListener, PreferenceManager.OnDisplayPreferenceDialogListener, PreferenceManager.OnNavigateToScreenListener, DialogPreference.TargetFragment {
    private boolean mHavePrefs;
    private boolean mInitDone;
    private RecyclerView mList;
    private PreferenceManager mPreferenceManager;
    private Runnable mSelectPreferenceRunnable;
    private Context mStyledContext;
    private int mLayoutResId = R$layout.preference_list_fragment;
    private final DividerDecoration mDividerDecoration = new DividerDecoration(this, null);
    private Handler mHandler = new Handler(this) { // from class: android.support.v14.preference.PreferenceFragment.1
        final PreferenceFragment this$0;

        {
            this.this$0 = this;
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    this.this$0.bindPreferences();
                    return;
                default:
                    return;
            }
        }
    };
    private final Runnable mRequestFocus = new Runnable(this) { // from class: android.support.v14.preference.PreferenceFragment.2
        final PreferenceFragment this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            this.this$0.mList.focusableViewAvailable(this.this$0.mList);
        }
    };

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:android/support/v14/preference/PreferenceFragment$DividerDecoration.class */
    public class DividerDecoration extends RecyclerView.ItemDecoration {
        private Drawable mDivider;
        private int mDividerHeight;
        final PreferenceFragment this$0;

        private DividerDecoration(PreferenceFragment preferenceFragment) {
            this.this$0 = preferenceFragment;
        }

        /* synthetic */ DividerDecoration(PreferenceFragment preferenceFragment, DividerDecoration dividerDecoration) {
            this(preferenceFragment);
        }

        private boolean shouldDrawDividerBelow(View view, RecyclerView recyclerView) {
            RecyclerView.ViewHolder childViewHolder = recyclerView.getChildViewHolder(view);
            if (childViewHolder instanceof PreferenceViewHolder ? ((PreferenceViewHolder) childViewHolder).isDividerAllowedBelow() : false) {
                boolean z = true;
                int indexOfChild = recyclerView.indexOfChild(view);
                if (indexOfChild < recyclerView.getChildCount() - 1) {
                    RecyclerView.ViewHolder childViewHolder2 = recyclerView.getChildViewHolder(recyclerView.getChildAt(indexOfChild + 1));
                    z = childViewHolder2 instanceof PreferenceViewHolder ? ((PreferenceViewHolder) childViewHolder2).isDividerAllowedAbove() : false;
                }
                return z;
            }
            return false;
        }

        @Override // android.support.v7.widget.RecyclerView.ItemDecoration
        public void getItemOffsets(Rect rect, View view, RecyclerView recyclerView, RecyclerView.State state) {
            if (shouldDrawDividerBelow(view, recyclerView)) {
                rect.bottom = this.mDividerHeight;
            }
        }

        @Override // android.support.v7.widget.RecyclerView.ItemDecoration
        public void onDrawOver(Canvas canvas, RecyclerView recyclerView, RecyclerView.State state) {
            if (this.mDivider == null) {
                return;
            }
            int childCount = recyclerView.getChildCount();
            int width = recyclerView.getWidth();
            for (int i = 0; i < childCount; i++) {
                View childAt = recyclerView.getChildAt(i);
                if (shouldDrawDividerBelow(childAt, recyclerView)) {
                    int y = ((int) ViewCompat.getY(childAt)) + childAt.getHeight();
                    this.mDivider.setBounds(0, y, width, this.mDividerHeight + y);
                    this.mDivider.draw(canvas);
                }
            }
        }

        public void setDivider(Drawable drawable) {
            if (drawable != null) {
                this.mDividerHeight = drawable.getIntrinsicHeight();
            } else {
                this.mDividerHeight = 0;
            }
            this.mDivider = drawable;
            this.this$0.mList.invalidateItemDecorations();
        }

        public void setDividerHeight(int i) {
            this.mDividerHeight = i;
            this.this$0.mList.invalidateItemDecorations();
        }
    }

    /* loaded from: a.zip:android/support/v14/preference/PreferenceFragment$OnPreferenceDisplayDialogCallback.class */
    public interface OnPreferenceDisplayDialogCallback {
        boolean onPreferenceDisplayDialog(PreferenceFragment preferenceFragment, Preference preference);
    }

    /* loaded from: a.zip:android/support/v14/preference/PreferenceFragment$OnPreferenceStartFragmentCallback.class */
    public interface OnPreferenceStartFragmentCallback {
        boolean onPreferenceStartFragment(PreferenceFragment preferenceFragment, Preference preference);
    }

    /* loaded from: a.zip:android/support/v14/preference/PreferenceFragment$OnPreferenceStartScreenCallback.class */
    public interface OnPreferenceStartScreenCallback {
        boolean onPreferenceStartScreen(PreferenceFragment preferenceFragment, PreferenceScreen preferenceScreen);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void bindPreferences() {
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        if (preferenceScreen != null) {
            getListView().setAdapter(onCreateAdapter(preferenceScreen));
            preferenceScreen.onAttached();
        }
        onBindPreferences();
    }

    private void postBindPreferences() {
        if (this.mHandler.hasMessages(1)) {
            return;
        }
        this.mHandler.obtainMessage(1).sendToTarget();
    }

    private void requirePreferenceManager() {
        if (this.mPreferenceManager == null) {
            throw new RuntimeException("This should be called after super.onCreate.");
        }
    }

    private void unbindPreferences() {
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        if (preferenceScreen != null) {
            preferenceScreen.onDetached();
        }
        onUnbindPreferences();
    }

    public void addPreferencesFromResource(@XmlRes int i) {
        requirePreferenceManager();
        setPreferenceScreen(this.mPreferenceManager.inflateFromResource(this.mStyledContext, i, getPreferenceScreen()));
    }

    @Override // android.support.v7.preference.DialogPreference.TargetFragment
    public Preference findPreference(CharSequence charSequence) {
        if (this.mPreferenceManager == null) {
            return null;
        }
        return this.mPreferenceManager.findPreference(charSequence);
    }

    public Fragment getCallbackFragment() {
        return null;
    }

    public final RecyclerView getListView() {
        return this.mList;
    }

    public PreferenceManager getPreferenceManager() {
        return this.mPreferenceManager;
    }

    public PreferenceScreen getPreferenceScreen() {
        return this.mPreferenceManager.getPreferenceScreen();
    }

    @Override // android.app.Fragment
    public void onActivityCreated(Bundle bundle) {
        Bundle bundle2;
        PreferenceScreen preferenceScreen;
        super.onActivityCreated(bundle);
        if (bundle == null || (bundle2 = bundle.getBundle("android:preferences")) == null || (preferenceScreen = getPreferenceScreen()) == null) {
            return;
        }
        preferenceScreen.restoreHierarchyState(bundle2);
    }

    protected void onBindPreferences() {
    }

    @Override // android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        TypedValue typedValue = new TypedValue();
        getActivity().getTheme().resolveAttribute(R$attr.preferenceTheme, typedValue, true);
        int i = typedValue.resourceId;
        if (i <= 0) {
            throw new IllegalStateException("Must specify preferenceTheme in theme");
        }
        this.mStyledContext = new ContextThemeWrapper(getActivity(), i);
        this.mPreferenceManager = new PreferenceManager(this.mStyledContext);
        this.mPreferenceManager.setOnNavigateToScreenListener(this);
        onCreatePreferences(bundle, getArguments() != null ? getArguments().getString("android.support.v7.preference.PreferenceFragmentCompat.PREFERENCE_ROOT") : null);
    }

    protected RecyclerView.Adapter onCreateAdapter(PreferenceScreen preferenceScreen) {
        return new PreferenceGroupAdapter(preferenceScreen);
    }

    public RecyclerView.LayoutManager onCreateLayoutManager() {
        return new LinearLayoutManager(getActivity());
    }

    public abstract void onCreatePreferences(Bundle bundle, String str);

    public RecyclerView onCreateRecyclerView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        RecyclerView recyclerView = (RecyclerView) layoutInflater.inflate(R$layout.preference_recyclerview, viewGroup, false);
        recyclerView.setLayoutManager(onCreateLayoutManager());
        recyclerView.setAccessibilityDelegateCompat(new PreferenceRecyclerViewAccessibilityDelegate(recyclerView));
        return recyclerView;
    }

    @Override // android.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        TypedArray obtainStyledAttributes = this.mStyledContext.obtainStyledAttributes(null, R$styleable.PreferenceFragment, TypedArrayUtils.getAttr(this.mStyledContext, R$attr.preferenceFragmentStyle, 16844038), 0);
        this.mLayoutResId = obtainStyledAttributes.getResourceId(R$styleable.PreferenceFragment_android_layout, this.mLayoutResId);
        Drawable drawable = obtainStyledAttributes.getDrawable(R$styleable.PreferenceFragment_android_divider);
        int dimensionPixelSize = obtainStyledAttributes.getDimensionPixelSize(R$styleable.PreferenceFragment_android_dividerHeight, -1);
        obtainStyledAttributes.recycle();
        TypedValue typedValue = new TypedValue();
        getActivity().getTheme().resolveAttribute(R$attr.preferenceTheme, typedValue, true);
        LayoutInflater cloneInContext = layoutInflater.cloneInContext(new ContextThemeWrapper(layoutInflater.getContext(), typedValue.resourceId));
        View inflate = cloneInContext.inflate(this.mLayoutResId, viewGroup, false);
        View findViewById = inflate.findViewById(16908351);
        if (findViewById instanceof ViewGroup) {
            ViewGroup viewGroup2 = (ViewGroup) findViewById;
            RecyclerView onCreateRecyclerView = onCreateRecyclerView(cloneInContext, viewGroup2, bundle);
            if (onCreateRecyclerView == null) {
                throw new RuntimeException("Could not create RecyclerView");
            }
            this.mList = onCreateRecyclerView;
            onCreateRecyclerView.addItemDecoration(this.mDividerDecoration);
            setDivider(drawable);
            if (dimensionPixelSize != -1) {
                setDividerHeight(dimensionPixelSize);
            }
            viewGroup2.addView(this.mList);
            this.mHandler.post(this.mRequestFocus);
            return inflate;
        }
        throw new RuntimeException("Content has view with id attribute 'android.R.id.list_container' that is not a ViewGroup class");
    }

    @Override // android.app.Fragment
    public void onDestroyView() {
        this.mHandler.removeCallbacks(this.mRequestFocus);
        this.mHandler.removeMessages(1);
        if (this.mHavePrefs) {
            unbindPreferences();
        }
        this.mList = null;
        super.onDestroyView();
    }

    @Override // android.support.v7.preference.PreferenceManager.OnDisplayPreferenceDialogListener
    public void onDisplayPreferenceDialog(Preference preference) {
        PreferenceDialogFragment newInstance;
        boolean z = false;
        if (getCallbackFragment() instanceof OnPreferenceDisplayDialogCallback) {
            z = ((OnPreferenceDisplayDialogCallback) getCallbackFragment()).onPreferenceDisplayDialog(this, preference);
        }
        boolean z2 = z;
        if (!z) {
            z2 = z;
            if (getActivity() instanceof OnPreferenceDisplayDialogCallback) {
                z2 = ((OnPreferenceDisplayDialogCallback) getActivity()).onPreferenceDisplayDialog(this, preference);
            }
        }
        if (!z2 && getFragmentManager().findFragmentByTag("android.support.v14.preference.PreferenceFragment.DIALOG") == null) {
            if (preference instanceof EditTextPreference) {
                newInstance = EditTextPreferenceDialogFragment.newInstance(preference.getKey());
            } else if (preference instanceof ListPreference) {
                newInstance = ListPreferenceDialogFragment.newInstance(preference.getKey());
            } else if (!(preference instanceof MultiSelectListPreference)) {
                throw new IllegalArgumentException("Tried to display dialog for unknown preference type. Did you forget to override onDisplayPreferenceDialog()?");
            } else {
                newInstance = MultiSelectListPreferenceDialogFragment.newInstance(preference.getKey());
            }
            newInstance.setTargetFragment(this, 0);
            newInstance.show(getFragmentManager(), "android.support.v14.preference.PreferenceFragment.DIALOG");
        }
    }

    @Override // android.support.v7.preference.PreferenceManager.OnNavigateToScreenListener
    public void onNavigateToScreen(PreferenceScreen preferenceScreen) {
        boolean z = false;
        if (getCallbackFragment() instanceof OnPreferenceStartScreenCallback) {
            z = ((OnPreferenceStartScreenCallback) getCallbackFragment()).onPreferenceStartScreen(this, preferenceScreen);
        }
        if (z || !(getActivity() instanceof OnPreferenceStartScreenCallback)) {
            return;
        }
        ((OnPreferenceStartScreenCallback) getActivity()).onPreferenceStartScreen(this, preferenceScreen);
    }

    @Override // android.support.v7.preference.PreferenceManager.OnPreferenceTreeClickListener
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference.getFragment() != null) {
            boolean z = false;
            if (getCallbackFragment() instanceof OnPreferenceStartFragmentCallback) {
                z = ((OnPreferenceStartFragmentCallback) getCallbackFragment()).onPreferenceStartFragment(this, preference);
            }
            boolean z2 = z;
            if (!z) {
                z2 = z;
                if (getActivity() instanceof OnPreferenceStartFragmentCallback) {
                    z2 = ((OnPreferenceStartFragmentCallback) getActivity()).onPreferenceStartFragment(this, preference);
                }
            }
            return z2;
        }
        return false;
    }

    @Override // android.app.Fragment
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        if (preferenceScreen != null) {
            Bundle bundle2 = new Bundle();
            preferenceScreen.saveHierarchyState(bundle2);
            bundle.putBundle("android:preferences", bundle2);
        }
    }

    @Override // android.app.Fragment
    public void onStart() {
        super.onStart();
        this.mPreferenceManager.setOnPreferenceTreeClickListener(this);
        this.mPreferenceManager.setOnDisplayPreferenceDialogListener(this);
    }

    @Override // android.app.Fragment
    public void onStop() {
        super.onStop();
        this.mPreferenceManager.setOnPreferenceTreeClickListener(null);
        this.mPreferenceManager.setOnDisplayPreferenceDialogListener(null);
    }

    protected void onUnbindPreferences() {
    }

    @Override // android.app.Fragment
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        if (this.mHavePrefs) {
            bindPreferences();
            if (this.mSelectPreferenceRunnable != null) {
                this.mSelectPreferenceRunnable.run();
                this.mSelectPreferenceRunnable = null;
            }
        }
        this.mInitDone = true;
    }

    public void setDivider(Drawable drawable) {
        this.mDividerDecoration.setDivider(drawable);
    }

    public void setDividerHeight(int i) {
        this.mDividerDecoration.setDividerHeight(i);
    }

    public void setPreferenceScreen(PreferenceScreen preferenceScreen) {
        if (!this.mPreferenceManager.setPreferences(preferenceScreen) || preferenceScreen == null) {
            return;
        }
        onUnbindPreferences();
        this.mHavePrefs = true;
        if (this.mInitDone) {
            postBindPreferences();
        }
    }
}

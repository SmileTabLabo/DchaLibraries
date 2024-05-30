package com.android.settings;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.android.settings.CustomListPreference;
import com.android.settings.RestrictedListPreference;
import com.android.settings.applications.LayoutPreference;
import com.android.settings.core.InstrumentedPreferenceFragment;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settings.search.actionbar.SearchMenuController;
import com.android.settings.support.actionbar.HelpMenuController;
import com.android.settings.support.actionbar.HelpResourceProvider;
import com.android.settings.widget.HighlightablePreferenceGroupAdapter;
import com.android.settings.widget.LoadingViewController;
import com.android.settingslib.CustomDialogPreference;
import com.android.settingslib.CustomEditTextPreference;
import com.android.settingslib.widget.FooterPreferenceMixin;
import java.util.UUID;
/* loaded from: classes.dex */
public abstract class SettingsPreferenceFragment extends InstrumentedPreferenceFragment implements DialogCreatable, HelpResourceProvider {
    private static final int ORDER_FIRST = -1;
    private static final String SAVE_HIGHLIGHTED_KEY = "android:preference_highlighted";
    private static final String TAG = "SettingsPreference";
    public HighlightablePreferenceGroupAdapter mAdapter;
    private boolean mAnimationAllowed;
    private ViewGroup mButtonBar;
    private ContentResolver mContentResolver;
    private RecyclerView.Adapter mCurrentRootAdapter;
    private SettingsDialogFragment mDialogFragment;
    private View mEmptyView;
    private LayoutPreference mHeader;
    private LinearLayoutManager mLayoutManager;
    private ViewGroup mPinnedHeaderFrameLayout;
    private ArrayMap<String, Preference> mPreferenceCache;
    protected final FooterPreferenceMixin mFooterPreferenceMixin = new FooterPreferenceMixin(this, getLifecycle());
    private boolean mIsDataSetObserverRegistered = false;
    private RecyclerView.AdapterDataObserver mDataSetObserver = new RecyclerView.AdapterDataObserver() { // from class: com.android.settings.SettingsPreferenceFragment.1
        @Override // android.support.v7.widget.RecyclerView.AdapterDataObserver
        public void onChanged() {
            SettingsPreferenceFragment.this.onDataSetChanged();
        }

        @Override // android.support.v7.widget.RecyclerView.AdapterDataObserver
        public void onItemRangeChanged(int i, int i2) {
            SettingsPreferenceFragment.this.onDataSetChanged();
        }

        @Override // android.support.v7.widget.RecyclerView.AdapterDataObserver
        public void onItemRangeChanged(int i, int i2, Object obj) {
            SettingsPreferenceFragment.this.onDataSetChanged();
        }

        @Override // android.support.v7.widget.RecyclerView.AdapterDataObserver
        public void onItemRangeInserted(int i, int i2) {
            SettingsPreferenceFragment.this.onDataSetChanged();
        }

        @Override // android.support.v7.widget.RecyclerView.AdapterDataObserver
        public void onItemRangeRemoved(int i, int i2) {
            SettingsPreferenceFragment.this.onDataSetChanged();
        }

        @Override // android.support.v7.widget.RecyclerView.AdapterDataObserver
        public void onItemRangeMoved(int i, int i2, int i3) {
            SettingsPreferenceFragment.this.onDataSetChanged();
        }
    };
    public boolean mPreferenceHighlighted = false;

    @Override // com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        SearchMenuController.init(this);
        HelpMenuController.init(this);
        if (bundle != null) {
            this.mPreferenceHighlighted = bundle.getBoolean(SAVE_HIGHLIGHTED_KEY);
        }
        HighlightablePreferenceGroupAdapter.adjustInitialExpandedChildCount(this);
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View onCreateView = super.onCreateView(layoutInflater, viewGroup, bundle);
        this.mPinnedHeaderFrameLayout = (ViewGroup) onCreateView.findViewById(R.id.pinned_header);
        this.mButtonBar = (ViewGroup) onCreateView.findViewById(R.id.button_bar);
        return onCreateView;
    }

    @Override // com.android.settings.core.InstrumentedPreferenceFragment, android.support.v14.preference.PreferenceFragment
    public void addPreferencesFromResource(int i) {
        super.addPreferencesFromResource(i);
        checkAvailablePrefs(getPreferenceScreen());
    }

    private void checkAvailablePrefs(PreferenceGroup preferenceGroup) {
        if (preferenceGroup == null) {
            return;
        }
        for (int i = 0; i < preferenceGroup.getPreferenceCount(); i++) {
            Preference preference = preferenceGroup.getPreference(i);
            if ((preference instanceof SelfAvailablePreference) && !((SelfAvailablePreference) preference).isAvailable(getContext())) {
                preferenceGroup.removePreference(preference);
            } else if (preference instanceof PreferenceGroup) {
                checkAvailablePrefs((PreferenceGroup) preference);
            }
        }
    }

    public ViewGroup getButtonBar() {
        return this.mButtonBar;
    }

    public View setPinnedHeaderView(int i) {
        View inflate = getActivity().getLayoutInflater().inflate(i, this.mPinnedHeaderFrameLayout, false);
        setPinnedHeaderView(inflate);
        return inflate;
    }

    public void setPinnedHeaderView(View view) {
        this.mPinnedHeaderFrameLayout.addView(view);
        this.mPinnedHeaderFrameLayout.setVisibility(0);
    }

    @Override // com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        if (this.mAdapter != null) {
            bundle.putBoolean(SAVE_HIGHLIGHTED_KEY, this.mAdapter.isHighlightRequested());
        }
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        setHasOptionsMenu(true);
    }

    @Override // com.android.settings.core.InstrumentedPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onResume() {
        super.onResume();
        highlightPreferenceIfNeeded();
    }

    @Override // android.support.v14.preference.PreferenceFragment
    protected void onBindPreferences() {
        registerObserverIfNeeded();
    }

    @Override // android.support.v14.preference.PreferenceFragment
    protected void onUnbindPreferences() {
        unregisterObserverIfNeeded();
    }

    public void setLoading(boolean z, boolean z2) {
        LoadingViewController.handleLoadingContainer(getView().findViewById(R.id.loading_container), getListView(), !z, z2);
    }

    public void registerObserverIfNeeded() {
        if (!this.mIsDataSetObserverRegistered) {
            if (this.mCurrentRootAdapter != null) {
                this.mCurrentRootAdapter.unregisterAdapterDataObserver(this.mDataSetObserver);
            }
            this.mCurrentRootAdapter = getListView().getAdapter();
            this.mCurrentRootAdapter.registerAdapterDataObserver(this.mDataSetObserver);
            this.mIsDataSetObserverRegistered = true;
            onDataSetChanged();
        }
    }

    public void unregisterObserverIfNeeded() {
        if (this.mIsDataSetObserverRegistered) {
            if (this.mCurrentRootAdapter != null) {
                this.mCurrentRootAdapter.unregisterAdapterDataObserver(this.mDataSetObserver);
                this.mCurrentRootAdapter = null;
            }
            this.mIsDataSetObserverRegistered = false;
        }
    }

    public void highlightPreferenceIfNeeded() {
        if (isAdded() && this.mAdapter != null) {
            this.mAdapter.requestHighlight(getView(), getListView());
        }
    }

    public int getInitialExpandedChildCount() {
        return 0;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onDataSetChanged() {
        highlightPreferenceIfNeeded();
        updateEmptyView();
    }

    public LayoutPreference getHeaderView() {
        return this.mHeader;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setHeaderView(int i) {
        this.mHeader = new LayoutPreference(getPrefContext(), i);
        addPreferenceToTop(this.mHeader);
    }

    protected void setHeaderView(View view) {
        this.mHeader = new LayoutPreference(getPrefContext(), view);
        addPreferenceToTop(this.mHeader);
    }

    private void addPreferenceToTop(LayoutPreference layoutPreference) {
        layoutPreference.setOrder(ORDER_FIRST);
        if (getPreferenceScreen() != null) {
            getPreferenceScreen().addPreference(layoutPreference);
        }
    }

    @Override // com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.support.v14.preference.PreferenceFragment
    public void setPreferenceScreen(PreferenceScreen preferenceScreen) {
        if (preferenceScreen != null && !preferenceScreen.isAttached()) {
            preferenceScreen.setShouldUseGeneratedIds(this.mAnimationAllowed);
        }
        super.setPreferenceScreen(preferenceScreen);
        if (preferenceScreen != null && this.mHeader != null) {
            preferenceScreen.addPreference(this.mHeader);
        }
    }

    void updateEmptyView() {
        if (this.mEmptyView == null) {
            return;
        }
        if (getPreferenceScreen() != null) {
            View findViewById = getActivity().findViewById(16908351);
            boolean z = true;
            if ((getPreferenceScreen().getPreferenceCount() - (this.mHeader != null ? 1 : 0)) - (this.mFooterPreferenceMixin.hasFooter() ? 1 : 0) > 0 && (findViewById == null || findViewById.getVisibility() == 0)) {
                z = false;
            }
            this.mEmptyView.setVisibility(z ? 0 : 8);
            return;
        }
        this.mEmptyView.setVisibility(0);
    }

    public void setEmptyView(View view) {
        if (this.mEmptyView != null) {
            this.mEmptyView.setVisibility(8);
        }
        this.mEmptyView = view;
        updateEmptyView();
    }

    public View getEmptyView() {
        return this.mEmptyView;
    }

    @Override // android.support.v14.preference.PreferenceFragment
    public RecyclerView.LayoutManager onCreateLayoutManager() {
        this.mLayoutManager = new LinearLayoutManager(getContext());
        return this.mLayoutManager;
    }

    @Override // android.support.v14.preference.PreferenceFragment
    protected RecyclerView.Adapter onCreateAdapter(PreferenceScreen preferenceScreen) {
        Bundle arguments = getArguments();
        this.mAdapter = new HighlightablePreferenceGroupAdapter(preferenceScreen, arguments == null ? null : arguments.getString(":settings:fragment_args_key"), this.mPreferenceHighlighted);
        return this.mAdapter;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setAnimationAllowed(boolean z) {
        this.mAnimationAllowed = z;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void cacheRemoveAllPrefs(PreferenceGroup preferenceGroup) {
        this.mPreferenceCache = new ArrayMap<>();
        int preferenceCount = preferenceGroup.getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            Preference preference = preferenceGroup.getPreference(i);
            if (!TextUtils.isEmpty(preference.getKey())) {
                this.mPreferenceCache.put(preference.getKey(), preference);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public Preference getCachedPreference(String str) {
        if (this.mPreferenceCache != null) {
            return this.mPreferenceCache.remove(str);
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void removeCachedPrefs(PreferenceGroup preferenceGroup) {
        for (Preference preference : this.mPreferenceCache.values()) {
            preferenceGroup.removePreference(preference);
        }
        this.mPreferenceCache = null;
    }

    protected int getCachedCount() {
        if (this.mPreferenceCache != null) {
            return this.mPreferenceCache.size();
        }
        return 0;
    }

    public boolean removePreference(String str) {
        return removePreference(getPreferenceScreen(), str);
    }

    boolean removePreference(PreferenceGroup preferenceGroup, String str) {
        int preferenceCount = preferenceGroup.getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            Preference preference = preferenceGroup.getPreference(i);
            if (TextUtils.equals(preference.getKey(), str)) {
                return preferenceGroup.removePreference(preference);
            }
            if ((preference instanceof PreferenceGroup) && removePreference((PreferenceGroup) preference, str)) {
                return true;
            }
        }
        return false;
    }

    public final void finishFragment() {
        getActivity().onBackPressed();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public ContentResolver getContentResolver() {
        Activity activity = getActivity();
        if (activity != null) {
            this.mContentResolver = activity.getContentResolver();
        }
        return this.mContentResolver;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public Object getSystemService(String str) {
        return getActivity().getSystemService(str);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public PackageManager getPackageManager() {
        return getActivity().getPackageManager();
    }

    @Override // android.app.Fragment
    public void onDetach() {
        if (isRemoving() && this.mDialogFragment != null) {
            this.mDialogFragment.dismiss();
            this.mDialogFragment = null;
        }
        super.onDetach();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void showDialog(int i) {
        if (this.mDialogFragment != null) {
            Log.e(TAG, "Old dialog fragment not null!");
        }
        this.mDialogFragment = new SettingsDialogFragment(this, i);
        this.mDialogFragment.show(getChildFragmentManager(), Integer.toString(i));
    }

    public Dialog onCreateDialog(int i) {
        return null;
    }

    public int getDialogMetricsCategory(int i) {
        return 0;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void removeDialog(int i) {
        if (this.mDialogFragment != null && this.mDialogFragment.getDialogId() == i) {
            this.mDialogFragment.dismissAllowingStateLoss();
        }
        this.mDialogFragment = null;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setOnCancelListener(DialogInterface.OnCancelListener onCancelListener) {
        if (this.mDialogFragment == null) {
            return;
        }
        this.mDialogFragment.mOnCancelListener = onCancelListener;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        if (this.mDialogFragment == null) {
            return;
        }
        this.mDialogFragment.mOnDismissListener = onDismissListener;
    }

    public void onDialogShowing() {
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.support.v7.preference.PreferenceManager.OnDisplayPreferenceDialogListener
    public void onDisplayPreferenceDialog(Preference preference) {
        DialogFragment newInstance;
        if (preference.getKey() == null) {
            preference.setKey(UUID.randomUUID().toString());
        }
        if (preference instanceof RestrictedListPreference) {
            newInstance = RestrictedListPreference.RestrictedListPreferenceDialogFragment.newInstance(preference.getKey());
        } else if (preference instanceof CustomListPreference) {
            newInstance = CustomListPreference.CustomListPreferenceDialogFragment.newInstance(preference.getKey());
        } else if (preference instanceof CustomDialogPreference) {
            newInstance = CustomDialogPreference.CustomPreferenceDialogFragment.newInstance(preference.getKey());
        } else if (preference instanceof CustomEditTextPreference) {
            newInstance = CustomEditTextPreference.CustomPreferenceDialogFragment.newInstance(preference.getKey());
        } else {
            super.onDisplayPreferenceDialog(preference);
            return;
        }
        newInstance.setTargetFragment(this, 0);
        newInstance.show(getFragmentManager(), "dialog_preference");
        onDialogShowing();
    }

    /* loaded from: classes.dex */
    public static class SettingsDialogFragment extends InstrumentedDialogFragment {
        private DialogInterface.OnCancelListener mOnCancelListener;
        private DialogInterface.OnDismissListener mOnDismissListener;
        private Fragment mParentFragment;

        public SettingsDialogFragment() {
        }

        public SettingsDialogFragment(DialogCreatable dialogCreatable, int i) {
            super(dialogCreatable, i);
            if (!(dialogCreatable instanceof Fragment)) {
                throw new IllegalArgumentException("fragment argument must be an instance of " + Fragment.class.getName());
            }
            this.mParentFragment = (Fragment) dialogCreatable;
        }

        @Override // com.android.settingslib.core.instrumentation.Instrumentable
        public int getMetricsCategory() {
            if (this.mDialogCreatable == null) {
                return 0;
            }
            int dialogMetricsCategory = this.mDialogCreatable.getDialogMetricsCategory(this.mDialogId);
            if (dialogMetricsCategory <= 0) {
                throw new IllegalStateException("Dialog must provide a metrics category");
            }
            return dialogMetricsCategory;
        }

        @Override // android.app.DialogFragment, android.app.Fragment
        public void onSaveInstanceState(Bundle bundle) {
            super.onSaveInstanceState(bundle);
            if (this.mParentFragment != null) {
                bundle.putInt("key_dialog_id", this.mDialogId);
                bundle.putInt("key_parent_fragment_id", this.mParentFragment.getId());
            }
        }

        @Override // com.android.settingslib.core.lifecycle.ObservableDialogFragment, android.app.DialogFragment, android.app.Fragment
        public void onStart() {
            super.onStart();
            if (this.mParentFragment != null && (this.mParentFragment instanceof SettingsPreferenceFragment)) {
                ((SettingsPreferenceFragment) this.mParentFragment).onDialogShowing();
            }
        }

        @Override // android.app.DialogFragment
        public Dialog onCreateDialog(Bundle bundle) {
            Object valueOf;
            if (bundle != null) {
                this.mDialogId = bundle.getInt("key_dialog_id", 0);
                this.mParentFragment = getParentFragment();
                int i = bundle.getInt("key_parent_fragment_id", SettingsPreferenceFragment.ORDER_FIRST);
                if (this.mParentFragment == null) {
                    this.mParentFragment = getFragmentManager().findFragmentById(i);
                }
                if (!(this.mParentFragment instanceof DialogCreatable)) {
                    StringBuilder sb = new StringBuilder();
                    if (this.mParentFragment != null) {
                        valueOf = this.mParentFragment.getClass().getName();
                    } else {
                        valueOf = Integer.valueOf(i);
                    }
                    sb.append(valueOf);
                    sb.append(" must implement ");
                    sb.append(DialogCreatable.class.getName());
                    throw new IllegalArgumentException(sb.toString());
                } else if (this.mParentFragment instanceof SettingsPreferenceFragment) {
                    ((SettingsPreferenceFragment) this.mParentFragment).mDialogFragment = this;
                }
            }
            return ((DialogCreatable) this.mParentFragment).onCreateDialog(this.mDialogId);
        }

        @Override // android.app.DialogFragment, android.content.DialogInterface.OnCancelListener
        public void onCancel(DialogInterface dialogInterface) {
            super.onCancel(dialogInterface);
            if (this.mOnCancelListener != null) {
                this.mOnCancelListener.onCancel(dialogInterface);
            }
        }

        @Override // android.app.DialogFragment, android.content.DialogInterface.OnDismissListener
        public void onDismiss(DialogInterface dialogInterface) {
            super.onDismiss(dialogInterface);
            if (this.mOnDismissListener != null) {
                this.mOnDismissListener.onDismiss(dialogInterface);
            }
        }

        public int getDialogId() {
            return this.mDialogId;
        }

        @Override // android.app.DialogFragment, android.app.Fragment
        public void onDetach() {
            super.onDetach();
            if ((this.mParentFragment instanceof SettingsPreferenceFragment) && ((SettingsPreferenceFragment) this.mParentFragment).mDialogFragment == this) {
                ((SettingsPreferenceFragment) this.mParentFragment).mDialogFragment = null;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean hasNextButton() {
        return ((ButtonBarHandler) getActivity()).hasNextButton();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public Button getNextButton() {
        return ((ButtonBarHandler) getActivity()).getNextButton();
    }

    public void finish() {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            activity.finish();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public Intent getIntent() {
        if (getActivity() == null) {
            return null;
        }
        return getActivity().getIntent();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setResult(int i, Intent intent) {
        if (getActivity() == null) {
            return;
        }
        getActivity().setResult(i, intent);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setResult(int i) {
        if (getActivity() == null) {
            return;
        }
        getActivity().setResult(i);
    }
}

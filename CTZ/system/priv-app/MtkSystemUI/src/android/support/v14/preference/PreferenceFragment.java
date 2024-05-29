package android.support.v14.preference;

import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroupAdapter;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceRecyclerViewAccessibilityDelegate;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.PreferenceViewHolder;
import android.support.v7.preference.R;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
/* loaded from: classes.dex */
public abstract class PreferenceFragment extends Fragment implements DialogPreference.TargetFragment, PreferenceManager.OnDisplayPreferenceDialogListener, PreferenceManager.OnNavigateToScreenListener, PreferenceManager.OnPreferenceTreeClickListener {
    private boolean mHavePrefs;
    private boolean mInitDone;
    private RecyclerView mList;
    private PreferenceManager mPreferenceManager;
    private Runnable mSelectPreferenceRunnable;
    private Context mStyledContext;
    private int mLayoutResId = R.layout.preference_list_fragment;
    private final DividerDecoration mDividerDecoration = new DividerDecoration();
    private final Handler mHandler = new Handler() { // from class: android.support.v14.preference.PreferenceFragment.1
        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                PreferenceFragment.this.bindPreferences();
            }
        }
    };
    private final Runnable mRequestFocus = new Runnable() { // from class: android.support.v14.preference.PreferenceFragment.2
        @Override // java.lang.Runnable
        public void run() {
            PreferenceFragment.this.mList.focusableViewAvailable(PreferenceFragment.this.mList);
        }
    };

    /* loaded from: classes.dex */
    public interface OnPreferenceDisplayDialogCallback {
        boolean onPreferenceDisplayDialog(PreferenceFragment preferenceFragment, Preference preference);
    }

    /* loaded from: classes.dex */
    public interface OnPreferenceStartFragmentCallback {
        boolean onPreferenceStartFragment(PreferenceFragment preferenceFragment, Preference preference);
    }

    /* loaded from: classes.dex */
    public interface OnPreferenceStartScreenCallback {
        boolean onPreferenceStartScreen(PreferenceFragment preferenceFragment, PreferenceScreen preferenceScreen);
    }

    public abstract void onCreatePreferences(Bundle bundle, String str);

    @Override // android.app.Fragment
    public void onCreate(Bundle savedInstanceState) {
        String rootKey;
        super.onCreate(savedInstanceState);
        TypedValue tv = new TypedValue();
        getActivity().getTheme().resolveAttribute(R.attr.preferenceTheme, tv, true);
        int theme = tv.resourceId;
        if (theme == 0) {
            throw new IllegalStateException("Must specify preferenceTheme in theme");
        }
        this.mStyledContext = new ContextThemeWrapper(getActivity(), theme);
        this.mPreferenceManager = new PreferenceManager(this.mStyledContext);
        this.mPreferenceManager.setOnNavigateToScreenListener(this);
        Bundle args = getArguments();
        if (args != null) {
            rootKey = getArguments().getString("android.support.v7.preference.PreferenceFragmentCompat.PREFERENCE_ROOT");
        } else {
            rootKey = null;
        }
        onCreatePreferences(savedInstanceState, rootKey);
    }

    @Override // android.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        TypedArray a = this.mStyledContext.obtainStyledAttributes(null, R.styleable.PreferenceFragment, TypedArrayUtils.getAttr(this.mStyledContext, R.attr.preferenceFragmentStyle, 16844038), 0);
        this.mLayoutResId = a.getResourceId(R.styleable.PreferenceFragment_android_layout, this.mLayoutResId);
        Drawable divider = a.getDrawable(R.styleable.PreferenceFragment_android_divider);
        int dividerHeight = a.getDimensionPixelSize(R.styleable.PreferenceFragment_android_dividerHeight, -1);
        boolean allowDividerAfterLastItem = a.getBoolean(R.styleable.PreferenceFragment_allowDividerAfterLastItem, true);
        a.recycle();
        TypedValue tv = new TypedValue();
        getActivity().getTheme().resolveAttribute(R.attr.preferenceTheme, tv, true);
        int theme = tv.resourceId;
        Context themedContext = new ContextThemeWrapper(inflater.getContext(), theme);
        LayoutInflater themedInflater = inflater.cloneInContext(themedContext);
        View view = themedInflater.inflate(this.mLayoutResId, container, false);
        View rawListContainer = view.findViewById(16908351);
        if (rawListContainer instanceof ViewGroup) {
            ViewGroup listContainer = (ViewGroup) rawListContainer;
            RecyclerView listView = onCreateRecyclerView(themedInflater, listContainer, savedInstanceState);
            if (listView != null) {
                this.mList = listView;
                listView.addItemDecoration(this.mDividerDecoration);
                setDivider(divider);
                if (dividerHeight != -1) {
                    setDividerHeight(dividerHeight);
                }
                this.mDividerDecoration.setAllowDividerAfterLastItem(allowDividerAfterLastItem);
                listContainer.addView(this.mList);
                this.mHandler.post(this.mRequestFocus);
                return view;
            }
            throw new RuntimeException("Could not create RecyclerView");
        }
        throw new RuntimeException("Content has view with id attribute 'android.R.id.list_container' that is not a ViewGroup class");
    }

    public void setDivider(Drawable divider) {
        this.mDividerDecoration.setDivider(divider);
    }

    public void setDividerHeight(int height) {
        this.mDividerDecoration.setDividerHeight(height);
    }

    @Override // android.app.Fragment
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (this.mHavePrefs) {
            bindPreferences();
            if (this.mSelectPreferenceRunnable != null) {
                this.mSelectPreferenceRunnable.run();
                this.mSelectPreferenceRunnable = null;
            }
        }
        this.mInitDone = true;
    }

    @Override // android.app.Fragment
    public void onActivityCreated(Bundle savedInstanceState) {
        Bundle container;
        PreferenceScreen preferenceScreen;
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null && (container = savedInstanceState.getBundle("android:preferences")) != null && (preferenceScreen = getPreferenceScreen()) != null) {
            preferenceScreen.restoreHierarchyState(container);
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

    @Override // android.app.Fragment
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        if (preferenceScreen != null) {
            Bundle container = new Bundle();
            preferenceScreen.saveHierarchyState(container);
            outState.putBundle("android:preferences", container);
        }
    }

    public PreferenceManager getPreferenceManager() {
        return this.mPreferenceManager;
    }

    public void setPreferenceScreen(PreferenceScreen preferenceScreen) {
        if (this.mPreferenceManager.setPreferences(preferenceScreen) && preferenceScreen != null) {
            onUnbindPreferences();
            this.mHavePrefs = true;
            if (this.mInitDone) {
                postBindPreferences();
            }
        }
    }

    public PreferenceScreen getPreferenceScreen() {
        return this.mPreferenceManager.getPreferenceScreen();
    }

    public void addPreferencesFromResource(int preferencesResId) {
        requirePreferenceManager();
        setPreferenceScreen(this.mPreferenceManager.inflateFromResource(this.mStyledContext, preferencesResId, getPreferenceScreen()));
    }

    @Override // android.support.v7.preference.PreferenceManager.OnPreferenceTreeClickListener
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference.getFragment() != null) {
            boolean handled = false;
            if (getCallbackFragment() instanceof OnPreferenceStartFragmentCallback) {
                handled = ((OnPreferenceStartFragmentCallback) getCallbackFragment()).onPreferenceStartFragment(this, preference);
            }
            if (!handled && (getActivity() instanceof OnPreferenceStartFragmentCallback)) {
                boolean handled2 = ((OnPreferenceStartFragmentCallback) getActivity()).onPreferenceStartFragment(this, preference);
                return handled2;
            }
            return handled;
        }
        return false;
    }

    @Override // android.support.v7.preference.PreferenceManager.OnNavigateToScreenListener
    public void onNavigateToScreen(PreferenceScreen preferenceScreen) {
        boolean handled = false;
        if (getCallbackFragment() instanceof OnPreferenceStartScreenCallback) {
            handled = ((OnPreferenceStartScreenCallback) getCallbackFragment()).onPreferenceStartScreen(this, preferenceScreen);
        }
        if (!handled && (getActivity() instanceof OnPreferenceStartScreenCallback)) {
            ((OnPreferenceStartScreenCallback) getActivity()).onPreferenceStartScreen(this, preferenceScreen);
        }
    }

    @Override // android.support.v7.preference.DialogPreference.TargetFragment
    public Preference findPreference(CharSequence key) {
        if (this.mPreferenceManager == null) {
            return null;
        }
        return this.mPreferenceManager.findPreference(key);
    }

    private void requirePreferenceManager() {
        if (this.mPreferenceManager == null) {
            throw new RuntimeException("This should be called after super.onCreate.");
        }
    }

    private void postBindPreferences() {
        if (this.mHandler.hasMessages(1)) {
            return;
        }
        this.mHandler.obtainMessage(1).sendToTarget();
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

    private void unbindPreferences() {
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        if (preferenceScreen != null) {
            preferenceScreen.onDetached();
        }
        onUnbindPreferences();
    }

    protected void onBindPreferences() {
    }

    protected void onUnbindPreferences() {
    }

    public final RecyclerView getListView() {
        return this.mList;
    }

    public RecyclerView onCreateRecyclerView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        RecyclerView recyclerView = (RecyclerView) inflater.inflate(R.layout.preference_recyclerview, parent, false);
        recyclerView.setLayoutManager(onCreateLayoutManager());
        recyclerView.setAccessibilityDelegateCompat(new PreferenceRecyclerViewAccessibilityDelegate(recyclerView));
        return recyclerView;
    }

    public RecyclerView.LayoutManager onCreateLayoutManager() {
        return new LinearLayoutManager(getActivity());
    }

    protected RecyclerView.Adapter onCreateAdapter(PreferenceScreen preferenceScreen) {
        return new PreferenceGroupAdapter(preferenceScreen);
    }

    @Override // android.support.v7.preference.PreferenceManager.OnDisplayPreferenceDialogListener
    public void onDisplayPreferenceDialog(Preference preference) {
        DialogFragment f;
        boolean handled = false;
        if (getCallbackFragment() instanceof OnPreferenceDisplayDialogCallback) {
            handled = ((OnPreferenceDisplayDialogCallback) getCallbackFragment()).onPreferenceDisplayDialog(this, preference);
        }
        if (!handled && (getActivity() instanceof OnPreferenceDisplayDialogCallback)) {
            handled = ((OnPreferenceDisplayDialogCallback) getActivity()).onPreferenceDisplayDialog(this, preference);
        }
        if (handled || getFragmentManager().findFragmentByTag("android.support.v14.preference.PreferenceFragment.DIALOG") != null) {
            return;
        }
        if (preference instanceof EditTextPreference) {
            f = EditTextPreferenceDialogFragment.newInstance(preference.getKey());
        } else if (preference instanceof ListPreference) {
            f = ListPreferenceDialogFragment.newInstance(preference.getKey());
        } else if (preference instanceof MultiSelectListPreference) {
            f = MultiSelectListPreferenceDialogFragment.newInstance(preference.getKey());
        } else {
            throw new IllegalArgumentException("Tried to display dialog for unknown preference type. Did you forget to override onDisplayPreferenceDialog()?");
        }
        f.setTargetFragment(this, 0);
        f.show(getFragmentManager(), "android.support.v14.preference.PreferenceFragment.DIALOG");
    }

    public Fragment getCallbackFragment() {
        return null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class DividerDecoration extends RecyclerView.ItemDecoration {
        private boolean mAllowDividerAfterLastItem;
        private Drawable mDivider;
        private int mDividerHeight;

        private DividerDecoration() {
            this.mAllowDividerAfterLastItem = true;
        }

        @Override // android.support.v7.widget.RecyclerView.ItemDecoration
        public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
            if (this.mDivider == null) {
                return;
            }
            int childCount = parent.getChildCount();
            int width = parent.getWidth();
            for (int childViewIndex = 0; childViewIndex < childCount; childViewIndex++) {
                View view = parent.getChildAt(childViewIndex);
                if (shouldDrawDividerBelow(view, parent)) {
                    int top = ((int) view.getY()) + view.getHeight();
                    this.mDivider.setBounds(0, top, width, this.mDividerHeight + top);
                    this.mDivider.draw(c);
                }
            }
        }

        @Override // android.support.v7.widget.RecyclerView.ItemDecoration
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            if (shouldDrawDividerBelow(view, parent)) {
                outRect.bottom = this.mDividerHeight;
            }
        }

        private boolean shouldDrawDividerBelow(View view, RecyclerView parent) {
            RecyclerView.ViewHolder holder = parent.getChildViewHolder(view);
            boolean z = false;
            boolean dividerAllowedBelow = (holder instanceof PreferenceViewHolder) && ((PreferenceViewHolder) holder).isDividerAllowedBelow();
            if (dividerAllowedBelow) {
                boolean nextAllowed = this.mAllowDividerAfterLastItem;
                int index = parent.indexOfChild(view);
                if (index < parent.getChildCount() - 1) {
                    View nextView = parent.getChildAt(index + 1);
                    RecyclerView.ViewHolder nextHolder = parent.getChildViewHolder(nextView);
                    if ((nextHolder instanceof PreferenceViewHolder) && ((PreferenceViewHolder) nextHolder).isDividerAllowedAbove()) {
                        z = true;
                    }
                    boolean nextAllowed2 = z;
                    return nextAllowed2;
                }
                return nextAllowed;
            }
            return false;
        }

        public void setDivider(Drawable divider) {
            if (divider != null) {
                this.mDividerHeight = divider.getIntrinsicHeight();
            } else {
                this.mDividerHeight = 0;
            }
            this.mDivider = divider;
            PreferenceFragment.this.mList.invalidateItemDecorations();
        }

        public void setDividerHeight(int dividerHeight) {
            this.mDividerHeight = dividerHeight;
            PreferenceFragment.this.mList.invalidateItemDecorations();
        }

        public void setAllowDividerAfterLastItem(boolean allowDividerAfterLastItem) {
            this.mAllowDividerAfterLastItem = allowDividerAfterLastItem;
        }
    }
}

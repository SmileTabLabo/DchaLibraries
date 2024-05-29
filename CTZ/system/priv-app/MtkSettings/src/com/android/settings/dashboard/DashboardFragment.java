package com.android.settings.dashboard;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.core.PreferenceControllerListHelper;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.search.Indexable;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.drawer.DashboardCategory;
import com.android.settingslib.drawer.SettingsDrawerActivity;
import com.android.settingslib.drawer.Tile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
/* loaded from: classes.dex */
public abstract class DashboardFragment extends SettingsPreferenceFragment implements SummaryLoader.SummaryConsumer, Indexable, SettingsDrawerActivity.CategoryListener {
    private DashboardFeatureProvider mDashboardFeatureProvider;
    private boolean mListeningToCategoryChange;
    private DashboardTilePlaceholderPreferenceController mPlaceholderPreferenceController;
    private SummaryLoader mSummaryLoader;
    private final Map<Class, List<AbstractPreferenceController>> mPreferenceControllers = new ArrayMap();
    private final Set<String> mDashboardTilePrefKeys = new ArraySet();

    protected abstract String getLogTag();

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.core.InstrumentedPreferenceFragment
    public abstract int getPreferenceScreenResId();

    @Override // com.android.settings.core.InstrumentedPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mDashboardFeatureProvider = FeatureFactory.getFactory(context).getDashboardFeatureProvider(context);
        ArrayList<AbstractPreferenceController> arrayList = new ArrayList();
        List<AbstractPreferenceController> createPreferenceControllers = createPreferenceControllers(context);
        List<BasePreferenceController> filterControllers = PreferenceControllerListHelper.filterControllers(PreferenceControllerListHelper.getPreferenceControllersFromXml(context, getPreferenceScreenResId()), createPreferenceControllers);
        if (createPreferenceControllers != null) {
            arrayList.addAll(createPreferenceControllers);
        }
        arrayList.addAll(filterControllers);
        final Lifecycle lifecycle = getLifecycle();
        filterControllers.stream().filter(new Predicate() { // from class: com.android.settings.dashboard.-$$Lambda$DashboardFragment$S-iRpeKDC_3jmfXOTbVaWpa8f5Y
            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return DashboardFragment.lambda$onAttach$0((BasePreferenceController) obj);
            }
        }).forEach(new Consumer() { // from class: com.android.settings.dashboard.-$$Lambda$DashboardFragment$iYpWkssUBFPuOKWOC_GeIjRUfdk
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                Lifecycle.this.addObserver((LifecycleObserver) ((BasePreferenceController) obj));
            }
        });
        this.mPlaceholderPreferenceController = new DashboardTilePlaceholderPreferenceController(context);
        arrayList.add(this.mPlaceholderPreferenceController);
        for (AbstractPreferenceController abstractPreferenceController : arrayList) {
            addPreferenceController(abstractPreferenceController);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ boolean lambda$onAttach$0(BasePreferenceController basePreferenceController) {
        return basePreferenceController instanceof LifecycleObserver;
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getPreferenceManager().setPreferenceComparisonCallback(new PreferenceManager.SimplePreferenceComparisonCallback());
        if (bundle != null) {
            updatePreferenceStates();
        }
    }

    @Override // com.android.settingslib.drawer.SettingsDrawerActivity.CategoryListener
    public void onCategoriesChanged() {
        if (this.mDashboardFeatureProvider.getTilesForCategory(getCategoryKey()) == null) {
            return;
        }
        refreshDashboardTiles(getLogTag());
    }

    @Override // com.android.settings.core.InstrumentedPreferenceFragment, android.support.v14.preference.PreferenceFragment
    public void onCreatePreferences(Bundle bundle, String str) {
        refreshAllPreferences(getLogTag());
    }

    @Override // com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onStart() {
        super.onStart();
        if (this.mDashboardFeatureProvider.getTilesForCategory(getCategoryKey()) == null) {
            return;
        }
        if (this.mSummaryLoader != null) {
            this.mSummaryLoader.setListening(true);
        }
        Activity activity = getActivity();
        if (activity instanceof SettingsDrawerActivity) {
            this.mListeningToCategoryChange = true;
            ((SettingsDrawerActivity) activity).addCategoryListener(this);
        }
    }

    @Override // com.android.settings.dashboard.SummaryLoader.SummaryConsumer
    public void notifySummaryChanged(Tile tile) {
        String dashboardKeyForTile = this.mDashboardFeatureProvider.getDashboardKeyForTile(tile);
        Preference findPreference = getPreferenceScreen().findPreference(dashboardKeyForTile);
        if (findPreference == null) {
            Log.d(getLogTag(), String.format("Can't find pref by key %s, skipping update summary %s/%s", dashboardKeyForTile, tile.title, tile.summary));
        } else {
            findPreference.setSummary(tile.summary);
        }
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settings.core.InstrumentedPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onResume() {
        super.onResume();
        updatePreferenceStates();
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.support.v7.preference.PreferenceManager.OnPreferenceTreeClickListener
    public boolean onPreferenceTreeClick(Preference preference) {
        Collection<List<AbstractPreferenceController>> values = this.mPreferenceControllers.values();
        this.mMetricsFeatureProvider.logDashboardStartIntent(getContext(), preference.getIntent(), getMetricsCategory());
        for (List<AbstractPreferenceController> list : values) {
            for (AbstractPreferenceController abstractPreferenceController : list) {
                if (abstractPreferenceController.handlePreferenceTreeClick(preference)) {
                    return true;
                }
            }
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override // com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onStop() {
        super.onStop();
        if (this.mSummaryLoader != null) {
            this.mSummaryLoader.setListening(false);
        }
        if (this.mListeningToCategoryChange) {
            Activity activity = getActivity();
            if (activity instanceof SettingsDrawerActivity) {
                ((SettingsDrawerActivity) activity).remCategoryListener(this);
            }
            this.mListeningToCategoryChange = false;
        }
    }

    @Override // com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onDestroy() {
        super.onDestroy();
        if (this.mSummaryLoader != null) {
            this.mSummaryLoader.release();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public <T extends AbstractPreferenceController> T use(Class<T> cls) {
        List<AbstractPreferenceController> list = this.mPreferenceControllers.get(cls);
        if (list != null) {
            if (list.size() > 1) {
                Log.w("DashboardFragment", "Multiple controllers of Class " + cls.getSimpleName() + " found, returning first one.");
            }
            return (T) list.get(0);
        }
        return null;
    }

    protected void addPreferenceController(AbstractPreferenceController abstractPreferenceController) {
        if (this.mPreferenceControllers.get(abstractPreferenceController.getClass()) == null) {
            this.mPreferenceControllers.put(abstractPreferenceController.getClass(), new ArrayList());
        }
        this.mPreferenceControllers.get(abstractPreferenceController.getClass()).add(abstractPreferenceController);
    }

    public String getCategoryKey() {
        return DashboardFragmentRegistry.PARENT_TO_CATEGORY_KEY_MAP.get(getClass().getName());
    }

    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return null;
    }

    protected boolean displayTile(Tile tile) {
        return true;
    }

    boolean tintTileIcon(Tile tile) {
        if (tile.icon == null) {
            return false;
        }
        Bundle bundle = tile.metaData;
        if (bundle != null && bundle.containsKey("com.android.settings.icon_tintable")) {
            return bundle.getBoolean("com.android.settings.icon_tintable");
        }
        String packageName = getContext().getPackageName();
        return (packageName == null || tile.intent == null || packageName.equals(tile.intent.getComponent().getPackageName())) ? false : true;
    }

    private void displayResourceTiles() {
        int preferenceScreenResId = getPreferenceScreenResId();
        if (preferenceScreenResId <= 0) {
            return;
        }
        addPreferencesFromResource(preferenceScreenResId);
        final PreferenceScreen preferenceScreen = getPreferenceScreen();
        this.mPreferenceControllers.values().stream().flatMap(new Function() { // from class: com.android.settings.dashboard.-$$Lambda$seyL25CSW2NInOydsTbSDrNW6pM
            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return ((List) obj).stream();
            }
        }).forEach(new Consumer() { // from class: com.android.settings.dashboard.-$$Lambda$DashboardFragment$wmCpqAavTrPCWLW0gqd6-3n9DOU
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ((AbstractPreferenceController) obj).displayPreference(PreferenceScreen.this);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void updatePreferenceStates() {
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        for (List<AbstractPreferenceController> list : this.mPreferenceControllers.values()) {
            for (AbstractPreferenceController abstractPreferenceController : list) {
                if (abstractPreferenceController.isAvailable()) {
                    String preferenceKey = abstractPreferenceController.getPreferenceKey();
                    Preference findPreference = preferenceScreen.findPreference(preferenceKey);
                    if (findPreference == null) {
                        Log.d("DashboardFragment", String.format("Cannot find preference with key %s in Controller %s", preferenceKey, abstractPreferenceController.getClass().getSimpleName()));
                    } else {
                        abstractPreferenceController.updateState(findPreference);
                    }
                }
            }
        }
    }

    private void refreshAllPreferences(String str) {
        if (getPreferenceScreen() != null) {
            getPreferenceScreen().removeAll();
        }
        displayResourceTiles();
        refreshDashboardTiles(str);
    }

    void refreshDashboardTiles(String str) {
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        DashboardCategory tilesForCategory = this.mDashboardFeatureProvider.getTilesForCategory(getCategoryKey());
        if (tilesForCategory == null) {
            Log.d(str, "NO dashboard tiles for " + str);
            return;
        }
        List<Tile> tiles = tilesForCategory.getTiles();
        if (tiles == null) {
            Log.d(str, "tile list is empty, skipping category " + ((Object) tilesForCategory.title));
            return;
        }
        ArrayList<String> arrayList = new ArrayList(this.mDashboardTilePrefKeys);
        if (this.mSummaryLoader != null) {
            this.mSummaryLoader.release();
        }
        Context context = getContext();
        this.mSummaryLoader = new SummaryLoader(getActivity(), getCategoryKey());
        this.mSummaryLoader.setSummaryConsumer(this);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(new int[]{16843817});
        int color = obtainStyledAttributes.getColor(0, context.getColor(17170443));
        obtainStyledAttributes.recycle();
        for (Tile tile : tiles) {
            String dashboardKeyForTile = this.mDashboardFeatureProvider.getDashboardKeyForTile(tile);
            if (TextUtils.isEmpty(dashboardKeyForTile)) {
                Log.d(str, "tile does not contain a key, skipping " + tile);
            } else if (displayTile(tile)) {
                if (tintTileIcon(tile)) {
                    tile.icon.setTint(color);
                }
                if (this.mDashboardTilePrefKeys.contains(dashboardKeyForTile)) {
                    this.mDashboardFeatureProvider.bindPreferenceToTile(getActivity(), getMetricsCategory(), preferenceScreen.findPreference(dashboardKeyForTile), tile, dashboardKeyForTile, this.mPlaceholderPreferenceController.getOrder());
                } else {
                    Preference preference = new Preference(getPrefContext());
                    this.mDashboardFeatureProvider.bindPreferenceToTile(getActivity(), getMetricsCategory(), preference, tile, dashboardKeyForTile, this.mPlaceholderPreferenceController.getOrder());
                    preferenceScreen.addPreference(preference);
                    this.mDashboardTilePrefKeys.add(dashboardKeyForTile);
                }
                arrayList.remove(dashboardKeyForTile);
            }
        }
        for (String str2 : arrayList) {
            this.mDashboardTilePrefKeys.remove(str2);
            Preference findPreference = preferenceScreen.findPreference(str2);
            if (findPreference != null) {
                preferenceScreen.removePreference(findPreference);
            }
        }
        this.mSummaryLoader.setListening(true);
    }
}

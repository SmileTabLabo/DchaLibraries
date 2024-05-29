package android.support.v7.preference;

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v7.preference.Preference;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;
/* loaded from: a.zip:android/support/v7/preference/PreferenceGroupAdapter.class */
public class PreferenceGroupAdapter extends RecyclerView.Adapter<PreferenceViewHolder> implements Preference.OnPreferenceChangeInternalListener {
    private PreferenceGroup mPreferenceGroup;
    private List<PreferenceLayout> mPreferenceLayouts;
    private List<Preference> mPreferenceList;
    private List<Preference> mPreferenceListInternal;
    private PreferenceLayout mTempPreferenceLayout = new PreferenceLayout();
    private Handler mHandler = new Handler();
    private Runnable mSyncRunnable = new Runnable(this) { // from class: android.support.v7.preference.PreferenceGroupAdapter.1
        final PreferenceGroupAdapter this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            this.this$0.syncMyPreferences();
        }
    };

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:android/support/v7/preference/PreferenceGroupAdapter$PreferenceLayout.class */
    public static class PreferenceLayout {
        private String name;
        private int resId;
        private int widgetResId;

        public PreferenceLayout() {
        }

        public PreferenceLayout(PreferenceLayout preferenceLayout) {
            this.resId = preferenceLayout.resId;
            this.widgetResId = preferenceLayout.widgetResId;
            this.name = preferenceLayout.name;
        }

        public boolean equals(Object obj) {
            if (obj instanceof PreferenceLayout) {
                PreferenceLayout preferenceLayout = (PreferenceLayout) obj;
                boolean z = false;
                if (this.resId == preferenceLayout.resId) {
                    z = false;
                    if (this.widgetResId == preferenceLayout.widgetResId) {
                        z = TextUtils.equals(this.name, preferenceLayout.name);
                    }
                }
                return z;
            }
            return false;
        }

        public int hashCode() {
            return ((((this.resId + 527) * 31) + this.widgetResId) * 31) + this.name.hashCode();
        }
    }

    public PreferenceGroupAdapter(PreferenceGroup preferenceGroup) {
        this.mPreferenceGroup = preferenceGroup;
        this.mPreferenceGroup.setOnPreferenceChangeInternalListener(this);
        this.mPreferenceList = new ArrayList();
        this.mPreferenceListInternal = new ArrayList();
        this.mPreferenceLayouts = new ArrayList();
        if (this.mPreferenceGroup instanceof PreferenceScreen) {
            setHasStableIds(((PreferenceScreen) this.mPreferenceGroup).shouldUseGeneratedIds());
        } else {
            setHasStableIds(true);
        }
        syncMyPreferences();
    }

    private void addPreferenceClassName(Preference preference) {
        PreferenceLayout createPreferenceLayout = createPreferenceLayout(preference, null);
        if (this.mPreferenceLayouts.contains(createPreferenceLayout)) {
            return;
        }
        this.mPreferenceLayouts.add(createPreferenceLayout);
    }

    private PreferenceLayout createPreferenceLayout(Preference preference, PreferenceLayout preferenceLayout) {
        if (preferenceLayout == null) {
            preferenceLayout = new PreferenceLayout();
        }
        preferenceLayout.name = preference.getClass().getName();
        preferenceLayout.resId = preference.getLayoutResource();
        preferenceLayout.widgetResId = preference.getWidgetLayoutResource();
        return preferenceLayout;
    }

    private void flattenPreferenceGroup(List<Preference> list, PreferenceGroup preferenceGroup) {
        preferenceGroup.sortPreferences();
        int preferenceCount = preferenceGroup.getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            Preference preference = preferenceGroup.getPreference(i);
            list.add(preference);
            addPreferenceClassName(preference);
            if (preference instanceof PreferenceGroup) {
                PreferenceGroup preferenceGroup2 = (PreferenceGroup) preference;
                if (preferenceGroup2.isOnSameScreenAsChildren()) {
                    flattenPreferenceGroup(list, preferenceGroup2);
                }
            }
            preference.setOnPreferenceChangeInternalListener(this);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void syncMyPreferences() {
        ArrayList<Preference> arrayList = new ArrayList(this.mPreferenceListInternal.size());
        flattenPreferenceGroup(arrayList, this.mPreferenceGroup);
        ArrayList arrayList2 = new ArrayList(arrayList.size());
        for (Preference preference : arrayList) {
            if (preference.isVisible()) {
                arrayList2.add(preference);
            }
        }
        this.mPreferenceList = arrayList2;
        this.mPreferenceListInternal = arrayList;
        notifyDataSetChanged();
    }

    public Preference getItem(int i) {
        if (i < 0 || i >= getItemCount()) {
            return null;
        }
        return this.mPreferenceList.get(i);
    }

    @Override // android.support.v7.widget.RecyclerView.Adapter
    public int getItemCount() {
        return this.mPreferenceList.size();
    }

    @Override // android.support.v7.widget.RecyclerView.Adapter
    public long getItemId(int i) {
        if (hasStableIds()) {
            return getItem(i).getId();
        }
        return -1L;
    }

    @Override // android.support.v7.widget.RecyclerView.Adapter
    public int getItemViewType(int i) {
        this.mTempPreferenceLayout = createPreferenceLayout(getItem(i), this.mTempPreferenceLayout);
        int indexOf = this.mPreferenceLayouts.indexOf(this.mTempPreferenceLayout);
        if (indexOf != -1) {
            return indexOf;
        }
        int size = this.mPreferenceLayouts.size();
        this.mPreferenceLayouts.add(new PreferenceLayout(this.mTempPreferenceLayout));
        return size;
    }

    @Override // android.support.v7.widget.RecyclerView.Adapter
    public void onBindViewHolder(PreferenceViewHolder preferenceViewHolder, int i) {
        getItem(i).onBindViewHolder(preferenceViewHolder);
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // android.support.v7.widget.RecyclerView.Adapter
    public PreferenceViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        PreferenceLayout preferenceLayout = this.mPreferenceLayouts.get(i);
        LayoutInflater from = LayoutInflater.from(viewGroup.getContext());
        TypedArray obtainStyledAttributes = viewGroup.getContext().obtainStyledAttributes((AttributeSet) null, R$styleable.BackgroundStyle);
        Drawable drawable = obtainStyledAttributes.getDrawable(R$styleable.BackgroundStyle_android_selectableItemBackground);
        Drawable drawable2 = drawable;
        if (drawable == null) {
            drawable2 = viewGroup.getContext().getResources().getDrawable(17301602);
        }
        obtainStyledAttributes.recycle();
        View inflate = from.inflate(preferenceLayout.resId, viewGroup, false);
        inflate.setBackgroundDrawable(drawable2);
        ViewGroup viewGroup2 = (ViewGroup) inflate.findViewById(16908312);
        if (viewGroup2 != null) {
            if (preferenceLayout.widgetResId != 0) {
                from.inflate(preferenceLayout.widgetResId, viewGroup2);
            } else {
                viewGroup2.setVisibility(8);
            }
        }
        return new PreferenceViewHolder(inflate);
    }

    @Override // android.support.v7.preference.Preference.OnPreferenceChangeInternalListener
    public void onPreferenceChange(Preference preference) {
        int indexOf = this.mPreferenceList.indexOf(preference);
        if (indexOf != -1) {
            notifyItemChanged(indexOf, preference);
        }
    }

    @Override // android.support.v7.preference.Preference.OnPreferenceChangeInternalListener
    public void onPreferenceHierarchyChange(Preference preference) {
        this.mHandler.removeCallbacks(this.mSyncRunnable);
        this.mHandler.post(this.mSyncRunnable);
    }
}

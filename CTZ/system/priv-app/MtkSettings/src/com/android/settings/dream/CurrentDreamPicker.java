package com.android.settings.dream;

import android.content.ComponentName;
import android.content.Context;
import android.graphics.drawable.Drawable;
import com.android.settings.R;
import com.android.settings.dream.CurrentDreamPicker;
import com.android.settings.widget.RadioButtonPickerFragment;
import com.android.settingslib.dream.DreamBackend;
import com.android.settingslib.widget.CandidateInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
/* loaded from: classes.dex */
public final class CurrentDreamPicker extends RadioButtonPickerFragment {
    private DreamBackend mBackend;

    @Override // com.android.settings.widget.RadioButtonPickerFragment, com.android.settings.core.InstrumentedPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mBackend = DreamBackend.getInstance(context);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.widget.RadioButtonPickerFragment, com.android.settings.core.InstrumentedPreferenceFragment
    public int getPreferenceScreenResId() {
        return R.xml.current_dream_settings;
    }

    @Override // com.android.settingslib.core.instrumentation.Instrumentable
    public int getMetricsCategory() {
        return 47;
    }

    @Override // com.android.settings.widget.RadioButtonPickerFragment
    protected boolean setDefaultKey(String str) {
        Map<String, ComponentName> dreamComponentsMap = getDreamComponentsMap();
        if (dreamComponentsMap.get(str) != null) {
            this.mBackend.setActiveDream(dreamComponentsMap.get(str));
            return true;
        }
        return false;
    }

    @Override // com.android.settings.widget.RadioButtonPickerFragment
    protected String getDefaultKey() {
        return this.mBackend.getActiveDream().flattenToString();
    }

    @Override // com.android.settings.widget.RadioButtonPickerFragment
    protected List<? extends CandidateInfo> getCandidates() {
        return (List) this.mBackend.getDreamInfos().stream().map(new Function() { // from class: com.android.settings.dream.-$$Lambda$hBSizG3ais67bSjAeIqNEa6sDBo
            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return new CurrentDreamPicker.DreamCandidateInfo((DreamBackend.DreamInfo) obj);
            }
        }).collect(Collectors.toList());
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.widget.RadioButtonPickerFragment
    public void onSelectionPerformed(boolean z) {
        super.onSelectionPerformed(z);
        getActivity().finish();
    }

    private Map<String, ComponentName> getDreamComponentsMap() {
        final HashMap hashMap = new HashMap();
        this.mBackend.getDreamInfos().forEach(new Consumer() { // from class: com.android.settings.dream.-$$Lambda$CurrentDreamPicker$t4o3LQXIuoDz_RsLdUZZYlwB3bA
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                CurrentDreamPicker.lambda$getDreamComponentsMap$0(hashMap, (DreamBackend.DreamInfo) obj);
            }
        });
        return hashMap;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ void lambda$getDreamComponentsMap$0(Map map, DreamBackend.DreamInfo dreamInfo) {
        ComponentName componentName = (ComponentName) map.put(dreamInfo.componentName.flattenToString(), dreamInfo.componentName);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static final class DreamCandidateInfo extends CandidateInfo {
        private final Drawable icon;
        private final String key;
        private final CharSequence name;

        /* JADX INFO: Access modifiers changed from: package-private */
        public DreamCandidateInfo(DreamBackend.DreamInfo dreamInfo) {
            super(true);
            this.name = dreamInfo.caption;
            this.icon = dreamInfo.icon;
            this.key = dreamInfo.componentName.flattenToString();
        }

        @Override // com.android.settingslib.widget.CandidateInfo
        public CharSequence loadLabel() {
            return this.name;
        }

        @Override // com.android.settingslib.widget.CandidateInfo
        public Drawable loadIcon() {
            return this.icon;
        }

        @Override // com.android.settingslib.widget.CandidateInfo
        public String getKey() {
            return this.key;
        }
    }
}

package com.android.settings.inputmethod;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.TextUtils;
import android.view.View;
import android.view.textservice.SpellCheckerInfo;
import com.android.settings.CustomListPreference;
import com.android.settings.R;
/* loaded from: classes.dex */
class SpellCheckerPreference extends CustomListPreference {
    private Intent mIntent;
    private final SpellCheckerInfo[] mScis;

    public SpellCheckerPreference(Context context, SpellCheckerInfo[] spellCheckerInfoArr) {
        super(context, null);
        this.mScis = spellCheckerInfoArr;
        setWidgetLayoutResource(R.layout.preference_widget_gear);
        CharSequence[] charSequenceArr = new CharSequence[spellCheckerInfoArr.length];
        CharSequence[] charSequenceArr2 = new CharSequence[spellCheckerInfoArr.length];
        for (int i = 0; i < spellCheckerInfoArr.length; i++) {
            charSequenceArr[i] = spellCheckerInfoArr[i].loadLabel(context.getPackageManager());
            charSequenceArr2[i] = String.valueOf(i);
        }
        setEntries(charSequenceArr);
        setEntryValues(charSequenceArr2);
    }

    @Override // com.android.settings.CustomListPreference
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder, DialogInterface.OnClickListener onClickListener) {
        builder.setTitle(R.string.choose_spell_checker);
        builder.setSingleChoiceItems(getEntries(), findIndexOfValue(getValue()), onClickListener);
    }

    public void setSelected(SpellCheckerInfo spellCheckerInfo) {
        if (spellCheckerInfo == null) {
            setValue(null);
            return;
        }
        for (int i = 0; i < this.mScis.length; i++) {
            if (this.mScis[i].getId().equals(spellCheckerInfo.getId())) {
                setValueIndex(i);
                return;
            }
        }
    }

    @Override // android.support.v7.preference.ListPreference
    public void setValue(String str) {
        super.setValue(str);
        int parseInt = str != null ? Integer.parseInt(str) : -1;
        if (parseInt == -1) {
            this.mIntent = null;
            return;
        }
        SpellCheckerInfo spellCheckerInfo = this.mScis[parseInt];
        String settingsActivity = spellCheckerInfo.getSettingsActivity();
        if (TextUtils.isEmpty(settingsActivity)) {
            this.mIntent = null;
            return;
        }
        this.mIntent = new Intent("android.intent.action.MAIN");
        this.mIntent.setClassName(spellCheckerInfo.getPackageName(), settingsActivity);
    }

    @Override // android.support.v7.preference.Preference
    public boolean callChangeListener(Object obj) {
        return super.callChangeListener(obj != null ? this.mScis[Integer.parseInt((String) obj)] : null);
    }

    @Override // android.support.v7.preference.Preference
    public void onBindViewHolder(PreferenceViewHolder preferenceViewHolder) {
        super.onBindViewHolder(preferenceViewHolder);
        View findViewById = preferenceViewHolder.findViewById(R.id.settings_button);
        findViewById.setVisibility(this.mIntent != null ? 0 : 4);
        findViewById.setOnClickListener(new View.OnClickListener() { // from class: com.android.settings.inputmethod.SpellCheckerPreference.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                SpellCheckerPreference.this.onSettingsButtonClicked();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onSettingsButtonClicked() {
        Context context = getContext();
        try {
            Intent intent = this.mIntent;
            if (intent != null) {
                context.startActivity(intent);
            }
        } catch (ActivityNotFoundException e) {
        }
    }
}

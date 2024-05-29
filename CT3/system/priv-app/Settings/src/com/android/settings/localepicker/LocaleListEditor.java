package com.android.settings.localepicker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.LocaleList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.android.internal.app.LocalePicker;
import com.android.internal.app.LocalePickerWithRegion;
import com.android.internal.app.LocaleStore;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
/* loaded from: classes.dex */
public class LocaleListEditor extends SettingsPreferenceFragment implements LocalePickerWithRegion.LocaleSelectedListener {
    private LocaleDragAndDropAdapter mAdapter;
    private View mAddLanguage;
    private Menu mMenu;
    private boolean mRemoveMode;
    private boolean mShowingRemoveDialog;

    @Override // com.android.settings.InstrumentedPreferenceFragment
    protected int getMetricsCategory() {
        return 344;
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        LocaleStore.fillCache(getContext());
        List<LocaleStore.LocaleInfo> feedsList = getUserLocaleList(getContext());
        this.mAdapter = new LocaleDragAndDropAdapter(getContext(), feedsList);
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstState) {
        View result = super.onCreateView(inflater, container, savedInstState);
        View myLayout = inflater.inflate(R.layout.locale_order_list, (ViewGroup) result);
        getActivity().setTitle(R.string.pref_title_lang_selection);
        configureDragAndDrop(myLayout);
        return result;
    }

    @Override // android.app.Fragment
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            this.mRemoveMode = savedInstanceState.getBoolean("localeRemoveMode", false);
            this.mShowingRemoveDialog = savedInstanceState.getBoolean("showingLocaleRemoveDialog", false);
        }
        setRemoveMode(this.mRemoveMode);
        this.mAdapter.restoreState(savedInstanceState);
        if (!this.mShowingRemoveDialog) {
            return;
        }
        showRemoveLocaleWarningDialog();
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("localeRemoveMode", this.mRemoveMode);
        outState.putBoolean("showingLocaleRemoveDialog", this.mShowingRemoveDialog);
        this.mAdapter.saveState(outState);
    }

    @Override // android.app.Fragment
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case 2:
                if (this.mRemoveMode) {
                    showRemoveLocaleWarningDialog();
                } else {
                    setRemoveMode(true);
                }
                return true;
            case 16908332:
                if (this.mRemoveMode) {
                    setRemoveMode(false);
                    return true;
                }
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setRemoveMode(boolean mRemoveMode) {
        this.mRemoveMode = mRemoveMode;
        this.mAdapter.setRemoveMode(mRemoveMode);
        this.mAddLanguage.setVisibility(mRemoveMode ? 4 : 0);
        updateVisibilityOfRemoveMenu();
    }

    private void showRemoveLocaleWarningDialog() {
        int checkedCount = this.mAdapter.getCheckedCount();
        if (checkedCount == 0) {
            setRemoveMode(this.mRemoveMode ? false : true);
        } else if (checkedCount == this.mAdapter.getItemCount()) {
            this.mShowingRemoveDialog = true;
            new AlertDialog.Builder(getActivity()).setTitle(R.string.dlg_remove_locales_error_title).setMessage(R.string.dlg_remove_locales_error_message).setPositiveButton(17039379, new DialogInterface.OnClickListener() { // from class: com.android.settings.localepicker.LocaleListEditor.1
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialog, int which) {
                }
            }).setOnDismissListener(new DialogInterface.OnDismissListener() { // from class: com.android.settings.localepicker.LocaleListEditor.2
                @Override // android.content.DialogInterface.OnDismissListener
                public void onDismiss(DialogInterface dialog) {
                    LocaleListEditor.this.mShowingRemoveDialog = false;
                }
            }).create().show();
        } else {
            String title = getResources().getQuantityString(R.plurals.dlg_remove_locales_title, checkedCount);
            this.mShowingRemoveDialog = true;
            new AlertDialog.Builder(getActivity()).setTitle(title).setMessage(R.string.dlg_remove_locales_message).setNegativeButton(17039369, new DialogInterface.OnClickListener() { // from class: com.android.settings.localepicker.LocaleListEditor.3
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialog, int which) {
                    LocaleListEditor.this.setRemoveMode(false);
                }
            }).setPositiveButton(17039379, new DialogInterface.OnClickListener() { // from class: com.android.settings.localepicker.LocaleListEditor.4
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialog, int which) {
                    LocaleListEditor.this.mRemoveMode = false;
                    LocaleListEditor.this.mShowingRemoveDialog = false;
                    LocaleListEditor.this.mAdapter.removeChecked();
                    LocaleListEditor.this.setRemoveMode(false);
                }
            }).setOnDismissListener(new DialogInterface.OnDismissListener() { // from class: com.android.settings.localepicker.LocaleListEditor.5
                @Override // android.content.DialogInterface.OnDismissListener
                public void onDismiss(DialogInterface dialog) {
                    LocaleListEditor.this.mShowingRemoveDialog = false;
                }
            }).create().show();
        }
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.app.Fragment
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem menuItem = menu.add(0, 2, 0, R.string.locale_remove_menu);
        menuItem.setShowAsAction(4);
        menuItem.setIcon(R.drawable.ic_delete);
        super.onCreateOptionsMenu(menu, inflater);
        this.mMenu = menu;
        updateVisibilityOfRemoveMenu();
    }

    private static List<LocaleStore.LocaleInfo> getUserLocaleList(Context context) {
        List<LocaleStore.LocaleInfo> result = new ArrayList<>();
        LocaleList localeList = LocalePicker.getLocales();
        for (int i = 0; i < localeList.size(); i++) {
            Locale locale = localeList.get(i);
            result.add(LocaleStore.getLocaleInfo(locale));
        }
        return result;
    }

    private void configureDragAndDrop(View view) {
        RecyclerView list = (RecyclerView) view.findViewById(R.id.dragList);
        LocaleLinearLayoutManager llm = new LocaleLinearLayoutManager(getContext(), this.mAdapter);
        llm.setAutoMeasureEnabled(true);
        list.setLayoutManager(llm);
        list.setHasFixedSize(true);
        this.mAdapter.setRecyclerView(list);
        list.setAdapter(this.mAdapter);
        this.mAddLanguage = view.findViewById(R.id.add_language);
        this.mAddLanguage.setOnClickListener(new View.OnClickListener() { // from class: com.android.settings.localepicker.LocaleListEditor.6
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                LocaleListEditor.this.getFragmentManager().beginTransaction().setTransition(4097).replace(LocaleListEditor.this.getId(), LocalePickerWithRegion.createLanguagePicker(LocaleListEditor.this.getContext(), LocaleListEditor.this, false)).addToBackStack("localeListEditor").commit();
            }
        });
    }

    public void onLocaleSelected(LocaleStore.LocaleInfo locale) {
        this.mAdapter.addLocale(locale);
        updateVisibilityOfRemoveMenu();
    }

    private void updateVisibilityOfRemoveMenu() {
        MenuItem menuItemRemove;
        if (this.mMenu == null || (menuItemRemove = this.mMenu.findItem(2)) == null) {
            return;
        }
        menuItemRemove.setShowAsAction(this.mRemoveMode ? 2 : 0);
        menuItemRemove.setVisible(this.mAdapter.getItemCount() > 1);
    }
}

package com.android.settings;

import android.app.ListFragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.UserDictionary;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AlphabetIndexer;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import com.android.settings.inputmethod.UserDictionaryAddWordFragment;
import com.android.settings.inputmethod.UserDictionarySettingsUtils;
import java.util.Locale;
/* loaded from: classes.dex */
public class UserDictionarySettings extends ListFragment {
    private static final String[] QUERY_PROJECTION = {"_id", "word", "shortcut"};
    private Cursor mCursor;
    protected String mLocale;

    @Override // android.app.ListFragment, android.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(17367222, container, false);
    }

    @Override // android.app.Fragment
    public void onActivityCreated(Bundle savedInstanceState) {
        String locale;
        super.onActivityCreated(savedInstanceState);
        getActivity().getActionBar().setTitle(R.string.user_dict_settings_title);
        Intent intent = getActivity().getIntent();
        String stringExtra = intent == null ? null : intent.getStringExtra("locale");
        Bundle arguments = getArguments();
        String localeFromArguments = arguments != null ? arguments.getString("locale") : null;
        if (localeFromArguments != null) {
            locale = localeFromArguments;
        } else if (stringExtra != null) {
            locale = stringExtra;
        } else {
            locale = null;
        }
        this.mLocale = locale;
        this.mCursor = createCursor(locale);
        TextView emptyView = (TextView) getView().findViewById(16908292);
        emptyView.setText(R.string.user_dict_settings_empty_text);
        ListView listView = getListView();
        listView.setAdapter(createAdapter());
        listView.setFastScrollEnabled(true);
        listView.setEmptyView(emptyView);
        setHasOptionsMenu(true);
        getActivity().getActionBar().setSubtitle(UserDictionarySettingsUtils.getLocaleDisplayName(getActivity(), this.mLocale));
    }

    private Cursor createCursor(String locale) {
        if ("".equals(locale)) {
            return getActivity().managedQuery(UserDictionary.Words.CONTENT_URI, QUERY_PROJECTION, "locale is null", null, "UPPER(word)");
        }
        String queryLocale = locale != null ? locale : Locale.getDefault().toString();
        return getActivity().managedQuery(UserDictionary.Words.CONTENT_URI, QUERY_PROJECTION, "locale=?", new String[]{queryLocale}, "UPPER(word)");
    }

    private ListAdapter createAdapter() {
        return new MyAdapter(getActivity(), R.layout.user_dictionary_item, this.mCursor, new String[]{"word", "shortcut"}, new int[]{16908308, 16908309}, this);
    }

    @Override // android.app.ListFragment
    public void onListItemClick(ListView l, View v, int position, long id) {
        String word = getWord(position);
        String shortcut = getShortcut(position);
        if (word == null) {
            return;
        }
        showAddOrEditDialog(word, shortcut);
    }

    @Override // android.app.Fragment
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem actionItem = menu.add(0, 1, 0, R.string.user_dict_settings_add_menu_title).setIcon(R.drawable.ic_menu_add_dark);
        actionItem.setShowAsAction(5);
    }

    @Override // android.app.Fragment
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            showAddOrEditDialog(null, null);
            return true;
        }
        return false;
    }

    private void showAddOrEditDialog(String editingWord, String editingShortcut) {
        Bundle args = new Bundle();
        args.putInt("mode", editingWord == null ? 1 : 0);
        args.putString("word", editingWord);
        args.putString("shortcut", editingShortcut);
        args.putString("locale", this.mLocale);
        SettingsActivity sa = (SettingsActivity) getActivity();
        sa.startPreferencePanel(UserDictionaryAddWordFragment.class.getName(), args, R.string.user_dict_settings_add_dialog_title, null, null, 0);
    }

    private String getWord(int position) {
        if (this.mCursor == null) {
            return null;
        }
        this.mCursor.moveToPosition(position);
        if (this.mCursor.isAfterLast()) {
            return null;
        }
        return this.mCursor.getString(this.mCursor.getColumnIndexOrThrow("word"));
    }

    private String getShortcut(int position) {
        if (this.mCursor == null) {
            return null;
        }
        this.mCursor.moveToPosition(position);
        if (this.mCursor.isAfterLast()) {
            return null;
        }
        return this.mCursor.getString(this.mCursor.getColumnIndexOrThrow("shortcut"));
    }

    public static void deleteWord(String word, String shortcut, ContentResolver resolver) {
        if (TextUtils.isEmpty(shortcut)) {
            resolver.delete(UserDictionary.Words.CONTENT_URI, "word=? AND shortcut is null OR shortcut=''", new String[]{word});
        } else {
            resolver.delete(UserDictionary.Words.CONTENT_URI, "word=? AND shortcut=?", new String[]{word, shortcut});
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class MyAdapter extends SimpleCursorAdapter implements SectionIndexer {
        private AlphabetIndexer mIndexer;
        private final SimpleCursorAdapter.ViewBinder mViewBinder;

        public MyAdapter(Context context, int layout, Cursor c, String[] from, int[] to, UserDictionarySettings settings) {
            super(context, layout, c, from, to);
            this.mViewBinder = new SimpleCursorAdapter.ViewBinder() { // from class: com.android.settings.UserDictionarySettings.MyAdapter.1
                @Override // android.widget.SimpleCursorAdapter.ViewBinder
                public boolean setViewValue(View v, Cursor c2, int columnIndex) {
                    if (columnIndex == 2) {
                        String shortcut = c2.getString(2);
                        if (TextUtils.isEmpty(shortcut)) {
                            v.setVisibility(8);
                        } else {
                            ((TextView) v).setText(shortcut);
                            v.setVisibility(0);
                        }
                        v.invalidate();
                        return true;
                    }
                    return false;
                }
            };
            if (c != null) {
                String alphabet = context.getString(17040418);
                int wordColIndex = c.getColumnIndexOrThrow("word");
                this.mIndexer = new AlphabetIndexer(c, wordColIndex, alphabet);
            }
            setViewBinder(this.mViewBinder);
        }

        @Override // android.widget.SectionIndexer
        public int getPositionForSection(int section) {
            if (this.mIndexer == null) {
                return 0;
            }
            return this.mIndexer.getPositionForSection(section);
        }

        @Override // android.widget.SectionIndexer
        public int getSectionForPosition(int position) {
            if (this.mIndexer == null) {
                return 0;
            }
            return this.mIndexer.getSectionForPosition(position);
        }

        @Override // android.widget.SectionIndexer
        public Object[] getSections() {
            if (this.mIndexer == null) {
                return null;
            }
            return this.mIndexer.getSections();
        }
    }
}

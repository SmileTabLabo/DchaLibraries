package com.android.settings.inputmethod;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.hardware.input.InputDeviceIdentifier;
import android.hardware.input.InputManager;
import android.hardware.input.KeyboardLayout;
import android.os.Bundle;
import android.view.InputDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.RadioButton;
import android.widget.TextView;
import com.android.settings.R;
import java.util.ArrayList;
import java.util.Collections;
/* loaded from: classes.dex */
public class KeyboardLayoutDialogFragment extends DialogFragment implements InputManager.InputDeviceListener, LoaderManager.LoaderCallbacks<Keyboards> {
    private KeyboardLayoutAdapter mAdapter;
    private boolean mHasShownLayoutSelectionScreen;
    private InputManager mIm;
    private int mInputDeviceId = -1;
    private InputDeviceIdentifier mInputDeviceIdentifier;

    /* loaded from: classes.dex */
    public static final class Keyboards {
        public final ArrayList<KeyboardLayout> keyboardLayouts = new ArrayList<>();
        public int current = -1;
    }

    /* loaded from: classes.dex */
    public interface OnSetupKeyboardLayoutsListener {
        void onSetupKeyboardLayouts(InputDeviceIdentifier inputDeviceIdentifier);
    }

    public KeyboardLayoutDialogFragment() {
    }

    public KeyboardLayoutDialogFragment(InputDeviceIdentifier inputDeviceIdentifier) {
        this.mInputDeviceIdentifier = inputDeviceIdentifier;
    }

    @Override // android.app.Fragment
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Context context = activity.getBaseContext();
        this.mIm = (InputManager) context.getSystemService("input");
        this.mAdapter = new KeyboardLayoutAdapter(context);
    }

    @Override // android.app.DialogFragment, android.app.Fragment
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            this.mInputDeviceIdentifier = savedInstanceState.getParcelable("inputDeviceIdentifier");
        }
        getLoaderManager().initLoader(0, null, this);
    }

    @Override // android.app.DialogFragment, android.app.Fragment
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("inputDeviceIdentifier", this.mInputDeviceIdentifier);
    }

    @Override // android.app.DialogFragment
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity();
        LayoutInflater inflater = LayoutInflater.from(context);
        AlertDialog.Builder builder = new AlertDialog.Builder(context).setTitle(R.string.keyboard_layout_dialog_title).setPositiveButton(R.string.keyboard_layout_dialog_setup_button, new DialogInterface.OnClickListener() { // from class: com.android.settings.inputmethod.KeyboardLayoutDialogFragment.1
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int which) {
                KeyboardLayoutDialogFragment.this.onSetupLayoutsButtonClicked();
            }
        }).setSingleChoiceItems(this.mAdapter, -1, new DialogInterface.OnClickListener() { // from class: com.android.settings.inputmethod.KeyboardLayoutDialogFragment.2
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int which) {
                KeyboardLayoutDialogFragment.this.onKeyboardLayoutClicked(which);
            }
        }).setView(inflater.inflate(R.layout.keyboard_layout_dialog_switch_hint, (ViewGroup) null));
        updateSwitchHintVisibility();
        return builder.create();
    }

    @Override // android.app.Fragment
    public void onResume() {
        super.onResume();
        this.mIm.registerInputDeviceListener(this, null);
        InputDevice inputDevice = this.mIm.getInputDeviceByDescriptor(this.mInputDeviceIdentifier.getDescriptor());
        if (inputDevice == null) {
            dismiss();
        } else {
            this.mInputDeviceId = inputDevice.getId();
        }
    }

    @Override // android.app.Fragment
    public void onPause() {
        this.mIm.unregisterInputDeviceListener(this);
        this.mInputDeviceId = -1;
        super.onPause();
    }

    @Override // android.app.DialogFragment, android.content.DialogInterface.OnCancelListener
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        dismiss();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onSetupLayoutsButtonClicked() {
        ((OnSetupKeyboardLayoutsListener) getTargetFragment()).onSetupKeyboardLayouts(this.mInputDeviceIdentifier);
    }

    @Override // android.app.Fragment
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        show(getActivity().getFragmentManager(), "layout");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onKeyboardLayoutClicked(int which) {
        if (which < 0 || which >= this.mAdapter.getCount()) {
            return;
        }
        KeyboardLayout keyboardLayout = this.mAdapter.getItem(which);
        if (keyboardLayout != null) {
            this.mIm.setCurrentKeyboardLayoutForInputDevice(this.mInputDeviceIdentifier, keyboardLayout.getDescriptor());
        }
        dismiss();
    }

    @Override // android.app.LoaderManager.LoaderCallbacks
    public Loader<Keyboards> onCreateLoader(int id, Bundle args) {
        return new KeyboardLayoutLoader(getActivity().getBaseContext(), this.mInputDeviceIdentifier);
    }

    @Override // android.app.LoaderManager.LoaderCallbacks
    public void onLoadFinished(Loader<Keyboards> loader, Keyboards data) {
        this.mAdapter.clear();
        this.mAdapter.addAll(data.keyboardLayouts);
        this.mAdapter.setCheckedItem(data.current);
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {
            dialog.getListView().setItemChecked(data.current, true);
        }
        updateSwitchHintVisibility();
        showSetupKeyboardLayoutsIfNecessary();
    }

    @Override // android.app.LoaderManager.LoaderCallbacks
    public void onLoaderReset(Loader<Keyboards> loader) {
        this.mAdapter.clear();
        updateSwitchHintVisibility();
    }

    @Override // android.hardware.input.InputManager.InputDeviceListener
    public void onInputDeviceAdded(int deviceId) {
    }

    @Override // android.hardware.input.InputManager.InputDeviceListener
    public void onInputDeviceChanged(int deviceId) {
        if (this.mInputDeviceId < 0 || deviceId != this.mInputDeviceId) {
            return;
        }
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override // android.hardware.input.InputManager.InputDeviceListener
    public void onInputDeviceRemoved(int deviceId) {
        if (this.mInputDeviceId < 0 || deviceId != this.mInputDeviceId) {
            return;
        }
        dismiss();
    }

    private void updateSwitchHintVisibility() {
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog == null) {
            return;
        }
        View customPanel = dialog.findViewById(16909094);
        customPanel.setVisibility(this.mAdapter.getCount() > 1 ? 0 : 8);
    }

    private void showSetupKeyboardLayoutsIfNecessary() {
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog == null || this.mAdapter.getCount() != 1 || this.mAdapter.getItem(0) != null || this.mHasShownLayoutSelectionScreen) {
            return;
        }
        this.mHasShownLayoutSelectionScreen = true;
        ((OnSetupKeyboardLayoutsListener) getTargetFragment()).onSetupKeyboardLayouts(this.mInputDeviceIdentifier);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static final class KeyboardLayoutAdapter extends ArrayAdapter<KeyboardLayout> {
        private int mCheckedItem;
        private final LayoutInflater mInflater;

        public KeyboardLayoutAdapter(Context context) {
            super(context, 17367269);
            this.mCheckedItem = -1;
            this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
        }

        public void setCheckedItem(int position) {
            this.mCheckedItem = position;
        }

        @Override // android.widget.ArrayAdapter, android.widget.Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            String label;
            String collection;
            KeyboardLayout item = getItem(position);
            if (item != null) {
                label = item.getLabel();
                collection = item.getCollection();
            } else {
                label = getContext().getString(R.string.keyboard_layout_default_label);
                collection = "";
            }
            boolean checked = position == this.mCheckedItem;
            if (collection.isEmpty()) {
                return inflateOneLine(convertView, parent, label, checked);
            }
            return inflateTwoLine(convertView, parent, label, collection, checked);
        }

        private View inflateOneLine(View convertView, ViewGroup parent, String label, boolean checked) {
            View view = convertView;
            if (convertView == null || isTwoLine(convertView)) {
                view = this.mInflater.inflate(17367055, parent, false);
                setTwoLine(view, false);
            }
            CheckedTextView headline = (CheckedTextView) view.findViewById(16908308);
            headline.setText(label);
            headline.setChecked(checked);
            return view;
        }

        private View inflateTwoLine(View convertView, ViewGroup parent, String label, String collection, boolean checked) {
            View view = convertView;
            if (convertView == null || !isTwoLine(convertView)) {
                view = this.mInflater.inflate(17367269, parent, false);
                setTwoLine(view, true);
            }
            TextView headline = (TextView) view.findViewById(16908308);
            TextView subText = (TextView) view.findViewById(16908309);
            RadioButton radioButton = (RadioButton) view.findViewById(16909190);
            headline.setText(label);
            subText.setText(collection);
            radioButton.setChecked(checked);
            return view;
        }

        private static boolean isTwoLine(View view) {
            return view.getTag() == Boolean.TRUE;
        }

        private static void setTwoLine(View view, boolean twoLine) {
            view.setTag(Boolean.valueOf(twoLine));
        }
    }

    /* loaded from: classes.dex */
    private static final class KeyboardLayoutLoader extends AsyncTaskLoader<Keyboards> {
        private final InputDeviceIdentifier mInputDeviceIdentifier;

        public KeyboardLayoutLoader(Context context, InputDeviceIdentifier inputDeviceIdentifier) {
            super(context);
            this.mInputDeviceIdentifier = inputDeviceIdentifier;
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.content.AsyncTaskLoader
        public Keyboards loadInBackground() {
            Keyboards keyboards = new Keyboards();
            InputManager im = (InputManager) getContext().getSystemService("input");
            String[] keyboardLayoutDescriptors = im.getEnabledKeyboardLayoutsForInputDevice(this.mInputDeviceIdentifier);
            for (String keyboardLayoutDescriptor : keyboardLayoutDescriptors) {
                KeyboardLayout keyboardLayout = im.getKeyboardLayout(keyboardLayoutDescriptor);
                if (keyboardLayout != null) {
                    keyboards.keyboardLayouts.add(keyboardLayout);
                }
            }
            Collections.sort(keyboards.keyboardLayouts);
            String currentKeyboardLayoutDescriptor = im.getCurrentKeyboardLayoutForInputDevice(this.mInputDeviceIdentifier);
            if (currentKeyboardLayoutDescriptor != null) {
                int numKeyboardLayouts = keyboards.keyboardLayouts.size();
                int i = 0;
                while (true) {
                    if (i < numKeyboardLayouts) {
                        if (!keyboards.keyboardLayouts.get(i).getDescriptor().equals(currentKeyboardLayoutDescriptor)) {
                            i++;
                        } else {
                            keyboards.current = i;
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }
            if (keyboards.keyboardLayouts.isEmpty()) {
                keyboards.keyboardLayouts.add(null);
                keyboards.current = 0;
            }
            return keyboards;
        }

        @Override // android.content.Loader
        protected void onStartLoading() {
            super.onStartLoading();
            forceLoad();
        }

        @Override // android.content.Loader
        protected void onStopLoading() {
            super.onStopLoading();
            cancelLoad();
        }
    }
}

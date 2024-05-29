package android.support.v14.preference;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.preference.EditTextPreference;
import android.view.View;
import android.widget.EditText;
/* loaded from: a.zip:android/support/v14/preference/EditTextPreferenceDialogFragment.class */
public class EditTextPreferenceDialogFragment extends PreferenceDialogFragment {
    private EditText mEditText;
    private CharSequence mText;

    private EditTextPreference getEditTextPreference() {
        return (EditTextPreference) getPreference();
    }

    public static EditTextPreferenceDialogFragment newInstance(String str) {
        EditTextPreferenceDialogFragment editTextPreferenceDialogFragment = new EditTextPreferenceDialogFragment();
        Bundle bundle = new Bundle(1);
        bundle.putString("key", str);
        editTextPreferenceDialogFragment.setArguments(bundle);
        return editTextPreferenceDialogFragment;
    }

    @Override // android.support.v14.preference.PreferenceDialogFragment
    protected boolean needInputMethod() {
        return true;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v14.preference.PreferenceDialogFragment
    public void onBindDialogView(View view) {
        super.onBindDialogView(view);
        this.mEditText = (EditText) view.findViewById(16908291);
        if (this.mEditText == null) {
            throw new IllegalStateException("Dialog view must contain an EditText with id @android:id/edit");
        }
        this.mEditText.setText(this.mText);
    }

    @Override // android.support.v14.preference.PreferenceDialogFragment, android.app.DialogFragment, android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (bundle == null) {
            this.mText = getEditTextPreference().getText();
        } else {
            this.mText = bundle.getCharSequence("EditTextPreferenceDialogFragment.text");
        }
    }

    @Override // android.support.v14.preference.PreferenceDialogFragment
    public void onDialogClosed(boolean z) {
        if (z) {
            String editable = this.mEditText.getText().toString();
            if (getEditTextPreference().callChangeListener(editable)) {
                getEditTextPreference().setText(editable);
            }
        }
    }

    @Override // android.support.v14.preference.PreferenceDialogFragment, android.app.DialogFragment, android.app.Fragment
    public void onSaveInstanceState(@NonNull Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putCharSequence("EditTextPreferenceDialogFragment.text", this.mText);
    }
}

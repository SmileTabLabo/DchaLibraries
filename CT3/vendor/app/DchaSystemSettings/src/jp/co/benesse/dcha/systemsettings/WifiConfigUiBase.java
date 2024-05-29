package jp.co.benesse.dcha.systemsettings;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.Button;
/* loaded from: s.zip:jp/co/benesse/dcha/systemsettings/WifiConfigUiBase.class */
public interface WifiConfigUiBase {
    void dispatchSubmit();

    Context getContext();

    Button getForgetButton();

    LayoutInflater getLayoutInflater();

    Button getSubmitButton();

    void setCancelButton(CharSequence charSequence);

    void setForgetButton(CharSequence charSequence);

    void setSubmitButton(CharSequence charSequence);

    void setTitle(int i);

    void setTitle(CharSequence charSequence);
}

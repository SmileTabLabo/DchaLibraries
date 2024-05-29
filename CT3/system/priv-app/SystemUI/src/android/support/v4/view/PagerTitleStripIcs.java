package android.support.v4.view;

import android.content.Context;
import android.text.method.SingleLineTransformationMethod;
import android.view.View;
import android.widget.TextView;
import java.util.Locale;
/* loaded from: a.zip:android/support/v4/view/PagerTitleStripIcs.class */
class PagerTitleStripIcs {

    /* loaded from: a.zip:android/support/v4/view/PagerTitleStripIcs$SingleLineAllCapsTransform.class */
    private static class SingleLineAllCapsTransform extends SingleLineTransformationMethod {
        private Locale mLocale;

        public SingleLineAllCapsTransform(Context context) {
            this.mLocale = context.getResources().getConfiguration().locale;
        }

        @Override // android.text.method.ReplacementTransformationMethod, android.text.method.TransformationMethod
        public CharSequence getTransformation(CharSequence charSequence, View view) {
            CharSequence transformation = super.getTransformation(charSequence, view);
            String str = null;
            if (transformation != null) {
                str = transformation.toString().toUpperCase(this.mLocale);
            }
            return str;
        }
    }

    PagerTitleStripIcs() {
    }

    public static void setSingleLineAllCaps(TextView textView) {
        textView.setTransformationMethod(new SingleLineAllCapsTransform(textView.getContext()));
    }
}

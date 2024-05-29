package com.android.systemui.tuner;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.KeyEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
/* loaded from: a.zip:com/android/systemui/tuner/KeycodeSelectionHelper.class */
public class KeycodeSelectionHelper {
    private static final ArrayList<String> mKeycodeStrings = new ArrayList<>();
    private static final ArrayList<Integer> mKeycodes = new ArrayList<>();

    /* loaded from: a.zip:com/android/systemui/tuner/KeycodeSelectionHelper$OnSelectionComplete.class */
    public interface OnSelectionComplete {
        void onSelectionComplete(int i);
    }

    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:15:0x006f -> B:12:0x0068). Please submit an issue!!! */
    static {
        Field[] declaredFields;
        for (Field field : KeyEvent.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && field.getName().startsWith("KEYCODE_") && field.getType().equals(Integer.TYPE)) {
                try {
                    mKeycodeStrings.add(formatString(field.getName()));
                    mKeycodes.add((Integer) field.get(null));
                } catch (IllegalAccessException e) {
                }
            }
        }
    }

    private static String formatString(String str) {
        StringBuilder sb = new StringBuilder(str.replace("KEYCODE_", "").replace("_", " ").toLowerCase());
        for (int i = 0; i < sb.length(); i++) {
            if (i == 0 || sb.charAt(i - 1) == ' ') {
                sb.setCharAt(i, Character.toUpperCase(sb.charAt(i)));
            }
        }
        return sb.toString();
    }

    public static Intent getSelectImageIntent() {
        return new Intent("android.intent.action.OPEN_DOCUMENT").addCategory("android.intent.category.OPENABLE").setType("image/*");
    }

    public static void showKeycodeSelect(Context context, OnSelectionComplete onSelectionComplete) {
        new AlertDialog.Builder(context).setTitle(2131493875).setItems((CharSequence[]) mKeycodeStrings.toArray(new String[0]), new DialogInterface.OnClickListener(onSelectionComplete) { // from class: com.android.systemui.tuner.KeycodeSelectionHelper.1
            final OnSelectionComplete val$listener;

            {
                this.val$listener = onSelectionComplete;
            }

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                this.val$listener.onSelectionComplete(((Integer) KeycodeSelectionHelper.mKeycodes.get(i)).intValue());
            }
        }).show();
    }
}

package com.android.systemui;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Map;
/* loaded from: a.zip:com/android/systemui/Prefs.class */
public final class Prefs {
    private Prefs() {
    }

    private static SharedPreferences get(Context context) {
        return context.getSharedPreferences(context.getPackageName(), 0);
    }

    public static Map<String, ?> getAll(Context context) {
        return get(context).getAll();
    }

    public static boolean getBoolean(Context context, String str, boolean z) {
        return get(context).getBoolean(str, z);
    }

    public static int getInt(Context context, String str, int i) {
        return get(context).getInt(str, i);
    }

    public static long getLong(Context context, String str, long j) {
        return get(context).getLong(str, j);
    }

    public static void putBoolean(Context context, String str, boolean z) {
        get(context).edit().putBoolean(str, z).apply();
    }

    public static void putInt(Context context, String str, int i) {
        get(context).edit().putInt(str, i).apply();
    }

    public static void putLong(Context context, String str, long j) {
        get(context).edit().putLong(str, j).apply();
    }

    public static void registerListener(Context context, SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
        get(context).registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

    public static void remove(Context context, String str) {
        get(context).edit().remove(str).apply();
    }

    public static void unregisterListener(Context context, SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
        get(context).unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }
}

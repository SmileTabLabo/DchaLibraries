package com.mediatek.launcher3;

import android.os.Build;
import android.util.Log;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
/* loaded from: a.zip:com/mediatek/launcher3/LauncherLog.class */
public final class LauncherLog {
    public static boolean DEBUG;
    public static boolean DEBUG_AUTOTESTCASE;
    public static boolean DEBUG_DRAG;
    public static boolean DEBUG_DRAW;
    public static boolean DEBUG_EDIT;
    public static boolean DEBUG_KEY;
    public static boolean DEBUG_LAYOUT;
    public static boolean DEBUG_LOADER;
    public static boolean DEBUG_LOADERS;
    public static boolean DEBUG_MOTION;
    public static boolean DEBUG_PERFORMANCE;
    public static boolean DEBUG_SURFACEWIDGET;
    public static boolean DEBUG_UNREAD;
    private static final LauncherLog INSTANCE = new LauncherLog();
    private static Method getProp;

    static {
        DEBUG = true;
        DEBUG_DRAW = false;
        DEBUG_DRAG = true;
        DEBUG_EDIT = true;
        DEBUG_KEY = true;
        DEBUG_LAYOUT = false;
        DEBUG_LOADER = true;
        DEBUG_MOTION = false;
        DEBUG_PERFORMANCE = true;
        DEBUG_SURFACEWIDGET = false;
        DEBUG_UNREAD = false;
        DEBUG_LOADERS = true;
        DEBUG_AUTOTESTCASE = false;
        if (getSysProperty("launcher.debug.all", false).booleanValue()) {
            Log.d("Launcher3", "enable all debug on-off");
            DEBUG = true;
            DEBUG_DRAW = true;
            DEBUG_DRAG = true;
            DEBUG_EDIT = true;
            DEBUG_KEY = true;
            DEBUG_LAYOUT = true;
            DEBUG_LOADER = true;
            DEBUG_MOTION = true;
            DEBUG_PERFORMANCE = true;
            DEBUG_SURFACEWIDGET = true;
            DEBUG_UNREAD = true;
            DEBUG_LOADERS = true;
            DEBUG_AUTOTESTCASE = true;
        } else {
            DEBUG = getSysProperty("launcher.debug", Boolean.valueOf("eng".equals(Build.TYPE))).booleanValue();
            DEBUG_DRAW = getSysProperty("launcher.debug.draw", false).booleanValue();
            DEBUG_DRAG = getSysProperty("launcher.debug.drag", false).booleanValue();
            DEBUG_EDIT = getSysProperty("launcher.debug.edit", false).booleanValue();
            DEBUG_KEY = getSysProperty("launcher.debug.key", false).booleanValue();
            DEBUG_LAYOUT = getSysProperty("launcher.debug.layout", false).booleanValue();
            DEBUG_LOADER = getSysProperty("launcher.debug.loader", false).booleanValue();
            DEBUG_MOTION = getSysProperty("launcher.debug.motion", false).booleanValue();
            DEBUG_PERFORMANCE = getSysProperty("launcher.debug.performance", false).booleanValue();
            DEBUG_SURFACEWIDGET = getSysProperty("launcher.debug.surfacewidget", false).booleanValue();
            DEBUG_UNREAD = getSysProperty("launcher.debug.unread", false).booleanValue();
            DEBUG_LOADERS = getSysProperty("launcher.debug.loaders", false).booleanValue();
            DEBUG_AUTOTESTCASE = getSysProperty("launcher.debug.autotestcase", false).booleanValue();
        }
        getProp = null;
    }

    private LauncherLog() {
    }

    public static void d(String str, String str2) {
        Log.d("Launcher3", str + ", " + str2);
    }

    public static void e(String str, String str2) {
        Log.e("Launcher3", str + ", " + str2);
    }

    public static Boolean getSysProperty(String str, Boolean bool) {
        try {
            if (getProp == null) {
                getProp = Class.forName("android.os.SystemProperties").getMethod("getBoolean", String.class, Boolean.class);
            }
            return (Boolean) getProp.invoke(null, str, bool);
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            return bool;
        }
    }

    public static String getSysProperty(String str) {
        try {
            return (String) Class.forName("android.os.SystemProperties").getMethod("getBoolean", String.class, String.class).invoke(null, str);
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            return null;
        }
    }

    public static void i(String str, String str2) {
        Log.i("Launcher3", str + ", " + str2);
    }

    public static void w(String str, String str2) {
        Log.w("Launcher3", str + ", " + str2);
    }
}

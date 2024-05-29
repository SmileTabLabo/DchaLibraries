package com.android.systemui.statusbar;

import android.app.AlertDialog;
import android.app.AppGlobals;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.hardware.input.InputManager;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseArray;
import android.view.ContextThemeWrapper;
import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.view.KeyboardShortcutGroup;
import android.view.KeyboardShortcutInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.internal.app.AssistUtils;
import com.android.systemui.recents.Recents;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
/* loaded from: a.zip:com/android/systemui/statusbar/KeyboardShortcuts.class */
public final class KeyboardShortcuts {
    private static KeyboardShortcuts sInstance;
    private static boolean sIsShowing;
    private final Context mContext;
    private KeyCharacterMap mKeyCharacterMap;
    private Dialog mKeyboardShortcutsDialog;
    private static final String TAG = KeyboardShortcuts.class.getSimpleName();
    private static final Object sLock = new Object();
    private final SparseArray<String> mSpecialCharacterNames = new SparseArray<>();
    private final SparseArray<String> mModifierNames = new SparseArray<>();
    private final SparseArray<Drawable> mSpecialCharacterDrawables = new SparseArray<>();
    private final SparseArray<Drawable> mModifierDrawables = new SparseArray<>();
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final DialogInterface.OnClickListener mDialogCloseListener = new DialogInterface.OnClickListener(this) { // from class: com.android.systemui.statusbar.KeyboardShortcuts.1
        final KeyboardShortcuts this$0;

        {
            this.this$0 = this;
        }

        @Override // android.content.DialogInterface.OnClickListener
        public void onClick(DialogInterface dialogInterface, int i) {
            this.this$0.dismissKeyboardShortcuts();
        }
    };
    private final Comparator<KeyboardShortcutInfo> mApplicationItemsComparator = new Comparator<KeyboardShortcutInfo>(this) { // from class: com.android.systemui.statusbar.KeyboardShortcuts.2
        final KeyboardShortcuts this$0;

        {
            this.this$0 = this;
        }

        @Override // java.util.Comparator
        public int compare(KeyboardShortcutInfo keyboardShortcutInfo, KeyboardShortcutInfo keyboardShortcutInfo2) {
            boolean isEmpty = keyboardShortcutInfo.getLabel() != null ? keyboardShortcutInfo.getLabel().toString().isEmpty() : true;
            boolean isEmpty2 = keyboardShortcutInfo2.getLabel() != null ? keyboardShortcutInfo2.getLabel().toString().isEmpty() : true;
            if (isEmpty && isEmpty2) {
                return 0;
            }
            if (isEmpty) {
                return 1;
            }
            if (isEmpty2) {
                return -1;
            }
            return keyboardShortcutInfo.getLabel().toString().compareToIgnoreCase(keyboardShortcutInfo2.getLabel().toString());
        }
    };
    private final IPackageManager mPackageManager = AppGlobals.getPackageManager();

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/statusbar/KeyboardShortcuts$StringOrDrawable.class */
    public static final class StringOrDrawable {
        public Drawable drawable;
        public String string;

        public StringOrDrawable(Drawable drawable) {
            this.drawable = drawable;
        }

        public StringOrDrawable(String str) {
            this.string = str;
        }
    }

    private KeyboardShortcuts(Context context) {
        this.mContext = new ContextThemeWrapper(context, 16974391);
        loadResources(context);
    }

    public static void dismiss() {
        synchronized (sLock) {
            if (sInstance != null) {
                sInstance.dismissKeyboardShortcuts();
                sInstance = null;
            }
            sIsShowing = false;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dismissKeyboardShortcuts() {
        if (this.mKeyboardShortcutsDialog != null) {
            this.mKeyboardShortcutsDialog.dismiss();
            this.mKeyboardShortcutsDialog = null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public KeyboardShortcutGroup getDefaultApplicationShortcuts() {
        int userId = this.mContext.getUserId();
        ArrayList arrayList = new ArrayList();
        PackageInfo packageInfo = null;
        try {
            packageInfo = this.mPackageManager.getPackageInfo(new AssistUtils(this.mContext).getAssistComponentForUser(userId).getPackageName(), 0, userId);
        } catch (RemoteException e) {
            Log.e(TAG, "PackageManagerService is dead");
        }
        if (packageInfo != null) {
            arrayList.add(new KeyboardShortcutInfo(this.mContext.getString(2131493835), Icon.createWithResource(packageInfo.applicationInfo.packageName, packageInfo.applicationInfo.icon), 0, 65536));
        }
        Icon iconForIntentCategory = getIconForIntentCategory("android.intent.category.APP_BROWSER", userId);
        if (iconForIntentCategory != null) {
            arrayList.add(new KeyboardShortcutInfo(this.mContext.getString(2131493836), iconForIntentCategory, 30, 65536));
        }
        Icon iconForIntentCategory2 = getIconForIntentCategory("android.intent.category.APP_CONTACTS", userId);
        if (iconForIntentCategory2 != null) {
            arrayList.add(new KeyboardShortcutInfo(this.mContext.getString(2131493837), iconForIntentCategory2, 31, 65536));
        }
        Icon iconForIntentCategory3 = getIconForIntentCategory("android.intent.category.APP_EMAIL", userId);
        if (iconForIntentCategory3 != null) {
            arrayList.add(new KeyboardShortcutInfo(this.mContext.getString(2131493838), iconForIntentCategory3, 33, 65536));
        }
        Icon iconForIntentCategory4 = getIconForIntentCategory("android.intent.category.APP_MESSAGING", userId);
        if (iconForIntentCategory4 != null) {
            arrayList.add(new KeyboardShortcutInfo(this.mContext.getString(2131493839), iconForIntentCategory4, 48, 65536));
        }
        Icon iconForIntentCategory5 = getIconForIntentCategory("android.intent.category.APP_MUSIC", userId);
        if (iconForIntentCategory5 != null) {
            arrayList.add(new KeyboardShortcutInfo(this.mContext.getString(2131493840), iconForIntentCategory5, 44, 65536));
        }
        Icon iconForIntentCategory6 = getIconForIntentCategory("android.intent.category.APP_CALENDAR", userId);
        if (iconForIntentCategory6 != null) {
            arrayList.add(new KeyboardShortcutInfo(this.mContext.getString(2131493842), iconForIntentCategory6, 40, 65536));
        }
        if (arrayList.size() == 0) {
            return null;
        }
        Collections.sort(arrayList, this.mApplicationItemsComparator);
        return new KeyboardShortcutGroup(this.mContext.getString(2131493834), arrayList, true);
    }

    private List<StringOrDrawable> getHumanReadableModifiers(KeyboardShortcutInfo keyboardShortcutInfo) {
        ArrayList arrayList = new ArrayList();
        int modifiers = keyboardShortcutInfo.getModifiers();
        if (modifiers == 0) {
            return arrayList;
        }
        int i = 0;
        while (i < this.mModifierNames.size()) {
            int keyAt = this.mModifierNames.keyAt(i);
            int i2 = modifiers;
            if ((modifiers & keyAt) != 0) {
                if (this.mModifierDrawables.get(keyAt) != null) {
                    arrayList.add(new StringOrDrawable(this.mModifierDrawables.get(keyAt)));
                } else {
                    arrayList.add(new StringOrDrawable(this.mModifierNames.get(keyAt).toUpperCase()));
                }
                i2 = modifiers & (keyAt ^ (-1));
            }
            i++;
            modifiers = i2;
        }
        if (modifiers != 0) {
            return null;
        }
        return arrayList;
    }

    private List<StringOrDrawable> getHumanReadableShortcutKeys(KeyboardShortcutInfo keyboardShortcutInfo) {
        String valueOf;
        List<StringOrDrawable> humanReadableModifiers = getHumanReadableModifiers(keyboardShortcutInfo);
        if (humanReadableModifiers == null) {
            return null;
        }
        Drawable drawable = null;
        if (keyboardShortcutInfo.getBaseCharacter() > 0) {
            valueOf = String.valueOf(keyboardShortcutInfo.getBaseCharacter());
        } else if (this.mSpecialCharacterDrawables.get(keyboardShortcutInfo.getKeycode()) != null) {
            drawable = this.mSpecialCharacterDrawables.get(keyboardShortcutInfo.getKeycode());
            valueOf = null;
        } else if (this.mSpecialCharacterNames.get(keyboardShortcutInfo.getKeycode()) != null) {
            valueOf = this.mSpecialCharacterNames.get(keyboardShortcutInfo.getKeycode());
        } else if (keyboardShortcutInfo.getKeycode() == 0) {
            return humanReadableModifiers;
        } else {
            char displayLabel = this.mKeyCharacterMap.getDisplayLabel(keyboardShortcutInfo.getKeycode());
            if (displayLabel == 0) {
                return null;
            }
            valueOf = String.valueOf(displayLabel);
        }
        if (drawable != null) {
            humanReadableModifiers.add(new StringOrDrawable(drawable));
        } else if (valueOf != null) {
            humanReadableModifiers.add(new StringOrDrawable(valueOf.toUpperCase()));
        }
        return humanReadableModifiers;
    }

    private Icon getIconForIntentCategory(String str, int i) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory(str);
        PackageInfo packageInfoForIntent = getPackageInfoForIntent(intent, i);
        if (packageInfoForIntent == null || packageInfoForIntent.applicationInfo.icon == 0) {
            return null;
        }
        return Icon.createWithResource(packageInfoForIntent.applicationInfo.packageName, packageInfoForIntent.applicationInfo.icon);
    }

    private static KeyboardShortcuts getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new KeyboardShortcuts(context);
        }
        return sInstance;
    }

    private PackageInfo getPackageInfoForIntent(Intent intent, int i) {
        try {
            ResolveInfo resolveIntent = this.mPackageManager.resolveIntent(intent, intent.resolveTypeIfNeeded(this.mContext.getContentResolver()), 0, i);
            if (resolveIntent == null || resolveIntent.activityInfo == null) {
                return null;
            }
            return this.mPackageManager.getPackageInfo(resolveIntent.activityInfo.packageName, 0, i);
        } catch (RemoteException e) {
            Log.e(TAG, "PackageManagerService is dead", e);
            return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public KeyboardShortcutGroup getSystemShortcuts() {
        KeyboardShortcutGroup keyboardShortcutGroup = new KeyboardShortcutGroup((CharSequence) this.mContext.getString(2131493827), true);
        keyboardShortcutGroup.addItem(new KeyboardShortcutInfo(this.mContext.getString(2131493828), 66, 65536));
        keyboardShortcutGroup.addItem(new KeyboardShortcutInfo(this.mContext.getString(2131493830), 67, 65536));
        keyboardShortcutGroup.addItem(new KeyboardShortcutInfo(this.mContext.getString(2131493829), 61, 2));
        keyboardShortcutGroup.addItem(new KeyboardShortcutInfo(this.mContext.getString(2131493831), 42, 65536));
        keyboardShortcutGroup.addItem(new KeyboardShortcutInfo(this.mContext.getString(2131493832), 76, 65536));
        keyboardShortcutGroup.addItem(new KeyboardShortcutInfo(this.mContext.getString(2131493833), 62, 65536));
        return keyboardShortcutGroup;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleShowKeyboardShortcuts(List<KeyboardShortcutGroup> list) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext);
        View inflate = ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(2130968630, (ViewGroup) null);
        populateKeyboardShortcuts((LinearLayout) inflate.findViewById(2131886290), list);
        builder.setView(inflate);
        builder.setPositiveButton(2131493564, this.mDialogCloseListener);
        this.mKeyboardShortcutsDialog = builder.create();
        this.mKeyboardShortcutsDialog.setCanceledOnTouchOutside(true);
        this.mKeyboardShortcutsDialog.getWindow().setType(2008);
        this.mKeyboardShortcutsDialog.show();
    }

    private void loadResources(Context context) {
        this.mSpecialCharacterNames.put(3, context.getString(2131493802));
        this.mSpecialCharacterNames.put(4, context.getString(2131493803));
        this.mSpecialCharacterNames.put(19, context.getString(2131493804));
        this.mSpecialCharacterNames.put(20, context.getString(2131493805));
        this.mSpecialCharacterNames.put(21, context.getString(2131493806));
        this.mSpecialCharacterNames.put(22, context.getString(2131493807));
        this.mSpecialCharacterNames.put(23, context.getString(2131493808));
        this.mSpecialCharacterNames.put(56, ".");
        this.mSpecialCharacterNames.put(61, context.getString(2131493809));
        this.mSpecialCharacterNames.put(62, context.getString(2131493810));
        this.mSpecialCharacterNames.put(66, context.getString(2131493811));
        this.mSpecialCharacterNames.put(67, context.getString(2131493812));
        this.mSpecialCharacterNames.put(85, context.getString(2131493813));
        this.mSpecialCharacterNames.put(86, context.getString(2131493814));
        this.mSpecialCharacterNames.put(87, context.getString(2131493815));
        this.mSpecialCharacterNames.put(88, context.getString(2131493816));
        this.mSpecialCharacterNames.put(89, context.getString(2131493817));
        this.mSpecialCharacterNames.put(90, context.getString(2131493818));
        this.mSpecialCharacterNames.put(92, context.getString(2131493819));
        this.mSpecialCharacterNames.put(93, context.getString(2131493820));
        this.mSpecialCharacterNames.put(96, context.getString(2131493801, "A"));
        this.mSpecialCharacterNames.put(97, context.getString(2131493801, "B"));
        this.mSpecialCharacterNames.put(98, context.getString(2131493801, "C"));
        this.mSpecialCharacterNames.put(99, context.getString(2131493801, "X"));
        this.mSpecialCharacterNames.put(100, context.getString(2131493801, "Y"));
        this.mSpecialCharacterNames.put(101, context.getString(2131493801, "Z"));
        this.mSpecialCharacterNames.put(102, context.getString(2131493801, "L1"));
        this.mSpecialCharacterNames.put(103, context.getString(2131493801, "R1"));
        this.mSpecialCharacterNames.put(104, context.getString(2131493801, "L2"));
        this.mSpecialCharacterNames.put(105, context.getString(2131493801, "R2"));
        this.mSpecialCharacterNames.put(108, context.getString(2131493801, "Start"));
        this.mSpecialCharacterNames.put(109, context.getString(2131493801, "Select"));
        this.mSpecialCharacterNames.put(110, context.getString(2131493801, "Mode"));
        this.mSpecialCharacterNames.put(112, context.getString(2131493821));
        this.mSpecialCharacterNames.put(111, "Esc");
        this.mSpecialCharacterNames.put(120, "SysRq");
        this.mSpecialCharacterNames.put(121, "Break");
        this.mSpecialCharacterNames.put(116, "Scroll Lock");
        this.mSpecialCharacterNames.put(122, context.getString(2131493822));
        this.mSpecialCharacterNames.put(123, context.getString(2131493823));
        this.mSpecialCharacterNames.put(124, context.getString(2131493824));
        this.mSpecialCharacterNames.put(131, "F1");
        this.mSpecialCharacterNames.put(132, "F2");
        this.mSpecialCharacterNames.put(133, "F3");
        this.mSpecialCharacterNames.put(134, "F4");
        this.mSpecialCharacterNames.put(135, "F5");
        this.mSpecialCharacterNames.put(136, "F6");
        this.mSpecialCharacterNames.put(137, "F7");
        this.mSpecialCharacterNames.put(138, "F8");
        this.mSpecialCharacterNames.put(139, "F9");
        this.mSpecialCharacterNames.put(140, "F10");
        this.mSpecialCharacterNames.put(141, "F11");
        this.mSpecialCharacterNames.put(142, "F12");
        this.mSpecialCharacterNames.put(143, context.getString(2131493825));
        this.mSpecialCharacterNames.put(144, context.getString(2131493826, "0"));
        this.mSpecialCharacterNames.put(145, context.getString(2131493826, "1"));
        this.mSpecialCharacterNames.put(146, context.getString(2131493826, "2"));
        this.mSpecialCharacterNames.put(147, context.getString(2131493826, "3"));
        this.mSpecialCharacterNames.put(148, context.getString(2131493826, "4"));
        this.mSpecialCharacterNames.put(149, context.getString(2131493826, "5"));
        this.mSpecialCharacterNames.put(150, context.getString(2131493826, "6"));
        this.mSpecialCharacterNames.put(151, context.getString(2131493826, "7"));
        this.mSpecialCharacterNames.put(152, context.getString(2131493826, "8"));
        this.mSpecialCharacterNames.put(153, context.getString(2131493826, "9"));
        this.mSpecialCharacterNames.put(154, context.getString(2131493826, "/"));
        this.mSpecialCharacterNames.put(155, context.getString(2131493826, "*"));
        this.mSpecialCharacterNames.put(156, context.getString(2131493826, "-"));
        this.mSpecialCharacterNames.put(157, context.getString(2131493826, "+"));
        this.mSpecialCharacterNames.put(158, context.getString(2131493826, "."));
        this.mSpecialCharacterNames.put(159, context.getString(2131493826, ","));
        this.mSpecialCharacterNames.put(160, context.getString(2131493826, context.getString(2131493811)));
        this.mSpecialCharacterNames.put(161, context.getString(2131493826, "="));
        this.mSpecialCharacterNames.put(162, context.getString(2131493826, "("));
        this.mSpecialCharacterNames.put(163, context.getString(2131493826, ")"));
        this.mSpecialCharacterNames.put(211, "半角/全角");
        this.mSpecialCharacterNames.put(212, "英数");
        this.mSpecialCharacterNames.put(213, "無変換");
        this.mSpecialCharacterNames.put(214, "変換");
        this.mSpecialCharacterNames.put(215, "かな");
        this.mModifierNames.put(65536, "Meta");
        this.mModifierNames.put(4096, "Ctrl");
        this.mModifierNames.put(2, "Alt");
        this.mModifierNames.put(1, "Shift");
        this.mModifierNames.put(4, "Sym");
        this.mModifierNames.put(8, "Fn");
        this.mSpecialCharacterDrawables.put(67, context.getDrawable(2130837667));
        this.mSpecialCharacterDrawables.put(66, context.getDrawable(2130837669));
        this.mSpecialCharacterDrawables.put(19, context.getDrawable(2130837673));
        this.mSpecialCharacterDrawables.put(22, context.getDrawable(2130837672));
        this.mSpecialCharacterDrawables.put(20, context.getDrawable(2130837668));
        this.mSpecialCharacterDrawables.put(21, context.getDrawable(2130837670));
        this.mModifierDrawables.put(65536, context.getDrawable(2130837671));
    }

    private void populateKeyboardShortcuts(LinearLayout linearLayout, List<KeyboardShortcutGroup> list) {
        LayoutInflater from = LayoutInflater.from(this.mContext);
        int size = list.size();
        TextView textView = (TextView) from.inflate(2130968629, (ViewGroup) null, false);
        textView.measure(0, 0);
        int measuredHeight = textView.getMeasuredHeight();
        int measuredHeight2 = (textView.getMeasuredHeight() - textView.getPaddingTop()) - textView.getPaddingBottom();
        for (int i = 0; i < size; i++) {
            KeyboardShortcutGroup keyboardShortcutGroup = list.get(i);
            TextView textView2 = (TextView) from.inflate(2130968626, (ViewGroup) linearLayout, false);
            textView2.setText(keyboardShortcutGroup.getLabel());
            textView2.setTextColor(keyboardShortcutGroup.isSystemGroup() ? this.mContext.getColor(2131558600) : this.mContext.getColor(2131558601));
            linearLayout.addView(textView2);
            LinearLayout linearLayout2 = (LinearLayout) from.inflate(2130968627, (ViewGroup) linearLayout, false);
            int size2 = keyboardShortcutGroup.getItems().size();
            for (int i2 = 0; i2 < size2; i2++) {
                KeyboardShortcutInfo keyboardShortcutInfo = keyboardShortcutGroup.getItems().get(i2);
                List<StringOrDrawable> humanReadableShortcutKeys = getHumanReadableShortcutKeys(keyboardShortcutInfo);
                if (humanReadableShortcutKeys == null) {
                    Log.w(TAG, "Keyboard Shortcut contains unsupported keys, skipping.");
                } else {
                    View inflate = from.inflate(2130968624, (ViewGroup) linearLayout2, false);
                    if (keyboardShortcutInfo.getIcon() != null) {
                        ImageView imageView = (ImageView) inflate.findViewById(2131886286);
                        imageView.setImageIcon(keyboardShortcutInfo.getIcon());
                        imageView.setVisibility(0);
                    }
                    TextView textView3 = (TextView) inflate.findViewById(2131886287);
                    textView3.setText(keyboardShortcutInfo.getLabel());
                    if (keyboardShortcutInfo.getIcon() != null) {
                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) textView3.getLayoutParams();
                        layoutParams.removeRule(20);
                        textView3.setLayoutParams(layoutParams);
                    }
                    ViewGroup viewGroup = (ViewGroup) inflate.findViewById(2131886288);
                    int size3 = humanReadableShortcutKeys.size();
                    for (int i3 = 0; i3 < size3; i3++) {
                        StringOrDrawable stringOrDrawable = humanReadableShortcutKeys.get(i3);
                        if (stringOrDrawable.drawable != null) {
                            ImageView imageView2 = (ImageView) from.inflate(2130968628, viewGroup, false);
                            Bitmap createBitmap = Bitmap.createBitmap(measuredHeight2, measuredHeight2, Bitmap.Config.ARGB_8888);
                            Canvas canvas = new Canvas(createBitmap);
                            stringOrDrawable.drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                            stringOrDrawable.drawable.draw(canvas);
                            imageView2.setImageBitmap(createBitmap);
                            viewGroup.addView(imageView2);
                        } else if (stringOrDrawable.string != null) {
                            TextView textView4 = (TextView) from.inflate(2130968629, viewGroup, false);
                            textView4.setMinimumWidth(measuredHeight);
                            textView4.setText(stringOrDrawable.string);
                            viewGroup.addView(textView4);
                        }
                    }
                    linearLayout2.addView(inflate);
                }
            }
            linearLayout.addView(linearLayout2);
            if (i < size - 1) {
                linearLayout.addView(from.inflate(2130968625, (ViewGroup) linearLayout, false));
            }
        }
    }

    private void retrieveKeyCharacterMap(int i) {
        InputDevice inputDevice;
        InputManager inputManager = InputManager.getInstance();
        if (i != -1 && (inputDevice = inputManager.getInputDevice(i)) != null) {
            this.mKeyCharacterMap = inputDevice.getKeyCharacterMap();
            return;
        }
        for (int i2 : inputManager.getInputDeviceIds()) {
            InputDevice inputDevice2 = inputManager.getInputDevice(i2);
            if (inputDevice2.getId() != -1 && inputDevice2.isFullKeyboard()) {
                this.mKeyCharacterMap = inputDevice2.getKeyCharacterMap();
                return;
            }
        }
        this.mKeyCharacterMap = inputManager.getInputDevice(-1).getKeyCharacterMap();
    }

    public static void show(Context context, int i) {
        synchronized (sLock) {
            if (sInstance != null && !sInstance.mContext.equals(context)) {
                dismiss();
            }
            getInstance(context).showKeyboardShortcuts(i);
            sIsShowing = true;
        }
    }

    private void showKeyboardShortcuts(int i) {
        retrieveKeyCharacterMap(i);
        Recents.getSystemServices().requestKeyboardShortcuts(this.mContext, new WindowManager.KeyboardShortcutsReceiver(this) { // from class: com.android.systemui.statusbar.KeyboardShortcuts.3
            final KeyboardShortcuts this$0;

            {
                this.this$0 = this;
            }

            public void onKeyboardShortcutsReceived(List<KeyboardShortcutGroup> list) {
                list.add(this.this$0.getSystemShortcuts());
                KeyboardShortcutGroup defaultApplicationShortcuts = this.this$0.getDefaultApplicationShortcuts();
                if (defaultApplicationShortcuts != null) {
                    list.add(defaultApplicationShortcuts);
                }
                this.this$0.showKeyboardShortcutsDialog(list);
            }
        }, i);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showKeyboardShortcutsDialog(List<KeyboardShortcutGroup> list) {
        this.mHandler.post(new Runnable(this, list) { // from class: com.android.systemui.statusbar.KeyboardShortcuts.4
            final KeyboardShortcuts this$0;
            final List val$keyboardShortcutGroups;

            {
                this.this$0 = this;
                this.val$keyboardShortcutGroups = list;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.handleShowKeyboardShortcuts(this.val$keyboardShortcutGroups);
            }
        });
    }

    public static void toggle(Context context, int i) {
        synchronized (sLock) {
            if (sIsShowing) {
                dismiss();
            } else {
                show(context, i);
            }
        }
    }
}

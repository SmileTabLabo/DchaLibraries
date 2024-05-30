package com.android.launcher3.views;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.android.launcher3.BaseDraggingActivity;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.popup.ArrowPopup;
import com.android.launcher3.shortcuts.DeepShortcutView;
import com.android.launcher3.widget.WidgetsFullSheet;
import java.util.ArrayList;
import java.util.List;
/* loaded from: classes.dex */
public class OptionsPopupView extends ArrowPopup implements View.OnClickListener, View.OnLongClickListener {
    private final ArrayMap<View, OptionItem> mItemMap;
    private RectF mTargetRect;

    public OptionsPopupView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public OptionsPopupView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mItemMap = new ArrayMap<>();
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        handleViewClick(view, 0);
    }

    @Override // android.view.View.OnLongClickListener
    public boolean onLongClick(View view) {
        return handleViewClick(view, 1);
    }

    private boolean handleViewClick(View view, int i) {
        OptionItem optionItem = this.mItemMap.get(view);
        if (optionItem == null) {
            return false;
        }
        if (optionItem.mControlTypeForLog > 0) {
            logTap(i, optionItem.mControlTypeForLog);
        }
        if (!optionItem.mClickListener.onLongClick(view)) {
            return false;
        }
        close(true);
        return true;
    }

    private void logTap(int i, int i2) {
        this.mLauncher.getUserEventDispatcher().logActionOnControl(i, i2);
    }

    @Override // com.android.launcher3.util.TouchController
    public boolean onControllerInterceptTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getAction() == 0 && !this.mLauncher.getDragLayer().isEventOverView(this, motionEvent)) {
            close(true);
            return true;
        }
        return false;
    }

    @Override // com.android.launcher3.AbstractFloatingView
    public void logActionCommand(int i) {
    }

    @Override // com.android.launcher3.AbstractFloatingView
    protected boolean isOfType(int i) {
        return (i & 256) != 0;
    }

    @Override // com.android.launcher3.popup.ArrowPopup
    protected void getTargetObjectLocation(Rect rect) {
        this.mTargetRect.roundOut(rect);
    }

    public static void show(Launcher launcher, RectF rectF, List<OptionItem> list) {
        OptionsPopupView optionsPopupView = (OptionsPopupView) launcher.getLayoutInflater().inflate(R.layout.longpress_options_menu, (ViewGroup) launcher.getDragLayer(), false);
        optionsPopupView.mTargetRect = rectF;
        for (OptionItem optionItem : list) {
            DeepShortcutView deepShortcutView = (DeepShortcutView) optionsPopupView.inflateAndAdd(R.layout.system_shortcut, optionsPopupView);
            deepShortcutView.getIconView().setBackgroundResource(optionItem.mIconRes);
            deepShortcutView.getBubbleText().setText(optionItem.mLabelRes);
            deepShortcutView.setDividerVisibility(4);
            deepShortcutView.setOnClickListener(optionsPopupView);
            deepShortcutView.setOnLongClickListener(optionsPopupView);
            optionsPopupView.mItemMap.put(deepShortcutView, optionItem);
        }
        optionsPopupView.reorderAndShow(optionsPopupView.getChildCount());
    }

    public static void showDefaultOptions(Launcher launcher, float f, float f2) {
        float dimension = launcher.getResources().getDimension(R.dimen.options_menu_thumb_size) / 2.0f;
        if (f < 0.0f || f2 < 0.0f) {
            f = launcher.getDragLayer().getWidth() / 2;
            f2 = launcher.getDragLayer().getHeight() / 2;
        }
        RectF rectF = new RectF(f - dimension, f2 - dimension, f + dimension, f2 + dimension);
        ArrayList arrayList = new ArrayList();
        arrayList.add(new OptionItem(R.string.widget_button_text, R.drawable.ic_widget, 2, new View.OnLongClickListener() { // from class: com.android.launcher3.views.-$$Lambda$pbN4MYZT_sQHhmIWZtgqyaKyewM
            @Override // android.view.View.OnLongClickListener
            public final boolean onLongClick(View view) {
                return OptionsPopupView.onWidgetsClicked(view);
            }
        }));
        show(launcher, rectF, arrayList);
    }

    public static boolean onWidgetsClicked(View view) {
        Launcher launcher = Launcher.getLauncher(view.getContext());
        if (launcher.getPackageManager().isSafeMode()) {
            Toast.makeText(launcher, (int) R.string.safemode_widget_error, 0).show();
            return false;
        }
        WidgetsFullSheet.show(launcher, true);
        return true;
    }

    public static boolean startSettings(View view) {
        Launcher launcher = Launcher.getLauncher(view.getContext());
        launcher.startActivity(new Intent("android.intent.action.APPLICATION_PREFERENCES").setPackage(launcher.getPackageName()).addFlags(268435456));
        return true;
    }

    public static boolean startWallpaperPicker(View view) {
        Launcher launcher = Launcher.getLauncher(view.getContext());
        if (!Utilities.isWallpaperAllowed(launcher)) {
            Toast.makeText(launcher, (int) R.string.msg_disabled_by_admin, 0).show();
            return false;
        }
        Intent putExtra = new Intent("android.intent.action.SET_WALLPAPER").putExtra(Utilities.EXTRA_WALLPAPER_OFFSET, launcher.getWorkspace().getWallpaperOffsetForCenterPage());
        putExtra.addFlags(32768);
        String string = launcher.getString(R.string.wallpaper_picker_package);
        if (!TextUtils.isEmpty(string)) {
            putExtra.setPackage(string);
        } else {
            putExtra.putExtra(BaseDraggingActivity.INTENT_EXTRA_IGNORE_LAUNCH_ANIMATION, true);
        }
        return launcher.startActivitySafely(view, putExtra, null);
    }

    /* loaded from: classes.dex */
    public static class OptionItem {
        private final View.OnLongClickListener mClickListener;
        private final int mControlTypeForLog;
        private final int mIconRes;
        private final int mLabelRes;

        public OptionItem(int i, int i2, int i3, View.OnLongClickListener onLongClickListener) {
            this.mLabelRes = i;
            this.mIconRes = i2;
            this.mControlTypeForLog = i3;
            this.mClickListener = onLongClickListener;
        }
    }
}

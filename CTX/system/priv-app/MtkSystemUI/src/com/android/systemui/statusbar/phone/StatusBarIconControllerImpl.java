package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.view.ViewGroup;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.android.systemui.R;
import com.android.systemui.SysUiServiceProvider;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.StatusIconDisplayable;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.phone.StatusBarIconList;
import com.android.systemui.statusbar.phone.StatusBarSignalPolicy;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.IconLogger;
import com.android.systemui.tuner.TunerService;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
/* loaded from: classes.dex */
public class StatusBarIconControllerImpl extends StatusBarIconList implements Dumpable, CommandQueue.Callbacks, StatusBarIconController, ConfigurationController.ConfigurationListener, TunerService.Tunable {
    private Context mContext;
    private final ArraySet<String> mIconBlacklist;
    private final ArrayList<StatusBarIconController.IconManager> mIconGroups;
    private final IconLogger mIconLogger;
    private boolean mIsDark;

    public StatusBarIconControllerImpl(Context context) {
        super(context.getResources().getStringArray(17236037), context);
        this.mIconGroups = new ArrayList<>();
        this.mIconBlacklist = new ArraySet<>();
        this.mIconLogger = (IconLogger) Dependency.get(IconLogger.class);
        this.mIsDark = false;
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this);
        this.mContext = context;
        loadDimens();
        ((CommandQueue) SysUiServiceProvider.getComponent(context, CommandQueue.class)).addCallbacks(this);
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "icon_blacklist");
    }

    @Override // com.android.systemui.statusbar.phone.StatusBarIconController
    public void addIconGroup(StatusBarIconController.IconManager iconManager) {
        this.mIconGroups.add(iconManager);
        ArrayList<StatusBarIconList.Slot> slots = getSlots();
        for (int i = 0; i < slots.size(); i++) {
            StatusBarIconList.Slot slot = slots.get(i);
            List<StatusBarIconHolder> holderListInViewOrder = slot.getHolderListInViewOrder();
            boolean contains = this.mIconBlacklist.contains(slot.getName());
            for (StatusBarIconHolder statusBarIconHolder : holderListInViewOrder) {
                statusBarIconHolder.getTag();
                iconManager.onIconAdded(getViewIndex(getSlotIndex(slot.getName()), statusBarIconHolder.getTag()), slot.getName(), contains, statusBarIconHolder);
            }
        }
    }

    @Override // com.android.systemui.statusbar.phone.StatusBarIconController
    public void removeIconGroup(StatusBarIconController.IconManager iconManager) {
        iconManager.destroy();
        this.mIconGroups.remove(iconManager);
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        if (!"icon_blacklist".equals(str)) {
            return;
        }
        this.mIconBlacklist.clear();
        this.mIconBlacklist.addAll((ArraySet<? extends String>) StatusBarIconController.getIconBlacklist(str2));
        ArrayList<StatusBarIconList.Slot> slots = getSlots();
        ArrayMap arrayMap = new ArrayMap();
        for (int size = slots.size() - 1; size >= 0; size--) {
            StatusBarIconList.Slot slot = slots.get(size);
            arrayMap.put(slot, slot.getHolderListInViewOrder());
            removeAllIconsForSlot(slot.getName());
        }
        for (int i = 0; i < slots.size(); i++) {
            StatusBarIconList.Slot slot2 = slots.get(i);
            List<StatusBarIconHolder> list = (List) arrayMap.get(slot2);
            if (list != null) {
                for (StatusBarIconHolder statusBarIconHolder : list) {
                    setIcon(getSlotIndex(slot2.getName()), statusBarIconHolder);
                }
            }
        }
    }

    private void loadDimens() {
    }

    private void addSystemIcon(int i, final StatusBarIconHolder statusBarIconHolder) {
        final String slotName = getSlotName(i);
        final int viewIndex = getViewIndex(i, statusBarIconHolder.getTag());
        final boolean contains = this.mIconBlacklist.contains(slotName);
        this.mIconLogger.onIconVisibility(getSlotName(i), statusBarIconHolder.isVisible());
        this.mIconGroups.forEach(new Consumer() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$StatusBarIconControllerImpl$fL8PZXISckai-5GwvhWVS3QVTsY
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ((StatusBarIconController.IconManager) obj).onIconAdded(viewIndex, slotName, contains, statusBarIconHolder);
            }
        });
    }

    @Override // com.android.systemui.statusbar.phone.StatusBarIconController
    public void setIcon(String str, int i, CharSequence charSequence) {
        int slotIndex = getSlotIndex(str);
        StatusBarIconHolder icon = getIcon(slotIndex, 0);
        if (icon == null) {
            setIcon(slotIndex, StatusBarIconHolder.fromIcon(new StatusBarIcon(UserHandle.SYSTEM, this.mContext.getPackageName(), Icon.createWithResource(this.mContext, i), 0, 0, charSequence)));
            return;
        }
        icon.getIcon().icon = Icon.createWithResource(this.mContext, i);
        icon.getIcon().contentDescription = charSequence;
        handleSet(slotIndex, icon);
    }

    @Override // com.android.systemui.statusbar.phone.StatusBarIconController
    public void setSignalIcon(String str, StatusBarSignalPolicy.WifiIconState wifiIconState) {
        int slotIndex = getSlotIndex(str);
        if (wifiIconState == null) {
            removeIcon(slotIndex, 0);
            return;
        }
        StatusBarIconHolder icon = getIcon(slotIndex, 0);
        if (icon == null) {
            setIcon(slotIndex, StatusBarIconHolder.fromWifiIconState(wifiIconState));
            return;
        }
        icon.setWifiState(wifiIconState);
        handleSet(slotIndex, icon);
    }

    @Override // com.android.systemui.statusbar.phone.StatusBarIconController
    public void setMobileIcons(String str, List<StatusBarSignalPolicy.MobileIconState> list) {
        StatusBarIconList.Slot slot = getSlot(str);
        int slotIndex = getSlotIndex(str);
        for (StatusBarSignalPolicy.MobileIconState mobileIconState : list) {
            StatusBarIconHolder holderForTag = slot.getHolderForTag(mobileIconState.subId);
            if (holderForTag == null) {
                setIcon(slotIndex, StatusBarIconHolder.fromMobileIconState(mobileIconState));
            } else {
                holderForTag.setMobileState(mobileIconState);
                handleSet(slotIndex, holderForTag);
            }
        }
    }

    @Override // com.android.systemui.statusbar.phone.StatusBarIconController
    public void setExternalIcon(String str) {
        final int viewIndex = getViewIndex(getSlotIndex(str), 0);
        final int dimensionPixelSize = this.mContext.getResources().getDimensionPixelSize(R.dimen.status_bar_icon_drawing_size);
        this.mIconGroups.forEach(new Consumer() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$StatusBarIconControllerImpl$rsmVGSlXlU7ffeIAEgpWeyyu04I
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ((StatusBarIconController.IconManager) obj).onIconExternal(viewIndex, dimensionPixelSize);
            }
        });
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks, com.android.systemui.statusbar.phone.StatusBarIconController
    public void setIcon(String str, StatusBarIcon statusBarIcon) {
        setIcon(getSlotIndex(str), statusBarIcon);
    }

    private void setIcon(int i, StatusBarIcon statusBarIcon) {
        if (statusBarIcon == null) {
            removeAllIconsForSlot(getSlotName(i));
        } else {
            setIcon(i, StatusBarIconHolder.fromIcon(statusBarIcon));
        }
    }

    @Override // com.android.systemui.statusbar.phone.StatusBarIconList
    public void setIcon(int i, StatusBarIconHolder statusBarIconHolder) {
        boolean z = getIcon(i, statusBarIconHolder.getTag()) == null;
        super.setIcon(i, statusBarIconHolder);
        if (z) {
            addSystemIcon(i, statusBarIconHolder);
        } else {
            handleSet(i, statusBarIconHolder);
        }
    }

    @Override // com.android.systemui.statusbar.phone.StatusBarIconController
    public void setIconVisibility(String str, boolean z) {
        int slotIndex = getSlotIndex(str);
        StatusBarIconHolder icon = getIcon(slotIndex, 0);
        if (icon == null || icon.isVisible() == z) {
            return;
        }
        icon.setVisible(z);
        handleSet(slotIndex, icon);
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void removeIcon(String str) {
        removeAllIconsForSlot(str);
    }

    @Override // com.android.systemui.statusbar.phone.StatusBarIconController
    public void removeAllIconsForSlot(String str) {
        StatusBarIconList.Slot slot = getSlot(str);
        if (!slot.hasIconsInSlot()) {
            return;
        }
        this.mIconLogger.onIconHidden(str);
        int slotIndex = getSlotIndex(str);
        for (StatusBarIconHolder statusBarIconHolder : slot.getHolderListInViewOrder()) {
            final int viewIndex = getViewIndex(slotIndex, statusBarIconHolder.getTag());
            slot.removeForTag(statusBarIconHolder.getTag());
            this.mIconGroups.forEach(new Consumer() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$StatusBarIconControllerImpl$uTqaHUAWHbu0P16vDWL0oAyCetk
                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    ((StatusBarIconController.IconManager) obj).onRemoveIcon(viewIndex);
                }
            });
        }
    }

    @Override // com.android.systemui.statusbar.phone.StatusBarIconList
    public void removeIcon(int i, int i2) {
        if (getIcon(i, i2) == null) {
            return;
        }
        this.mIconLogger.onIconHidden(getSlotName(i));
        super.removeIcon(i, i2);
        final int viewIndex = getViewIndex(i, 0);
        this.mIconGroups.forEach(new Consumer() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$StatusBarIconControllerImpl$XIHL8F8kJA04U9X_9IHtSYwXxLU
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ((StatusBarIconController.IconManager) obj).onRemoveIcon(viewIndex);
            }
        });
    }

    private void handleSet(int i, final StatusBarIconHolder statusBarIconHolder) {
        final int viewIndex = getViewIndex(i, statusBarIconHolder.getTag());
        this.mIconLogger.onIconVisibility(getSlotName(i), statusBarIconHolder.isVisible());
        this.mIconGroups.forEach(new Consumer() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$StatusBarIconControllerImpl$ayp5xWywAkBOOSd-6MshVHM8Mms
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ((StatusBarIconController.IconManager) obj).onSetIconHolder(viewIndex, statusBarIconHolder);
            }
        });
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("StatusBarIconController state:");
        Iterator<StatusBarIconController.IconManager> it = this.mIconGroups.iterator();
        while (it.hasNext()) {
            StatusBarIconController.IconManager next = it.next();
            if (next.shouldLog()) {
                ViewGroup viewGroup = next.mGroup;
                int childCount = viewGroup.getChildCount();
                printWriter.println("  icon views: " + childCount);
                for (int i = 0; i < childCount; i++) {
                    printWriter.println("    [" + i + "] icon=" + ((StatusIconDisplayable) viewGroup.getChildAt(i)));
                }
            }
        }
        super.dump(printWriter);
    }

    public void dispatchDemoCommand(String str, Bundle bundle) {
        Iterator<StatusBarIconController.IconManager> it = this.mIconGroups.iterator();
        while (it.hasNext()) {
            StatusBarIconController.IconManager next = it.next();
            if (next.isDemoable()) {
                next.dispatchDemoCommand(str, bundle);
            }
        }
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onDensityOrFontScaleChanged() {
        loadDimens();
    }
}

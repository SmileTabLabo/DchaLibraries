package com.android.systemui.tuner;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v14.preference.PreferenceFragment;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.plugins.IntentButtonProvider;
import com.android.systemui.statusbar.ScalingDrawableWrapper;
import com.android.systemui.statusbar.phone.ExpandableIndicator;
import com.android.systemui.statusbar.policy.ExtensionController;
import com.android.systemui.tuner.LockscreenFragment;
import com.android.systemui.tuner.ShortcutParser;
import com.android.systemui.tuner.TunerService;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Consumer;
/* loaded from: classes.dex */
public class LockscreenFragment extends PreferenceFragment {
    private Handler mHandler;
    private final ArrayList<TunerService.Tunable> mTunables = new ArrayList<>();
    private TunerService mTunerService;

    @Override // android.support.v14.preference.PreferenceFragment
    public void onCreatePreferences(Bundle bundle, String str) {
        this.mTunerService = (TunerService) Dependency.get(TunerService.class);
        this.mHandler = new Handler();
        addPreferencesFromResource(R.xml.lockscreen_settings);
        setupGroup("sysui_keyguard_left", "sysui_keyguard_left_unlock");
        setupGroup("sysui_keyguard_right", "sysui_keyguard_right_unlock");
    }

    @Override // android.app.Fragment
    public void onDestroy() {
        super.onDestroy();
        this.mTunables.forEach(new Consumer() { // from class: com.android.systemui.tuner.-$$Lambda$LockscreenFragment$Lo7jOQgOiEZ4M1LxVUxyoD69g0s
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                LockscreenFragment.this.mTunerService.removeTunable((TunerService.Tunable) obj);
            }
        });
    }

    private void setupGroup(String str, String str2) {
        final Preference findPreference = findPreference(str);
        final SwitchPreference switchPreference = (SwitchPreference) findPreference(str2);
        addTunable(new TunerService.Tunable() { // from class: com.android.systemui.tuner.-$$Lambda$LockscreenFragment$0AVJL9CTzprG2BMD2je5SHaUt-w
            @Override // com.android.systemui.tuner.TunerService.Tunable
            public final void onTuningChanged(String str3, String str4) {
                LockscreenFragment.lambda$setupGroup$1(LockscreenFragment.this, switchPreference, findPreference, str3, str4);
            }
        }, str);
    }

    public static /* synthetic */ void lambda$setupGroup$1(LockscreenFragment lockscreenFragment, SwitchPreference switchPreference, Preference preference, String str, String str2) {
        switchPreference.setVisible(!TextUtils.isEmpty(str2));
        lockscreenFragment.setSummary(preference, str2);
    }

    private void setSummary(Preference preference, String str) {
        if (str == null) {
            preference.setSummary(R.string.lockscreen_none);
            return;
        }
        if (str.contains("::")) {
            ShortcutParser.Shortcut shortcutInfo = getShortcutInfo(getContext(), str);
            preference.setSummary(shortcutInfo != null ? shortcutInfo.label : null);
        } else if (str.contains("/")) {
            ActivityInfo activityinfo = getActivityinfo(getContext(), str);
            preference.setSummary(activityinfo != null ? activityinfo.loadLabel(getContext().getPackageManager()) : null);
        } else {
            preference.setSummary(R.string.lockscreen_none);
        }
    }

    private void addTunable(TunerService.Tunable tunable, String... strArr) {
        this.mTunables.add(tunable);
        this.mTunerService.addTunable(tunable, strArr);
    }

    public static ActivityInfo getActivityinfo(Context context, String str) {
        try {
            return context.getPackageManager().getActivityInfo(ComponentName.unflattenFromString(str), 0);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public static ShortcutParser.Shortcut getShortcutInfo(Context context, String str) {
        return ShortcutParser.Shortcut.create(context, str);
    }

    /* loaded from: classes.dex */
    public static class Holder extends RecyclerView.ViewHolder {
        public final ExpandableIndicator expand;
        public final ImageView icon;
        public final TextView title;

        public Holder(View view) {
            super(view);
            this.icon = (ImageView) view.findViewById(16908294);
            this.title = (TextView) view.findViewById(16908310);
            this.expand = (ExpandableIndicator) view.findViewById(R.id.expand);
        }
    }

    /* loaded from: classes.dex */
    private static class StaticShortcut extends Item {
        private final Context mContext;
        private final ShortcutParser.Shortcut mShortcut;

        @Override // com.android.systemui.tuner.LockscreenFragment.Item
        public Drawable getDrawable() {
            return this.mShortcut.icon.loadDrawable(this.mContext);
        }

        @Override // com.android.systemui.tuner.LockscreenFragment.Item
        public String getLabel() {
            return this.mShortcut.label;
        }

        @Override // com.android.systemui.tuner.LockscreenFragment.Item
        public Boolean getExpando() {
            return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class App extends Item {
        private final ArrayList<Item> mChildren;
        private final Context mContext;
        private boolean mExpanded;
        private final LauncherActivityInfo mInfo;

        @Override // com.android.systemui.tuner.LockscreenFragment.Item
        public Drawable getDrawable() {
            return this.mInfo.getBadgedIcon(this.mContext.getResources().getConfiguration().densityDpi);
        }

        @Override // com.android.systemui.tuner.LockscreenFragment.Item
        public String getLabel() {
            return this.mInfo.getLabel().toString();
        }

        @Override // com.android.systemui.tuner.LockscreenFragment.Item
        public Boolean getExpando() {
            if (this.mChildren.size() != 0) {
                return Boolean.valueOf(this.mExpanded);
            }
            return null;
        }

        @Override // com.android.systemui.tuner.LockscreenFragment.Item
        public void toggleExpando(final Adapter adapter) {
            this.mExpanded = !this.mExpanded;
            if (this.mExpanded) {
                this.mChildren.forEach(new Consumer() { // from class: com.android.systemui.tuner.-$$Lambda$LockscreenFragment$App$ETExpSuIeTllbJ9AB_3DTGOAJgk
                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        adapter.addItem(LockscreenFragment.App.this, (LockscreenFragment.Item) obj);
                    }
                });
            } else {
                this.mChildren.forEach(new Consumer() { // from class: com.android.systemui.tuner.-$$Lambda$LockscreenFragment$App$KymmDZF-Q8mj0Qr5uc4akrkgskU
                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        LockscreenFragment.Adapter.this.remItem((LockscreenFragment.Item) obj);
                    }
                });
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static abstract class Item {
        public abstract Drawable getDrawable();

        public abstract Boolean getExpando();

        public abstract String getLabel();

        private Item() {
        }

        public void toggleExpando(Adapter adapter) {
        }
    }

    /* loaded from: classes.dex */
    public static class Adapter extends RecyclerView.Adapter<Holder> {
        private final Consumer<Item> mCallback;
        private ArrayList<Item> mItems;

        @Override // android.support.v7.widget.RecyclerView.Adapter
        public Holder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new Holder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.tuner_shortcut_item, viewGroup, false));
        }

        @Override // android.support.v7.widget.RecyclerView.Adapter
        public void onBindViewHolder(final Holder holder, int i) {
            Item item = this.mItems.get(i);
            holder.icon.setImageDrawable(item.getDrawable());
            holder.title.setText(item.getLabel());
            holder.itemView.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.tuner.-$$Lambda$LockscreenFragment$Adapter$VuIE2eL9-LHOyBflZw_Px7xwF04
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    r0.mCallback.accept(LockscreenFragment.Adapter.this.mItems.get(holder.getAdapterPosition()));
                }
            });
            Boolean expando = item.getExpando();
            if (expando != null) {
                holder.expand.setVisibility(0);
                holder.expand.setExpanded(expando.booleanValue());
                holder.expand.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.tuner.-$$Lambda$LockscreenFragment$Adapter$fS6IuUEavDgpMOkDZLNh46UcUNQ
                    @Override // android.view.View.OnClickListener
                    public final void onClick(View view) {
                        r0.mItems.get(holder.getAdapterPosition()).toggleExpando(LockscreenFragment.Adapter.this);
                    }
                });
                return;
            }
            holder.expand.setVisibility(8);
        }

        @Override // android.support.v7.widget.RecyclerView.Adapter
        public int getItemCount() {
            return this.mItems.size();
        }

        public void remItem(Item item) {
            int indexOf = this.mItems.indexOf(item);
            this.mItems.remove(item);
            notifyItemRemoved(indexOf);
        }

        public void addItem(Item item, Item item2) {
            int indexOf = this.mItems.indexOf(item) + 1;
            this.mItems.add(indexOf, item2);
            notifyItemInserted(indexOf);
        }
    }

    /* loaded from: classes.dex */
    public static class LockButtonFactory implements ExtensionController.TunerFactory<IntentButtonProvider.IntentButton> {
        private final Context mContext;
        private final String mKey;

        @Override // com.android.systemui.statusbar.policy.ExtensionController.TunerFactory
        public /* bridge */ /* synthetic */ IntentButtonProvider.IntentButton create(Map map) {
            return create((Map<String, String>) map);
        }

        public LockButtonFactory(Context context, String str) {
            this.mContext = context;
            this.mKey = str;
        }

        @Override // com.android.systemui.statusbar.policy.ExtensionController.TunerFactory
        public String[] keys() {
            return new String[]{this.mKey};
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // com.android.systemui.statusbar.policy.ExtensionController.TunerFactory
        public IntentButtonProvider.IntentButton create(Map<String, String> map) {
            ActivityInfo activityinfo;
            String str = map.get(this.mKey);
            if (!TextUtils.isEmpty(str)) {
                if (str.contains("::")) {
                    ShortcutParser.Shortcut shortcutInfo = LockscreenFragment.getShortcutInfo(this.mContext, str);
                    if (shortcutInfo != null) {
                        return new ShortcutButton(this.mContext, shortcutInfo);
                    }
                    return null;
                } else if (str.contains("/") && (activityinfo = LockscreenFragment.getActivityinfo(this.mContext, str)) != null) {
                    return new ActivityButton(this.mContext, activityinfo);
                } else {
                    return null;
                }
            }
            return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class ShortcutButton implements IntentButtonProvider.IntentButton {
        private final IntentButtonProvider.IntentButton.IconState mIconState = new IntentButtonProvider.IntentButton.IconState();
        private final ShortcutParser.Shortcut mShortcut;

        public ShortcutButton(Context context, ShortcutParser.Shortcut shortcut) {
            this.mShortcut = shortcut;
            this.mIconState.isVisible = true;
            this.mIconState.drawable = shortcut.icon.loadDrawable(context).mutate();
            this.mIconState.contentDescription = this.mShortcut.label;
            this.mIconState.drawable = new ScalingDrawableWrapper(this.mIconState.drawable, ((int) TypedValue.applyDimension(1, 32.0f, context.getResources().getDisplayMetrics())) / this.mIconState.drawable.getIntrinsicWidth());
            this.mIconState.tint = false;
        }

        @Override // com.android.systemui.plugins.IntentButtonProvider.IntentButton
        public IntentButtonProvider.IntentButton.IconState getIcon() {
            return this.mIconState;
        }

        @Override // com.android.systemui.plugins.IntentButtonProvider.IntentButton
        public Intent getIntent() {
            return this.mShortcut.intent;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class ActivityButton implements IntentButtonProvider.IntentButton {
        private final IntentButtonProvider.IntentButton.IconState mIconState = new IntentButtonProvider.IntentButton.IconState();
        private final Intent mIntent;

        public ActivityButton(Context context, ActivityInfo activityInfo) {
            this.mIntent = new Intent().setComponent(new ComponentName(activityInfo.packageName, activityInfo.name));
            this.mIconState.isVisible = true;
            this.mIconState.drawable = activityInfo.loadIcon(context.getPackageManager()).mutate();
            this.mIconState.contentDescription = activityInfo.loadLabel(context.getPackageManager());
            this.mIconState.drawable = new ScalingDrawableWrapper(this.mIconState.drawable, ((int) TypedValue.applyDimension(1, 32.0f, context.getResources().getDisplayMetrics())) / this.mIconState.drawable.getIntrinsicWidth());
            this.mIconState.tint = false;
        }

        @Override // com.android.systemui.plugins.IntentButtonProvider.IntentButton
        public IntentButtonProvider.IntentButton.IconState getIcon() {
            return this.mIconState;
        }

        @Override // com.android.systemui.plugins.IntentButtonProvider.IntentButton
        public Intent getIntent() {
            return this.mIntent;
        }
    }
}

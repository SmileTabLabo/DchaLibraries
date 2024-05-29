package com.android.systemui.tuner;

import android.R;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import com.android.systemui.statusbar.phone.NavigationBarInflaterView;
import com.android.systemui.tuner.KeycodeSelectionHelper;
import com.android.systemui.tuner.TunerService;
import java.util.ArrayList;
import java.util.List;
/* loaded from: a.zip:com/android/systemui/tuner/NavBarTuner.class */
public class NavBarTuner extends Fragment implements TunerService.Tunable {
    private NavBarAdapter mNavBarAdapter;
    private PreviewNavInflater mPreview;

    /* loaded from: a.zip:com/android/systemui/tuner/NavBarTuner$Dividers.class */
    private static class Dividers extends RecyclerView.ItemDecoration {
        private final Drawable mDivider;

        public Dividers(Context context) {
            TypedValue typedValue = new TypedValue();
            context.getTheme().resolveAttribute(16843284, typedValue, true);
            this.mDivider = context.getDrawable(typedValue.resourceId);
        }

        @Override // android.support.v7.widget.RecyclerView.ItemDecoration
        public void onDraw(Canvas canvas, RecyclerView recyclerView, RecyclerView.State state) {
            super.onDraw(canvas, recyclerView, state);
            int paddingLeft = recyclerView.getPaddingLeft();
            int width = recyclerView.getWidth();
            int paddingRight = recyclerView.getPaddingRight();
            int childCount = recyclerView.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childAt = recyclerView.getChildAt(i);
                int bottom = childAt.getBottom() + ((RecyclerView.LayoutParams) childAt.getLayoutParams()).bottomMargin;
                this.mDivider.setBounds(paddingLeft, bottom, width - paddingRight, bottom + this.mDivider.getIntrinsicHeight());
                this.mDivider.draw(canvas);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/tuner/NavBarTuner$Holder.class */
    public static class Holder extends RecyclerView.ViewHolder {
        private TextView title;

        public Holder(View view) {
            super(view);
            this.title = (TextView) view.findViewById(16908310);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/tuner/NavBarTuner$NavBarAdapter.class */
    public class NavBarAdapter extends RecyclerView.Adapter<Holder> implements View.OnClickListener {
        private int mButtonLayout;
        private int mCategoryLayout;
        private int mKeycode;
        private ItemTouchHelper mTouchHelper;
        final NavBarTuner this$0;
        private List<String> mButtons = new ArrayList();
        private List<CharSequence> mLabels = new ArrayList();
        private final ItemTouchHelper.Callback mCallbacks = new ItemTouchHelper.Callback(this) { // from class: com.android.systemui.tuner.NavBarTuner.NavBarAdapter.1
            final NavBarAdapter this$1;

            {
                this.this$1 = this;
            }

            private <T> void move(int i, int i2, List<T> list) {
                list.add(i > i2 ? i2 : i2 + 1, list.get(i));
                int i3 = i;
                if (i > i2) {
                    i3 = i + 1;
                }
                list.remove(i3);
            }

            @Override // android.support.v7.widget.helper.ItemTouchHelper.Callback
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                return viewHolder.getItemViewType() != 1 ? makeMovementFlags(0, 0) : makeMovementFlags(3, 0);
            }

            @Override // android.support.v7.widget.helper.ItemTouchHelper.Callback
            public boolean isItemViewSwipeEnabled() {
                return false;
            }

            @Override // android.support.v7.widget.helper.ItemTouchHelper.Callback
            public boolean isLongPressDragEnabled() {
                return false;
            }

            @Override // android.support.v7.widget.helper.ItemTouchHelper.Callback
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder viewHolder2) {
                int adapterPosition = viewHolder.getAdapterPosition();
                int adapterPosition2 = viewHolder2.getAdapterPosition();
                if (adapterPosition2 == 0) {
                    return false;
                }
                move(adapterPosition, adapterPosition2, this.this$1.mButtons);
                move(adapterPosition, adapterPosition2, this.this$1.mLabels);
                this.this$1.this$0.notifyChanged();
                this.this$1.notifyItemMoved(adapterPosition, adapterPosition2);
                return true;
            }

            @Override // android.support.v7.widget.helper.ItemTouchHelper.Callback
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int i) {
            }
        };

        public NavBarAdapter(NavBarTuner navBarTuner, Context context) {
            this.this$0 = navBarTuner;
            this.mButtonLayout = context.getTheme().obtainStyledAttributes(null, R.styleable.Preference, 16842894, 0).getResourceId(3, 0);
            this.mCategoryLayout = context.getTheme().obtainStyledAttributes(null, R.styleable.Preference, 16842892, 0).getResourceId(3, 0);
        }

        private void bindAdd(Holder holder) {
            TypedValue typedValue = new TypedValue();
            Context context = holder.itemView.getContext();
            context.getTheme().resolveAttribute(16843829, typedValue, true);
            ImageView imageView = (ImageView) holder.itemView.findViewById(16908294);
            imageView.setImageResource(2130837620);
            imageView.setImageTintList(ColorStateList.valueOf(context.getColor(typedValue.resourceId)));
            holder.itemView.findViewById(16908304).setVisibility(8);
            holder.itemView.setClickable(true);
            holder.itemView.setOnClickListener(new View.OnClickListener(this) { // from class: com.android.systemui.tuner.NavBarTuner.NavBarAdapter.2
                final NavBarAdapter this$1;

                {
                    this.this$1 = this;
                }

                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    this.this$1.showAddDialog(view.getContext());
                }
            });
        }

        private void bindButton(Holder holder, int i) {
            holder.itemView.findViewById(16908350).setVisibility(8);
            holder.itemView.findViewById(16908304).setVisibility(8);
            bindClick(holder.itemView.findViewById(2131886535), holder);
            bindClick(holder.itemView.findViewById(2131886534), holder);
            holder.itemView.findViewById(2131886536).setOnTouchListener(new View.OnTouchListener(this, holder) { // from class: com.android.systemui.tuner.NavBarTuner.NavBarAdapter.3
                final NavBarAdapter this$1;
                final Holder val$holder;

                {
                    this.this$1 = this;
                    this.val$holder = holder;
                }

                @Override // android.view.View.OnTouchListener
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    this.this$1.mTouchHelper.startDrag(this.val$holder);
                    return true;
                }
            });
        }

        private void bindClick(View view, Holder holder) {
            view.setOnClickListener(this);
            view.setTag(holder);
        }

        private int getLayoutId(int i) {
            return i == 2 ? this.mCategoryLayout : this.mButtonLayout;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void onImageSelected(Uri uri) {
            int size = this.mButtons.size() - 1;
            this.mButtons.add(size, "key(" + this.mKeycode + ":" + uri.toString() + ")");
            this.mLabels.add(size, NavBarTuner.getLabel("key", this.this$0.getContext()));
            notifyItemInserted(size);
            this.this$0.notifyChanged();
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void showAddDialog(Context context) {
            String[] strArr = {"back", "home", "recent", "menu_ime", "space", "clipboard", "key"};
            CharSequence[] charSequenceArr = new CharSequence[strArr.length];
            for (int i = 0; i < strArr.length; i++) {
                charSequenceArr[i] = NavBarTuner.getLabel(strArr[i], context);
            }
            new AlertDialog.Builder(context).setTitle(2131493863).setItems(charSequenceArr, new DialogInterface.OnClickListener(this, strArr, context, charSequenceArr) { // from class: com.android.systemui.tuner.NavBarTuner.NavBarAdapter.4
                final NavBarAdapter this$1;
                final Context val$context;
                final CharSequence[] val$labels;
                final String[] val$options;

                {
                    this.this$1 = this;
                    this.val$options = strArr;
                    this.val$context = context;
                    this.val$labels = charSequenceArr;
                }

                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i2) {
                    if ("key".equals(this.val$options[i2])) {
                        this.this$1.showKeyDialogs(this.val$context);
                        return;
                    }
                    int size = this.this$1.mButtons.size() - 1;
                    this.this$1.showAddedMessage(this.val$context, this.val$options[i2]);
                    this.this$1.mButtons.add(size, this.val$options[i2]);
                    this.this$1.mLabels.add(size, this.val$labels[i2]);
                    this.this$1.notifyItemInserted(size);
                    this.this$1.this$0.notifyChanged();
                }
            }).setNegativeButton(17039360, (DialogInterface.OnClickListener) null).show();
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void showAddedMessage(Context context, String str) {
            if ("clipboard".equals(str)) {
                new AlertDialog.Builder(context).setTitle(2131493870).setMessage(2131493871).setPositiveButton(17039370, (DialogInterface.OnClickListener) null).show();
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void showKeyDialogs(Context context) {
            new AlertDialog.Builder(context).setTitle(2131493873).setMessage(2131493874).setPositiveButton(17039370, new DialogInterface.OnClickListener(this, context, new KeycodeSelectionHelper.OnSelectionComplete(this) { // from class: com.android.systemui.tuner.NavBarTuner.NavBarAdapter.5
                final NavBarAdapter this$1;

                {
                    this.this$1 = this;
                }

                @Override // com.android.systemui.tuner.KeycodeSelectionHelper.OnSelectionComplete
                public void onSelectionComplete(int i) {
                    this.this$1.mKeycode = i;
                    this.this$1.this$0.selectImage();
                }
            }) { // from class: com.android.systemui.tuner.NavBarTuner.NavBarAdapter.6
                final NavBarAdapter this$1;
                final Context val$context;
                final KeycodeSelectionHelper.OnSelectionComplete val$listener;

                {
                    this.this$1 = this;
                    this.val$context = context;
                    this.val$listener = r6;
                }

                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i) {
                    KeycodeSelectionHelper.showKeycodeSelect(this.val$context, this.val$listener);
                }
            }).show();
        }

        private void showWidthDialog(Holder holder, Context context) {
            String str = this.mButtons.get(holder.getAdapterPosition());
            float extractSize = NavigationBarInflaterView.extractSize(str);
            AlertDialog create = new AlertDialog.Builder(context).setTitle(2131493869).setView(2130968717).setNegativeButton(17039360, (DialogInterface.OnClickListener) null).create();
            create.setButton(-1, context.getString(17039370), new DialogInterface.OnClickListener(this, str, create, holder) { // from class: com.android.systemui.tuner.NavBarTuner.NavBarAdapter.7
                final NavBarAdapter this$1;
                final String val$buttonSpec;
                final AlertDialog val$dialog;
                final Holder val$holder;

                {
                    this.this$1 = this;
                    this.val$buttonSpec = str;
                    this.val$dialog = create;
                    this.val$holder = holder;
                }

                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i) {
                    String extractButton = NavigationBarInflaterView.extractButton(this.val$buttonSpec);
                    SeekBar seekBar = (SeekBar) this.val$dialog.findViewById(2131886537);
                    if (seekBar.getProgress() == 75) {
                        this.this$1.mButtons.set(this.val$holder.getAdapterPosition(), extractButton);
                    } else {
                        this.this$1.mButtons.set(this.val$holder.getAdapterPosition(), extractButton + "[" + ((seekBar.getProgress() + 25) / 100.0f) + "]");
                    }
                    this.this$1.this$0.notifyChanged();
                }
            });
            create.show();
            SeekBar seekBar = (SeekBar) create.findViewById(2131886537);
            seekBar.setMax(150);
            seekBar.setProgress((int) ((extractSize - 0.25f) * 100.0f));
        }

        public void addButton(String str, CharSequence charSequence) {
            this.mButtons.add(str);
            this.mLabels.add(charSequence);
            notifyItemInserted(this.mLabels.size() - 1);
            this.this$0.notifyChanged();
        }

        public void clear() {
            this.mButtons.clear();
            this.mLabels.clear();
            notifyDataSetChanged();
        }

        @Override // android.support.v7.widget.RecyclerView.Adapter
        public int getItemCount() {
            return this.mButtons.size();
        }

        @Override // android.support.v7.widget.RecyclerView.Adapter
        public int getItemViewType(int i) {
            String str = this.mButtons.get(i);
            if (str.equals("start") || str.equals("center") || str.equals("end")) {
                return 2;
            }
            return str.equals("add") ? 0 : 1;
        }

        public String getNavString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < this.mButtons.size() - 1; i++) {
                String str = this.mButtons.get(i);
                if (str.equals("center") || str.equals("end")) {
                    if (sb.length() == 0 || sb.toString().endsWith(";")) {
                        sb.append("space");
                    }
                    sb.append(";");
                } else {
                    if (sb.length() != 0 && !sb.toString().endsWith(";")) {
                        sb.append(",");
                    }
                    sb.append(str);
                }
            }
            if (sb.toString().endsWith(";")) {
                sb.append("space");
            }
            return sb.toString();
        }

        public boolean hasHomeButton() {
            int size = this.mButtons.size();
            for (int i = 0; i < size; i++) {
                if (this.mButtons.get(i).startsWith("home")) {
                    return true;
                }
            }
            return false;
        }

        @Override // android.support.v7.widget.RecyclerView.Adapter
        public void onBindViewHolder(Holder holder, int i) {
            holder.title.setText(this.mLabels.get(i));
            if (holder.getItemViewType() == 1) {
                bindButton(holder, i);
            } else if (holder.getItemViewType() == 0) {
                bindAdd(holder);
            }
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            Holder holder = (Holder) view.getTag();
            if (view.getId() == 2131886534) {
                showWidthDialog(holder, view.getContext());
            } else if (view.getId() == 2131886535) {
                int adapterPosition = holder.getAdapterPosition();
                this.mButtons.remove(adapterPosition);
                this.mLabels.remove(adapterPosition);
                notifyItemRemoved(adapterPosition);
                this.this$0.notifyChanged();
            }
        }

        @Override // android.support.v7.widget.RecyclerView.Adapter
        public Holder onCreateViewHolder(ViewGroup viewGroup, int i) {
            LayoutInflater from = LayoutInflater.from(viewGroup.getContext());
            View inflate = from.inflate(getLayoutId(i), viewGroup, false);
            if (i == 1) {
                from.inflate(2130968715, (ViewGroup) inflate.findViewById(16908312));
            }
            return new Holder(inflate);
        }

        public void setTouchHelper(ItemTouchHelper itemTouchHelper) {
            this.mTouchHelper = itemTouchHelper;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static CharSequence getLabel(String str, Context context) {
        return str.startsWith("home") ? context.getString(2131493348) : str.startsWith("back") ? context.getString(2131493347) : str.startsWith("recent") ? context.getString(2131493350) : str.startsWith("space") ? context.getString(2131493861) : str.startsWith("menu_ime") ? context.getString(2131493862) : str.startsWith("clipboard") ? context.getString(2131493870) : str.startsWith("key") ? context.getString(2131493873) : str;
    }

    private void inflatePreview(ViewGroup viewGroup) {
        Display defaultDisplay = getActivity().getWindowManager().getDefaultDisplay();
        boolean z = defaultDisplay.getRotation() != 1 ? defaultDisplay.getRotation() == 3 : true;
        Configuration configuration = new Configuration(getContext().getResources().getConfiguration());
        boolean z2 = z && configuration.smallestScreenWidthDp < 600;
        float f = z2 ? 0.75f : 0.95f;
        configuration.densityDpi = (int) (configuration.densityDpi * f);
        this.mPreview = (PreviewNavInflater) LayoutInflater.from(getContext().createConfigurationContext(configuration)).inflate(2130968714, viewGroup, false);
        ViewGroup.LayoutParams layoutParams = this.mPreview.getLayoutParams();
        layoutParams.width = (int) ((z2 ? defaultDisplay.getHeight() : defaultDisplay.getWidth()) * f);
        layoutParams.height = (int) (layoutParams.height * f);
        if (z2) {
            int i = layoutParams.width;
            layoutParams.width = layoutParams.height;
            layoutParams.height = i;
        }
        viewGroup.addView(this.mPreview);
        if (z) {
            this.mPreview.findViewById(2131886532).setVisibility(8);
            this.mPreview.findViewById(2131886533);
            return;
        }
        this.mPreview.findViewById(2131886533).setVisibility(8);
        this.mPreview.findViewById(2131886532);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void notifyChanged() {
        this.mPreview.onTuningChanged("sysui_nav_bar", this.mNavBarAdapter.getNavString());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void selectImage() {
        startActivityForResult(KeycodeSelectionHelper.getSelectImageIntent(), 42);
    }

    @Override // android.app.Fragment
    public void onActivityResult(int i, int i2, Intent intent) {
        if (i != 42 || i2 != -1 || intent == null) {
            super.onActivityResult(i, i2, intent);
            return;
        }
        Uri data = intent.getData();
        getContext().getContentResolver().takePersistableUriPermission(data, intent.getFlags() & 1);
        this.mNavBarAdapter.onImageSelected(data);
    }

    @Override // android.app.Fragment
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);
        menu.add(0, 2, 0, getString(2131493865)).setShowAsAction(1);
        menu.add(0, 3, 0, getString(2131493866));
    }

    @Override // android.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View inflate = layoutInflater.inflate(2130968713, viewGroup, false);
        inflatePreview((ViewGroup) inflate.findViewById(2131886531));
        return inflate;
    }

    @Override // android.app.Fragment
    public void onDestroyView() {
        super.onDestroyView();
        TunerService.get(getContext()).removeTunable(this);
    }

    @Override // android.app.Fragment
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() != 2) {
            if (menuItem.getItemId() == 3) {
                Settings.Secure.putString(getContext().getContentResolver(), "sysui_nav_bar", null);
                return true;
            }
            return super.onOptionsItemSelected(menuItem);
        } else if (this.mNavBarAdapter.hasHomeButton()) {
            Settings.Secure.putString(getContext().getContentResolver(), "sysui_nav_bar", this.mNavBarAdapter.getNavString());
            return true;
        } else {
            new AlertDialog.Builder(getContext()).setTitle(2131493867).setMessage(2131493868).setPositiveButton(17039370, (DialogInterface.OnClickListener) null).show();
            return true;
        }
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        String[] split;
        if ("sysui_nav_bar".equals(str)) {
            Context context = getContext();
            String str3 = str2;
            if (str2 == null) {
                str3 = context.getString(2131493281);
            }
            String[] split2 = str3.split(";");
            String string = getString(2131493858);
            String string2 = getString(2131493859);
            String string3 = getString(2131493860);
            this.mNavBarAdapter.clear();
            for (int i = 0; i < 3; i++) {
                this.mNavBarAdapter.addButton(new String[]{"start", "center", "end"}[i], new String[]{string, string2, string3}[i]);
                for (String str4 : split2[i].split(",")) {
                    this.mNavBarAdapter.addButton(str4, getLabel(str4, context));
                }
            }
            this.mNavBarAdapter.addButton("add", getString(2131493864));
            setHasOptionsMenu(true);
        }
    }

    @Override // android.app.Fragment
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(16908298);
        Context context = getContext();
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        this.mNavBarAdapter = new NavBarAdapter(this, context);
        recyclerView.setAdapter(this.mNavBarAdapter);
        recyclerView.addItemDecoration(new Dividers(context));
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(this.mNavBarAdapter.mCallbacks);
        this.mNavBarAdapter.setTouchHelper(itemTouchHelper);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        TunerService.get(getContext()).addTunable(this, "sysui_nav_bar");
    }
}

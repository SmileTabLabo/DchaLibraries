package com.android.settings.sim;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import com.android.settings.R;
import com.mediatek.settings.UtilsExt;
import com.mediatek.settings.ext.ISettingsMiscExt;
import com.mediatek.settings.ext.ISimManagementExt;
import com.mediatek.settings.sim.SimHotSwapHandler;
/* loaded from: classes.dex */
public class SimPreferenceDialog extends Activity {
    private final String SIM_NAME = "sim_name";
    private final String TINT_POS = "tint_pos";
    AlertDialog.Builder mBuilder;
    private String[] mColorStrings;
    private Context mContext;
    private Dialog mDialog;
    View mDialogLayout;
    private ISettingsMiscExt mMiscExt;
    private SimHotSwapHandler mSimHotSwapHandler;
    private ISimManagementExt mSimManagementExt;
    private int mSlotId;
    private SubscriptionInfo mSubInfoRecord;
    private SubscriptionManager mSubscriptionManager;
    private int[] mTintArr;
    private int mTintSelectorPos;

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mContext = this;
        Bundle extras = getIntent().getExtras();
        this.mSlotId = extras.getInt("slot_id", -1);
        this.mSubscriptionManager = SubscriptionManager.from(this.mContext);
        this.mSubInfoRecord = this.mSubscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(this.mSlotId);
        if (this.mSubInfoRecord == null) {
            Log.w("SimPreferenceDialog", "mSubInfoRecord is null, finish the activity");
            finish();
            return;
        }
        this.mTintArr = this.mContext.getResources().getIntArray(17235979);
        this.mColorStrings = this.mContext.getResources().getStringArray(R.array.color_picker);
        this.mTintSelectorPos = 0;
        this.mBuilder = new AlertDialog.Builder(this.mContext);
        LayoutInflater inflater = (LayoutInflater) this.mContext.getSystemService("layout_inflater");
        this.mDialogLayout = inflater.inflate(R.layout.multi_sim_dialog, (ViewGroup) null);
        this.mBuilder.setView(this.mDialogLayout);
        this.mMiscExt = UtilsExt.getMiscPlugin(getApplicationContext());
        this.mSimManagementExt = UtilsExt.getSimManagmentExtPlugin(getApplicationContext());
        createEditDialog(bundle);
        this.mSimHotSwapHandler = new SimHotSwapHandler(getApplicationContext());
        this.mSimHotSwapHandler.registerOnSimHotSwap(new SimHotSwapHandler.OnSimHotSwapListener() { // from class: com.android.settings.sim.SimPreferenceDialog.1
            @Override // com.mediatek.settings.sim.SimHotSwapHandler.OnSimHotSwapListener
            public void onSimHotSwap() {
                Log.d("SimPreferenceDialog", "onSimHotSwap, finish Activity~~");
                SimPreferenceDialog.this.finish();
            }
        });
    }

    @Override // android.app.Activity
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt("tint_pos", this.mTintSelectorPos);
        EditText nameText = (EditText) this.mDialogLayout.findViewById(R.id.sim_name);
        savedInstanceState.putString("sim_name", nameText.getText().toString());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override // android.app.Activity
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int pos = savedInstanceState.getInt("tint_pos");
        Spinner tintSpinner = (Spinner) this.mDialogLayout.findViewById(R.id.spinner);
        tintSpinner.setSelection(pos);
        this.mTintSelectorPos = pos;
        EditText nameText = (EditText) this.mDialogLayout.findViewById(R.id.sim_name);
        nameText.setText(savedInstanceState.getString("sim_name"));
    }

    private void createEditDialog(Bundle bundle) {
        Resources res = this.mContext.getResources();
        EditText nameText = (EditText) this.mDialogLayout.findViewById(R.id.sim_name);
        nameText.setText(this.mSubInfoRecord.getDisplayName());
        customizeSimNameTitle(this.mDialogLayout);
        final Spinner tintSpinner = (Spinner) this.mDialogLayout.findViewById(R.id.spinner);
        SelectColorAdapter adapter = new SelectColorAdapter(this.mContext, R.layout.settings_color_picker_item, this.mColorStrings);
        adapter.setDropDownViewResource(17367049);
        tintSpinner.setAdapter((SpinnerAdapter) adapter);
        int i = 0;
        while (true) {
            if (i >= this.mTintArr.length) {
                break;
            } else if (this.mTintArr[i] != this.mSubInfoRecord.getIconTint()) {
                i++;
            } else {
                tintSpinner.setSelection(i);
                this.mTintSelectorPos = i;
                break;
            }
        }
        tintSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() { // from class: com.android.settings.sim.SimPreferenceDialog.2
            @Override // android.widget.AdapterView.OnItemSelectedListener
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                tintSpinner.setSelection(pos);
                SimPreferenceDialog.this.mTintSelectorPos = pos;
            }

            @Override // android.widget.AdapterView.OnItemSelectedListener
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        TextView numberView = (TextView) this.mDialogLayout.findViewById(R.id.number);
        String rawNumber = tm.getLine1Number(this.mSubInfoRecord.getSubscriptionId());
        if (TextUtils.isEmpty(rawNumber)) {
            numberView.setText(res.getString(17039374));
        } else {
            numberView.setText(PhoneNumberUtils.formatNumber(rawNumber));
        }
        String simCarrierName = tm.getSimOperatorName(this.mSubInfoRecord.getSubscriptionId());
        TextView carrierView = (TextView) this.mDialogLayout.findViewById(R.id.carrier);
        if (TextUtils.isEmpty(simCarrierName)) {
            simCarrierName = this.mContext.getString(17039374);
        }
        carrierView.setText(simCarrierName);
        this.mBuilder.setTitle(String.format(res.getString(R.string.sim_editor_title), Integer.valueOf(this.mSubInfoRecord.getSimSlotIndex() + 1)));
        customizeDialogTitle(this.mBuilder);
        this.mBuilder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() { // from class: com.android.settings.sim.SimPreferenceDialog.3
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int whichButton) {
                EditText nameText2 = (EditText) SimPreferenceDialog.this.mDialogLayout.findViewById(R.id.sim_name);
                String displayName = nameText2.getText().toString();
                int subId = SimPreferenceDialog.this.mSubInfoRecord.getSubscriptionId();
                SimPreferenceDialog.this.mSubInfoRecord.setDisplayName(displayName);
                SimPreferenceDialog.this.mSubscriptionManager.setDisplayName(displayName, subId, 2L);
                int tintSelected = tintSpinner.getSelectedItemPosition();
                int subscriptionId = SimPreferenceDialog.this.mSubInfoRecord.getSubscriptionId();
                int tint = SimPreferenceDialog.this.mTintArr[tintSelected];
                SimPreferenceDialog.this.mSubInfoRecord.setIconTint(tint);
                SimPreferenceDialog.this.mSubscriptionManager.setIconTint(tint, subscriptionId);
                dialog.dismiss();
                SimPreferenceDialog.this.finish();
            }
        });
        this.mBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() { // from class: com.android.settings.sim.SimPreferenceDialog.4
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
                SimPreferenceDialog.this.finish();
            }
        });
        this.mSimManagementExt.hideSimEditorView(this.mDialogLayout, this.mContext);
        this.mBuilder.setOnKeyListener(new DialogInterface.OnKeyListener() { // from class: com.android.settings.sim.SimPreferenceDialog.5
            @Override // android.content.DialogInterface.OnKeyListener
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == 4) {
                    SimPreferenceDialog.this.finish();
                    return true;
                }
                return false;
            }
        });
        this.mBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() { // from class: com.android.settings.sim.SimPreferenceDialog.6
            @Override // android.content.DialogInterface.OnCancelListener
            public void onCancel(DialogInterface dialog) {
                SimPreferenceDialog.this.finish();
            }
        });
        this.mDialog = this.mBuilder.create();
        this.mDialog.show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class SelectColorAdapter extends ArrayAdapter<CharSequence> {
        private Context mContext;
        private int mResId;

        public SelectColorAdapter(Context context, int resource, String[] arr) {
            super(context, resource, arr);
            this.mContext = context;
            this.mResId = resource;
        }

        @Override // android.widget.ArrayAdapter, android.widget.Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView;
            ViewHolder holder;
            LayoutInflater inflater = (LayoutInflater) this.mContext.getSystemService("layout_inflater");
            Resources res = this.mContext.getResources();
            int iconSize = res.getDimensionPixelSize(R.dimen.color_swatch_size);
            int strokeWidth = res.getDimensionPixelSize(R.dimen.color_swatch_stroke_width);
            if (convertView == null) {
                rowView = inflater.inflate(this.mResId, (ViewGroup) null);
                holder = new ViewHolder(this, null);
                ShapeDrawable drawable = new ShapeDrawable(new OvalShape());
                drawable.setIntrinsicHeight(iconSize);
                drawable.setIntrinsicWidth(iconSize);
                drawable.getPaint().setStrokeWidth(strokeWidth);
                holder.label = (TextView) rowView.findViewById(R.id.color_text);
                holder.icon = (ImageView) rowView.findViewById(R.id.color_icon);
                holder.swatch = drawable;
                rowView.setTag(holder);
            } else {
                rowView = convertView;
                holder = (ViewHolder) convertView.getTag();
            }
            holder.label.setText(getItem(position));
            holder.swatch.getPaint().setColor(SimPreferenceDialog.this.mTintArr[position]);
            holder.swatch.getPaint().setStyle(Paint.Style.FILL_AND_STROKE);
            holder.icon.setVisibility(0);
            holder.icon.setImageDrawable(holder.swatch);
            return rowView;
        }

        @Override // android.widget.ArrayAdapter, android.widget.BaseAdapter, android.widget.SpinnerAdapter
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View rowView = getView(position, convertView, parent);
            ViewHolder holder = (ViewHolder) rowView.getTag();
            if (SimPreferenceDialog.this.mTintSelectorPos == position) {
                holder.swatch.getPaint().setStyle(Paint.Style.FILL_AND_STROKE);
            } else {
                holder.swatch.getPaint().setStyle(Paint.Style.STROKE);
            }
            holder.icon.setVisibility(0);
            return rowView;
        }

        /* JADX INFO: Access modifiers changed from: private */
        /* loaded from: classes.dex */
        public class ViewHolder {
            ImageView icon;
            TextView label;
            ShapeDrawable swatch;

            /* synthetic */ ViewHolder(SelectColorAdapter this$1, ViewHolder viewHolder) {
                this();
            }

            private ViewHolder() {
            }
        }
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        if (this.mDialog != null && this.mDialog.isShowing()) {
            this.mDialog.dismiss();
            this.mDialog = null;
        }
        if (this.mSimHotSwapHandler == null) {
            return;
        }
        this.mSimHotSwapHandler.unregisterOnSimHotSwap();
    }

    private void customizeSimNameTitle(View dialogLayout) {
        int subId = -1;
        if (this.mSubInfoRecord != null) {
            subId = this.mSubInfoRecord.getSubscriptionId();
        }
        TextView nameTitle = (TextView) dialogLayout.findViewById(R.id.sim_name_title);
        nameTitle.setText(this.mMiscExt.customizeSimDisplayString(nameTitle.getText().toString(), subId));
        EditText nameText = (EditText) dialogLayout.findViewById(R.id.sim_name);
        nameText.setHint(this.mMiscExt.customizeSimDisplayString(getResources().getString(R.string.sim_name_hint), subId));
    }

    private void customizeDialogTitle(AlertDialog.Builder builder) {
        if (this.mSubInfoRecord == null) {
            return;
        }
        int subId = this.mSubInfoRecord.getSubscriptionId();
        builder.setTitle(String.format(this.mMiscExt.customizeSimDisplayString(getResources().getString(R.string.sim_editor_title), subId), Integer.valueOf(this.mSubInfoRecord.getSimSlotIndex() + 1)));
    }
}

package com.android.settings;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.preference.Preference;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ListPopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TimePicker;
import java.util.Calendar;
import java.util.TimeZone;
/* loaded from: classes.dex */
public class DateTimeSettingsSetupWizard extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener, CompoundButton.OnCheckedChangeListener, PreferenceFragment.OnPreferenceStartFragmentCallback {
    private static final String TAG = DateTimeSettingsSetupWizard.class.getSimpleName();
    private CompoundButton mAutoDateTimeButton;
    private DatePicker mDatePicker;
    private InputMethodManager mInputMethodManager;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() { // from class: com.android.settings.DateTimeSettingsSetupWizard.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            DateTimeSettingsSetupWizard.this.updateTimeAndDateDisplay();
        }
    };
    private TimeZone mSelectedTimeZone;
    private TimePicker mTimePicker;
    private SimpleAdapter mTimeZoneAdapter;
    private Button mTimeZoneButton;
    private ListPopupWindow mTimeZonePopup;
    private boolean mUsingXLargeLayout;

    @Override // android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(1);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.date_time_settings_setupwizard);
        this.mUsingXLargeLayout = findViewById(R.id.time_zone_button) != null;
        if (this.mUsingXLargeLayout) {
            initUiForXl();
        } else {
            findViewById(R.id.next_button).setOnClickListener(this);
        }
        this.mTimeZoneAdapter = ZonePicker.constructTimezoneAdapter(this, false, R.layout.date_time_setup_custom_list_item_2);
        if (this.mUsingXLargeLayout) {
            return;
        }
        View layoutRoot = findViewById(R.id.layout_root);
        layoutRoot.setSystemUiVisibility(4194304);
    }

    public void initUiForXl() {
        boolean autoDateTimeEnabled;
        TimeZone tz = TimeZone.getDefault();
        this.mSelectedTimeZone = tz;
        this.mTimeZoneButton = (Button) findViewById(R.id.time_zone_button);
        this.mTimeZoneButton.setText(tz.getDisplayName());
        this.mTimeZoneButton.setOnClickListener(this);
        Intent intent = getIntent();
        if (intent.hasExtra("extra_initial_auto_datetime_value")) {
            autoDateTimeEnabled = intent.getBooleanExtra("extra_initial_auto_datetime_value", false);
        } else {
            autoDateTimeEnabled = isAutoDateTimeEnabled();
        }
        this.mAutoDateTimeButton = (CompoundButton) findViewById(R.id.date_time_auto_button);
        this.mAutoDateTimeButton.setChecked(autoDateTimeEnabled);
        this.mAutoDateTimeButton.setOnCheckedChangeListener(this);
        this.mTimePicker = (TimePicker) findViewById(R.id.time_picker);
        this.mTimePicker.setEnabled(!autoDateTimeEnabled);
        this.mDatePicker = (DatePicker) findViewById(R.id.date_picker);
        this.mDatePicker.setEnabled(autoDateTimeEnabled ? false : true);
        this.mDatePicker.setCalendarViewShown(false);
        DateTimeSettings.configureDatePicker(this.mDatePicker);
        this.mInputMethodManager = (InputMethodManager) getSystemService("input_method");
        ((Button) findViewById(R.id.next_button)).setOnClickListener(this);
        Button skipButton = (Button) findViewById(R.id.skip_button);
        if (skipButton == null) {
            return;
        }
        skipButton.setOnClickListener(this);
    }

    @Override // android.app.Activity
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.TIME_TICK");
        filter.addAction("android.intent.action.TIME_SET");
        filter.addAction("android.intent.action.TIMEZONE_CHANGED");
        registerReceiver(this.mIntentReceiver, filter, null, null);
    }

    @Override // android.app.Activity
    public void onPause() {
        super.onPause();
        unregisterReceiver(this.mIntentReceiver);
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.next_button /* 2131886260 */:
                if (this.mSelectedTimeZone != null) {
                    TimeZone systemTimeZone = TimeZone.getDefault();
                    if (!systemTimeZone.equals(this.mSelectedTimeZone)) {
                        Log.i(TAG, "Another TimeZone is selected by a user. Changing system TimeZone.");
                        AlarmManager alarm = (AlarmManager) getSystemService("alarm");
                        alarm.setTimeZone(this.mSelectedTimeZone.getID());
                    }
                }
                if (this.mAutoDateTimeButton != null) {
                    Settings.Global.putInt(getContentResolver(), "auto_time", this.mAutoDateTimeButton.isChecked() ? 1 : 0);
                    if (!this.mAutoDateTimeButton.isChecked()) {
                        DateTimeSettings.setDate(this, this.mDatePicker.getYear(), this.mDatePicker.getMonth(), this.mDatePicker.getDayOfMonth());
                        DateTimeSettings.setTime(this, this.mTimePicker.getCurrentHour().intValue(), this.mTimePicker.getCurrentMinute().intValue());
                        break;
                    }
                }
                break;
            case R.id.time_zone_button /* 2131886337 */:
                showTimezonePicker(R.id.time_zone_button);
                return;
            case R.id.skip_button /* 2131886495 */:
                break;
            default:
                return;
        }
        setResult(-1);
        finish();
    }

    @Override // android.widget.CompoundButton.OnCheckedChangeListener
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        View focusedView;
        if (buttonView == this.mAutoDateTimeButton) {
            Settings.Global.putInt(getContentResolver(), "auto_time", isChecked ? 1 : 0);
            this.mTimePicker.setEnabled(!isChecked);
            this.mDatePicker.setEnabled(isChecked ? false : true);
        }
        if (!isChecked || (focusedView = getCurrentFocus()) == null) {
            return;
        }
        this.mInputMethodManager.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
        focusedView.clearFocus();
    }

    @Override // android.widget.AdapterView.OnItemClickListener
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TimeZone tz = ZonePicker.obtainTimeZoneFromItem(parent.getItemAtPosition(position));
        if (this.mUsingXLargeLayout) {
            this.mSelectedTimeZone = tz;
            Calendar now = Calendar.getInstance(tz);
            if (this.mTimeZoneButton != null) {
                this.mTimeZoneButton.setText(tz.getDisplayName());
            }
            this.mDatePicker.updateDate(now.get(1), now.get(2), now.get(5));
            this.mTimePicker.setCurrentHour(Integer.valueOf(now.get(11)));
            this.mTimePicker.setCurrentMinute(Integer.valueOf(now.get(12)));
        } else {
            AlarmManager alarm = (AlarmManager) getSystemService("alarm");
            alarm.setTimeZone(tz.getID());
            DateTimeSettings settingsFragment = (DateTimeSettings) getFragmentManager().findFragmentById(R.id.date_time_settings_fragment);
            settingsFragment.updateTimeAndDateDisplay(this);
        }
        this.mTimeZonePopup.dismiss();
    }

    @Override // android.support.v14.preference.PreferenceFragment.OnPreferenceStartFragmentCallback
    public boolean onPreferenceStartFragment(PreferenceFragment caller, Preference pref) {
        showTimezonePicker(R.id.timezone_dropdown_anchor);
        return true;
    }

    private void showTimezonePicker(int anchorViewId) {
        View anchorView = findViewById(anchorViewId);
        if (anchorView == null) {
            Log.e(TAG, "Unable to find zone picker anchor view " + anchorViewId);
            return;
        }
        this.mTimeZonePopup = new ListPopupWindow(this, null);
        this.mTimeZonePopup.setWidth(anchorView.getWidth());
        this.mTimeZonePopup.setAnchorView(anchorView);
        this.mTimeZonePopup.setAdapter(this.mTimeZoneAdapter);
        this.mTimeZonePopup.setOnItemClickListener(this);
        this.mTimeZonePopup.setModal(true);
        this.mTimeZonePopup.show();
    }

    private boolean isAutoDateTimeEnabled() {
        try {
            return Settings.Global.getInt(getContentResolver(), "auto_time") > 0;
        } catch (Settings.SettingNotFoundException e) {
            return true;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateTimeAndDateDisplay() {
        if (!this.mUsingXLargeLayout) {
            return;
        }
        Calendar now = Calendar.getInstance();
        this.mTimeZoneButton.setText(now.getTimeZone().getDisplayName());
        this.mDatePicker.updateDate(now.get(1), now.get(2), now.get(5));
        this.mTimePicker.setCurrentHour(Integer.valueOf(now.get(11)));
        this.mTimePicker.setCurrentMinute(Integer.valueOf(now.get(12)));
    }
}

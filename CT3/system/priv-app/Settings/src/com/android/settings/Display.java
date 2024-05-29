package com.android.settings;

import android.R;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
/* loaded from: classes.dex */
public class Display extends Activity implements View.OnClickListener {
    private DisplayMetrics mDisplayMetrics;
    private Spinner mFontSize;
    private TextView mPreview;
    private TypedValue mTextSizeTyped;
    private AdapterView.OnItemSelectedListener mFontSizeChanged = new AdapterView.OnItemSelectedListener() { // from class: com.android.settings.Display.1
        @Override // android.widget.AdapterView.OnItemSelectedListener
        public void onItemSelected(AdapterView av, View v, int position, long id) {
            if (position == 0) {
                Display.this.mFontScale = 0.75f;
            } else if (position == 2) {
                Display.this.mFontScale = 1.25f;
            } else {
                Display.this.mFontScale = 1.0f;
            }
            Display.this.updateFontScale();
        }

        @Override // android.widget.AdapterView.OnItemSelectedListener
        public void onNothingSelected(AdapterView av) {
        }
    };
    private float mFontScale = 1.0f;

    @Override // android.app.Activity
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.display);
        this.mFontSize = (Spinner) findViewById(R.id.fontSize);
        this.mFontSize.setOnItemSelectedListener(this.mFontSizeChanged);
        Resources r = getResources();
        String[] states = {r.getString(R.string.small_font), r.getString(R.string.medium_font), r.getString(R.string.large_font)};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 17367048, states);
        adapter.setDropDownViewResource(17367049);
        this.mFontSize.setAdapter((SpinnerAdapter) adapter);
        this.mPreview = (TextView) findViewById(R.id.preview);
        this.mPreview.setText(r.getText(R.string.font_size_preview_text));
        Button save = (Button) findViewById(R.id.save);
        save.setText(r.getText(R.string.font_size_save));
        save.setOnClickListener(this);
        this.mTextSizeTyped = new TypedValue();
        TypedArray styledAttributes = obtainStyledAttributes(R.styleable.TextView);
        styledAttributes.getValue(2, this.mTextSizeTyped);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        this.mDisplayMetrics = new DisplayMetrics();
        this.mDisplayMetrics.density = metrics.density;
        this.mDisplayMetrics.heightPixels = metrics.heightPixels;
        this.mDisplayMetrics.scaledDensity = metrics.scaledDensity;
        this.mDisplayMetrics.widthPixels = metrics.widthPixels;
        this.mDisplayMetrics.xdpi = metrics.xdpi;
        this.mDisplayMetrics.ydpi = metrics.ydpi;
        styledAttributes.recycle();
    }

    @Override // android.app.Activity
    public void onResume() {
        super.onResume();
        ContentResolver resolver = getContentResolver();
        this.mFontScale = Settings.System.getFloat(resolver, "font_scale", 1.0f);
        if (this.mFontScale < 1.0f) {
            this.mFontSize.setSelection(0);
        } else if (this.mFontScale > 1.0f) {
            this.mFontSize.setSelection(2);
        } else {
            this.mFontSize.setSelection(1);
        }
        updateFontScale();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateFontScale() {
        this.mDisplayMetrics.scaledDensity = this.mDisplayMetrics.density * this.mFontScale;
        float size = this.mTextSizeTyped.getDimension(this.mDisplayMetrics);
        this.mPreview.setTextSize(0, size);
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View v) {
        ContentResolver resolver = getContentResolver();
        Settings.System.putFloat(resolver, "font_scale", this.mFontScale);
        finish();
    }
}

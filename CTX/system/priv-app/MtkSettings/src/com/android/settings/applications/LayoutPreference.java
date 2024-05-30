package com.android.settings.applications;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.android.settings.R;
import com.android.settings.Utils;
/* loaded from: classes.dex */
public class LayoutPreference extends Preference {
    private boolean mAllowDividerAbove;
    private boolean mAllowDividerBelow;
    private final View.OnClickListener mClickListener;
    View mRootView;

    public LayoutPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mClickListener = new View.OnClickListener() { // from class: com.android.settings.applications.-$$Lambda$LayoutPreference$-oL9WiG-H2u60jStTsEh5xi7a7Q
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                LayoutPreference.this.performClick(view);
            }
        };
        init(context, attributeSet, 0);
    }

    public LayoutPreference(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mClickListener = new View.OnClickListener() { // from class: com.android.settings.applications.-$$Lambda$LayoutPreference$-oL9WiG-H2u60jStTsEh5xi7a7Q
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                LayoutPreference.this.performClick(view);
            }
        };
        init(context, attributeSet, i);
    }

    public LayoutPreference(Context context, int i) {
        this(context, LayoutInflater.from(context).inflate(i, (ViewGroup) null, false));
    }

    public LayoutPreference(Context context, View view) {
        super(context);
        this.mClickListener = new View.OnClickListener() { // from class: com.android.settings.applications.-$$Lambda$LayoutPreference$-oL9WiG-H2u60jStTsEh5xi7a7Q
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) {
                LayoutPreference.this.performClick(view2);
            }
        };
        setView(view);
    }

    private void init(Context context, AttributeSet attributeSet, int i) {
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.Preference);
        this.mAllowDividerAbove = TypedArrayUtils.getBoolean(obtainStyledAttributes, 16, 16, false);
        this.mAllowDividerBelow = TypedArrayUtils.getBoolean(obtainStyledAttributes, 17, 17, false);
        obtainStyledAttributes.recycle();
        TypedArray obtainStyledAttributes2 = context.obtainStyledAttributes(attributeSet, com.android.internal.R.styleable.Preference, i, 0);
        int resourceId = obtainStyledAttributes2.getResourceId(3, 0);
        if (resourceId == 0) {
            throw new IllegalArgumentException("LayoutPreference requires a layout to be defined");
        }
        obtainStyledAttributes2.recycle();
        setView(LayoutInflater.from(getContext()).inflate(resourceId, (ViewGroup) null, false));
    }

    private void setView(View view) {
        setLayoutResource(R.layout.layout_preference_frame);
        ViewGroup viewGroup = (ViewGroup) view.findViewById(R.id.all_details);
        if (viewGroup != null) {
            Utils.forceCustomPadding(viewGroup, true);
        }
        this.mRootView = view;
        setShouldDisableView(false);
    }

    @Override // android.support.v7.preference.Preference
    public void onBindViewHolder(PreferenceViewHolder preferenceViewHolder) {
        preferenceViewHolder.itemView.setOnClickListener(this.mClickListener);
        boolean isSelectable = isSelectable();
        preferenceViewHolder.itemView.setFocusable(isSelectable);
        preferenceViewHolder.itemView.setClickable(isSelectable);
        preferenceViewHolder.setDividerAllowedAbove(this.mAllowDividerAbove);
        preferenceViewHolder.setDividerAllowedBelow(this.mAllowDividerBelow);
        FrameLayout frameLayout = (FrameLayout) preferenceViewHolder.itemView;
        frameLayout.removeAllViews();
        ViewGroup viewGroup = (ViewGroup) this.mRootView.getParent();
        if (viewGroup != null) {
            viewGroup.removeView(this.mRootView);
        }
        frameLayout.addView(this.mRootView);
    }

    public <T extends View> T findViewById(int i) {
        return (T) this.mRootView.findViewById(i);
    }
}

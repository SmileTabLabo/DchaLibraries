package com.android.settings;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import com.android.settings.widget.DotsPageIndicator;
import com.android.settings.widget.LabeledSeekBar;
/* loaded from: classes.dex */
public abstract class PreviewSeekBarPreferenceFragment extends SettingsPreferenceFragment {
    protected int mActivityLayoutResId;
    protected int mCurrentIndex;
    protected String[] mEntries;
    protected int mInitialIndex;
    private TextView mLabel;
    private View mLarger;
    private DotsPageIndicator mPageIndicator;
    private ViewPager mPreviewPager;
    private PreviewPagerAdapter mPreviewPagerAdapter;
    protected int[] mPreviewSampleResIds;
    private View mSmaller;
    private ViewPager.OnPageChangeListener mPreviewPageChangeListener = new ViewPager.OnPageChangeListener() { // from class: com.android.settings.PreviewSeekBarPreferenceFragment.1
        @Override // android.support.v4.view.ViewPager.OnPageChangeListener
        public void onPageScrollStateChanged(int state) {
        }

        @Override // android.support.v4.view.ViewPager.OnPageChangeListener
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override // android.support.v4.view.ViewPager.OnPageChangeListener
        public void onPageSelected(int position) {
            PreviewSeekBarPreferenceFragment.this.mPreviewPager.sendAccessibilityEvent(16384);
        }
    };
    private ViewPager.OnPageChangeListener mPageIndicatorPageChangeListener = new ViewPager.OnPageChangeListener() { // from class: com.android.settings.PreviewSeekBarPreferenceFragment.2
        @Override // android.support.v4.view.ViewPager.OnPageChangeListener
        public void onPageScrollStateChanged(int state) {
        }

        @Override // android.support.v4.view.ViewPager.OnPageChangeListener
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override // android.support.v4.view.ViewPager.OnPageChangeListener
        public void onPageSelected(int position) {
            PreviewSeekBarPreferenceFragment.this.setPagerIndicatorContentDescription(position);
        }
    };

    protected abstract void commit();

    protected abstract Configuration createConfig(Configuration configuration, int i);

    /* loaded from: classes.dex */
    private class onPreviewSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        private boolean mSeekByTouch;

        /* synthetic */ onPreviewSeekBarChangeListener(PreviewSeekBarPreferenceFragment this$0, onPreviewSeekBarChangeListener onpreviewseekbarchangelistener) {
            this();
        }

        private onPreviewSeekBarChangeListener() {
        }

        @Override // android.widget.SeekBar.OnSeekBarChangeListener
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            PreviewSeekBarPreferenceFragment.this.setPreviewLayer(progress, true);
            if (this.mSeekByTouch) {
                return;
            }
            PreviewSeekBarPreferenceFragment.this.commit();
        }

        @Override // android.widget.SeekBar.OnSeekBarChangeListener
        public void onStartTrackingTouch(SeekBar seekBar) {
            this.mSeekByTouch = true;
        }

        @Override // android.widget.SeekBar.OnSeekBarChangeListener
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (PreviewSeekBarPreferenceFragment.this.mPreviewPagerAdapter.isAnimating()) {
                PreviewSeekBarPreferenceFragment.this.mPreviewPagerAdapter.setAnimationEndAction(new Runnable() { // from class: com.android.settings.PreviewSeekBarPreferenceFragment.onPreviewSeekBarChangeListener.1
                    @Override // java.lang.Runnable
                    public void run() {
                        PreviewSeekBarPreferenceFragment.this.commit();
                    }
                });
            } else {
                PreviewSeekBarPreferenceFragment.this.commit();
            }
            this.mSeekByTouch = false;
        }
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        ViewGroup listContainer = (ViewGroup) root.findViewById(16908351);
        listContainer.removeAllViews();
        View content = inflater.inflate(this.mActivityLayoutResId, listContainer, false);
        listContainer.addView(content);
        this.mLabel = (TextView) content.findViewById(R.id.current_label);
        int max = Math.max(1, this.mEntries.length - 1);
        final LabeledSeekBar seekBar = (LabeledSeekBar) content.findViewById(R.id.seek_bar);
        seekBar.setLabels(this.mEntries);
        seekBar.setMax(max);
        seekBar.setProgress(this.mInitialIndex);
        seekBar.setOnSeekBarChangeListener(new onPreviewSeekBarChangeListener(this, null));
        this.mSmaller = content.findViewById(R.id.smaller);
        this.mSmaller.setOnClickListener(new View.OnClickListener() { // from class: com.android.settings.PreviewSeekBarPreferenceFragment.3
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                int progress = seekBar.getProgress();
                if (progress <= 0) {
                    return;
                }
                seekBar.setProgress(progress - 1, true);
            }
        });
        this.mLarger = content.findViewById(R.id.larger);
        this.mLarger.setOnClickListener(new View.OnClickListener() { // from class: com.android.settings.PreviewSeekBarPreferenceFragment.4
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                int progress = seekBar.getProgress();
                if (progress >= seekBar.getMax()) {
                    return;
                }
                seekBar.setProgress(progress + 1, true);
            }
        });
        if (this.mEntries.length == 1) {
            seekBar.setEnabled(false);
        }
        Context context = getPrefContext();
        Configuration origConfig = context.getResources().getConfiguration();
        boolean isLayoutRtl = origConfig.getLayoutDirection() == 1;
        Configuration[] configurations = new Configuration[this.mEntries.length];
        for (int i = 0; i < this.mEntries.length; i++) {
            configurations[i] = createConfig(origConfig, i);
        }
        this.mPreviewPager = (ViewPager) content.findViewById(R.id.preview_pager);
        this.mPreviewPagerAdapter = new PreviewPagerAdapter(context, isLayoutRtl, this.mPreviewSampleResIds, configurations);
        this.mPreviewPager.setAdapter(this.mPreviewPagerAdapter);
        this.mPreviewPager.setCurrentItem(isLayoutRtl ? this.mPreviewSampleResIds.length - 1 : 0);
        this.mPreviewPager.addOnPageChangeListener(this.mPreviewPageChangeListener);
        this.mPageIndicator = (DotsPageIndicator) content.findViewById(R.id.page_indicator);
        if (this.mPreviewSampleResIds.length > 1) {
            this.mPageIndicator.setViewPager(this.mPreviewPager);
            this.mPageIndicator.setVisibility(0);
            this.mPageIndicator.setOnPageChangeListener(this.mPageIndicatorPageChangeListener);
        } else {
            this.mPageIndicator.setVisibility(8);
        }
        setPreviewLayer(this.mInitialIndex, false);
        return root;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setPreviewLayer(int index, boolean animate) {
        this.mLabel.setText(this.mEntries[index]);
        this.mSmaller.setEnabled(index > 0);
        this.mLarger.setEnabled(index < this.mEntries.length + (-1));
        setPagerIndicatorContentDescription(this.mPreviewPager.getCurrentItem());
        this.mPreviewPagerAdapter.setPreviewLayer(index, this.mCurrentIndex, this.mPreviewPager.getCurrentItem(), animate);
        this.mCurrentIndex = index;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setPagerIndicatorContentDescription(int position) {
        this.mPageIndicator.setContentDescription(getPrefContext().getString(R.string.preview_page_indicator_content_description, Integer.valueOf(position + 1), Integer.valueOf(this.mPreviewSampleResIds.length)));
    }
}

package android.support.v17.leanback.widget;

import android.content.Context;
import android.content.res.Resources;
import android.support.v17.leanback.R$color;
import android.support.v17.leanback.R$drawable;
import android.support.v17.leanback.R$fraction;
import android.support.v17.leanback.R$layout;
import android.support.v17.leanback.widget.SearchOrbView;
import android.util.AttributeSet;
/* loaded from: a.zip:android/support/v17/leanback/widget/SpeechOrbView.class */
public class SpeechOrbView extends SearchOrbView {
    private int mCurrentLevel;
    private boolean mListening;
    private final SearchOrbView.Colors mListeningOrbColors;
    private final SearchOrbView.Colors mNotListeningOrbColors;
    private final float mSoundLevelMaxZoom;

    public SpeechOrbView(Context context) {
        this(context, null);
    }

    public SpeechOrbView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public SpeechOrbView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mCurrentLevel = 0;
        this.mListening = false;
        Resources resources = context.getResources();
        this.mSoundLevelMaxZoom = resources.getFraction(R$fraction.lb_search_bar_speech_orb_max_level_zoom, 1, 1);
        this.mNotListeningOrbColors = new SearchOrbView.Colors(resources.getColor(R$color.lb_speech_orb_not_recording), resources.getColor(R$color.lb_speech_orb_not_recording_pulsed), resources.getColor(R$color.lb_speech_orb_not_recording_icon));
        this.mListeningOrbColors = new SearchOrbView.Colors(resources.getColor(R$color.lb_speech_orb_recording), resources.getColor(R$color.lb_speech_orb_recording), 0);
        showNotListening();
    }

    @Override // android.support.v17.leanback.widget.SearchOrbView
    int getLayoutResourceId() {
        return R$layout.lb_speech_orb;
    }

    public void setSoundLevel(int i) {
        if (this.mListening) {
            if (i > this.mCurrentLevel) {
                this.mCurrentLevel += (i - this.mCurrentLevel) / 2;
            } else {
                this.mCurrentLevel = (int) (this.mCurrentLevel * 0.7f);
            }
            scaleOrbViewOnly(1.0f + (((this.mSoundLevelMaxZoom - getFocusedZoom()) * this.mCurrentLevel) / 100.0f));
        }
    }

    public void showListening() {
        setOrbColors(this.mListeningOrbColors);
        setOrbIcon(getResources().getDrawable(R$drawable.lb_ic_search_mic));
        animateOnFocus(true);
        enableOrbColorAnimation(false);
        scaleOrbViewOnly(1.0f);
        this.mCurrentLevel = 0;
        this.mListening = true;
    }

    public void showNotListening() {
        setOrbColors(this.mNotListeningOrbColors);
        setOrbIcon(getResources().getDrawable(R$drawable.lb_ic_search_mic_out));
        animateOnFocus(hasFocus());
        scaleOrbViewOnly(1.0f);
        this.mListening = false;
    }
}

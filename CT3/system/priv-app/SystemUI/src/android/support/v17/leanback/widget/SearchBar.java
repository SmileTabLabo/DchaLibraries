package android.support.v17.leanback.widget;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.support.v17.leanback.R$color;
import android.support.v17.leanback.R$dimen;
import android.support.v17.leanback.R$id;
import android.support.v17.leanback.R$integer;
import android.support.v17.leanback.R$layout;
import android.support.v17.leanback.R$raw;
import android.support.v17.leanback.R$string;
import android.support.v17.leanback.widget.SearchEditText;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.ArrayList;
/* loaded from: a.zip:android/support/v17/leanback/widget/SearchBar.class */
public class SearchBar extends RelativeLayout {
    private static final String TAG = SearchBar.class.getSimpleName();
    private AudioManager.OnAudioFocusChangeListener mAudioFocusChangeListener;
    private AudioManager mAudioManager;
    private boolean mAutoStartRecognition;
    private int mBackgroundAlpha;
    private int mBackgroundSpeechAlpha;
    private Drawable mBadgeDrawable;
    private ImageView mBadgeView;
    private Drawable mBarBackground;
    private int mBarHeight;
    private final Context mContext;
    private final Handler mHandler;
    private String mHint;
    private final InputMethodManager mInputMethodManager;
    private boolean mListening;
    private SearchBarPermissionListener mPermissionListener;
    private boolean mRecognizing;
    private SearchBarListener mSearchBarListener;
    private String mSearchQuery;
    private SearchEditText mSearchTextEditor;
    private SparseIntArray mSoundMap;
    private SoundPool mSoundPool;
    private SpeechOrbView mSpeechOrbView;
    private SpeechRecognitionCallback mSpeechRecognitionCallback;
    private SpeechRecognizer mSpeechRecognizer;
    private final int mTextColor;
    private final int mTextColorSpeechMode;
    private final int mTextHintColor;
    private final int mTextHintColorSpeechMode;
    private String mTitle;

    /* renamed from: android.support.v17.leanback.widget.SearchBar$6  reason: invalid class name */
    /* loaded from: a.zip:android/support/v17/leanback/widget/SearchBar$6.class */
    class AnonymousClass6 implements TextView.OnEditorActionListener {
        final SearchBar this$0;

        AnonymousClass6(SearchBar searchBar) {
            this.this$0 = searchBar;
        }

        @Override // android.widget.TextView.OnEditorActionListener
        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
            boolean z = true;
            if ((3 == i || i == 0) && this.this$0.mSearchBarListener != null) {
                this.this$0.hideNativeKeyboard();
                this.this$0.mHandler.postDelayed(new Runnable(this) { // from class: android.support.v17.leanback.widget.SearchBar.6.1
                    final AnonymousClass6 this$1;

                    {
                        this.this$1 = this;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        this.this$1.this$0.submitQuery();
                    }
                }, 500L);
            } else if (1 == i && this.this$0.mSearchBarListener != null) {
                this.this$0.hideNativeKeyboard();
                this.this$0.mHandler.postDelayed(new Runnable(this) { // from class: android.support.v17.leanback.widget.SearchBar.6.2
                    final AnonymousClass6 this$1;

                    {
                        this.this$1 = this;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        this.this$1.this$0.mSearchBarListener.onKeyboardDismiss(this.this$1.this$0.mSearchQuery);
                    }
                }, 500L);
            } else if (2 == i) {
                this.this$0.hideNativeKeyboard();
                this.this$0.mHandler.postDelayed(new Runnable(this) { // from class: android.support.v17.leanback.widget.SearchBar.6.3
                    final AnonymousClass6 this$1;

                    {
                        this.this$1 = this;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        this.this$1.this$0.mAutoStartRecognition = true;
                        this.this$1.this$0.mSpeechOrbView.requestFocus();
                    }
                }, 500L);
            } else {
                z = false;
            }
            return z;
        }
    }

    /* loaded from: a.zip:android/support/v17/leanback/widget/SearchBar$SearchBarListener.class */
    public interface SearchBarListener {
        void onKeyboardDismiss(String str);

        void onSearchQueryChange(String str);

        void onSearchQuerySubmit(String str);
    }

    /* loaded from: a.zip:android/support/v17/leanback/widget/SearchBar$SearchBarPermissionListener.class */
    public interface SearchBarPermissionListener {
        void requestAudioPermission();
    }

    public SearchBar(Context context) {
        this(context, null);
    }

    public SearchBar(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public SearchBar(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener(this) { // from class: android.support.v17.leanback.widget.SearchBar.1
            final SearchBar this$0;

            {
                this.this$0 = this;
            }

            @Override // android.media.AudioManager.OnAudioFocusChangeListener
            public void onAudioFocusChange(int i2) {
                this.this$0.stopRecognition();
            }
        };
        this.mHandler = new Handler();
        this.mAutoStartRecognition = false;
        this.mSoundMap = new SparseIntArray();
        this.mRecognizing = false;
        this.mContext = context;
        Resources resources = getResources();
        LayoutInflater.from(getContext()).inflate(R$layout.lb_search_bar, (ViewGroup) this, true);
        this.mBarHeight = getResources().getDimensionPixelSize(R$dimen.lb_search_bar_height);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(-1, this.mBarHeight);
        layoutParams.addRule(10, -1);
        setLayoutParams(layoutParams);
        setBackgroundColor(0);
        setClipChildren(false);
        this.mSearchQuery = "";
        this.mInputMethodManager = (InputMethodManager) context.getSystemService("input_method");
        this.mTextColorSpeechMode = resources.getColor(R$color.lb_search_bar_text_speech_mode);
        this.mTextColor = resources.getColor(R$color.lb_search_bar_text);
        this.mBackgroundSpeechAlpha = resources.getInteger(R$integer.lb_search_bar_speech_mode_background_alpha);
        this.mBackgroundAlpha = resources.getInteger(R$integer.lb_search_bar_text_mode_background_alpha);
        this.mTextHintColorSpeechMode = resources.getColor(R$color.lb_search_bar_hint_speech_mode);
        this.mTextHintColor = resources.getColor(R$color.lb_search_bar_hint);
        this.mAudioManager = (AudioManager) context.getSystemService("audio");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void hideNativeKeyboard() {
        this.mInputMethodManager.hideSoftInputFromWindow(this.mSearchTextEditor.getWindowToken(), 0);
    }

    private boolean isVoiceMode() {
        return this.mSpeechOrbView.isFocused();
    }

    private void loadSounds(Context context) {
        int[] iArr;
        for (int i : new int[]{R$raw.lb_voice_failure, R$raw.lb_voice_open, R$raw.lb_voice_no_input, R$raw.lb_voice_success}) {
            this.mSoundMap.put(i, this.mSoundPool.load(context, i, 1));
        }
    }

    private void play(int i) {
        this.mHandler.post(new Runnable(this, i) { // from class: android.support.v17.leanback.widget.SearchBar.11
            final SearchBar this$0;
            final int val$resId;

            {
                this.this$0 = this;
                this.val$resId = i;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.mSoundPool.play(this.this$0.mSoundMap.get(this.val$resId), 1.0f, 1.0f, 1, 0, 1.0f);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void playSearchFailure() {
        play(R$raw.lb_voice_failure);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void playSearchOpen() {
        play(R$raw.lb_voice_open);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void playSearchSuccess() {
        play(R$raw.lb_voice_success);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setSearchQueryInternal(String str) {
        if (TextUtils.equals(this.mSearchQuery, str)) {
            return;
        }
        this.mSearchQuery = str;
        if (this.mSearchBarListener != null) {
            this.mSearchBarListener.onSearchQueryChange(this.mSearchQuery);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showNativeKeyboard() {
        this.mHandler.post(new Runnable(this) { // from class: android.support.v17.leanback.widget.SearchBar.9
            final SearchBar this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.mSearchTextEditor.requestFocusFromTouch();
                this.this$0.mSearchTextEditor.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), 0, this.this$0.mSearchTextEditor.getWidth(), this.this$0.mSearchTextEditor.getHeight(), 0));
                this.this$0.mSearchTextEditor.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), 1, this.this$0.mSearchTextEditor.getWidth(), this.this$0.mSearchTextEditor.getHeight(), 0));
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void submitQuery() {
        if (TextUtils.isEmpty(this.mSearchQuery) || this.mSearchBarListener == null) {
            return;
        }
        this.mSearchBarListener.onSearchQuerySubmit(this.mSearchQuery);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void toggleRecognition() {
        if (this.mRecognizing) {
            stopRecognition();
        } else {
            startRecognition();
        }
    }

    private void updateHint() {
        String string = getResources().getString(R$string.lb_search_bar_hint);
        if (!TextUtils.isEmpty(this.mTitle)) {
            string = isVoiceMode() ? getResources().getString(R$string.lb_search_bar_hint_with_title_speech, this.mTitle) : getResources().getString(R$string.lb_search_bar_hint_with_title, this.mTitle);
        } else if (isVoiceMode()) {
            string = getResources().getString(R$string.lb_search_bar_hint_speech);
        }
        this.mHint = string;
        if (this.mSearchTextEditor != null) {
            this.mSearchTextEditor.setHint(this.mHint);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateUi(boolean z) {
        if (z) {
            this.mBarBackground.setAlpha(this.mBackgroundSpeechAlpha);
            if (isVoiceMode()) {
                this.mSearchTextEditor.setTextColor(this.mTextHintColorSpeechMode);
                this.mSearchTextEditor.setHintTextColor(this.mTextHintColorSpeechMode);
            } else {
                this.mSearchTextEditor.setTextColor(this.mTextColorSpeechMode);
                this.mSearchTextEditor.setHintTextColor(this.mTextHintColorSpeechMode);
            }
        } else {
            this.mBarBackground.setAlpha(this.mBackgroundAlpha);
            this.mSearchTextEditor.setTextColor(this.mTextColor);
            this.mSearchTextEditor.setHintTextColor(this.mTextHintColor);
        }
        updateHint();
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mSoundPool = new SoundPool(2, 1, 0);
        loadSounds(this.mContext);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        stopRecognition();
        this.mSoundPool.release();
        super.onDetachedFromWindow();
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mBarBackground = ((RelativeLayout) findViewById(R$id.lb_search_bar_items)).getBackground();
        this.mSearchTextEditor = (SearchEditText) findViewById(R$id.lb_search_text_editor);
        this.mBadgeView = (ImageView) findViewById(R$id.lb_search_bar_badge);
        if (this.mBadgeDrawable != null) {
            this.mBadgeView.setImageDrawable(this.mBadgeDrawable);
        }
        this.mSearchTextEditor.setOnFocusChangeListener(new View.OnFocusChangeListener(this) { // from class: android.support.v17.leanback.widget.SearchBar.2
            final SearchBar this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnFocusChangeListener
            public void onFocusChange(View view, boolean z) {
                if (z) {
                    this.this$0.showNativeKeyboard();
                }
                this.this$0.updateUi(z);
            }
        });
        this.mSearchTextEditor.addTextChangedListener(new TextWatcher(this, new Runnable(this) { // from class: android.support.v17.leanback.widget.SearchBar.3
            final SearchBar this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.setSearchQueryInternal(this.this$0.mSearchTextEditor.getText().toString());
            }
        }) { // from class: android.support.v17.leanback.widget.SearchBar.4
            final SearchBar this$0;
            final Runnable val$mOnTextChangedRunnable;

            {
                this.this$0 = this;
                this.val$mOnTextChangedRunnable = r5;
            }

            @Override // android.text.TextWatcher
            public void afterTextChanged(Editable editable) {
            }

            @Override // android.text.TextWatcher
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override // android.text.TextWatcher
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                if (this.this$0.mRecognizing) {
                    return;
                }
                this.this$0.mHandler.removeCallbacks(this.val$mOnTextChangedRunnable);
                this.this$0.mHandler.post(this.val$mOnTextChangedRunnable);
            }
        });
        this.mSearchTextEditor.setOnKeyboardDismissListener(new SearchEditText.OnKeyboardDismissListener(this) { // from class: android.support.v17.leanback.widget.SearchBar.5
            final SearchBar this$0;

            {
                this.this$0 = this;
            }

            @Override // android.support.v17.leanback.widget.SearchEditText.OnKeyboardDismissListener
            public void onKeyboardDismiss() {
                if (this.this$0.mSearchBarListener != null) {
                    this.this$0.mSearchBarListener.onKeyboardDismiss(this.this$0.mSearchQuery);
                }
            }
        });
        this.mSearchTextEditor.setOnEditorActionListener(new AnonymousClass6(this));
        this.mSearchTextEditor.setPrivateImeOptions("EscapeNorth=1;VoiceDismiss=1;");
        this.mSpeechOrbView = (SpeechOrbView) findViewById(R$id.lb_search_bar_speech_orb);
        this.mSpeechOrbView.setOnOrbClickedListener(new View.OnClickListener(this) { // from class: android.support.v17.leanback.widget.SearchBar.7
            final SearchBar this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                this.this$0.toggleRecognition();
            }
        });
        this.mSpeechOrbView.setOnFocusChangeListener(new View.OnFocusChangeListener(this) { // from class: android.support.v17.leanback.widget.SearchBar.8
            final SearchBar this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnFocusChangeListener
            public void onFocusChange(View view, boolean z) {
                if (z) {
                    this.this$0.hideNativeKeyboard();
                    if (this.this$0.mAutoStartRecognition) {
                        this.this$0.startRecognition();
                        this.this$0.mAutoStartRecognition = false;
                    }
                } else {
                    this.this$0.stopRecognition();
                }
                this.this$0.updateUi(z);
            }
        });
        updateUi(hasFocus());
        updateHint();
    }

    @Override // android.view.View
    public void setNextFocusDownId(int i) {
        this.mSpeechOrbView.setNextFocusDownId(i);
        this.mSearchTextEditor.setNextFocusDownId(i);
    }

    public void startRecognition() {
        if (this.mRecognizing) {
            return;
        }
        if (!hasFocus()) {
            requestFocus();
        }
        if (this.mSpeechRecognitionCallback != null) {
            this.mSearchTextEditor.setText("");
            this.mSearchTextEditor.setHint("");
            this.mSpeechRecognitionCallback.recognizeSpeech();
            this.mRecognizing = true;
        } else if (this.mSpeechRecognizer == null) {
        } else {
            if (getContext().checkCallingOrSelfPermission("android.permission.RECORD_AUDIO") != 0) {
                if (Build.VERSION.SDK_INT < 23 || this.mPermissionListener == null) {
                    throw new IllegalStateException("android.permission.RECORD_AUDIO required for search");
                }
                this.mPermissionListener.requestAudioPermission();
                return;
            }
            this.mRecognizing = true;
            if (this.mAudioManager.requestAudioFocus(this.mAudioFocusChangeListener, 3, 3) != 1) {
                Log.w(TAG, "Could not get audio focus");
            }
            this.mSearchTextEditor.setText("");
            Intent intent = new Intent("android.speech.action.RECOGNIZE_SPEECH");
            intent.putExtra("android.speech.extra.LANGUAGE_MODEL", "free_form");
            intent.putExtra("android.speech.extra.PARTIAL_RESULTS", true);
            this.mSpeechRecognizer.setRecognitionListener(new RecognitionListener(this) { // from class: android.support.v17.leanback.widget.SearchBar.10
                final SearchBar this$0;

                {
                    this.this$0 = this;
                }

                @Override // android.speech.RecognitionListener
                public void onBeginningOfSpeech() {
                }

                @Override // android.speech.RecognitionListener
                public void onBufferReceived(byte[] bArr) {
                }

                @Override // android.speech.RecognitionListener
                public void onEndOfSpeech() {
                }

                @Override // android.speech.RecognitionListener
                public void onError(int i) {
                    switch (i) {
                        case 1:
                            Log.w(SearchBar.TAG, "recognizer network timeout");
                            break;
                        case 2:
                            Log.w(SearchBar.TAG, "recognizer network error");
                            break;
                        case 3:
                            Log.w(SearchBar.TAG, "recognizer audio error");
                            break;
                        case 4:
                            Log.w(SearchBar.TAG, "recognizer server error");
                            break;
                        case 5:
                            Log.w(SearchBar.TAG, "recognizer client error");
                            break;
                        case 6:
                            Log.w(SearchBar.TAG, "recognizer speech timeout");
                            break;
                        case 7:
                            Log.w(SearchBar.TAG, "recognizer no match");
                            break;
                        case 8:
                            Log.w(SearchBar.TAG, "recognizer busy");
                            break;
                        case 9:
                            Log.w(SearchBar.TAG, "recognizer insufficient permissions");
                            break;
                        default:
                            Log.d(SearchBar.TAG, "recognizer other error");
                            break;
                    }
                    this.this$0.stopRecognition();
                    this.this$0.playSearchFailure();
                }

                @Override // android.speech.RecognitionListener
                public void onEvent(int i, Bundle bundle) {
                }

                @Override // android.speech.RecognitionListener
                public void onPartialResults(Bundle bundle) {
                    ArrayList<String> stringArrayList = bundle.getStringArrayList("results_recognition");
                    if (stringArrayList == null || stringArrayList.size() == 0) {
                        return;
                    }
                    this.this$0.mSearchTextEditor.updateRecognizedText(stringArrayList.get(0), stringArrayList.size() > 1 ? stringArrayList.get(1) : null);
                }

                @Override // android.speech.RecognitionListener
                public void onReadyForSpeech(Bundle bundle) {
                    this.this$0.mSpeechOrbView.showListening();
                    this.this$0.playSearchOpen();
                }

                @Override // android.speech.RecognitionListener
                public void onResults(Bundle bundle) {
                    ArrayList<String> stringArrayList = bundle.getStringArrayList("results_recognition");
                    if (stringArrayList != null) {
                        this.this$0.mSearchQuery = stringArrayList.get(0);
                        this.this$0.mSearchTextEditor.setText(this.this$0.mSearchQuery);
                        this.this$0.submitQuery();
                    }
                    this.this$0.stopRecognition();
                    this.this$0.playSearchSuccess();
                }

                @Override // android.speech.RecognitionListener
                public void onRmsChanged(float f) {
                    this.this$0.mSpeechOrbView.setSoundLevel(f < 0.0f ? 0 : (int) (10.0f * f));
                }
            });
            this.mListening = true;
            this.mSpeechRecognizer.startListening(intent);
        }
    }

    public void stopRecognition() {
        if (this.mRecognizing) {
            this.mSearchTextEditor.setText(this.mSearchQuery);
            this.mSearchTextEditor.setHint(this.mHint);
            this.mRecognizing = false;
            if (this.mSpeechRecognitionCallback != null || this.mSpeechRecognizer == null) {
                return;
            }
            this.mSpeechOrbView.showNotListening();
            if (this.mListening) {
                this.mSpeechRecognizer.cancel();
                this.mListening = false;
                this.mAudioManager.abandonAudioFocus(this.mAudioFocusChangeListener);
            }
            this.mSpeechRecognizer.setRecognitionListener(null);
        }
    }
}

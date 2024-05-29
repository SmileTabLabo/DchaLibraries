package com.android.systemui.volume;

import android.animation.LayoutTransition;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.service.notification.Condition;
import android.service.notification.ZenModeConfig;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.ArraySet;
import android.util.AttributeSet;
import android.util.Log;
import android.util.MathUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.Prefs;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.volume.Interaction;
import com.android.systemui.volume.SegmentedButtons;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Objects;
/* loaded from: a.zip:com/android/systemui/volume/ZenModePanel.class */
public class ZenModePanel extends LinearLayout {
    private boolean mAttached;
    private int mAttachedZen;
    private int mBucketIndex;
    private Callback mCallback;
    private Condition[] mConditions;
    private final Context mContext;
    private ZenModeController mController;
    private boolean mCountdownConditionSupported;
    private Condition mExitCondition;
    private boolean mExpanded;
    private final Uri mForeverId;
    private final H mHandler;
    private boolean mHidden;
    protected final LayoutInflater mInflater;
    private final Interaction.Callback mInteractionCallback;
    private final ZenPrefs mPrefs;
    private boolean mRequestingConditions;
    private Condition mSessionExitCondition;
    private int mSessionZen;
    private final SpTexts mSpTexts;
    private String mTag;
    private Condition mTimeCondition;
    private final TransitionHelper mTransitionHelper;
    private boolean mVoiceCapable;
    private TextView mZenAlarmWarning;
    protected SegmentedButtons mZenButtons;
    protected final SegmentedButtons.Callback mZenButtonsCallback;
    private final ZenModeController.Callback mZenCallback;
    protected LinearLayout mZenConditions;
    private View mZenIntroduction;
    private View mZenIntroductionConfirm;
    private TextView mZenIntroductionCustomize;
    private TextView mZenIntroductionMessage;
    private RadioGroup mZenRadioGroup;
    private LinearLayout mZenRadioGroupContent;
    private static final boolean DEBUG = Log.isLoggable("ZenModePanel", 3);
    private static final int[] MINUTE_BUCKETS = ZenModeConfig.MINUTE_BUCKETS;
    private static final int MIN_BUCKET_MINUTES = MINUTE_BUCKETS[0];
    private static final int MAX_BUCKET_MINUTES = MINUTE_BUCKETS[MINUTE_BUCKETS.length - 1];
    private static final int DEFAULT_BUCKET_INDEX = Arrays.binarySearch(MINUTE_BUCKETS, 60);
    public static final Intent ZEN_SETTINGS = new Intent("android.settings.ZEN_MODE_SETTINGS");
    public static final Intent ZEN_PRIORITY_SETTINGS = new Intent("android.settings.ZEN_MODE_PRIORITY_SETTINGS");

    /* renamed from: com.android.systemui.volume.ZenModePanel$2  reason: invalid class name */
    /* loaded from: a.zip:com/android/systemui/volume/ZenModePanel$2.class */
    class AnonymousClass2 implements SegmentedButtons.Callback {
        final ZenModePanel this$0;

        AnonymousClass2(ZenModePanel zenModePanel) {
            this.this$0 = zenModePanel;
        }

        @Override // com.android.systemui.volume.Interaction.Callback
        public void onInteraction() {
            this.this$0.fireInteraction();
        }

        @Override // com.android.systemui.volume.SegmentedButtons.Callback
        public void onSelected(Object obj, boolean z) {
            if (obj != null && this.this$0.mZenButtons.isShown() && this.this$0.isAttachedToWindow()) {
                int intValue = ((Integer) obj).intValue();
                if (z) {
                    MetricsLogger.action(this.this$0.mContext, 165, intValue);
                }
                if (ZenModePanel.DEBUG) {
                    Log.d(this.this$0.mTag, "mZenButtonsCallback selected=" + intValue);
                }
                AsyncTask.execute(new Runnable(this, intValue, this.this$0.getRealConditionId(this.this$0.mSessionExitCondition)) { // from class: com.android.systemui.volume.ZenModePanel.2.1
                    final AnonymousClass2 this$1;
                    final Uri val$realConditionId;
                    final int val$zen;

                    {
                        this.this$1 = this;
                        this.val$zen = intValue;
                        this.val$realConditionId = r6;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        this.this$1.this$0.mController.setZen(this.val$zen, this.val$realConditionId, "ZenModePanel.selectZen");
                        if (this.val$zen != 0) {
                            Prefs.putInt(this.this$1.this$0.mContext, "DndFavoriteZen", this.val$zen);
                        }
                    }
                });
            }
        }
    }

    /* loaded from: a.zip:com/android/systemui/volume/ZenModePanel$Callback.class */
    public interface Callback {
        void onExpanded(boolean z);

        void onInteraction();

        void onPrioritySettings();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/volume/ZenModePanel$ConditionTag.class */
    public static class ConditionTag {
        Condition condition;
        TextView line1;
        TextView line2;
        View lines;
        RadioButton rb;

        private ConditionTag() {
        }

        /* synthetic */ ConditionTag(ConditionTag conditionTag) {
            this();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/volume/ZenModePanel$H.class */
    public final class H extends Handler {
        final ZenModePanel this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        private H(ZenModePanel zenModePanel) {
            super(Looper.getMainLooper());
            this.this$0 = zenModePanel;
        }

        /* synthetic */ H(ZenModePanel zenModePanel, H h) {
            this(zenModePanel);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            switch (message.what) {
                case 2:
                    this.this$0.handleUpdateManualRule((ZenModeConfig.ZenRule) message.obj);
                    return;
                case 3:
                    this.this$0.updateWidgets();
                    return;
                default:
                    return;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/volume/ZenModePanel$TransitionHelper.class */
    public final class TransitionHelper implements LayoutTransition.TransitionListener, Runnable {
        private boolean mPendingUpdateWidgets;
        private boolean mTransitioning;
        private final ArraySet<View> mTransitioningViews;
        final ZenModePanel this$0;

        private TransitionHelper(ZenModePanel zenModePanel) {
            this.this$0 = zenModePanel;
            this.mTransitioningViews = new ArraySet<>();
        }

        /* synthetic */ TransitionHelper(ZenModePanel zenModePanel, TransitionHelper transitionHelper) {
            this(zenModePanel);
        }

        private void updateTransitioning() {
            boolean isTransitioning = isTransitioning();
            if (this.mTransitioning == isTransitioning) {
                return;
            }
            this.mTransitioning = isTransitioning;
            if (ZenModePanel.DEBUG) {
                Log.d(this.this$0.mTag, "TransitionHelper mTransitioning=" + this.mTransitioning);
            }
            if (this.mTransitioning) {
                return;
            }
            if (this.mPendingUpdateWidgets) {
                this.this$0.mHandler.post(this);
            } else {
                this.mPendingUpdateWidgets = false;
            }
        }

        public void clear() {
            this.mTransitioningViews.clear();
            this.mPendingUpdateWidgets = false;
        }

        @Override // android.animation.LayoutTransition.TransitionListener
        public void endTransition(LayoutTransition layoutTransition, ViewGroup viewGroup, View view, int i) {
            this.mTransitioningViews.remove(view);
            updateTransitioning();
        }

        public boolean isTransitioning() {
            return !this.mTransitioningViews.isEmpty();
        }

        public void pendingUpdateWidgets() {
            this.mPendingUpdateWidgets = true;
        }

        @Override // java.lang.Runnable
        public void run() {
            if (ZenModePanel.DEBUG) {
                Log.d(this.this$0.mTag, "TransitionHelper run mPendingUpdateWidgets=" + this.mPendingUpdateWidgets);
            }
            if (this.mPendingUpdateWidgets) {
                this.this$0.updateWidgets();
            }
            this.mPendingUpdateWidgets = false;
        }

        @Override // android.animation.LayoutTransition.TransitionListener
        public void startTransition(LayoutTransition layoutTransition, ViewGroup viewGroup, View view, int i) {
            this.mTransitioningViews.add(view);
            updateTransitioning();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/volume/ZenModePanel$ZenPrefs.class */
    public final class ZenPrefs implements SharedPreferences.OnSharedPreferenceChangeListener {
        private boolean mConfirmedPriorityIntroduction;
        private boolean mConfirmedSilenceIntroduction;
        private int mMinuteIndex;
        private final int mNoneDangerousThreshold;
        private int mNoneSelected;
        final ZenModePanel this$0;

        private ZenPrefs(ZenModePanel zenModePanel) {
            this.this$0 = zenModePanel;
            this.mNoneDangerousThreshold = zenModePanel.mContext.getResources().getInteger(2131755086);
            Prefs.registerListener(zenModePanel.mContext, this);
            updateMinuteIndex();
            updateNoneSelected();
            updateConfirmedPriorityIntroduction();
            updateConfirmedSilenceIntroduction();
        }

        /* synthetic */ ZenPrefs(ZenModePanel zenModePanel, ZenPrefs zenPrefs) {
            this(zenModePanel);
        }

        private int clampIndex(int i) {
            return MathUtils.constrain(i, -1, ZenModePanel.MINUTE_BUCKETS.length - 1);
        }

        private int clampNoneSelected(int i) {
            return MathUtils.constrain(i, 0, Integer.MAX_VALUE);
        }

        private void updateConfirmedPriorityIntroduction() {
            boolean z = Prefs.getBoolean(this.this$0.mContext, "DndConfirmedPriorityIntroduction", false);
            if (z == this.mConfirmedPriorityIntroduction) {
                return;
            }
            this.mConfirmedPriorityIntroduction = z;
            if (ZenModePanel.DEBUG) {
                Log.d(this.this$0.mTag, "Confirmed priority introduction: " + this.mConfirmedPriorityIntroduction);
            }
        }

        private void updateConfirmedSilenceIntroduction() {
            boolean z = Prefs.getBoolean(this.this$0.mContext, "DndConfirmedSilenceIntroduction", false);
            if (z == this.mConfirmedSilenceIntroduction) {
                return;
            }
            this.mConfirmedSilenceIntroduction = z;
            if (ZenModePanel.DEBUG) {
                Log.d(this.this$0.mTag, "Confirmed silence introduction: " + this.mConfirmedSilenceIntroduction);
            }
        }

        private void updateMinuteIndex() {
            this.mMinuteIndex = clampIndex(Prefs.getInt(this.this$0.mContext, "DndCountdownMinuteIndex", ZenModePanel.DEFAULT_BUCKET_INDEX));
            if (ZenModePanel.DEBUG) {
                Log.d(this.this$0.mTag, "Favorite minute index: " + this.mMinuteIndex);
            }
        }

        private void updateNoneSelected() {
            this.mNoneSelected = clampNoneSelected(Prefs.getInt(this.this$0.mContext, "DndNoneSelected", 0));
            if (ZenModePanel.DEBUG) {
                Log.d(this.this$0.mTag, "None selected: " + this.mNoneSelected);
            }
        }

        public int getMinuteIndex() {
            return this.mMinuteIndex;
        }

        @Override // android.content.SharedPreferences.OnSharedPreferenceChangeListener
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String str) {
            updateMinuteIndex();
            updateNoneSelected();
            updateConfirmedPriorityIntroduction();
            updateConfirmedSilenceIntroduction();
        }

        public void setMinuteIndex(int i) {
            int clampIndex = clampIndex(i);
            if (clampIndex == this.mMinuteIndex) {
                return;
            }
            this.mMinuteIndex = clampIndex(clampIndex);
            if (ZenModePanel.DEBUG) {
                Log.d(this.this$0.mTag, "Setting favorite minute index: " + this.mMinuteIndex);
            }
            Prefs.putInt(this.this$0.mContext, "DndCountdownMinuteIndex", this.mMinuteIndex);
        }

        public void trackNoneSelected() {
            this.mNoneSelected = clampNoneSelected(this.mNoneSelected + 1);
            if (ZenModePanel.DEBUG) {
                Log.d(this.this$0.mTag, "Setting none selected: " + this.mNoneSelected + " threshold=" + this.mNoneDangerousThreshold);
            }
            Prefs.putInt(this.this$0.mContext, "DndNoneSelected", this.mNoneSelected);
        }
    }

    public ZenModePanel(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mHandler = new H(this, null);
        this.mTransitionHelper = new TransitionHelper(this, null);
        this.mTag = "ZenModePanel/" + Integer.toHexString(System.identityHashCode(this));
        this.mBucketIndex = -1;
        this.mZenCallback = new ZenModeController.Callback(this) { // from class: com.android.systemui.volume.ZenModePanel.1
            final ZenModePanel this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.systemui.statusbar.policy.ZenModeController.Callback
            public void onManualRuleChanged(ZenModeConfig.ZenRule zenRule) {
                this.this$0.mHandler.obtainMessage(2, zenRule).sendToTarget();
            }
        };
        this.mZenButtonsCallback = new AnonymousClass2(this);
        this.mInteractionCallback = new Interaction.Callback(this) { // from class: com.android.systemui.volume.ZenModePanel.3
            final ZenModePanel this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.systemui.volume.Interaction.Callback
            public void onInteraction() {
                this.this$0.fireInteraction();
            }
        };
        this.mContext = context;
        this.mPrefs = new ZenPrefs(this, null);
        this.mInflater = LayoutInflater.from(this.mContext.getApplicationContext());
        this.mForeverId = Condition.newId(this.mContext).appendPath("forever").build();
        this.mSpTexts = new SpTexts(this.mContext);
        this.mVoiceCapable = Util.isVoiceCapable(this.mContext);
        if (DEBUG) {
            Log.d(this.mTag, "new ZenModePanel");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void announceConditionSelection(ConditionTag conditionTag) {
        String string;
        switch (getSelectedZen(0)) {
            case 1:
                string = this.mContext.getString(2131493614);
                break;
            case 2:
                string = this.mContext.getString(2131493613);
                break;
            case 3:
                string = this.mContext.getString(2131493615);
                break;
            default:
                return;
        }
        announceForAccessibility(this.mContext.getString(2131493680, string, conditionTag.line1.getText()));
    }

    private void bind(Condition condition, View view, int i) {
        if (condition == null) {
            throw new IllegalArgumentException("condition must not be null");
        }
        boolean z = condition.state == 1;
        ConditionTag conditionTag = view.getTag() != null ? (ConditionTag) view.getTag() : new ConditionTag(null);
        view.setTag(conditionTag);
        boolean z2 = conditionTag.rb == null;
        if (conditionTag.rb == null) {
            conditionTag.rb = (RadioButton) this.mZenRadioGroup.getChildAt(i);
        }
        conditionTag.condition = condition;
        Uri conditionId = getConditionId(conditionTag.condition);
        if (DEBUG) {
            Log.d(this.mTag, "bind i=" + this.mZenRadioGroupContent.indexOfChild(view) + " first=" + z2 + " condition=" + conditionId);
        }
        conditionTag.rb.setEnabled(z);
        conditionTag.rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(this, conditionTag, conditionId) { // from class: com.android.systemui.volume.ZenModePanel.6
            final ZenModePanel this$0;
            final Uri val$conditionId;
            final ConditionTag val$tag;

            {
                this.this$0 = this;
                this.val$tag = conditionTag;
                this.val$conditionId = conditionId;
            }

            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public void onCheckedChanged(CompoundButton compoundButton, boolean z3) {
                if (this.this$0.mExpanded && z3) {
                    this.val$tag.rb.setChecked(true);
                    if (ZenModePanel.DEBUG) {
                        Log.d(this.this$0.mTag, "onCheckedChanged " + this.val$conditionId);
                    }
                    MetricsLogger.action(this.this$0.mContext, 164);
                    this.this$0.select(this.val$tag.condition);
                    this.this$0.announceConditionSelection(this.val$tag);
                }
            }
        });
        if (conditionTag.lines == null) {
            conditionTag.lines = view.findViewById(16908290);
        }
        if (conditionTag.line1 == null) {
            conditionTag.line1 = (TextView) view.findViewById(16908308);
            this.mSpTexts.add(conditionTag.line1);
        }
        if (conditionTag.line2 == null) {
            conditionTag.line2 = (TextView) view.findViewById(16908309);
            this.mSpTexts.add(conditionTag.line2);
        }
        String str = !TextUtils.isEmpty(condition.line1) ? condition.line1 : condition.summary;
        String str2 = condition.line2;
        conditionTag.line1.setText(str);
        if (TextUtils.isEmpty(str2)) {
            conditionTag.line2.setVisibility(8);
        } else {
            conditionTag.line2.setVisibility(0);
            conditionTag.line2.setText(str2);
        }
        conditionTag.lines.setEnabled(z);
        conditionTag.lines.setAlpha(z ? 1.0f : 0.4f);
        ImageView imageView = (ImageView) view.findViewById(16908313);
        imageView.setOnClickListener(new View.OnClickListener(this, view, conditionTag, i) { // from class: com.android.systemui.volume.ZenModePanel.7
            final ZenModePanel this$0;
            final View val$row;
            final int val$rowId;
            final ConditionTag val$tag;

            {
                this.this$0 = this;
                this.val$row = view;
                this.val$tag = conditionTag;
                this.val$rowId = i;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view2) {
                this.this$0.onClickTimeButton(this.val$row, this.val$tag, false, this.val$rowId);
            }
        });
        ImageView imageView2 = (ImageView) view.findViewById(16908314);
        imageView2.setOnClickListener(new View.OnClickListener(this, view, conditionTag, i) { // from class: com.android.systemui.volume.ZenModePanel.8
            final ZenModePanel this$0;
            final View val$row;
            final int val$rowId;
            final ConditionTag val$tag;

            {
                this.this$0 = this;
                this.val$row = view;
                this.val$tag = conditionTag;
                this.val$rowId = i;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view2) {
                this.this$0.onClickTimeButton(this.val$row, this.val$tag, true, this.val$rowId);
            }
        });
        conditionTag.lines.setOnClickListener(new View.OnClickListener(this, conditionTag) { // from class: com.android.systemui.volume.ZenModePanel.9
            final ZenModePanel this$0;
            final ConditionTag val$tag;

            {
                this.this$0 = this;
                this.val$tag = conditionTag;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view2) {
                this.val$tag.rb.setChecked(true);
            }
        });
        long tryParseCountdownConditionId = ZenModeConfig.tryParseCountdownConditionId(conditionId);
        if (i == 2 || tryParseCountdownConditionId <= 0) {
            imageView.setVisibility(8);
            imageView2.setVisibility(8);
        } else {
            imageView.setVisibility(0);
            imageView2.setVisibility(0);
            if (this.mBucketIndex > -1) {
                imageView.setEnabled(this.mBucketIndex > 0);
                imageView2.setEnabled(this.mBucketIndex < MINUTE_BUCKETS.length - 1);
            } else {
                imageView.setEnabled(tryParseCountdownConditionId - System.currentTimeMillis() > ((long) (MIN_BUCKET_MINUTES * 60000)));
                imageView2.setEnabled(!Objects.equals(condition.summary, ZenModeConfig.toTimeCondition(this.mContext, MAX_BUCKET_MINUTES, ActivityManager.getCurrentUser()).summary));
            }
            imageView.setAlpha(imageView.isEnabled() ? 1.0f : 0.5f);
            imageView2.setAlpha(imageView2.isEnabled() ? 1.0f : 0.5f);
        }
        if (z2) {
            Interaction.register(conditionTag.rb, this.mInteractionCallback);
            Interaction.register(conditionTag.lines, this.mInteractionCallback);
            Interaction.register(imageView, this.mInteractionCallback);
            Interaction.register(imageView2, this.mInteractionCallback);
        }
        view.setVisibility(0);
    }

    private void checkForAttachedZenChange() {
        int selectedZen = getSelectedZen(-1);
        if (DEBUG) {
            Log.d(this.mTag, "selectedZen=" + selectedZen);
        }
        if (selectedZen != this.mAttachedZen) {
            if (DEBUG) {
                Log.d(this.mTag, "attachedZen: " + this.mAttachedZen + " -> " + selectedZen);
            }
            if (selectedZen == 2) {
                this.mPrefs.trackNoneSelected();
            }
        }
    }

    private String computeAlarmWarningText(boolean z) {
        int i;
        if (z) {
            long currentTimeMillis = System.currentTimeMillis();
            long nextAlarm = this.mController.getNextAlarm();
            if (nextAlarm < currentTimeMillis) {
                return null;
            }
            if (this.mSessionExitCondition == null || isForever(this.mSessionExitCondition)) {
                i = 2131493724;
            } else {
                long tryParseCountdownConditionId = ZenModeConfig.tryParseCountdownConditionId(this.mSessionExitCondition.id);
                i = 0;
                if (tryParseCountdownConditionId > currentTimeMillis) {
                    i = 0;
                    if (nextAlarm < tryParseCountdownConditionId) {
                        i = 2131493725;
                    }
                }
            }
            if (i == 0) {
                return null;
            }
            boolean z2 = nextAlarm - currentTimeMillis < 86400000;
            boolean is24HourFormat = DateFormat.is24HourFormat(this.mContext, ActivityManager.getCurrentUser());
            return getResources().getString(i, getResources().getString(z2 ? 2131493726 : 2131493727, DateFormat.format(DateFormat.getBestDateTimePattern(Locale.getDefault(), z2 ? is24HourFormat ? "Hm" : "hma" : is24HourFormat ? "EEEHm" : "EEEhma"), nextAlarm)));
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void confirmZenIntroduction() {
        String prefKeyForConfirmation = prefKeyForConfirmation(getSelectedZen(0));
        if (prefKeyForConfirmation == null) {
            return;
        }
        if (DEBUG) {
            Log.d("ZenModePanel", "confirmZenIntroduction " + prefKeyForConfirmation);
        }
        Prefs.putBoolean(this.mContext, prefKeyForConfirmation, true);
        this.mHandler.sendEmptyMessage(3);
    }

    private static Condition copy(Condition condition) {
        return condition == null ? null : condition.copy();
    }

    private void ensureSelection() {
        int visibleConditions = getVisibleConditions();
        if (visibleConditions == 0) {
            return;
        }
        for (int i = 0; i < visibleConditions; i++) {
            ConditionTag conditionTagAt = getConditionTagAt(i);
            if (conditionTagAt != null && conditionTagAt.rb.isChecked()) {
                if (DEBUG) {
                    Log.d(this.mTag, "Not selecting a default, checked=" + conditionTagAt.condition);
                    return;
                }
                return;
            }
        }
        ConditionTag conditionTagAt2 = getConditionTagAt(0);
        if (conditionTagAt2 == null) {
            return;
        }
        if (DEBUG) {
            Log.d(this.mTag, "Selecting a default");
        }
        int minuteIndex = this.mPrefs.getMinuteIndex();
        if (minuteIndex == -1 || !this.mCountdownConditionSupported) {
            conditionTagAt2.rb.setChecked(true);
            return;
        }
        this.mTimeCondition = ZenModeConfig.toTimeCondition(this.mContext, MINUTE_BUCKETS[minuteIndex], ActivityManager.getCurrentUser());
        this.mBucketIndex = minuteIndex;
        bind(this.mTimeCondition, this.mZenRadioGroupContent.getChildAt(1), 1);
        getConditionTagAt(1).rb.setChecked(true);
    }

    private void fireExpanded() {
        if (this.mCallback != null) {
            this.mCallback.onExpanded(this.mExpanded);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void fireInteraction() {
        if (this.mCallback != null) {
            this.mCallback.onInteraction();
        }
    }

    private Condition forever() {
        return new Condition(this.mForeverId, foreverSummary(this.mContext), "", "", 0, 1, 0);
    }

    private static String foreverSummary(Context context) {
        return context.getString(17040819);
    }

    private static Uri getConditionId(Condition condition) {
        Uri uri = null;
        if (condition != null) {
            uri = condition.id;
        }
        return uri;
    }

    private ConditionTag getConditionTagAt(int i) {
        return (ConditionTag) this.mZenRadioGroupContent.getChildAt(i).getTag();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Uri getRealConditionId(Condition condition) {
        return isForever(condition) ? null : getConditionId(condition);
    }

    private Condition getSelectedCondition() {
        int visibleConditions = getVisibleConditions();
        for (int i = 0; i < visibleConditions; i++) {
            ConditionTag conditionTagAt = getConditionTagAt(i);
            if (conditionTagAt != null && conditionTagAt.rb.isChecked()) {
                return conditionTagAt.condition;
            }
        }
        return null;
    }

    private int getSelectedZen(int i) {
        Object selectedValue = this.mZenButtons.getSelectedValue();
        if (selectedValue != null) {
            i = ((Integer) selectedValue).intValue();
        }
        return i;
    }

    private Condition getTimeUntilNextAlarmCondition() {
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        long timeInMillis = gregorianCalendar.getTimeInMillis();
        setToMidnight(gregorianCalendar);
        gregorianCalendar.add(5, 6);
        long nextAlarm = this.mController.getNextAlarm();
        if (nextAlarm > 0) {
            GregorianCalendar gregorianCalendar2 = new GregorianCalendar();
            gregorianCalendar2.setTimeInMillis(nextAlarm);
            setToMidnight(gregorianCalendar2);
            if (gregorianCalendar.compareTo((Calendar) gregorianCalendar2) >= 0) {
                return ZenModeConfig.toNextAlarmCondition(this.mContext, timeInMillis, nextAlarm, ActivityManager.getCurrentUser());
            }
            return null;
        }
        return null;
    }

    private int getVisibleConditions() {
        int i = 0;
        int childCount = this.mZenRadioGroupContent.getChildCount();
        for (int i2 = 0; i2 < childCount; i2++) {
            i += this.mZenRadioGroupContent.getChildAt(i2).getVisibility() == 0 ? 1 : 0;
        }
        return i;
    }

    private void handleExitConditionChanged(Condition condition) {
        setExitCondition(condition);
        if (DEBUG) {
            Log.d(this.mTag, "handleExitConditionChanged " + this.mExitCondition);
        }
        int visibleConditions = getVisibleConditions();
        for (int i = 0; i < visibleConditions; i++) {
            ConditionTag conditionTagAt = getConditionTagAt(i);
            if (conditionTagAt != null && sameConditionId(conditionTagAt.condition, this.mExitCondition)) {
                bind(condition, this.mZenRadioGroupContent.getChildAt(i), i);
            }
        }
    }

    private void handleUpdateConditions() {
        if (this.mTransitionHelper.isTransitioning()) {
            return;
        }
        int length = this.mConditions == null ? 0 : this.mConditions.length;
        if (DEBUG) {
            Log.d(this.mTag, "handleUpdateConditions conditionCount=" + length);
        }
        bind(forever(), this.mZenRadioGroupContent.getChildAt(0), 0);
        if (this.mCountdownConditionSupported && this.mTimeCondition != null) {
            bind(this.mTimeCondition, this.mZenRadioGroupContent.getChildAt(1), 1);
        }
        if (this.mCountdownConditionSupported) {
            Condition timeUntilNextAlarmCondition = getTimeUntilNextAlarmCondition();
            if (timeUntilNextAlarmCondition != null) {
                this.mZenRadioGroup.getChildAt(2).setVisibility(0);
                this.mZenRadioGroupContent.getChildAt(2).setVisibility(0);
                bind(timeUntilNextAlarmCondition, this.mZenRadioGroupContent.getChildAt(2), 2);
            } else {
                this.mZenRadioGroup.getChildAt(2).setVisibility(8);
                this.mZenRadioGroupContent.getChildAt(2).setVisibility(8);
            }
        }
        if (this.mExpanded && isShown()) {
            ensureSelection();
        }
        this.mZenConditions.setVisibility(this.mSessionZen != 0 ? 0 : 8);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleUpdateManualRule(ZenModeConfig.ZenRule zenRule) {
        Condition condition = null;
        handleUpdateZen(zenRule != null ? zenRule.zenMode : 0);
        if (zenRule != null) {
            condition = zenRule.condition;
        }
        handleExitConditionChanged(condition);
    }

    private void handleUpdateZen(int i) {
        if (this.mSessionZen != -1 && this.mSessionZen != i) {
            setExpanded(isShown());
            this.mSessionZen = i;
        }
        this.mZenButtons.setSelectedValue(Integer.valueOf(i), false);
        updateWidgets();
        handleUpdateConditions();
        if (this.mExpanded) {
            Condition selectedCondition = getSelectedCondition();
            if (Objects.equals(this.mExitCondition, selectedCondition)) {
                return;
            }
            select(selectedCondition);
        }
    }

    private void hideAllConditions() {
        int childCount = this.mZenRadioGroupContent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            this.mZenRadioGroupContent.getChildAt(i).setVisibility(8);
        }
    }

    private static boolean isCountdown(Condition condition) {
        return condition != null ? ZenModeConfig.isValidCountdownConditionId(condition.id) : false;
    }

    private boolean isForever(Condition condition) {
        return condition != null ? this.mForeverId.equals(condition.id) : false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onClickTimeButton(View view, ConditionTag conditionTag, boolean z, int i) {
        Condition timeCondition;
        Condition condition;
        int i2;
        int i3;
        long j;
        MetricsLogger.action(this.mContext, 163, z);
        int length = MINUTE_BUCKETS.length;
        if (this.mBucketIndex == -1) {
            long tryParseCountdownConditionId = ZenModeConfig.tryParseCountdownConditionId(getConditionId(conditionTag.condition));
            long currentTimeMillis = System.currentTimeMillis();
            int i4 = 0;
            while (true) {
                condition = null;
                if (i4 >= length) {
                    break;
                }
                i2 = z ? i4 : (length - 1) - i4;
                i3 = MINUTE_BUCKETS[i2];
                j = currentTimeMillis + (60000 * i3);
                if ((!z || j <= tryParseCountdownConditionId) && (z || j >= tryParseCountdownConditionId)) {
                    i4++;
                }
            }
            this.mBucketIndex = i2;
            condition = ZenModeConfig.toTimeCondition(this.mContext, j, i3, ActivityManager.getCurrentUser(), false);
            timeCondition = condition;
            if (condition == null) {
                this.mBucketIndex = DEFAULT_BUCKET_INDEX;
                timeCondition = ZenModeConfig.toTimeCondition(this.mContext, MINUTE_BUCKETS[this.mBucketIndex], ActivityManager.getCurrentUser());
            }
        } else {
            this.mBucketIndex = Math.max(0, Math.min(length - 1, (z ? 1 : -1) + this.mBucketIndex));
            timeCondition = ZenModeConfig.toTimeCondition(this.mContext, MINUTE_BUCKETS[this.mBucketIndex], ActivityManager.getCurrentUser());
        }
        this.mTimeCondition = timeCondition;
        bind(this.mTimeCondition, view, i);
        conditionTag.rb.setChecked(true);
        select(this.mTimeCondition);
        announceConditionSelection(conditionTag);
    }

    private static Condition parseExistingTimeCondition(Context context, Condition condition) {
        if (condition == null) {
            return null;
        }
        long tryParseCountdownConditionId = ZenModeConfig.tryParseCountdownConditionId(condition.id);
        if (tryParseCountdownConditionId == 0) {
            return null;
        }
        long currentTimeMillis = tryParseCountdownConditionId - System.currentTimeMillis();
        if (currentTimeMillis <= 0 || currentTimeMillis > MAX_BUCKET_MINUTES * 60000) {
            return null;
        }
        return ZenModeConfig.toTimeCondition(context, tryParseCountdownConditionId, Math.round(((float) currentTimeMillis) / 60000.0f), ActivityManager.getCurrentUser(), false);
    }

    private static String prefKeyForConfirmation(int i) {
        switch (i) {
            case 1:
                return "DndConfirmedPriorityIntroduction";
            case 2:
                return "DndConfirmedSilenceIntroduction";
            default:
                return null;
        }
    }

    private static boolean sameConditionId(Condition condition, Condition condition2) {
        boolean z = false;
        if (condition == null) {
            if (condition2 == null) {
                z = true;
            }
        } else if (condition2 != null) {
            z = condition.id.equals(condition2.id);
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void select(Condition condition) {
        if (DEBUG) {
            Log.d(this.mTag, "select " + condition);
        }
        if (this.mSessionZen == -1 || this.mSessionZen == 0) {
            if (DEBUG) {
                Log.d(this.mTag, "Ignoring condition selection outside of manual zen");
                return;
            }
            return;
        }
        Uri realConditionId = getRealConditionId(condition);
        if (this.mController != null) {
            AsyncTask.execute(new Runnable(this, realConditionId) { // from class: com.android.systemui.volume.ZenModePanel.10
                final ZenModePanel this$0;
                final Uri val$realConditionId;

                {
                    this.this$0 = this;
                    this.val$realConditionId = realConditionId;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$0.mController.setZen(this.this$0.mSessionZen, this.val$realConditionId, "ZenModePanel.selectCondition");
                }
            });
        }
        setExitCondition(condition);
        if (realConditionId == null) {
            this.mPrefs.setMinuteIndex(-1);
        } else if (isCountdown(condition) && this.mBucketIndex != -1) {
            this.mPrefs.setMinuteIndex(this.mBucketIndex);
        }
        setSessionExitCondition(copy(condition));
    }

    private void setExitCondition(Condition condition) {
        if (Objects.equals(this.mExitCondition, condition)) {
            return;
        }
        this.mExitCondition = condition;
        if (DEBUG) {
            Log.d(this.mTag, "mExitCondition=" + getConditionId(this.mExitCondition));
        }
        updateWidgets();
    }

    private void setExpanded(boolean z) {
        if (z == this.mExpanded) {
            return;
        }
        if (DEBUG) {
            Log.d(this.mTag, "setExpanded " + z);
        }
        this.mExpanded = z;
        if (this.mExpanded && isShown()) {
            ensureSelection();
        }
        updateWidgets();
        fireExpanded();
    }

    private void setRequestingConditions(boolean z) {
        if (this.mRequestingConditions == z) {
            return;
        }
        if (DEBUG) {
            Log.d(this.mTag, "setRequestingConditions " + z);
        }
        this.mRequestingConditions = z;
        if (!this.mRequestingConditions) {
            hideAllConditions();
            return;
        }
        this.mTimeCondition = parseExistingTimeCondition(this.mContext, this.mExitCondition);
        if (this.mTimeCondition != null) {
            this.mBucketIndex = -1;
        } else {
            this.mBucketIndex = DEFAULT_BUCKET_INDEX;
            this.mTimeCondition = ZenModeConfig.toTimeCondition(this.mContext, MINUTE_BUCKETS[this.mBucketIndex], ActivityManager.getCurrentUser());
        }
        if (DEBUG) {
            Log.d(this.mTag, "Initial bucket index: " + this.mBucketIndex);
        }
        this.mConditions = null;
        handleUpdateConditions();
    }

    private void setSessionExitCondition(Condition condition) {
        if (Objects.equals(condition, this.mSessionExitCondition)) {
            return;
        }
        if (DEBUG) {
            Log.d(this.mTag, "mSessionExitCondition=" + getConditionId(condition));
        }
        this.mSessionExitCondition = condition;
    }

    private void setToMidnight(Calendar calendar) {
        calendar.set(11, 0);
        calendar.set(12, 0);
        calendar.set(13, 0);
        calendar.set(14, 0);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateWidgets() {
        if (this.mTransitionHelper.isTransitioning()) {
            this.mTransitionHelper.pendingUpdateWidgets();
            return;
        }
        int selectedZen = getSelectedZen(0);
        boolean z = selectedZen == 1;
        boolean z2 = selectedZen == 2;
        boolean z3 = (!z || this.mPrefs.mConfirmedPriorityIntroduction) ? z2 && !this.mPrefs.mConfirmedSilenceIntroduction : true;
        this.mZenButtons.setVisibility(this.mHidden ? 8 : 0);
        this.mZenIntroduction.setVisibility(z3 ? 0 : 8);
        if (z3) {
            this.mZenIntroductionMessage.setText(z ? 2131493601 : this.mVoiceCapable ? 2131493603 : 2131493604);
            this.mZenIntroductionCustomize.setVisibility(z ? 0 : 8);
        }
        String computeAlarmWarningText = computeAlarmWarningText(z2);
        this.mZenAlarmWarning.setVisibility(computeAlarmWarningText != null ? 0 : 8);
        this.mZenAlarmWarning.setText(computeAlarmWarningText);
    }

    protected void addZenConditions(int i) {
        for (int i2 = 0; i2 < i; i2++) {
            View inflate = this.mInflater.inflate(2130968838, (ViewGroup) this, false);
            inflate.setId(i2);
            this.mZenRadioGroup.addView(inflate);
            View inflate2 = this.mInflater.inflate(2130968839, (ViewGroup) this, false);
            inflate2.setId(i2 + i);
            this.mZenRadioGroupContent.addView(inflate2);
        }
    }

    protected void createZenButtons() {
        this.mZenButtons = (SegmentedButtons) findViewById(2131886764);
        this.mZenButtons.addButton(2131493616, 2131493612, 2);
        this.mZenButtons.addButton(2131493618, 2131493615, 3);
        this.mZenButtons.addButton(2131493617, 2131493614, 1);
        this.mZenButtons.setCallback(this.mZenButtonsCallback);
    }

    public void init(ZenModeController zenModeController) {
        this.mController = zenModeController;
        this.mCountdownConditionSupported = this.mController.isCountdownConditionSupported();
        addZenConditions((this.mCountdownConditionSupported ? 2 : 0) + 1);
        this.mSessionZen = getSelectedZen(-1);
        handleUpdateManualRule(this.mController.getManualRule());
        if (DEBUG) {
            Log.d(this.mTag, "init mExitCondition=" + this.mExitCondition);
        }
        hideAllConditions();
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        boolean z = true;
        super.onAttachedToWindow();
        if (DEBUG) {
            Log.d(this.mTag, "onAttachedToWindow");
        }
        this.mAttached = true;
        this.mAttachedZen = getSelectedZen(-1);
        this.mSessionZen = this.mAttachedZen;
        this.mTransitionHelper.clear();
        this.mController.addCallback(this.mZenCallback);
        setSessionExitCondition(copy(this.mExitCondition));
        updateWidgets();
        if (this.mHidden) {
            z = false;
        }
        setRequestingConditions(z);
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        if (this.mZenButtons != null) {
            this.mZenButtons.updateLocale();
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (DEBUG) {
            Log.d(this.mTag, "onDetachedFromWindow");
        }
        checkForAttachedZenChange();
        this.mAttached = false;
        this.mAttachedZen = -1;
        this.mSessionZen = -1;
        this.mController.removeCallback(this.mZenCallback);
        setSessionExitCondition(null);
        setRequestingConditions(false);
        this.mTransitionHelper.clear();
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        createZenButtons();
        this.mZenIntroduction = findViewById(2131886765);
        this.mZenIntroductionMessage = (TextView) findViewById(2131886767);
        this.mSpTexts.add(this.mZenIntroductionMessage);
        this.mZenIntroductionConfirm = findViewById(2131886766);
        this.mZenIntroductionConfirm.setOnClickListener(new View.OnClickListener(this) { // from class: com.android.systemui.volume.ZenModePanel.4
            final ZenModePanel this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                this.this$0.confirmZenIntroduction();
            }
        });
        this.mZenIntroductionCustomize = (TextView) findViewById(2131886768);
        this.mZenIntroductionCustomize.setOnClickListener(new View.OnClickListener(this) { // from class: com.android.systemui.volume.ZenModePanel.5
            final ZenModePanel this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                this.this$0.confirmZenIntroduction();
                if (this.this$0.mCallback != null) {
                    this.this$0.mCallback.onPrioritySettings();
                }
            }
        });
        this.mSpTexts.add(this.mZenIntroductionCustomize);
        this.mZenConditions = (LinearLayout) findViewById(2131886769);
        this.mZenAlarmWarning = (TextView) findViewById(2131886772);
        this.mZenRadioGroup = (RadioGroup) findViewById(2131886770);
        this.mZenRadioGroupContent = (LinearLayout) findViewById(2131886771);
    }

    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }
}

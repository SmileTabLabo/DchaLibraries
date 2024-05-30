package com.android.settings.applications.assist;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.provider.Settings;
import android.service.voice.VoiceInteractionServiceInfo;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.app.AssistUtils;
import com.android.settings.R;
import com.android.settings.applications.defaultapps.DefaultAppPickerFragment;
import com.android.settingslib.applications.DefaultAppInfo;
import com.android.settingslib.widget.CandidateInfo;
import java.util.ArrayList;
import java.util.List;
/* loaded from: classes.dex */
public class DefaultAssistPicker extends DefaultAppPickerFragment {
    private AssistUtils mAssistUtils;
    private final List<Info> mAvailableAssistants = new ArrayList();
    private static final Intent ASSIST_SERVICE_PROBE = new Intent("android.service.voice.VoiceInteractionService");
    private static final Intent ASSIST_ACTIVITY_PROBE = new Intent("android.intent.action.ASSIST");

    @Override // com.android.settingslib.core.instrumentation.Instrumentable
    public int getMetricsCategory() {
        return 843;
    }

    @Override // com.android.settings.widget.RadioButtonPickerFragment
    protected boolean shouldShowItemNone() {
        return true;
    }

    @Override // com.android.settings.applications.defaultapps.DefaultAppPickerFragment, com.android.settings.widget.RadioButtonPickerFragment, com.android.settings.core.InstrumentedPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mAssistUtils = new AssistUtils(context);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.widget.RadioButtonPickerFragment, com.android.settings.core.InstrumentedPreferenceFragment
    public int getPreferenceScreenResId() {
        return R.xml.default_assist_settings;
    }

    @Override // com.android.settings.widget.RadioButtonPickerFragment
    protected List<DefaultAppInfo> getCandidates() {
        this.mAvailableAssistants.clear();
        addAssistServices();
        addAssistActivities();
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        for (Info info : this.mAvailableAssistants) {
            String packageName = info.component.getPackageName();
            if (!arrayList.contains(packageName)) {
                arrayList.add(packageName);
                arrayList2.add(new DefaultAppInfo(getContext(), this.mPm, this.mUserId, info.component));
            }
        }
        return arrayList2;
    }

    @Override // com.android.settings.widget.RadioButtonPickerFragment
    protected String getDefaultKey() {
        ComponentName currentAssist = getCurrentAssist();
        if (currentAssist != null) {
            return new DefaultAppInfo(getContext(), this.mPm, this.mUserId, currentAssist).getKey();
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.applications.defaultapps.DefaultAppPickerFragment
    public String getConfirmationMessage(CandidateInfo candidateInfo) {
        if (candidateInfo == null) {
            return null;
        }
        return getContext().getString(R.string.assistant_security_warning, candidateInfo.loadLabel());
    }

    @Override // com.android.settings.widget.RadioButtonPickerFragment
    protected boolean setDefaultKey(String str) {
        if (TextUtils.isEmpty(str)) {
            setAssistNone();
            return true;
        }
        Info findAssistantByPackageName = findAssistantByPackageName(ComponentName.unflattenFromString(str).getPackageName());
        if (findAssistantByPackageName == null) {
            setAssistNone();
            return true;
        }
        if (findAssistantByPackageName.isVoiceInteractionService()) {
            setAssistService(findAssistantByPackageName);
        } else {
            setAssistActivity(findAssistantByPackageName);
        }
        return true;
    }

    public ComponentName getCurrentAssist() {
        return this.mAssistUtils.getAssistComponentForUser(this.mUserId);
    }

    private void addAssistServices() {
        PackageManager packageManager = this.mPm.getPackageManager();
        for (ResolveInfo resolveInfo : packageManager.queryIntentServices(ASSIST_SERVICE_PROBE, 128)) {
            VoiceInteractionServiceInfo voiceInteractionServiceInfo = new VoiceInteractionServiceInfo(packageManager, resolveInfo.serviceInfo);
            if (voiceInteractionServiceInfo.getSupportsAssist()) {
                this.mAvailableAssistants.add(new Info(new ComponentName(resolveInfo.serviceInfo.packageName, resolveInfo.serviceInfo.name), voiceInteractionServiceInfo));
            }
        }
    }

    private void addAssistActivities() {
        for (ResolveInfo resolveInfo : this.mPm.getPackageManager().queryIntentActivities(ASSIST_ACTIVITY_PROBE, 65536)) {
            this.mAvailableAssistants.add(new Info(new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name)));
        }
    }

    private Info findAssistantByPackageName(String str) {
        for (Info info : this.mAvailableAssistants) {
            if (TextUtils.equals(info.component.getPackageName(), str)) {
                return info;
            }
        }
        return null;
    }

    private void setAssistNone() {
        Settings.Secure.putString(getContext().getContentResolver(), "assistant", "");
        Settings.Secure.putString(getContext().getContentResolver(), "voice_interaction_service", "");
        Settings.Secure.putString(getContext().getContentResolver(), "voice_recognition_service", getDefaultRecognizer());
    }

    private void setAssistService(Info info) {
        String flattenToShortString = info.component.flattenToShortString();
        String flattenToShortString2 = new ComponentName(info.component.getPackageName(), info.voiceInteractionServiceInfo.getRecognitionService()).flattenToShortString();
        Settings.Secure.putString(getContext().getContentResolver(), "assistant", flattenToShortString);
        Settings.Secure.putString(getContext().getContentResolver(), "voice_interaction_service", flattenToShortString);
        Settings.Secure.putString(getContext().getContentResolver(), "voice_recognition_service", flattenToShortString2);
    }

    private void setAssistActivity(Info info) {
        Settings.Secure.putString(getContext().getContentResolver(), "assistant", info.component.flattenToShortString());
        Settings.Secure.putString(getContext().getContentResolver(), "voice_interaction_service", "");
        Settings.Secure.putString(getContext().getContentResolver(), "voice_recognition_service", getDefaultRecognizer());
    }

    private String getDefaultRecognizer() {
        ResolveInfo resolveService = this.mPm.getPackageManager().resolveService(new Intent("android.speech.RecognitionService"), 128);
        if (resolveService == null || resolveService.serviceInfo == null) {
            Log.w("DefaultAssistPicker", "Unable to resolve default voice recognition service.");
            return "";
        }
        return new ComponentName(resolveService.serviceInfo.packageName, resolveService.serviceInfo.name).flattenToShortString();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static class Info {
        public final ComponentName component;
        public final VoiceInteractionServiceInfo voiceInteractionServiceInfo;

        Info(ComponentName componentName) {
            this.component = componentName;
            this.voiceInteractionServiceInfo = null;
        }

        Info(ComponentName componentName, VoiceInteractionServiceInfo voiceInteractionServiceInfo) {
            this.component = componentName;
            this.voiceInteractionServiceInfo = voiceInteractionServiceInfo;
        }

        public boolean isVoiceInteractionService() {
            return this.voiceInteractionServiceInfo != null;
        }
    }
}

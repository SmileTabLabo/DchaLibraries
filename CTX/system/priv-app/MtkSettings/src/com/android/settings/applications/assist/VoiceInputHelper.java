package com.android.settings.applications.assist;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.provider.Settings;
import android.service.voice.VoiceInteractionServiceInfo;
import android.util.ArraySet;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import com.android.internal.R;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.xmlpull.v1.XmlPullParserException;
/* loaded from: classes.dex */
public final class VoiceInputHelper {
    final List<ResolveInfo> mAvailableRecognition;
    final List<ResolveInfo> mAvailableVoiceInteractions;
    final Context mContext;
    ComponentName mCurrentRecognizer;
    ComponentName mCurrentVoiceInteraction;
    final ArrayList<InteractionInfo> mAvailableInteractionInfos = new ArrayList<>();
    final ArrayList<RecognizerInfo> mAvailableRecognizerInfos = new ArrayList<>();

    /* loaded from: classes.dex */
    public static class BaseInfo implements Comparable {
        public final CharSequence appLabel;
        public final ComponentName componentName;
        public final String key;
        public final CharSequence label;
        public final String labelStr;
        public final ServiceInfo service;
        public final ComponentName settings;

        public BaseInfo(PackageManager packageManager, ServiceInfo serviceInfo, String str) {
            this.service = serviceInfo;
            this.componentName = new ComponentName(serviceInfo.packageName, serviceInfo.name);
            this.key = this.componentName.flattenToShortString();
            this.settings = str != null ? new ComponentName(serviceInfo.packageName, str) : null;
            this.label = serviceInfo.loadLabel(packageManager);
            this.labelStr = this.label.toString();
            this.appLabel = serviceInfo.applicationInfo.loadLabel(packageManager);
        }

        @Override // java.lang.Comparable
        public int compareTo(Object obj) {
            return this.labelStr.compareTo(((BaseInfo) obj).labelStr);
        }
    }

    /* loaded from: classes.dex */
    public static class InteractionInfo extends BaseInfo {
        public final VoiceInteractionServiceInfo serviceInfo;

        public InteractionInfo(PackageManager packageManager, VoiceInteractionServiceInfo voiceInteractionServiceInfo) {
            super(packageManager, voiceInteractionServiceInfo.getServiceInfo(), voiceInteractionServiceInfo.getSettingsActivity());
            this.serviceInfo = voiceInteractionServiceInfo;
        }
    }

    /* loaded from: classes.dex */
    public static class RecognizerInfo extends BaseInfo {
        public RecognizerInfo(PackageManager packageManager, ServiceInfo serviceInfo, String str) {
            super(packageManager, serviceInfo, str);
        }
    }

    public VoiceInputHelper(Context context) {
        this.mContext = context;
        this.mAvailableVoiceInteractions = this.mContext.getPackageManager().queryIntentServices(new Intent("android.service.voice.VoiceInteractionService"), 128);
        this.mAvailableRecognition = this.mContext.getPackageManager().queryIntentServices(new Intent("android.speech.RecognitionService"), 128);
    }

    public void buildUi() {
        XmlResourceParser xmlResourceParser;
        String str;
        Resources resourcesForApplication;
        AttributeSet asAttributeSet;
        String string = Settings.Secure.getString(this.mContext.getContentResolver(), "voice_interaction_service");
        if (string == null || string.isEmpty()) {
            this.mCurrentVoiceInteraction = null;
        } else {
            this.mCurrentVoiceInteraction = ComponentName.unflattenFromString(string);
        }
        ArraySet arraySet = new ArraySet();
        int size = this.mAvailableVoiceInteractions.size();
        for (int i = 0; i < size; i++) {
            ResolveInfo resolveInfo = this.mAvailableVoiceInteractions.get(i);
            VoiceInteractionServiceInfo voiceInteractionServiceInfo = new VoiceInteractionServiceInfo(this.mContext.getPackageManager(), resolveInfo.serviceInfo);
            if (voiceInteractionServiceInfo.getParseError() != null) {
                Log.w("VoiceInteractionService", "Error in VoiceInteractionService " + resolveInfo.serviceInfo.packageName + "/" + resolveInfo.serviceInfo.name + ": " + voiceInteractionServiceInfo.getParseError());
            } else {
                this.mAvailableInteractionInfos.add(new InteractionInfo(this.mContext.getPackageManager(), voiceInteractionServiceInfo));
                arraySet.add(new ComponentName(resolveInfo.serviceInfo.packageName, voiceInteractionServiceInfo.getRecognitionService()));
            }
        }
        Collections.sort(this.mAvailableInteractionInfos);
        String string2 = Settings.Secure.getString(this.mContext.getContentResolver(), "voice_recognition_service");
        if (string2 == null || string2.isEmpty()) {
            this.mCurrentRecognizer = null;
        } else {
            this.mCurrentRecognizer = ComponentName.unflattenFromString(string2);
        }
        int size2 = this.mAvailableRecognition.size();
        for (int i2 = 0; i2 < size2; i2++) {
            ResolveInfo resolveInfo2 = this.mAvailableRecognition.get(i2);
            arraySet.contains(new ComponentName(resolveInfo2.serviceInfo.packageName, resolveInfo2.serviceInfo.name));
            ServiceInfo serviceInfo = resolveInfo2.serviceInfo;
            try {
                xmlResourceParser = serviceInfo.loadXmlMetaData(this.mContext.getPackageManager(), "android.speech");
            } catch (PackageManager.NameNotFoundException e) {
                e = e;
                xmlResourceParser = null;
                str = null;
            } catch (IOException e2) {
                e = e2;
                xmlResourceParser = null;
                str = null;
            } catch (XmlPullParserException e3) {
                e = e3;
                xmlResourceParser = null;
                str = null;
            } catch (Throwable th) {
                th = th;
                xmlResourceParser = null;
            }
            if (xmlResourceParser == null) {
                throw new XmlPullParserException("No android.speech meta-data for " + serviceInfo.packageName);
            }
            try {
                try {
                    resourcesForApplication = this.mContext.getPackageManager().getResourcesForApplication(serviceInfo.applicationInfo);
                    asAttributeSet = Xml.asAttributeSet(xmlResourceParser);
                    while (true) {
                        int next = xmlResourceParser.next();
                        if (next == 1 || next == 2) {
                            break;
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (xmlResourceParser != null) {
                        xmlResourceParser.close();
                    }
                    throw th;
                }
            } catch (PackageManager.NameNotFoundException e4) {
                e = e4;
                str = null;
            } catch (IOException e5) {
                e = e5;
                str = null;
            } catch (XmlPullParserException e6) {
                e = e6;
                str = null;
            }
            if (!"recognition-service".equals(xmlResourceParser.getName())) {
                throw new XmlPullParserException("Meta-data does not start with recognition-service tag");
            }
            TypedArray obtainAttributes = resourcesForApplication.obtainAttributes(asAttributeSet, R.styleable.RecognitionService);
            str = obtainAttributes.getString(0);
            try {
                obtainAttributes.recycle();
            } catch (PackageManager.NameNotFoundException e7) {
                e = e7;
                Log.e("VoiceInputHelper", "error parsing recognition service meta-data", e);
                if (xmlResourceParser != null) {
                    xmlResourceParser.close();
                    this.mAvailableRecognizerInfos.add(new RecognizerInfo(this.mContext.getPackageManager(), resolveInfo2.serviceInfo, str));
                } else {
                    this.mAvailableRecognizerInfos.add(new RecognizerInfo(this.mContext.getPackageManager(), resolveInfo2.serviceInfo, str));
                }
            } catch (IOException e8) {
                e = e8;
                Log.e("VoiceInputHelper", "error parsing recognition service meta-data", e);
                if (xmlResourceParser != null) {
                    xmlResourceParser.close();
                    this.mAvailableRecognizerInfos.add(new RecognizerInfo(this.mContext.getPackageManager(), resolveInfo2.serviceInfo, str));
                } else {
                    this.mAvailableRecognizerInfos.add(new RecognizerInfo(this.mContext.getPackageManager(), resolveInfo2.serviceInfo, str));
                }
            } catch (XmlPullParserException e9) {
                e = e9;
                Log.e("VoiceInputHelper", "error parsing recognition service meta-data", e);
                if (xmlResourceParser == null) {
                    this.mAvailableRecognizerInfos.add(new RecognizerInfo(this.mContext.getPackageManager(), resolveInfo2.serviceInfo, str));
                }
                xmlResourceParser.close();
                this.mAvailableRecognizerInfos.add(new RecognizerInfo(this.mContext.getPackageManager(), resolveInfo2.serviceInfo, str));
            }
            if (xmlResourceParser == null) {
                this.mAvailableRecognizerInfos.add(new RecognizerInfo(this.mContext.getPackageManager(), resolveInfo2.serviceInfo, str));
            }
            xmlResourceParser.close();
            this.mAvailableRecognizerInfos.add(new RecognizerInfo(this.mContext.getPackageManager(), resolveInfo2.serviceInfo, str));
        }
        Collections.sort(this.mAvailableRecognizerInfos);
    }
}

package com.android.settings.dashboard.conditional;

import android.content.Context;
import android.os.AsyncTask;
import android.os.PersistableBundle;
import android.util.Log;
import android.util.Xml;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;
/* loaded from: classes.dex */
public class ConditionManager implements LifecycleObserver, OnPause, OnResume {
    private static final Comparator<Condition> CONDITION_COMPARATOR = new Comparator<Condition>() { // from class: com.android.settings.dashboard.conditional.ConditionManager.1
        @Override // java.util.Comparator
        public int compare(Condition condition, Condition condition2) {
            return Long.compare(condition.getLastChange(), condition2.getLastChange());
        }
    };
    private static ConditionManager sInstance;
    private final Context mContext;
    private File mXmlFile;
    private final ArrayList<ConditionListener> mListeners = new ArrayList<>();
    private final ArrayList<Condition> mConditions = new ArrayList<>();

    /* loaded from: classes.dex */
    public interface ConditionListener {
        void onConditionsChanged();
    }

    private ConditionManager(Context context, boolean z) {
        this.mContext = context;
        if (z) {
            Log.d("ConditionManager", "conditions loading synchronously");
            ConditionLoader conditionLoader = new ConditionLoader();
            conditionLoader.onPostExecute(conditionLoader.doInBackground(new Void[0]));
            return;
        }
        Log.d("ConditionManager", "conditions loading asychronously");
        new ConditionLoader().execute(new Void[0]);
    }

    public void refreshAll() {
        ArrayList arrayList = new ArrayList(this.mConditions);
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            ((Condition) arrayList.get(i)).refreshState();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void readFromXml(File file, ArrayList<Condition> arrayList) {
        try {
            XmlPullParser newPullParser = Xml.newPullParser();
            FileReader fileReader = new FileReader(file);
            newPullParser.setInput(fileReader);
            for (int eventType = newPullParser.getEventType(); eventType != 1; eventType = newPullParser.next()) {
                if ("c".equals(newPullParser.getName())) {
                    int depth = newPullParser.getDepth();
                    String attributeValue = newPullParser.getAttributeValue("", "cls");
                    if (!attributeValue.startsWith("com.android.settings.dashboard.conditional.")) {
                        attributeValue = "com.android.settings.dashboard.conditional." + attributeValue;
                    }
                    Condition createCondition = createCondition(Class.forName(attributeValue));
                    PersistableBundle restoreFromXml = PersistableBundle.restoreFromXml(newPullParser);
                    if (createCondition != null) {
                        createCondition.restoreState(restoreFromXml);
                        arrayList.add(createCondition);
                    } else {
                        Log.e("ConditionManager", "failed to add condition: " + attributeValue);
                    }
                    while (newPullParser.getDepth() > depth) {
                        newPullParser.next();
                    }
                }
            }
            fileReader.close();
        } catch (IOException | ClassNotFoundException | XmlPullParserException e) {
            Log.w("ConditionManager", "Problem reading condition_state.xml", e);
        }
    }

    private void saveToXml() {
        try {
            XmlSerializer newSerializer = Xml.newSerializer();
            FileWriter fileWriter = new FileWriter(this.mXmlFile);
            newSerializer.setOutput(fileWriter);
            newSerializer.startDocument("UTF-8", true);
            newSerializer.startTag("", "cs");
            int size = this.mConditions.size();
            for (int i = 0; i < size; i++) {
                PersistableBundle persistableBundle = new PersistableBundle();
                if (this.mConditions.get(i).saveState(persistableBundle)) {
                    newSerializer.startTag("", "c");
                    newSerializer.attribute("", "cls", this.mConditions.get(i).getClass().getSimpleName());
                    persistableBundle.saveToXml(newSerializer);
                    newSerializer.endTag("", "c");
                }
            }
            newSerializer.endTag("", "cs");
            newSerializer.flush();
            fileWriter.close();
        } catch (IOException | XmlPullParserException e) {
            Log.w("ConditionManager", "Problem writing condition_state.xml", e);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void addMissingConditions(ArrayList<Condition> arrayList) {
        addIfMissing(AirplaneModeCondition.class, arrayList);
        addIfMissing(HotspotCondition.class, arrayList);
        addIfMissing(DndCondition.class, arrayList);
        addIfMissing(BatterySaverCondition.class, arrayList);
        addIfMissing(CellularDataCondition.class, arrayList);
        addIfMissing(BackgroundDataCondition.class, arrayList);
        addIfMissing(WorkModeCondition.class, arrayList);
        addIfMissing(NightDisplayCondition.class, arrayList);
        addIfMissing(RingerMutedCondition.class, arrayList);
        addIfMissing(RingerVibrateCondition.class, arrayList);
        Collections.sort(arrayList, CONDITION_COMPARATOR);
    }

    private void addIfMissing(Class<? extends Condition> cls, ArrayList<Condition> arrayList) {
        Condition createCondition;
        if (getCondition(cls, arrayList) == null && (createCondition = createCondition(cls)) != null) {
            arrayList.add(createCondition);
        }
    }

    private Condition createCondition(Class<?> cls) {
        if (AirplaneModeCondition.class == cls) {
            return new AirplaneModeCondition(this);
        }
        if (HotspotCondition.class == cls) {
            return new HotspotCondition(this);
        }
        if (DndCondition.class == cls) {
            return new DndCondition(this);
        }
        if (BatterySaverCondition.class == cls) {
            return new BatterySaverCondition(this);
        }
        if (CellularDataCondition.class == cls) {
            return new CellularDataCondition(this);
        }
        if (BackgroundDataCondition.class == cls) {
            return new BackgroundDataCondition(this);
        }
        if (WorkModeCondition.class == cls) {
            return new WorkModeCondition(this);
        }
        if (NightDisplayCondition.class == cls) {
            return new NightDisplayCondition(this);
        }
        if (RingerMutedCondition.class == cls) {
            return new RingerMutedCondition(this);
        }
        if (RingerVibrateCondition.class == cls) {
            return new RingerVibrateCondition(this);
        }
        Log.e("ConditionManager", "unknown condition class: " + cls.getSimpleName());
        return null;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Context getContext() {
        return this.mContext;
    }

    public <T extends Condition> T getCondition(Class<T> cls) {
        return (T) getCondition(cls, this.mConditions);
    }

    private <T extends Condition> T getCondition(Class<T> cls, List<Condition> list) {
        int size = list.size();
        for (int i = 0; i < size; i++) {
            if (cls.equals(list.get(i).getClass())) {
                return (T) list.get(i);
            }
        }
        return null;
    }

    public List<Condition> getConditions() {
        return this.mConditions;
    }

    public void notifyChanged(Condition condition) {
        saveToXml();
        Collections.sort(this.mConditions, CONDITION_COMPARATOR);
        int size = this.mListeners.size();
        for (int i = 0; i < size; i++) {
            this.mListeners.get(i).onConditionsChanged();
        }
    }

    public void addListener(ConditionListener conditionListener) {
        this.mListeners.add(conditionListener);
        conditionListener.onConditionsChanged();
    }

    public void remListener(ConditionListener conditionListener) {
        this.mListeners.remove(conditionListener);
    }

    @Override // com.android.settingslib.core.lifecycle.events.OnResume
    public void onResume() {
        int size = this.mConditions.size();
        for (int i = 0; i < size; i++) {
            this.mConditions.get(i).onResume();
        }
    }

    @Override // com.android.settingslib.core.lifecycle.events.OnPause
    public void onPause() {
        int size = this.mConditions.size();
        for (int i = 0; i < size; i++) {
            this.mConditions.get(i).onPause();
        }
    }

    /* loaded from: classes.dex */
    private class ConditionLoader extends AsyncTask<Void, Void, ArrayList<Condition>> {
        private ConditionLoader() {
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public ArrayList<Condition> doInBackground(Void... voidArr) {
            Log.d("ConditionManager", "loading conditions from xml");
            ArrayList<Condition> arrayList = new ArrayList<>();
            ConditionManager.this.mXmlFile = new File(ConditionManager.this.mContext.getFilesDir(), "condition_state.xml");
            if (ConditionManager.this.mXmlFile.exists()) {
                ConditionManager.this.readFromXml(ConditionManager.this.mXmlFile, arrayList);
            }
            ConditionManager.this.addMissingConditions(arrayList);
            return arrayList;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onPostExecute(ArrayList<Condition> arrayList) {
            Log.d("ConditionManager", "conditions loaded from xml, refreshing conditions");
            ConditionManager.this.mConditions.clear();
            ConditionManager.this.mConditions.addAll(arrayList);
            ConditionManager.this.refreshAll();
        }
    }

    public static ConditionManager get(Context context) {
        return get(context, true);
    }

    public static ConditionManager get(Context context, boolean z) {
        if (sInstance == null) {
            sInstance = new ConditionManager(context.getApplicationContext(), z);
        }
        return sInstance;
    }
}

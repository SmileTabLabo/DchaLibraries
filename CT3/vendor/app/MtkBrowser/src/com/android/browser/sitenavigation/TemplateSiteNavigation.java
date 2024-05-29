package com.android.browser.sitenavigation;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.util.Log;
import android.util.TypedValue;
import com.android.browser.R;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/* loaded from: b.zip:com/android/browser/sitenavigation/TemplateSiteNavigation.class */
public class TemplateSiteNavigation {
    private static HashMap<Integer, TemplateSiteNavigation> sCachedTemplates = new HashMap<>();
    private static boolean sCountryChanged = false;
    private static String sCurrentCountry = "US";
    private HashMap<String, Object> mData;
    private List<Entity> mTemplate;

    /* loaded from: b.zip:com/android/browser/sitenavigation/TemplateSiteNavigation$CursorListEntityWrapper.class */
    public static abstract class CursorListEntityWrapper implements ListEntityIterator {
        private Cursor mCursor;

        public CursorListEntityWrapper(Cursor cursor) {
            this.mCursor = cursor;
        }

        public Cursor getCursor() {
            return this.mCursor;
        }

        @Override // com.android.browser.sitenavigation.TemplateSiteNavigation.EntityData
        public ListEntityIterator getListIterator(String str) {
            return null;
        }

        @Override // com.android.browser.sitenavigation.TemplateSiteNavigation.ListEntityIterator
        public boolean moveToNext() {
            return this.mCursor.moveToNext();
        }

        @Override // com.android.browser.sitenavigation.TemplateSiteNavigation.ListEntityIterator
        public void reset() {
            this.mCursor.moveToPosition(-1);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: b.zip:com/android/browser/sitenavigation/TemplateSiteNavigation$Entity.class */
    public interface Entity {
        void write(OutputStream outputStream, EntityData entityData) throws IOException;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: b.zip:com/android/browser/sitenavigation/TemplateSiteNavigation$EntityData.class */
    public interface EntityData {
        ListEntityIterator getListIterator(String str);

        void writeValue(OutputStream outputStream, String str) throws IOException;
    }

    /* loaded from: b.zip:com/android/browser/sitenavigation/TemplateSiteNavigation$HashMapEntityData.class */
    static class HashMapEntityData implements EntityData {
        HashMap<String, Object> mData;

        public HashMapEntityData(HashMap<String, Object> hashMap) {
            this.mData = hashMap;
        }

        @Override // com.android.browser.sitenavigation.TemplateSiteNavigation.EntityData
        public ListEntityIterator getListIterator(String str) {
            return (ListEntityIterator) this.mData.get(str);
        }

        @Override // com.android.browser.sitenavigation.TemplateSiteNavigation.EntityData
        public void writeValue(OutputStream outputStream, String str) throws IOException {
            outputStream.write((byte[]) this.mData.get(str));
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: b.zip:com/android/browser/sitenavigation/TemplateSiteNavigation$ListEntity.class */
    public static class ListEntity implements Entity {
        String mKey;
        TemplateSiteNavigation mSubTemplate;

        public ListEntity(Context context, String str, String str2) {
            this.mKey = str;
            this.mSubTemplate = new TemplateSiteNavigation(context, str2, null);
        }

        @Override // com.android.browser.sitenavigation.TemplateSiteNavigation.Entity
        public void write(OutputStream outputStream, EntityData entityData) throws IOException {
            ListEntityIterator listIterator = entityData.getListIterator(this.mKey);
            listIterator.reset();
            while (listIterator.moveToNext()) {
                this.mSubTemplate.write(outputStream, listIterator);
            }
        }
    }

    /* loaded from: b.zip:com/android/browser/sitenavigation/TemplateSiteNavigation$ListEntityIterator.class */
    interface ListEntityIterator extends EntityData {
        boolean moveToNext();

        void reset();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: b.zip:com/android/browser/sitenavigation/TemplateSiteNavigation$SimpleEntity.class */
    public static class SimpleEntity implements Entity {
        String mKey;

        public SimpleEntity(String str) {
            this.mKey = str;
        }

        @Override // com.android.browser.sitenavigation.TemplateSiteNavigation.Entity
        public void write(OutputStream outputStream, EntityData entityData) throws IOException {
            entityData.writeValue(outputStream, this.mKey);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: b.zip:com/android/browser/sitenavigation/TemplateSiteNavigation$StringEntity.class */
    public static class StringEntity implements Entity {
        byte[] mValue;

        public StringEntity(String str) {
            this.mValue = str.getBytes();
        }

        @Override // com.android.browser.sitenavigation.TemplateSiteNavigation.Entity
        public void write(OutputStream outputStream, EntityData entityData) throws IOException {
            outputStream.write(this.mValue);
        }
    }

    private TemplateSiteNavigation(Context context, int i) {
        this(context, readRaw(context, i));
    }

    private TemplateSiteNavigation(Context context, String str) {
        this.mData = new HashMap<>();
        this.mTemplate = new ArrayList();
        parseTemplate(context, replaceConsts(context, str));
    }

    /* synthetic */ TemplateSiteNavigation(Context context, String str, TemplateSiteNavigation templateSiteNavigation) {
        this(context, str);
    }

    private TemplateSiteNavigation(TemplateSiteNavigation templateSiteNavigation) {
        this.mData = new HashMap<>();
        this.mTemplate = templateSiteNavigation.mTemplate;
    }

    public static TemplateSiteNavigation getCachedTemplate(Context context, int i) {
        TemplateSiteNavigation copy;
        String displayCountry = context.getResources().getConfiguration().locale.getDisplayCountry();
        Log.d("@M_browser/TemplateSiteNavigation", "TemplateSiteNavigation.getCachedTemplate() display country :" + displayCountry + ", before country :" + sCurrentCountry);
        if (displayCountry != null && !displayCountry.equals(sCurrentCountry)) {
            sCountryChanged = true;
            sCurrentCountry = displayCountry;
        }
        synchronized (sCachedTemplates) {
            TemplateSiteNavigation templateSiteNavigation = sCachedTemplates.get(Integer.valueOf(i));
            if (templateSiteNavigation == null || sCountryChanged) {
                sCountryChanged = false;
                templateSiteNavigation = new TemplateSiteNavigation(context, i);
                sCachedTemplates.put(Integer.valueOf(i), templateSiteNavigation);
            }
            copy = templateSiteNavigation.copy();
        }
        return copy;
    }

    private static String readRaw(Context context, int i) {
        InputStream openRawResource = context.getResources().openRawResource(i);
        try {
            byte[] bArr = new byte[openRawResource.available()];
            openRawResource.read(bArr);
            openRawResource.close();
            return new String(bArr, "utf-8");
        } catch (IOException e) {
            return "<html><body>Error</body></html>";
        }
    }

    private static String replaceConsts(Context context, String str) {
        String charSequence;
        Pattern compile = Pattern.compile("<%@\\s*(\\w+/\\w+)\\s*%>");
        Resources resources = context.getResources();
        String name = R.class.getPackage().getName();
        Matcher matcher = compile.matcher(str);
        StringBuffer stringBuffer = new StringBuffer();
        while (matcher.find()) {
            String group = matcher.group(1);
            if (group.startsWith("drawable/")) {
                matcher.appendReplacement(stringBuffer, "res/" + group);
            } else {
                int identifier = resources.getIdentifier(group, null, name);
                if (identifier != 0) {
                    TypedValue typedValue = new TypedValue();
                    resources.getValue(identifier, typedValue, true);
                    if (typedValue.type == 5) {
                        float dimension = resources.getDimension(identifier);
                        int i = (int) dimension;
                        charSequence = ((float) i) == dimension ? Integer.toString(i) : Float.toString(dimension);
                    } else {
                        charSequence = typedValue.coerceToString().toString();
                    }
                    matcher.appendReplacement(stringBuffer, charSequence);
                }
            }
        }
        matcher.appendTail(stringBuffer);
        return stringBuffer.toString();
    }

    public void assignLoop(String str, ListEntityIterator listEntityIterator) {
        this.mData.put(str, listEntityIterator);
    }

    TemplateSiteNavigation copy() {
        return new TemplateSiteNavigation(this);
    }

    void parseTemplate(Context context, String str) {
        int i;
        Matcher matcher = Pattern.compile("<%([=\\{])\\s*(\\w+)\\s*%>").matcher(str);
        int i2 = 0;
        while (true) {
            i = i2;
            if (!matcher.find()) {
                break;
            }
            String substring = str.substring(i, matcher.start());
            if (substring.length() > 0) {
                this.mTemplate.add(new StringEntity(substring));
            }
            String group = matcher.group(1);
            String group2 = matcher.group(2);
            if (group.equals("=")) {
                this.mTemplate.add(new SimpleEntity(group2));
            } else if (group.equals("{")) {
                Matcher matcher2 = Pattern.compile("<%\\}\\s*" + Pattern.quote(group2) + "\\s*%>").matcher(str);
                if (matcher2.find(matcher.end())) {
                    int end = matcher.end();
                    matcher.region(matcher2.end(), str.length());
                    this.mTemplate.add(new ListEntity(context, group2, str.substring(end, matcher2.start())));
                    i2 = matcher2.end();
                }
            }
            i2 = matcher.end();
        }
        String substring2 = str.substring(i, str.length());
        if (substring2.length() > 0) {
            this.mTemplate.add(new StringEntity(substring2));
        }
    }

    public void write(OutputStream outputStream) throws IOException {
        write(outputStream, new HashMapEntityData(this.mData));
    }

    public void write(OutputStream outputStream, EntityData entityData) throws IOException {
        for (Entity entity : this.mTemplate) {
            entity.write(outputStream, entityData);
        }
    }
}

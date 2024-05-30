package com.android.browser.homepages;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
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
/* loaded from: classes.dex */
public class Template {
    private static HashMap<Integer, Template> sCachedTemplates = new HashMap<>();
    private HashMap<String, Object> mData;
    private List<Entity> mTemplate;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public interface Entity {
        void write(OutputStream outputStream, EntityData entityData) throws IOException;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public interface EntityData {
        ListEntityIterator getListIterator(String str);

        void writeValue(OutputStream outputStream, String str) throws IOException;
    }

    /* loaded from: classes.dex */
    interface ListEntityIterator extends EntityData {
        boolean moveToNext();

        void reset();
    }

    public static Template getCachedTemplate(Context context, int i) {
        Template copy;
        synchronized (sCachedTemplates) {
            Template template = sCachedTemplates.get(Integer.valueOf(i));
            if (template == null) {
                template = new Template(context, i);
                sCachedTemplates.put(Integer.valueOf(i), template);
            }
            copy = template.copy();
        }
        return copy;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static class StringEntity implements Entity {
        byte[] mValue;

        public StringEntity(String str) {
            this.mValue = str.getBytes();
        }

        @Override // com.android.browser.homepages.Template.Entity
        public void write(OutputStream outputStream, EntityData entityData) throws IOException {
            outputStream.write(this.mValue);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static class SimpleEntity implements Entity {
        String mKey;

        public SimpleEntity(String str) {
            this.mKey = str;
        }

        @Override // com.android.browser.homepages.Template.Entity
        public void write(OutputStream outputStream, EntityData entityData) throws IOException {
            entityData.writeValue(outputStream, this.mKey);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static class ListEntity implements Entity {
        String mKey;
        Template mSubTemplate;

        public ListEntity(Context context, String str, String str2) {
            this.mKey = str;
            this.mSubTemplate = new Template(context, str2);
        }

        @Override // com.android.browser.homepages.Template.Entity
        public void write(OutputStream outputStream, EntityData entityData) throws IOException {
            ListEntityIterator listIterator = entityData.getListIterator(this.mKey);
            listIterator.reset();
            while (listIterator.moveToNext()) {
                this.mSubTemplate.write(outputStream, listIterator);
            }
        }
    }

    /* loaded from: classes.dex */
    public static abstract class CursorListEntityWrapper implements ListEntityIterator {
        private Cursor mCursor;

        public CursorListEntityWrapper(Cursor cursor) {
            this.mCursor = cursor;
        }

        @Override // com.android.browser.homepages.Template.ListEntityIterator
        public boolean moveToNext() {
            return this.mCursor.moveToNext();
        }

        @Override // com.android.browser.homepages.Template.ListEntityIterator
        public void reset() {
            this.mCursor.moveToPosition(-1);
        }

        @Override // com.android.browser.homepages.Template.EntityData
        public ListEntityIterator getListIterator(String str) {
            return null;
        }

        public Cursor getCursor() {
            return this.mCursor;
        }
    }

    /* loaded from: classes.dex */
    static class HashMapEntityData implements EntityData {
        HashMap<String, Object> mData;

        public HashMapEntityData(HashMap<String, Object> hashMap) {
            this.mData = hashMap;
        }

        @Override // com.android.browser.homepages.Template.EntityData
        public ListEntityIterator getListIterator(String str) {
            return (ListEntityIterator) this.mData.get(str);
        }

        @Override // com.android.browser.homepages.Template.EntityData
        public void writeValue(OutputStream outputStream, String str) throws IOException {
            outputStream.write((byte[]) this.mData.get(str));
        }
    }

    private Template(Context context, int i) {
        this(context, readRaw(context, i));
    }

    private Template(Context context, String str) {
        this.mData = new HashMap<>();
        this.mTemplate = new ArrayList();
        parseTemplate(context, replaceConsts(context, str));
    }

    private Template(Template template) {
        this.mData = new HashMap<>();
        this.mTemplate = template.mTemplate;
    }

    Template copy() {
        return new Template(this);
    }

    void parseTemplate(Context context, String str) {
        Matcher matcher = Pattern.compile("<%([=\\{])\\s*(\\w+)\\s*%>").matcher(str);
        int i = 0;
        while (matcher.find()) {
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
                    i = matcher2.end();
                }
            }
            i = matcher.end();
        }
        String substring2 = str.substring(i, str.length());
        if (substring2.length() > 0) {
            this.mTemplate.add(new StringEntity(substring2));
        }
    }

    public void assign(String str, String str2) {
        this.mData.put(str, str2.getBytes());
    }

    public void assignLoop(String str, ListEntityIterator listEntityIterator) {
        this.mData.put(str, listEntityIterator);
    }

    public void write(OutputStream outputStream) throws IOException {
        write(outputStream, new HashMapEntityData(this.mData));
    }

    public void write(OutputStream outputStream, EntityData entityData) throws IOException {
        for (Entity entity : this.mTemplate) {
            entity.write(outputStream, entityData);
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
                        if (i == dimension) {
                            charSequence = Integer.toString(i);
                        } else {
                            charSequence = Float.toString(dimension);
                        }
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

    private static String readRaw(Context context, int i) {
        InputStream openRawResource = context.getResources().openRawResource(i);
        try {
            byte[] bArr = new byte[openRawResource.available()];
            openRawResource.read(bArr);
            return new String(bArr, "utf-8");
        } catch (IOException e) {
            return "<html><body>Error</body></html>";
        }
    }
}

package com.android.launcher3;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.android.launcher3.AutoInstallsLayout;
import com.android.launcher3.DefaultLayoutParser;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParserException;
/* loaded from: a.zip:com/android/launcher3/CommonAppTypeParser.class */
public class CommonAppTypeParser implements AutoInstallsLayout.LayoutParserCallback {
    final Context mContext;
    private final long mItemId;
    final int mResId;
    Intent parsedIntent;
    String parsedTitle;
    ContentValues parsedValues;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/launcher3/CommonAppTypeParser$MyLayoutParser.class */
    public class MyLayoutParser extends DefaultLayoutParser {
        final CommonAppTypeParser this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public MyLayoutParser(CommonAppTypeParser commonAppTypeParser) {
            super(commonAppTypeParser.mContext, null, commonAppTypeParser, commonAppTypeParser.mContext.getResources(), commonAppTypeParser.mResId, "resolve");
            this.this$0 = commonAppTypeParser;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // com.android.launcher3.AutoInstallsLayout
        public long addShortcut(String str, Intent intent, int i) {
            if (i == 0) {
                this.this$0.parsedIntent = intent;
                this.this$0.parsedTitle = str;
            }
            return super.addShortcut(str, intent, i);
        }

        public void parseValues() {
            XmlResourceParser xml = this.mSourceRes.getXml(this.mLayoutId);
            try {
                beginDocument(xml, this.mRootTag);
                new DefaultLayoutParser.ResolveParser(this).parseAndAdd(xml);
            } catch (IOException | XmlPullParserException e) {
                Log.e("CommonAppTypeParser", "Unable to parse default app info", e);
            }
            xml.close();
        }
    }

    public CommonAppTypeParser(long j, int i, Context context) {
        this.mItemId = j;
        this.mContext = context;
        this.mResId = getResourceForItemType(i);
    }

    public static int decodeItemTypeFromFlag(int i) {
        return (i & 240) >> 4;
    }

    public static int encodeItemTypeToFlag(int i) {
        return i << 4;
    }

    public static int getResourceForItemType(int i) {
        switch (i) {
            case 1:
                return 2131165189;
            case 2:
                return 2131165188;
            case 3:
                return 2131165186;
            case 4:
                return 2131165184;
            case 5:
                return 2131165187;
            case 6:
                return 2131165185;
            default:
                return 0;
        }
    }

    public boolean findDefaultApp() {
        if (this.mResId == 0) {
            return false;
        }
        this.parsedIntent = null;
        this.parsedValues = null;
        new MyLayoutParser(this).parseValues();
        boolean z = false;
        if (this.parsedValues != null) {
            z = false;
            if (this.parsedIntent != null) {
                z = true;
            }
        }
        return z;
    }

    @Override // com.android.launcher3.AutoInstallsLayout.LayoutParserCallback
    public long generateNewItemId() {
        return this.mItemId;
    }

    @Override // com.android.launcher3.AutoInstallsLayout.LayoutParserCallback
    public long insertAndCheck(SQLiteDatabase sQLiteDatabase, ContentValues contentValues) {
        this.parsedValues = contentValues;
        contentValues.put("iconType", (Integer) null);
        contentValues.put("iconPackage", (String) null);
        contentValues.put("iconResource", (String) null);
        contentValues.put("icon", (byte[]) null);
        return 1L;
    }
}

package android.support.v7.content.res;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.appcompat.R$attr;
import android.support.v7.appcompat.R$styleable;
import android.util.AttributeSet;
import android.util.StateSet;
import android.util.Xml;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: a.zip:android/support/v7/content/res/AppCompatColorStateListInflater.class */
public final class AppCompatColorStateListInflater {
    private AppCompatColorStateListInflater() {
    }

    @NonNull
    public static ColorStateList createFromXml(@NonNull Resources resources, @NonNull XmlPullParser xmlPullParser, @Nullable Resources.Theme theme) throws XmlPullParserException, IOException {
        int next;
        AttributeSet asAttributeSet = Xml.asAttributeSet(xmlPullParser);
        do {
            next = xmlPullParser.next();
            if (next == 2) {
                break;
            }
        } while (next != 1);
        if (next != 2) {
            throw new XmlPullParserException("No start tag found");
        }
        return createFromXmlInner(resources, xmlPullParser, asAttributeSet, theme);
    }

    @NonNull
    private static ColorStateList createFromXmlInner(@NonNull Resources resources, @NonNull XmlPullParser xmlPullParser, @NonNull AttributeSet attributeSet, @Nullable Resources.Theme theme) throws XmlPullParserException, IOException {
        String name = xmlPullParser.getName();
        if (name.equals("selector")) {
            return inflate(resources, xmlPullParser, attributeSet, theme);
        }
        throw new XmlPullParserException(xmlPullParser.getPositionDescription() + ": invalid color state list tag " + name);
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v15, types: [java.lang.Object, int[], int[][]] */
    /* JADX WARN: Type inference failed for: r0v53, types: [java.lang.Object[]] */
    /* JADX WARN: Type inference failed for: r0v55, types: [int[][]] */
    private static ColorStateList inflate(@NonNull Resources resources, @NonNull XmlPullParser xmlPullParser, @NonNull AttributeSet attributeSet, @Nullable Resources.Theme theme) throws XmlPullParserException, IOException {
        int depth;
        int depth2 = xmlPullParser.getDepth() + 1;
        int[] iArr = new int[20];
        int[] iArr2 = new int[iArr.length];
        int i = 0;
        while (true) {
            int next = xmlPullParser.next();
            if (next == 1 || ((depth = xmlPullParser.getDepth()) < depth2 && next == 3)) {
                break;
            } else if (next == 2 && depth <= depth2 && xmlPullParser.getName().equals("item")) {
                TypedArray obtainAttributes = obtainAttributes(resources, theme, attributeSet, R$styleable.ColorStateListItem);
                int color = obtainAttributes.getColor(R$styleable.ColorStateListItem_android_color, -65281);
                float f = 1.0f;
                if (obtainAttributes.hasValue(R$styleable.ColorStateListItem_android_alpha)) {
                    f = obtainAttributes.getFloat(R$styleable.ColorStateListItem_android_alpha, 1.0f);
                } else if (obtainAttributes.hasValue(R$styleable.ColorStateListItem_alpha)) {
                    f = obtainAttributes.getFloat(R$styleable.ColorStateListItem_alpha, 1.0f);
                }
                obtainAttributes.recycle();
                int attributeCount = attributeSet.getAttributeCount();
                int[] iArr3 = new int[attributeCount];
                int i2 = 0;
                for (int i3 = 0; i3 < attributeCount; i3++) {
                    int attributeNameResource = attributeSet.getAttributeNameResource(i3);
                    if (attributeNameResource != 16843173 && attributeNameResource != 16843551 && attributeNameResource != R$attr.alpha) {
                        int i4 = i2 + 1;
                        if (!attributeSet.getAttributeBooleanValue(i3, false)) {
                            attributeNameResource = -attributeNameResource;
                        }
                        iArr3[i2] = attributeNameResource;
                        i2 = i4;
                    }
                }
                int[] trimStateSet = StateSet.trimStateSet(iArr3, i2);
                int modulateColorAlpha = modulateColorAlpha(color, f);
                if (i == 0 || trimStateSet.length == 0) {
                }
                iArr2 = GrowingArrayUtils.append(iArr2, i, modulateColorAlpha);
                iArr = (int[][]) GrowingArrayUtils.append((int[][]) iArr, i, trimStateSet);
                i++;
            }
        }
        int[] iArr4 = new int[i];
        ?? r0 = new int[i];
        System.arraycopy(iArr2, 0, iArr4, 0, i);
        System.arraycopy(iArr, 0, r0, 0, i);
        return new ColorStateList(r0, iArr4);
    }

    private static int modulateColorAlpha(int i, float f) {
        return ColorUtils.setAlphaComponent(i, Math.round(Color.alpha(i) * f));
    }

    private static TypedArray obtainAttributes(Resources resources, Resources.Theme theme, AttributeSet attributeSet, int[] iArr) {
        return theme == null ? resources.obtainAttributes(attributeSet, iArr) : theme.obtainStyledAttributes(attributeSet, iArr, 0, 0);
    }
}

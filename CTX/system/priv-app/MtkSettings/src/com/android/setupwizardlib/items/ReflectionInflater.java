package com.android.setupwizardlib.items;

import android.content.Context;
import android.util.AttributeSet;
import android.view.InflateException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
/* loaded from: classes.dex */
public abstract class ReflectionInflater<T> extends SimpleInflater<T> {
    private static final Class<?>[] CONSTRUCTOR_SIGNATURE = {Context.class, AttributeSet.class};
    private static final HashMap<String, Constructor<?>> sConstructorMap = new HashMap<>();
    private final Context mContext;
    private String mDefaultPackage;
    private final Object[] mTempConstructorArgs;

    /* JADX INFO: Access modifiers changed from: protected */
    public ReflectionInflater(Context context) {
        super(context.getResources());
        this.mTempConstructorArgs = new Object[2];
        this.mContext = context;
    }

    public final T createItem(String str, String str2, AttributeSet attributeSet) {
        String str3;
        if (str2 != null && str.indexOf(46) == -1) {
            str3 = str2.concat(str);
        } else {
            str3 = str;
        }
        Constructor<?> constructor = sConstructorMap.get(str3);
        if (constructor == null) {
            try {
                constructor = this.mContext.getClassLoader().loadClass(str3).getConstructor(CONSTRUCTOR_SIGNATURE);
                constructor.setAccessible(true);
                sConstructorMap.put(str, constructor);
            } catch (Exception e) {
                throw new InflateException(attributeSet.getPositionDescription() + ": Error inflating class " + str3, e);
            }
        }
        this.mTempConstructorArgs[0] = this.mContext;
        this.mTempConstructorArgs[1] = attributeSet;
        T t = (T) constructor.newInstance(this.mTempConstructorArgs);
        this.mTempConstructorArgs[0] = null;
        this.mTempConstructorArgs[1] = null;
        return t;
    }

    @Override // com.android.setupwizardlib.items.SimpleInflater
    protected T onCreateItem(String str, AttributeSet attributeSet) {
        return createItem(str, this.mDefaultPackage, attributeSet);
    }

    public void setDefaultPackage(String str) {
        this.mDefaultPackage = str;
    }
}

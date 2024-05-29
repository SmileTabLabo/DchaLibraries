package com.android.setupwizardlib.items;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.InflateException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
/* loaded from: classes.dex */
public abstract class GenericInflater<T> {
    private static final Class[] mConstructorSignature = {Context.class, AttributeSet.class};
    private static final HashMap<String, Constructor<?>> sConstructorMap = new HashMap<>();
    private final Object[] mConstructorArgs = new Object[2];
    protected final Context mContext;
    private String mDefaultPackage;
    private Factory<T> mFactory;

    /* loaded from: classes.dex */
    public interface Factory<T> {
        T onCreateItem(String str, Context context, AttributeSet attributeSet);
    }

    protected abstract void onAddChildItem(T t, T t2);

    /* JADX INFO: Access modifiers changed from: protected */
    public GenericInflater(Context context) {
        this.mContext = context;
    }

    public void setDefaultPackage(String defaultPackage) {
        this.mDefaultPackage = defaultPackage;
    }

    public Context getContext() {
        return this.mContext;
    }

    public T inflate(int resource) {
        return inflate(resource, null);
    }

    public T inflate(int resource, T root) {
        return inflate(resource, (int) root, root != null);
    }

    public T inflate(int resource, T root, boolean attachToRoot) {
        XmlResourceParser parser = getContext().getResources().getXml(resource);
        try {
            return inflate((XmlPullParser) parser, (XmlResourceParser) root, attachToRoot);
        } finally {
            parser.close();
        }
    }

    public T inflate(XmlPullParser parser, T root, boolean attachToRoot) {
        int type;
        T result;
        synchronized (this.mConstructorArgs) {
            AttributeSet attrs = Xml.asAttributeSet(parser);
            this.mConstructorArgs[0] = this.mContext;
            do {
                try {
                    type = parser.next();
                    if (type == 2) {
                        break;
                    }
                } catch (IOException e) {
                    InflateException ex = new InflateException(parser.getPositionDescription() + ": " + e.getMessage());
                    ex.initCause(e);
                    throw ex;
                } catch (XmlPullParserException e2) {
                    InflateException ex2 = new InflateException(e2.getMessage());
                    ex2.initCause(e2);
                    throw ex2;
                }
            } while (type != 1);
            if (type != 2) {
                throw new InflateException(parser.getPositionDescription() + ": No start tag found!");
            }
            T xmlRoot = createItemFromTag(parser, parser.getName(), attrs);
            result = onMergeRoots(root, attachToRoot, xmlRoot);
            rInflate(parser, result, attrs);
        }
        return result;
    }

    public final T createItem(String name, String prefix, AttributeSet attrs) throws ClassNotFoundException, InflateException {
        Constructor constructor = sConstructorMap.get(name);
        if (constructor == null) {
            try {
                Class<?> clazz = this.mContext.getClassLoader().loadClass(prefix != null ? prefix + name : name);
                constructor = clazz.getConstructor(mConstructorSignature);
                constructor.setAccessible(true);
                sConstructorMap.put(name, constructor);
            } catch (ClassNotFoundException e) {
                throw e;
            } catch (NoSuchMethodException e2) {
                StringBuilder append = new StringBuilder().append(attrs.getPositionDescription()).append(": Error inflating class ");
                if (prefix != null) {
                    name = prefix + name;
                }
                InflateException ie = new InflateException(append.append(name).toString());
                ie.initCause(e2);
                throw ie;
            } catch (Exception e3) {
                StringBuilder append2 = new StringBuilder().append(attrs.getPositionDescription()).append(": Error inflating class ");
                if (prefix != null) {
                    name = prefix + name;
                }
                InflateException ie2 = new InflateException(append2.append(name).toString());
                ie2.initCause(e3);
                throw ie2;
            }
        }
        Object[] args = this.mConstructorArgs;
        args[1] = attrs;
        return (T) constructor.newInstance(args);
    }

    protected T onCreateItem(String name, AttributeSet attrs) throws ClassNotFoundException {
        return createItem(name, this.mDefaultPackage, attrs);
    }

    private T createItemFromTag(XmlPullParser parser, String name, AttributeSet attrs) {
        try {
            T onCreateItem = this.mFactory == null ? null : this.mFactory.onCreateItem(name, this.mContext, attrs);
            if (onCreateItem == null) {
                if (-1 == name.indexOf(46)) {
                    return onCreateItem(name, attrs);
                }
                return createItem(name, null, attrs);
            }
            return onCreateItem;
        } catch (InflateException e) {
            throw e;
        } catch (Exception e2) {
            InflateException ie = new InflateException(attrs.getPositionDescription() + ": Error inflating class " + name);
            ie.initCause(e2);
            throw ie;
        }
    }

    private void rInflate(XmlPullParser parser, T node, AttributeSet attrs) throws XmlPullParserException, IOException {
        int depth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if ((type == 3 && parser.getDepth() <= depth) || type == 1) {
                return;
            }
            if (type == 2 && !onCreateCustomFromTag(parser, node, attrs)) {
                String name = parser.getName();
                T item = createItemFromTag(parser, name, attrs);
                onAddChildItem(node, item);
                rInflate(parser, item, attrs);
            }
        }
    }

    protected boolean onCreateCustomFromTag(XmlPullParser parser, T node, AttributeSet attrs) throws XmlPullParserException {
        return false;
    }

    protected T onMergeRoots(T givenRoot, boolean attachToGivenRoot, T xmlRoot) {
        return xmlRoot;
    }
}

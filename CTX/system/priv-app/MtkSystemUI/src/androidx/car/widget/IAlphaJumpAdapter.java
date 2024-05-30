package androidx.car.widget;

import java.util.Collection;
/* loaded from: classes.dex */
public interface IAlphaJumpAdapter {

    /* loaded from: classes.dex */
    public interface Bucket {
        int getIndex();

        CharSequence getLabel();

        boolean isEmpty();
    }

    Collection<Bucket> getAlphaJumpBuckets();

    void onAlphaJumpEnter();

    void onAlphaJumpLeave(Bucket bucket);
}

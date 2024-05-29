package androidx.slice.builders.impl;

import android.app.PendingIntent;
import android.support.v4.graphics.drawable.IconCompat;
import androidx.slice.builders.SliceAction;
import java.util.List;
/* loaded from: classes.dex */
public interface ListBuilder {

    /* loaded from: classes.dex */
    public interface InputRangeBuilder extends RangeBuilder {
        void setInputAction(PendingIntent pendingIntent);
    }

    /* loaded from: classes.dex */
    public interface RangeBuilder {
        void setMax(int i);

        void setPrimaryAction(SliceAction sliceAction);

        void setSubtitle(CharSequence charSequence);

        void setTitle(CharSequence charSequence);

        void setValue(int i);
    }

    /* loaded from: classes.dex */
    public interface RowBuilder {
        void addEndItem(SliceAction sliceAction, boolean z);

        void setPrimaryAction(SliceAction sliceAction);

        void setSubtitle(CharSequence charSequence, boolean z);

        void setTitle(CharSequence charSequence);

        void setTitleItem(IconCompat iconCompat, int i, boolean z);
    }

    void addInputRange(TemplateBuilderImpl templateBuilderImpl);

    void addRow(TemplateBuilderImpl templateBuilderImpl);

    TemplateBuilderImpl createInputRangeBuilder();

    TemplateBuilderImpl createRowBuilder();

    void setColor(int i);

    void setKeywords(List<String> list);

    void setTtl(long j);
}

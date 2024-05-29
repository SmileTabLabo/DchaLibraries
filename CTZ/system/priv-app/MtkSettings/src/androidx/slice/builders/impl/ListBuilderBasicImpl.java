package androidx.slice.builders.impl;

import android.support.v4.graphics.drawable.IconCompat;
import androidx.slice.Slice;
import androidx.slice.SliceSpec;
import androidx.slice.builders.SliceAction;
import androidx.slice.builders.impl.ListBuilder;
import androidx.slice.builders.impl.ListBuilderV1Impl;
import java.util.List;
/* loaded from: classes.dex */
public class ListBuilderBasicImpl extends TemplateBuilderImpl implements ListBuilder {
    boolean mIsError;

    public ListBuilderBasicImpl(Slice.Builder b, SliceSpec spec) {
        super(b, spec);
    }

    @Override // androidx.slice.builders.impl.ListBuilder
    public void addRow(TemplateBuilderImpl impl) {
    }

    @Override // androidx.slice.builders.impl.ListBuilder
    public void addInputRange(TemplateBuilderImpl builder) {
    }

    @Override // androidx.slice.builders.impl.ListBuilder
    public void setColor(int color) {
    }

    @Override // androidx.slice.builders.impl.ListBuilder
    public void setKeywords(List<String> keywords) {
        Slice.Builder sb = new Slice.Builder(getBuilder());
        for (int i = 0; i < keywords.size(); i++) {
            sb.addText(keywords.get(i), (String) null, new String[0]);
        }
        getBuilder().addSubSlice(sb.addHints("keywords").build());
    }

    @Override // androidx.slice.builders.impl.ListBuilder
    public void setTtl(long ttl) {
    }

    @Override // androidx.slice.builders.impl.ListBuilder
    public TemplateBuilderImpl createRowBuilder() {
        return new RowBuilderImpl(this);
    }

    @Override // androidx.slice.builders.impl.ListBuilder
    public TemplateBuilderImpl createInputRangeBuilder() {
        return new ListBuilderV1Impl.InputRangeBuilderImpl(getBuilder());
    }

    @Override // androidx.slice.builders.impl.TemplateBuilderImpl
    public void apply(Slice.Builder builder) {
        if (this.mIsError) {
            builder.addHints("error");
        }
    }

    /* loaded from: classes.dex */
    public static class RowBuilderImpl extends TemplateBuilderImpl implements ListBuilder.RowBuilder {
        public RowBuilderImpl(ListBuilderBasicImpl parent) {
            super(parent.createChildBuilder(), null);
        }

        @Override // androidx.slice.builders.impl.ListBuilder.RowBuilder
        public void addEndItem(SliceAction action, boolean isLoading) {
        }

        @Override // androidx.slice.builders.impl.ListBuilder.RowBuilder
        public void setTitleItem(IconCompat icon, int imageMode, boolean isLoading) {
        }

        @Override // androidx.slice.builders.impl.ListBuilder.RowBuilder
        public void setPrimaryAction(SliceAction action) {
        }

        @Override // androidx.slice.builders.impl.ListBuilder.RowBuilder
        public void setTitle(CharSequence title) {
        }

        @Override // androidx.slice.builders.impl.ListBuilder.RowBuilder
        public void setSubtitle(CharSequence subtitle, boolean isLoading) {
        }

        @Override // androidx.slice.builders.impl.TemplateBuilderImpl
        public void apply(Slice.Builder builder) {
        }
    }
}

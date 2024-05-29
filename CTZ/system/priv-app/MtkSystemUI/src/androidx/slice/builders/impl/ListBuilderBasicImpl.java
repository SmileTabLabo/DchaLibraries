package androidx.slice.builders.impl;

import android.net.Uri;
import android.support.v4.graphics.drawable.IconCompat;
import androidx.slice.Slice;
import androidx.slice.SliceSpec;
import androidx.slice.builders.SliceAction;
import androidx.slice.builders.impl.ListBuilder;
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
    public TemplateBuilderImpl createRowBuilder(Uri uri) {
        return new RowBuilderImpl(uri);
    }

    @Override // androidx.slice.builders.impl.TemplateBuilderImpl
    public void apply(Slice.Builder builder) {
        if (this.mIsError) {
            builder.addHints("error");
        }
    }

    /* loaded from: classes.dex */
    public static class RowBuilderImpl extends TemplateBuilderImpl implements ListBuilder.RowBuilder {
        public RowBuilderImpl(Uri uri) {
            super(new Slice.Builder(uri), null);
        }

        @Override // androidx.slice.builders.impl.ListBuilder.RowBuilder
        public void setContentDescription(CharSequence description) {
        }

        @Override // androidx.slice.builders.impl.ListBuilder.RowBuilder
        public void setPrimaryAction(SliceAction action) {
        }

        @Override // androidx.slice.builders.impl.ListBuilder.RowBuilder
        public void setTitle(CharSequence title) {
        }

        @Override // androidx.slice.builders.impl.ListBuilder.RowBuilder
        public void addEndItem(IconCompat icon, int imageMode, boolean isLoading) {
        }

        @Override // androidx.slice.builders.impl.TemplateBuilderImpl
        public void apply(Slice.Builder builder) {
        }
    }
}

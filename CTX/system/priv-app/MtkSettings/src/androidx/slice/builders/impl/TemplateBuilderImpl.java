package androidx.slice.builders.impl;

import androidx.slice.Clock;
import androidx.slice.Slice;
import androidx.slice.SliceSpec;
import androidx.slice.SystemClock;
/* loaded from: classes.dex */
public abstract class TemplateBuilderImpl {
    private Clock mClock;
    private final Slice.Builder mSliceBuilder;
    private final SliceSpec mSpec;

    public abstract void apply(Slice.Builder builder);

    /* JADX INFO: Access modifiers changed from: protected */
    public TemplateBuilderImpl(Slice.Builder b, SliceSpec spec) {
        this(b, spec, new SystemClock());
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public TemplateBuilderImpl(Slice.Builder b, SliceSpec spec, Clock clock) {
        this.mSliceBuilder = b;
        this.mSpec = spec;
        this.mClock = clock;
    }

    public Slice build() {
        this.mSliceBuilder.setSpec(this.mSpec);
        apply(this.mSliceBuilder);
        return this.mSliceBuilder.build();
    }

    public Slice.Builder getBuilder() {
        return this.mSliceBuilder;
    }

    public Slice.Builder createChildBuilder() {
        return new Slice.Builder(this.mSliceBuilder);
    }

    public Clock getClock() {
        return this.mClock;
    }
}

package com.android.quicksearchbox.util;
/* loaded from: a.zip:com/android/quicksearchbox/util/NowOrLaterWrapper.class */
public abstract class NowOrLaterWrapper<A, B> implements NowOrLater<B> {
    private final NowOrLater<A> mWrapped;

    public NowOrLaterWrapper(NowOrLater<A> nowOrLater) {
        this.mWrapped = nowOrLater;
    }

    public abstract B get(A a);

    @Override // com.android.quicksearchbox.util.NowOrLater
    public void getLater(Consumer<? super B> consumer) {
        this.mWrapped.getLater(new Consumer<A>(this, consumer) { // from class: com.android.quicksearchbox.util.NowOrLaterWrapper.1
            final NowOrLaterWrapper this$0;
            final Consumer val$consumer;

            {
                this.this$0 = this;
                this.val$consumer = consumer;
            }

            /* JADX WARN: Multi-variable type inference failed */
            @Override // com.android.quicksearchbox.util.Consumer
            public boolean consume(A a) {
                return this.val$consumer.consume(this.this$0.get(a));
            }
        });
    }

    @Override // com.android.quicksearchbox.util.NowOrLater
    public B getNow() {
        return get(this.mWrapped.getNow());
    }

    @Override // com.android.quicksearchbox.util.NowOrLater
    public boolean haveNow() {
        return this.mWrapped.haveNow();
    }
}

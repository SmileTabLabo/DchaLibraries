package com.android.quicksearchbox.util;

import android.os.Handler;
/* loaded from: a.zip:com/android/quicksearchbox/util/Consumers.class */
public class Consumers {
    private Consumers() {
    }

    public static <A extends QuietlyCloseable> void consumeCloseable(Consumer<A> consumer, A a) {
        try {
            if (consumer.consume(a) || a == null) {
                return;
            }
            a.close();
        } catch (Throwable th) {
            if (0 == 0 && a != null) {
                a.close();
            }
            throw th;
        }
    }

    public static <A extends QuietlyCloseable> void consumeCloseableAsync(Handler handler, Consumer<A> consumer, A a) {
        if (handler == null) {
            consumeCloseable(consumer, a);
        } else {
            handler.post(new Runnable(consumer, a) { // from class: com.android.quicksearchbox.util.Consumers.2
                final Consumer val$consumer;
                final QuietlyCloseable val$value;

                {
                    this.val$consumer = consumer;
                    this.val$value = a;
                }

                @Override // java.lang.Runnable
                public void run() {
                    Consumers.consumeCloseable(this.val$consumer, this.val$value);
                }
            });
        }
    }
}

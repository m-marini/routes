/*
 *
 * Copyright (c) 2021 Marco Marini, marco.marini@mmarini.org
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 *    END OF TERMS AND CONDITIONS
 *
 */

package org.mmarini.routes.swing;

import io.reactivex.rxjava3.core.Flowable;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

/**
 * @param <T>
 */
public class SwingObserveOn<T> extends Flowable<T> {
    private static final Logger logger = LoggerFactory.getLogger(SwingObserveOn.class);
    private final Flowable<T> source;

    /**
     * @param source
     */
    protected SwingObserveOn(final Flowable<T> source) {
        super();
        logger.debug("SwingObserveOn created");
        this.source = source;
    }

    @Override
    protected void subscribeActual(final Subscriber<? super T> s) {
        source.subscribe(new ObserveOnSubscriber<>(s));
    }

    static final class ObserveOnSubscriber<T> implements Subscriber<T>, Subscription {
        private final Subscriber<? super T> subscriber;
        volatile boolean disposed;
        private Subscription subscription;

        public ObserveOnSubscriber(final Subscriber<? super T> subscriber) {
            this.subscriber = subscriber;
        }

        @Override
        public void cancel() {
            disposed = true;
            subscription.cancel();
        }

        @Override
        public void onComplete() {
            EventQueue.invokeLater(() -> {
                if (!disposed) {
                    subscriber.onComplete();
                }
            });
        }

        @Override
        public void onError(final Throwable t) {
            EventQueue.invokeLater(() -> {
                if (!disposed) {
                    subscriber.onError(t);
                }
            });
        }

        @Override
        public void onNext(final T t) {
            EventQueue.invokeLater(() -> {
                if (!disposed) {
                    subscriber.onNext(t);
                }
            });
        }

        @Override
        public void onSubscribe(final Subscription s) {
            this.subscription = s;
            subscriber.onSubscribe(this);
        }

        @Override
        public void request(final long n) {
            subscription.request(n);
        }
    }
}

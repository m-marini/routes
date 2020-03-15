package org.mmarini.routes.swing.v2;

import java.awt.EventQueue;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.rxjava3.core.Flowable;

public class SwingObserveOn<T> extends Flowable<T> {
	static final class ObserveOnSubscriber<T> implements Subscriber<T>, Subscription {
		private final Subscriber<? super T> subscriber;
		private Subscription subscription;
		volatile boolean disposed;

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
}

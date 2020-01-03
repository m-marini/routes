package org.mmarini.routes.swing.v2;

import static java.awt.event.MouseEvent.MOUSE_CLICKED;
import static java.awt.event.MouseEvent.MOUSE_DRAGGED;
import static java.awt.event.MouseEvent.MOUSE_ENTERED;
import static java.awt.event.MouseEvent.MOUSE_EXITED;
import static java.awt.event.MouseEvent.MOUSE_MOVED;
import static java.awt.event.MouseEvent.MOUSE_WHEEL;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Predicate;
import io.reactivex.rxjava3.functions.Supplier;

public class RxUtils {

	static class WithMouseObservable extends WithObservable<MouseEvent> {
		/**
		 * @param obs
		 */
		protected WithMouseObservable(final Observable<MouseEvent> obs) {
			super(obs);
		}

		public WithMouseObservable click() {
			return withType(MOUSE_CLICKED);
		}

		public WithMouseObservable drag() {
			return withType(MOUSE_DRAGGED);
		}

		public WithMouseObservable enter() {
			return withType(MOUSE_ENTERED);
		}

		public WithMouseObservable exit() {
			return withType(MOUSE_EXITED);
		}

		public WithMouseObservable move() {
			return withType(MOUSE_MOVED);
		}

		public Observable<Point> toPoint() {
			return observable.map(MouseEvent::getPoint);
		}

		public Observable<Point2D> toPoint2D() {
			return toPoint().map(pt -> (Point2D) pt);
		}

		public WithMouseObservable wheel() {
			return withType(MOUSE_WHEEL);
		}

		public WithMouseObservable withFilter(final Predicate<? super MouseEvent> p) {
			return new WithMouseObservable(observable.filter(p));
		}

		public WithPoint2DObservable withPoint() {
			return withPointObs(toPoint2D());
		}

		public WithMouseObservable withType(final int type) {
			return withFilter(ev -> ev.getID() == type);
		}

	}

	static abstract class WithObservable<T> {
		protected final Observable<T> observable;

		/**
		 * @param obs
		 */
		protected WithObservable(final Observable<T> obs) {
			super();
			this.observable = obs;
		}

		public Observable<T> observable() {
			return observable;
		}
	}

	static class WithPoint2DObservable extends WithObservable<Point2D> {

		protected WithPoint2DObservable(final Observable<Point2D> obs) {
			super(obs);
		}

		public WithPoint2DObservable doOnNext(final Consumer<? super Point2D> c) {
			return new WithPoint2DObservable(observable.doOnNext(c));
		}

		public Observable<Point> toPoint() {
			return observable.map(pt -> new Point((int) Math.round(pt.getX()), (int) Math.round(pt.getY())));
		}

		public WithPoint2DObservable transform(final Supplier<AffineTransform> transform) {
			return new WithPoint2DObservable(observable.map(pt -> transform.get().transform(pt, new Point2D.Double())));
		}

		public WithPoint2DObservable withFilter(final Predicate<Point2D> p) {
			return new WithPoint2DObservable(observable.filter(p));
		}
	}

	public static WithMouseObservable withMouseObs(final Observable<MouseEvent> obs) {
		return new WithMouseObservable(obs);
	}

	public static WithPoint2DObservable withPointObs(final Observable<Point2D> obs) {
		return new WithPoint2DObservable(obs);
	}
}

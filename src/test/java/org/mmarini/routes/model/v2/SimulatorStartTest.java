package org.mmarini.routes.model.v2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mmarini.routes.model.v2.Simulator.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.subscribers.TestSubscriber;

/**
 * Test build status at cross proximity
 */
public class SimulatorStartTest {
	private static final Logger logger = LoggerFactory.getLogger(SimulatorStartTest.class);

	private Simulator<Double> sim;
	private TestSubscriber<Double> eventSubscriber;

	/**
	 * @return
	 */
	@BeforeEach
	void createSimulator() {
		sim = Simulator.create((event, time) -> {
			final double t = time;
			logger.debug("new event {} = {} + {}", t, event, t);
			return t;
		}, Functions.identity());
		eventSubscriber = TestSubscriber.create();
		sim.getEvents().subscribe(eventSubscriber);

		sim.setEvent(0.0).start();

		eventSubscriber.awaitCount(2);
		assertThat(eventSubscriber.values(), hasSize(greaterThanOrEqualTo(2)));
		assertThat(eventSubscriber.values().get(0), equalTo(0.0));
		assertThat(sim.getStatus(), equalTo(Status.ACTIVE));
	}

	@Test
	public void request() throws InterruptedException {
		sim.request(ev -> 100.0);

		eventSubscriber.await(100, TimeUnit.MILLISECONDS);
		final int n = eventSubscriber.values().size();
		assertThat(eventSubscriber.values().get(n - 1), greaterThanOrEqualTo(100.0));

		assertThat(sim.getStatus(), equalTo(Status.ACTIVE));

		eventSubscriber.await(100, TimeUnit.MILLISECONDS);
		assertThat(eventSubscriber.values(), hasSize(greaterThan((n))));

		eventSubscriber.assertNoErrors();
		eventSubscriber.assertNotComplete();
	}

	@Test
	void setEvent() throws InterruptedException {
		sim.setEvent(100.0);

		eventSubscriber.await(100, TimeUnit.MILLISECONDS);
		final int n = eventSubscriber.values().size();

		eventSubscriber.awaitCount(n + 100);
		assertThat(eventSubscriber.values(), hasSize(greaterThanOrEqualTo(n + 100)));
		assertThat(eventSubscriber.values().get(n + 100 - 1), greaterThanOrEqualTo(100.0));

		eventSubscriber.assertNoErrors();
		eventSubscriber.assertNotComplete();
	}

	@Test
	void setSpeed() throws InterruptedException {
		sim.setSpeed(2);

		eventSubscriber.await(100, TimeUnit.MILLISECONDS);
		final int n = eventSubscriber.values().size();

		assertThat(sim.getSpeed(), equalTo(2.0));

		eventSubscriber.await(100, TimeUnit.MILLISECONDS);
		assertThat(eventSubscriber.values(), hasSize(greaterThan(n)));

		eventSubscriber.assertNoErrors();
		eventSubscriber.assertNotComplete();
	}

	@Test
	public void start() throws InterruptedException {
		sim.setEvent(0.0);
		sim.start();

		eventSubscriber.awaitCount(10);
		assertThat(eventSubscriber.values(), hasSize(greaterThanOrEqualTo(10)));
		assertThat(eventSubscriber.values().get(9), greaterThanOrEqualTo(0.0));
		assertThat(sim.getStatus(), equalTo(Status.ACTIVE));

		eventSubscriber.assertNoErrors();
		eventSubscriber.assertNotComplete();
	}

	@Test
	public void stop() throws InterruptedException {
		sim.stop();

		eventSubscriber.await(100, TimeUnit.MILLISECONDS);
		final int n = eventSubscriber.values().size();

		assertThat(sim.getStatus(), equalTo(Status.IDLE));

		eventSubscriber.await(100, TimeUnit.MILLISECONDS);
		assertThat(eventSubscriber.values(), hasSize(equalTo(n)));

		eventSubscriber.assertNoErrors();
		eventSubscriber.assertNotComplete();
	}

}

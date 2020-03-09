package org.mmarini.routes.model.v2;

import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.Scheduler.Worker;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class FlowableTest {

	private static final Logger logger = LoggerFactory.getLogger(FlowableTest.class);

	@Test
	public void test() throws InterruptedException {
		final Scheduler sc = Schedulers.newThread();
		final Worker wk = sc.createWorker();
		IntStream.rangeClosed(1, 10).forEach(i -> {
			logger.debug("Scheduled {}", i);
			wk.schedule(() -> {
				try {
					logger.debug("Started {}", i);
					Thread.sleep(100);
					logger.debug("Ended {}", i);
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}
			});
		});
		Thread.sleep(2000);
	}
}

/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

import javax.cache.expiry.Duration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test class of {@link CronExpiryPolicy}
 */
class CronExpiryPolicyTest {

	private static final ZoneId PARIS = ZoneId.of("Europe/Paris");

	/**
	 * Every day at 03:00.
	 */
	private static final String NIGHTLY = "0 0 3 * * *";

	private static CronExpiryPolicy policy(final String expression, final String now) {
		return new CronExpiryPolicy(expression, Clock.fixed(Instant.parse(now), PARIS));
	}

	/**
	 * At 09:00 in Paris, an entry expires at 03:00 the next day, 18 hours later.
	 */
	@Test
	void getExpiryForCreation() {
		final var duration = policy(NIGHTLY, "2026-11-10T08:00:00Z").getExpiryForCreation();
		Assertions.assertEquals(TimeUnit.MILLISECONDS, duration.getTimeUnit());
		Assertions.assertEquals(18 * 3600 * 1000L, duration.getDurationAmount());
	}

	@Test
	void getExpiryForUpdate() {
		Assertions.assertEquals(18 * 3600 * 1000L, policy(NIGHTLY, "2026-11-10T08:00:00Z").getExpiryForUpdate().getDurationAmount());
	}

	/**
	 * An access does not change the expiry.
	 */
	@Test
	void getExpiryForAccess() {
		Assertions.assertNull(policy(NIGHTLY, "2026-11-10T08:00:00Z").getExpiryForAccess());
	}

	/**
	 * Just before the time, the entry still lives at least one millisecond.
	 */
	@Test
	void getExpiryJustBefore() {
		Assertions.assertEquals(1, policy(NIGHTLY, "2026-11-11T01:59:59.999900Z").getExpiryForCreation().getDurationAmount());
	}

	/**
	 * An expression without any next time, such as February 30th: the entry never expires.
	 */
	@Test
	void getExpiryNever() {
		final var policy = policy("0 0 0 30 2 *", "2026-11-10T08:00:00Z");
		Assertions.assertEquals(Duration.ETERNAL, policy.getExpiryForCreation());
		Assertions.assertEquals(Instant.MAX, policy.next(Instant.parse("2026-11-10T08:00:00Z")));
	}

	/**
	 * The next time is strictly after the given one, in the time zone of the clock.
	 */
	@Test
	void next() {
		final var policy = policy(NIGHTLY, "2026-11-10T08:00:00Z");
		Assertions.assertEquals(Instant.parse("2026-11-11T02:00:00Z"), policy.next(Instant.parse("2026-11-10T08:00:00Z")));
		Assertions.assertEquals(Instant.parse("2026-11-12T02:00:00Z"), policy.next(Instant.parse("2026-11-11T02:00:00Z")));
	}

	/**
	 * The time zone of the times, for a scheduler running at the same times.
	 */
	@Test
	void getZone() {
		Assertions.assertEquals(PARIS, policy(NIGHTLY, "2026-11-10T08:00:00Z").getZone());
		Assertions.assertEquals(ZoneId.systemDefault(), new CronExpiryPolicy(NIGHTLY).getZone());
	}

	@Test
	void invalid() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> new CronExpiryPolicy("every night"));
	}

	/**
	 * Hazelcast serializes the cache configuration: the policy survives it.
	 */
	@Test
	void serializable() throws IOException, ClassNotFoundException {
		final var bytes = new ByteArrayOutputStream();
		try (var out = new ObjectOutputStream(bytes)) {
			out.writeObject(policy(NIGHTLY, "2026-11-10T08:00:00Z"));
		}
		try (var in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
			final var policy = (CronExpiryPolicy) in.readObject();
			Assertions.assertEquals(NIGHTLY, policy.getExpression());
			Assertions.assertEquals(18 * 3600 * 1000L, policy.getExpiryForCreation().getDurationAmount());
		}
	}

	@Test
	void factoryOf() {
		final var policy = (CronExpiryPolicy) CronExpiryPolicy.factoryOf(NIGHTLY).create();
		Assertions.assertEquals(NIGHTLY, policy.getExpression());
		Assertions.assertTrue(policy.getExpiryForCreation().getDurationAmount() > 0);
	}
}

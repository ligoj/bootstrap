/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.cache;

import java.io.Serial;
import java.io.Serializable;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

import javax.cache.configuration.Factory;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ExpiryPolicy;

import org.springframework.scheduling.support.CronExpression;

import lombok.Getter;

/**
 * Expiry policy of the cache entries expiring at fixed times, given by a Spring CRON expression such as
 * {@code 0 0 3 * * *}, every day at 03:00, in the time zone of the JVM. An entry expires at the first time after its
 * creation or its last update; an access does not change it.
 */
public class CronExpiryPolicy implements ExpiryPolicy, Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * The CRON expression.
	 */
	@Getter
	private final String expression;

	/**
	 * The clock giving the current time and the time zone.
	 */
	private final Clock clock;

	/**
	 * The parsed expression, parsed again after a deserialization.
	 */
	private transient volatile CronExpression cron;

	/**
	 * Policy in the time zone of the JVM.
	 *
	 * @param expression The CRON expression.
	 * @throws IllegalArgumentException When the expression is invalid.
	 */
	public CronExpiryPolicy(final String expression) {
		this(expression, Clock.systemDefaultZone());
	}

	/**
	 * Policy with a clock.
	 *
	 * @param expression The CRON expression.
	 * @param clock      The clock giving the current time and the time zone.
	 * @throws IllegalArgumentException When the expression is invalid.
	 */
	CronExpiryPolicy(final String expression, final Clock clock) {
		this.cron = CronExpression.parse(expression);
		this.expression = expression;
		this.clock = clock;
	}

	/**
	 * Return a factory of this policy, for a cache configuration.
	 *
	 * @param expression The CRON expression.
	 * @return The factory of the policy.
	 * @throws IllegalArgumentException When the expression is invalid.
	 */
	public static Factory<ExpiryPolicy> factoryOf(final String expression) {
		return new FactoryBuilder.SingletonFactory<>(new CronExpiryPolicy(expression));
	}

	/**
	 * Return the first time of the expression strictly after the given one.
	 *
	 * @param after The reference time.
	 * @return The next time, {@link Instant#MAX} when there is none.
	 */
	public Instant next(final Instant after) {
		final var next = getCron().next(after.atZone(clock.getZone()));
		return next == null ? Instant.MAX : next.toInstant();
	}

	/**
	 * Return the time zone of the times, for a scheduler running at the same times.
	 *
	 * @return The time zone.
	 */
	public ZoneId getZone() {
		return clock.getZone();
	}

	@Override
	public Duration getExpiryForCreation() {
		return untilNext();
	}

	@Override
	public Duration getExpiryForAccess() {
		// Unchanged
		return null;
	}

	@Override
	public Duration getExpiryForUpdate() {
		return untilNext();
	}

	/**
	 * Return the duration until the next time, at least one millisecond.
	 */
	private Duration untilNext() {
		final var now = clock.instant();
		final var next = next(now);
		if (next.equals(Instant.MAX)) {
			return Duration.ETERNAL;
		}
		return new Duration(TimeUnit.MILLISECONDS, Math.max(1, java.time.Duration.between(now, next).toMillis()));
	}

	private CronExpression getCron() {
		if (cron == null) {
			cron = CronExpression.parse(expression);
		}
		return cron;
	}
}

package org.ligoj.bootstrap.core.template;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.lang3.time.DateUtils;

/**
 * Decimal date.
 * 
 * @see "http://excelsemipro.com/2010/08/date-and-time-calculation-in-excel/"
 * @see "https://www.fourmilab.ch/documents/calendar/"
 */
public class DecimalDateProcessor extends Processor<String> {

	/**
	 * Empty, no context data constructor.
	 */
	public DecimalDateProcessor() {
		this(null);
	}

	/**
	 * Data constructor.
	 * 
	 * @param data
	 *            the context data or another {@link Processor} instance.
	 */
	public DecimalDateProcessor(final Object data) {
		super(data);
	}

	@Override
	public Date getValue(final String context) {
		final String data = (String) super.getValue(context);

		try {
			final double date = Double.parseDouble(data.replace(',', '.'));
			final Calendar calendar = new GregorianCalendar(); // using default time-zone
			final int wholeDays = (int) Math.floor(date);
			final int millisecondsInDay = (int) Math.round((date - wholeDays) * DateUtils.MILLIS_PER_DAY);

			// Excel thinks 2/29/1900 is a valid date, which it isn't
			calendar.set(1900, 0, wholeDays - 1, 0, 0, 0);
			calendar.set(Calendar.MILLISECOND, millisecondsInDay);
			calendar.add(Calendar.MILLISECOND, 500);
			calendar.clear(Calendar.MILLISECOND);
			return calendar.getTime();
		} catch (final NumberFormatException e) {
			// Invalid format of String
			throw new IllegalArgumentException("Invalid string '" + data + "' for decimal Excel date", e);
		}
	}

}

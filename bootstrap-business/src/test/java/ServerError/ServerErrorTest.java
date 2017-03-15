package ServerError;

import org.junit.Assert;
import org.junit.Test;
import org.ligoj.bootstrap.core.resource.ServerError;
import org.ligoj.bootstrap.core.resource.TechnicalException;

/**
 * Test class of {@link ServerError}
 */
public class ServerErrorTest {

	@Test
	public void throwableNull() {
		final ServerError serverError = new ServerError();
		serverError.setThrowable(new NullPointerException());
		Assert.assertNull(serverError.getCause());
		Assert.assertNull(serverError.getMessage());
	}

	@Test
	public void throwableNullMessage() {
		final ServerError serverError = new ServerError();
		serverError.setThrowable(new TechnicalException("message", new NullPointerException()));
		Assert.assertNull(serverError.getCause());
		Assert.assertEquals("message", serverError.getMessage());
	}

	@Test
	public void throwableCauseMessage() {
		final ServerError serverError = new ServerError();
		final TechnicalException exception = new TechnicalException("message2");
		serverError.setThrowable(new TechnicalException("message1", exception));
		Assert.assertNull(serverError.getCause().getCause());
		Assert.assertEquals("message2", serverError.getCause().getMessage());
		Assert.assertEquals("message1", serverError.getMessage());
	}
}

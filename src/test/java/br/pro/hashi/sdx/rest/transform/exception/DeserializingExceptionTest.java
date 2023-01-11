package br.pro.hashi.sdx.rest.transform.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class DeserializingExceptionTest {
	private DeserializingException e;

	@Test
	void constructsWithNoArgs() {
		e = new DeserializingException();
		assertNull(e.getMessage());
		assertNull(e.getCause());
	}

	@Test
	void constructsWithMessage() {
		e = new DeserializingException("message");
		assertEquals("message", e.getMessage());
		assertNull(e.getCause());
	}

	@Test
	void constructsWithMessageAndCause() {
		Throwable cause = new Throwable();
		e = new DeserializingException("message", cause);
		assertEquals("message", e.getMessage());
		assertSame(cause, e.getCause());
	}

	@Test
	void constructsWithCause() {
		Throwable cause = new Throwable();
		e = new DeserializingException(cause);
		assertEquals(cause.toString(), e.getMessage());
		assertSame(cause, e.getCause());
	}
}

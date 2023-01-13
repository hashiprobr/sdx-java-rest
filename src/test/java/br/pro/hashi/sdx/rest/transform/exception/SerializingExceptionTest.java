package br.pro.hashi.sdx.rest.transform.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class SerializingExceptionTest {
	private SerializingException e;

	@Test
	void constructsWithNoArgs() {
		e = new SerializingException();
		assertNull(e.getMessage());
		assertNull(e.getCause());
	}

	@Test
	void constructsWithMessage() {
		e = new SerializingException("message");
		assertEquals("message", e.getMessage());
		assertNull(e.getCause());
	}

	@Test
	void constructsWithMessageAndCause() {
		Throwable cause = new Throwable();
		e = new SerializingException("message", cause);
		assertEquals("message", e.getMessage());
		assertSame(cause, e.getCause());
	}

	@Test
	void constructsWithCause() {
		Throwable cause = new Throwable();
		e = new SerializingException(cause);
		assertEquals(cause.toString(), e.getMessage());
		assertSame(cause, e.getCause());
	}
}

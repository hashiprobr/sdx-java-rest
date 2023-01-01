package br.pro.hashi.sdx.rest.transformer.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class DeserializingExceptionTest {
	private DeserializingException e;

	@Test
	void noArgsConstructor() {
		e = new DeserializingException();
		assertNull(e.getMessage());
		assertNull(e.getCause());
	}

	@Test
	void messageConstructor() {
		e = new DeserializingException("message");
		assertEquals("message", e.getMessage());
		assertNull(e.getCause());
	}

	@Test
	void messageAndCauseConstructor() {
		Throwable cause = new Throwable();
		e = new DeserializingException("message", cause);
		assertEquals("message", e.getMessage());
		assertSame(cause, e.getCause());
	}

	@Test
	void causeConstructor() {
		Throwable cause = new Throwable();
		e = new DeserializingException(cause);
		assertEquals(cause.toString(), e.getMessage());
		assertSame(cause, e.getCause());
	}
}

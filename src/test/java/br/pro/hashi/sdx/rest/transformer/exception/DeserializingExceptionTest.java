package br.pro.hashi.sdx.rest.transformer.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class DeserializingExceptionTest {
	private static final String MESSAGE = "message";

	private DeserializingException e;

	@Test
	void noArgsConstructor() {
		e = new DeserializingException();
		assertNull(e.getMessage());
		assertNull(e.getCause());
	}

	@Test
	void messageConstructor() {
		e = new DeserializingException(MESSAGE);
		assertEquals(MESSAGE, e.getMessage());
		assertNull(e.getCause());
	}

	@Test
	void messageAndCauseConstructor() {
		Throwable t = new Throwable();
		e = new DeserializingException(MESSAGE, t);
		assertEquals(MESSAGE, e.getMessage());
		assertSame(t, e.getCause());
	}

	@Test
	void causeConstructor() {
		Throwable t = new Throwable();
		e = new DeserializingException(t);
		assertEquals(t.toString(), e.getMessage());
		assertSame(t, e.getCause());
	}
}

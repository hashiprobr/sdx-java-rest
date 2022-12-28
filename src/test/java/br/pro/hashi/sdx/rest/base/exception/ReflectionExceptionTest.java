package br.pro.hashi.sdx.rest.base.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class ReflectionExceptionTest {
	private static final String MESSAGE = "message";

	private ReflectionException e;

	@Test
	void messageConstructor() {
		e = new ReflectionException(MESSAGE);
		assertEquals(MESSAGE, e.getMessage());
		assertNull(e.getCause());
	}

	@Test
	void causeConstructor() {
		Throwable cause = new Throwable();
		e = new ReflectionException(cause);
		assertEquals(cause.toString(), e.getMessage());
		assertSame(cause, e.getCause());
	}
}

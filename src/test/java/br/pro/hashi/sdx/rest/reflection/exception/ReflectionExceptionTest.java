package br.pro.hashi.sdx.rest.reflection.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class ReflectionExceptionTest {
	private ReflectionException e;

	@Test
	void constructsWithMessage() {
		e = new ReflectionException("message");
		assertEquals("message", e.getMessage());
		assertNull(e.getCause());
	}

	@Test
	void constructsWithCause() {
		Throwable cause = new Throwable();
		e = new ReflectionException(cause);
		assertEquals(cause.toString(), e.getMessage());
		assertSame(cause, e.getCause());
	}
}

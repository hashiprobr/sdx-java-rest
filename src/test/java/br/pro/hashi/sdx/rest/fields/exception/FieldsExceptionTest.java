package br.pro.hashi.sdx.rest.fields.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class FieldsExceptionTest {
	private FieldsException e;

	@Test
	void constructsWithMessage() {
		e = new FieldsException("message");
		assertEquals("message", e.getMessage());
		assertNull(e.getCause());
	}

	@Test
	void constructsWithCause() {
		Throwable cause = new Throwable();
		e = new FieldsException(cause);
		assertEquals(cause.toString(), e.getMessage());
		assertSame(cause, e.getCause());
	}
}

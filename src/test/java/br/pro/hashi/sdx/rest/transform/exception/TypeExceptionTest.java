package br.pro.hashi.sdx.rest.transform.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class TypeExceptionTest {
	private TypeException e;

	@Test
	void constructsWithNoArgs() {
		e = new TypeException();
		assertNull(e.getMessage());
		assertNull(e.getCause());
	}

	@Test
	void constructsWithMessage() {
		String message = "message";
		e = new TypeException(message);
		assertEquals(message, e.getMessage());
		assertNull(e.getCause());
	}

	@Test
	void constructsWithMessageAndCause() {
		String message = "message";
		Throwable cause = new Throwable();
		e = new TypeException(message, cause);
		assertEquals(message, e.getMessage());
		assertSame(cause, e.getCause());
	}

	@Test
	void constructsWithCause() {
		Throwable cause = new Throwable();
		e = new TypeException(cause);
		assertEquals(cause.toString(), e.getMessage());
		assertSame(cause, e.getCause());
	}
}

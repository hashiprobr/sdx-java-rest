package br.pro.hashi.sdx.rest.coding.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class CharsetExceptionTest {
	private static final String MESSAGE = "message";

	private CharsetException e;

	@Test
	void messageConstructor() {
		e = new CharsetException(MESSAGE);
		assertEquals(MESSAGE, e.getMessage());
		assertNull(e.getCause());
	}
}

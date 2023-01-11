package br.pro.hashi.sdx.rest.coding.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class CharsetExceptionTest {
	private CharsetException e;

	@Test
	void constructsWithMessage() {
		e = new CharsetException("message");
		assertEquals("message", e.getMessage());
		assertNull(e.getCause());
	}
}

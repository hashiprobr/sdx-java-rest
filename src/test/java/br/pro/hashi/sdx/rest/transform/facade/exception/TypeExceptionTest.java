package br.pro.hashi.sdx.rest.transform.facade.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class TypeExceptionTest {
	private SupportException e;

	@Test
	void constructsWithMessage() {
		e = new SupportException("message");
		assertEquals("message", e.getMessage());
		assertNull(e.getCause());
	}
}

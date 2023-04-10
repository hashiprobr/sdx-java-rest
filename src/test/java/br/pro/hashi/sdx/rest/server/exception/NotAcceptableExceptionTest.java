package br.pro.hashi.sdx.rest.server.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class NotAcceptableExceptionTest {
	private NotAcceptableException e;

	@Test
	void constructs() {
		e = new NotAcceptableException("message");
		assertNull(e.getMessage());
		assertNull(e.getCause());
		assertEquals(406, e.getStatus());
		assertEquals("message", e.getBody());
		assertEquals(String.class, e.getType());
	}
}

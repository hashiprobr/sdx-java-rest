package br.pro.hashi.sdx.rest.server.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class PayloadTooLargeExceptionTest {
	private PayloadTooLargeException e;

	@Test
	void constructs() {
		e = new PayloadTooLargeException("message");
		assertNull(e.getMessage());
		assertNull(e.getCause());
		assertEquals(413, e.getStatus());
		assertEquals("message", e.getBody());
		assertEquals(String.class, e.getType());
	}
}

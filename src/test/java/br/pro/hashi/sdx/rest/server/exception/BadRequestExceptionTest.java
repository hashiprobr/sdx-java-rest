package br.pro.hashi.sdx.rest.server.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class BadRequestExceptionTest {
	private BadRequestException e;

	@Test
	void constructs() {
		e = new BadRequestException("message");
		assertEquals(400, e.getStatus());
		assertEquals("message", e.getMessage());
		assertNull(e.getCause());
	}
}

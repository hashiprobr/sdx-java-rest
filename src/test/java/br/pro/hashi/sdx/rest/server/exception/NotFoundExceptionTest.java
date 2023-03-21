package br.pro.hashi.sdx.rest.server.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class NotFoundExceptionTest {
	private NotFoundException e;

	@Test
	void constructs() {
		e = new NotFoundException();
		assertNull(e.getMessage());
		assertNull(e.getCause());
		assertEquals(404, e.getStatus());
		assertEquals("Endpoint not found", e.getBody());
		assertEquals(String.class, e.getType());
	}
}

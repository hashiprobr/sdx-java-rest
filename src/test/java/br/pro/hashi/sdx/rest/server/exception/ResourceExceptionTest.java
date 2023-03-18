package br.pro.hashi.sdx.rest.server.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class ResourceExceptionTest {
	private ResourceException e;

	@Test
	void constructsWithTypeNameAndMessage() {
		e = new ResourceException("Type", "message");
		assertEquals("Type: message", e.getMessage());
		assertNull(e.getCause());
	}
}

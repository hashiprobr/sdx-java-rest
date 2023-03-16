package br.pro.hashi.sdx.rest.server.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ResponseExceptionTest {
	private ResponseException e;

	@Test
	void constructsWithClientError() {
		e = new ResponseException(450, "message");
		assertEquals(450, e.getStatus());
		assertEquals("message", e.getMessage());
		assertNull(e.getCause());
	}

	@Test
	void constructsWithServerError() {
		e = new ResponseException(550, "message");
		assertEquals(550, e.getStatus());
		assertEquals("message", e.getMessage());
		assertNull(e.getCause());
	}

	@Test
	void doesNotConstructWithSmallStatus() {
		assertThrows(IllegalArgumentException.class, () -> {
			new ResponseException(350, "message");
		});
	}

	@Test
	void doesNotConstructWithLargeStatus() {
		assertThrows(IllegalArgumentException.class, () -> {
			new ResponseException(650, "message");
		});
	}
}

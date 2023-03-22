package br.pro.hashi.sdx.rest.server.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class MessageResponseExceptionTest {
	private MessageResponseException e;

	@Test
	void constructsWithClientStatus() {
		e = new MessageResponseException(450, "message");
		assertNull(e.getMessage());
		assertNull(e.getCause());
		assertEquals(450, e.getStatus());
		assertEquals("message", e.getBody());
		assertEquals(String.class, e.getType());
	}

	@Test
	void constructsWithServerStatus() {
		e = new MessageResponseException(550, "message");
		assertNull(e.getMessage());
		assertNull(e.getCause());
		assertEquals(550, e.getStatus());
		assertSame("message", e.getBody());
		assertEquals(String.class, e.getType());
	}

	@Test
	void doesNotConstructWithSmallStatus() {
		Object body = new Object();
		assertThrows(IllegalArgumentException.class, () -> {
			new ResponseException(350, body);
		});
	}

	@Test
	void doesNotConstructWithLargeStatus() {
		Object body = new Object();
		assertThrows(IllegalArgumentException.class, () -> {
			new ResponseException(650, body);
		});
	}
}
